<template>
  <div class="home">
    <!-- 1. 特色推荐 + 强推榜 -->
    <section class="section hero">
      <div class="container row">
        <div class="featured-area">
          <FeaturedNovel v-if="currentFeatured" :novel="currentFeatured" />
          <div class="recommend-scroll">
            <div class="scroll-list">
              <div
                v-for="n in recommendStripList"
                :key="n.id"
                class="strip-item"
                :class="{ active: currentFeatured?.id === n.id }"
                @mouseenter="currentFeatured = n"
              >
                <router-link :to="`/novel/${n.id}`" class="strip-link">
                  <img :src="n.coverUrl || 'https://via.placeholder.com/80x107/f0f0f0/999?text=无封面'" :alt="n.title" />
                </router-link>
              </div>
            </div>
          </div>
          <div class="completed-tag-list">
            <div class="tag-title">
              <span class="tag-line">完书</span>
              <span class="tag-line">推荐</span>
            </div>
            <div class="completed-box">
              <router-link v-for="c in completedSix" :key="c.id" :to="`/novel/${c.id}`" class="completed-item">{{ c.publisherName }}·{{ c.title }}</router-link>
            </div>
          </div>
          <!-- 新书精选（3x3网格布局） -->
          <div class="new-select-section">
            <h3 class="new-select-title new-select-title-underline">新书精选</h3>
            <div class="new-select-grid">
              <NewSelectCard v-for="n in homeData.newSelectList" :key="n.id" :novel="n" />
            </div>
          </div>
        </div>
        <aside class="sidebar">
          <HotList :list="homeData.strongRecommendList" title="强推榜" :first-only-cover="true" />
          <HotList :list="homeData.newBookChart" title="新书榜" :showRank="true" :first-only-cover="true" :showRecommendInline="true" />
        </aside>
      </div>
    </section>

    <!-- 
      2. 热门小说模块
      布局：左侧33.33%显示6本热门小说轮播，右侧66.67%显示8个文库的小说列表（4列x2行）
    -->
    <section class="section hot-novel-section">
      <h2 class="section-title hot-novel-title">热门小说</h2>
      <div class="container hot-novel-container">
        <!-- 左侧：6本热门小说轮播模块 -->
        <div class="hot-novel-left">
          <HotNovelCarousel :novels="homeData.hotNovelsList" />
        </div>
        <!-- 右侧：8个文库的小说列表模块 -->
        <div class="hot-novel-right">
          <!-- 遍历前8个文库，每个文库显示一个模块 -->
          <div v-for="pn in homeData.publisherNovels.slice(0, 8)" :key="pn.publisherId" class="publisher-block">
            <!-- 文库标题和"更多"链接 -->
            <div class="publisher-header">
              <span>{{ pn.publisherName }}</span>
              <a href="#">更多&gt;</a>
            </div>
            <!-- 该文库的小说列表 -->
            <ul class="novel-list">
              <li v-for="n in pn.novels" :key="n.id">
                <!-- 小说链接：显示[文库]前缀（淡灰色）+ 书名（黑色），超过15字用...截断 -->
                <router-link :to="`/novel/${n.id}`" class="novel-link" v-html="formatNovelTitle(pn.publisherName, n.title)"></router-link>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <!-- 3. 经典完本 + 完本推荐 + 热点榜 -->
    <section class="section three-col">
      <div class="container row">
        <div class="col classic">
          <h3>经典完本</h3>
          <HotNovelCarousel :novels="classicCompletedList" />
        </div>
        <div class="col completed-rec">
          <h3>完本推荐</h3>
          <div class="completed-recommend-grid">
            <NewSelectCard v-for="c in homeData.completedRecommend?.slice(0, 6)" :key="c.id" :novel="c" />
          </div>
        </div>
        <aside class="col hot-sidebar">
          <HotList :list="homeData.hotChartList" title="热点榜" :showRank="true" :first-only-cover="true" :showRecommendInline="true" />
        </aside>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { novelApi } from '../api'
import FeaturedNovel from '../components/FeaturedNovel.vue'
import NovelCard from '../components/NovelCard.vue'
import HotList from '../components/HotList.vue'
import NewSelectCard from '../components/NewSelectCard.vue'
import HotNovelCarousel from '../components/HotNovelCarousel.vue'

const homeData = ref({
  featured: null,
  strongRecommendList: [],
  hotChartList: [],
  hotList: [],
  recommendList: [],
  homeRecommendList: [],
  newSelectList: [],
  newBookChart: [],
  completedRecommend: [],
  classicCompletedList: [],
  publisherNovels: [],
  hotNovelsList: []
})

const currentFeatured = ref(null)

// 首页推荐榜6本：优先使用固定配置（与图片完全一致），否则回退到推荐列表
const recommendStripList = computed(() => {
  const homeList = homeData.value.homeRecommendList || []
  if (homeList.length >= 6) return homeList.slice(0, 6)
  const list = homeData.value.recommendList || []
  const feat = homeData.value.featured
  if (!feat) return list.slice(0, 6)
  const rest = list.filter(n => n.id !== feat.id)
  return [feat, ...rest].slice(0, 6)
})

const completedSix = computed(() => (homeData.value.completedRecommend || []).slice(0, 6))

watch(() => homeData.value.featured, (v) => {
  if (v && !currentFeatured.value) currentFeatured.value = v
}, { immediate: true })

const classicCompleted = computed(() =>
  homeData.value.classicCompleted ||
  (Array.isArray(homeData.value.completedRecommend) && homeData.value.completedRecommend[0]) ||
  null
)

// 经典完本列表：使用后端返回的classicCompletedList
const classicCompletedList = computed(() => {
  if (homeData.value.classicCompletedList && Array.isArray(homeData.value.classicCompletedList) && homeData.value.classicCompletedList.length > 0) {
    return homeData.value.classicCompletedList
  }
  return []
})

const getPublisherPrefix = (name) => {
  if (!name) return ''
  if (name.length >= 2) return name.substring(0, 2)
  return name
}

const formatNovelTitle = (publisherName, title) => {
  const prefix = getPublisherPrefix(publisherName)
  const prefixHtml = `<span class="publisher-prefix">[${prefix}]</span>`
  const fullTitle = prefixHtml + title
  
  // 超过15个字就用...补齐（不包括HTML标签）
  const textLength = prefix.length + 2 + title.length // [文库] + 书名
  if (textLength > 15) {
    const remainingChars = 15 - prefix.length - 2 // 减去[文库]的长度
    if (remainingChars > 0) {
      return prefixHtml + title.substring(0, remainingChars) + '...'
    } else {
      return prefixHtml + '...'
    }
  }
  return fullTitle
}

onMounted(async () => {
  try {
    const data = await novelApi.getHomeData()
    homeData.value = data
    if (data.featured) currentFeatured.value = data.featured
  } catch (e) {
    const fallback = {
      featured: { id: 1, title: '用催眠APP打造梦幻后宫生活', authorName: 'みょん', tagNames: ['科幻','校园青春','后宫','欢乐向'], synopsis: '有女友的经历=年龄。这样的我在手机里安装了可以操纵任何人的「催眠APP」。', latestChapterTitle: '第七章 尽情讴歌青春!' },
      strongRecommendList: [],
      hotChartList: [],
      hotList: [],
      recommendList: [],
      homeRecommendList: [],
      newSelectList: [],
      newBookChart: [],
      completedRecommend: [],
      classicCompletedList: [],
      publisherNovels: [],
      hotNovelsList: [],
      classicCompleted: null
    }
    homeData.value = fallback
    currentFeatured.value = fallback.featured
  }
})
</script>

<style scoped>
.home { padding-bottom: 40px; }

.section {
  padding: 24px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.row {
  display: flex;
  gap: 24px;
}

.section-title {
  max-width: 1200px;
  margin: 0 auto 16px;
  padding: 0 20px;
  font-size: 1.25rem;
}

.featured-area { flex: 1; min-width: 0; }

.sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.recommend-scroll { margin-top: 16px; }
.scroll-list {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 8px;
}
.scroll-list::-webkit-scrollbar { height: 6px; }
.scroll-list::-webkit-scrollbar-thumb { background: #ccc; border-radius: 3px; }

.strip-item {
  flex: 0 0 80px;
  border-radius: 4px;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  transition: border-color 0.2s, opacity 0.2s;
}
.strip-item:hover, .strip-item.active {
  border-color: var(--primary);
}
.strip-item:not(.active) { opacity: 0.85; }
.strip-link {
  display: block;
}
.strip-link img {
  width: 80px;
  height: 107px;
  object-fit: cover;
  display: block;
}

.completed-tag-list {
  margin-top: 20px;
  max-height: 95px;
  display: flex;
  align-items: flex-start;
  gap: 0;
  background: var(--card-bg);
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  overflow: hidden;
  border-radius: 8px;
}

.tag-title {
  flex-shrink: 0;
  background: var(--primary);
  color: #fff;
  padding: 16px 12px;
  font-size: 0.9rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  line-height: 1.4;
  min-height: 95px;
  box-sizing: border-box;
  border-radius: 8px 0 0 8px;
}

.tag-line {
  display: block;
  text-align: center;
}

.completed-box {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(2, 1fr);
  padding: 12px 16px;
  gap: 8px 0;
  position: relative;
  border-radius: 0 8px 8px 0;
}

.completed-item {
  font-size: 0.9rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding-right: 12px;
  position: relative;
}

/* 为第1列和第2列的项目添加右侧分隔符 */
.completed-item:nth-child(3n-2)::after,
.completed-item:nth-child(3n-1)::after {
  content: '|';
  position: absolute;
  right: 0;
  top: 0;
  color: #ddd;
  font-size: 0.9rem;
  pointer-events: none;
  line-height: 1.4;
}

.completed-item:hover { color: var(--primary); }

/* 新书精选（3x3网格布局） */
.new-select-section {
  margin-top: 24px;
}
.new-select-title {
  margin-bottom: 16px;
  font-size: 1.1rem;
  font-weight: 600;
}
.new-select-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

/* 热门小说模块样式 */
.hot-novel-section {
  padding: 24px 0;
}

/* 热门小说容器：左右布局，无间距 */
.hot-novel-container {
  display: flex;
  gap: 0; /* 左右模块之间无间距 */
  width: 100%;
}

/* 左侧模块：占据33.33%宽度，显示6本热门小说轮播 */
.hot-novel-left {
  width: calc(20%);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  height: fit-content;
}

/* 右侧模块：占据66.67%宽度，4列网格布局，显示8个文库模块 */
.hot-novel-right {
  width: calc(80%);
  display: grid;
  grid-template-columns: repeat(4, 1fr); /* 4列布局 */
  gap: 16px; /* 内部模块间距 */
  padding-left: 0px; /* 与左侧模块的间距（减少空白） */
}

/* 文库模块容器 */
.hot-novel-right .publisher-block {
  width: 100%;
  background: #f5f5f5; /* 淡灰色背景，与左侧模块一致 */
  padding: 12px;
  border-radius: 0;
  box-shadow: none;
  display: flex;
  flex-direction: column;
  height: fit-content;
}

/* 小说列表样式 */
.novel-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

/* 小说列表项样式 */
.hot-novel-right .novel-list li {
  padding: 4px 0;
  font-size: 0.9rem;
  line-height: 1.4;
}

/* 小说链接样式 */
.novel-link {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

/* [文库]前缀样式：淡灰色 */
.novel-link .publisher-prefix {
  color: #999;
}

/* 文库标题区域 */
.hot-novel-right .publisher-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: 600;
  font-size: 0.9rem;
}

/* "更多"链接样式 */
.hot-novel-right .publisher-header a {
  font-size: 0.85rem;
  font-weight: normal;
  color: var(--text-muted);
}

/* "更多"链接悬停效果 */
.hot-novel-right .publisher-header a:hover {
  color: var(--primary);
}

/* "热门小说"标题样式 */
.hot-novel-title {
  position: relative;
  padding-bottom: 8px;
}

/* "热门小说"标题下方的下划线 */
.hot-novel-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: #333;
}

.new-select-title-underline {
  position: relative;
  padding-bottom: 8px;
}

.new-select-title-underline::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: #333;
}

.publisher-block {
  background: var(--card-bg);
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}

.publisher-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 600;
}

.publisher-header a { font-size: 0.85rem; font-weight: normal; color: var(--text-muted); }
.publisher-header a:hover { color: var(--primary); }

.novel-list li {
  padding: 6px 0;
  font-size: 0.9rem;
}
.novel-list a:hover { color: var(--primary); }

/* 三栏 */
.three-col .row { 
  align-items: stretch; /* 确保所有列高度一致 */
  gap: 24px;
}
.three-col .col.classic { 
  flex: 0 0 calc(20% - 16px); /* 经典完本占20%，减去部分gap，与热门小说模块左侧宽度相同 */
  min-width: 0; 
  display: flex;
  flex-direction: column;
}
.three-col .col.completed-rec { 
  flex: 1; /* 完本推荐自动填充剩余空间 */
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.three-col .col.hot-sidebar { 
  flex: 0 0 280px; /* 热点榜宽度与新书榜模块相同（280px） */
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.three-col h3 { margin-bottom: 16px; font-size: 1.1rem; }

/* 完本推荐网格布局：2列x3行，每行2本 */
.completed-recommend-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  width: 100%;
}

/* 完本推荐模块中的NewSelectCard样式调整，确保每行的两本完书模块能占满模块的宽 */
.completed-recommend-grid .new-select-card {
  width: 100%;
  min-width: 0;
}
</style>
