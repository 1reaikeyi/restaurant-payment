<template>
  <div class="add-employee-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="page-title">{{ isEdit ? '修改员工信息' : '添加员工' }}</span>
        </template>
      </el-page-header>
    </div>

    <!-- 表单区域 -->
    <div class="form-container">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        class="employee-form"
      >
        <el-form-item label="账号" prop="userName">
          <el-input
            v-model="formData.userName"
            placeholder="请输入账号"
            maxlength="20"
            :disabled="isEdit"
          />
        </el-form-item>

        <el-form-item label="员工姓名" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入员工姓名"
            maxlength="12"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="formData.sex">
            <el-radio label="1">男</el-radio>
            <el-radio label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="身份证号" prop="idNumber">
          <el-input
            v-model="formData.idNumber"
            placeholder="请输入身份证号"
            maxlength="18"
          />
        </el-form-item>

        <el-form-item class="form-buttons">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" @click="handleSubmit(false)">保存</el-button>
          <el-button v-if="!isEdit" type="primary" @click="handleSubmit(true)">
            保存并继续添加
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getEmployeeDetail, addEmployee, updateEmployee } from '@/api/aimin/employee.js'

// 路由实例
const router = useRouter()
const route = useRoute()

// 表单引用
const formRef = ref(null)

// 是否为编辑模式
const isEdit = computed(() => !!route.query.id)

// 表单数据
const formData = reactive({
  id: '',
  userName: '',
  name: '',
  phone: '',
  sex: '1',
  idNumber: ''
})

// 手机号验证
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

// 身份证号验证
const validateIdNumber = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入身份证号'))
  } else if (!/^\d{17}[\dXx]$/.test(value)) {
    callback(new Error('请输入正确的身份证号'))
  } else {
    callback()
  }
}

// 表单验证规则
const rules = {
  userName: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-z0-9]+$/i, message: '账号只能包含字母和数字', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' },
    { max: 12, message: '姓名最多12个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  idNumber: [
    { required: true, validator: validateIdNumber, trigger: 'blur' }
  ]
}

// 返回列表页
const goBack = () => {
  router.push('/employee')
}

// 获取员工详情
const fetchEmployeeDetail = async () => {
  try {
    const res = await getEmployeeDetail(route.query.id)
    if (res?.data) {
      Object.assign(formData, {
        id: res.data.id,
        userName: res.data.userName,
        name: res.data.name,
        phone: res.data.phone,
        sex: String(res.data.sex),
        idNumber: res.data.idNumber
      })
    }
  } catch (error) {
    console.error('获取员工详情失败:', error)
    ElMessage.error('获取员工详情失败')
  }
}

// 提交表单
const handleSubmit = async (continueAdd) => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    const params = {
      ...formData,
      sex: formData.sex
    }

    if (isEdit.value) {
      // 编辑模式
      await updateEmployee(params)
      ElMessage.success('员工信息修改成功')
      goBack()
    } else {
      // 添加模式
      await addEmployee(params)
      ElMessage.success('员工添加成功')

      if (continueAdd) {
        // 继续添加，重置表单
        formRef.value.resetFields()
      } else {
        goBack()
      }
    }
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 初始化
onMounted(() => {
  if (isEdit.value) {
    fetchEmployeeDetail()
  }
})
</script>

<style lang="scss" scoped>
/* 系统色板 - 严格只使用以下 9 种颜色及其透明度变体 */
$sys-blue: #0A84FF;      // 系统蓝 - 主题主色
$sys-indigo: #5E5CE6;    // 系统靛蓝 - 深色/文字

.add-employee-container {
  padding: 20px;
  /* 容器背景使用系统蓝极浅透明度 */
  background: rgba(10, 132, 255, 0.04);
  border-radius: 4px;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 20px;
  /* 分隔线使用系统蓝半透明 */
  border-bottom: 1px solid rgba(10, 132, 255, 0.2);
}

.page-title {
  font-size: 16px;
  font-weight: 500;
  /* 标题文字使用系统靛蓝 */
  color: $sys-indigo;
}

.form-container {
  max-width: 600px;
  padding: 20px 0;
}

.employee-form {
  .el-input {
    width: 300px;
  }
}

.form-buttons {
  margin-top: 30px;
  padding-top: 20px;
  /* 顶部分隔线使用系统蓝半透明 */
  border-top: 1px solid rgba(10, 132, 255, 0.2);
}
</style>
