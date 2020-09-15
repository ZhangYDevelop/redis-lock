package com.zy.text.redis.lock.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * redis 集群模式中分布式锁处理
 */
@Service
@SuppressWarnings("all")
public class RedisClusterRedissionService {
    // 商品锁 key 值
    private String lockKey = "computer_key_123";
    private Logger logger = LoggerFactory.getLogger(RedisClusterRedissionService.class);
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    public String takeOrder() {
        String userID = UUID.randomUUID().toString();
        RLock rLock = redissonClient.getLock(lockKey);
        while (true) {
            rLock.lock();
            // cumputer_stock 为redis中提前设置好的库存
            String stockStr = stringRedisTemplate.opsForValue().get("cumputer_stock");
            int stock = Integer.parseInt(stockStr);
            logger.info("用户：{} 获取锁", userID);
            try {
                if (stock <= 0) {// 检查库存
                    logger.info("已售罄");
                    break;
                }
                try {
                    // 模拟业务操作
                    Thread.sleep(new Random().nextInt(3000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 扣减库存
                stringRedisTemplate.opsForValue().set("cumputer_stock", (stock - 1) + "");
                logger.info("用户：{}，抢单成功, 剩余库存： {}", userID, stock - 1);
            } finally {
                // redis 集群
                rLock = redissonClient.getLock(lockKey);
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }

            }
            return "用户：" + userID + ",抢单成功";
        }
        return "已售罄";
    }
}
