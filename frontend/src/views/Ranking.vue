<template>
  <div class="ranking-page">
    <div class="container">
      <h1 class="page-title">轻小说榜单</h1>

      <p v-if="loading" class="tip">加载中…</p>
      <p v-else-if="error" class="error-tip">{{ error }}</p>
      <!-- 6 个榜单 2x3 网格 -->
      <div v-else class="charts-grid">
        <section
          v-for="chart in chartConfigs"
          :key="chart.key"
          class="chart-block"
        >
          <h3 class="chart-title">{{ chart.title }}</h3>
          <ul class="chart-list">
            <li
              v-for="(item, i) in (rankings[chart.key] || [])"
              :key="item.id"
              class="chart-item"
              :class="{ 'first-item': i === 0, 'text-only': i > 0 }"
            >
              <span class="rank-num" :class="getRankClass(i)">{{ i + 1 }}</span>
              <div v-if="i === 0" class="cover-wrap">
                <router-link :to="`/novel/${item.id}`" class="cover-link">
                  <img
                    :src="item.coverUrl || 'https://via.placeholder.com/80x107/f0f0f0/999?text=无'"
                    :alt="item.title"
                    class="cover-img"
                  />
                </router-link>
              </div>
              <div class="meta">
                <div class="title-row">
                  <router-link :to="`/novel/${item.id}`" class="novel-title">
                    {{ item.title }}
                  </router-link>
                  <span class="value-text">{{ formatValue(chart, item, i) }}</span>
                </div>
                <p v-if="i === 0" class="author-pub">
                  {{ item.authorName }}<span v-if="item.publisherName"> [{{ item.publisherName }}]</span>
                </p>
              </div>
            </li>
          </ul>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { novelApi } from '../api'

const loading = ref(true)
const error = ref('')
const rankings = ref({
  wordCount: [],
  rating: [],
  viewCount: [],
  recommendCount: [],
  flowerCount: [],
  favoriteCount: []
})

const chartConfigs = [
  { key: 'wordCount', title: '字数榜' },
  { key: 'rating', title: '评分榜' },
  { key: 'viewCount', title: '热度榜' },
  { key: 'recommendCount', title: '推荐榜' },
  { key: 'flowerCount', title: '鲜花榜' },
  { key: 'favoriteCount', title: '收藏榜' }
]

function getRankClass(i) {
  if (i === 0) return 'rank-first'
  if (i === 1 || i === 2) return 'rank-medal'
  return 'rank-normal'
}

function formatValue(chart, item, i) {
  const key = chart.key
  if (key === 'wordCount') {
    const n = item.wordCount || 0
    if (n >= 10000) return `${(n / 10000).toFixed(1)}万字`
    return `${n}字`
  }
  if (key === 'rating') {
    const r = item.rating != null ? Number(item.rating) : 0
    return `${r}分`
  }
  if (key === 'viewCount') {
    return String(item.viewCount ?? 0)
  }
  if (key === 'recommendCount') {
    const c = item.recommendCount ?? 0
    return i === 0 ? `${c}人推荐` : String(c)
  }
  if (key === 'flowerCount') {
    return String(item.flowerCount ?? 0)
  }
  if (key === 'favoriteCount') {
    const c = item.favoriteCount ?? 0
    return i === 0 ? `${c}人收藏` : String(c)
  }
  return ''
}

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await novelApi.getRankings()
    rankings.value = res && typeof res === 'object' ? res : rankings.value
  } catch (e) {
    error.value = '获取排行榜失败，请检查后端是否启动（端口 8080）及接口 /api/novels/rankings'
    console.error('获取排行榜失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.ranking-page {
  min-height: calc(100vh - var(--header-height));
  padding: 24px 0 48px;
  background: #f5f5f5;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  box-sizing: border-box;
}

.page-title {
  font-size: 1.5rem;
  color: var(--text);
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--primary);
}

.tip {
  color: var(--text-muted);
  margin: 16px 0;
}

.error-tip {
  color: #c0392b;
  margin: 16px 0;
  font-size: 0.95rem;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  width: 100%;
  max-width: 100%;
}

.chart-block {
  background: var(--card-bg);
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 16px;
  min-height: 420px;
  display: flex;
  flex-direction: column;
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
}

.chart-title {
  font-size: 1rem;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border);
  color: var(--text);
  flex-shrink: 0;
}

.chart-list {
  list-style: none;
  margin: 0;
  padding: 0;
  flex: 1;
  min-height: 0;
}

.chart-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
}

.chart-item:last-child {
  border-bottom: none;
}

.chart-item.text-only {
  align-items: center;
}

.chart-item.text-only .meta {
  flex: 1;
}

.chart-item.text-only .title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.rank-num {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 600;
  border-radius: 4px;
}

.rank-first {
  background: #e74c3c;
  color: #fff;
}

.rank-medal {
  background: #f39c12;
  color: #fff;
}

.rank-normal {
  background: #e0e0e0;
  color: #666;
}

.cover-wrap {
  flex-shrink: 0;
}

.cover-link {
  display: block;
}

.cover-img {
  width: 80px;
  height: 107px;
  object-fit: cover;
  border-radius: 4px;
}

.meta {
  flex: 1;
  min-width: 0;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.novel-title {
  font-size: 0.95rem;
  color: var(--text);
  text-decoration: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
  flex: 1;
}

.novel-title:hover {
  color: var(--primary);
}

.value-text {
  font-size: 0.85rem;
  color: var(--text-muted);
  flex-shrink: 0;
}

.author-pub {
  font-size: 0.8rem;
  color: var(--text-muted);
  margin: 4px 0 0 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 900px) {
  .charts-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 560px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
