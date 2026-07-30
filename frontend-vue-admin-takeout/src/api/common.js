import request from '@/utils/request.js'

/**
 * 公共 API
 * 包含文件上传等通用操作
 */

// 文件上传
export const uploadFile = (data) => {
  return request({
    url: '/admin/common/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 文件下载
export const downloadFile = (name) => {
  return request({
    url: `/admin/common/download?name=${name}`,
    method: 'get',
    responseType: 'blob'
  })
}
