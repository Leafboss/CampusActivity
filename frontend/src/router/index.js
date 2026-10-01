import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import DetailView from '../views/DetailView.vue'
import AdminView from '../views/AdminView.vue'

// 三个视图：活动列表（含 hero）、活动详情、活动管理（增删改）
const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/activity/:id', name: 'detail', component: DetailView },
  { path: '/manage', name: 'manage', component: AdminView },
]

export default createRouter({
  history: createWebHistory(),
  routes,
  // 切换路由时回到顶部，详情页体验更自然
  scrollBehavior: () => ({ top: 0 }),
})
