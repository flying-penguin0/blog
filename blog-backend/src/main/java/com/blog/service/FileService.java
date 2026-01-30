package com.blog.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 * 
 * @author Blog System
 */
public interface FileService {
    
    /**
     * 上传文件
     * 
     * @param file 文件
     * @return 文件访问 URL
     */
    String uploadFile(MultipartFile file);
    
    /**
     * 删除文件
     * 
     * @param fileName 文件名
     */
    void deleteFile(String fileName);
}
