<template>
  <div class="admin-panel">
    <h2 class="page-title">数据统计</h2>
    <p class="panel-desc">统计用户阅读数据、小说热度数据，生成运营分析报表。</p>

    <div class="toolbar">
      <button type="button" class="btn-refresh" @click="fetchOverview" :disabled="loading">刷新数据</button>
      <button type="button" class="btn-export" @click="exportReport" :disabled="loading || exporting">
        {{ exporting ? '导出中...' : '导出报表(CSV)' }}
      </button>
    </div>

    <div v-if="loading" class="loading-tip">加载中...</div>
    <div v-else-if="errorMsg" class="error-tip">{{ errorMsg }}</div>

    <div v-else class="content-wrap">
      <div class="card-grid">
        <div class="stat-card">
          <div class="label">总用户数</div>
          <div class="value">{{ fmt(overview.core?.totalUsers) }}</div>
        </div>
        <div class="stat-card">
          <div class="label">总小说数</div>
          <div class="value">{{ fmt(overview.core?.totalNovels) }}</div>
        </div>
        <div class="stat-card">
          <div class="label">累计阅读事件</div>
          <div class="value">{{ fmt(overview.core?.totalReadEvents) }}</div>
        </div>
        <div class="stat-card">
          <div class="label">累计评论数</div>
          <div class="value">{{ fmt(overview.core?.totalComments) }}</div>
        </div>
      </div>

      <div class="section">
        <h3>阅读统计</h3>
        <div class="mini-grid">
          <div class="mini-item">今日阅读事件：<b>{{ fmt(overview.reading?.todayReadEvents) }}</b></div>
          <div class="mini-item">近7日阅读事件：<b>{{ fmt(overview.reading?.sevenDayReadEvents) }}</b></div>
          <div class="mini-item">累计阅读用户：<b>{{ fmt(overview.reading?.totalReadingUsers) }}</b></div>
        </div>
      </div>

      <div class="section">
        <h3>用户活跃</h3>
        <div class="mini-grid">
          <div class="mini-item">今日活跃用户：<b>{{ fmt(overview.active?.todayActiveUsers) }}</b></div>
          <div class="mini-item">近7日活跃用户：<b>{{ fmt(overview.active?.sevenDayActiveUsers) }}</b></div>
          <div class="mini-item">今日新增用户：<b>{{ fmt(overview.active?.todayNewUsers) }}</b></div>
          <div class="mini-item">近7日新增用户：<b>{{ fmt(overview.active?.sevenDayNewUsers) }}</b></div>
          <div class="mini-item">近7日新增评论：<b>{{ fmt(overview.active?.sevenDayNewComments) }}</b></div>
        </div>
      </div>

      <div class="section">
        <h3>小说热度TOP10</h3>
        <table class="data-table">
          <thead>
            <tr>
              <th>排名</th>
              <th>小说ID</th>
              <th>小说名称</th>
              <th>阅读量</th>
              <th>阅读用户数</th>
              <th>热度分</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(n, idx) in overview.hotNovels || []" :key="n.novelId">
              <td>{{ idx + 1 }}</td>
              <td>{{ n.novelId }}</td>
              <td class="left">{{ n.title }}</td>
              <td>{{ fmt(n.viewCount) }}</td>
              <td>{{ fmt(n.readingUsers) }}</td>
              <td>{{ fmt(n.heatScore) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminStatisticsApi } from '../../api'

const loading = ref(true)
const exporting = ref(false)
const errorMsg = ref('')
const overview = ref({})

function fmt(val) {
  return Number(val || 0).toLocaleString('zh-CN')
}

async function fetchOverview() {
  loading.value = true
  errorMsg.value = ''
  try {
    const data = await adminStatisticsApi.overview()
    overview.value = data || {}
  } catch (e) {
    errorMsg.value = e?.message || '获取统计数据失败'
  } finally {
    loading.value = false
  }
}

async function exportReport() {
  exporting.value = true
  try {
    const blob = await adminStatisticsApi.exportReport()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `运营分析报表_${Date.now()}.csv`
    a.click()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    alert(e?.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(fetchOverview)
</script>

<style scoped>
.admin-panel {
  width: 98%;
}
.page-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #2c3e50;
}
.panel-desc {
  color: #666;
  margin-bottom: 16px;
  font-size: 0.95rem;
}
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}
.btn-refresh,
.btn-export {
  border: 1px solid #d9d9d9;
  background: #fff;
  border-radius: 6px;
  padding: 8px 14px;
  cursor: pointer;
}
.btn-export {
  background: #2c3e50;
  color: #fff;
  border-color: #2c3e50;
}
.btn-refresh:disabled,
.btn-export:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}
.loading-tip,
.error-tip {
  padding: 18px;
  background: #fff;
  border-radius: 8px;
  text-align: center;
}
.error-tip {
  color: #d93025;
}
.content-wrap {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.card-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 12px;
}
.stat-card {
  background: #fff;
  border: 1px solid #ececec;
  border-radius: 8px;
  padding: 14px;
}
.stat-card .label {
  font-size: 0.88rem;
  color: #666;
  margin-bottom: 8px;
}
.stat-card .value {
  font-size: 1.2rem;
  font-weight: 700;
  color: #2c3e50;
}
.section {
  background: #fff;
  border: 1px solid #ececec;
  border-radius: 8px;
  padding: 14px;
}
.section h3 {
  margin: 0 0 10px 0;
  font-size: 1rem;
  color: #2c3e50;
}
.mini-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(180px, 1fr));
  gap: 8px 12px;
}
.mini-item {
  font-size: 0.92rem;
  color: #888;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.92rem;
}
.data-table th,
.data-table td {
  border-bottom: 1px solid #f0f0f0;
  padding: 10px 8px;
  text-align: center;
}
.data-table th {
  background: #fafafa;
  color: #333;
}
.data-table td.left,
.data-table th.left {
  text-align: left;
}
</style>
