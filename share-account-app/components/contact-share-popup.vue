<template>
  <uni-popup ref="popup" type="center" :mask-click="true" @maskClick="close">
    <view class="popup-container">
      <view class="popup-header">
        <text class="popup-title">选择联系人</text>
        <custom-icon type="guanbi" :size="20" color="#999" @tap="close" class="icon-hover"></custom-icon>
      </view>
      <view class="divider"></view>
      <view class="popup-content">
        <view v-if="loading" class="loading-tip">加载中...</view>
        <view v-else-if="contacts.length === 0" class="empty-tip">快去添加联系人</view>
        <view v-else class="contact-list">
          <view
            v-for="contact in contacts"
            :key="contact.userId"
            class="contact-item"
            @tap="selectContact(contact)"
          >
            <view class="avatar-container">
              <user-avatar :avatar-url="contact.avatarUrl || ''" :uploadable="false" :clickable="false"></user-avatar>
            </view>
            <view class="contact-info">
              <text class="contact-name">{{ contact.nickName || '未设置昵称' }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </uni-popup>
</template>

<script>
import request from '@/common/request.js';
import backUrl from '@/common/back_url.js';
import UniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
import UserAvatar from '@/components/user-avatar.vue';

export default {
  name: 'ContactSharePopup',
  components: {
    UniPopup,
    UserAvatar
  },
  data() {
    return {
      contacts: [],
      loading: false
    }
  },
  methods: {
    async loadContacts() {
      this.loading = true
      try {
        const res = await request({
          url: backUrl.endpoints.contact_list,
          method: 'GET'
        })
        this.contacts = res || []
      } catch (e) {
        console.error('加载联系人失败', e)
        this.contacts = []
      } finally {
        this.loading = false
      }
    },
    open() {
      this.loadContacts()
      this.$refs.popup.open()
    },
    close() {
      this.$refs.popup.close()
    },
    selectContact(contact) {
      this.$emit('select', contact)
      this.close()
    }
  }
}
</script>

<style scoped>
.popup-container {
  width: 600rpx;
  background-color: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.15);
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.popup-header {
  padding: 24rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
}

.popup-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.divider {
  height: 1rpx;
  background-color: #f0f0f0;
  width: 100%;
}

.popup-content {
  overflow-y: auto;
  padding: 16rpx 0;
  max-height: 60vh;
}

.loading-tip,
.empty-tip {
  padding: 48rpx 20rpx;
  text-align: center;
  font-size: 28rpx;
  color: #999999;
}

.contact-list {
  padding: 0 16rpx;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 20rpx 16rpx;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.2s;
}

.contact-item:active {
  background-color: #f5f5f5;
}

.avatar-container {
    width: 80rpx !important;
    height: 80rpx !important;
    margin-right: 20rpx;
    border-radius: 50% !important;
    background: linear-gradient(135deg, #fce7f3 0%, #c084fc 50%, #7dd3fc 100%) !important;
    padding: 4rpx !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    overflow: hidden !important;
    position: relative !important;
    box-shadow: 0 4rpx 16rpx rgba(168, 85, 247, 0.2) !important;
    border: 4rpx solid rgba(255, 255, 255, 0.8) !important;
    flex-shrink: 0 !important;
    box-sizing: border-box !important;
}

.avatar-container > user-avatar {
    width: 100% !important;
    height: 100% !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    background-color: #fff !important;
    border-radius: 50% !important;
    overflow: hidden !important;
}

.avatar-container ::v-deep .user-avatar-container,
.avatar-container ::v-deep view.user-avatar-container {
    width: 72rpx !important;
    height: 72rpx !important;
    min-width: 72rpx !important;
    min-height: 72rpx !important;
    max-width: 72rpx !important;
    max-height: 72rpx !important;
    padding: 0 !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
}

.avatar-container ::v-deep .avatar-wrapper,
.avatar-container ::v-deep view.avatar-wrapper {
    width: 72rpx !important;
    height: 72rpx !important;
    min-width: 72rpx !important;
    min-height: 72rpx !important;
    max-width: 72rpx !important;
    max-height: 72rpx !important;
    padding: 0 !important;
    border: none !important;
    box-shadow: none !important;
    background: transparent !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    overflow: hidden !important;
    position: relative !important;
}

.avatar-container ::v-deep image.avatar-img,
.avatar-container ::v-deep .avatar-img,
.avatar-container ::v-deep image {
    width: 72rpx !important;
    height: 72rpx !important;
    min-width: 72rpx !important;
    min-height: 72rpx !important;
    max-width: 72rpx !important;
    max-height: 72rpx !important;
    object-fit: cover !important;
    object-position: center !important;
}

.contact-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.contact-name {
  font-size: 30rpx;
  color: #333333;
}
</style>
