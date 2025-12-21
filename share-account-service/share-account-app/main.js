import App from './App.vue'
import store from './store'
import $backUrlConfig from './common/back_url.js'
import request from './common/request.js'
import { uploadFile } from './common/upload.js' // 引入上传模块

// 计算前景色（黑/白）以保证对比度
function pickFrontColor(hex) {
  try {
    const c = (hex || '').replace('#','')
    if (c.length < 6) return '#ffffff'
    const r = parseInt(c.slice(0,2),16), g = parseInt(c.slice(2,4),16), b = parseInt(c.slice(4,6),16)
    const luminance = (0.299*r + 0.587*g + 0.114*b)
    return luminance > 186 ? '#000000' : '#ffffff'
  } catch (e) { return '#ffffff' }
}

// 判断当前是否 TabBar 页面（用于微信/抖音小程序、App）
const TABBAR_PAGES = ['/pages/firstpage/firstpage','/pages/mycenter/mycenter']
function isTabBarPage() {
  try {
    if (typeof getCurrentPages !== 'function') return false
    const pages = getCurrentPages()
    const cur = pages[pages.length - 1]
    const route = '/' + (cur?.route || cur?.$page?.route || '')
    return TABBAR_PAGES.includes(route)
  } catch (e) {
    return false
  }
}

// #ifndef VUE3
import Vue from 'vue'
import customIcon from './components/custom-icon/custom-icon.vue';
Vue.config.productionTip = false

Vue.prototype.$store = store
Vue.prototype.$adpid = "1111111111"
Vue.prototype.$backUrlConfig = $backUrlConfig
Vue.prototype.$request = request
Vue.prototype.$uploadFile = uploadFile // 挂载上传模块到Vue原型
Vue.component('custom-icon', customIcon);
Vue.prototype.$backgroundAudioData = {
  playing: false,
  playTime: 0,
  formatedPlayTime: '00:00:00'
}

// 全局混入：每个页面 onShow 时应用主题（微信/抖音小程序、App 生效）
Vue.mixin({
  onShow() {
    const color = uni.getStorageSync('themePrimaryColor') || store.state.themePrimaryColor || '#2979ff'
    const frontColor = pickFrontColor(color)
    try { uni.setNavigationBarColor({ backgroundColor: color, frontColor }) } catch (e) {}
    try { uni.setBackgroundColor({ backgroundColor: color, backgroundColorTop: color, backgroundColorBottom: color }) } catch (e) {}
  }
})

App.mpType = 'app'
const app = new Vue({
  store,
  ...App
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
import * as Pinia from 'pinia'
import customIconVue3 from './components/custom-icon/custom-icon.vue'

export function createApp() {
  const app = createSSRApp(App)
  
  app.use(store)
  app.use(Pinia.createPinia())
  
  app.config.globalProperties.$adpid = "1111111111"
  app.config.globalProperties.$backUrlConfig = $backUrlConfig
  app.config.globalProperties.$request = request
  app.config.globalProperties.$uploadFile = uploadFile // 挂载上传模块到Vue3全局属性
  app.component('custom-icon', customIconVue3) // 全局注册自定义图标组件
  app.config.globalProperties.$backgroundAudioData = {
    playing: false,
    playTime: 0,
    formatedPlayTime: '00:00:00'
  }

  // 全局混入：每个页面 onShow 时应用主题（微信/抖音小程序、App 生效）
  app.mixin({
    onShow() {
      const color = uni.getStorageSync('themePrimaryColor') || store.state.themePrimaryColor || '#ffffff'
      const frontColor = pickFrontColor(color)
      try { uni.setNavigationBarColor({ backgroundColor: color, frontColor }) } catch (e) {}
      try { uni.setBackgroundColor({ backgroundColor: color, backgroundColorTop: color, backgroundColorBottom: color }) } catch (e) {}
    }
  })
  
  return {
    app,
    Pinia
  }
}
// #endif
