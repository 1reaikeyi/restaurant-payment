<template>
  <div class="dish-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="菜品名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请填写菜品名称"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="菜品分类">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="请选择"
            clearable
            @clear="handleSearch"
          >
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="售卖状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择"
            clearable
            @clear="handleSearch"
          >
            <el-option label="启售" :value="1" />
            <el-option label="停售" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="warning" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="danger" link @click="handleBatchDelete">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
          <el-button type="warning" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新建菜品
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 菜品列表 -->
    <el-table
      :data="tableData"
      stripe
      v-loading="loading"
      class="dish-table"
      @selection-change="handleSelectionChange"
      :header-cell-style="{ background: '#fdf6ec', color: '#e6a23c', fontWeight: 'bold' }"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column prop="name" label="菜品名称" min-width="120" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <!-- 图片路径直接从后端返回，数据库存储值如 /云/img/1.png -->
          <el-image
            :src="row.image"
            :preview-src-list="[row.image]"
            fit="cover"
            class="dish-image"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="菜品分类" min-width="100" />
      <el-table-column label="售价" min-width="100">
        <template #default="{ row }">
          <span class="price">￥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
      <el-table-column label="售卖状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启售' : '停售' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" min-width="180" />
      <el-table-column label="操作" width="220" align="center" fixed="right">
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
            {{ row.status === 1 ? '停售' : '启售' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!loading && tableData.length === 0" description="暂无菜品数据">
      <el-button type="warning" @click="handleAdd">添加第一个菜品</el-button>
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
      @size-change="fetchDishList"
      @current-change="fetchDishList"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Switch, Picture } from '@element-plus/icons-vue'
import { getDishList, deleteDish, updateDishStatus } from '@/api/dish'
import { getCategoryById } from '@/api/category'

// 路由实例
const router = useRouter()

// 搜索表单
const searchForm = reactive({
  name: '',
  categoryId: null,
  status: null
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

// 分类列表
const categoryList = ref([])

// 选中的菜品
const selectedIds = ref([])

// 获取全部分类列表（按 id 1~10 轮询）
const fetchCategoryList = async () => {
  try {
    const list = []
    for (let id = 1; id <= 10; id++) {
      const res = await getCategoryById(id)
      if (res?.data) {
        list.push(res.data)
      }
    }
    categoryList.value = list
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取菜品列表
const fetchDishList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      categoryId: searchForm.categoryId || undefined,
      status: searchForm.status
    }
    const res = await getDishList(params)
    if (res?.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取菜品列表失败:', error)
    ElMessage.error('获取菜品列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchDishList()
}

// 添加菜品
const handleAdd = () => {
  router.push('/dish/add')
}

// 编辑菜品
const handleEdit = (row) => {
  router.push({ path: '/dish/add', query: { id: row.id } })
}

// 修改菜品状态
const handleStatusChange = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启售' : '停售'

  try {
    await ElMessageBox.confirm(`确认${actionText}该菜品?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await updateDishStatus(row.id, newStatus)
    ElMessage.success(`菜品${actionText}成功`)
    fetchDishList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改状态失败:', error)
      ElMessage.error('修改状态失败')
    }
  }
}

// 删除菜品
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该菜品?', '确定删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteDish([id])
    ElMessage.success('删除成功')
    fetchDishList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的菜品')
    return
  }

  try {
    await ElMessageBox.confirm('确定删除选中的菜品?', '确定删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteDish(selectedIds.value)
    ElMessage.success('删除成功')
    selectedIds.value = []
    fetchDishList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 初始化
onMounted(() => {
  fetchCategoryList()
  fetchDishList()
})
</script>

<style lang="scss" scoped>
/* 黄色主题变量 */
$yellow-primary: #e6a23c;
$yellow-light: #fdf6ec;
$yellow-dark: #d49133;

.dish-container {
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

.dish-table {
  width: 100%;
  margin-bottom: 20px;

  :deep(.el-table__header-wrapper) {
    th {
      background-color: $yellow-light !important;
      color: $yellow-dark !important;
      font-weight: bold;
    }
  }

  .dish-image {
    width: 80px;
    height: 50px;
    border-radius: 4px;
  }

  .image-placeholder {
    width: 80px;
    height: 50px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #909399;
  }

  .price {
    color: #f56c6c;
    font-weight: 500;
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
