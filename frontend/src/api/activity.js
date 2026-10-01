// ============================================================
// 活动接口封装：前端所有后端请求都走这里，路径集中管理。
// 开发环境通过 vite.config.js 的 proxy 把 /api 转发到 8080 后端。
// ============================================================

import axios from 'axios'

// 统一响应体 {code, msg, data}：code=1 时取 data，否则抛出 msg 交给页面提示
function unwrap(res) {
  if (res.data.code !== 1) {
    throw new Error(res.data.msg || '请求失败')
  }
  return res.data.data
}

/** 活动列表 */
export function getActivityList() {
  return axios.get('/api/activities').then(unwrap)
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
