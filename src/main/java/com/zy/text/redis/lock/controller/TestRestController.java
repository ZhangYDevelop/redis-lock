package com.zy.text.redis.lock.controller;

import com.zy.text.redis.lock.service.RedisClusterRedissionService;
import com.zy.text.redis.lock.service.RedisService;
import com.zy.text.redis.lock.service.RedissionService;
import com.zy.text.redis.lock.service.SpringDataRedisLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api")
public class TestRestController {



    @Autowired
    private RedisService redisService;

    @Autowired
    private SpringDataRedisLockService springDataRedisLock;

    @Autowired
    private RedissionService redissionService;

    @Autowired
    private RedisClusterRedissionService redisClusterRedissionService;
    @GetMapping("/order")
    public String test() {
      return  redisClusterRedissionService.takeOrder();
    }
}
