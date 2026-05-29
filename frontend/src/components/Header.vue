<template>
  <header class="header">
    <div class="header-inner">
      <router-link to="/" class="logo">哔哩轻小说</router-link>
      <!-- 登录/注册页不显示导航与搜索 -->
      <template v-if="!isAuthPage">
        <nav class="nav">
          <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">首页</router-link>
          <router-link to="/browse" class="nav-item" :class="{ active: $route.path === '/browse' }">分类</router-link>
          <div class="nav-item dropdown dropdown-wrap" @mouseenter="showLibrary = true" @mouseleave="showLibrary = false">
            <span>文库</span>
            <ul v-show="showLibrary" class="dropdown-menu">
              <li v-for="p in publishers" :key="'l-' + p.id"><router-link :to="{ path: '/library', query: { publisher: p.id } }" class="dropdown-link">{{ p.name }}</router-link></li>
            </ul>
          </div>
          <router-link to="/ranking" class="nav-item" :class="{ active: $route.path === '/ranking' }">排行</router-link>
          <router-link to="/fullbook" class="nav-item" :class="{ active: $route.path === '/fullbook' }">全本</router-link>
        </nav>
        <div class="search-box">
          <input v-model="keyword" type="text" placeholder="请输入关键词搜索" @keyup.enter="search" />
          <button class="btn-search" @click="search">🔍</button>
        </div>
      </template>
      <div class="user-actions">
        <template v-if="isLoggedIn">
          <router-link to="/bookshelf" class="btn-bookshelf" :class="{ active: $route.path === '/bookshelf' }" title="我的书架">
            <img src="/img/我的书架.jpg" alt="我的书架" class="bookshelf-icon" />
            <span>我的书架</span>
          </router-link>
          <router-link to="/profile" class="profile-link" title="个人主页">
            <img
              v-if="avatarUrl"
              :src="avatarUrl"
              alt="头像"
              class="avatar-img"
            />
            <span v-else class="avatar-placeholder">{{ displayName.charAt(0) }}</span>
          </router-link>
        </template>
        <template v-else>
          <template v-if="isAuthPage && $route.path === '/login'">
            <span class="no-account">没有账号</span>
            <router-link to="/register" class="btn-register">注册</router-link>
          </template>
          <template v-else-if="isAuthPage && $route.path === '/register'">
            <span class="no-account">已有账号</span>
            <router-link to="/login" class="btn-login">登录</router-link>
          </template>
          <template v-else>
            <router-link to="/login" class="btn-login">登录</router-link>
            <router-link to="/register" class="btn-register">注册</router-link>
          </template>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { publisherApi } from '../api'
import { authUpdated, notifyAuthUpdate } from '../store/auth'

const route = useRoute()
const router = useRouter()
const showLibrary = ref(false)
const publishers = ref([])
const keyword = ref('')

const isAuthPage = computed(() => {
  const p = route.path
  return p === '/login' || p === '/register'
})
// 依赖 authUpdated，登录/注册/登出后立即重新计算，从而立即显示浏览历史与头像
const isLoggedIn = computed(() => {
  authUpdated.value
  return !!localStorage.getItem('token')
})
const displayName = computed(() => {
  authUpdated.value
  return localStorage.getItem('nickname') || localStorage.getItem('username') || '用户'
})
const avatarUrl = computed(() => {
  authUpdated.value
  return localStorage.getItem('avatar_url') || ''
})

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('nickname')
  localStorage.removeItem('userId')
  localStorage.removeItem('avatar_url')
  sessionStorage.removeItem('token')
  notifyAuthUpdate()
  router.push('/')
}

watch(() => route.query.keyword, (q) => {
  if (q != null && route.path === '/search') keyword.value = q
}, { immediate: true })

// 在搜索模块中点击其他模块时清空搜索框
watch(() => route.path, (newPath, oldPath) => {
  if (oldPath === '/search' && newPath !== '/search') {
    keyword.value = ''
  }
})

onMounted(async () => {
  try {
    publishers.value = await publisherApi.list()
  } catch (e) {
    publishers.value = [
      { id: 1, name: '电击文库' }, { id: 2, name: '富士见文库' }, { id: 3, name: '角川文库' },
      { id: 4, name: 'MF文库J' }, { id: 5, name: 'Fami通文库' }, { id: 6, name: 'GA文库' },
      { id: 7, name: 'HJ文库' }, { id: 8, name: '一迅社' }, { id: 9, name: '集英社' },
      { id: 10, name: '小学馆' }, { id: 11, name: '讲谈社' }, { id: 12, name: '少女文库' },
      { id: 13, name: '其他文库' }, { id: 14, name: '华文轻小说' }
    ]
  }
})

function search() {
  const k = keyword.value.trim()
  if (k) {
    // 添加 _r 时间戳，使同一关键词多次搜索能触发刷新
    router.push({ path: '/search', query: { keyword: k, _r: Date.now() } })
  }
}
</script>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--header-height);
  background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,.08);
  z-index: 1000;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 24px;
}

.logo {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--primary);
  flex-shrink: 0;
}

.nav {
  display: flex;
  gap: 8px;
  position: relative;
}

.nav-item {
  padding: 8px 12px;
  color: var(--text);
  font-size: 0.95rem;
}

.nav-item:hover, .nav-item.active { color: var(--primary); }

.dropdown {
  position: relative;
}

/* 用 padding 填满触发区与菜单之间的空隙，鼠标移入时不会触发 mouseleave */
.dropdown-wrap {
  padding-bottom: 4px;
}

.dropdown-wrap .dropdown-menu {
  top: 100%;
}

.dropdown-menu {
  position: absolute;
  left: 0;
  background: #2c2c2c;
  color: #fff;
  min-width: 140px;
  padding: 8px 0;
  border-radius: 4px;
  margin-top: 0;
  box-shadow: 0 4px 12px rgba(0,0,0,.2);
}

.dropdown-menu a,
.dropdown-menu .dropdown-link {
  display: block;
  padding: 8px 16px;
  color: #fff;
  font-size: 0.9rem;
  text-decoration: none;
}

.dropdown-menu a:hover,
.dropdown-menu .dropdown-link:hover {
  background: rgba(255,255,255,.1);
  color: var(--primary);
}

.search-box {
  flex: 1;
  max-width: 320px;
  display: flex;
  border: 1px solid var(--border);
  border-radius: 20px;
  overflow: hidden;
}

.search-box input {
  flex: 1;
  padding: 8px 16px;
  border: none;
  outline: none;
  font-size: 0.9rem;
}

.btn-search {
  padding: 8px 16px;
  background: var(--primary);
  color: #fff;
  border: none;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-left: auto;
}

.no-account {
  font-size: 0.9rem;
  color: #666;
}

.username {
  font-size: 0.9rem;
  color: var(--text);
}

.btn-register {
  padding: 6px 16px;
  background: transparent;
  color: var(--primary) !important;
  border: 1px solid var(--primary);
  border-radius: 4px;
  font-size: 0.9rem;
  text-decoration: none;
}

.btn-register:hover {
  background: rgba(251, 114, 153, 0.1);
  color: var(--primary) !important;
}

.btn-login {
  padding: 6px 16px;
  background: transparent;
  color: var(--primary) !important;
  border: 1px solid var(--primary);
  border-radius: 4px;
  font-size: 0.9rem;
  text-decoration: none;
}

.btn-login:hover {
  background: rgba(251, 114, 153, 0.1);
  color: var(--primary) !important;
}

.link {
  font-size: 0.9rem;
  color: var(--text);
  text-decoration: none;
}

.link:hover { color: var(--primary); }

/* 登录后：我的书架按钮 */
.btn-bookshelf {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  font-size: 0.9rem;
  color: var(--text);
  text-decoration: none;
  border-radius: 4px;
}

.btn-bookshelf:hover,
.btn-bookshelf.active {
  color: var(--primary);
}

.bookshelf-icon {
  width: 24px;
  height: 24px;
  object-fit: cover;
  border-radius: 4px;
}

/* 登录后：个人主页头像（圆形） */
.profile-link {
  display: flex;
  align-items: center;
  text-decoration: none;
}

.avatar-img,
.avatar-placeholder {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

.avatar-placeholder {
  background: var(--primary);
  color: #fff;
  font-size: 1.1rem;
  font-weight: 600;
  line-height: 44px;
  text-align: center;
}
</style>
