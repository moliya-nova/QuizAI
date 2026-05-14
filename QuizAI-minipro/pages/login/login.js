const app = getApp()

Page({
  data: {
    username: '',
    password: ''
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  handleLogin() {
    const { username, password } = this.data
    if (!username || !password) {
      wx.showToast({
        title: '请输入账号和密码',
        icon: 'none'
      })
      return
    }

    wx.showLoading({ title: '登录中' })
    
    // TODO: 替换为实际的后端接口地址
    wx.request({
      url: `${app.globalData.baseUrl}/api/user/login`, 
      method: 'POST',
      data: {
        username,
        password
      },
      success: (res) => {
        wx.hideLoading()
        if (res.data.code === 200) {
          wx.showToast({ title: '登录成功' })
          // 保存 Token 和用户信息
          wx.setStorageSync('token', res.data.data.token)
          wx.setStorageSync('userInfo', res.data.data.userInfo)
          // 跳转到首页
          setTimeout(() => {
            wx.switchTab({ url: '/pages/index/index' })
          }, 1000)
        } else {
          wx.showToast({
            title: res.data.message || '登录失败',
            icon: 'none'
          })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({
          title: '网络请求失败',
          icon: 'none'
        })
      }
    })
  },

  goToRegister() {
    wx.navigateTo({
      url: '/pages/register/register'
    })
  }
})
