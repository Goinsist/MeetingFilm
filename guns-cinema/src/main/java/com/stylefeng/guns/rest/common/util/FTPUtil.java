package com.stylefeng.guns.rest.common.util;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "ftp")
public class FTPUtil {
    //地址 端口 用户名 密码
    private String hostName;
    private Integer port;
    private String userName;
    private String password;
   private FTPClient ftpClient;
    private void initFTPClient(){
        try {
        ftpClient=new FTPClient();
            ftpClient.setConnectTimeout(100*1000);
        ftpClient.setControlEncoding("utf-8");
        ftpClient.connect(hostName,port);
        ftpClient.login(userName,password);
        }catch (Exception e){
          log.error("初始化FTP失败",e);
        }
    }
    //输入一个路径将路径里的文件转换成字符串返回给我
    public String getFileStrByAddress(String fileAddress){
        BufferedReader bufferedReader=null;
        try{
             initFTPClient();
            ftpClient.enterLocalPassiveMode();
            System.err.println(System.currentTimeMillis());
            bufferedReader=new BufferedReader(new InputStreamReader(ftpClient.retrieveFileStream(fileAddress)));
        StringBuffer stringBuffer=new StringBuffer();
            while (true){
                String lineStr=bufferedReader.readLine();
                if(lineStr==null){
                    break;
                }
                stringBuffer.append(lineStr);
           }
ftpClient.logout();
            System.err.println(System.currentTimeMillis());
            return stringBuffer.toString();
        }catch (Exception e){
            log.error("获取文件信息失败",e);
        }finally {
             try {
                 if(bufferedReader!=null){
                     bufferedReader.close();
                 }

             }catch (IOException e){
                 e.printStackTrace();
             }
        }
        return null;
    }

    public static void main(String[] args) {
        FTPUtil ftpUtil=new FTPUtil();
        ftpUtil.getFileStrByAddress("");
    }
}
