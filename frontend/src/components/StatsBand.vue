<template>
  <!-- 珠科红横带：数据统计（借自学校官网的红色栏目带） -->
  <section class="band">
    <div class="container band-inner">
      <div v-for="item in stats" :key="item.label" class="stat">
        <span class="num">{{ item.num }}</span>
        <span class="label">{{ item.label }}</span>
      </div>
    </div>
  </section>
</template>

<script setup>
// 红带统计数据（静态展示文案，不来自后端）
const stats = [
  { num: '30+', label: '每学期活动' },
  { num: '5000+', label: '累计参与人次' },
  { num: '20+', label: '合作社团' },
  { num: '10', label: '活动场地' },
]
</script>

<style scoped>
.band {
  background: var(--red-deep);
  color: #fff;
  margin: 80px 0;
}
.band-inner {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding-top: 48px;
  padding-bottom: 48px;
}
.stat {
  text-align: center;
}
/* 相邻项之间加细白线分隔 */
.stat + .stat {
  border-left: 1px solid rgba(255, 255, 255, 0.3);
}
.num {
  display: block;
  font-family: var(--font-en);
  font-size: 42px;
  letter-spacing: 2px;
}
.label {
  display: block;
  font-size: 14px;
  letter-spacing: 2px;
  opacity: 0.9;
  margin-top: 6px;
}

@media (max-width: 640px) {
  .band-inner {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px 0;
  }
  .stat:nth-child(3) {
    border-left: none;
  }
}
</style>
