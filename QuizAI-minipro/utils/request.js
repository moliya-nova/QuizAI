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
    header
  })
  return requestTask
}

module.exports = { request }
