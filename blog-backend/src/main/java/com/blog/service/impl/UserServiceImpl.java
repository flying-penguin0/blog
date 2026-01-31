package com.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.PageResult;
import com.blog.common.ResultCode;
import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import com.blog.service.UserService;
import com.blog.utils.JwtUtil;
import com.blog.utils.RedisUtil;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * @author Blog System
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private com.blog.mapper.ArticleMapper articleMapper;
    
    @Autowired
    private com.blog.mapper.CommentMapper commentMapper;
    
    @Autowired
    private com.blog.utils.CaptchaGenerator captchaGenerator;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        // 检查用户名是否存在
        User existUser = getUserByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }
        
        // 检查邮箱是否存在
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, dto.getEmail());
            if (this.count(wrapper) > 0) {
                throw new BusinessException(ResultCode.EMAIL_EXIST);
            }
        }
        
        // 检查昵称是否存在
        String nickname = dto.getNickname() != null && !dto.getNickname().trim().isEmpty() 
                ? dto.getNickname() 
                : dto.getUsername();
        User existNickname = getUserByNickname(nickname);
        if (existNickname != null) {
            throw new BusinessException(ResultCode.NICKNAME_EXIST);
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setNickname(nickname);
        user.setRole("user");
        user.setStatus(1);
        
        this.save(user);
    }
    
    @Override
    public LoginVO login(LoginDTO dto) {
        // 验证验证码
        if (!captchaGenerator.verifyCaptcha(dto.getCaptchaKey(), dto.getCaptchaCode())) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
        
        // 查询用户
        User user = getUserByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        
        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        
        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 存储 Token 到 Redis（7天过期）
        String tokenKey = "token:" + user.getId();
        redisUtil.set(tokenKey, token, 7, TimeUnit.DAYS);
        
        // 返回登录信息
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        return new LoginVO(token, userVO);
    }
    
    @Override
    public UserVO getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentUser(Long userId, UserVO userVO) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 只允许更新昵称、邮箱、头像和签名
        if (userVO.getNickname() != null && !userVO.getNickname().trim().isEmpty()) {
            // 检查昵称是否被其他用户使用
            User existNickname = getUserByNickname(userVO.getNickname());
            if (existNickname != null && !existNickname.getId().equals(userId)) {
                throw new BusinessException(ResultCode.NICKNAME_EXIST);
            }
            user.setNickname(userVO.getNickname());
        }
        if (userVO.getEmail() != null) {
            user.setEmail(userVO.getEmail());
        }
        if (userVO.getAvatar() != null) {
            user.setAvatar(userVO.getAvatar());
        }
        if (userVO.getSignature() != null) {
            user.setSignature(userVO.getSignature());
        }
        
        this.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, com.blog.dto.ChangePasswordDTO dto) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 验证原密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(user);
    }
    
    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return this.getOne(wrapper);
    }
    
    @Override
    public User getUserByNickname(String nickname) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getNickname, nickname);
        return this.getOne(wrapper);
    }
    
    @Override
    public PageResult<UserVO> getAllUsers(String username, String role, Integer page, Integer size) {
        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 用户名搜索
        if (username != null && !username.trim().isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        
        // 角色筛选
        if (role != null && !role.trim().isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);
        
        // 查询总数
        Long total = this.count(wrapper);
        
        // 分页查询
        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);
        
        List<User> users = this.list(wrapper);
        
        // 转换为 VO
        List<UserVO> userVOList = users.stream()
                .map(user -> BeanUtil.copyProperties(user, UserVO.class))
                .collect(Collectors.toList());
        
        return new PageResult<>(userVOList, total, page, size);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserVO userVO) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 更新用户信息
        if (userVO.getNickname() != null && !userVO.getNickname().trim().isEmpty()) {
            // 检查昵称是否被其他用户使用
            User existNickname = getUserByNickname(userVO.getNickname());
            if (existNickname != null && !existNickname.getId().equals(id)) {
                throw new BusinessException(ResultCode.NICKNAME_EXIST);
            }
            user.setNickname(userVO.getNickname());
        }
        if (userVO.getEmail() != null) {
            user.setEmail(userVO.getEmail());
        }
        if (userVO.getSignature() != null) {
            user.setSignature(userVO.getSignature());
        }
        if (userVO.getAvatar() != null) {
            user.setAvatar(userVO.getAvatar());
        }
        
        this.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleUserStatus(Long id, Long currentUserId) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 不能禁用自己
        if (id.equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "不能禁用自己的账号");
        }
        
        // 切换状态
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        this.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 不能删除管理员
        if ("admin".equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        
        this.removeById(id);
    }
    
    @Override
    public java.util.Map<String, Object> getUserStats(Long userId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // 获取用户信息
        User user = this.getById(userId);
        if (user != null) {
            stats.put("signature", user.getSignature());
        }
        
        // 获取用户文章数
        LambdaQueryWrapper<com.blog.entity.Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.eq(com.blog.entity.Article::getUserId, userId);
        long articleCount = articleMapper.selectCount(articleWrapper);
        
        // 获取用户文章的总浏览量
        List<com.blog.entity.Article> articles = articleMapper.selectList(articleWrapper);
        long viewCount = articles.stream()
                .mapToLong(com.blog.entity.Article::getViewCount)
                .sum();
        
        // 获取用户发表的评论数
        LambdaQueryWrapper<com.blog.entity.Comment> myCommentWrapper = new LambdaQueryWrapper<>();
        myCommentWrapper.eq(com.blog.entity.Comment::getUserId, userId);
        long myCommentCount = commentMapper.selectCount(myCommentWrapper);
        
        // 获取用户文章收到的评论总数（只统计已通过审核的评论）
        List<Long> articleIds = articles.stream()
                .map(com.blog.entity.Article::getId)
                .collect(Collectors.toList());
        
        long commentCount = 0;
        if (!articleIds.isEmpty()) {
            LambdaQueryWrapper<com.blog.entity.Comment> commentWrapper = new LambdaQueryWrapper<>();
            commentWrapper.in(com.blog.entity.Comment::getArticleId, articleIds);
            commentWrapper.eq(com.blog.entity.Comment::getStatus, "approved"); // 只统计已通过审核的评论
            commentCount = commentMapper.selectCount(commentWrapper);
        }
        
        stats.put("articleCount", articleCount);
        stats.put("viewCount", viewCount);
        stats.put("myCommentCount", myCommentCount);
        stats.put("commentCount", commentCount);
        
        return stats;
    }
}
