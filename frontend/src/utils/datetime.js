// ============================================================
// 活动时间的输入规范：把「能填什么」和「填错了怎么提示」收在这一个文件里。
// 必须和后端对齐 —— Activity.time 用 @JsonFormat("yyyy-MM-dd HH:mm") 解析，
// 而 Jackson 的 yyyy 是无符号 4 位年份：多写一位（如 191919）直接解析失败，
// 后端只会回一句「请求参数格式不正确」，用户看不出自己错在哪。
// ============================================================

/** 允许的年份区间：校园活动用不到史前/几百年后，越界一律拦下 */
export const MIN_YEAR = 2000
export const MAX_YEAR = 2100

/** datetime-local 控件的值格式固定是 "YYYY-MM-DDTHH:mm"（日期与时间之间是字母 T） */
const LOCAL_DATETIME_RE = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})$/

/** 传给 <input type="datetime-local"> 的 min / max：让原生日期面板也选不出界外年份 */
export const localMin = `${MIN_YEAR}-01-01T00:00`
export const localMax = `${MAX_YEAR}-12-31T23:59`

/**
 * 校验 datetime-local 输入框的值。
 * @returns {string} 空字符串 = 通过；否则是显示在字段下方的错误文案
 */
export function validateLocalDateTime(value) {
  if (!value) return '请选择活动时间'

  // 第 1 关：严格匹配 "4 位年-2 位月-2 位日T2 位时:2 位分"
  // 年份写成 191919 这种，正则直接不认（后端也会拒，但那时用户已经等了一次往返）
  const matched = LOCAL_DATETIME_RE.exec(value)
  if (!matched) {
    // 把「年份不是 4 位」单独挑出来说清楚：datetime-local 控件允许在年份段手打 5、6 位，
    // 这是这个坑最常见的触发方式，提示里直接点破比笼统说「格式不对」有用
    if (/^\d{5,}-/.test(value)) return `年份必须是 4 位数，例如 ${new Date().getFullYear()}`
    return '请填写完整的「年-月-日 时:分」，年份必须是 4 位'
  }

  // matched[0] 是整串，后面 5 个捕获组依次是 年 / 月 / 日 / 时 / 分
  const [, year, month, day, hour, minute] = matched.map(Number)

  // 第 2 关：年份落在合理区间
  if (year < MIN_YEAR || year > MAX_YEAR) {
    return `年份请填 ${MIN_YEAR}—${MAX_YEAR} 之间的 4 位数`
  }

  // 第 3 关：日期必须真实存在。JS 的 Date 会把 2026-02-31 自动"进位"成 3 月 3 日，
  // 所以再把填进去的年月日时分读回来比对一次，不一致就说明原本是个不存在的日期。
  const parsed = new Date(year, month - 1, day, hour, minute)
  const sameAsInput =
    parsed.getFullYear() === year &&
    parsed.getMonth() === month - 1 &&
    parsed.getDate() === day &&
    parsed.getHours() === hour &&
    parsed.getMinutes() === minute
  if (!sameAsInput) return '这一天不存在，请检查月、日、时、分'

  return ''
}