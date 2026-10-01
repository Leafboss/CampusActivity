// ============================================================
// 活动状态映射：接口和数据库里用 0/1 存，界面上要显示中文文案和 CSS 类名。
// 刻意只在这一个文件里做翻译 —— 以后加状态、改文案只改这里，
// 不用去三个组件里翻魔法数字和字面量。
// 约定：1 = 报名中 / 0 = 已结束（与 init.sql 列注释、后端 Activity 注释同一份约定）
// ============================================================

/** 状态码 → 中文文案 */
export function statusText(status) {
  return status === 1 ? '报名中' : '已结束'
}

/** 状态码 → CSS 类名（.badge.upcoming / .badge.ended 这些样式沿用原有的） */
export function statusClass(status) {
  return status === 1 ? 'upcoming' : 'ended'
}