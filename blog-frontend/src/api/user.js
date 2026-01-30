import request from './request'

/**
 * 用户登录
 */
export const login = (data) => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 */
export const register = (data) => {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 获取用户信息
 */
export const getUserInfo = () => {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 更新用户信息
 */
export const updateUserInfo = (data) => {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

/**
 * 获取所有用户列表（管理员）
 */
export const getAllUsers = (params) => {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

// 别名，为了兼容性
export const getUserList = getAllUsers

/**
 * 更新用户（管理员）
 */
export const updateUser = (id, data) => {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data
  })
}

/**
 * 切换用户状态（管理员）
 */
export const toggleUserStatus = (id) => {
  return request({
    url: `/user/${id}/status`,
    method: 'put'
  })
}

/**
 * 删除用户（管理员）
 */
export const deleteUser = (id) => {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

/**
 * 获取用户统计信息
 */
export const getUserStats = (userId) => {
  return request({
    url: `/user/${userId}/stats`,
    method: 'get'
  })
}

/**
 * 修改密码
 */
export const changePassword = (data) => {
  return request({
    url: '/user/change-password',
    method: 'put',
    data
  })
}
