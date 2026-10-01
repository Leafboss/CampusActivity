<template>
  <header>
    <!-- 顶部公告条（参考模板站的细条设计） -->
    <div class="topbar">
      <span>珠海科技学院 · 校园活动信息平台</span>
      <span class="topbar-right">2026 秋季学期</span>
    </div>

    <!-- 主导航 -->
    <nav class="navbar container">
      <RouterLink to="/" class="logo">
        <span class="logo-cn">校园活动</span>
        <span class="logo-en">CAMPUS EVENTS</span>
      </RouterLink>

      <div class="links">
        <RouterLink to="/" class="link">活动列表</RouterLink>
        <RouterLink to="/manage" class="link">活动管理</RouterLink>
      </div>
    </nav>
  </header>
</template>

<style scoped>
/* 公告条：学校官网同款导航红渐变 */
.topbar {
  background: var(--nav-grad);
  color: #fff;
  font-size: 13px;
  letter-spacing: 1px;
  display: flex;
  justify-content: space-between;
  padding: 6px 24px;
}
.topbar-right {
  opacity: 0.85;
}

/* 导航栏：白底，下方细分隔线 */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  border-bottom: 1px solid var(--line);
}

/* logo：中文名 + 英文窄体大写（模板站风格） */
.logo {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.logo-cn {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 3px;
}
.logo-en {
  font-family: var(--font-en);
  font-size: 13px;
  letter-spacing: 2px;
  color: var(--red-text);
  text-transform: uppercase;
}

.links {
  display: flex;
  gap: 32px;
}
.link {
  font-size: 15px;
  letter-spacing: 1px;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
  transition: color 0.2s;
}
.link:hover {
  color: var(--red-text);
}
/* 当前路由高亮（router-link 自动加的类） */
.link.router-link-active {
  color: var(--red-text);
  border-bottom-color: var(--red-text);
}
</style>
