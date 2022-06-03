package com.pcl.pojo;

import java.io.Serializable;
import java.util.List;

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
public class ObjectDetectionTask implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//本次任务使用的模型名称
	private String modelName;

	//本次任务的ID,在任务完成之后，回复标注信息的时候要带回
	private String taskId;
	
	//对象存储文件的位置  /minio/ 或者/obs/   文件列表
	private List<String> pictureList;
	
	//每个文件对应 的ID
	private List<String> pictureIdList;
	
	//消息回调函数
	private String msgCallBackUrl;
	/*回调消息体,json：
	{
		"taskid":taskId,   //任务ID，需要带回
		"filename":文件名称，不需要带路径
	}
	*/

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
