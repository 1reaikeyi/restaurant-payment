import request from '@/utils/request.js'

/**
 * 套餐管理 API
 * 包含添加套餐、套餐列表、套餐详情、更新套餐、套餐状态、删除套餐等操作
 */

// 添加套餐 - 创建套餐
export const addSetmeal = (data) => {
  return request({
    url: '/admin/setmeals',
    method: 'post',
    data
  })
}

// 套餐列表 - 分页查询套餐
export const getSetmealList = (params) => {
  return request({
    url: '/admin/setmeals',
    method: 'get',
    params
  })
}

// 套餐详情 - 查询单个套餐
export const getSetmealDetail = (id) => {
  return request({
    url: `/admin/setmeals/${id}`,
    method: 'get'
  })
}

// 更新套餐 - 修改套餐信息
export const updateSetmeal = (data) => {
  return request({
    url: '/admin/setmeals',
    method: 'put',
    data
  })
}

// 套餐状态 - 起售/停售套餐
export const updateSetmealStatus = (id, status) => {
  return request({
    url: `/admin/setmeals/${id}/status/${status}`,
    method: 'put'
  })
}

// 删除套餐 - 批量删除套餐
export const deleteSetmeal = (ids) => {
  return request({
    url: '/admin/setmeals',
    method: 'delete',
    data: ids
  })
}
