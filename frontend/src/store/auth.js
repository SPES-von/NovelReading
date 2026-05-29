/**
 * 登录状态更新信号：登录/注册/登出后调用，让 Header 等组件立即从 localStorage 重新读取并更新 UI
 */
import { ref } from 'vue'

export const authUpdated = ref(0)

export function notifyAuthUpdate() {
  authUpdated.value++
}
