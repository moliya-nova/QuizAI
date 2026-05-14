<template>
  <div class="login-container">
    <!-- 粒子背景 -->
    <vue-particles
      id="tsparticles"
      :particlesInit="particlesInit"
      :options="particlesOptions"
    />
    
    <div class="login-box">
      <div class="login-left">
        <div class="left-content">
          <img src="/assets/img/logo.png" class="left-img" alt="illustration" />
          <h2 class="left-title">欢迎使用</h2>
          <p class="left-desc">QuizAI 智能刷题后台管理系统</p>
        </div>
      </div>
      <div class="login-right">
        <el-card class="login-card" shadow="never">
          <div class="login-header">
            <div class="header-logo">
              <img src="https://img.alicdn.com/tfs/TB1Z0Xvj3mTBuNjy1XbXXaMrVXa-140-140.png" alt="logo" class="logo-img" />
            </div>
            <h2 class="sys-title">系统登录</h2>
            <p class="sys-sub">Please Login to Your Account</p>
          </div>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large" class="login-form">
            <el-form-item prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="请输入管理员账号" 
                prefix-icon="User" 
                clearable 
                class="custom-input"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="请输入管理员密码" 
                prefix-icon="Lock" 
                show-password 
                @keyup.enter="handleLogin" 
                class="custom-input"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { loadFull } from "tsparticles"

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

// 粒子动画初始化
const particlesInit = async engine => {
  await loadFull(engine);
};

// 粒子动画配置
const particlesOptions = reactive({
  background: {
    color: {
      value: "#f2f5f8" // 浅色背景
    }
  },
  fpsLimit: 60,
  interactivity: {
    events: {
      onClick: {
        enable: true,
        mode: "push"
      },
      onHover: {
        enable: true,
        mode: "grab"
      },
      resize: true
    },
    modes: {
      push: {
        quantity: 4
      },
      grab: {
        distance: 200,
        links: {
          opacity: 0.5
        }
      }
    }
  },
  particles: {
    color: {
      value: "#409eff" // 主题蓝粒子
    },
    links: {
      color: "#a0cfff",
      distance: 150,
      enable: true,
      opacity: 0.4,
      width: 1
    },
    collisions: {
      enable: true
    },
    move: {
      direction: "none",
      enable: true,
      outModes: {
        default: "bounce"
      },
      random: false,
      speed: 2,
      straight: false
    },
    number: {
      density: {
        enable: true,
        area: 800
      },
      value: 60
    },
    opacity: {
      value: 0.5
    },
    shape: {
      type: "circle"
    },
    size: {
      value: { min: 1, max: 4 }
    }
  },
  detectRetina: true
})

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      request.post('/admin/login', form).then(res => {
        ElMessage.success('登录成功')
        localStorage.setItem('adminToken', res.data.token)
        localStorage.setItem('adminInfo', JSON.stringify(res.data.adminInfo))
        router.push('/')
      }).finally(() => {
        loading.value = false
      })
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

#tsparticles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.login-box {
  display: flex;
  width: 900px;
  height: 500px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 1;
  backdrop-filter: blur(10px);
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #409eff 0%, #1e80ff 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  padding: 40px;
}

.left-content {
  text-align: center;
}

.left-img {
  width: 320px;
  margin-bottom: 30px;
  filter: drop-shadow(0 10px 20px rgba(0,0,0,0.2));
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-15px); }
  100% { transform: translateY(0px); }
}

.left-title {
  font-size: 32px;
  margin: 0 0 10px 0;
  font-weight: 600;
  letter-spacing: 2px;
}

.left-desc {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.login-right {
  flex: 1;
  padding: 40px 50px;
  display: flex;
  align-items: center;
}

.login-card {
  width: 100%;
  border: none;
  background: transparent;
}

:deep(.el-card__body) {
  padding: 0;
}

.login-header {
  text-align: left;
  margin-bottom: 40px;
}

.header-logo {
  margin-bottom: 20px;
}

.logo-img {
  width: 48px;
  height: 48px;
}

.sys-title {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 28px;
  font-weight: bold;
}

.sys-sub {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.login-form {
  margin-top: 20px;
}

.custom-input :deep(.el-input__wrapper) {
  background-color: #f5f7fa;
  box-shadow: none;
  border-radius: 8px;
  padding: 0 15px;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
  background-color: #fff;
}

.login-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 4px;
  margin-top: 10px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}
</style>
