<template>
  <div class="question-manage">
    <el-card shadow="never">
      <!-- 搜索区 -->
      <div class="filter-container" style="margin-bottom: 20px; display: flex; gap: 10px;">
        <el-select v-model="queryParams.subject" placeholder="所有科目" clearable style="width: 150px">
          <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.name" />
        </el-select>
        <el-input v-model="queryParams.keyword" placeholder="搜索题干内容" style="width: 250px" clearable @clear="fetchData" @keyup.enter="fetchData" />
        <el-button type="primary" icon="Search" @click="fetchData">搜索</el-button>
        <el-button type="success" icon="Plus" @click="handleAdd" style="margin-left: auto;">新增题目</el-button>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="subject" label="科目" width="120" />
        <el-table-column prop="content" label="题干" show-overflow-tooltip />
        <el-table-column prop="correctAnswer" label="正确答案" width="100" align="center">
          <template #default="scope">
            <el-tag type="success">{{ scope.row.correctAnswer }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增题目' : '编辑题目'" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属科目" prop="subject">
          <el-select v-model="form.subject" placeholder="请选择科目" style="width: 100%">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="题干内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题干" />
        </el-form-item>
        <el-form-item label="选项A" prop="optionA">
          <el-input v-model="form.optionA" placeholder="请输入选项A内容" />
        </el-form-item>
        <el-form-item label="选项B" prop="optionB">
          <el-input v-model="form.optionB" placeholder="请输入选项B内容" />
        </el-form-item>
        <el-form-item label="选项C" prop="optionC">
          <el-input v-model="form.optionC" placeholder="请输入选项C内容" />
        </el-form-item>
        <el-form-item label="选项D" prop="optionD">
          <el-input v-model="form.optionD" placeholder="请输入选项D内容" />
        </el-form-item>
        <el-form-item label="正确答案" prop="correctAnswer">
          <el-radio-group v-model="form.correctAnswer">
            <el-radio label="A">A</el-radio>
            <el-radio label="B">B</el-radio>
            <el-radio label="C">C</el-radio>
            <el-radio label="D">D</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="答案解析" prop="analysis">
          <el-input v-model="form.analysis" type="textarea" :rows="3" placeholder="请输入解析" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
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
const categories = ref([])

const queryParams = reactive({
  page: 1,
  size: 10,
  subject: '',
  keyword: ''
})

const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const form = reactive({
  id: null,
  subject: '',
  content: '',
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  correctAnswer: 'A',
  analysis: ''
})

const rules = {
  subject: [{ required: true, message: '请选择科目', trigger: 'change' }],
  content: [{ required: true, message: '请输入题干内容', trigger: 'blur' }],
  optionA: [{ required: true, message: '请输入选项A', trigger: 'blur' }],
  optionB: [{ required: true, message: '请输入选项B', trigger: 'blur' }],
  optionC: [{ required: true, message: '请输入选项C', trigger: 'blur' }],
  optionD: [{ required: true, message: '请输入选项D', trigger: 'blur' }],
  correctAnswer: [{ required: true, message: '请选择正确答案', trigger: 'change' }]
}

const fetchCategories = () => {
  request.get('/admin/category/list').then(res => {
    categories.value = res.data
  })
}

const fetchData = () => {
  loading.value = true
  request.get('/admin/question/list', { params: queryParams }).then(res => {
    tableData.value = res.data.records
    total.value = res.data.total
  }).finally(() => {
    loading.value = false
  })
}

const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(form, { id: null, subject: '', content: '', optionA: '', optionB: '', optionC: '', optionD: '', correctAnswer: 'A', analysis: '' })
  dialogVisible.value = true
  if (formRef.value) formRef.value.clearValidate()
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
  if (formRef.value) formRef.value.clearValidate()
}

const submitForm = () => {
  formRef.value.validate(valid => {
    if (valid) {
      const url = dialogType.value === 'add' ? '/admin/question/add' : '/admin/question/update'
      request.post(url, form).then(res => {
        ElMessage.success(res.message || '操作成功')
        dialogVisible.value = false
        fetchData()
      })
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该题目吗?', '提示', { type: 'warning' }).then(() => {
    request.post('/admin/question/delete', { id: row.id }).then(res => {
      ElMessage.success('删除成功')
      fetchData()
    })
  }).catch(() => {})
}

onMounted(() => {
  fetchCategories()
  fetchData()
})
</script>
