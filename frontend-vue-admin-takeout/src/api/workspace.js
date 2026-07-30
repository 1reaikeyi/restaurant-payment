import request from '@/utils/request.js'

/**
 * 工作台统计 API
 * 包含菜品概览、套餐概览、订单概览、今日订单、菜品统计、套餐统计、TOP菜品、TOP套餐等操作
 * 所有接口均使用 POST 方式
 */

// 菜品概览 - 获取菜品在售/停售数量
export const getDishOverview = () => {
  return request({
    url: '/admin/workspace/overview/dish',
    method: 'post'
  })
}

// 套餐概览 - 获取套餐在售/停售数量
export const getSetmealOverview = () => {
  return request({
    url: '/admin/workspace/overview/setmeal',
    method: 'post'
  })
}

// 订单概览 - 获取订单统计（总数/完成/退款/取消）
export const getOrderOverview = () => {
  return request({
    url: '/admin/workspace/overview/order',
    method: 'post'
  })
}

// 今日订单 - 获取今日订单数据统计
export const getTodayOrder = () => {
  return request({
    url: '/admin/workspace/orderDay',
    method: 'post'
  })
}

// 菜品统计 - 获取所有菜品销量排行
export const getDishScore = () => {
  return request({
    url: '/admin/workspace/dishScole',
    method: 'post'
  })
}

// 套餐统计 - 获取所有套餐销量排行
export const getSetmealScore = () => {
  return request({
    url: '/admin/workspace/setmealScole',
    method: 'post'
  })
}

// TOP菜品 - 获取销量前5菜品
export const getTopDish = () => {
  return request({
    url: '/admin/workspace/statisticsTopDish',
    method: 'post'
  })
}

// TOP套餐 - 获取销量前5套餐
export const getTopSetmeal = () => {
  return request({
    url: '/admin/workspace/statisticsTopSetmeal',
    method: 'post'
  })
}