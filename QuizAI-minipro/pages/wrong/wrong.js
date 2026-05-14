const { request } = require('../../utils/request')
const app = getApp()

Page({
  data: {
    subjects: ['全部'],
    selectedSubject: '全部',
    wrongList: [],
    loading: true
  },

  onShow() {
    this.fetchCategories()
    this.fetchWrongList()
  },

  fetchCategories() {
    request({
      url: `${app.globalData.baseUrl}/api/category/list`,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          const categoryNames = res.data.data.map(c => c.name)
          this.setData({
            subjects: ['全部', ...categoryNames]
          })
        }
      }
    })
  },

  fetchWrongList() {
    const userInfo = wx.getStorageSync('userInfo')
    if (!userInfo) return

    this.setData({ loading: true })

    request({
      url: `${app.globalData.baseUrl}/api/wrong/list`,
      data: {
        userId: userInfo.id,
        subject: this.data.selectedSubject
      },
      success: (res) => {
        if (res.data.code === 200) {
          // 给每条数据增加一个 showAnalysis 控制字段
          const list = (res.data.data || []).map(item => ({
            ...item,
            showAnalysis: false
          }))
          this.setData({
            wrongList: list,
            loading: false
          })
        } else {
          this.setData({ loading: false })
          wx.showToast({ title: '加载失败', icon: 'none' })
        }
      },
      fail: () => {
        this.setData({ loading: false })
        wx.showToast({ title: '网络错误', icon: 'none' })
      }
    })
  },

  onSelectSubject(e) {
    const subject = e.currentTarget.dataset.subject
    this.setData({ selectedSubject: subject }, () => {
      this.fetchWrongList()
    })
  },

  toggleAnalysis(e) {
    const index = e.currentTarget.dataset.index
    const key = `wrongList[${index}].showAnalysis`
    this.setData({
      [key]: !this.data.wrongList[index].showAnalysis
    })
  },

  removeWrong(e) {
    const id = e.currentTarget.dataset.id
    const index = e.currentTarget.dataset.index
    const userInfo = wx.getStorageSync('userInfo')

    wx.showModal({
      title: '确认移除',
      content: '确定要将这道题从错题本移除吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '移除中' })
          request({
            url: `${app.globalData.baseUrl}/api/wrong/remove`,
            method: 'POST',
            data: {
              userId: userInfo.id,
              questionId: id
            },
            success: (resp) => {
              wx.hideLoading()
              if (resp.data.code === 200) {
                wx.showToast({ title: '已移除' })
                // 前端静默删除
                const list = this.data.wrongList
                list.splice(index, 1)
                this.setData({ wrongList: list })
              }
            }
          })
        }
      }
    })
  },

  startWrongPractice() {
    if (this.data.wrongList.length === 0) {
      return wx.showToast({ title: '当前没有错题', icon: 'none' })
    }
    wx.navigateTo({
      url: `/pages/practice/practice?subject=${this.data.selectedSubject}&mode=wrong&count=10`
    })
  },

  goBack() {
    wx.navigateBack()
  }
})
