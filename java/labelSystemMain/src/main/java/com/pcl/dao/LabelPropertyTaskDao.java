package com.pcl.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.pcl.pojo.mybatis.LabelPropertyTask;


@Mapper
public interface LabelPropertyTaskDao {

	/**
	 * 分页查询
	 * @param paramMap
	 * @return
	 */
	public List<LabelPropertyTask> queryLabelPropertyTask(Map<String,Object> paramMap);
	
	public List<LabelPropertyTask> queryAllLabelPropertyTask(Map<String,Object> paramMap);

	/**
	 * 分页查询返回的总记录数
	 * @param paramMap
	 * @return
	 */
	public int queryLabelPropertyTaskCount(Map<String,Object> paramMap);
	

	public int addLabelPropertyTask(LabelPropertyTask propertyTask);
	
	
	public LabelPropertyTask queryLabelPropertyTaskById(String id);
	
	
	public int deleteLabelPropertyTaskById(String id);
	
	public int updateLabelPropertyTask(Map<String,Object> paramMap);
	
}
