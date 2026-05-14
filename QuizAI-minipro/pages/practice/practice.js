const { request } = require('../../utils/request')
const app = getApp()

Page({
  data: {
    subject: '',
    mode: 'normal',
    count: 10,
    loading: true,
    
    questions: [],
    currentIndex: 0,
    currentQuestion: {},
    
    selectedOption: '',
    isAnswered: false,
    
    correctCount: 0,
    wrongCount: 0
  },

  onLoad(options) {
    this.setData({
      subject: options.subject || '全部',
      mode: options.mode || 'normal',
      count: parseInt(options.count) || 10
    })
    
    this.fetchQuestions()
  },

  fetchQuestions() {
    this.setData({ loading: true })
    
    const userInfo = wx.getStorageSync('userInfo')
    
    let url = `${app.globalData.baseUrl}/api/question/random`
    let data = {
      subject: this.data.subject,
      count: this.data.count
    }
    
    if (this.data.mode === 'wrong') {
      if (!userInfo) {
        wx.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      url = `${app.globalData.baseUrl}/api/wrong/random`
      data = {
        userId: userInfo.id,
        subject: this.data.subject,
        count: this.data.count
      }
    }

    // 调用后端接口获取题目
    request({
      url: url,
      data: data,
      success: (res) => {
        if (res.data.code === 200 && res.data.data && res.data.data.length > 0) {
          const questions = res.data.data
          this.setData({
            questions,
            currentQuestion: questions[0],
            loading: false,
            aiLoading: false,
            aiAnalysisResult: ''
          })
        } else {
          this.setData({ loading: false })
          wx.showModal({
            title: '提示',
            content: '未找到题目',
            showCancel: false,
            success: () => {
              wx.navigateBack()
            }
          })
        }
      },
      fail: () => {
        this.setData({ loading: false })
        wx.showToast({
          title: '网络请求失败',
          icon: 'none'
        })
      }
    })
  },

  onSelectOption(e) {
    // 如果已经答题，则不允许修改选项
    if (this.data.isAnswered) return

    const option = e.currentTarget.dataset.option
    this.setData({ selectedOption: option })
  },

  submitAnswer() {
    const { selectedOption, currentQuestion, isAnswered, mode } = this.data
    
    // 如果没有选择选项，或者已经提交过了，直接返回
    if (!selectedOption || isAnswered) return

    const isCorrect = selectedOption === currentQuestion.correctAnswer
    
    this.setData({
      isAnswered: true,
      correctCount: this.data.correctCount + (isCorrect ? 1 : 0),
      wrongCount: this.data.wrongCount + (isCorrect ? 0 : 1)
    })

    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      if (!isCorrect && mode !== 'wrong') {
        // 答错了，加入错题本
        request({
          url: `${app.globalData.baseUrl}/api/wrong/add`,
          method: 'POST',
          data: {
            userId: userInfo.id,
            questionId: currentQuestion.id
          },
          success: (res) => {
            console.log('加入错题本结果:', res.data)
          }
        })
      } else if (isCorrect && mode === 'wrong') {
        // 错题重练答对了，询问是否移出错题本
        wx.showModal({
          title: '提示',
          content: '错题已掌握，是否从错题本删除该题目？',
          success: (modalRes) => {
            if (modalRes.confirm) {
              request({
                url: `${app.globalData.baseUrl}/api/wrong/remove`,
                method: 'POST',
                data: {
                  userId: userInfo.id,
                  questionId: currentQuestion.id
                },
                success: (res) => {
                  console.log('移出错题本结果:', res.data)
                  wx.showToast({ title: '已删除', icon: 'success' })
                }
              })
            }
          }
        })
      }
    }
  },

  nextQuestion() {
    const { currentIndex, questions } = this.data
    
    if (currentIndex < questions.length - 1) {
      // 进入下一题
      this.setData({
        currentIndex: currentIndex + 1,
        currentQuestion: questions[currentIndex + 1],
        selectedOption: '',
        isAnswered: false,
        aiLoading: false,
        aiAnalysisResult: ''
      })
    } else {
      // 题目做完，结束练习
      this.finishPractice()
    }
  },

  finishPractice() {
    const total = this.data.questions.length
    const correct = this.data.correctCount
    const accuracy = Math.round((correct / total) * 100)
    
    if (this.data.mode === 'wrong') {
      wx.showModal({
        title: '提示',
        content: '错题重练完成 🎉',
        confirmText: '返回',
        showCancel: false,
        success: () => {
          wx.navigateBack()
        }
      })
      return
    }

    const userInfo = wx.getStorageSync('userInfo')
    if (!userInfo) return

    wx.showLoading({ title: '正在保存记录...' })

    // 调用后端接口保存本次练习记录
    request({
      url: `${app.globalData.baseUrl}/api/record/submit`,
      method: 'POST',
      data: {
        userId: userInfo.id,
        subject: this.data.subject,
        totalCount: total,
        correctCount: correct
      },
      success: (res) => {
        wx.hideLoading()
        if (res.data.code === 200) {
          wx.showModal({
            title: '练习完成 🎉',
            content: `共 ${total} 题，答对 ${correct} 题\n正确率：${accuracy}%`,
            confirmText: '返回首页',
            showCancel: false,
            success: () => {
              wx.navigateBack()
            }
          })
        } else {
          wx.showToast({ title: '记录保存失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '网络请求失败', icon: 'none' })
      }
    })
  },

  //非流式请求方法
  
  getAIAnalysis() {
    const currentQ = this.data.currentQuestion;
    const userSelected = this.data.selectedOption;
    
    // 只有答错的时候才允许请求 AI，或者您可以放开限制
    if (!userSelected) {
      return wx.showToast({ title: '请先答题', icon: 'none' })
    }

    if (this.data.aiLoading) return; // 防止重复点击

    this.setData({ aiLoading: true, aiAnalysisResult: '' })

    request({
      url: `${app.globalData.baseUrl}/api/ai/explain`,
      method: 'GET',
      data: {
        questionContent: currentQ.content,
        userAnswer: userSelected,
        correctAnswer: currentQ.correctAnswer,
        options:[currentQ.optionA,currentQ.optionB,currentQ.optionC,currentQ.optionD]
      },
      success: (res) => {
        console.log(res);
        if (res.data.code === 200) {
          this.setData({
            aiLoading: false,
            aiAnalysisResult: res.data.data
          })
        } else {
          this.setData({ aiLoading: false })
          wx.showToast({ title: res.data.msg || 'AI 解析生成失败', icon: 'none' })
        }
      },
      fail: () => {
        this.setData({ aiLoading: false })
        wx.showToast({ title: '网络请求失败', icon: 'none' })
      }
    })
  },

  // 流式获取数据的函数
  // getAIAnalysis() {
  //   const currentQ = this.data.currentQuestion;
  //   const userSelected = this.data.selectedOption;

  //   if (!userSelected) {
  //     return wx.showToast({ title: '请先答题', icon: 'none' })
  //   }

  //   if (this.data.aiLoading) return;

  //   this.setData({ 
  //     aiLoading: true,
  //     aiAnalysisResult: ''
  //   });

  //   const requestTask = wx.request({
  //     url: `${app.globalData.baseUrl}/api/ai/explainStream`, // 假设后端流式接口地址
  //     method: 'GET',
  //     enableChunked: true, // 开启分块接收
  //     data: {
  //       questionContent: currentQ.content,
  //       userAnswer: userSelected,
  //       correctAnswer: currentQ.correctAnswer,
  //       options: [currentQ.optionA, currentQ.optionB, currentQ.optionC, currentQ.optionD]
  //     },
  //     success: (res) => {
  //       this.setData({ aiLoading: false });
  //     },
  //     fail: () => {
  //       this.setData({ aiAnalysisResult: '网络请求失败，请检查网络。', aiLoading: false });
  //     }
  //   });

  //   // 维护一个 buffer，用于处理被截断的数据块
  //   let bufferString = '';

  //   requestTask.onChunkReceived((res) => {
  //     // 1. ArrayBuffer 转为字符串 (简单兼容处理，若有中文乱码建议引入 TextDecoder polyfill)
  //     const arrayBuffer = res.data;
  //     let text = '';
  //     try {
  //       const uint8Array = new Uint8Array(arrayBuffer);
  //       // 处理中文的简单方式，如果基础库支持 TextDecoder 则更好
  //       text = decodeURIComponent(escape(String.fromCharCode.apply(null, uint8Array)));
  //     } catch (e) {
  //       text = String.fromCharCode.apply(null, new Uint8Array(arrayBuffer));
  //     }

  //     // 2. 将新收到的数据拼接到 buffer 中
  //     bufferString += text;

  //     // 3. 按照 SSE 的格式(双换行)进行分割处理
  //     const parts = bufferString.split('\n\n');
      
  //     // 最后一个部分可能是不完整的，留到下一次处理
  //     bufferString = parts.pop();

  //     for (let part of parts) {
  //       if (part.trim() === '') continue;
        
  //       // 假设后端返回格式为 "data: {JSON}"
  //       if (part.startsWith('data:')) {
  //         const content = part.substring(5).trim();
  //         if (content === '[DONE]') {
  //            this.setData({ aiLoading: false });
  //            continue;
  //         }
  //         try {
  //           // 这里假设后端返回的是 JSON 字符串，根据实际情况可能需要调整
  //           // 例如通义千问流式返回的 JSON 结构
  //           const jsonObj = JSON.parse(content);
  //           let chunkText = '';
            
  //           // 兼容不同的返回结构
  //           if (jsonObj.choices && jsonObj.choices[0].delta && jsonObj.choices[0].delta.content) {
  //              chunkText = jsonObj.choices[0].delta.content;
  //           } else if (jsonObj.output && jsonObj.output.text) {
  //              // 阿里云通义千问常见结构
  //              chunkText = jsonObj.output.text;
  //              // 通义千问流式输出通常是叠加的完整字符串，而不是增量，所以直接覆盖
  //              this.setData({
  //                aiAnalysisResult: chunkText,
  //                aiLoading: false
  //              });
  //              continue;
  //           } else if (typeof jsonObj === 'string') {
  //              chunkText = jsonObj;
  //           }
            
  //           if (chunkText) {
  //             this.setData({
  //               aiAnalysisResult: this.data.aiAnalysisResult + chunkText,
  //               aiLoading: false
  //             });
  //           }
  //         } catch (e) {
  //           // 如果后端直接返回纯文本而不是 JSON
  //           this.setData({
  //             aiAnalysisResult: this.data.aiAnalysisResult + content,
  //             aiLoading: false
  //           });
  //         }
  //       }
  //     }
  //   });
  // },

  goBack() {
    wx.navigateBack()
  }
})
