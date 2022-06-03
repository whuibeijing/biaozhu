package com.pcl.gitea.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pcl.constant.Constants;
import com.pcl.constant.UserConstants;
import com.pcl.dao.DataSetDao;
import com.pcl.dao.LabelDcmTaskItemDao;
import com.pcl.dao.LabelTaskDao;
import com.pcl.dao.LabelTaskItemDao;
import com.pcl.gitea.constant.GiteaConstant;
import com.pcl.pojo.PageResult;
import com.pcl.pojo.display.DisplayDataSet;
import com.pcl.pojo.mybatis.DataSet;
import com.pcl.pojo.mybatis.LabelTask;
import com.pcl.pojo.mybatis.LabelTaskItem;
import com.pcl.service.LabelSystemDbPolicy;
import com.pcl.service.ObjectFileService;
import com.pcl.util.CocoAnnotationsUtil;
import com.pcl.util.GroundTruthRectUtil;
import com.pcl.util.JsonUtil;
import com.pcl.util.PclJsonAnnotationsUtil;
import com.pcl.util.TimeUtil;
import com.pcl.util.VocAnnotationsUtil;

@Service
public class GiteaDataSetService {

	private static Logger logger = LoggerFactory.getLogger(GiteaDataSetService.class);

	@Autowired
	private DataSetDao dataSetDao;

	@Autowired
	private LabelTaskItemDao labelTaskItemDao;
	
	@Autowired
	private LabelTaskDao labelTaskDao;
	
	@Autowired
	private LabelDcmTaskItemDao labelDcmTaskItemDao;

	@Autowired
	private VocAnnotationsUtil vocUtil;

	@Autowired
	private PclJsonAnnotationsUtil pclJsonUtil;

	@Autowired
	private CocoAnnotationsUtil cocoUtil;

	@Autowired
	private GiteaObjectFileService giteaObjectFileService;
	
	@Autowired
	private LabelSystemDbPolicy giteaDbPolicy;
	
	@Value("${video.postfix:mp4,avi,wmv,rm,mpg,mpeg}")
    private String videoPostfix;
	
	@Value("${largepicture.postfix:svs,tif}")
    private String largePicturePostfix;

	@Value("${dcm.postfix:dcm,dicom}")
    private String dcmPostfix;
	
	public void addGiteaDataSetByAttach(Map<String,Object> attachMap) {
		if(attachMap.get("UUID") == null) {
			logger.info("attach error. cannot add dataset.");
			return;
		}
		int type = JsonUtil.getIntValue("Type", attachMap);
		if(type == 0) {//minio
			addFileToDataset(
					attachMap,
					giteaObjectFileService.getObjectFileService(GiteaConstant.MINIO_FILESERVICE),
					GiteaConstant.MINIO_BUCKET_NAMEPREFIX,
					giteaObjectFileService.getDefaulMinioBucketName(),
					giteaObjectFileService.getMinioBasePath());
		}else {//OBS
			try {
				addFileToDataset(
						attachMap,
						giteaObjectFileService.getObjectFileService(GiteaConstant.OBS_FILESERVICE),
						GiteaConstant.OBS_BUCKET_NAMEPREFIX,
						giteaObjectFileService.getDefaultObsBucketName(),
						giteaObjectFileService.getObsBasePath());
			}catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void deleteGiteaDataSetByAttach(Map<String,Object> attachMap) {
		if(attachMap.get("UUID") == null) {
			logger.info("attach error. cannot delete dataset.");
			return;
		}
		String datasetuuid = attachMap.get("UUID").toString();

		labelTaskItemDao.deleteLabelTaskById(giteaDbPolicy.getTableName(datasetuuid, UserConstants.LABEL_TASK_SINGLE_TABLE,-1), datasetuuid);

		dataSetDao.deleteDataSetById(datasetuuid);
		
		//将对应的标注任务状态设置为不可用
		List<LabelTask> labelTaskList = labelTaskDao.queryLabelTaskByDataSetId(datasetuuid);
		for(LabelTask lableTask : labelTaskList) {
			Map<String,Object> paramMap = new HashMap<>();
			paramMap.put("id", lableTask.getId());
			paramMap.put("task_status", Constants.LABEL_TASK_STATUS_DATASET_DELETED);
			logger.info("update labeltask dataset delete:" + JsonUtil.toJson(paramMap));
			labelTaskDao.updateLabelTaskStatus(paramMap);
		}
	}

	private void addFileToDataset(Map<String, Object> attachMap, ObjectFileService fileService,String prefix,String buketName,String basePath) {
		String datasetuuid = attachMap.get("UUID").toString();

		int user_id = JsonUtil.getIntValue("UploaderID", attachMap);
		String repoId = attachMap.get("RepoID").toString();
		String name = attachMap.get("AttachName").toString();
		String labelSystemUUID = datasetuuid;

		DataSet dataSet = new DataSet();
		dataSet.setZip_bucket_name(buketName);
		String objectName =  basePath + "/" + datasetuuid.charAt(0) + "/" + datasetuuid.charAt(1) + "/" + datasetuuid+datasetuuid;

		dataSet.setZip_object_name(objectName);
		dataSet.setId(labelSystemUUID);
		dataSet.setUser_id(user_id);
		dataSet.setTask_name(name);
		dataSet.setAssign_user_id(user_id);
		dataSet.setTask_add_time(TimeUtil.getCurrentTimeStr());
		dataSet.setTask_status(Constants.DATASET_PROCESS_NOT_START);
		dataSet.setAppid(repoId);

		if(name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith("tar.gz") || name.toLowerCase().endsWith(".tgz")) {
				//name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".tar.gz") || name.toLowerCase().endsWith(".tgz")) {
			dataSet.setDataset_type(Constants.DATASET_TYPE_PICTURE);
		}else if(isPostfixEnd(name,videoPostfix)) {
			dataSet.setDataset_type(Constants.DATASET_TYPE_VIDEO);
		}else if(isPostfixEnd(name,largePicturePostfix)) {
			dataSet.setDataset_type(Constants.DATASET_TYPE_SVS);
		}
		dataSetDao.addDataSet(dataSet);
	
		
		if(name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith("tar.gz") || name.toLowerCase().endsWith(".tgz")) {
			logger.info("add to dataset, name=" + name + " uuid=" + datasetuuid);
			addDataSetFromZipFile(fileService, prefix, buketName, user_id, labelSystemUUID, dataSet, objectName);
		}
		logger.info("finished to add dataset:" + JsonUtil.toJson(attachMap));
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

	private void addDataSetFromZipFile(ObjectFileService fileService, String prefix, String buketName, int user_id,
			String labelSystemUUID, DataSet dataSet, String objectName) {
		List<String> fileList = fileService.listAllFile(buketName + "/" + objectName);
		logger.info("list dataset file size=" + fileList.size());
		Map<String,List<LabelTaskItem>> annation = getAnnationMap(fileList,buketName,fileService);
		Map<String,Set<String>> labelPropertyInfoMap = new HashMap<>();
		List<LabelTaskItem> batchList = new ArrayList<>();
		for(String fileName : fileList) {
			LabelTaskItem taskItem = new LabelTaskItem();
			taskItem.setId(UUID.randomUUID().toString().replaceAll("-",""));
			taskItem.setItem_add_time(TimeUtil.getCurrentTimeStr());
			taskItem.setLabel_task_id(labelSystemUUID);
			String relativePath =  prefix +  buketName +  "/" + fileName;
//			if(fileName.lastIndexOf(".") == -1) {
//				continue;
//			}
			String key = getFileNameNoPostfix(relativePath);
			if(annation.get(key) != null) {
				List<LabelTaskItem> tmpList = annation.get(key);
				//logger.info("key=" + key + "  find size=" + tmpList.size());
				if(tmpList.size() == 1) {
					taskItem.setLabel_info(tmpList.get(0).getLabel_info());
					collectLabelPropertyInfo(JsonUtil.getLabelList(tmpList.get(0).getLabel_info()),labelPropertyInfoMap);
					taskItem.setPic_object_name(tmpList.get(0).getPic_object_name());
				}else {
					//找一个最匹配的
					LabelTaskItem re = find(tmpList,fileName);
					taskItem.setLabel_info(re.getLabel_info());
					collectLabelPropertyInfo(JsonUtil.getLabelList(re.getLabel_info()),labelPropertyInfoMap);
					taskItem.setPic_object_name(re.getPic_object_name());
				}
				
			}
			taskItem.setPic_image_field(relativePath);
			taskItem.setLabel_status(Constants.LABEL_TASK_STATUS_NOT_FINISHED);
			batchList.add(taskItem);
			if(batchList.size() > 500) {
				addBatchItemToDb(user_id, batchList,labelSystemUUID);
				batchList.clear();
			}
		}
		
		if(batchList.size() > 0) {
			addBatchItemToDb(user_id, batchList,labelSystemUUID);
		}
		
		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("id", dataSet.getId());
		paramMap.put("total", batchList.size());
		paramMap.put("labelPropertyInfo", JsonUtil.toJson(labelPropertyInfoMap));
		paramMap.put("task_status", Constants.DATASET_PROCESS_PREPARE);
		logger.info("item size=" + batchList.size());
		dataSetDao.updateDataSet(paramMap);
	}


	private LabelTaskItem find(List<LabelTaskItem> tmpList, String fileName) {
		int max = 1;
		String tmpFileNames[] =  fileName.split("/");
		LabelTaskItem result = tmpList.get(0);
		//logger.info("fileName=" + fileName);
		for(LabelTaskItem item : tmpList) {
			if(item.getPic_image_field() == null) {
				continue;
			}
			//logger.info("item.getPic_image_field()=" + item.getPic_image_field());
			String tmp[] = item.getPic_image_field().split("/");
			int tmpMax = 0;
			for(int index = 1; ;index++) {
				if((tmp.length - index) >= 0 && (tmpFileNames.length - index) >= 0) {
					if(isNameEqual(tmp[tmp.length - index], tmpFileNames[tmpFileNames.length - index])) {
						tmpMax++;
					}else {
						break;
					}
				}else {
					break;
				}
			}
			if(tmpMax > max) {
				//logger.info("tmpMax=" + tmpMax);
				max = tmpMax;
				result = item;
			}
		}
		logger.info("fileName=" + fileName + "  findResult=" + result.getPic_image_field());
		return result;
	}

	private boolean isNameEqual(String string1, String string2) {
		if(getFileNameNoPostfix(string1).equals(getFileNameNoPostfix(string2))) {
			return true;
		}
		return false;
	}

	private void collectLabelPropertyInfo(ArrayList<Map<String, Object>> labelList,
			Map<String,Set<String>> labelPropertyInfoMap) {
		for(Map<String,Object> label : labelList) {
			Object classNameObj = label.get("class_name");
			if(classNameObj != null) {
				Set<String> set = labelPropertyInfoMap.get("type");
				if(set == null) {
					set = new HashSet<>();
					labelPropertyInfoMap.put("type", set);
				}
				set.add(classNameObj.toString());
			}
		}
	}

	private void addBatchItemToDb(int user_id,List<LabelTaskItem> batchList,String datasetId) {
		Map<String,Object> paramMap = new HashMap<>();

		paramMap.put("user_id", giteaDbPolicy.getTableName(datasetId, UserConstants.LABEL_TASK_SINGLE_TABLE,-1));

		paramMap.put("list", batchList);
		labelTaskItemDao.addBatchLabelTaskItemMap(paramMap);
	}


	private void addToResult(LabelTaskItem xmlItem,String returnKey,Map<String, List<LabelTaskItem>> result) {
		List<LabelTaskItem> itemList = result.get(returnKey);
		if(itemList == null) {
			itemList = new ArrayList<>();
			result.put(returnKey, itemList);
		}
		itemList.add(xmlItem);
	}
	
	private Map<String, List<LabelTaskItem>> getAnnationMap(List<String> fileList,String buketName,ObjectFileService fileService)  {
		logger.info("start deal annatotion....");
		Map<String, List<LabelTaskItem>> result = new HashMap<>();

		for (String filePath : fileList) {  
			if(!(filePath.endsWith(".xml") || filePath.endsWith(".json") || filePath.endsWith(".txt") )) {
				continue;
			}

			try(InputStream in  = fileService.getPicture(buketName, filePath)) {

				if(filePath.endsWith(".xml")) {
					LabelTaskItem xmlItem = vocUtil.readLabelInfoFromXmlDocument(in);
					String returnKey = getFileNameNoPostfix(filePath);
					xmlItem.setPic_image_field(filePath);
					addToResult(xmlItem, returnKey, result);
					
				}else if(filePath.endsWith(".json")) {
					if(!result.containsKey(filePath)) {
						Map<String,Object> map = getJsonMap(in);
						LabelTaskItem item =  pclJsonUtil.readLabelInfoFromJsonMap(map);
						if(item == null) {
							if(cocoUtil.isCocoFormat(map)) {
								String tmpPath[] = filePath.split("/");
								String filePathPrefix = filePath.substring(0,filePath.length() - tmpPath[tmpPath.length - 1].length());
								logger.info("read coco format label. file=" + filePath + "  filePathPrefix=" + filePathPrefix);
								List<LabelTaskItem> labelList = CocoAnnotationsUtil.readCocoJson(map);
								for(LabelTaskItem tmp : labelList) {
									logger.info("labelList.size=" + labelList.size());
									addToResult(tmp, getFileNameNoPostfix(tmp.getPic_image_field()), result);
								}
							}
						}else {
							LabelTaskItem xmlItem = pclJsonUtil.readLabelInfoFromPclJson(in);
							xmlItem.setPic_image_field(filePath);
							addToResult(xmlItem, getFileNameNoPostfix(filePath), result);
						}
					}
				}else if(filePath.endsWith(".txt")) {
					if(filePath.endsWith("groundtruth_rect.txt")) {
						GroundTruthRectUtil objectTrack = new GroundTruthRectUtil();
						//目标跟踪的标注
						List<LabelTaskItem> labelList = objectTrack.readObjectTraceLabel(fileList, in, filePath);
						for(LabelTaskItem tmp : labelList) {
							addToResult(tmp, getFileNameNoPostfix(tmp.getPic_image_field()), result);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.info("end deal annatotion....size=" + result.size());
		return result;
	}


	private Map<String,Object> getJsonMap(InputStream in){
		StringBuilder strBuild = new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(in))){
			String line = null;
			while(true) {
				line = reader.readLine();
				if(line == null) {
					break;
				}
				strBuild.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String,Object> map = JsonUtil.getMap(strBuild.toString());
		return map;
	}

	private String getFileNameNoPostfix(String filePath) {
		int index = filePath.lastIndexOf("/");
		if(index != -1) {
			String tmpName = filePath.substring(index + 1);
			int dotIndex = tmpName.lastIndexOf(".");
			if(dotIndex != -1) {
				return tmpName.substring(0,dotIndex);
			}
			return tmpName;
		}else {
			int dotIndex = filePath.lastIndexOf(".");
			if(dotIndex != -1) {
				return filePath.substring(0,dotIndex);
			}

			return filePath;
		}
	}


	public List<DisplayDataSet> queryAllDataSet(String uid, String repoId,String datesetType) {
		List<String> typeList = null;
		if(datesetType != null) {
			typeList = new ArrayList<>();
			typeList.addAll(JsonUtil.getList(datesetType));
		}
		
		List<DisplayDataSet>  result = new ArrayList<>();
		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("appid", repoId);
		paramMap.put("datasettypeList", typeList);
		
		List<DataSet> dataSetList = dataSetDao.queryDataSetByParams(paramMap);
		for(DataSet dataSet : dataSetList) {
			DisplayDataSet displayDataSet = new DisplayDataSet();
			displayDataSet.setId(dataSet.getId());
			displayDataSet.setTask_name(dataSet.getTask_name());
			displayDataSet.setTask_add_time(dataSet.getTask_add_time());
			displayDataSet.setDatasetType(dataSet.getDataset_type());
			displayDataSet.setTotal(dataSet.getTotal());
			displayDataSet.setTask_desc(dataSet.getTask_desc());
			result.add(displayDataSet);
		}

		return result;
	}


	public String queryDataSetLabelInfo(String uid) {

		DataSet dataSet =  dataSetDao.queryDataSetById(uid);
		if(dataSet != null) {
			String labelInfo = dataSet.getLabelPropertyInfo();
			if(labelInfo == null || labelInfo.length() < 3) {
				return "";
			}
			return labelInfo;
		}else {
			return "";
		}

	}

	public PageResult queryDataSetPictureItemPage(String datasetId,int currPage,int pageSize){

		PageResult pageResult = new PageResult();

		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("currPage", currPage * pageSize);
		paramMap.put("pageSize", pageSize);
		paramMap.put("label_task_id", datasetId);
		
		DataSet dataSet = dataSetDao.queryDataSetById(datasetId);
		
		paramMap.put("user_id", giteaDbPolicy.getTableName(datasetId, UserConstants.LABEL_TASK_SINGLE_TABLE,-1));
		
		if(dataSet.getDataset_type() == Constants.DATASET_TYPE_DCM) {
			List<LabelTaskItem> result = labelDcmTaskItemDao.queryLabelTaskItemPageByLabelTaskId(paramMap);
			int totalCount = labelDcmTaskItemDao.queryLabelTaskItemPageCountByLabelTaskId(paramMap);
			pageResult.setTotal(totalCount);
			pageResult.setData(result);

		}else {
			List<LabelTaskItem> result = labelTaskItemDao.queryLabelTaskItemPageByLabelTaskId(paramMap);
			int totalCount = labelTaskItemDao.queryLabelTaskItemPageCountByLabelTaskId(paramMap);
			pageResult.setTotal(totalCount);
			pageResult.setData(result);
		}

		pageResult.setCurrent(currPage);
		return pageResult;
	}

}
