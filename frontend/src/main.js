/**
 * 轻小说阅读系统 - 前端入口
 * Vue 3 + Vite 实现响应式界面
 */
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './styles/main.css'

createApp(App).use(router).mount('#app')
