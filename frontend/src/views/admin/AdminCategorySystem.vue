<template>
  <div class="admin-panel">
    <div class="category-panel">
      <!-- 上模块：与小说标签增删一致 - 作品主题标签网格 -->
      <section class="tags-upper">
        <h3 class="section-title">作品主题</h3>
        <div class="tag-grid">
          <button
            v-for="tag in tags"
            :key="tag.id"
            type="button"
            class="tag-chip"
            :class="{ active: selectedTagId === tag.id }"
            @click="selectTag(tag)"
          >
            {{ tag.name }}
          </button>
          <p v-if="tagsLoading" class="tag-loading">加载中…</p>
          <p v-else-if="!tags.length" class="tag-empty">暂无标签</p>
        </div>
      </section>
      <!-- 下模块：左-小说下拉（模糊搜索） 右-该小说现有标签 -->
      <section class="category-lower">
        <div class="lower-left">
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
            <p v-if="showNovelDropdown && novelSearch && !filteredNovels.length" class="no-match">无匹配小说</p>
          </div>
        </div>
        <div class="lower-right" v-show="selectedNovel">
          <div class="right-row">
            <h4 class="right-title">当前标签</h4>
            <div v-if="novelDetailLoading" class="tag-loading">加载中…</div>
            <div v-else class="tag-grid novel-tags">
              <span v-for="(name, i) in novelTagNames" :key="i" class="tag-chip static">{{ name }}</span>
              <span v-if="!novelTagNames.length" class="tag-empty">该小说暂无标签</span>
            </div>
          </div>
        </div>
      </section>
      <!-- 下模块：文本框 + 添加 / 删除（与小说标签增删一致） -->
      <section class="tags-lower">
        <div class="input-row">
          <input
            v-model.trim="inputName"
            type="text"
            class="tag-input"
            placeholder="输入标签名称"
            maxlength="50"
            @keydown.enter.prevent="handleAdd"
          />
          <button type="button" class="btn btn-add" :disabled="!selectedNovel" @click="handleAdd">添加</button>
          <button type="button" class="btn btn-del" :disabled="!selectedNovel" @click="handleDelete">删除</button>
        </div>
        <p v-if="message" class="msg" :class="{ error: isError }">{{ message }}</p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { adminTagApi, adminNovelApi, adminNovelTagApi, novelApi } from '../../api'

const tags = ref([])
const tagsLoading = ref(false)
const novels = ref([])
const novelSearch = ref('')
const showNovelDropdown = ref(false)
const selectedNovel = ref(null)
const novelDetail = ref(null)
const novelDetailLoading = ref(false)
const selectedTagId = ref(null)
const inputName = ref('')
const message = ref('')
const isError = ref(false)

const filteredNovels = computed(() => {
  const q = (novelSearch.value || '').trim().toLowerCase()
  if (!q) return novels.value.slice(0, 100)
  return novels.value.filter(n => n.title.toLowerCase().includes(q))
})

const novelTagNames = computed(() => {
  return novelDetail.value?.tagNames || []
})

function selectNovel(n) {
  selectedNovel.value = n
  novelSearch.value = n.title
  showNovelDropdown.value = false
}

function onNovelSearchInput() {
  showNovelDropdown.value = true
  const q = (novelSearch.value || '').trim()
  if (!q || (selectedNovel.value && novelSearch.value !== selectedNovel.value.title)) {
    selectedNovel.value = null
  }
}

function showMsg(text, error = false) {
  message.value = text
  isError.value = error
  if (text) setTimeout(() => { message.value = '' }, error ? 3000 : 4500)
}

function selectTag(tag) {
  selectedTagId.value = tag.id
  inputName.value = tag.name
}

async function handleAdd() {
  if (!selectedNovel.value) {
    showMsg('请先选择小说', true)
    return
  }
  const tagId = selectedTagId.value
  if (!tagId) {
    showMsg('请先在上方点击要添加的标签', true)
    return
  }
  try {
    await adminNovelTagApi.add(selectedNovel.value.id, tagId)
    showMsg('添加成功')
    await fetchNovelDetail(selectedNovel.value.id)
  } catch (e) {
    const msg = e?.statusCode === 409 ? '该小说存在该标签' : (e?.message || '添加失败')
    showMsg(msg, true)
  }
}

async function handleDelete() {
  if (!selectedNovel.value) {
    showMsg('请先选择小说', true)
    return
  }
  const tagId = selectedTagId.value
  if (!tagId) {
    showMsg('请先在上方点击要删除的标签', true)
    return
  }
  try {
    await adminNovelTagApi.remove(selectedNovel.value.id, tagId)
    showMsg('删除成功')
    await fetchNovelDetail(selectedNovel.value.id)
  } catch (e) {
    const msg = e?.statusCode === 404 ? '该小说不存在该标签' : (e?.message || '删除失败')
    showMsg(msg, true)
  }
}

async function fetchTags() {
  tagsLoading.value = true
  try {
    tags.value = await adminTagApi.list()
    if (selectedTagId.value && !tags.value.some(t => t.id === selectedTagId.value)) {
      selectedTagId.value = null
    }
  } catch {
    tags.value = []
  } finally {
    tagsLoading.value = false
  }
}

async function fetchNovels() {
  try {
    novels.value = await adminNovelApi.list()
  } catch {
    novels.value = []
  }
}

async function fetchNovelDetail(id) {
  novelDetailLoading.value = true
  novelDetail.value = null
  try {
    novelDetail.value = await novelApi.getById(id)
  } catch {
    novelDetail.value = null
  } finally {
    novelDetailLoading.value = false
  }
}

watch(selectedNovel, (n) => {
  if (n) fetchNovelDetail(n.id)
  else novelDetail.value = null
})

function onDocumentClick(e) {
  const el = document.querySelector('.novel-select-wrap')
  if (el && !el.contains(e.target)) showNovelDropdown.value = false
}

onMounted(() => {
  fetchTags()
  fetchNovels()
  document.addEventListener('click', onDocumentClick)
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

.category-panel {
  width: 100%;
}

.tags-upper {
  width: 100%;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.tag-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 12px;
}

.tag-chip {
  padding: 8px 14px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  color: #555;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, border-color 0.2s;
}

.tag-chip:hover {
  background: #f0f0f0;
  border-color: #ccc;
}

.tag-chip.static {
  cursor: default;
}

.tag-chip.active {
  background: #e91e63;
  color: #fff;
  border-color: #e91e63;
}

.tag-loading,
.tag-empty {
  color: #888;
  font-size: 0.9rem;
  margin: 0;
}

.category-lower {
  width: 100%;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.lower-left {
  flex: 0 1 320px;
  min-width: 200px;
}

.novel-select-wrap {
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

.lower-right {
  flex: 1;
  min-width: 200px;
}

.right-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.right-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex-shrink: 0;
}

.novel-tags .tag-chip {
  cursor: default;
}

.tags-lower {
  width: 100%;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.input-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.tag-input {
  flex: 1;
  min-width: 160px;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
}

.tag-input:focus {
  outline: none;
  border-color: #2c3e50;
}

.btn {
  padding: 10px 18px;
  border-radius: 6px;
  font-size: 0.95rem;
  cursor: pointer;
  border: none;
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

.btn-del {
  background: #fff;
  color: #c0392b;
  border: 1px solid #c0392b;
}

.btn-del:hover:not(:disabled) {
  background: #c0392b;
  color: #fff;
}

.btn-del:disabled {
  background: #f0f0f0;
  color: #999;
  border-color: #ddd;
  cursor: not-allowed;
  opacity: 0.8;
}

.msg {
  margin: 10px 0 0 0;
  font-size: 0.9rem;
  color: #2e7d32;
}

.msg.error {
  color: #c62828;
}
</style>
