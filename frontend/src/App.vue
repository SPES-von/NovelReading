<template>
  <div class="app">
    <Header v-if="!isReadPage && !isAdminPage" />
    <main class="main" :class="{ 'main-no-header': isReadPage || isAdminPage }">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from './components/Header.vue'

const route = useRoute()
const isReadPage = computed(() => route.name === 'NovelRead')
const isAdminPage = computed(() => route.path.startsWith('/admin'))
</script>

<style scoped>
.app {
  min-height: 100vh;
}
.main {
  padding-top: var(--header-height);
}
.main-no-header {
  padding-top: 0;
}
</style>
