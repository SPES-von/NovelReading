<template>
  <div class="browse-page">
    <div class="container">
      <!-- 筛选区 -->
      <section class="filter-section">
        <div class="filter-row">
          <span class="filter-label">作品主题：</span>
          <div class="filter-tags">
            <button type="button" class="filter-tag" :class="{ active: !query.tagId }" @click="setFilter('tagId', null)">不限</button>
            <button
              v-for="t in tags"
              :key="t.id"
              type="button"
              class="filter-tag"
              :class="{ active: query.tagId === t.id }"
              @click="setFilter('tagId', t.id)"
            >{{ t.name }}</button>
          </div>
        </div>
        <div class="filter-row">
          <span class="filter-label">是否动画化：</span>
          <div class="filter-tags">
            <button type="button" class="filter-tag" :class="{ active: query.isAnimated == null }" @click="setFilter('isAnimated', null)">不限</button>
            <button type="button" class="filter-tag" :class="{ active: query.isAnimated === 1 }" @click="setFilter('isAnimated', 1)">已动画化</button>
            <button type="button" class="filter-tag" :class="{ active: query.isAnimated === 0 }" @click="setFilter('isAnimated', 0)">未动画化</button>
          </div>
        </div>
        <div class="filter-row">
          <span class="filter-label">作品字数：</span>
          <div class="filter-tags">
            <button type="button" class="filter-tag" :class="{ active: !query.wordCountRange }" @click="setFilter('wordCountRange', null)">不限</button>
            <button type="button" class="filter-tag" :class="{ active: query.wordCountRange === 'under30' }" @click="setFilter('wordCountRange', 'under30')">30万以下</button>
            <button type="button" class="filter-tag" :class="{ active: query.wordCountRange === '30_50' }" @click="setFilter('wordCountRange', '30_50')">30-50万</button>
            <button type="button" class="filter-tag" :class="{ active: query.wordCountRange === '50_100' }" @click="setFilter('wordCountRange', '50_100')">50-100万</button>
            <button type="button" class="filter-tag" :class="{ active: query.wordCountRange === 'over100' }" @click="setFilter('wordCountRange', 'over100')">100万以上</button>
          </div>
        </div>
        <div class="filter-row">
          <span class="filter-label">更新时间：</span>
          <div class="filter-tags">
            <button type="button" class="filter-tag" :class="{ active: query.updatedWithinDays == null }" @click="setFilter('updatedWithinDays', null)">不限</button>
            <button type="button" class="filter-tag" :class="{ active: query.updatedWithinDays === 3 }" @click="setFilter('updatedWithinDays', 3)">三日内</button>
            <button type="button" class="filter-tag" :class="{ active: query.updatedWithinDays === 7 }" @click="setFilter('updatedWithinDays', 7)">七日内</button>
            <button type="button" class="filter-tag" :class="{ active: query.updatedWithinDays === 15 }" @click="setFilter('updatedWithinDays', 15)">半个月内</button>
            <button type="button" class="filter-tag" :class="{ active: query.updatedWithinDays === 30 }" @click="setFilter('updatedWithinDays', 30)">一个月内</button>
          </div>
        </div>
      </section>

      <!-- 排序方式 -->
      <section class="sort-section">
        <span class="sort-label">排序方式：</span>
        <div class="sort-tags">
          <button type="button" class="sort-tag" :class="{ active: query.sort === 'flower' }" @click="setFilter('sort', 'flower')">鲜花数</button>
          <button type="button" class="sort-tag" :class="{ active: query.sort === 'favorite' }" @click="setFilter('sort', 'favorite')">收藏数</button>
          <button type="button" class="sort-tag" :class="{ active: query.sort === 'created' }" @click="setFilter('sort', 'created')">入库时间</button>
          <button type="button" class="sort-tag" :class="{ active: query.sort === 'updated' }" @click="setFilter('sort', 'updated')">更新时间</button>
        </div>
      </section>

      <!-- 列表 -->
      <section class="list-section">
        <p v-if="loading" class="loading">加载中...</p>
        <p v-else-if="!novels.length" class="empty">暂无符合条件的小说</p>
        <ul v-else class="novel-list">
          <li v-for="n in novels" :key="n.id" class="novel-item">
            <router-link :to="`/novel/${n.id}`" class="novel-link">
              <div class="cover-wrap">
                <img :src="n.coverUrl || 'https://via.placeholder.com/100x133/f0f0f0/999?text=无封面'" :alt="n.title" />
              </div>
              <div class="info">
                <h3 class="title">{{ n.title }}</h3>
                <p class="meta">{{ n.authorName }} | {{ n.publisherName }}</p>
                <p class="status-row">
                  <span class="status">{{ n.isCompleted ? '完结' : '连载' }}</span>
                  <span v-if="n.updatedAt" class="date">{{ formatDate(n.updatedAt) }}</span>
                </p>
                <p v-if="n.synopsis" class="synopsis">{{ n.synopsis }}</p>
                <div v-if="n.tagNames && n.tagNames.length" class="tag-list">
                  <span v-for="tag in n.tagNames" :key="tag" class="tag">{{ tag }}</span>
                </div>
              </div>
            </router-link>
          </li>
        </ul>
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
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { tagApi, browseApi } from '../api'

const route = useRoute()
const router = useRouter()
const tags = ref([])
const novels = ref([])
const loading = ref(false)
const totalPages = ref(0)
const totalElements = ref(0)
const page = ref(0)
const pageSize = 12

const query = ref({
  tagId: null,
  isAnimated: null,
  wordCountRange: null,
  updatedWithinDays: null,
  sort: 'flower'
})

/** 分页栏展示的页码数组（最多 5 个，当前页居中），与搜索页一致 */
const visiblePages = computed(() => {
  const total = totalPages.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i)
  const cur = page.value
  let start = Math.max(0, cur - 2)
  let end = Math.min(total, start + 5)
  if (end - start < 5) start = Math.max(0, end - 5)
  return Array.from({ length: end - start }, (_, i) => start + i)
})

function setFilter(key, value) {
  query.value = { ...query.value, [key]: value }
  page.value = 0
  applyQueryToRoute()
}

function setPage(p) {
  if (p < 0 || p >= totalPages.value) return
  page.value = p
  applyQueryToRoute()
}

/** 跳转到指定页并滚动到顶部（与搜索页行为一致） */
function goPage(p) {
  if (p >= 0 && p < totalPages.value) {
    setPage(p)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

function applyQueryToRoute() {
  const q = {}
  if (query.value.tagId != null) q.tagId = query.value.tagId
  if (query.value.isAnimated != null) q.isAnimated = query.value.isAnimated
  if (query.value.wordCountRange) q.wordCountRange = query.value.wordCountRange
  if (query.value.updatedWithinDays != null) q.updatedWithinDays = query.value.updatedWithinDays
  if (query.value.sort && query.value.sort !== 'flower') q.sort = query.value.sort
  if (page.value > 0) q.page = page.value
  router.replace({ path: '/browse', query: q })
}

function parseQueryFromRoute() {
  const q = route.query
  query.value = {
    tagId: q.tagId != null ? Number(q.tagId) : null,
    isAnimated: q.isAnimated != null ? Number(q.isAnimated) : null,
    wordCountRange: q.wordCountRange || null,
    updatedWithinDays: q.updatedWithinDays != null ? Number(q.updatedWithinDays) : null,
    sort: q.sort || 'flower'
  }
  page.value = q.page != null ? Math.max(0, parseInt(q.page, 10)) : 0
}

function formatDate(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

async function loadTags() {
  try {
    tags.value = await tagApi.list()
  } catch (e) {
    tags.value = []
  }
}

async function loadNovels() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize
    }
    if (query.value.tagId != null) params.tagId = query.value.tagId
    if (query.value.isAnimated != null) params.isAnimated = query.value.isAnimated
    if (query.value.wordCountRange) params.wordCountRange = query.value.wordCountRange
    if (query.value.updatedWithinDays != null) params.updatedWithinDays = query.value.updatedWithinDays
    if (query.value.sort) params.sort = query.value.sort
    const data = await browseApi.browse(params)
    novels.value = data.content || []
    totalPages.value = data.totalPages ?? 0
    totalElements.value = data.totalElements ?? 0
  } catch (e) {
    novels.value = []
    totalPages.value = 0
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

watch(() => ({ ...route.query }), () => {
  parseQueryFromRoute()
  loadNovels()
}, { immediate: false })

onMounted(() => {
  parseQueryFromRoute()
  loadTags()
  loadNovels()
})
</script>

<style scoped>
.browse-page {
  padding-top: 24px;
  min-height: 100vh;
  background: var(--bg, #f5f5f5);
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 24px;
}
.filter-section {
  background: #eee;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 2px rgba(0,0,0,.05);
}
.filter-row {
  margin-bottom: 12px;
}
.filter-row:last-child { margin-bottom: 0; }
.filter-label, .sort-label {
  display: inline-block;
  min-width: 88px;
  font-size: 0.9rem;
  color: var(--text-muted);
  vertical-align: top;
  margin-right: 8px;
}
.filter-tags, .sort-tags {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 8px;
}
.filter-tag, .sort-tag {
  padding: 6px 14px;
  font-size: 0.85rem;
  border: 1px solid var(--border, #e0e0e0);
  border-radius: 4px;
  background: #fff;
  color: var(--text);
  cursor: pointer;
}
.filter-tag:hover, .sort-tag:hover {
  border-color: var(--primary);
  color: var(--primary);
}
.filter-tag.active, .sort-tag.active {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
}
.sort-section {
  background: #eee;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0,0,0,.05);
}
.sort-tags { margin-left: 0; }
.list-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.loading, .empty {
  color: var(--text-muted);
  text-align: center;
  padding: 24px;
}
.novel-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.novel-item {
  border-bottom: 1px solid var(--border, #eee);
}
.novel-item:last-child { border-bottom: none; }
.novel-link {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  text-decoration: none;
  color: inherit;
}
.novel-link:hover .title { color: var(--primary); }
.cover-wrap {
  flex-shrink: 0;
  width: 100px;
  aspect-ratio: 3/4;
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
.title {
  font-size: 1rem;
  margin: 0 0 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.meta {
  font-size: 0.85rem;
  color: var(--text-muted);
  margin: 0 0 6px;
}
.status-row {
  font-size: 0.8rem;
  color: var(--text-muted);
  margin: 0 0 8px;
}
.status { margin-right: 12px; }
.synopsis {
  font-size: 0.85rem;
  color: var(--text-muted);
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.tag {
  display: inline-block;
  padding: 2px 8px;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 0.75rem;
  color: var(--text-muted);
}
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border, #eee);
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
  border: 1px solid var(--border, #ddd);
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
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
}
.page-info { font-size: 0.9rem; color: var(--text-muted); }
</style>
