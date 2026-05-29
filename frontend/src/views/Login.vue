<template>
  <div class="auth-page login-page">
    <!-- 提示 Toast（如：用户不存在） -->
    <Transition name="fade">
      <div v-if="toastTip" class="success-toast">{{ toastTip }}</div>
    </Transition>
    <div class="auth-layout">
      <div class="auth-banner" :style="{ backgroundImage: 'url(/img/登录.jpg)' }">
      </div>
      <div class="auth-form-wrap">
        <div class="auth-card">
          <h2 class="auth-title">快速登录</h2>
          <form class="auth-form" @submit.prevent="handleLogin">
            <div class="form-group">
              <span class="input-icon">👤</span>
              <input
                v-model="form.username"
                type="text"
                placeholder="用户名或邮箱"
                autocomplete="username"
              />
            </div>
            <div class="form-group">
              <span class="input-icon">🔒</span>
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="密码"
                autocomplete="current-password"
              />
            </div>
            <div class="form-options">
              <label class="login-status">
                <span>登录状态</span>
                <select v-model="form.sessionType">
                  <option value="browser">浏览器进程</option>
                  <option value="session">当前会话</option>
                </select>
              </label>
              <a href="#" class="forgot-link" @click.prevent>忘记密码</a>
            </div>
            <p v-if="error" class="error-msg">{{ error }}</p>
            <button type="submit" class="btn-submit" :disabled="loading">
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </form>
          <div class="third-party">
            <p class="third-title">第三方登录</p>
            <div class="third-icons">
              <span class="third-btn" title="微信登录">微信</span>
              <span class="third-btn" title="QQ登录">QQ</span>
              <span class="third-btn" title="微博登录">微博</span>
            </div>
            <p class="third-tip">第三方登录功能敬请期待</p>
          </div>
          <div class="admin-entry">
            <router-link to="/admin/login" class="admin-login-btn">管理员登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'
import { notifyAuthUpdate } from '../store/auth'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const toastTip = ref('')
const showPassword = ref(false)

const form = reactive({
  username: '',
  password: '',
  sessionType: 'browser'
})

async function handleLogin() {
  error.value = ''
  // 前端基础校验：避免空请求打到后端。
  if (!form.username.trim()) {
    error.value = '请输入用户名或邮箱'
    return
  }
  if (!form.password) {
    error.value = '请输入密码'
    return
  }
  loading.value = true
  try {
    // 调用后端用户端登录接口（/api/auth/login）。
    const res = await authApi.login({
      username: form.username.trim(),
      password: form.password
    })
    // 登录成功后写入 token 与用户信息，供请求拦截器和页面状态使用。
    localStorage.setItem('token', res.token)
    if (form.sessionType === 'session') {
      sessionStorage.setItem('token', res.token)
    }
    localStorage.setItem('username', res.username)
    if (res.nickname) localStorage.setItem('nickname', res.nickname)
    localStorage.setItem('userId', String(res.userId))
    if (res.avatarUrl) localStorage.setItem('avatar_url', res.avatarUrl)
    notifyAuthUpdate()
    router.replace('/')
  } catch (e) {
    // 与后端返回的 message 对齐，做细粒度提示。
    const msg = e?.message || e?.error || '登录失败'
    if (msg === '密码错误') {
      toastTip.value = '密码错误'
      form.password = ''
      setTimeout(() => { toastTip.value = '' }, 2000)
    } else if (msg === '用户不存在' || msg === '邮箱未注册') {
      toastTip.value = msg
      setTimeout(() => { toastTip.value = '' }, 2000)
    } else if (msg === '该用户已经被封禁') {
      toastTip.value = '该用户已经被封禁'
      setTimeout(() => { toastTip.value = '' }, 3000)
    } else {
      error.value = msg
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.success-toast {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  background: rgba(0, 0, 0, 0.75);
  color: #fff;
  border-radius: 8px;
  font-size: 0.95rem;
  z-index: 2000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.auth-page {
  min-height: calc(100vh - var(--header-height));
  display: flex;
  align-items: stretch;
}

.auth-layout {
  display: flex;
  width: 100%;
  min-height: calc(100vh - var(--header-height));
}

.auth-banner {
  flex: 1;
  min-width: 0;
  background-size: cover;
  background-position: center;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.auth-slogan {
  font-size: 2rem;
  font-weight: 700;
  color: #c0392b;
  text-shadow: 0 1px 2px rgba(0,0,0,.1);
  text-align: center;
  max-width: 400px;
}

.auth-form-wrap {
  width: 440px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fff;
}

.auth-card {
  width: 100%;
  max-width: 360px;
}

.auth-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid #333;
}

.auth-form .form-group {
  display: flex;
  align-items: center;
  border: 1px solid #ddd;
  border-radius: 6px;
  margin-bottom: 16px;
  overflow: hidden;
}

.input-icon {
  padding: 0 12px;
  color: #999;
  font-size: 1rem;
}

.auth-form input {
  flex: 1;
  padding: 12px 14px;
  border: none;
  outline: none;
  font-size: 0.95rem;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 0.9rem;
}

.login-status {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.login-status select {
  padding: 4px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
}

.forgot-link {
  color: var(--primary);
}

.forgot-link:hover {
  text-decoration: underline;
}

.error-msg {
  color: #c0392b;
  font-size: 0.9rem;
  margin-bottom: 12px;
}

.btn-submit {
  width: 100%;
  padding: 12px 24px;
  background: #8b4513;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
}

.btn-submit:hover:not(:disabled) {
  background: #6d3410;
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.third-party {
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.third-title {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 12px;
}

.third-icons {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.third-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.8rem;
  color: #666;
  cursor: pointer;
}

.third-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.third-tip {
  font-size: 0.8rem;
  color: #999;
}

.admin-entry {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.admin-login-btn {
  font-size: 0.85rem;
  color: #888;
  text-decoration: none;
}

.admin-login-btn:hover {
  color: var(--primary);
  text-decoration: underline;
}
</style>
