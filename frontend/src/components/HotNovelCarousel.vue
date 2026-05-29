<!--
  热门小说轮播组件
  功能：展示6本热门小说，支持轮播切换
  特点：
    1. 封面堆叠效果：只显示3张封面（当前+前一张+后一张），当前封面最大，其他封面较小且部分遮挡
    2. 自动轮播：每2秒自动切换到下一本小说
    3. 手动切换：点击封面或指示器可手动切换
    4. 简介换行：每14个字换一行，最多3行，超出用......补齐
-->
<template>
  <!-- 热门小说轮播容器 -->
  <div v-if="novels && novels.length > 0" class="hot-novel-carousel">
    <div class="carousel-container">
      <div class="novel-display">
        <!-- 封面堆叠区域：显示3张封面（当前+前一张+后一张） -->
        <div class="cover-stack">
          <div
            v-for="idx in getVisibleIndices"
            :key="novels[idx].id"
            class="cover-item"
            :class="{ 'cover-active': currentIndex === idx }"
            :style="getCoverStyle(idx)"
            @click="currentIndex = idx"
          >
            <!-- 小说封面图片 -->
            <img
              :src="novels[idx].coverUrl || 'https://via.placeholder.com/180x240/f0f0f0/999?text=无封面'"
              :alt="novels[idx].title"
              class="cover-img"
            />
          </div>
        </div>
        
        <!-- 小说信息区域 -->
        <div class="novel-info">
          <!-- 淡入淡出过渡效果 -->
          <transition name="fade" mode="out-in">
            <div :key="currentNovel.id" class="novel-info-content">
              <!-- 小说标题（超过15字截断） -->
              <h2 class="novel-title">{{ truncateTitle(currentNovel.title) }}</h2>
              <!-- 作者信息 -->
              <p class="novel-author">作者：{{ currentNovel.authorName }}</p>
              <!-- 出版社和字数信息 -->
              <p class="novel-publisher">
                {{ currentNovel.publisherName }}
                <!-- 字数显示（超过1万字显示为"X.X万字"） -->
                <span v-if="currentNovel.wordCount" class="word-count">| {{ formatWordCount(currentNovel.wordCount) }}</span>
              </p>
              <!-- 小说简介（每14字换行，最多3行，超出用......补齐） -->
              <p class="novel-synopsis">{{ truncateSynopsis(currentNovel.synopsis) }}</p>
              <!-- 立即阅读按钮 -->
              <router-link :to="`/novel/${currentNovel.id}`" class="btn-read">立即阅读</router-link>
              <!-- 轮播指示器：显示在"立即阅读"按钮下方 -->
              <div class="carousel-indicators">
                <span
                  v-for="(novel, index) in novels"
                  :key="novel.id"
                  class="indicator"
                  :class="{ active: currentIndex === index }"
                  @click="currentIndex = index"
                ></span>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

/**
 * 组件属性定义
 * @property {Array} novels - 热门小说列表，包含6本小说
 */
const props = defineProps({
  novels: {
    type: Array,
    default: () => []
  }
})

// 当前显示的小说索引
const currentIndex = ref(0)
// 自动轮播定时器ID
let intervalId = null

/**
 * 计算当前显示的小说对象
 * @returns {Object|null} 当前小说对象，如果没有则返回null
 */
const currentNovel = computed(() => {
  if (!props.novels || props.novels.length === 0) return null
  return props.novels[currentIndex.value]
})

/**
 * 获取上一本小说（未使用，保留备用）
 * @param {number} index - 当前索引
 * @returns {Object|null} 上一本小说对象
 */
const getPrevNovel = (index) => {
  if (!props.novels || props.novels.length === 0) return null
  const prevIndex = index === 0 ? props.novels.length - 1 : index - 1
  return props.novels[prevIndex]
}

/**
 * 获取下一本小说（未使用，保留备用）
 * @param {number} index - 当前索引
 * @returns {Object|null} 下一本小说对象
 */
const getNextNovel = (index) => {
  if (!props.novels || props.novels.length === 0) return null
  const nextIndex = index === props.novels.length - 1 ? 0 : index + 1
  return props.novels[nextIndex]
}

/**
 * 格式化字数显示
 * @param {number} count - 字数
 * @returns {string} 格式化后的字数字符串（如"50.0万字"或"5000字"）
 */
const formatWordCount = (count) => {
  if (!count) return ''
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万字'
  }
  return count + '字'
}

/**
 * 截断标题（超过7字用...补齐）
 * @param {string} title - 小说标题
 * @returns {string} 截断后的标题
 */
const truncateTitle = (title) => {
  if (!title) return ''
  if (title.length > 7) {
    return title.substring(0, 7) + '...'
  }
  return title
}

/**
 * 截断并格式化简介
 * 规则：前两行每行14个字，第三行最多13个字，第三行超过13个字用...补齐
 * @param {string} synopsis - 小说简介
 * @returns {string} 格式化后的简介（包含换行符\n）
 */
const truncateSynopsis = (synopsis) => {
  if (!synopsis) return ''
  const maxLines = 3 // 最多3行
  const charsPerLine1 = 14 // 前两行每行14个字
  const charsPerLine3 = 13 // 第三行最多13个字
  const maxChars = charsPerLine1 * 2 + charsPerLine3 // 最多41个字（14+14+13）
  
  if (synopsis.length <= maxChars) {
    // 如果不超过最大字符数，按规则换行
    let result = ''
    let currentPos = 0
    
    // 第一行：14个字
    if (synopsis.length > currentPos) {
      result += synopsis.substring(currentPos, currentPos + charsPerLine1)
      currentPos += charsPerLine1
    }
    
    // 第二行：14个字
    if (synopsis.length > currentPos) {
      result += '\n' + synopsis.substring(currentPos, currentPos + charsPerLine1)
      currentPos += charsPerLine1
    }
    
    // 第三行：最多13个字
    if (synopsis.length > currentPos) {
      result += '\n' + synopsis.substring(currentPos, currentPos + charsPerLine3)
    }
    
    return result
  } else {
    // 超过最大字符数，按规则截取
    let result = ''
    
    // 第一行：14个字
    result += synopsis.substring(0, charsPerLine1)
    
    // 第二行：14个字
    result += '\n' + synopsis.substring(charsPerLine1, charsPerLine1 * 2)
    
    // 第三行：13个字 + ...
    result += '\n' + synopsis.substring(charsPerLine1 * 2, maxChars) + '...'
    
    return result
  }
}

/**
 * 计算需要显示的封面索引（只显示3张：当前+前一张+后一张）
 * @returns {Array<number>} 需要显示的封面索引数组
 */
const getVisibleIndices = computed(() => {
  if (!props.novels || props.novels.length === 0) return []
  const total = props.novels.length
  const current = currentIndex.value
  const indices = []
  
  // 前一张（循环：如果当前是第一本，则前一张是最后一本）
  const prevIndex = current === 0 ? total - 1 : current - 1
  indices.push(prevIndex)
  
  // 当前
  indices.push(current)
  
  // 后一张（循环：如果当前是最后一本，则后一张是第一本）
  const nextIndex = current === total - 1 ? 0 : current + 1
  indices.push(nextIndex)
  
  return indices
})

/**
 * 计算封面的样式（位置、大小、层级）
 * 实现堆叠效果：当前封面最大居中，其他封面较小且部分遮挡
 * @param {number} index - 封面索引
 * @returns {Object} 样式对象（left、transform、zIndex）
 */
const getCoverStyle = (index) => {
  const diff = index - currentIndex.value
  const absDiff = Math.abs(diff)
  
  // 处理循环差异（考虑数组边界，确保最短路径）
  let normalizedDiff = diff
  if (absDiff > props.novels.length / 2) {
    normalizedDiff = diff > 0 ? diff - props.novels.length : diff + props.novels.length
  }
  
  // 当前展示的小说，居中，最大，层级最高
  if (normalizedDiff === 0) {
    return {
      left: '50%',
      transform: 'translate(-50%, -50%)',
      zIndex: 10
    }
  }
  
  // 其他小说的位置计算
  // 当前小说居中，宽度110px，高度147px
  // 非激活小说宽度55px，高度73px，只显示一半
  // 左侧小说：应该显示右半部分，所以向右偏移35px (55/2)
  // 右侧小说：应该显示左半部分，所以向左偏移35px
  const baseOffset = normalizedDiff * 70 // 基础间距70px
  const halfOffset = normalizedDiff < 0 ? 35 : -35 // 半宽偏移量，实现部分遮挡效果
  
  return {
    left: `calc(50% + ${baseOffset + halfOffset}px)`,
    transform: 'translate(-50%, -50%)',
    zIndex: 10 - Math.abs(normalizedDiff) // 层级递减，后面的封面层级更低
  }
}

/**
 * 切换到下一本小说（自动轮播使用）
 */
const nextSlide = () => {
  currentIndex.value = (currentIndex.value + 1) % props.novels.length
}

/**
 * 组件挂载时启动自动轮播
 * 如果有多本小说，每2秒自动切换一次
 */
onMounted(() => {
  if (props.novels && props.novels.length > 1) {
    intervalId = setInterval(nextSlide, 2000) // 每2秒切换
  }
})

/**
 * 组件卸载时清除定时器，防止内存泄漏
 */
onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId)
  }
})
</script>

<style scoped>
/* 热门小说轮播容器 */
.hot-novel-carousel {
  background: #f5f5f5; /* 淡灰色背景 */
  border-radius: 0;
  width: 100%;
  box-shadow: none;
  overflow: hidden;
  height: fit-content;
}

/* 轮播容器 */
.carousel-container {
  width: 100%;
  position: relative;
}

/* 小说展示区域 */
.novel-display {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 20px;
  align-items: center;
}

/* 封面堆叠容器：用于实现3张封面的堆叠效果 */
.cover-stack {
  position: relative;
  width: 100%;
  max-width: 350px;
  height: 140px; /* 固定高度，确保封面堆叠效果 */
  flex-shrink: 0;
  overflow: visible; /* 允许封面超出容器显示 */
  margin: 0 auto;
}

/* 单个封面项 */
.cover-item {
  position: absolute;
  transition: all 0.5s ease; /* 平滑过渡动画 */
  cursor: pointer;
  top: 50%;
}

/* 非激活状态的封面：较小且半透明 */
.cover-item:not(.cover-active) {
  width: 55px;
  height: 73px;
  opacity: 0.85;
  transform: translate(-50%, -50%);
  filter: brightness(0.9); /* 稍微变暗 */
}

/* 激活状态的封面：最大且完全不透明 */
.cover-item.cover-active {
  width: 110px;
  height: 147px;
  opacity: 1;
  transform: translate(-50%, -50%);
}

/* 封面图片样式 */
.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保持比例填充 */
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0,0,0,.15); /* 阴影效果 */
}

/* 小说信息区域 */
.novel-info {
  width: 100%;
  text-align: center;
}

/* 小说信息内容容器 */
.novel-info-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 小说标题样式 */
.novel-title {
  font-size: 1.05rem;
  margin-bottom: 6px;
  color: var(--text);
  font-weight: 600;
}

/* 作者信息样式 */
.novel-author {
  color: var(--text-muted);
  font-size: 0.9rem;
  margin-bottom: 6px;
}

/* 出版社信息样式 */
.novel-publisher {
  color: var(--text-muted);
  font-size: 0.85rem;
  margin-bottom: 12px;
}

/* 字数样式：淡灰色，与出版社信息一致 */
.word-count {
  color: #999;
  font-weight: normal;
}

/* 简介样式：支持换行，最多3行 */
.novel-synopsis {
  font-size: 0.8rem;
  color: #666;
  line-height: 1.4;
  margin-bottom: 12px;
  text-align: left;
  width: 100%;
  white-space: pre-line; /* 保留换行符\n */
  max-height: 4.2em; /* 最多3行，每行1.4em */
  overflow: hidden;
}

/* 立即阅读按钮 */
.btn-read {
  display: inline-block;
  padding: 8px 20px;
  background: var(--primary);
  color: #fff;
  border-radius: 4px;
  font-weight: 500;
  text-decoration: none;
  transition: background 0.2s;
  font-size: 0.9rem;
}

/* 按钮悬停效果 */
.btn-read:hover {
  background: var(--primary-dark);
}

/* 轮播指示器容器：显示在"立即阅读"按钮下方 */
.carousel-indicators {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding: 12px 0 0 0;
  background: transparent; /* 透明背景 */
  margin-top: 8px;
}

/* 单个指示器：小圆点 */
.indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ccc;
  cursor: pointer;
  transition: background 0.2s;
}

/* 激活状态的指示器：变长变宽，显示主题色 */
.indicator.active {
  background: var(--primary);
  width: 24px;
  border-radius: 4px;
}

/* 指示器悬停效果 */
.indicator:hover {
  background: var(--primary);
}

/* 淡入淡出过渡动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
