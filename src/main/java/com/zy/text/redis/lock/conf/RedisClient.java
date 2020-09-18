package com.zy.text.redis.lock.conf;

import redis.clients.jedis.Jedis;

import java.util.Collections;

public class RedisClient {
    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private static final Long RELEASE_SUCCESS = 1L;

    /**
     * 尝试获取分布式锁,该方法确保了在同一时刻只有一个线程能抢占到锁
     * @param lockKey 锁
     * @param requestId 请求标识
     * @param expireTime 超期时间
     * @return 是否获取成功
     */
    public boolean tryGetLock( String lockKey, String requestId, int expireTime) {
        Jedis jedis = RedisPool.getJedis();
        String result = jedis.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        RedisPool.close(jedis);
        if (LOCK_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public  boolean releaseLock( String lockKey, String requestId) {
        Jedis jedis = RedisPool.getJedis();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey),
                Collections.singletonList(requestId));
        RedisPool.close(jedis);
        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

    /**
     * 设置key值
     * @param key
     * @param value
     * @return
     */
    public  boolean setKey( String key, String value) {
        Jedis jedis = RedisPool.getJedis();
        String result = jedis.set(key.getBytes(), value.getBytes());
        RedisPool.close(jedis);
        if (LOCK_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

    /**
     * 获取key操作
     * @param key
     * @return
     */
    public  String getKey( String key) {
        Jedis jedis = RedisPool.getJedis();
        String result = jedis.get(key);
        RedisPool.close(jedis);
        return result;
    }

    /**
     * 给锁延时
     * @param key
     * @param second
     * @return
     */
    public Long exporeKey (String key, int second) {
        Jedis jedis = RedisPool.getJedis();
        Long result = jedis.expire(key, second );
        RedisPool.close(jedis);
        return  result;
    }
}
