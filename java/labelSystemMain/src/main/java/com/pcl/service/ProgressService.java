package com.pcl.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pcl.dao.ProgressDao;
import com.pcl.pojo.Progress;
import com.pcl.util.JsonUtil;

@EnableScheduling   // 开启定时任务
@Service
public class ProgressService {

	@Autowired
	private ProgressDao progressDao;

	private ConcurrentHashMap<String, Progress> progressCache = new ConcurrentHashMap<>();

	private static Logger logger = LoggerFactory.getLogger(ProgressService.class);

	public void updateProgress(String id,long process) {

		Progress pro = progressCache.get(id);
		if(pro != null) {
			pro.setProgress(process);
		}

		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("id", id);
		paramMap.put("progress", process);
		logger.info("update progress:" + JsonUtil.toJson(paramMap));
		progressDao.updateProgress(paramMap);
	}

	public void updateProgress(Progress pro,long process) {
		//		Map<String,Object> paramMap = new HashMap<>();
		//		paramMap.put("id", pro.getId());
		//		paramMap.put("progress", (long)(process * pro.getRatio() + pro.getBase()));
		//		logger.info("update progress:" + JsonUtil.toJson(paramMap));
		//		progressDao.updateProgress(paramMap);

		updateProgress(pro.getId(), (long)(process * pro.getRatio() + pro.getBase()));
	}

	public Progress queryProgressById(String taskId) {
		if(progressCache.get(taskId) != null) {
			return progressCache.get(taskId);
		}
		Progress pro = progressDao.queryProgressById(taskId);
		//logger.info("query progress=" + JsonUtil.toJson(pro));
		return pro;
	}

	public Progress queryLabelTask(String taskId) {
		Progress progress = progressDao.queryProgressById(taskId);
		if(progress != null) {
			long escTime = (System.currentTimeMillis()/1000) - progress.getStartTime();
			logger.info("escTime=" + escTime);
			double proD = escTime * 1.0/progress.getExceedTime();
			if(proD >= 1) {
				proD = 1;
			}
			progress.setProgress((int)(proD * 100));
			return progress;
		}
		logger.info("return null progress.");
		return null;
	}

	public void removeProgress(String taskId) {

		//progressDao.deleteProgress(taskId);
		removeProgressByInfo(taskId);
	}

	public void removeProgressByInfo(String taskId) {
		Progress tmp = progressDao.queryProgressById(taskId);
		if(tmp != null) {
			logger.info("pro tmp=" + JsonUtil.toJson(tmp));

			progressDao.deleteProgress(taskId);
			logger.info("success delete progress task,info=" + JsonUtil.toJson(tmp));
		}else {
			logger.info("task has been deleted.taskId=" + taskId);
		}
		progressCache.remove(taskId);
	}

	public void putProgress(Progress pro) {
		logger.info("add progress:" + JsonUtil.toJson(pro));
		Progress tmp = progressDao.queryProgressById(pro.getId());
		if(tmp != null) {
			progressDao.deleteProgress(pro.getId());
		}
		progressCache.put(pro.getId(), pro);
		progressDao.addProgress(pro);
	}

	public long updateProgress(String taskId, Map<String, Object> msgMap) {

		Progress progress = queryProgressById(taskId);
		logger.info("taskId=" + taskId + " progress=" + JsonUtil.toJson(progress));
		if(progress != null) {
			progress.setTotalTime(progress.getTotalTime() + 1);
			if(progress.getInfo() != null) {
				Map<String,Object> map = JsonUtil.getMap(progress.getInfo());
				int total = JsonUtil.getIntValue("total", map);
				if(progress.getTotalTime() <= total) {
					progress.setProgress((long)((progress.getTotalTime() * 100.0)/total));
				}
			}

			updateProgress(progress.getId(), progress.getProgress());
			return progress.getProgress();
		}
		return 0l;
	}


	public long updateTrackProgress(String taskId, Map<String, Object> msgMap) {

		Progress progress = queryProgressById(taskId);
		logger.info("taskId=" + taskId + " progress=" + JsonUtil.toJson(progress));
		if(progress != null) {
			int total = JsonUtil.getIntValue("total", msgMap);
			int index = JsonUtil.getIntValue("index", msgMap);
			progress.setProgress((long)((index * 100.0)/total));
			updateProgress(progress.getId(), progress.getProgress());
			return progress.getProgress();
		}
		return 0l;
	}


	@Scheduled(cron = "0 0 0 */2 * ?")//每隔2分钟检查一下心跳
	public void dealExceedTime() {
		List<String> idList = new ArrayList<>();
		for(Entry<String, Progress> entry : progressCache.entrySet()) {
			Progress pro = entry.getValue();
			long currentTime = System.currentTimeMillis() / 1000;
			if(currentTime - pro.getStartTime() > pro.getExceedTime()) {
				idList.add(entry.getKey());				
			}
		}
		//删除已经超时的任务
		for(String id : idList) {
			removeProgress(id);
		}

	}

}
