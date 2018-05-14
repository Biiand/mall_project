package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by hasee on 2018/5/1.
 */
public interface IFileService {

    String uploadImages(MultipartFile file,String path);
}
