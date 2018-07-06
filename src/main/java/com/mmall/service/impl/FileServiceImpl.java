package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by hasee on 2018/5/1.
 */
@Service("iFileService")
@Slf4j
public class FileServiceImpl implements IFileService {

//    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String uploadImages(MultipartFile file, String path) {
        String originalFileName = file.getOriginalFilename();

        log.info("file是否为空:{}",file.isEmpty());

        String fileExtensionName = originalFileName.substring(originalFileName.lastIndexOf(".")+1);
//        为上传的文件重新设一个唯一的文件名，解决上传的文件重名的问题
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;

        log.info("开始上传文件，原始文件名：{}，接收路径：{}，上传文件名：{}",originalFileName,path,uploadFileName);

        File fileDir = new File(path);
//        如果path下的文件夹不存在，则先创建该文件夹并设为可写
        if(!fileDir.exists()){
            fileDir.setWritable(true);
//            mkdir:创建path路径下的这个文件夹
//            fileDir.mkdir();
//            mkdirs:创建path路径下的这个文件夹是，包括需要但不存在的父文件夹
            fileDir.mkdirs();
        }

//        创建path路径下的名为uploadFileName的文件的实例
        File targetFile = new File(path,uploadFileName);

        try {
//            将上传文件保存到tomcat目录下
            file.transferTo(targetFile);
//            将tomcat下的文件上传到FTP服务器上,
//              其实可以直接将文件上传到FTP，这里是为了对上传文件进行一个练习，所以先传给tomcat
            Boolean isSuccess = FTPUtil.uploadFile(Lists.newArrayList(targetFile));
//              执行上传FTP后不管是否成功都要将tomcat目录下的文件删除
            targetFile.delete();
            if(isSuccess){
                return targetFile.getName();
            }
            return null;
        } catch (IOException e) {
            log.error("上传文件异常",e);
            return null;
        }
    }
}
