<template>
  <div class="add-dish-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="page-title">{{ isEdit ? '修改菜品' : '添加菜品' }}</span>
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
        class="dish-form"
      >
        <el-form-item label="菜品名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请填写菜品名称"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="菜品分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择菜品分类">
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="菜品价格" prop="price">
          <el-input
            v-model="formData.price"
            placeholder="请设置菜品价格"
            type="number"
          >
            <template #append>元</template>
          </el-input>
        </el-form-item>

        <!-- 口味配置 -->
        <el-form-item label="口味做法配置">
          <div class="flavor-box">
            <el-button
              v-if="flavors.length === 0"
              type="warning"
              plain
              @click="addFlavor"
            >
              + 添加口味
            </el-button>

            <div v-else class="flavor-content">
              <div class="flavor-header">
                <span class="flavor-title">口味名（3个字内）</span>
                <span class="flavor-tags">口味标签</span>
              </div>

              <div
                v-for="(item, index) in flavors"
                :key="index"
                class="flavor-item"
              >
                <!-- 口味选择器 -->
                <div class="flavor-select">
                  <el-select
                    v-model="item.name"
                    placeholder="请选择口味"
                    @change="handleFlavorChange(index)"
                  >
                    <el-option
                      v-for="flavor in availableFlavors"
                      :key="flavor.name"
                      :label="flavor.name"
                      :value="flavor.name"
                      :disabled="isFlavorSelected(flavor.name, index)"
                    />
                  </el-select>
                </div>

                <!-- 口味标签 -->
                <div class="flavor-tags-box">
                  <el-tag
                    v-for="(tag, tagIndex) in item.value"
                    :key="tagIndex"
                    closable
                    type="warning"
                    @close="removeFlavorTag(index, tagIndex)"
                  >
                    {{ tag }}
                  </el-tag>
                  <el-input
                    v-model="item.newTag"
                    class="tag-input"
                    placeholder="输入后回车"
                    @keyup.enter="addFlavorTag(index)"
                  />
                </div>

                <!-- 删除按钮 -->
                <el-button
                  type="danger"
                  link
                  @click="removeFlavor(index)"
                >
                  删除
                </el-button>
              </div>

              <el-button
                v-if="flavors.length < flavorOptions.length"
                type="warning"
                plain
                class="add-flavor-btn"
                @click="addFlavor"
              >
                + 添加口味
              </el-button>
            </div>
          </div>
        </el-form-item>

        <!-- 图片上传 -->
        <el-form-item label="菜品图片" prop="image">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :http-request="handleImageUpload"
          >
            <el-image
              v-if="formData.image"
              :src="formData.image"
              class="dish-image"
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

        <!-- 菜品描述 -->
        <el-form-item label="菜品描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            maxlength="200"
            placeholder="菜品描述，最长200字"
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getDishDetail, addDish, updateDish } from '@/api/dish'
import { getCategoryByType } from '@/api/category'
import { uploadFile } from '@/api/common'

// 路由实例
const router = useRouter()
const route = useRoute()

// 表单引用
const formRef = ref(null)

// 是否为编辑模式
const isEdit = computed(() => !!route.query.id)

// 分类列表
const categoryList = ref([])

// 口味选项
const flavorOptions = [
  { name: '甜味', value: ['无糖', '少糖', '半糖', '多糖', '全糖'] },
  { name: '温度', value: ['热饮', '常温', '去冰', '少冰', '多冰'] },
  { name: '忌口', value: ['不要葱', '不要蒜', '不要香菜', '不要辣'] },
  { name: '辣度', value: ['不辣', '微辣', '中辣', '重辣'] }
]

// 可选口味（排除已选）
const availableFlavors = computed(() => {
  const selectedNames = flavors.value.map(f => f.name)
  return flavorOptions.filter(f => !selectedNames.includes(f.name))
})

// 表单数据
const formData = reactive({
  id: '',
  name: '',
  categoryId: '',
  price: '',
  image: '',
  description: ''
})

// 口味数据
const flavors = ref([])

// 价格验证
const validatePrice = (rule, value, callback) => {
  const reg = /^([1-9]\d{0,5}|0)(\.\d{1,2})?$/
  if (!value) {
    callback(new Error('请输入菜品价格'))
  } else if (!reg.test(value) || Number(value) <= 0) {
    callback(new Error('请输入大于零且最多保留两位小数的金额'))
  } else {
    callback()
  }
}

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入菜品名称', trigger: 'blur' },
    { min: 2, max: 20, message: '菜品名称长度为2-20个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择菜品分类', trigger: 'change' }
  ],
  price: [
    { required: true, validator: validatePrice, trigger: 'blur' }
  ],
  image: [
    { required: true, message: '请上传菜品图片', trigger: 'change' }
  ]
}

// 返回列表页
const goBack = () => {
  router.push('/dish')
}

// 获取分类列表
const fetchCategoryList = async () => {
  try {
    const res = await getCategoryByType(1)
    if (res?.data) {
      categoryList.value = res.data || []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取菜品详情
const fetchDishDetail = async () => {
  try {
    const res = await getDishDetail(route.query.id)
    if (res?.data) {
      const data = res.data
      formData.id = data.id
      formData.name = data.name
      formData.categoryId = data.categoryId
      formData.price = String(data.price)
      formData.image = data.image
      formData.description = data.description || ''

      // 解析口味数据
      if (data.flavors && Array.isArray(data.flavors)) {
        flavors.value = data.flavors.map(f => ({
          name: f.name,
          value: JSON.parse(f.value),
          newTag: ''
        }))
      }
    }
  } catch (error) {
    console.error('获取菜品详情失败:', error)
    ElMessage.error('获取菜品详情失败')
  }
}

// 添加口味
const addFlavor = () => {
  flavors.value.push({
    name: '',
    value: [],
    newTag: ''
  })
}

// 删除口味
const removeFlavor = (index) => {
  flavors.value.splice(index, 1)
}

// 口味选择变化
const handleFlavorChange = (index) => {
  const flavorName = flavors.value[index].name
  const flavorOption = flavorOptions.find(f => f.name === flavorName)
  if (flavorOption) {
    flavors.value[index].value = [...flavorOption.value]
  }
}

// 检查口味是否已选择
const isFlavorSelected = (name, currentIndex) => {
  return flavors.value.some((f, i) => f.name === name && i !== currentIndex)
}

// 添加口味标签
const addFlavorTag = (index) => {
  const tag = flavors.value[index].newTag.trim()
  if (tag && !flavors.value[index].value.includes(tag)) {
    flavors.value[index].value.push(tag)
    flavors.value[index].newTag = ''
  }
}

// 删除口味标签
const removeFlavorTag = (flavorIndex, tagIndex) => {
  flavors.value[flavorIndex].value.splice(tagIndex, 1)
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
    const formData = new FormData()
    formData.append('file', options.file)
    const res = await uploadFile(formData)
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

    // 构建口味数据
    const flavorsData = flavors.value.map(f => ({
      name: f.name,
      value: JSON.stringify(f.value)
    }))

    const params = {
      name: formData.name,
      categoryId: formData.categoryId,
      price: formData.price,
      image: formData.image,
      description: formData.description,
      flavors: flavorsData
    }

    if (isEdit.value) {
      // 编辑模式
      params.id = formData.id
      await updateDish(params)
      ElMessage.success('菜品修改成功')
      goBack()
    } else {
      // 添加模式
      await addDish(params)
      ElMessage.success('菜品添加成功')

      if (continueAdd) {
        formRef.value.resetFields()
        flavors.value = []
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
    fetchDishDetail()
  }
})
</script>

<style lang="scss" scoped>
/* 黄色主题变量 */
$yellow-primary: #e6a23c;
$yellow-light: #fdf6ec;
$yellow-dark: #d49133;

.add-dish-container {
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

.dish-form {
  .el-select {
    width: 300px;
  }

  .el-input {
    width: 300px;
  }
}

/* 口味配置样式 */
.flavor-box {
  width: 100%;
}

.flavor-content {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background: #fafafa;
}

.flavor-header {
  display: flex;
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;

  .flavor-title {
    width: 150px;
  }

  .flavor-tags {
    flex: 1;
  }
}

.flavor-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 15px;

  .flavor-select {
    width: 150px;

    .el-select {
      width: 100%;
    }
  }

  .flavor-tags-box {
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 8px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background: #fff;
    min-height: 40px;

    .el-tag {
      margin: 0;
    }

    .tag-input {
      width: 100px;

      :deep(.el-input__wrapper) {
        padding: 0 8px;
        box-shadow: none;
      }
    }
  }
}

.add-flavor-btn {
  margin-top: 10px;
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

  .dish-image {
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
</style>
