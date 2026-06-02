const { request, formatTime } = require('../../utils/request')
const app = getApp()

Page({
  data: {
    tab: 'posts',
    postList: [],
    favoriteList: [],
    page: 1,
    size: 10,
    loading: false,
    hasMore: true
  },

  onShow() {
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    this.resetAndFetch()
  },

  onPullDownRefresh() {
    this.resetAndFetch()
    wx.stopPullDownRefresh()
  },

  resetAndFetch() {
    this.setData({ page: 1, postList: [], favoriteList: [], hasMore: true })
    if (this.data.tab === 'posts') {
      this.fetchMyPosts()
    } else {
      this.fetchMyFavorites()
    }
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ page: this.data.page + 1 })
      if (this.data.tab === 'posts') {
        this.fetchMyPosts()
      } else {
        this.fetchMyFavorites()
      }
    }
  },

  fetchMyPosts() {
    this.setData({ loading: true })
    request({
      url: `${app.globalData.baseUrl}/api/forum/post/my`,
      params: { page: this.data.page, size: this.data.size },
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          const records = (res.data.data.records || []).map(r => ({
            ...r,
            createTimeFormatted: formatTime(r.createTime)
          }))
          this.setData({
            postList: this.data.page === 1 ? records : this.data.postList.concat(records),
            hasMore: records.length >= this.data.size
          })
        }
      },
      complete: () => {
        this.setData({ loading: false })
      }
    })
  },

  fetchMyFavorites() {
    this.setData({ loading: true })
    request({
      url: `${app.globalData.baseUrl}/api/forum/favorite/my`,
      params: { page: this.data.page, size: this.data.size },
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          const records = (res.data.data.records || []).map(r => ({
            ...r,
            createTimeFormatted: formatTime(r.createTime)
          }))
          this.setData({
            favoriteList: this.data.page === 1 ? records : this.data.favoriteList.concat(records),
            hasMore: records.length >= this.data.size
          })
        }
      },
      complete: () => {
        this.setData({ loading: false })
      }
    })
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    if (tab === this.data.tab) return
    this.setData({ tab })
    this.resetAndFetch()
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/forum-detail/forum-detail?id=' + id })
  },

  deletePost(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '提示',
      content: '确定要删除这个帖子吗？',
      success: (res) => {
        if (res.confirm) {
          request({
            url: `${app.globalData.baseUrl}/api/forum/post/delete`,
            method: 'POST',
            params: { id: id },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '删除成功', icon: 'success' })
                this.resetAndFetch()
              } else {
                wx.showToast({ title: res.data.msg || '删除失败', icon: 'none' })
              }
            }
          })
        }
      }
    })
  },

  statusText(status) {
    const map = { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已关闭' }
    return map[status] || '未知'
  },

  goBack() {
    wx.navigateBack()
  }
})
