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

import com.pcl.constant.Constants;
import com.pcl.constant.ModelConstants;
import com.pcl.constant.UserConstants;
import com.pcl.dao.AutoLabelModelRegistMsgDao;
import com.pcl.dao.LabelTaskDao;
import com.pcl.dao.LabelTaskItemDao;
import com.pcl.dao.PrePredictTaskDao;
import com.pcl.exception.LabelSystemException;
import com.pcl.pojo.Progress;
import com.pcl.pojo.body.ObjectDetectionTaskBody;
import com.pcl.pojo.mybatis.AutoLabelModelRegistMsg;
import com.pcl.pojo.mybatis.LabelTask;
import com.pcl.pojo.mybatis.LabelTaskItem;
import com.pcl.pojo.mybatis.PrePredictTask;
import com.pcl.service.LabelSystemDbPolicy;
import com.pcl.service.ProgressService;
import com.pcl.util.JsonUtil;
import com.pcl.util.RestUtil;

@Service
public class RegistModelAutoLabelService {
	
	private static Logger logger = LoggerFactory.getLogger(RegistModelAutoLabelService.class);

	@Autowired
	private LabelTaskItemDao labelTaskItemDao;
	
	@Autowired
	private LabelTaskDao labelTaskDao;

	@Autowired
	private AutoLabelModelRegistMsgDao autoLabelModelRegistMsgDao;
	
	@Autowired
	private LabelSystemDbPolicy dbPolicy;
	
	@Autowired
	private ProgressService progressService;
	
	@Autowired
	private PrePredictTaskDao prePredictTaskDao;
	
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
		body.setModelName(registModel.getName());
		body.setPictureList(pictureList);
		body.setTaskId(ModelConstants.OBJECT_DETECTION_SERVICE_TYPE_MULTI + "##" + task.getUser_id() + "##"+ task.getId());
		
		body.setPictureIdList(pictureIdList);
		
		RestUtil restUtil = new RestUtil();
		restUtil.post(registModel.getAgent_receive_task_url(), body);
		
		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("id", task.getId());
		paramMap.put("task_status", Constants.PREDICT_TASK_STATUS_PROGRESSING);
		paramMap.put("task_status_desc", "开始进行标注");
		prePredictTaskDao.updatePrePredictTaskStatus(paramMap);
	}
	
	
	public void doExec(LabelTask labelTask,int modelId) throws LabelSystemException {
		ThreadSchedule.execThread(new Runnable() {
			@Override
			public void run() {
				try {
					threadExec(labelTask, modelId);
				} catch (LabelSystemException e) {
					e.printStackTrace();
				}
			}
		});
		
	}


	private void threadExec(LabelTask labelTask, int modelId) throws LabelSystemException {
		logger.info("start to send msg to auto machine.");
		AutoLabelModelRegistMsg registModel = autoLabelModelRegistMsgDao.queryById(modelId);
		
		if(registModel == null) {
			logger.error("not found regist model.id=" + modelId + " task name=" + labelTask.getTask_name());
			return;
		}
		
		Map<String,Object> itemParamMap = new HashMap<>();
		itemParamMap.put("label_task_id", labelTask.getId());
		itemParamMap.put("user_id",dbPolicy.getTableName(labelTask.getId(), UserConstants.LABEL_TASK_SINGLE_TABLE, labelTask.getUser_id())) ;
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
		body.setModelName(registModel.getName());
		body.setPictureList(pictureList);
		body.setTaskId(ModelConstants.OBJECT_DETECTION_SERVICE_TYPE_LABEL_TASK_MULTI + "##" + labelTask.getUser_id() + "##"+ labelTask.getId());

		body.setPictureIdList(pictureIdList);
		logger.info("send msg:" + JsonUtil.toJson(body));
		RestUtil restUtil = new RestUtil();
		Object re = restUtil.post(registModel.getAgent_receive_task_url(), body);
		if(re != null) {
			Progress progress = new Progress();
			progress.setId(labelTask.getId());
			progress.setStartTime(System.currentTimeMillis()/1000);
			progress.setExceedTime(count * 2 + 600);
			progress.setProgress(0);
			HashMap<String,Integer> total = new HashMap<>();
			total.put("total", count);
			progress.setInfo(JsonUtil.toJson(total));
			progressService.putProgress(progress);
		}else {
			logger.info("Task failed.");
			Map<String,Object> paramMap = new HashMap<>();
			paramMap.put("id", labelTask.getId());
			paramMap.put("task_status", Constants.LABEL_TASK_STATUS_AUTO_LABEL_EXCEPTION);
			labelTaskDao.updateLabelTaskStatus(paramMap);
			
		}
	}
	
	public boolean heart(AutoLabelModelRegistMsg registModel) {
		ObjectDetectionTaskBody body = new ObjectDetectionTaskBody();
		body.setModelName("");
		logger.info("send heart msg:" + JsonUtil.toJson(body) + "  url=" + registModel.getAgent_receive_task_url());
		RestUtil restUtil = new RestUtil();
		Object re = restUtil.post(registModel.getAgent_receive_task_url(), body);
		if(re != null) {
			return true;
		}
		return false;
	}
}
