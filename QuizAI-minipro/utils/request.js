const app = getApp()

/**
 * 统一请求封装，自动携带 Authorization header
 * 支持 params 对象自动拼接为 query string
 */
function request(options) {
  const token = wx.getStorageSync('token')
  const header = options.header || {}

  if (token) {
    header['Authorization'] = 'Bearer ' + token
  }

  // 支持 params 对象自动拼接 URL
  let url = options.url
  if (options.params && typeof options.params === 'object') {
    const queryString = Object.entries(options.params)
      .filter(([_, v]) => v !== undefined && v !== null && v !== '')
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
      .join('&')
    if (queryString) {
      url += (url.includes('?') ? '&' : '?') + queryString
    }
  }

  // 包装 success 回调，统一处理业务错误
  const originalSuccess = options.success
  const wrappedSuccess = (res) => {
    // token 过期处理
    if (res.data.code === 401) {
      wx.removeStorageSync('token')
      wx.removeStorageSync('userInfo')
      wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
      setTimeout(() => {
        wx.redirectTo({ url: '/pages/login/login' })
      }, 1500)
      return
    }
    originalSuccess && originalSuccess(res)
  }

  const requestTask = wx.request({
    ...options,
    url,
    header,
    success: wrappedSuccess,
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
