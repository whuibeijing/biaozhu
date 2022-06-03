package com.pcl.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pcl.constant.UserConstants;
import com.pcl.dao.ReIDLabelTaskItemDao;
import com.pcl.dao.ReIDTaskDao;
import com.pcl.pojo.Progress;
import com.pcl.pojo.mybatis.LabelTaskItem;
import com.pcl.pojo.mybatis.ReIDTask;
import com.pcl.service.schedule.ThreadSchedule;
import com.pcl.util.CocoAnnotationsUtil;
import com.pcl.util.FileUtil;
import com.pcl.util.JsonUtil;

/**
 * 鹏城实验室特殊标注数据导出服务
 * @author 邹安平
 *
 */
@Service
public class PCLSpecialExportService {
	
	private static Logger logger = LoggerFactory.getLogger(PCLSpecialExportService.class);
	
	@Autowired
	private ReIDLabelTaskItemDao reIdLabelTaskItemDao;
	
	@Autowired
	private ProgressService progressService;

	@Autowired
	private ReIDTaskDao reIdTaskDao;
	
	@Autowired
	private LabelSystemDbPolicy dbPolicy;
	
	private static final String REID_POSTFIX = "reID";
	

	public String exportExceptionFile(String labelTaskId, int type,HttpServletResponse response) throws IOException{
		
		
		ReIDTask reIdtask = reIdTaskDao.queryReIDTaskById(labelTaskId);

		String key = labelTaskId + REID_POSTFIX;

		String taskName = FileUtil.getRemoveChineseCharName(reIdtask.getTask_name());
		
		String relatedName =  System.nanoTime() + File.separator + taskName + "_exception" + ".csv";
		
		String fileName = LabelDataSetMerge.getUserDataSetPath() + File.separator + relatedName;
		Progress pro = new Progress();
		pro.setId(key);
		pro.setStartTime(System.currentTimeMillis() / 1000);
		pro.setExceedTime(10 * 60);
		pro.setRelatedFileName(relatedName);
		pro.setBase(0);
		pro.setRatio(1);
		progressService.putProgress(pro);

		ThreadSchedule.execExportThread(()->{
			try {
				downloadReIdTaskFileWriter(reIdtask,String.valueOf(type), fileName, pro);
			} catch (Exception e) {
				pro.setId(e.getMessage());
				logger.info(e.getMessage(),e);
			}
		});

		return key;
		

	}
	

	
	Pattern pattern = Pattern.compile("^\\d+$");
	
	private boolean isExceptionType(String class_name,String reId) {
		if(reId != null) {
			if(reId.toLowerCase().startsWith("b") || reId.toLowerCase().startsWith("c")) {
				return true;
			}
		}
		if(class_name != null) {
			Matcher isNum = pattern.matcher(class_name);
			if(isNum.matches()) {
				return true;
			}else {
				return false;
			}
		}
		return true;
	}


	public void downloadReIdTaskFileWriter(ReIDTask reIdtask,String type,String fileName1,Progress pro) throws IOException {
		File zipFile = new File(fileName1) ;
		zipFile.getParentFile().mkdirs();
		logger.info("start to write exception csv.fileName1=" + fileName1);
		int total = 0;
		int count = 0;
		List<LabelTaskItem> reList = reIdLabelTaskItemDao.queryLabelTaskItemByLabelTaskId(
				dbPolicy.getTableName(reIdtask.getId(),UserConstants.REID_TASK_SINGLE_TABLE, reIdtask.getUser_id()),reIdtask.getId());
				//TokenManager.getUserTablePos(reIdtask.getUser_id(), UserConstants.REID_TASK_SINGLE_TABLE),reIdtask.getId());
		total = reList.size();

		try(FileOutputStream zos = new FileOutputStream(zipFile)) {
			long start = System.currentTimeMillis();
			
			LinkedHashMap<String,Map<String,String>> resultMap = new LinkedHashMap<>();
			for(int i = 0; i < total; i++) {
				LabelTaskItem item = reList.get(i);
				
				List<Map<String,Object>> labelList = JsonUtil.getLabelList(item.getLabel_info());
				
				for(Map<String,Object> labelMap : labelList) {
					String class_name = CocoAnnotationsUtil.getClassName(labelMap);
					String reId = CocoAnnotationsUtil.getStrValue(labelMap,"reId");
					if(reId == null) {
						continue;
					}
					if(isExceptionType(class_name,reId)) {
						
						String number = getNumber(reId);
						
						Map<String,String> record = resultMap.get(number);
						if(record == null) {
							record = new HashMap<>();
							resultMap.put(number, record);
						}
								
						String frames = item.getPic_image_field();
						frames = frames.substring(frames.lastIndexOf("/") + 1);
						frames = frames.substring(0,frames.lastIndexOf("."));
						
						record.put("fileName", reIdtask.getTask_name());
						if(class_name != null) {
							record.put("type",class_name);
						}
						
						
						Object tagObj = CocoAnnotationsUtil.getObjValue(labelMap, "tag");
						if(tagObj != null && tagObj instanceof Map) {
							Map<String,Object> tagMap = (Map<String,Object>)tagObj;
							
							if(tagMap.get("start") != null) {
								record.put("start",frames);
								record.put("box","\"" + CocoAnnotationsUtil.getStrValue(labelMap,"box") + "\"");
							}
							if(tagMap.get("end") != null) {
								record.put("end",frames);
							}
							if(tagMap.get("exception") != null) {
								record.put("exception",frames);
								record.put("dot","\"" + CocoAnnotationsUtil.getStrValue(labelMap,"box") + "\"");
							}
						}
						
						String weather = CocoAnnotationsUtil.getStrValue(labelMap,"weather");
						String severity = CocoAnnotationsUtil.getStrValue(labelMap,"severity");
						if(!Strings.isEmpty(weather)) {
							logger.info("weather =" + weather);
							record.put("weather", weather);
						}
						if(!Strings.isEmpty(severity)) {
							logger.info("severity =" + severity);
							record.put("severity", severity);
						}
						record.put("reId", number);
					}

				}
				
				count ++;
				if(count != total) {
					progressService.updateProgress(pro, (long)((count *1.0 / total) * 100));
				}
			}
			
			StringBuilder strB = new StringBuilder();
			strB.append("视频序列名").append(",");//fileName
			strB.append("事故类型（类别号）").append(",");//type
			strB.append("reId").append(",");//reId
			strB.append("开始帧").append(",");//start
			strB.append("结束帧").append(",");//end
			strB.append("发生区域").append(",");//box
			strB.append("事故峰值帧").append(",");//exception
			strB.append("事故峰值点").append(",");//
			strB.append("天气类别（类别号）").append(",");//weather
			strB.append("严重程度（得分）");//severity
			
			strB.append("\n");
			for(Entry<String,Map<String,String>> entry : resultMap.entrySet()) {
				Map<String,String> record = entry.getValue();
				strB.append(record.get("fileName")).append(",");
				strB.append(record.get("type")).append(",");
				strB.append(record.get("reId")).append(",");
				strB.append(record.get("start") == null ? "": record.get("start")).append(",");
				strB.append(record.get("end") == null ? "": record.get("end")).append(",");
				strB.append(record.get("box")).append(",");
				strB.append(record.get("exception") == null ? "": record.get("exception")).append(",");
				strB.append(record.get("dot") == null ? "": record.get("dot")).append(",");
				strB.append(record.get("weather") == null ? "": record.get("weather")).append(",");
				strB.append(record.get("severity") == null ? "": record.get("severity")).append(",");
				
				strB.append("\n");
			}
			logger.info("result=");
			logger.info(strB.toString());
			byte[] bytes = strB.toString().getBytes("utf-8");
			
			int len = bytes.length;
			zos.write(bytes, 0, len);
			
			zos.flush();

			long end = System.currentTimeMillis();
			logger.info("finished to writer csv, cost: " + (end - start) +" ms");
		} catch (Exception e) {
			throw new RuntimeException("writer csv error ",e);
		}

		progressService.updateProgress(pro, 100);
		
	}


	private String getNumber(String reId) {
		return reId.substring(1);
	}

	
	public String exportExceptionReIdTaskListFile(String reIdTaskIdList, String type) throws IOException {
		List<String> reidList = JsonUtil.getList(reIdTaskIdList);
		return downloadReIdTaskListFile(reidList, type, LabelDataSetMerge.getUserDataSetPath(),true);
	}
	
	
	private String downloadReIdTaskListFile(List<String> reidList, String type,String tmpPath,boolean isNeedNanoTime) throws IOException {
		
		logger.info("reidList size=" + reidList.size());
		String key = UUID.randomUUID().toString().replaceAll("-","") + REID_POSTFIX;

		List<String> fileNameList = new ArrayList<>();

		Progress pro = new Progress();
		pro.setId(key);
		pro.setStartTime(System.currentTimeMillis() / 1000);
		pro.setExceedTime(10 * 60);

		pro.setRatio(1.0/reidList.size());


		for(String reIdTaskId : reidList) {
			logger.info("query reIdTaskId=" + reIdTaskId);
			String prefix = "";
			if(isNeedNanoTime) {
				prefix = System.nanoTime() + File.separator;
			}
			ReIDTask reIdtask = reIdTaskDao.queryReIDTaskById(reIdTaskId);
			String taskName = FileUtil.getRemoveChineseCharName(reIdtask.getTask_name());
			String relatedName =  prefix + taskName + "_exception" + ".csv";
			
			fileNameList.add(relatedName);
		}
		String fileJsonStr = JsonUtil.toJson(fileNameList);
		if(fileJsonStr.length() > 10000) {
			fileJsonStr = "";
		}
		pro.setRelatedFileName(fileJsonStr);
		progressService.putProgress(pro);

		ThreadSchedule.execExportThread(()->{
			try {
				double base = 0.0;
				for(int i = 0; i <reidList.size(); i++) {
					logger.info("start deal " + (i + 1) + " task.");
					String reIdTaskId = reidList.get(i);
					ReIDTask reIdtask = reIdTaskDao.queryReIDTaskById(reIdTaskId);

					String relatedName = fileNameList.get(i);

					String fileName = tmpPath + File.separator + relatedName;

					downloadReIdTaskFileWriter(reIdtask,type, fileName, pro);

					base+= pro.getRatio();

					pro.setBase((long)(base * 100));

				}
			} catch (Exception e) {
				pro.setId(e.getMessage());
				logger.info(e.getMessage(),e);
			}

			progressService.updateProgress(pro,100);
		});
		return key;

	}


}
