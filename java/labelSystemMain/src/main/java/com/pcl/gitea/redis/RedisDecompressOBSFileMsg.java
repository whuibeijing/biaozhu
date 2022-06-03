package com.pcl.gitea.redis;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pcl.gitea.service.OBSDecompressService;
import com.pcl.util.JsonUtil;

public class RedisDecompressOBSFileMsg implements RedisMsg{
	
	private OBSDecompressService decompressService;
	
	public RedisDecompressOBSFileMsg(OBSDecompressService decompressService) {
		this.decompressService = decompressService;
	}

	private static Logger logger = LoggerFactory.getLogger(RedisAddDataSetFileMsg.class);
	
	@Override
	public void receiveMessage(String message) {
		
		logger.info("receive redis decompress obs msg:"  + message);
		
		Map<String,Object> attachMap = JsonUtil.getMap(message);
		
		decompressService.decopressObsFile(attachMap);
		
	}

}
