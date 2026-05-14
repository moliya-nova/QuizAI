const app = getApp()

Page({
  data: {
    username: '',
    password: '',
    confirmPassword: '',
    nickname: ''
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  onConfirmPasswordInput(e) {
    this.setData({ confirmPassword: e.detail.value })
  },

  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value })
  },

  handleRegister() {
    const { username, password, confirmPassword, nickname } = this.data

    if (!username || !password) {
      wx.showToast({ title: '账号和密码不能为空', icon: 'none' })
      return
    }

    if (password !== confirmPassword) {
      wx.showToast({ title: '两次输入的密码不一致', icon: 'none' })
      return
    }

    wx.showLoading({ title: '注册中' })

    // TODO: 替换为实际的后端接口地址
    wx.request({
      url: `${app.globalData.baseUrl}/api/user/register`, 
      method: 'POST',
      data: {
        username,
        password,
        nickname
      },
      success: (res) => {
        wx.hideLoading()
        if (res.data.code === 200) {
          wx.showToast({ title: '注册成功' })
          setTimeout(() => {
            // 注册成功后返回登录页
            wx.navigateBack()
          }, 1500)
        } else {
          wx.showToast({
            title: res.data.message || '注册失败',
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

  goToLogin() {
    wx.navigateBack()
  }
})
