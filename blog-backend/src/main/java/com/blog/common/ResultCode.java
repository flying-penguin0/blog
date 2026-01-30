package com.blog.common;

import lombok.Getter;

/**
 * 返回状态码枚举
 * 
 * @author Blog System
 */
@Getter
public enum ResultCode {
    
    // 成功
    SUCCESS(200, "操作成功"),
    
    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    
    // 业务错误 4xx
    USERNAME_EXIST(4001, "用户名已存在"),
    EMAIL_EXIST(4002, "邮箱已存在"),
    USER_NOT_FOUND(4003, "用户不存在"),
    PASSWORD_ERROR(4004, "密码错误"),
    USER_DISABLED(4005, "用户已被禁用"),
    ARTICLE_NOT_FOUND(4006, "文章不存在"),
    COMMENT_NOT_FOUND(4007, "评论不存在"),
    CATEGORY_NOT_FOUND(4008, "分类不存在"),
    TAG_NOT_FOUND(4009, "标签不存在"),
    FILE_UPLOAD_ERROR(4010, "文件上传失败"),
    FILE_TYPE_ERROR(4011, "文件类型不支持"),
    FILE_SIZE_ERROR(4012, "文件大小超出限制"),
    CONTENT_AUDIT_FAILED(4013, "内容审核未通过"),
    
    // 服务器错误 5xx
    ERROR(500, "系统错误"),
    DATABASE_ERROR(5001, "数据库错误"),
    REDIS_ERROR(5002, "缓存错误"),
    MINIO_ERROR(5003, "文件存储错误"),
    AI_API_ERROR(5004, "AI接口调用失败"),
    EXTERNAL_API_ERROR(5005, "外部接口调用失败");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
