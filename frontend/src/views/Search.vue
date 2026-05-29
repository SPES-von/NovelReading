<!--
  搜索页视图 Search.vue
  功能：根据 URL 中的 keyword 展示小说搜索结果，支持分页；右侧展示点击榜、收藏榜
  路由：/search?keyword=xxx
-->
<template>
  <div class="search-page">
    <!-- 加入成功提示 -->
    <Transition name="fade">
      <div v-if="successTip" class="success-toast">{{ successTip }}</div>
    </Transition>
    <div class="layout">
      <!-- 左侧主区域：搜索结果列表（约占 80% 宽度） -->
      <div class="results-area">
        <!-- 有关键词时显示结果总数，无关键词时提示输入 -->
        <h2 v-if="keyword" class="results-title">共搜索到 {{ totalElements }} 部与「{{ keyword }}」相关结果</h2>
        <h2 v-else class="results-title">请输入关键词搜索</h2>

        <!-- 加载中 / 未输入提示 / 错误信息 -->
        <p v-if="loading" class="tip">加载中…</p>
        <p v-else-if="!keyword" class="tip">在顶部搜索框输入关键词后点击搜索</p>
        <p v-else-if="error" class="error-tip">{{ error }}</p>
        <!-- 有结果时：列表 + 分页 -->
        <template v-else-if="novels.length">
          <ul class="result-list">
            <li v-for="n in novels" :key="n.id" class="result-item">
              <!-- 封面图，点击进入详情 -->
              <router-link :to="`/novel/${n.id}`" class="cover-wrap">
                <img :src="n.coverUrl || 'https://via.placeholder.com/100x133/f0f0f0/999?text=无'" :alt="n.title" />
              </router-link>
              <!-- 文字信息：标题、作者/出版社、连载状态、字数、简介、标签 -->
              <div class="info">
                <h3 class="title">
                  <router-link :to="`/novel/${n.id}`" class="title-link">{{ n.title }}</router-link>
                </h3>
                <p class="meta">
                  {{ n.authorName }}<span v-if="n.publisherName"> · {{ n.publisherName }}</span>
                  <span class="status-badge">{{ n.isCompleted ? '完结' : '连载' }}</span>
                  <span v-if="n.wordCount" class="word-count">{{ formatWordCount(n.wordCount) }}</span>
                </p>
                <p v-if="n.synopsis" class="synopsis">{{ n.synopsis }}</p>
                <div v-if="n.tagNames && n.tagNames.length" class="tag-list">
                  <span v-for="tag in n.tagNames" :key="tag" class="tag">{{ tag }}</span>
                </div>
              </div>
              <!-- 操作按钮：书籍详情、开始阅读、加入书架（与封面同行居右） -->
              <div class="actions">
                <router-link :to="`/novel/${n.id}`" class="btn btn-detail">书籍详情</router-link>
                <router-link :to="`/novel/${n.id}`" class="btn btn-read">开始阅读</router-link>
                <button type="button" class="btn btn-shelf" @click.stop="onFavorite(n)">加入书架</button>
              </div>
            </li>
          </ul>
          <!-- 分页：首页、上一页、页码、下一页、末页、当前页/总页数 -->
          <div v-if="totalPages > 1" class="pagination">
            <button type="button" :disabled="page <= 0" @click="goPage(0)">«</button>
            <button type="button" :disabled="page <= 0" @click="goPage(page - 1)">‹</button>
            <span class="page-nums">
              <button
                v-for="p in visiblePages"
                :key="p"
                type="button"
                class="page-btn"
                :class="{ active: p === page }"
                @click="goPage(p)"
              >{{ p + 1 }}</button>
            </span>
            <button type="button" :disabled="page >= totalPages - 1" @click="goPage(page + 1)">›</button>
            <button type="button" :disabled="page >= totalPages - 1" @click="goPage(totalPages - 1)">»</button>
            <span class="page-info">{{ page + 1 }} / {{ totalPages }}</span>
          </div>
        </template>
        <!-- 有关键词但无结果 -->
        <p v-else class="empty-tip">暂无符合条件的小说</p>
      </div>

      <!-- 右侧边栏（约 20%）：点击榜、收藏榜，随页面一起滚动 -->
      <aside class="sidebar">
        <HotList
          :list="rankings.viewCount || []"
          title="点击榜"
          :show-rank="true"
          :first-only-cover="true"
          count-type="view"
          :show-more="true"
        />
        <HotList
          :list="rankings.favoriteCount || []"
          title="收藏榜"
          :show-rank="true"
          :first-only-cover="true"
          count-type="favorite"
          :show-more="true"
        />
      </aside>
    </div>
  </div>
</template>

<script setup>
/**
 * 搜索页逻辑
 * - 从 route.query.keyword 读取关键词，query._r 用于同一关键词再次搜索时强制刷新
 * - 分页状态在组件内维护，换页时请求对应页并滚动到顶部
 */
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { novelApi } from '../api'
import { authUpdated } from '../store/auth'
import HotList from '../components/HotList.vue'

const route = useRoute()

// ---------- 状态 ----------
/** 当前搜索关键词（来自 URL query，去除首尾空格） */
const keyword = computed(() => (route.query.keyword || '').trim())
/** 当前页的小说列表 */
const novels = ref([])
/** 符合条件的总条数（用于显示「共搜索到 N 部」） */
const totalElements = ref(0)
/** 总页数 */
const totalPages = ref(0)
/** 当前页码（0 起） */
const page = ref(0)
/** 每页条数 */
const pageSize = 10
/** 搜索请求是否进行中 */
const loading = ref(false)
/** 搜索失败时的错误文案 */
const error = ref('')
/** 右侧榜单数据：viewCount 点击榜，favoriteCount 收藏榜 */
const rankings = ref({ viewCount: [], favoriteCount: [] })

// ---------- 工具函数 ----------
/** 将字数格式化为「1.5万」或原数字字符串 */
function formatWordCount(n) {
  if (!n) return ''
  if (n >= 10000) return `${(n / 10000).toFixed(1)}万`
  return `${n}`
}

// ---------- 数据加载 ----------
/** 根据当前 keyword 和 page 请求搜索结果并更新 novels / totalElements / totalPages */
async function loadSearch() {
  if (!keyword.value) {
    novels.value = []
    totalElements.value = 0
    totalPages.value = 0
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await novelApi.search(keyword.value, page.value, pageSize)
    novels.value = res.content || []
    totalElements.value = res.totalElements ?? 0
    totalPages.value = res.totalPages ?? 0
  } catch (e) {
    error.value = '搜索失败，请稍后重试'
    novels.value = []
  } finally {
    loading.value = false
  }
}

/** 请求排行榜数据，填充右侧点击榜、收藏榜 */
async function loadRankings() {
  try {
    const res = await novelApi.getRankings()
    if (res && typeof res === 'object') {
      rankings.value = {
        viewCount: res.viewCount || [],
        favoriteCount: res.favoriteCount || []
      }
    }
  } catch (_) {}
}

// ---------- 分页 ----------
/** 切换到第 p 页（0 起），并平滑滚动到页面顶部 */
function goPage(p) {
  if (p >= 0 && p < totalPages.value) {
    page.value = p
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

/** 分页栏中要展示的页码数组（最多 5 个，当前页居中） */
const visiblePages = computed(() => {
  const total = totalPages.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i)
  const cur = page.value
  let start = Math.max(0, cur - 2)
  let end = Math.min(total, start + 5)
  if (end - start < 5) start = Math.max(0, end - 5)
  return Array.from({ length: end - start }, (_, i) => start + i)
})

// ---------- 操作 ----------
/** 是否已登录（依赖 authUpdated 以响应登录状态变化） */
const isLoggedIn = computed(() => {
  authUpdated.value
  return !!localStorage.getItem('token')
})

/** 加入成功提示文案（短暂显示） */
const successTip = ref('')

/** 将小说加入书架：未登录仅提示，已登录则调用接口并显示结果 */
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

// ---------- 监听与初始化 ----------
/** 搜索触发条件：关键词 + _r。同一关键词再次点击搜索时 Header 会改 _r，从而触发重置页码和重新请求 */
const searchTrigger = computed(() => `${keyword.value}|${route.query._r || 0}`)
/** 关键词或 _r 变化时重置到第一页 */
watch(searchTrigger, () => { page.value = 0 }, { immediate: true })
/** 触发条件或页码变化时重新请求搜索结果（immediate: true 确保首次进入带 keyword 的搜索页时立即请求） */
watch([searchTrigger, page], loadSearch, { immediate: true })
/** 进入页面时加载右侧榜单 */
loadRankings()
</script>

<style scoped>
/* ---------- 页面容器 ---------- */
.search-page {
  min-height: calc(100vh - var(--header-height));
  padding: 24px 0 48px;
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

/* 主布局：左结果区 + 右侧边栏，最大宽度 1200px */
.layout {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: flex-start;
  gap: 24px;
  box-sizing: border-box;
}

/* ---------- 左侧结果区 ---------- */
.results-area {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 24px;
}

.results-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px;
}

/* 提示文案：加载中、未输入、错误、无结果 */
.tip, .error-tip, .empty-tip {
  padding: 24px 0;
  color: #666;
}
.error-tip { color: #c0392b; }

.result-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

/* 单条结果：封面 | 信息 | 操作按钮 */
.result-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.result-item:last-child { border-bottom: none; }

/* 封面图固定尺寸 */
.cover-wrap {
  flex-shrink: 0;
  width: 100px;
  height: 133px;
  border-radius: 6px;
  overflow: hidden;
  background: #f0f0f0;
}

.cover-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info { flex: 1; min-width: 0; }

/* 操作按钮：与封面同行、垂直居中、靠右 */
.actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-self: center;
  margin-left: auto;
}

.title { margin: 0 0 8px; }
.title-link {
  font-size: 1rem;
  font-weight: 600;
  color: var(--primary, #e74c3c);
  text-decoration: none;
}
.title-link:hover { text-decoration: underline; }

.meta {
  font-size: 0.85rem;
  color: #666;
  margin: 0 0 8px;
}

/* 连载/完结标签 */
.status-badge {
  display: inline-block;
  margin-right: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
  background: #f0f0f0;
  color: #666;
}
.status-badge:empty { display: none; }
.word-count { margin-left: 8px; color: #999; }

/* 简介最多显示 3 行 */
.synopsis {
  font-size: 0.9rem;
  color: #666;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 0;
}

.tag {
  padding: 2px 10px;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 0.8rem;
  color: #666;
}

/* ---------- 按钮 ---------- */
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

.btn-detail {
  background: var(--primary, #e74c3c);
  border-color: var(--primary, #e74c3c);
  color: #fff;
  text-align: center;
}

.btn-detail:hover {
  opacity: 0.9;
  color: #fff;
}

.btn-read {
  background: var(--primary, #e74c3c);
  border-color: var(--primary, #e74c3c);
  color: #fff;
  text-align: center;
}

.btn-read:hover {
  opacity: 0.9;
  color: #fff;
}

.btn-shelf:hover {
  border-color: var(--primary, #fb7299);
  color: var(--primary, #fb7299);
}

/* ---------- 分页 ---------- */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.page-nums {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination button {
  min-width: 36px;
  padding: 6px 12px;
  font-size: 0.9rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination button:not(:disabled):hover,
.page-btn.active {
  background: var(--primary, #e74c3c);
  border-color: var(--primary, #e74c3c);
  color: #fff;
}

.page-info {
  font-size: 0.9rem;
  color: #666;
}

/* ---------- 右侧边栏 ---------- */
.sidebar {
  flex: 0 0 20%;
  min-width: 200px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  /* 不设置 sticky，随主内容区一起滚动 */
}

.sidebar .hot-list {
  flex-shrink: 0;
  width: 100%;
}

/* 小屏：左右改为上下排列，榜单横向排列 */
@media (max-width: 900px) {
  .layout { flex-direction: column; }
  .sidebar { flex: none; width: 100%; min-width: 0; flex-direction: row; flex-wrap: wrap; }
  .sidebar .hot-list { flex: 1; min-width: 240px; }
}
</style>
