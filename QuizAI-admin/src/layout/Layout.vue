<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '72px' : '230px'" class="aside">
      <!-- Logo 区域 -->
      <div class="logo-area" :class="{ collapsed: isCollapse }">
        <div class="logo-icon">
          <img :src="avatarImg" class="sidebar-avatar" alt="avatar">
        </div>
        <transition name="fade">
          <div v-show="!isCollapse" class="logo-text">
            <span class="logo-title">QuizAI</span>
            <span class="logo-sub">Admin Console</span>
          </div>
        </transition>
      </div>

      <!-- 导航菜单 -->
      <div class="nav-wrapper">
        <div class="nav-section-label" v-show="!isCollapse">主菜单</div>
        <template v-for="item in menuItems" :key="item.path">
          <!-- 有子菜单的项 -->
          <div v-if="item.children" class="nav-group">
            <div
              class="nav-item"
              :class="{ active: isParentActive(item) }"
              @click="toggleExpand(item)"
            >
              <div class="nav-item-bg"></div>
              <div class="nav-item-accent"></div>
              <el-icon class="nav-icon" :size="20">
                <component :is="item.icon" />
              </el-icon>
              <span class="nav-label" v-show="!isCollapse">{{ item.label }}</span>
              <el-icon v-show="!isCollapse" class="expand-arrow" :class="{ expanded: forumExpanded }">
                <ArrowDown />
              </el-icon>
            </div>
            <transition name="submenu">
              <div v-show="forumExpanded && !isCollapse" class="sub-menu">
                <div
                  v-for="child in item.children"
                  :key="child.path"
                  class="nav-item sub-item"
                  :class="{ active: isActive(child.path) }"
                  @click="navigate(child.path)"
                >
                  <div class="nav-item-bg"></div>
                  <div class="nav-item-accent"></div>
                  <el-icon class="nav-icon" :size="16">
                    <component :is="child.icon" />
                  </el-icon>
                  <span class="nav-label">{{ child.label }}</span>
                  <div v-if="isActive(child.path) && !isCollapse" class="active-dot"></div>
                </div>
              </div>
            </transition>
          </div>
          <!-- 普通菜单项 -->
          <div
            v-else
            class="nav-item"
            :class="{ active: isActive(item.path) }"
            @click="navigate(item.path)"
          >
            <div class="nav-item-bg"></div>
            <div class="nav-item-accent"></div>
            <el-icon class="nav-icon" :size="20">
              <component :is="item.icon" />
            </el-icon>
            <span class="nav-label" v-show="!isCollapse">{{ item.label }}</span>
            <div v-if="isActive(item.path) && !isCollapse" class="active-dot"></div>
          </div>
        </template>
      </div>

      <!-- 底部折叠按钮 -->
      <div class="collapse-btn" @click="isCollapse = !isCollapse">
        <el-icon :size="18" class="collapse-icon">
          <DArrowLeft v-if="!isCollapse" />
          <DArrowRight v-else />
        </el-icon>
      </div>
    </el-aside>

    <!-- 主体区域 -->
    <el-container class="main-container">
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-breadcrumb">
          <el-icon class="breadcrumb-icon"><HomeFilled /></el-icon>
          <span class="breadcrumb-sep">/</span>
          <span class="breadcrumb-current">{{ currentPageName }}</span>
        </div>
        <div class="header-right">
          <div class="header-time">{{ currentTime }}</div>
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-avatar">
              <img :src="avatarImg" class="avatar-circle" alt="avatar">
              <span class="avatar-name">Admin</span>
              <el-icon class="avatar-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>

    <!-- 悬浮 AI 助手按钮 -->
    <div class="ai-float-btn" @click="showChat = !showChat" :class="{ active: showChat }">
      <el-icon :size="24"><ChatDotRound /></el-icon>
    </div>

    <!-- 悬浮聊天窗 -->
    <ChatDialog v-model="showChat" />
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ChatDialog from '@/components/ChatDialog/index.vue'
import avatarImg from '@/assets/img/avatar.jpg'
import {
  User, Menu, Document, Cpu, ChatLineSquare, Warning, ArrowDown, DArrowLeft, DArrowRight,
  HomeFilled, SwitchButton, ChatDotRound
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const currentTime = ref('')
const showChat = ref(false)
const forumExpanded = ref(route.path.startsWith('/forum'))

const menuItems = [
  { path: '/user', label: '用户管理', icon: User },
  { path: '/category', label: '分类管理', icon: Menu },
  { path: '/question', label: '题目管理', icon: Document },
  { path: '/agent', label: 'Agent管理', icon: Cpu },
  {
    path: '/forum',
    label: '论坛管理',
    icon: ChatLineSquare,
    children: [
      { path: '/forum', label: '帖子管理', icon: ChatLineSquare },
      { path: '/forum/report', label: '举报管理', icon: Warning },
    ]
  },
]

const isActive = (path) => route.path === path
const isParentActive = (item) => item.children && route.path.startsWith(item.path)

const navigate = (path) => {
  router.push(path)
}

const toggleExpand = (item) => {
  if (item.children) {
    forumExpanded.value = !forumExpanded.value
  }
}

const pageNameMap = {
  '/user': '用户管理',
  '/category': '分类管理',
  '/question': '题目管理',
  '/agent': 'Agent管理',
  '/forum': '帖子管理',
  '/forum/report': '举报管理',
}

const currentPageName = computed(() => {
  return pageNameMap[route.path] || '仪表盘'
})

let timer = null
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
}
onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})
onUnmounted(() => {
  clearInterval(timer)
})

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminInfo')
    router.push('/login')
  }
}
</script>

<style scoped>
/* ========== 布局容器 ========== */
.layout-container {
  height: 100vh;
  background: #f0f2f5;
}

/* ========== 侧边栏 ========== */
.aside {
  position: relative;
  background: linear-gradient(175deg, #10b981 0%, #059669 40%, #047857 100%);
  overflow: hidden;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.12);
}

/* 侧边栏微妙纹理 */
.aside::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 50% 0%, rgba(255, 255, 255, 0.15) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 100%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

/* ========== Logo 区域 ========== */
.logo-area {
  position: relative;
  z-index: 1;
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  transition: padding 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.logo-area.collapsed {
  padding: 20px 16px;
  justify-content: center;
}

.logo-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.15));
}

.sidebar-avatar {
  width: 100%;
  height: 100%;
  border-radius: 10px;
  object-fit: cover;
}

.logo-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
  white-space: nowrap;
}

.logo-title {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
  line-height: 1.2;
}

.logo-sub {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 400;
  letter-spacing: 1px;
  text-transform: uppercase;
}

/* ========== 导航区域 ========== */
.nav-wrapper {
  flex: 1;
  padding: 16px 12px;
  position: relative;
  z-index: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-wrapper::-webkit-scrollbar {
  width: 3px;
}

.nav-wrapper::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.nav-section-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
  text-transform: uppercase;
  letter-spacing: 2px;
  padding: 0 12px 10px;
  font-weight: 600;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  height: 46px;
  padding: 0 16px;
  margin-bottom: 4px;
  border-radius: 10px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
  overflow: hidden;
}

.nav-item-bg {
  position: absolute;
  inset: 0;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0);
  transition: background 0.3s ease;
}

.nav-item-accent {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  border-radius: 0 4px 4px 0;
  background: #fff;
  transition: height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-item:hover {
  color: #fff;
  border-color: rgba(255, 255, 255, 0.25);
}

.nav-item:hover .nav-item-bg {
  background: rgba(255, 255, 255, 0.12);
}

.nav-item.active {
  color: #047857;
  border-color: transparent;
}

.nav-item.active .nav-item-bg {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.nav-item.active .nav-item-accent {
  height: 24px;
  background: #10b981;
}

.nav-icon {
  position: relative;
  z-index: 1;
  flex-shrink: 0;
  transition: transform 0.25s ease;
}

.nav-item:hover .nav-icon {
  transform: scale(1.08);
}

.nav-item.active .nav-icon {
  color: #047857;
}

.nav-label {
  position: relative;
  z-index: 1;
  margin-left: 12px;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  transition: opacity 0.2s ease;
}

.active-dot {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 6px rgba(16, 185, 129, 0.4);
}

/* ========== 子菜单 ========== */
.nav-group {
  margin-bottom: 4px;
}

.expand-arrow {
  position: relative;
  z-index: 1;
  margin-left: auto;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  transition: transform 0.3s ease;
}

.expand-arrow.expanded {
  transform: rotate(180deg);
}

.sub-menu {
  overflow: hidden;
  padding: 4px 0 0 0;
}

.nav-item.sub-item {
  height: 40px;
  padding: 0 16px 0 44px;
  margin-bottom: 2px;
  font-size: 13px;
}

.submenu-enter-active,
.submenu-leave-active {
  transition: all 0.25s ease;
}

.submenu-enter-from,
.submenu-leave-to {
  opacity: 0;
  max-height: 0;
}

.submenu-enter-to,
.submenu-leave-from {
  opacity: 1;
  max-height: 200px;
}

/* ========== 折叠按钮 ========== */
.collapse-btn {
  position: relative;
  z-index: 1;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  cursor: pointer;
  color: rgba(255, 255, 255, 0.6);
  transition: all 0.25s ease;
}

.collapse-btn:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.collapse-icon {
  transition: transform 0.3s ease;
}

/* ========== 头部 ========== */
.main-container {
  flex-direction: column;
}

.header {
  height: 56px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.03);
  position: relative;
  z-index: 10;
}

.header-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #909399;
}

.breadcrumb-icon {
  color: #10b981;
}

.breadcrumb-sep {
  color: #c0c4cc;
}

.breadcrumb-current {
  color: #303133;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-time {
  font-size: 13px;
  color: #909399;
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.5px;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 8px;
  transition: background 0.2s ease;
}

.user-avatar:hover {
  background: #f5f7fa;
}

.avatar-circle {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.3);
}

.avatar-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.avatar-arrow {
  color: #909399;
  font-size: 12px;
  transition: transform 0.2s ease;
}

/* ========== 内容区 ========== */
.main {
  background: #f0f2f5;
  padding: 24px;
  min-height: 0;
}

/* ========== 过渡动画 ========== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 悬浮 AI 助手按钮 */
.ai-float-btn {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 9997;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.35);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:hover {
    transform: scale(1.08);
    box-shadow: 0 12px 32px rgba(99, 102, 241, 0.5);
  }

  &.active {
    background: #ef4444;
    box-shadow: 0 8px 24px rgba(239, 68, 68, 0.35);
    &:hover { box-shadow: 0 12px 32px rgba(239, 68, 68, 0.5); }
  }
}
</style>
