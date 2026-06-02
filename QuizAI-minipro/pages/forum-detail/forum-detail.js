const { request, uploadFile, formatTime } = require('../../utils/request')
const app = getApp()

Page({
  data: {
    post: null,
    comments: [],
    commentText: '',
    loading: true,
    replyTo: null,
    expandedMap: {},
    commentImages: [],
    maxCommentImages: 3,
    likeLoading: false,
    favoriteLoading: false,
    submitLoading: false
  },

  onLoad(options) {
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    if (options.id) {
      this.postId = options.id
      this.fetchDetail()
      this.fetchComments()
    }
  },

  parseMediaField(jsonStr) {
    if (!jsonStr) return []
    try { return JSON.parse(jsonStr) } catch (e) { return [] }
  },

  fetchDetail() {
    request({
      url: `${app.globalData.baseUrl}/api/forum/post/detail`,
      params: { id: this.postId },
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          const post = res.data.data
          post.imageList = this.parseMediaField(post.images)
          post.videoList = this.parseMediaField(post.videos)
          post.createTimeFormatted = formatTime(post.createTime)
          this.setData({ post, loading: false })
        }
      },
      fail: () => {
        this.setData({ loading: false })
      }
    })
  },

  fetchComments() {
    request({
      url: `${app.globalData.baseUrl}/api/forum/comment/list`,
      params: { postId: this.postId },
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          const comments = res.data.data.map(c => ({
            ...c,
            imageList: this.parseMediaField(c.images),
            createTimeFormatted: formatTime(c.createTime)
          }))
          this.setData({ comments })
        }
      }
    })
  },

  previewImage(e) {
    const { urls, current } = e.currentTarget.dataset
    wx.previewImage({ urls, current })
  },

  toggleLike() {
    if (this.data.likeLoading) return
    this.setData({ likeLoading: true })
    request({
      url: `${app.globalData.baseUrl}/api/forum/like/toggle`,
      method: 'POST',
      data: { postId: this.postId },
      success: (res) => {
        if (res.data.code === 200) {
          const data = res.data.data
          this.setData({
            'post.liked': data.liked,
            'post.likeCount': data.likeCount
          })
        }
      },
      complete: () => {
        this.setData({ likeLoading: false })
      }
    })
  },

  toggleFavorite() {
    if (this.data.favoriteLoading) return
    this.setData({ favoriteLoading: true })
    request({
      url: `${app.globalData.baseUrl}/api/forum/favorite/toggle`,
      method: 'POST',
      data: { postId: this.postId },
      success: (res) => {
        if (res.data.code === 200) {
          const data = res.data.data
          this.setData({
            'post.favorited': data.favorited,
            'post.favoriteCount': data.favoriteCount
          })
        }
      },
      complete: () => {
        this.setData({ favoriteLoading: false })
      }
    })
  },

  onCommentInput(e) {
    this.setData({ commentText: e.detail.value })
  },

  chooseCommentImage() {
    const remaining = this.data.maxCommentImages - this.data.commentImages.length
    if (remaining <= 0) {
      wx.showToast({ title: `最多${this.data.maxCommentImages}张图片`, icon: 'none' })
      return
    }
    wx.chooseMedia({
      count: remaining,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const newImages = res.tempFiles.map(f => ({
          tempFilePath: f.tempFilePath,
          url: '',
          uploading: false,
          uploaded: false
        }))
        this.setData({ commentImages: this.data.commentImages.concat(newImages) })
      }
    })
  },

  removeCommentImage(e) {
    const index = e.currentTarget.dataset.index
    const list = this.data.commentImages
    list.splice(index, 1)
    this.setData({ commentImages: list })
  },

  submitComment() {
    const content = this.data.commentText.trim()
    if (!content && this.data.commentImages.length === 0) {
      wx.showToast({ title: '请输入评论内容或添加图片', icon: 'none' })
      return
    }
    if (this.data.submitLoading) return

    this.setData({ submitLoading: true })

    const doSubmit = (imageUrls) => {
      const data = { postId: this.postId, content: content }
      if (imageUrls.length > 0) data.images = JSON.stringify(imageUrls)
      if (this.data.replyTo) {
        data.parentId = this.data.replyTo.commentId
        data.replyToUserId = this.data.replyTo.userId
      }
      request({
        url: `${app.globalData.baseUrl}/api/forum/comment/add`,
        method: 'POST',
        data: data,
        success: (res) => {
          if (res.data.code === 200) {
            this.setData({ commentText: '', replyTo: null, commentImages: [] })
            this.fetchComments()
            this.fetchDetail()
            wx.showToast({ title: '评论成功', icon: 'success' })
          } else {
            wx.showToast({ title: res.data.msg || '评论失败', icon: 'none' })
          }
        },
        complete: () => {
          this.setData({ submitLoading: false })
        }
      })
    }

    const { commentImages } = this.data
    if (commentImages.length === 0) {
      doSubmit([])
      return
    }

    let uploadedCount = 0
    const urls = new Array(commentImages.length)
    commentImages.forEach((item, index) => {
      if (item.uploaded && item.url) {
        urls[index] = item.url
        uploadedCount++
        if (uploadedCount === commentImages.length) doSubmit(urls.filter(Boolean))
        return
      }
      uploadFile(
        item.tempFilePath,
        'image',
        (url) => {
          urls[index] = url
          uploadedCount++
          if (uploadedCount === commentImages.length) doSubmit(urls.filter(Boolean))
        },
        () => {
          wx.showToast({ title: '图片上传失败', icon: 'none' })
          this.setData({ submitLoading: false })
        }
      )
    })
  },

  replyToComment(e) {
    const { commentid, userid, nickname } = e.currentTarget.dataset
    this.setData({
      replyTo: { commentId: commentid, userId: userid, nickname: nickname }
    })
  },

  cancelReply() {
    this.setData({ replyTo: null })
  },

  toggleExpand(e) {
    const id = e.currentTarget.dataset.id
    const key = `expandedMap.${id}`
    this.setData({ [key]: !this.data.expandedMap[id] })
  },

  deletePost() {
    wx.showModal({
      title: '提示',
      content: '确定要删除这个帖子吗？',
      success: (res) => {
        if (res.confirm) {
          request({
            url: `${app.globalData.baseUrl}/api/forum/post/delete`,
            method: 'POST',
            params: { id: this.postId },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '删除成功', icon: 'success' })
                setTimeout(() => { wx.navigateBack() }, 1500)
              } else {
                wx.showToast({ title: res.data.msg || '删除失败', icon: 'none' })
              }
            }
          })
        }
      }
    })
  },

  deleteComment(e) {
    const commentId = e.currentTarget.dataset.commentid
    wx.showModal({
      title: '提示',
      content: '确定要删除这条评论吗？',
      success: (res) => {
        if (res.confirm) {
          request({
            url: `${app.globalData.baseUrl}/api/forum/comment/delete`,
            method: 'POST',
            params: { id: commentId },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '删除成功', icon: 'success' })
                this.fetchComments()
                this.fetchDetail()
              } else {
                wx.showToast({ title: res.data.msg || '删除失败', icon: 'none' })
              }
            }
          })
        }
      }
    })
  },

  reportPost() {
    wx.showActionSheet({
      itemList: ['广告/垃圾信息', '不当内容', '抄袭/侵权', '其他'],
      success: (res) => {
        const reasons = ['广告/垃圾信息', '不当内容', '抄袭/侵权', '其他']
        request({
          url: `${app.globalData.baseUrl}/api/forum/report/create`,
          method: 'POST',
          data: { postId: this.postId, reason: reasons[res.tapIndex] },
          success: (res) => {
            if (res.data.code === 200) {
              wx.showToast({ title: '举报已提交', icon: 'success' })
            } else {
              wx.showToast({ title: res.data.msg || '举报失败', icon: 'none' })
            }
          }
        })
      }
    })
  },

  goToHelpQuestion() {
    if (this.data.post && this.data.post.questionId) {
      wx.navigateTo({
        url: `/pages/practice/practice?mode=single&questionId=${this.data.post.questionId}`
      })
    }
  },

  goBack() {
    wx.navigateBack()
  }
})
