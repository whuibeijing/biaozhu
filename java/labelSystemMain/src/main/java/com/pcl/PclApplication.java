package com.pcl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ResourceUtils;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class PclApplication {

	private static Logger logger = LoggerFactory.getLogger(PclApplication.class);
	
	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		System.setProperty("sun.jnu.encoding","utf-8");
		boolean isExistSelfProperties = false;
		String fileName ="application-runtime.properties";
		if(args != null && args.length > 0 && args[0] != null) {
			fileName = args[0];
		}
		try(FileInputStream in = new FileInputStream(ResourceUtils.getFile(fileName).getAbsolutePath())){
			Properties properties = new Properties();
			properties.load(in);
			isExistSelfProperties  = true;
			
			logger.info("run app used application-runtime.properties, version=plabelv3.0,date=2021-12-29");
			
			SpringApplication app = new SpringApplication(PclApplication.class);
			app.setDefaultProperties(properties);
			app.run(args);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(!isExistSelfProperties) {
			SpringApplication.run(PclApplication.class, args);
		}
	}
	
}
