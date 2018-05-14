package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;

/**
 * Created by hasee on 2018/4/24.
 */
public interface IUserService {

    ServiceResponse<User> login(String username, String password);

    ServiceResponse<String> register(User user);

    ServiceResponse<String> checkValid(String type,String value);

    ServiceResponse<String> selectQuestion(String username);

    ServiceResponse<String> checkAnswer(String username,String question,String answer);

    ServiceResponse<String> forgetResetPassword(String username,String newPassword,String token);

    ServiceResponse<String> resetPassword(User user,String oldPassword,String newPassword);

    ServiceResponse<User> updateUserInfo(User user);

    ServiceResponse<User> getUserInfo(Integer userId);

    ServiceResponse checkAdminRole(User user);

    ServiceResponse checkAdminBeforeOperate(User user);
}
