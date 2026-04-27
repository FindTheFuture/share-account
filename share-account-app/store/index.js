import { createStore } from 'vuex'
const store = createStore({
	state: {
		hasLogin: false,
		isUniverifyLogin: false,
		loginProvider: "",
		openid: null,
		testvuex: false,
		colorIndex: 0,
		colorList: ['#FF0000', '#00FF00', '#0000FF'],
		noMatchLeftWindow: true,
		active: 'componentPage',
		leftWinActive: '/pages/component/view/view',
		activeOpen: '',
		menu: [],
		univerifyErrorMsg: '',
		// vuex测试例使用
		username: "foo",
		sex: "男",
		age: 10,
		themePrimaryColor: '#ffffff',
	},
	mutations: {
		login(state, provider) {
			state.hasLogin = true;
			state.loginProvider = provider;
		},
		logout(state) {
			state.hasLogin = false
			state.openid = null
		},
		setOpenid(state, openid) {
			state.openid = openid
		},
		setTestTrue(state) {
			state.testvuex = true
		},
		setTestFalse(state) {
			state.testvuex = false
		},
		setColorIndex(state, index) {
			state.colorIndex = index
		},
		setMatchLeftWindow(state, matchLeftWindow) {
			state.noMatchLeftWindow = !matchLeftWindow
		},
		setActive(state, tabPage) {
			state.active = tabPage
		},
		setLeftWinActive(state, leftWinActive) {
			state.leftWinActive = leftWinActive
		},
		setActiveOpen(state, activeOpen) {
			state.activeOpen = activeOpen
		},
		setMenu(state, menu) {
			state.menu = menu
		},
		setUniverifyLogin(state, payload) {
			typeof payload !== 'boolean' ? payload = !!payload : '';
			state.isUniverifyLogin = payload;
		},
		setUniverifyErrorMsg(state,payload = ''){
			state.univerifyErrorMsg = payload
		},
		// vuex测试例使用
		increment(state) {
		  state.age++;
		},
		incrementTen(state, payload) {
		  state.age += payload.amount
		},
		resetAge(state){
		  state.age = 10
		},
		setThemePrimaryColor(state, color) {
		  state.themePrimaryColor = color
		  const pickFrontColor = (hex) => {
		    try {
		      const c = (hex || '').replace('#','')
		      if (c.length < 6) return '#ffffff'
		      const r = parseInt(c.slice(0,2),16), g = parseInt(c.slice(2,4),16), b = parseInt(c.slice(4,6),16)
		      const luminance = (0.299*r + 0.587*g + 0.114*b)
		      return luminance > 186 ? '#000000' : '#ffffff'
		    } catch (e) { return '#ffffff' }
		  }
		  const frontColor = pickFrontColor(color)
		  try {
		    if (typeof document !== 'undefined') {
		      document.documentElement.style.setProperty('--app-primary', color)
		    }
		  } catch (e) {}
		  try { uni.setStorageSync('themePrimaryColor', color) } catch (e) {}
		  try { uni.setNavigationBarColor({ backgroundColor: color, frontColor: frontColor }) } catch (e) {}
		},
	},
	getters: {
		currentColor(state) {
			return state.colorList[state.colorIndex]
		},
		// vuex测试例使用
		doubleAge(state) {
		  return state.age * 2;
		},
		themePrimaryColor: (state) => state.themePrimaryColor,
	},
	actions: {
		incrementAsync(context , payload) {
		  context.commit('incrementTen',payload)
		},
		getUserOpenId: async function({
			commit,
			state
		}) {
			return await new Promise((resolve, reject) => {
				if (state.openid) {
					resolve(state.openid)
				} else {
					uni.login({
						success: (data) => {
							commit('login')
							setTimeout(function() {
								const openid = '123456789'
								console.log('uni.request mock openid[' + openid + ']');
								commit('setOpenid', openid)
								resolve(openid)
							}, 1000)
						},
						fail: (err) => {
							console.log('uni.login 接口调用失败，将无法正常使用开放接口等服务', err)
							reject(err)
						}
					})
				}
			})
		},
		fetchThemeConfig: async function({ commit, state }) {
			try {
				const baseUrl = uni.$backUrlConfig?.baseUrl;
				const res = await new Promise((resolve, reject) => {
					uni.request({
						url: `${baseUrl}/config/theme`,
						method: 'GET',
						success: (res) => {
							if (res.data.code == 200) {
								resolve(res.data.data)
							} else {
								reject(new Error(res.data.message || '获取主题配置失败'))
							}
						},
						fail: (err) => {
							reject(err)
						}
					})
				})
				
				if (res.primaryColor) {
					commit('setThemePrimaryColor', res.primaryColor)
				}
				
				return res
			} catch (e) {
				console.error('获取主题配置失败:', e)
				return null
			}
		}
	}
})

export default store
