<template>
  <div class="admin-login-page">
    <div class="admin-login-card">
      <h2 class="admin-login-title">管理员登录</h2>
      <form class="admin-login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label>管理员 ID</label>
          <input
            v-model.number="form.adminId"
            type="number"
            placeholder="请输入管理员ID"
            autocomplete="username"
          />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
          />
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <div class="back-link">
        <router-link to="/login">返回用户登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { adminAuthApi } from '../../api'

const router = useRouter()
const loading = ref(false)
const error = ref('')

const form = reactive({
  adminId: '',
  password: ''
})

/**
 * 管理员登录流程：
 * 1) 前端做必填校验
 * 2) 调用 /api/admin/auth/login 获取 token
 * 3) 将 token/adminId 写入 localStorage
 * 4) 跳转到 /admin，后续接口由请求拦截器自动携带 adminToken
 */
async function handleLogin() {
  error.value = ''
  if (form.adminId === '' || form.adminId == null) {
    error.value = '请输入ID'
    return
  }
  if (!form.password) {
    error.value = '请输入密码'
    return
  }
  loading.value = true
  try {
    const res = await adminAuthApi.login(form.adminId, form.password)
    localStorage.setItem('adminToken', res.token)
    localStorage.setItem('adminId', String(res.adminId))
    router.replace('/admin')
  } catch (e) {
    // 后端会返回明确错误文案：如“密码错误请重新输入”“不存在该管理员”
    const msg = e?.message || e?.error || '登录失败'
    error.value = msg
    if (msg === '密码错误请重新输入') {
      form.password = ''
    }
    if (msg === '不存在该管理员') {
      form.adminId = ''
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url(/img/管理登录.jpg) no-repeat center center;
  background-size: cover;
}

.admin-login-card {
  width: 100%;
  max-width: 380px;
  min-height: 420px;
  padding: 40px 36px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.admin-login-title {
  font-size: 1.35rem;
  font-weight: 600;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid #333;
}

.admin-login-form .form-group {
  margin-bottom: 18px;
}

.admin-login-form .form-group label {
  display: block;
  font-size: 0.9rem;
  color: #555;
  margin-bottom: 6px;
}

.admin-login-form .form-group input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
  box-sizing: border-box;
}

.admin-login-form .form-group input:focus {
  outline: none;
  border-color: #8b4513;
}

.error-msg {
  color: #c0392b;
  font-size: 0.9rem;
  margin-bottom: 12px;
}

.btn-submit {
  width: 100%;
  padding: 12px 24px;
  background: #2c3e50;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
}

.btn-submit:hover:not(:disabled) {
  background: #1a252f;
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.back-link {
  margin-top: 20px;
  text-align: center;
}

.back-link a {
  font-size: 0.9rem;
  color: #666;
  text-decoration: none;
}

.back-link a:hover {
  color: var(--primary, #8b4513);
  text-decoration: underline;
}
</style>
