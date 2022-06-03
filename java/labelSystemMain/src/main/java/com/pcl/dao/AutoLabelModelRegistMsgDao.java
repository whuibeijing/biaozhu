package com.pcl.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.pcl.pojo.mybatis.AutoLabelModelRegistMsg;

@Mapper
public interface AutoLabelModelRegistMsgDao {

	public int saveAutoLabelModelRegistMsg(AutoLabelModelRegistMsg msg);
	
	public int updateAutoLabelModelRegistMsg(Map<String,Object> paramMap);
	
	public int deleteAutoLabelModelRegistMsg(int id);
	
	public List<AutoLabelModelRegistMsg> queryAll();
	
	public List<AutoLabelModelRegistMsg> queryAllOnLine();
	
	public AutoLabelModelRegistMsg queryById(int id);
	
	public List<AutoLabelModelRegistMsg> queryByName(String name);
}
