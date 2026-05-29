<template>
  <div class="library-page">
    <div class="container">
      <h1 class="page-title">{{ publisherName || '文库' }}</h1>
      <ul v-if="novels.length" class="novel-list">
        <li v-for="n in novels" :key="n.id">
          <router-link :to="`/novel/${n.id}`" class="novel-link">{{ n.title }}</router-link>
        </li>
      </ul>
      <p v-else class="empty">暂无该文库小说</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { novelApi } from '../api'

const route = useRoute()
const publisherNovels = ref([])

const publisherId = computed(() => {
  const id = route.query.publisher
  return id != null ? Number(id) : null
})

const publisherName = computed(() => {
  const block = publisherNovels.value.find(x => x.publisherId === publisherId.value)
  return block ? block.publisherName : ''
})

const novels = computed(() => {
  if (publisherId.value == null) return []
  const block = publisherNovels.value.find(x => x.publisherId === publisherId.value)
  return block ? (block.novels || []) : []
})

async function load() {
  try {
    const data = await novelApi.getPublisherNovels()
    publisherNovels.value = Array.isArray(data) ? data : []
  } catch (e) {
    publisherNovels.value = []
  }
}

watch(() => route.query.publisher, load, { immediate: true })
</script>

<style scoped>
.library-page {
  padding-top: calc(var(--header-height) + 24px);
  min-height: 100vh;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}
.page-title {
  font-size: 1.5rem;
  color: var(--text);
  margin-bottom: 20px;
}
.novel-list {
  list-style: none;
}
.novel-list li {
  margin-bottom: 8px;
}
.novel-link {
  color: var(--text);
  text-decoration: none;
}
.novel-link:hover {
  color: var(--primary);
}
.empty {
  color: var(--text-muted);
}
</style>
