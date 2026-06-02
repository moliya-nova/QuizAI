<template>
  <div class="forum-report-manage">
    <el-card shadow="never">
      <!-- 搜索区 -->
      <div class="filter-container" style="margin-bottom: 20px; display: flex; gap: 10px;">
        <el-select v-model="queryParams.status" placeholder="所有状态" clearable style="width: 150px">
          <el-option label="待处理" :value="0" />
          <el-option label="已驳回" :value="1" />
          <el-option label="已下架" :value="2" />
        </el-select>
        <el-button type="primary" icon="Search" @click="fetchData">搜索</el-button>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="userId" label="举报用户ID" width="120" align="center" />
        <el-table-column prop="postId" label="帖子ID" width="100" align="center" />
        <el-table-column prop="reason" label="举报原因" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="举报时间" width="180" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button v-if="scope.row.status === 0" size="small" type="primary" @click="handleReport(scope.row)">处理</el-button>
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

    <!-- 处理弹窗 -->
    <el-dialog v-model="handleVisible" title="处理举报" width="500px">
      <el-form label-width="80px">
        <el-form-item label="举报原因">
          <span>{{ handleData.reason }}</span>
        </el-form-item>
        <el-form-item label="处理方式">
          <el-radio-group v-model="handleForm.status">
            <el-radio :value="1">驳回举报</el-radio>
            <el-radio :value="2">下架帖子</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="2" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10,
  status: ''
})

const handleVisible = ref(false)
const handleData = reactive({ id: null, reason: '' })
const handleForm = reactive({ id: null, status: 1, handleRemark: '' })

const statusText = (status) => {
  const map = { 0: '待处理', 1: '已驳回', 2: '已下架' }
  return map[status] || '未知'
}

const statusTagType = (status) => {
  const map = { 0: 'warning', 1: 'info', 2: 'danger' }
  return map[status] || 'info'
}

const fetchData = () => {
  loading.value = true
  request.get('/admin/forum/report/list', { params: queryParams }).then(res => {
    tableData.value = res.data.records
    total.value = res.data.total
  }).finally(() => {
    loading.value = false
  })
}

const handleReport = (row) => {
  Object.assign(handleData, { id: row.id, reason: row.reason })
  Object.assign(handleForm, { id: row.id, status: 1, handleRemark: '' })
  handleVisible.value = true
}

const submitHandle = () => {
  request.post('/admin/forum/report/handle', handleForm).then(res => {
    ElMessage.success('处理成功')
    handleVisible.value = false
    fetchData()
  })
}

onMounted(() => {
  fetchData()
})
</script>
