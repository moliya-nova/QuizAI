const { request, uploadFile } = require('../../utils/request')
const app = getApp()

// 草稿存储 key
const DRAFT_KEY = 'forum_draft'

Page({
  data: {
    title: '',
    content: '',
    type: 0,
    questionId: null,
    questionList: [],
    anonymous: false,
    nickname: '',
    submitting: false,
    mediaType: '',
    mediaList: [],
    maxImageCount: 9,
    hasDraft: false,
    published: false
  },

  onLoad() {
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo && userInfo.nickname) {
      this.setData({ nickname: userInfo.nickname })
    }
    // 检查是否有草稿
    this.checkDraft()
  },

  onUnload() {
    // 未发布且有内容时才保存草稿
    if (!this.data.published && (this.data.title.trim() || this.data.content.trim())) {
      this.saveDraft()
    }
  },

  checkDraft() {
    const draft = wx.getStorageSync(DRAFT_KEY)
    if (draft && (draft.title || draft.content)) {
      this.setData({ hasDraft: true })
    }
  },

  restoreDraft() {
    const draft = wx.getStorageSync(DRAFT_KEY)
    if (draft) {
      this.setData({
        title: draft.title || '',
        content: draft.content || '',
        type: draft.type || 0,
        anonymous: draft.anonymous || false
      })
      wx.showToast({ title: '已恢复草稿', icon: 'success' })
    }
    this.setData({ hasDraft: false })
  },

  clearDraft() {
    wx.removeStorageSync(DRAFT_KEY)
    this.setData({ hasDraft: false })
  },

  saveDraft() {
    const { title, content, type, anonymous } = this.data
    wx.setStorageSync(DRAFT_KEY, { title, content, type, anonymous })
  },

  onAnonymousToggle() {
    this.setData({ anonymous: !this.data.anonymous })
  },

  onTitleInput(e) {
    this.setData({ title: e.detail.value })
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },

  onTypeChange(e) {
    const type = parseInt(e.currentTarget.dataset.type)
    this.setData({ type, questionId: null })
    if (type === 1 && this.data.questionList.length === 0) {
      this.fetchQuestions()
    }
  },

  fetchQuestions() {
    request({
      url: `${app.globalData.baseUrl}/api/question/random?count=20`,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          this.setData({ questionList: res.data.data })
        }
      }
    })
  },

  onSelectQuestion(e) {
    const id = parseInt(e.currentTarget.dataset.id)
    this.setData({ questionId: id })
  },

  chooseMedia(e) {
    const type = e.currentTarget.dataset.type
    const count = type === 'image' ? this.data.maxImageCount : 1
    wx.chooseMedia({
      count: count,
      mediaType: [type],
      sourceType: ['album', 'camera'],
      maxDuration: type === 'video' ? 60 : undefined,
      success: (res) => {
        const files = res.tempFiles.map(f => ({
          tempFilePath: f.tempFilePath,
          url: '',
          uploading: false,
          uploaded: false,
          thumbTempFilePath: f.thumbTempFilePath || ''
        }))
        this.setData({ mediaType: type, mediaList: files })
      }
    })
  },

  removeMedia(e) {
    const index = e.currentTarget.dataset.index
    const list = this.data.mediaList
    list.splice(index, 1)
    this.setData({
      mediaList: list,
      mediaType: list.length > 0 ? this.data.mediaType : ''
    })
  },

  uploadAllMedia() {
    const { mediaList, mediaType } = this.data
    if (mediaList.length === 0) return Promise.resolve([])
    return new Promise((resolve, reject) => {
      let uploadedCount = 0
      const totalCount = mediaList.length
      const updatedList = [...mediaList]
      mediaList.forEach((item, index) => {
        if (item.uploaded && item.url) {
          uploadedCount++
          if (uploadedCount === totalCount) resolve(updatedList)
          return
        }
        updatedList[index] = { ...item, uploading: true }
        this.setData({ mediaList: updatedList })
        uploadFile(
          item.tempFilePath,
          mediaType,
          (url) => {
            updatedList[index] = { ...updatedList[index], url, uploading: false, uploaded: true }
            this.setData({ mediaList: updatedList })
            uploadedCount++
            if (uploadedCount === totalCount) resolve(updatedList)
          },
          (errMsg) => {
            updatedList[index] = { ...updatedList[index], uploading: false }
            this.setData({ mediaList: updatedList })
            reject(errMsg)
          }
        )
      })
    })
  },

  submitPost() {
    const { title, content, type, questionId, anonymous, submitting, mediaList, mediaType } = this.data
    console.log('[submitPost] called, submitting:', submitting)
    if (submitting) return
    if (!title.trim()) {
      wx.showToast({ title: '请输入标题', icon: 'none' })
      return
    }
    if (title.length > 120) {
      wx.showToast({ title: '标题不能超过120字', icon: 'none' })
      return
    }
    if (!content.trim()) {
      wx.showToast({ title: '请输入内容', icon: 'none' })
      return
    }
    if (type === 1 && !questionId) {
      wx.showToast({ title: '请选择关联题目', icon: 'none' })
      return
    }

    console.log('[submitPost] validation passed, submitting...')
    this.setData({ submitting: true })

    this.uploadAllMedia().then((updatedList) => {
      const imageUrls = []
      const videoUrls = []
      updatedList.forEach(item => {
        if (item.url) {
          if (mediaType === 'image') imageUrls.push(item.url)
          else videoUrls.push(item.url)
        }
      })

      const data = { title: title.trim(), content: content.trim(), type, anonymous: anonymous ? 1 : 0 }
      if (imageUrls.length > 0) data.images = JSON.stringify(imageUrls)
      if (videoUrls.length > 0) data.videos = JSON.stringify(videoUrls)
      if (type === 1 && questionId) data.questionId = questionId

      console.log('[submitPost] sending request:', JSON.stringify(data))
      request({
        url: `${app.globalData.baseUrl}/api/forum/post/create`,
        method: 'POST',
        data: data,
        success: (res) => {
          console.log('[submitPost] success:', JSON.stringify(res.data))
          if (res.data.code === 200) {
            this.setData({ published: true })
            wx.removeStorageSync(DRAFT_KEY)
            wx.showToast({ title: '发帖成功，等待审核', icon: 'success' })
            setTimeout(() => { wx.navigateBack() }, 1500)
          } else {
            wx.showToast({ title: res.data.msg || '发帖失败', icon: 'none' })
          }
        },
        fail: (err) => {
          console.log('[submitPost] request fail:', JSON.stringify(err))
        },
        complete: () => {
          console.log('[submitPost] complete')
          this.setData({ submitting: false })
        }
      })
    }).catch((errMsg) => {
      console.log('[submitPost] upload error:', errMsg)
      this.setData({ submitting: false })
      wx.showToast({ title: errMsg || '文件上传失败', icon: 'none' })
    })
  },

  goBack() {
    wx.navigateBack()
  }
})
