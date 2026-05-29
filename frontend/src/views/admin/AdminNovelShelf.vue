<template>
  <div class="admin-panel">
    <Transition name="fade">
      <div v-if="shelfTip" class="success-toast">{{ shelfTip }}</div>
    </Transition>
    <section class="shelf-upper">
      <div class="row-left">
        <input
          v-model.trim="novelSearch"
          type="text"
          class="novel-input"
          placeholder="小说名"
          autocomplete="off"
          @keydown.enter.prevent="handleAdd"
        />
      </div>
      <div class="row-center">
        <input
          v-model.trim="volumeName"
          type="text"
          class="volume-input"
          placeholder="卷名"
          autocomplete="off"
          @keydown.enter.prevent="handleAdd"
        />
      </div>
      <div class="row-chapter">
        <input
          v-model.trim="chapterName"
          type="text"
          class="chapter-input"
          placeholder="章节名"
          autocomplete="off"
          @keydown.enter.prevent="handleAdd"
        />
      </div>
      <div class="row-right">
        <button
          type="button"
          class="btn btn-add"
          :disabled="!novelSearch || !volumeName || !chapterName"
          @click="handleAdd"
        >
          上架
        </button>
      </div>
    </section>
    <section class="intro-section">
      <h3 class="section-title">简介填写</h3>
      <textarea
        v-model="introText"
        class="intro-textarea"
        placeholder="请输入简介"
        rows="8"
      />
    </section>
    <section class="cover-section">
      <h3 class="section-title">基础信息</h3>
      <div class="cover-row">
        <!-- 封面路径（40%） -->
        <div class="cover-col-cover">
          <div class="field-label">封面添加</div>
          <input
            v-model.trim="coverImagePath"
            type="text"
            class="cover-input"
            placeholder="输入封面图片路径"
            autocomplete="off"
          />
        </div>
        <!-- 作者选择（30%） -->
        <div class="cover-col-author">
          <div class="field-label">作者添加</div>
          <div class="author-select-wrap">
            <input
              v-model.trim="authorKeyword"
              type="text"
              class="author-input"
              placeholder="搜索并选择作者"
              autocomplete="off"
              @focus="showAuthorDropdown = true"
              @input="onAuthorSearchInput"
            />
            <ul
              v-show="showAuthorDropdown && filteredAuthors.length > 0"
              class="author-dropdown"
            >
              <li
                v-for="a in filteredAuthors"
                :key="a.id"
                class="author-option"
                @mousedown.prevent="handleSelectAuthor(a)"
              >
                {{ a.name }}
              </li>
            </ul>
            <p
              v-if="showAuthorDropdown && authorKeyword && !filteredAuthors.length"
              class="no-author-match"
            >
              无匹配作者
            </p>
          </div>
        </div>
        <!-- 文库/出版社选择（30%） -->
        <div class="cover-col-publisher">
          <div class="field-label">文库添加</div>
          <div class="publisher-select-wrap">
            <input
              type="text"
              class="publisher-input"
              :value="selectedPublisher ? selectedPublisher.name : ''"
              placeholder="请选择文库"
              readonly
              @click="showPublisherDropdown = !showPublisherDropdown"
            />
            <ul
              v-show="showPublisherDropdown && publishers.length > 0"
              class="author-dropdown"
            >
              <li
                v-for="p in publishers"
                :key="p.id"
                class="author-option"
                @mousedown.prevent="handleSelectPublisher(p)"
              >
                {{ p.name }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </section>
    <section class="content-section">
      <div class="content-header">
        <h3 class="section-title">内容输入</h3>
      </div>
      <textarea
        v-model="chapterContent"
        class="content-textarea"
        placeholder="请输入章节内容"
        rows="12"
      />
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { publisherApi, authorApi, adminNovelApi } from '../../api'

const novelSearch = ref('')
const volumeName = ref('')
const chapterName = ref('')
const introText = ref('')
const coverImagePath = ref('')
const chapterContent = ref('')
const shelfTip = ref('')

// 作者与文库（出版社）相关状态
const authorKeyword = ref('')
const authors = ref([])
const selectedAuthor = ref(null)
const showAuthorDropdown = ref(false)

const publishers = ref([])
const selectedPublisherId = ref('')
const selectedPublisher = ref(null)
const showPublisherDropdown = ref(false)

const filteredAuthors = computed(() => {
  const keyword = authorKeyword.value.trim().toLowerCase()
  if (!keyword) return authors.value.slice(0, 100)
  return authors.value.filter(a =>
    (a.name || '').toLowerCase().includes(keyword)
  )
})

async function loadAuthors() {
  try {
    const res = await authorApi.list()
    const list = Array.isArray(res) ? res : (res?.content || [])
    authors.value = list
  } catch (e) {
    // ignore error, 此处仅用于下拉选择
  }
}

async function loadPublishers() {
  try {
    const res = await publisherApi.list()
    const list = Array.isArray(res) ? res : (res?.content || [])
    publishers.value = list
  } catch (e) {
    // ignore error
  }
}

function handleSelectAuthor(author) {
  selectedAuthor.value = author
  authorKeyword.value = author.name || ''
  showAuthorDropdown.value = false
}

function onAuthorSearchInput() {
  showAuthorDropdown.value = true
  const q = (authorKeyword.value || '').trim()
  if (!q || (selectedAuthor.value && authorKeyword.value !== selectedAuthor.value.name)) {
    selectedAuthor.value = null
  }
}

function onDocumentClick(e) {
  const authorWrap = document.querySelector('.author-select-wrap')
  const publisherWrap = document.querySelector('.publisher-select-wrap')
  if (authorWrap && !authorWrap.contains(e.target)) {
    showAuthorDropdown.value = false
  }
  if (publisherWrap && !publisherWrap.contains(e.target)) {
    showPublisherDropdown.value = false
  }
}

function handleSelectPublisher(publisher) {
  selectedPublisher.value = publisher
  selectedPublisherId.value = String(publisher.id)
  showPublisherDropdown.value = false
}

onMounted(() => {
  loadAuthors()
  loadPublishers()
  document.addEventListener('click', onDocumentClick)
})

onUnmounted(() => {
  document.removeEventListener('click', onDocumentClick)
})

async function handleAdd() {
  if (!novelSearch.value?.trim() || !introText.value?.trim() || !chapterContent.value?.trim()) {
    shelfTip.value = '请填写小说名、简介和内容'
    setTimeout(() => { shelfTip.value = '' }, 2000)
    return
  }

  let author = selectedAuthor.value
  const authorName = authorKeyword.value?.trim()

  // 若未从下拉中选中，但输入框中有作者名，则先创建作者
  if (!author && authorName) {
    try {
      const created = await authorApi.add(authorName)
      author = created
      selectedAuthor.value = created
    } catch (e) {
      shelfTip.value = e?.message || '创建作者失败'
      setTimeout(() => { shelfTip.value = '' }, 2000)
      return
    }
  }

  if (!author) {
    shelfTip.value = '请输入或选择作者'
    setTimeout(() => { shelfTip.value = '' }, 2000)
    return
  }
  if (!selectedPublisherId.value) {
    shelfTip.value = '请选择文库'
    setTimeout(() => { shelfTip.value = '' }, 2000)
    return
  }

  const rawContent = chapterContent.value || ''
  const pureChars = rawContent.replace(/\s+/g, '')
  const wordCount = pureChars.length
  // 如果用户直接粘贴了带有 <p>、<img> 等标签的完整 HTML（例如 init-data.sql 中的示例），
  // 则保持原样入库；否则仅按换行切分成多行纯文本，再用换行符拼接，不额外包任何标签。
  const hasHtmlTag = /<\s*(p|img|br|div|span|section)[\s>]/i.test(rawContent)
  let chapterHtml
  if (hasHtmlTag) {
    // 对已带 HTML 的内容做一点格式化：
    // 1. 标准化换行为 \n
    // 2. 在相邻标签之间补一个换行符，让每个 <p>/<img> 基本上一行，接近 init-data.sql 的观感
    chapterHtml = rawContent
      .replace(/\r\n/g, '\n')
      .replace(/>\s*</g, '>\n<')
      .trim()
  } else {
    // 仅做换行标准化：\r\n → \n，保留用户实际的分段形式
    chapterHtml = rawContent.replace(/\r\n/g, '\n')
  }

  const payload = {
    title: novelSearch.value.trim(),
    synopsis: introText.value.trim(),
    authorId: author.id,
    publisherId: Number(selectedPublisherId.value),
    coverUrl: coverImagePath.value?.trim() || null,
    wordCount,
    volume: volumeName.value.trim(),
    chapterTitle: chapterName.value.trim(),
    chapterContent: chapterHtml
  }

  try {
    await adminNovelApi.add(payload)
    // 上架成功后清空表单
    novelSearch.value = ''
    volumeName.value = ''
    chapterName.value = ''
    introText.value = ''
    coverImagePath.value = ''
    chapterContent.value = ''
    authorKeyword.value = ''
    selectedAuthor.value = null
    selectedPublisherId.value = ''
    selectedPublisher.value = null

    shelfTip.value = '上架成功'
    setTimeout(() => { shelfTip.value = '' }, 2000)
  } catch (e) {
    shelfTip.value = e?.message || '上架失败'
    setTimeout(() => { shelfTip.value = '' }, 2000)
  }
}
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

.intro-section {
  width: 100%;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.intro-textarea {
  width: 100%;
  min-height: 160px;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
  font-family: inherit;
  resize: vertical;
}

.intro-textarea:focus {
  outline: none;
  border-color: #2c3e50;
}

.cover-section {
  width: 100%;
  padding: 16px 20px 20px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  margin-top: 24px;
}

.cover-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.cover-col-cover {
  flex: 0 1 40%;
  min-width: 0;
}

.cover-col-author,
.cover-col-publisher {
  flex: 0 1 30%;
  min-width: 0;
}

.field-label {
  font-size: 0.85rem;
  color: #555;
  margin-bottom: 4px;
}

.cover-input,
.author-input,
.publisher-input {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
  background-color: #fff;
}

.cover-input:focus,
.author-input:focus,
.publisher-input:focus {
  outline: none;
  border-color: #2c3e50;
}

.author-select-wrap {
  position: relative;
}

.publisher-select-wrap {
  position: relative;
}

.author-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin: 4px 0 0;
  padding: 4px 0;
  max-height: 220px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  list-style: none;
  z-index: 10;
}

.author-option {
  padding: 8px 12px;
  font-size: 0.9rem;
  cursor: pointer;
}

.author-option:hover {
  background: #f5f5f5;
}

.no-author-match {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin: 4px 0 0;
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  font-size: 0.85rem;
  color: #888;
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
  resize: vertical;
}

.content-textarea:focus {
  outline: none;
  border-color: #2c3e50;
}

.btn-shelf {
  background: #2c3e50;
  color: #fff;
}

.btn-shelf:hover {
  background: #34495e;
}

.row-left {
  flex: 0 1 35%;
  min-width: 0;
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

.row-center {
  flex: 0 1 30%;
  min-width: 0;
}

.row-chapter {
  flex: 0 1 30%;
  min-width: 0;
}

.chapter-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
}

.chapter-input:focus {
  outline: none;
  border-color: #2c3e50;
}

.volume-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
}

.volume-input:focus {
  outline: none;
  border-color: #2c3e50;
}

.row-right {
  flex: 0 0 5%;
  min-width: 0;
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
</style>
