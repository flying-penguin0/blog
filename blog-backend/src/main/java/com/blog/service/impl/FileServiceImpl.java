package com.blog.service.impl;

import com.blog.common.ResultCode;
import com.blog.exception.BusinessException;
import com.blog.service.FileService;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * 文件服务实现类
 * 
 * @author Blog System
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {
    
    @Autowired
    private MinioClient minioClient;
    
    @Value("${minio.endpoint}")
    private String endpoint;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    // 允许的图片格式
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
    
    // 最大文件大小 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    @Override
    public String uploadFile(MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "文件不能为空");
            }
            
            // 检查文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new BusinessException(ResultCode.FILE_SIZE_ERROR.getCode(), "文件大小不能超过10MB");
            }
            
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "文件名不能为空");
            }
            
            // 检查文件扩展名
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            boolean isAllowed = false;
            for (String ext : ALLOWED_EXTENSIONS) {
                if (ext.equals(extension)) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) {
                throw new BusinessException(ResultCode.FILE_TYPE_ERROR.getCode(), "不支持的文件格式");
            }
            
            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
            
            // 确保桶存在
            ensureBucketExists();
            
            // 压缩图片（如果是图片）
            InputStream inputStream;
            long fileSize;
            String contentType = file.getContentType();
            
            if (contentType != null && contentType.startsWith("image/")) {
                // 压缩图片
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Thumbnails.of(file.getInputStream())
                        .scale(1.0)
                        .outputQuality(0.8)
                        .toOutputStream(outputStream);
                byte[] bytes = outputStream.toByteArray();
                inputStream = new ByteArrayInputStream(bytes);
                fileSize = bytes.length;
            } else {
                inputStream = file.getInputStream();
                fileSize = file.getSize();
            }
            
            // 上传文件到 MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, fileSize, -1)
                            .contentType(contentType)
                            .build()
            );
            
            // 返回文件访问 URL
            String fileUrl = endpoint + "/" + bucketName + "/" + fileName;
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteFile(String fileName) {
        try {
            // 从 URL 中提取文件名
            if (fileName.contains("/")) {
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            }
            
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            
            log.info("文件删除成功: {}", fileName);
            
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "文件删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 确保桶存在，不存在则创建
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            
            if (!exists) {
                // 创建桶
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                
                // 设置桶为公开访问
                String policy = """
                        {
                            "Version": "2012-10-17",
                            "Statement": [
                                {
                                    "Effect": "Allow",
                                    "Principal": {"AWS": "*"},
                                    "Action": ["s3:GetObject"],
                                    "Resource": ["arn:aws:s3:::%s/*"]
                                }
                            ]
                        }
                        """.formatted(bucketName);
                
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policy)
                                .build()
                );
                
                log.info("MinIO 桶创建成功: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查或创建 MinIO 桶失败", e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "MinIO 初始化失败");
        }
    }
}
