<template>
  <view class="guide-card" v-if="card">
    <view class="guide-header">
      <text class="guide-title">{{ card.title || '快速引导' }}</text>
      <text class="guide-close" @click="$emit('close')">×</text>
    </view>
    <view class="guide-content">
      <text class="guide-text">{{ card.content }}</text>
    </view>
    <view class="guide-footer">
      <button class="guide-cta" @click="handleCta">{{ card.ctaText || '去升级' }}</button>
    </view>
  </view>
</template>

<script>
import $backUrlConfig from '../common/back_url.js';

export default {
  name: 'GuideCard',
  props: {
    card: {
      type: Object,
      default: null
    }
  },
  methods: {
    handleCta() {
      // 优先尝试“游客升级为正式用户”，失败（例如已绑定其他账号）再跳转登录页
      uni.showLoading({ title: '升级中...' });
      try {
        uni.login({
          provider: 'weixin',
          success: ({ code }) => {
            if (!code) {
              uni.hideLoading();
              uni.showToast({ title: '获取微信code失败', icon: 'none' });
              return;
            }

            const token = uni.getStorageSync('token');
            const additionalId = uni.getStorageSync('additionalId');

            uni.request({
              url: `${$backUrlConfig.baseUrl}${$backUrlConfig.endpoints.login_upgrade}`,
              method: 'POST',
              header: {
                'content-type': 'application/json',
                ...(token ? { 'Aboluo': `Aboluo ${token}` } : {}),
                ...(additionalId ? { 'Additional': `${additionalId}` } : {})
              },
              data: { code },
              success: (res) => {
                const body = res.data || {};
                if (res.statusCode === 200 && body && body.code === 200 && body.data) {
                  const { token, refresh_token, expires_in, additionalId: newAdditionalId, thunder, canSendMessage } = body.data;
                  // 持久化令牌与用户信息
                  uni.setStorageSync('token', token);
                  uni.setStorageSync('additionalId', newAdditionalId);
                  uni.setStorageSync('refreshToken', refresh_token);
                  const expireAt = Date.now() + (expires_in || 0) * 1000;
                  uni.setStorageSync('expireAt', expireAt);
                  uni.setStorageSync('thunder', thunder);
                  uni.setStorageSync('canSendMessage', canSendMessage);
                  // 标记非游客并清理引导卡
                  uni.setStorageSync('isGuest', false);
                  uni.removeStorageSync('guideCard');

                  uni.hideLoading();
                  uni.showToast({ title: '升级成功', icon: 'success', duration: 1500 });

                  // 跳转首页
                  setTimeout(() => {
                    uni.switchTab({ url: '/pages/firstpage/firstpage' });
                  }, 500);
                } else {
                  const msg = (body && body.message) ? body.message : '升级失败，请稍后再试';
                  uni.hideLoading();
                  uni.showToast({ title: msg, icon: 'none', duration: 2000 });

                  // 若已绑定其他账号，分流到登录页
                  if (msg.includes('已绑定其他账号')) {
                    setTimeout(() => {
                      uni.navigateTo({ url: '/pages/login/login' });
                    }, 500);
                  }
                }
              },
              fail: (err) => {
                console.error('调用升级接口失败:', err);
                uni.hideLoading();
                uni.showToast({ title: '网络连接失败', icon: 'none' });
              }
            });
          },
          fail: (e) => {
            uni.hideLoading();
            uni.showToast({ title: '获取微信登录态失败', icon: 'none' });
          }
        });
      } catch (e) {
        uni.hideLoading();
        uni.showToast({ title: '升级过程异常', icon: 'none' });
      }
    }
  }
}
</script>

<style scoped>
.guide-card { 
  background: #fff; 
  border-radius: 8px; 
  box-shadow: 0 4px 12px rgba(0,0,0,0.08); 
  padding: 12px; 
  margin: 10px; 
  border: 1px solid #f0f0f0; 
}
.guide-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
}
.guide-title { 
  font-size: 16px; 
  font-weight: 600; 
}
.guide-close { 
  font-size: 20px; 
  color: #999; 
}
.guide-content { 
  margin-top: 8px; 
}
.guide-text { 
  font-size: 14px; 
  color: #666; 
  line-height: 1.6; 
}
.guide-footer { 
  margin-top: 12px; 
}
.guide-cta { 
  width: 100%; 
  height: 40px; 
  line-height: 40px; 
  background-color: #1989fa; 
  color: #fff; 
  border: none; 
  border-radius: 6px; 
}
</style>