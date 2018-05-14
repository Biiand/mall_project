package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(String username);

    int checkEmail(String email);

    int checkEmailByUserId(@Param("email") String email,@Param("user_id") Integer userId);

    int checkAnswer(@Param("username") String username,@Param("question") String question,@Param("answer") String answer);

    User selectLogin(@Param("userName") String username,@Param("password") String password);

    String selectQuestionByUsername(String username);

    int updatePasswordByUsername(@Param("username") String username,@Param("password") String md5Password);

    int checkPassword(@Param("password") String password,@Param("user_id") Integer userId);

}