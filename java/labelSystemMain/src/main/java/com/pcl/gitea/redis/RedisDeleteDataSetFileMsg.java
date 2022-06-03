package com.pcl.gitea.redis;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pcl.gitea.service.GiteaDataSetService;
import com.pcl.util.JsonUtil;

public class RedisDeleteDataSetFileMsg implements RedisMsg{
	
	private GiteaDataSetService datasetService;
	
	public RedisDeleteDataSetFileMsg(GiteaDataSetService datasetService) {
		this.datasetService = datasetService;
	}

	private static Logger logger = LoggerFactory.getLogger(RedisDeleteDataSetFileMsg.class);
	
	//删除数据集的消息
	@Override
	public void receiveMessage(String message) {
		
		logger.info("receive delete redis msg:"  + message);
		
		Map<String,Object> attachMap = JsonUtil.getMap(message);
		
		//删除数据集
		datasetService.deleteGiteaDataSetByAttach(attachMap);
		
		//删除标注任务
		
		logger.info("finished deal delete redis msg:"  + message);
	}

}
