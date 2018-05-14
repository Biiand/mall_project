package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by hasee on 2018/5/1.
 */
public class FTPUtil {

    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    private String ip;
    private int port;
    private String user;
    private String password;
    private FTPClient client;

    private FTPUtil(String ip,int port,String user,String password){
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public static boolean uploadFile(List<File> fileList) throws IOException {
//        FTP配置文件中配置了端口号为21
        FTPUtil ftpUtil = new FTPUtil(ftpIp,21,ftpUser,ftpPass);
        logger.info("开始连接FTP服务器上传文件");

        boolean isSuccess = ftpUtil.uploadFile("img",fileList);

        logger.info("结束上传，上传结果：{}",isSuccess);
        return isSuccess;
    }

    private boolean connectServer(String ip,String user,String password){
        client = new FTPClient();
        boolean isSuccess = false;
        try {
            client.connect(ip);
            isSuccess = client.login(user,password);
        } catch (IOException e) {
            logger.error("连接FTP服务器异常",e);
        }
        return isSuccess;
    }

    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        boolean isSuccess = false;
        FileInputStream fis = null;
        if(connectServer(this.ip,this.user,this.password)){
            try {
                client.changeWorkingDirectory(remotePath);
                client.setBufferSize(1024);
                client.setControlEncoding("UTP-8");
//                设置被传输的文件的存在形式，这里设为将文件转换为二进制文件进行传输，避免产生乱码问题
                client.setFileType(FTPClient.BINARY_FILE_TYPE);
//                开启本地被动模式：客户端在上传文件给服务器之前要告诉服务器提供一个端口，客户端使用这个端口，
//                对应的是enterLocalActiveMode,本地主动模式，客户端在上传时使用客户端保存的端口
                client.enterLocalPassiveMode();
                for(File fileItem : fileList){
                    fis = new FileInputStream(fileItem);
                    client.storeFile(fileItem.getName(),fis);
                }
                isSuccess = true;
            } catch (IOException e) {
                logger.error("上传文件到FTP服务器异常",e);
            }finally {
//                这里产生的IOException向外抛不做处理是因为将异常抛到业务层会进行进一步的处理，
//                不是所有的异常都要在最开始就处理掉，可以合理利用异常
                fis.close();
                client.disconnect();
            }
        }
        return isSuccess;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public FTPClient getClient() {
        return client;
    }

    public void setClient(FTPClient client) {
        this.client = client;
    }
}
