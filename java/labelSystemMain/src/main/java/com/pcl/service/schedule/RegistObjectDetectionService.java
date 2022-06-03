package com.pcl.service.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pcl.constant.ModelConstants;
import com.pcl.constant.UserConstants;
import com.pcl.dao.AutoLabelModelRegistMsgDao;
import com.pcl.dao.LabelTaskItemDao;
import com.pcl.pojo.body.ObjectDetectionTaskBody;
import com.pcl.pojo.mybatis.AutoLabelModelRegistMsg;
import com.pcl.pojo.mybatis.LabelTaskItem;
import com.pcl.pojo.mybatis.PrePredictTask;
import com.pcl.service.LabelSystemDbPolicy;
import com.pcl.service.TokenManager;
import com.pcl.util.RestUtil;

@Service
public class RegistObjectDetectionService {
	
	private static Logger logger = LoggerFactory.getLogger(LabelForPictureSchedule.class);

	@Autowired
	private LabelTaskItemDao labelTaskItemDao;

	@Value("${server.port}")
	private String port;

	@Value("${msgresttype:https}")
	private String msgresttype;

	@Value("${msgrestip:127.0.0.1}")
	private String msgrestip;
	
	@Autowired
	private AutoLabelModelRegistMsgDao autoLabelModelRegistMsgDao;
	
	@Autowired
	private LabelSystemDbPolicy dbPolicy;
	
	public void doExec(PrePredictTask task) {
		
		AutoLabelModelRegistMsg registModel = autoLabelModelRegistMsgDao.queryById(task.getAlg_model_id());
		
		if(registModel == null) {
			return;
		}
		
		Map<String,Object> itemParamMap = new HashMap<>();
		itemParamMap.put("label_task_id", task.getDataset_id());
		itemParamMap.put("user_id",dbPolicy.getTableName(task.getDataset_id(), UserConstants.LABEL_TASK_SINGLE_TABLE, task.getUser_id())) ;
				//TokenManager.getUserTablePos(task.getUser_id(), UserConstants.LABEL_TASK_SINGLE_TABLE));
		logger.info("itemParamMap ==" + itemParamMap.toString());
		
		List<String> pictureList = new ArrayList<>();
		List<String> pictureIdList = new ArrayList<>();
		int count = labelTaskItemDao.queryLabelTaskItemPageCountByLabelTaskId(itemParamMap);
		int pageSize = 1000;
		itemParamMap.put("pageSize", pageSize);
		for(int i = 0; i < (count / pageSize) + 1; i++) {
			itemParamMap.put("currPage", i * pageSize);
			List<LabelTaskItem> list = labelTaskItemDao.queryLabelTaskItemPageByLabelTaskId(itemParamMap);
			for(LabelTaskItem item : list) {
				pictureList.add(item.getPic_image_field());
				pictureIdList.add(item.getId());
			}
		}
		
		ObjectDetectionTaskBody body = new ObjectDetectionTaskBody();
		body.setPictureList(pictureList);
		body.setTaskId(ModelConstants.OBJECT_DETECTION_SERVICE_TYPE_MULTI + "##" + task.getUser_id() + "##"+ task.getId());
		body.setMsgCallBackUrl(msgresttype + "://" + msgrestip + ":" + port + "/api/objectDetectionMsg");
		body.setPictureIdList(pictureIdList);
		
		RestUtil restUtil = new RestUtil();
		restUtil.post(registModel.getAgent_receive_task_url(), body);
	}
}
