<template>
  <router-link :to="`/novel/${novel.id}`" class="novel-card" :class="{ small }">
    <div class="cover-wrap">
      <img :src="novel.coverUrl || 'https://via.placeholder.com/100x133/f0f0f0/999?text=无封面'" :alt="novel.title" />
    </div>
    <div v-if="!small" class="info">
      <h4 class="title">{{ novel.title }}</h4>
      <p class="synopsis">{{ novel.synopsis }}</p>
      <p class="author">{{ novel.authorName }}</p>
      <span v-if="novel.tagName" class="tag">{{ novel.tagName }}</span>
    </div>
    <p v-else class="title-s">{{ novel.title }}</p>
  </router-link>
</template>

<script setup>
defineProps({
  novel: { type: Object, required: true },
  small: { type: Boolean, default: false }
})
</script>

<style scoped>
.novel-card {
  display: block;
  background: var(--card-bg);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
  transition: transform 0.2s;
}

.novel-card:hover { transform: translateY(-2px); }

.novel-card.small { flex: 0 0 100px; }

.cover-wrap {
  aspect-ratio: 3/4;
  overflow: hidden;
}

.cover-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.title-s {
  padding: 8px;
  font-size: 0.85rem;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info { padding: 12px; }

.title {
  font-size: 0.95rem;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.synopsis {
  font-size: 0.8rem;
  color: var(--text-muted);
  -webkit-line-clamp: 2;
  overflow: hidden;
  margin-bottom: 8px;
}

.author { font-size: 0.85rem; color: var(--text-muted); margin-bottom: 6px; }

.tag {
  display: inline-block;
  padding: 2px 8px;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 0.75rem;
}
</style>
