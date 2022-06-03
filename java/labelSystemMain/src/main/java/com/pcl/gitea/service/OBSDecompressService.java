package com.pcl.gitea.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.pcl.gitea.constant.GiteaConstant;
import com.pcl.service.LabelDataSetMerge;
import com.pcl.service.ObjectFileService;
import com.pcl.service.schedule.ThreadSchedule;
import com.pcl.util.FileUtil;
import com.pcl.util.JsonUtil;

@Service
public class OBSDecompressService {

	private static Logger logger = LoggerFactory.getLogger(OBSDecompressService.class);
	
	@Value("${obs.callbackurl}")
	private String callBackUrl;  //attachments/decompress_done_notify
	
	@Autowired
	private GiteaObjectFileService giteaObjectFileService;

	private final static String DecompressSuccess = "0";
	private final static String DecompressFailed  = "1";
	
	public void decopressObsFile(Map<String,Object> attachMap) {
		
		String datasetuuid = attachMap.get("UUID").toString();
		String time = String.valueOf(System.nanoTime());
		String tmpPath = LabelDataSetMerge.getUserDataSetPath() + File.separator + time;
		try {
			
			logger.info("init obs service success.");
			new File(tmpPath).mkdirs();
			String name = attachMap.get("Name").toString();
			
			ObjectFileService fileService = giteaObjectFileService.getObjectFileService(GiteaConstant.OBS_FILESERVICE);
			
			String obsDefaultBucketName = giteaObjectFileService.getDefaultObsBucketName();
			
			String objectName = giteaObjectFileService.getObsBasePath() + "/" + datasetuuid.charAt(0) + "/" + datasetuuid.charAt(1) + "/" + datasetuuid + "/" + name;

			long fileLength = fileService.getMinioObjectLength(obsDefaultBucketName, objectName);
			if(fileLength > 50 * 1024 * 1024 * 1024l) {//50G以上，不处理
				
				logger.info("The file size is too large. so do not deal it. fileLength=" + fileLength);
				sendSuccessMsgToCallBackUrl(datasetuuid,DecompressFailed);
				return;
			}
			
			//获取图片，解压
			if(name.toLowerCase().endsWith(".zip")) {
				decompressZipFile(fileService,obsDefaultBucketName,objectName,datasetuuid, name, tmpPath);
			}else if(name.toLowerCase().endsWith(".tar.gz") || name.toLowerCase().endsWith(".tgz")) {
				decompressTarGzFile(fileService, obsDefaultBucketName, objectName, datasetuuid, objectName, tmpPath);
			}
		
			logger.info("unzip dataset  finised.");
			sendSuccessMsgToCallBackUrl(datasetuuid,DecompressSuccess);
		}catch (Exception e) {
			e.printStackTrace();
			sendSuccessMsgToCallBackUrl(datasetuuid,DecompressFailed);
		}finally {
			FileUtil.delDir(tmpPath);
		}
		
	}
	
	void decompressTarGzFile(ObjectFileService fileService,String obsDefaultBucketName,String objectName, String datasetuuid,String name,String tmpPath) throws Exception {

		String objectNamePrefix = giteaObjectFileService.getObsBasePath() + "/" + datasetuuid.charAt(0) + "/" + datasetuuid.charAt(1) + "/" + datasetuuid + datasetuuid + "/";

		try(TarArchiveInputStream fin = new TarArchiveInputStream(new GzipCompressorInputStream(fileService.getPicture(obsDefaultBucketName, objectName)));){
			TarArchiveEntry entry = null;
			while ((entry = fin.getNextTarEntry()) != null) {
				String zipEntryName = entry.getName();
				logger.info("zipEntryName22=" + zipEntryName);
				if(entry.isDirectory()) {
					continue;
				}else {
					File curfile = new File(tmpPath, entry.getName());
		            File parent = curfile.getParentFile();
		            if (!parent.exists()) {
		                parent.mkdirs();
		            }
	
		            final byte[] buffer = new byte[8096];
		            int n = 0;
		            long count=0;
		            while (-1 != (n = fin.read(buffer))) {
		                count = fileService.uploadFileByManyBytes(obsDefaultBucketName, objectNamePrefix + zipEntryName, buffer, n, count);
		            }
		            
		            //fileService.uploadFile(new FileInputStream(curfile), obsDefaultBucketName, objectNamePrefix + zipEntryName);
		            //curfile.delete();
				}
			}
		}
	}
	
	
	private void decompressZipFile(ObjectFileService fileService,String obsDefaultBucketName,String objectName, String datasetuuid,String name,String tmpPath) throws Exception {
		
		String downloadFilePath = fileService.downLoadFileFromMinio(obsDefaultBucketName, objectName, tmpPath);
		File downloadFile = new File(downloadFilePath);

		String objectNamePrefix = giteaObjectFileService.getObsBasePath() + "/" + datasetuuid.charAt(0) + "/" + datasetuuid.charAt(1) + "/" + datasetuuid + datasetuuid + "/";

		//解压上传
		try(ZipFile zip = new ZipFile(downloadFile,Charset.forName("GBK"))){//解决中文文件夹乱码
			for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {  
				ZipEntry entry = (ZipEntry) entries.nextElement();  
				String zipEntryName = entry.getName();
				if(entry.isDirectory()) {
					continue;
				}
				try(InputStream in = zip.getInputStream(entry)){
					fileService.uploadFile(in, obsDefaultBucketName, objectNamePrefix + zipEntryName);
				}
			}  
		}
	}

	private void sendSuccessMsgToCallBackUrl(String uuid,String result) {
		String url = callBackUrl;
		logger.info("url=" + url);
		//使用Restemplate来发送HTTP请求
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<String, Object>();
		postParameters.add("uuid", uuid);
		postParameters.add("result", result);
		HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(postParameters,headers);
		Object re = restTemplate.postForEntity(url, entity, Object.class);
		logger.info("json re=" + JsonUtil.toJson(re));
	}
	
	public static void main(String[] args) {
		String extraceFolder = "D:\\tmp\\111";
		try {
			FileInputStream inputStream = new FileInputStream(new File("D:\\avi\\cars_test.tgz"));
			try(TarArchiveInputStream fin = new TarArchiveInputStream(new GzipCompressorInputStream(inputStream));){
				TarArchiveEntry entry = null;
				// 将 tar 文件解压到 extractPath 目录下
				while ((entry = fin.getNextTarEntry()) != null) {
					String zipEntryName = entry.getName();
					logger.info("zipEntryName=" + zipEntryName);
					if(entry.isDirectory()) {
						File curfile = new File(extraceFolder, entry.getName());
						curfile.mkdir();
					}else {
						File curfile = new File(extraceFolder, entry.getName());
			            File parent = curfile.getParentFile();
			            if (!parent.exists()) {
			                parent.mkdirs();
			            }
			            // 将文件写出到解压的目录
			            IOUtils.copy(fin, new FileOutputStream(curfile));
					}
					
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		
	}

}
