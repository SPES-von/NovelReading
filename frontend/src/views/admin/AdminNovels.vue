<template>
  <div class="admin-panel">
    <Transition name="fade">
      <div v-if="tip" class="success-toast">{{ tip }}</div>
    </Transition>

    <!-- 顶部：小说 / 卷 / 章节 选择，与上架页布局一致 -->
    <section class="shelf-upper">
      <!-- 小说选择（可搜索下拉，与分类体系中一致） -->
      <div class="row-left">
        <div class="novel-select-wrap">
          <input
            v-model="novelSearch"
            type="text"
            class="novel-input"
            placeholder="搜索并选择小说"
            autocomplete="off"
            @focus="showNovelDropdown = true"
            @input="onNovelSearchInput"
          />
          <ul
            v-show="showNovelDropdown && filteredNovels.length > 0"
            class="novel-dropdown"
          >
            <li
              v-for="n in filteredNovels"
              :key="n.id"
              class="novel-option"
              @mousedown.prevent="selectNovel(n)"
            >
              {{ n.title }}
            </li>
          </ul>
          <p
            v-if="showNovelDropdown && novelSearch && !filteredNovels.length"
            class="no-match"
          >
            无匹配小说
          </p>
        </div>
      </div>

      <!-- 卷名选择：根据小说 id 从章节列表去重 volume，可输入过滤的下拉文本框 -->
      <div class="row-center">
        <div class="simple-select-wrap">
          <input
            v-model="volumeName"
            type="text"
            class="novel-input"
            placeholder="选择卷名"
            autocomplete="off"
            @focus="onVolumeInput"
            @input="onVolumeInput"
          />
          <ul
            v-show="showVolumeDropdown && filteredVolumeOptions.length > 0"
            class="novel-dropdown"
          >
            <li
              v-for="v in filteredVolumeOptions"
              :key="v"
              class="novel-option"
              @mousedown.prevent="selectVolume(v)"
            >
              {{ v }}
            </li>
          </ul>
          <p
            v-if="showVolumeDropdown && volumeName && !filteredVolumeOptions.length && selectedNovel"
            class="no-match"
          >
            该小说暂无卷信息
          </p>
        </div>
      </div>

      <!-- 章节名选择：根据小说 id 和卷名过滤章节，可输入过滤的下拉文本框 -->
      <div class="row-chapter">
        <div class="simple-select-wrap">
          <input
            v-model="chapterName"
            type="text"
            class="novel-input"
            placeholder="选择章节"
            autocomplete="off"
            @focus="onChapterInput"
            @input="onChapterInput"
          />
          <ul
            v-show="showChapterDropdown && filteredChapterOptions.length > 0"
            class="novel-dropdown"
          >
            <li
              v-for="c in filteredChapterOptions"
              :key="c.id"
              class="novel-option"
              @mousedown.prevent="selectChapter(c)"
            >
              {{ c.title }}
            </li>
          </ul>
          <p
            v-if="showChapterDropdown && chapterName && !filteredChapterOptions.length && selectedNovel && volumeName"
            class="no-match"
          >
            该卷暂无章节
          </p>
        </div>
      </div>

      <!-- 按钮：更新章节内容（目前为从服务端重新拉取） -->
      <div class="row-right">
        <button
          type="button"
          class="btn btn-add"
          :disabled="!selectedNovel || !volumeName || !chapterName"
          @click="loadSelectedChapterContent"
        >
          更新
        </button>
      </div>
    </section>

    <!-- 章节内容模块：根据内容自适应高度 -->
    <section class="content-section">
      <div class="content-header">
        <h3 class="section-title">章节内容</h3>
      </div>
      <textarea
        v-model="chapterContent"
        class="content-textarea"
        placeholder="请输入章节内容"
        ref="contentTextarea"
        @input="autoResize"
      />
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { adminNovelApi, chapterApi } from '../../api'

const tip = ref('')

// 小说下拉（与 AdminCategorySystem 中小说下拉逻辑保持一致）
const novels = ref([])
const novelSearch = ref('')
const showNovelDropdown = ref(false)
const selectedNovel = ref(null)

// 章节与卷信息
const chapters = ref([]) // 当前选中小说的全部章节
const volumeName = ref('')
const showVolumeDropdown = ref(false)
const chapterName = ref('')
const showChapterDropdown = ref(false)
const selectedChapter = ref(null)
const chapterContent = ref('')

// 章节内容文本框引用，用于自适应高度
const contentTextarea = ref(null)

const filteredNovels = computed(() => {
  const q = (novelSearch.value || '').trim().toLowerCase()
  if (!q) return novels.value.slice(0, 100)
  return novels.value.filter(n => (n.title || '').toLowerCase().includes(q))
})

// 根据章节列表去重 volume，作为卷名下拉数据
const volumeOptions = computed(() => {
  const set = new Set()
  chapters.value.forEach(ch => {
    if (ch.volume) set.add(ch.volume)
  })
  return Array.from(set)
})

// 根据选中卷过滤章节列表
const chapterOptions = computed(() => {
  if (!volumeName.value) return []
  return chapters.value.filter(ch => ch.volume === volumeName.value)
})

// 卷名可输入过滤
const filteredVolumeOptions = computed(() => {
  const keyword = (volumeName.value || '').trim().toLowerCase()
  const all = volumeOptions.value
  if (!keyword) return all
  return all.filter(v => v.toLowerCase().includes(keyword))
})

// 章节名可输入过滤
const filteredChapterOptions = computed(() => {
  const keyword = (chapterName.value || '').trim().toLowerCase()
  const all = chapterOptions.value
  if (!keyword) return all
  return all.filter(c => (c.title || '').toLowerCase().includes(keyword))
})

function selectNovel(novel) {
  selectedNovel.value = novel
  novelSearch.value = novel.title
  showNovelDropdown.value = false
}

function onNovelSearchInput() {
  showNovelDropdown.value = true
  const q = (novelSearch.value || '').trim()
  if (!q || (selectedNovel.value && novelSearch.value !== selectedNovel.value.title)) {
    selectedNovel.value = null
  }
}

function onVolumeInput() {
  if (!selectedNovel.value) {
    tip.value = '请先选择小说'
    setTimeout(() => { tip.value = '' }, 2000)
    return
  }
  showVolumeDropdown.value = true
  showChapterDropdown.value = false
  // 修改卷名时，清空已选章节和内容
  selectedChapter.value = null
  chapterName.value = ''
  chapterContent.value = ''
}

function onChapterInput() {
  if (!selectedNovel.value) {
    tip.value = '请先选择小说'
    setTimeout(() => { tip.value = '' }, 2000)
    return
  }
  if (!volumeName.value) {
    tip.value = '请先选择卷名'
    setTimeout(() => { tip.value = '' }, 2000)
    return
  }
  showChapterDropdown.value = true
  showVolumeDropdown.value = false
}

function selectVolume(v) {
  volumeName.value = v
  showVolumeDropdown.value = false
  // 切换卷时清空已选章节
  selectedChapter.value = null
  chapterName.value = ''
  chapterContent.value = ''
}

function selectChapter(chapter) {
  selectedChapter.value = chapter
  chapterName.value = chapter.title || ''
  showChapterDropdown.value = false
  // 直接将章节内容（HTML）转为纯文本填入编辑区
  chapterContent.value = toPlainText(chapter.content || '')
  // 选择章节后，直接根据内容自适应高度
  nextTickAutoResize()
}

async function fetchNovels() {
  try {
    novels.value = await adminNovelApi.list()
  } catch {
    novels.value = []
  }
}

async function fetchChaptersByNovelId(novelId) {
  chapters.value = []
  volumeName.value = ''
  chapterName.value = ''
  selectedChapter.value = null
  chapterContent.value = ''
  if (!novelId) return
  try {
    const list = await chapterApi.getByNovel(novelId)
    chapters.value = Array.isArray(list) ? list : (list?.content || [])
  } catch {
    chapters.value = []
  }
}

function existsVolume(v) {
  return volumeOptions.value.includes(v)
}

function existsChapterTitleInCurrentVolume(title) {
  const t = (title || '').trim()
  if (!t) return false
  return chapterOptions.value.some(c => (c.title || '').trim() === t)
}

function computeWordCount(raw) {
  return (raw || '').replace(/\s+/g, '').length
}

// 更新按钮：
// - 若当前卷不存在：新增章节（sort_order=0；sort=该书最大 sort + 1）
// - 若已选中章节：更新该章节内容（同样做内容格式化由后端处理）
async function loadSelectedChapterContent() {
  if (!selectedNovel.value) {
    tip.value = '请先选择小说'
    setTimeout(() => { tip.value = '' }, 2000)
    return
  }
  if (!volumeName.value?.trim()) {
    tip.value = '请先选择卷名'
    setTimeout(() => { tip.value = '' }, 2000)
    return
  }
  if (!chapterName.value?.trim()) {
    tip.value = '请先输入章节名'
    setTimeout(() => { tip.value = '' }, 2000)
    return
  }

  const novelId = selectedNovel.value.id
  const volume = volumeName.value.trim()
  const title = chapterName.value.trim()
  const rawContent = chapterContent.value || ''

  try {
    if (!existsVolume(volume)) {
      await adminNovelApi.addChapter(novelId, {
        volume,
        title,
        content: rawContent,
        wordCount: computeWordCount(rawContent)
      })
      tip.value = '新增章节成功'
      setTimeout(() => { tip.value = '' }, 2000)
      // 刷新章节列表失败不影响“新增成功”提示
      try { await fetchChaptersByNovelId(novelId) } catch {}
      return
    }

    // 卷存在：
    // - 章节名不存在：新增章节（sort_order=max+1，sort=该卷sort）
    // - 章节名存在且已选中章节：更新该章节
    if (!existsChapterTitleInCurrentVolume(title)) {
      const savedChapter = await adminNovelApi.addChapter(novelId, {
        volume,
        title,
        content: rawContent,
        wordCount: computeWordCount(rawContent)
      })
      selectedChapter.value = savedChapter
      tip.value = '新增章节成功'
      setTimeout(() => { tip.value = '' }, 2000)
      try { await fetchChaptersByNovelId(novelId) } catch {}
      return
    }

    if (!selectedChapter.value) {
      tip.value = '该章节已存在：请先在下拉中选择章节再更新'
      setTimeout(() => { tip.value = '' }, 2000)
      return
    }

    const savedChapter = await adminNovelApi.updateChapter(selectedChapter.value.id, {
      volume,
      title,
      content: rawContent,
      wordCount: computeWordCount(rawContent)
    })
    // 更新成功后把新 id 回写，避免章节列表刷新失败导致下一次更新找不到旧 id
    selectedChapter.value = savedChapter
    tip.value = '更新章节成功'
    setTimeout(() => { tip.value = '' }, 2000)
    // 刷新章节列表失败不影响“更新成功”提示
    try { await fetchChaptersByNovelId(novelId) } catch {}
  } catch (e) {
    tip.value = e?.message || '操作失败'
    setTimeout(() => { tip.value = '' }, 2000)
  }
}

// 将 HTML 内容（包含 <p class="read-dialogue"> 等标签）转换为纯文本
function toPlainText(html) {
  if (!html) return ''
  // 先在原始 HTML 文本层面，把每个段落和图片「拆行」
  // 1) 每个 </p> 之后补一个换行
  // 2) 每个 <br> 视为换行
  // 图片不再单独强制换行，只按原有文本流展示
  const normalized = html
    .replace(/\r\n/g, '\n')
    .replace(/<\/p>\s*/gi, '</p>\n')
    // 让 <br> 等价于“换行 + 空行”，以便与“章节内容中存在空行”在编辑区的观感一致
    .replace(/<br\s*\/?>\s*/gi, '<br />\n\n')

  const container = document.createElement('div')
  container.innerHTML = normalized
  // 特殊处理图片：将 <img> 标签替换为其 src 路径文本（不额外强制换行）
  const imgs = container.querySelectorAll('img')
  imgs.forEach(img => {
    const src = img.getAttribute('src') || ''
    const textNode = document.createTextNode(src)
    img.parentNode && img.parentNode.replaceChild(textNode, img)
  })
  // textContent 会自动去掉剩余标签，只保留文字（包含我们刚替换进去的图片路径）
  const text = container.textContent || container.innerText || ''
  // 去掉首尾空白，保留我们人为加上的换行，让每段内容/每张图单独一行
  return text.replace(/\r\n/g, '\n').trim()
}

// 监听选中小说变化，自动拉取章节列表
watch(selectedNovel, (n) => {
  const id = n?.id
  fetchChaptersByNovelId(id)
})

// 自适应文本域高度
function autoResize() {
  const el = contentTextarea.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${el.scrollHeight}px`
}

function nextTickAutoResize() {
  // 简单的微任务延迟，确保 v-model 更新已渲染
  queueMicrotask(() => {
    autoResize()
  })
}

function onDocumentClick(e) {
  const novelWrap = document.querySelector('.novel-select-wrap')
  const simpleSelects = document.querySelectorAll('.simple-select-wrap')
  if (novelWrap && !novelWrap.contains(e.target)) {
    showNovelDropdown.value = false
  }
  let clickedInsideSimple = false
  simpleSelects.forEach(el => {
    if (el.contains(e.target)) clickedInsideSimple = true
  })
  if (!clickedInsideSimple) {
    showVolumeDropdown.value = false
    showChapterDropdown.value = false
  }
}

onMounted(() => {
  fetchNovels()
  document.addEventListener('click', onDocumentClick)
  // 初始时内容为空，也执行一次自适应，避免高度过大
  nextTickAutoResize()
})

onUnmounted(() => {
  document.removeEventListener('click', onDocumentClick)
})
</script>

<style scoped>
.admin-panel {
  width: 98%;
  max-width: none;
  margin: 0 auto;
}

.shelf-upper {
  width: 100%;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.content-section {
  width: 100%;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  margin-top: 24px;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.content-header .section-title {
  margin: 0;
}

.content-textarea {
  width: 100%;
  min-height: 240px;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
  font-family: inherit;
  /* 自适应高度，由脚本控制；隐藏内部滚动条 */
  resize: none;
  overflow-y: hidden;
}

.content-textarea:focus {
  outline: none;
  border-color: #2c3e50;
}

.row-left {
  flex: 0 1 35%;
  min-width: 0;
}

.row-center {
  flex: 0 1 30%;
  min-width: 0;
}

.row-chapter {
  flex: 0 1 30%;
  min-width: 0;
}

.row-right {
  flex: 0 0 5%;
  min-width: 0;
}

.novel-select-wrap,
.simple-select-wrap {
  position: relative;
}

.novel-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
}

.novel-input:focus {
  outline: none;
  border-color: #2c3e50;
}

.novel-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin: 4px 0 0;
  padding: 4px 0;
  max-height: 280px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  list-style: none;
  z-index: 10;
}

.novel-option {
  padding: 10px 14px;
  cursor: pointer;
  font-size: 0.95rem;
}

.novel-option:hover {
  background: #f0f0f0;
}

.no-match {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin: 4px 0 0;
  padding: 12px 14px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  color: #888;
  font-size: 0.9rem;
}

.btn {
  padding: 10px 18px;
  border-radius: 6px;
  font-size: 0.8rem;
  cursor: pointer;
  border: none;
  white-space: nowrap;
}

.btn-add {
  background: #2c3e50;
  color: #fff;
}

.btn-add:hover:not(:disabled) {
  background: #34495e;
}

.btn-add:disabled {
  background: #b0b0b0;
  color: #fff;
  cursor: not-allowed;
  opacity: 0.8;
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
</style>
