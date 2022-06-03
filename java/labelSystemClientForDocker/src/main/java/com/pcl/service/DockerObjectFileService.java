package com.pcl.service;

import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pcl.exception.LabelSystemException;
import com.pcl.pojo.BucketObjectName;



@Service
public class DockerObjectFileService {
	
	private static Logger logger = LoggerFactory.getLogger(DockerObjectFileService.class);
	
	private MinioFileService minioFileService;
	
	private OBSFileService obsFileService;
	
	@Value("${obs.ak:11}")
	private String accessKey; //取值为获取的AK

	@Value("${obs.sk:22}")
	private String securityKey;  //取值为获取的SK

	@Value("${obs.region:22}")
	private String region; // 取值为规划桶所在的区域
	
	@Value("${obs.url:22}")
	private String url;
	
	
	@Value("${minio.url:1}")
	private String minioUrl;

	@Value("${minio.username:1}")
	private String minioName;

	@Value("${minio.password:1}")
	private String minioPassword;

	public final static String MINIO_BUCKET_NAMEPREFIX = "/minio/";
	
	public final static String OBS_BUCKET_NAMEPREFIX = "/obs/";

	@PostConstruct
	public void init() {
		if(!accessKey.equals("22")) {
			obsFileService = new OBSFileService();
			obsFileService.init(accessKey, securityKey, region, url);
		}
		if(!minioUrl.equals("1")) {
			minioFileService = new MinioFileService();
			minioFileService.init(minioUrl, minioName, minioPassword);
		}
		
		logger.info("init gitea object service sucess.");
	}
	


	public InputStream getFile(String filename) throws Exception {
		BucketObjectName bucketObject = new BucketObjectName(filename);
		if(filename.startsWith(MINIO_BUCKET_NAMEPREFIX)) {
			return minioFileService.getPicture(bucketObject.getBucketName(), bucketObject.getObjectName());
		}else if(filename.startsWith(OBS_BUCKET_NAMEPREFIX)) {
			return obsFileService.getPicture(bucketObject.getBucketName(), bucketObject.getObjectName());
		}else {
			throw new LabelSystemException("cannot deal the filename=" + filename);
		}
		
	}
	
	public void downLoadDirFile(String objectFileRootPath,String destFilePath) {
		BucketObjectName bucketObject = new BucketObjectName(objectFileRootPath);
		try {
			List<String> allFileList = listAllFile(objectFileRootPath);
			for(String objectName : allFileList) {
				if(objectFileRootPath.startsWith(MINIO_BUCKET_NAMEPREFIX)) {
					minioFileService.downLoadFile(bucketObject.getBucketName(), objectName, destFilePath + objectName);
				}
				if(objectFileRootPath.startsWith(OBS_BUCKET_NAMEPREFIX)) {
					obsFileService.downLoadFile(bucketObject.getBucketName(), objectName, destFilePath + objectName);
				}
			}
		} catch (LabelSystemException e) {
			e.printStackTrace();
		}
	}
	
	public void downLoadAFile(String objectFilePath,String destFile) {
	
		try {
			BucketObjectName bucketObject = new BucketObjectName(objectFilePath);
			if(objectFilePath.startsWith(MINIO_BUCKET_NAMEPREFIX)) {
				minioFileService.downLoadFile(bucketObject.getBucketName(), bucketObject.getObjectName(), destFile);
			}
			else if(objectFilePath.startsWith(OBS_BUCKET_NAMEPREFIX)) {
				obsFileService.downLoadFile(bucketObject.getBucketName(), bucketObject.getObjectName(), destFile);
			}

		} catch (LabelSystemException e) {
			e.printStackTrace();
		}
	}

	public List<String> listAllFile(String objectFileRootPath) throws LabelSystemException {
		if(objectFileRootPath.startsWith(MINIO_BUCKET_NAMEPREFIX)) {
			return minioFileService.listAllFile(objectFileRootPath.substring(MINIO_BUCKET_NAMEPREFIX.length()));
		}else if(objectFileRootPath.startsWith(OBS_BUCKET_NAMEPREFIX)) {
			return obsFileService.listAllFile(objectFileRootPath.substring(OBS_BUCKET_NAMEPREFIX.length()));
		}else {
			throw new LabelSystemException("cannot deal the filename=" + objectFileRootPath);
		}
	}
}
