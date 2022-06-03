package com.pcl.service;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

import com.pcl.exception.LabelSystemException;


public interface ObjectFileService {

	
	public InputStream getInputStream(String bucketname,String fileName,long offset) throws Exception;

	public InputStream getPicture(String bucketname,String fileName) throws Exception;
	
	public InputStream getDziPicture(String bucketname,String fileName) throws Exception;


	public String getImageWidthHeight(String relativeMinioUrl);

	public BufferedImage getBufferedImage(String relativeMinioUrl);

	public InputStream getImageInputStream(String relativeMinioUrl);


	public boolean isExistMinioFile(String bucketname,String objectName);
	
	public boolean isExistMinioFileAndDeleteNotComplete(String bucketname,String objectName);

	public long getMinioObjectLength(String bucketname,String objectName);


	public String downLoadFile(String bucketName, String objectName,String destPath) throws LabelSystemException ;

	public String downLoadFileAndSetPictureName(String bucketName, String objectName,String pictureName) throws LabelSystemException;
	
	public List<String> listAllFile(String obsPath);

}
