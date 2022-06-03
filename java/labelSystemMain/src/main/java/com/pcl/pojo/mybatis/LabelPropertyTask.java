package com.pcl.pojo.mybatis;

public class LabelPropertyTask {

		private String id;
		
		private String task_name;
		
		private String task_type;
		
		private String task_desc;
		
		private String propertyJson;
	
		private int user_id;
		
		private String assign_user;
		
		private int assign_user_id;
		
		private int task_status;
		
		private String task_add_time;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getPropertyJson() {
			return propertyJson;
		}

		public void setPropertyJson(String propertyJson) {
			this.propertyJson = propertyJson;
		}

		public int getUser_id() {
			return user_id;
		}

		public void setUser_id(int user_id) {
			this.user_id = user_id;
		}

		public int getAssign_user_id() {
			return assign_user_id;
		}

		public void setAssign_user_id(int assign_user_id) {
			this.assign_user_id = assign_user_id;
		}

		public String getTask_add_time() {
			return task_add_time;
		}

		public void setTask_add_time(String task_add_time) {
			this.task_add_time = task_add_time;
		}

		public String getTask_name() {
			return task_name;
		}

		public void setTask_name(String task_name) {
			this.task_name = task_name;
		}

		public String getTask_type() {
			return task_type;
		}

		public void setTask_type(String task_type) {
			this.task_type = task_type;
		}

		public String getTask_desc() {
			return task_desc;
		}

		public void setTask_desc(String task_desc) {
			this.task_desc = task_desc;
		}

		public String getAssign_user() {
			return assign_user;
		}

		public void setAssign_user(String assign_user) {
			this.assign_user = assign_user;
		}

		public int getTask_status() {
			return task_status;
		}

		public void setTask_status(int task_status) {
			this.task_status = task_status;
		}
}
