package com.pcl.service;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.HeaderResponse;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.pcl.exception.LabelSystemException;


public class OBSFileService implements ObjectFileService{

	private static Logger logger = LoggerFactory.getLogger(OBSFileService.class);


	private String accessKey; //取值为获取的AK


	private String securityKey;  //取值为获取的SK


	private String region; // 取值为规划桶所在的区域


	private String url;

	private String createBucketTemplate =
			"<CreateBucketConfiguration " +
					"xmlns=\"" + url + "/doc/2015-06-30/\">\n" +
					"<Location>" + region + "</Location>\n" +
					"</CreateBucketConfiguration>";

	private static String MEDICAL_BUCKETNAME = "healthcare";

	CloseableHttpClient httpClient = getHttpClient();

	ObsClient obsClient;



	public void init(String accessKey,String securityKey,String region,String url) {
		this.accessKey = accessKey;
		this.region = region;
		this.securityKey = securityKey;
		this.url = url;
		obsClient = new ObsClient(accessKey, securityKey, url);
		logger.info("obs client inited.url=" + url);
	}

	public void close() {
		if(obsClient != null) {
			try {
				obsClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private  CloseableHttpClient getHttpClient()  {
		try {
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
					return true;
				}
			}).build();

			return HttpClients.custom().setSSLContext(sslContext).
					setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
		}catch (Exception e) {
			logger.info(e.getMessage(), e);
		}
		return null;
	}


	public boolean createBucketNameBySDK(String bucketName) {
		// 创建桶
		try{
			// 创建桶成功
			HeaderResponse response = obsClient.createBucket(bucketName, region);

			logger.info("create bucket success: " + response.getRequestId());
			return true;
		}
		catch (ObsException e)
		{
			// 创建桶失败
			logger.info("HTTP Code: " + e.getResponseCode());
			logger.info("Error Code:" + e.getErrorCode());
			logger.info("Error Message: " + e.getErrorMessage());

			logger.info("Request ID:" + e.getErrorRequestId());
			logger.info("Host ID:" + e.getErrorHostId());
		}
		return false;

	}

	private String getUrl(String bucketName) {
		int length = "https://".length();

		String newUrl =  url.substring(0,length) + bucketName + "." + url.substring(length);
		logger.info("newUrl=" + newUrl);

		return newUrl;
	}

	public boolean createBucketName(String bucketName) {

		String requesttime = formateHuaWeiCloudDate(System.currentTimeMillis());
		String contentType = "application/xml";

		HttpPut httpPut = new HttpPut(getUrl(bucketName));

		httpPut.addHeader("Date", requesttime);
		httpPut.addHeader("Content-Type", contentType);

		/** 根据请求计算签名**/
		String contentMD5 = "";
		String canonicalizedHeaders = "";
		String canonicalizedResource = "/" + bucketName + "/";
		// Content-MD5 、Content-Type 没有直接换行， data格式为RFC 1123，和请求中的时间一致
		String canonicalString = "PUT" + "\n" + contentMD5 + "\n" + contentType + "\n" + requesttime + "\n" + canonicalizedHeaders + canonicalizedResource;
		//logger.info("StringToSign:[" + canonicalString + "]");
		String signature = null;
		CloseableHttpResponse httpResponse = null;
		try {
			signature = signWithHmacSha1(securityKey, canonicalString);

			// 增加签名头域 Authorization: OBS AccessKeyID:signature
			httpPut.addHeader("Authorization", "OBS " + accessKey + ":" + signature);
			// 增加body体
			httpPut.setEntity(new StringEntity(createBucketTemplate));

			httpResponse = httpClient.execute(httpPut);
			StatusLine httpResponseStatus = httpResponse.getStatusLine();
			if(httpResponseStatus.getStatusCode() == 200) {
				logger.info("createBucketName " + bucketName +  " success.");
				return true;
			}else {

				outputHttpRespone(httpResponse);
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		logger.info("createBucketName " + bucketName +  " request error.");
		return false;
	}



	public boolean putObjectToBucket(String bucketName,List<File> fileList) {

		CloseableHttpResponse httpResponse = null;
		try {
			for(File file : fileList) {

				try(InputStream inputStream = new FileInputStream(file)){
					String requesttime = formateHuaWeiCloudDate(System.currentTimeMillis());
					HttpPut httpPut = new HttpPut(getUrl(bucketName) + "/" +  file.getName());

					httpPut.addHeader("Date", requesttime);

					/** 根据请求计算签名 **/
					String contentMD5 = "";
					String contentType = "";
					String canonicalizedHeaders = "";
					String canonicalizedResource = "/"+ bucketName + "/" + file.getName();
					// Content-MD5 、Content-Type 没有直接换行， data格式为RFC 1123，和请求中的时间一致
					String canonicalString = "PUT" + "\n" + contentMD5 + "\n" + contentType + "\n" + requesttime + "\n" + canonicalizedHeaders + canonicalizedResource;
					//logger.info("StringToSign:[" + canonicalString + "]");
					String signature = null;

					signature = signWithHmacSha1(securityKey, canonicalString);
					// 上传的文件目录

					InputStreamEntity entity = new InputStreamEntity(inputStream);
					httpPut.setEntity(entity);

					// 增加签名头域 Authorization: OBS AccessKeyID:signature
					httpPut.addHeader("Authorization", "OBS " + accessKey + ":" + signature);
					httpResponse = httpClient.execute(httpPut);

					StatusLine httpResponseStatus = httpResponse.getStatusLine();
					// 打印发送请求信息和收到的响应消息
					if(httpResponseStatus.getStatusCode() != 200) {
						logger.info("putObjectToBucket request error.");
						outputHttpRespone(httpResponse);
						return false;
					}
					logger.info("put file to obs succeed. file=" + file.getAbsolutePath());
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;

	}


	public void getObjectFromBucket(String bucketName,String objectName,String destPath) {


		CloseableHttpResponse httpResponse = null;
		HttpGet httpGet = new HttpGet(getUrl(bucketName) + "/" + objectName);

		String requesttime = formateHuaWeiCloudDate(System.currentTimeMillis());
		httpGet.addHeader("Date", requesttime);

		/** 根据请求计算签名**/
		String contentMD5 = "";
		String contentType = "";
		String canonicalizedHeaders = "";
		String canonicalizedResource = "/"+ bucketName + "/" + objectName;
		// Content-MD5 、Content-Type 没有直接换行， data格式为RFC 1123，和请求中的时间一致
		String canonicalString = "GET" + "\n" + contentMD5 + "\n" + contentType + "\n" + requesttime + "\n" + canonicalizedHeaders + canonicalizedResource;
		//logger.info("StringToSign:[" + canonicalString + "]");
		String signature = null;
		try {
			signature = signWithHmacSha1(securityKey, canonicalString);

			// 增加签名头域 Authorization: OBS AccessKeyID:signature
			httpGet.addHeader("Authorization", "OBS " + accessKey + ":" + signature);
			httpResponse = httpClient.execute(httpGet);

			// 打印发送请求信息和收到的响应消息

			StatusLine httpResponseStatus = httpResponse.getStatusLine();
			if(httpResponseStatus.getStatusCode() == 200) {
				logger.info("get success.bucketName=" +bucketName + " objectname=" + objectName);
				//解释xml格式
				try(InputStream in = httpResponse.getEntity().getContent();
						FileOutputStream outputStream = new FileOutputStream(new File(destPath,objectName))){;
						byte buf[] = new byte[4096];
						while(true) {
							int length = in.read(buf);
							if(length == -1) {
								break;
							}
							outputStream.write(buf, 0, length);
						}
				}
			}else {
				logger.info("getObjectFromBucket request error.");
				outputHttpRespone(httpResponse);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}



	private void outputHttpRespone(CloseableHttpResponse httpResponse) {
		logger.info("" +httpResponse.getStatusLine());
		for (Header header : httpResponse.getAllHeaders()) {
			logger.info(header.getName() + ":" + header.getValue());
		}
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(
				httpResponse.getEntity().getContent()))){

			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
				logger.info(inputLine);
			}

		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static String signWithHmacSha1(String sk, String canonicalString) throws UnsupportedEncodingException {

		try {
			SecretKeySpec signingKey = new SecretKeySpec(sk.getBytes("UTF-8"), "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);
			return Base64.getEncoder().encodeToString(mac.doFinal(canonicalString.getBytes("UTF-8")));
		} catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String formateHuaWeiCloudDate(long time){
		DateFormat serverDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
		serverDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return serverDateFormat.format(time);
	}





	@Override
	public InputStream getDziPicture(String bucketName, String objectName) throws Exception {
		objectName = bucketName + "/" + objectName;
		return getPicture(MEDICAL_BUCKETNAME, objectName);
	}

	@Override
	public InputStream getPicture(String bucketName, String fileName) throws Exception {
		//bucketName = BUCKETNAME;
		ObsObject obsObject = obsClient.getObject(bucketName, fileName);

		// 读取对象内容
		return obsObject.getObjectContent();
	}




	@Override
	public String getImageWidthHeight(String relativeMinioUrl) {
		try {

			if(relativeMinioUrl.startsWith("/minio/") || relativeMinioUrl.startsWith("/obs/")) {
				ObsPath obsPath = getObsPath(relativeMinioUrl);
				try(InputStream inputStream = getPicture(obsPath.bucketName, obsPath.fileName)){
					BufferedImage sourceImg =ImageIO.read(inputStream);
					int width = sourceImg.getWidth();
					int height = sourceImg.getHeight();
					return width + "," + height;
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return "";
	}

	class ObsPath{
		String bucketName;
		String fileName;
	}

	private ObsPath getObsPath(String relativeMinioUrl) {
		if(relativeMinioUrl.startsWith("/minio/") || relativeMinioUrl.startsWith("/dcm/") || relativeMinioUrl.startsWith("/obs/")) {
			String tmp[] = relativeMinioUrl.split("/");
			String bucketName = tmp[2];
			int index = relativeMinioUrl.indexOf("/" + bucketName + "/");
			String fileName = relativeMinioUrl.substring(index + bucketName.length() + 2);
			ObsPath obsPath = new ObsPath();
			obsPath.bucketName = bucketName;
			obsPath.fileName = fileName;
			return obsPath;
		}
		return null;
	}

	@Override
	public BufferedImage getBufferedImage(String relativeMinioUrl) {
		try {

			if(relativeMinioUrl.startsWith("/minio/") || relativeMinioUrl.startsWith("/obs/")) {
				ObsPath obsPath = getObsPath(relativeMinioUrl);
				try(InputStream inputStream = getPicture(obsPath.bucketName, obsPath.fileName)){
					BufferedImage sourceImg =ImageIO.read(inputStream);

					return sourceImg;
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}



	@Override
	public InputStream getImageInputStream(String relativeMinioUrl) {
		try {
			if(relativeMinioUrl.startsWith("/minio/") || relativeMinioUrl.startsWith("/dcm/") || relativeMinioUrl.startsWith("/obs/")) {
				ObsPath obsPath = getObsPath(relativeMinioUrl);
				return getPicture(obsPath.bucketName, obsPath.fileName);
			}}catch (Exception e) {
				e.printStackTrace();
				logger.info(e.getMessage());
			}

		return null;
	}


	@Override
	public boolean isExistMinioFile(String bucketName, String objectName) {
		//bucketName = BUCKETNAME;
		CloseableHttpResponse httpResponse = null;
		HttpHead httpGet = new HttpHead(getUrl(bucketName) + "/" + objectName);

		String requesttime = formateHuaWeiCloudDate(System.currentTimeMillis());
		httpGet.addHeader("Date", requesttime);

		/** 根据请求计算签名**/
		String contentMD5 = "";
		String contentType = "";
		String canonicalizedHeaders = "";
		String canonicalizedResource = "/"+ bucketName + "/" + objectName;
		// Content-MD5 、Content-Type 没有直接换行， data格式为RFC 1123，和请求中的时间一致
		String canonicalString = "HEAD" + "\n" + contentMD5 + "\n" + contentType + "\n" + requesttime + "\n" + canonicalizedHeaders + canonicalizedResource;
		//logger.info("StringToSign:[" + canonicalString + "]");
		String signature = null;
		try {
			signature = signWithHmacSha1(securityKey, canonicalString);

			// 增加签名头域 Authorization: OBS AccessKeyID:signature
			httpGet.addHeader("Authorization", "OBS " + accessKey + ":" + signature);
			httpResponse = httpClient.execute(httpGet);

			// 打印发送请求信息和收到的响应消息

			StatusLine httpResponseStatus = httpResponse.getStatusLine();
			if(httpResponseStatus.getStatusCode() == 200) {
				return true;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return false;
	}


	@Override
	public boolean isExistMinioFileAndDeleteNotComplete(String bucketName, String objectName) {
		//bucketName = BUCKETNAME;
		return isExistMinioFile(bucketName, objectName);
	}


	@Override
	public long getMinioObjectLength(String bucketName, String objectName) {
		//bucketName = BUCKETNAME;
		logger.info("getMinioObjectLength  bucketName=" + bucketName + " objectName=" + objectName);
		ObjectMetadata metadata = obsClient.getObjectMetadata(bucketName, objectName);
		if(metadata != null) {
			return metadata.getContentLength();
		}
		return 0l;
	}




	public boolean isExistBucketNameBySDK(String bucketName) {
		return obsClient.headBucket(bucketName);
	}

	


	public void uploadSvsDziFile(File file, String objectName, String bucketName) {
		objectName = bucketName + "/" + objectName; //需要特殊处理。

		uploadFile(file, objectName, MEDICAL_BUCKETNAME);
	}


	private void uploadFile(File file, String objectName, String bucketName) {

		CloseableHttpResponse httpResponse = null;
		try {
			try(InputStream inputStream = new FileInputStream(file)){
				String requesttime = formateHuaWeiCloudDate(System.currentTimeMillis());
				HttpPut httpPut = new HttpPut(getUrl(bucketName) + "/" + objectName);

				httpPut.addHeader("Date", requesttime);

				/** 根据请求计算签名 **/
				String contentMD5 = "";
				String contentType = "";
				String canonicalizedHeaders = "";
				String canonicalizedResource = "/"+ bucketName + "/" + objectName;
				// Content-MD5 、Content-Type 没有直接换行， data格式为RFC 1123，和请求中的时间一致
				String canonicalString = "PUT" + "\n" + contentMD5 + "\n" + contentType + "\n" + requesttime + "\n" + canonicalizedHeaders + canonicalizedResource;
				//logger.info("StringToSign:[" + canonicalString + "]");
				String signature = null;

				signature = signWithHmacSha1(securityKey, canonicalString);
				// 上传的文件目录

				InputStreamEntity entity = new InputStreamEntity(inputStream);
				httpPut.setEntity(entity);

				// 增加签名头域 Authorization: OBS AccessKeyID:signature
				httpPut.addHeader("Authorization", "OBS " + accessKey + ":" + signature);
				httpResponse = httpClient.execute(httpPut);

				StatusLine httpResponseStatus = httpResponse.getStatusLine();
				// 打印发送请求信息和收到的响应消息
				if(httpResponseStatus.getStatusCode() == 200) {
					logger.info("put file to obs succeed. file=" + file.getAbsolutePath());
				}else {
					logger.info("putObjectToBucket request error.");
					outputHttpRespone(httpResponse);
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static String getBuketName(String dataSetType) {
		return MEDICAL_BUCKETNAME;
	}

	




	@Override
	public String downLoadFile(String bucketName, String objectName, String destPath)
			throws LabelSystemException {
		try {
			//bucketName = BUCKETNAME;
			logger.info("start to download file:" + bucketName + "/" + objectName + ", destPath=" + destPath);
			String destFilePath = destPath + File.separator + objectName;
			new File(destFilePath).getParentFile().mkdirs();
			logger.info("create dir=" + new File(destFilePath).getParentFile().getAbsolutePath());
			try(InputStream intpuStream = getPicture(bucketName, objectName);
					FileOutputStream outputStream = new FileOutputStream(destFilePath)){
				byte buffer[] = new byte[2048];
				while(true) {
					int length = intpuStream.read(buffer);
					if(length < 0) {
						break;
					}
					outputStream.write(buffer, 0, length);
				}
			}
			logger.info("succeed to download file success.");
			return destPath + File.separator + objectName;

		} catch (Exception e) {
			e.printStackTrace();
			logger.info("error to upload file end.");
			throw new LabelSystemException("Failed to download file.");
		}
	}


	@Override
	public String downLoadFileAndSetPictureName(String bucketName, String objectName, String pictureName)
			throws LabelSystemException {
		try {
			//bucketName = BUCKETNAME;
			new File(pictureName).getParentFile().mkdirs();
			try(InputStream intpuStream = getPicture(bucketName, objectName);
					FileOutputStream outputStream = new FileOutputStream(pictureName)){
				byte buffer[] = new byte[2048];
				while(true) {
					int length = intpuStream.read(buffer);
					if(length < 0) {
						break;
					}
					outputStream.write(buffer, 0, length);
				}
			}
			logger.info("succeed to download file success. minioName=" + bucketName + "/" + objectName + ", pictureName=" + pictureName);
			return pictureName;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("error to upload file end.");
			throw new LabelSystemException("Failed to download file.");
		}
	}

	@Override
	public List<String> listAllFile(String obsPath) {

		int index = obsPath.indexOf("/",1);
		String bucketName = obsPath.substring(0,index);
		if(bucketName.startsWith("/")) {
			bucketName = bucketName.substring(1);
		}
		String directory = obsPath.substring(index + 1);
		if(!directory.endsWith("/")) {
			directory += "/";
		}
		logger.info("list all file from obs,bucket name=" + bucketName + "   directory=" + directory);
		List<String> resultList = new ArrayList<>();
		ListObjectsRequest request = new ListObjectsRequest(bucketName);
		// 设置文件夹对象名"dir/"为前缀
		request.setPrefix(directory);
		request.setMaxKeys(1000);

		ObjectListing result;
		do{
			result = obsClient.listObjects(request);
			for (ObsObject obsObject : result.getObjects()){
				//System.out.println("\t" + obsObject.getObjectKey());
				//System.out.println("\t" + obsObject.getOwner());
				resultList.add(obsObject.getObjectKey());
			}
			request.setMarker(result.getNextMarker());
		}while(result.isTruncated());

		logger.info("total file is :" + resultList.size());

		return resultList;


	}

	@Override
	public InputStream getInputStream(String bucketname, String fileName, long offset) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	





}
