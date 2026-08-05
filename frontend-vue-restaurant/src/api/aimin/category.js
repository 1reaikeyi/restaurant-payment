import request from '@/utils/request.js'

/**
 * 分类管理 API
 * 包含添加分类、分类列表、类型查询、更新分类、分类状态、删除分类等操作
 */

// 添加分类 - 创建菜品/套餐分类
export const addCategory = (data) => {
  return request({
    url: '/admin/categories',
    method: 'post',
    data
  })
}

// 分类列表 - 分页查询分类
export const getCategoryList = (params) => {
  return request({
    url: '/admin/categories',
    method: 'get',
    params
  })
}
// ID查询 - 按ID查询分类
export const getCategoryById = (id) => {
  return request({
    url: `/admin/categories/id/${id}`,
    method: 'get'
  })
}
// 类型查询 - 按类型查询分类
export const getCategoryByType = (type) => {
  return request({
    url: `/admin/categories/type/${type}`,
    method: 'get'
  })
}

// 更新分类 - 修改分类信息
export const updateCategory = (data) => {
  return request({
    url: '/admin/categories',
    method: 'put',
    data
  })
}

// 分类状态 - 启用/禁用分类
export const updateCategoryStatus = (id, status) => {
  return request({
    url: `/admin/categories/${id}/status/${status}`,
    method: 'put'
  })
}

// 删除分类 - 删除指定分类
export const deleteCategory = (id) => {
  return request({
    url: `/admin/categories/${id}`,
    method: 'delete'
  })
}
