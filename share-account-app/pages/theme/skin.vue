<template>
  <view class="skin-page">
    <view class="tabs">
      <view :class="['tab-item', activeTab==='free' ? 'active' : '']" @tap="activeTab='free'">免费皮肤</view>
      <view :class="['tab-item', activeTab==='vip' ? 'active' : '']" @tap="activeTab='vip'">会员专属</view>
    </view>

    <view v-if="activeTab==='free'" class="free-tab">
      <view class="section-title">颜色选项（点击应用）</view>
      <view class="color-grid">
        <view v-for="(c,i) in freeColors" :key="i" class="color-item" :style="{ backgroundColor: c.hex }" @tap="applyColor(c)">
          <text class="color-hex" :style="{ color: textColor(c.hex) }">{{ c.name }}</text>
        </view>
      </view>
    </view>

    <view v-else class="vip-tab">
      <view class="section-title">会员专属皮肤，后续上线，关注服务号抢先知道</view>
      <view class="color-grid">
        <view v-for="(s,i) in vipSkins" :key="s.id || i" class="color-item" :style="{ backgroundColor: s.previewColor }" @tap="applySkin(s)">
          <text class="color-hex" :style="{ color: textColor(s.previewColor) }">{{ s.name }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapMutations } from 'vuex'
import request from '@/common/request.js'
import $backUrlConfig from '@/common/back_url.js'

export default {
  data() {
    return {
      activeTab: 'free',
      freeColors: [],
      vipSkins: []
    }
  },
  onLoad() {
    this.fetchFreeColors()
    this.fetchVipSkins()
  },
  methods: {
    ...mapMutations(['setThemePrimaryColor']),
    async fetchFreeColors() {
      try {
        const data = await request.get($backUrlConfig.endpoints.theme_freeColors)
        this.freeColors = Array.isArray(data) ? data : []
      } catch (e) {
        this.freeColors = []
      }
    },
    async fetchVipSkins() {
      try {
        const data = await request.get($backUrlConfig.endpoints.theme_skins_vip)
        this.vipSkins = Array.isArray(data) ? data : []
      } catch (e) {
        this.vipSkins = []
      }
    },
    async applyColor(color) {
      try {
        await request.post($backUrlConfig.endpoints.theme_apply, { colorHex: color.hex })
        this.setThemePrimaryColor(color.hex)
        uni.showToast({ title: '已应用全局颜色', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: (e && e.message) || '应用失败', icon: 'none' })
      }
    },
    async applySkin(skin) {
      try {
        const res = await request.post($backUrlConfig.endpoints.theme_apply, { skinId: skin.id })
        const newColor = (res && res.primaryColor) || skin.previewColor
        if (newColor) this.setThemePrimaryColor(newColor)
        uni.showToast({ title: '已应用会员皮肤', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: (e && e.message) || '应用失败', icon: 'none' })
      }
    },
    textColor(hex) {
      // 根据背景色亮度选择对比文字颜色
      const h = hex.replace('#','')
      const r = parseInt(h.substring(0,2),16)
      const g = parseInt(h.substring(2,4),16)
      const b = parseInt(h.substring(4,6),16)
      // YIQ 对比公式
      const yiq = (r*299 + g*587 + b*114) / 1000
      return yiq >= 150 ? '#000' : '#fff'
    }
  }
}
</script>

<style scoped>
.skin-page { display: flex; flex-direction: column; min-height: 100vh; background-color: var(--app-primary); }
.header { height: 180rpx; display: flex; align-items: flex-end; padding: 24rpx; }
.header-title { font-size: 36rpx; color: #fff; font-weight: 600; }
.tabs { display: flex; background: #fff; border-bottom: 1rpx solid #eee; }
.tab-item { flex: 1; text-align: center; padding: 24rpx 0; font-size: 28rpx; color: #374151; }
.tab-item.active { color: var(--app-primary); font-weight: 600; border-bottom: 4rpx solid var(--app-primary); }
.section-title { padding: 24rpx; font-size: 28rpx; color: #6b7280; }
.color-grid { display: flex; flex-wrap: wrap; padding: 0 24rpx 24rpx; gap: 20rpx; }
.color-item { width: calc((100% - 80rpx) / 3); height: 140rpx; border-radius: 16rpx; position: relative; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.08); }
.color-hex { position: absolute; bottom: 12rpx; left: 12rpx; right: 12rpx; font-size: 24rpx; color: #fff; text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.2); }
.vip-placeholder { margin: 24rpx; padding: 24rpx; background: #fff; border-radius: 12rpx; color: #6b7280; font-size: 26rpx; line-height: 1.6; }
</style>