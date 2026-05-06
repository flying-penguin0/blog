import request from './request'

/**
 * 分页查询敏感词
 */
export const getSensitiveWordPage = (params) => {
  return request.get('/sensitive-words/page', { params })
}

/**
 * 创建敏感词
 */
export const createSensitiveWord = (data) => {
  return request.post('/sensitive-words', data)
}

/**
 * 更新敏感词
 */
export const updateSensitiveWord = (id, data) => {
  return request.put(`/sensitive-words/${id}`, data)
}

/**
 * 删除敏感词
 */
export const deleteSensitiveWord = (id) => {
  return request.delete(`/sensitive-words/${id}`)
}

/**
 * 批量删除敏感词
 */
export const batchDeleteSensitiveWords = (ids) => {
  return request.delete('/sensitive-words/batch', { data: ids })
}

/**
 * 刷新敏感词缓存
 */
export const refreshSensitiveWordCache = () => {
  return request.post('/sensitive-words/refresh')
}

/**
 * 获取所有启用的敏感词（用于前端过滤）
 */
export const getEnabledSensitiveWords = () => {
  return request.get('/sensitive-words/enabled')
}

/**
 * 检测文本是否包含敏感词
 */
export const checkSensitiveWord = (content) => {
  return request.post('/sensitive-words/check', content, {
    headers: {
      'Content-Type': 'text/plain'
    }
  })
}
