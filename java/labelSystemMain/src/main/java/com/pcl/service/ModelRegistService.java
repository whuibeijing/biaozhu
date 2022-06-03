package com.pcl.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pcl.constant.ModelConstants;
import com.pcl.dao.AutoLabelModelRegistMsgDao;
import com.pcl.pojo.mybatis.AutoLabelModelRegistMsg;
import com.pcl.service.schedule.RegistModelAutoLabelService;
import com.pcl.util.JsonUtil;

/**
 * 自动标注模型注册服务
 * @author 邹安平
 *
 */
@EnableScheduling   // 开启定时任务
@Service
public class ModelRegistService {

	private static Logger logger = LoggerFactory.getLogger(ModelRegistService.class);
	
	@Autowired
	private AutoLabelModelRegistMsgDao autoLabelModelRegistMsgDao;
	
	@Autowired
	private RegistModelAutoLabelService heartService;
	
	private int startId = 10000;
	
	public String regist(AutoLabelModelRegistMsg msg) {
		
		List<AutoLabelModelRegistMsg> reList = autoLabelModelRegistMsgDao.queryByName(msg.getName());
		if(reList != null && reList.size() > 0) {
			for(AutoLabelModelRegistMsg exist : reList) {
				if(exist.getName().equals(msg.getName()) && exist.getAgent_receive_task_url().equals(msg.getAgent_receive_task_url())) {
					logger.info("The model has exist. dbinfo=" + JsonUtil.toJson(exist));
					if(exist.getStatus() ==  ModelConstants.MODEL_OFFLINE) {
						logger.info("update status to MODEL_ONLINE, model_name=" + msg.getName());
						Map<String,Object> paramMap = new HashMap<>();
						paramMap.put("id", msg.getId());
						paramMap.put("status", ModelConstants.MODEL_ONLINE);
						autoLabelModelRegistMsgDao.updateAutoLabelModelRegistMsg(paramMap);
					}
					return "true";
				}
			}
		}
		
		
		List<AutoLabelModelRegistMsg> all = autoLabelModelRegistMsgDao.queryAll();
		for(AutoLabelModelRegistMsg tmp : all) {
			if(tmp.getId() > startId) {
				startId = tmp.getId();
			}
		}
		startId ++;
		msg.setId(startId);
		logger.info("add  model to. dbinfo=" + JsonUtil.toJson(msg));
		autoLabelModelRegistMsgDao.saveAutoLabelModelRegistMsg(msg);
		
		return "true";
	}
	
	
	@Scheduled(cron = "0 0 */1 * * ?")//每隔1个小时检查一下心跳
	private void heartBeat() {
		logger.info("to heart beat.");
		List<AutoLabelModelRegistMsg> reList = autoLabelModelRegistMsgDao.queryAll();
		for(AutoLabelModelRegistMsg msg : reList) {
			if(!heartService.heart(msg)) {
				logger.info("update status to MODEL_OFFLINE, model_name=" + msg.getName());
				Map<String,Object> paramMap = new HashMap<>();
				paramMap.put("id", msg.getId());
				paramMap.put("status", ModelConstants.MODEL_OFFLINE);
				autoLabelModelRegistMsgDao.updateAutoLabelModelRegistMsg(paramMap);
			}else {
				if(msg.getStatus() == ModelConstants.MODEL_OFFLINE) {
					logger.info("update status to MODEL_ONLINE, model_name=" + msg.getName());
					Map<String,Object> paramMap = new HashMap<>();
					paramMap.put("id", msg.getId());
					paramMap.put("status", ModelConstants.MODEL_ONLINE);
					autoLabelModelRegistMsgDao.updateAutoLabelModelRegistMsg(paramMap);
				}
			}
		}
		
	}
	
}
