import request from '@/utils/request.js'

/**
 * 订单管理 API
 * 包含订单详情、订单统计、条件查询、确认订单、取消订单、派送订单、完成订单等操作
 */
// 获取所有订单
export const readAllOrders = () => {
  return request({
    url: `/admin/orders/all`,
    method: 'get'
  })
}

// 订单详情 - 查询订单详情
export const getOrderDetail = (id) => {
  return request({
    url: `/admin/orders/get/${id}`,
    method: 'get'
  })
}

// 订单统计 - 统计待确认/配送中/已完成订单
export const getOrderStatistics = () => {
  return request({
    url: '/admin/orders/statistics',
    method: 'get'
  })
}

// 条件查询 - 模糊分页查询订单
// 注意：后端只支持 phone 查询，不支持 number 和 status 参数
export const searchOrders = (params) => {
  return request({
    url: '/admin/orders/conditionSearch',
    method: 'get',
    params: {
      page: params.page,
      pageSize: params.pageSize,
      phone: params.phone || undefined
      // 注意：后端不支持 number 和 status 参数，已移除
    }
  })
}

// 确认订单 - 商家确认接单
export const confirmOrder = (orderId) => {
  return request({
    url: `/admin/orders/confirm/${orderId}`,
    method: 'put'
  })
}

// 取消订单 - 商家取消订单
export const cancelOrder = (id) => {
  return request({
    url: `/admin/orders/cancel/${id}`,
    method: 'put'
  })
}

// 派送订单 - 标记订单开始配送
export const deliveryOrder = (id) => {
  return request({
    url: `/admin/orders/delivery/${id}`,
    method: 'put'
  })
}

// 完成订单 - 标记订单已完成
export const completeOrder = (id) => {
  return request({
    url: `/admin/orders/complete/${id}`,
    method: 'put'
  })
}
