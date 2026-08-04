package service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.RedisPrefixConstant;
import lombok.extern.slf4j.Slf4j;
import mapper.DishMapper;
import model.dto.DishDTO;
import model.entity.DishDetail;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import model.entity.Dish;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import service.DishDetailService;
import service.DishService;
import service.redis.RedisData;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * 菜品 Service（对应 dish 表）
 */
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private DishDetailService dishDetailService;

    private static final Random RANDOM = new Random();
    private static final long RENEW_TTL = 10L;
    private static final long PLUS_TTL = 30L;
    private static final long FINAL_TTL = 86400L;
    private static final ExecutorService DISH_EXECUTOR = new ThreadPoolExecutor(5, 5,
            60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), // 有界队列，防止无限堆积
            r -> {
                Thread t = new Thread (r, "read-dish-handler");
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 队列满了，交给调用线程执行，不丢弃任务
    );

    @Override
    public Dish readCache(Long id) {
        String value;
        try {
            value = stringRedisTemplate.opsForValue().get(RedisPrefixConstant.DISH_PREFIX + id);
        } catch (Exception e) {
            // Redis 不可用：降级直查数据库
            log.info("Redis 宕机，降级查库:{}", e.getMessage());
            return super.getById(id);
        }
        //        1不存在缓存：加锁双重检查，防止并发穿透打库
        if (value == null) {
            log.info("不存在缓存---------");
            return getDishLock(id);
        }
        // 缓存JSON损坏，直接走数据库重建缓存
        RedisData redisData;
        try {
            redisData = JSONUtil.toBean(value, RedisData.class);
        } catch (Exception e) {
            return getDishLock(id);
        }
        //        2缓存存在
        //        2.1逻辑未过期：返回缓存数据；空值缓存直接返回 null（不查库，防穿透）
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())){
            if (redisData.getData() == null) {
                return null;
            }
            long remainSec = Duration.between(LocalDateTime.now(), redisData.getExpireTime()).getSeconds();
            if (remainSec < RENEW_TTL) {
                redisData.setExpireTime(LocalDateTime.now().plusSeconds(PLUS_TTL));
                try {
                    stringRedisTemplate.opsForValue().set(RedisPrefixConstant.DISH_PREFIX + id, JSONUtil.toJsonStr(redisData),
                            FINAL_TTL, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.info("续期写缓存失败:"+ e.getMessage());
                }
            }
            log.info("缓存dish没有过期------------");
            return BeanUtil.toBean(redisData.getData(), Dish.class);
        }
        //        2.2逻辑过期：返回旧数据，异步重建缓存
        try {
            log.info("缓存过期-------------");
            dishCache(id);
        } catch (Exception e) {
            log.info("异步刷新失败:{}", e.getMessage());
        }
        return redisData.getData() == null ? null : BeanUtil.toBean(redisData.getData(), Dish.class);
    }
    /**
     * 避免缓存击穿（多个并发请求同时查库）
     */
    private Dish getDishLock(Long id) {
        RLock lock = redissonClient.getLock("dish:lock:" + id);
        boolean locked = false;
        try {
            locked = lock.tryLock(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.info("Redis 不可用,直接查库:{}", e.getMessage());
            return getDish(id);
        }
        try {
            if (!locked) {
               return null;
            }
            // 双重检查：等待期间其他线程可能已重建缓存
            String latestVal;
            try {
                latestVal = stringRedisTemplate.opsForValue().get(RedisPrefixConstant.DISH_PREFIX + id);
            } catch (Exception e) {
                latestVal = null; // Redis 异常，视为无缓存，走查库重建
            }
            if (latestVal != null) {
                try {
                    RedisData cached = JSONUtil.toBean(latestVal, RedisData.class);
                    if (cached.getExpireTime().isAfter(LocalDateTime.now())) {
                        // 缓存已由其他线程重建且有效，直接返回（含空值缓存）
                        return cached.getData() == null ? null : BeanUtil.toBean(cached.getData(), Dish.class);
                    }
                } catch (Exception ignore) {
                    // 缓存解析失败，忽略，走查库重建
                }
            }
            return getDish(id);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    private Dish getDish(Long id){
        //查询数据库
        Dish dish = super.getById(id);
        RedisData redisData = new RedisData();
        //缓存穿透
        if (dish == null) {
            redisData.setData(null);
            redisData.setExpireTime(LocalDateTime.now().plusSeconds(PLUS_TTL+ (RANDOM.nextInt(-5,5))));
            try {
                stringRedisTemplate.opsForValue().set(RedisPrefixConstant.DISH_PREFIX + id, JSONUtil.toJsonStr(redisData),
                        FINAL_TTL, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.info("空值缓存写入失败{}", e.getMessage());
            }
            return null;
        }
        redisData.setData(dish);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(PLUS_TTL + (RANDOM.nextInt(-5,5))));
        try {
            stringRedisTemplate.opsForValue().set(RedisPrefixConstant.DISH_PREFIX + id, JSONUtil.toJsonStr(redisData),
                    FINAL_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.info("缓存写入失败" + e.getMessage());
        }
        return dish;
    }
    private void dishCache(Long id){
        DISH_EXECUTOR.submit(() -> {
            log.info("缓存过期，DISH_EXECUTOR处理-------------");
            RLock redissonLock = redissonClient.getLock("dish:lock:" + id);
            boolean locked = false;
            try {
                locked = redissonLock.tryLock(10, TimeUnit.SECONDS);
                if (!locked) {
                    return;
                }
                // 双重检查：防止多个异步任务同时进入后重复刷新
                String latestVal = stringRedisTemplate.opsForValue().get(RedisPrefixConstant.DISH_PREFIX + id);
                if (latestVal != null) {
                    RedisData ckerkeData;
                    try {
                        ckerkeData = JSONUtil.toBean(latestVal, RedisData.class);
                    } catch (Exception e) {
                        ckerkeData = null;
                    }
                    // 双重检查：缓存仍有效（含空值缓存）则无需重复刷新
                    if (ckerkeData != null && ckerkeData.getExpireTime().isAfter(LocalDateTime.now())) {
                        return;
                    }
                }
                getDish(id);
            } catch (Exception e) {
               log.info(DISH_EXECUTOR+"处理失败:"+e.getMessage());
            } finally {
                // 同一线程获取和释放锁，watchdog 会随线程退出自动停止续期
                if (locked && redissonLock.isHeldByCurrentThread()) {
                    redissonLock.unlock();
                }
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCache(DishDTO dishDTO) {
        // 1.更新菜品基本信息（update_time/update_user 由填充器自动写入）
        Dish dish = BeanUtil.toBean(dishDTO, Dish.class);
        super.updateById(dish);
        // 2.重建菜品口味：先删除旧口味，再插入新口味
        dishDetailService.remove(new LambdaQueryWrapper<DishDetail>()
                .eq(DishDetail::getDishId, dish.getId()));
        List<DishDetail> dishDetailList = dishDTO.getDishDetails();
        if (dishDetailList != null) {
            dishDetailList.forEach(detail -> detail.setDishId(dish.getId()));
            dishDetailService.saveBatch(dishDetailList);
        }
        // 3.事务提交后再删除缓存：
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    stringRedisTemplate.delete(RedisPrefixConstant.DISH_PREFIX + dish.getId());
                } catch (Exception e) {
                    log.info("缓存删除失败:"+e.getMessage());
                }
            }
        });
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteCache(List<Long> ids) {
        // 1.删除菜品及其口味（dish_detail 通过 dish_id 关联）
        super.removeByIds(ids);
        dishDetailService.remove(new LambdaQueryWrapper<DishDetail>()
                .in(DishDetail::getDishId, ids));
        // 2.删除缓存
        try {
            stringRedisTemplate.delete(ids.stream()
                    .map(id -> RedisPrefixConstant.DISH_PREFIX + id)
                    .toList());
        } catch (Exception e) {
            log.info("缓存删除失败(忽略):{}", e.getMessage());
        }
    }

    @Override
    public void deleteCacheById(Long id) {
        try {
            stringRedisTemplate.delete(RedisPrefixConstant.DISH_PREFIX + id);
        } catch (Exception e) {
            log.info("缓存删除失败" + e.getMessage());
        }
    }
}
