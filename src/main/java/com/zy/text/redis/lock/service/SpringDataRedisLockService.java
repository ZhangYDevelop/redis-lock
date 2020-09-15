package com.zy.text.redis.lock.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Spring boot data redis 分布式锁使用
 */
@Service
@SuppressWarnings("all")
public class SpringDataRedisLockService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    // 商品锁 key 值
    private String lockKey = "computer_key";
    private Logger logger = LoggerFactory.getLogger(RedisService.class);
    // 线程池
    ExecutorService executorService = new ThreadPoolExecutor(4, 4, 1L,
            TimeUnit.MICROSECONDS, new LinkedBlockingDeque<Runnable>());


    public List<String> testRedisLock() {
        CountDownLatch countDownLatch = new CountDownLatch(200);
        // 抢到商品的用户
        List<String> shopUser = new ArrayList<>();
        // 模拟用户数据
        List<String> userArray = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            userArray.add(UUID.randomUUID().toString());
        }
        // 模拟抢单
        userArray.stream().parallel().forEach(userId -> {
            executorService.execute(() -> {
                String user = takeOrder(userId);
                if (!StringUtils.isEmpty(user)) {
                    shopUser.add(user);
                }
                countDownLatch.countDown();
            });
        });
        // executorService.shutdown(); shutdown后，后续再调用会触发决绝策略
        System.out.println("wait start");
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("wait end ! 成功抢单用户数量：" + shopUser.size());
        return shopUser;
    }

    private String takeOrder(String b) {
        while (true) {
            Boolean lockFlag = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, b, 1L, TimeUnit.SECONDS);
            if (lockFlag) { // 设置锁的过期时间，避免宕机或者其他情况，导致死锁
                // cumputer_stock 为redis中提前设置好的库存
                String stockStr = stringRedisTemplate.opsForValue().get("cumputer_stock");
                int stock = Integer.parseInt(stockStr);
                logger.info("用户：{} 获取锁", b);
                try {
                    if (stock <= 0) {// 检查库存
                        logger.info("已售罄");
                        break;
                    }
                    try {
                        // 模拟业务操作
                        Thread.sleep(new Random().nextInt(300));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 扣减库存
                    stringRedisTemplate.opsForValue().set("cumputer_stock", (stock - 1) + "");
                    logger.info("用户：{}，抢单成功, 剩余库存： {}", b, stock -1);
                } finally {
                    // 释放锁, 必须放在finally，确保锁能释放
                    if (b.equals(stringRedisTemplate.opsForValue().get(lockKey))) { // 避免误删，导致锁失效
                        boolean flag = stringRedisTemplate.delete(lockKey);
                        logger.info("用户：{}，释放锁: {}", b, flag);
                    }
                }
                return b;
            }
        }
        return null;
    }

}
