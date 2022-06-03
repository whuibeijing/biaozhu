/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 80018
Source Host           : localhost:3306
Source Database       : labelsystem

Target Server Type    : MYSQL
Target Server Version : 80018
File Encoding         : 65001

Date: 2020-01-23 15:39:57
*/
use labelsystem;
SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for alg_warehouse_alginstance
-- ----------------------------
CREATE TABLE IF NOT EXISTS `alg_warehouse_alginstance` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `alg_name` varchar(128) NOT NULL,
  `add_time` datetime(6) NOT NULL,
  `alg_type_name` varchar(128) NOT NULL,
  `alg_root_dir` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 ;

-- ----------------------------
-- Table structure for alg_warehouse_algmodel
-- ----------------------------
CREATE TABLE IF NOT EXISTS `alg_warehouse_algmodel` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `conf_path` varchar(255) DEFAULT NULL,
  `model_name` varchar(128) NOT NULL,
  `local_path` varchar(128) DEFAULT NULL,
  `model_url` varchar(128) DEFAULT NULL,
  `alg_instance_id` int(11) NOT NULL,
  `exec_script` varchar(512) DEFAULT NULL,
  `train_script` varchar(512) DEFAULT NULL,
  `model_type` INT(11) NULL,
  `auto_used` INT(11) NULL,
  `hand_label_used` INT(11) NULL,
  `type_list` VARCHAR(2000) NULL,
  `threshold` DOUBLE NULL,
  `a_picture_cost_time` INT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 ;


-- ----------------------------
-- Table structure for authtoken_token
-- ----------------------------
CREATE TABLE IF NOT EXISTS `authtoken_token` (
  `token` varchar(40) NOT NULL,
  `created` datetime(6) NOT NULL,
  `user_id` int(11) NOT NULL,
  `loginTime` BIGINT(20) NULL,
  PRIMARY KEY (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


-- ----------------------------
-- Table structure for samples_manager_sampleclass
-- ----------------------------
CREATE TABLE IF NOT EXISTS `class_manage` (
  `id` int(11) NOT NULL,
  `class_name` varchar(200) NOT NULL,
  `super_class_name` varchar(200)  NULL,
  `class_desc` varchar(1000)  NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;

-- ----------------------------
-- Table structure for tasks_labeltask
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tasks_labeltask` (
  `id` varchar(64) NOT NULL,
  `task_name` varchar(64) NOT NULL,
  `task_add_time` datetime(6) NOT NULL,
  `relate_task_id` varchar(128) DEFAULT NULL,
  `relate_task_name` varchar(400) DEFAULT NULL,
  `finished_picture` int(11) DEFAULT NULL,
  `total_picture` int(11) DEFAULT NULL,
  `task_type` int(11) DEFAULT NULL,
  `zip_object_name` varchar(255) DEFAULT NULL,
  `zip_bucket_name` varchar(255) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `assign_user_id` int(11) DEFAULT NULL,
  `relate_other_label_task` varchar(400) DEFAULT NULL,
  `task_flow_type` int(11) DEFAULT NULL,
  `task_label_type_info` VARCHAR(4000) DEFAULT NULL,
  `appid` VARCHAR(128) NULL,
  `user_info` VARCHAR(2000) DEFAULT NULL,
  `verify_user_id` INT(11) NULL,
  `task_status` INT(11) NULL,
  `task_status_desc` VARCHAR(400) NULL,
  `total_label` INT(11) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


-- ----------------------------
-- Table structure for tasks_labeltaskitem
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tasks_labeltaskitem` (
  `id` varchar(64) NOT NULL,
  `pic_url` varchar(256) DEFAULT NULL,
  `pic_object_name` varchar(128) DEFAULT NULL,
  `label_info` TEXT(65535) NULL,
  `label_task_id` varchar(64) NOT NULL,
  `item_add_time` datetime(6) NOT NULL,
  `pic_image_field` varchar(400) DEFAULT NULL,
  `label_status` int(11) NOT NULL,
  `display_order1` INT(11) NULL,
  `display_order2` INT(11) NULL,
  `verify_status` INT(11) NULL,
  `verify_desc` VARCHAR(400) NULL,
  `pic_real_name` VARCHAR(400) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


CREATE TABLE IF NOT EXISTS `tasks_labeldcmtaskitem` (
  `id` varchar(64) NOT NULL,
  `pic_url` varchar(256) DEFAULT NULL,
  `pic_object_name` varchar(128) DEFAULT NULL,
  `label_info` TEXT(65535) NULL,
  `label_task_id` varchar(64) NOT NULL,
  `item_add_time` datetime(6) NOT NULL,
  `pic_image_field` varchar(400) DEFAULT NULL,
  `label_status` int(11) NOT NULL,
  `display_order1` INT(11) NULL,
  `display_order2` INT(11) NULL,
  `verify_status` INT(11) NULL,
  `verify_desc` VARCHAR(400) NULL,
  `pic_real_name` VARCHAR(400) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


-- ----------------------------
-- Table structure for tasks_prepredictresult
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tasks_prepredictresult` (
  `id` varchar(64) NOT NULL,
  `pic_url` varchar(256) DEFAULT NULL,
  `pic_object_name` varchar(128) DEFAULT NULL,
  `label_info` TEXT(65535) NULL,
  `item_add_time` datetime(6) NOT NULL,
  `pre_predict_task_id` varchar(64) NOT NULL,
  `pic_image_field` varchar(400) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;

-- ----------------------------
-- Table structure for tasks_prepredicttasks
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tasks_prepredicttasks` (
  `id` varchar(64) NOT NULL,
  `task_name` varchar(400) NOT NULL,
  `zip_object_name` varchar(128)  NULL,
  `task_start_time` datetime(6) NOT NULL,
  `task_finish_time` datetime(6) DEFAULT NULL,
  `task_status` int(11) NOT NULL,
  `alg_model_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `zip_bucket_name` varchar(64)  NULL,
  `task_status_desc` varchar(2000) CHARACTER SET utf8mb4  DEFAULT NULL,
  `appid` VARCHAR(128) NULL,
  `user_info` VARCHAR(2000) DEFAULT NULL,
  `dataset_id` VARCHAR(64) NULL,
  `delete_no_label_picture` TINYINT NULL,
  `score_threshhold` DOUBLE NULL,
  `delete_similar_picture` INT(11) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


CREATE TABLE IF NOT EXISTS `tasks_reidtask` (
  `id` varchar(64) NOT NULL,
  `task_name` varchar(400) NOT NULL,
  `src_predict_taskid` varchar(128)  NULL,
  `dest_predict_taskid` varchar(4000)  NULL,
  `task_start_time` datetime(6) NOT NULL,
  `task_finish_time` datetime(6) DEFAULT NULL,
  `task_status` int(11) NOT NULL,
  `alg_model_id` int(11) NULL,
  `user_id` int(11) NOT NULL,
  `src_bucket_name` varchar(64)  NULL,
  `dest_bucket_name` varchar(64)  NULL,
  `task_status_desc` varchar(1000) CHARACTER SET utf8mb4  DEFAULT NULL,
  `assign_user_id` int(11) DEFAULT NULL,
  `relate_other_label_task` varchar(400) DEFAULT NULL,
  `task_flow_type` int(11) DEFAULT NULL,
  `task_type` int(11) DEFAULT NULL,
  `task_label_type_info` VARCHAR(4000) DEFAULT NULL,
  `finished_picture` int(11) DEFAULT NULL,
  `total_picture` int(11) DEFAULT NULL,
  `appid` VARCHAR(128) NULL,
  `user_info` VARCHAR(2000) DEFAULT NULL,
  `reid_obj_type` INT(11) NULL,
  `verify_user_id` INT(11) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


CREATE TABLE IF NOT EXISTS `tasks_reidtaskitem` (
  `id` varchar(64) NOT NULL,
  `pic_url` varchar(256) DEFAULT NULL,
  `pic_object_name` varchar(128) DEFAULT NULL,
  `label_info` TEXT(65535) NULL,
  `label_task_id` varchar(64) NOT NULL,
  `item_add_time` datetime(6) NOT NULL,
  `pic_image_field` varchar(400) DEFAULT NULL,
  `label_status` int(11) NOT NULL,
  `display_order1` INT(11) NULL,
  `display_order2` INT(11) NULL,
  `verify_status` INT(11) NULL,
  `verify_desc` VARCHAR(400) NULL,
  `pic_real_name` VARCHAR(400) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


CREATE TABLE IF NOT EXISTS `tasks_reidtask_show_result` (
  `label_task_id` varchar(64)  NOT NULL,
  `reid_name` varchar(500) NOT NULL,
  `related_info` TEXT(65535) NULL,
  PRIMARY KEY (`label_task_id`,`reid_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


CREATE TABLE IF NOT EXISTS `tasks_reidtask_result` (
  `id` varchar(64) NOT NULL,
  `src_image_info` varchar(500) NOT NULL,
  `label_task_id` varchar(64)  NOT NULL,
  `label_task_name` varchar(400)  NULL,
  `related_info` TEXT(65535) NULL,
  PRIMARY KEY (`id`, `src_image_info`, `label_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


-- ----------------------------
-- Table structure for tasks_retrainresult
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tasks_retrainresult` (
  `id` varchar(64) NOT NULL,
  `loss_train` varchar(128) DEFAULT NULL,
  `lr` varchar(128) DEFAULT NULL,
  `epoch_num` varchar(128) DEFAULT NULL,
  `epoch_total` varchar(128) DEFAULT NULL,
  `step_num` varchar(128) DEFAULT NULL,
  `step_total` varchar(128) DEFAULT NULL,
  `learning_rate` varchar(128) DEFAULT NULL,
  `accuracy_rate_train` varchar(128) DEFAULT NULL,
  `item_add_time` datetime(6) DEFAULT NULL,
  `item_cur_time` datetime(6) DEFAULT NULL,
  `alg_model_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;

-- ----------------------------
-- Table structure for tasks_retraintasks
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tasks_retraintasks` (
  `id` varchar(64) NOT NULL,
  `task_name` varchar(400) NOT NULL,
  `task_start_time` datetime(6) DEFAULT NULL,
  `task_finish_time` datetime(6) DEFAULT NULL,
  `task_status` int(11) NOT NULL,
  `task_status_desc` varchar(2000) DEFAULT NULL,
  `alg_model_id` int(11) NOT NULL,
  `pre_predict_task_id` varchar(64) NOT NULL,
  `user_id` int(11) NOT NULL,
  `pid` int(11) NOT NULL,
  `confPath` VARCHAR(200) NULL,
  `modelPath` VARCHAR(200) NULL,
  `retrain_type` VARCHAR(45) NULL,
  `retrain_data` VARCHAR(4000) NULL,
  `detection_type` VARCHAR(45) NULL,
  `detection_type_input` VARCHAR(45) NULL,
  `retrain_model_name` VARCHAR(128) NULL,
  `testTrainRatio` DOUBLE NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


-- ----------------------------
-- Table structure for users_userprofile
-- ----------------------------
CREATE TABLE IF NOT EXISTS `users_userprofile` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `password` varchar(128) NOT NULL,
  `last_login` datetime(6) DEFAULT NULL,
  `is_superuser` tinyint(1) NOT NULL,
  `username` varchar(150) NOT NULL,
  `first_name` varchar(30) CHARACTER SET utf8mb4  DEFAULT NULL,
  `last_name` varchar(30) CHARACTER SET utf8mb4  DEFAULT NULL,
  `email` varchar(254) NOT NULL,
  `is_staff` tinyint(1) NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  `date_joined` datetime(6) NOT NULL,
  `nick_name` varchar(50) DEFAULT NULL,
  `address` varchar(100) DEFAULT NULL,
  `mobile` varchar(100) DEFAULT NULL,
  `company` varchar(128) DEFAULT NULL,
  `parent_invite_code` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `mobile` (`mobile`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 ;


CREATE TABLE IF NOT EXISTS `tasks_dataset` (
  `id` varchar(64) NOT NULL,
  `task_name` varchar(400) NOT NULL,
  `task_desc` varchar(1000) DEFAULT NULL,
  `task_add_time` datetime(6) NOT NULL,
  `dataset_type` int(11) DEFAULT NULL,
  `total` int(11) DEFAULT NULL,
  `zip_object_name` varchar(255) DEFAULT NULL,
  `zip_bucket_name` varchar(255) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `assign_user_id` int(11) DEFAULT NULL,
  `task_status` int(11) DEFAULT NULL,
  `file_bucket_name` VARCHAR(255) NULL,
  `camera_number` VARCHAR(256) NULL,
  `camera_gps` VARCHAR(64) NULL,
  `camera_date` VARCHAR(64) NULL,
  `appid` VARCHAR(128) NULL,
  `user_info` VARCHAR(2000) DEFAULT NULL,
  `videoSet` VARCHAR(4000) NULL,
  `mainVideoInfo` VARCHAR(2000) NULL,
  `labelPropertyInfo` VARCHAR(4000) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ALTER table tasks_dataset DROP INDEX dataset_appid_index;
CREATE INDEX dataset_appid_index ON tasks_dataset (appid);


CREATE TABLE IF NOT EXISTS `tasks_videocounttask` (
  `id` varchar(64) NOT NULL,
  `task_name` varchar(400) NOT NULL,
  `dataset_id` varchar(128)  NULL,
  `task_add_time` datetime(6) NOT NULL,
  `task_finish_time` datetime(6) DEFAULT NULL,
  `task_status` int(11) NOT NULL,
  `zip_object_name` varchar(255) DEFAULT NULL,
  `zip_bucket_name` varchar(255) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `task_status_desc` varchar(1000) CHARACTER SET utf8mb4  DEFAULT NULL,
  `assign_user_id` int(11) DEFAULT NULL,
  `appid` VARCHAR(128) NULL,
  `user_info` VARCHAR(2000) DEFAULT NULL,
  `verify_user_id` INT(11) NULL,
  `mainVideoInfo` VARCHAR(2000) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


CREATE TABLE IF NOT EXISTS `tasks_videocounttaskitem` (
  `id` varchar(64) NOT NULL,
  `pic_url` varchar(256) DEFAULT NULL,
  `pic_object_name` varchar(128) DEFAULT NULL,
  `label_info` TEXT(65535) NULL,
  `label_task_id` varchar(64) NOT NULL,
  `item_add_time` datetime(6) NOT NULL,
  `pic_image_field` varchar(400) DEFAULT NULL,
  `label_status` int(11) NOT NULL,
  `display_order1` INT(11) NULL,
  `display_order2` INT(11) NULL,
  `verify_status` INT(11) NULL,
  `verify_desc` VARCHAR(400) NULL,
  `pic_real_name` VARCHAR(400) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


CREATE TABLE IF NOT EXISTS `log_info` (
  `id` varchar(64) NOT NULL,
  `oper_type` int(11) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `oper_name` varchar(200) NULL,
  `oper_id` varchar(200) NULL,
  `oper_json_content_old` TEXT(65535) NULL,
  `oper_json_content_new` TEXT(65535) NULL,
  `oper_time_start` datetime(6) NOT NULL,
  `oper_time_end` datetime(6) NOT NULL,
  `record_id` varchar(64) NULL,
  `extend1` varchar(400) NULL,
  `extend2` varchar(400) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


CREATE TABLE IF NOT EXISTS `report_label_task` (
  `user_id` int(11) NOT NULL,
  `oper_time` datetime(6) NOT NULL,
  `rectUpdate` int(11)  NULL,
  `rectAdd` int(11)  NULL,
  `properties`  int(11)  NULL,
  `pictureUpdate`  int(11)  NULL,
  `notValide` INT(11) NULL,
  PRIMARY KEY (`user_id`,`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


CREATE TABLE IF NOT EXISTS `tasks_dataset_videoinfo` (
  `id` varchar(64) NOT NULL,
  `dataset_id` varchar(400) NOT NULL,
  `minio_url` varchar(1000) DEFAULT NULL,
  `video_info` MediumText DEFAULT NULL,
  `camera_number` VARCHAR(256) NULL,
  `camera_gps` VARCHAR(64) NULL,
  `camera_date` VARCHAR(64) NULL,
  `duration` VARCHAR(64) NULL,
  `bitrate` VARCHAR(64) NULL,
  `startTime` VARCHAR(64) NULL,
  `videoCode` VARCHAR(400) NULL,
  `videoFormat` VARCHAR(400) NULL,
  `resolutionRatio` VARCHAR(64) NULL,
  `audioCode` VARCHAR(64) NULL,
  `audioFrequncy` VARCHAR(64) NULL,
  `fps` VARCHAR(45) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS `tasks_videolabeltask` (
  `id` varchar(64) NOT NULL,
  `task_name` varchar(400) NOT NULL,
  `dataset_id` varchar(128)  NULL,
  `task_add_time` datetime(6) NOT NULL,
  `task_finish_time` datetime(6) DEFAULT NULL,
  `task_status` int(11) NOT NULL,
  `zip_object_name` varchar(255) DEFAULT NULL,
  `zip_bucket_name` varchar(255) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `task_status_desc` varchar(1000) CHARACTER SET utf8mb4  DEFAULT NULL,
  `assign_user_id` int(11) DEFAULT NULL,
  `mainVideoInfo` VARCHAR(2000) NULL DEFAULT NULL,
  `appid` VARCHAR(128) NULL,
  `user_info` VARCHAR(2000) DEFAULT NULL,
  `task_label_type_info` VARCHAR(4000) NULL,
  `verify_user_id` INT(11) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


CREATE TABLE IF NOT EXISTS `tasks_videolabeltaskitem` (
  `id` varchar(64) NOT NULL,
  `pic_url` varchar(256) DEFAULT NULL,
  `pic_object_name` varchar(128) DEFAULT NULL,
  `label_info` TEXT(65535) NULL,
  `label_task_id` varchar(64) NOT NULL,
  `item_add_time` datetime(6) NOT NULL,
  `pic_image_field` varchar(400) DEFAULT NULL,
  `label_status` int(11) NOT NULL,
  `display_order1` INT(11) NULL,
  `display_order2` INT(11) NULL,
  `verify_status` INT(11) NULL,
  `verify_desc` VARCHAR(400) NULL,
  `pic_real_name` VARCHAR(400) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


-- --------------------------
-- 0715
-- --------------------------
CREATE TABLE IF NOT EXISTS `log_info_history` (
  `id` varchar(64) NOT NULL,
  `oper_type` int(11) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `oper_name` varchar(200) NULL,
  `oper_id` varchar(200) NULL,
  `oper_json_content_old` TEXT(65535) NULL,
  `oper_json_content_new` TEXT(65535) NULL,
  `oper_time_start` datetime(6) NOT NULL,
  `oper_time_end` datetime(6) NOT NULL,
  `record_id` varchar(64) NULL,
  `extend1` varchar(400) NULL,
  `extend2` varchar(400) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;



CREATE TABLE IF NOT EXISTS `tasks_largepicturelabeltask` (
  `id` varchar(64) NOT NULL,
  `task_name` varchar(400) NOT NULL,
  `dataset_id` varchar(128)  NULL,
  `task_add_time` datetime(6) NOT NULL,
  `task_finish_time` datetime(6) DEFAULT NULL,
  `task_status` int(11) NOT NULL,
  `zip_object_name` varchar(255) DEFAULT NULL,
  `zip_bucket_name` varchar(255) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `task_status_desc` varchar(1000) CHARACTER SET utf8mb4  DEFAULT NULL,
  `assign_user_id` int(11) DEFAULT NULL,
  `verify_user_id` INT(11) NULL,
  `mainVideoInfo` VARCHAR(2000) NULL DEFAULT NULL,
  `user_info` VARCHAR(2000) DEFAULT NULL,
  `appid` VARCHAR(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


CREATE TABLE IF NOT EXISTS `tasks_largepicturetaskitem` (
  `id` varchar(64) NOT NULL,
  `pic_url` varchar(256) DEFAULT NULL,
  `pic_object_name` varchar(128) DEFAULT NULL,
  `label_info` MediumText NULL,
  `label_task_id` varchar(64) NOT NULL,
  `item_add_time` datetime(6) NOT NULL,
  `pic_image_field` varchar(400) DEFAULT NULL,
  `label_status` int(11) NOT NULL,
  `verify_status` INT(11) NULL,
  `verify_desc` VARCHAR(400) NULL,
  `display_order1` INT(11) NULL,
  `display_order2` INT(11) NULL,
  `pic_real_name` VARCHAR(400) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;



-- --------------------------------
-- 0902
-- --------------------------------
-- add progress table
CREATE TABLE IF NOT EXISTS `tasks_progress` (
  `id` varchar(500) NOT NULL,
  `taskId` varchar(256) DEFAULT NULL,
  `progress` INT(11) NULL,
  `status` INT(11) NULL,
  `startTime` INT(11) NULL,
  `totalTime` INT(11) NULL,
  `exceedTime`  INT(11) NULL,
  `relatedFileName` varchar(2000) DEFAULT NULL,
  `info` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


-- ---------------------------------
-- 0907  add secure log info.
-- ---------------------------------
CREATE TABLE IF NOT EXISTS `log_sec_info` (
  `id` varchar(64) NOT NULL,
  `oper_type` int(11) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `oper_name` varchar(200) NULL,
  `oper_id` varchar(200) NULL,
  `log_info` varchar(2000) NULL,
  `oper_time_start` datetime(6) NOT NULL,
  `oper_time_end` datetime(6) NOT NULL,
  `record_id` varchar(64) NULL,
  `extend1` varchar(400) NULL,
  `extend2` varchar(400) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;

-- ---------------------------------
-- 0910  add login error info.
-- ---------------------------------
CREATE TABLE IF NOT EXISTS `login_info` (
  `user_id` int(11) NOT NULL,
  `login_error_time` int(11) NULL,
  `last_login_time` datetime(6) NOT NULL,
  `extend1` varchar(400) NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


CREATE TABLE IF NOT EXISTS `user_extend` (
  `user_id` int(11) NOT NULL,
  `func_table_name`  varchar(4000) NULL,
  `properties` varchar(4000) NULL,
  `oper_time` datetime(6) NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


-- 20210802 add label properity task table
CREATE TABLE IF NOT EXISTS `tasks_labelpropertytask` (
  `id` varchar(64) NOT NULL,
  `task_name` varchar(400) DEFAULT NULL,
  `task_type` varchar(100) DEFAULT NULL,
  `task_desc` varchar(100) DEFAULT NULL,
  `propertyJson` MediumText NULL,
  `user_id` int(11) NOT NULL,
  `assign_user_id` int(11) NOT NULL,
  `task_add_time` datetime(6) NOT NULL,
  `user_info` VARCHAR(2000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


-- 20210901
CREATE TABLE IF NOT EXISTS `regist_auto_model` (
  `id` int(1) NOT NULL AUTO_INCREMENT,
  `name` varchar(400) DEFAULT NULL,
  `model_desc` varchar(128) NOT NULL,
  `model_type` int(11) DEFAULT NULL,
  `object_type` varchar(4000) DEFAULT NULL,
  `agent_receive_task_url` varchar(400) NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4 ;


INSERT INTO alg_warehouse_alginstance(id, alg_name, add_time, alg_type_name, alg_root_dir) VALUES ('4', 'FreeAnchor', '2020-01-08 17:44:24.000000', '目标检测', '/mmdetection/');
INSERT INTO alg_warehouse_alginstance(id, alg_name, add_time, alg_type_name, alg_root_dir) VALUES ('5', 'Retinanet', '2020-01-08 17:44:24.000000', '目标检测', '/mmdetection/');

INSERT INTO alg_warehouse_alginstance(`id`, `alg_name`, `add_time`, `alg_type_name`, `alg_root_dir`) VALUES ('10', 'Tracking_pyECO', '2020-08-19 17:44:24.000000', '目标跟踪', '/pyECO/');
INSERT INTO alg_warehouse_alginstance(`id`, `alg_name`, `add_time`, `alg_type_name`, `alg_root_dir`) VALUES ('11', 'FastReID', '2020-08-19 17:44:24.000000', '目标重识别', '/fastreid/');




INSERT INTO alg_warehouse_algmodel (id, conf_path, model_name, local_path, model_url, alg_instance_id, exec_script, train_script, type_list, threshold) VALUES ('4', 'configs/free_anchor/retinanet_free_anchor_r50_fpn_1x.py', 'FreeAnchor', '', 'model/retinanet_free_anchor_r50_fpn_1x/epoch_12.pth', '4', 'python3 demoForJava.py --cfg {configPath} --checkpoint {modelPath}', 'python3  tools/train.py {configPath}',NULL,NULL);


INSERT INTO alg_warehouse_algmodel(id, conf_path, model_name, local_path, model_url, alg_instance_id, exec_script, train_script, type_list, threshold) VALUES ('7', 'configs/retinanet/retinanet_x101_64x4d_fpn_1x_car.py', 'Retinanet(all car)', NULL, 'model/retinanet_x101_64x4d_fpn_1x/epoch_9_car_new.pth', '5', 'python3 demoForJava.py --cfg {configPath} --checkpoint {modelPath}', 'python3  tools/train.py {configPath}',NULL,NULL);


INSERT INTO `alg_warehouse_algmodel` (`id`, `model_name`, `alg_instance_id`, `exec_script`) VALUES ('16', 'Tracking_pyECO', '10', 'python3 bin/demo_ECO_hc.py --video_dir  {data_dir} --custom_dataset_name  {data_name}');
INSERT INTO `alg_warehouse_algmodel` (`id`, `model_name`, `alg_instance_id`, `exec_script`, `type_list`) VALUES ('17', 'FastReID(Person)', '11', 'python3 tools/test_net.py --config-file ./configs/Person/sbs_R101-ibn-test.yml --datadir  {data_dir} --json_path  {data_dir}/test.json', '[\"person\"]');



INSERT INTO `users_userprofile`(id,password,last_login,is_superuser,username,first_name,last_name,email,is_staff,is_active,date_joined,nick_name,address,mobile,company,parent_invite_code) VALUES ('5', '�R�\'�~2~�f#Vd`8�7V����;}�t\\h�', NULL, '0', 'LabelSystem01', null, null, 'zouap@pcl.com.cn', '0', '0', '2019-12-24 16:02:52.000000', 'admin', '鹏城实验室', '1235698755', '实验室', null);



UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `type_list` = '[\"car\",\"person\"]' WHERE (`id` = '5');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `type_list` = '[\"person\"]' WHERE (`id` = '3');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `type_list` = '[\"person\"]' WHERE (`id` = '4');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `type_list` = '[\"car\"]' WHERE (`id` = '6');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `type_list` = '[\"car\"]' WHERE (`id` = '7');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `type_list` = '[\"car\"]' WHERE (`id` = '9');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `type_list` = '[\"person\"]' WHERE (`id` = '8');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `type_list` = '[\"person\"]' WHERE (`id` = '13');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `type_list` = '[\"cell\"]' WHERE (`id` = '10');



-- -------------------------------
-- 0721
-- --------------------------------
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `train_script` = './tools/dist_train.sh {configPath} {gpunum}  --validate' WHERE (`id` = '3');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `train_script` = './tools/dist_train.sh {configPath} {gpunum}  --validate' WHERE (`id` = '4');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `train_script` = './tools/dist_train.sh {configPath} {gpunum}  --validate' WHERE (`id` = '5');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `train_script` = './tools/dist_train.sh {configPath} {gpunum}  --validate' WHERE (`id` = '6');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `train_script` = './tools/dist_train.sh {configPath} {gpunum}  --validate' WHERE (`id` = '7');



UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `model_type` = '1' WHERE (`id` = '21');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `model_type` = '1' WHERE (`id` = '16');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `model_type` = '1' WHERE (`id` = '12');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `model_type` = '1' WHERE (`id` = '20');

UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `model_name` = 'Multiple Target FairMOT Tracking(person)' WHERE (`id` = '20');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `model_name` = 'Multiple Target CenterTrack Tracking(car+person)' WHERE (`id` = '21');

UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `auto_used` = '1' WHERE (`id` = '3');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `auto_used` = '1' WHERE (`id` = '4');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `auto_used` = '1' WHERE (`id` = '5');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `auto_used` = '1' WHERE (`id` = '6');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `auto_used` = '1' WHERE (`id` = '7');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `auto_used` = '1' WHERE (`id` = '11');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `auto_used` = '1' WHERE (`id` = '18');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `auto_used` = '1' WHERE (`id` = '20');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `auto_used` = '1' WHERE (`id` = '21');

UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `hand_label_used` = '1' WHERE (`id` = '3');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `hand_label_used` = '1' WHERE (`id` = '4');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `hand_label_used` = '1' WHERE (`id` = '5');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `hand_label_used` = '1' WHERE (`id` = '6');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `hand_label_used` = '1' WHERE (`id` = '7');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `hand_label_used` = '1' WHERE (`id` = '10');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `hand_label_used` = '1' WHERE (`id` = '11');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `hand_label_used` = '1' WHERE (`id` = '12');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `hand_label_used` = '1' WHERE (`id` = '16');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `hand_label_used` = '1' WHERE (`id` = '18');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `hand_label_used` = '1' WHERE (`id` = '20');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `hand_label_used` = '1' WHERE (`id` = '21');

UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `model_name` = 'Tracking_pyECO (Single Target)' WHERE (`id` = '16');
UPDATE `labelsystem`.`alg_warehouse_algmodel` SET `model_name` = 'Tracking (Single Target)' WHERE (`id` = '12');


-- 20210421
INSERT INTO `labelsystem`.`alg_warehouse_alginstance` (`id`, `alg_name`, `add_time`, `alg_type_name`, `alg_root_dir`) VALUES ('17', 'Segmentation', '2021-04-21 15:08:24.000000', '自动分割', '/deeplabv3/');
INSERT INTO `labelsystem`.`alg_warehouse_algmodel` (`id`, `model_name`, `alg_instance_id`, `exec_script`, `type_list`, `auto_used`) VALUES ('100', 'Auto Segmentation', '17', 'python3 tools_forlabel.py', '[\"road\",\"sidewalk\",\"building\",\"wall\",\"fence\",\"pole\",\"trafficlight\",\"trafficsign\",\"vegetation\",\"terrain\",\"sky\",\"person\",\"rider\",\"car\",\"truck\",\"bus\",\"train\",\"motorcycle\",\"bicycle\",\"other\"]', '1');


-- 20210609
INSERT INTO `labelsystem`.`alg_warehouse_alginstance` (`id`, `alg_name`, `add_time`, `alg_type_name`, `alg_root_dir`) VALUES ('19', 'MIAOD', '2021-06-09 15:08:24.000000', '主动分类', '/MIAL/');
INSERT INTO `labelsystem`.`alg_warehouse_algmodel` (`id`, `model_name`, `alg_instance_id`, `train_script`) VALUES ('60', 'MIAOD', '19', 'python tools/train.py configs/MIAOD.py --work_directory {work_directory} --num_classes {num_classes}');


