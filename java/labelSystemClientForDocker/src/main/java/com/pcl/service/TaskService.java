package com.pcl.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.pcl.exception.LabelSystemException;
import com.pcl.pojo.AutoLabelModelRegistMsg;
import com.pcl.pojo.ObjectDetectionTask;
import com.pcl.pojo.ObjectTrackTask;
import com.pcl.util.FileUtil;
import com.pcl.util.JsonUtil;
import com.pcl.util.ProcessExeUtil;
import com.pcl.util.RestUtil;

@Service
public class TaskService {

	private static Logger logger = LoggerFactory.getLogger(TaskService.class);
	
	public final static String REID_KEY = "reId";

	private ConcurrentHashMap<String, AutoLabelModelRegistMsg> modelMap = new ConcurrentHashMap<>();

	private  static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(8,16,100,TimeUnit.MINUTES,new ArrayBlockingQueue<Runnable>(2000));

	/**
	 * 	通知使用此模型的URL。
	 *      消息体：{
	 *       taskId:   此次消息id，发送消息中要带回此taskId
	 *       objectFilePath: 对象文件存储地址（有可能是目录，也可能只是一个文件）
	 *      }
	 * 
	 */
	@Value("${agent_receive_task_url}")
	private String agent_receive_task_url;

	@Value("${label_system_model_regist_url}")
	private String labelSystemModelRegistUrl;
	
	@Value("${label_system_model_receive_msg:1}")
	private String labelSystemModelDetectionMsgUrl;
	
	@Value("${label_system_model_receive_track_msg:1}")
	private String labelSystemModelTrackMsgUrl;

	@Autowired
	private DockerObjectFileService dockerObjectFileService;

	public void dealDetectionTask(ObjectDetectionTask task) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					exec(task);
				} catch (LabelSystemException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void dealTrackTask(ObjectTrackTask task) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					exec(task);
				} catch (LabelSystemException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void exec(ObjectTrackTask task) throws LabelSystemException{
		long time = System.currentTimeMillis();
		AutoLabelModelRegistMsg model = modelMap.get(task.getModelName());
		if(model == null) {
			logger.info("model is null. model name=" + task.getModelName() + " taskid=" + task.getTaskId());
			return;
		}else {
			logger.info("model=" + JsonUtil.toJson(model));
		}
		File inputFile = new File(model.getInput_path(),String.valueOf(time));
		if(!inputFile.exists()) {
			inputFile.mkdirs();
		}

		String destFilePath = inputFile.getAbsolutePath() + File.separator;
		logger.info("start to download file, destFilePath=" + destFilePath);
		//下载文件
		if(task.getPictureList() != null) {
			int length = task.getPictureList().size();
			if(task.getPictureIdList().size() != length) {
				logger.info("input error.task.getPictureIdList().size()=" + task.getPictureIdList().size() + " length=" + length);
				return;
			}
			for(int i = 0; i < length; i++) {
				String picture = task.getPictureList().get(i);
				String id = (i+1) + "";
				String postFix = picture.substring(picture.lastIndexOf("."));

				dockerObjectFileService.downLoadAFile(picture, destFilePath + id + postFix);
			}
		}else {
			logger.info("need to download picture is null.");
			return;
		}
		
		File outputFile = new File(model.getOutput_path(), String.valueOf(System.currentTimeMillis()));
		outputFile.mkdirs();
		logger.info("output path=" + outputFile.getAbsolutePath());
		String script = model.getExe_script();

		File resultFile = new File(outputFile,"result.txt");
		
		script += " --input_dir " + destFilePath;
		script += " --output_dir " + resultFile.getAbsolutePath();
		script += " --taskid " + task.getTaskId();
		script += " --callback_url "+ labelSystemModelTrackMsgUrl;

		logger.info("start to exec script:" + script);
		//每张图片2秒钟超时时间，再加10分钟基础时间
		int timeSeconds = task.getPictureList().size() * 2 + 600;

		ProcessExeUtil.execScript(script, model.getAlgRootPath(), timeSeconds);
		
		if(resultFile.exists()) {
			logger.info("result file is existed.");
			if(model.getModel_type() == AutoLabelModelRegistMsg.TRACK_MULT) {//多目标跟踪
				multiTrackingResult(task, resultFile);
			}
		}else {
			logger.info("result file not existed.");
		}
		
	}
	
	

	private void multiTrackingResult(ObjectTrackTask task, File resultFile) {
		//多目标跟踪结果处理
		List<String> contentList = FileUtil.getAllLineList(resultFile.getAbsolutePath(), "utf-8");
		
		/*
		msg={}
        msg['type']="1"  #label picture
        msg['taskid']= args.taskid
        msg['index']= str(tmpCount)
        msg['total']= str(len(dataset))
        msg['filename'] = name
        msg['label_info'] = box_list
        */
		RestUtil rest = new RestUtil();
		Map<String,Object> msgMap = new HashMap<>();
		List<Map<String,Object>> boxList = new ArrayList<>();
		int first = -1;
		for(String line : contentList) {
			String tmp[] = line.split(" ");
			//tmp[0]  图片下标
			//tmp[1]  目标跟踪的ID
			//tmp[2],[3],[4],[5], 目标跟踪的坐标
			//tmp[6,7,8], 目标为-1
			//tmp[9] 为目标类别
			
			int id = Integer.parseInt(tmp[0]);
			
			if(id != first) {
				if(boxList.size() > 0) {
					//send msg
					msgMap.put("type", String.valueOf(AutoLabelModelRegistMsg.TRACK_MULT));
					msgMap.put("taskid", task.getTaskId());
					msgMap.put("index", first);
					msgMap.put("total", task.getPictureList().size());
					msgMap.put("filename", task.getPictureIdList().get(first - 1) + ".jpg");
					msgMap.put("label_info", boxList);
					if(task.getUseSence() == 1) {
						msgMap.put("reId", "true");
					}
					logger.info("msg=" + JsonUtil.toJson(msgMap));
					rest.post(labelSystemModelTrackMsgUrl, JsonUtil.toJson(msgMap));
					//send msg
					boxList = new ArrayList<>();
				}
				first = id;
			}
			Map<String,Object> map = new HashMap<>();
			map.put("id", tmp[1]);
			map.put("class_name", tmp[9]);
			List<String> box = new ArrayList<>();
			box.add(tmp[2]);
			box.add(tmp[3]);
			box.add(tmp[4]);
			box.add(tmp[5]);
			map.put("box", box);
			if(task.getUseSence() == 1) {
				map.put(REID_KEY,tmp[1]);
			}
			
			boxList.add(map);
		}
		
		if(boxList.size() > 0) {
			//send msg
			msgMap.put("type", String.valueOf(AutoLabelModelRegistMsg.TRACK_MULT));
			msgMap.put("taskid", task.getTaskId());
			msgMap.put("index", first);
			msgMap.put("total", task.getPictureList().size());
			msgMap.put("filename", task.getPictureIdList().get(first - 1) + ".jpg");
			msgMap.put("label_info", boxList);
			if(task.getUseSence() == 1) {
				msgMap.put("reId", "true");
			}
			//send msg
			logger.info("msg=" + JsonUtil.toJson(msgMap));
			rest.post(labelSystemModelTrackMsgUrl, JsonUtil.toJson(msgMap));
		}
		
	}

	private void exec(ObjectDetectionTask task) throws LabelSystemException {
		long time = System.currentTimeMillis();
		AutoLabelModelRegistMsg model = modelMap.get(task.getModelName());
		if(model == null) {
			logger.info("model is null. model name=" + task.getModelName() + " taskid=" + task.getTaskId());
			return;
		}
		File inputFile = new File(model.getInput_path(),String.valueOf(time));
		if(!inputFile.exists()) {
			inputFile.mkdirs();
		}

		String destFilePath = inputFile.getAbsolutePath() + File.separator;
		logger.info("start to download file, destFilePath=" + destFilePath);
		//下载文件
		if(task.getPictureList() != null) {
			int length = task.getPictureList().size();
			if(task.getPictureIdList().size() != length) {
				logger.info("input error.task.getPictureIdList().size()=" + task.getPictureIdList().size() + " length=" + length);
				return;
			}
			for(int i = 0; i < length; i++) {
				String picture = task.getPictureList().get(i);
				String id = task.getPictureIdList().get(i);
				String postFix = picture.substring(picture.lastIndexOf("."));

				dockerObjectFileService.downLoadAFile(picture, destFilePath + id + postFix);
			}
		}else {
			logger.info("need to download picture is null.");
			return;
		}
		FileUtil.delDir(model.getOutput_path());
		new File(model.getOutput_path()).mkdirs();
		logger.info("output path=" + model.getOutput_path());
		String script = model.getExe_script();

		script += " --input_dir " + destFilePath;
		script += " --output_dir " + model.getOutput_path();
		script += " --taskid " + task.getTaskId();
		script += " --callback_url "+ labelSystemModelDetectionMsgUrl;

		logger.info("start to exec script:" + script);
		//每张图片2秒钟超时时间，再加10分钟基础时间
		int timeSeconds = task.getPictureList().size() * 2 + 600;

		ProcessExeUtil.execScript(script, model.getAlgRootPath(), timeSeconds);

	}

	@PostConstruct
	public void init() {

		try(FileInputStream in = new FileInputStream(ResourceUtils.getFile("application-runtime.properties").getAbsolutePath())){
			Properties properties = new Properties();
			properties.load(in);

			for(int i =0; i<10; i++) {
				String keyPost = String.valueOf(i);
				if(i == 0) {
					keyPost = "";
				}
				if(!properties.containsKey("model_name" + keyPost)) {
					break;
				}
				AutoLabelModelRegistMsg msg = new AutoLabelModelRegistMsg();
				msg.setModel_desc(properties.getProperty("model_desc" + keyPost));
				msg.setModel_type(Integer.parseInt(properties.getProperty("model_type" + keyPost)));
				msg.setName(properties.getProperty("model_name" + keyPost));
				msg.setAgent_receive_task_url(agent_receive_task_url);
				msg.setObject_type(properties.getProperty("object_type" + keyPost));
				msg.setAlgRootPath(properties.getProperty("algRootPath" + keyPost));
				msg.setExe_script(properties.getProperty("exe_script" + keyPost));
				msg.setInput_path(properties.getProperty("input_path" + keyPost));
				msg.setOutput_path(properties.getProperty("output_path" + keyPost));

				String url = labelSystemModelRegistUrl;
				logger.info("url=" + url);

				//发送消息到标注系统中进行注册，循环发送
				try {
					RestUtil rest = new RestUtil();
					rest.post(url, msg);
					modelMap.put(msg.getName(), msg);
					break;
				}catch (Exception e) {
					logger.info(e.getMessage());
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							while(true) {
								try {
									RestUtil rest = new RestUtil();
									rest.post(url, msg);
									modelMap.put(msg.getName(), msg);
									break;
								}catch (Exception e) {
									logger.info(e.getMessage());
									try {
										Thread.sleep(2000);
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}
								}
								
							}
							
						}
					}).start();
				}

			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}



	}


}
