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
            <span class="tag" :class="item.status">
              {{ item.status === 'upcoming' ? '报名中' : '已结束' }}
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
          <input v-model="form.time" required placeholder="格式：2026-10-15 19:00" />
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
        <label>状态
          <select v-model="form.status">
            <option value="upcoming">报名中</option>
            <option value="ended">已结束</option>
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
import { addActivity, deleteActivity, getActivityList, updateActivity } from '../api/activity'
import { full } from '../utils/date'

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
    tip.value = '加载失败，请确认后端服务已启动。'
  }
})

// 表单状态：editing 控制弹层显隐，form 为当前编辑的数据副本
const editing = ref(false)
const emptyForm = { id: null, name: '', time: '', location: '', summary: '', detail: '', status: 'upcoming' }
const form = reactive({ ...emptyForm })

function openCreate() {
  Object.assign(form, emptyForm)
  editing.value = true
}

function openEdit(item) {
  Object.assign(form, item) // 拷贝一份，避免表单输入直接改到表格
  editing.value = true
}

function cancel() {
  editing.value = false
}

async function save() {
  try {
    if (form.id) {
      // 编辑：PUT /api/activities/{id}
      await updateActivity({ ...form })
    } else {
      // 新增：POST /api/activities（不传图片，后端会用默认占位图）
      await addActivity({ ...form })
    }
    tip.value = '保存成功'
    editing.value = false
    await load()
  } catch (e) {
    tip.value = '保存失败，请稍后再试。'
  }
}

async function remove(item) {
  if (!window.confirm(`确定删除「${item.name}」吗？`)) return
  try {
    await deleteActivity(item.id)
    tip.value = '删除成功'
    await load()
  } catch (e) {
    tip.value = '删除失败，请稍后再试。'
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
.form label {
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
</style>
