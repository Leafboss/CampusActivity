<template>
  <!-- 活动管理页：表格 + 新增/编辑表单，直接调用后端增删改接口 -->
  <div class="container admin">
    <div class="section-title">
      <h2>活动管理</h2>
      <span class="en">Manage Events</span>
    </div>

    <!-- 新增按钮：点击展开表单 -->
    <button class="btn add-btn" @click="openCreate">+ 新增活动</button>

    <!-- 操作结果的轻提示 -->
    <p v-if="tip" class="tip">{{ tip }}</p>

    <!-- 活动表格 -->
    <table class="table">
      <thead>
        <tr>
          <th>名称</th>
          <th>时间</th>
          <th>地点</th>
          <th>状态</th>
          <th class="ops-col">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.name }}</td>
          <td>{{ full(item.time) }}</td>
          <td>{{ item.location }}</td>
          <td>
            <span class="tag" :class="statusClass(item.status)">
              {{ statusText(item.status) }}
            </span>
          </td>
          <td class="ops">
            <button class="op edit" @click="openEdit(item)">编辑</button>
            <button class="op del" @click="remove(item)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 新增/编辑表单（简单弹层） -->
    <div v-if="editing" class="form-mask" @click.self="cancel">
      <form class="form" @submit.prevent="save">
        <h3 class="form-title">{{ form.id ? '编辑活动' : '新增活动' }}</h3>

        <label>活动名称
          <input v-model.trim="form.name" required placeholder="如：迎新晚会" />
        </label>
        <label>活动时间
          <!-- 用浏览器原生的「日期 + 时间」选择器：格式由控件保证，不用手打字符串。
               min/max 先把可选范围卡住（原生日期面板就选不出界外年份），
               再交给 checkTime() 校验「必须 4 位年份 / 日期真实存在」（规则见 utils/datetime.js）。
               两层都要：min/max 挡不住手打，校验函数才是真正拦人的那一道 -->
          <input
            v-model="form.time"
            type="datetime-local"
            required
            :min="localMin"
            :max="localMax"
            :class="{ invalid: timeError }"
            @input="timeError = ''"
            @change="checkTime"
          />
          <span v-if="timeError" class="field-error">{{ timeError }}</span>
        </label>
        <label>活动地点
          <input v-model.trim="form.location" required placeholder="如：学校大礼堂" />
        </label>
        <label>活动简介
          <input v-model.trim="form.summary" required placeholder="一句话介绍" />
        </label>
        <label>活动详情
          <textarea v-model.trim="form.detail" rows="4" required placeholder="详细介绍"></textarea>
        </label>
        <div class="field">活动图片
          <!-- 图片上传：选择后立即传到后端，返回路径存进 form.image。
               原生 file 输入框被隐藏（它的「未选择任何文件」文案无法定制，会和图片状态自相矛盾），
               这里只把它当成触发器，用户看到的是下面自定义文案的按钮 -->
          <input ref="fileInput" class="file-input" type="file" accept="image/*" @change="onPickImage" />
          <div class="upload-row">
            <button type="button" class="btn-upload" :disabled="uploading" @click="fileInput.click()">
              {{ uploading ? '上传中…' : (form.image ? '更换图片' : '选择图片') }}
            </button>
            <!-- 只有本次真的选了文件才显示文件名；编辑旧活动时数据库里只有路径，不重复展示 -->
            <span v-if="pickedName" class="img-name">{{ pickedName }}</span>
          </div>
          <!-- 有图：预览 + 移除；没图：占位图说明 -->
          <div v-if="form.image" class="img-preview">
            <img :src="form.image" alt="活动图片预览" />
            <button type="button" class="img-remove" @click="clearImage">移除</button>
          </div>
          <span v-else class="img-hint">未选择图片，保存后自动使用默认占位图</span>
        </div>
        <label>状态
          <!-- :value 绑定数字（不是字符串），对应接口约定的 1=报名中 / 0=已结束 -->
          <select v-model="form.status">
            <option :value="1">报名中</option>
            <option :value="0">已结束</option>
          </select>
        </label>

        <div class="form-ops">
          <button type="button" class="btn ghost" @click="cancel">取消</button>
          <button type="submit" class="btn">保存</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { addActivity, deleteActivity, errText, getActivityList, updateActivity, uploadImage } from '../api/activity'
import { full } from '../utils/date'
import { localMax, localMin, validateLocalDateTime } from '../utils/datetime'
import { statusClass, statusText } from '../utils/status'

// 表格数据：来自后端，增删改成功后重新拉取
const list = ref([])
const tip = ref('')

async function load() {
  list.value = await getActivityList()
}

onMounted(async () => {
  try {
    await load()
  } catch (e) {
    tip.value = errText(e, '加载失败，请确认后端服务已启动。')
  }
})

// 表单状态：editing 控制弹层显隐，form 为当前编辑的数据副本
const editing = ref(false)
const emptyForm = { id: null, name: '', time: '', location: '', summary: '', detail: '', image: '', status: 1 }
const form = reactive({ ...emptyForm })

// 活动时间的字段级错误文案：空串表示合法（规则见 utils/datetime.js）
const timeError = ref('')

/** 校验「活动时间」，结果直接显示在字段下方；save() 提交前还会再调一次 */
function checkTime() {
  timeError.value = validateLocalDateTime(form.time)
}

function openCreate() {
  Object.assign(form, emptyForm)
  pickedName.value = ''
  timeError.value = ''
  editing.value = true
}

function openEdit(item) {
  Object.assign(form, item) // 拷贝一份，避免表单输入直接改到表格
  // 接口给的是 "2026-10-15 19:00"，而 datetime-local 输入框要的是 "2026-10-15T19:00"（中间是 T）
  form.time = item.time ? item.time.replace(' ', 'T') : ''
  // 数据库只存图片路径，拿不到用户的原始文件名，所以这里不预填名字，交给下方预览图表示「已有图片」
  pickedName.value = ''
  timeError.value = ''
  editing.value = true
}

function cancel() {
  editing.value = false
}

// 隐藏的原生 file 输入框：模板里靠它的 click() 打开系统选图框
const fileInput = ref(null)
// 图片是否正在上传：true 时按钮禁用并显示「上传中…」
const uploading = ref(false)
// 本次选择的原始文件名（只用于回显，不参与提交；编辑旧活动时为空）
const pickedName = ref('')

async function onPickImage(e) {
  const file = e.target.files[0]
  if (!file) return
  // 超过 10MB 直接拦下（后端也有同样限制，前端先拦省一次请求）
  if (file.size > 10 * 1024 * 1024) {
    tip.value = '图片不能超过 10MB'
    e.target.value = ''
    return
  }
  uploading.value = true
  try {
    form.image = await uploadImage(file)
    pickedName.value = file.name // 回显用户选择的原始文件名
    tip.value = ''
  } catch (err) {
    tip.value = errText(err, '图片上传失败，请稍后再试。')
  } finally {
    uploading.value = false
    // 清空 input，否则连续选同一张图不会触发 change
    e.target.value = ''
  }
}

// 移除图片：同时清空路径和文件名
function clearImage() {
  form.image = ''
  pickedName.value = ''
}

async function save() {
  // 提交前先本地校验：年份必须 4 位、日期要真实存在。不通过就停在字段下方提示、不发请求
  checkTime()
  if (timeError.value) return

  try {
    // 提交前把输入框的 "2026-10-15T19:00" 还原成接口约定的 "2026-10-15 19:00"
    const payload = { ...form, time: form.time ? form.time.replace('T', ' ') : '' }
    if (form.id) {
      // 编辑：PUT /api/activities/{id}
      await updateActivity(payload)
    } else {
      // 新增：POST /api/activities
      await addActivity(payload)
    }
    tip.value = '保存成功'
    editing.value = false
    await load()
  } catch (e) {
    // 后端明确给出的原因（如「请求参数格式不正确…」）原样显示，其余（断网、5xx）才用兜底文案
    tip.value = errText(e, '保存失败，请稍后再试。')
  }
}

async function remove(item) {
  if (!window.confirm(`确定删除「${item.name}」吗？`)) return
  try {
    await deleteActivity(item.id)
    tip.value = '删除成功'
    await load()
  } catch (e) {
    tip.value = errText(e, '删除失败，请稍后再试。')
  }
}
</script>

<style scoped>
.admin {
  padding-top: 56px;
}

.add-btn {
  margin-bottom: 24px;
}

.tip {
  color: var(--red-text);
  font-size: 14px;
  margin-bottom: 12px;
}

/* 表格：极简线条风 */
.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.table th {
  text-align: left;
  letter-spacing: 2px;
  color: var(--gray-text);
  font-weight: 500;
  padding: 12px 16px;
  border-bottom: 2px solid var(--ink);
}
.table td {
  padding: 14px 16px;
  border-bottom: 1px solid var(--line);
}
.ops-col {
  width: 140px;
}

.tag {
  padding: 3px 10px;
  font-size: 12px;
  color: #fff;
}
.tag.upcoming {
  background: var(--red);
}
.tag.ended {
  background: #8a8a8a;
}

.ops {
  display: flex;
  gap: 8px;
}
.op {
  border: 1px solid var(--line);
  background: #fff;
  padding: 5px 14px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.op.edit:hover {
  border-color: var(--ink);
}
.op.del:hover {
  border-color: var(--red);
  color: var(--red);
}

/* 表单弹层 */
.form-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}
.form {
  background: #fff;
  width: 480px;
  max-width: 92vw;
  max-height: 88vh;
  overflow-y: auto;
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.form-title {
  font-size: 20px;
  letter-spacing: 2px;
  margin-bottom: 4px;
}
.form label,
.form .field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  letter-spacing: 1px;
  color: var(--gray-text);
}
.form input,
.form textarea,
.form select {
  border: 1px solid var(--line);
  padding: 10px 12px;
  font-size: 14px;
  font-family: inherit;
  color: var(--ink);
}
.form input:focus,
.form textarea:focus,
.form select:focus {
  outline: 1px solid var(--red);
  border-color: var(--red);
}
.form-ops {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

/* 图片上传区域 */
/* 原生 file 输入框：只为编程式点击而存在，不显示（它自带的「未选择任何文件」文案无法定制） */
.file-input {
  display: none;
}
.upload-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
/* 自定义上传按钮：文案随状态变化，替代原生那行「未选择任何文件」 */
.btn-upload {
  border: 1px solid var(--line);
  background: #fff;
  color: var(--ink);
  padding: 8px 18px;
  font-size: 13px;
  font-family: inherit;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-upload:hover:not(:disabled) {
  border-color: var(--red);
  color: var(--red-text);
}
.btn-upload:disabled {
  color: var(--gray-text);
  cursor: not-allowed;
}
/* 本次选择的文件名回显 */
.img-name {
  font-size: 13px;
  color: var(--ink);
  letter-spacing: 0;
  word-break: break-all;
}
.img-preview {
  position: relative;
  width: 200px;
}
.img-preview img {
  width: 100%;
  display: block;
  border: 1px solid var(--line);
}
.img-remove {
  position: absolute;
  top: 6px;
  right: 6px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border: none;
  padding: 3px 10px;
  font-size: 12px;
  cursor: pointer;
}
.img-hint {
  font-size: 12px;
  color: var(--gray-text);
  letter-spacing: 0;
}

/* 校验不通过的输入框描红 + 字段下方的错误文案 */
.form input.invalid {
  border-color: var(--red);
  outline: 1px solid var(--red);
}
.field-error {
  color: var(--red-text);
  font-size: 12px;
  letter-spacing: 0;
}
</style>
