package com.pcl.gitea.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GiteaPgDbOperService {

	@Value("${pg.url:aaa}")
	private String url;
	//192.168.62.129:5432
	
	@Value("${pg.username:aaa}")
    private String username;
    
	@Value("${pg.password:aaa}")
    private String password;
    
    private Connection connection = null;

    public Connection getConn() {
        try {
            Class.forName("org.postgresql.Driver").newInstance();
            connection = DriverManager.getConnection(url, username, password);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }


    public List<Map<String,Object>>  query(String sql) {
    	List<Map<String,Object>> result = new ArrayList<>();
        try(Connection conn = getConn();
        		PreparedStatement pStatement = conn.prepareStatement(sql);
        		ResultSet rs = pStatement.executeQuery()) {
           
            int columnCount = rs.getMetaData().getColumnCount();
            while(rs.next()) {
            	Map<String,Object> record = new HashMap<>();
            	for(int i = 1; i <= columnCount; i++) {
            		Object value = rs.getObject(i);
            		record.put(rs.getMetaData().getColumnName(i).toLowerCase(), value);
            	}
            	result.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        return result;
    }
    
    
    public String getUserName(long uid) {
    	String sql = "SELECT name FROM public.user where id=" + uid;
    	try(Connection conn = getConn();
    			PreparedStatement pStatement = conn.prepareStatement(sql);
    			ResultSet rs = pStatement.executeQuery()) {
    		if(rs.next()) {
    			return rs.getString(1);
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    	} 
    	return String.valueOf(uid);
    }

	
}
