import request from '@/utils/request'

const agentTimeout = 30000

/**
 * 获取 AI 服务状态
 */
export function getAiStatus(silent = false) {
  return request({
    url: '/agent/manage/status',
    method: 'get',
    timeout: agentTimeout,
    silent
  })
}

/**
 * 切换 AI 服务状态
 */
export function toggleAiStatus(enabled) {
  return request({
    url: '/agent/manage/status',
    method: 'put',
    params: { enabled },
    timeout: agentTimeout
  })
}

/**
 * 获取并发设置
 */
export function getConcurrency() {
  return request({
    url: '/agent/manage/concurrency',
    method: 'get',
    timeout: agentTimeout
  })
}

/**
 * 设置最大并发数
 */
export function setConcurrency(data) {
  return request({
    url: '/agent/manage/concurrency',
    method: 'put',
    data,
    timeout: agentTimeout
  })
}

/**
 * 获取 AI 配置
 */
export function getAiConfig() {
  return request({
    url: '/agent/manage/config',
    method: 'get',
    timeout: agentTimeout
  })
}

/**
 * 更新 AI 配置
 */
export function updateAiConfig(data) {
  return request({
    url: '/agent/manage/config',
    method: 'put',
    data,
    timeout: agentTimeout
  })
}

/**
 * 获取综合统计信息
 */
export function getAiStats(silent = false) {
  return request({
    url: '/agent/manage/stats',
    method: 'get',
    timeout: agentTimeout,
    silent
  })
}

/**
 * 获取活跃会话列表
 */
export function getActiveThreads(silent = false) {
  return request({
    url: '/agent/manage/threads',
    method: 'get',
    timeout: agentTimeout,
    silent
  })
}

/**
 * 清除所有会话记忆
 */
export function clearAllMemory() {
  return request({
    url: '/agent/manage/memory',
    method: 'delete'
  })
}

/**
 * 删除单个会话
 */
export function deleteThread(threadId) {
  return request({
    url: `/agent/manage/thread/${threadId}`,
    method: 'delete'
  })
}

/**
 * 获取模型预设列表（仅启用状态）
 */
export function getModelPresets() {
  return request({
    url: '/agent/manage/presets',
    method: 'get'
  })
}

/**
 * 查询模型预设列表（分页）
 */
export function listModelPreset(query) {
  return request({
    url: '/agent/preset/list',
    method: 'get',
    params: query
  })
}

/**
 * 新增模型预设
 */
export function addModelPreset(data) {
  return request({
    url: '/agent/preset',
    method: 'post',
    data
  })
}

/**
 * 修改模型预设
 */
export function updateModelPreset(data) {
  return request({
    url: '/agent/preset',
    method: 'put',
    data
  })
}

/**
 * RAG 向量库重建
 */
export function rebuildRagIndex() {
  return request({
    url: '/agent/manage/rag-rebuild',
    method: 'post',
    timeout: 120000
  })
}

/**
 * 删除模型预设
 */
export function delModelPreset(ids) {
  return request({
    url: `/agent/preset/${ids}`,
    method: 'delete'
  })
}
