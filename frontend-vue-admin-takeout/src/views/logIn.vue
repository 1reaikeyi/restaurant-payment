<template>
  <div class="login">
    <div class="login-box">
      <img src="../assets/login/login-l.png" alt="" />
      <div class="login-form">
        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules">
          <div class="login-form-title">
            <img
                src="../assets/login/icon_logo.png"
                style="width: 149px; height: 38px"
                alt=""
            />
          </div>
          <el-form-item prop="userName">
            <el-input
                v-model="loginForm.userName"
                type="text"
                auto-complete="off"
                placeholder="账号"
                prefix-icon="el-icon-user"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                prefix-icon="el-icon-lock"
                @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item style="width: 100%">
            <el-button
                :loading="loadLogIn"
                class="login-btn"
                size="medium"
                type="primary"
                style="width: 100%"
                @click="handleLogin"
            >
              <span v-if="!loadLogIn">登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginEmployee } from '@/api/employee'
import { useEmployeeStore } from '@/stores'

// 加载状态
const loadLogIn = ref(false)

// 登录表单
const loginForm = reactive({
  userName: '',
  password: '',
})

const isRegister = ref(false)
// 修复：reactive 不能用 .value 赋值
watch(isRegister, () => {
  loginForm.userName = ''
  loginForm.password = ''
})

// 表单实例
const loginFormRef = ref()

// 表单校验规则
const loginRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码必须在6位以上', trigger: 'blur' }
  ]
}

const router = useRouter()
const jwt = useEmployeeStore()

// 登录方法 - 核心修复：去掉 loginForm.value
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loadLogIn.value = true

    // 重点：直接传 loginForm，不要 .value
    const res = await loginEmployee(loginForm)
    jwt.setToken(res.data)
    ElMessage.success('登录成功')
    router.push('/')

  } catch (error) {
    console.error('登录失败', error)
    ElMessage.error('登录失败，请重试')
  } finally {
    loadLogIn.value = false
  }
}
</script>

<style lang="scss" scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #333;
}

.login-box {
  width: 1000px;
  height: 474.38px;
  border-radius: 8px;
  display: flex;
  img {
    width: 60%;
    height: auto;
  }
}

.title {
  margin: 0px auto 10px auto;
  text-align: left;
  color: #707070;
}

.login-form {
  background: #ffffff;
  width: 40%;
  border-radius: 0px 8px 8px 0px;
  display: flex;
  justify-content: center;
  align-items: center;
  .el-form {
    width: 214px;
    height: 307px;
  }
  .el-form-item {
    margin-bottom: 30px;
  }
  .el-form-item.is-error .el-input__inner {
    border: 0 !important;
    border-bottom: 1px solid #fd7065 !important;
    background: #fff !important;
  }
  .input-icon {
    height: 32px;
    width: 18px;
    margin-left: -2px;
  }
  .el-input__inner {
    border: 0;
    border-bottom: 1px solid #e9e9e8;
    border-radius: 0;
    font-size: 12px;
    font-weight: 400;
    color: #333333;
    height: 32px;
    line-height: 32px;
  }
  .el-input__prefix {
    left: 0;
  }
  .el-input--prefix .el-input__inner {
    padding-left: 26px;
  }
  .el-input__inner::placeholder {
    color: #aeb5c4;
  }
  .el-form-item--medium .el-form-item__content {
    line-height: 32px;
  }
  .el-input--medium .el-input__icon {
    line-height: 32px;
  }
}

.login-btn {
  border-radius: 17px;
  padding: 11px 20px !important;
  margin-top: 10px;
  font-weight: 500;
  font-size: 12px;
  border: 0;
  font-weight: 500;
  color: #333333;
  background-color: #ffc200;
  &:hover,
  &:focus {
    background-color: #ffc200;
    color: #ffffff;
  }
}
.login-form-title {
  height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 40px;
  .title-label {
    font-weight: 500;
    font-size: 20px;
    color: #333333;
    margin-left: 10px;
  }
}
</style>