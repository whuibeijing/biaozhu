ALTER TABLE `labelsystem`.`tasks_labeltask` ADD COLUMN `appid` VARCHAR(128) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltask` ADD COLUMN `user_info` VARCHAR(2000) NULL;
ALTER TABLE `labelsystem`.`tasks_prepredicttasks` ADD COLUMN `appid` VARCHAR(128) NULL;
ALTER TABLE `labelsystem`.`tasks_prepredicttasks` ADD COLUMN `user_info` VARCHAR(2000) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtask` ADD COLUMN `appid` VARCHAR(128) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtask` ADD COLUMN `user_info` VARCHAR(2000) NULL;
ALTER TABLE `labelsystem`.`tasks_dataset` ADD COLUMN `appid` VARCHAR(128) NULL;
ALTER TABLE `labelsystem`.`tasks_dataset` ADD COLUMN `user_info` VARCHAR(2000) NULL;
CREATE INDEX dataset_appid_index ON tasks_dataset (appid);

ALTER TABLE `labelsystem`.`tasks_videocounttask` ADD COLUMN `appid` VARCHAR(128) NULL;
ALTER TABLE `labelsystem`.`tasks_videocounttask` ADD COLUMN `user_info` VARCHAR(2000) NULL;

ALTER TABLE `labelsystem`.`tasks_videolabeltask` ADD COLUMN `appid` VARCHAR(128) NULL;
ALTER TABLE `labelsystem`.`tasks_videolabeltask` ADD COLUMN `user_info` VARCHAR(2000) NULL;

ALTER TABLE `labelsystem`.`tasks_largepicturelabeltask` ADD COLUMN `appid` VARCHAR(128) NULL;
ALTER TABLE `labelsystem`.`tasks_largepicturelabeltask` ADD COLUMN `user_info` VARCHAR(2000) NULL;

ALTER TABLE `labelsystem`.`tasks_dataset` 
ADD COLUMN `labelPropertyInfo` VARCHAR(4000) NULL DEFAULT NULL AFTER `mainVideoInfo`;

-- 20210802 add label properity task table
CREATE TABLE `tasks_labelpropertytask` (
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
CREATE TABLE `regist_auto_model` (
  `id` varchar(64) NOT NULL,
  `name` varchar(400) DEFAULT NULL,
  `model_desc` varchar(128) NOT NULL,
  `model_type` int(11) DEFAULT NULL,
  `object_type` varchar(4000) DEFAULT NULL,
  `agent_receive_task_url` varchar(400) NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 ;

-- 20210928
ALTER TABLE `labelsystem`.`regist_auto_model` 
ADD COLUMN `agent_receive_task_url` VARCHAR(400) NULL DEFAULT NULL AFTER `object_type`;


-- 20211012,support chinese file name
ALTER TABLE `labelsystem`.`tasks_reidtaskitem` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_0` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_1` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_2` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_3` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_4` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_5` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_6` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_7` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_8` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_9` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_10` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_11` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_12` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_13` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_14` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_15` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_16` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_17` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_18` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_19` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_20` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_21` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_22` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_23` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_24` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_25` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_26` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_27` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_28` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_29` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_30` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_31` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_32` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_33` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_34` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_35` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_36` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_37` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_38` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_39` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_40` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_41` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_42` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_43` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_44` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_45` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_46` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_47` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_48` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_49` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_50` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_51` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_52` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_53` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_54` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_55` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_56` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_57` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_58` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_59` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_60` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_61` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_62` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_63` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_64` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_65` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_66` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_67` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_68` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_69` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_70` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_71` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_72` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_73` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_74` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_75` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_76` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_77` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_78` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_79` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_80` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_81` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_82` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_83` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_84` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_85` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_86` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_87` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_88` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_89` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_90` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_91` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_92` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_93` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_94` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_95` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_96` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_97` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_98` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_reidtaskitem_99` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;


ALTER TABLE `labelsystem`.`tasks_labeltaskitem` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_0` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_1` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_2` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_3` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_4` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_5` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_6` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_7` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_8` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_9` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_10` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_11` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_12` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_13` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_14` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_15` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_16` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_17` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_18` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_19` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_20` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_21` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_22` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_23` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_24` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_25` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_26` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_27` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_28` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_29` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_30` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_31` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_32` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_33` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_34` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_35` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_36` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_37` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_38` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_39` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_40` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_41` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_42` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_43` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_44` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_45` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_46` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_47` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_48` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_49` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_50` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_51` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_52` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_53` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_54` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_55` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_56` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_57` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_58` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_59` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_60` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_61` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_62` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_63` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_64` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_65` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_66` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_67` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_68` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_69` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_70` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_71` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_72` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_73` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_74` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_75` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_76` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_77` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_78` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_79` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_80` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_81` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_82` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_83` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_84` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_85` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_86` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_87` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_88` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_89` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_90` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_91` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_92` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_93` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_94` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_95` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_96` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_97` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_98` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;
ALTER TABLE `labelsystem`.`tasks_labeltaskitem_99` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;


ALTER TABLE `labelsystem`.`tasks_videolabeltaskitem` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;

ALTER TABLE `labelsystem`.`tasks_videocounttaskitem` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;

ALTER TABLE `labelsystem`.`tasks_largepicturetaskitem` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;

ALTER TABLE `labelsystem`.`tasks_labeldcmtaskitem` ADD COLUMN `pic_real_name` VARCHAR(400) NULL;

ALTER TABLE `labelsystem`.`tasks_prepredicttasks` 
ADD COLUMN `score_threshhold` DOUBLE NULL;
