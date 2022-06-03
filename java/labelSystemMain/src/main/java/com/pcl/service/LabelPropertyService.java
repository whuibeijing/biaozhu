package com.pcl.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pcl.dao.LabelPropertyTaskDao;
import com.pcl.dao.LabelTaskDao;
import com.pcl.dao.ReIDTaskDao;
import com.pcl.exception.LabelSystemException;
import com.pcl.pojo.PageResult;
import com.pcl.pojo.Result;
import com.pcl.pojo.display.DisplayLabelTask;
import com.pcl.pojo.mybatis.LabelPropertyTask;
import com.pcl.pojo.mybatis.LabelTask;
import com.pcl.pojo.mybatis.ReIDTask;
import com.pcl.util.JsonUtil;
import com.pcl.util.TimeUtil;

//标注属性导入导出服务
@Service
public class LabelPropertyService {

	private static final int NOT_EDIT = 1;

	@Autowired
	private LabelTaskDao labelTaskDao;

	@Autowired
	private ReIDTaskDao reIdTaskDao;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private LabelPropertyTaskDao labelPropertyTaskDao;
		
	private static Logger logger = LoggerFactory.getLogger(LabelExportService.class);
	
	public void queryLabelProperty(String token, String taskId,String type,HttpServletResponse response) {
		logger.info("start to writer label property json. id=" + taskId);
		response.setContentType("application/force-download");// 设置强制下载不打开
		response.setHeader("Content-Disposition", "attachment;fileName="+ taskId + ".json");  
		String json = "";
		try(OutputStream fos = response.getOutputStream()) {
			if(type.equals("reid")) {
				ReIDTask task = reIdTaskDao.queryReIDTaskById(taskId);
				if(task != null) {
					json = task.getTask_label_type_info();
				}
			}else if(type.equals("labeltask")) {
				LabelTask task = labelTaskDao.queryLabelTaskById(taskId);
				if(task != null) {
					json = task.getTask_label_type_info();
				}
			}else if(type.equals("labelpropertytask")) {
				LabelPropertyTask task = labelPropertyTaskDao.queryLabelPropertyTaskById(taskId);
				if(task != null) {
					logger.info("task is not null.");
					json = task.getPropertyJson();
				}
			}
			logger.info("json=" + json);
			fos.write(json.getBytes("utf-8"));
			fos.flush();
		} catch (IOException e) {

			e.printStackTrace();
		}
		logger.info("end to writer label property json.");
	}

	public Result importLabelPropertyJson(String token, String jsonContent, String taskType, String taskId) {
	
		try {
			checkLabelJson(jsonContent);
		} catch (LabelSystemException e) {
			Result re = new Result();
			re.setCode(1);
			re.setMessage(e.getMessage());
			return re;
		}
	
		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("id", taskId);
		paramMap.put("task_label_type_info", jsonContent);
		if(taskType.equals("reid")) {
			reIdTaskDao.updateReIDTaskSelfDefineInfo(paramMap);
		}else if(taskType.equals("labeltask")) {
			labelTaskDao.updateLabelTask(paramMap);
		}
		Result re = new Result();
		re.setCode(0);
		return re;
	}
	
	public static void checkLabelJson(String jsonContent) throws LabelSystemException{
		Map<String,Object> jsonMap = JsonUtil.getMap(jsonContent);
		
		if(jsonMap == null) {
			throw new LabelSystemException("导入的Json非法。");
		}
		
		if(jsonMap.get("id") == null) {
			throw new LabelSystemException("导入的标注Json中必需包括id及type属性。");
		}
		
		for(Entry<String,Object> entry :jsonMap.entrySet()) {
			Object valueObj = entry.getValue();
			if(!(valueObj instanceof Map)) {
				throw new LabelSystemException("导入的标注Json中属性格式错误。");
			}
			
			Map<String,Object> valueMap = (Map<String,Object>)valueObj;
			if(valueMap.get("type") == null) {
				throw new LabelSystemException("导入的标注Json中属性没有标注类别。");
			}
			String typeValue = valueMap.get("type").toString();
			if(!Arrays.asList("dropdown","text","checkbox","radio").contains(typeValue)) {
				throw new LabelSystemException("导入的标注Json中属性标注类别应该为：dropdown,text,checkbox,radio四种之一。");
			}
		}
		
	}

	
	public PageResult selectLabelProperty(String token, Integer currPage, Integer pageSize) {
		
		PageResult pageResult = new PageResult();
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		
		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("currPage", currPage * pageSize);
		paramMap.put("pageSize", pageSize);
		paramMap.put("user_id", userId);
		List<LabelPropertyTask> labelTaskList = labelPropertyTaskDao.queryLabelPropertyTask(paramMap);

		
		
		int totalCount = labelPropertyTaskDao.queryLabelPropertyTaskCount(paramMap);
		
		Map<Integer,String> userIdForName = userService.getAllUser();
		for(LabelPropertyTask labelPropertyTask : labelTaskList) {
			labelPropertyTask.setAssign_user(userIdForName.get(labelPropertyTask.getUser_id()));
			if(labelPropertyTask.getUser_id() != userId) {
				labelPropertyTask.setTask_status(NOT_EDIT);
			}
		}
		
		pageResult.setTotal(totalCount);
		pageResult.setData(labelTaskList);
		pageResult.setCurrent(currPage);
		
		
		return pageResult;
	}

	public void deleteLabelPropertyById(String token, String labelPropertyTaskId) throws LabelSystemException {
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		
		LabelPropertyTask dbTask = labelPropertyTaskDao.queryLabelPropertyTaskById(labelPropertyTaskId);
		
		if(dbTask.getUser_id() != userId) {
			logger.info("You cannot delete other people's tasks. task=" + JsonUtil.toJson(dbTask));
			throw new LabelSystemException(1,"You cannot delete other people's tasks.");
		}
		
		labelPropertyTaskDao.deleteLabelPropertyTaskById(labelPropertyTaskId);
		
	}

	public void addLabelProperty(String token, LabelPropertyTask body) throws LabelSystemException {
		
		
		checkLabelJson(body.getPropertyJson());
		
		
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		body.setUser_id(userId);
		
		if(body.getAssign_user_id() <=0) {
			body.setAssign_user_id(userId);
		}
		
		body.setId(UUID.randomUUID().toString().replaceAll("-",""));
		body.setTask_add_time(TimeUtil.getCurrentTimeStr());
		
		labelPropertyTaskDao.addLabelPropertyTask(body);
		
	}

	public void updateLabelProperty(String token, LabelPropertyTask body) throws LabelSystemException {
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		
		checkLabelJson(body.getPropertyJson());
		
		LabelPropertyTask dbTask = labelPropertyTaskDao.queryLabelPropertyTaskById(body.getId());
		
		if(dbTask.getUser_id() != userId) {
			throw new LabelSystemException("Cannot modify other user task.");
		}
		
		Map<String,Object> paramMap = new HashMap<>();
		
		paramMap.put("id", body.getId());
		
		paramMap.put("task_type", body.getTask_type());
		paramMap.put("task_name", body.getTask_name());
		paramMap.put("propertyJson", body.getPropertyJson());
		paramMap.put("task_add_time", TimeUtil.getCurrentTimeStr());
		
		
		labelPropertyTaskDao.updateLabelPropertyTask(paramMap);
	}

	public List<LabelPropertyTask> selectAllLabelProperty(String token) {
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("user_id", userId);
		List<LabelPropertyTask> labelTaskList = labelPropertyTaskDao.queryAllLabelPropertyTask(paramMap);

		return labelTaskList;
	}


}
