package com.zy.text.redis.lock.conf;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RedissionClient 客户端配置
 */
@Configuration
public class RedissionClientConf {
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.setTransportMode(TransportMode.NIO);
        config.useSingleServer().setAddress("redis://192.168.106.116:6379").setPassword("redis");
//        config.useSingleServer().setAddress("redis://192.168.106.137:6379");
//        config.useClusterServers().addNodeAddress("redis://192.168.106.137:6379", "redis://192.168.106.137:6380","redis://192.168.106.136:6379",
//                "redis://192.168.106.136:6380","redis://192.168.106.135:6379","redis://192.168.106.135:6380");
        RedissonClient redisson = Redisson.create(config);
        return  redisson;
    }
}
