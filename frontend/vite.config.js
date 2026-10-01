import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // 开发时把 /api 开头的请求转发给本地 8080 的 Spring Boot 后端，避免跨域
      '/api': {
        target: 'http://localhost:4987',
        changeOrigin: true,
      },
    },
  },
})
