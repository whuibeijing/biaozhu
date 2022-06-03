package com.pcl.gitea.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.util.Strings;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.pcl.constant.Constants;
import com.pcl.constant.UserConstants;
import com.pcl.dao.LabelDcmTaskItemDao;
import com.pcl.dao.LabelTaskDao;
import com.pcl.dao.LabelTaskItemDao;
import com.pcl.gitea.constant.GiteaConstant;
import com.pcl.pojo.Progress;
import com.pcl.pojo.mybatis.LabelTask;
import com.pcl.pojo.mybatis.LabelTaskItem;
import com.pcl.service.LabelDataSetMerge;
import com.pcl.service.LabelSystemDbPolicy;
import com.pcl.service.ProgressService;
import com.pcl.service.schedule.ThreadSchedule;
import com.pcl.util.CocoAnnotationsUtil;
import com.pcl.util.FileUtil;
import com.pcl.util.JsonUtil;
import com.pcl.util.LabelInfoUtil;
import com.pcl.util.TimeUtil;
import com.pcl.util.VocAnnotationsUtil;

@Service
public class GiteaLabelDataExportService {

	private static Logger logger = LoggerFactory.getLogger(GiteaLabelDataExportService.class);

	@Autowired
	private LabelTaskItemDao labelTaskItemDao;

	@Autowired
	private LabelDcmTaskItemDao labelDcmTaskItemDao;

	@Autowired
	private LabelTaskDao labelTaskDao;

	@Autowired
	private CocoAnnotationsUtil cocoUtil;

	@Autowired
	private VocAnnotationsUtil vocUtil;
	
	@Autowired
	private LabelSystemDbPolicy giteaDbPolicy;

	@Autowired
	private GiteaObjectFileService giteaObjectFileService;

	@Autowired
	private ProgressService progressService;
	
	private Gson gson = new Gson();
	
	private static final String LABEL_POSTFIX = "label";



	public String downloadLabelTaskFile(String labelTaskId, int type, int exportFormat, double maxscore,
			double minscore) {
		
		List<String> idList = new ArrayList<>();
		LabelTask labelTask = null;
		logger.info("download labelTaskId=" + labelTaskId);
		if(labelTaskId.startsWith("[")) {
			//Json格式
			idList.addAll(JsonUtil.getList(labelTaskId));
		}else {
			idList.add(labelTaskId);
			labelTask = labelTaskDao.queryLabelTaskById(labelTaskId);
		}
		String key = System.nanoTime() + LABEL_POSTFIX;

		String relatedName =  System.nanoTime() + File.separator + TimeUtil.getCurrentTimeStrByyyyyMMddHHmmss() +"_" + LABEL_POSTFIX + ".zip";
		if(labelTask != null) {
			relatedName =  System.nanoTime() + File.separator + FileUtil.getRemoveChineseCharName(labelTask.getTask_name()) +"_" + LABEL_POSTFIX + ".zip";
		}

		Progress pro = new Progress();
		pro.setId(key);
		pro.setStartTime(System.currentTimeMillis() / 1000);
		pro.setExceedTime(10 * 60);
		pro.setRelatedFileName(relatedName);

		progressService.putProgress(pro);
		
		String fileName = LabelDataSetMerge.getUserDataSetPath() + File.separator + relatedName;

		ThreadSchedule.execExportThread(()->{
			try {
				int total = idList.size();
				pro.setRatio( 1.0 / total);
				List<String> fileList = new ArrayList<>();

				double base = 0;
				for(String tmpTaskId : idList) {


					LabelTask tmpTask = labelTaskDao.queryLabelTaskById(tmpTaskId);
					String tmpFileName=   LabelDataSetMerge.getUserDataSetPath() + File.separator + System.nanoTime() + File.separator + FileUtil.getRemoveChineseCharName(tmpTask.getTask_name()) +"_" + LABEL_POSTFIX + ".zip";;
					
					downloadLabelTaskFileWriter(tmpTask, tmpFileName, type, pro,maxscore,minscore,exportFormat);
					fileList.add(tmpFileName);

					base+= pro.getRatio();
					pro.setBase((long)(base * 100));

					//确保文件写完成了，进度再更新到100
				}
				File zipFile = new File(fileName) ;
				zipFile.getParentFile().mkdirs();
				if(fileList.size() == 1) {//如果只有一个重新命名即可。
					new File(fileList.get(0)).renameTo(zipFile);
				}else {
					HashSet<String> zipEntrySet = new HashSet<>();
					int count = 1;
					try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
						for(String azipFileName : fileList) {
							File aZipFile = new File(azipFileName);
							String zipEntryName = aZipFile.getName();
							if(zipEntrySet.contains(zipEntryName)) {
								zipEntryName = count++ + "_" +  zipEntryName;
							}
							zipEntrySet.add(zipEntryName);
							zos.putNextEntry(new ZipEntry(zipEntryName));
							byte buffer[] = new byte[2048];
							try(FileInputStream inputStream = new FileInputStream(aZipFile)) {
								while(true) {
									int length = inputStream.read(buffer);
									if(length < 0) {
										break;
									}
									zos.write(buffer, 0, length);
								}
								zos.closeEntry();
							}
						}
					}
				}
				
				pro.setInfo("压缩完成。");
				//pro.setProgress(100l);
				progressService.updateProgress(pro.getId(), 100);
				logger.info("writer finished.fileName=" + fileName);
			} catch (Exception e) {
				pro.setId(e.getMessage());
				logger.info(e.getMessage(),e);
			}
		});

		return key;
		
	}
	
	public void downloadLabelTaskFileWriter(LabelTask labelTask,String fileName,int type,Progress pro,double maxscore,double minscore,int exportFormat) throws IOException {

		//Map<String,Object> typeKeyValue = JsonUtil.getMap(labelTask.getTask_label_type_info());
		Map<String,Object> typeOrColorMapName = new HashMap<>();


		long start = System.currentTimeMillis();

		long total = 0;
		int count = 0;

		File zipFile = new File(fileName) ;
		zipFile.getParentFile().mkdirs();
		try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {

			Map<String,Object> dataSetInfo = new HashMap<>();

			Map<String,Object> tmpParam = new HashMap<>();
			tmpParam.put("label_task_id", labelTask.getId());
			
			tmpParam.put("user_id", giteaDbPolicy.getTableName(labelTask.getId(), UserConstants.LABEL_TASK_SINGLE_TABLE,-1));
			
			//tmpParam.put("user_id", TokenManager.getUserTablePos(userId, UserConstants.LABEL_TASK_SINGLE_TABLE));
			//int count = 0;

			if(labelTask.getTask_type() == Constants.LABEL_TASK_TYPE_ORIGIN_DCM) {
				total = labelDcmTaskItemDao.queryLabelTaskItemPageCountByLabelTaskId(tmpParam);
			}else {
				total = labelTaskItemDao.queryLabelTaskItemPageCountByLabelTaskId(tmpParam);
			}

			int pageSize = 1000;
			Map<String,Object> lastCocoMap = new HashMap<>();
			for(int i = 0; i < (total/pageSize) +1; i++) {
				tmpParam.put("currPage", i * pageSize);
				tmpParam.put("pageSize", pageSize);
				if(labelTask.getTask_type() == Constants.LABEL_TASK_TYPE_ORIGIN_DCM) {
					List<LabelTaskItem> itemList = labelDcmTaskItemDao.queryLabelTaskItemPageByLabelTaskId(tmpParam);
					
					for(LabelTaskItem item : itemList) {
						writerDataToZip(type, pro, typeOrColorMapName, total, count, zos, dataSetInfo, item,maxscore,minscore,exportFormat);
						count++;
					}
					if(exportFormat == GiteaConstant.EXPORT_FORMAT_COCO) {
						cocoUtil.getCocoJson(itemList, lastCocoMap);
					}
				}else {
					List<LabelTaskItem> itemList =labelTaskItemDao.queryLabelTaskItemPageByLabelTaskId(tmpParam);
					for(LabelTaskItem item : itemList) {
						writerDataToZip(type, pro, typeOrColorMapName, total, count, zos, dataSetInfo, item,maxscore,minscore,exportFormat);
						count++;
					}
					if(exportFormat == GiteaConstant.EXPORT_FORMAT_COCO) {
						cocoUtil.getCocoJson(itemList, lastCocoMap);
					}
				}
			}
			if(exportFormat == GiteaConstant.EXPORT_FORMAT_COCO) {
				writeJson(lastCocoMap, "instances_train2017.json", zos);
			}
			long end = System.currentTimeMillis();
			logger.info("finished zip, filename=" +fileName + " ,耗时：" + (end - start) +" ms");
		} catch (Exception e) {
			throw new RuntimeException("zip error from ZipUtils",e);
		}
	}
	
	private void writerDataToZip(int type, Progress pro, Map<String, Object> typeOrColorMapName, long total, int count,
			ZipOutputStream zos, Map<String, Object> dataSetInfo, LabelTaskItem item,double maxscore,double minscore,int exportFormat) throws Exception {
		if(type ==3 ) {
			//抠图
			writeCutImageToOutputZip(item, zos,maxscore,minscore);

		}else {
			if(exportFormat == GiteaConstant.EXPORT_FORMAT_VOC) {
				writeXml(item, zos,dataSetInfo,typeOrColorMapName);
			}
			if(type == 2) {
				try(InputStream intpuStream = giteaObjectFileService.getFile(item.getPic_image_field())){
					if(intpuStream == null) {
						return;
					}
					String fileName = LabelInfoUtil.getRealPath(item.getPic_image_field());
					String entryName = fileName;
					if(!entryName.startsWith("img/")) {
						entryName = "img/" + entryName;
					}
					zos.putNextEntry(new ZipEntry(entryName));
					byte buffer[] = new byte[2048];
					while(true) {
						int length = intpuStream.read(buffer);
						if(length < 0) {
							break;
						}
						zos.write(buffer, 0, length);
					}
					zos.closeEntry();
				}
			}
		}
		if(count != total) {
			long progressInt = (long)((count *1.0 / total) * 100);

			progressService.updateProgress(pro,progressInt);
			//pro.setProgress((long)((count *1.0 / total) * 100));
		}
	}
	
	private int writeCutImageToOutputZip(LabelTaskItem item, ZipOutputStream zos,double maxscore,double minscore) throws Exception {
		int count = 0;
		String jsonLabelInfo = item.getLabel_info();
		if(Strings.isBlank(jsonLabelInfo)) {
			logger.info("jsonLabelInfo is null. jsonLabelInfo=" + jsonLabelInfo);
			return count;
		}
		ArrayList<Map<String,Object>> labelList = gson.fromJson(jsonLabelInfo, new TypeToken<ArrayList<Map<String,Object>>>() {
			private static final long serialVersionUID = 1L;}.getType());
		if(labelList.isEmpty()) {
			logger.info("jsonLabelInfo is empty. jsonLabelInfo=" + jsonLabelInfo);
			return count;
		}

		BufferedImage bufferImage = ImageIO.read(giteaObjectFileService.getFile(item.getPic_image_field()));
		if(bufferImage == null) {
			logger.info("image is null. path=" + item.getPic_image_field());
			return count;
		}

		String imageName = item.getPic_image_field();
		imageName = imageName.substring(imageName.lastIndexOf("/") + 1);
		imageName = imageName.substring(0,imageName.length() - 4);
		for(Map<String,Object> label : labelList) {

			Object idObj = label.get("id");
			if(idObj == null) {
				continue;
			}
			String id = idObj.toString();
			
			double score = getScore(label);
			
			if(score > maxscore || score < minscore) {
				continue;
			}

			List<Object> boxList = (List<Object>)label.get("box");
			if(boxList != null) {//矩形标注
				int xmin = CocoAnnotationsUtil.getIntStr(String.valueOf(boxList.get(0)));
				int ymin = CocoAnnotationsUtil.getIntStr(String.valueOf(boxList.get(1)));
				int xmax = CocoAnnotationsUtil.getIntStr(String.valueOf(boxList.get(2)));
				int ymax = CocoAnnotationsUtil.getIntStr(String.valueOf(boxList.get(3)));
				if(xmax-xmin <=0 || ymax - ymin <=0) {
					continue;
				}
				if(xmin < 0) {
					xmin = 0;
				}
				if(xmax <= 0) {
					xmax = 1;
				}
				if(xmax >= bufferImage.getWidth()) {
					xmax =  bufferImage.getWidth();
				}
				if(xmin >= bufferImage.getWidth()) {
					xmin = bufferImage.getWidth() - 1;
				}
				if(ymax >= bufferImage.getHeight()) {
					ymax = bufferImage.getHeight();
				}
				if(ymin >= bufferImage.getHeight()) {
					ymin = bufferImage.getHeight() - 1;
				}

				try {
					BufferedImage subImage = bufferImage.getSubimage(xmin, ymin, xmax-xmin, ymax-ymin);
					String name = imageName + "_" + id + ".jpg";
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					ImageIO.write(subImage, "jpg", out);
					byte data[] = out.toByteArray();
					zos.putNextEntry(new ZipEntry("img/" + name));
					zos.write(data, 0, data.length);
					zos.closeEntry();
				} catch (Exception e) {
					logger.info(e.getMessage());
				}
			}
		}
		return count;

	}



	private double getScore(Map<String, Object> label) {
		Object scoreObj = label.get("score");
		if(scoreObj != null && scoreObj.toString().length() > 0) {
			return Double.parseDouble(scoreObj.toString());
		}
		return 1.0;
	}



	private void writeJson(Map<String,Object> jsonMap,String entryName, ZipOutputStream zos) throws IOException {
		String json = gson.toJson(jsonMap);
		byte[] xmlBytes = json.getBytes("utf-8");
		zos.putNextEntry(new ZipEntry(entryName));
		int len = xmlBytes.length;
		zos.write(xmlBytes, 0, len);
		zos.closeEntry();
	}

	private void writeXml(LabelTaskItem item,ZipOutputStream zos,Map<String,Object> pictureInfo,Map<String,Object> typeOrColorMapName) throws IOException {
		Document doc = vocUtil.getXmlDocument(item,pictureInfo,typeOrColorMapName);
		if(doc == null) {
			return;
		}
		String fileName = LabelInfoUtil.getRealPath(item.getPic_image_field());
		fileName = fileName.substring(0,fileName.lastIndexOf(".")) +  ".xml";
		String entryName = fileName;
		if(!entryName.startsWith("xml/")) {
			entryName = "xml/" + entryName;
		}
		StringWriter strWriter = new StringWriter();
		OutputFormat format = new OutputFormat("\t", true);
		format.setTrimText(true);//去掉原来的空白(\t和换行和空格)！

		XMLWriter writer = new XMLWriter(strWriter, format);
		// 把document对象写到out流中。
		writer.write(doc);
		byte[] xmlBytes = strWriter.toString().getBytes("utf-8");
		logger.info("entryName=" + entryName);
		zos.putNextEntry(new ZipEntry(entryName));
		int len = xmlBytes.length;
		zos.write(xmlBytes, 0, len);
		zos.closeEntry();
	}

	
	
	public static void main(String[] args) {
		
		//System.out.println(LabelInfoUtil.getRealPath("/minio/opendata/attachments/2/9/29910202-d4e9-4d6b-a6c6-a92d0b81ff1229910202-d4e9-4d6b-a6c6-a92d0b81ff12/img/000048_voc2007.jpg"));
		
		//System.out.println(LabelInfoUtil.getRealPath("/minio/opendata/attachments/2/9/img/000048_voc2007.jpg"));
		
		for(int i = 0; i < 100;i++) {
			System.out.println("drop table if exists tasks_labeltaskitem_" + i + ";");
		}
		
	}
	
}
