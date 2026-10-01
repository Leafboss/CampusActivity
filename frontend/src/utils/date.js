// 日期格式化工具：把 "2026-10-15 19:00" 拆成卡片上的日期块

/** 取"日"（大数字），如 15 */
export function dayOf(timeStr) {
  return timeStr.slice(8, 10)
}

/** 取"年-月"，如 2026.10 */
export function yearMonth(timeStr) {
  return timeStr.slice(0, 7).replace('-', '.')
}

/** 取时刻，如 19:00 */
export function hhmm(timeStr) {
  return timeStr.slice(11)
}

/** 完整日期展示，如 2026.10.15 19:00 */
export function full(timeStr) {
  return timeStr.slice(0, 10).replaceAll('-', '.') + ' ' + timeStr.slice(11)
}
