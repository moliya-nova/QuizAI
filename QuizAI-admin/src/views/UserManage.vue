<template>
  <div class="user-manage">
    <el-card shadow="never">
      <!-- 搜索区 -->
      <div class="filter-container">
        <el-input v-model="queryParams.keyword" placeholder="搜索用户名或昵称" style="width: 250px" clearable @clear="fetchData" @keyup.enter="fetchData" />
        <el-button type="primary" icon="Search" @click="fetchData" style="margin-left: 10px;">搜索</el-button>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%; margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称">
          <template #default="scope">
            {{ scope.row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button size="small" type="success" @click="showStats(scope.row)">答题统计</el-button>
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

    <!-- 统计图表弹窗 -->
    <el-dialog v-model="statsVisible" :title="`【${currentUser.username}】答题统计`" width="800px" @opened="initChart">
      <div class="stats-overview" style="display: flex; justify-content: space-around; margin-bottom: 30px;">
        <div>累计练习：<el-tag type="info" size="large">{{ statsData.totalPractice || 0 }} 题</el-tag></div>
        <div>累计答对：<el-tag type="success" size="large">{{ statsData.totalCorrect || 0 }} 题</el-tag></div>
        <div>平均正确率：<el-tag type="warning" size="large">{{ statsData.accuracy || 0 }}%</el-tag></div>
      </div>
      <div class="chart-container" style="display: flex; height: 350px;">
        <div ref="lineChartRef" style="flex: 1; height: 100%;"></div>
        <div ref="pieChartRef" style="flex: 1; height: 100%;"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, shallowRef } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import * as echarts from 'echarts'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

const statsVisible = ref(false)
const currentUser = ref({})
const statsData = ref({})
const lineChartRef = ref(null)
const pieChartRef = ref(null)
let lineChart = null
let pieChart = null

const fetchData = () => {
  loading.value = true
  request.get('/admin/user/list', { params: queryParams }).then(res => {
    tableData.value = res.data.records
    total.value = res.data.total
  }).finally(() => {
    loading.value = false
  })
}

const handleStatusChange = (row, val) => {
  request.post('/admin/user/status', { id: row.id, status: val }).then(res => {
    ElMessage.success('状态修改成功')
  }).catch(() => {
    row.status = val === 1 ? 0 : 1 // 回滚
  })
}

const showStats = (row) => {
  currentUser.value = row
  statsData.value = {}
  request.get('/admin/user/stats', { params: { userId: row.id } }).then(res => {
    statsData.value = res.data
    statsVisible.value = true
  })
}

const initChart = () => {
  if (lineChart) lineChart.dispose()
  if (pieChart) pieChart.dispose()

  // 折线图
  lineChart = echarts.init(lineChartRef.value)
  lineChart.setOption({
    title: { text: '近期正确率趋势', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis', formatter: '{b} : {c}%' },
    xAxis: { type: 'category', data: statsData.value.trendLabels || [] },
    yAxis: { type: 'value', max: 100 },
    series: [{
      data: statsData.value.trendData || [],
      type: 'line',
      smooth: true,
      areaStyle: {},
      itemStyle: { color: '#409EFF' }
    }]
  })

  // 饼图
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    title: { text: '各科目练习分布', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b} : {c} ({d}%)' },
    series: [{
      name: '练习数量',
      type: 'pie',
      radius: '50%',
      data: statsData.value.subjectStats || [],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  })
}

onMounted(() => {
  fetchData()
})
</script>
