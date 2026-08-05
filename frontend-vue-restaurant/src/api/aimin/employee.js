import request from '@/utils/request.js'

/**
 * 员工管理 API
 * 包含员工注册、登录、退出、添加、列表、详情、更新、状态管理、修改密码、删除等操作
 */

// 员工注册 - 创建员工账户
export const registerEmployee = (data) => {
  return request({
    url: '/admin/employees/register',
    method: 'post',
    data
  })
}

// 员工登录 - 管理员身份验证
export const loginEmployee = (data) => {
  return request({
    url: '/admin/employees/login',
    method: 'post',
    data
  })
}

// 员工退出 - 注销管理员登录
export const logoutEmployee = () => {
  return request({
    url: '/admin/employees/logout',
    method: 'post'
  })
}

// 添加员工 - 创建新员工账号
export const addEmployee = (data) => {
  return request({
    url: '/admin/employees',
    method: 'post',
    data
  })
}

// 员工列表 - 分页查询员工
export const getEmployeeList = (params) => {
  return request({
    url: '/admin/employees',
    method: 'get',
    params
  })
}

// 员工详情 - 查询单个员工
export const getEmployeeDetail = (id) => {
  return request({
    url: `/admin/employees/${id}`,
    method: 'get'
  })
}

// 更新员工 - 修改员工信息
export const updateEmployee = (data) => {
  return request({
    url: '/admin/employees',
    method: 'put',
    data
  })
}

// 状态管理 - 启用/禁用员工
export const updateEmployeeStatus = (id, status) => {
  return request({
    url: `/admin/employees/${id}/status/${status}`,
    method: 'put'
  })
}

// 修改密码 - 更新员工密码
export const updateEmployeePassword = (data) => {
  return request({
    url: '/admin/employees/password',
    method: 'put',
    data
  })
}

// 删除员工 - 删除单个员工
export const deleteEmployee = (id) => {
  return request({
    url: `/admin/employees/${id}`,
    method: 'delete'
  })
}

// 批量删除 - 批量删除员工
export const batchDeleteEmployees = (ids) => {
  return request({
    url: '/admin/employees',
    method: 'delete',
    data: ids
  })
}
