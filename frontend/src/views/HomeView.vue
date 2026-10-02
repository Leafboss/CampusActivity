<template>
  <HeroBanner />

  <!-- 活动列表：双语文档标题（学校官网风格） -->
  <section id="activity-list" class="container list-section">
    <div class="section-title">
      <h2>近期活动</h2>
      <span class="en">Upcoming Events</span>
    </div>

    <!-- 搜索 + 状态筛选：条件拼成查询参数发给后端，由 SQL 去筛（GET /api/activities?keyword=&status=）
         这里是「表单 + 回车/点搜索才发请求」，所以每按一次只打一次接口，不用做防抖 -->
    <form class="filter-bar" @submit.prevent="load">
      <input
        v-model.trim="keyword"
        class="search-input"
        type="search"
        placeholder="搜索活动名称、地点或简介，回车确认"
        aria-label="搜索活动"
      />
      <button type="submit" class="btn search-btn">搜索</button>
      <div class="tabs">
        <button
          v-for="tab in statusTabs"
          :key="String(tab.value)"
          type="button"
          class="tab"
          :class="{ active: status === tab.value }"
          @click="pickStatus(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
      <!-- 筛选生效时才报命中条数，免得没搜索也挂个数字 -->
      <span v-if="isFiltering" class="result-tip">找到 {{ activities.length }} 场</span>
    </form>

    <!-- 接口异常时的友好提示 -->
    <p v-if="errorMsg" class="error-tip">{{ errorMsg }}</p>

    <!-- 三种情况分开说：正在加载 / 筛没了 / 一条活动都没有 -->
    <p v-else-if="!loaded" class="empty-tip">正在加载活动…</p>
    <p v-else-if="!activities.length && isFiltering" class="empty-tip">
      没有符合条件的活动，换个关键词或筛选条件试试。
    </p>
    <p v-else-if="!activities.length" class="empty-tip">
      还没有发布任何活动，可以去 <RouterLink to="/manage">活动管理</RouterLink> 添加一个。
    </p>

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
import { computed, onMounted, ref } from 'vue'
import HeroBanner from '../components/HeroBanner.vue'
import ActivityCard from '../components/ActivityCard.vue'
import StatsBand from '../components/StatsBand.vue'
import { getActivityList, errText } from '../api/activity'
import { STATUS_TABS } from '../utils/status'

// 筛选条件：keyword 是搜索框内容；status 为 null 表示「全部」，1=报名中 / 0=已结束。
// 注意这两个值不参与任何本地计算，它们只是「要发给后端的查询参数」。
const statusTabs = STATUS_TABS
const keyword = ref('')
const status = ref(null)

// 活动列表：后端返回什么就渲染什么（已经是筛选后的结果）
const activities = ref([])
const errorMsg = ref('')
// 是否至少成功请求过一次：用来区分「还在加载」和「一条都没有」
const loaded = ref(false)

// 是否启用了筛选条件（只影响提示文案，筛选本身由后端做）
const isFiltering = computed(() => keyword.value !== '' || status.value !== null)

/** 按当前条件向后端要一次列表：GET /api/activities?keyword=xx&status=1 */
async function load() {
  try {
    activities.value = await getActivityList({
      // 空串 / null 都会被 axios 从 URL 里去掉，后端看到的就是「这个条件不参与筛选」
      keyword: keyword.value || null,
      status: status.value,
    })
    errorMsg.value = ''
    loaded.value = true
  } catch (e) {
    errorMsg.value = errText(e, '活动列表加载失败，请确认后端服务已启动。')
  }
}

/** 点状态按钮：先切换条件，再按新条件查一次 */
function pickStatus(value) {
  status.value = value
  load()
}

onMounted(load)
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

/* 搜索 + 状态筛选：细线直角，沿用站点风格 */
.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 28px;
}
.search-input {
  flex: 0 1 300px;
  padding: 9px 12px;
  border: 1px solid var(--line);
  background: #fff;
  font-size: 14px;
  font-family: inherit;
  color: var(--ink);
}
.search-input:focus {
  outline: 1px solid var(--red);
  border-color: var(--red);
}
/* 搜索按钮：比通用 .btn 小一号，才配得上筛选栏的高度 */
.search-btn {
  padding: 9px 24px;
  font-size: 14px;
  letter-spacing: 1px;
}
/* 状态按钮连成一排：用 -1px 外间距把相邻边框压成一条线 */
.tabs {
  display: flex;
}
.tab {
  padding: 8px 18px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--gray-text);
  font-size: 14px;
  font-family: inherit;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.2s;
}
.tab + .tab {
  margin-left: -1px;
}
.tab:hover {
  position: relative;
  color: var(--red-text);
  border-color: var(--red);
}
.tab.active {
  position: relative;
  background: var(--red);
  border-color: var(--red);
  color: #fff;
}
.result-tip {
  font-size: 13px;
  color: var(--gray-text);
  letter-spacing: 1px;
}

/* 空态：没有活动 / 筛没了 */
.empty-tip {
  color: var(--gray-text);
  text-align: center;
  padding: 48px 0;
}
.empty-tip a {
  color: var(--red-text);
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
