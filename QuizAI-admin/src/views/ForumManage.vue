<template>
  <div class="forum-manage">
    <el-card shadow="never">
      <!-- 搜索区 -->
      <div class="filter-container" style="margin-bottom: 20px; display: flex; gap: 10px;">
        <el-select v-model="queryParams.status" placeholder="所有状态" clearable style="width: 150px">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
          <el-option label="已关闭" :value="3" />
        </el-select>
        <el-select v-model="queryParams.type" placeholder="所有类型" clearable style="width: 150px">
          <el-option label="普通帖" :value="0" />
          <el-option label="求助帖" :value="1" />
        </el-select>
        <el-input v-model="queryParams.keyword" placeholder="搜索标题/内容" style="width: 250px" clearable @clear="fetchData" @keyup.enter="fetchData" />
        <el-button type="primary" icon="Search" @click="fetchData">搜索</el-button>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="发帖用户" width="120">
          <template #default="scope">
            {{ scope.row.anonymous ? '匿名用户' : (scope.row.nickname || '未知') }}
            <el-tag v-if="scope.row.anonymous" type="info" size="small" style="margin-left: 4px;">匿名</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column label="类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.type === 1 ? 'warning' : 'info'">
              {{ scope.row.type === 1 ? '求助帖' : '普通帖' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="likeCount" label="点赞" width="80" align="center" />
        <el-table-column prop="commentCount" label="评论" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" align="center">
          <template #default="scope">
            <el-button v-if="scope.row.status === 0" size="small" type="success" @click="handleAudit(scope.row)">审核</el-button>
            <el-button size="small" type="primary" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container" style="margin-top: 20px; text-align: right;">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="queryParams.size"
          v-model:current-page="queryParams.page"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditVisible" title="审核帖子" width="600px">
      <div style="margin-bottom: 16px;">
        <h4 style="margin: 0 0 8px;">{{ auditPost.title }}</h4>
        <p style="color: #606266; margin: 0; white-space: pre-wrap;">{{ auditPost.content }}</p>
      </div>
      <el-form label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="auditForm.status === 2" label="拒绝原因">
          <el-input v-model="auditForm.rejectReason" type="textarea" :rows="2" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看弹窗 -->
    <el-dialog v-model="viewVisible" title="帖子详情" width="600px">
      <div>
        <h3 style="margin: 0 0 12px;">{{ viewPost.title }}</h3>
        <div style="color: #909399; font-size: 13px; margin-bottom: 12px;">
          发帖人: {{ viewPost.anonymous ? '匿名用户' : (viewPost.nickname || '未知') }} | 时间: {{ viewPost.createTime }}
          <el-tag v-if="viewPost.type === 1" type="warning" size="small" style="margin-left: 8px;">求助帖</el-tag>
        </div>
        <div style="color: #303133; white-space: pre-wrap; line-height: 1.8;">{{ viewPost.content }}</div>
        <div v-if="viewPost.rejectReason" style="margin-top: 16px; color: #f56c6c; font-size: 13px;">
          拒绝原因: {{ viewPost.rejectReason }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10,
  status: '',
  type: '',
  keyword: ''
})

const auditVisible = ref(false)
const auditPost = reactive({ title: '', content: '' })
const auditForm = reactive({ id: null, status: 1, rejectReason: '' })

const viewVisible = ref(false)
const viewPost = reactive({})

const statusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已关闭' }
  return map[status] || '未知'
}

const statusTagType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[status] || 'info'
}

const fetchData = () => {
  loading.value = true
  request.get('/admin/forum/post/list', { params: queryParams }).then(res => {
    tableData.value = res.data.records
    total.value = res.data.total
  }).finally(() => {
    loading.value = false
  })
}

const handleAudit = (row) => {
  Object.assign(auditPost, { title: row.title, content: row.content })
  Object.assign(auditForm, { id: row.id, status: 1, rejectReason: '' })
  auditVisible.value = true
}

const submitAudit = () => {
  if (auditForm.status === 2 && !auditForm.rejectReason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  request.post('/admin/forum/post/audit', auditForm).then(res => {
    ElMessage.success('审核成功')
    auditVisible.value = false
    fetchData()
  })
}

const handleView = (row) => {
  Object.assign(viewPost, row)
  viewVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该帖子吗?', '提示', { type: 'warning' }).then(() => {
    request.post('/admin/forum/post/delete', { id: row.id }).then(res => {
      ElMessage.success('删除成功')
      fetchData()
    })
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>
