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
      <button class="guide-cta" @click="handleCta">{{ card.ctaText || '去登录' }}</button>
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
      try {
        // 直接跳转到登录页面
        uni.navigateTo({ url: '/pages/login/login' });
      } catch (e) {
        console.error('操作过程异常:', e);
        // 即使出错也要跳转到登录页面
        uni.navigateTo({ url: '/pages/login/login' });
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