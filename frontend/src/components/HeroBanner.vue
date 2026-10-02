<template>
  <!-- Hero：全屏大图 + 居中大标题（模板站风格）。
       背景图 src/assets/hero.jpg 是学校实景航拍（真实照片），换图直接替换该文件即可 -->
  <section class="hero">
    <div class="hero-mask">
      <p class="hero-eyebrow">珠海科技学院</p>
      <h1 class="hero-title">校园活动</h1>
      <p class="hero-en">CAMPUS EVENTS</p>
      <a class="btn hero-btn" href="#activity-list">浏览活动</a>
    </div>
    <div class="scroll-hint">向下滚动</div>
  </section>
</template>

<style scoped>
.hero {
  height: 78vh;
  min-height: 480px;
  background: url('../assets/hero.jpg') center / cover no-repeat;
  position: relative;
}

/* 半透明遮罩保证文字可读，真实照片换上后同样适用 */
.hero-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.hero-eyebrow {
  font-size: 15px;
  letter-spacing: 6px;
  opacity: 0.9;
}

/* 超大标题：海报感的核心 */
.hero-title {
  font-size: clamp(48px, 9vw, 110px);
  font-weight: 700;
  letter-spacing: 12px;
  margin: 12px 0 4px;
  text-shadow: 0 2px 24px rgba(0, 0, 0, 0.4);
}

.hero-en {
  font-family: var(--font-en);
  font-size: clamp(16px, 2.4vw, 26px);
  letter-spacing: 10px;
  text-transform: uppercase;
  opacity: 0.9;
}

.hero-btn {
  margin-top: 36px;
}

/* 底部滚动提示：让首屏下方内容"露个苗头" */
.scroll-hint {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  color: rgba(255, 255, 255, 0.85);
  font-size: 12px;
  letter-spacing: 4px;
  animation: float 2s ease-in-out infinite;
}
@keyframes float {
  0%, 100% { transform: translate(-50%, 0); }
  50% { transform: translate(-50%, 8px); }
}
</style>
