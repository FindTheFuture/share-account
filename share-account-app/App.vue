<template>
	<view class="app">
		<!-- 简化路由配置，移除keep-alive标签 -->
		<router-view></router-view>
	</view>
</template>

<script>
	import {
		mapMutations
	} from 'vuex'
	import {
		version
	} from './package.json'
	// #ifdef APP
	import checkUpdate from '@/uni_modules/uni-upgrade-center-app/utils/check-update';
	// #endif
	// 引入back_url配置
	import $backUrlConfig from './common/back_url.js';
	export default {
	provide() {
		return {
			$backUrlConfig
		}
	},
	onLaunch: function() {
			console.log('App Launch: 初始化应用');
			// 初始化全局数据
			if (!this.globalData) {
				this.globalData = {};
			}
		// #ifdef H5
		console.log(
			`%c hello uniapp %c v${version} `,
			'background:#35495e ; padding: 1px; border-radius: 3px 0 0 3px;  color: #fff',
			'background:#007aff ;padding: 1px; border-radius: 0 3px 3px 0;  color: #fff; font-weight: bold;'
		)
		// #endif
		console.log('App Launch');
		// #ifdef APP-PLUS
		// App平台检测升级
		if (plus.runtime.appid !== 'HBuilder') {
			checkUpdate()
		}

		// 一键登录预登陆
		uni.preLogin({
			provider: 'univerify',
			success: (res) => {
				this.setUniverifyErrorMsg();
				console.log("preLogin success: ", res);
			},
			fail: (res) => {
				this.setUniverifyLogin(false);
				this.setUniverifyErrorMsg(res.errMsg);
				console.log("preLogin fail res: ", res);
			}
		})
		// #endif
		try {
		  const savedColor = uni.getStorageSync('themePrimaryColor');
		  if (savedColor && typeof document !== 'undefined') {
		    document.documentElement.style.setProperty('--app-primary', savedColor);
		  }
		  if (savedColor && this.$store) {
		    this.$store.commit('setThemePrimaryColor', savedColor);
		  }
		  if (savedColor) { try { uni.setNavigationBarColor({ backgroundColor: savedColor, frontColor: '#ffffff' }) } catch (e) {} }
		} catch (e) {}
	},
	onShow: function() {
		console.log('App Show')
	},
	onHide: function() {
		console.log('App Hide')
	},
	methods: {
		...mapMutations([
			'setUniverifyLogin',
			'setUniverifyErrorMsg'
		])
	},
	globalData: {
		test: ''
	}
}
</script>

<style lang="scss">
	@import '@/uni_modules/uni-scss/index.scss';
	/* #ifndef APP-PLUS-NVUE */
	/* uni.css - 通用组件、模板样式库，可以当作一套ui库应用 */
	@import './common/uni.css';
	@import "/static/fonts/iconfont.css";
	
	/* H5 兼容 pc 所需 */
	/* #ifdef H5 */
	@media screen and (min-width: 768px) {
		body {
			overflow-y: scroll;
		}
	}

	/* 顶栏通栏样式 */
	/* .uni-top-window {
	    left: 0;
	    right: 0;
	} */

	uni-page-body {
		background-color: #F5F5F5 !important;
		min-height: 100% !important;
		height: auto !important;
	}

	.uni-app--showleftwindow .hideOnPc {
		display: none !important;
	}

	/* #endif */

	/* 以下样式用于 hello uni-app 演示所需 */
	page {
		background-color: #efeff4;
		height: 100%;
		font-size: 28rpx;
		/* line-height: 1.8; */
	}

	.fix-pc-padding {
		padding: 0 50px;
	}

	.uni-header-logo {
		padding: 30rpx;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		margin-top: 10rpx;
	}

	.uni-header-image {
		width: 100px;
		height: 100px;
	}

	.uni-hello-text {
		color: #7A7E83;
	}

	.uni-hello-addfile {
		text-align: center;
		line-height: 300rpx;
		background: #FFF;
		padding: 50rpx;
		margin-top: 10px;
		font-size: 38rpx;
		color: #808080;
	}

	/* #endif*/
/* 全局主题变量与工具类 */
:root { --app-primary: #ffffff; }
.theme-primary-bg { background-color: var(--app-primary); }
.theme-primary-color { color: var(--app-primary); }
/* 让常用主按钮跟随主题色 */
button[type="primary"] { background-color: var(--app-primary) !important; border-color: var(--app-primary) !important; }
button[type="primary"][plain] { color: var(--app-primary) !important; border-color: var(--app-primary) !important; }
/* 顶部标题栏组件 */
.common-page-head { background-color: var(--app-primary) !important; }
.common-page-head-title { color: #fff !important; border-bottom-color: rgba(255,255,255,0.65) !important; }
/* 导航栏（uni-nav-bar） */
.uni-navbar__content, .uni-navbar__header { background-color: var(--app-primary) !important; }
.uni-nav-bar-text, .uni-nav-bar-right-text, .uni-navbar-btn-text text { color: #fff !important; }
/* 常见内容块背景 */
.box-bg { background-color: var(--app-primary) !important; }
/* 页面背景 */
page { background-color: var(--app-primary) !important; }
uni-page-body { background-color: var(--app-primary) !important; }
</style>
