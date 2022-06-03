package com.pcl.pojo;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Token implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -707193229963731031L;

	private String token;
	
	private String userName;
	
	private String nickName;
	
	private int userType;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public int getUserType() {
		return userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}
	
	public static void main(String[] args) {
		Calendar calendar = Calendar.getInstance();  
		calendar.set(Calendar.YEAR, 2021);  
		calendar.set(Calendar.MONTH, 10);  
		calendar.set(Calendar.DAY_OF_MONTH, 1); 
		
		Date today = calendar.getTime();  

		SimpleDateFormat df3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println( df3.format(today));
		
		System.out.println(today.getTime());
		
	}
	
}
