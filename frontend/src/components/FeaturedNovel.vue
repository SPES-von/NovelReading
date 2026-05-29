<template>
  <div v-if="novel" class="featured-novel">
    <div class="cover-wrap">
      <img :src="novel.coverUrl || 'https://via.placeholder.com/180x240/f0f0f0/999?text=无封面'" :alt="novel.title" class="cover" />
    </div>
    <div class="info">
      <h1 class="title">{{ novel.title }}</h1>
      <p class="author">作者 {{ novel.authorName }}</p>
      <div v-if="displayTags.length" class="tags">
        <span class="tag-label">类型</span>
        <span v-for="t in displayTags" :key="t" class="tag">{{ t }}</span>
      </div>
      <p class="synopsis">{{ novel.synopsis }}</p>
      <p v-if="novel.latestChapterTitle" class="latest">最新章节 {{ novel.latestChapterTitle }}</p>
      <router-link :to="`/novel/${novel.id}`" class="btn-read">立即阅读</router-link>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({
  novel: { type: Object, default: null }
})
const displayTags = computed(() => {
  const n = props.novel
  if (!n) return []
  if (Array.isArray(n.tagNames) && n.tagNames.length) return n.tagNames
  if (n.tagName) return [n.tagName]
  return []
})
</script>

<style scoped>
.featured-novel {
  display: flex;
  gap: 24px;
  background: var(--card-bg);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 6px rgba(0,0,0,.08);
}

.cover-wrap {
  width: 180px;
  flex-shrink: 0;
}

.cover {
  width: 100%;
  aspect-ratio: 3/4;
  object-fit: cover;
  border-radius: 4px;
}

.info { flex: 1; min-width: 0; }

.title {
  font-size: 1.35rem;
  margin-bottom: 8px;
}

.author {
  color: var(--text-muted);
  font-size: 0.95rem;
  margin-bottom: 8px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tag-label {
  font-size: 0.85rem;
  color: var(--text-muted);
  margin-right: 4px;
}
.tag {
  padding: 2px 10px;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 0.85rem;
  color: var(--text);
}

.synopsis {
  font-size: 0.95rem;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.latest {
  font-size: 0.9rem;
  color: var(--primary);
  margin-bottom: 16px;
}

.btn-read {
  display: inline-block;
  padding: 10px 24px;
  background: var(--primary);
  color: #fff;
  border-radius: 4px;
  font-weight: 500;
}

.btn-read:hover {
  background: var(--primary-dark);
}
</style>
