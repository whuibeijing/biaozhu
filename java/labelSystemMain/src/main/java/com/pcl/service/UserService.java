package com.pcl.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pcl.constant.Constants;
import com.pcl.constant.UserConstants;
import com.pcl.dao.AuthTokenDao;
import com.pcl.dao.LabelTaskItemDao;
import com.pcl.dao.PrePredictTaskResultDao;
import com.pcl.dao.ReIDLabelTaskItemDao;
import com.pcl.dao.UserDao;
import com.pcl.dao.UserExtendDao;
import com.pcl.exception.LabelSystemException;
import com.pcl.pojo.PageResult;
import com.pcl.pojo.mybatis.LogSecInfo;
import com.pcl.pojo.mybatis.User;
import com.pcl.pojo.mybatis.UserExtend;
import com.pcl.util.JsonUtil;
import com.pcl.util.PwdCheckUtil;
import com.pcl.util.SHAUtil;
import com.pcl.util.TimeUtil;

@Service
public class UserService {

	private static Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private UserExtendDao userExtendDao;
	
	@Autowired
	private LabelTaskItemDao labelTaskItemDao;
	
	@Autowired
	private PrePredictTaskResultDao prePredictTaskResultDao;
	
	@Autowired
	private ReIDLabelTaskItemDao reIDLabelTaskItemDao;
	
	@Autowired
	private AuthTokenDao authTokenDao;
	
	@Value("${password.policy:2}")
	private int pwdPolicy;
	
	@Autowired
	private LogSecService logSecService;
	
	public Map<String,String> getUserInfo(int userId,int assignUserId,int verifyUserId){
		
		Map<String,String> userInfo = new HashMap<>();
		
		if(userId != -1) {
			User user = queryUserById(userId);
			if(user != null) {
				userInfo.put(UserConstants.USER_NAME, user.getUsername());
			}
		}
		if(assignUserId != -1) {
			User user = queryUserById(assignUserId);
			if(user != null) {
				userInfo.put(UserConstants.ASSIGN_USER_NAME, user.getUsername());
			}
		}
		if(verifyUserId != -1) {
			User user = queryUserById(userId);
			if(user != null) {
				userInfo.put(UserConstants.VERIFY_USER_NAME, user.getUsername());
			}
		}
		
		return userInfo;
	}
	
	//private final static int DATASET_SINGLE_TABLE = 1;
	public int addMedicalUser(User user) throws LabelSystemException {
		logger.info("add medical user=" + JsonUtil.toJson(user));
		try {
			String savePasswordStr = SHAUtil.getEncriptStr(user.getPassword());
			user.setPassword(savePasswordStr);
			
			String timeStr = TimeUtil.getCurrentTimeStr();
			if(user.getDate_joined() == null) {
				user.setDate_joined(timeStr); 
			}
			if(user.getLast_login() == null) {
				user.setLast_login(timeStr);
			}
			if(user.getIs_superuser() == 0) {
				user.setIs_superuser(1);
			}
			
			logger.info("add user id:" + user.getId() + " name=" + user.getUsername());
			userDao.addUser(user);
			
			List<User> userList = userDao.queryUser(user.getUsername());
			int addUserID = userList.get(0).getId();
			
			LogSecInfo logSecInfo = new LogSecInfo();
			logSecInfo.setOper_id("addUser");
			logSecInfo.setLog_info("????????????????????????????????????" + user.getUsername());
			logSecInfo.setUser_id(addUserID);
			logSecInfo.setOper_name("????????????");
			logSecInfo.setOper_time_start(TimeUtil.getCurrentTimeStr());
			logSecInfo.setOper_time_end(TimeUtil.getCurrentTimeStr());
			
			logSecService.addSecLogInfo(logSecInfo);
			
			return userList.get(0).getId();
		}catch (Exception e) {
			e.printStackTrace();
			if(e.getMessage().indexOf("for key 'username'") != -1) {
				throw new LabelSystemException("??????????????????");
			}else if(e.getMessage().indexOf("for key 'mobile'") != -1) {
				throw new LabelSystemException("??????????????????????????????");
			}
			throw new LabelSystemException(e.getMessage());
		}

	}
	

	public int addUser(String token, User user) throws LabelSystemException {
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		
		User loginUser = userDao.queryUserById(userId);
		if(loginUser == null || loginUser.getIs_superuser() != Constants.USER_SUPER) {
			throw new LabelSystemException(user.getNick_name() + " ????????????????????????");
		}
		
		return addMedicalUser(user);

	}
	
	
	public int deleteUser(String token, int userId) throws LabelSystemException {
		int loginUserId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		User loginUser = userDao.queryUserById(loginUserId);
		if(loginUser == null || loginUser.getIs_superuser() != Constants.USER_SUPER) {
			throw new LabelSystemException(userId + " ????????????????????????");
		}
		//?????????token??????user
		authTokenDao.deleteTokenByUser(String.valueOf(userId));
		TokenManager.removeToken(token);
		try {
			User deleteUser = userDao.queryUserById(userId);
			userDao.deleteUser(userId);
			
			LogSecInfo logSecInfo = new LogSecInfo();
			logSecInfo.setOper_id("deleteUser");
			logSecInfo.setLog_info("????????????????????????????????????" + deleteUser.getUsername());
			logSecInfo.setUser_id(userId);
			logSecInfo.setOper_name("????????????");
			logSecInfo.setOper_time_start(TimeUtil.getCurrentTimeStr());
			logSecInfo.setOper_time_end(TimeUtil.getCurrentTimeStr());
			
			logSecService.addSecLogInfo(logSecInfo);
			
			return 1;
		}catch (Exception e) {
			logger.info(e.getMessage());
			throw new LabelSystemException("????????????????????????????????????????????????????????????????????????????????????????????????????????????");
		}
	}

	public PageResult queryUser(String token, Integer currPage, Integer pageSize) throws LabelSystemException {
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		User loginUser = userDao.queryUserById(userId);
		if(loginUser == null || loginUser.getIs_superuser() != Constants.USER_SUPER) {
			throw new LabelSystemException(userId + " ????????????????????????");
		}
		
		Map<String,Integer> paramMap = new HashMap<>();
		paramMap.put("currPage", currPage * pageSize);
		paramMap.put("pageSize", pageSize);
		List<User> dbList = userDao.queryUserPage(paramMap);
		int totalCount = userDao.queryUserCount(paramMap);
		
		PageResult re = new PageResult();
		re.setCurrent(currPage);
		re.setTotal(totalCount);
		re.setData(dbList);
		
		return re;
	}
	
	public User queryUserById(int userId) {
		
		return userDao.queryUserById(userId);
		
	}
	
	public List<User> queryAllUser(String token) throws LabelSystemException {
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		User loginUser = userDao.queryUserById(userId);
		if(loginUser == null) {
			throw new LabelSystemException(userId + " ??????????????????");
		}

		return userDao.queryAllIdOrName();
	}
	
	public Map<Integer,String> getAllUser(){
		List<User> userList = userDao.queryAll();
		HashMap<Integer,String> userIdForName = new HashMap<>();
		for(User user : userList) {
			userIdForName.put(user.getId(), user.getUsername());
		}
		return userIdForName;
	}


	public List<User> queryAllUserBySuperUser(String token) throws LabelSystemException {
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		User loginUser = userDao.queryUserById(userId);
		if(loginUser == null) {
			throw new LabelSystemException("???????????????user_id=" + userId);
		}
		if(loginUser.getIs_superuser() == Constants.USER_SUPER) {
			return userDao.queryAll();
		}else {
			return Arrays.asList(loginUser);
		}
	}


	public void updateUserPassword(String token, int user_id, String newPassword,String oldPassword) throws LabelSystemException {
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		User loginUser = userDao.queryUserById(userId);
		if(loginUser.getIs_superuser() != Constants.USER_SUPER) {
			if(user_id != userId) {
				throw new LabelSystemException("??????????????????");
			}
		}
		
		if(loginUser.getIs_superuser() != Constants.USER_SUPER) {//???????????????????????????
			User updateUser = userDao.queryUserById(user_id);
			String oldPasswordStr = SHAUtil.getEncriptStr(oldPassword);
			if(!oldPasswordStr.equals(updateUser.getPassword())) {
				throw new LabelSystemException("?????????????????????????????????");
			}
		}
		if(UserConstants.POLICY_MIDDLE == pwdPolicy || UserConstants.POLICY_COMPLEX == pwdPolicy) {
			if(!PwdCheckUtil.checkPasswordLength(newPassword, "8", "24")){
				throw new LabelSystemException("?????????????????????8?????????24????????????");
			}
			if(UserConstants.POLICY_MIDDLE == pwdPolicy) {
				if(!(PwdCheckUtil.checkContainDigit(newPassword) && PwdCheckUtil.checkContainCase(newPassword))) {
					throw new LabelSystemException("????????????????????????????????????");
				}
			}
			if(UserConstants.POLICY_COMPLEX == pwdPolicy) {
				if(!(PwdCheckUtil.checkContainDigit(newPassword) && PwdCheckUtil.checkContainCase(newPassword) && PwdCheckUtil.checkContainSpecialChar(newPassword))) {
					throw new LabelSystemException("???????????????????????????????????????????????????");
				}
			}
		}
		
		logger.info("update password. user=" + loginUser.getUsername());
		String savePasswordStr = SHAUtil.getEncriptStr(newPassword);
		User user = new User();
		user.setId(user_id);
		user.setPassword(savePasswordStr);
		userDao.updateUserPassword(user);
		
		LogSecInfo logSecInfo = new LogSecInfo();
		logSecInfo.setOper_id("updateUser");
		logSecInfo.setLog_info("??????????????????????????????????????????" + loginUser.getUsername());
		logSecInfo.setUser_id(userId);
		logSecInfo.setOper_name("????????????");
		logSecInfo.setOper_time_start(TimeUtil.getCurrentTimeStr());
		logSecInfo.setOper_time_end(TimeUtil.getCurrentTimeStr());
		
		logSecService.addSecLogInfo(logSecInfo);
	}


	public List<User> queryVerifyUser(String token) throws LabelSystemException {
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		User loginUser = userDao.queryUserById(userId);
		if(loginUser == null) {
			throw new LabelSystemException(userId + " ??????????????????");
		}
		return userDao.queryVerifyUser();
	}


	public int queryUserIdByToken(String token) throws LabelSystemException {
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		User loginUser = userDao.queryUserById(userId);
		if(loginUser == null) {
			throw new LabelSystemException(userId + " ??????????????????");
		}
		return userId;
	}


	public void updateUserExtendTableName(String token, int user_id, String funcTableInfo) throws LabelSystemException {
		List<String> tableInfoList = JsonUtil.getList(funcTableInfo);
		
		if(tableInfoList.isEmpty()) {
			return;
		}
		
		UserExtend userExtend = new UserExtend();
		userExtend.setUser_id(user_id);
		userExtend.setFuncTableName(funcTableInfo);
		userExtend.setOperTime(TimeUtil.getCurrentTimeStr());
		
		if(userExtendDao.queryUserExtend(user_id) == null) {
		   userExtendDao.addUserExtend(userExtend);
		}else {
		   userExtendDao.updateUserExtendFuncTableName(userExtend);
		}
		
		//?????????
		for(String tableInfo : tableInfoList) {
			if(UserConstants.LABEL_TASK_SINGLE_TABLE == Integer.parseInt(tableInfo)) {
				String tableName = UserConstants.LABEL_TASK_SINGLE_TABLE_NAME + user_id;
				if(labelTaskItemDao.existTable(tableName) == 0) {
					labelTaskItemDao.createTable(tableName);
				}
				
			}
			else if(UserConstants.PREDICT_SINGLE_TABLE == Integer.parseInt(tableInfo)) {
				String tableName = UserConstants.PREDICT_SINGLE_TABLE_NAME + user_id;
				if(prePredictTaskResultDao.existTable(tableName) == 0) {
					prePredictTaskResultDao.createTable(tableName);
				}
			}
			else if(UserConstants.REID_TASK_SINGLE_TABLE == Integer.parseInt(tableInfo)) {
				String tableName = UserConstants.REID_TASK_SINGLE_TABLE_NAME + user_id;
				if(reIDLabelTaskItemDao.existTable(tableName) == 0) {
					reIDLabelTaskItemDao.createTable(tableName);
				}
			}
		}
	}


	public void updateMedicalUserPassword(String userName, String newPassword) throws LabelSystemException {
		
		List<User> loginUserList = userDao.queryUser(userName);
		if(loginUserList == null || loginUserList.isEmpty()) {
			return;
		}
		User loginUser = loginUserList.get(0);
		logger.info("update password. user=" + loginUser.getUsername());
		String savePasswordStr = SHAUtil.getEncriptStr(newPassword);
		User user = new User();
		user.setId(loginUser.getId());
		user.setPassword(savePasswordStr);
		userDao.updateUserPassword(user);
		
		LogSecInfo logSecInfo = new LogSecInfo();
		logSecInfo.setOper_id("updateUser");
		logSecInfo.setLog_info("??????????????????????????????????????????" + loginUser.getUsername());
		logSecInfo.setUser_id(loginUser.getId());
		logSecInfo.setOper_name("????????????");
		logSecInfo.setOper_time_start(TimeUtil.getCurrentTimeStr());
		logSecInfo.setOper_time_end(TimeUtil.getCurrentTimeStr());
		
		logSecService.addSecLogInfo(logSecInfo);
	}


	public void updateUserIdentity(String token, int user_id, int identity) throws LabelSystemException {
		int userId = TokenManager.getUserIdByToken(TokenManager.getServerToken(token));
		User loginUser = userDao.queryUserById(userId);
		if(loginUser.getIs_superuser() != Constants.USER_SUPER) {
			if(user_id != userId) {
				throw new LabelSystemException("??????????????????");
			}
		}
		
		if(!(identity == 1 || identity == 2)) {
			throw new LabelSystemException("???????????????????????????????????????????????????????????????");
		}
		
		logger.info("update identity. user=" + loginUser.getUsername() + " identity=" + identity);

		User user = new User();
		user.setId(user_id);
		user.setIs_superuser(identity);
		userDao.updateUserIndentity(user);
		
		LogSecInfo logSecInfo = new LogSecInfo();
		logSecInfo.setOper_id("updateUser");
		logSecInfo.setLog_info("??????????????????????????????????????????" + identity);
		logSecInfo.setUser_id(userId);
		logSecInfo.setOper_name("????????????");
		logSecInfo.setOper_time_start(TimeUtil.getCurrentTimeStr());
		logSecInfo.setOper_time_end(TimeUtil.getCurrentTimeStr());
		
		logSecService.addSecLogInfo(logSecInfo);
	}
	

}
