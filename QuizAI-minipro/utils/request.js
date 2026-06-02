const app = getApp()

/**
 * 统一请求封装，自动携带 Authorization header
 * 支持 401 错误自动跳转登录页
 */
function request(options) {
  const token = wx.getStorageSync('token')
  const header = options.header || {}

  if (token) {
    header['Authorization'] = 'Bearer ' + token
  }

  // 包装 success 回调，处理 401 错误
  const originalSuccess = options.success
  options.success = function (res) {
    if (res.statusCode === 401) {
      // token 无效或过期，清除本地存储并跳转登录
      wx.removeStorageSync('token')
      wx.removeStorageSync('userInfo')
      wx.showModal({
        title: '提示',
        content: '登录已过期，请重新登录',
        showCancel: false,
        success: () => {
          wx.navigateTo({
            url: '/pages/login/login'
          })
        }
      })
      return
    }
    // 正常响应，调用原始 success 回调
    if (originalSuccess) {
      originalSuccess(res)
    }
  }

  const requestTask = wx.request({
    ...options,
    header,
    fail: (err) => {
      wx.showToast({ title: '网络请求失败', icon: 'none' })
      options.fail && options.fail(err)
    }
  })
  return requestTask
}

/**
 * 文件上传封装，自动携带 Authorization header
 * @param {string} filePath - 临时文件路径
 * @param {string} type - 'image' 或 'video'
 * @param {function} onSuccess - 成功回调，参数为返回的 URL 字符串
 * @param {function} onFail - 失败回调
 */
function uploadFile(filePath, type, onSuccess, onFail) {
  const token = wx.getStorageSync('token')
  const header = {}
  if (token) {
    header['Authorization'] = 'Bearer ' + token
  }

  wx.uploadFile({
    url: `${app.globalData.baseUrl}/api/file/upload`,
    filePath: filePath,
    name: 'file',
    header: header,
    formData: { type: type || 'image' },
    success: (res) => {
      if (res.statusCode === 200) {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          onSuccess && onSuccess(data.data)
        } else {
          onFail && onFail(data.msg || '上传失败')
        }
      } else {
        onFail && onFail('上传请求失败')
      }
    },
    fail: () => {
      onFail && onFail('网络错误')
    }
  })
}

/**
 * 时间格式化工具
 * @param {string} timeStr - 时间字符串
 * @returns {string} 格式化后的时间
 */
function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr.replace(/-/g, '/'))
  const now = new Date()
  const diff = (now - date) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  if (diff < 604800) return Math.floor(diff / 86400) + '天前'
  return timeStr.substring(5, 16)
}

module.exports = { request, uploadFile, formatTime }
