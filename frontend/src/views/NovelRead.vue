<template>
  <div class="novel-read" :class="{ 'theme-dark': appliedTheme === 'dark' }" :style="contentStyle">
    <Transition name="fade">
      <div v-if="toast" class="read-toast">
        <template v-if="toast === 'loginRequired'">该功能请在<router-link :to="{ path: '/login', query: { redirect: route.fullPath } }" class="toast-login">登录</router-link>后使用</template>
        <template v-else>{{ toast }}</template>
      </div>
    </Transition>

    <!-- 顶部栏：无头部导航，仅阅读设置 + 标记书签 | 给书送花 -->
    <header class="read-header">
      <div class="read-header-inner">
        <button type="button" class="btn-settings" @click="showSettings = true">
          <span class="settings-icon">⚙</span>
          <span>阅读设置</span>
        </button>
        <div class="read-actions">
          <button type="button" class="action-link action-listen" :class="{ active: isListening }" @click="toggleListen">
            {{ isListening ? '停止听书' : '一键听书' }}
          </button>
          <span class="action-sep">|</span>
          <button type="button" class="action-link" @click="onBookmark">标记书签</button>
          <span class="action-sep">|</span>
          <button type="button" class="action-link" @click="onFlower">给书送花</button>
        </div>
      </div>
    </header>

    <!-- 正文区域：正文模块 + 下方同宽模块（返回目录、+书签、下一页） -->
    <main class="read-main">
      <div v-if="loading" class="read-loading">加载中...</div>
      <template v-else-if="chapter">
        <div class="read-panel">
          <h1 class="read-title">{{ chapter.title }}</h1>
          <!-- 按字数分页：每页 4000 字（不含图片），显示当前页 HTML -->
          <div class="read-content read-content-inner" :style="textStyle" v-html="displayContent"></div>
          <div v-if="totalPages > 1" class="page-indicator">
            第 {{ currentPage + 1 }} / {{ totalPages }} 页
          </div>
        </div>
        <!-- 正文下方同宽模块：与正文保持间隔，同款边框，背景随阅读主题 -->
        <div class="read-footer" :style="footerStyle">
          <router-link :to="backToCatalogLink" class="footer-link footer-link-active">返回目录</router-link>
          <button type="button" class="footer-link" @click="onBookmark">+书签</button>
          <button type="button" class="footer-link" @click="onPrevPage">上一页</button>
          <button type="button" class="footer-link" @click="onNextPage">下一页</button>
          <button type="button" class="footer-link" @click="onNextChapter">下一章</button>
        </div>
      </template>
      <div v-else class="read-error">章节不存在或加载失败</div>
    </main>

    <!-- 设置弹窗 -->
    <Transition name="modal">
      <div v-if="showSettings" class="settings-overlay" @click.self="showSettings = false">
        <div class="settings-panel">
          <div class="settings-header">
            <h2 class="settings-title">设置</h2>
            <button type="button" class="btn-close" aria-label="关闭" @click="showSettings = false">×</button>
          </div>
          <div class="settings-body">
            <!-- 阅读主题 -->
            <div class="setting-row">
              <label class="setting-label">阅读主题</label>
              <div class="theme-swatches">
                <button
                  v-for="t in themeOptions"
                  :key="t.id"
                  type="button"
                  class="theme-swatch"
                  :class="{ active: localTheme === t.id }"
                  :style="{ backgroundColor: t.bg }"
                  :title="t.name"
                  @click="localTheme = t.id"
                >
                  <span v-if="localTheme === t.id" class="theme-check">✓</span>
                  <span v-else-if="t.dark" class="theme-moon">🌙</span>
                </button>
              </div>
            </div>
            <!-- 正文字体 -->
            <div class="setting-row">
              <label class="setting-label">正文字体</label>
              <div class="font-buttons">
                <button
                  v-for="f in fontOptions"
                  :key="f.id"
                  type="button"
                  class="font-btn"
                  :class="{ active: localFont === f.id }"
                  :style="localFont === f.id ? { background: '#c00', color: '#fff', borderColor: '#c00' } : {}"
                  @click="localFont = f.id"
                >{{ f.name }}</button>
              </div>
            </div>
            <!-- 字体大小 -->
            <div class="setting-row">
              <label class="setting-label">字体大小</label>
              <div class="font-size-bar">
                <button type="button" class="size-btn" @click="decreaseFontSize">A-</button>
                <span class="size-value">{{ localFontSize }}</span>
                <button type="button" class="size-btn" @click="increaseFontSize">A+</button>
              </div>
            </div>
          </div>
          <div class="settings-footer">
            <button type="button" class="btn-save" @click="applySettings">保存</button>
            <button type="button" class="btn-cancel" @click="showSettings = false">取消</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { chapterApi, novelApi, readingProgressApi } from '../api'
import { authUpdated, notifyAuthUpdate } from '../store/auth'

/** 每页字数（仅统计文本，不计入图片） */
const CHARS_PER_PAGE = 2000

const route = useRoute()
const router = useRouter()
const novelId = computed(() => route.params.id)
const chapterId = computed(() => route.params.chapterId)

const chapter = ref(null)
const chapters = ref([])
const loading = ref(true)
const showSettings = ref(false)
const toast = ref('')
const contentBoxRef = ref(null)
/** 听书模式：是否正在朗读当前页 */
const isListening = ref(false)

/** 按字数拆分后的每页 HTML 数组 */
const pageHtmls = ref([])
const currentPage = ref(0)
const totalPages = computed(() => Math.max(1, pageHtmls.value.length))

const themeOptions = [
  { id: 'cream', name: '浅黄', bg: '#f5f0e1' },
  { id: 'green', name: '浅绿', bg: '#e8f5e9' },
  { id: 'blue', name: '浅蓝', bg: '#e3f2fd' },
  { id: 'pink', name: '浅粉', bg: '#fce4ec' },
  { id: 'grey', name: '浅灰', bg: '#eceff1' },
  { id: 'dark', name: '夜间', dark: true, bg: '#2c2c2c' },
  { id: 'white', name: '白色', bg: '#fff' }
]

const fontOptions = [
  { id: 'yahei', name: '雅黑' },
  { id: 'song', name: '宋体' },
  { id: 'kai', name: '楷体' }
]

const STORAGE_KEYS = { theme: 'read_theme', fontSize: 'read_font_size', font: 'read_font' }

const localTheme = ref('grey')
const localFontSize = ref(20)
const localFont = ref('yahei')

const appliedTheme = ref('grey')
const appliedFontSize = ref(20)
const appliedFont = ref('yahei')

function loadStored() {
  try {
    const t = localStorage.getItem(STORAGE_KEYS.theme)
    if (t) localTheme.value = t
    const s = localStorage.getItem(STORAGE_KEYS.fontSize)
    if (s) localFontSize.value = Math.max(14, Math.min(28, parseInt(s, 10) || 20))
    const f = localStorage.getItem(STORAGE_KEYS.font)
    if (f) localFont.value = f
    appliedTheme.value = localTheme.value
    appliedFontSize.value = localFontSize.value
    appliedFont.value = localFont.value
  } catch (_) {}
}

function applySettings() {
  appliedTheme.value = localTheme.value
  appliedFontSize.value = localFontSize.value
  appliedFont.value = localFont.value
  localStorage.setItem(STORAGE_KEYS.theme, localTheme.value)
  localStorage.setItem(STORAGE_KEYS.fontSize, String(localFontSize.value))
  localStorage.setItem(STORAGE_KEYS.font, localFont.value)
  showSettings.value = false
}

function decreaseFontSize() {
  if (localFontSize.value > 14) localFontSize.value -= 2
}

function increaseFontSize() {
  if (localFontSize.value < 28) localFontSize.value += 2
}

const themeBg = computed(() => {
  const t = themeOptions.find(x => x.id === appliedTheme.value)
  return t ? t.bg : '#eceff1'
})

const themeFg = computed(() => (appliedTheme.value === 'dark' ? '#e0e0e0' : '#333'))

const contentStyle = computed(() => ({
  backgroundColor: themeBg.value,
  color: themeFg.value
}))

/** 下方三按钮区域：仅背景随阅读主题，使用与正文相同的边框；不随字体/字号变化 */
const footerStyle = computed(() => ({
  backgroundColor: themeBg.value
}))

const fontFamily = computed(() => {
  const map = { yahei: '"Microsoft YaHei","微软雅黑",sans-serif', song: 'SimSun,"宋体",serif', kai: 'KaiTi,"楷体",serif' }
  return map[appliedFont.value] || map.yahei
})

const textStyle = computed(() => ({
  fontSize: `${appliedFontSize.value}px`,
  fontFamily: fontFamily.value
}))

/** 合并段落后的正文（与 displayContent 的合并规则一致），用于按字数拆分 */
function getMergedContent() {
  const c = chapter.value?.content
  if (c == null || typeof c !== 'string') return ''
  return c.replace(/<\/p>\s*<p class="read-p">/gi, '')
}

/** 章节正文的纯文字长度（用于计算阅读位置偏移） */
function getChapterTextLength() {
  if (typeof document === 'undefined') return 0
  const merged = getMergedContent()
  if (!merged) return 0
  const div = document.createElement('div')
  div.innerHTML = merged
  return (div.textContent || '').length
}

/**
 * 按字数将 HTML 拆成多页（仅统计文本，不计入图片），每页最多 CHARS_PER_PAGE 字
 * 使用 DOM + Range 在安全边界处切分，保证不破坏标签
 */
function splitContentByChars(html, charsPerPage = CHARS_PER_PAGE) {
  if (!html || typeof html !== 'string') return ['']
  if (typeof document === 'undefined') return [html]
  const div = document.createElement('div')
  div.innerHTML = html
  document.body.appendChild(div)
  const pages = []
  try {
    while (div.childNodes.length > 0) {
      let count = 0
      let found = null
      const walker = document.createTreeWalker(div, NodeFilter.SHOW_TEXT, null)
      let n
      while ((n = walker.nextNode())) {
        const len = n.textContent.length
        count += len
        if (count >= charsPerPage) {
          const offset = len - (count - charsPerPage)
          found = { node: n, offset }
          break
        }
      }
      if (!found) {
        pages.push(div.innerHTML)
        break
      }
      const range = document.createRange()
      range.setStart(div, 0)
      range.setEnd(found.node, found.offset)
      const fragment = range.cloneContents()
      const pageDiv = document.createElement('div')
      pageDiv.appendChild(fragment)
      pages.push(pageDiv.innerHTML)
      range.setStart(found.node, found.offset)
      range.setEnd(div, div.childNodes.length)
      const restFragment = range.cloneContents()
      div.innerHTML = ''
      while (restFragment.firstChild) {
        div.appendChild(restFragment.firstChild)
      }
      // 若剩余内容无实质文本（如刚好整页截断），不再循环，避免多出一页空页
      if (!div.textContent || !div.textContent.trim()) break
    }
  } finally {
    if (div.parentNode) div.parentNode.removeChild(div)
  }
  return pages.length ? pages : ['']
}

/** 当前页要显示的 HTML（按字数分页后的第 currentPage 页） */
const displayContent = computed(() => {
  const list = pageHtmls.value
  const idx = Math.min(currentPage.value, list.length - 1)
  return list[idx] ?? ''
})

/** 当前页的纯文本（用于听书） */
function getCurrentPageText() {
  const html = displayContent.value
  if (!html) return ''
  if (typeof document === 'undefined') return html.replace(/<[^>]+>/g, '')
  const div = document.createElement('div')
  div.innerHTML = html
  return (div.textContent || '').trim()
}

/** 一键听书：使用浏览器语音合成朗读当前页；再次点击停止 */
function toggleListen() {
  if (isListening.value) {
    if (typeof window !== 'undefined' && window.speechSynthesis) {
      window.speechSynthesis.cancel()
    }
    isListening.value = false
    return
  }
  const text = getCurrentPageText()
  if (!text) {
    toast.value = '当前页暂无内容可读'
    setTimeout(() => { toast.value = '' }, 1500)
    return
  }
  if (typeof window === 'undefined' || !window.speechSynthesis) {
    toast.value = '您的浏览器不支持语音朗读'
    setTimeout(() => { toast.value = '' }, 2000)
    return
  }
  const utterance = new window.SpeechSynthesisUtterance(text)
  utterance.lang = 'zh-CN'
  utterance.rate = 1
  utterance.onend = () => { isListening.value = false }
  utterance.onerror = () => { isListening.value = false }
  window.speechSynthesis.speak(utterance)
  isListening.value = true
}

/** 章节内容变化时按字数重新拆分；若有 bookmark 查询参数则跳到记忆页 */
function buildPageHtmls() {
  const merged = getMergedContent()
  if (!merged) {
    pageHtmls.value = ['']
    currentPage.value = 0
    return
  }
  nextTick(() => {
    pageHtmls.value = splitContentByChars(merged, CHARS_PER_PAGE)
    if (currentPage.value >= pageHtmls.value.length) {
      currentPage.value = Math.max(0, pageHtmls.value.length - 1)
    }
    // 存在书签时跳到记忆位置：bookmark 为从正文开始到该页顶部的字符数，每页 CHARS_PER_PAGE
    const bookmarkParam = route.query.bookmark
    if (bookmarkParam != null && pageHtmls.value.length > 0) {
      const bookmarkNum = Math.max(0, parseInt(String(bookmarkParam), 10) || 0)
      const targetPage = Math.min(
        Math.floor(bookmarkNum / CHARS_PER_PAGE),
        pageHtmls.value.length - 1
      )
      currentPage.value = targetPage
    }
  })
}

const isLoggedIn = computed(() => {
  authUpdated.value
  return !!localStorage.getItem('token')
})

/** 返回目录：跳转到书籍详情页并定位到当前卷 */
const backToCatalogLink = computed(() => {
  const nid = novelId.value
  const vol = chapter.value?.volume
  if (!nid) return { path: '/' }
  return vol != null && vol !== ''
    ? { path: `/novel/${nid}`, query: { volume: String(vol) } }
    : { path: `/novel/${nid}` }
})

/** 当前章节在全书中的下一章（按 volumeSort、sort_order 顺序） */
const nextChapter = computed(() => {
  const list = chapters.value
  const cid = chapter.value?.id
  if (!cid || !list.length) return null
  const idx = list.findIndex(c => c.id === cid)
  return idx >= 0 && idx < list.length - 1 ? list[idx + 1] : null
})

/** 当前章节在全书中的上一章 */
const prevChapter = computed(() => {
  const list = chapters.value
  const cid = chapter.value?.id
  if (!cid || !list.length) return null
  const idx = list.findIndex(c => c.id === cid)
  return idx > 0 ? list[idx - 1] : null
})

/** 当前卷内 sort_order 加 1 的下一章（同书、同卷、sort_order = 当前 + 1） */
const nextChapterInVolume = computed(() => {
  const list = chapters.value
  const cur = chapter.value
  if (!cur?.id || !list?.length) return null
  const vol = cur.volume
  const order = Number(cur.sort_order ?? cur.sortOrder ?? 0)
  const next = list.find(
    c => (c.volume === vol || (c.volume == null && vol == null)) && Number(c.sort_order ?? c.sortOrder) === order + 1
  )
  return next ?? null
})

function showLoginTip() {
  toast.value = 'loginRequired'
  setTimeout(() => { toast.value = '' }, 3000)
}

function onBookmark() {
  if (!isLoggedIn.value) {
    showLoginTip()
    return
  }
  const nid = novelId.value
  const cid = chapter.value?.id
  if (!nid || !cid) {
    toast.value = '无法获取当前书籍或章节'
    setTimeout(() => { toast.value = '' }, 1500)
    return
  }
  const totalChars = getChapterTextLength()
  // 记忆位置 = 从正文开始到当前页面显示最上面的字符数；每多一页换页多加 CHARS_PER_PAGE 字
  const positionFromStart = currentPage.value * CHARS_PER_PAGE
  const progressPosition = Math.min(positionFromStart, totalChars)
  const bookmark = progressPosition
  readingProgressApi.saveBookmark(nid, cid, progressPosition, bookmark).then(() => {
    toast.value = '书签已保存'
    setTimeout(() => { toast.value = '' }, 1500)
  }).catch((err) => {
    if (err?.statusCode === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('nickname')
      localStorage.removeItem('userId')
      localStorage.removeItem('avatar_url')
      sessionStorage.removeItem('token')
      notifyAuthUpdate()
      toast.value = 'loginRequired'
      setTimeout(() => { toast.value = '' }, 3000)
    } else {
      const msg = err?.message || '书签保存失败'
      toast.value = msg
      setTimeout(() => { toast.value = '' }, 1500)
    }
  })
}

function onFlower() {
  if (!isLoggedIn.value) {
    showLoginTip()
    return
  }
  const nid = novelId.value
  if (!nid) return
  novelApi.flower(nid).then(() => {
    toast.value = '送花成功'
    setTimeout(() => { toast.value = '' }, 1500)
  }).catch(() => {
    toast.value = '送花失败'
    setTimeout(() => { toast.value = '' }, 1500)
  })
}

async function fetchChapter() {
  const cid = chapterId.value
  if (!cid) { loading.value = false; return }
  loading.value = true
  try {
    // 阅读页正文数据：按章节ID请求 /chapters/{id}
    chapter.value = await chapterApi.getById(cid)
  } catch (_) {
    chapter.value = null
  } finally {
    loading.value = false
  }
}

async function fetchChaptersForNovel() {
  const nid = novelId.value
  if (!nid) return
  try {
    // 阅读页目录基准：用于上一章/下一章计算，顺序由后端统一保证
    const list = await chapterApi.getByNovel(nid) || []
    chapters.value = list
  } catch (_) {
    chapters.value = []
  }
}

/** 上一页：当前章上一页；若无上一页则提示 */
function onPrevPage() {
  if (currentPage.value > 0) {
    currentPage.value--
    window.scrollTo({ top: 0, behavior: 'smooth' })
    return
  }
  toast.value = '前面的区域以后再来探索吧'
  setTimeout(() => { toast.value = '' }, 2000)
}

/** 下一页：当前章下一页；若已是最后一页则提示，不自动跳下一章 */
function onNextPage() {
  if (totalPages.value > 1 && currentPage.value < totalPages.value - 1) {
    currentPage.value++
    window.scrollTo({ top: 0, behavior: 'smooth' })
    return
  }
  toast.value = '该章节已经读完'
  setTimeout(() => { toast.value = '' }, 2000)
}

/** 下一章：同书同卷、sort_order + 1，不存在则提示该卷已读完 */
function onNextChapter() {
  const next = nextChapterInVolume.value
  if (next?.id) {
    router.push(`/novel/${novelId.value}/read/${next.id}`)
    return
  }
  toast.value = '该卷内容已读完'
  setTimeout(() => { toast.value = '' }, 2000)
}

watch([novelId, chapterId], fetchChapter, { immediate: true })
watch(novelId, fetchChaptersForNovel, { immediate: true })
watch(chapterId, () => { currentPage.value = 0 })
watch(() => chapter.value?.content, buildPageHtmls, { immediate: true })
onMounted(loadStored)
</script>

<style scoped>
.novel-read {
  min-height: 100vh;
  padding-top: 52px;
}

.read-toast {
  position: fixed;
  top: 60px;
  left: 50%;
  transform: translateX(-50%);
  padding: 10px 20px;
  background: rgba(0,0,0,.75);
  color: #fff;
  border-radius: 6px;
  z-index: 2000;
  font-size: 14px;
}

.toast-login {
  color: #ffb3c1;
  text-decoration: underline;
  margin: 0 2px;
}

.toast-login:hover { color: #fff; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* 顶栏 */
.read-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 48px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  z-index: 100;
}

.read-header-inner {
  width: 70vw;
  min-width: 320px;
  margin: 0 auto;
  height: 100%;
  padding: 0 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.btn-settings {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  font-size: 14px;
  color: #333;
  background: none;
  border: none;
  cursor: pointer;
}

.btn-settings:hover { color: #c00; }

.settings-icon { font-size: 16px; }

.read-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-link {
  background: none;
  border: none;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  padding: 4px 0;
}

.action-link:hover { color: #c00; }

.action-listen {
  color: #e91e8c;
  font-weight: 500;
}

.action-listen:hover {
  color: #c2185b;
}

.action-listen.active {
  color: #ad1457;
  text-decoration: underline;
}

.action-sep {
  color: #ccc;
  font-size: 14px;
}

/* 正文 */
.read-main {
  margin: 0 auto;
  padding: 32px 0 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.read-panel {
  width: 70vw;
  min-width: 320px;
  padding: 32px 28px 40px;
  border: 1px solid rgba(0,0,0,.12);
  border-radius: 4px;
  background: inherit;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.read-content-inner {
  width: 90%;
  overflow: visible;
  padding-bottom: 1em;
}

.page-indicator {
  margin-top: 12px;
  font-size: 14px;
  color: #888;
}

.read-loading, .read-error {
  text-align: center;
  color: #666;
  padding: 40px;
}

.read-title {
  font-size: 2.25rem;
  font-weight: 400;
  text-align: center;
  margin: 0 0 24px;
  padding-bottom: 16px;
  border-bottom: none;
  line-height: 1.3;
  align-self: stretch;
}

.read-title::after {
  content: '';
  display: block;
  width: 90%;
  margin: 16px auto 0;
  border-bottom: 2px solid #ccc;
}

.read-content {
  line-height: 1.8;
  white-space: normal;
  word-break: break-word;
}

.read-content :deep(.read-section-title) {
  text-indent: 0;
  margin: 1.2em 0 0.6em;
  font-weight: 700;
}

.read-content :deep(.read-p-no-indent) {
  text-indent: 0;
  margin: 0 0 0.8em;
}

/* 正文/对话：仅首行缩进 2em，换行后与段落左缘对齐 */
.read-content :deep(.read-dialogue) {
  text-indent: 2em;
  margin: 0 0 0.4em;
  margin-left: 0;
}

.read-content :deep(.read-intro-name) {
  text-indent: 0;
  margin: 0 0 0.5em;
}

/* 正文段落：仅首行缩进 2em，后续行不缩进 */
.read-content :deep(.read-p) {
  text-indent: 2em;
  margin: 0 0 0.8em;
}

.read-content :deep(.read-p:last-of-type) { margin-bottom: 0; }
.read-content :deep(.read-p-no-indent:last-of-type) { margin-bottom: 0; }

/* 正文内图片宽度与正文/下划线一致：阅读模块 70vw，78% => 54.6vw */
.read-content :deep(.read-content-img) {
  display: block;
  width: 100%;
  max-width: 100%;
  height: auto;
  margin: 1em auto;
}

/* 正文下方同宽模块：与正文保持上下右间隔，使用与正文相同的边框圈起，背景由阅读主题控制 */
.read-footer {
  width: 70vw;
  min-width: 320px;
  margin-top: 20px;
  padding: 16px 28px;
  border: 1px solid rgba(0,0,0,.12);
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 96px;
}

.footer-link {
  font-size: 18px;
  font-family: inherit;
  color: inherit;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px 8px;
  text-decoration: none;
}

.read-footer .footer-link { color: #555; }

.read-footer .footer-link:hover { color: #333; }

.footer-link-active {
  color: #d97b29;
}

.read-footer .footer-link-active:hover { color: #b85f1a; }

.read-footer .footer-link:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.theme-dark .read-footer .footer-link:disabled { opacity: 0.5; }

.theme-dark .read-footer .footer-link { color: #e0e0e0; }
.theme-dark .read-footer .footer-link:hover { color: #fff; }
.theme-dark .read-footer .footer-link-active { color: #e8a856; }
.theme-dark .read-footer .footer-link-active:hover { color: #f0b868; }

/* 设置弹窗 */
.settings-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.4);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.settings-panel {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,.15);
  width: 100%;
  max-width: 420px;
  overflow: hidden;
}

.settings-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.settings-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #333;
}

.btn-close {
  width: 32px;
  height: 32px;
  font-size: 24px;
  line-height: 1;
  color: #666;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.btn-close:hover { color: #333; }

.settings-body {
  padding: 20px;
}

.setting-row {
  margin-bottom: 20px;
}

.setting-row:last-child { margin-bottom: 0; }

.setting-label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 10px;
}

.theme-swatches {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.theme-swatch {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #333;
}

.theme-swatch.active {
  border-color: #c00;
  box-shadow: 0 0 0 1px #c00;
}

.theme-check { color: #fff; text-shadow: 0 0 1px #000; }
.theme-moon { font-size: 16px; }

.font-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.font-btn {
  padding: 8px 16px;
  font-size: 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  color: #333;
  cursor: pointer;
}

.font-btn:hover { border-color: #999; }
.font-btn.active { border-color: #c00; }

.font-size-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.size-btn {
  width: 40px;
  height: 36px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  font-size: 16px;
  cursor: pointer;
}

.size-btn:hover { border-color: #999; }

.size-value {
  min-width: 32px;
  text-align: center;
  font-size: 16px;
  color: #333;
}

.settings-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #eee;
}

.btn-save {
  flex: 1;
  padding: 10px 20px;
  background: #c00;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 15px;
  cursor: pointer;
}

.btn-save:hover { background: #a00; }

.btn-cancel {
  padding: 10px 20px;
  background: #fff;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 15px;
  cursor: pointer;
}

.btn-cancel:hover { border-color: #999; }

.modal-enter-active, .modal-leave-active { transition: opacity 0.2s; }
.modal-enter-active .settings-panel, .modal-leave-active .settings-panel { transition: transform 0.2s; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
.modal-enter-from .settings-panel, .modal-leave-to .settings-panel { transform: scale(0.95); }
</style>
