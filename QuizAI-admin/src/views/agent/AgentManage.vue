<template>
  <div class="agent-manage">
    <!-- 页面标题 -->
    <div class="page-header animate-slide-down" style="--delay: 0">
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
      <!-- 第一行：服务管理 & 运行统计 & 当前运行配置 -->
      <div class="row triple-row">
        <!-- 服务管理卡片（合并：服务状态 + 并发控制 + RAG向量库重建） -->
        <div class="card service-mgmt-card animate-scale-up" style="--delay: 1">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><Monitor /></el-icon>
              <span>服务管理</span>
            </div>
            <div class="status-indicator" :class="{ 'is-active': statusData.enabled }">
              <span class="status-dot"></span>
              <span class="status-text">{{ statusData.enabled ? '运行中' : '已停止' }}</span>
            </div>
          </div>
          <div class="card-body service-mgmt-body">
            <!-- 1. 服务开关 -->
            <div class="mgmt-section animate-fade-in" style="--delay: 0">
              <div class="mgmt-section-left">
                <div class="mgmt-icon-wrap service-icon">
                  <el-icon :size="18"><Cpu /></el-icon>
                </div>
                <div class="mgmt-text-group">
                  <span class="mgmt-section-title">服务开关</span>
                  <span class="mgmt-section-desc">控制 AI 服务的启停状态</span>
                </div>
              </div>
              <el-switch v-model="statusData.enabled" :loading="statusLoading" @change="handleStatusChange" active-color="#10b981" inactive-color="#d1d5db" />
            </div>
            <!-- 2. 并发控制 -->
            <div class="mgmt-section animate-fade-in" style="--delay: 1">
              <div class="mgmt-section-left">
                <div class="mgmt-icon-wrap concurrency-icon">
                  <el-icon :size="18"><Connection /></el-icon>
                </div>
                <div class="mgmt-text-group">
                  <span class="mgmt-section-title">并发控制</span>
                  <span class="mgmt-section-desc">最大并发数 <b>{{ concurrencyData.maxConcurrency }}</b></span>
                </div>
              </div>
              <div class="mgmt-section-right">
                <el-input-number
                  v-model="concurrencyData.maxConcurrency"
                  :min="1" :max="100" size="small"
                  @change="handleConcurrencyChange"
                />
                <el-button class="mgmt-btn save-btn" size="small" :loading="concurrencyLoading" @click="saveConcurrency">
                  <el-icon><Check /></el-icon>保存
                </el-button>
              </div>
            </div>
            <!-- 3. RAG 向量库重建 -->
            <div class="mgmt-section animate-fade-in" style="--delay: 2">
              <div class="mgmt-section-left">
                <div class="mgmt-icon-wrap rag-icon">
                  <el-icon :size="18"><FolderOpened /></el-icon>
                </div>
                <div class="mgmt-text-group">
                  <span class="mgmt-section-title">RAG 向量库重建</span>
                  <span class="mgmt-section-desc">重新生成知识库向量索引</span>
                </div>
              </div>
              <el-button class="mgmt-btn rag-btn" size="small" :loading="ragLoading" @click="handleRagRebuild">
                <el-icon><RefreshRight /></el-icon>重建索引
              </el-button>
            </div>
          </div>
        </div>

        <!-- 运行统计卡片（横向三列） -->
        <div class="card stats-card animate-scale-up" style="--delay: 2">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><DataAnalysis /></el-icon>
              <span>运行统计</span>
            </div>
          </div>
          <div class="card-body">
            <div class="stats-horizontal">
              <div class="stat-item requests animate-bounce-in" style="--delay: 0">
                <div class="stat-glow requests-glow"></div>
                <div class="stat-icon-wrap">
                  <el-icon :size="24"><Lightning /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ statsData.totalRequests }}</div>
                  <div class="stat-label">累计请求</div>
                </div>
                <div class="stat-trend">
                  <svg viewBox="0 0 48 24" class="trend-line">
                    <polyline points="0,20 8,16 16,18 24,10 32,14 40,6 48,8" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
              </div>
              <div class="stat-item connections animate-bounce-in" style="--delay: 1">
                <div class="stat-glow connections-glow"></div>
                <div class="stat-icon-wrap">
                  <el-icon :size="24"><Connection /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ statsData.activeConnections }}</div>
                  <div class="stat-label">活跃连接</div>
                </div>
                <div class="stat-trend">
                  <svg viewBox="0 0 48 24" class="trend-line">
                    <polyline points="0,12 8,14 16,10 24,12 32,8 40,10 48,6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
              </div>
              <div class="stat-item sessions animate-bounce-in" style="--delay: 2">
                <div class="stat-glow sessions-glow"></div>
                <div class="stat-icon-wrap">
                  <el-icon :size="24"><ChatDotRound /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ activeThreads.length }}</div>
                  <div class="stat-label">活跃会话</div>
                </div>
                <div class="stat-trend">
                  <svg viewBox="0 0 48 24" class="trend-line">
                    <polyline points="0,18 8,14 16,16 24,12 32,10 40,8 48,4" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
              </div>
            </div>
            <div class="stats-footer">
              <el-button text :icon="Refresh" @click="fetchStats" :loading="statsLoading" size="small">刷新数据</el-button>
            </div>
          </div>
        </div>

        <!-- 当前运行配置 -->
        <div class="card current-config-card animate-scale-up" style="--delay: 3">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><Setting /></el-icon>
              <span>当前运行配置</span>
              <el-tag v-if="activePresetName" type="success" size="small" effect="plain">{{ activePresetName }}</el-tag>
            </div>
          </div>
          <div class="card-body">
            <div v-if="currentConfig.llm_model" class="config-showcase animate-fade-in" style="--delay: 0">
              <div class="config-main">
                <div class="config-model-display">
                  <ModelIcon :icon="getModelIcon(currentConfig)" :size="48" />
                  <div class="config-model-info">
                    <div class="config-model-name">{{ currentConfig.llm_model }}</div>
                    <el-tag :type="getProviderTagType(currentConfig.llm_provider)" size="small" effect="dark">
                      {{ getProviderLabel(currentConfig.llm_provider) }}
                    </el-tag>
                  </div>
                </div>
              </div>
              <div class="config-details">
                <div class="config-detail-item">
                  <div class="detail-icon">
                    <el-icon><Link /></el-icon>
                  </div>
                  <div class="detail-content">
                    <div class="detail-label">API 地址</div>
                    <div class="detail-value">{{ currentConfig.llm_base_url || '默认' }}</div>
                  </div>
                </div>
                <div class="config-detail-item">
                  <div class="detail-icon">
                    <el-icon><Lock /></el-icon>
                  </div>
                  <div class="detail-content">
                    <div class="detail-label">API Key</div>
                    <div class="detail-value">{{ maskApiKey(currentConfig.llm_api_key) }}</div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="config-empty">
              <div class="empty-icon">
                <el-icon :size="40"><WarningFilled /></el-icon>
              </div>
              <div class="empty-text">
                <div class="empty-title">尚未配置模型</div>
                <div class="empty-desc">请从下方已配置模型中启用一个</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 第二行：模型预设配置 -->
      <div class="card preset-card animate-slide-up" style="--delay: 4">
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
            <el-table-column width="60" align="center">
              <template #default="{ row }">
                <div class="table-icon-wrap">
                  <ModelIcon :icon="row.icon" :size="36" />
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

      <!-- 第三行：会话记忆管理 -->
      <div class="card sessions-card animate-slide-up" style="--delay: 5">
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
            <div v-for="(thread, index) in activeThreads" :key="thread.thread_id" class="session-item animate-slide-right" :style="{ '--delay': index }">
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
  CircleCheckFilled, Search, FolderOpened, RefreshRight, Link, Lock
} from '@element-plus/icons-vue'
import ModelIcon from '@/components/ModelIcon.vue'
import {
  getAiStatus, toggleAiStatus, getConcurrency, setConcurrency,
  getAiStats, getActiveThreads, clearAllMemory, deleteThread,
  getAiConfig, updateAiConfig,
  listModelPreset, addModelPreset, updateModelPreset, delModelPreset,
  rebuildRagIndex
} from '@/api/agent/manage'

// ── 加载状态 ──
const refreshing = ref(false)
const statusLoading = ref(false)
const concurrencyLoading = ref(false)
const statsLoading = ref(false)
const clearLoading = ref(false)
const ragLoading = ref(false)
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

const getModelIcon = (config) => {
  if (!config) return 'custom'
  const model = (config.llm_model || '').toLowerCase()
  if (model.includes('gpt') || model.includes('o1') || model.includes('o3')) return 'openai'
  if (model.includes('claude')) return 'anthropic'
  if (model.includes('deepseek')) return 'deepseek'
  if (model.includes('qwen') || model.includes('qwq')) return 'qwen'
  if (model.includes('glm') || model.includes('chatglm')) return 'zhipu'
  if (model.includes('kimi') || model.includes('moonshot')) return 'kimi'
  if (model.includes('ernie') || model.includes('wenxin')) return 'baidu'
  if (model.includes('doubao') || model.includes('bytedance')) return 'doubao'
  if (model.includes('spark') || model.includes('xunfei')) return 'spark'
  if (config.llm_provider === 'ollama') return 'ollama'
  return 'custom'
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

const handleRagRebuild = async () => {
  await ElMessageBox.confirm(
    '确定要重建 RAG 向量库索引吗？该操作会重新生成所有知识库向量，数据量大时耗时较长。',
    'RAG 向量库重建',
    { type: 'warning', confirmButtonText: '确认重建', cancelButtonText: '取消' }
  )
  ragLoading.value = true
  try {
    await rebuildRagIndex()
    ElMessage.success('RAG 向量库重建成功')
  } catch (e) {
    ElMessage.error('重建失败，请稍后重试')
  } finally { ragLoading.value = false }
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

// 服务管理 - 精致卡片区块
.service-mgmt-card {
  .card-title .title-icon { color: #3b82f6; }
  .card-header {
    min-height: 43px; // 与其他卡片标题区域对齐
  }
  .service-mgmt-body { display: flex; flex-direction: column; gap: 12px; }
  .mgmt-section {
    display: flex; align-items: center; justify-content: space-between;
    padding: 14px 18px; background: linear-gradient(135deg, #fafbfc 0%, #f8fafc 100%);
    border-radius: 12px; border: 1px solid #e8ecf1;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    &:hover {
      border-color: #c7d2fe; background: #ffffff;
      box-shadow: 0 4px 16px rgba(99, 102, 241, 0.06);
      transform: translateY(-1px);
    }
  }
  .mgmt-section-left {
    display: flex; align-items: center; gap: 14px;
  }
  .mgmt-icon-wrap {
    width: 40px; height: 40px; border-radius: 10px;
    display: flex; align-items: center; justify-content: center;
    flex-shrink: 0; transition: all 0.3s ease;
    &.service-icon {
      background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
      color: #3b82f6; border: 1px solid #bfdbfe;
    }
    &.concurrency-icon {
      background: linear-gradient(135deg, #ede9fe 0%, #f5f3ff 100%);
      color: #7c3aed; border: 1px solid #ddd6fe;
    }
    &.rag-icon {
      background: linear-gradient(135deg, #fef3c7 0%, #fffbeb 100%);
      color: #d97706; border: 1px solid #fde68a;
    }
  }
  .mgmt-text-group {
    display: flex; flex-direction: column; gap: 2px;
  }
  .mgmt-section-title {
    font-size: 13px; font-weight: 600; color: #1e293b; letter-spacing: 0.01em;
  }
  .mgmt-section-desc {
    font-size: 11px; color: #94a3b8; font-weight: 400;
    b { color: #6366f1; font-weight: 700; }
  }
  .mgmt-section-right {
    display: flex; align-items: center; gap: 10px;
  }
  .mgmt-btn {
    border-radius: 8px; font-weight: 600; font-size: 12px;
    transition: all 0.25s ease; letter-spacing: 0.02em;
    &.save-btn {
      background: linear-gradient(135deg, #6366f1 0%, #818cf8 100%);
      border: none; color: #fff;
      box-shadow: 0 2px 8px rgba(99, 102, 241, 0.25);
      &:hover { transform: translateY(-1px); box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35); }
    }
    &.rag-btn {
      background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
      border: none; color: #fff;
      box-shadow: 0 2px 8px rgba(245, 158, 11, 0.25);
      &:hover { transform: translateY(-1px); box-shadow: 0 4px 14px rgba(245, 158, 11, 0.35); }
    }
  }
}

// 当前配置 - 展示型布局
.current-config-card {
  .card-title .title-icon { color: #10b981; }
  .card-header {
    min-height: 43px; // 与其他卡片标题区域对齐
  }
  .config-showcase {
    display: flex; flex-direction: column; gap: 16px;
  }
  .config-main {
    padding: 20px; background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
    border-radius: 16px; border: 1px solid #bbf7d0;
  }
  .config-model-display {
    display: flex; align-items: center; gap: 16px;
  }
  .config-model-info {
    display: flex; flex-direction: column; gap: 6px;
  }
  .config-model-name {
    font-size: 18px; font-weight: 700; color: #166534;
    font-family: 'JetBrains Mono', 'Fira Code', 'SF Mono', monospace;
    letter-spacing: -0.02em;
  }
  .config-details {
    display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;
  }
  .config-detail-item {
    display: flex; align-items: flex-start; gap: 12px;
    padding: 14px 16px; background: #f9fafb; border-radius: 12px;
    border: 1px solid #f3f4f6;
    transition: all 0.25s ease;
    &:hover {
      background: #ffffff; border-color: #e5e7eb;
      box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    }
  }
  .detail-icon {
    width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;
    background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
    border-radius: 10px; color: #6366f1; flex-shrink: 0;
    font-size: 16px;
  }
  .detail-content { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
  .detail-label {
    font-size: 11px; color: #6b7280; font-weight: 500;
    letter-spacing: 0.04em; text-transform: uppercase;
  }
  .detail-value {
    font-size: 13px; color: #1f2937; font-weight: 500;
    font-family: 'JetBrains Mono', 'Fira Code', 'SF Mono', monospace;
    word-break: break-all; line-height: 1.4;
  }
  .config-empty {
    display: flex; align-items: center; justify-content: center; gap: 16px;
    padding: 32px 24px; background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
    border-radius: 16px; border: 1px solid #fde68a;
  }
  .empty-icon {
    width: 56px; height: 56px; display: flex; align-items: center; justify-content: center;
    background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
    border-radius: 16px; color: #fff;
    box-shadow: 0 8px 24px rgba(245,158,11,0.25);
  }
  .empty-text { display: flex; flex-direction: column; gap: 4px; }
  .empty-title { font-size: 15px; font-weight: 600; color: #92400e; }
  .empty-desc { font-size: 13px; color: #b45309; }
}

// 统计 - 现代化卡片设计
.stats-card {
  .card-title .title-icon { color: #f59e0b; }
  .card-header {
    min-height: 43px; // 与其他卡片标题区域对齐
  }
  .stats-horizontal {
    display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px;
  }
  .stats-footer {
    display: flex; justify-content: flex-end; margin-top: 12px;
    .el-button { color: #6b7280; &:hover { color: #f59e0b; } }
  }
  .stat-item {
    position: relative; overflow: hidden;
    padding: 20px 16px 16px; border-radius: 16px; text-align: left;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    background: #ffffff;
    border: 1px solid #e5e7eb;
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 40px rgba(0,0,0,0.08);
      border-color: transparent;
      .stat-glow { opacity: 1; }
      .stat-icon-wrap { transform: scale(1.1); }
      .trend-line { stroke-dashoffset: 0 !important; }
    }
    &.requests {
      .stat-icon-wrap {
        background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
        color: #fff; box-shadow: 0 8px 24px rgba(245,158,11,0.3);
      }
      .stat-value { color: #b45309; }
      .stat-glow { background: radial-gradient(circle at top right, rgba(251,191,36,0.15), transparent 70%); }
      .trend-line { color: #f59e0b; }
    }
    &.connections {
      .stat-icon-wrap {
        background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
        color: #fff; box-shadow: 0 8px 24px rgba(59,130,246,0.3);
      }
      .stat-value { color: #1d4ed8; }
      .stat-glow { background: radial-gradient(circle at top right, rgba(96,165,250,0.15), transparent 70%); }
      .trend-line { color: #3b82f6; }
    }
    &.sessions {
      .stat-icon-wrap {
        background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
        color: #fff; box-shadow: 0 8px 24px rgba(16,185,129,0.3);
      }
      .stat-value { color: #047857; }
      .stat-glow { background: radial-gradient(circle at top right, rgba(52,211,153,0.15), transparent 70%); }
      .trend-line { color: #10b981; }
    }
  }
  .stat-glow {
    position: absolute; top: 0; left: 0; right: 0; bottom: 0;
    opacity: 0.6; transition: opacity 0.4s ease; pointer-events: none;
  }
  .stat-icon-wrap {
    width: 48px; height: 48px; display: flex; align-items: center; justify-content: center;
    border-radius: 14px; margin-bottom: 14px; position: relative; z-index: 1;
    transition: transform 0.3s ease;
  }
  .stat-content { position: relative; z-index: 1; }
  .stat-value {
    font-size: 32px; font-weight: 800; line-height: 1.1;
    font-variant-numeric: tabular-nums;
    letter-spacing: -0.03em;
    font-family: 'JetBrains Mono', 'Fira Code', 'SF Mono', monospace;
  }
  .stat-label {
    margin-top: 4px; font-size: 12px; color: #6b7280; font-weight: 500;
    letter-spacing: 0.02em;
  }
  .stat-trend {
    position: absolute; bottom: 12px; right: 12px; z-index: 1;
    width: 64px; height: 32px; opacity: 0.4;
  }
  .trend-line {
    width: 100%; height: 100%;
    stroke-dasharray: 200;
    stroke-dashoffset: 200;
    transition: stroke-dashoffset 1s ease;
  }
  .stat-item:hover .trend-line { stroke-dashoffset: 0; }
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
  .table-icon-wrap {
    width: 44px; height: 44px; display: flex; align-items: center; justify-content: center;
    background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
    border-radius: 12px; margin: 4px auto;
    border: 1px solid #e5e7eb;
    transition: all 0.3s ease;
    &:hover {
      transform: scale(1.1);
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }
  }
  .model-name-cell { display: flex; align-items: center; gap: 8px; }
  .name-text { font-weight: 600; color: #1a1a2e; font-size: 13px; }
  .running-tag {
    flex-shrink: 0;
    animation: pulse-success 2s infinite;
  }
  @keyframes pulse-success {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.7; }
  }
  .model-id-text {
    font-family: 'JetBrains Mono', 'Fira Code', 'SF Mono', monospace;
    font-size: 12px; color: #0ea5e9; font-weight: 500;
  }
  .api-url-text {
    font-family: 'JetBrains Mono', 'Fira Code', 'SF Mono', monospace;
    font-size: 11px; color: #64748b; word-break: break-all;
  }
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

// ── 入场动画 ──
.animate-slide-down {
  animation: slideDown 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  animation-delay: calc(var(--delay, 0) * 0.1s);
  opacity: 0;
}

.animate-slide-up {
  animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  animation-delay: calc(var(--delay, 0) * 0.12s);
  opacity: 0;
}

.animate-scale-up {
  animation: scaleUp 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
  animation-delay: calc(var(--delay, 0) * 0.12s);
  opacity: 0;
}

.animate-fade-in {
  animation: fadeIn 0.5s ease forwards;
  animation-delay: calc(var(--delay, 0) * 0.1s + 0.3s);
  opacity: 0;
}

.animate-bounce-in {
  animation: bounceIn 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
  animation-delay: calc(var(--delay, 0) * 0.15s + 0.2s);
  opacity: 0;
}

.animate-slide-right {
  animation: slideRight 0.5s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  animation-delay: calc(var(--delay, 0) * 0.08s + 0.4s);
  opacity: 0;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleUp {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes bounceIn {
  0% {
    opacity: 0;
    transform: scale(0.3);
  }
  50% {
    transform: scale(1.05);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes slideRight {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

// 表格行动画
.models-table {
  :deep(.el-table__row) {
    animation: tableRowFadeIn 0.4s ease forwards;
    opacity: 0;

    @for $i from 1 through 20 {
      &:nth-child(#{$i}) {
        animation-delay: #{$i * 0.06 + 0.5}s;
      }
    }
  }
}

@keyframes tableRowFadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
