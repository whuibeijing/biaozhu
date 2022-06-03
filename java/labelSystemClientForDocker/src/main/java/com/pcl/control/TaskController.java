package com.pcl.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.pcl.pojo.ObjectDetectionTask;
import com.pcl.pojo.ObjectTrackTask;
import com.pcl.service.TaskService;
import com.pcl.util.JsonUtil;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/task")
public class TaskController {

	private static Logger logger = LoggerFactory.getLogger(TaskController.class);
	
	@Autowired
	private TaskService taskService;
	/**
	 * 	通知使用此模型的URL。
	  *        消息体：{
	  *       taskId:   此次消息id，在msgCallBackUrl中要带回此taskId
	  *       objectFilePath: 对象文件存储地址（有可能是目录，也可能只是一个文件）
	  *       msgCallBackUrl: 消息回调URL，即返回标注信息
	  *      }
	 * 
	 */
	@ApiOperation(value="接收一个目标检测的任务", notes="接收一个目标检测的任务")
	@RequestMapping(value="/receiveObjectDetectionTask", method = RequestMethod.POST, produces ="application/json;charset=utf-8")
	public String receiveTask(@RequestBody ObjectDetectionTask task) {
		logger.info("receive a detection task:" + JsonUtil.toJson(task));
		
		taskService.dealDetectionTask(task);
		
		return "true";
	}
	
	@ApiOperation(value="接收一个目标跟踪的任务", notes="接收一个目标跟踪的任务")
	@RequestMapping(value="/receiveObjectTrackTask", method = RequestMethod.POST, produces ="application/json;charset=utf-8")
	public String receiveTrackTask(@RequestBody ObjectTrackTask task) {
		logger.info("receive a track task:" + JsonUtil.toJson(task));
		
		taskService.dealTrackTask(task);
		
		return "true";
	}
	
}
