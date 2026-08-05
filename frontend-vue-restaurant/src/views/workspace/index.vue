<template>
  <div class="dashboard-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">工作台</h2>
      <p class="page-desc">数据概览与业务统计</p>
    </div>

    <!-- 今日订单区域 -->
    <div class="order-view-section">
      <div class="section-header">
        <h3 class="section-title">今日订单</h3>
        <span class="section-date">{{ currentDate }}</span>
      </div>

      <div class="order-stats">
        <div class="order-stat-item">
          <div class="stat-icon yellow-bg">
            <el-icon><Ticket /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-number">{{ todayOrder.orderCountList || 0 }}</span>
            <span class="stat-name">订单数量</span>
          </div>
        </div>
        <div class="order-stat-item">
          <div class="stat-icon yellow-bg">
            <el-icon><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-number">{{ todayOrder.validOrderCountList || 0 }}</span>
            <span class="stat-name">有效订单</span>
          </div>
        </div>
        <div class="order-stat-item">
          <div class="stat-icon yellow-bg">
            <el-icon><Promotion /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-number">{{ (todayOrder.validRate || 0).toFixed(1) }}%</span>
            <span class="stat-name">有效率</span>
          </div>
        </div>
      </div>
    </div>
    <!-- 概览卡片区域 -->
    <div class="overview-section">
      <!-- 菜品概览卡片 -->
      <div class="overview-card">
        <div class="card-icon yellow">
          <el-icon><Crop /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">菜品概览</h3>
          <div class="card-stats">
            <span class="stat-item">
              <span class="stat-value green">{{ dishOverview.sold || 0 }}</span>
              <span class="stat-label">在售</span>
            </span>
            <span class="stat-divider"></span>
            <span class="stat-item">
              <span class="stat-value red">{{ dishOverview.discontinued || 0 }}</span>
              <span class="stat-label">停售</span>
            </span>
          </div>
          <p class="card-total">总计：{{ (dishOverview.sold || 0) + (dishOverview.discontinued || 0) }} 道菜品</p>
        </div>
      </div>

      <!-- 套餐概览卡片 -->
      <div class="overview-card">
        <div class="card-icon yellow">
          <el-icon><ShoppingCart /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">套餐概览</h3>
          <div class="card-stats">
            <span class="stat-item">
              <span class="stat-value green">{{ setmealOverview.sold || 0 }}</span>
              <span class="stat-label">在售</span>
            </span>
            <span class="stat-divider"></span>
            <span class="stat-item">
              <span class="stat-value red">{{ setmealOverview.discontinued || 0 }}</span>
              <span class="stat-label">停售</span>
            </span>
          </div>
          <p class="card-total">总计：{{ (setmealOverview.sold || 0) + (setmealOverview.discontinued || 0) }} 个套餐</p>
        </div>
      </div>

      <!-- 订单概览卡片 -->
      <div class="overview-card">
        <div class="card-icon yellow">
          <el-icon><Document /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">订单概览</h3>
          <div class="card-stats">
            <span class="stat-item">
              <span class="stat-value blue">{{ orderOverview.allOrders || 0 }}</span>
              <span class="stat-label">总数</span>
            </span>
            <span class="stat-item">
              <span class="stat-value green">{{ orderOverview.completedOrders || 0 }}</span>
              <span class="stat-label">完成</span>
            </span>
            <span class="stat-item">
              <span class="stat-value orange">{{ orderOverview.refundOrders || 0 }}</span>
              <span class="stat-label">退款</span>
            </span>
            <span class="stat-item">
              <span class="stat-value red">{{ orderOverview.cancelledOrders || 0 }}</span>
              <span class="stat-label">取消</span>
            </span>
          </div>
        </div>
      </div>
    </div>
    <!-- 统计图表区域 -->
    <div class="statistics-section">
      <!-- 菜品统计 -->
      <div class="stats-card">
        <div class="section-header">
          <h3 class="section-title">菜品统计</h3>
          <span class="section-subtitle">TOP 5 热销菜品</span>
        </div>
        <div class="ranking">
          <div 
            v-for="(item, index) in topDish.slice(0, 5)" 
            :key="index" 
            class="ranking-item"
          >
            <div class="ranking-num" :class="'rank-' + (index + 1)">
              {{ index + 1 }}
            </div>
            <div class="ranking-info">
              <span class="ranking-name">{{ item.name || '未知菜品' }}</span>
              <div class="ranking-progress">
                <div 
                  class="progress-bar yellow" 
                  :style="{ width: `${(item.number / maxDishSales) * 100}%` }"
                ></div>
              </div>
            </div>
            <span class="ranking-sales">{{ item.number || 0 }} 份</span>
          </div>
          <div v-if="topDish.length === 0" class="empty-state">
            <el-empty description="暂无菜品销量数据" />
          </div>
        </div>
      </div>

      <!-- 套餐统计 -->
      <div class="stats-card">
        <div class="section-header">
          <h3 class="section-title">套餐统计</h3>
          <span class="section-subtitle">TOP 5 热销套餐</span>
        </div>
        <div class="ranking">
          <div 
            v-for="(item, index) in topSetmeal.slice(0, 5)" 
            :key="index" 
            class="ranking-item"
          >
            <div class="ranking-num" :class="'rank-' + (index + 1)">
              {{ index + 1 }}
            </div>
            <div class="ranking-info">
              <span class="ranking-name">{{ item.name || '未知套餐' }}</span>
              <div class="ranking-progress">
                <div 
                  class="progress-bar yellow" 
                  :style="{ width: `${(item.number / maxSetmealSales) * 100}%` }"
                ></div>
              </div>
            </div>
            <span class="ranking-sales">{{ item.number || 0 }} 份</span>
          </div>
          <div v-if="topSetmeal.length === 0" class="empty-state">
            <el-empty description="暂无套餐销量数据" />
          </div>
        </div>
      </div>
    </div>

    <!-- 订单区间统计 -->
    <div class="order-list-section">
      <div class="section-header">
        <h3 class="section-title">订单概览</h3>
      </div>

      <div class="stats-grid">
        <div class="stat-box">
          <div class="stat-box-icon blue-bg">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-box-content">
            <span class="stat-box-value">{{ orderOverview.allOrders || 0 }}</span>
            <span class="stat-box-label">总订单数</span>
          </div>
        </div>
        <div class="stat-box">
          <div class="stat-box-icon green-bg">
            <el-icon><Ticket /></el-icon>
          </div>
          <div class="stat-box-content">
            <span class="stat-box-value">{{ orderOverview.completedOrders || 0 }}</span>
            <span class="stat-box-label">完成订单数</span>
          </div>
        </div>
        <div class="stat-box">
          <div class="stat-box-icon orange-bg">
            <el-icon><Promotion /></el-icon>
          </div>
          <div class="stat-box-content">
            <span class="stat-box-value">{{ orderOverview.refundOrders || 0 }}</span>
            <span class="stat-box-label">退款订单数</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
// 使用已确认存在的 Element Plus 图标
import { Crop, ShoppingCart, Document, Ticket, Wallet, Promotion, Money } from '@element-plus/icons-vue'

// 导入 workspace API
import { getDishOverview, getSetmealOverview, getOrderOverview, getTodayOrder, getDishScore, getSetmealScore, getTopDish, getTopSetmeal } from '@/api/aimin/workspace.js'

// 数据定义
const dishOverview = ref({})
const setmealOverview = ref({})
const orderOverview = ref({})
const todayOrder = ref({})
const dishScore = ref([])
const setmealScore = ref([])
const topDish = ref([])
const topSetmeal = ref([])

// 格式化数字
const formatNumber = (num) => {
  return Number(num).toFixed(2)
}

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${year}-${month}-${day} ${weekDays[now.getDay()]}`
})

// 最大菜品销量
const maxDishSales = computed(() => {
  const sales = topDish.value.map(item => item.number || 0)
  return Math.max(...sales, 1)
})

// 最大套餐销量
const maxSetmealSales = computed(() => {
  const sales = topSetmeal.value.map(item => item.number || 0)
  return Math.max(...sales, 1)
})

// API 请求函数
const fetchData = async () => {
  try {
    // 请求所有接口
    const [
      dishRes,
      setmealRes,
      orderRes,
      todayRes,
      dishScoreRes,
      setmealScoreRes,
      topDishRes,
      topSetmealRes
    ] = await Promise.all([
      getDishOverview(),
      getSetmealOverview(),
      getOrderOverview(),
      getTodayOrder(),
      getDishScore(),
      getSetmealScore(),
      getTopDish(),
      getTopSetmeal()
    ])

    // 赋值响应数据
    if (dishRes?.data) dishOverview.value = dishRes.data || {}
    if (setmealRes?.data) setmealOverview.value = setmealRes.data || {}
    if (orderRes?.data) orderOverview.value = orderRes.data || {}
    if (todayRes?.data) todayOrder.value = todayRes.data || {}
    if (dishScoreRes?.data) dishScore.value = dishScoreRes.data || []
    if (setmealScoreRes?.data) setmealScore.value = setmealScoreRes.data || []
    if (topDishRes?.data) topDish.value = topDishRes.data || []
    if (topSetmealRes?.data) topSetmeal.value = topSetmealRes.data || []
  } catch (error) {
    console.error('获取工作台数据失败', error)
    ElMessage.error('获取工作台数据失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
/* 系统色板 - 严格只使用以下 9 种颜色及其透明度变体 */
$sys-blue: #0A84FF;      // 系统蓝 - 主题主色
$sys-red: #FF453A;       // 系统红
$sys-orange: #FF9F0A;    // 系统橙
$sys-yellow: #FFD60A;    // 系统黄
$sys-green: #30D158;     // 系统绿
$sys-cyan: #40C8E0;      // 系统青
$sys-indigo: #5E5CE6;    // 系统靛蓝 - 深色/文字
$sys-purple: #BF5AF2;    // 系统紫
$sys-pink: #FF375F;      // 系统粉

/* 主题色变量（基于系统蓝） */
$primary: $sys-blue;
$primary-light: rgba(10, 132, 255, 0.1);   // 主色浅背景
$primary-dark: $sys-indigo;                  // 主色深色

.dashboard-container {
  margin: 20px;
  /* 仪表盘渐变背景使用系统蓝透明度 */
  background: linear-gradient(135deg, rgba(10, 132, 255, 0.08) 0%, rgba(10, 132, 255, 0.15) 100%);
  min-height: calc(100vh - 100px);
  padding: 20px;
  border-radius: 12px;
}

.page-header {
  padding: 20px 0;
  margin-bottom: 20px;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    /* 标题使用系统靛蓝 */
    color: $sys-indigo;
    margin: 0;
  }

  .page-desc {
    font-size: 14px;
    /* 描述使用系统靛蓝半透明 */
    color: rgba(94, 92, 230, 0.55);
    margin: 8px 0 0 0;
  }
}

/* 概览卡片 */
.overview-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;

  @media (max-width: 900px) {
    flex-direction: column;
  }
}

.overview-card {
  flex: 1;
  /* 卡片背景使用系统蓝极浅透明度 */
  background: rgba(10, 132, 255, 0.05);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  /* 阴影使用系统蓝透明度 */
  box-shadow: 0 4px 16px rgba(10, 132, 255, 0.15);
  /* 左侧装饰条使用系统蓝 */
  border-left: 4px solid $primary;
  transition: transform 0.3s, box-shadow 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 6px 24px rgba(10, 132, 255, 0.25);
  }
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;

  &.yellow {
    /* 图标背景渐变使用系统黄到系统橙 */
    background: linear-gradient(135deg, $sys-yellow 0%, $sys-orange 100%);
    color: $sys-indigo;
  }
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: $sys-indigo;
  margin: 0 0 12px 0;
}

.card-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;

  /* 数值颜色使用色板 */
  &.green { color: $sys-green; }
  &.red { color: $sys-red; }
  &.blue { color: $sys-blue; }
  &.orange { color: $primary; }
}

.stat-label {
  font-size: 12px;
  /* 标签使用系统靛蓝半透明 */
  color: rgba(94, 92, 230, 0.55);
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  /* 分隔线使用系统蓝半透明 */
  background: rgba(10, 132, 255, 0.2);
}

.card-total {
  font-size: 13px;
  color: rgba(94, 92, 230, 0.55);
  margin: 8px 0 0 0;
}

/* 今日订单区域 */
.order-view-section {
  background: rgba(10, 132, 255, 0.05);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(10, 132, 255, 0.15);
  /* 顶部装饰条使用系统蓝 */
  border-top: 4px solid $primary;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: $sys-indigo;
  margin: 0;
}

.section-date {
  font-size: 14px;
  color: rgba(94, 92, 230, 0.55);
}

.section-subtitle {
  font-size: 13px;
  color: rgba(94, 92, 230, 0.55);
  /* 副标题背景使用系统蓝浅透明度 */
  background: rgba(10, 132, 255, 0.08);
  padding: 4px 12px;
  border-radius: 12px;
}

.order-stats {
  display: flex;
  justify-content: space-around;
  padding-bottom: 20px;
  /* 底部分隔线使用系统蓝半透明 */
  border-bottom: 1px solid rgba(10, 132, 255, 0.1);
  margin-bottom: 20px;

  @media (max-width: 600px) {
    flex-wrap: wrap;
    gap: 16px;
  }
}

.order-stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;

  &.yellow-bg {
    /* 图标背景使用主色浅背景 */
    background: $primary-light;
    color: $primary;
  }
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 20px;
  font-weight: 700;
  color: $sys-indigo;

  &.highlight {
    /* 高亮数值使用系统蓝 */
    color: $primary;
    font-size: 24px;
  }
}

.stat-name {
  font-size: 12px;
  color: rgba(94, 92, 230, 0.55);
  margin-top: 4px;
}

/* 统计图表区域 */
.statistics-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;

  > div {
    flex: 1;
  }

  @media (max-width: 1200px) {
    flex-direction: column;
  }
}

.stats-card {
  background: rgba(10, 132, 255, 0.05);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(10, 132, 255, 0.15);
  border-top: 4px solid $primary;
}

.ranking {
  margin-top: 16px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  /* 底部分隔线使用系统蓝浅透明度 */
  border-bottom: 1px solid rgba(10, 132, 255, 0.08);

  &:last-child {
    border-bottom: none;
  }
}

.ranking-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  /* 默认排名背景使用系统蓝浅透明度 */
  background: rgba(10, 132, 255, 0.08);
  color: rgba(94, 92, 230, 0.55);
  margin-right: 12px;

  /* 第一名：系统黄到系统橙渐变 */
  &.rank-1 {
    background: linear-gradient(135deg, $sys-yellow 0%, $sys-orange 100%);
    color: $sys-indigo;
  }

  /* 第二名：系统青到系统靛蓝渐变 */
  &.rank-2 {
    background: linear-gradient(135deg, $sys-cyan 0%, $sys-indigo 100%);
    color: $sys-yellow;
  }

  /* 第三名：系统橙到系统紫渐变 */
  &.rank-3 {
    background: linear-gradient(135deg, $sys-orange 0%, $sys-purple 100%);
    color: $sys-yellow;
  }
}

.ranking-info {
  flex: 1;
  min-width: 0;
}

.ranking-name {
  font-size: 14px;
  color: $sys-indigo;
  display: block;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ranking-progress {
  height: 6px;
  /* 进度条背景使用系统蓝浅透明度 */
  background: rgba(10, 132, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  border-radius: 3px;
  transition: width 0.5s ease;

  /* 进度条使用系统黄到系统蓝渐变 */
  &.yellow {
    background: linear-gradient(90deg, $sys-yellow 0%, $primary 100%);
  }
}

.ranking-sales {
  font-size: 14px;
  font-weight: 600;
  /* 销量使用系统蓝 */
  color: $primary;
  margin-left: 16px;
  min-width: 50px;
  text-align: right;
}

.empty-state {
  padding: 40px;
}

/* 订单区间统计 */
.order-list-section {
  background: rgba(10, 132, 255, 0.05);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(10, 132, 255, 0.15);
  border-top: 4px solid $primary;
}

.range-select {
  width: 140px;
}

.stats-grid {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.stat-box {
  flex: 1;
  min-width: 200px;
  /* 统计盒背景使用主色浅背景 */
  background: $primary-light;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.3s, box-shadow 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(10, 132, 255, 0.2);
  }
}

.stat-box-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;

  /* 蓝色图标渐变：系统蓝到系统靛蓝 */
  &.blue-bg {
    background: linear-gradient(135deg, $sys-blue 0%, $sys-indigo 100%);
    color: $sys-yellow;
  }

  /* 绿色图标渐变：系统绿到系统青 */
  &.green-bg {
    background: linear-gradient(135deg, $sys-green 0%, $sys-cyan 100%);
    color: $sys-yellow;
  }

  /* 橙色图标渐变：系统橙到系统红 */
  &.orange-bg {
    background: linear-gradient(135deg, $sys-orange 0%, $sys-red 100%);
    color: $sys-yellow;
  }
}

.stat-box-content {
  display: flex;
  flex-direction: column;
}

.stat-box-value {
  font-size: 24px;
  font-weight: 700;
  color: $sys-indigo;
}

.stat-box-label {
  font-size: 12px;
  color: rgba(94, 92, 230, 0.55);
  margin-top: 4px;
}
</style>