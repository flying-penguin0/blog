import request from './request'

/**
 * 获取图片验证码
 */
export const getCaptcha = () => {
  return request.get('/captcha')
}

/**
 * 发送邮箱验证码
 */
export const sendEmailCode = (username, email) => {
  return request.post('/captcha/email', null, {
    params: { username, email }
  })
}
