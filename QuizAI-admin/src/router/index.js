import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../layout/Layout.vue'),
    redirect: '/user',
    children: [
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('../views/UserManage.vue')
      },
      {
        path: 'category',
        name: 'CategoryManage',
        component: () => import('../views/CategoryManage.vue')
      },
      {
        path: 'question',
        name: 'QuestionManage',
        component: () => import('../views/QuestionManage.vue')
      },
      {
        path: 'agent',
        name: 'AgentManage',
        component: () => import('../views/agent/AgentManage.vue')
      },
      {
        path: 'forum',
        name: 'ForumManage',
        component: () => import('../views/ForumManage.vue')
      },
      {
        path: 'forum/report',
        name: 'ForumReportManage',
        component: () => import('../views/ForumReportManage.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('adminToken')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
