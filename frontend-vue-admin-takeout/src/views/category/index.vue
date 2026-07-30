<template>
  <div class="category-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="分类名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请填写分类名称"
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
            新增分类
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 分类列表 -->
    <el-table 
      :data="tableData" 
      stripe 
      v-loading="loading" 
      class="category-table"
      :header-cell-style="{ background: '#fdf6ec', color: '#e6a23c', fontWeight: 'bold' }"
    >
      <el-table-column prop="name" label="分类名称" min-width="150" />
      <el-table-column prop="sort" label="排序" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" min-width="180" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="warning" link size="small" @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>
            修改
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row.id)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
          <el-button
            :type="row.status === 1 ? 'danger' : 'success'"
            link
            size="small"
            @click="handleStatusChange(row)"
          >
            <el-icon><Switch /></el-icon>
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!loading && tableData.length === 0" description="暂无分类数据">
      <el-button type="warning" @click="handleAdd(1)">添加第一个分类</el-button>
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
      @size-change="fetchCategoryList"
      @current-change="fetchCategoryList"
    />

    <!-- 添加/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入分类名称"
            maxlength="20"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            :min="0"
            :max="99"
            placeholder="请输入排序"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">取消</el-button>
          <el-button type="warning" @click="handleSubmit(false)">确定</el-button>
          <el-button v-if="!isEdit" type="warning" plain @click="handleSubmit(true)">
            保存并继续添加
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Switch } from '@element-plus/icons-vue'
import {
  getCategoryList,
  addCategory,
  updateCategory,
  deleteCategory,
  updateCategoryStatus
} from '@/api/category'

// 搜索表单
const searchForm = reactive({
  name: '',
  type: null
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

// 弹窗相关
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

// 弹窗标题
const dialogTitle = computed(() => {
  return isEdit.value ? '修改分类' : '新增分类'
})

// 表单数据
const formData = reactive({
  id: '',
  name: '',
  sort: 0
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '分类名称长度为2-20个字符', trigger: 'blur' },
    { pattern: /^[A-Za-z\u4e00-\u9fa5]+$/, message: '分类名称只能包含中文和英文', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ]
}

// 获取分类列表
const fetchCategoryList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined
    }
    const res = await getCategoryList(params)
    if (res?.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchCategoryList()
}

// 添加分类
const handleAdd = () => {
  isEdit.value = false
  formData.id = ''
  formData.name = ''
  formData.sort = 0
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row) => {
  isEdit.value = true
  formData.id = row.id
  formData.name = row.name
  formData.sort = row.sort
  dialogVisible.value = true
}

// 关闭弹窗
const handleCloseDialog = () => {
  dialogVisible.value = false
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async (continueAdd) => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    if (isEdit.value) {
      // 编辑模式
      await updateCategory({
        id: formData.id,
        name: formData.name,
        sort: formData.sort
      })
      ElMessage.success('分类修改成功')
      handleCloseDialog()
      fetchCategoryList()
    } else {
      // 添加模式
      await addCategory({
        name: formData.name,
        sort: formData.sort
      })
      ElMessage.success('分类添加成功')

      if (continueAdd) {
        formRef.value.resetFields()
      } else {
        handleCloseDialog()
      }
      fetchCategoryList()
    }
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 修改分类状态
const handleStatusChange = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确认${actionText}该分类?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await updateCategoryStatus(row.id, newStatus)
    ElMessage.success(`分类${actionText}成功`)
    fetchCategoryList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改状态失败:', error)
      ElMessage.error('修改状态失败')
    }
  }
}

// 删除分类
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('此操作将永久删除该分类，是否继续？', '确定删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteCategory(id)
    ElMessage.success('删除成功')
    fetchCategoryList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  fetchCategoryList()
})
</script>

<style lang="scss" scoped>
/* 黄色主题变量 */
$yellow-primary: #e6a23c;
$yellow-light: #fdf6ec;
$yellow-dark: #d49133;

.category-container {
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

    &.is-plain {
      background-color: #fff;
      color: $yellow-primary;
      border-color: $yellow-primary;

      &:hover {
        background-color: $yellow-primary;
        color: #fff;
      }
    }
  }
}

.category-table {
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;

  :deep(.el-button--warning) {
    background-color: $yellow-primary;
    border-color: $yellow-primary;
    color: #fff;

    &:hover {
      background-color: $yellow-dark;
      border-color: $yellow-dark;
    }

    &.is-plain {
      background-color: $yellow-light;
      color: $yellow-primary;
      border-color: $yellow-primary;

      &:hover {
        background-color: $yellow-primary;
        color: #fff;
      }
    }
  }
}
</style>
