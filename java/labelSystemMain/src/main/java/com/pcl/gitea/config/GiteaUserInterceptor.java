package com.pcl.gitea.config;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.web.servlet.HandlerInterceptor;

import com.pcl.gitea.redis.RedisUtil;
import com.pcl.service.TokenManager;

public class GiteaUserInterceptor implements HandlerInterceptor {

	RedisUtil redisUtil;

	static String regex="^[1-9]+[0-9]*$";
	//^[1-9]+\\d*$
	private static Pattern intPattern=Pattern.compile(regex);

	public GiteaUserInterceptor(RedisUtil redisUtil) {
		this.redisUtil = redisUtil;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String token = request.getHeader("authorization");
		if(token != null && intPattern.matcher(token).find()) {
			TokenManager.addToken(token, Integer.parseInt(token));	
		}
		return true;
	}
	
	public static void main(String[] args) {
		String token = "11";
		
		System.out.println(intPattern.matcher(token).find());
		
		
	}
}
