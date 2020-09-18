package com.zy.text.redis.lock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class RedisLockApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(RedisLockApplication.class, args);
    }

}
