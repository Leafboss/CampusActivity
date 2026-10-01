<template>
  <!-- 活动卡片：整卡可点，跳转详情页 -->
  <article class="card" @click="goDetail">
    <div class="thumb">
      <img :src="activity.image" :alt="activity.name" />
      <!-- 状态徽章：报名中=红，已结束=灰 -->
      <span class="badge" :class="activity.status">
        {{ activity.status === 'upcoming' ? '报名中' : '已结束' }}
      </span>
    </div>

    <div class="body">
      <!-- 日期块：大数字日 + 年月（借自学校官网新闻列表） -->
      <div class="date-block">
        <span class="day">{{ dayOf(activity.time) }}</span>
        <span class="ym">{{ yearMonth(activity.time) }}</span>
      </div>

      <div class="info">
        <h3 class="name">{{ activity.name }}</h3>
        <p class="meta">
          <span class="meta-item">🕐 {{ hhmm(activity.time) }}</span>
          <span class="meta-item">📍 {{ activity.location }}</span>
        </p>
        <p class="summary">{{ activity.summary }}</p>
      </div>
    </div>
  </article>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { dayOf, yearMonth, hhmm } from '../utils/date'

// 父组件传入单个活动对象
const props = defineProps({
  activity: { type: Object, required: true },
})

const router = useRouter()
function goDetail() {
  router.push(`/activity/${props.activity.id}`)
}
</script>

<style scoped>
/* 直角卡片（模板站风格），hover 时图片微放大 */
.card {
  background: #fff;
  border: 1px solid var(--line);
  cursor: pointer;
  transition: box-shadow 0.25s, transform 0.25s;
}
.card:hover {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
}

.thumb {
  position: relative;
  aspect-ratio: 4 / 3;
  overflow: hidden;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}
.card:hover .thumb img {
  transform: scale(1.05);
}

.badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  font-size: 12px;
  letter-spacing: 1px;
  color: #fff;
}
.badge.upcoming {
  background: var(--red);
}
.badge.ended {
  background: #8a8a8a;
}

/* 卡片下半部：左日期块 + 右文字 */
.body {
  display: flex;
  gap: 16px;
  padding: 18px;
}

.date-block {
  flex-shrink: 0;
  width: 56px;
  text-align: center;
  border-right: 1px solid var(--line);
  padding-right: 14px;
}
.day {
  display: block;
  font-size: 30px;
  font-weight: 700;
  line-height: 1.1;
  color: var(--red-text);
}
.ym {
  display: block;
  font-size: 12px;
  color: var(--gray-text);
  margin-top: 2px;
}

.name {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 6px;
}
.meta {
  display: flex;
  gap: 14px;
  font-size: 13px;
  color: var(--gray-text);
  margin-bottom: 8px;
}
.summary {
  font-size: 14px;
  color: var(--gray-text);
  /* 简介最多显示两行，保持卡片高度一致 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
