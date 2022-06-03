package com.pcl.gitea.redis;

import org.springframework.stereotype.Component;

@Component
public interface RedisMsg {

    public void receiveMessage(String message);
    
}