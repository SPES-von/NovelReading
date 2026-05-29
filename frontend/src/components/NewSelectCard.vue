<template>
  <router-link :to="`/novel/${novel.id}`" class="new-select-card">
    <div class="cover-wrap">
      <img :src="novel.coverUrl || 'https://via.placeholder.com/80x107/f0f0f0/999?text=无封面'" :alt="novel.title" />
    </div>
    <div class="content">
      <h4 class="title">{{ truncatedTitle }}</h4>
      <p class="synopsis">{{ truncatedSynopsis }}</p>
      <div class="footer">
        <span class="author">{{ novel.authorName }}</span>
        <span v-if="novel.publisherName" class="publisher-tag">{{ novel.publisherName }}</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  novel: { type: Object, required: true }
})

// 书名截取前10字，超过加......
const truncatedTitle = computed(() => {
  const title = props.novel.title || ''
  if (title.length <= 10) return title
  return title.substring(0, 10) + '......'
})

// 简介截取前20字，超过加......
const truncatedSynopsis = computed(() => {
  const synopsis = props.novel.synopsis || ''
  if (synopsis.length <= 20) return synopsis
  return synopsis.substring(0, 20) + '......'
})
</script>

<style scoped>
.new-select-card {
  display: flex;
  gap: 12px;
  background: var(--card-bg);
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  transition: transform 0.2s, box-shadow 0.2s;
  text-decoration: none;
  color: inherit;
  height: 100%;
}

.new-select-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 6px rgba(0,0,0,.1);
}

.cover-wrap {
  flex-shrink: 0;
  width: 80px;
  height: 107px;
  border-radius: 4px;
  overflow: hidden;
}

.cover-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.title {
  font-size: 0.9rem;
  font-weight: 600;
  margin: 0 0 6px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  color: var(--text);
}

.synopsis {
  font-size: 0.8rem;
  color: var(--text-muted);
  margin: 0 0 8px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.4em;
}

.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  gap: 8px;
}

.author {
  font-size: 0.8rem;
  color: var(--text-muted);
  flex-shrink: 0;
}

.publisher-tag {
  font-size: 0.75rem;
  padding: 2px 8px;
  background: #ff6b35;
  color: #fff;
  border: 1px solid #ff6b35;
  border-radius: 4px;
  white-space: nowrap;
  flex-shrink: 0;
  font-weight: 500;
}
</style>
