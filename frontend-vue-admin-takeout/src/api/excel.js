import request from '@/utils/request.js'

/**
 * Excel报表 API
 * 包含导出报表等操作
 */

// 导出报表 - 导出订单数据为Excel
export const exportExcel = (params) => {
  return request({
    url: '/excel/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
