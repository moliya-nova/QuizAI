<template>
  <div class="category-manage">
    <el-card shadow="never">
      <div class="filter-container" style="margin-bottom: 20px;">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增分类</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="icon" label="图标" width="100" align="center" />
        <el-table-column prop="sort" label="排序权重" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增分类' : '编辑分类'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标(如 emoji)" />
        </el-form-item>
        <el-form-item label="排序权重" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
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

const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const form = reactive({
  id: null,
  name: '',
  icon: '',
  sort: 0
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const fetchData = () => {
  loading.value = true
  request.get('/admin/category/list').then(res => {
    tableData.value = res.data
  }).finally(() => {
    loading.value = false
  })
}

const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(form, { id: null, name: '', icon: '', sort: 0 })
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
      const url = dialogType.value === 'add' ? '/admin/category/add' : '/admin/category/update'
      request.post(url, form).then(res => {
        ElMessage.success(res.message || '操作成功')
        dialogVisible.value = false
        fetchData()
      })
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该分类吗?', '提示', { type: 'warning' }).then(() => {
    request.post('/admin/category/delete', { id: row.id }).then(res => {
      ElMessage.success('删除成功')
      fetchData()
    })
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>
