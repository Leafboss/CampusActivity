<template>
  <!-- 顶部导航：所有页面共享 -->
  <AppHeader />

  <!-- 路由出口：列表页 / 详情页 / 管理页在这里切换 -->
  <main>
    <RouterView />
  </main>

  <AppFooter />
</template>

<script setup>
import AppHeader from './components/AppHeader.vue'
import AppFooter from './components/AppFooter.vue'
</script>
