package com.pcl.pojo.body;

import java.io.Serializable;
import java.util.List;

public class ObjectDetectionTaskBody implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 100L;
	
	//本次任务使用的模型名称
	private String modelName;

	//本次任务的ID,在任务完成之后，回复标注信息的时候要带回
	private String taskId;
	
	//对象存储文件的位置  /minio/ 或者/obs/   （有可能是目录，也可能只是一个文件）
	private List<String> pictureList;
	
	
	private List<String> pictureIdList;
	
	//消息回调函数
	private String msgCallBackUrl;

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	

	public String getMsgCallBackUrl() {
		return msgCallBackUrl;
	}

	public void setMsgCallBackUrl(String msgCallBackUrl) {
		this.msgCallBackUrl = msgCallBackUrl;
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
	
}
