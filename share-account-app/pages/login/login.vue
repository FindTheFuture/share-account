<template>
  <view class="container">
    <image class="bg-image" src="https://shareaccount-1302778096.cos.ap-beijing.myqcloud.com/static/login.webp" mode="aspectFill"></image>

    <!-- 操作区（居中下部） -->
    <view class="actions-wrapper">
      <view class="actions-card">
        <!-- 用户协议勾选 -->
        <checkbox-group class="agreement" @change="onAgreeChange">
          <view class="agreement-content">
            <label class="checkbox-wrapper">
              <checkbox :checked="agreed" value="agree" />
            </label>
            <text class="agreement-text">我已阅读并同意</text>
            <navigator url="/pages/login/agreementText" hover-class="none" class="agreement-link" open-type="navigate">
              《用户协议》
            </navigator>
            <text class="agreement-text">及</text>
            <navigator url="/pages/login/privacyText" hover-class="none" class="agreement-link" open-type="navigate">
              《隐私保护指引》
            </navigator>
          </view>
        </checkbox-group>

    <!-- 轻提示文案：放在登录按钮上方 -->
    <view class="promo-tip">
      <text>记账 3 天，帮你摸清每月钱花在哪</text>
    </view>

    <!-- 登录按钮 -->
    <button class="login-btn" @click="onLogin" :disabled="isDisabled">{{ loginButtonText }}</button>

    <!-- 游客体验按钮 -->
    <button class="guest-btn" @click="guestLogin" :disabled="isDisabled">游客体验</button>

    <!-- 环境提示 -->
    <view class="env-tip" v-if="showEnvTip">
      <text class="env-tip-text">{{ envTipMessage }}</text>
    </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      loginButtonText: '微信登录',
      agreed: false,
      isDisabled: false,
      showCountdown: false,
      failTime: 0,
      showEnvTip: false,
      envTipMessage: '',
      isDevMode: process.env.NODE_ENV !== 'production' // 仅开发环境显示测试按钮
    };
  },
  onLoad() {
    // 检查当前环境
    this.checkEnvironment();
  },
  methods: {
    // 检查当前运行环境
    checkEnvironment() {
      const platform = uni.getSystemInfoSync().platform;
      console.log('当前运行平台:', platform);
      
      // 在微信开发者工具中，即使oauth为空也允许尝试登录
      const isWechatDevtools = platform === 'devtools';
      const oauthProviders = uni.getProvider({ service: 'oauth' }).oauth || [];
      console.log('可用的OAuth提供商:', oauthProviders);
      
      // 非微信小程序环境提示
      // 对于安卓和iOS设备，即使oauthProviders中没有找到'weixin'也允许尝试登录
      if (!isWechatDevtools && platform !== 'android' && platform !== 'ios' && (!Array.isArray(oauthProviders) || oauthProviders.indexOf('weixin') === -1)) {
        this.showEnvTip = true;
        this.envTipMessage = '当前环境不支持微信登录，请在微信小程序中打开';
        return false;
      }
      
      // 非真机或开发者工具环境提示
      if (platform !== 'devtools' && platform !== 'ios' && platform !== 'android') {
        this.showEnvTip = true;
        this.envTipMessage = `当前运行在${platform}环境，微信登录可能无法正常工作。建议在微信小程序环境中使用。`;
        // 允许继续尝试登录
        // return false;
      }
      
      return true;
    },
    
    onAgreeChange(e) {
      this.agreed = e.detail.value.length > 0;
    },

    onLogin() {
      // 再次检查环境
      if (!this.checkEnvironment()) {
        return;
      }
      
      if (!this.agreed) {
        uni.showToast({
          title: '请先阅读并勾选协议内容',
          icon: 'none',
          duration: 3000
        });
        return;
      }

      this.isDisabled = true;
      this.loginButtonText = '登录中...';

      uni.login({
        success: (loginRes) => {
          if (loginRes.code) {
            const request = { code: loginRes.code };
            
            uni.request({
              url: `${this.$backUrlConfig.baseUrl}${this.$backUrlConfig.endpoints.login_login}`,
              method: 'POST',
              data: request,
              success: (response) => {
                console.log('登录接口响应:', response);
                const data = response.data;

                if (data.code == 200 && data.data.token && data.data.refresh_token) {
                  // 登录成功处理
                  const { token, refresh_token, expires_in, additionalId, thunder, canSendMessage, isNewUser } = data.data;
                  this.saveToken(token, refresh_token, expires_in, additionalId, thunder, canSendMessage, isNewUser);
                  // 正式登录：清除游客引导状态
                  uni.setStorageSync('isGuest', false);
                  uni.removeStorageSync('guideCard');

                  this.checkLoginStatus().then(isLoggedIn => {
                    if (isLoggedIn) {
                      uni.showToast({
                        title: '登录成功',
                        icon: 'success',
                        duration: 2000
                      });

                      setTimeout(() => {
                        this.redirectToReturnPage();
                      }, 500);
                    } else {
                      throw new Error('Token验证失败');
                    }
                  }).catch(error => {
                    console.error('验证登录状态失败:', error);
                    this.handleLoginFailure(60);
                  });

                } else {
                  console.error('登录接口返回错误:', data);
                  this.handleLoginFailure(data.failTime || 60);
                }
              },
              fail: (err) => {
                console.error('请求登录接口失败:', err);
                
                // 特殊错误处理
                let errorMsg = '请求登录接口失败';
                if (err.errMsg) {
                  errorMsg += ': ' + err.errMsg;
                }
                
                if (err.errMsg && err.errMsg.includes('url')) {
                  errorMsg += '\n\n请检查：\n1. 微信公众平台中是否配置了合法域名\n2. API接口域名是否在白名单中';
                }
                
                uni.showModal({
                  title: '登录失败',
                  content: errorMsg,
                  showCancel: false
                });
                
                this.handleLoginFailure(60);
              }
            });
          } else {
            console.error('获取登录code失败:', loginRes.errMsg);
            this.handleLoginFailure(60);
          }
        },
        fail: (err) => {
          // 增强错误日志
          console.error('调用uni.login失败:', err);
          
          let errorMsg = '微信登录失败';
          if (err.errMsg) {
            errorMsg += ': ' + err.errMsg;
          }
          
          // 特殊错误处理
          if (err.errMsg && err.errMsg.includes('appid')) {
            errorMsg += '\n\n请检查：\n1. manifest.json中的AppID是否正确\n2. 微信公众平台中的AppID和AppSecret是否匹配';
          } else if (err.errMsg && err.errMsg.includes('url')) {
            errorMsg += '\n\n请检查：\n1. 微信公众平台中是否配置了合法域名\n2. API接口域名是否在白名单中';
          }
          
          uni.showModal({
            title: '登录失败',
            content: errorMsg,
            showCancel: false,
            success: () => {
              // 重置登录按钮状态
              this.loginButtonText = '微信登录';
              this.isDisabled = false;
            }
          });
          
          this.handleLoginFailure(60);
        }
      });
    },

    // 游客登录
    guestLogin() {
      if (!this.agreed) {
        uni.showToast({
          title: '请先阅读并勾选协议内容',
          icon: 'none',
          duration: 3000
        });
        return;
      }

      this.isDisabled = true;
      uni.showLoading({ title: '登录中...' });

      uni.request({
        url: `${this.$backUrlConfig.baseUrl}${this.$backUrlConfig.endpoints.login_guest}`,
        method: 'POST',
        success: (response) => {
          console.log('游客登录响应:', response);
          const data = response.data;

          if (data.code == 200 && data.data && data.data.token && data.data.refresh_token) {
            const { token, refresh_token, expires_in, additionalId, thunder, canSendMessage, isNewUser, guideCard } = data.data;
            this.saveToken(token, refresh_token, expires_in, additionalId, thunder, canSendMessage, isNewUser);
            // 游客登录：存储引导卡片和游客标记
            if (guideCard) {
              try { uni.setStorageSync('guideCard', JSON.stringify(guideCard)); } catch (e) { uni.setStorageSync('guideCard', guideCard); }
            }
            uni.setStorageSync('isGuest', true);
            
            this.checkLoginStatus().then(isLoggedIn => {
              if (isLoggedIn) {
                uni.hideLoading();
                uni.showToast({
                  title: '登录成功',
                  icon: 'success',
                  duration: 2000
                });

                setTimeout(() => {
                  this.redirectToReturnPage();
                }, 500);
              } else {
                throw new Error('Token验证失败');
              }
            }).catch(error => {
              console.error('游客登录后验证失败:', error);
              uni.hideLoading();
              this.handleLoginFailure(60);
            });
          } else {
            console.error('游客登录接口返回错误:', data);
            uni.hideLoading();
            this.handleLoginFailure(data.failTime || 60);
          }
        },
        fail: (err) => {
          console.error('请求游客登录接口失败:', err);
          uni.hideLoading();
          
          let errorMsg = '请求游客登录接口失败';
          if (err.errMsg) {
            errorMsg += ': ' + err.errMsg;
          }
          
          uni.showModal({
            title: '登录失败',
            content: errorMsg,
            showCancel: false
          });
          this.handleLoginFailure(60);
        },
        complete: () => {
          this.isDisabled = false;
        }
      });
    },



    handleLoginFailure(failTime) {
      this.loginButtonText = `${failTime}秒后可重试`;
      this.showCountdown = true;
      this.failTime = failTime;
      this.isDisabled = true;
      this.startCountdown();
    },

    startCountdown() {
      let failTime = this.failTime;
      const timer = setInterval(() => {
        if (failTime <= 0) {
          clearInterval(timer);
          this.loginButtonText = '微信登录';
          this.showCountdown = false;
          this.isDisabled = false;
        } else {
          this.failTime = --failTime;
          this.loginButtonText = `${failTime}秒后可重试`;
        }
      }, 1000);
    },

    saveToken(token, refresh_token, expires_in, additionalId, thunder, canSendMessage, isNewUser) {
      uni.setStorageSync('token', token);
      uni.setStorageSync('additionalId', additionalId);
      uni.setStorageSync('refreshToken', refresh_token);
      const expireAt = Date.now() + expires_in * 1000;
      uni.setStorageSync('expireAt', expireAt);

      uni.setStorageSync('thunder', thunder);
      uni.setStorageSync('canSendMessage', canSendMessage);

      const app = getApp();
      app.globalData.token = token;
      app.globalData.refreshToken = refresh_token;
      app.globalData.expireAt = expireAt;
      app.globalData.additionalId = additionalId;
      app.globalData.thunder = thunder;
      app.globalData.canSendMessage = canSendMessage;
    },

    goHomeList() {
      uni.switchTab({
        url: '/pages/firstpage/firstpage' // 改为首页地址
      });
    },

    redirectToReturnPage() {
      // 首先检查我们在request.js中保存的重定向URL
      const redirectAfterLogin = uni.getStorageSync('redirectAfterLogin');
      if (redirectAfterLogin) {
        uni.removeStorageSync('redirectAfterLogin');
        tryToSwitchOrRelaunch(redirectAfterLogin);
        return;
      }
      
      // 兼容原有逻辑
      const returnPage = uni.getStorageSync('returnPage');
      if (returnPage && returnPage.path) {
        uni.removeStorageSync('returnPage');

        let urlWithParams = `/${returnPage.path}`;
        if (returnPage.options) {
          urlWithParams += '?' + Object.keys(returnPage.options).map(key => `${key}=${encodeURIComponent(returnPage.options[key])}`).join('&');
        }

        tryToSwitchOrRelaunch(urlWithParams);
        return;
      }

      // 默认返回首页
      tryToSwitchOrRelaunch('/pages/firstpage/firstpage');
    },

    checkLoginStatus() {
      return new Promise((resolve) => {
        // 这里可以请求一个需要鉴权的接口来确认token是否有效
        // 为了简化，这里直接检查本地是否有token且未过期
        const token = uni.getStorageSync('token');
        const expireAt = uni.getStorageSync('expireAt');
        const isValid = token && expireAt && Date.now() < expireAt;
        resolve(!!isValid);
      });
    }
  }
}

function tryToSwitchOrRelaunch(url) {
  // 先尝试switchTab，如果失败则使用reLaunch
  uni.switchTab({
    url,
    fail: () => {
      uni.reLaunch({ url });
    }
  });
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  background-color: #f7f8fa;
  padding: 24rpx;
  overflow: hidden;
}

/* 顶部 hero 区域 */
.hero {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 34vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 操作区定位到屏幕中下部 */
.actions-wrapper {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 15vh;
  display: flex;
  justify-content: center;
  z-index: 1;
}

/* 全屏背景图（避免 WXSS 本地图片限制） */
.bg-image {
  position: absolute;
  inset: 0;
  width: 100vw;
  height: 100vh;
  z-index: 0;
  pointer-events: none;
}

/* 背景遮罩，提升可读性 */
.bg-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,233,242,0.55) 0%, rgba(244,247,255,0.45) 50%, rgba(232,255,245,0.55) 100%);
  z-index: 0;
}

/* 柔和背景泡泡 */
.bubble {
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
  opacity: 0.9;
}
.bubble-1 {
  width: 160rpx;
  height: 160rpx;
  left: 8vw;
  top: 4vh;
  background: radial-gradient(circle, #FFD6E7 0%, #FFE9F2 100%);
}
.bubble-2 {
  width: 220rpx;
  height: 220rpx;
  right: 10vw;
  top: 2vh;
  background: radial-gradient(circle, #D6F0FF 0%, #F4F7FF 100%);
}
.bubble-3 {
  width: 140rpx;
  height: 140rpx;
  left: 20vw;
  bottom: -2vh;
  background: radial-gradient(circle, #C8F7DC 0%, #E8FFF5 100%);
}

/* 装饰星星 */
.decor-star {
  position: absolute;
  width: 48rpx;
  height: 48rpx;
  opacity: 0.85;
}
.star-1 { top: 5vh; left: 12vw; }
.star-2 { top: 6vh; right: 14vw; }
.star-3 { bottom: 3vh; left: 28vw; }

.logo {
  width: 68vw;
  max-width: 660rpx;
  height: 24vh;
  margin-top: 5vh;
  margin-bottom: 2vh;
  filter: drop-shadow(0 6rpx 18rpx rgba(0,0,0,0.08));
}

/* 操作区卡片 */
.actions-card {
  position: relative;
  z-index: 1;
  max-width: 720rpx;
}

.agreement {
  margin: 0px 0;
}

.agreement-content {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.checkbox-wrapper {
  display: inline-block;
  margin-right: 8rpx;
}

.agreement-text {
  font-size: 24rpx;
  color: #666;
  margin: 0 4rpx;
}

.agreement-link {
  color: #007aff;
  font-size: 24rpx;
  margin: 0 4rpx;
  text-decoration: underline;
  display: inline-block;
}

.promo-tip {
  width: 100%;
  text-align: center;
  color: #8c8c8c;
  font-size: 24rpx;
  box-sizing: border-box;
}

.login-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: #007aff;
  color: #fff;
  border: none;
  border-radius: 48rpx;
  text-align: center;
  font-size: 32rpx;
  font-weight: 700;
  margin-top: 16rpx;
  letter-spacing: 1rpx;
  box-shadow: 0 12rpx 24rpx rgba(7, 193, 96, 0.28);
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.login-btn:active {
  opacity: 0.96;
  transform: scale(0.985);
}

.guest-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: linear-gradient(90deg, #A9B4FF, #FFB1D3);
  color: #fff;
  border: none;
  border-radius: 48rpx;
  text-align: center;
  font-size: 32rpx;
  font-weight: 700;
  margin-top: 14rpx;
  letter-spacing: 1rpx;
  box-shadow: 0 12rpx 24rpx rgba(255, 177, 211, 0.26);
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.guest-btn:active {
  opacity: 0.96;
  transform: scale(0.985);
}

.env-tip {
  position: relative;
  z-index: 1;
  margin-top: 10px;
  background: #ffffffcc;
  border: 1rpx solid #e9eef3;
  color: #5c5c5c;
  padding: 12px 16px;
  border-radius: 8rpx;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.08);
  width: 86vw;
  max-width: 720rpx;
}

.env-tip-text {
  font-size: 12px;
}

.fixed-top-left {
  position: fixed;
  top: 10px;
  left: 10px;
  z-index: 999;
}

.goHomeList {
  background-color: #007aff;
  color: #fff;
  padding: 6px 12px;
  border-radius: 4px;
}
</style>