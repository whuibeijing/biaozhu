package com.pcl.gitea.service;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pcl.constant.UserConstants;
import com.pcl.dao.LabelTaskItemDao;

@Service
public class GiteaDbPolicy {
	
	private static Logger logger = LoggerFactory.getLogger(GiteaDbPolicy.class);
	
	private ConcurrentHashMap<String,String> existedTableName = new ConcurrentHashMap<>();

	@Autowired
	private LabelTaskItemDao labelTaskItemDao;
	
	@PostConstruct
	private void loadExistTableToCache() {
		for(int i = 0; i < 100; i++) {
			String tableName = UserConstants.LABEL_TASK_SINGLE_TABLE_NAME + i;
			if(labelTaskItemDao.existTable(tableName) > 0) {
				existedTableName.put(tableName, "true");
			}
		}
	}
	
	/**
	 * 取任务ID前两位进行映射，映射到0..99 中。
	 * @param taskId
	 * @return
	 */
	public String getTableName(String taskId,int taskType) {
		String postfix = getTabelNamePostfix(taskId);
		if(postfix.length() > 0) {
			if(UserConstants.LABEL_TASK_SINGLE_TABLE == taskType) {
				String tableName = UserConstants.LABEL_TASK_SINGLE_TABLE_NAME + postfix;
				if(existedTableName.get(tableName) == null) {
					logger.info("create table :" + tableName);
					labelTaskItemDao.createTable(tableName);
					existedTableName.put(tableName, "true");
				}
			}
			return "_" + postfix;
		}
		return postfix;
	}
	
	private String getTabelNamePostfix(String taskId) {
		if(taskId == null || taskId.length() < 3) {
			return "";
		}
		char first = taskId.charAt(0);
		char second = taskId.charAt(1);
		
		int firstInt = Character.getNumericValue(first);
		int secondInt = Character.getNumericValue(second);
		
		int total = (firstInt * 10 + secondInt) % 100;
		
		return String.valueOf(total);
	}
	
	public static void main(String[] args) {
		int firstInt = Character.getNumericValue('c');
		int secondInt = Character.getNumericValue('0');
		int total = (firstInt * 10 + secondInt) % 100;
		
		System.out.println(String.valueOf(total));
	}
}
