import request from './request'

/**
 * 获取所有公告列表（管理员）
 */
export const getAnnouncementList = (params) => {
  return request({
    url: '/announcement/list',
    method: 'get',
    params
  })
}

/**
 * 获取最新公告（前台展示）
 */
export const getLatestAnnouncements = (limit = 5) => {
  return request({
    url: '/announcement/latest',
    method: 'get',
    params: { limit }
  })
}

/**
 * 根据ID获取公告详情
 */
export const getAnnouncementById = (id) => {
  return request({
    url: `/announcement/${id}`,
    method: 'get'
  })
}

/**
 * 创建公告
 */
export const createAnnouncement = (data) => {
  return request({
    url: '/announcement',
    method: 'post',
    data
  })
}

/**
 * 更新公告
 */
export const updateAnnouncement = (id, data) => {
  return request({
    url: `/announcement/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除公告
 */
export const deleteAnnouncement = (id) => {
  return request({
    url: `/announcement/${id}`,
    method: 'delete'
  })
}
