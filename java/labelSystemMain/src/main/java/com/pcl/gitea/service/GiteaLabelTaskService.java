package com.pcl.gitea.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.util.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.pcl.constant.Constants;
import com.pcl.constant.LogConstants;
import com.pcl.constant.UserConstants;
import com.pcl.dao.DataSetDao;
import com.pcl.dao.LabelDcmTaskItemDao;
import com.pcl.dao.LabelPropertyTaskDao;
import com.pcl.dao.LabelTaskDao;
import com.pcl.dao.LabelTaskItemDao;
import com.pcl.exception.LabelSystemException;
import com.pcl.pojo.PageResult;
import com.pcl.pojo.Progress;
import com.pcl.pojo.body.LabelTaskBody;
import com.pcl.pojo.display.DisplayLabelTask;
import com.pcl.pojo.mybatis.DataSet;
import com.pcl.pojo.mybatis.LabelPropertyTask;
import com.pcl.pojo.mybatis.LabelTask;
import com.pcl.pojo.mybatis.LabelTaskItem;
import com.pcl.pojo.mybatis.LogInfo;
import com.pcl.service.LabelSystemDbPolicy;
import com.pcl.service.LogService;
import com.pcl.service.ProgressService;
import com.pcl.service.TokenManager;
import com.pcl.service.UserService;
import com.pcl.service.schedule.RegistModelAutoLabelService;
import com.pcl.util.JsonUtil;
import com.pcl.util.TimeUtil;

@Service
public class GiteaLabelTaskService {

	@Autowired
	private LabelTaskDao labelTaskDao;

	@Autowired
	private LabelTaskItemDao labelTaskItemDao;
	
	@Autowired
	private LabelPropertyTaskDao labelPropertyTaskDao;

	@Autowired
	private LabelDcmTaskItemDao labelDcmTaskItemDao;
	
	@Autowired
	private UserService userService;

	@Autowired
	private DataSetDao dataSetDao;

	@Autowired
	private LogService logService;
	
	@Autowired
	private LabelSystemDbPolicy giteaDbPolicy;
	
	@Autowired
	private GiteaPgDbOperService pgDbService;
	
	@Value("${picture.postfix:jpeg,jpg,png,gif,bmp}")
    private String picturePostfix;

	@Autowired
	private RegistModelAutoLabelService registObjectDetectionService;
	
	@Autowired
	private ProgressService progressService;

	private Gson gson = new Gson();
	
	private static final int VERIFY_UPDATE_FLAG = 100;
	
	private static final int LABEL_UPDATE_FLAG = 0;
	
	private static final String NOT_VALIDE_VALUE = "1";

	private static Logger logger = LoggerFactory.getLogger(GiteaLabelTaskService.class);

	public DisplayLabelTask queryLabelTaskById(String token, String id) {
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		LabelTask labelTask = labelTaskDao.queryLabelTaskById(id);

		if(labelTask == null) {
			logger.info("the label task is not exist. id=" + id);
			return null;
		}

		Map<String, Integer> countMap = getLabelTaskStatus(userId,Arrays.asList(labelTask));

		DisplayLabelTask reTask = new DisplayLabelTask();
		reTask.setId(labelTask.getId());
		reTask.setTask_flow_type(labelTask.getTask_flow_type());
		reTask.setTask_name(labelTask.getTask_name());
		reTask.setTask_add_time(labelTask.getTask_add_time());
		reTask.setRelate_task_name(labelTask.getRelate_task_name());
		reTask.setTask_type(labelTask.getTask_type());
		reTask.setTask_label_type_info(labelTask.getTask_label_type_info());
		reTask.setRelate_task_id(labelTask.getRelate_task_id());
		
		String otherLabelTask = labelTask.getRelate_other_label_task();
		if(!Strings.isNullOrEmpty(otherLabelTask)) {
			List<String> idLists = gson.fromJson(otherLabelTask, new TypeToken<List<String>>() {
				private static final long serialVersionUID = 1L;}.getType());
			if(idLists != null && idLists.size() > 0) {
				Map<Integer,String> userIdMap = userService.getAllUser();
				List<LabelTask> taskList = labelTaskDao.queryLabelTaskByIds(idLists);
				Map<String,String> idTaskName = new HashMap<>();
				for(LabelTask task : taskList) {
					idTaskName.put(task.getId(), task.getTask_name() + "(" + userIdMap.get(task.getAssign_user_id()) +")");
				}
				reTask.setRelate_other_label_task(gson.toJson(idTaskName));
			}
		}

		setLabelStatus(countMap, labelTask, reTask);

		return reTask;
	}

	public void queryLabelCount(String token) {
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		
		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("user_id", userId);
		
		new Thread(()->{
			
			long startTime = System.currentTimeMillis();
			logger.info("update userId=" + userId + "  label count start.");
			
			List<LabelTask> taskList = labelTaskDao.queryLabelTaskByUser(paramMap);
			
			for(LabelTask labelTask : taskList) {
				doLabelCount(labelTask);
			}
			
			logger.info("update userId=" + userId + "  label count finished. cost=" + (System.currentTimeMillis() - startTime));
			
		}).start();
		
	}

	private void doLabelCount(LabelTask labelTask) {
		Map<String,Object> tmpParam = new HashMap<>();
		tmpParam.put("label_task_id", labelTask.getId());
		tmpParam.put("user_id", giteaDbPolicy.getTableName(labelTask.getId(), UserConstants.LABEL_TASK_SINGLE_TABLE,-1));
		int count = 0;
		count =labelTaskItemDao.queryLabelTaskItemPageCountByLabelTaskId(tmpParam);
		int total = 0;
		int pageSize = 1000;
		for(int i = 0; i < (count/pageSize) +1; i++) {
			tmpParam.put("currPage", i * pageSize);
			tmpParam.put("pageSize", pageSize);
			
			List<LabelTaskItem> itemList =labelTaskItemDao.queryLabelTaskItemPageByLabelTaskId(tmpParam);
			total += countItem(itemList);
		}
		
		//更新属性
		Map<String,Object> updateParam = new HashMap<>();
		updateParam.put("id", labelTask.getId());
		updateParam.put("total_label", total);
		labelTaskDao.updateLabelTaskLabelCount(updateParam);
	}
	
	
	

	private int countItem(List<LabelTaskItem> itemList) {
		int re = 0;
		for(LabelTaskItem item : itemList) {
			String labelInfo = item.getLabel_info();
			if(Strings.isNullOrEmpty(labelInfo)) {
				continue;
			}
			ArrayList<Map<String,Object>> labelList = gson.fromJson(labelInfo, new TypeToken<ArrayList<Map<String,Object>>>() {
				private static final long serialVersionUID = 1L;}.getType());
			if(labelList.isEmpty()) {
				continue;
			}
			re += labelList.size();
		}
		return re;
	}

	public PageResult queryLabelTask(String token,int currPage, int pageSize){

		PageResult pageResult = new PageResult();
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		List<DisplayLabelTask> result = new ArrayList<>();

		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("currPage", currPage * pageSize);
		paramMap.put("pageSize", pageSize);
		paramMap.put("user_id", userId);
		List<LabelTask> labelTaskList = labelTaskDao.queryLabelTask(paramMap);

		int totalCount = labelTaskDao.queryLabelTaskCount(paramMap);

		pageResult.setTotal(totalCount);
		pageResult.setData(result);
		pageResult.setCurrent(currPage);

		if(totalCount > 0) {

			Map<Integer,String> userIdForName = userService.getAllUser();
			Map<String, Integer> labelCountMap = getLabelTaskStatus(userId,labelTaskList);

			for(LabelTask labelTask : labelTaskList) {
				DisplayLabelTask reTask = new DisplayLabelTask();
				reTask.setId(labelTask.getId());
				reTask.setTask_name(labelTask.getTask_name());
				reTask.setTask_add_time(labelTask.getTask_add_time());
				reTask.setRelate_task_name(labelTask.getRelate_task_name());
				reTask.setTask_type(labelTask.getTask_type());
				reTask.setTask_flow_type(labelTask.getTask_flow_type());
				
				if(labelTask.getTask_status() == Constants.LABEL_TASK_STATUS_AUTO_LABEL_PROGRESS) {
					//给自动标注进度
					Progress pro = progressService.queryProgressById(labelTask.getId());
					if(pro != null){
						reTask.setTask_status_desc(pro.getProgress() + "%");
					}else {
						reTask.setTask_status(Constants.LABEL_TASK_STATUS_AUTO_LABEL_EXCEPTION);
						reTask.setTask_status_desc("自动标注超时。");
					}
					reTask.setTask_status(labelTask.getTask_status());
				}else if(labelTask.getTask_status() == Constants.LABEL_TASK_STATUS_AUTO_LABEL_EXCEPTION) {
					reTask.setTask_status(labelTask.getTask_status());
					reTask.setTask_status_desc("自动标注失败，模型不在线。");
				}
				else {
					setLabelStatus(labelCountMap, labelTask, reTask);
				}
				
				Map<String,String> userInfo = JsonUtil.getStrMap(labelTask.getUser_info());
				setUserInfo(userIdForName, labelTask, reTask, userInfo);
				reTask.setTotal_label(labelTask.getTotal_label());
				
				result.add(reTask);
			}
		}

		return pageResult;
	}

	private void setUserInfo(Map<Integer, String> userIdForName, LabelTask labelTask, DisplayLabelTask reTask,
			Map<String, String> userInfo) {
		if(userIdForName.containsKey(labelTask.getUser_id())) {
			reTask.setUser(userIdForName.get(labelTask.getUser_id()));
		}else {
			logger.info("userInfo=" + labelTask.getUser_info());
			reTask.setUser(userInfo.get(UserConstants.USER_NAME));
		}
		if(userIdForName.containsKey(labelTask.getVerify_user_id())) {
			reTask.setVerify_user(userIdForName.get(labelTask.getVerify_user_id()));
		}
		if(labelTask.getAssign_user_id() == 0) {
			if(userIdForName.containsKey(labelTask.getUser_id())) {
				reTask.setAssign_user(userIdForName.get(labelTask.getUser_id()));
			}else {
				reTask.setAssign_user(userInfo.get(UserConstants.ASSIGN_USER_NAME));
			}
		}else {
			if(userIdForName.containsKey(labelTask.getAssign_user_id())) {
				reTask.setAssign_user(userIdForName.get(labelTask.getAssign_user_id()));
			}else {
				reTask.setAssign_user(userInfo.get(UserConstants.ASSIGN_USER_NAME));
			}
		}
	}

	private void setLabelStatus(Map<String, Integer> countMap, LabelTask labelTask, DisplayLabelTask reTask) {
		int progress = 0;
		int finished = 0;
		String key = labelTask.getId();
		if(labelTask.getTask_status() == Constants.LABEL_TASK_STATUS_LABEL) {
			key += "label";
		}else if(labelTask.getTask_status() == Constants.LABEL_TASK_STATUS_VERIFY){
			key += "verify";
		}
		if(countMap.containsKey(key)) {
			finished = countMap.get(key);
		}
		if(finished == labelTask.getTotal_picture()) {
			progress = 100;
		}else {
			double tmp = finished * 1.0d / labelTask.getTotal_picture();
			tmp *= 100;
			progress = (int)tmp;
		}
		reTask.setTask_status(labelTask.getTask_status());
		reTask.setTask_status_desc(progress + "%(" + finished +  "/" + labelTask.getTotal_picture() + ")");
	}

	private Map<String, Integer> getLabelTaskStatus(int userId,List<LabelTask> labelTaskList) {
		
		Map<String,Integer> countMap = new HashMap<>();
		
		for(LabelTask labelTask : labelTaskList) {
			Map<String,Object> paramMap = new HashMap<>();
			paramMap.put("user_id", giteaDbPolicy.getTableName(labelTask.getId(), UserConstants.LABEL_TASK_SINGLE_TABLE,-1));
			paramMap.put("list", Arrays.asList(labelTask.getId()));
			
			List<Map<String,Object>> labelTaskStatusList = labelTaskItemDao.queryLabelTaskStatusByLabelTaskId(paramMap);
			
			for(Map<String,Object> map : labelTaskStatusList) {
				String label_task_id = map.get("label_task_id").toString();
				int total = Integer.parseInt(map.get("total").toString());
				int label_not_finished = Integer.parseInt(map.get("label_not_finished").toString());
				int verify_finished = 0;
				if(map.get("verify_finished") != null) {
					verify_finished = Integer.parseInt(map.get("verify_finished").toString());
				}
				countMap.put(label_task_id + "label", total - label_not_finished);
				countMap.put(label_task_id + "verify", verify_finished);
			}
		}

		return countMap;
	}

	private boolean isPostfixEnd(String name,String configPostfix) {
		String videoPostfixArray[] = configPostfix.split(",");
		for(String postfix : videoPostfixArray) {
			if(name.endsWith("." + postfix)) {
				return true;
			}
		}
		return false;
	}

	private void dealDataSetLabelTask(DataSet dataSet,LabelTask labelTask) throws LabelSystemException {
		labelTask.setRelate_task_name(dataSet.getTask_name());
		if(dataSet.getDataset_type() == Constants.DATASET_TYPE_VIDEO) {
			labelTask.setTask_type(Constants.LABEL_TASK_TYPE_VIDEO);
		}else {
			labelTask.setTask_type(Constants.LABEL_TASK_TYPE_ORIGIN);
		}
		
		//拷贝一次记录，文件不再动
		List<LabelTaskItem> list = labelTaskItemDao.queryLabelTaskItemByLabelTaskId(giteaDbPolicy.getTableName(dataSet.getId(), UserConstants.LABEL_TASK_SINGLE_TABLE,-1),dataSet.getId());
		logger.info("select from " + giteaDbPolicy.getTableName(dataSet.getId(), UserConstants.LABEL_TASK_SINGLE_TABLE,-1) + " size=" + list.size());
		copyLabelTaskItem(dataSet.getUser_id(),labelTask, list);

	}

	private void copyLabelTaskItem(int userId,LabelTask labelTask, List<LabelTaskItem> list) throws LabelSystemException {
		int total = 0;
		List<LabelTaskItem> batchList = new ArrayList<>();
		for(LabelTaskItem item : list) {
			if(!isPostfixEnd(item.getPic_image_field(), picturePostfix)) {//不是图片则不需要标注直接去掉
				continue;
			}
			LabelTaskItem taskItem = new LabelTaskItem();
			taskItem.setId(UUID.randomUUID().toString().replaceAll("-",""));
			taskItem.setItem_add_time(TimeUtil.getCurrentTimeStr());
			taskItem.setLabel_task_id(labelTask.getId());
			taskItem.setPic_real_name(item.getPic_real_name());
			taskItem.setPic_image_field(item.getPic_image_field());
			taskItem.setPic_object_name(item.getPic_object_name());
			taskItem.setPic_url(item.getPic_url());
			taskItem.setDisplay_order1(item.getDisplay_order1());
			taskItem.setDisplay_order2(item.getDisplay_order2());
			taskItem.setLabel_status(Constants.LABEL_TASK_STATUS_NOT_FINISHED);
			taskItem.setLabel_info(item.getLabel_info());
			batchList.add(taskItem);
			total ++;
			if(batchList.size() == 2000) {
				Map<String,Object> paramMap = new HashMap<>();
				paramMap.put("user_id",  giteaDbPolicy.getTableName(labelTask.getId(), UserConstants.LABEL_TASK_SINGLE_TABLE,-1));
				paramMap.put("list", batchList);
				labelTaskItemDao.addBatchLabelTaskItemMap(paramMap);
				batchList.clear();
			}
		}
		if(batchList.size() > 0) {
			Map<String,Object> paramMap = new HashMap<>();
			paramMap.put("user_id", giteaDbPolicy.getTableName(labelTask.getId(), UserConstants.LABEL_TASK_SINGLE_TABLE,-1));
			paramMap.put("list", batchList);
			labelTaskItemDao.addBatchLabelTaskItemMap(paramMap);
			batchList.clear();
		}
		if( total == 0) {
			throw new LabelSystemException("可标注的对象数目为0，不能创建标注任务。");
		}
		labelTask.setTotal_picture(total);
	}

	public int updateLabelTask(String token,String labelTaskId,String taskLabelTypeInfo) {

		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("id", labelTaskId);
		paramMap.put("task_label_type_info", taskLabelTypeInfo);

		return labelTaskDao.updateLabelTask(paramMap);

	}


	public int addLabelTask(String token,LabelTaskBody body) throws LabelSystemException {
		//		if(Strings.isNullOrEmpty(token)) {
		//			token = "JWT d7f52e6fbc154ac285cece3e2704468a";
		//		}
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));

		try {
			if(body.getLabelPropertyTaskId() != null) {
				LabelPropertyTask task = labelPropertyTaskDao.queryLabelPropertyTaskById(body.getLabelPropertyTaskId());
				if(task != null) {
					body.setTaskLabelTypeInfo(task.getPropertyJson());
				}
			}
		}catch (Exception e) {
			logger.info(e.getMessage());
			body.setTaskLabelTypeInfo(null);
		}
		
		LabelTask labelTask = new LabelTask();
		labelTask.setId(UUID.randomUUID().toString().replaceAll("-",""));
		labelTask.setRelate_task_id(body.getRelateTaskId());
		labelTask.setTask_name(body.getTaskName());
		labelTask.setUser_id(userId);
		labelTask.setTask_add_time(TimeUtil.getCurrentTimeStr());
		labelTask.setTask_label_type_info(body.getTaskLabelTypeInfo());
		if(body.getAssign_user_id() == 0) {
			labelTask.setAssign_user_id(userId);
		}else {
			labelTask.setAssign_user_id(body.getAssign_user_id());
		}
		labelTask.setTask_flow_type(body.getTask_flow_type());
		labelTask.setRelate_other_label_task(body.getRelate_other_label_task());
		labelTask.setTask_status(0);
		labelTask.setAppid(body.getAppid());
		
		Map<String,String> userInfo = new HashMap<>();
		String userName = pgDbService.getUserName(userId);
		userInfo.put(UserConstants.USER_NAME, userName);
		userInfo.put(UserConstants.ASSIGN_USER_NAME, userName);
		
		labelTask.setUser_info(JsonUtil.toJson(userInfo));
		String taskId = body.getRelateTaskId();
		DataSet dataSet = dataSetDao.queryDataSetById(taskId);
		
		if(body.getTaskType() == Constants.LABEL_TASK_TYPE_ORIGIN || body.getTaskType() == Constants.LABEL_TASK_TYPE_AUTO) {
			if(dataSet == null) {
				throw new LabelSystemException("关联的任务ID错误。");
			}
			if(dataSet.getTotal() == 0) {
				throw new LabelSystemException("数据集中图片数量为0，不能创建人工标注。");
			}
			labelTask.setRelate_task_name(dataSet.getTask_name());
			dealDataSetLabelTask(dataSet, labelTask);
			if(body.getTaskType() == Constants.LABEL_TASK_TYPE_AUTO) {
				labelTask.setTask_type(Constants.LABEL_TASK_TYPE_AUTO);
			}
		}

		labelTaskDao.addLabelTask(labelTask);
		if(body.getTaskType() == Constants.LABEL_TASK_TYPE_AUTO) {
			//更新状态为自动标注中
			Map<String,Object> paramMap = new HashMap<>();
			paramMap.put("id", labelTask.getId());
			paramMap.put("task_status", Constants.LABEL_TASK_STATUS_AUTO_LABEL_PROGRESS);
			labelTaskDao.updateLabelTaskStatus(paramMap);
			
			try {
				registObjectDetectionService.doExec(labelTask, body.getModelId());
			}catch (Exception e) {
				//自动标注失败
				logger.info(e.getMessage());
				paramMap.put("task_status", Constants.LABEL_TASK_STATUS_AUTO_LABEL_EXCEPTION);
				labelTaskDao.updateLabelTaskStatus(paramMap);
			}
		}
		return 1;
	}


	public List<LabelTaskItem> queryLabelTaskItemByLabelTaskIdAndPicImage(String token, String labelTaskId,String picImageListStr){


		LabelTask labelTask = labelTaskDao.queryLabelTaskById(labelTaskId);

		List<String> picImageList = gson.fromJson(picImageListStr, new TypeToken<List<String>>() {
			private static final long serialVersionUID = 1L;}.getType());

		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("label_task_id", labelTaskId);
		paramMap.put("picList", picImageList);
		paramMap.put("user_id", 
				 giteaDbPolicy.getTableName(labelTaskId, UserConstants.LABEL_TASK_SINGLE_TABLE,-1));

		if(labelTask != null) {
			return labelTaskItemDao.queryLabelTaskItemByLabelTaskIdAndPicImage(paramMap);
		}

		return new ArrayList<>();
	}


	public PageResult queryLabelTaskItemPageByLabelTaskId(String labelTaskId,int currPage, int pageSize, int orderType, int findLast,String token){
		
		PageResult pageResult = new PageResult();

		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("currPage", currPage * pageSize);
		paramMap.put("pageSize", pageSize);
		paramMap.put("label_task_id", labelTaskId);
		paramMap.put("orderType", orderType);
		paramMap.put("user_id", giteaDbPolicy.getTableName(labelTaskId, UserConstants.LABEL_TASK_SINGLE_TABLE,-1));
		
		LabelTask labelTask = labelTaskDao.queryLabelTaskById(labelTaskId);

		if(labelTask != null) {

			if(findLast == Constants.QUERY_ITEM_PAGE_FIND_LAST) {
				logger.info("findLast order.");
				if(labelTask.getTask_type() == Constants.LABEL_TASK_TYPE_ORIGIN_DCM) {
					int totalCount = labelDcmTaskItemDao.queryLabelTaskItemPageCountByLabelTaskId(paramMap);
					List<LabelTaskItem> result = new ArrayList<>();
					
					int count = 0;
					while(count < totalCount) {
						
						result = labelDcmTaskItemDao.queryLabelTaskItemPageByLabelTaskId(paramMap);
						boolean isExistNotFinished = false;

						for(LabelTaskItem item :result) {
							if(item.getLabel_status() == Constants.LABEL_TASK_STATUS_NOT_FINISHED) {
								isExistNotFinished = true;
								break;
							}
						}
						if(isExistNotFinished) {
							break;
						}else {
							currPage ++;
							count += result.size();
							paramMap.put("currPage", currPage * pageSize);
						}
					}
					pageResult.setTotal(totalCount);
					pageResult.setData(result);
					pageResult.setCurrent(currPage);
				}else {
					int totalCount = labelTaskItemDao.queryLabelTaskItemPageCountByLabelTaskId(paramMap);
					List<LabelTaskItem> result = new ArrayList<>();
					
					int count = 0;
					while(count < totalCount) {
						
						result = labelTaskItemDao.queryLabelTaskItemPageByLabelTaskId(paramMap);
						boolean isExistNotFinished = false;

						for(LabelTaskItem item :result) {
							if(item.getLabel_status() == Constants.LABEL_TASK_STATUS_NOT_FINISHED) {
								isExistNotFinished = true;
								break;
							}
						}
						if(isExistNotFinished) {
							break;
						}else {
							currPage ++;
							count += result.size();
							paramMap.put("currPage", currPage * pageSize);
						}
					}
					pageResult.setTotal(totalCount);
					pageResult.setData(result);
					pageResult.setCurrent(currPage);
				}

			}
			
			else {
				if(findLast == Constants.QUERY_ITEM_PAGE_MIAOD) {
					paramMap.put("orderType", 2);
				}
				
				List<LabelTaskItem> result = labelTaskItemDao.queryLabelTaskItemPageByLabelTaskId(paramMap);
				int totalCount = labelTaskItemDao.queryLabelTaskItemPageCountByLabelTaskId(paramMap);
				pageResult.setTotal(totalCount);
				pageResult.setData(result);
				
				pageResult.setCurrent(currPage);
			}
		}

		return pageResult;
	}


	public int updateLabelTaskItem(LabelTaskItem updateBody,String token) {

		Map<String,Object> paramMap = new HashMap<>();
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		LabelTaskItem oldLabelTaskItem = labelTaskItemDao.queryLabelTaskItemById(giteaDbPolicy.getTableName(updateBody.getLabel_task_id(), UserConstants.LABEL_TASK_SINGLE_TABLE,-1),updateBody.getId());

		if(oldLabelTaskItem != null) {
			if(labelEquals(oldLabelTaskItem.getLabel_info(),updateBody.getLabel_info())){
				if(updateBody.getLabel_status() == oldLabelTaskItem.getLabel_status()) {
					return 0;
				}
			}
		}
		//logger.info("udpate item:" + JsonUtil.toJson(updateBody));
		String time = TimeUtil.getCurrentTimeStr();
		updateBody.setItem_add_time(time);

		paramMap.put("id", updateBody.getId());
		paramMap.put("label_info", updateBody.getLabel_info());
		
		paramMap.put("label_status", getLabelStatus(updateBody));
		
		
		if(updateBody.getDisplay_order2() == LABEL_UPDATE_FLAG) {//如果是标注更新，则需要将审核状态修改为未审核
		    paramMap.put("verify_status", 0);//修改成未审核
		}else {
			paramMap.put("verify_status", updateBody.getVerify_status());
		}
		paramMap.put("pic_object_name", updateBody.getPic_object_name());
		paramMap.put("item_add_time", updateBody.getItem_add_time());
		
		paramMap.put("user_id", giteaDbPolicy.getTableName(updateBody.getLabel_task_id(), UserConstants.LABEL_TASK_SINGLE_TABLE,-1));

		int re = labelTaskItemDao.updateLabelTaskItem(paramMap);

		
		LabelTask labelTask = labelTaskDao.queryLabelTaskById(oldLabelTaskItem.getLabel_task_id());
		
		if(labelTask.getTask_status() == Constants.LABEL_TASK_STATUS_AUTO_LABEL_EXCEPTION) {
			Map<String,Object> statusMap = new HashMap<>();
			statusMap.put("id", labelTask.getId());
			statusMap.put("task_status", Constants.LABEL_TASK_STATUS_LABEL);
			labelTaskDao.updateLabelTaskStatus(statusMap);
		}
		
		Map<String,String> extend = new HashMap<>();
		extend.put("label_user_id", String.valueOf(labelTask.getUser_id()));	
		logUpdateLabelTaskItem(oldLabelTaskItem,updateBody,userId,JsonUtil.toJson(extend));

		return re;
	}
	
	

	//只有当新旧比较时，仅且多了一个不合格的属性时，才由标注完成转换成标注未完成。
	private int getLabelStatus(LabelTaskItem updateBody) {
		if(updateBody.getDisplay_order2() != VERIFY_UPDATE_FLAG) {//审核状态更新，需要将display_order2状态设置100
			return updateBody.getLabel_status();
		}
		List<Map<String,Object>> newList = JsonUtil.getLabelList(updateBody.getLabel_info());
		
		for(Map<String,Object> label : newList) {
			if(label.get("other") != null ) {
				Map<String,Object> newOther = (Map<String,Object>)label.get("other");
				Map<String,Object> newregion_attributes = (Map<String,Object>)newOther.get(LogConstants.REGION_ATTRIBUTES);
				if(newregion_attributes != null && newregion_attributes.get(LogConstants.VERIFY_FIELD) != null) {
					if(newregion_attributes.get(LogConstants.VERIFY_FIELD).toString().equals(NOT_VALIDE_VALUE)) {
						logger.info("only exist verify == 1");
						return Constants.LABEL_TASK_STATUS_NOT_FINISHED;
					}
				}
			}
		}
		return updateBody.getLabel_status();
	}
	

	private void logUpdateLabelTaskItem(LabelTaskItem oldLabelTaskItem, LabelTaskItem updateBody,int user_id,String extend1) {
		LogInfo logInfo = new LogInfo();
		logInfo.setOper_type(LogConstants.LOG_UPATE);
		logInfo.setUser_id(user_id);
		logInfo.setOper_name("通用标注");
		logInfo.setOper_id(LogConstants.LOG_NORMAL_UPDATE_LABEL_ITEM);
		logInfo.setOper_time_start(updateBody.getItem_add_time());
		logInfo.setOper_time_end(updateBody.getItem_add_time());
		logInfo.setOper_json_content_new(updateBody.getLabel_info());
		logInfo.setOper_json_content_old(oldLabelTaskItem.getLabel_info());
		logInfo.setRecord_id(oldLabelTaskItem.getId());
		logInfo.setExtend1(extend1);
		logInfo.setExtend2(oldLabelTaskItem.getPic_image_field());
		logService.addLogInfo(logInfo);
	}


	public int deleteLabelTaskById(String token, String labelTaskId) {
		
			//数据库删除
		labelTaskItemDao.deleteLabelTaskById(giteaDbPolicy.getTableName(labelTaskId, UserConstants.LABEL_TASK_SINGLE_TABLE,-1),labelTaskId);
		
		return labelTaskDao.deleteLabelTaskById(labelTaskId);
	}

	public List<DisplayLabelTask> queryLabelTaskByRelatedDataSetId(String token, String dataSetId){
		List<DisplayLabelTask> re = new ArrayList<>();
		List<LabelTask> taskList = labelTaskDao.queryLabelTaskByDataSetId(dataSetId);

		Map<Integer,String> userIdForName = userService.getAllUser();

		for(LabelTask task : taskList) {
			DisplayLabelTask displayLabelTask = new DisplayLabelTask();
			displayLabelTask.setId(task.getId());
			displayLabelTask.setTask_name(task.getTask_name());
			displayLabelTask.setTask_flow_type(task.getTask_flow_type());
			if(task.getAssign_user_id() == 0) {
				displayLabelTask.setAssign_user(userIdForName.get(task.getUser_id()));
			}else {
				displayLabelTask.setAssign_user(userIdForName.get(task.getAssign_user_id()));
			}
			re.add(displayLabelTask);
		}
		return re;
	}




	private boolean labelEquals(String old_label_info, String new_label_info) {
		if(Strings.isNullOrEmpty(old_label_info) && Strings.isNullOrEmpty(new_label_info)) {
			return true;
		}
		if(Strings.isNullOrEmpty(old_label_info) && "[]".equals(new_label_info)) {
			return true;
		}
		if(!Strings.isNullOrEmpty(old_label_info) && !Strings.isNullOrEmpty(new_label_info)) {
			if(old_label_info.equals(new_label_info)) {
				return true;
			}
		}
		return false;
	}





	public int updateLabelTaskItemStatus(LabelTaskItem updateBody, String token) {
		String time = TimeUtil.getCurrentTimeStr();
		updateBody.setItem_add_time(time);
		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("id", updateBody.getId());
		paramMap.put("label_status", updateBody.getLabel_status());
		paramMap.put("item_add_time", updateBody.getItem_add_time());
		paramMap.put("user_id", giteaDbPolicy.getTableName(updateBody.getLabel_task_id(), UserConstants.LABEL_TASK_SINGLE_TABLE,-1));
		return labelTaskItemDao.updateLabelTaskItem(paramMap);
		
	}

	public int updateLabelTaskStatus(String token, String labelTaskId,int verifyUserId, int taskStatus) {
		
		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("id", labelTaskId);
		paramMap.put("task_status", taskStatus);
		if(verifyUserId != -1) {
			paramMap.put("verify_user_id", verifyUserId);
			//如果转换为审核状态，给每个框增加一个是否合格的属性
			if(verifyUserId > 0) {
				LabelTask labelTask = labelTaskDao.queryLabelTaskById(labelTaskId);
				Map<String,Object> map = JsonUtil.getMap(labelTask.getTask_label_type_info());
				if(map.isEmpty()) {
					putIdAndDefaultType(map);
				}
				if(!map.containsKey("verify")) {
					Map<String,Object> verify = new HashMap<>();
					map.put("verify", verify);
					verify.put("type", "dropdown");
					verify.put("description", "用于审核状态是否此框是否合格。");
					Map<String,Object> optionsMap = new HashMap<>();
					verify.put("options", optionsMap);
					optionsMap.put(LogConstants.VERIFY_FIELD_RESULT_VALID_0, "合格");
					optionsMap.put(LogConstants.VERIFY_FIELD_RESULT_NOTVALID_1, "大小不合格");
					optionsMap.put(LogConstants.VERIFY_FIELD_RESULT_NOTVALID_2, "颜色不合格");
					optionsMap.put(LogConstants.VERIFY_FIELD_RESULT_NOTVALID_3, "其它不合格");
					paramMap.put("task_label_type_info", JsonUtil.toJson(map));
				}
			}
		}
		logger.info("update label task status:" + paramMap.toString());
		return labelTaskDao.updateLabelTaskStatus(paramMap);
	}

	
	private void putIdAndDefaultType(Map<String,Object> map) {
		
		Map<String,Object> id = new HashMap<>();
		map.put("id", id);
		id.put("type", "text");
		id.put("description", "标注框id，在一张图片中唯一，数字。");
		id.put("default_value", "");

		Map<String,Object> defaultType = new HashMap<>();
		map.put("type", defaultType);
		defaultType.put("type", "dropdown");
		defaultType.put("description", "缺省标注类型，car或者person。");
		Map<String,Object> optionsMap = new HashMap<>();
		defaultType.put("options", optionsMap);
		optionsMap.put("car", "car");
		optionsMap.put("person", "person");
		optionsMap.put("non-motor", "non-motor");
		
	}
	
	public int updateLabelItemVerifyStatus(LabelTaskItem updateBody, String token) {
		String time = TimeUtil.getCurrentTimeStr();
		updateBody.setItem_add_time(time);
		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("id", updateBody.getId());
		paramMap.put("verify_status", updateBody.getVerify_status());
		paramMap.put("item_add_time", updateBody.getItem_add_time());
		paramMap.put("user_id", giteaDbPolicy.getTableName(updateBody.getLabel_task_id(), UserConstants.LABEL_TASK_SINGLE_TABLE,-1));
		return labelTaskItemDao.updateLabelTaskItem(paramMap);
	}

	public void deleteLabel(String labelTaskId, Integer startId, Integer endId, String one_name, String token) {
		
		
		List<LabelTaskItem> taskItem = labelTaskItemDao.queryLabelTaskItemByLabelTaskId(giteaDbPolicy.getTableName(labelTaskId, UserConstants.LABEL_TASK_SINGLE_TABLE,-1),labelTaskId);
		List<LabelTaskItem> deleteItemList = new ArrayList<>();
		for(int i = startId - 1; i< taskItem.size() && i < endId; i++) {
			deleteItemList.add(taskItem.get(i));
		}
		for(LabelTaskItem deleteItem : deleteItemList) {
			Map<String,Object> paramMap = new HashMap<>();
			paramMap.put("id", deleteItem.getId());
			
			paramMap.put("label_info", JsonUtil.toJson(new ArrayList<>()));
			paramMap.put("label_status", Constants.LABEL_TASK_STATUS_NOT_FINISHED);
			paramMap.put("user_id", giteaDbPolicy.getTableName(labelTaskId, UserConstants.LABEL_TASK_SINGLE_TABLE,-1));
			paramMap.put("item_add_time", TimeUtil.getCurrentTimeStr());
			labelTaskItemDao.updateLabelTaskItem(paramMap);
		}
	}
	
}
