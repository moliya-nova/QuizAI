const app = getApp()

Page({
  data: {
    userInfo: null,
    showNicknameModal: false,
    tempNickname: ''
  },

  onLoad() {
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      this.setData({ userInfo })
    }
  },

  // 修改头像
  chooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath
        // 实际项目中这里应该调用后端上传接口，获取到线上URL后再更新。
        wx.showLoading({ title: '上传中' })
        
        // 模拟上传成功
        setTimeout(() => {
          wx.hideLoading()
          this.updateUserInfo({ avatar: tempFilePath })
        }, 800)
      }
    })
  },

  // 打开修改昵称弹窗
  editNickname() {
    this.setData({
      showNicknameModal: true,
      tempNickname: this.data.userInfo.nickname || ''
    })
  },

  // 监听昵称输入
  onNicknameInput(e) {
    this.setData({ tempNickname: e.detail.value })
  },

  // 关闭昵称弹窗
  closeNicknameModal() {
    this.setData({ showNicknameModal: false })
  },

  // 保存昵称
  saveNickname() {
    const { tempNickname, userInfo } = this.data
    if (!tempNickname.trim()) {
      wx.showToast({ title: '昵称不能为空', icon: 'none' })
      return
    }
    if (tempNickname === userInfo.nickname) {
      this.closeNicknameModal()
      return
    }

    this.updateUserInfo({ nickname: tempNickname })
    this.closeNicknameModal()
  },

  // 调用后端接口更新用户信息
  updateUserInfo(updateData) {
    const { userInfo } = this.data
    wx.showLoading({ title: '保存中' })

    wx.request({
      url: `${app.globalData.baseUrl}/api/user/update`,
      method: 'POST',
      data: {
        id: userInfo.id,
        ...updateData
      },
      success: (res) => {
        wx.hideLoading()
        if (res.data.code === 200) {
          wx.showToast({ title: '更新成功', icon: 'success' })
          
          // 更新本地缓存和页面数据
          const newUserInfo = { ...userInfo, ...updateData }
          wx.setStorageSync('userInfo', newUserInfo)
          this.setData({ userInfo: newUserInfo })
        } else {
          wx.showToast({ title: res.data.message || '更新失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '网络错误', icon: 'none' })
      }
    })
  }
})