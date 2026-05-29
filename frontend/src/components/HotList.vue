<template>
  <div class="hot-list">
    <div v-if="title || showMore" class="list-header">
      <h3 v-if="title" class="list-title">{{ title }}</h3>
      <router-link v-if="showMore" to="/ranking" class="more-link">更多</router-link>
    </div>
    <ul>
      <li v-for="(item, i) in list" :key="item.id" class="item" :class="{ 'text-only': firstOnlyCover && i > 0 }">
        <span v-if="showRank" class="rank" :class="getRankClass(i)">{{ i + 1 }}</span>
        <div v-if="showCover(item, i)" class="cover-mini">
          <img :src="item.coverUrl || 'https://via.placeholder.com/40x54/f0f0f0/999?text=无'" :alt="item.title" />
        </div>
        <div class="meta">
          <div class="title-author-row">
            <router-link :to="`/novel/${item.id}`" class="title">{{ item.title }}</router-link>
            <!-- 新书榜：第2项起显示推荐数（与书名同行居右），第1项有封面时显示作者 -->
            <span v-if="showRecommendInline && i > 0" class="author-right">{{ formatRecommendCount(item, i) }}</span>
            <span v-else-if="showRecommendInline && i === 0 && !showCover(item, i)" class="author-right">{{ formatRecommendCount(item, i) }}</span>
            <span v-else-if="(countType === 'view' || countType === 'favorite') && i > 0" class="author-right">{{ formatCount(item, i, countType) }}</span>
            <span v-else class="author-right">{{ item.authorName }}<span v-if="showPublisher(item, i)"> [{{ item.publisherName }}]</span></span>
          </div>
          <p v-if="showRecommendInline && i === 0 && showCover(item, i) && item.recommendCount" class="count">{{ item.recommendCount }}人推荐</p>
          <p v-else-if="showRank && !showRecommendInline && (countType === 'view' || countType === 'favorite') && i === 0 && showCover(item, i)" class="count">{{ formatCount(item, 0, countType) }}</p>
          <p v-else-if="showRank && !showRecommendInline && countType !== 'view' && countType !== 'favorite' && item.recommendCount" class="count">{{ item.recommendCount }}人推荐</p>
          <p v-else-if="showRank && !showRecommendInline && countType !== 'view' && countType !== 'favorite' && item.viewCount" class="count">{{ item.viewCount }}</p>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup>
const props = defineProps({
  list: { type: Array, default: () => [] },
  title: { type: String, default: '' },
  showRank: { type: Boolean, default: false },
  firstOnlyCover: { type: Boolean, default: false },
  showRecommendInline: { type: Boolean, default: false },
  /** 点击榜: view, 收藏榜: favorite, 推荐榜: recommend */
  countType: { type: String, default: '' },
  showMore: { type: Boolean, default: false }
})

const showCover = (item, i) => {
  if (!props.firstOnlyCover) return item.coverUrl
  return i === 0
}

const showPublisher = (item, i) => {
  if (props.firstOnlyCover && i > 0) return false
  return item.publisherName
}

// 新书榜：第一本显示"185人推荐"，其余显示纯数字
const formatRecommendCount = (item, i) => {
  const c = item.recommendCount
  if (!c) return ''
  return i === 0 ? `${c}人推荐` : String(c)
}

const formatCount = (item, i, type) => {
  if (type === 'view') {
    const c = item.viewCount ?? 0
    return i === 0 ? `${c}人点击` : String(c)
  }
  if (type === 'favorite') {
    const c = item.favoriteCount ?? 0
    return i === 0 ? `${c}人收藏` : String(c)
  }
  const c = item.recommendCount ?? 0
  return i === 0 ? `${c}人推荐` : String(c)
}

const getRankClass = (i) => {
  if (i === 0) return 'gold'
  if (i === 1) return 'silver'
  if (i === 2) return 'bronze'
  return ''
}
</script>

<style scoped>
.hot-list {
  background: var(--card-bg);
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
  width: 100%;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border);
}
.list-title {
  font-size: 1rem;
  margin: 0;
}
.more-link {
  font-size: 0.85rem;
  color: var(--text-muted);
  text-decoration: none;
}
.more-link:hover { color: var(--primary); }

.item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
}

.item:last-child { border-bottom: none; }

.item.text-only {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
.item.text-only .meta { flex: 1; }

.title-author-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  width: 100%;
}
.title-author-row .title {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.author-right {
  flex-shrink: 0;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.rank {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 600;
  border-radius: 4px;
  background: #eee;
  color: #666;
}

.rank.gold { background: #e74c3c; color: #fff; }
.rank.silver { background: #f39c12; color: #fff; }
.rank.bronze { background: #f1c40f; color: #333; }

.cover-mini {
  width: 40px;
  height: 54px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
}

.cover-mini img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.meta { flex: 1; min-width: 0; }

.meta .title {
  font-size: 0.9rem;
  display: block;
}

.meta .title:hover { color: var(--primary); }

.count {
  font-size: 0.8rem;
  color: var(--text-muted);
  margin-top: 4px;
}
</style>
