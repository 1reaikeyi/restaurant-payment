package service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.RedisPrefixConstant;
import lombok.extern.slf4j.Slf4j;
import mapper.PlanMapper;
import model.dto.PlanDTO;
import model.entity.Dish;
import model.entity.DishDetail;
import model.entity.PlanDetail;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import model.entity.Plan;
import org.springframework.transaction.annotation.Transactional;
import service.PlanDetailService;
import service.PlanService;
import service.redis.RedisData;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 套餐 Service（对应 plan 表）
 */
@Service
@Slf4j
public class PlanServiceImpl extends ServiceImpl<PlanMapper, Plan> implements PlanService {
    @Autowired
    private PlanDetailService planDetailService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    private static final long FINAL_PLAN_TTL = 86400L;
    private static final long RENEW_TTL = 10L;
    private static final long PLUS_TTL = 30L;
    private static final Random RANDOM = new Random();
    private static final ExecutorService PLAN_EXECUTOR = new ThreadPoolExecutor(5, 5,
            60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), // 有界队列，防止无限堆积
            r -> {
                Thread t = new Thread (r, "read-plan-handler");
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 队列满了，交给调用线程执行，不丢弃任务
    );

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCache(PlanDTO planDTO) {
        Plan plan = BeanUtil.toBean(planDTO, Plan.class);
        super.updateById(plan);
        stringRedisTemplate.delete(RedisPrefixConstant.PLAN_PREFIX + plan.getId());
        planDetailService.remove(new LambdaQueryWrapper<PlanDetail>()
                .in(PlanDetail::getPlanId,plan.getId()));
        List<PlanDetail> planDetailList = planDTO.getPlanDetails();
        if (planDetailList != null) {
            planDetailList.forEach(planDetail -> planDetail.setPlanId(plan.getId()));
            planDetailService.saveBatch(planDetailList);
        }
    }

    @Override
    public void deleteCache(List<Long> ids) {
        super.removeByIds(ids);
        planDetailService.remove(new LambdaQueryWrapper<PlanDetail>()
                .in(PlanDetail::getPlanId, ids));
        stringRedisTemplate.delete(ids.stream()
                .map(id -> RedisPrefixConstant.PLAN_PREFIX + id)
                .toList());
    }

    @Override
    public void deleteCacheById(Long id) {
        stringRedisTemplate.delete(RedisPrefixConstant.PLAN_PREFIX + id);
    }

    @Override
    public Plan readCache(Long id) {
        String value = stringRedisTemplate.opsForValue().get(RedisPrefixConstant.PLAN_PREFIX + id);
        //1.缓存不存在
        if(value == null){
            log.info("plan 缓存不存在");
            Plan plan = getPlan(id);
            return plan;
        }

        RedisData redisData;
        try {
            redisData = JSONUtil.toBean(value, RedisData.class);
        } catch (Exception e) {
            // 缓存JSON损坏，直接走数据库重建缓存
            return getPlan(id);
        }
        //2缓存存在

        //2.1 逻辑未过期：返回缓存数据；空值缓存直接返回 null（不查库，防穿透）
        if(redisData.getExpireTime().isAfter(LocalDateTime.now())){
            if (redisData.getData() == null) {
                return null;
            }
            long remainSec = Duration.between(LocalDateTime.now(), redisData.getExpireTime()).getSeconds();
            if (remainSec < RENEW_TTL){
                redisData.setExpireTime(LocalDateTime.now().plusSeconds(PLUS_TTL));
                stringRedisTemplate.opsForValue().set(RedisPrefixConstant.PLAN_PREFIX + id, JSONUtil.toJsonStr(redisData),
                        FINAL_PLAN_TTL, TimeUnit.SECONDS);
            }
            log.info("plan缓存没有过期------------");
            return BeanUtil.toBean(redisData.getData(), Plan.class);
        }
        //2.2逻辑过期：返回旧数据（空值返回 null），异步重建缓存
        log.info("缓存过期-------------");
        try {
            planCache(id);
        } catch (Exception e) {
            log.info("异步刷新失败:{}", e.getMessage());
        }
        return redisData.getData() == null ? null : BeanUtil.toBean(redisData.getData(), Plan.class);
    }
    public Plan getPlanLock(Long id){
        RLock redissonLock = redissonClient.getLock("plan:lock:" + id);
        boolean locked = false;
        try {
            locked = redissonLock.tryLock(10, TimeUnit.SECONDS);
            if (!locked) {
                return null;
            }
            String latestVal = stringRedisTemplate.opsForValue().get(RedisPrefixConstant.PLAN_PREFIX + id);
            if (latestVal != null) {
                RedisData ckerkeData;
                try {
                    ckerkeData = JSONUtil.toBean(latestVal, RedisData.class);
                } catch (Exception e) {
                    ckerkeData = null;
                }
                // 双重检查：缓存仍有效（含空值缓存）则无需重复刷新
                if (ckerkeData != null && ckerkeData.getExpireTime().isAfter(LocalDateTime.now())) {
                    return null;
                }
            }
            getPlan(id);
        } catch (Exception e) {
            log.info(PLAN_EXECUTOR+"处理失败:"+e.getMessage());
        } finally {
            // 同一线程获取和释放锁，watchdog 会随线程退出自动停止续期
            if (locked && redissonLock.isHeldByCurrentThread()) {
                redissonLock.unlock();
            }
        }
        return null;
    }
    public Plan getPlan(Long id) {
        //查询数据库
        Plan plan = super.getById(id);
        RedisData redisData = new RedisData();
//        plan不存在
        if (plan == null) {
            // 缓存空值防止缓存穿透
            redisData.setData(null);
            redisData.setExpireTime(LocalDateTime.now().plusSeconds(PLUS_TTL + RANDOM.nextInt(-5,5)));
            stringRedisTemplate.opsForValue().set(RedisPrefixConstant.PLAN_PREFIX + id,
                    JSONUtil.toJsonStr(redisData), FINAL_PLAN_TTL, TimeUnit.SECONDS);
            return null;
        }
//        plan存在
        redisData.setData(plan);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(PLUS_TTL + RANDOM.nextInt(-5,5)));
        stringRedisTemplate.opsForValue().set(RedisPrefixConstant.PLAN_PREFIX + id,
                JSONUtil.toJsonStr(redisData), FINAL_PLAN_TTL, TimeUnit.SECONDS);
        return plan;
    }
    public void planCache(Long id) {
        PLAN_EXECUTOR.submit(() -> {
            log.info("缓存过期，PLAN_EXECUTOR处理-------------");
            RLock redissonLock = redissonClient.getLock("plan:lock:" + id);
            boolean locked = false;
            try {
                locked = redissonLock.tryLock(10, TimeUnit.SECONDS);
                if (!locked) {
                    return;
                }
                // 双重检查：防止多个异步任务同时进入后重复刷新
                String latestVal = stringRedisTemplate.opsForValue().get(RedisPrefixConstant.PLAN_PREFIX + id);
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
                getPlan(id);
            } catch (Exception e) {
                log.info(PLAN_EXECUTOR+"处理失败:"+e.getMessage());
            } finally {
                // 同一线程获取和释放锁，watchdog 会随线程退出自动停止续期
                if (locked && redissonLock.isHeldByCurrentThread()) {
                    redissonLock.unlock();
                }
            }
        });
    }

}
