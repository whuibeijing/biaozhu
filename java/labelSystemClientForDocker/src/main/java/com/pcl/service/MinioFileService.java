package com.pcl.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xmlpull.v1.XmlPullParserException;

import com.pcl.exception.LabelSystemException;

import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.NoResponseException;
import io.minio.messages.Item;


public class MinioFileService implements ObjectFileService{

	private static Logger logger = LoggerFactory.getLogger(MinioFileService.class);


	private MinioClient minioClient;


	public InputStream getPicture(String bucketname,String fileName) throws Exception {
		MinioClient minioClient = getMinioClient();

		return minioClient.getObject(bucketname, fileName);
	}
	
	public void init(String minioUrl,String minioName,String minioPassword) {
		try {
			minioClient = new MinioClient(minioUrl, minioName, minioPassword);
		} catch (InvalidEndpointException e) {
			e.printStackTrace();
		} catch (InvalidPortException e) {
			e.printStackTrace();
		} 
	}


	private MinioClient getMinioClient() {
		
		return minioClient;
	}


	public String getImageWidthHeight(String relativeMinioUrl) {
		try {
			MinioClient minioClient = getMinioClient();
			if(relativeMinioUrl.startsWith("/minio/")) {
				String tmp[] = relativeMinioUrl.split("/");
				int length = tmp.length;
				try(InputStream inputStream = (minioClient.getObject(tmp[length-2], tmp[length-1]))){
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

	public BufferedImage getBufferedImage(String relativeMinioUrl) {
		try {
			MinioClient minioClient = getMinioClient();
			if(relativeMinioUrl.startsWith("/minio/")) {
				String tmp[] = relativeMinioUrl.split("/");
				int length = tmp.length;
				try(InputStream inputStream = (minioClient.getObject(tmp[length-2], tmp[length-1]))){
					BufferedImage sourceImg =ImageIO.read(inputStream);

					return sourceImg;
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public InputStream getImageInputStream(String relativeMinioUrl) {
		try {
			MinioClient minioClient = getMinioClient();
			if(relativeMinioUrl.startsWith("/minio/") || relativeMinioUrl.startsWith("/dcm/")) {
				String tmp[] = relativeMinioUrl.split("/");
				int length = tmp.length;
				return minioClient.getObject(tmp[length-2], tmp[length-1]);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}




	public boolean isExistMinioFile(String bucketname,String objectName) {
		try {
			MinioClient minioClient = getMinioClient();
			ObjectStat obj = minioClient.statObject(bucketname, objectName);
			if(obj != null) {
				return true;
			}
		}catch (Exception e) {
			//e.printStackTrace();
			logger.info("the file is not exist, bucketname=" + bucketname +  " objectName=" + objectName);
			logger.info(e.getMessage());
		}
		return false;
	}
	
	public boolean isExistMinioFileAndDeleteNotComplete(String bucketname,String objectName) {
		try {
			MinioClient minioClient = getMinioClient();
			ObjectStat obj = minioClient.statObject(bucketname, objectName);
			if(obj != null) {
				if(obj.length() > 20) {
					return true;
				}else {
					//不是完整的，删除
					minioClient.removeObject(bucketname, objectName);
				}
			}
		}catch (Exception e) {
			//e.printStackTrace();
			logger.info("the file is not exist, bucketname=" + bucketname +  " objectName=" + objectName);
			logger.info(e.getMessage());
		}
		return false;
	}

	public long getMinioObjectLength(String bucketname,String objectName) {
		try {
			MinioClient minioClient = getMinioClient();
			ObjectStat obj = minioClient.statObject(bucketname, objectName);
			if(obj != null) {
				return obj.length();
			}
		}catch (Exception e) {
			//e.printStackTrace();
			logger.info("the file is not exist, bucketname=" + bucketname +  " objectName=" + objectName);
			logger.info(e.getMessage());
		}
		return -1;
	}



	public String downLoadFile(String bucketName, String objectName,String destFilePath) throws LabelSystemException {
		try {
			logger.info("start to download file:" + bucketName + "/" + objectName + ", destFilePath=" + destFilePath);
			
			new File(destFilePath).getParentFile().mkdirs();
			MinioClient minioClient = getMinioClient();
			try(InputStream intpuStream = minioClient.getObject(bucketName, objectName);
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
			return destFilePath;

		} catch ( InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | IOException | XmlPullParserException e) {
			e.printStackTrace();
			logger.info("error to upload file end.");
			throw new LabelSystemException("Failed to download file.");
		}
	}

	public String downLoadFileAndSetPictureName(String bucketName, String objectName,String pictureName) throws LabelSystemException {
		try {
			//logger.info("start to download file:" + bucketName + "/" + objectName + ", pictureName=" + pictureName);
			MinioClient minioClient = getMinioClient();
			try(InputStream intpuStream = minioClient.getObject(bucketName, objectName);
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
		} catch ( InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | IOException | XmlPullParserException e) {
			e.printStackTrace();
			logger.info("error to upload file end.");
			throw new LabelSystemException("Failed to download file.");
		}
	}


	@Override
	public List<String> listAllFile(String minioPath)  {
		ArrayList<String> result = new ArrayList<>(); 
		try {
			MinioClient minioClient = getMinioClient();
			String bucketName = "";
			String prefix = "";
			int index = minioPath.indexOf("/");
			if(index != -1) {
				bucketName = minioPath.substring(0,index);
				prefix = minioPath.substring(index + 1);
				
				Iterable<Result<Item>> re = minioClient.listObjects(bucketName, prefix);
				Iterator<Result<Item>> iterator = re.iterator();
				while(iterator.hasNext()) {
					Result<Item> item = iterator.next();
					result.add(item.get().objectName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}



	@Override
	public InputStream getInputStream(String bucketname, String fileName, long offset) throws Exception {
		MinioClient minioClient = getMinioClient();
		return minioClient.getObject(bucketname, fileName,offset);
	}



	@Override
	public InputStream getDziPicture(String bucketname, String fileName) throws Exception {
		return getPicture(bucketname, fileName);
	}




}
