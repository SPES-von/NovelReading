<!--
  管理端「系统设置」页：
  - 调用 GET /admin/settings/overview 一次拉取 systemParams、serverStatus、logs、generatedAt
  - 日志为后端聚合的业务活动流（注册/评论/阅读进度），非服务器 .log 文件
  - 改密码：PUT /admin/settings/password，成功后清 adminToken 并跳转登录
-->
<template>
  <div class="admin-panel">
    <h2 class="page-title">系统设置</h2>

    <p v-if="error" class="error-msg">{{ error }}</p>
    <p v-else-if="loading" class="loading-tip">加载中…</p>

    <div v-if="!loading && overview" class="grid">
      <!-- 系统参数 -->
      <section class="card">
        <h3 class="card-title">系统参数</h3>
        <div class="table-wrap">
          <table class="data-table">
            <tbody>
              <tr v-for="(v, k) in overview.systemParams" :key="k">
                <th>{{ labelForSystemParam(k) }}</th>
                <td class="cell-text">{{ formatCell(v) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- 服务器状态 -->
      <section class="card">
        <h3 class="card-title">服务器状态</h3>
        <div class="table-wrap">
          <table class="data-table">
            <tbody>
              <tr v-for="(v, k) in overview.serverStatus" :key="k">
                <th>{{ labelForServerStatus(k) }}</th>
                <td class="cell-text">{{ formatCell(v) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- 日志查看：overview.logs，每项含 type / time / content / source -->
      <section class="card logs-card">
        <div class="card-title-row">
          <h3 class="card-title">日志查看</h3>
          <span v-if="overview.generatedAt" class="meta-text">生成时间：{{ formatTime(overview.generatedAt) }}</span>
        </div>
        <div class="table-wrap">
          <table class="data-table data-table-logs" v-if="overview.logs && overview.logs.length">
            <thead>
              <tr>
                <th>类型</th>
                <th>时间</th>
                <th>内容</th>
                <th>来源</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(l, idx) in overview.logs" :key="idx">
                <td>{{ l.type }}</td>
                <td>{{ formatTime(l.time) }}</td>
                <td class="cell-content">{{ l.content }}</td>
                <td>{{ l.source }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-tip">暂无日志</div>
        </div>
      </section>
    </div>

    <!-- 修改密码 -->
    <section class="card password-card" v-if="!loading">
      <h3 class="card-title">管理员密码修改</h3>
      <form class="password-form" @submit.prevent="onChangePassword">
        <div class="form-row">
          <label>原密码</label>
          <input v-model="pwForm.oldPassword" type="password" autocomplete="current-password" required />
        </div>
        <div class="form-row">
          <label>新密码</label>
          <input v-model="pwForm.newPassword" type="password" autocomplete="new-password" required minlength="6" />
        </div>
        <div class="form-row">
          <label>确认新密码</label>
          <input v-model="pwForm.newPassword2" type="password" autocomplete="new-password" required minlength="6" />
        </div>
        <div class="form-actions">
          <button type="submit" class="btn btn-submit" :disabled="pwLoading">
            {{ pwLoading ? '提交中…' : '修改密码' }}
          </button>
          <span v-if="pwMsg" class="pw-msg">{{ pwMsg }}</span>
        </div>
      </form>
    </section>
  </div>
</template>

<style scoped>
.admin-panel {
  width: 98%;
  max-width: none;
  margin: 0 auto;
}

.page-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  margin-top: 18px;
}

@media (max-width: 980px) {
  .grid { grid-template-columns: 1fr; }
}

.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  padding: 18px 18px 8px;
  min-height: 180px;
}

.logs-card {
  grid-column: 1 / span 2;
}

@media (max-width: 980px) {
  .logs-card { grid-column: auto; }
}

.card-title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.card-title {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.meta-text {
  font-size: 0.85rem;
  color: #888;
}

.table-wrap {
  background: #fff;
  border-radius: 8px;
  overflow: visible;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #eee;
  text-align: left;
  font-size: 0.95rem;
}

.data-table th {
  width: 180px;
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.data-table-logs thead th {
  background: #f8f9fa;
}

.cell-text {
  color: #333;
}

.cell-content {
  max-width: 520px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-tip {
  padding: 18px 0;
  text-align: center;
  color: #888;
}

.password-card {
  margin-top: 18px;
  padding-bottom: 18px;
}

.password-form {
  margin-top: 12px;
}

.form-row {
  display: grid;
  grid-template-columns: 160px 1fr;
  gap: 12px;
  align-items: center;
  margin-bottom: 14px;
}

.form-row label {
  font-size: 0.9rem;
  color: #444;
}

.form-row input {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
}

.form-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.btn {
  padding: 10px 16px;
  border-radius: 6px;
  border: none;
  font-size: 0.95rem;
  cursor: pointer;
}

.btn-submit {
  background: #2c3e50;
  color: #fff;
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.pw-msg {
  color: #2c3e50;
  font-size: 0.9rem;
}

.error-msg {
  color: #c0392b;
  font-size: 0.95rem;
  margin-top: 12px;
}

.loading-tip {
  color: #666;
  margin-top: 12px;
}
</style>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { adminSettingsApi } from '../../api'

const loading = ref(true)
const error = ref('')
/** 接口返回的整体概览；null 表示未加载或失败 */
const overview = ref(null)
const router = useRouter()

/** 管理员密码修改表单与状态 */
const pwLoading = ref(false)
const pwMsg = ref('')
const pwForm = reactive({
  oldPassword: '',
  newPassword: '',
  newPassword2: ''
})

onMounted(() => {
  fetchOverview()
})

/** 加载系统设置总览（系统参数、服务器状态、聚合日志） */
async function fetchOverview() {
  loading.value = true
  error.value = ''
  try {
    const data = await adminSettingsApi.overview()
    overview.value = data || null
  } catch (e) {
    error.value = e?.message || '加载系统设置失败'
    overview.value = null
  } finally {
    loading.value = false
  }
}

/** 日志/生成时间等字段的本地化展示 */
function formatTime(val) {
  if (!val) return '-'
  try {
    const d = new Date(val)
    return d.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return String(val)
  }
}

/** 表格单元格：对象转 JSON 字符串，空值显示为 - */
function formatCell(v) {
  if (v === null || v === undefined) return '-'
  if (typeof v === 'object') return JSON.stringify(v)
  return String(v)
}

/** 将后端 systemParams 的英文 key 映射为中文表头 */
function labelForSystemParam(key) {
  const map = {
    adminCount: '管理员数量',
    userCount: '用户数量',
    bannedUserCount: '封禁用户数量',
    commentVisibilityRule: '评论展示规则',
    maintenanceMode: '维护模式（当前）'
  }
  return map[key] || key
}

/** 将后端 serverStatus 的英文 key 映射为中文表头 */
function labelForServerStatus(key) {
  const map = {
    osName: '操作系统',
    osVersion: '系统版本',
    javaVersion: 'Java 版本',
    availableProcessors: 'CPU 核心数',
    jvmUptimeSeconds: 'JVM 运行时长(秒)',
    jvmStartTime: 'JVM 启动时间',
    memoryUsedMB: '内存已使用(MB)',
    memoryTotalMB: '内存总量(MB)',
    memoryMaxMB: '内存最大(MB)'
  }
  return map[key] || key
}

/** 校验后提交改密；成功则清除本地管理员 token 并跳转 /admin/login */
async function onChangePassword() {
  pwMsg.value = ''
  if (!pwForm.oldPassword) {
    pwMsg.value = '请输入原密码'
    return
  }
  if (!pwForm.newPassword || pwForm.newPassword.length < 6) {
    pwMsg.value = '新密码至少 6 位'
    return
  }
  if (pwForm.newPassword !== pwForm.newPassword2) {
    pwMsg.value = '两次新密码不一致'
    return
  }
  pwLoading.value = true
  try {
    await adminSettingsApi.changePassword({
      oldPassword: pwForm.oldPassword,
      newPassword: pwForm.newPassword
    })
    pwMsg.value = '密码修改成功，即将跳转登录'
    pwForm.oldPassword = ''
    pwForm.newPassword = ''
    pwForm.newPassword2 = ''
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminId')
    setTimeout(() => {
      router.replace('/admin/login')
    }, 600)
  } catch (e) {
    pwMsg.value = e?.message || '密码修改失败'
  } finally {
    pwLoading.value = false
  }
}
</script>
