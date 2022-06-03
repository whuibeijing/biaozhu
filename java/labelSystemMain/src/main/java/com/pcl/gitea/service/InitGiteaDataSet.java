package com.pcl.gitea.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import com.pcl.dao.DataSetDao;
import com.pcl.pojo.mybatis.DataSet;
import com.pcl.util.JsonUtil;

@Service
@DependsOn("dbScriptUpdate")
public class InitGiteaDataSet {

	private static Logger logger = LoggerFactory.getLogger(InitGiteaDataSet.class);
	
	@Autowired
	private DataSetDao dataSetDao;
	
	@Autowired
	private GiteaPgDbOperService giteaPgDbOperService;
	
	@Autowired
	private GiteaDataSetService giteaDataSetService;
	
	@PostConstruct
	public void init() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				syncDataSet();
			}
		}).start();
		
	}

	private void syncDataSet() {
		long start = System.currentTimeMillis();
		logger.info("start to sync dataset.");
		List<DataSet> allDataSet = dataSetDao.queryAllDataSet();
		
		Map<String,DataSet> allDataSetMap= new HashMap<>();
		for(DataSet dataSet : allDataSet) {
			allDataSetMap.put(dataSet.getId(), dataSet);
		}
		
		List<Map<String,Object>> attachmentList = giteaPgDbOperService.query("select * from public.attachment where decompress_state=1");
		
		List<Map<String,Object>> giteaDatasetList = giteaPgDbOperService.query("select id,repo_id from public.dataset");
		Map<String,Object> giteaDatasetMap = new HashMap<>();
		for(Map<String,Object> record : giteaDatasetList) {
			giteaDatasetMap.put(record.get("id").toString(), record.get("repo_id"));
		}
		logger.info("start to init dataset, total size=" + attachmentList.size());
		
		for(int i = 0; i < attachmentList.size(); i++) {
			logger.info("start to deal " + (i + 1) + " dataset.");
			Map<String,Object> record = attachmentList.get(i);
			String uuid = record.get("uuid").toString();
			if(allDataSetMap.get(uuid) == null) {
				logger.info("need to sync ." + (i + 1) + " uuid=" + uuid);
				Map<String,Object> attachMap = new HashMap<>();
				attachMap.put("UUID", uuid);
				attachMap.put("Type", record.get("type"));
				attachMap.put("UploaderID", record.get("uploader_id"));
				attachMap.put("AttachName", record.get("name"));
				
				Object datasetObj = record.get("dataset_id");
				if(datasetObj == null) {
					continue;
				}
				if(giteaDatasetMap.get(datasetObj.toString()) == null) {
					continue;
				}
				attachMap.put("RepoID", giteaDatasetMap.get(datasetObj.toString()));
				try {
					giteaDataSetService.addGiteaDataSetByAttach(attachMap);
				}catch (Exception e) {
					logger.info("error to deal: " + JsonUtil.toJson(record));
				}
			}else {
				logger.info("not need to sync." + (i + 1) + " uuid=" + uuid);
			}
			
		}
		
		logger.info("finished to sync dataset.cost=" + (System.currentTimeMillis() -start) + " ms");
	}
	
}
