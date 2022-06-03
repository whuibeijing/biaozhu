package com.pcl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pcl.pojo.mybatis.LabelTaskItem;

public class GroundTruthRectUtil {

	
	public List<LabelTaskItem> readObjectTraceLabel(List<String> allFileList,InputStream in,String filePath){
		ArrayList<LabelTaskItem> result = new ArrayList<>();
		String tmp[] = filePath.split("/");
		String type = tmp[tmp.length - 2];
		
		List<String[]> boxList = new ArrayList<>();
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(in))){
			String line = null;
			while(true) {
				line = reader.readLine();
				if(line == null) {
					break;
				}
				line = line.replace('\t', ' ');
				String rect[] = line.split(",");
				if(rect.length != 4) {
					rect = line.split(" ");
				}
				if(rect.length != 4) {
					rect = line.split("	");
				}
				boxList.add(rect);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String tmpPath[] = filePath.split("/");
		String filePathPrefix = filePath.substring(0,filePath.length() - tmpPath[tmpPath.length - 1].length());
		filePathPrefix += "img/";
		
		List<String> typeFileList = new ArrayList<>();
		for(String path : allFileList) {
			if(path.startsWith(filePathPrefix) && !path.equals(filePathPrefix)) {
				typeFileList.add(path);
			}
		}
		
		if(typeFileList.size() == boxList.size()) {
			Collections.sort(typeFileList);
			
			for(int i = 0; i < boxList.size();i++) {
				String rect[] = boxList.get(i);
				LabelTaskItem item = new LabelTaskItem();
				item.setItem_add_time(TimeUtil.getCurrentTimeStr());

				Map<String,Object> label = new HashMap<>();
				label.put("id", "1");
				label.put("class_name", type);

				double xmin = Double.parseDouble(rect[0]);
				double ymin =  Double.parseDouble(rect[1]);
				double o_width =  Double.parseDouble(rect[2]);
				double o_height =  Double.parseDouble(rect[3]);

				List<Double> bbox = new ArrayList<>();
				bbox.add(xmin);
				bbox.add(ymin);
				double xmax =xmin + o_width;
				double ymax = ymin + o_height;
				bbox.add(xmax);
				bbox.add(ymax);

				label.put("box", bbox);

				item.setLabel_info(JsonUtil.toJson(Arrays.asList(label)));
				item.setPic_image_field(typeFileList.get(i));
				
				result.add(item);
			}

		}
		
		return result;
	}
	
	public static void main(String[] args) {
		String fileName = "D:\\2021文档\\问题\\groundtruth_rect.txt";
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))))){
			String line = null;
			while(true) {
				line = reader.readLine();
				if(line == null) {
					break;
				}
				line = line.replace('\t', ' ');
				String rect[] = line.split(",");
				if(rect.length != 4) {
					rect = line.split(" ");
				}
				System.out.println(rect.length);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
