package com.pcl.pojo.body;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 	接受任务的任务信息。消息体：
  *      {
  *       taskId:   此次消息id，在msgCallBackUrl中要带回此taskId
  *       pictureList: 对象文件存储地址列表
  *       pictureIdList: 文件对应的数据库id列表
  *       msgCallBackUrl: 消息回调URL，即返回标注信息
  *      }
 * 
 */
public class ObjectTrackTask implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 10000L;
	
	//本次任务使用的模型名称
	private String modelName;

	//本次任务的ID,在任务完成之后，回复标注信息的时候要带回
	private String taskId;
	
	//对象存储文件的位置  /minio/ 或者/obs/   文件列表
	private List<String> pictureList;
	
	//每个文件对应 的ID
	private List<String> pictureIdList;
	
	//单目标跟踪
	private boolean isSingleTrack;
	
	private int useSence; //1 reid
	
	//如果是单目标跟踪，则第一帧的标注信息
	private Map<String,Object> firstTrackInfo;
	
	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}


	public List<String> getPictureList() {
		return pictureList;
	}

	public void setPictureList(List<String> pictureList) {
		this.pictureList = pictureList;
	}

	public List<String> getPictureIdList() {
		return pictureIdList;
	}

	public void setPictureIdList(List<String> pictureIdList) {
		this.pictureIdList = pictureIdList;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}


	public boolean isSingleTrack() {
		return isSingleTrack;
	}

	public void setSingleTrack(boolean isSingleTrack) {
		this.isSingleTrack = isSingleTrack;
	}

	public int getUseSence() {
		return useSence;
	}

	public void setUseSence(int useSence) {
		this.useSence = useSence;
	}

	public Map<String, Object> getFirstTrackInfo() {
		return firstTrackInfo;
	}

	public void setFirstTrackInfo(Map<String, Object> firstTrackInfo) {
		this.firstTrackInfo = firstTrackInfo;
	}
	
	
}
