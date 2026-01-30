import request from './request'

/**
 * 发表评论
 */
export const createComment = (data) => {
  return request({
    url: '/comment',
    method: 'post',
    data
  })
}

/**
 * 删除评论
 */
export const deleteComment = (id) => {
  return request({
    url: `/comment/${id}`,
    method: 'delete'
  })
}

/**
 * 获取文章评论列表
 */
export const getArticleComments = (articleId) => {
  return request({
    url: `/comment/article/${articleId}`,
    method: 'get'
  })
}

/**
 * 点赞评论
 */
export const likeComment = (id) => {
  return request({
    url: `/comment/${id}/like`,
    method: 'post'
  })
}

/**
 * 审核评论（管理员）
 */
export const auditComment = (id, status) => {
  return request({
    url: `/comment/${id}/audit`,
    method: 'put',
    params: { status }
  })
}

/**
 * 批量审核评论（管理员）
 */
export const batchAuditComments = (ids, status) => {
  return request({
    url: '/comment/batch/audit',
    method: 'put',
    params: { status },
    data: ids
  })
}

/**
 * 批量删除评论（管理员）
 */
export const batchDeleteComments = (ids) => {
  return request({
    url: '/comment/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 获取所有评论列表（管理员）
 */
export const getAllComments = (params) => {
  return request({
    url: '/comment/list',
    method: 'get',
    params
  })
}

/**
 * 获取评论列表（支持用户筛选）
 */
export const getCommentList = (params) => {
  return request({
    url: '/comment/list',
    method: 'get',
    params
  })
}
