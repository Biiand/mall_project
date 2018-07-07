package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by hasee on 2018/4/24.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServiceResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServiceResponse.createByErrorMessage("用户名不存在");
        }
//        使用todo关键字对预留的功能位置进行标注,从下方的TODO可以快速定位
//        "t odo" 密码登陆MD5
//      对密码进行MD5加密，使用加密后的密码与数据库进行比较,新建一个引用是为了与原来的引用进行区分，便于阅读
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServiceResponse.createByErrorMessage("密码错误");
        }

//        用户登陆成功后将用户的User对象的密码置为空字符串（""），为什么要调用Utils方法，而不是直接传一个"" ?
//        使用成熟的工具类能减少重复工作和提高程序的健壮性
        user.setPassword(StringUtils.EMPTY);

        return ServiceResponse.createBySuccess("登陆成功", user);
    }


    @Override
    public ServiceResponse register(User user) {
//          校验用户名和邮箱
        ServiceResponse validResponse = this.checkValid(Const.USERNAME, user.getUsername());
        if (!validResponse.isSuccess()) return validResponse;
        validResponse = this.checkValid(Const.EMAIL, user.getEmail());
        if (!validResponse.isSuccess()) return validResponse;

//        为用户设定默认角色
        user.setRole(Const.Role.ROLE_CUSTOMER);
//        为密码进行MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServiceResponse.createByErrorMessage("注册失败");
        }
        return ServiceResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServiceResponse checkValid(String type, String value) {
        if (StringUtils.isNotBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(value);
                if (resultCount > 0) {
                    return ServiceResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(value);
                if (resultCount > 0) {
                    return ServiceResponse.createByErrorMessage("邮箱已存在");
                }
            }
        } else {
            return ServiceResponse.createByErrorMessage("参数错误");
        }
        return ServiceResponse.createBySuccessMessage("校验通过");
    }

    @Override
    public ServiceResponse<String> selectQuestion(String username) {
        ServiceResponse response = this.checkValid(Const.USERNAME, username);
        if (response.isSuccess()) {
            return ServiceResponse.createByErrorMessage("该用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServiceResponse.createBySuccess(question);
        }
        return ServiceResponse.createByErrorMessage("未找到密码提示问题");
    }

    @Override
    public ServiceResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount == 0) {
            return ServiceResponse.createByErrorMessage("答案错误");
        }
//        当用户答对问题后为用户生成唯一的token，在用户重置密码的时候校验该token，避免横向越权问题（具体看5-1 9分30秒部分）
        String userToken = UUID.randomUUID().toString();

//        v1.0版本将userToken存入本地缓存，在v2.0进行tomcat集群后需要迁移到Redis中，
//          因为本地缓存是在单台的Tomcat的内存中，当用户重置密码时如果访问到其它的tomcat上，是读不到userToken的；
//        v1.0 : 将userToken放入本地缓存,注意key值是使用用户名进行拼接的，这样就保证了key值的唯一性
//        TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,userToken);

//        v2.0 : 将userToken放入Redis
        RedisShardedPoolUtil.setEx(Const.TOKEN_PREFIX + username, userToken, 60 * 60);
        return ServiceResponse.createBySuccess(userToken);
    }

    @Override
    public ServiceResponse<String> forgetResetPassword(String username, String newPassword, String token) {
        ServiceResponse response = this.checkValid(Const.USERNAME, username);
        if (response.isSuccess()) {
            return ServiceResponse.createByErrorMessage("该用户不存在");
        }

        if (StringUtils.isBlank(token)) {
            return ServiceResponse.createByErrorMessage("非法操作,token为空");
        }

//        v1.0 : 从本地缓存中取值
//        String tokenInCache = TokenCache.getValue(TokenCache.TOKEN_PREFIX + username);

//        v2.0 : 从Redis中取值
        String userToken = RedisShardedPoolUtil.get(Const.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(userToken)) {
            return ServiceResponse.createByErrorMessage("token无效或过期");
        }

//        StringUtils中的equals方法直接将比较的字符串传入，在方法内进行非空判断，可以避免普通equals方法的空指针问题
        if (StringUtils.equals(token, userToken)) {
            String md5Password = MD5Util.MD5EncodeUtf8(newPassword);
            int resultCount = userMapper.updatePasswordByUsername(username, md5Password);
            if (resultCount > 0) {
                return ServiceResponse.createBySuccessMessage("修改密码成功");
            }
        } else {
            return ServiceResponse.createByErrorMessage("token错误");
        }
        return ServiceResponse.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServiceResponse<String> resetPassword(User user, String oldPassword, String newPassword) {
//        为了避免横向越权，在用户修改密码时需要校验旧密码, 并用id确认该用户是唯一的，因为单纯使用密码一个字段容易被撞库
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPassword), user.getId());
        if (resultCount == 0) {
            return ServiceResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(newPassword));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServiceResponse.createBySuccessMessage("修改密码成功");
        }
        return ServiceResponse.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServiceResponse<User> updateUserInfo(User user) {
//        校验用户修改的的email，新的email不能和当前用户已有的email之外的email相同
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServiceResponse.createByErrorMessage("邮箱已存在,请使用其它邮箱");
        }
//        new一个新的user对象存储想要更新的字段，避免直接传入初始user进行更新时重写所有字段，这有性能的考虑，
//          也有业务复杂后数据安全的考虑，因为前端传来的数据组成的对象中可能被恶意的添加了数据，例如修改了user的
//              权限，这样直接插入数据库就会造成纵向越权问题
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setPhone(user.getPhone());
        updateUser.setEmail(user.getEmail());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
//            更新成功后将用户的信息查出来，因为需要更新session，
//              这里是对课程进行了修改，按课程里通过取旧的user对象给新的user赋值的方式信息不全，只是这样就多了一次数据库访问
            updateUser = userMapper.selectByPrimaryKey(user.getId());
            if (updateUser != null) {
                updateUser.setPassword(StringUtils.EMPTY);
                return ServiceResponse.createBySuccess("更新信息成功", updateUser);
            }

        }
        return ServiceResponse.createByErrorMessage("更新信息失败");
    }

    @Override
    public ServiceResponse<User> getUserInfo(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServiceResponse.createByErrorMessage("找不到该用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServiceResponse.createBySuccess(user);
    }

    @Override
    public ServiceResponse checkAdminRole(User user) {
        if (user != null && user.getRole() == Const.Role.ROLE_ADMIN) {
            return ServiceResponse.createBySuccess();
        }
        return ServiceResponse.createByErrorMessage("无操作权限");
    }

    @Override
    public ServiceResponse checkAdminBeforeOperate(User user) {
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要登陆");
        }
        return checkAdminRole(user);
    }
}
