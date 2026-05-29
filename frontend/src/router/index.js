/**
 * Vue Router 配置
 * 实现 SPA 路由与页面切换
 */
import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Profile from '../views/Profile.vue'
import Bookshelf from '../views/Bookshelf.vue'
import Library from '../views/Library.vue'
import Browse from '../views/Browse.vue'
import Ranking from '../views/Ranking.vue'
import NovelDetail from '../views/NovelDetail.vue'
import NovelRead from '../views/NovelRead.vue'
import FullBook from '../views/FullBook.vue'
import Search from '../views/Search.vue'
import AdminLogin from '../views/admin/AdminLogin.vue'
import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import AdminUsers from '../views/admin/AdminUsers.vue'
import AdminNovels from '../views/admin/AdminNovels.vue'
import AdminNovelShelf from '../views/admin/AdminNovelShelf.vue'
import AdminCategorySystem from '../views/admin/AdminCategorySystem.vue'
import AdminTags from '../views/admin/AdminTags.vue'
import AdminComments from '../views/admin/AdminComments.vue'
import AdminStatistics from '../views/admin/AdminStatistics.vue'
import AdminSettings from '../views/admin/AdminSettings.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/profile', name: 'Profile', component: Profile },
  { path: '/bookshelf', name: 'Bookshelf', component: Bookshelf },
  { path: '/library', name: 'Library', component: Library },
  { path: '/browse', name: 'Browse', component: Browse },
  { path: '/ranking', name: 'Ranking', component: Ranking },
  { path: '/fullbook', name: 'FullBook', component: FullBook },
  { path: '/search', name: 'Search', component: Search },
  { path: '/novel/:id', name: 'NovelDetail', component: NovelDetail },
  { path: '/novel/:id/read/:chapterId', name: 'NovelRead', component: NovelRead },
  { path: '/admin/login', name: 'AdminLogin', component: AdminLogin, meta: { adminAuth: false } },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { adminAuth: true },
    children: [
      { path: '', name: 'AdminDashboard', component: AdminDashboard },
      { path: 'users', name: 'AdminUsers', component: AdminUsers },
      { path: 'novels', redirect: { name: 'AdminNovelsManage' } },
      { path: 'novels/shelf', name: 'AdminNovelShelf', component: AdminNovelShelf },
      { path: 'novels/manage', name: 'AdminNovelsManage', component: AdminNovels },
      { path: 'categories', redirect: { name: 'AdminCategorySystem' } },
      { path: 'categories/system', name: 'AdminCategorySystem', component: AdminCategorySystem },
      { path: 'categories/tags', name: 'AdminTags', component: AdminTags },
      { path: 'comments', name: 'AdminComments', component: AdminComments },
      { path: 'statistics', name: 'AdminStatistics', component: AdminStatistics },
      { path: 'settings', name: 'AdminSettings', component: AdminSettings }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const adminToken = localStorage.getItem('adminToken')
  if (to.meta.adminAuth === true && !adminToken) {
    next({ path: '/admin/login' })
    return
  }
  if (to.path === '/admin/login' && adminToken) {
    next({ path: '/admin' })
    return
  }
  next()
})

export default router
