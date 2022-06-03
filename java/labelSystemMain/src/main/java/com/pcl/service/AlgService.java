package com.pcl.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pcl.constant.Constants;
import com.pcl.dao.AlgInstanceDao;
import com.pcl.dao.AlgModelDao;
import com.pcl.dao.AutoLabelModelRegistMsgDao;
import com.pcl.pojo.mybatis.AlgInstance;
import com.pcl.pojo.mybatis.AlgModel;
import com.pcl.pojo.mybatis.AutoLabelModelRegistMsg;
import com.pcl.util.TimeUtil;

@Service
public class AlgService {

	private static Logger logger = LoggerFactory.getLogger(AlgService.class);

	@Autowired
	private AlgModelDao algModelDao;

	@Autowired
	private AlgInstanceDao algInstanceDao;
	
	@Autowired
	private AutoLabelModelRegistMsgDao autoLabelModelRegistMsgDao;


	public int addAlgModel(AlgModel algModel) {
		logger.info("start add alg model, modelName=" + algModel.getModel_name());
		return algModelDao.addAlgModel(algModel);
	}

	public int deleteAlgModel(int id) {
		logger.info("start delete alg model, alg model id=" + id);
		return algModelDao.delete(id);
	}


	public List<AlgModel> queryAlgModel() {
		logger.info("start query all alg model");
		List<AlgModel> reList = algModelDao.queryAlgModelAll();
		for(AlgModel algModel : reList) {
			algModel.setExec_script(null);
		}
		return reList;
	}


	public List<AlgModel> queryAlgModelContainWiseMedical() {
		logger.info("start query queryAlgModelContainWiseMedical ");
		List<AlgModel> reList = algModelDao.queryAlgModelContainWiseMedical();
		for(AlgModel algModel : reList) {
			algModel.setExec_script(null);
		}
		return reList;
	}


	public int deleteAlgInstance(int id) {
		logger.info("start delete alg instance, alg instance id=" + id);
		return algInstanceDao.delete(id);
	}


	public int addAlgInstance(AlgInstance algInstance) {
		logger.info("start add alg instance, alg name=" + algInstance.getAlg_name());

		algInstance.setAdd_time(TimeUtil.getCurrentTimeStr());

		return algInstanceDao.addAlgInstance(algInstance);
	}

	public List<AlgModel> queryAlgModelForRetrain() {
		List<AlgModel> dbList = algModelDao.queryAlgModelAll();
		List<AlgModel> returnList = new ArrayList<>();
		for(AlgModel algModel : dbList) {
			if(algModel.getTrain_script() != null && algModel.getTrain_script().length() > 0) {
				returnList.add(algModel);
			}
		}
		return returnList;
	}

	public List<AlgModel> queryAlgModelForTracking() {
		List<AlgModel> dbList = algModelDao.queryAlgModelForTracking();
		List<AutoLabelModelRegistMsg> reList = autoLabelModelRegistMsgDao.queryAllOnLine();
		
		for(AutoLabelModelRegistMsg msg : reList) {
			AlgModel algModel = new AlgModel();
			algModel.setId(msg.getId());
			if(msg.getModel_type() ==5 || msg.getModel_type() ==6) {
				//目标跟踪
				algModel.setModel_type(msg.getModel_type());
				algModel.setModel_name(msg.getName());
				dbList.add(algModel);
			}
		}
		return dbList;
	}


	public List<AlgModel> queryAlgModelForAutoLabel() {
		List<AlgModel> dbList = algModelDao.queryAlgModelForAutoLabel();

		List<AutoLabelModelRegistMsg> reList = autoLabelModelRegistMsgDao.queryAllOnLine();
		
		for(AutoLabelModelRegistMsg msg : reList) {
			AlgModel algModel = new AlgModel();
			algModel.setId(msg.getId());
			if(msg.getModel_type() ==5 || msg.getModel_type() ==6) {
				//目标跟踪不需要显示在这里
				continue;
			}
			algModel.setModel_type(msg.getModel_type());
			algModel.setModel_name(msg.getName());
			dbList.add(algModel);
		}
		
		return dbList;
	}

	public List<AlgModel> queryAlgModelForHandLabel() {
		List<AlgModel> dbList = algModelDao.queryAlgModelForHandLabel();
		List<AutoLabelModelRegistMsg> reList = autoLabelModelRegistMsgDao.queryAllOnLine();
		
		for(AutoLabelModelRegistMsg msg : reList) {
			AlgModel algModel = new AlgModel();
			algModel.setId(msg.getId());
			
			algModel.setModel_type(msg.getModel_type());
			algModel.setModel_name(msg.getName());
			dbList.add(algModel);
		}
		return dbList;
	}

	public List<AlgModel> queryAlgModelForProperty() {
		List<AlgModel> dbList = algModelDao.queryAlgModelForProperty();
		return dbList;
	}

}
