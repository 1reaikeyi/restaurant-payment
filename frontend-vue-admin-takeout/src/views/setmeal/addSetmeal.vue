<template>
  <div class="add-setmeal-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="page-title">{{ isEdit ? '修改套餐' : '添加套餐' }}</span>
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
        class="setmeal-form"
      >
        <el-form-item label="套餐名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请填写套餐名称"
            maxlength="14"
          />
        </el-form-item>

        <el-form-item label="套餐分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择套餐分类">
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="Number(item.id)"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="套餐价格" prop="price">
          <el-input
            v-model="formData.price"
            placeholder="请设置套餐价格"
            type="number"
          >
            <template #append>元</template>
          </el-input>
        </el-form-item>

        <!-- 套餐菜品 -->
        <el-form-item label="套餐菜品" required>
          <div class="dish-box">
            <el-button
              v-if="dishTable.length === 0"
              type="warning"
              plain
              @click="openAddDishDialog"
            >
              + 添加菜品
            </el-button>

            <div v-else class="dish-content">
              <el-button
                type="warning"
                plain
                class="add-dish-btn"
                @click="openAddDishDialog"
              >
                + 添加菜品
              </el-button>

              <el-table :data="dishTable" class="dish-table">
                <el-table-column prop="name" label="名称" align="center" />
                <el-table-column label="原价" align="center">
                  <template #default="{ row }">
                    ￥{{ row.price }}
                  </template>
                </el-table-column>
                <el-table-column label="份数" align="center" width="150">
                  <template #default="{ row }">
                    <el-input-number
                      v-model="row.copies"
                      :min="1"
                      :max="99"
                      size="small"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="操作" align="center" width="100">
                  <template #default="{ $index }">
                    <el-button
                      type="danger"
                      link
                      size="small"
                      @click="removeDish($index)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-form-item>

        <!-- 图片上传 -->
        <el-form-item label="套餐图片" prop="image">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :http-request="handleImageUpload"
          >
            <el-image
              v-if="formData.image"
              :src="formData.image"
              class="setmeal-image"
              fit="cover"
            />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">
            图片大小不超过2M<br>
            仅能上传 PNG JPEG JPG 类型图片<br>
            建议上传200*200或300*300尺寸的图片
          </div>
        </el-form-item>

        <!-- 套餐描述 -->
        <el-form-item label="套餐描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            maxlength="200"
            placeholder="套餐描述，最长200字"
            show-word-limit
          />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item class="form-buttons">
          <el-button @click="goBack">取消</el-button>
          <el-button type="warning" @click="handleSubmit(false)">保存</el-button>
          <el-button v-if="!isEdit" type="warning" plain @click="handleSubmit(true)">
            保存并继续添加
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 添加菜品弹窗 -->
    <el-dialog
      v-model="dishDialogVisible"
      title="添加菜品"
      width="70%"
      :close-on-click-modal="false"
    >
      <div class="add-dish-dialog">
        <!-- 搜索框 -->
        <div class="search-box">
          <el-input
            v-model="dishSearchKey"
            placeholder="请输入菜品名称进行搜索"
            clearable
            @keyup.enter="searchDish"
          >
            <template #prefix>
              <el-icon @click="searchDish"><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="dish-select-content">
          <!-- 左侧分类和菜品列表 -->
          <div class="left-panel">
            <!-- 分类标签 -->
            <div v-if="!dishSearchKey" class="category-tabs">
              <div
                v-for="(item, index) in dishCategoryList"
                :key="item.id"
                :class="['category-tab', { active: currentCategoryIndex === index }]"
                @click="selectCategory(index, item.id)"
              >
                {{ item.name }}
              </div>
            </div>

            <!-- 菜品列表 -->
            <div class="dish-list">
              <el-checkbox-group v-model="selectedDishNames">
                <div
                  v-for="dish in dishList"
                  :key="dish.id"
                  class="dish-item"
                >
                  <el-checkbox :label="dish.name">
                    <div class="dish-info">
                      <span class="dish-name">{{ dish.name }}</span>
                      <span class="dish-status">
                        {{ dish.status === 1 ? '在售' : '停售' }}
                      </span>
                      <span class="dish-price">￥{{ dish.price }}</span>
                    </div>
                  </el-checkbox>
                </div>
              </el-checkbox-group>

              <el-empty v-if="dishList.length === 0" description="暂无菜品数据" />
            </div>
          </div>

          <!-- 右侧已选菜品 -->
          <div class="right-panel">
            <div class="selected-header">
              已选菜品({{ selectedDishList.length }})
            </div>
            <div class="selected-list">
              <div
                v-for="dish in selectedDishList"
                :key="dish.id"
                class="selected-item"
              >
                <span class="selected-name">{{ dish.name }}</span>
                <span class="selected-price">￥{{ dish.price }}</span>
                <el-icon class="remove-icon" @click="removeSelectedDish(dish.name)">
                  <Close />
                </el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="dishDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmAddDish">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search, Close } from '@element-plus/icons-vue'
import { getSetmealDetail, addSetmeal, updateSetmeal } from '@/api/setmeal'
import { getCategoryByType } from '@/api/category'
import { getDishList } from '@/api/dish'
import { uploadFile } from '@/api/common'

// 路由实例
const router = useRouter()
const route = useRoute()

// 表单引用
const formRef = ref(null)

// 是否为编辑模式
const isEdit = computed(() => !!route.query.id)

// 分类列表（type=2 的分组）
const categoryList = ref([])

// 表单数据
// 注意：categoryId 初始设为 null，避免与数字类型 option 的值类型不匹配
const formData = reactive({
  id: '',
  name: '',
  categoryId: null,
  price: '',
  image: '',
  description: ''
})

// 套餐菜品列表
const dishTable = ref([])

// 价格验证
const validatePrice = (rule, value, callback) => {
  const reg = /^([1-9]\d{0,5}|0)(\.\d{1,2})?$/
  if (!value) {
    callback(new Error('请输入套餐价格'))
  } else if (!reg.test(value) || Number(value) <= 0) {
    callback(new Error('请输入大于零且最多保留两位小数的金额'))
  } else {
    callback()
  }
}

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入套餐名称', trigger: 'blur' },
    { min: 2, max: 20, message: '套餐名称长度为2-20个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择套餐分类', trigger: 'change' }
  ],
  price: [
    { required: true, validator: validatePrice, trigger: 'blur' }
  ],
  image: [
    { required: true, message: '请上传套餐图片', trigger: 'change' }
  ]
}

// ========== 添加菜品弹窗相关 ==========
const dishDialogVisible = ref(false)
const dishSearchKey = ref('')
const dishCategoryList = ref([])
const dishList = ref([])
const currentCategoryIndex = ref(0)
const selectedDishNames = ref([])
const allDishList = ref([]) // 所有菜品缓存

// 已选菜品列表
const selectedDishList = computed(() => {
  return allDishList.value.filter(dish => selectedDishNames.value.includes(dish.name))
})

// 返回列表页
const goBack = () => {
  router.push('/setmeal')
}

// 获取 type=2 分组的分类列表
const fetchCategoryList = async () => {
  try {
    const res = await getCategoryByType(2)
    if (res?.data) {
      categoryList.value = res.data || []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取套餐详情
const fetchSetmealDetail = async () => {
  try {
    const res = await getSetmealDetail(route.query.id)
    if (res?.data) {
      const data = res.data
      formData.id = data.id
      formData.name = data.name
      // 将 categoryId 转为数字，确保与 el-option 的 value 类型一致
      formData.categoryId = data.categoryId ? Number(data.categoryId) : null
      formData.price = String(data.price)
      formData.image = data.image
      formData.description = data.description || ''

      // 解析套餐菜品
      if (data.setmealDishes && Array.isArray(data.setmealDishes)) {
        dishTable.value = data.setmealDishes.map(d => ({
          ...d,
          copies: d.copies || 1
        }))
      }
    }
  } catch (error) {
    console.error('获取套餐详情失败:', error)
    ElMessage.error('获取套餐详情失败')
  }
}

// ========== 添加菜品弹窗方法 ==========
// 打开添加菜品弹窗
const openAddDishDialog = async () => {
  selectedDishNames.value = dishTable.value.map(d => d.name)
  dishSearchKey.value = ''
  currentCategoryIndex.value = 0

  // 获取 type=1 分组的分类（用于菜品选择弹窗）
  await fetchDishCategoryList()
  // 获取菜品列表
  if (dishCategoryList.value.length > 0) {
    await fetchDishListByCategory(dishCategoryList.value[0].id)
  }

  dishDialogVisible.value = true
}

// 获取 type=1 分组的分类列表（菜品弹窗用）
const fetchDishCategoryList = async () => {
  try {
    const res = await getCategoryByType(1)
    if (res?.data) {
      dishCategoryList.value = res.data || []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 根据分类获取菜品列表
const fetchDishListByCategory = async (categoryId) => {
  try {
    const res = await getDishList({ categoryId, pageSize: 1000 })
    if (res?.data) {
      const dishes = res.data.records || []
      dishList.value = dishes.map(d => ({
        ...d,
        dishId: d.id,
        dishName: d.name
      }))

      // 缓存所有菜品
      dishes.forEach(d => {
        if (!allDishList.value.some(item => item.id === d.id)) {
          allDishList.value.push({
            ...d,
            dishId: d.id,
            dishName: d.name
          })
        }
      })
    }
  } catch (error) {
    console.error('获取菜品列表失败:', error)
  }
}

// 搜索菜品
const searchDish = async () => {
  if (!dishSearchKey.value.trim()) {
    // 如果搜索关键词为空，显示当前分类的菜品
    if (dishCategoryList.value.length > 0) {
      await fetchDishListByCategory(dishCategoryList.value[currentCategoryIndex.value].id)
    }
    return
  }

  try {
    const res = await getDishList({ name: dishSearchKey.value, pageSize: 1000 })
    if (res?.data) {
      dishList.value = res.data.records.map(d => ({
        ...d,
        dishId: d.id,
        dishName: d.name
      }))
    }
  } catch (error) {
    console.error('搜索菜品失败:', error)
  }
}

// 选择分类
const selectCategory = async (index, categoryId) => {
  currentCategoryIndex.value = index
  await fetchDishListByCategory(categoryId)
}

// 移除已选菜品
const removeSelectedDish = (name) => {
  const index = selectedDishNames.value.indexOf(name)
  if (index > -1) {
    selectedDishNames.value.splice(index, 1)
  }
}

// 确认添加菜品
const confirmAddDish = () => {
  // 合并已选菜品和新选菜品
  const newDishes = selectedDishList.value.filter(
    d => !dishTable.value.some(item => item.id === d.id)
  )

  // 更新菜品表格
  dishTable.value = selectedDishList.value.map(d => {
    const existing = dishTable.value.find(item => item.id === d.id)
    return existing || { ...d, copies: 1 }
  })

  dishDialogVisible.value = false
}

// 删除菜品
const removeDish = (index) => {
  dishTable.value.splice(index, 1)
}

// 图片上传前验证
const beforeImageUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 图片上传
const handleImageUpload = async (options) => {
  try {
    const uploadFormData = new FormData()
    uploadFormData.append('file', options.file)
    const res = await uploadFile(uploadFormData)
    if (res?.data) {
      formData.image = res.data
      ElMessage.success('图片上传成功')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  }
}

// 提交表单
const handleSubmit = async (continueAdd) => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    if (dishTable.value.length === 0) {
      ElMessage.error('请添加套餐菜品')
      return
    }

    const params = {
      name: formData.name,
      categoryId: formData.categoryId,
      price: formData.price,
      image: formData.image,
      description: formData.description,
      setmealDishes: dishTable.value.map(d => ({
        copies: d.copies,
        dishId: d.dishId || d.id,
        name: d.name,
        price: d.price
      }))
    }

    if (isEdit.value) {
      // 编辑模式
      params.id = formData.id
      await updateSetmeal(params)
      ElMessage.success('套餐修改成功')
      goBack()
    } else {
      // 添加模式
      await addSetmeal(params)
      ElMessage.success('套餐添加成功')

      if (continueAdd) {
        formRef.value.resetFields()
        dishTable.value = []
        formData.image = ''
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
  fetchCategoryList()
  if (isEdit.value) {
    fetchSetmealDetail()
  }
})
</script>

<style lang="scss" scoped>
/* 黄色主题变量 */
$yellow-primary: #e6a23c;
$yellow-light: #fdf6ec;
$yellow-dark: #d49133;

.add-setmeal-container {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  font-size: 16px;
  font-weight: 500;
}

.form-container {
  max-width: 800px;
  padding: 20px 0;
}

.setmeal-form {
  .el-select {
    width: 300px;
  }

  .el-input {
    width: 300px;
  }
}

/* 套餐菜品样式 */
.dish-box {
  width: 100%;
}

.dish-content {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background: #fafafa;
}

.add-dish-btn {
  margin-bottom: 15px;
}

.dish-table {
  width: 100%;
}

/* 图片上传样式 */
.image-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;

    &:hover {
      border-color: $yellow-primary;
    }
  }

  .setmeal-image {
    width: 200px;
    height: 160px;
    display: block;
  }

  .image-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 200px;
    height: 160px;
    line-height: 160px;
    text-align: center;
  }
}

.upload-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

.form-buttons {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;

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

/* 添加菜品弹窗样式 */
.add-dish-dialog {
  .search-box {
    margin-bottom: 15px;

    .el-input {
      width: 250px;
    }

    .el-icon {
      cursor: pointer;
    }
  }

  .dish-select-content {
    display: flex;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    min-height: 400px;
  }

  .left-panel {
    flex: 1;
    border-right: 1px solid #ebeef5;
    display: flex;
    flex-direction: column;
  }

  .category-tabs {
    width: 120px;
    border-right: 1px solid #ebeef5;
    padding: 10px 0;

    .category-tab {
      padding: 10px 15px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background: #f5f7fa;
      }

      &.active {
        color: $yellow-primary;
        background: $yellow-light;
        border-right: 2px solid $yellow-primary;
      }
    }
  }

  .dish-list {
    flex: 1;
    padding: 10px;
    overflow-y: auto;
    max-height: 400px;

    .dish-item {
      padding: 10px;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .dish-info {
        display: flex;
        align-items: center;
        width: 100%;

        .dish-name {
          flex: 1;
        }

        .dish-status {
          width: 60px;
          text-align: center;
          color: #909399;
        }

        .dish-price {
          width: 80px;
          text-align: right;
          color: #f56c6c;
        }
      }
    }
  }

  .right-panel {
    width: 250px;
    padding: 15px;

    .selected-header {
      font-weight: 500;
      margin-bottom: 10px;
      padding-bottom: 10px;
      border-bottom: 1px solid #ebeef5;
    }

    .selected-list {
      max-height: 350px;
      overflow-y: auto;

      .selected-item {
        display: flex;
        align-items: center;
        padding: 8px 10px;
        margin-bottom: 8px;
        background: #f5f7fa;
        border-radius: 4px;

        .selected-name {
          flex: 1;
        }

        .selected-price {
          color: #f56c6c;
          margin-right: 10px;
        }

        .remove-icon {
          cursor: pointer;
          color: #909399;

          &:hover {
            color: #f56c6c;
          }
        }
      }
    }
  }
}
</style>
