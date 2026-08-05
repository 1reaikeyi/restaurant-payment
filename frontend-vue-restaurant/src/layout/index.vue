<script setup>
// 导入 Element Plus 图标组件
import {
  Management,
  Promotion,
  UserFilled,
  User,
  Crop,
  EditPen,
  CaretBottom
} from '@element-plus/icons-vue'
// 导入默认头像图片
import avatar from '@/assets/login/avatar.png'
// 导入员工状态管理
import { useEmployeeStore } from '@/stores/index.js'
// 导入 Vue 生命周期钩子
import { onMounted } from 'vue'
// 导入路由
import { useRouter } from 'vue-router'

// 初始化员工状态和路由实例
const employeeStore = useEmployeeStore()
// 组件挂载时获取员工信息
onMounted(() => {
  employeeStore.getUser()
})
const router = useRouter()
// 处理下拉菜单命令
const handleCommand = async (key) => {
  router.push(`/employee/${key}`)
}
</script>

<template>
  <el-container class="layout-container">
    <el-aside width="200px">

      <div class="el-dropdown__box">
        <el-avatar :src="employeeStore.user?.userPic || avatar" :size="60" />
      </div>

      <!-- Logo区域 -->
      <div class="el-aside__logo"></div>

      <!-- 侧边栏菜单 -->
      <el-menu active-text-color="#ffd04b" background-color="#232323" :default-active="$route.path" text-color="#fff" router>

        <el-menu-item index="/category"><el-icon><Grid /></el-icon><span>菜单</span></el-menu-item>

        <el-menu-item index="/employee"><el-icon><User /></el-icon><span>菜品管理</span></el-menu-item>

        <el-menu-item index="/workspace"><el-icon><HomeFilled /></el-icon><span>套餐管理</span></el-menu-item>

        <el-menu-item index="/dish"><el-icon><KnifeFork /></el-icon><span>订单管理</span></el-menu-item>

        <el-menu-item index="/setmeal"><el-icon><Present /></el-icon><span>工作台</span></el-menu-item>

        <el-menu-item index="/order"><el-icon><ShoppingCart /></el-icon><span>员工管理</span></el-menu-item>

        <el-menu-item index="/shop"><el-icon><Shop /></el-icon><span>店铺管理</span></el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <!-- 头部标题 -->
        <div>小餐馆==> 当前员工昵称: <strong>{{ employeeStore.user?.userName || '员工' }}</strong></div>
        
        <!-- 用户下拉菜单 -->
        <el-dropdown placement="bottom-end" @command="handleCommand">
          <span class="el-dropdown__box">
            <el-avatar :src="employeeStore.user?.userPic || avatar" :size="60" />
            <el-icon><CaretBottom /></el-icon>
          </span>
          
          <!-- 下拉菜单内容 -->
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile" :icon="User">基本资料</el-dropdown-item>
              <el-dropdown-item command="avatar" :icon="User">更换头像</el-dropdown-item>
              <el-dropdown-item command="password" :icon="User">重置密码</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      
      <!-- 主内容区域 -->
      <el-main>
        <router-view></router-view>
      </el-main>
      
      <!-- 底部 -->
      <el-footer>餐馆</el-footer>
    </el-container>
  </el-container>
</template>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  width: 100%;
  margin: 0;
  padding: 0;
  position: absolute;
  left: 0;
  top: 0;
  .el-aside {
    background-color: #232323;
    // 侧边栏头像容器：水平居中，与下方 logo/菜单对齐
    .el-dropdown__box {
      display: flex;
      justify-content: center; // 水平居中
      padding: 10px 0; // 上下留白，与下方元素保持间距
    }
    &__logo {
      height: 120px;
      background: url('@/assets/login/layout.png') no-repeat center / 190px auto;
    }
    .el-menu {
      border-right: none;
    }
  }
  .el-header {
    background-color: #fff;
    display: flex;
    align-items: center;
    justify-content: space-between;
    .el-dropdown__box {
      display: flex;
      align-items: center;
      .el-icon {
        color: #999;
        margin-left: 10px;
      }

      &:active,
      &:focus {
        outline: none;
      }
    }
  }
  .el-footer {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: #666;
  }
}
</style>
