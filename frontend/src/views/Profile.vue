<!--
  个人中心页面
  左侧导航 20%，右侧内容 80%
  账户信息、阅读历史（占位）、收藏列表、设置个性、退出登录
-->
<template>
  <div class="profile-page">
    <div v-if="!isLoggedIn" class="need-login">
      <p>请先登录后查看个人中心</p>
      <router-link to="/login" class="btn-login">去登录</router-link>
    </div>
    <div v-else class="profile-layout">
      <!-- 左侧导航 20% -->
      <aside class="profile-nav">
        <nav class="nav-list">
          <button
            v-for="item in navItems"
            :key="item.key"
            type="button"
            class="nav-item"
            :class="{ active: activeNav === item.key && item.key !== 'logout' }"
            @click="onNavClick(item)"
          >
            <img :src="item.iconSrc" :alt="item.label" class="nav-icon-img" @error="onIconError" />
            <span class="nav-text">{{ item.label }}</span>
          </button>
        </nav>
      </aside>

      <!-- 右侧内容 80% -->
      <main class="profile-content">
        <!-- 账户信息 -->
        <section v-show="activeNav === 'account'" class="panel account-panel">
          <h2 class="panel-title">账户信息</h2>
          <div v-if="profileLoading" class="loading">加载中...</div>
          <form v-else class="account-form" @submit.prevent>
            <div class="form-row avatar-row">
              <div class="avatar-wrap">
                <img v-if="profile.avatarUrl" :src="profile.avatarUrl" alt="头像" class="avatar-img" />
                <span v-else class="avatar-placeholder">{{ (profile.nickname || profile.username || '用').charAt(0) }}</span>
              </div>
              <div class="user-basic">
                <p class="uid">UID: {{ displayUid }}</p>
              </div>
            </div>
            <div class="form-group">
              <label>用户名</label>
              <input v-model="profileForm.username" type="text" required placeholder="用于登录" />
            </div>
            <div class="form-group">
              <label>昵称</label>
              <input v-model="profileForm.nickname" type="text" placeholder="显示名称" />
            </div>
            <div class="form-group">
              <label>邮箱</label>
              <input v-model="profileForm.email" type="email" placeholder="选填" />
            </div>
            <div class="form-group">
              <label>新密码</label>
              <input v-model="profileForm.newPassword" type="password" placeholder="不修改请留空" />
            </div>
            <button type="button" class="btn-modify" :class="{ disabled: isModifyDisabled }" :disabled="isModifyDisabled" @click="onModifyClick">修改</button>
          </form>
        </section>

        <!-- 阅读历史 -->
        <section v-show="activeNav === 'history'" class="panel history-panel">
          <h2 class="panel-title">阅读历史</h2>
          <div v-if="historyLoading" class="loading">加载中...</div>
          <div v-else-if="!historyList.length" class="empty-tip">暂无阅读记录</div>
          <div v-else class="history-grid">
            <div v-for="item in historyList" :key="item.novelId" class="history-card">
              <router-link :to="getHistoryReadLink(item)" class="history-link">
                <div class="cover-wrap">
                  <img :src="item.coverUrl || 'https://via.placeholder.com/100x133/f0f0f0/999?text=无封面'" :alt="item.title" />
                </div>
                <h3 class="book-title">{{ item.title }}</h3>
                <p class="history-chapter"><span class="history-label">当前阅读到：</span><span class="chapter-name">{{ item.chapterTitle || '未知章节' }}</span></p>
              </router-link>
            </div>
          </div>
        </section>

        <!-- 收藏列表 -->
        <section v-show="activeNav === 'bookshelf'" class="panel bookshelf-panel">
          <h2 class="panel-title">收藏列表</h2>
          <div v-if="bookshelfLoading" class="loading">加载中...</div>
          <div v-else-if="!bookshelf.length" class="empty-tip">暂无收藏，快去添加吧~</div>
          <div v-else class="bookshelf-grid">
            <div v-for="n in bookshelf" :key="n.id" class="book-card">
              <router-link :to="`/novel/${n.id}`" class="book-link">
                <div class="cover-wrap">
                  <img :src="n.coverUrl || 'https://via.placeholder.com/100x133/f0f0f0/999?text=无封面'" :alt="n.title" />
                </div>
                <h3 class="book-title">{{ n.title }}</h3>
              </router-link>
              <button type="button" class="btn-remove" title="移出收藏" @click.stop="removeFromBookshelf(n)">×</button>
            </div>
          </div>
        </section>

        <!-- 设置个性（阅读偏好） -->
        <section v-show="activeNav === 'settings'" class="panel settings-panel">
          <h2 class="panel-title">个性化阅读偏好</h2>
          <div v-if="prefsLoading" class="loading">加载中...</div>
          <form v-else class="settings-form" @submit.prevent="savePreferences">
            <div class="form-group">
              <label>字体大小</label>
              <select v-model.number="prefsForm.fontSize">
                <option :value="14">14px</option>
                <option :value="16">16px</option>
                <option :value="18">18px</option>
                <option :value="20">20px</option>
                <option :value="22">22px</option>
              </select>
            </div>
            <div class="form-group">
              <label>行高</label>
              <select v-model.number="prefsForm.lineHeight">
                <option :value="1.5">1.5</option>
                <option :value="1.8">1.8</option>
                <option :value="2.0">2.0</option>
                <option :value="2.2">2.2</option>
              </select>
            </div>
            <div class="form-group">
              <label>阅读主题</label>
              <select v-model="prefsForm.theme">
                <option value="default">默认</option>
                <option value="sepia">护眼</option>
                <option value="night">夜间</option>
              </select>
            </div>
            <div class="form-group">
              <label>阅读区域宽度 (px)</label>
              <input v-model.number="prefsForm.pageWidth" type="number" min="0" max="1200" placeholder="0 为自适应" />
            </div>
            <button type="submit" class="btn-save">保存</button>
          </form>
        </section>

      </main>
    </div>

    <!-- 退出登录确认弹窗 -->
    <Transition name="modal">
      <div v-if="showLogoutModal" class="modal-overlay" @click.self="showLogoutModal = false">
        <div class="modal-box modal-styled">
          <p>确定要退出登录吗？</p>
          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="showLogoutModal = false">取消</button>
            <button type="button" class="btn-confirm btn-confirm-pink" @click="doLogout">确认</button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 账户信息修改确认弹窗 -->
    <Transition name="modal">
      <div v-if="showAccountModifyModal" class="modal-overlay" @click.self="showAccountModifyModal = false">
        <div class="modal-box modal-styled">
          <p>是否进行修改？</p>
          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="showAccountModifyModal = false">取消</button>
            <button type="button" class="btn-confirm btn-confirm-pink" @click="confirmAccountModify">确认</button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 移除收藏确认弹窗 -->
    <Transition name="modal">
      <div v-if="removeConfirmNovel" class="modal-overlay" @click.self="removeConfirmNovel = null">
        <div class="modal-box modal-styled">
          <p>确定从收藏中移除《{{ removeConfirmNovel?.title }}》吗？</p>
          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="removeConfirmNovel = null">取消</button>
            <button type="button" class="btn-confirm btn-confirm-pink" @click="confirmRemoveFromBookshelf">确认删除</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi, bookshelfApi, readingProgressApi } from '../api'
import { authUpdated, notifyAuthUpdate } from '../store/auth'

const router = useRouter()
const activeNav = ref('account')
const showLogoutModal = ref(false)
const showAccountModifyModal = ref(false)
const removeConfirmNovel = ref(null)

const navItems = [
  { key: 'account', label: '账户信息', iconSrc: '/img/账户信息.jpg' },
  { key: 'history', label: '阅读历史', iconSrc: '/img/阅读历史.jpg' },
  { key: 'bookshelf', label: '收藏列表', iconSrc: '/img/收藏列表.jpg' },
  { key: 'settings', label: '设置个性', iconSrc: '/img/设置个性.jpg' },
  { key: 'logout', label: '退出登录', iconSrc: '/img/退出登录.jpg' }
]

const isLoggedIn = computed(() => {
  authUpdated.value
  return !!localStorage.getItem('token')
})

/** UID 显示为 10000 + user.id */
const displayUid = computed(() => 10000 + (profile.value?.userId || 0))

/** 密码为空且其他字段与数据库相同时，修改按钮禁用 */
const isModifyDisabled = computed(() => {
  const p = profile.value
  const f = profileForm.value
  if (!p || profileLoading.value) return true
  const pwdEmpty = !f.newPassword || !String(f.newPassword).trim()
  const sameUsername = (f.username || '').trim() === (p.username || '')
  const sameNickname = (f.nickname || '').trim() === (p.nickname || '')
  const sameEmail = (f.email || '').trim() === (p.email || '')
  return pwdEmpty && sameUsername && sameNickname && sameEmail
})

// 账户信息
const profile = ref({})
const profileForm = ref({ username: '', nickname: '', email: '', newPassword: '' })
const profileLoading = ref(false)

// 收藏列表
const bookshelf = ref([])
const bookshelfLoading = ref(false)

// 阅读历史
const historyList = ref([])
const historyLoading = ref(false)

// 阅读偏好
const prefsForm = ref({ fontSize: 16, lineHeight: 1.8, theme: 'default', pageWidth: 720 })
const prefsLoading = ref(false)

async function loadProfile() {
  profileLoading.value = true
  try {
    profile.value = await userApi.getProfile()
    profileForm.value = {
      username: profile.value.username || '',
      nickname: profile.value.nickname || '',
      email: profile.value.email || '',
      newPassword: ''
    }
  } catch (e) {
    if (e?.statusCode === 401) router.push({ path: '/login', query: { redirect: '/profile' } })
  } finally {
    profileLoading.value = false
  }
}

function onModifyClick() {
  if (isModifyDisabled.value) return
  showAccountModifyModal.value = true
}

async function confirmAccountModify() {
  showAccountModifyModal.value = false
  try {
    await userApi.updateProfile({
      username: profileForm.value.username,
      nickname: profileForm.value.nickname,
      email: profileForm.value.email,
      newPassword: profileForm.value.newPassword || undefined
    })
    doLogout()
  } catch (e) {
    alert(e?.message || '修改失败')
  }
}

async function loadBookshelf() {
  bookshelfLoading.value = true
  try {
    bookshelf.value = await bookshelfApi.list() || []
  } catch (e) {
    if (e?.statusCode === 401) router.push({ path: '/login', query: { redirect: '/profile' } })
    bookshelf.value = []
  } finally {
    bookshelfLoading.value = false
  }
}

function getHistoryReadLink(item) {
  if (!item?.novelId || !item?.chapterId) return { path: '/novel/' + (item?.novelId || '') }
  const q = item.bookmark != null ? { bookmark: item.bookmark } : {}
  return { path: `/novel/${item.novelId}/read/${item.chapterId}`, query: q }
}

async function loadHistory() {
  historyLoading.value = true
  try {
    historyList.value = await readingProgressApi.listAll() || []
  } catch (e) {
    if (e?.statusCode === 401) router.push({ path: '/login', query: { redirect: '/profile' } })
    historyList.value = []
  } finally {
    historyLoading.value = false
  }
}

function removeFromBookshelf(novel) {
  removeConfirmNovel.value = novel
}

async function confirmRemoveFromBookshelf() {
  const novel = removeConfirmNovel.value
  if (!novel) return
  removeConfirmNovel.value = null
  try {
    await bookshelfApi.remove(novel.id)
    bookshelf.value = bookshelf.value.filter(x => x.id !== novel.id)
  } catch (e) {
    alert('移除失败')
  }
}

async function loadPreferences() {
  prefsLoading.value = true
  try {
    const p = await userApi.getPreferences()
    prefsForm.value = {
      fontSize: p.fontSize ?? 16,
      lineHeight: p.lineHeight ?? 1.8,
      theme: p.theme ?? 'default',
      pageWidth: p.pageWidth ?? 720
    }
  } catch (e) {
    if (e?.statusCode === 401) router.push({ path: '/login', query: { redirect: '/profile' } })
  } finally {
    prefsLoading.value = false
  }
}

async function savePreferences() {
  try {
    await userApi.updatePreferences(prefsForm.value)
    alert('保存成功')
  } catch (e) {
    alert('保存失败')
  }
}

function onNavClick(item) {
  if (item.key === 'logout') {
    confirmLogout()
  } else {
    activeNav.value = item.key
  }
}

function onIconError(e) {
  e.target.style.display = 'none'
}

function confirmLogout() {
  showLogoutModal.value = true
}

function doLogout() {
  showLogoutModal.value = false
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('nickname')
  localStorage.removeItem('userId')
  localStorage.removeItem('avatar_url')
  sessionStorage.removeItem('token')
  notifyAuthUpdate()
  router.push('/')
}

watch(activeNav, (key) => {
  if (key === 'account') loadProfile()
  else if (key === 'bookshelf') loadBookshelf()
  else if (key === 'history') loadHistory()
  else if (key === 'settings') loadPreferences()
})

onMounted(() => {
  if (!isLoggedIn.value) return
  if (activeNav.value === 'account') loadProfile()
  else if (activeNav.value === 'bookshelf') loadBookshelf()
  else if (activeNav.value === 'history') loadHistory()
  else if (activeNav.value === 'settings') loadPreferences()
})
</script>

<style scoped>
.profile-page {
  min-height: calc(100vh - var(--header-height));
  padding-top: 24px;
  background: var(--bg, #f5f5f5);
}

.profile-layout {
  margin-top: 0;
  padding-top: 0;
}

.need-login {
  max-width: 400px;
  margin: 80px auto;
  padding: 48px 24px;
  text-align: center;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}

.need-login p { color: var(--text-muted); margin-bottom: 20px; }
.btn-login {
  display: inline-block;
  padding: 10px 24px;
  background: var(--primary);
  color: #fff;
  border-radius: 6px;
  text-decoration: none;
}

.profile-layout {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - var(--header-height));
}

.profile-nav {
  width: 20%;
  min-width: 180px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
  padding: 15px 0 20px 0;
}

.nav-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 12px 20px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 0.95rem;
  color: var(--text);
  text-align: left;
}

.nav-item:hover {
  background: rgba(251, 114, 153, 0.08);
  color: var(--primary);
}

.nav-item.active {
  background: rgba(251, 114, 153, 0.12);
  color: var(--primary);
  font-weight: 600;
}

.nav-icon-img {
  width: 24px;
  height: 24px;
  object-fit: contain;
  flex-shrink: 0;
}

.profile-content {
  flex: 1;
  padding: 15px 32px 24px;
  background: #fff;
  margin: 0 0 0 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
  border-radius: 4px;
}

.panel-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border, #eee);
}

.loading, .empty-tip, .placeholder-tip {
  color: var(--text-muted);
  padding: 48px 0;
  text-align: center;
}

/* 账户信息 */
.account-form .avatar-row {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.avatar-wrap {
  flex-shrink: 0;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  background: #f0f0f0;
}

.avatar-img, .avatar-placeholder {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-placeholder {
  background: var(--primary);
  color: #fff;
  font-size: 1.5rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-basic .uid { font-size: 0.9rem; color: var(--text-muted); margin: 0; }

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 0.9rem;
  color: var(--text);
  margin-bottom: 8px;
}

.form-group input,
.form-group select {
  width: 100%;
  max-width: 360px;
  padding: 10px 12px;
  border: 1px solid var(--border, #ddd);
  border-radius: 6px;
  font-size: 0.95rem;
}

.btn-save,
.btn-modify {
  padding: 10px 24px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
}

.btn-save:hover,
.btn-modify:hover { opacity: 0.9; }

.btn-modify:disabled,
.btn-modify.disabled {
  background: #ccc;
  color: #999;
  cursor: not-allowed;
  opacity: 1;
}

.btn-modify:disabled:hover,
.btn-modify.disabled:hover { opacity: 1; }

/* 收藏列表：每行4本，每本约 20% 宽（参考第二张图：图片上、书名下） */
.bookshelf-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.book-card {
  position: relative;
  flex: 0 0 calc(25% - 12px);
  min-width: 100px;
}

.book-link {
  display: block;
  text-decoration: none;
  color: inherit;
}

.cover-wrap {
  aspect-ratio: 3/4;
  border-radius: 8px;
  overflow: hidden;
  background: #f0f0f0;
}

.cover-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-title {
  font-size: 0.9rem;
  font-weight: 600;
  margin: 10px 0 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.book-link:hover .book-title { color: var(--primary); }

.btn-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: rgba(0,0,0,.5);
  color: #fff;
  font-size: 1.2rem;
  line-height: 1;
  cursor: pointer;
}

.btn-remove:hover {
  background: rgba(200,0,0,.8);
}

/* 阅读历史：与收藏列表同款网格与卡片，无删除按钮，书名下增加一行「当前阅读到：章名」且章名过长省略 */
.history-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.history-card {
  position: relative;
  flex: 0 0 calc(25% - 12px);
  min-width: 100px;
}

.history-link {
  display: block;
  text-decoration: none;
  color: inherit;
}

.history-card .cover-wrap {
  aspect-ratio: 3/4;
  border-radius: 8px;
  overflow: hidden;
  background: #f0f0f0;
}

.history-card .cover-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.history-card .book-title {
  font-size: 0.9rem;
  font-weight: 600;
  margin: 10px 0 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.history-chapter {
  font-size: 0.8rem;
  color: var(--text-muted);
  margin: 4px 0 0;
  line-height: 1.3;
  display: flex;
  max-width: 100%;
  overflow: hidden;
}

.history-chapter .history-label {
  flex-shrink: 0;
}

.history-chapter .chapter-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-link:hover .book-title { color: var(--primary); }

/* 弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-box {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  min-width: 280px;
}

.modal-styled {
  background: #f0f0f0;
  border: 2px solid var(--primary);
}

.modal-box p { margin: 0 0 20px; font-size: 1rem; }

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-cancel {
  padding: 8px 20px;
  background: #e0e0e0;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-confirm {
  padding: 8px 20px;
  background: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-confirm-pink {
  background: var(--primary);
}

.btn-confirm-pink:hover {
  opacity: 0.9;
}

.modal-enter-active, .modal-leave-active {
  transition: opacity 0.2s;
}
.modal-enter-from, .modal-leave-to {
  opacity: 0;
}
</style>
