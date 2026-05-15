Component({
  data: {
    selected: 0,
    list: [
      {
        pagePath: "/pages/index/index",
        text: "首页",
        iconPath: "/assets/tabbar/home.png",
        selectedIconPath: "/assets/tabbar/home-active.png"
      },
      {
        pagePath: "/pages/chat/chat",
        text: "AI对话",
        isCenter: true
      },
      {
        pagePath: "/pages/profile/profile",
        text: "我的",
        iconPath: "/assets/tabbar/profile.png",
        selectedIconPath: "/assets/tabbar/profile-active.png"
      }
    ]
  },

  methods: {
    switchTab(e) {
      const data = e.currentTarget.dataset;
      const url = data.path;
      const index = data.index;

      if (this.data.selected === index) return;

      this.setData({ selected: index });

      wx.switchTab({ url });
    },

    setSelected(index) {
      this.setData({ selected: index });
    }
  }
});
