package com.zy.text.redis.lock.service;

import com.zy.text.redis.lock.conf.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * 单机模式下的分布式锁
 */
@Service
@SuppressWarnings("all")
public class RedisService {

    // 商品锁 key 值
    private String lockKey = "computer_key";
    private Logger logger = LoggerFactory.getLogger(RedisService.class);
    // 线程池
    ExecutorService executorService = new ThreadPoolExecutor(4, 4, 1L,
            TimeUnit.MICROSECONDS, new LinkedBlockingDeque<Runnable>());

    @Autowired
    private RedisClient redisClient;

    // 锁的延时标识 volatile修饰修改属性，对线程可见
    private volatile boolean lockOverTime = true;

    // 重试时间
    private int timeOut = 10000;

    public String takeOrder(String b) {
        long startTime = System.currentTimeMillis();
        // 超时范围类让用户重试
        while ((startTime + timeOut) >= System.currentTimeMillis()) {
            startTime = System.currentTimeMillis();
            if (redisClient.tryGetLock(lockKey, b, 1000)) { // 设置锁的过期时间，避免宕机或者其他情况，导致死锁
                // cumputer_stock 为redis中提前设置好的库存
                String stockStr = redisClient.getKey("cumputer_stock");
                int stock = Integer.parseInt(stockStr);
                logger.info("用户：{} 获取锁", b);
                try {
                    if (stock <= 0) {// 检查库存
                        logger.info("已售罄");
                        return "已售罄";
                    }
                    // 开始业务操作前，开启一个线程延长锁的时间
                    lockOverTime = true;
                    // 启动子线
                    new MyThread(lockKey).start();
                    try {
                        // 模拟业务操作
                        int sleepTime = new Random().nextInt(4000);
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 扣减库存
                    long start = System.currentTimeMillis();
                    redisClient.setKey("cumputer_stock", (stock - 1) + "");
                    logger.info("用户：{}，抢单成功, 剩余库存： {}", b, stock - 1);
                } finally {
//                    System.out.println("修改延时结束标识");
                    lockOverTime = false;
                    // 释放锁, 必须放在finally，确保锁能释放
                    String rediskey = redisClient.getKey(lockKey);
                    if (b.equals(redisClient.getKey(lockKey))) { // 避免误删，导致锁失效
                        boolean flag = redisClient.releaseLock(lockKey, b.toString());
                        logger.info("用户：{}，释放锁: {}", b, flag);
                    }
                }
                return b;
            }
        }
        return "抢单失败";
    }

    private class MyThread extends Thread {
        private String key;

        public MyThread(String key) {
            this.key = key;
        }


        @Override
        public void run() {
            logger.info("延时开始，startTime:" + System.currentTimeMillis());
            for (;  ; ) {
                if (!lockOverTime) {
                    logger.info("延时结束,endTime:" + System.currentTimeMillis());
                    break;
                }
                redisClient.exporeKey(key, 3);
            }
        }
    }

}
