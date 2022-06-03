package com.pcl.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.pcl.dao.ClassManageDao;
import com.pcl.pojo.mybatis.ClassManage;
import com.pcl.pojo.mybatis.LabelTaskItem;
import com.pcl.service.ObjectFileService;

/**
 *   将标注信息写成coco格式
 * @author 邹安平
 *
 */
@Service
public class CocoAnnotationsUtil {

	@Autowired
	private ObjectFileService fileService;

	@Autowired
	private ClassManageDao classManageDao;

	//private Gson gson = new Gson();

	//instances_train2017.json
	public void getCocoJson(List<LabelTaskItem> itemList,Map<String,Object> lastCocoLabel){

		List<ClassManage> allClass = classManageDao.queryAll();
		int maxClassId = getMaxClassId(allClass);
		Map<String,Integer> classMapId = getClassMapId(allClass);
		Map<String,String> classMapSuperClass = getClassMapSuperClass(allClass);

		
		lastCocoLabel.put("type","instances");
		
		List<Map<String,Object>> imageList = new ArrayList<>();

		Map<String,Map<String,Object>> categories = new HashMap<>();
		if(lastCocoLabel.get("categories") != null) {
			List<Map<String,Object>> lastCategories = (List<Map<String,Object>>)lastCocoLabel.get("categories");
			for(Map<String,Object> tmpCategories : lastCategories) {
				categories.put(tmpCategories.get("name").toString(), tmpCategories);
				classMapId.put(tmpCategories.get("name").toString(), (Integer)tmpCategories.get("id"));
			}
		}
		int id = 0;
		if(lastCocoLabel.get("images") != null) {
			List<Map<String,Object>> lastImageList = (List<Map<String,Object>>)lastCocoLabel.get("images");
			id = lastImageList.size();
		}
		
		List<Map<String,Object>> annotationList = new ArrayList<>();

		long time = System.nanoTime();
		time = time % 1000;
		time = time * 100000;
		
		int annid = 1;
		for(LabelTaskItem item : itemList) {
			id ++;
			String labelInfo = item.getLabel_info();
			if(Strings.isBlank(labelInfo)) {
				continue;
			}
			ArrayList<Map<String,Object>> labelList = JsonUtil.getLabelList(labelInfo);
			if(labelList.isEmpty()) {
				continue;
			}

			long imageId = getId(time, id);
			Map<String, Object> imageMap = getImageFileInfo(imageId, item);
			imageList.add(imageMap);

			for(Map<String,Object> label : labelList) {
				Map<String,Object> annotation = new HashMap<>();
				annotation.put("id", annid);
				annotation.put("image_id", imageId);

				String className = getClassName(label);
				if(Strings.isBlank(className)) {
					continue;
				}
				//String className = getClassName(label.get("class_name").toString());
				String superClassName = getStrOrNull(label.remove("super_class"));

				if(!classMapId.containsKey(className)) {
					maxClassId++;
					newACategory(maxClassId, classMapId, classMapSuperClass, className, superClassName);
				}
				int categoryId = classMapId.get(className);

				if(!categories.containsKey(className)) {
					Map<String,Object> categoryMap = new HashMap<>();
					categoryMap.put("name", className);
					categoryMap.put("id", classMapId.get(className));
					categoryMap.put("supercategory", classMapSuperClass.get(className));

					categories.put(className, categoryMap);
				}

				annotation.put("category_id", categoryId);
				List<Object> boxList = (List<Object>)label.remove("box");
				if(boxList != null) {//矩形标注
					int xmin = getIntStr(String.valueOf(boxList.get(0)));
					int ymin = getIntStr(String.valueOf(boxList.get(1)));
					int xmax = getIntStr(String.valueOf(boxList.get(2)));
					int ymax = getIntStr(String.valueOf(boxList.get(3)));

					List<Integer> bbox = new ArrayList<>();
					bbox.add(xmin);
					bbox.add(ymin);
					int o_width = Math.abs(xmax - xmin);
					int o_height = Math.abs(ymax - ymin);
					bbox.add(o_width);
					bbox.add(o_height);

					annotation.put("bbox", bbox);
					annotation.put("area", o_width * o_height);
				}

				List<Object> maskList = (List<Object>)label.remove("mask");
				if(maskList == null) {
					maskList = (List<Object>)label.remove("segmentation");
				}
				if(maskList != null) {
					//多边形标注
					annotation.put("segmentation", maskList);
					annotation.put("area", caculateArea(maskList));
				}else {
					annotation.put("segmentation", new ArrayList<>());
				}

				//点标注，只支持一个点
				List<Object> keypointsList = (List<Object>)label.remove("keypoints");
				if(keypointsList != null) {
					annotation.put("keypoints", keypointsList);
					annotation.put("num_keypoints", 1);
				}
				
				List<Object> lineList = (List<Object>)label.remove("line");
				if(lineList != null) {
					annotation.put("line", lineList);
					annotation.put("num_linepoints", lineList.size() / 2);
				}

				annotation.put("iscrowd", 0);
				annotation.put("ignore", 0);

				//addEleToMap(label, annotation);

				annotationList.add(annotation);
				annid++;
			}
		}



		List<Map<String,Object>> categoriesList = new ArrayList<>();
		for(Entry<String,Map<String,Object>> entry : categories.entrySet()) {
			categoriesList.add(entry.getValue());
		}
		if(lastCocoLabel.get("images") != null) {
			List<Map<String,Object>> lastImageList = (List<Map<String,Object>>)lastCocoLabel.get("images");
			imageList.addAll(lastImageList);
		}
	
		if(lastCocoLabel.get("annotations") != null) {
			List<Map<String,Object>> lastAnnotationList = (List<Map<String,Object>>)lastCocoLabel.get("annotations");
			annotationList.addAll(lastAnnotationList);
		}
		
		lastCocoLabel.put("images", imageList);
		lastCocoLabel.put("categories", categoriesList);
		lastCocoLabel.put("annotations", annotationList);

	}

	public  Map<String,Object> getTmpCocoJson(List<LabelTaskItem> itemList){

		List<ClassManage> allClass = new ArrayList<>();
		int maxClassId = -1;
		Map<String,Integer> classMapId = getClassMapId(allClass);
		Map<String,String> classMapSuperClass = getClassMapSuperClass(allClass);

		Map<String,Object> re = new HashMap<>();
		re.put("type","instances");
		List<Map<String,Object>> imageList = new ArrayList<>();

		Map<String,Map<String,Object>> categories = new HashMap<>();
		List<Map<String,Object>> annotationList = new ArrayList<>();

		long time = System.nanoTime();
		time = time % 1000;
		time = time * 100000;
		int id = 0;
		int annid = 1;
		for(LabelTaskItem item : itemList) {
			id ++;
			String labelInfo = item.getLabel_info();
			if(Strings.isBlank(labelInfo)) {
				continue;
			}
			ArrayList<Map<String,Object>> labelList = JsonUtil.getLabelList(labelInfo);
			if(labelList.isEmpty()) {
				continue;
			}

			long imageId = getId(time, id);
			Map<String, Object> imageMap = getImageFileInfo(imageId, item);
			imageList.add(imageMap);

			for(Map<String,Object> label : labelList) {
				Map<String,Object> annotation = new HashMap<>();
				annotation.put("id", annid);
				annotation.put("image_id", imageId);


				String className = getClassName(label);
				if(Strings.isBlank(className)) {
					continue;
				}
				//String className = getClassName(label.get("class_name").toString());
				String superClassName = getStrOrNull(label.remove("super_class"));

				if(!classMapId.containsKey(className)) {
					maxClassId++;
					newACategory(maxClassId, classMapId, classMapSuperClass, className, superClassName);
				}
				int categoryId = classMapId.get(className);

				if(!categories.containsKey(className)) {
					Map<String,Object> categoryMap = new HashMap<>();
					categoryMap.put("name", className);
					categoryMap.put("id", classMapId.get(className));
					categoryMap.put("supercategory", "none");

					categories.put(className, categoryMap);
				}

				annotation.put("category_id", categoryId);

				List<Object> boxList = (List<Object>)label.remove("box");
				if(boxList != null) {//矩形标注
					int xmin = getIntStr(String.valueOf(boxList.get(0)));
					int ymin = getIntStr(String.valueOf(boxList.get(1)));
					int xmax = getIntStr(String.valueOf(boxList.get(2)));
					int ymax = getIntStr(String.valueOf(boxList.get(3)));

					List<Integer> bbox = new ArrayList<>();
					bbox.add(xmin);
					bbox.add(ymin);
					int o_width = Math.abs(xmax - xmin);
					int o_height = Math.abs(ymax - ymin);
					bbox.add(o_width);
					bbox.add(o_height);

					annotation.put("bbox", bbox);
					annotation.put("area", o_width * o_height);
				}

				List<Object> maskList = (List<Object>)label.remove("mask");
				if(maskList == null) {
					maskList = (List<Object>)label.remove("segmentation");
				}
				if(maskList != null) {
					//多边形标注
					annotation.put("segmentation", Arrays.asList(maskList));
					annotation.put("area", caculateArea(maskList));
				}else {
					annotation.put("segmentation", new ArrayList<>());
				}

				//点标注，只支持一个点
				List<Object> keypointsList = (List<Object>)label.remove("keypoints");
				if(keypointsList != null) {
					annotation.put("keypoints", keypointsList);
					annotation.put("num_keypoints", 1);
				}
				
				List<Object> lineList = (List<Object>)label.remove("line");
				if(lineList != null) {
					annotation.put("line", lineList);
					annotation.put("num_linepoints", lineList.size() / 2);
				}

				annotation.put("iscrowd", 0);
				annotation.put("ignore", 0);

				//addEleToMap(label, annotation);

				annotationList.add(annotation);
				annid++;
			}
		}

		List<Map<String,Object>> categoriesList = new ArrayList<>();
		for(Entry<String,Map<String,Object>> entry : categories.entrySet()) {
			categoriesList.add(entry.getValue());
		}

		re.put("images", imageList);
		re.put("categories", categoriesList);
		re.put("annotations", annotationList);

		return re;
	}

	public static void addEleToMap(Map<String,Object> label,Map<String,Object> annotation) {
		for(Entry<String,Object> tmp : label.entrySet()) {
			String key = tmp.getKey();
			if(key.equals("other")) {
				Object other = tmp.getValue();
				if(!isEmpty(other) && other instanceof Map) {
					Map<String,Object> otherMap = (Map<String,Object>)other;
					if(otherMap.get("region_attributes") != null && otherMap.get("region_attributes") instanceof Map) {
						Map<String,Object> region_attributesMap = (Map<String,Object>)otherMap.get("region_attributes");
						for(Entry<String,Object> tmpEle : region_attributesMap.entrySet()) {
							annotation.put(key, tmp.getValue());
						}
					}
				}else if(!isEmpty(other)) {
					annotation.put(key, tmp.getValue());
				}
			}else {
				if(!isEmpty(tmp.getValue())) {
					annotation.put(key, tmp.getValue());
				}
			}
		}
	}

	public static boolean isEmpty(Object obj) {
		if(obj == null) {
			return true;
		}
		if(obj.toString().isEmpty()) {
			return true;
		}
		return false;
	}

	public static String getClassName(Map<String, Object> label) {
		Object obj = label.remove("class_name");
		if(obj != null && !Strings.isEmpty(obj.toString())) {
			return obj.toString();
		}else {
			Object other = label.get("other");
			if(other != null) {
				Map<String,Object> otherMap = (Map<String,Object>)other;
				Object tmpObj = otherMap.remove("type");
				if(tmpObj != null && !Strings.isEmpty(tmpObj.toString())) {
					return tmpObj.toString();
				}
			}
		}
		return null;
	}


	public static String getStrValue(Map<String, Object> label,String key) {
		Object obj = label.get(key);
		if(obj != null && !Strings.isEmpty(obj.toString())) {
			return obj.toString();
		}else {
			Object other = label.get("other");
			if(other != null) {
				Map<String,Object> otherMap = (Map<String,Object>)other;
				Object region_attributesObj = otherMap.get("region_attributes");
				if(region_attributesObj != null && region_attributesObj instanceof Map) {
					Map<String,Object> region_attributesMap = (Map<String,Object>)region_attributesObj;
					obj = region_attributesMap.get(key);
					if(obj != null && !Strings.isEmpty(obj.toString())) {
						return obj.toString();
					}
				}
			}
		}
		return null;
	}


	public static Object getObjValue(Map<String, Object> label,String key) {
		Object obj = label.get(key);
		if(obj != null) {
			return obj;
		}else {
			Object other = label.get("other");
			if(other != null) {
				Map<String,Object> otherMap = (Map<String,Object>)other;
				Object region_attributesObj = otherMap.get("region_attributes");
				if(region_attributesObj != null && region_attributesObj instanceof Map) {
					Map<String,Object> region_attributesMap = (Map<String,Object>)region_attributesObj;
					return region_attributesMap.get(key);
				}
			}
		}
		return null;
	}

	public static float caculateArea(List<Object> maskList) {
		Point vertex[] = new Point[maskList.size() / 2];
		for(int i = 0; i < maskList.size(); i += 2) {
			Point tmpPoint = new Point();
			tmpPoint.x = getIntStr(String.valueOf(maskList.get(i)));
			tmpPoint.y = getIntStr(String.valueOf(maskList.get(i + 1)));
			vertex[i/2] = tmpPoint;
		}

		return caculate(vertex, vertex.length);
	}

	public static int getIntStr(String doubleStr) {
		int index = doubleStr.indexOf(".");
		if(index != -1) {
			doubleStr = doubleStr.substring(0,index);
		}
		return Integer.parseInt(doubleStr);
	}

	public static float caculate(Point vertex[],int pointNum){
		int i=0;
		float temp=0;
		for(;i<pointNum-1;i++)
		{
			temp+=(vertex[i].x-vertex[i+1].x)*(vertex[i].y+vertex[i+1].y);
		}
		temp+=(vertex[i].x-vertex[0].x)*(vertex[i].y+vertex[0].y);
		return temp/2;
	}



	private void newACategory(int maxClassId, Map<String, Integer> classMapId, Map<String, String> classMapSuperClass,
			String className, String superClassName) {

		classMapId.put(className, maxClassId);
		if(superClassName != null) {
			classMapSuperClass.put(className, superClassName);
		}else {
			classMapSuperClass.put(className, className);
		}
		//		ClassManage classManage = new ClassManage();
		//		classManage.setId(maxClassId);
		//		classManage.setClass_name(className);
		//		classManage.setSuper_class_name(superClassName);
		//		classManageDao.addClassManage(classManage);

	}

	private String getStrOrNull(Object object) {
		if(object != null) {
			return object.toString();
		}
		return null;
	}

	private int getMaxClassId(List<ClassManage> allClass) {
		int max = -1;
		for(ClassManage clazz : allClass) {
			if(clazz.getId() > max) {
				max = clazz.getId();
			}
		}
		return max;
	}

	private Map<String, String> getClassMapSuperClass(List<ClassManage> allClass) {
		Map<String,String> re = new HashMap<>();
		for(ClassManage clazz : allClass) {
			if(!Strings.isEmpty(clazz.getSuper_class_name())) {
				re.put(clazz.getClass_name(), clazz.getSuper_class_name());
			}else {
				re.put(clazz.getClass_name(), clazz.getClass_name());
			}
		}
		return re;
	}

	private Map<String, Integer> getClassMapId(List<ClassManage> allClass) {
		Map<String,Integer> re = new HashMap<>();
		for(ClassManage clazz : allClass) {
			re.put(clazz.getClass_name(), clazz.getId());
		}
		return re;
	}

	private  Map<String, Object> getImageFileInfo(long imageId, LabelTaskItem item) {

		String fileName = LabelInfoUtil.getRealPath(item.getPic_image_field());
		
		Map<String,Object> imageMap = new HashMap<>();

		String widthHeigth = item.getPic_object_name();
		if(widthHeigth == null || widthHeigth.indexOf(",") == -1) {
			widthHeigth = fileService.getImageWidthHeight(item.getPic_image_field());
		}
		if(!Strings.isEmpty(widthHeigth)) {
			String tmp[] = widthHeigth.split(",");
			imageMap.put("width", Integer.parseInt(tmp[0]));
			imageMap.put("height", Integer.parseInt(tmp[1]));
		}else {
			imageMap.put("width", 0);
			imageMap.put("height", 0);
		}
		imageMap.put("id", imageId);
		imageMap.put("file_name", fileName);
		return imageMap;
	}


	private  long getId(long time, int id) {
		return id;
	}

	static class Point{
		public int x;

		public int y;

	}


	public boolean isCocoFormat(Map<String,Object> conMap) {

		if(conMap.get("images") != null && conMap.get("annotations") != null && conMap.get("categories") != null) {
			return true;
		}
		return false;
	}


	public static List<LabelTaskItem> readCocoJson(Map<String,Object> conMap){
		List<LabelTaskItem> result = new ArrayList<>();

		Map<Integer,List<Map<String,Object>>> labelImageMap = new HashMap<>();
		List<Map<String,Object>> images = (List<Map<String,Object>>)conMap.get("images");
		Map<Integer,Map<String,Object>> imageMap = new HashMap<>();
		for(Map<String,Object> tmpImageMap : images) {
			int imageId = JsonUtil.getIntValue("id", tmpImageMap);
			imageMap.put(imageId, tmpImageMap);
			labelImageMap.put(imageId, new ArrayList<>());
		}



		List<Map<String,Object>> categories = (List<Map<String,Object>>)conMap.get("categories");
		Map<Integer,Map<String,Object>> categorieMap = new HashMap<>();
		for(Map<String,Object> tmpCategorieMap : categories) {
			int categorieId = JsonUtil.getIntValue("id", tmpCategorieMap);
			categorieMap.put(categorieId, tmpCategorieMap);
		}

		List<Map<String,Object>> annotations = (List<Map<String,Object>>)conMap.get("annotations");
		for(Map<String,Object> annoMap : annotations) {
			int imageId = JsonUtil.getIntValue("image_id", annoMap) ;

			Map<String,Object> image = imageMap.get(imageId);
			if(image != null && image.get("file_name") != null) {
				if(image.get("file_name").toString().endsWith("0001.jpg")) {
					System.out.println(image.get("file_name"));
				}
			}

			Map<String,Object> label = new HashMap<>();
			label.put("id", String.valueOf((JsonUtil.getIntValue("id", annoMap))));

			int categorieId = JsonUtil.getIntValue("category_id", annoMap);
			Map<String,Object> tmpCategorieMap = categorieMap.get(categorieId);
			if(tmpCategorieMap == null) {
				label.put("class_name", "none");
			}else {
				label.put("class_name", tmpCategorieMap.get("name"));
			}
			

			Object boxObj = annoMap.get("bbox");
			if(boxObj != null) {
				List<Object> boxList = (List<Object>)boxObj;
				double xmin = Double.parseDouble((String.valueOf(boxList.get(0))));
				double ymin =  Double.parseDouble((String.valueOf(boxList.get(1))));
				double o_width =  Double.parseDouble((String.valueOf(boxList.get(2))));
				double o_height =  Double.parseDouble((String.valueOf(boxList.get(3))));

				List<Double> bbox = new ArrayList<>();
				bbox.add(xmin);
				bbox.add(ymin);
				double xmax =xmin + o_width;
				double ymax = ymin + o_height;
				bbox.add(xmax);
				bbox.add(ymax);

				label.put("box", bbox);
			}
			Object segmentObj = annoMap.get("segmentation");
			if(segmentObj != null) {
				if(segmentObj instanceof List) {
					List<Object> segmentList = (List<Object>)segmentObj;

					for(Object obj : segmentList) {
						if(obj instanceof List) {
							label.put("mask", obj);//拿第一段
							break;
						}
					}
				}else {
					//TODO RLE格式（Run Length Encoding（行程长度压缩算法））
					/**
					 * 
					 * 
segmentation : 
{
    u'counts': [272, 2, 4, 4, 4, 4, 2, 9, 1, 2, 16, 43, 143, 24......], 
    u'size': [240, 320]
}
将图像中目标区域的像素值设定为1，背景设定为0，则形成一个张二值图，该二值图可以使用z字形按照位置进行
编码，例如：0011110011100000……
但是这样的形式太复杂了，可以采用统计有多少个0和1的形式进行局部压缩，因此上面的RLE编码形式为：
2-0-4-1-2-0-3-1-5-0……（表示有2个0,4个1,2个0,3个1,5个0）

对于向量 M=[0 0 1 1 1 0 1]，RLE 计算结果为 [2 3 1 1]；对于向量 M=[1 1 1 1 1 1 0]，RLE 计算结果为 [0 6 1].注：索引从零开始.

					 * 
					 * 
					 */
				}

			}

			Object keypointObj = annoMap.get("keypoints");
			if(keypointObj != null) {
				label.put("keypoints", keypointObj);
			}
			
			Object lineObj = annoMap.get("line");
			if(lineObj != null) {
				label.put("line", lineObj);
			}


			labelImageMap.get(imageId).add(label);
		}

		for(Entry<Integer,List<Map<String,Object>>> entry : labelImageMap.entrySet()) {

			LabelTaskItem item = new LabelTaskItem();
			item.setItem_add_time(TimeUtil.getCurrentTimeStr());

			Map<String,Object> image = imageMap.get(entry.getKey()); 
			int width = JsonUtil.getIntValue("width", image);
			int height = JsonUtil.getIntValue("height", image);
			item.setPic_object_name(width + "," + height);

			item.setLabel_info(JsonUtil.toJson(entry.getValue()));
			item.setPic_image_field(image.get("file_name").toString());
			result.add(item);
		}

		return result;
	}
	/**
	private static String getFileNameNoPostfix(String filePath) {
		int index = filePath.lastIndexOf("/");
		if(index != -1) {
			String tmpName = filePath.substring(index + 1);
			int dotIndex = tmpName.lastIndexOf(".");
			if(dotIndex != -1) {
				return tmpName.substring(0,dotIndex);
			}
			return tmpName;
		}else {
			int dotIndex = filePath.lastIndexOf(".");
			if(dotIndex != -1) {
				return filePath.substring(0,dotIndex);
			}

			return filePath;
		}
	}
	
	private static void addToResult(LabelTaskItem xmlItem,String returnKey,Map<String, List<LabelTaskItem>> result) {
		List<LabelTaskItem> itemList = result.get(returnKey);
		if(itemList == null) {
			itemList = new ArrayList<>();
			result.put(returnKey, itemList);
		}
		itemList.add(xmlItem);
	}
	
	
	private static LabelTaskItem find(List<LabelTaskItem> tmpList, String fileName) {
		int max = 1;
		String tmpFileNames[] =  fileName.split("/");
		LabelTaskItem result = tmpList.get(0);
		System.out.println("fileName=" + fileName);
		for(LabelTaskItem item : tmpList) {
			if(item.getPic_image_field() == null) {
				continue;
			}
			System.out.println("item.getPic_image_field()=" + item.getPic_image_field());
			String tmp[] = item.getPic_image_field().split("/");
			int tmpMax = 0;
			for(int index = 1; ;index++) {
				if((tmp.length - index) >= 0 && (tmpFileNames.length - index) >= 0) {
					if(isNameEqual(tmp[tmp.length - index], tmpFileNames[tmpFileNames.length - index])) {
						tmpMax++;
					}else {
						break;
					}
				}else {
					break;
				}
			}
			if(tmpMax > max) {
				System.out.println("tmpMax=" + tmpMax);
				max = tmpMax;
				result = item;
			}
		}
		System.out.println("fileName=" + fileName + "  findResult=" + result.getPic_image_field());
		return result;
	}

	private static boolean isNameEqual(String string1, String string2) {
		if(getFileNameNoPostfix(string1).equals(getFileNameNoPostfix(string2))) {
			return true;
		}
		return false;
	}
	
	public static void main(String[] args) {
		StringBuilder strBuild = new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\avi\\instances_train2017.json")))){
			String line = null;
			while(true) {
				line = reader.readLine();
				if(line == null) {
					break;
				}
				strBuild.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String,Object> map = JsonUtil.getMap(strBuild.toString());


		List<LabelTaskItem> re = readCocoJson(map);
		Map<String, List<LabelTaskItem>> result = new HashMap<>();
		for(LabelTaskItem tmp : re) {
			//System.out.println(getFileNameNoPostfix(tmp.getPic_image_field()));
			//System.out.println(tmp.getPic_image_field());
			if(getFileNameNoPostfix(tmp.getPic_image_field()).equals("0001")) {
				System.out.println("001=" + tmp.getPic_image_field());
			}
			addToResult(tmp, getFileNameNoPostfix(tmp.getPic_image_field()), result);
		}
		
		LabelTaskItem tmp = find(result.get("0001"), "aaabb/aaaa/OTB50/Car4/img/0001.jpg");
		
	}
	*/
}
