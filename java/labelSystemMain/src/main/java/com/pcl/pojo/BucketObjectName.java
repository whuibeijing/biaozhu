package com.pcl.pojo;

import com.pcl.util.JsonUtil;

public class BucketObjectName {

	public BucketObjectName(String wholeUrl) {
		String tmpUrl = wholeUrl;
		if(wholeUrl.startsWith("/minio/") ){
			tmpUrl = wholeUrl.substring("/minio/".length());
		}
		if(wholeUrl.startsWith("/obs/") || wholeUrl.startsWith("/dcm/")) {
			tmpUrl = wholeUrl.substring(5);
		}
		int index = tmpUrl.indexOf("/");
		if(index != -1) {
			bucketName = tmpUrl.substring(0,index);
			objectName = tmpUrl.substring(index + 1);
		}
	}
	
	private String bucketName;
	
	private String objectName;

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	
	public static void main(String[] args) {
		System.out.println(JsonUtil.toJson(new BucketObjectName("/minio/opendata/attachments/a/5/a5f092a4-8aaf-4f63-a66d-54071774952ba5f092a4-8aaf-4f63-a66d-54071774952b/instances_train2017.json")));
		System.out.println(JsonUtil.toJson(new BucketObjectName("/obs/opendata/attachments/a/5/a5f092a4-8aaf-4f63-a66d-54071774952ba5f092a4-8aaf-4f63-a66d-54071774952b/instances_train2017.json")));
		System.out.println(JsonUtil.toJson(new BucketObjectName("opendata/attachments/a/5/a5f092a4-8aaf-4f63-a66d-54071774952ba5f092a4-8aaf-4f63-a66d-54071774952b/instances_train2017.json")));
	}
	
}
