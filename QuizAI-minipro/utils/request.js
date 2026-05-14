const app = getApp()

/**
 * 统一请求封装，自动携带 Authorization header
 */
function request(options) {
  const token = wx.getStorageSync('token')
  const header = options.header || {}

  if (token) {
    header['Authorization'] = 'Bearer ' + token
  }

  const requestTask = wx.request({
    ...options,
    header
  })
  return requestTask
}

module.exports = { request }
