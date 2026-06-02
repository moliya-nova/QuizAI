const { request, formatTime } = require('../../utils/request')
const app = getApp()

// 防抖定时器
let searchTimer = null

Page({
  data: {
    postList: [],
    loading: false,
    page: 1,
    size: 10,
    hasMore: true,
    type: -1,
    keyword: '',
    typeTabs: [
      { name: '全部', value: -1 },
      { name: '普通帖', value: 0 },
      { name: '求助帖', value: 1 }
    ]
  },

  onShow() {
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    this.setData({ page: 1, postList: [], hasMore: true })
    this.fetchPosts()
  },

  onPullDownRefresh() {
    this.setData({ page: 1, postList: [], hasMore: true })
    this.fetchPosts().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ page: this.data.page + 1 })
      this.fetchPosts()
    }
  },

  fetchPosts() {
    this.setData({ loading: true })
    const params = {
      page: this.data.page,
      size: this.data.size
    }
    if (this.data.type >= 0) params.type = this.data.type
    if (this.data.keyword) params.keyword = this.data.keyword

    return request({
      url: `${app.globalData.baseUrl}/api/forum/post/list`,
      params: params,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          const records = (res.data.data.records || []).map(r => ({
            ...r,
            imageList: r.images ? JSON.parse(r.images) : [],
            videoList: r.videos ? JSON.parse(r.videos) : [],
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

  onTypeChange(e) {
    const type = parseInt(e.currentTarget.dataset.type)
    this.setData({ type, page: 1, postList: [], hasMore: true })
    this.fetchPosts()
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
    // 输入防抖，500ms 后自动搜索
    if (searchTimer) clearTimeout(searchTimer)
    searchTimer = setTimeout(() => {
      this.setData({ page: 1, postList: [], hasMore: true })
      this.fetchPosts()
    }, 500)
  },

  onSearch() {
    if (searchTimer) clearTimeout(searchTimer)
    this.setData({ page: 1, postList: [], hasMore: true })
    this.fetchPosts()
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/forum-detail/forum-detail?id=' + id })
  },

  goToCreate() {
    wx.navigateTo({ url: '/pages/forum-create/forum-create' })
  },

  goToMy() {
    wx.navigateTo({ url: '/pages/forum-my/forum-my' })
  },

  goBack() {
    wx.navigateBack({ delta: 1 })
  }
})
