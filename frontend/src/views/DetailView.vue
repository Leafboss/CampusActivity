<template>
  <!-- 活动详情页 -->
  <div v-if="activity" class="container detail">
    <!-- 返回列表 -->
    <button class="back" @click="router.back()">← 返回列表</button>

    <!-- 头部大图 -->
    <div class="hero-img">
      <img :src="activity.image" :alt="activity.name" />
      <span class="badge" :class="statusClass(activity.status)">
        {{ statusText(activity.status) }}
      </span>
    </div>

    <div class="content">
      <h1 class="name">{{ activity.name }}</h1>

      <!-- 时间地点信息条 -->
      <div class="meta-bar">
        <div class="meta-block">
          <span class="meta-label">活动时间</span>
          <span class="meta-value">{{ full(activity.time) }}</span>
        </div>
        <div class="meta-block">
          <span class="meta-label">活动地点</span>
          <span class="meta-value">{{ activity.location }}</span>
        </div>
      </div>

      <!-- 正文详情 -->
      <div class="section-title">
        <h2>活动详情</h2>
        <span class="en">Details</span>
      </div>
      <p class="text">{{ activity.detail }}</p>
    </div>
  </div>

  <!-- id 不存在时的兜底 -->
  <div v-else class="container not-found">
    <p>没有找到该活动。</p>
    <RouterLink class="btn" to="/">返回活动列表</RouterLink>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getActivityDetail } from '../api/activity'
import { full } from '../utils/date'
import { statusClass, statusText } from '../utils/status'

// 从路由参数取 id，请求后端获取活动详情；查不到或接口异常时显示兜底页
const route = useRoute()
const router = useRouter()
const activity = ref(null)

onMounted(async () => {
  try {
    activity.value = await getActivityDetail(route.params.id)
  } catch (e) {
    activity.value = null
  }
})
</script>

<style scoped>
.detail {
  padding-top: 40px;
  max-width: 900px;
}

.back {
  background: none;
  border: none;
  font-size: 14px;
  color: var(--gray-text);
  cursor: pointer;
  margin-bottom: 20px;
  letter-spacing: 1px;
}
.back:hover {
  color: var(--red-text);
}

.hero-img {
  position: relative;
  aspect-ratio: 16 / 8;
  overflow: hidden;
}
.hero-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 6px 16px;
  font-size: 13px;
  letter-spacing: 1px;
  color: #fff;
}
.badge.upcoming {
  background: var(--red);
}
.badge.ended {
  background: #8a8a8a;
}

.content {
  padding: 36px 0;
}
.name {
  font-size: 34px;
  font-weight: 700;
  letter-spacing: 2px;
}

/* 时间/地点信息条：浅灰底，两个信息块并排 */
.meta-bar {
  display: flex;
  gap: 48px;
  background: var(--gray-bg);
  padding: 20px 24px;
  margin: 24px 0 40px;
}
.meta-label {
  display: block;
  font-size: 12px;
  letter-spacing: 2px;
  color: var(--gray-text);
  margin-bottom: 4px;
}
.meta-value {
  font-size: 16px;
  font-weight: 600;
}

.text {
  font-size: 16px;
  line-height: 2;
  color: #333;
}

.not-found {
  padding: 120px 24px;
  text-align: center;
}
.not-found p {
  margin-bottom: 24px;
  color: var(--gray-text);
}
</style>
