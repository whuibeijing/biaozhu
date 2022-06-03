package com.pcl.gitea.redis;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pcl.gitea.service.GiteaDataSetService;
import com.pcl.util.JsonUtil;

public class RedisAddDataSetFileMsg implements RedisMsg{
	
	private GiteaDataSetService datasetService;
	
	public RedisAddDataSetFileMsg(GiteaDataSetService datasetService) {
		this.datasetService = datasetService;
	}

	private static Logger logger = LoggerFactory.getLogger(RedisAddDataSetFileMsg.class);
	
	@Override
	public void receiveMessage(String message) {
		
		logger.info("receive redis msg:"  + message);
		
		Map<String,Object> attachMap = JsonUtil.getMap(message);
		
		datasetService.addGiteaDataSetByAttach(attachMap);
		
		logger.info("finished deal redis msg:"  + message);
		
	}

}
