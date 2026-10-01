<template>
  <HeroBanner />

  <!-- 活动列表：双语文档标题（学校官网风格） -->
  <section id="activity-list" class="container list-section">
    <div class="section-title">
      <h2>近期活动</h2>
      <span class="en">Upcoming Events</span>
    </div>

    <!-- 接口异常时的友好提示 -->
    <p v-if="errorMsg" class="error-tip">{{ errorMsg }}</p>

    <div v-else class="grid">
      <ActivityCard
        v-for="item in activities"
        :key="item.id"
        :activity="item"
      />
    </div>
  </section>

  <StatsBand />
</template>

<script setup>
import { onMounted, ref } from 'vue'
import HeroBanner from '../components/HeroBanner.vue'
import ActivityCard from '../components/ActivityCard.vue'
import StatsBand from '../components/StatsBand.vue'
import { getActivityList } from '../api/activity'

// 活动列表数据：来自后端 GET /api/activities
const activities = ref([])
const errorMsg = ref('')

onMounted(async () => {
  try {
    activities.value = await getActivityList()
  } catch (e) {
    errorMsg.value = '活动列表加载失败，请确认后端服务已启动。'
  }
})
</script>

<style scoped>
.list-section {
  padding-top: 72px;
}

.error-tip {
  color: var(--gray-text);
  text-align: center;
  padding: 48px 0;
}

/* 三列卡片网格，平板两列、手机一列 */
.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
@media (max-width: 960px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 640px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
