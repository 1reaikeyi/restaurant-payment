import request from '@/utils/request.js'

/**
 * 菜品管理 API
 * 包含添加菜品、菜品列表、菜品详情、分类菜品、更新菜品、菜品状态、删除菜品等操作
 */

// 添加菜品 - 创建菜品（含口味配置）
export const addDish = (data) => {
  return request({
    url: '/admin/dishes',
    method: 'post',
    data
  })
}

// 菜品列表 - 分页查询菜品
export const getDishList = (params) => {
  return request({
    url: '/admin/dishes',
    method: 'get',
    params
  })
}

// 菜品详情 - 查询单个菜品
export const getDishDetail = (id) => {
  return request({
    url: `/admin/dishes/${id}`,
    method: 'get'
  })
}

// 分类菜品 - 按分类查询菜品
export const getDishByCategory = (categoryId) => {
  return request({
    url: `/admin/dishes/category/${categoryId}`,
    method: 'get'
  })
}

// 更新菜品 - 修改菜品信息
export const updateDish = (data) => {
  return request({
    url: '/admin/dishes',
    method: 'put',
    data
  })
}

// 菜品状态 - 起售/停售菜品
export const updateDishStatus = (id, status) => {
  return request({
    url: `/admin/dishes/${id}/status/${status}`,
    method: 'put'
  })
}

// 删除菜品 - 批量删除菜品
export const deleteDish = (ids) => {
  return request({
    url: '/admin/dishes',
    method: 'delete',
    data: ids
  })
}
