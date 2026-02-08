import request from './request'

// 获取历史消息
export const getHistory = (limit = 50) => {
  return request.get('/chatroom/history', { params: { limit } })
}

// 获取在线用户
export const getOnlineUsers = () => {
  return request.get('/chatroom/online-users')
}

// 管理员：获取所有消息
export const getAdminMessages = (page, size) => {
  return request.get('/chatroom/admin/messages', {
    params: { page, size }
  })
}

// 管理员：删除消息
export const deleteMessage = (id) => {
  return request.delete(`/chatroom/admin/messages/${id}`)
}

// 管理员：审核通过消息
export const approveMessage = (id) => {
  return request.put(`/chatroom/admin/messages/${id}/approve`)
}

// 管理员：拒绝消息
export const rejectMessage = (id) => {
  return request.put(`/chatroom/admin/messages/${id}/reject`)
}

// 管理员：获取待审核消息数量
export const getPendingCount = () => {
  return request.get('/chatroom/admin/pending-count')
}

// 用户上线
export const userOnline = () => {
  return request.post('/chatroom/online')
}

// 用户下线
export const userOffline = () => {
  return request.post('/chatroom/offline')
}

// 获取我的聊天记录
export const getMyChatMessages = (params) => {
  return request.get('/chatroom/my-messages', { params })
}
