package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告Mapper接口
 * 
 * @author Blog System
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
