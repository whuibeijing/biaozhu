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
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.util.ResourceUtils;

import springfox.documentation.swagger2.annotations.EnableSwagger2;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableSwagger2
public class LabelForDockerApplication {

	private static Logger logger = LoggerFactory.getLogger(LabelForDockerApplication.class);
	
	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		System.setProperty("sun.jnu.encoding","utf-8");
		boolean isExistSelfProperties = false;
		try(FileInputStream in = new FileInputStream(ResourceUtils.getFile("application-runtime.properties").getAbsolutePath())){
			Properties properties = new Properties();
			properties.load(in);
			isExistSelfProperties  = true;
			
			logger.info("run LabelForDockerApplication used application-runtime.properties, version=plabelv3.0,date=2021-09-01");
			
			SpringApplication app = new SpringApplication(LabelForDockerApplication.class);
			app.setDefaultProperties(properties);
			app.run(args);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(!isExistSelfProperties) {
			SpringApplication.run(LabelForDockerApplication.class, args);
		}
	}
	
}
