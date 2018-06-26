package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.ProductMapper;
import com.mmall.dao.RedisDao;
import com.mmall.dao.SecKillDetailMapper;
import com.mmall.dao.SecKillProductMapper;
import com.mmall.dto.Exposer;
import com.mmall.exception.SecKillException;
import com.mmall.pojo.Product;
import com.mmall.pojo.SecKillProduct;
import com.mmall.service.IProductService;
import com.mmall.service.ISecKIllService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.SecKillProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SecKillServiceImpl implements ISecKIllService {

    @Autowired
    SecKillProductMapper secKillProductMapper;

    @Autowired
    SecKillDetailMapper secKillDetailMapper;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    RedisDao redisDao;

    @Override
    public ServiceResponse<PageInfo> getSecKillList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<SecKillProduct> list = secKillProductMapper.selectList();
        List<Integer> idList = new ArrayList<>();
        for (SecKillProduct product : list) {
            idList.add(product.getId());
        }
        List<Product> productList = productMapper.selectById(idList);
        List<SecKillProductListVo> secKillProductListVoList = new ArrayList<>();
        for (int i = 0; i < productList.size(); i++) {
            secKillProductListVoList.add(assembleSecKillProductListVo(productList.get(i), list.get(i)));
        }
        PageInfo pageInfo = new PageInfo(list);
        pageInfo.setList(secKillProductListVoList);
        return ServiceResponse.createBySuccess(pageInfo);
    }

    /**
     * 执行秒杀操作需要获取md5Token，防止用户提前秒杀
     * 针对秒杀商品时增加的大量的商品查询操作，使用Redis进行缓存，减少数据库访问量
     * 使用最简单的基于超时的数据一致性维护
     *
     * @param secKillId
     * @param userId
     * @return
     */
    @Override
    public ServiceResponse<Exposer> getExposer(Integer secKillId, Integer userId) {
        SecKillProduct secKillProduct = redisDao.get(secKillId);
        if(secKillProduct == null){
            secKillProduct = secKillProductMapper.selectById(secKillId);
            if (secKillProduct == null) {
                return ServiceResponse.createBySuccess(new Exposer(false, secKillId));
            }
            redisDao.set(secKillProduct);
        }

        long startTime = secKillProduct.getStartTime().getTime();
        long endTime = secKillProduct.getEndTime().getTime();
        long currentTime = new Date().getTime();
        if (currentTime < startTime || currentTime > endTime) {
            return ServiceResponse.createBySuccess(new Exposer(false, secKillId, currentTime, startTime, endTime));
        }
        String MD5Token = generateMD5Token(secKillId);
        if (TokenCache.getValue(TokenCache.SECKILL_TOKEN_PREFIX + secKillId) == null) {
            TokenCache.setKey(TokenCache.SECKILL_TOKEN_PREFIX + secKillId, MD5Token);
        }
        return ServiceResponse.createBySuccess(new Exposer(true, secKillId, MD5Token));
    }

    /**
     * 验证秒杀条件
     *
     * @param secKillId
     * @param userId
     * @param MD5Token
     * @return
     */
    @Override
    public ServiceResponse secKillExecution(Integer secKillId, Integer userId, String MD5Token) {
        if (secKillId == null || userId == null) {
            return ServiceResponse.createByErrorMessage("参数错误");
        }
        String TokenInCache = TokenCache.getValue(TokenCache.SECKILL_TOKEN_PREFIX + secKillId);
        if (StringUtils.isEmpty(MD5Token) || !MD5Token.equals(TokenInCache)) {
            return ServiceResponse.createByErrorMessage("非法操作");
        }

        SecKillProduct product = secKillProductMapper.selectById(secKillId);
        if (product == null) {
            return ServiceResponse.createByErrorMessage("未找到该商品");
        } else if (product.getStock() == 0) {
            return ServiceResponse.createByErrorMessage("秒杀结束");
        }

        int resultCount = secKillDetailMapper.insert(secKillId, userId);
        if (resultCount <= 0) {
            return ServiceResponse.createByErrorMessage("重复秒杀");
        }
        return ServiceResponse.createBySuccess();
    }

    @Override
    public ServiceResponse saveOrUpdate(SecKillProduct secKillProduct) {
        if (secKillProduct == null) {
            return ServiceResponse.createByErrorMessage("参数错误");
        }
        Product product = productMapper.selectByPrimaryKey(secKillProduct.getProductId());
        if (product == null) {
            return ServiceResponse.createByErrorMessage("未找到此商品");
        }
        if (secKillProduct.getStock() > product.getStock() || secKillProduct.getPrice().compareTo(product.getPrice()) == 1) {
            return ServiceResponse.createByErrorMessage("库存不足或秒杀价格高于原价");
        }
        int resultCount;
        Product productForUpdate;
        if (secKillProduct.getId() != null) {
            SecKillProduct origin = secKillProductMapper.selectById(secKillProduct.getId());

            resultCount = secKillProductMapper.updateByPrimaryKeySelective(secKillProduct);
            if (resultCount > 0) {
//              更新成功后修改当前商品秒杀活动外的部分的库存
                int stockDiff = origin.getStock() - secKillProduct.getStock();
                if (stockDiff != 0) {
                    productForUpdate = new Product();
                    productForUpdate.setStock(product.getStock() + stockDiff);
                    resultCount = productMapper.updateByPrimaryKeySelective(productForUpdate);

                    if (resultCount <= 0) {
                        throw new SecKillException("修改秒杀商品失败");
                    }
                }
                return ServiceResponse.createBySuccessMessage("修改秒杀商品成功");
            }
            return ServiceResponse.createByErrorMessage("修改秒杀商品失败");
        }else{
            resultCount = secKillProductMapper.insert(secKillProduct);
            if (resultCount > 0) {
                productForUpdate = new Product();
                productForUpdate.setStock(product.getStock() - secKillProduct.getStock());
                resultCount = productMapper.updateByPrimaryKeySelective(productForUpdate);

                if (resultCount <= 0) {
                    throw new SecKillException("添加秒杀商品失败");
                }

                return ServiceResponse.createBySuccessMessage("添加秒杀商品成功");
            }
            return ServiceResponse.createByErrorMessage("添加秒杀商品失败");
        }

    }

    /**
     * 生成秒杀商品需要的Token,防止用户越权执行秒杀
     *
     * @param secKillId
     * @return
     */
    private String generateMD5Token(int secKillId) {
//        md5算法不可逆，但要防止被加密的内容太简单，可能会被猜出来，所以要添加混乱的盐值和按一定规则拼接
        String base = PropertiesUtil.getProperty("exposer.salt") + "//" + secKillId;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

    private SecKillProductListVo assembleSecKillProductListVo(Product product, SecKillProduct secKillProduct) {
        SecKillProductListVo vo = new SecKillProductListVo();

        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setCategoryId(product.getCategoryId());
        vo.setSubtitle(product.getSubtitle());
        vo.setMainImage(product.getMainImage());
        vo.setOriginalPrice(product.getPrice());
        vo.setPrice(secKillProduct.getPrice());
        vo.setStatus(product.getStatus());
        vo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        vo.setStartTime(DateTimeUtil.dateToStr(secKillProduct.getStartTime()));
        vo.setCurrentTime(DateTimeUtil.dateToStr(new Date()));
        vo.setEndTime(DateTimeUtil.dateToStr(secKillProduct.getEndTime()));

        return vo;
    }


}
