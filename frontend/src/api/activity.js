// ============================================================
// 活动接口封装：前端所有后端请求都走这里，路径集中管理。
// 开发环境通过 vite.config.js 的 proxy 把 /api 转发到 4987 后端。
// ============================================================

import axios from 'axios'

// 统一响应体 {code, msg, data}：code=1 时取 data，否则抛出 msg 交给页面提示
function unwrap(res) {
  if (res.data.code !== 1) {
    // fromServer 标记：区分「后端明确告诉我们的原因」和「断网/5xx 这类意外」。
    // 前者原样显示给用户（如「请求参数格式不正确…」），后者才用调用方的兜底文案。
    throw Object.assign(new Error(res.data.msg || '请求失败'), { fromServer: true })
  }
  return res.data.data
}

/**
 * 从异常里取出给用户看的提示文案。
 * @param {*} e 捕获到的异常
 * @param {string} fallback 非业务错误（网络、5xx）时使用的兜底文案
 */
export function errText(e, fallback) {
  return e && e.fromServer && e.message ? e.message : fallback
}

/**
 * 活动列表（可按条件筛选）。
 * @param {{keyword?: string, status?: number|null}} [params]
 *   keyword 传空、status 传 null 时 axios 会自动把它们从 URL 里去掉，
 *   于是「不筛选」就等于不带参数 —— 后端那两个参数都是可选（required = false）。
 */
export function getActivityList(params) {
  return axios.get('/api/activities', { params }).then(unwrap)
}

/** 活动详情 */
export function getActivityDetail(id) {
  return axios.get(`/api/activities/${id}`).then(unwrap)
}

/** 新增活动 */
export function addActivity(data) {
  return axios.post('/api/activities', data).then(unwrap)
}

/** 修改活动 */
export function updateActivity(data) {
  return axios.put(`/api/activities/${data.id}`, data).then(unwrap)
}

/** 删除活动 */
export function deleteActivity(id) {
  return axios.delete(`/api/activities/${id}`).then(unwrap)
}

/** 上传图片：表单字段名 file，返回可访问路径（如 /uploads/xxx.jpg） */
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  // axios 会自动设置 multipart/form-data 的 Content-Type（带 boundary），不用手动写
  return axios.post('/api/upload', formData).then(unwrap)
}
