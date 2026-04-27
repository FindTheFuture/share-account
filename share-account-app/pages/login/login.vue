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
    <button class="login-btn" @click="showSmsLoginPopup">{{ loginButtonText }}</button>

    <!-- 微信登录按钮 - 仅微信小程序显示 -->
    <!-- #ifdef MP-WEIXIN -->
    <button class="wechat-login-btn" @click="onWechatLogin" :disabled="wechatDisabled">{{ wechatLoginText }}</button>
    <!-- #endif -->

    <!-- 环境提示 -->
    <view class="env-tip" v-if="showEnvTip">
      <text class="env-tip-text">{{ envTipMessage }}</text>
    </view>
      </view>
    </view>

    <!-- 手机号登录弹窗 -->
    <view class="sms-popup-mask" v-if="showSmsLogin" @click="hideSmsLoginPopup">
      <view class="sms-popup-card" @click.stop>
        <view class="sms-popup-header">
          <text class="sms-popup-title">手机号登录</text>
          <text class="sms-popup-close" @click="hideSmsLoginPopup">✕</text>
        </view>
        <view class="sms-popup-body">
          <view class="sms-input-wrapper">
            <input type="number" v-model="phone" placeholder="请输入手机号" maxlength="11" class="sms-popup-input" />
          </view>
          <view class="sms-input-wrapper verify-wrapper">
            <input type="number" v-model="smsCode" placeholder="请输入验证码" maxlength="6" class="sms-popup-input" />
            <button class="send-code-btn" :disabled="sendCodeDisabled" @click="sendSmsCode">{{ sendCodeText }}</button>
          </view>
        </view>
        <view class="sms-popup-footer">
          <button class="sms-login-submit" @click="onLogin" :disabled="isDisabled">{{ isDisabled ? '登录中...' : '登录' }}</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      loginButtonText: '手机号登录(新用户)',
      agreed: false,
      isDisabled: false,
      showCountdown: false,
      failTime: 0,
      showEnvTip: false,
      envTipMessage: '',
      isDevMode: process.env.NODE_ENV !== 'production',
      showSmsLogin: false,
      phone: '',
      smsCode: '',
      sendCodeText: '发送验证码',
      sendCodeDisabled: false,
      countdown: 60,
      wechatLoginText: '微信登录(老用户，即将下线)',
      wechatDisabled: false
    };
  },
  onLoad() {
    this.checkEnvironment();
  },
  methods: {
    checkEnvironment() {
      this.showEnvTip = false;
      return true;
    },
    
    onAgreeChange(e) {
      this.agreed = e.detail.value.length > 0;
    },

    showSmsLoginPopup() {
      if (!this.agreed) {
        uni.showToast({
          title: '请先阅读并勾选协议内容',
          icon: 'none',
          duration: 3000
        });
        return;
      }
      this.showSmsLogin = true;
    },

    hideSmsLoginPopup() {
      this.showSmsLogin = false;
    },

    sendSmsCode() {
      if (!this.agreed) {
        uni.showToast({
          title: '请先阅读并勾选协议内容',
          icon: 'none',
          duration: 3000
        });
        return;
      }
      
      if (!this.phone || this.phone.length !== 11) {
        uni.showToast({
          title: '请输入正确的手机号',
          icon: 'none',
          duration: 2000
        });
        return;
      }
      
      this.sendCodeDisabled = true;
      this.countdown = 60;
      
      uni.request({
        url: `${this.$backUrlConfig.baseUrl}${this.$backUrlConfig.endpoints.sms_send}`,
        method: 'POST',
        data: { phone: this.phone },
        success: (res) => {
          if (res.data.code == 200) {
            uni.showToast({
              title: '发送成功',
              icon: 'success',
              duration: 2000
            });
            this.startSendCountdown();
          } else {
            uni.showToast({
              title: res.data.message || '发送失败',
              icon: 'none',
              duration: 2000
            });
            this.sendCodeDisabled = false;
          }
        },
        fail: (err) => {
          uni.showToast({
            title: '发送失败，请稍后重试',
            icon: 'none',
            duration: 2000
          });
          this.sendCodeDisabled = false;
        }
      });
    },
    
    startSendCountdown() {
      const timer = setInterval(() => {
        if (this.countdown <= 0) {
          clearInterval(timer);
          this.sendCodeText = '发送验证码';
          this.sendCodeDisabled = false;
        } else {
          this.countdown--;
          this.sendCodeText = `${this.countdown}秒后重发`;
        }
      }, 1000);
    },

    onLogin() {
      if (!this.agreed) {
        uni.showToast({
          title: '请先阅读并勾选协议内容',
          icon: 'none',
          duration: 3000
        });
        return;
      }
      
      if (!this.phone || this.phone.length !== 11) {
        uni.showToast({
          title: '请输入正确的手机号',
          icon: 'none',
          duration: 2000
        });
        return;
      }
      
      if (!this.smsCode || this.smsCode.length !== 6) {
        uni.showToast({
          title: '请输入6位验证码',
          icon: 'none',
          duration: 2000
        });
        return;
      }

      this.isDisabled = true;
      this.loginButtonText = '登录中...';

      uni.request({
        url: `${this.$backUrlConfig.baseUrl}${this.$backUrlConfig.endpoints.login_sms}`,
        method: 'POST',
        data: { phone: this.phone, code: this.smsCode },
        success: (response) => {
          const data = response.data;

          if (data.code == 200 && data.data.token) {
            const { token, expires_in, additionalId, thunder, canSendMessage } = data.data;
            this.clearCache();
            uni.setStorageSync('isGuest', false);
            this.saveToken(token, expires_in, additionalId, thunder, canSendMessage);

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
            uni.showToast({
              title: data.message || '登录失败',
              icon: 'none',
              duration: 2000
            });
            this.handleLoginFailure(60);
          }
        },
        fail: (err) => {
          console.error('请求登录接口失败:', err);
          uni.showToast({
            title: '请求登录接口失败',
            icon: 'none',
            duration: 2000
          });
          this.handleLoginFailure(60);
        }
      });
    },

    onWechatLogin() {
      if (!this.agreed) {
        uni.showToast({
          title: '请先阅读并勾选协议内容',
          icon: 'none',
          duration: 3000
        });
        return;
      }

      this.wechatDisabled = true;
      this.wechatLoginText = '登录中...';

      uni.login({
        success: (loginRes) => {
          if (loginRes.code) {
            uni.request({
              url: `${this.$backUrlConfig.baseUrl}${this.$backUrlConfig.endpoints.login_login}`,
              method: 'POST',
              data: { code: loginRes.code },
              success: (response) => {
                const data = response.data;

                if (data.code == 200 && data.data.token) {
                  const { token, expires_in, additionalId, thunder, canSendMessage } = data.data;
                  this.clearCache();
                  uni.setStorageSync('isGuest', false);
                  this.saveToken(token, expires_in, additionalId, thunder, canSendMessage);

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
                    this.handleWechatLoginFailure(60);
                  });

                } else {
                  console.error('微信登录接口返回错误:', data);
                  this.handleWechatLoginFailure(data.failTime || 60);
                }
              },
              fail: (err) => {
                console.error('请求微信登录接口失败:', err);
                uni.showToast({
                  title: '请求登录接口失败',
                  icon: 'none',
                  duration: 2000
                });
                this.handleWechatLoginFailure(60);
              }
            });
          } else {
            console.error('获取登录code失败:', loginRes.errMsg);
            this.handleWechatLoginFailure(60);
          }
        },
        fail: (err) => {
          console.error('调用uni.login失败:', err);
          let errorMsg = '微信登录失败';
          if (err.errMsg) {
            errorMsg += ': ' + err.errMsg;
          }
          uni.showModal({
            title: '登录失败',
            content: errorMsg,
            showCancel: false,
            success: () => {
              this.wechatLoginText = '微信登录';
              this.wechatDisabled = false;
            }
          });
          this.handleWechatLoginFailure(60);
        }
      });
    },

    handleWechatLoginFailure(failTime) {
      this.wechatLoginText = `${failTime}秒后可重试`;
      this.wechatDisabled = true;
      setTimeout(() => {
        this.wechatLoginText = '微信登录';
        this.wechatDisabled = false;
      }, failTime * 1000);
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
          this.loginButtonText = '登录';
          this.showCountdown = false;
          this.isDisabled = false;
        } else {
          this.failTime = --failTime;
          this.loginButtonText = `${failTime}秒后可重试`;
        }
      }, 1000);
    },

    saveToken(token, expires_in, additionalId, thunder, canSendMessage) {
      uni.setStorageSync('token', token);
      uni.setStorageSync('additionalId', additionalId);
      const expireAt = Date.now() + expires_in * 1000;
      uni.setStorageSync('expireAt', expireAt);

      uni.setStorageSync('thunder', thunder);
      uni.setStorageSync('canSendMessage', canSendMessage);

      const app = getApp();
      app.globalData.token = token;
      app.globalData.expireAt = expireAt;
      app.globalData.additionalId = additionalId;
      app.globalData.thunder = thunder;
      app.globalData.canSendMessage = canSendMessage;
    },


    clearCache() {
      uni.removeStorageSync('token');
      uni.removeStorageSync('additionalId');
      uni.removeStorageSync('expireAt');
      uni.removeStorageSync('thunder');
      uni.removeStorageSync('canSendMessage');
      uni.removeStorageSync('isGuest');
      uni.removeStorageSync('guideCard');
    },

    goHomeList() {
      uni.switchTab({
        url: '/pages/firstpage/firstpage'
      });
    },

    redirectToReturnPage() {
      const redirectAfterLogin = uni.getStorageSync('redirectAfterLogin');
      if (redirectAfterLogin) {
        uni.removeStorageSync('redirectAfterLogin');
        tryToSwitchOrRelaunch(redirectAfterLogin);
        return;
      }
      
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

      tryToSwitchOrRelaunch('/pages/firstpage/firstpage');
    },

    checkLoginStatus() {
      return new Promise((resolve) => {
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
  bottom: 10vh;
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
  margin-bottom: 20rpx;
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

/* 手机号登录弹窗 */
.sms-popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.sms-popup-card {
  width: 600rpx;
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.15);
}

.sms-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.sms-popup-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.sms-popup-close {
  font-size: 32rpx;
  color: #999999;
}

.sms-popup-body {
  padding: 32rpx;
}

.sms-input-wrapper {
  margin-bottom: 20rpx;
}

.sms-input-wrapper.verify-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.sms-popup-input {
  width: 100%;
  height: 88rpx;
  background: #ffffff;
  border-radius: 44rpx;
  padding: 0 32rpx;
  font-size: 28rpx;
  color: #333333;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  border: 2rpx solid #e9eef3;
  box-sizing: border-box;
}

.sms-popup-input:focus {
  border-color: #07c160;
  box-shadow: 0 4rpx 16rpx rgba(7, 193, 96, 0.2);
}

.verify-wrapper .sms-popup-input {
  flex: 1;
}

.sms-popup-footer {
  padding: 0 32rpx 32rpx;
}

.sms-login-submit {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, #07c160 0%, #05a054 100%);
  color: #fff;
  border: none;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 700;
  box-shadow: 0 8rpx 24rpx rgba(7, 193, 96, 0.28);
}

.sms-login-submit[disabled] {
  background: #cccccc;
  box-shadow: none;
}

/* 短信登录表单样式 */
.sms-login-form {
  width: 100%;
  margin-bottom: 16rpx;
}

.input-group {
  margin-bottom: 20rpx;
}

.verify-group {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.sms-input {
  flex: 1;
  height: 88rpx;
  background: #ffffff;
  border-radius: 44rpx;
  padding: 0 32rpx;
  font-size: 28rpx;
  color: #333;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.send-code-btn {
  width: 200rpx;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, #07c160 0%, #05a054 100%);
  color: #fff;
  border: none;
  border-radius: 44rpx;
  font-size: 24rpx;
  font-weight: 500;
  padding: 0;
  margin: 0;
}

.send-code-btn[disabled] {
  background: #cccccc;
  color: #ffffff;
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

.wechat-login-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: linear-gradient(135deg, #07c160 0%, #05a054 100%);
  color: #fff;
  border: none;
  border-radius: 48rpx;
  text-align: center;
  font-size: 32rpx;
  font-weight: 700;
  margin-top: 14rpx;
  letter-spacing: 1rpx;
  box-shadow: 0 12rpx 24rpx rgba(7, 193, 96, 0.28);
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.wechat-login-btn:active {
  opacity: 0.96;
  transform: scale(0.985);
}

.wechat-login-btn[disabled] {
  background: #cccccc;
  color: #ffffff;
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