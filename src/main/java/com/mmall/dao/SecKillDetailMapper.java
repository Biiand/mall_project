package com.mmall.dao;

import com.mmall.pojo.SecKillDetail;
import com.mmall.pojo.SecKillProduct;
import org.apache.ibatis.annotations.Param;

public interface SecKillDetailMapper {

    int insert(@Param("secKillId") Integer secKillId,@Param("userId") Integer userId);


}
