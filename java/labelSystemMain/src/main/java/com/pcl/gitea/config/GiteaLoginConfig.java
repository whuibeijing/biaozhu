package com.pcl.gitea.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.pcl.gitea.redis.RedisUtil;

@Configuration
public class GiteaLoginConfig implements WebMvcConfigurer {
	
	private static Logger logger = LoggerFactory.getLogger(GiteaLoginConfig.class);
	
	@Autowired
	private RedisUtil redisUtil;
	
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	logger.info("regist GiteaUserInterceptor");
    	//注册TestInterceptor拦截器
    	InterceptorRegistration registration = registry.addInterceptor(new GiteaUserInterceptor(redisUtil));
    	registration.addPathPatterns("/**");                      //所有路径都被拦截

    }
}