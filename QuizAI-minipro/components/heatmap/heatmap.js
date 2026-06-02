const { request } = require('../../utils/request')
const app = getApp()

const COLOR_LEVELS = ['#EBEDF0', '#9BE9A8', '#40C463', '#30A14E', '#216E39']

Component({
  data: {
    grid: [],
    gridWidth: 0,       // 网格实际宽度(rpx)
    scrollLeft: 0,      // 滚动到最右侧的位置(px)
    totalQuestions: 0,
    activeDays: 0,
    tooltip: '',
    showTooltip: false,
    loading: true,
    hasError: false
  },

  lifetimes: {
    attached() {
      this.fetchHeatmapData()
    }
  },

  methods: {
    fetchHeatmapData() {
      this.setData({ loading: true, hasError: false })
      request({
        url: `${app.globalData.baseUrl}/api/component/heatmap`,
        method: 'GET',
        success: (res) => {
          if (res.data.code === 200) {
            this.buildGrid(res.data.data || [])
          } else {
            console.error('热力图接口返回错误:', res.data.msg)
            this.setData({ loading: false, hasError: true })
          }
        },
        fail: (err) => {
          console.error('热力图请求失败:', err)
          this.setData({ loading: false, hasError: true })
        }
      })
    },

    buildGrid(apiData) {
      const countMap = {}
      let totalQuestions = 0
      let activeDays = 0
      apiData.forEach(item => {
        countMap[item.date] = item.count
        totalQuestions += item.count
        if (item.count > 0) activeDays++
      })

      const MONTH_NAMES = ['1月', '2月', '3月', '4月', '5月', '6月',
                           '7月', '8月', '9月', '10月', '11月', '12月']

      // 从今天的本周一往前回退52周，确定起始周一
      const today = new Date()
      const todayWeekday = today.getDay() === 0 ? 6 : today.getDay() - 1
      const thisMonday = new Date(today)
      thisMonday.setDate(today.getDate() - todayWeekday)
      const startDate = new Date(thisMonday)
      startDate.setDate(thisMonday.getDate() - 52 * 7)

      // 计算实际需要的列数（到本周日为止，不含未来日期）
      const thisSunday = new Date(thisMonday)
      thisSunday.setDate(thisMonday.getDate() + 6)
      const totalDays = Math.ceil((thisSunday - startDate) / (1000 * 60 * 60 * 24)) + 1
      const cols = Math.ceil(totalDays / 7)

      // 预计算每个月第一次出现的列（用 year-month 做 key，区分不同年份）
      const monthFirstCol = {}
      for (let col = 0; col < cols; col++) {
        const d = new Date(startDate)
        d.setDate(startDate.getDate() + col * 7)
        const key = d.getFullYear() * 100 + d.getMonth() // 如 202504 = 2025年5月
        if (monthFirstCol[key] === undefined) {
          monthFirstCol[key] = col
        }
      }

      // 生成 7 x cols 的网格
      const grid = []
      for (let row = 0; row < 7; row++) {
        const rowArr = []
        for (let col = 0; col < cols; col++) {
          const d = new Date(startDate)
          d.setDate(startDate.getDate() + col * 7 + row)
          const dateStr = this.formatDate(d)
          const count = countMap[dateStr] || 0
          const key = d.getFullYear() * 100 + d.getMonth()

          rowArr.push({
            date: dateStr,
            count: count,
            color: this.getColor(count),
            shortDate: `${d.getMonth() + 1}/${d.getDate()}`,
            monthLabel: (row === 0 && monthFirstCol[key] === col) ? MONTH_NAMES[d.getMonth()] : ''
          })
        }
        grid.push(rowArr)
      }

      // 每列宽 24rpx + 6rpx间距
      const gridWidth = cols * 30

      this.setData({
        grid,
        gridWidth,
        totalQuestions,
        activeDays,
        loading: false,
        hasError: false
      })

      // 延迟滚动到最右边，确保 DOM 已渲染完成
      setTimeout(() => {
        this.scrollToEnd()
      }, 200)
    },

    getColor(count) {
      if (count <= 0) return COLOR_LEVELS[0]
      if (count <= 3) return COLOR_LEVELS[1]
      if (count <= 6) return COLOR_LEVELS[2]
      if (count <= 9) return COLOR_LEVELS[3]
      return COLOR_LEVELS[4]
    },

    formatDate(date) {
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    },

    onCellTap(e) {
      const { date, count, shortdate } = e.currentTarget.dataset
      const text = count > 0
        ? `${shortdate}: ${count} 题`
        : `${shortdate}: 未练习`
      this.setData({ tooltip: text, showTooltip: true })
      setTimeout(() => {
        this.setData({ showTooltip: false })
      }, 2000)
    },

    scrollToEnd() {
      const query = this.createSelectorQuery()
      query.select('.grid-scroll').boundingClientRect()
      query.select('.grid-inner').boundingClientRect()
      query.exec((res) => {
        if (res[0] && res[1]) {
          const containerWidth = res[0].width   // 容器可见宽度(px)
          const contentWidth = res[1].width      // 内容实际宽度(px)
          const scrollLeft = Math.max(0, contentWidth - containerWidth)
          this.setData({ scrollLeft })
        }
      })
    }
  }
})
