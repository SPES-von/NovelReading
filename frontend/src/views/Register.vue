<template>
  <div class="auth-page register-page">
    <!-- 失败提示 Toast（与加入书架同款样式） -->
    <Transition name="fade">
      <div v-if="toastTip" class="success-toast">{{ toastTip }}</div>
    </Transition>
    <div class="auth-layout">
      <div class="auth-banner" :style="{ backgroundImage: 'url(/img/注册.jpg)' }">
      </div>
      <div class="auth-form-wrap">
        <div class="auth-card">
          <h2 class="auth-title">注册账号</h2>
          <form class="auth-form" @submit.prevent="handleRegister">
            <div class="form-group">
              <span class="input-icon">👤</span>
              <input
                v-model="form.username"
                type="text"
                placeholder="用户名"
                autocomplete="username"
              />
            </div>
            <div class="form-group">
              <span class="input-icon">🔒</span>
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="密码"
                autocomplete="new-password"
              />
            </div>
            <div class="form-group">
              <span class="input-icon">🔒</span>
              <input
                v-model="form.confirmPassword"
                :type="showPassword ? 'text' : 'password'"
                placeholder="确认密码"
                autocomplete="new-password"
              />
            </div>
            <div class="form-group">
              <span class="input-icon">✏️</span>
              <input
                v-model="form.nickname"
                type="text"
                placeholder="昵称（选填）"
                autocomplete="nickname"
              />
            </div>
            <div class="form-group">
              <span class="input-icon">📧</span>
              <input
                v-model="form.email"
                type="email"
                placeholder="邮箱"
                autocomplete="email"
              />
            </div>
            <button type="submit" class="btn-submit" :disabled="loading">
              {{ loading ? '注册中...' : '注册' }}
            </button>
          </form>
          <div class="third-party">
            <p class="third-title">第三方快捷注册</p>
            <div class="third-icons">
              <span class="third-btn" title="微信">微信</span>
              <span class="third-btn" title="QQ">QQ</span>
              <span class="third-btn" title="微博">微博</span>
            </div>
            <p class="third-tip">第三方登录功能敬请期待</p>
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
const toastTip = ref('')
const showPassword = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: ''
})

async function handleRegister() {
  toastTip.value = ''
  const username = form.username.trim()
  const email = form.email.trim()
  // 前端基础校验：与后端规则保持一致，优先给用户即时反馈。
  if (!username) {
    toastTip.value = '没有填写用户名'
    setTimeout(() => { toastTip.value = '' }, 2000)
    return
  }
  if (!email) {
    toastTip.value = '邮件没有填写'
    setTimeout(() => { toastTip.value = '' }, 2000)
    return
  }
  if (!form.password) {
    toastTip.value = '密码没有填写'
    setTimeout(() => { toastTip.value = '' }, 2000)
    return
  }
  if (form.username.trim().length < 2) {
    toastTip.value = '用户名至少2个字符'
    setTimeout(() => { toastTip.value = '' }, 2000)
    return
  }
  if (form.password.length < 6) {
    toastTip.value = '密码至少6位'
    setTimeout(() => { toastTip.value = '' }, 2000)
    return
  }
  if (form.password !== form.confirmPassword) {
    toastTip.value = '两次密码不一致'
    setTimeout(() => { toastTip.value = '' }, 2000)
    return
  }
  loading.value = true
  try {
    // 调用后端用户端注册接口（/api/auth/register）。
    const res = await authApi.register({
      username,
      password: form.password,
      nickname: form.nickname.trim() || undefined,
      email
    })
    // 注册成功后直接写入登录态（后端会返回 token），无需再次登录。
    localStorage.setItem('token', res.token)
    localStorage.setItem('username', res.username)
    if (res.nickname) localStorage.setItem('nickname', res.nickname)
    localStorage.setItem('userId', String(res.userId))
    if (res.avatarUrl) localStorage.setItem('avatar_url', res.avatarUrl)
    notifyAuthUpdate()
    router.replace('/')
  } catch (e) {
    // 对常见占用场景做友好提示，其余错误透传。
    const msg = e?.message || e?.error || '注册失败'
    if (msg === '用户名已经被注册' || msg === '该用户名已经被注册') {
      toastTip.value = '用户名已经被注册'
    } else if (msg === '该邮箱已经被注册' || msg === '邮箱已经被注册') {
      toastTip.value = '该邮箱已经被注册'
    } else {
      toastTip.value = msg
    }
    setTimeout(() => { toastTip.value = '' }, 2000)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 与加入书架一致的浮层样式 */
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

.btn-submit {
  width: 100%;
  padding: 12px 24px;
  margin-top: 8px;
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
</style>
