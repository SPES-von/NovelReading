<!--
  我的书架页面
  展示当前用户在 user_bookshelf 表中的所有书籍，样式参考分类页书本模块
  需登录访问
-->
<template>
  <div class="bookshelf-page">
    <div class="container">
      <p v-if="!isLoggedIn" class="tip">请先登录后查看您的书架</p>
      <p v-else-if="loading" class="loading">加载中...</p>
      <p v-else-if="!novels.length" class="empty">书架中暂无书籍，快去添加吧~</p>
      <template v-else>
      <ul class="novel-list">
        <li v-for="n in paginatedNovels" :key="n.id" class="novel-item">
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
              <div class="bottom-row">
                <div v-if="n.tagNames && n.tagNames.length" class="tag-list">
                  <span v-for="tag in n.tagNames" :key="tag" class="tag">{{ tag }}</span>
                </div>
                <span class="latest-chapter"><span class="latest-label">最新章节：</span>{{ n.latestChapterTitle || '暂无' }}</span>
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
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { bookshelfApi } from '../api'
import { authUpdated } from '../store/auth'

const router = useRouter()
const novels = ref([])
const loading = ref(false)
const page = ref(0)
const pageSize = 8

const totalPages = computed(() => Math.max(1, Math.ceil(novels.value.length / pageSize)))
const paginatedNovels = computed(() => {
  const start = page.value * pageSize
  return novels.value.slice(start, start + pageSize)
})
/** 分页栏展示的页码数组（参考分类页） */
const visiblePages = computed(() => {
  const total = totalPages.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i)
  const cur = page.value
  let start = Math.max(0, cur - 2)
  let end = Math.min(total, start + 5)
  if (end - start < 5) start = Math.max(0, end - 5)
  return Array.from({ length: end - start }, (_, i) => start + i)
})

const isLoggedIn = computed(() => {
  authUpdated.value
  return !!localStorage.getItem('token')
})

function formatDate(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

async function loadBookshelf() {
  if (!isLoggedIn.value) {
    novels.value = []
    return
  }
  loading.value = true
  page.value = 0
  try {
    novels.value = await bookshelfApi.list() || []
  } catch (e) {
    if (e?.statusCode === 401) {
      router.push({ path: '/login', query: { redirect: '/bookshelf' } })
    }
    novels.value = []
  } finally {
    loading.value = false
  }
}

function goPage(p) {
  if (p >= 0 && p < totalPages.value) {
    page.value = p
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

watch(isLoggedIn, (v) => {
  if (v) loadBookshelf()
  else { novels.value = []; page.value = 0 }
})

onMounted(() => {
  if (isLoggedIn.value) loadBookshelf()
})
</script>

<style scoped>
.bookshelf-page {
  min-height: calc(100vh - var(--header-height));
  padding-top: 24px;
  background: var(--bg, #f5f5f5);
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 24px;
}
.tip, .loading, .empty {
  color: var(--text-muted);
  text-align: center;
  padding: 48px 24px;
}
.novel-list {
  list-style: none;
  padding: 0;
  margin: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}
.novel-item {
  border-bottom: 1px solid var(--border, #eee);
}
.novel-item:last-child { border-bottom: none; }
.novel-link {
  display: flex;
  gap: 16px;
  padding: 20px;
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
.info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.title {
  font-size: 1rem;
  font-weight: 600;
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
  color: #000;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.bottom-row {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 16px;
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
.latest-chapter {
  flex-shrink: 0;
  font-size: 0.85rem;
  color: #000;
}
.latest-label {
  color: #999;
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
