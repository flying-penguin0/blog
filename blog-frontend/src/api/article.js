import request from './request'

/**
 * 获取文章列表
 */
export const getArticleList = (params) => {
  return request({
    url: '/article/list',
    method: 'get',
    params
  })
}

/**
 * 获取文章详情
 */
export const getArticleDetail = (id) => {
  return request({
    url: `/article/${id}`,
    method: 'get'
  })
}

/**
 * 创建文章
 */
export const createArticle = (data) => {
  return request({
    url: '/article',
    method: 'post',
    data
  })
}

/**
 * 更新文章
 */
export const updateArticle = (id, data) => {
  return request({
    url: `/article/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除文章
 */
export const deleteArticle = (id) => {
  return request({
    url: `/article/${id}`,
    method: 'delete'
  })
}

/**
 * 获取热门文章
 */
export const getHotArticles = (limit = 10) => {
  return request({
    url: '/article/hot',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取分类列表（不分页）
 */
export const getCategoryList = () => {
  return request({
    url: '/category/list',
    method: 'get'
  })
}

/**
 * 分页查询分类
 */
export const getCategoryPage = (params) => {
  return request({
    url: '/category/page',
    method: 'get',
    params
  })
}

/**
 * 创建分类
 */
export const createCategory = (data) => {
  return request({
    url: '/category',
    method: 'post',
    data
  })
}

/**
 * 更新分类
 */
export const updateCategory = (id, data) => {
  return request({
    url: `/category/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除分类
 */
export const deleteCategory = (id) => {
  return request({
    url: `/category/${id}`,
    method: 'delete'
  })
}

/**
 * 获取标签列表
 */
export const getTagList = (params) => {
  return request({
    url: '/tag/list',
    method: 'get',
    params
  })
}

/**
 * 分页查询标签
 */
export const getTagPage = (params) => {
  return request({
    url: '/tag/page',
    method: 'get',
    params
  })
}

/**
 * 创建标签
 */
export const createTag = (data) => {
  return request({
    url: '/tag',
    method: 'post',
    data
  })
}

/**
 * 更新标签
 */
export const updateTag = (id, data) => {
  return request({
    url: `/tag/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除标签
 */
export const deleteTag = (id) => {
  return request({
    url: `/tag/${id}`,
    method: 'delete'
  })
}
