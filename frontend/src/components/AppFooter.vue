<template>
  <footer class="footer">
    <div class="container footer-inner">
      <div class="brand">
        <span class="cn">校园活动信息平台</span>
        <span class="en">CAMPUS EVENTS PLATFORM</span>
      </div>
      <p class="note">本作品为技术开发岗位一轮面试考核作品 · 数据为演示用途</p>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: var(--ink);
  color: #fff;
  margin-top: 80px;
}
.footer-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 36px;
  padding-bottom: 36px;
}
.brand {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.cn {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 2px;
}
.en {
  font-family: var(--font-en);
  font-size: 12px;
  letter-spacing: 2px;
  color: #999;
}
.note {
  font-size: 13px;
  color: #999;
}
</style>
