package com.pcl.service.update;

import java.sql.Connection;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

/**
 * 	脚本自动升级服务
 * @author 邹安平
 *
 */
@Service
public class DbScriptUpdate {

	private static Logger logger = LoggerFactory.getLogger(DbScriptUpdate.class);
	
	@Value("${init.sqlfile:null}")
	private String initSqlFile; // 取值为规划桶所在的区域
	
	@Autowired
    private DataSource datasource;
	
	@PostConstruct
	public void updateDb() {
		logger.info("start to update script.");
		try(Connection conn = datasource.getConnection();){
			conn.createStatement().execute("alter table regist_auto_model modify id int;");
		}catch (Exception e) {
			e.printStackTrace();
			logger.info("error to exe to sql.alter table regist_auto_model modify id int");
		}
		if("null".equals(initSqlFile)) {
			logger.info("not need to exec.sql");
			return;
		}
		ClassPathResource resource = new ClassPathResource(initSqlFile);
		if(!resource.exists()) {
			logger.info("not exist " + initSqlFile + " file.");
			return;
		}else {
			logger.info("start to exec " + initSqlFile + " file.");
		}
		try(Connection conn = datasource.getConnection();){
			
			
			ScriptUtils.executeSqlScript(conn, new EncodedResource(resource,"utf-8"), true, true, 
					ScriptUtils.DEFAULT_COMMENT_PREFIX, 
					ScriptUtils.DEFAULT_STATEMENT_SEPARATOR, 
					ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER, 
					ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
			
			logger.info("success to exe script.");
		}catch (Exception e) {
			e.printStackTrace();
			logger.info("error to exe to sql.");
		}
		
	}
	
}
