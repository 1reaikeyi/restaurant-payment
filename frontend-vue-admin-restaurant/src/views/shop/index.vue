<template>
  <div class="shop-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">店铺营业状态</h2>
      <p class="page-subtitle">管理店铺的营业/打烊状态及订单提醒</p>
    </div>

    <!-- 状态展示卡片 -->
    <div class="status-card" :class="{ open: isOpen, closed: !isOpen }">
      <div class="status-icon">
        <el-icon :size="64">
          <Shop v-if="isOpen" />
          <CircleClose v-else />
        </el-icon>
      </div>
      <div class="status-text">
        <h3>{{ statusText }}</h3>
        <p>{{ statusDesc }}</p>
      </div>
      <el-tag
        :type="isOpen ? 'success' : 'info'"
        size="large"
        class="status-tag"
      >
        {{ isOpen ? '营业中' : '已打烊' }}
      </el-tag>
    </div>

    <!-- 营业状态切换 -->
    <div class="action-area">
      <div class="action-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>{{ actionTip }}</span>
      </div>

      <el-button
        :type="isOpen ? 'danger' : 'warning'"
        size="large"
        class="toggle-btn"
        :loading="loading"
        @click="handleToggleStatus"
      >
        <el-icon v-if="!loading">
          <SwitchButton v-if="isOpen" />
          <SwitchButton v-else />
        </el-icon>
        {{ isOpen ? '立即打烊' : '开始营业' }}
      </el-button>
    </div>

    <!-- 订单提醒按钮 -->
    <div class="reminder-section">
      <el-card class="reminder-card">
        <template #header>
          <div class="card-header">
            <el-icon><Bell /></el-icon>
            <span>订单提醒</span>
          </div>
        </template>
        <div class="reminder-buttons">
          <el-button
            type="primary"
            size="large"
            class="reminder-btn urgent-btn"
            @click="handleUrgent"
          >
            <el-icon><AlarmClock /></el-icon>
            催办提醒
          </el-button>
          <el-button
            type="warning"
            size="large"
            class="reminder-btn notify-btn"
            @click="handleNotify"
          >
            <el-icon><Message /></el-icon>
            下单提醒
          </el-button>
        </div>
        <div class="reminder-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>点击按钮可测试提醒音效，实际下单时将自动触发对应提醒音</span>
        </div>
      </el-card>
    </div>

    <!-- 报表导出 -->
    <div class="export-section">
      <el-card class="export-card">
        <template #header>
          <div class="card-header">
            <el-icon><Download /></el-icon>
            <span>报表导出</span>
          </div>
        </template>
        <div class="export-content">
          <div class="export-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>导出订单数据为Excel文件，方便进行数据分析和存档</span>
          </div>
          <el-button
            type="success"
            size="large"
            class="export-btn"
            :loading="exportLoading"
            @click="handleExportExcel"
          >
            <el-icon><Download /></el-icon>
            导出Excel报表
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 营业提示信息 -->
    <div class="info-section">
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <el-icon><HelpFilled /></el-icon>
            <span>营业提示</span>
          </div>
        </template>
        <ul class="tip-list">
          <li>
            <el-icon><Check /></el-icon>
            <span>营业状态下，用户可以在小程序正常下单</span>
          </li>
          <li>
            <el-icon><Check /></el-icon>
            <span>打烊状态下，用户将无法在小程序下单</span>
          </li>
          <li>
            <el-icon><Check /></el-icon>
            <span>下单时播放提醒音，催办时播放催办音</span>
          </li>
        </ul>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Shop,
  CircleClose,
  InfoFilled,
  SwitchButton,
  Bell,
  Check,
  AlarmClock,
  Message,
  HelpFilled,
  Download
} from '@element-plus/icons-vue'
import { getShopStatus, setShopStatus } from '@/api/shop'
import { exportExcel } from '@/api/excel'

// 引入音频文件
import previewAudio from '@/assets/preview.mp3'
import reminderAudio from '@/assets/reminder.mp3'

// 加载状态
const loading = ref(false)

// 导出加载状态
const exportLoading = ref(false)

// 店铺状态（true: 营业中, false: 已打烊）
const isOpen = ref(false)

// 状态文本
const statusText = computed(() => {
  return isOpen.value ? '店铺正在营业中' : '店铺当前已打烊'
})

// 状态描述
const statusDesc = computed(() => {
  return isOpen.value
    ? '顾客可以在小程序正常浏览和下单'
    : '顾客无法在小程序下单，请先开始营业'
})

// 操作提示
const actionTip = computed(() => {
  return isOpen.value
    ? '打烊后顾客将无法下单，请确认是否继续？'
    : '开始营业后顾客将可以正常下单'
})

// 播放音频
const playAudio = (audioSrc, message) => {
  const audio = new Audio(audioSrc)
  audio.play().catch(err => {
    console.log('音频播放失败:', err)
  })
  if (message) {
    ElMessage.success(message)
  }
}

// 催办提醒 - 播放 reminder.mp3
const handleUrgent = () => {
  playAudio(reminderAudio, '催办提醒音已播放')
}

// 下单提醒 - 播放 preview.mp3
const handleNotify = () => {
  playAudio(previewAudio, '下单提醒音已播放')
}

// 导出Excel报表
const handleExportExcel = async () => {
  exportLoading.value = true
  try {
    const res = await exportExcel()
    if (res?.data) {
      // 创建下载链接
      const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      // 生成文件名（带日期）
      const date = new Date()
      const fileName = `订单报表_${date.getFullYear()}${String(date.getMonth() + 1).padStart(2, '0')}${String(date.getDate()).padStart(2, '0')}.xlsx`
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      ElMessage.success('Excel报表导出成功')
    }
  } catch (error) {
    console.error('导出Excel失败:', error)
    ElMessage.error('导出Excel失败')
  } finally {
    exportLoading.value = false
  }
}

// 获取店铺状态
const fetchShopStatus = async () => {
  try {
    const res = await getShopStatus()
    if (res?.data) {
      // 后端返回 "营业中" 或 "已打烊"
      isOpen.value = res.data === '营业中' || res.data === '1'
    }
  } catch (error) {
    console.error('获取店铺状态失败:', error)
    ElMessage.error('获取店铺状态失败')
  }
}

// 切换营业状态
const handleToggleStatus = async () => {
  const action = isOpen.value ? '打烊' : '营业'
  const newStatus = isOpen.value ? 0 : 1

  try {
    await ElMessageBox.confirm(
      `确定要${action}吗？${isOpen.value ? '打烊后顾客将无法下单。' : '营业后顾客将可以正常下单。'}`,
      `确认${action}`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    const res = await setShopStatus(newStatus)
    if (res?.data) {
      isOpen.value = !isOpen.value
      ElMessage.success(`${action}成功`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换状态失败:', error)
      ElMessage.error('操作失败')
    }
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchShopStatus()
})
</script>

<style lang="scss" scoped>
/* 黄色主题变量 */
$yellow-primary: #e6a23c;
$yellow-light: #fdf6ec;
$yellow-dark: #d49133;

.shop-container {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 30px;

  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
  }

  .page-subtitle {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

/* 状态展示卡片 */
.status-card {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 50px 30px;
  border-radius: 12px;
  margin-bottom: 30px;
  transition: all 0.3s ease;

  &.open {
    background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
    border: 2px solid #67c23a;

    .status-icon {
      color: #67c23a;
    }

    .status-text h3 {
      color: #67c23a;
    }
  }

  &.closed {
    background: linear-gradient(135deg, #f4f4f5 0%, #e9e9eb 100%);
    border: 2px solid #909399;

    .status-icon {
      color: #909399;
    }

    .status-text h3 {
      color: #909399;
    }
  }

  .status-icon {
    margin-bottom: 16px;
    transition: all 0.3s ease;
  }

  .status-text {
    text-align: center;
    margin-bottom: 16px;

    h3 {
      font-size: 24px;
      font-weight: 600;
      margin: 0 0 8px 0;
    }

    p {
      font-size: 14px;
      color: #606266;
      margin: 0;
    }
  }

  .status-tag {
    font-size: 14px;
    padding: 8px 20px;
  }
}

/* 操作区域 */
.action-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;

  .action-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 16px;
    font-size: 14px;
    color: #606266;

    .el-icon {
      color: $yellow-primary;
    }
  }

  .toggle-btn {
    width: 200px;
    height: 50px;
    font-size: 16px;
    border-radius: 8px;

    /* 黄色主题按钮 */
    &.el-button--warning {
      background-color: $yellow-primary;
      border-color: $yellow-primary;
      color: #fff;

      &:hover {
        background-color: $yellow-dark;
        border-color: $yellow-dark;
      }
    }

    .el-icon {
      margin-right: 6px;
    }
  }
}

/* 报表导出区域 */
.export-section {
  margin-bottom: 30px;

  .export-card {
    :deep(.el-card__header) {
      padding: 15px 20px;
      background: $yellow-light;
      border-bottom: 1px solid #f0e6d3;
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 500;
      color: $yellow-dark;

      .el-icon {
        color: $yellow-primary;
      }
    }
  }

  .export-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
    gap: 15px;

    .export-tip {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: #606266;

      .el-icon {
        color: $yellow-primary;
      }
    }

    .export-btn {
      width: 180px;
      height: 48px;
      font-size: 15px;
      border-radius: 8px;

      &.el-button--success {
        background-color: #67c23a;
        border-color: #67c23a;

        &:hover {
          background-color: #529b2e;
          border-color: #529b2e;
        }
      }

      .el-icon {
        margin-right: 8px;
      }
    }
  }
}

/* 订单提醒区域 */
.reminder-section {
  margin-bottom: 30px;

  .reminder-card {
    :deep(.el-card__header) {
      padding: 15px 20px;
      background: $yellow-light;
      border-bottom: 1px solid #f0e6d3;
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 500;
      color: $yellow-dark;

      .el-icon {
        color: $yellow-primary;
      }
    }
  }

  .reminder-buttons {
    display: flex;
    justify-content: center;
    gap: 20px;
    margin: 20px 0;

    .reminder-btn {
      width: 160px;
      height: 48px;
      font-size: 15px;
      border-radius: 8px;

      .el-icon {
        margin-right: 8px;
        font-size: 18px;
      }
    }

    .urgent-btn {
      &.el-button--primary {
        background-color: #f56c6c;
        border-color: #f56c6c;

        &:hover {
          background-color: #e45a5a;
          border-color: #e45a5a;
        }
      }
    }

    .notify-btn {
      &.el-button--warning {
        background-color: $yellow-primary;
        border-color: $yellow-primary;
        color: #fff;

        &:hover {
          background-color: $yellow-dark;
          border-color: $yellow-dark;
        }
      }
    }
  }

  .reminder-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding-top: 15px;
    border-top: 1px dashed #ebeef5;
    font-size: 13px;
    color: #909399;

    .el-icon {
      color: $yellow-primary;
    }
  }
}

/* 信息区域 */
.info-section {
  max-width: 600px;
  margin: 0 auto;

  .info-card {
    :deep(.el-card__header) {
      padding: 15px 20px;
      background: $yellow-light;
      border-bottom: 1px solid #f0e6d3;
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 500;
      color: $yellow-dark;

      .el-icon {
        color: $yellow-primary;
      }
    }
  }

  .tip-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      font-size: 14px;
      color: #606266;

      &:last-child {
        border-bottom: none;
      }

      .el-icon {
        color: $yellow-primary;
        font-size: 16px;
      }
    }
  }
}
</style>
