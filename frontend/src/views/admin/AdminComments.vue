<template>
  <div class="admin-panel">
    <div class="search-card">
      <div class="search-row">
        <input
          v-model="searchKeyword"
          type="text"
          class="search-input"
          placeholder="按评论内容或用户ID搜索"
          autocomplete="off"
        />
        <input
          v-model="searchDate"
          type="date"
          class="search-date"
          :class="{ 'is-empty': !searchDate }"
        />
        <button type="button" class="btn btn-search" @click.prevent="onSearch">搜索</button>
      </div>
    </div>
    <div class="table-wrap">
      <table class="data-table" v-if="comments.length">
        <thead>
          <tr>
            <th>评论内容</th>
            <th>评论时间</th>
            <th>用户ID</th>
            <th class="actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in comments" :key="c.id">
            <td class="cell-content">{{ c.content }}</td>
            <td>{{ formatTime(c.createdAt) }}</td>
            <td>{{ c.userId }}</td>
            <td class="actions">
              <button
                v-if="c.audit === 0"
                type="button"
                class="btn btn-audit"
                @click="onAudit(c)"
              >
                审核
              </button>
              <button
                v-else
                type="button"
                class="btn btn-audited"
                disabled
              >
                已审核
              </button>
              <button type="button" class="btn btn-danger" @click="onDelete(c)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else-if="!loading" class="empty-tip">暂无评论数据</div>
      <div v-else class="loading-tip">加载中…</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminCommentApi } from '../../api'

const comments = ref([])
const loading = ref(true)
const searchKeyword = ref('')
const searchDate = ref('')

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

async function fetchList() {
  loading.value = true
  try {
    const params = buildListParams()
    const data = await adminCommentApi.list(params || {})
    comments.value = Array.isArray(data) ? data : []
  } catch (e) {
    comments.value = []
    console.error(e)
  } finally {
    loading.value = false
  }
}

function onSearch() {
  fetchList()
}

function onAudit(c) {
  if (!confirm('确定通过该评论审核？通过后将在书籍详情页显示。')) return
  adminCommentApi
    .audit(c.id)
    .then(() => fetchList())
    .catch(err => alert(err?.message || '审核失败'))
}

function onDelete(c) {
  if (!confirm('确定删除该评论？')) return
  adminCommentApi
    .delete(c.id)
    .then(() => fetchList())
    .catch(err => alert(err?.message || '删除失败'))
}

onMounted(() => fetchList())
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
.search-date {
  width: 25%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
  box-sizing: border-box;
}
.search-date:focus {
  outline: none;
  border-color: #2c3e50;
}
/* 未选日期时不显示浏览器自带的 yyyy/mm/日 占位，显示为空白 */
.search-date.is-empty::-webkit-datetime-edit,
.search-date.is-empty::-webkit-datetime-edit-year-field,
.search-date.is-empty::-webkit-datetime-edit-month-field,
.search-date.is-empty::-webkit-datetime-edit-day-field {
  color: transparent;
}
.search-date.is-empty::-webkit-datetime-edit-text {
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
}
.cell-content {
  max-width: 360px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.actions .btn {
  margin-left: 8px;
}
.btn {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  border: 1px solid transparent;
}
.btn-audit {
  background: #e3f2fd;
  color: #1565c0;
  border-color: #90caf9;
}
.btn-audited {
  background: #f5f5f5;
  color: #9e9e9e;
  border-color: #e0e0e0;
  cursor: not-allowed;
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
</style>
