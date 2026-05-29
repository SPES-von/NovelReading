/**
 * API 请求封装
 * 统一调用后端 RESTful API，支持 JWT 认证
 */
import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截：管理员接口使用 adminToken，其余使用用户 token
api.interceptors.request.use(config => {
  const isAdminApi = config.url && config.url.includes('/admin') && !config.url.includes('/admin/auth/login')
  const token = isAdminApi ? localStorage.getItem('adminToken') : localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截：处理 401 等错误，保留 statusCode 便于业务层判断
api.interceptors.response.use(
  res => res.data,
  err => {
    const data = err.response?.data
    const status = err.response?.status
    const url = err.config?.url || ''
    const isAdminApi = typeof url === 'string' && url.includes('/admin') && !url.includes('/admin/auth/login')
    const payload = typeof data === 'object' && data !== null ? { ...data } : { message: data || err.message }
    payload.statusCode = status

    // 管理端 token 失效/不匹配时：清理并跳转管理员登录，避免一直 403
    if (isAdminApi && (status === 401 || status === 403)) {
      try {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('adminId')
      } catch {}
      // 不依赖 router，直接跳转
      if (typeof window !== 'undefined' && window.location?.pathname !== '/admin/login') {
        window.location.href = '/admin/login'
      }
    }
    return Promise.reject(payload)
  }
)

export const novelApi = {
  getHomeData: () => api.get('/novels/home'),
  getHotList: () => api.get('/novels/hot'),
  getFeatured: () => api.get('/novels/featured'),
  getCompleted: () => api.get('/novels/completed'),
  getPublisherNovels: () => api.get('/novels/by-publisher'),
  getById: id => api.get(`/novels/${id}`),
  getRankings: () => api.get('/novels/rankings'),
  getCompletedChart: (sort = 'viewCount') => api.get('/novels/completed-chart', { params: { sort } }),
  search: (keyword, page = 0, size = 20) =>
    api.get('/novels/search', { params: { keyword, page, size } }),
  flower: id => api.post(`/novels/${id}/flower`),
  favorite: id => api.post(`/novels/${id}/favorite`)
}

/** 书籍评论：获取某书评论列表（公开）；发布评论需登录；点赞/踩每次 +1 */
export const commentApi = {
  getByNovel: novelId => api.get(`/novels/${novelId}/comments`),
  publish: (novelId, content) => api.post(`/novels/${novelId}/comments`, { content }),
  like: (novelId, commentId) => api.post(`/novels/${novelId}/comments/${commentId}/like`),
  dislike: (novelId, commentId) => api.post(`/novels/${novelId}/comments/${commentId}/dislike`)
}

export const publisherApi = {
  list: () => api.get('/publishers')
}

export const authorApi = {
  list: () => api.get('/authors'),
  add: name => api.post('/authors', { name })
}

export const tagApi = {
  list: () => api.get('/tags')
}

/** 筛选页：tagId, isAnimated(0|1), wordCountRange, updatedWithinDays, sort, page, size */
export const browseApi = {
  browse: (params) => api.get('/novels/browse', { params })
}

export const authApi = {
  // 用户端登录：POST /api/auth/login
  login: data => api.post('/auth/login', data),
  // 用户端注册：POST /api/auth/register
  register: data => api.post('/auth/register', data)
}

/** 管理员认证（独立接口，不附加用户 token） */
export const adminAuthApi = {
  login: (adminId, password) => api.post('/admin/auth/login', { adminId: Number(adminId), password })
}

/** 管理员用户管理；列表支持 keyword、year/month/day 注册日期搜索 */
export const adminUserApi = {
  list: (params) => api.get('/admin/users', { params }),
  delete: id => api.delete(`/admin/users/${id}`),
  ban: (id, durationHours) => api.post(`/admin/users/${id}/ban`, { durationHours }),
  unban: id => api.post(`/admin/users/${id}/unban`)
}

/** 管理员标签管理 */
export const adminTagApi = {
  list: () => api.get('/admin/tags'),
  add: name => api.post('/admin/tags', { name }),
  delete: id => api.delete(`/admin/tags/${id}`)
}

/** 管理员小说列表（id+title，用于下拉等） */
export const adminNovelApi = {
  list: () => api.get('/admin/novels'),
  add: data => api.post('/admin/novels', data),
  addChapter: (novelId, data) => api.post(`/admin/novels/${novelId}/chapters`, data),
  updateChapter: (chapterId, data) => api.put(`/admin/novels/chapters/${chapterId}`, data)
}

/** 管理员小说-标签关联（novel_tag）：为某小说添加/删除标签 */
export const adminNovelTagApi = {
  add: (novelId, tagId) => api.post(`/admin/novels/${novelId}/tags/${tagId}`),
  remove: (novelId, tagId) => api.delete(`/admin/novels/${novelId}/tags/${tagId}`)
}

/** 管理员评论管理：列表、审核、删除；列表支持 keyword、year/month/day 搜索 */
export const adminCommentApi = {
  list: (params) => api.get('/admin/comments', { params }),
  audit: id => api.put(`/admin/comments/${id}/audit`),
  delete: id => api.delete(`/admin/comments/${id}`)
}

/** 管理员统计：概览数据、运营报表导出 */
export const adminStatisticsApi = {
  overview: () => api.get('/admin/statistics/overview'),
  exportReport: () => api.get('/admin/statistics/export', { responseType: 'blob' })
}

/**
 * 管理员系统设置
 * - overview：{ systemParams, serverStatus, logs[], generatedAt }；logs 为后端聚合的业务活动，非文件日志
 * - changePassword：改当前管理员密码
 */
export const adminSettingsApi = {
  overview: () => api.get('/admin/settings/overview'),
  changePassword: (data) => api.put('/admin/settings/password', data)
}

/** 我的书架：需登录 */
export const bookshelfApi = {
  list: () => api.get('/bookshelf'),
  remove: novelId => api.delete(`/bookshelf/${novelId}`)
}

/** 用户个人中心：需登录 */
export const userApi = {
  getProfile: () => api.get('/user/profile'),
  updateProfile: data => api.put('/user/profile', data),
  getPreferences: () => api.get('/user/preferences'),
  updatePreferences: data => api.put('/user/preferences', data)
}

/** 章节接口：详情页目录与阅读页正文都从这里取数 */
export const chapterApi = {
  /** 获取某本书的全量章节目录（后端按卷序+章序返回） */
  getByNovel: novelId => api.get(`/chapters/novel/${novelId}`),
  /** 获取单章正文（标题、内容等） */
  getById: id => api.get(`/chapters/${id}`)
}

/** 阅读进度/书签：需登录 */
export const readingProgressApi = {
  /** 获取当前用户对某本书的阅读进度，返回 { chapterId, chapterTitle } 或 null */
  getProgress: novelId => api.get('/reading-progress', { params: { novelId } }),
  /** 获取当前用户全部阅读历史列表 */
  listAll: () => api.get('/reading-progress/list'),
  saveBookmark: (novelId, chapterId, progressPosition = null, bookmark = null) =>
    api.post('/reading-progress/bookmark', { novelId, chapterId, progressPosition, bookmark })
}

export default api
