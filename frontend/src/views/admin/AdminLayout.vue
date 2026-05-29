<template>
  <div class="admin-layout">
    <header class="admin-header">
      <h1 class="admin-title">轻小说阅读系统 - 管理后台</h1>
      <div class="admin-header-right">
        <span class="admin-id">管理员 #{{ adminId }}</span>
        <button type="button" class="btn-logout" @click="handleLogout">退出</button>
      </div>
    </header>
    <div class="admin-body">
      <aside class="admin-sidebar">
        <nav class="admin-nav">
          <router-link to="/admin" class="nav-item" active-class="active" exact-active-class="active">首页概览</router-link>
          <router-link to="/admin/users" class="nav-item" active-class="active">用户管理</router-link>
          <div class="nav-group" :class="{ expanded: isNovelExpanded }">
            <button type="button" class="nav-group-title" @click="isNovelExpanded = !isNovelExpanded" :aria-expanded="isNovelExpanded">
              小说管理
            </button>
            <div class="nav-group-items">
              <router-link to="/admin/novels/shelf" class="nav-item nav-sub" active-class="active">小说上架</router-link>
              <router-link to="/admin/novels/manage" class="nav-item nav-sub" active-class="active">小说管理</router-link>
            </div>
          </div>
          <div class="nav-group" :class="{ expanded: isCategoryExpanded }">
            <button type="button" class="nav-group-title" @click="isCategoryExpanded = !isCategoryExpanded" :aria-expanded="isCategoryExpanded">
              分类管理
            </button>
            <div class="nav-group-items">
              <router-link to="/admin/categories/system" class="nav-item nav-sub" active-class="active">小说分类体系</router-link>
              <router-link to="/admin/categories/tags" class="nav-item nav-sub" active-class="active">小说标签增删</router-link>
            </div>
          </div>
          <router-link to="/admin/comments" class="nav-item" active-class="active">评论管理</router-link>
          <router-link to="/admin/statistics" class="nav-item" active-class="active">数据统计</router-link>
          <router-link to="/admin/settings" class="nav-item" active-class="active">系统设置</router-link>
        </nav>
      </aside>
      <main class="admin-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const adminId = computed(() => localStorage.getItem('adminId') || '')
const isNovelExpanded = ref(route.path.startsWith('/admin/novels'))
const isCategoryExpanded = ref(route.path.startsWith('/admin/categories'))
watch(() => route.path, (path) => {
  if (path.startsWith('/admin/novels')) isNovelExpanded.value = true
  if (path.startsWith('/admin/categories')) isCategoryExpanded.value = true
})

function handleLogout() {
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminId')
  router.replace('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f6fa;
  overflow: hidden;
}

.admin-header {
  height: 56px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #2c3e50;
  color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.admin-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0;
}

.admin-header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-id {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.85);
}

.btn-logout {
  padding: 6px 14px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: #fff;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
}

.btn-logout:hover {
  background: rgba(255, 255, 255, 0.1);
}

.admin-body {
  flex: 1;
  display: flex;
  min-height: 0;
  height: calc(100vh - 56px);
  overflow: hidden;
}

.admin-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  overflow-y: auto;
  position: sticky;
  top: 0;
  align-self: flex-start;
}

.admin-nav {
  padding: 16px 0;
}

.nav-item {
  display: block;
  padding: 12px 24px;
  color: #333;
  text-decoration: none;
  font-size: 0.95rem;
  transition: background 0.2s;
}

.nav-item:hover {
  background: #f0f0f0;
}

.nav-item.active {
  background: #e8f4fc;
  color: #2c3e50;
  font-weight: 500;
  border-right: 3px solid #2c3e50;
}

.nav-group {
  border-bottom: 1px solid #f0f0f0;
}
.nav-group-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 12px 24px;
  color: #333;
  font-size: 0.95rem;
  background: none;
  border: none;
  cursor: pointer;
  text-align: left;
  transition: background 0.2s;
}
.nav-group-title:hover {
  background: #f0f0f0;
}
.nav-group-items {
  overflow: hidden;
  max-height: 0;
  transition: max-height 0.2s ease-out;
}
.nav-group.expanded .nav-group-items {
  max-height: 120px;
}
.nav-item.nav-sub {
  padding-left: 40px;
  font-size: 0.9rem;
  color: #555;
}
.nav-item.nav-sub.active {
  background: #e8f4fc;
  color: #2c3e50;
  font-weight: 500;
  border-right: 3px solid #2c3e50;
}

.admin-main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  overflow-x: hidden;
  background: #f5f6fa;
  position: relative;
  min-height: 0;
}
</style>
