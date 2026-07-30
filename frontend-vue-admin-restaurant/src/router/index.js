import { createRouter, createWebHistory } from 'vue-router'
import { useEmployeeStore } from '@/stores'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {   path: '/login', component: () => import('@/views/logIn.vue') }, // 登录页
        {
            path: '/',
            component: () => import('@/layout/index.vue'), // 使用 layout 作为外层布局
            redirect: '/category',
            children: [
                // 分类管理 - 菜品/套餐分类的增删改查（对应 category API）
                { path: '/category', component: () => import('@/views/category/index.vue') },
                // 工作台 - 仪表盘，展示数据概览（对应 workspace API）
                { path: '/workspace', component: () => import('@/views/workspace/index.vue') },
                
                // 员工管理 - 包含员工列表、添加员工等功能（对应 employee API）
                { path: '/employee', component: () => import('@/views/employee/index.vue') },
                { path: '/employee/add', component: () => import('@/views/employee/addEmployee.vue') },

                // 菜品管理 - 菜品列表、添加菜品、口味配置等（对应 dish API）
                { path: '/dish', component: () => import('@/views/dish/index.vue') },
                { path: '/dish/add', component: () => import('@/views/dish/addDishtype.vue') },
                
                // 套餐管理 - 套餐列表、添加套餐（对应 setmeal API）
                { path: '/setmeal', component: () => import('@/views/setmeal/index.vue') },
                { path: '/setmeal/add', component: () => import('@/views/setmeal/addSetmeal.vue') },
                
                // 订单管理 - 订单列表、订单详情、订单状态更新（对应 order API）
                { path: '/order', component: () => import('@/views/orderDetails/index.vue') },
                
                // 店铺管理 - 营业状态设置（对应 shop API） // 消息通知 - 系统通知/消息
                { path: '/shop', component: () => import('@/views/shop/index.vue') },
            ]
        }
    ]
})

// 登录访问拦截 => 默认是直接放行的
// 如果没有token, 且访问的是非登录页，拦截到登录，其他情况正常放行
router.beforeEach((to) => {
    // 直接从localStorage中获取token，避免初始化顺序问题
    const token = localStorage.getItem('takeout') ? JSON.parse(localStorage.getItem('takeout')).token : ''
    if (!token && to.path !== '/login') return '/login'
})

export default router
