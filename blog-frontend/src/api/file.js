import request from './request'

/**
 * 上传文件
 */
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除文件
 */
export const deleteFile = (fileName) => {
  return request({
    url: '/file',
    method: 'delete',
    params: { fileName }
  })
}
