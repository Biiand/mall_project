package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.dto.Exposer;
import com.mmall.pojo.SecKillProduct;

public interface ISecKIllService {

    ServiceResponse<PageInfo> getSecKillList(Integer pageNum,Integer pageSize);

    ServiceResponse<Exposer> getExposer(Integer secKillId, Integer userId);

    ServiceResponse secKillExecution(Integer secKillId,Integer userId,String MD5Token);

    ServiceResponse saveOrUpdate(SecKillProduct secKillProduct);
}
