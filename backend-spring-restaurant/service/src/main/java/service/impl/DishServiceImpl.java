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
import service.DishDetailService;
import service.DishService;
import service.redis.RedisData;

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
    private static final long RENEW_THRESHOLD = 10L;
    private static final long PLUS_TTL = 30L;
    private static final long FINAL_TTL = 86400;
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
//        查询缓存
        String value = stringRedisTemplate.opsForValue().get(RedisPrefixConstant.DISH_PREFIX + id);
//        1不存在缓存
        if (value == null) {
            log.info("不存在缓存---------");
            return getDish(id);
        }
//        2缓存存在
        RedisData redisData;
        try {
            redisData = JSONUtil.toBean(value, RedisData.class);
        } catch (Exception e) {
            // 缓存JSON损坏，直接走数据库重建缓存
            return getDish(id);
        }
//        2.2缓存为空
        if (redisData.getData() == null) {
            return getDish(id);
        }
//        2.1缓存不为空
        //        2.2.1缓存没有过期
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())){
            long remainSec = java.time.Duration.between(LocalDateTime.now(), redisData.getExpireTime()).getSeconds();
            // 临近逻辑过期时间（小于阈值），异步续期 RedisData 的 expireTime
            // 注意：Redis 键 TTL 仍为 FINAL_TTL（长 TTL），保证物理不过期
            if (remainSec < RENEW_THRESHOLD) {
                redisData.setExpireTime(LocalDateTime.now().plusSeconds(PLUS_TTL));
                stringRedisTemplate.opsForValue().set(RedisPrefixConstant.DISH_PREFIX + id, JSONUtil.toJsonStr(redisData),
                        FINAL_TTL, TimeUnit.SECONDS);
            }
            log.info("缓存没有过期------------");
            Dish dish = BeanUtil.toBean(redisData.getData(), Dish.class);
            return dish;
        }
        //        2.2.2缓存过期
        try {
            log.info("缓存过期-------------");
            Dish dish = getDishCache(id);
            if (dish == null) {
                dish = BeanUtil.toBean(redisData.getData(), Dish.class);
            }
            return dish;
        } catch (Exception e) {
            //旧数据
            Dish dish = BeanUtil.toBean(redisData.getData(), Dish.class);
            return dish;
        }

    }
    private Dish getDish(Long id){
        //查询数据库
        Dish dish = super.getById(id);
        RedisData redisData = new RedisData();
        if (dish == null) {
            // 缓存空值防止缓存穿透：Redis 键 TTL 用 FINAL_TTL（长 TTL），
            // 逻辑过期时间设短一些（PLUS_TTL），由 readCache 中的逻辑过期判断处理
            redisData.setData(null);
            redisData.setExpireTime(LocalDateTime.now().plusSeconds(PLUS_TTL+ (RANDOM.nextInt(-5,5))));
            stringRedisTemplate.opsForValue().set(RedisPrefixConstant.DISH_PREFIX + id, JSONUtil.toJsonStr(redisData),
                    FINAL_TTL, TimeUnit.SECONDS);
            return null;
        }
        // 写入 RedisData，逻辑过期时间 PLUS_TTL，Redis 键 TTL 用 FINAL_TTL（长 TTL），
        // 实现"逻辑过期 + 异步刷新"缓存模式
        redisData.setData(dish);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(PLUS_TTL + (RANDOM.nextInt(-5,5))));
        stringRedisTemplate.opsForValue().set(RedisPrefixConstant.DISH_PREFIX + id, JSONUtil.toJsonStr(redisData),
                FINAL_TTL, TimeUnit.SECONDS);
        return dish;
    }
    private Dish getDishCache(Long id){
        DISH_EXECUTOR.submit(() -> {
            log.info("缓存过期，DISH_EXECUTOR处理-------------");
            RLock redissonLock = redissonClient.getLock("dish:lock:" + id);
            boolean locked = false;
            try {
                locked = redissonLock.tryLock(0, TimeUnit.SECONDS);
                if (!locked) {
                    log.info("缓存过期，已有其他线程在刷新，跳过");
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
                    if (ckerkeData != null
                            && ckerkeData.getData() != null
                            && ckerkeData.getExpireTime().isAfter(LocalDateTime.now())) {
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
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCache(DishDTO dishDTO) {
        // 1.更新菜品基本信息（update_time/update_user 由填充器自动写入）
        Dish dish = BeanUtil.toBean(dishDTO, Dish.class);
        super.updateById(dish);
        // 2.删除缓存，保证下次读取时重建
        stringRedisTemplate.delete(RedisPrefixConstant.DISH_PREFIX + dish.getId());
        // 3.重建菜品口味：先删除旧口味，再插入新口味
        dishDetailService.remove(new LambdaQueryWrapper<DishDetail>()
                .eq(DishDetail::getDishId, dish.getId()));
        List<DishDetail> dishDetailList = dishDTO.getDishDetails();
        if (dishDetailList != null) {
            dishDetailList.forEach(detail -> detail.setDishId(dish.getId()));
            dishDetailService.saveBatch(dishDetailList);
        }
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteCache(List<Long> ids) {
        // 1.删除菜品及其口味（dish_detail 通过 dish_id 关联）
        super.removeByIds(ids);
        dishDetailService.remove(new LambdaQueryWrapper<DishDetail>()
                .in(DishDetail::getDishId, ids));
        // 2.删除缓存
        for (Long id : ids) {
            stringRedisTemplate.delete(RedisPrefixConstant.DISH_PREFIX + id);
        }
    }
}
