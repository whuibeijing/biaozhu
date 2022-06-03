package com.pcl.gitea.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.pcl.exception.LabelSystemException;
import com.pcl.gitea.redis.RedisUtil;
import com.pcl.gitea.service.GiteaDataSetService;
import com.pcl.gitea.service.GiteaLabelDataExportService;
import com.pcl.gitea.service.GiteaObjectFileService;
import com.pcl.pojo.PageResult;
import com.pcl.pojo.Result;
import com.pcl.pojo.display.DisplayDataSet;
import io.swagger.annotations.ApiOperation;

@RestController
public class GiteaController {

	private static Logger logger = LoggerFactory.getLogger(GiteaController.class);

	@Autowired
	private HttpServletResponse response;

	@Autowired
	private GiteaDataSetService giteaDataSetService;

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private GiteaLabelDataExportService giteaLabelDataExportService;

	@Autowired
	private GiteaObjectFileService giteaObjectFileService;

	@Value("${gitea.cookie.name:i_like_openi}")
    private String cookieName;
	
	@CrossOrigin(origins = "*", maxAge=3600)
	@RequestMapping(method = RequestMethod.GET, value = "/getgiteaimage")  
	@ResponseBody  
	public void getGiteaImageObject(@RequestParam(value="filename") String filename) throws LabelSystemException {
		//checkValidAccess(request);

		//String objectName = GiteaConstant.OBJECTNAMEPREFIX + "/" + uuid.charAt(0) + "/" + uuid.charAt(1) + "/" + uuid + uuid + "/" + filename;
		logger.info("Get picture: " + filename);
		response.setContentType("image/jpg;charset=utf-8");
		try(InputStream inStream = giteaObjectFileService.getFile(filename);
				OutputStream outStream = response.getOutputStream()){
			byte[] buf = new byte[1024 * 16];
			int len = 0;
			while ((len = inStream.read(buf)) != -1){
				outStream.write(buf, 0, len);
			}
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}  

	/*
	 * 
	name: zouap uid:1    length=107
	stb=e 3f 3f 4 1 2 3f 3f 0 1 10 1 10 0 0 5b 3f 3f 0 3 6 73 74 72 69 6e 67 c 5 0 3 75 69 64 5 69 6e 74 36 34 4 2 0 2 6 73 74 72 69 6e 67 c 7 0 5 75 6e 61 6d 65 6 73 74 72 6
	9 6e 67 c 7 0 5 7a 6f 75 61 70 6 73 74 72 69 6e 67 c a 0 8 5f 6f 6c 64 5f 75 69 64 6 73 74 72 69 6e 67 c 3 0 1 31 

	name:Test_1 uid:2    length=108
	stb=e 3f 3f 4 1 2 3f 3f 0 1 10 1 10 0 0 5c 3f 3f 0 3 6 73 74 72 69 6e 67 c 7 0 5 75 6e 61 6d 65 6 73 74 72 69 6e 67 c 8 0 6 54 65 73 74 5f 31 6 73 74 72 69 6e 67 c a 0 8 
5f 6f 6c 64 5f 75 69 64 6 73 74 72 69 6e 67 c 3 0 1 32 6 73 74 72 69 6e 67 c 5 0 3 75 69 64 5 69 6e 74 36 34 4 2 0 4

	name:test_1234567890 uid:11  length=118
	stb=e 3f 3f 4 1 2 3f 3f 0 1 10 1 10 0 0 66 3f 3f 0 3 6 73 74 72 69 6e 67 c a 0 8 5f 6f 6c 64 5f 75 69 64 6 73 74 72 69 6e 67 c 4 0 2 31 31 6 73 74 72 69 6e 67 c 5 0 3 75 
69 64 5 69 6e 74 36 34 4 2 0 16 6 73 74 72 69 6e 67 c 7 0 5 75 6e 61 6d 65 6 73 74 72 69 6e 67 c 11 0 f 54 65 73 74 5f 31 32 33 34 35 36 37 38 39 30

	 */
	void checkValidAccess(HttpServletRequest request) throws LabelSystemException {
		Cookie[] cookies = request.getCookies();
		if(cookies != null){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals(cookieName)){
					logger.info("cookie i_like_gitea value=" + cookie.getValue());

					Object obj = redisUtil.get(cookie.getValue());
					if(obj == null) {
						logger.info("no session.i_like_gitea=" + cookie.getValue());
						throw new LabelSystemException("No right.");
					}
					//logger.info("obj class=" + obj.getClass().getName());
					//logger.info("redis session json value=" + obj);
					StringBuffer stb = new StringBuffer();
					byte[] objbyte = ((String)obj).getBytes();
					for(byte b : objbyte) {
						stb.append(Integer.toHexString(b)).append(" ");
					}
					//logger.info("sesson length = "  + obj.toString().length());
					//logger.info("stb=" + stb.toString());
				}
			}
		}
	}

	@CrossOrigin(origins = "*", maxAge=3600)
	@RequestMapping(method = RequestMethod.GET, value = "/getgiteatext")  
	public String getGiteaTextObject(@RequestParam(value="uuid") String uuid,@RequestParam(value="filename") String filename) throws LabelSystemException {
		//checkValidAccess(request);

		logger.info("Get text area: " + filename);
		StringBuilder re = new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(giteaObjectFileService.getFile(filename),"GBK"))){
			String line = null;
			while((line = reader.readLine()) != null) {
				re.append(line).append("\n\r");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//logger.info("re=" + re.toString());
		return re.toString();
	}  
	
	@CrossOrigin(origins = "*", maxAge=3600)
	@RequestMapping(method = RequestMethod.GET, value = "/getlabelinfo")  
	public String getlabelInfoFromDataSet(@RequestParam(value="uuid") String uuid) throws LabelSystemException {
		//checkValidAccess(request);

		logger.info("Get label info dataset: " + uuid);
		return giteaDataSetService.queryDataSetLabelInfo(uuid);

	}  


	@ApiOperation(value="查询所有的数据集任务", notes="返回所有的数据集任务")
	@RequestMapping(value="/gitea-dataset", method = RequestMethod.GET)
	public List<DisplayDataSet> queryDataSet(@RequestParam("repoId") String repoId,@RequestParam("dateset_type") String datesetType) throws LabelSystemException{

		String token = request.getHeader("authorization");
		if(token == null) {
			throw new LabelSystemException("user not login.");
		}
		logger.info("queryDataSet  token =" + token + " repoId=" + repoId + " datesetType=" + datesetType);

		return giteaDataSetService.queryAllDataSet(token,repoId,datesetType);
	}

	//type=1,不带图片，2带图片，3
	@ApiOperation(value="导出标注数据文件接口", notes="返回导出查询进度信息的id")
	@RequestMapping(value ="/gitea-label-task-export", method = RequestMethod.GET)
	public Result downFile(@RequestParam("label_task_id") String labelTaskId,
			@RequestParam("needPicture") int type,
			@RequestParam("exportFormat") int exportFormat,
			@RequestParam(value="maxscore",required=false, defaultValue="1.1")  double maxscore,
			@RequestParam(value="minscore",required=false, defaultValue="0.0")  double minscore) {
		Result result = new Result();

		logger.info("export file label_task_id= :" +  labelTaskId + "  needPicture=" + type);
		result.setCode(0);
		result.setMessage(giteaLabelDataExportService.downloadLabelTaskFile(labelTaskId,type,exportFormat,maxscore,minscore));

		return result;
	}
	
	@ApiOperation(value="分页查询所有待标注的图片任务", notes="分页查询所有待标注的图片任务")
	@RequestMapping(value="/gitea-dateset-item-page", method = RequestMethod.GET)
	public PageResult queryLabelItemPageByTaskId(@RequestParam("datasetId") String datasetId,@RequestParam("startPage") Integer startPage, @RequestParam("pageSize") Integer pageSize) throws LabelSystemException{
		//String token = request.getHeader("authorization");
		logger.info("queryLabelItemPageByTaskId  datasetId= "  + datasetId + " startPage =" + startPage + " pageSize=" + pageSize);
		//checkValidAccess(request);
//		if(token == null) {
//			throw new LabelSystemException("user not login.");
//		}
		
		return giteaDataSetService.queryDataSetPictureItemPage(datasetId, startPage, pageSize);
	}
	
	@ApiOperation(value="分页查询所有待标注的图片任务", notes="分页查询所有待标注的图片任务")
	@RequestMapping(value="/mergeObs", method = RequestMethod.GET)
	public void mergeObs(@RequestParam("bucket") String bucket,@RequestParam("uploadId") String uploadId,@RequestParam("objectName") String objectName) throws LabelSystemException{
		//String token = request.getHeader("authorization");
		logger.info("queryLabelItemPageByTaskId  bucket= "  + bucket + " uploadId =" + uploadId + " objectName=" + objectName);
		
		
		giteaObjectFileService.ListObsPart(bucket, objectName, uploadId);
		
	}
	
}
