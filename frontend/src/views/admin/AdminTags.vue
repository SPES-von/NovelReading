<template>
  <div class="admin-panel">
    <div class="tags-panel">
      <!-- 上模块：作品主题标签网格 -->
      <section class="tags-upper">
        <h3 class="section-title">作品主题</h3>
        <div class="tag-grid">
          <button
            v-for="tag in tags"
            :key="tag.id"
            type="button"
            class="tag-chip"
            :class="{ active: selectedId === tag.id }"
            @click="selectTag(tag)"
          >
            {{ tag.name }}
          </button>
          <p v-if="loading" class="tag-loading">加载中…</p>
          <p v-else-if="!tags.length" class="tag-empty">暂无标签</p>
        </div>
      </section>
      <!-- 下模块：文本框 + 添加 / 删除 -->
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
          <button type="button" class="btn btn-add" @click="handleAdd">添加</button>
          <button type="button" class="btn btn-del" @click="handleDelete">删除</button>
        </div>
        <p v-if="message" class="msg" :class="{ error: isError }">{{ message }}</p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminTagApi } from '../../api'

const tags = ref([])
const loading = ref(false)
const selectedId = ref(null)
const inputName = ref('')
const message = ref('')
const isError = ref(false)

function showMsg(text, error = false) {
  message.value = text
  isError.value = error
  if (text) setTimeout(() => { message.value = '' }, error ? 3000 : 4500)
}

function selectTag(tag) {
  selectedId.value = tag.id
  inputName.value = tag.name
}

async function fetchTags() {
  loading.value = true
  try {
    tags.value = await adminTagApi.list()
    if (selectedId.value && !tags.value.some(t => t.id === selectedId.value)) {
      selectedId.value = null
    }
  } catch (e) {
    showMsg(e?.message || '加载标签失败', true)
  } finally {
    loading.value = false
  }
}

async function handleAdd() {
  const name = inputName.value
  if (!name) {
    showMsg('请输入标签名称', true)
    return
  }
  try {
    await adminTagApi.add(name)
    inputName.value = ''
    showMsg('添加成功')
    await fetchTags()
  } catch (e) {
    showMsg(e?.message || '添加失败', true)
  }
}

async function handleDelete() {
  const id = selectedId.value
  if (!id) {
    showMsg('请先在上方点击要删除的标签', true)
    return
  }
  try {
    await adminTagApi.delete(id)
    selectedId.value = null
    inputName.value = ''
    showMsg('删除成功')
    await fetchTags()
  } catch (e) {
    showMsg(e?.message || '删除失败', true)
  }
}

onMounted(() => fetchTags())
</script>

<style scoped>
.admin-panel {
  width: 98%;
  max-width: none;
  margin: 0 auto;
}

.tags-panel {
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

.btn-add:hover {
  background: #34495e;
}

.btn-del {
  background: #fff;
  color: #c0392b;
  border: 1px solid #c0392b;
}

.btn-del:hover {
  background: #c0392b;
  color: #fff;
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
