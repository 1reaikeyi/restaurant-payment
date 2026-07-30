<template>
  <div class="employee-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="账号">
          <el-input
            v-model="searchForm.userName"
            placeholder="请输入账号"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="warning" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="warning" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加员工
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 员工列表 -->
    <el-table 
      :data="tableData" 
      stripe 
      v-loading="loading" 
      class="employee-table"
      :header-cell-style="{ background: '#fdf6ec', color: '#e6a23c', fontWeight: 'bold' }"
    >
      <el-table-column prop="name" label="员工姓名" min-width="100" />
      <el-table-column prop="userName" label="账号" min-width="120" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column label="性别" width="80" align="center">
        <template #default="{ row }">
          {{ row.sex === '1' ? '男' : '女' }}
        </template>
      </el-table-column>
      <el-table-column label="账号状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" min-width="180" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="warning" link size="small" @click="handleView(row)">
            <el-icon><View /></el-icon>
            查看
          </el-button>
          <el-button 
            type="warning" 
            link 
            size="small" 
            @click="handleEdit(row)"
            :disabled="row.userName === 'admin'"
          >
            <el-icon><Edit /></el-icon>
            修改
          </el-button>
          <el-button
            :type="row.status === 1 ? 'danger' : 'success'"
            link
            size="small"
            @click="handleStatusChange(row)"
            :disabled="row.userName === 'admin'"
          >
            <el-icon><Switch /></el-icon>
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!loading && tableData.length === 0" description="暂无员工数据">
      <el-button type="warning" @click="handleAdd">添加第一个员工</el-button>
    </el-empty>

    <!-- 分页 -->
    <el-pagination
      v-if="total > 0"
      class="pagination"
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :page-sizes="[10, 20, 30, 40]"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      @size-change="fetchEmployeeList"
      @current-change="fetchEmployeeList"
    />

    <!-- 员工详情弹窗 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="员工详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="员工姓名">{{ viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ viewData.userName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ viewData.phone }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ viewData.sex === '1' ? '男' : '女' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ viewData.idNumber }}</el-descriptions-item>
        <el-descriptions-item label="账号状态">
          <el-tag :type="viewData.status === 1 ? 'success' : 'danger'">
            {{ viewData.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ viewData.updateTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, View, Edit, Switch } from '@element-plus/icons-vue'
import { getEmployeeList, updateEmployeeStatus, addEmployee, updateEmployee } from '@/api/employee'

// 搜索表单
const searchForm = reactive({
  userName: ''
})

// 分页参数
const pagination = reactive({
  page: 1,
  pageSize: 10
})

// 表格数据
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 详情弹窗
const viewDialogVisible = ref(false)
const viewData = reactive({
  name: '',
  userName: '',
  phone: '',
  sex: '',
  idNumber: '',
  status: 1,
  createTime: '',
  updateTime: ''
})

// 获取员工列表
const fetchEmployeeList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      userName: searchForm.userName || undefined
    }
    const res = await getEmployeeList(params)
    if (res?.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchEmployeeList()
}

// 查看员工详情
const handleView = (row) => {
  viewData.name = row.name || ''
  viewData.userName = row.userName || ''
  viewData.phone = row.phone || ''
  viewData.sex = row.sex || ''
  viewData.idNumber = row.idNumber || ''
  viewData.status = row.status || 1
  viewData.createTime = row.createTime || ''
  viewData.updateTime = row.updateTime || ''
  viewDialogVisible.value = true
}

// 添加员工
const handleAdd = () => {
  ElMessageBox.prompt('请输入员工姓名', '添加员工', {
    confirmButtonText: '下一步',
    cancelButtonText: '取消',
    inputPattern: /^.{2,20}$/,
    inputErrorMessage: '姓名长度为2-20个字符'
  }).then(({ value: name }) => {
    ElMessageBox.prompt('请输入账号', '添加员工', {
      confirmButtonText: '下一步',
      cancelButtonText: '取消',
      inputPattern: /^.{3,20}$/,
      inputErrorMessage: '账号长度为3-20个字符'
    }).then(({ value: userName }) => {
      ElMessageBox.prompt('请输入手机号', '添加员工', {
        confirmButtonText: '下一步',
        cancelButtonText: '取消',
        inputPattern: /^1[3-9]\d{9}$/,
        inputErrorMessage: '请输入正确的手机号'
      }).then(({ value: phone }) => {
        ElMessageBox.prompt('请输入密码（默认密码：123456）', '设置密码', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '留空则使用默认密码',
          inputPattern: /^.{6,20}$/,
          inputErrorMessage: '密码长度为6-20个字符'
        }).then(async ({ value: password }) => {
          try {
            const data = {
              name: name,
              userName: userName,
              phone: phone,
              password: password || '123456',
              sex: '1',
              idNumber: ''
            }
            await addEmployee(data)
            ElMessage.success('员工添加成功')
            fetchEmployeeList()
          } catch (error) {
            console.error('添加失败:', error)
            ElMessage.error('添加失败：' + (error.message || '未知错误'))
          }
        }).catch(() => {})
      }).catch(() => {})
    }).catch(() => {})
  }).catch(() => {})
}

// 编辑员工
const handleEdit = (row) => {
  if (row.userName === 'admin') {
    ElMessage.warning('不能修改管理员账号')
    return
  }
  ElMessageBox.prompt('请输入新的员工姓名', '修改员工', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: row.name,
    inputPattern: /^.{2,20}$/,
    inputErrorMessage: '姓名长度为2-20个字符'
  }).then(async ({ value: name }) => {
    try {
      await updateEmployee({
        id: row.id,
        name: name,
        userName: row.userName,
        phone: row.phone,
        sex: row.sex,
        idNumber: row.idNumber,
        status: row.status
      })
      ElMessage.success('员工信息修改成功')
      fetchEmployeeList()
    } catch (error) {
      console.error('修改失败:', error)
      ElMessage.error('修改失败：' + (error.message || '未知错误'))
    }
  }).catch(() => {})
}

// 修改员工状态
const handleStatusChange = async (row) => {
  if (row.userName === 'admin') {
    ElMessage.warning('不能修改管理员账号状态')
    return
  }

  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确认${actionText}该账号?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await updateEmployeeStatus(row.id, newStatus)
    ElMessage.success(`账号${actionText}成功`)
    fetchEmployeeList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改状态失败:', error)
      ElMessage.error('修改状态失败')
    }
  }
}

// 初始化
onMounted(() => {
  fetchEmployeeList()
})
</script>

<style lang="scss" scoped>
/* 黄色主题变量 */
$yellow-primary: #e6a23c;
$yellow-light: #fdf6ec;
$yellow-dark: #d49133;

.employee-container {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  min-height: calc(100vh - 120px);
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: $yellow-light;
  border-radius: 4px;

  .el-form-item {
    margin-bottom: 0;
  }

  :deep(.el-button--warning) {
    background-color: $yellow-primary;
    border-color: $yellow-primary;
    color: #fff;

    &:hover {
      background-color: $yellow-dark;
      border-color: $yellow-dark;
    }
  }
}

.employee-table {
  width: 100%;
  margin-bottom: 20px;

  :deep(.el-table__header-wrapper) {
    th {
      background-color: $yellow-light !important;
      color: $yellow-dark !important;
      font-weight: bold;
    }
  }

  :deep(.el-tag--warning) {
    background-color: $yellow-light;
    color: $yellow-dark;
    border-color: $yellow-primary;
  }

  :deep(.el-button--warning) {
    color: $yellow-primary;

    &:hover {
      color: $yellow-dark;
    }
  }
}

.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 20px 0;

  :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
    background-color: $yellow-primary;
  }
  
  :deep(.el-pagination.is-background .btn-prev:hover),
  :deep(.el-pagination.is-background .btn-next:hover) {
    color: $yellow-primary;
  }
}
</style>
