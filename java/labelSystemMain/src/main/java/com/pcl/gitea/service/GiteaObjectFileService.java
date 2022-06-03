package com.pcl.gitea.service;

import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import com.obs.services.model.ListPartsRequest;
import com.obs.services.model.ListPartsResult;
import com.obs.services.model.Multipart;
import com.pcl.exception.LabelSystemException;
import com.pcl.gitea.constant.GiteaConstant;
import com.pcl.service.ObjectFileService;
import com.pcl.service.obs.OBSFileService;



@Service
@DependsOn("dbScriptUpdate")
public class GiteaObjectFileService {
	
	private static Logger logger = LoggerFactory.getLogger(GiteaObjectFileService.class);
	
	@Autowired
	private ObjectFileService minioFileService;
	
	@Value("${obs.defaulbucket:test-opendata}")
	private String defaultObsBucketName;
	
	@Value("${obs.ak:11}")
	private String accessKey; //取值为获取的AK

	@Value("${obs.sk:22}")
	private String securityKey;  //取值为获取的SK

	@Value("${obs.region:22}")
	private String region; // 取值为规划桶所在的区域
	
	@Value("${obs.url:22}")
	private String url;
	
	@Value("${obs.base.path:attachment}")
	private String obsBasePath;
	
	@Value("${minio.base.path:attachments}")
	private String minioBasePath;
	
	private OBSFileService obsFileService;
	
	@PostConstruct
	public void init() {
		obsFileService = new OBSFileService();
		obsFileService.init(accessKey, securityKey, region, url);
		logger.info("init gitea object service sucess.");
	}
	
	public String getDefaultObsBucketName() {
		return defaultObsBucketName;
	}
	
	public String getDefaulMinioBucketName() {
		return GiteaConstant.MINIO_DEFAULT_BUCKETNAME;
	}
	
	public ObjectFileService getObjectFileService(String type) {
		if(GiteaConstant.MINIO_FILESERVICE.equals(type)) {
			return minioFileService;
		}else if(GiteaConstant.OBS_FILESERVICE.equals(type)) {
			return obsFileService;
		}
		return null;
	}
	
	public InputStream getFile(String filename) throws Exception {
		
		if(filename.startsWith(GiteaConstant.MINIO_BUCKET_NAMEPREFIX)) {
			String bucketname = getDefaulMinioBucketName();
			String objectName = filename.substring(GiteaConstant.MINIO_BUCKET_NAMEPREFIX.length() + bucketname.length() + 1);
			return minioFileService.getPicture(bucketname, objectName);
		}else if(filename.startsWith(GiteaConstant.OBS_BUCKET_NAMEPREFIX)) {
			String bucketname = defaultObsBucketName;
			String objectName = filename.substring(GiteaConstant.OBS_BUCKET_NAMEPREFIX.length() + bucketname.length() + 1);
			return obsFileService.getPicture(bucketname, objectName);
		}else {
			throw new LabelSystemException("cannot deal the filename=" + filename);
		}
		
	}

	public String getObsBasePath() {
		return obsBasePath;
	}

	public String getMinioBasePath() {
		return minioBasePath;
	}

	public List<String> listAllFile(String objectFileRootPath) throws LabelSystemException {
		if(objectFileRootPath.startsWith(GiteaConstant.MINIO_BUCKET_NAMEPREFIX)) {
			return minioFileService.listAllFile(objectFileRootPath.substring(GiteaConstant.MINIO_BUCKET_NAMEPREFIX.length()));
		}else if(objectFileRootPath.startsWith(GiteaConstant.OBS_BUCKET_NAMEPREFIX)) {
			return obsFileService.listAllFile(objectFileRootPath.substring(GiteaConstant.OBS_BUCKET_NAMEPREFIX.length()));
		}else {
			throw new LabelSystemException("cannot deal the filename=" + objectFileRootPath);
		}
	}
	
	
	public void ListObsPart(String bucket,String objectName,String uploadId) {
		ListPartsRequest request = new ListPartsRequest(bucket, objectName);
		request.setUploadId(uploadId);
		ListPartsResult result;
		try {
			result = obsFileService.listPart(request);
			for(Multipart part : result.getMultipartList()){
			    // 分段号，上传时候指定
			   System.out.println("\t"+part.getPartNumber());
			    // 段数据大小
			   System.out.println("\t"+part.getSize());
			    // 分段的ETag值
			   System.out.println("\t"+part.getEtag());
			    // 段的最后上传时间
			   System.out.println("\t"+part.getLastModified());
			}
		} catch (LabelSystemException e) {
			e.printStackTrace();
		}

		
	}
	
}
