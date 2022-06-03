package com.pcl.constant;

public class ModelConstants {

	public final static int OBJECT_DETECTION_SERVICE_TYPE_MULTI = 1; //目标检测服务类型，1代表了批量的自动标注服务，对应界面上自动标注服务
	
	public final static int OBJECT_DETECTION_SERVICE_TYPE_SINGLE = 2; //目标检测服务类型，2代表了单个图片的自动标注服务，对应人工标注界面上使用自动标注
	
	public final static int OBJECT_DETECTION_SERVICE_TYPE_LABEL_TASK_MULTI = 3; //目标检测服务类型，1代表了批量的自动标注服务，对应界面上自动标注服务
	
	public final static int OBJECT_TRACKING_SERVICE_TYPE_SINGLE = 5; //单个目标跟踪服务类型
	
	public final static int OBJECT_TRACKING_SERVICE_TYPE_MULTI = 6; //多个目标跟踪服务类型
	
	public final static int MODEL_ONLINE = 0;
	
	public final static int MODEL_OFFLINE = 1;
}
