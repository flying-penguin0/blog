import request from './request'

/**
 * 创建留言
 */
export const createMessage = (data) => {
  return request.post('/message', data)
}

/**
 * 获取留言列表
 */
export const getMessageList = (params) => {
  return request.get('/message', { params })
}

/**
 * 删除留言
 */
export const deleteMessage = (id) => {
  return request.delete(`/message/${id}`)
}

/**
 * 审核留言
 */
export const auditMessage = (id, status) => {
  return request.put(`/message/${id}/audit`, null, { params: { status } })
}
