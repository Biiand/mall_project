package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hasee on 2018/5/3.
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    ShippingMapper shippingMapper;

    @Override
    public ServiceResponse add(Integer userId, Shipping shipping) {
        if(shipping == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        shipping.setUserId(userId);
        int resultCount = shippingMapper.insert(shipping);
        if(resultCount > 0){
            Map resultMap = new HashMap<>();
//           将新建的地址的id返回给前端，用于订单时使用，使用shipping.getId()返回insert时数据库创建的主键，
//              因为在insert上使用了userGeneratedKey属性返回主键，并使用keyproperty = id将返回的键值绑定到id属性上
            resultMap.put("shippingId",shipping.getId());
            return ServiceResponse.createBySuccess("新建地址成功",resultMap);
        }
        return ServiceResponse.createByErrorMessage("新建地址失败");
    }

    @Override
    public ServiceResponse delete(Integer userId, Integer shippingId) {
//        使用userId和shippingId共同限定删除的数据，避免出现横向越权问题，
//          可以不用事前校验shippingId == null,因为值为null数据库就找不到数据进行删除
        int resultCount = shippingMapper.deleteByUserIdAndPrimaryKey(userId,shippingId);
        if(resultCount > 0){
            return ServiceResponse.createBySuccessMessage("删除地址成功");
        }
        return ServiceResponse.createByErrorMessage("删除地址失败");
    }

    @Override
    public ServiceResponse update(Integer userId, Shipping shipping) {
        if(shipping == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        shipping.setUserId(userId);
        int resultCount = shippingMapper.updateByPrimaryKeyAndUserId(shipping);
        if(resultCount > 0){
            return ServiceResponse.createBySuccessMessage("修改地址成功");
        }
        return ServiceResponse.createByErrorMessage("修改地址失败");
    }

    @Override
    public ServiceResponse getDetail(Integer userId, Integer shippingId) {
        Shipping shipping = shippingMapper.selectByPrimaryKeyAndUserId(userId,shippingId);
        if(shipping == null){
            return ServiceResponse.createByErrorMessage("未找到该地址信息");
        }
        return ServiceResponse.createBySuccess(shipping);
    }

    @Override
    public ServiceResponse<PageInfo> getList(Integer userId,Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServiceResponse.createBySuccess(pageInfo);
    }
}
