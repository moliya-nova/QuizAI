<template>
  <div class="agent-manage">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="28"><Cpu /></el-icon>
        </div>
        <div class="header-text">
          <h1>Agent 管理</h1>
          <p>AI 服务状态、模型配置与会话管理</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" circle @click="refreshAll" :loading="refreshing" />
      </div>
    </div>

    <div class="content-grid">
      <!-- 第一行：服务状态 & 并发控制 & 当前运行配置 -->
      <div class="row triple-row">
        <!-- 服务状态卡片 -->
        <div class="card status-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><Monitor /></el-icon>
              <span>服务状态</span>
            </div>
            <div class="status-indicator" :class="{ 'is-active': statusData.enabled }">
              <span class="status-dot"></span>
              <span class="status-text">{{ statusData.enabled ? '运行中' : '已停止' }}</span>
            </div>
          </div>
          <div class="card-body">
            <div class="status-visual">
              <div class="status-circle" :class="{ 'is-active': statusData.enabled }">
                <el-icon :size="36"><Cpu /></el-icon>
              </div>
              <div class="status-message">{{ statusData.message }}</div>
            </div>
            <div class="status-switch">
              <span class="switch-label">{{ statusData.enabled ? '关闭服务' : '开启服务' }}</span>
              <el-switch v-model="statusData.enabled" :loading="statusLoading" @change="handleStatusChange" />
            </div>
          </div>
        </div>

        <!-- 并发控制卡片 -->
        <div class="card concurrency-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><Connection /></el-icon>
              <span>并发控制</span>
            </div>
          </div>
          <div class="card-body">
            <div class="concurrency-visual">
              <div class="concurrency-display">
                <div class="concurrency-number">{{ concurrencyData.maxConcurrency }}</div>
                <div class="concurrency-label">最大并发数</div>
              </div>
            </div>
            <div class="concurrency-control">
              <el-input-number
                v-model="concurrencyData.maxConcurrency"
                :min="1" :max="100" size="large"
                @change="handleConcurrencyChange"
              />
              <el-button type="primary" :loading="concurrencyLoading" @click="saveConcurrency" class="save-btn">
                保存设置
              </el-button>
            </div>
          </div>
        </div>

        <!-- 当前运行配置 -->
        <div class="card current-config-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><Setting /></el-icon>
              <span>当前运行配置</span>
              <el-tag v-if="activePresetName" type="success" size="small" effect="plain">{{ activePresetName }}</el-tag>
            </div>
          </div>
          <div class="card-body">
            <div v-if="currentConfig.llm_model" class="config-grid">
              <div class="config-item">
                <div class="config-label">协议</div>
                <div class="config-value">
                  <el-tag :type="getProviderTagType(currentConfig.llm_provider)" size="small">
                    {{ getProviderLabel(currentConfig.llm_provider) }}
                  </el-tag>
                </div>
              </div>
              <div class="config-item">
                <div class="config-label">模型</div>
                <div class="config-value model-name">{{ currentConfig.llm_model }}</div>
              </div>
              <div class="config-item">
                <div class="config-label">API 地址</div>
                <div class="config-value api-url">{{ currentConfig.llm_base_url || '默认' }}</div>
              </div>
              <div class="config-item">
                <div class="config-label">API Key</div>
                <div class="config-value">{{ maskApiKey(currentConfig.llm_api_key) }}</div>
              </div>
            </div>
            <div v-else class="config-empty">
              <el-icon :size="32"><WarningFilled /></el-icon>
              <span>尚未配置任何模型，请从下方已配置模型中启用一个</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 第二行：运行统计 -->
      <div class="card stats-card">
        <div class="card-header">
          <div class="card-title">
            <el-icon class="title-icon"><DataAnalysis /></el-icon>
            <span>运行统计</span>
          </div>
          <el-button text :icon="Refresh" @click="fetchStats" :loading="statsLoading">刷新数据</el-button>
        </div>
        <div class="card-body">
          <div class="stats-grid">
            <div class="stat-item requests">
              <div class="stat-icon"><el-icon :size="28"><Lightning /></el-icon></div>
              <div class="stat-content">
                <div class="stat-value">{{ statsData.totalRequests }}</div>
                <div class="stat-label">累计请求</div>
              </div>
            </div>
            <div class="stat-item connections">
              <div class="stat-icon"><el-icon :size="28"><Connection /></el-icon></div>
              <div class="stat-content">
                <div class="stat-value">{{ statsData.activeConnections }}</div>
                <div class="stat-label">活跃连接</div>
              </div>
            </div>
            <div class="stat-item sessions">
              <div class="stat-icon"><el-icon :size="28"><ChatDotRound /></el-icon></div>
              <div class="stat-content">
                <div class="stat-value">{{ activeThreads.length }}</div>
                <div class="stat-label">活跃会话</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 第三行：模型预设配置 -->
      <div class="card preset-card">
        <div class="card-header">
          <div class="card-title">
            <el-icon class="title-icon"><Box /></el-icon>
            <span>已配置模型</span>
            <el-tag type="info" size="small" effect="plain">{{ allPresets.length }}</el-tag>
          </div>
          <div class="header-right">
            <el-input
              v-model="searchKeyword" placeholder="搜索模型..." clearable size="small"
              class="search-input" :prefix-icon="Search"
            />
            <el-button type="primary" size="small" @click="handleAdd">
              <el-icon><Plus /></el-icon>自定义添加
            </el-button>
          </div>
        </div>
        <div class="card-body">
          <el-table
            :data="filteredPresets" stripe highlight-current-row
            class="models-table" empty-text="暂无已配置模型"
          >
            <el-table-column width="50" align="center">
              <template #default="{ row }">
                <div class="table-icon" :style="getIconStyle(row.icon)">
                  <span class="table-icon-text">{{ getIconLabel(row.icon) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="名称" min-width="140">
              <template #default="{ row }">
                <div class="model-name-cell">
                  <span class="name-text">{{ row.name }}</span>
                  <el-tag v-if="isActivePreset(row)" type="success" size="small" effect="dark" class="running-tag">运行中</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="协议" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getProviderTagType(row.protocol || row.provider)" size="small" effect="plain">
                  {{ getProviderLabel(row.protocol || row.provider) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="模型" min-width="180">
              <template #default="{ row }">
                <span class="model-id-text">{{ getModelName(row) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="API 地址" min-width="200">
              <template #default="{ row }">
                <span class="api-url-text">{{ getBaseUrl(row) || '默认' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" width="240" align="center" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="!isActivePreset(row)" type="primary" size="small" link
                  @click="handleEnable(row)" :loading="enablingId === row.id"
                >
                  <el-icon><Check /></el-icon>启用
                </el-button>
                <el-button v-else type="success" size="small" link disabled>
                  <el-icon><CircleCheckFilled /></el-icon>当前使用
                </el-button>
                <el-button type="primary" size="small" link @click="openEditDialog(row)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button type="danger" size="small" link @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 第四行：会话记忆管理 -->
      <div class="card sessions-card">
        <div class="card-header">
          <div class="card-title">
            <el-icon class="title-icon"><User /></el-icon>
            <span>会话记忆管理</span>
            <el-badge :value="activeThreads.length" class="thread-badge" />
          </div>
          <el-button type="danger" text :loading="clearLoading" @click="handleClearAll" :disabled="activeThreads.length === 0">
            清除全部
          </el-button>
        </div>
        <div class="card-body">
          <div v-if="activeThreads.length === 0" class="empty-sessions">
            <el-icon :size="48"><ChatDotRound /></el-icon>
            <p>暂无活跃会话</p>
          </div>
          <div v-else class="sessions-list">
            <div v-for="thread in activeThreads" :key="thread.thread_id" class="session-item">
              <div class="session-info">
                <div class="session-avatar"><el-icon><User /></el-icon></div>
                <div class="session-details">
                  <div class="session-user">{{ thread.username }}</div>
                  <div class="session-id">{{ thread.thread_id }}</div>
                </div>
              </div>
              <div class="session-meta">
                <el-tag type="success" size="small" effect="plain">活跃</el-tag>
                <span class="session-expires">剩余 {{ formatExpires(thread.expires_in) }}</span>
              </div>
              <el-button type="danger" text size="small" :loading="deletingThread === thread.thread_id"
                @click="handleDeleteThread(thread.thread_id)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="520px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" class="edit-form">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="模型显示名称" />
        </el-form-item>
        <el-form-item label="协议" prop="protocol">
          <el-radio-group v-model="form.protocol" @change="onProtocolChange">
            <el-radio-button value="openai">OpenAI</el-radio-button>
            <el-radio-button value="anthropic">Anthropic</el-radio-button>
            <el-radio-button value="ollama">Ollama</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <template v-if="form.protocol === 'openai'">
          <el-form-item label="模型" prop="openai_model">
            <el-input v-model="form.openai_model" placeholder="如: gpt-4o, deepseek-chat" />
          </el-form-item>
          <el-form-item label="API 地址" prop="openai_base_url">
            <el-input v-model="form.openai_base_url" placeholder="如: https://api.openai.com/v1" />
          </el-form-item>
        </template>
        <template v-if="form.protocol === 'anthropic'">
          <el-form-item label="模型" prop="anthropic_model">
            <el-input v-model="form.anthropic_model" placeholder="如: claude-sonnet-4-20250514" />
          </el-form-item>
          <el-form-item label="API 地址" prop="anthropic_base_url">
            <el-input v-model="form.anthropic_base_url" placeholder="留空使用官方地址" />
          </el-form-item>
        </template>
        <template v-if="form.protocol === 'ollama'">
          <el-form-item label="模型" prop="ollama_model">
            <el-input v-model="form.ollama_model" placeholder="如: qwen2.5:7b" />
          </el-form-item>
          <el-form-item label="API 地址" prop="ollama_base_url">
            <el-input v-model="form.ollama_base_url" placeholder="http://localhost:11434" />
          </el-form-item>
        </template>
        <el-form-item label="API Key" prop="api_key">
          <el-input v-model="form.api_key" type="password" show-password placeholder="该模型的 API Key" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">
            {{ isEdit ? '保存修改' : '添加配置' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Cpu, Monitor, Connection, DataAnalysis, Lightning, ChatDotRound,
  Refresh, User, Setting, Box, Plus, Delete, Check, Edit, WarningFilled,
  CircleCheckFilled, Search
} from '@element-plus/icons-vue'
import {
  getAiStatus, toggleAiStatus, getConcurrency, setConcurrency,
  getAiStats, getActiveThreads, clearAllMemory, deleteThread,
  getAiConfig, updateAiConfig,
  listModelPreset, addModelPreset, updateModelPreset, delModelPreset
} from '@/api/agent/manage'

// ── 加载状态 ──
const refreshing = ref(false)
const statusLoading = ref(false)
const concurrencyLoading = ref(false)
const statsLoading = ref(false)
const clearLoading = ref(false)
const submitLoading = ref(false)
const deletingThread = ref(null)
const enablingId = ref(null)

// ── 数据 ──
const statusData = reactive({
  enabled: false, maxConcurrency: 10, activeConnections: 0, totalRequests: 0, message: '加载中...'
})
const concurrencyData = reactive({ maxConcurrency: 10 })
const statsData = reactive({
  enabled: false, totalRequests: 0, activeConnections: 0, maxConcurrency: 10, activeThreads: []
})
const activeThreads = ref([])
const allPresets = ref([])
const searchKeyword = ref('')
const currentConfig = ref({ llm_provider: '', llm_model: '', llm_base_url: '', llm_api_key: '' })

const activePresetName = computed(() => {
  if (!currentConfig.value.llm_model) return ''
  const active = allPresets.value.find(p => isActivePreset(p))
  return active?.name || ''
})

const filteredPresets = computed(() => {
  if (!searchKeyword.value) return allPresets.value
  const keyword = searchKeyword.value.toLowerCase()
  return allPresets.value.filter(p =>
    p.name.toLowerCase().includes(keyword) ||
    p.remark?.toLowerCase().includes(keyword) ||
    getModelName(p)?.toLowerCase().includes(keyword)
  )
})

// ── 对话框 ──
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const isEdit = ref(false)

const defaultForm = () => ({
  id: 0, name: '', provider: 'openai', protocol: 'openai',
  openai_model: '', openai_base_url: '',
  anthropic_model: '', anthropic_base_url: '',
  ollama_model: '', ollama_base_url: '',
  api_key: '', icon: 'custom', sort_order: 100, status: '0', remark: ''
})
const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  protocol: [{ required: true, message: '请选择协议', trigger: 'change' }],
  api_key: [{ required: true, message: '请输入 API Key', trigger: 'blur' }]
}

// ── 工具方法 ──
const iconStyleMap = {
  openai: { background: 'linear-gradient(135deg, #10b981 0%, #34d399 100%)' },
  anthropic: { background: 'linear-gradient(135deg, #f97316 0%, #fb923c 100%)' },
  deepseek: { background: 'linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%)' },
  qwen: { background: 'linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%)' },
  zhipu: { background: 'linear-gradient(135deg, #ef4444 0%, #f87171 100%)' },
  kimi: { background: 'linear-gradient(135deg, #06b6d4 0%, #22d3ee 100%)' },
  baidu: { background: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)' },
  doubao: { background: 'linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%)' },
  spark: { background: 'linear-gradient(135deg, #ec4899 0%, #f472b6 100%)' },
  ollama: { background: 'linear-gradient(135deg, #6b7280 0%, #9ca3af 100%)' },
  custom: { background: 'linear-gradient(135deg, #a855f7 0%, #c084fc 100%)' }
}

const iconLabelMap = {
  openai: 'AI', anthropic: 'An', deepseek: 'DS', qwen: 'Qw', zhipu: 'GL',
  kimi: 'Ki', baidu: 'Er', doubao: 'Db', spark: 'Sp', ollama: 'Ol', custom: 'Cu'
}

const getIconStyle = (icon) => iconStyleMap[icon] || iconStyleMap.custom
const getIconLabel = (icon) => iconLabelMap[icon] || icon?.substring(0, 2)?.toUpperCase() || 'AI'

const getProviderTagType = (provider) => {
  const map = { openai: 'success', anthropic: 'warning', ollama: 'info' }
  return map[provider] || 'info'
}

const getProviderLabel = (provider) => {
  const map = { openai: 'OpenAI', anthropic: 'Anthropic', ollama: 'Ollama' }
  return map[provider] || provider || '未知'
}

const getModelName = (preset) => {
  const protocol = preset.protocol || preset.provider
  if (protocol === 'openai') return preset.openai_model
  if (protocol === 'anthropic') return preset.anthropic_model
  if (protocol === 'ollama') return preset.ollama_model
  return ''
}

const getBaseUrl = (preset) => {
  const protocol = preset.protocol || preset.provider
  if (protocol === 'openai') return preset.openai_base_url
  if (protocol === 'anthropic') return preset.anthropic_base_url
  if (protocol === 'ollama') return preset.ollama_base_url
  return ''
}

const maskApiKey = (key) => {
  if (!key) return '未配置'
  if (key.length <= 8) return '***'
  return key.substring(0, 4) + '****' + key.substring(key.length - 4)
}

const isActivePreset = (preset) => {
  const protocol = preset.protocol || preset.provider
  const model = getModelName(preset)
  return currentConfig.value.llm_provider === protocol && currentConfig.value.llm_model === model
}

const formatExpires = (seconds) => {
  if (seconds < 60) return `${seconds}秒`
  if (seconds < 3600) return `${Math.floor(seconds / 60)}分钟`
  return `${Math.floor(seconds / 3600)}小时`
}

// ── 数据获取 ──
const fetchStatus = async (silent = false) => {
  try {
    const res = await getAiStatus(silent)
    Object.assign(statusData, res.data)
  } catch (e) { if (!silent) console.error('获取状态失败:', e) }
}

const fetchConcurrency = async () => {
  try {
    const res = await getConcurrency()
    concurrencyData.maxConcurrency = res.data?.maxConcurrency || 10
  } catch (e) { console.error('获取并发失败:', e) }
}

const fetchStats = async (silent = false) => {
  if (!silent) statsLoading.value = true
  try {
    const res = await getAiStats(silent)
    Object.assign(statsData, res.data)
  } catch (e) { if (!silent) console.error('获取统计失败:', e) }
  finally { if (!silent) statsLoading.value = false }
}

const fetchThreads = async (silent = false) => {
  try {
    const res = await getActiveThreads(silent)
    const data = res.data
    activeThreads.value = Array.isArray(data) ? data : []
  } catch (e) { /* 静默 */ }
}

const fetchPresets = async () => {
  try {
    const res = await listModelPreset({})
    allPresets.value = res.data?.records || []
  } catch (e) { console.error('获取预设列表失败:', e) }
}

const fetchCurrentConfig = async () => {
  try {
    const res = await getAiConfig()
    const data = res.data
    if (data && typeof data === 'object') {
      currentConfig.value = data
    }
  } catch (e) { console.error('获取当前配置失败:', e) }
}

const refreshAll = async () => {
  refreshing.value = true
  await Promise.all([fetchStatus(), fetchConcurrency(), fetchStats(), fetchThreads(), fetchPresets(), fetchCurrentConfig()])
  refreshing.value = false
}

// ── 操作 ──
const handleStatusChange = async (val) => {
  statusLoading.value = true
  try {
    await toggleAiStatus(val)
    ElMessage.success(val ? 'AI 服务已开启' : 'AI 服务已关闭')
    await fetchStatus()
  } catch (e) {
    statusData.enabled = !val
    ElMessage.error('操作失败')
  } finally { statusLoading.value = false }
}

const handleConcurrencyChange = (val) => {
  if (val) concurrencyData.maxConcurrency = val
}

const saveConcurrency = async () => {
  concurrencyLoading.value = true
  try {
    await setConcurrency({ maxConcurrency: concurrencyData.maxConcurrency })
    ElMessage.success('并发设置已保存')
  } catch (e) { ElMessage.error('保存失败') }
  finally { concurrencyLoading.value = false }
}

const handleEnable = async (preset) => {
  const protocol = preset.protocol || preset.provider
  const model = getModelName(preset)
  const baseUrl = getBaseUrl(preset)

  if (!preset.api_key && protocol !== 'ollama') {
    ElMessage.warning('该模型未配置 API Key，请先编辑补充')
    return
  }
  if (!baseUrl && protocol !== 'ollama') {
    ElMessage.warning('该模型未配置 API 地址，请先编辑补充')
    return
  }

  await ElMessageBox.confirm(
    `确定要启用模型 "${preset.name}" (${model}) 作为当前运行配置吗？`,
    '启用模型',
    { type: 'info', confirmButtonText: '确认启用', cancelButtonText: '取消' }
  )

  enablingId.value = preset.id
  try {
    await updateAiConfig({
      llm_provider: protocol,
      llm_model: model,
      llm_base_url: baseUrl || '',
      llm_api_key: preset.api_key || ''
    })
    ElMessage.success(`模型 "${preset.name}" 已启用`)
    await fetchCurrentConfig()
  } catch (e) {
    ElMessage.error('启用模型失败')
  } finally { enablingId.value = null }
}

const handleDelete = async (preset) => {
  await ElMessageBox.confirm(
    `确定要删除模型配置 "${preset.name}" 吗？`,
    '删除确认',
    { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
  )
  try {
    await delModelPreset(String(preset.id))
    ElMessage.success('删除成功')
    await fetchPresets()
  } catch (e) { ElMessage.error('删除失败') }
}

const openEditDialog = (preset) => {
  Object.assign(form, { ...preset })
  if (!form.protocol) form.protocol = form.provider
  dialogTitle.value = '编辑模型配置'
  isEdit.value = !!preset.id
  dialogVisible.value = true
}

const handleAdd = () => {
  Object.assign(form, defaultForm())
  dialogTitle.value = '自定义添加模型'
  isEdit.value = false
  dialogVisible.value = true
}

const onProtocolChange = (protocol) => {
  form.provider = protocol
}

const submitForm = async () => {
  try { await formRef.value?.validate() } catch { return }
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateModelPreset({ ...form })
      ElMessage.success('保存成功')
    } else {
      await addModelPreset({ ...form })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    await fetchPresets()
  } catch (e) { ElMessage.error(isEdit.value ? '保存失败' : '添加失败') }
  finally { submitLoading.value = false }
}

const handleDeleteThread = async (threadId) => {
  await ElMessageBox.confirm('确定删除此会话？', '确认', { type: 'warning' })
  deletingThread.value = threadId
  try {
    await deleteThread(threadId)
    ElMessage.success('会话已删除')
    await fetchThreads()
  } catch (e) { ElMessage.error('删除失败') }
  finally { deletingThread.value = null }
}

const handleClearAll = async () => {
  await ElMessageBox.confirm(
    '确定清除所有会话记忆？此操作不可恢复。',
    '确认清除',
    { type: 'error', confirmButtonText: '清除全部' }
  )
  clearLoading.value = true
  try {
    await clearAllMemory()
    ElMessage.success('所有会话记忆已清除')
    await fetchThreads()
  } catch (e) { ElMessage.error('清除失败') }
  finally { clearLoading.value = false }
}

// ── 定时器 ──
let refreshTimer = null

onMounted(async () => {
  await refreshAll()
  refreshTimer = setInterval(() => {
    fetchStatus(true)
    fetchStats(true)
    fetchThreads(true)
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped lang="scss">
.agent-manage {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px; padding: 16px 24px; background: #ffffff;
  border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  .header-content { display: flex; align-items: center; gap: 12px; }
  .header-icon {
    width: 44px; height: 44px; display: flex; align-items: center; justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px; color: #ffffff; box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  }
  .header-text h1 { margin: 0; font-size: 18px; font-weight: 600; color: #1a1a2e; }
  .header-text p { margin: 2px 0 0; font-size: 13px; color: #6b7280; }
}

.content-grid { display: flex; flex-direction: column; gap: 16px; }

.row {
  display: grid; gap: 16px;
  &.triple-row { grid-template-columns: repeat(3, 1fr); }
  @media (max-width: 1200px) { &.triple-row { grid-template-columns: repeat(2, 1fr); } }
  @media (max-width: 768px) { &.triple-row { grid-template-columns: 1fr; } }
}

.card {
  background: #ffffff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden; transition: all 0.3s ease;
  &:hover { box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1); }
  .card-header {
    display: flex; justify-content: space-between; align-items: center;
    padding: 12px 16px; border-bottom: 1px solid #f0f0f0;
  }
  .card-title {
    display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #1a1a2e;
    .title-icon { font-size: 16px; }
  }
  .card-body { padding: 16px; }
}

// 服务状态
.status-card {
  .card-title .title-icon { color: #3b82f6; }
  .status-indicator {
    display: flex; align-items: center; gap: 8px; padding: 6px 16px; border-radius: 20px;
    background: #f3f4f6; font-size: 13px; color: #6b7280; transition: all 0.3s ease;
    &.is-active { background: #dbeafe; color: #3b82f6; .status-dot { background: #3b82f6; box-shadow: 0 0 12px rgba(59,130,246,0.5); } }
    .status-dot { width: 8px; height: 8px; border-radius: 50%; background: #ef4444; transition: all 0.3s ease; }
  }
  .status-visual { display: flex; flex-direction: column; align-items: center; padding: 12px 0; }
  .status-circle {
    width: 72px; height: 72px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
    background: #f3f4f6; border: 3px solid #e5e7eb; color: #9ca3af; transition: all 0.5s ease;
    &.is-active { background: #dbeafe; border-color: #3b82f6; color: #3b82f6; box-shadow: 0 0 40px rgba(59,130,246,0.2); animation: pulse-blue 2s infinite; }
  }
  .status-message { margin-top: 10px; font-size: 14px; color: #6b7280; }
  .status-switch { display: flex; justify-content: space-between; align-items: center; padding: 16px; background: #f9fafb; border-radius: 12px; }
  .switch-label { font-size: 14px; color: #4b5563; }
}

@keyframes pulse-blue {
  0%, 100% { box-shadow: 0 0 40px rgba(59,130,246,0.2); }
  50% { box-shadow: 0 0 60px rgba(59,130,246,0.4); }
}

// 并发控制
.concurrency-card {
  .card-title .title-icon { color: #8b5cf6; }
  .concurrency-visual { display: flex; justify-content: center; padding: 12px 0; }
  .concurrency-display { text-align: center; }
  .concurrency-number { font-size: 40px; font-weight: 700; background: linear-gradient(135deg, #8b5cf6, #a78bfa); -webkit-background-clip: text; -webkit-text-fill-color: transparent; line-height: 1; }
  .concurrency-label { margin-top: 8px; font-size: 14px; color: #6b7280; }
  .concurrency-control { display: flex; gap: 12px; :deep(.el-input-number) { flex: 1; } }
  .save-btn { background: linear-gradient(135deg, #8b5cf6, #a78bfa); border: none; font-weight: 600; &:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(139,92,246,0.3); } }
}

// 当前配置
.current-config-card {
  .card-title .title-icon { color: #10b981; }
  .config-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .config-item { padding: 10px 12px; background: #f8fafc; border-radius: 8px; border: 1px solid #e2e8f0; }
  .config-label { font-size: 11px; color: #64748b; margin-bottom: 4px; }
  .config-value { font-size: 13px; color: #1e293b; font-weight: 500; &.model-name { font-family: 'SF Mono', Monaco, monospace; color: #0ea5e9; font-size: 12px; } &.api-url { font-size: 11px; color: #64748b; font-family: 'SF Mono', Monaco, monospace; word-break: break-all; } }
  .config-empty { display: flex; align-items: center; justify-content: center; gap: 8px; padding: 16px; color: #f59e0b; background: #fffbeb; border-radius: 8px; border: 1px solid #fde68a; font-size: 13px; }
}

// 统计
.stats-card {
  .card-title .title-icon { color: #f59e0b; }
  .stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
  @media (max-width: 768px) { .stats-grid { grid-template-columns: 1fr; } }
  .stat-item {
    display: flex; align-items: center; gap: 16px; padding: 24px; border-radius: 16px;
    transition: all 0.3s ease; &:hover { transform: translateY(-4px); }
    &.requests { background: linear-gradient(135deg, #fef3c7, #fde68a); .stat-icon { background: #f59e0b; color: #fff; } }
    &.connections { background: linear-gradient(135deg, #dbeafe, #bfdbfe); .stat-icon { background: #3b82f6; color: #fff; } }
    &.sessions { background: linear-gradient(135deg, #d1fae5, #a7f3d0); .stat-icon { background: #10b981; color: #fff; } }
  }
  .stat-icon { width: 56px; height: 56px; display: flex; align-items: center; justify-content: center; border-radius: 14px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
  .stat-value { font-size: 32px; font-weight: 700; color: #1a1a2e; line-height: 1; }
  .stat-label { margin-top: 4px; font-size: 13px; color: #6b7280; }
}

// 预设表格
.preset-card {
  .card-title .title-icon { color: #667eea; }
  .header-right { display: flex; align-items: center; gap: 8px; }
  .search-input { width: 180px; }
  .models-table {
    :deep(.el-table__header th) { background: #f8fafc; color: #475569; font-weight: 600; font-size: 13px; }
    :deep(.el-table__row) { cursor: default; }
  }
  .table-icon { width: 32px; height: 32px; border-radius: 6px; display: flex; align-items: center; justify-content: center; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
  .table-icon-text { font-size: 11px; font-weight: 700; color: #ffffff; letter-spacing: 0.5px; }
  .model-name-cell { display: flex; align-items: center; gap: 6px; }
  .name-text { font-weight: 600; color: #1a1a2e; font-size: 13px; }
  .running-tag { flex-shrink: 0; }
  .model-id-text { font-family: 'SF Mono', Monaco, monospace; font-size: 12px; color: #0ea5e9; }
  .api-url-text { font-family: 'SF Mono', Monaco, monospace; font-size: 11px; color: #64748b; word-break: break-all; }
}

// 会话管理
.sessions-card {
  .card-title .title-icon { color: #06b6d4; }
  .thread-badge { margin-left: 8px; :deep(.el-badge__content) { background: #06b6d4; } }
  .empty-sessions { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 48px; color: #d1d5db; p { margin-top: 16px; font-size: 14px; color: #9ca3af; } }
  .sessions-list { display: flex; flex-direction: column; gap: 12px; max-height: 400px; overflow-y: auto; }
  .session-item {
    display: flex; align-items: center; justify-content: space-between; padding: 16px;
    background: #f9fafb; border-radius: 12px; transition: all 0.3s ease;
    &:hover { background: #f0f9ff; transform: translateX(4px); }
  }
  .session-info { display: flex; align-items: center; gap: 12px; }
  .session-avatar { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #06b6d4, #22d3ee); border-radius: 10px; color: #ffffff; font-size: 18px; }
  .session-details { display: flex; flex-direction: column; gap: 2px; }
  .session-user { font-weight: 600; color: #1a1a2e; font-size: 14px; }
  .session-id { font-family: 'SF Mono', Monaco, monospace; font-size: 11px; color: #9ca3af; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .session-meta { display: flex; align-items: center; gap: 12px; }
  .session-expires { font-size: 12px; color: #6b7280; }
}

.dialog-footer { display: flex; justify-content: flex-end; gap: 8px; }
</style>
