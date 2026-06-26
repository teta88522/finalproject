package com.pixcel.app.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisPublisher {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void publish(String channel, byte[] message) {

        System.out.println("Publish : " + channel);

        redisTemplate.convertAndSend(channel, message);
    }
}