<template>
  <div class="fullbook-page">
    <!-- 加入成功提示 -->
    <Transition name="fade">
      <div v-if="successTip" class="success-toast">{{ successTip }}</div>
    </Transition>
    <div class="layout">
      <!-- 左侧导航 15% -->
      <aside class="sidebar">
        <h2 class="sidebar-title">轻小说(完本)榜单</h2>
        <ul class="nav-list">
          <li v-for="tab in tabs" :key="tab.key">
            <button
              type="button"
              class="nav-item"
              :class="{ active: currentSort === tab.key }"
              @click="switchSort(tab.key)"
            >
              {{ tab.label }}
            </button>
          </li>
        </ul>
      </aside>

      <!-- 右侧内容 85% -->
      <main class="main">
        <div class="banner">
          <span class="banner-text">全本小说 - {{ currentTabLabel }}</span>
        </div>

        <p v-if="loading" class="tip">加载中…</p>
        <p v-else-if="error" class="error-tip">{{ error }}</p>
        <ul v-else class="book-list">
          <li v-for="(item, i) in list" :key="item.id" class="book-item">
            <span class="rank-badge">{{ i + 1 }}</span>
            <router-link :to="`/novel/${item.id}`" class="cover-wrap">
              <img
                :src="item.coverUrl || 'https://via.placeholder.com/100x133/f0f0f0/999?text=无'"
                :alt="item.title"
                class="cover-img"
              />
            </router-link>
            <div class="info">
              <h3 class="title">{{ item.title }}</h3>
              <p class="meta">
                {{ item.authorName }}<span v-if="item.publisherName"> · {{ item.publisherName }}</span>
                <span class="status">完结</span>
              </p>
              <p v-if="item.synopsis" class="synopsis">{{ item.synopsis }}</p>
              <p class="latest-row">
                <span class="latest-left">
                  <span class="latest-label">最新章节：</span>
                  <span class="latest-title">{{ item.latestChapterTitle || '暂无' }}</span>
                </span>
                <span v-if="item.updatedAt" class="latest-date">{{ formatDate(item.updatedAt) }}</span>
              </p>
              <div class="actions">
                <router-link :to="`/novel/${item.id}`" class="btn btn-read">立即阅读</router-link>
                <button type="button" class="btn btn-shelf" @click="onFavorite(item)">加入书架</button>
              </div>
            </div>
          </li>
        </ul>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { novelApi } from '../api'
import { authUpdated } from '../store/auth'

const route = useRoute()
const tabs = [
  { key: 'viewCount', label: '热度榜' },
  { key: 'flower', label: '鲜花榜' },
  { key: 'rating', label: '评分榜' },
  { key: 'favorite', label: '收藏榜' }
]

const currentSort = ref('viewCount')
const list = ref([])
const loading = ref(true)
const error = ref('')

const currentTabLabel = computed(() => {
  return tabs.find(t => t.key === currentSort.value)?.label || '热度榜'
})

function switchSort(key) {
  currentSort.value = key
}

function formatDate(val) {
  if (!val) return ''
  const d = new Date(val)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

async function loadList() {
  loading.value = true
  error.value = ''
  try {
    list.value = await novelApi.getCompletedChart(currentSort.value) || []
  } catch (e) {
    error.value = '获取完本榜单失败，请稍后重试'
    list.value = []
  } finally {
    loading.value = false
  }
}

/** 是否已登录（依赖 authUpdated 以响应登录状态变化） */
const isLoggedIn = computed(() => {
  authUpdated.value
  return !!localStorage.getItem('token')
})
/** 加入成功提示（短暂显示） */
const successTip = ref('')

async function onFavorite(novel) {
  if (!novel?.id) return
  if (!isLoggedIn.value) {
    successTip.value = '请先登录后再加入书架'
    setTimeout(() => { successTip.value = '' }, 2000)
    return
  }
  try {
    const res = await novelApi.favorite(novel.id)
    successTip.value = res?.added === false ? '书架中已存在此书' : '加入成功'
    setTimeout(() => { successTip.value = '' }, 2000)
  } catch (_) {
    if (_?.statusCode === 401) {
      successTip.value = '请先登录后再加入书架'
      setTimeout(() => { successTip.value = '' }, 2000)
    }
  }
}

watch(currentSort, loadList, { immediate: true })
</script>

<style scoped>
.fullbook-page {
  min-height: calc(100vh - var(--header-height));
  padding-top: 24px;
  padding-bottom: 48px;
  background: #f5f5f5;
  position: relative;
}

.success-toast {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  background: rgba(0, 0, 0, 0.75);
  color: #fff;
  border-radius: 8px;
  font-size: 0.95rem;
  z-index: 2000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.layout {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  gap: 24px;
  box-sizing: border-box;
}

.sidebar {
  flex: 0 0 15%;
  min-width: 140px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 16px 0;
  height: fit-content;
}

.sidebar-title {
  font-size: 1rem;
  font-weight: 700;
  color: #e74c3c;
  margin: 0 16px 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.nav-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-item {
  display: block;
  width: 100%;
  padding: 10px 20px;
  text-align: left;
  font-size: 0.9rem;
  color: #666;
  background: none;
  border: none;
  cursor: pointer;
  position: relative;
  box-sizing: border-box;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #999;
}

.nav-item.active {
  color: #e74c3c;
  font-weight: 500;
}

.nav-item.active::before {
  background: #e74c3c;
}

.nav-item:hover:not(.active) {
  color: #333;
}

.main {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.banner {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  padding: 24px 24px;
  min-height: 60px;
  display: flex;
  align-items: center;
}

.banner-text {
  font-size: 1.25rem;
  font-weight: 600;
  color: #fff;
}

.tip, .error-tip {
  padding: 24px;
  text-align: center;
  color: #666;
}

.error-tip { color: #c0392b; }

.book-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.book-item {
  display: flex;
  gap: 16px;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
  position: relative;
}

.book-item:last-child { border-bottom: none; }

.rank-badge {
  position: absolute;
  top: 16px;
  right: 24px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e74c3c;
  color: #fff;
  font-size: 0.9rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.cover-wrap {
  flex-shrink: 0;
  width: 100px;
  height: 133px;
  border-radius: 6px;
  overflow: hidden;
  background: #f0f0f0;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info {
  flex: 1;
  min-width: 0;
  padding-right: 40px;
}

.title {
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 6px;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  font-size: 0.85rem;
  color: #666;
  margin: 0 0 6px;
}

.status {
  margin-left: 8px;
  color: #999;
}

.synopsis {
  font-size: 0.85rem;
  color: #666;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.latest-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  font-size: 0.85rem;
  color: #666;
  margin: 0 0 12px;
}

.latest-left {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.latest-label { color: #999; }
.latest-title { margin-right: 8px; }
.latest-date { color: #999; flex-shrink: 0; }

.actions {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 8px 20px;
  font-size: 0.9rem;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
}

.btn-read {
  background: #e74c3c;
  border-color: #e74c3c;
  color: #fff;
}

.btn-read:hover {
  background: #c0392b;
  border-color: #c0392b;
  color: #fff;
}

.btn-shelf:hover {
  border-color: var(--primary, #fb7299);
  color: var(--primary, #fb7299);
}

@media (max-width: 768px) {
  .layout { flex-direction: column; }
  .sidebar { flex: none; width: 100%; min-width: 0; }
  .book-item { flex-wrap: wrap; }
  .rank-badge { top: 12px; right: 12px; }
  .info { padding-right: 0; }
}
</style>
