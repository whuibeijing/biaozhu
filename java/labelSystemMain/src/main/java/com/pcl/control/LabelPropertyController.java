package com.pcl.control;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.pcl.exception.LabelSystemException;
import com.pcl.pojo.PageResult;
import com.pcl.pojo.Result;
import com.pcl.pojo.body.PrePredictTaskBody;
import com.pcl.pojo.mybatis.LabelPropertyTask;
import com.pcl.service.LabelPropertyService;
import com.pcl.util.JsonUtil;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api")
public class LabelPropertyController {
	
	private static Logger logger = LoggerFactory.getLogger(LabelPropertyController.class);
	
	@Autowired
	private LabelPropertyService  labelPropertyService;

	@Autowired
	private HttpServletResponse response; 
	
	@Autowired
	HttpServletRequest request;

	@ResponseBody
	@ApiOperation(value="导入指定任务的标注属性", notes="返回导入结果")
	@RequestMapping(value ="/task-import-label-property", method = RequestMethod.POST)
	public Result importLabelPropertyJson(@RequestParam("jsonContent") String jsonContent,@RequestParam("taskType") String taskType,@RequestParam("taskId") String taskId) {
		
		String token = request.getHeader("authorization");
		logger.info("importLabelPropertyJson, jsonContent =" + jsonContent + " taskType=" + taskType + " taskId=" + taskId);
		return labelPropertyService.importLabelPropertyJson(token,jsonContent,taskType,taskId);
	}
	
	@ApiOperation(value="根据指定标注任务查询该任务导出该标注任务设置的属性", notes="返回标注属性文件流")
	@RequestMapping(value="/task-export-label-property", method = RequestMethod.GET)
	public void exportLabelProperty(@RequestParam("task_id") String taskId,@RequestParam("type") String type) {
		String token = request.getHeader("authorization");
		logger.info("queryLabelProperty, taskId =" + taskId + " type=" + type);
		labelPropertyService.queryLabelProperty(token,taskId,type,response);
		
	}
	
	@ApiOperation(value="分页查询标注属性任务", notes="返回标注属性任务")
	@RequestMapping(value = "/label-property-task-page",method = RequestMethod.GET)
	public PageResult queryLabelPropertyTaskPage(@RequestParam("startPage") Integer startPage, @RequestParam("pageSize") Integer pageSize) throws LabelSystemException {
		String token = request.getHeader("authorization");
		if(token == null) {
			throw new LabelSystemException("user not login.");
		}
		logger.info("queryLabelPropertyTaskPage  token =" + token + ", startPage=" + startPage + " pageSize=" + pageSize);

		return  labelPropertyService.selectLabelProperty(token,startPage,pageSize);
	}
	
	@ApiOperation(value="分页查询标注属性任务", notes="返回标注属性任务")
	@RequestMapping(value = "/label-property-task-all",method = RequestMethod.GET)
	public List<LabelPropertyTask> queryAllLabelPropertyTask() throws LabelSystemException {
		String token = request.getHeader("authorization");
		if(token == null) {
			throw new LabelSystemException("user not login.");
		}
		logger.info("queryAllLabelPropertyTask  token =" + token );

		return  labelPropertyService.selectAllLabelProperty(token);
	}
	
	@RequestMapping(value = "/add-label-property-task",method = RequestMethod.POST, produces ="application/json;charset=utf-8")
	public Result addLabelPropertyTask(@RequestBody LabelPropertyTask body) {
		logger.info("LabelPropertyTask, body =" + JsonUtil.toJson(body));

		Result re = new Result();
		try {
			String token = request.getHeader("authorization");
			
			labelPropertyService.addLabelProperty(token, body);
			re.setCode(0);
		}catch (Exception e) {
			e.printStackTrace();
			re.setCode(1);
			re.setMessage(e.getMessage());
		}
		return re;
	}
	
	@RequestMapping(value = "/update-label-property-task",method = RequestMethod.POST, produces ="application/json;charset=utf-8")
	public Result updateLabelPropertyTask(@RequestBody LabelPropertyTask body) {
		logger.info("update LabelPropertyTask, body =" + JsonUtil.toJson(body));

		Result re = new Result();
		try {
			String token = request.getHeader("authorization");
			
			labelPropertyService.updateLabelProperty(token, body);
			re.setCode(0);
		}catch (Exception e) {
			e.printStackTrace();
			re.setCode(1);
			re.setMessage(e.getMessage());
		}
		return re;
	}
	
	@RequestMapping(value = "/delete-label-property-task",method = RequestMethod.DELETE)
	public Result deleteLabelPropertyTask(@RequestParam("property_task_id") String propertyTaskId) {
		logger.info("delete LabelProperty Task, id =" + propertyTaskId);

		Result re = new Result();
		try {
			String token = request.getHeader("authorization");
			
			labelPropertyService.deleteLabelPropertyById(token, propertyTaskId);
			re.setCode(0);
		}catch (Exception e) {
			e.printStackTrace();
			re.setCode(1);
			re.setMessage(e.getMessage());
		}
		return re;
	}
}
