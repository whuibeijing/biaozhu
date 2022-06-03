package com.pcl.service.schedule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pcl.constant.Constants;
import com.pcl.constant.ModelConstants;
import com.pcl.constant.UserConstants;
import com.pcl.dao.AlgInstanceDao;
import com.pcl.dao.AlgModelDao;
import com.pcl.dao.AutoLabelModelRegistMsgDao;
import com.pcl.dao.LabelTaskItemDao;
import com.pcl.dao.ReIDLabelTaskItemDao;
import com.pcl.exception.LabelSystemException;
import com.pcl.pojo.body.AutoLabelBody;
import com.pcl.pojo.body.ObjectDetectionTaskBody;
import com.pcl.pojo.body.ObjectTrackTask;
import com.pcl.pojo.mybatis.AlgInstance;
import com.pcl.pojo.mybatis.AlgModel;
import com.pcl.pojo.mybatis.AutoLabelModelRegistMsg;
import com.pcl.pojo.mybatis.LabelTaskItem;
import com.pcl.service.LabelSystemDbPolicy;
import com.pcl.service.ObjectFileService;
import com.pcl.service.ReIdTaskService;
import com.pcl.util.FileUtil;
import com.pcl.util.JsonUtil;
import com.pcl.util.LabelInfoUtil;
import com.pcl.util.ProcessExeUtil;
import com.pcl.util.RestUtil;
import com.pcl.util.TimeUtil;

@Service
public class TrackingSchedule {

	private static Logger logger = LoggerFactory.getLogger(TrackingSchedule.class);


	@Autowired
	private AlgModelDao algModelDao;

	@Autowired
	private AlgInstanceDao algInstanceDao;

	@Autowired
	private LabelTaskItemDao labelTaskItemDao;

	@Autowired
	private ReIDLabelTaskItemDao reIDlabelTaskItemDao;

	@Autowired
	private ObjectFileService fileService;

	@Autowired
	private ReIdTaskService reIdTaskService;

	@Autowired
	private LabelSystemDbPolicy dbPolicy;

	@Autowired
	private AutoLabelModelRegistMsgDao autoLabelModelRegistMsgDao;

	private final static String REID_TASK_TRACKING = "4";

	private boolean isMultiTrack(AlgModel algModel) {
		if(algModel.getId() == 20 || algModel.getId() == 21) {
			return true;
		}
		return false;
	}


	public void tracking(AutoLabelBody body,int exceedTime) throws LabelSystemException {

		int algId = body.getModel();
	
		AlgModel algModel = algModelDao.queryAlgModelById(algId);//
		AlgInstance algInstance = null;
		if(algModel != null) {
			algInstance = algInstanceDao.queryAlgInstanceById(Integer.parseInt(algModel.getAlg_instance_id()));
		}
		if(algInstance == null) {
			logger.info("the algInstance is null. algInstanceId=" + algModel.getAlg_instance_id());
			return;
		}

		List<LabelTaskItem> taskItem = null;
		if(REID_TASK_TRACKING.equals(body.getTask_type())) {
			taskItem = reIDlabelTaskItemDao.queryLabelTaskItemByLabelTaskIdOderbyImageNameAsc(
					dbPolicy.getTableName(body.getTaskId(), UserConstants.REID_TASK_SINGLE_TABLE, body.getUserId()),body.getTaskId());
			//TokenManager.getUserTablePos(body.getUserId(), UserConstants.REID_TASK_SINGLE_TABLE),body.getTaskId());
		}else {
			taskItem = labelTaskItemDao.queryLabelTaskItemByLabelTaskId(
					dbPolicy.getTableName(body.getTaskId(), UserConstants.LABEL_TASK_SINGLE_TABLE, body.getUserId()),body.getTaskId());
			//TokenManager.getUserTablePos(body.getUserId(), UserConstants.LABEL_TASK_SINGLE_TABLE),body.getTaskId());
		}

		List<LabelTaskItem> autoTrackingList = new ArrayList<>();
		if(body.getEndIndex() > 0 || body.getStartIndex() > 0) {
			if(body.getEndIndex() > body.getStartIndex()) {
				for(int i = body.getStartIndex() - 1; i< taskItem.size() && i < body.getEndIndex(); i++) {
					autoTrackingList.add(taskItem.get(i));
				}
			}else {
				for(int i = body.getStartIndex() - 1; i >= 0 && i >= body.getEndIndex(); i--) {
					autoTrackingList.add(taskItem.get(i));
				}
			}
		}
		if(autoTrackingList.size() == 0) {
			autoTrackingList.addAll(taskItem);
		}

		doTrack(body, exceedTime, algModel, algInstance, autoTrackingList);
	}


	public void doRemoteTrack(AutoLabelBody body, int exceedTime) {
		int algId = body.getModel();
		AutoLabelModelRegistMsg registModel = autoLabelModelRegistMsgDao.queryById(algId);
		if(registModel == null) {
			logger.info("the algInstance is null. algInstanceId=" + algId);
			return;
		}
		
		List<LabelTaskItem> taskItem = null;
		if(REID_TASK_TRACKING.equals(body.getTask_type())) {
			taskItem = reIDlabelTaskItemDao.queryLabelTaskItemByLabelTaskIdOderbyImageNameAsc(
					dbPolicy.getTableName(body.getTaskId(), UserConstants.REID_TASK_SINGLE_TABLE, body.getUserId()),body.getTaskId());
			//TokenManager.getUserTablePos(body.getUserId(), UserConstants.REID_TASK_SINGLE_TABLE),body.getTaskId());
		}else {
			taskItem = labelTaskItemDao.queryLabelTaskItemByLabelTaskId(
					dbPolicy.getTableName(body.getTaskId(), UserConstants.LABEL_TASK_SINGLE_TABLE, body.getUserId()),body.getTaskId());
			//TokenManager.getUserTablePos(body.getUserId(), UserConstants.LABEL_TASK_SINGLE_TABLE),body.getTaskId());
		}

		List<LabelTaskItem> autoTrackingList = new ArrayList<>();
		if(body.getEndIndex() > 0 || body.getStartIndex() > 0) {
			if(body.getEndIndex() > body.getStartIndex()) {
				for(int i = body.getStartIndex() - 1; i< taskItem.size() && i < body.getEndIndex(); i++) {
					autoTrackingList.add(taskItem.get(i));
				}
			}else {
				for(int i = body.getStartIndex() - 1; i >= 0 && i >= body.getEndIndex(); i--) {
					autoTrackingList.add(taskItem.get(i));
				}
			}
		}
		if(autoTrackingList.size() == 0) {
			autoTrackingList.addAll(taskItem);
		}
		
		if(registModel.getModel_type() == 6) {//多目标跟踪
			List<String> pictureList = new ArrayList<>();
			List<String> pictureIdList = new ArrayList<>();
			
			for(LabelTaskItem item : autoTrackingList) {
				pictureList.add(item.getPic_image_field());
				pictureIdList.add(item.getId());
			}
			logger.info("send multi track msg to remote docker. size=" + autoTrackingList.size());
			ObjectTrackTask taskBody = new ObjectTrackTask();
			taskBody.setModelName(registModel.getName());
			taskBody.setPictureList(pictureList);
			taskBody.setTaskId(ModelConstants.OBJECT_TRACKING_SERVICE_TYPE_MULTI + "##" + body.getUserId() + "##"+ body.getTaskId());
			if(REID_TASK_TRACKING.equals(body.getTask_type())) {
				taskBody.setUseSence(1);
			}
			taskBody.setSingleTrack(false);
			taskBody.setPictureIdList(pictureIdList);
			
			RestUtil restUtil = new RestUtil();
			restUtil.post(registModel.getAgent_receive_task_url(), taskBody);
		}	
	}


	private void doTrack(AutoLabelBody body, int exceedTime, AlgModel algModel, AlgInstance algInstance,
			List<LabelTaskItem> autoTrackingList) throws LabelSystemException {
		//本地
		if(!isMultiTrack(algModel)) {//单目标跟踪需要人工标注帧
			doSingleTrack(body, exceedTime, algModel, algInstance, autoTrackingList);
		}else {
			doMultiTrack(body, exceedTime, algModel, algInstance, autoTrackingList);
		}

	}


	private void doSingleTrack(AutoLabelBody body, int exceedTime, AlgModel algModel, AlgInstance algInstance,
			List<LabelTaskItem> autoTrackingList) throws LabelSystemException {

		String datasetName = "auto";
		String imageDir =  algInstance.getAlg_root_dir()  + "img_tracking" + File.separator + System.nanoTime() + File.separator;
		File imageDirFile = new File(imageDir);
		if(!imageDirFile.exists()) {
			imageDirFile.mkdir();
		}
		File dataSetRootFile = new File(imageDir,datasetName);
		dataSetRootFile.mkdirs();

		//生成groundtruth.txt
		Map<String,Object> firstLableMap = new HashMap<>();
		//单目标跟踪需要人工标注帧
		String labelInfo = autoTrackingList.get(0).getLabel_info();
		List<Map<String,Object>> labelList = JsonUtil.getLabelList(labelInfo);

		if(labelList.isEmpty()) {
			throw new LabelSystemException("第一帧数据没有标注。");
		}

		List<String> labelIdList = getLabelId(body.getLabel_id());

		copyImgToPath(autoTrackingList,dataSetRootFile);

		for(String labelId : labelIdList) {

			firstLableMap = getFirstLabelMap(labelList,labelId);

			writeGroundTxt(dataSetRootFile,firstLableMap);

			String tmpScript = algModel.getExec_script();
			tmpScript = tmpScript.replace("{data_dir}", imageDir);
			tmpScript = tmpScript.replace("{data_name}", datasetName);
			String resultFileName = datasetName +".txt";

			final String script = tmpScript;
			final String rootPath = algInstance.getAlg_root_dir();

			//int timeSeconds = 24 * 3600;
			logger.info("labelid =" + labelId + "  exec sigle track script:" + script);
			try {
				ProcessExeUtil.execScript(script, rootPath, exceedTime);

				File resultFile = new File(imageDir,resultFileName); 
				if(resultFile.exists()) {
					logger.info("start to write tracking result.");
					//read result;
					singleTrackingResult(body, autoTrackingList, firstLableMap, resultFile);

				}else {
					logger.info("not found result:" + resultFile.getAbsolutePath());
				}
			} catch (LabelSystemException e) {
				e.printStackTrace();
			}
		}

		FileUtil.delDir(imageDir);

	}

	private List<String> getLabelId(String label_id) {
		ArrayList<String> labelIdList = new ArrayList<>();
		if(label_id != null) {

			String tmps[] = label_id.split(",");

			for(String tmp :tmps) {
				if(tmp.indexOf("-") != -1) {
					String t[] = tmp.split("-");
					if(t.length == 2) {
						int start = Integer.parseInt(t[0]) - 1;
						int end = Integer.parseInt(t[1]) - 1;
						for(int j = start; j <=end; j++) {
							labelIdList.add(String.valueOf(j));
						}
					}
				}else {
					labelIdList.add(tmp);
				}
			}

		}

		return labelIdList;
	}


	private void doMultiTrack(AutoLabelBody body, int exceedTime, AlgModel algModel, AlgInstance algInstance,
			List<LabelTaskItem> autoTrackingList) throws LabelSystemException {

		String datasetName = "auto";
		String imageDir =  algInstance.getAlg_root_dir()  + "img_tracking" + File.separator + System.nanoTime() + File.separator;
		File imageDirFile = new File(imageDir);
		if(!imageDirFile.exists()) {
			imageDirFile.mkdir();
		}
		File dataSetRootFile = new File(imageDir,datasetName);
		dataSetRootFile.mkdirs();


		Map<String,LabelTaskItem> itemMap = new HashMap<>();

		itemMap.putAll(copyImgToPath(autoTrackingList,new File(imageDir)));

		String tmpScript = algModel.getExec_script();
		tmpScript = tmpScript.replace("{data_dir}", imageDir);
		tmpScript = tmpScript.replace("{data_name}", datasetName);
		String resultFileName = datasetName +".txt";


		tmpScript = tmpScript.replace("{output}", imageDir + "result.json");

		if(body.getLabel_option() == 11) {
			tmpScript = tmpScript.replace("{class_name}", "car");
			tmpScript = tmpScript.replace("models/mot17_half.pth", "models/coco_tracking.pth");
			tmpScript = tmpScript.replace("--num_class 1", "");
		}
		else {
			tmpScript = tmpScript.replace("{class_name}", "person");
		}
		resultFileName = "result.json";

		final String script = tmpScript;
		final String rootPath = algInstance.getAlg_root_dir();

		//int timeSeconds = 24 * 3600;
		logger.info("exec multitrack script:" + script);
		try {
			ProcessExeUtil.execScript(script, rootPath, exceedTime);

			File resultFile = new File(imageDir,resultFileName); 
			if(resultFile.exists()) {
				logger.info("start to write tracking result.");
				//read result;
				multiTrackingResult(body, itemMap, resultFile);

				FileUtil.delDir(imageDir);
			}else {
				logger.info("not found result:" + resultFile.getAbsolutePath());
			}
		} catch (LabelSystemException e) {
			e.printStackTrace();
		}
	}

	private void multiTrackingResult(AutoLabelBody body, Map<String, LabelTaskItem> itemMap, File resultFile) {
		//多目标跟踪结果处理
		String content = FileUtil.getAllContent(resultFile.getAbsolutePath(), "utf-8");
		List<Map<String,Object>> resultList = JsonUtil.getLabelList(content);


		logger.info("start to save multi track result. size=" + resultList.size());
		for(Map<String,Object> resultMap : resultList) {
			for(Entry<String,Object> entry :resultMap.entrySet()) {
				String fileName = entry.getKey();
				Object value = entry.getValue();
				List<Map<String,Object>> currentLabelList = new ArrayList<>();
				if(value instanceof List) {
					List<?> valueList = (List)value;
					for(int i = 0; i < valueList.size() ;i++) {
						Object valueObj = valueList.get(i);
						if(valueObj instanceof Map) {
							Map<String,Object> label = (Map<String,Object>)valueObj;
							//specialDeal(label);
							if(REID_TASK_TRACKING.equals(body.getTask_type())) {
								label.put(Constants.REID_KEY, label.get("id"));
							}
							currentLabelList.add(label);
						}									
					}

				}
				logger.info("fileName=" +fileName + " currentLabelList=" + JsonUtil.toJson(currentLabelList));
				LabelTaskItem item = itemMap.get(fileName);
				if(item != null) {
					saveLabel(body, item, currentLabelList);
				}
			}
		}
	}



	private void singleTrackingResult(AutoLabelBody body, List<LabelTaskItem> autoTrackingList,
			Map<String, Object> firstLableMap, File resultFile) {
		List<String> allLine = FileUtil.getAllLineList(resultFile.getAbsolutePath(), "utf-8");
		for(int i = 1; i <allLine.size(); i++) {//第一帧已经标注过了，不需要再标注。
			logger.info("line=" + allLine.get(i));
			String line = allLine.get(i);
			Map<String,Object> newLabelMap = getLabelInfo(line,firstLableMap);
			LabelTaskItem item = autoTrackingList.get(i);
			List<Map<String,Object>> currentLabelList = new ArrayList<>();
			if(body.getLabel_option() == 1) {//清除已有的标注。
				currentLabelList.add(newLabelMap);
			}else {//合并已有的标注
				currentLabelList = JsonUtil.getLabelList(item.getLabel_info());
				mergeLabel(currentLabelList,newLabelMap);
			}

			saveLabel(body, item, currentLabelList);
		}
	}


	private void saveLabel(AutoLabelBody body, LabelTaskItem item, List<Map<String, Object>> currentLabelList) {

		if(REID_TASK_TRACKING.equals(body.getTask_type())) {
			LabelTaskItem updateBodyItem = new LabelTaskItem();
			updateBodyItem.setId(item.getId());
			updateBodyItem.setLabel_info(JsonUtil.toJson(currentLabelList));
			updateBodyItem.setPic_object_name(item.getPic_object_name());
			updateBodyItem.setLabel_status(Constants.LABEL_TASK_STATUS_FINISHED);
			updateBodyItem.setLabel_task_id(body.getTaskId());

			reIdTaskService.updateReIDLabelTaskItem(updateBodyItem, body.getUserId());

			//reIDlabelTaskItemDao.updateLabelTaskItem(paramMap);

		}else {
			Map<String,Object> paramMap = new HashMap<>();
			String time = TimeUtil.getCurrentTimeStr();

			paramMap.put("id", item.getId());
			paramMap.put("label_info", JsonUtil.toJson(currentLabelList));
			paramMap.put("label_status", Constants.LABEL_TASK_STATUS_FINISHED);
			paramMap.put("item_add_time", time);

			paramMap.put("user_id", dbPolicy.getTableName(body.getTaskId(), UserConstants.LABEL_TASK_SINGLE_TABLE, body.getUserId()));
			//TokenManager.getUserTablePos(body.getUserId(), UserConstants.LABEL_TASK_SINGLE_TABLE));
			labelTaskItemDao.updateLabelTaskItem(paramMap);
		}
	}


	private void mergeLabel(List<Map<String, Object>> currentLabelList, Map<String, Object> newLabelMap) {
		int id = 0;
		Set<Integer> idSet = new HashSet<>();
		for(Map<String,Object> labelMap : currentLabelList) {
			Object obj = labelMap.get("id");
			if(obj != null) {
				try {
					int objIntId = 2;
					if(obj instanceof Double) {
						objIntId = (int)Double.parseDouble(obj.toString());
					}else if(obj instanceof Integer) {
						objIntId = Integer.parseInt(obj.toString());
					}else {
						objIntId = (int)Double.parseDouble(obj.toString());
					}
					if(id < objIntId) {
						id = objIntId;
					}
					idSet.add(objIntId);
				}catch (Exception e) {
					logger.info("id is not int, id=" + obj.toString());
				}
			}
		}
		id = id + 1;
		while(idSet.contains(id)) {
			id = id + 1;
		}
		currentLabelList.add(LabelInfoUtil.putKey(newLabelMap, "id", String.valueOf(id)));
	}


	private Map<String, Object> getFirstLabelMap(List<Map<String, Object>> labelList, String label_id) {
		for(Map<String,Object> labelMap : labelList) {
			if(labelMap.get("id") != null) {
				if(label_id.equals(getId(labelMap.get("id")))) {
					return labelMap;
				}
			}
		}
		return labelList.get(0);
	}

	private String getId(Object obj) {
		int objIntId = 0;
		if(obj instanceof Double) {
			objIntId = (int)Double.parseDouble(obj.toString());
		}else if(obj instanceof Integer) {
			objIntId = Integer.parseInt(obj.toString());
		}else {
			objIntId = (int)Double.parseDouble(obj.toString());
		}
		return String.valueOf(objIntId);
	}

	private Map<String,Object>  getLabelInfo(String line,Map<String,Object> firstLabel) {
		Map<String,Object> label = new HashMap<>();


		String tmp[] = line.split(",");
		List<String> boxList = new ArrayList<>();

		int xmin = (int)Double.parseDouble(tmp[0]);
		int ymin = (int)Double.parseDouble(tmp[1]);
		int xmax =  (int)(Double.parseDouble(tmp[2]) + Double.parseDouble(tmp[0]));
		int ymax = (int)(Double.parseDouble(tmp[3]) + Double.parseDouble(tmp[1]));

		boxList.add(String.valueOf(xmin));
		boxList.add(String.valueOf(ymin));
		boxList.add(String.valueOf(xmax));
		boxList.add(String.valueOf(ymax));

		label.put("box", boxList);
		label.put("class_name", firstLabel.get("class_name"));
		if(firstLabel.get("reId") != null) {
			label.put("reId", firstLabel.get("reId"));
		}
		label.put("id", "1");

		Object other = firstLabel.get("other");
		if(other != null) {
			Object region_attributesObj = ((Map<String,Object>)other).get("region_attributes");
			if(region_attributesObj != null) {
				Map<String,Object> region_attributesMap = ((Map<String,Object>)region_attributesObj);

				Map<String,Object> newRegion= new HashMap<>();
				newRegion.putAll(region_attributesMap);
				newRegion.put("id", "1");
				if(firstLabel.get("reId") != null) {
					newRegion.put("reId", firstLabel.get("reId"));
				}
				newRegion.put("type", firstLabel.get("class_name"));

				Map<String,Object> otherMap= new HashMap<>();
				otherMap.put("region_attributes", newRegion);
				label.put("other", otherMap);
			}
		}

		return label;
	}


	private Map<String,LabelTaskItem> copyImgToPath(List<LabelTaskItem> autoTrackingList, File dataSetRootFile) throws LabelSystemException {
		Map<String,LabelTaskItem> itemMap = new HashMap<>();

		String picturePath = dataSetRootFile.getAbsolutePath() + File.separator + "img";
		new File(picturePath).mkdir();
		DecimalFormat df = new DecimalFormat("00000000");
		for(int i = 0; i < autoTrackingList.size();i++) {
			LabelTaskItem item = autoTrackingList.get(i);
			String relate_url = item.getPic_image_field();
			String tmp[] = relate_url.split("/");
			int length = tmp.length;
			String name = df.format(i) + tmp[length-1].substring(tmp[length-1].lastIndexOf("."));
			String pictureName = picturePath + File.separator + name;
			fileService.downLoadFileFromMinioAndSetPictureName(tmp[length-2], tmp[length-1], pictureName);
			itemMap.put(name, item);
		}
		return itemMap;
	}


	private void writeGroundTxt(File dataSetRootFile, Map<String, Object> firstLableMap) throws LabelSystemException {
		String fileName = "groundtruth.txt";
		@SuppressWarnings("unchecked")
		List<Object> boxList = (List<Object>)firstLableMap.get("box");
		if(boxList != null) {
			int xmin = (getIntStr(String.valueOf(boxList.get(0))));
			int ymin = (getIntStr(String.valueOf(boxList.get(1))));
			int xmax = (getIntStr(String.valueOf(boxList.get(2))));
			int ymax = (getIntStr(String.valueOf(boxList.get(3))));
			String line = xmin + "," + ymin + "," + Math.abs(xmax-xmin) + "," + Math.abs(ymax-ymin);
			try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dataSetRootFile.getAbsolutePath(),fileName)))){
				writer.write(line);
				writer.newLine();
				writer.write(line);
			}catch (Exception e) {
				logger.info(e.getMessage());
				throw new LabelSystemException("writer ." + fileName + " error.");
			}
		}else {
			throw new LabelSystemException("第一帧数据没有标注。");
		}
	}

	private int getIntStr(String doubleStr) {
		return (int)Double.parseDouble(doubleStr);
	}


}
