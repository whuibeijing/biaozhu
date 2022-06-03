package com.pcl.pojo;

public class AutoLabelModelRegistMsg {
	
	public final static int OBJECT_DETECTION_PICTURE = 1;
	
	public final static int OBJECT_DETECTION_DCM = 2;
	
	public final static int OBJECT_DETECTION_LARGE_PICTURE = 3;
	
	public final static int OBJECT_DETECTION_VIDEO = 4;
	
	public final static int TRACK_SINGLE = 5;
	
	public final static int TRACK_MULT = 6;
	
	public final static int TEXT_LABEL = 7;
	
	

	/**
	 * 模型名称
	 */
	private String name;
	
	/**
	 * 模型描述
	 */
	private String model_desc;
	
	/**
	 * 模型唯一标识
	 */
	private int id;
	
	/**  模型类型
	 * 1:picture，图像
	 * 2:dcm, CT影像
	 * 3:svs,tif等病理图像
	 * 4：视频
	 * 5：单目标跟踪
	 * 6：多目标跟踪
	 * 7:文本处理
	 */
	private int model_type;
	
	/**
	 * 模型识别的目标类型
	 * car, person等
	 */
	private String object_type;
	
	/**
	 * 自动标注模型代理服务接收URL
	 */
	private String agent_receive_task_url;
	
	//模型状态，是否在线
	private int status;
	
	private String exe_script;
	
	private String input_path;
	
	private String algRootPath;
	
	private String output_path;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getModel_desc() {
		return model_desc;
	}

	public void setModel_desc(String model_desc) {
		this.model_desc = model_desc;
	}


	public int getModel_type() {
		return model_type;
	}

	public void setModel_type(int model_type) {
		this.model_type = model_type;
	}

	public String getObject_type() {
		return object_type;
	}

	public void setObject_type(String object_type) {
		this.object_type = object_type;
	}



	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getAgent_receive_task_url() {
		return agent_receive_task_url;
	}

	public void setAgent_receive_task_url(String agent_receive_task_url) {
		this.agent_receive_task_url = agent_receive_task_url;
	}

	public String getExe_script() {
		return exe_script;
	}

	public void setExe_script(String exe_script) {
		this.exe_script = exe_script;
	}

	public String getInput_path() {
		return input_path;
	}

	public void setInput_path(String input_path) {
		this.input_path = input_path;
	}

	public String getAlgRootPath() {
		return algRootPath;
	}

	public void setAlgRootPath(String algRootPath) {
		this.algRootPath = algRootPath;
	}

	public String getOutput_path() {
		return output_path;
	}

	public void setOutput_path(String output_path) {
		this.output_path = output_path;
	}


	
}
