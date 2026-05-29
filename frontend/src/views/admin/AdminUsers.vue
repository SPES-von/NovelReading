<template>
  <div class="admin-panel">
    <div class="search-card">
      <div class="search-row">
        <input
          v-model="searchKeyword"
          type="text"
          class="search-input"
          placeholder="按用户ID、用户名或邮箱搜索"
          autocomplete="off"
        />
        <div class="search-date-wrap" @click="openDatePicker">
          <span v-if="!searchDate" class="date-placeholder">搜索注册日期</span>
          <input
            ref="dateInputRef"
            v-model="searchDate"
            type="date"
            class="search-date-input"
            :class="{ 'is-empty': !searchDate }"
            @change="onSearch"
          />
        </div>
        <button type="button" class="btn btn-search" @click.prevent="onSearch">搜索</button>
      </div>
    </div>
    <div class="table-wrap">
      <table class="data-table" v-if="pageUsers.length">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>注册时间</th>
            <th class="actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in pageUsers" :key="u.id">
            <td>{{ u.id }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.email }}</td>
            <td>{{ formatTime(u.createdAt) }}</td>
            <td class="actions">
              <div v-if="!isBanned(u)" class="select-wrapper">
                <button
                  type="button"
                  class="select-trigger"
                  :class="{ open: openSelectId === u.id }"
                  @click.stop="toggleSelect(u.id)"
                >
                  <span>{{ banChoice[u.id] ? labelOf(u.id) : '选择封禁时长' }}</span>
                  <span class="select-arrow">▼</span>
                </button>
                <ul v-show="openSelectId === u.id" class="select-dropdown">
                  <li
                    v-for="opt in banOptions"
                    :key="opt.hours"
                    @click.stop="chooseDuration(u.id, opt.hours)"
                  >
                    {{ opt.label }}
                  </li>
                </ul>
              </div>
              <button
                v-if="!isBanned(u)"
                type="button"
                class="btn btn-ban"
                :disabled="!banChoice[u.id]"
                @click="onBan(u)"
              >
                封禁
              </button>
              <button
                v-else
                type="button"
                class="btn btn-unban"
                @click="onUnban(u)"
              >
                解禁
              </button>
              <button type="button" class="btn btn-danger" @click="onDelete(u)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else-if="!loading" class="empty-tip">暂无用户数据</div>
      <div v-else class="loading-tip">加载中…</div>
      <div v-if="totalPages > 1 && !loading" class="pagination">
        <button type="button" class="page-btn" :disabled="currentPage <= 1" @click="currentPage = currentPage - 1">上一页</button>
        <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页，共 {{ users.length }} 人</span>
        <button type="button" class="page-btn" :disabled="currentPage >= totalPages" @click="currentPage = currentPage + 1">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { adminUserApi } from '../../api'

const PAGE_SIZE = 15
const users = ref([])
const loading = ref(true)
const currentPage = ref(1)
const banChoice = ref({})
const openSelectId = ref(null)
const searchKeyword = ref('')
const searchDate = ref('')
const dateInputRef = ref(null)

const totalPages = computed(() => Math.max(1, Math.ceil(users.value.length / PAGE_SIZE)))
const pageUsers = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return users.value.slice(start, start + PAGE_SIZE)
})

const banOptions = [
  { hours: 24, label: '24小时' },
  { hours: 64, label: '64小时' },
  { hours: 8760, label: '1年' },
  { hours: 26280, label: '3年' },
  { hours: 43800, label: '5年' },
  { hours: 87600, label: '10年' }
]

function isBanned(u) {
  return u.isBanned === 1
}

function labelOf(uid) {
  const hours = banChoice.value[uid]
  const opt = banOptions.find(o => o.hours === hours)
  return opt ? opt.label : ''
}

function toggleSelect(uid) {
  openSelectId.value = openSelectId.value === uid ? null : uid
}

function chooseDuration(uid, hours) {
  banChoice.value = { ...banChoice.value, [uid]: hours }
  openSelectId.value = null
}

function onCloseSelect(e) {
  if (e.target && !e.target.closest('.select-wrapper')) openSelectId.value = null
}

onMounted(() => {
  fetchList()
  document.addEventListener('click', onCloseSelect)
})

function formatTime(val) {
  if (!val) return '-'
  const d = new Date(val)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatDisplayDate(ymd) {
  if (!ymd) return ''
  const [y, m, d] = ymd.split('-').map(Number)
  return `${y}年${m}月${d}日`
}

function buildListParams() {
  const params = {}
  if (searchKeyword.value && searchKeyword.value.trim()) params.keyword = searchKeyword.value.trim()
  if (searchDate.value) {
    const [y, m, d] = searchDate.value.split('-').map(Number)
    params.year = y
    params.month = m
    params.day = d
  }
  return params
}

function onSearch() {
  fetchList()
}

function openDatePicker() {
  const el = dateInputRef.value
  if (el && typeof el.click === 'function') el.click()
}

async function fetchList() {
  loading.value = true
  try {
    const params = buildListParams()
    const data = await adminUserApi.list(params || {})
    users.value = Array.isArray(data) ? data : []
    banChoice.value = {}
    const maxPage = Math.max(1, Math.ceil(users.value.length / PAGE_SIZE))
    if (currentPage.value > maxPage) currentPage.value = maxPage
  } catch (e) {
    users.value = []
    console.error(e)
  } finally {
    loading.value = false
  }
}

function onBan(u) {
  const hours = banChoice.value[u.id]
  if (!hours) return
  const opt = banOptions.find(o => o.hours === hours)
  const label = opt ? opt.label : `${hours}小时`
  if (!confirm(`确定封禁用户「${u.username}」${label}？`)) return
  adminUserApi
    .ban(u.id, hours)
    .then(() => fetchList())
    .catch(err => alert(err?.message || '封禁失败'))
}

function onUnban(u) {
  if (!confirm(`确定解禁用户「${u.username}」？`)) return
  adminUserApi
    .unban(u.id)
    .then(() => fetchList())
    .catch(err => alert(err?.message || '解禁失败'))
}

function onDelete(u) {
  if (!confirm('是否删除该用户？')) return
  adminUserApi
    .delete(u.id)
    .then(() => fetchList())
    .catch(err => alert(err?.message || '删除失败'))
}

onUnmounted(() => document.removeEventListener('click', onCloseSelect))
</script>

<style scoped>
.admin-panel {
  width: 98%;
  max-width: none;
  margin: 0 auto;
}
.search-card {
  width: 100%;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
}
.search-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.search-input {
  width: 70%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
  box-sizing: border-box;
}
.search-input:focus {
  outline: none;
  border-color: #2c3e50;
}
.search-date-wrap {
  width: 25%;
  position: relative;
  cursor: pointer;
  min-height: 42px;
}
.search-date-wrap .date-placeholder {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.95rem;
  pointer-events: none;
  z-index: 0;
  color: #999;
}
.search-date-input {
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
  box-sizing: border-box;
  background: transparent;
  cursor: pointer;
}
.search-date-input:focus {
  outline: none;
  border-color: #2c3e50;
}
.search-date-input.is-empty::-webkit-datetime-edit,
.search-date-input.is-empty::-webkit-datetime-edit-year-field,
.search-date-input.is-empty::-webkit-datetime-edit-month-field,
.search-date-input.is-empty::-webkit-datetime-edit-day-field,
.search-date-input.is-empty::-webkit-datetime-edit-text {
  color: transparent;
}
.btn-search {
  width: 5%;
  min-width: 64px;
  padding: 10px 18px;
  font-size: 0.95rem;
  border-radius: 6px;
  border: none;
  background: #2c3e50;
  color: #fff;
  cursor: pointer;
}
.btn-search:hover {
  background: #34495e;
}
.table-wrap {
  background: #fff;
  border-radius: 8px;
  overflow: visible;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table th,
.data-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
}
.data-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}
.data-table th.actions,
.data-table td.actions {
  text-align: right;
  white-space: nowrap;
  overflow: visible;
}
.actions .btn {
  margin-left: 8px;
}
.select-wrapper {
  display: inline-block;
  position: relative;
  vertical-align: middle;
  margin-right: 8px;
}
.select-trigger {
  padding: 6px 10px;
  min-width: 120px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  background: #fff;
  cursor: pointer;
  text-align: left;
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
}
.select-arrow {
  font-size: 0.7rem;
  opacity: 0.8;
}
.select-trigger.open {
  border-color: #2c3e50;
  outline: none;
}
.select-trigger.open .select-arrow {
  transform: rotate(180deg);
}
.select-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  margin: 2px 0 0;
  padding: 4px 0;
  list-style: none;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  min-width: 120px;
  z-index: 10;
  text-align: left;
}
.select-dropdown li {
  padding: 8px 12px;
  cursor: pointer;
  font-size: 0.9rem;
  text-align: left;
}
.select-dropdown li:hover {
  background: #f0f0f0;
}
.btn {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  border: 1px solid transparent;
}
.btn-ban {
  background: #fff3e0;
  color: #e65100;
  border-color: #ffcc80;
}
.btn-ban:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-unban {
  background: #e8f5e9;
  color: #2e7d32;
  border-color: #a5d6a7;
}
.btn-danger {
  background: #ffebee;
  color: #c62828;
  border-color: #ef9a9a;
}
.btn:hover:not(:disabled) {
  opacity: 0.9;
}
.empty-tip,
.loading-tip {
  padding: 40px;
  text-align: center;
  color: #888;
}
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 16px;
  border-top: 1px solid #eee;
  background: #fafafa;
}
.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  font-size: 0.9rem;
}
.page-btn:hover:not(:disabled) {
  background: #f0f0f0;
  border-color: #2c3e50;
}
.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.page-info {
  font-size: 0.9rem;
  color: #666;
}
</style>
