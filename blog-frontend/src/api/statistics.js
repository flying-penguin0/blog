import request from './request'

/**
 * 获取统计数据
 */
export const getStatistics = () => {
  return request({
    url: '/statistics',
    method: 'get'
  })
}
