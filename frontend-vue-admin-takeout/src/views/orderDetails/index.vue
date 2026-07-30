<template>
  <div class="order-container">
    <!-- 订单状态标签页 -->
    <div class="order-tabs">
      <div
        v-for="item in orderTabs"
        :key="item.value"
        class="tab-item"
        :class="{ active: activeTab === item.value }"
        @click="handleTabChange(item.value)"
      >
        <el-badge
          v-if="[2, 3, 4].includes(item.value) && item.num > 0"
          :value="item.num > 99 ? '99+' : item.num"
          :max="99"
          class="tab-badge"
        >
          <span class="tab-label">{{ item.label }}</span>
        </el-badge>
        <span v-else class="tab-label">{{ item.label }}</span>
      </div>
    </div>

    <!-- 搜索栏 -->
    <!-- 注意：后端只支持手机号查询，订单号查询已移除 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="手机号">
          <el-input
            v-model="searchForm.phone"
            placeholder="请填写手机号"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="warning" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 订单列表 -->
    <el-table :data="tableData" stripe v-loading="loading" class="order-table">
      <el-table-column prop="number" label="订单号" min-width="150" />
      <el-table-column
        v-if="[2, 3, 4].includes(activeTab)"
        prop="orderDishes"
        label="订单菜品"
        min-width="200"
        show-overflow-tooltip
      />
      <el-table-column
        v-if="activeTab === 0"
        label="订单状态"
        min-width="100"
      >
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" effect="light">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        v-if="[0, 5, 6, 7].includes(activeTab)"
        prop="consignee"
        label="用户名"
        min-width="100"
        show-overflow-tooltip
      />
      <el-table-column
        v-if="[0, 5, 6, 7].includes(activeTab)"
        prop="phone"
        label="手机号"
        min-width="120"
      />
      <el-table-column
        v-if="[0, 2, 3, 4, 5, 6, 7].includes(activeTab)"
        prop="address"
        label="地址"
        min-width="200"
        show-overflow-tooltip
      />
      <el-table-column
        v-if="[0, 6].includes(activeTab)"
        prop="orderTime"
        label="下单时间"
        min-width="160"
      />
      <el-table-column
        v-if="activeTab === 6"
        prop="cancelTime"
        label="取消时间"
        min-width="160"
      />
      <el-table-column
        v-if="activeTab === 6"
        prop="cancelReason"
        label="取消原因"
        min-width="120"
      />
      <el-table-column
        v-if="activeTab === 5"
        prop="deliveryTime"
        label="送达时间"
        min-width="160"
      />
      <el-table-column
        v-if="[2, 3, 4].includes(activeTab)"
        prop="estimatedDeliveryTime"
        label="预计送达时间"
        min-width="160"
      />
      <el-table-column
        v-if="[0, 2, 5].includes(activeTab)"
        label="实收金额"
        min-width="100"
        align="center"
      >
        <template #default="{ row }">
          <span class="amount">￥{{ row.amount?.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        v-if="[2, 3, 4, 5].includes(activeTab)"
        prop="remark"
        label="备注"
        min-width="120"
        show-overflow-tooltip
      />
      <el-table-column
        v-if="[2, 3, 4].includes(activeTab)"
        prop="tablewareNumber"
        label="餐具数量"
        min-width="80"
        align="center"
      />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.statusNum === 2"
            type="warning"
            link
            size="small"
            @click="handleOrderAccept(row)"
          >
            接单
          </el-button>
          <el-button
            v-if="row.statusNum === 3"
            type="warning"
            link
            size="small"
            @click="handleDelivery(row)"
          >
            派送
          </el-button>
          <el-button
            v-if="row.statusNum === 4"
            type="success"
            link
            size="small"
            @click="handleComplete(row)"
          >
            完成
          </el-button>
          <el-button
            v-if="row.statusNum === 2"
            type="danger"
            link
            size="small"
            @click="handleReject(row)"
          >
            拒单
          </el-button>
          <el-button
            v-if="[1, 3, 4, 5].includes(row.statusNum)"
            type="danger"
            link
            size="small"
            @click="handleCancel(row)"
          >
            取消
          </el-button>
          <el-button type="primary" link size="small" @click="handleViewDetail(row)">
            查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!loading && tableData.length === 0" description="暂无订单数据" />

    <!-- 分页 -->
    <el-pagination
      v-if="total > 0"
      class="pagination"
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :page-sizes="[10, 20, 30, 40]"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      @size-change="fetchOrderList"
      @current-change="fetchOrderList"
    />

    <!-- 订单详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="订单信息"
      width="55%"
      :close-on-click-modal="false"
      class="order-detail-dialog"
    >
      <el-scrollbar style="height: 100%">
        <div class="detail-content">
          <!-- 订单状态头部 -->
          <div class="detail-header">
            <div class="header-left">
              <span class="order-number">订单号：{{ detailData.number }}</span>
              <el-tag :type="getStatusType(detailData.status)">
                {{ getStatusText(detailData.status) }}
              </el-tag>
            </div>
            <p class="order-time">下单时间：{{ detailData.orderTime }}</p>
          </div>

          <!-- 用户信息 -->
          <div class="user-section">
            <div class="user-info-box">
              <div class="info-row">
                <span class="info-label">用户名：</span>
                <span class="info-value">{{ detailData.consignee }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">手机号：</span>
                <span class="info-value">{{ detailData.phone }}</span>
              </div>
              <div v-if="[2, 3, 4, 5].includes(detailData.statusNum)" class="info-row">
                <span class="info-label">{{ detailData.statusNum === 5 ? '送达时间：' : '预计送达时间：' }}</span>
                <span class="info-value">
                  {{ detailData.statusNum === 5 ? detailData.deliveryTime : detailData.estimatedDeliveryTime }}
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">地址：</span>
                <span class="info-value">{{ detailData.address }}</span>
              </div>
            </div>
            <div class="user-remark" :class="{ 'cancel-remark': detailData.statusNum === 6 }">
              <span class="remark-title">{{ detailData.statusNum === 6 ? '取消原因' : '备注' }}：</span>
              <span class="remark-content">
                {{ detailData.statusNum === 6 ? (detailData.cancelReason || detailData.rejectionReason) : detailData.remark }}
              </span>
            </div>
          </div>

          <!-- 菜品信息 -->
          <div class="dish-section">
            <div class="section-title">菜品</div>
            <div class="dish-list">
              <div
                v-for="(item, index) in detailData.orderDetailList"
                :key="index"
                class="dish-item"
              >
                <div class="dish-info">
                  <span class="dish-name">{{ item.name }}</span>
                  <span class="dish-num">x{{ item.number }}</span>
                </div>
                <span class="dish-price">￥{{ item.amount?.toFixed(2) }}</span>
              </div>
            </div>
            <div class="dish-summary">
              <span>菜品小计：</span>
              <span class="summary-price">￥{{ ((detailData.amount || 0) - 6 - (detailData.packAmount || 0)).toFixed(2) }}</span>
            </div>
          </div>

          <!-- 费用信息 -->
          <div class="amount-section">
            <div class="section-title">费用</div>
            <div class="amount-list">
              <div class="amount-item">
                <span>菜品小计：</span>
                <span>￥{{ ((detailData.amount || 0) - 6 - (detailData.packAmount || 0)).toFixed(2) }}</span>
              </div>
              <div class="amount-item">
                <span>派送费：</span>
                <span>￥6.00</span>
              </div>
              <div class="amount-item">
                <span>打包费：</span>
                <span>￥{{ (detailData.packAmount || 0).toFixed(2) }}</span>
              </div>
              <div class="amount-item total">
                <span>合计：</span>
                <span>￥{{ (detailData.amount || 0).toFixed(2) }}</span>
              </div>
              <div class="amount-item">
                <span>支付渠道：</span>
                <span>{{ detailData.payMethod === 1 ? '微信支付' : '支付宝支付' }}</span>
              </div>
              <div class="amount-item">
                <span>支付时间：</span>
                <span>{{ detailData.checkoutTime }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-scrollbar>

      <template #footer>
        <div class="dialog-footer">
          <el-checkbox
            v-if="detailData.statusNum === 2 && activeTab === 2"
            v-model="isAutoNext"
          >
            处理完自动跳转下一条
          </el-checkbox>
          <el-button
            v-if="detailData.statusNum === 2"
            type="danger"
            @click="handleReject(rowData)"
          >
            拒 单
          </el-button>
          <el-button
            v-if="detailData.statusNum === 2"
            type="warning"
            @click="handleOrderAccept(rowData)"
          >
            接 单
          </el-button>
          <el-button
            v-if="[1, 3, 4, 5].includes(detailData.statusNum)"
            @click="detailVisible = false"
          >
            返 回
          </el-button>
          <el-button
            v-if="detailData.statusNum === 3"
            type="warning"
            @click="handleDelivery(rowData)"
          >
            派 送
          </el-button>
          <el-button
            v-if="detailData.statusNum === 4"
            type="success"
            @click="handleComplete(rowData)"
          >
            完 成
          </el-button>
          <el-button
            v-if="[1].includes(detailData.statusNum)"
            type="danger"
            @click="handleCancel(rowData)"
          >
            取消订单
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 取消/拒单原因弹窗 -->
    <el-dialog
      v-model="cancelVisible"
      :title="cancelTitle + '原因'"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px">
        <el-form-item :label="cancelTitle + '原因：'">
          <el-select v-model="cancelReason" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="item in cancelReasonList"
              :key="item.value"
              :label="item.label"
              :value="item.label"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="cancelReason === '自定义原因'" label="原因：">
          <el-input
            v-model="customReason"
            type="textarea"
            :placeholder="'请填写' + cancelTitle + '原因（限20字内）'"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible = false">取 消</el-button>
        <el-button type="warning" @click="confirmCancel">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  readAllOrders,
  getOrderStatistics,
  searchOrders,
  getOrderDetail,
  confirmOrder,
  cancelOrder,
  deliveryOrder,
  completeOrder
} from '@/api/order'

// 路由
const route = useRoute()

// 加载状态
const loading = ref(false)

// 当前标签页
const activeTab = ref(0)

// 分页参数
const pagination = reactive({
  page: 1,
  pageSize: 10
})

// 总数
const total = ref(0)

// 表格数据
const tableData = ref([])

// 搜索表单
// 注意：后端只支持手机号查询，订单号查询已移除
const searchForm = reactive({
  phone: ''
})

// 订单统计
const orderStatics = reactive({
  toBeConfirmed: 0,
  confirmed: 0,
  deliveryInProgress: 0
})

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref({})
const rowData = ref({})
const isAutoNext = ref(true)

// 取消/拒单弹窗
const cancelVisible = ref(false)
const cancelTitle = ref('')
const cancelReason = ref('')
const customReason = ref('')
const currentOrderId = ref('')

// 取消原因列表
const cancelReasonList = ref([
  { value: 1, label: '订单量较多，暂时无法接单' },
  { value: 2, label: '菜品已销售完，暂时无法接单' },
  { value: 3, label: '餐厅已打烊，暂时无法接单' },
  { value: 0, label: '自定义原因' }
])

// 取消订单原因列表
const cancelOrderReasonList = ref([
  { value: 1, label: '订单量较多，暂时无法接单' },
  { value: 2, label: '菜品已销售完，暂时无法接单' },
  { value: 3, label: '骑手不足无法配送' },
  { value: 4, label: '客户电话取消' },
  { value: 0, label: '自定义原因' }
])

// 订单标签页
const orderTabs = computed(() => [
  { label: '全部订单', value: 0, num: 0 },
  { label: '待付款', value: 1, num: 0 },
  { label: '已支付(待接单)', value: 2, num: orderStatics.toBeConfirmed },
  { label: '制作中', value: 3, num: orderStatics.confirmed },
  { label: '正在派送', value: 4, num: orderStatics.deliveryInProgress },
  { label: '已送达', value: 5, num: 0 },
  { label: '已取消', value: 6, num: 0 },
  { label: '退款', value: 7, num: 0 }
])

// 获取订单状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待付款',
    'ACCEPTED': '已支付,寻找商家',
    'COMFIRM': '制作中',
    'DELIVERING': '正在派送',
    'COMPLETED': '已送达',
    'CANCELLED': '取消支付',
    'REFUNDED': '退款'
  }
  return statusMap[status] || '未知'
}

// 获取订单状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'info',
    'ACCEPTED': 'warning',
    'COMFIRM': 'warning',
    'DELIVERING': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'info',
    'REFUNDED': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取订单统计
const fetchOrderStatistics = async () => {
  try {
    const res = await getOrderStatistics()
    if (res?.data) {
      Object.assign(orderStatics, res.data)
    }
  } catch (error) {
    console.error('获取订单统计失败:', error)
  }
}

// 获取订单列表
// 注意：后端接口限制 - conditionSearch 只支持手机号查询，不支持状态过滤
// 因此需要在前端进行状态过滤
const fetchOrderList = async () => {
  loading.value = true
  try {
    let res

    // 如果有手机号搜索，使用 conditionSearch 接口
    if (searchForm.phone) {
      const params = {
        page: pagination.page,
        pageSize: pagination.pageSize,
        phone: searchForm.phone
      }
      res = await searchOrders(params)
      if (res?.data) {
        let orders = res.data.records || []

        // 如果不是"全部订单"标签页，需要在前端过滤状态
        if (activeTab.value !== 0) {
          orders = orders.filter(item => convertStatusToNum(item.status) === activeTab.value)
        }

        tableData.value = orders.map(item => ({
          ...item,
          statusNum: convertStatusToNum(item.status)
        }))
        total.value = activeTab.value === 0 ? (res.data.total || 0) : orders.length
      }
    } else {
      // 没有搜索条件时，使用 readAllOrders 获取所有订单（限制20条）
      res = await readAllOrders()
      if (res?.data) {
        let orders = res.data || []

        // 如果不是"全部订单"标签页，需要在前端过滤状态
        if (activeTab.value !== 0) {
          orders = orders.filter(item => convertStatusToNum(item.status) === activeTab.value)
        }

        tableData.value = orders.map(item => ({
          ...item,
          statusNum: convertStatusToNum(item.status)
        }))
        total.value = orders.length
      }
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 将后端状态字符串转换为数字
const convertStatusToNum = (status) => {
  const statusMap = {
    'PENDING': 1,
    'ACCEPTED': 2,
    'COMFIRM': 3,
    'DELIVERING': 4,
    'COMPLETED': 5,
    'CANCELLED': 6,
    'REFUNDED': 7
  }
  return statusMap[status] || 0
}

// 标签页切换
const handleTabChange = (value) => {
  if (activeTab.value === value) return
  activeTab.value = value
  pagination.page = 1
  searchForm.phone = '' // 只重置手机号，订单号已移除
  fetchOrderList()
  fetchOrderStatistics()
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchOrderList()
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const res = await getOrderDetail(row.id)
    if (res?.data) {
      detailData.value = {
        ...res.data,
        statusNum: convertStatusToNum(res.data.status)
      }
      rowData.value = row
      detailVisible.value = true
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  }
}

// 接单
const handleOrderAccept = async (row) => {
  try {
    await ElMessageBox.confirm('确认接单？', '提示', { type: 'warning' })
    const res = await confirmOrder(row.id)
    if (res?.data) {
      ElMessage.success('接单成功')
      detailVisible.value = false
      fetchOrderList()
      fetchOrderStatistics()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('接单失败:', error)
      ElMessage.error('接单失败')
    }
  }
}

// 派送
const handleDelivery = async (row) => {
  try {
    await ElMessageBox.confirm('确认开始派送？', '提示', { type: 'warning' })
    const res = await deliveryOrder(row.id)
    if (res?.data) {
      ElMessage.success('派送成功')
      detailVisible.value = false
      fetchOrderList()
      fetchOrderStatistics()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('派送失败:', error)
      ElMessage.error('派送失败')
    }
  }
}

// 完成
const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm('确认完成订单？', '提示', { type: 'success' })
    const res = await completeOrder(row.id)
    if (res?.data) {
      ElMessage.success('订单已完成')
      detailVisible.value = false
      fetchOrderList()
      fetchOrderStatistics()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 拒单
const handleReject = (row) => {
  cancelTitle.value = '拒绝'
  cancelReasonList.value = [
    { value: 1, label: '订单量较多，暂时无法接单' },
    { value: 2, label: '菜品已销售完，暂时无法接单' },
    { value: 3, label: '餐厅已打烊，暂时无法接单' },
    { value: 0, label: '自定义原因' }
  ]
  currentOrderId.value = row.id
  cancelReason.value = ''
  customReason.value = ''
  cancelVisible.value = true
  detailVisible.value = false
}

// 取消订单
const handleCancel = (row) => {
  cancelTitle.value = '取消'
  cancelReasonList.value = [
    { value: 1, label: '订单量较多，暂时无法接单' },
    { value: 2, label: '菜品已销售完，暂时无法接单' },
    { value: 3, label: '骑手不足无法配送' },
    { value: 4, label: '客户电话取消' },
    { value: 0, label: '自定义原因' }
  ]
  currentOrderId.value = row.id
  cancelReason.value = ''
  customReason.value = ''
  cancelVisible.value = true
  detailVisible.value = false
}

// 确认取消/拒单
// 注意：后端 cancel 接口不接受参数，取消原因由后端硬编码
// 这是一个后端设计问题，建议后端修改接口接受取消原因参数
const confirmCancel = async () => {
  if (!cancelReason.value) {
    ElMessage.error(`请选择${cancelTitle.value}原因`)
    return
  }
  if (cancelReason.value === '自定义原因' && !customReason.value) {
    ElMessage.error(`请输入${cancelTitle.value}原因`)
    return
  }

  // 注意：后端接口不接受取消原因参数，这里的原因仅用于前端显示
  const reason = cancelReason.value === '自定义原因' ? customReason.value : cancelReason.value
  console.log(`${cancelTitle.value}原因:`, reason) // 输出原因到控制台，实际应传递给后端

  try {
    // 后端接口只接受订单ID，不接受取消原因
    const res = await cancelOrder(currentOrderId.value)
    if (res?.data) {
      ElMessage.success('操作成功')
      cancelVisible.value = false
      fetchOrderList()
      fetchOrderStatistics()
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 初始化
onMounted(() => {
  // 如果路由有 status 参数，使用它
  if (route.query.status) {
    activeTab.value = Number(route.query.status)
  }
  fetchOrderStatistics()
  fetchOrderList()
})
</script>

<style lang="scss" scoped>
// 黄色主题变量
$yellow-primary: #e6a23c;
$yellow-light: #fdf6ec;
$yellow-dark: #d49133;

.order-container {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  min-height: calc(100vh - 120px);
}

// 订单状态标签页
.order-tabs {
  display: flex;
  border-radius: 4px;
  margin-bottom: 20px;
  background: #f5f5f5;
  padding: 4px;

  .tab-item {
    width: 120px;
    height: 40px;
    text-align: center;
    line-height: 40px;
    color: #333;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      color: $yellow-primary;
    }

    .tab-badge {
      :deep(.el-badge__content) {
        background-color: #f56c6c;
      }
    }

    .tab-label {
      font-size: 14px;
    }
  }

  .active {
    background-color: $yellow-primary;
    color: #fff;
    font-weight: bold;

    &:hover {
      color: #fff;
    }
  }
}

// 搜索栏
.search-bar {
  margin-bottom: 20px;

  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-input) {
    width: 180px;
  }

  :deep(.el-date-editor) {
    width: 280px !important;
  }
}

// 表格
.order-table {
  margin-bottom: 20px;

  .amount {
    color: $yellow-dark;
    font-weight: 500;
  }
}

// 分页
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

// 详情弹窗
.order-detail-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
  }

  .detail-content {
    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 20px;
      border-bottom: 1px solid #e7e6e6;

      .header-left {
        display: flex;
        align-items: center;
        gap: 15px;

        .order-number {
          font-size: 16px;
          font-weight: bold;
          color: #333;
        }
      }

      .order-time {
        color: #666;
      }
    }

    .user-section {
      background: #fbfbfa;
      padding: 20px;
      margin-top: 20px;
      border-radius: 4px;

      .user-info-box {
        display: flex;
        flex-wrap: wrap;

        .info-row {
          flex: 0 0 50%;
          margin-bottom: 12px;

          .info-label {
            color: #666;
            margin-right: 10px;
          }

          .info-value {
            color: #333;
          }
        }
      }

      .user-remark {
        margin-top: 15px;
        padding: 12px 15px;
        background: #fffbf0;
        border: 1px solid #fbe396;
        border-radius: 4px;

        .remark-title {
          color: #666;
          margin-right: 10px;
        }

        .remark-content {
          color: #333;
        }
      }

      .cancel-remark {
        background: #fef0f0;
        border-color: #fde2e2;
      }
    }

    .dish-section {
      margin-top: 20px;

      .section-title {
        font-size: 15px;
        font-weight: 500;
        color: #333;
        margin-bottom: 15px;
        padding-left: 10px;
        border-left: 3px solid $yellow-primary;
      }

      .dish-list {
        background: #fafafa;
        padding: 15px;
        border-radius: 4px;

        .dish-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 8px 0;
          border-bottom: 1px dashed #eee;

          &:last-child {
            border-bottom: none;
          }

          .dish-info {
            display: flex;
            gap: 10px;

            .dish-name {
              color: #333;
            }

            .dish-num {
              color: #999;
            }
          }

          .dish-price {
            color: $yellow-dark;
          }
        }
      }

      .dish-summary {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        padding: 12px 15px;
        background: $yellow-light;
        border-radius: 4px;
        margin-top: 10px;

        .summary-price {
          color: $yellow-dark;
          font-weight: 500;
          margin-left: 10px;
        }
      }
    }

    .amount-section {
      margin-top: 20px;

      .section-title {
        font-size: 15px;
        font-weight: 500;
        color: #333;
        margin-bottom: 15px;
        padding-left: 10px;
        border-left: 3px solid $yellow-primary;
      }

      .amount-list {
        background: #fafafa;
        padding: 15px;
        border-radius: 4px;

        .amount-item {
          display: flex;
          justify-content: space-between;
          padding: 8px 0;
          color: #666;

          &.total {
            padding-top: 15px;
            margin-top: 10px;
            border-top: 1px solid #ddd;
            font-weight: 500;
            font-size: 16px;
            color: $yellow-dark;
          }
        }
      }
    }
  }

  .dialog-footer {
    display: flex;
    align-items: center;
    gap: 15px;

    :deep(.el-checkbox) {
      margin-right: auto;
    }
  }
}
</style>
