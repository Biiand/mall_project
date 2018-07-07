package com.mmall.controller.backend;

import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hasee on 2018/4/29.
 */
@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    IUserService iUserService;

    @Autowired
    IProductService iProductService;

    @Autowired
    IFileService iFileService;

    @RequestMapping(value = "save_product.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse productSave(HttpServletRequest request, Product product){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iProductService.saveOrUpdateProduct(product);
        }
        return response;
    }

    /**
     * 更改商品状态，status在数据库中的取值范围为（1,2,3），由前端进行限定
     * @param request
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping(value = "set_sale_status.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse setSaleStatus(HttpServletRequest request, Integer productId,Integer status){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iProductService.setSaleStatus(productId,status);
        }
        return response;
    }

    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse getProductDetail(HttpServletRequest request, Integer productId){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iProductService.manageProductDetail(productId);
        }
        return response;
    }

    @RequestMapping(value = "get_list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse getProductList(HttpServletRequest request,
                                          @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                          @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iProductService.getProductList(pageNum,pageSize);
        }
        return response;
    }

    @RequestMapping(value = "search.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse searchProduct(HttpServletRequest request,String productName,Integer productId,
                                         @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        }
        return response;
    }

    @RequestMapping(value = "upload.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse uploadImages(@RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request){
//       进行任何操作前对账户进行权限校验是为了避免遭到恶意攻击
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            /**
             * ServletContext的定义，每一个部署的web应用都有唯一的一个ServletContext对象，信息被该web应用下的servlet实例共享
             * Defines a set of methods that a servlet uses to communicate with its servlet
             * container, for example, to get the MIME type of a file, dispatch requests, or
             * write to a log file.
             * There is one context per "web application" per Java Virtual Machine.
             * request.getServletContext()获取servletContext对象，根据该对象获取web容器下的目录的路径upload，
             * 这里upload与WEB-INF同级，upload目录交给service中的代码来创建，因为手动创建文件夹和业务无关
             */
            if(file.isEmpty()){
                return ServiceResponse.createByErrorMessage("上传文件为空，请选择上传文件");
            }
            String path = request.getServletContext().getRealPath("upload");
            String targetFileName = iFileService.uploadImages(file,path);
            if(StringUtils.isBlank(targetFileName)){
                return ServiceResponse.createByErrorMessage("上传失败");
            }
//           组装访问图片的url
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            Map fileMap = new HashMap<>();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServiceResponse.createBySuccess(fileMap);
        }
        return response;
    }

    /**
     * 富文本上传，项目前端使用了simditor插件处理富文本上传，所以返回值需要按照该插件要求的格式进行处理
     * 因为就只是该方法中需要按此格式返回，所以就不新建类了，直接使用一个map进行存储
     * @param request
     * @param response
     * @param file
     * @return
     */
    @RequestMapping(value = "rich_text_img_upload.do",method = RequestMethod.POST)
    @ResponseBody
    public Map richTextImgUpload(HttpServletRequest request,HttpServletResponse response,
                                        @RequestParam(value = "upload_file",required = false) MultipartFile file){
        Map resultMap = new HashMap<>();

//       进行任何操作前对账户进行权限校验是为了避免遭到恶意攻击
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            resultMap.put("success",false);
            resultMap.put("msg","未登录，需要登陆");
            return resultMap;
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);

        if(user == null){
            resultMap.put("success",false);
            resultMap.put("msg","未登录，需要登陆");
            return resultMap;
        }
        ServiceResponse serviceResponse = iUserService.checkAdminRole(user);
        if(serviceResponse.isSuccess()){
            if(file.isEmpty()){
                resultMap.put("success",false);
                resultMap.put("msg","上传文件为空，请选择上传文件");
                return resultMap;
            }
            String path = request.getServletContext().getRealPath("upload");
            String targetFileName = iFileService.uploadImages(file,path);
            if(StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }
//           组装访问图片的url
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
//              simditor插件对返回标头的要求
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");

            return resultMap;
        }else{
            resultMap.put("success",false);
            resultMap.put("msg","无操作权限");
            return resultMap;
        }
    }
}
