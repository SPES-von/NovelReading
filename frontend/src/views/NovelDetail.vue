<template>
  <div class="novel-detail-page">
    <Transition name="fade">
      <div v-if="successTip" class="success-toast">{{ successTip }}</div>
    </Transition>
    <div v-if="loading" class="container">加载中...</div>
    <div v-else-if="!novel" class="container">未找到该小说</div>
    <div v-else class="container layout">
      <!-- 左侧主内容 -->
      <main class="main">
        <div class="header-row">
          <div class="cover-wrap">
            <img :src="novel.coverUrl || 'https://via.placeholder.com/160x220/f0f0f0/999?text=无封面'" :alt="novel.title" class="cover" />
          </div>
          <div class="info">
            <h1 class="title">{{ novel.title }}</h1>
            <div v-if="tags.length" class="tags">
              <span v-if="novel.isCompleted" class="tag tag-completed">完结</span>
              <span class="tag tag-publisher">{{ novel.publisherName }}</span>
              <span v-for="t in tags" :key="t" class="tag">{{ t }}</span>
            </div>
            <p class="meta-line">
              最后更新: {{ formatDate(novel.updatedAt) }}
              <span v-if="novel.wordCount != null"> · 字数: {{ formatWordCount(novel.wordCount) }}</span>
              <span v-if="novel.flowerCount != null"> · 鲜花数：{{ novel.flowerCount }}</span>
            </p>
            <p v-if="novel.synopsis" class="synopsis">{{ novel.synopsis }}</p>
            <div class="action-row">
              <div class="action-buttons">
                <router-link :to="startReadLink" class="btn btn-primary">开始阅读</router-link>
                <button type="button" class="btn btn-outline" @click="onFavorite">加入书架</button>
              </div>
              <div class="action-right">
                <div v-if="isLoggedIn && progress" class="bookmark-info">
                  <span class="bookmark-current">当前阅读到：{{ progress.chapterTitle }}</span>
                </div>
                <button type="button" class="btn btn-flower" @click="onFlower">
                  <img :src="iconFlower" alt="" class="btn-flower-icon" />
                  <span>送鲜花</span>
                </button>
              </div>
            </div>
          </div>
        </div>
        <!-- 第 volume 卷目录（从左到右排列，每行整行下划线） -->
        <section v-if="filteredChapters.length" class="toc-section">
          <h2 class="toc-title">{{ volumeDisplay }}·目录</h2>
          <div class="toc-rows">
            <div v-for="(row, ri) in chapterRows" :key="ri" class="toc-row">
              <router-link
                v-for="ch in row"
                :key="ch.id"
                :to="`/novel/${novel.id}/read/${ch.id}`"
                class="toc-link"
              >{{ ch.title }}</router-link>
            </div>
          </div>
        </section>
        <!-- 评论区（样式参考图一：输入框 + 热门评论列表） -->
        <section v-if="novel?.id" class="comment-section">
          <div class="comment-input-wrap">
            <textarea
              v-model="commentContent"
              class="comment-textarea"
              placeholder="写下感想......"
              rows="3"
            />
            <div class="comment-input-actions">
              <span class="comment-civil">评论请文明</span>
              <button type="button" class="btn-comment btn-publish" :disabled="commentPublishing" @click="onPublishComment">发布</button>
            </div>
          </div>
          <h3 class="comment-list-title">热门评论</h3>
          <p v-if="commentLoading" class="comment-tip">加载中…</p>
          <p v-else-if="commentError" class="comment-error">{{ commentError }}</p>
          <ul v-else-if="commentList.length" class="comment-list">
            <li v-for="c in commentList" :key="c.id" class="comment-item">
              <img :src="c.userAvatarUrl || 'https://via.placeholder.com/40?text=头'" :alt="c.userNickname" class="comment-avatar" />
              <div class="comment-body">
                <div class="comment-meta">
                  <span class="comment-nickname">{{ c.userNickname || '用户' }}</span>
                  <span class="comment-time">{{ formatCommentTime(c.createdAt) }}</span>
                </div>
                <p class="comment-content">{{ c.content }}</p>
                <div class="comment-stats">
                  <button type="button" class="btn-stat" :disabled="statLoading[c.id]" @click="onLike(c)">
                    <img src="/img/点赞.jpg" alt="赞" class="stat-icon-img" /> {{ c.likeCount ?? 0 }}
                  </button>
                  <button type="button" class="btn-stat" :disabled="statLoading[c.id]" @click="onDislike(c)">
                    <img src="/img/踩.jpg" alt="踩" class="stat-icon-img" /> {{ c.dislikeCount ?? 0 }}
                  </button>
                </div>
              </div>
            </li>
          </ul>
          <p v-else class="comment-tip">暂无评论，快来写下第一条感想吧～</p>
        </section>
      </main>
      <!-- 右侧边栏 -->
      <aside class="sidebar">
        <div class="author-card">
          <img :src="iconAuthor" alt="作者" class="author-avatar" />
          <p class="author-label">作者</p>
          <p class="author-name">{{ novel.authorName }}</p>
          <p v-if="volumeCount > 0" class="author-vol">卷数：{{ volumeCount }}</p>
        </div>
        <div class="stats-card">
          <div class="stat">
            <span class="stat-label">本书字数</span>
            <span class="stat-value">{{ formatWordCount(novel.wordCount ?? 0) }}</span>
          </div>
          <div class="stat">
            <span class="stat-label">章节数</span>
            <span class="stat-value">{{ currentVolumeChapterCount }}</span>
          </div>
          <div class="stat">
            <span class="stat-label">收藏数</span>
            <span class="stat-value">{{ novel.favoriteCount ?? 0 }}</span>
          </div>
        </div>
        <div class="work-info">
          <h3 class="sidebar-title">作品信息</h3>
          <p class="work-desc">《{{ novel.title }}》是作者{{ novel.authorName }}创作的轻小说作品。</p>
        </div>
        <div v-if="volumeList.length" class="volume-buttons">
          <button
            v-for="vol in volumeList"
            :key="vol"
            type="button"
            class="btn-volume"
            :class="{ active: selectedVolume === vol }"
            @click="selectedVolume = vol"
          >{{ vol }}</button>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { novelApi, chapterApi, readingProgressApi, commentApi } from '../api'
import { authUpdated } from '../store/auth'

const route = useRoute()
const router = useRouter()
const iconFlower = '/img/鲜花.jpg'
const iconAuthor = '/img/作者.jpg'
const isLoggedIn = computed(() => {
  authUpdated.value
  return !!localStorage.getItem('token')
})
const successTip = ref('')
const novel = ref(null)
const chapters = ref([])
const selectedVolume = ref('1')
const loading = ref(true)
/** 当前用户对该书的阅读进度（书签），有则 { chapterId, chapterTitle } */
const progress = ref(null)
/** 评论区：列表、输入内容、加载/错误状态、发布中 */
const commentList = ref([])
const commentContent = ref('')
const commentLoading = ref(false)
const commentError = ref('')
const commentPublishing = ref(false)
/** 点赞/踩请求中，按评论 id 标记，用于禁用按钮 */
const statLoading = ref({})

const tags = computed(() => {
  const list = novel.value?.tagNames || []
  const extra = []
  if (novel.value?.isAnimated) extra.push('已动画化')
  return [...list, ...extra]
})

function formatWordCount(n) {
  if (n == null || n === 0) return '0'
  if (n >= 10000) return (n / 10000).toFixed(1).replace(/\.0$/, '') + '万'
  return String(n)
}

function formatDate(d) {
  if (!d) return '-'
  const date = new Date(d)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function formatCommentTime(d) {
  if (!d) return ''
  const date = new Date(d)
  const y = date.getFullYear()
  const m = date.getMonth() + 1
  const day = date.getDate()
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${y}年${m}月${day}日 ${h}:${min}`
}

async function fetchComments() {
  const novelId = novel.value?.id
  if (!novelId) return
  commentLoading.value = true
  commentError.value = ''
  try {
    commentList.value = await commentApi.getByNovel(novelId) || []
  } catch (e) {
    commentError.value = '加载评论失败，请稍后重试'
    commentList.value = []
  } finally {
    commentLoading.value = false
  }
}

async function onPublishComment() {
  if (!novel.value?.id) return
  if (!isLoggedIn.value) {
    successTip.value = '该功能请在登录后使用'
    setTimeout(() => { successTip.value = '' }, 2500)
    return
  }
  const content = (commentContent.value || '').trim()
  if (!content) {
    successTip.value = '请输入你想评论的内容'
    setTimeout(() => { successTip.value = '' }, 1500)
    return
  }
  commentPublishing.value = true
  try {
    await commentApi.publish(novel.value.id, content)
    commentContent.value = ''
    successTip.value = '发布成功'
    setTimeout(() => { successTip.value = '' }, 1500)
    await fetchComments()
  } catch (e) {
    if (e?.statusCode === 401) {
      router.push({ path: '/login', query: { redirect: route.fullPath } })
    } else {
      successTip.value = e?.message || '发布失败，请稍后重试'
      setTimeout(() => { successTip.value = '' }, 2000)
    }
  } finally {
    commentPublishing.value = false
  }
}

async function onLike(c) {
  const novelId = novel.value?.id
  if (!novelId || !c?.id) return
  statLoading.value = { ...statLoading.value, [c.id]: true }
  try {
    await commentApi.like(novelId, c.id)
    await fetchComments()
  } catch (_) {}
  statLoading.value = { ...statLoading.value, [c.id]: false }
}

async function onDislike(c) {
  const novelId = novel.value?.id
  if (!novelId || !c?.id) return
  statLoading.value = { ...statLoading.value, [c.id]: true }
  try {
    await commentApi.dislike(novelId, c.id)
    await fetchComments()
  } catch (_) {}
  statLoading.value = { ...statLoading.value, [c.id]: false }
}

const volumeList = computed(() => {
  const list = chapters.value
  const map = new Map() // volume -> volumeSort（同一卷取首次出现的 排序 值）
  for (const c of list) {
    const v = c.volume != null && c.volume !== '' ? String(c.volume) : '1'
    if (!map.has(v)) map.set(v, c.volumeSort != null ? c.volumeSort : 0)
  }
  return [...map.entries()]
    .sort((a, b) => (a[1] ?? 0) - (b[1] ?? 0))
    .map(([vol]) => vol)
})
const volumeCount = computed(() => volumeList.value.length)
const currentVolumeChapterCount = computed(() => filteredChapters.value.length)
const filteredChapters = computed(() => {
  const v = selectedVolume.value
  return chapters.value.filter(c => (c.volume != null && c.volume !== '' ? String(c.volume) : '1') === v)
})
const CHAPTERS_PER_ROW = 2
const chapterRows = computed(() => {
  const list = filteredChapters.value
  const rows = []
  for (let i = 0; i < list.length; i += CHAPTERS_PER_ROW) {
    rows.push(list.slice(i, i + CHAPTERS_PER_ROW))
  }
  return rows
})
const firstChapterId = computed(() => {
  const list = filteredChapters.value
  return list.length ? list[0].id : novel.value?.id
})

/** 全书第一章 id（volumeSort、sort_order 最小），无书签时「开始阅读」用此 */
const defaultFirstChapterId = computed(() => {
  const list = chapters.value
  return list.length ? list[0].id : novel.value?.id
})

/** 开始阅读跳转：有书签则打开书签章节，否则打开全书第一章 */
const startReadChapterId = computed(() => {
  if (progress.value?.chapterId) return progress.value.chapterId
  return defaultFirstChapterId.value
})

const startReadLink = computed(() => {
  const nid = novel.value?.id
  const cid = startReadChapterId.value
  if (!nid || !cid) return { path: '#' }
  const query = progress.value?.bookmark != null ? { bookmark: progress.value.bookmark } : {}
  return { path: `/novel/${nid}/read/${cid}`, query }
})
const numMap = { 1: '一', 2: '二', 3: '三', 4: '四', 5: '五', 6: '六', 7: '七', 8: '八', 9: '九', 10: '十' }
const volumeDisplay = computed(() => selectedVolume.value || '第一卷')

async function fetchNovel() {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    novel.value = await novelApi.getById(id)
    chapters.value = []
    progress.value = null
    if (novel.value?.id) {
      try {
        // 详情页目录数据：来自 /chapters/novel/{novelId}，后端已按卷序+章序排好
        chapters.value = await chapterApi.getByNovel(novel.value.id) || []
        const vols = (chapters.value || []).reduce((acc, c) => {
          const v = c.volume != null && c.volume !== '' ? String(c.volume) : '1'
          if (!acc.includes(v)) acc.push(v)
          return acc
        }, [])
        const fromQuery = route.query.volume
        selectedVolume.value = (fromQuery && vols.includes(fromQuery)) ? fromQuery : (vols.length ? vols[0] : '1')
        if (isLoggedIn.value) {
          try {
            const res = await readingProgressApi.getProgress(novel.value.id)
            progress.value = res ?? null
          } catch (_) {
            progress.value = null
          }
        }
      } catch (_) {}
    }
  } catch (e) {
    novel.value = null
    chapters.value = []
  } finally {
    loading.value = false
  }
}

async function onFlower() {
  if (!novel.value?.id) return
  if (!isLoggedIn.value) {
    successTip.value = '该功能请在登录后使用'
    setTimeout(() => { successTip.value = '' }, 2500)
    return
  }
  try {
    await novelApi.flower(novel.value.id)
    novel.value = { ...novel.value, flowerCount: (novel.value.flowerCount ?? 0) + 1 }
  } catch (e) {
    if (e?.statusCode === 401) {
      successTip.value = '该功能请在登录后使用'
      setTimeout(() => { successTip.value = '' }, 2500)
    }
  }
}

async function onFavorite() {
  if (!novel.value?.id) return
  if (!isLoggedIn.value) {
    successTip.value = '请先登录后再加入书架'
    setTimeout(() => { successTip.value = '' }, 2000)
    return
  }
  try {
    const res = await novelApi.favorite(novel.value.id)
    successTip.value = res?.added === false ? '书架中已存在此书' : '加入成功'
    if (res?.added !== false) {
      novel.value = { ...novel.value, favoriteCount: (novel.value.favoriteCount ?? 0) + 1 }
    }
    setTimeout(() => { successTip.value = '' }, 2000)
  } catch (e) {
    if (e?.statusCode === 401) {
      successTip.value = '请先登录后再加入书架'
      setTimeout(() => { successTip.value = '' }, 2000)
    }
  }
}

watch(() => route.params.id, fetchNovel, { immediate: true })
watch(() => novel.value?.id, (id) => { if (id) fetchComments() }, { immediate: true })
</script>

<style scoped>
.novel-detail-page {
  min-height: calc(100vh - var(--header-height));
  padding-top: 24px;
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
.fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.main {
  flex: 0 0 90%;
  min-width: 0;
}

.sidebar {
  flex: 0 0 10%;
  min-width: 140px;
  max-width: 180px;
}

.header-row {
  display: flex;
  gap: 24px;
  margin-bottom: 28px;
}

.cover-wrap {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.cover {
  width: 160px;
  height: 220px;
  object-fit: cover;
  border-radius: 6px;
}

.info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 10px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.tag {
  display: inline-block;
  padding: 2px 10px;
  font-size: 0.8rem;
  border-radius: 4px;
  background: #f0f0f0;
  color: var(--text);
}

.tag-completed { background: #e8f5e9; color: #2e7d32; }
.tag-publisher { background: #e3f2fd; color: #1565c0; }

.meta-line {
  color: var(--text-muted);
  font-size: 0.9rem;
  margin-bottom: 12px;
}

.synopsis {
  color: var(--text);
  font-size: 0.95rem;
  line-height: 1.65;
  margin-bottom: 16px;
}

.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
  max-width: 100%;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-right {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-right: 11px;
}

.bookmark-info {
  font-size: 0.9rem;
  color: var(--text-muted);
}

.bookmark-current {
  color: var(--text);
}

.btn {
  padding: 8px 20px;
  font-size: 0.95rem;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid var(--border);
  background: #fff;
  color: var(--text);
}

.btn-primary {
  background: var(--primary, #ec407a);
  color: #fff;
  border-color: var(--primary, #ec407a);
}

.btn-primary:hover {
  opacity: 0.9;
}

.btn-outline:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.btn-icon {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
}

.btn-icon:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.btn-flower {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 0;
  font-size: 0.9rem;
  color: #888;
  background: transparent;
  border: none;
  cursor: pointer;
}

.btn-flower:hover {
  color: #666;
}

.btn-flower-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
  filter: grayscale(1);
  opacity: 0.85;
}

.btn-flower:hover .btn-flower-icon {
  opacity: 1;
}

.action-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
  vertical-align: middle;
}

.toc-section {
  margin-top: 24px;
  padding-top: 0;
}

.toc-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 0;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}

.toc-rows {
  display: flex;
  flex-direction: column;
}

.toc-row {
  display: flex;
  flex-direction: row;
  border-bottom: 1px solid var(--border);
}

.toc-link {
  flex: 1;
  color: var(--text);
  text-decoration: none;
  font-size: 0.95rem;
  padding: 10px 8px;
  display: block;
  text-align: left;
  min-width: 0;
}

.toc-link:hover {
  color: var(--primary);
  text-decoration: underline;
  text-decoration-color: var(--primary);
  text-underline-offset: 4px;
}

.author-card {
  text-align: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
}

.author-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
  margin: 0 auto 6px;
}

.author-label {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-bottom: 2px;
}

.author-name {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--text);
  word-break: break-all;
}

.author-vol {
  font-size: 0.8rem;
  color: var(--text-muted);
  margin-top: 6px;
  margin-bottom: 0;
}

.stats-card {
  display: grid;
  grid-template-columns: 1fr;
  gap: 6px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
}

.stat {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.stat-value {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text);
}

.work-info {
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
}

.volume-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px 0;
}

.btn-volume {
  width: 100%;
  min-height: 40px;
  padding: 10px 14px;
  font-size: 0.95rem;
  border-radius: 6px;
  border: 1px solid var(--border);
  background: #fff;
  color: var(--text);
  cursor: pointer;
  box-sizing: border-box;
}

.btn-volume:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.btn-volume.active {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
}

.sidebar-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 6px;
}

.work-desc {
  font-size: 0.85rem;
  color: var(--text-muted);
  line-height: 1.5;
}

/* 评论区（参考图一：输入框 + 热门评论） */
.comment-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border);
}

.comment-input-wrap {
  margin-bottom: 24px;
}

.comment-textarea {
  width: 100%;
  padding: 12px 14px;
  font-size: 0.95rem;
  line-height: 1.5;
  border: 1px solid #ddd;
  border-radius: 6px;
  resize: vertical;
  min-height: 80px;
  box-sizing: border-box;
  color: var(--text);
  background: #fff;
}

.comment-textarea::placeholder {
  color: #999;
}

.comment-textarea:focus {
  outline: none;
  border-color: var(--primary, #ec407a);
}

.comment-input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  flex-wrap: wrap;
  gap: 10px;
}

.comment-civil {
  font-size: 0.85rem;
  color: var(--text-muted);
}

.comment-hint {
  font-size: 0.85rem;
  color: var(--text-muted);
  margin: 0 0 8px;
}

.comment-buttons {
  display: flex;
  gap: 10px;
}

.btn-comment {
  padding: 6px 16px;
  font-size: 0.9rem;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
}

.btn-comment:hover:not(:disabled) {
  border-color: var(--primary);
  color: var(--primary);
}

.btn-publish {
  background: var(--primary, #ec407a);
  border-color: var(--primary, #ec407a);
  color: #fff;
}

.btn-publish:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-publish:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.comment-list-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text);
  margin: 0 0 16px;
  padding-bottom: 8px;
}

.comment-tip, .comment-error {
  font-size: 0.9rem;
  color: var(--text-muted);
  margin: 0 0 16px;
}

.comment-error { color: #c0392b; }

.comment-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid var(--border);
}

.comment-item:last-child { border-bottom: none; }

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  background: #f0f0f0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.comment-nickname {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text);
}

.comment-time {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.comment-content {
  font-size: 0.95rem;
  color: var(--text);
  line-height: 1.55;
  margin: 0 0 8px;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-stats {
  display: flex;
  gap: 12px;
  font-size: 0.85rem;
  color: var(--text-muted);
  margin-left: auto;
  justify-content: flex-end;
}

.btn-stat {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  font-size: 0.85rem;
  color: var(--text-muted);
  background: #f5f5f5;
  border: 1px solid #eee;
  border-radius: 4px;
  cursor: pointer;
}

.btn-stat:hover:not(:disabled) {
  border-color: var(--primary);
  color: var(--primary);
}

.btn-stat:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.stat-icon-img {
  width: 18px;
  height: 18px;
  object-fit: contain;
  vertical-align: middle;
}
</style>
