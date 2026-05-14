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

  return wx.request({
    ...options,
    header
  })
}

module.exports = { request }
