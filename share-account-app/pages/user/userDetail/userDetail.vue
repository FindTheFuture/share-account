<template>
  <view class="user-detail-page">
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载用户信息..."></uni-load-more>
    </view>
    
    <!-- 内容区域 -->
    <view class="content-container" v-else>
      <!-- 顶部用户信息 -->
      <view class="user-header">
        <view class="user-avatar">
          <user-avatar
            :avatar-url="userInfo.pictureAddress"
            :user-id="haoe"
            :uploadable="false"
            :show-edit-mask="false"
            :clickable="true"
            @click="previewAvatar"
          ></user-avatar>
          <view class="avatar-badge" v-if="userInfo.roleName">
            {{ userInfo.roleName }}
          </view>
        </view>
        <view class="user-basic-info">
          <text class="user-nickname">{{ userInfo.nickName || '未设置昵称' }}</text>
          <text class="user-id-text">#{{ userInfo.haoe || '未知' }}</text>
        </view>
      </view>
      
      <!-- 积分信息卡片 
      <view class="integral-card card">
        <view class="integral-value">
          <text class="value-text">{{ userInfo.validIntegral || 0 }}</text>
          <text class="value-desc">有效积分</text>
        </view>
      </view> -->
      
      <!-- 基本信息卡片 -->
      <view class="info-card card">
        <view class="card-title">基本信息</view>
        <view class="info-item" v-for="(item, index) in infoList" :key="index">
          <text class="item-label">{{ item.label }}</text>
          <text class="item-value">{{ item.value || '未设置' }}</text>
        </view>
      </view>
      
      <!-- 通知设置卡片 -->
      <view class="notify-card card">
        <view class="card-title">通知设置</view>
        <view class="info-item">
          <text class="item-label">消息通知</text>
          <text class="item-value" :class="getNotifyStatusClass(userInfo.canSendMessage)">{{ userInfo.canSendMessageName || '未知' }}</text>
        </view>
        <view class="info-item">
          <view class="item-label">
            <text>日报通知</text>
            <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click="onNotifyTipClick" />
          </view>
          <text class="item-value">每日{{userInfo.notityBill}}点</text>
          <!-- <text class="item-value">{{ getNotifyTime(userInfo.notityBill) }}</text> -->
        </view>
      </view>
      
      <!-- 时间信息卡片 -->
      <view class="time-card card">
        <view class="card-title">时间信息</view>
        <view class="info-item">
          <text class="item-label">最新登录时间</text>
          <text class="item-value">{{ formatTime(userInfo.lastLoginTime) || '未登录过' }}</text>
        </view>
        <view class="info-item">
          <text class="item-label">注册时间</text> 
          <text class="item-value">{{ formatTime(userInfo.createTime) || '未知' }}</text>
        </view>
      </view>
      
      <!-- 底部编辑按钮 -->
      <view class="edit-btn-container">
        <button :disabled="userInfo.isGuest" @click="onEditClick">{{ userInfo.isGuest ? '游客模式不允许编辑' : '编辑' }}</button>
        <button v-if="!userInfo.isGuest" class="logout-btn" @click="onLogoutClick">退出登录</button>
      </view>
    </view>
  </view>
</template>

<script>
import UserAvatar from '@/components/user-avatar.vue'

export default {
  components: {
    UserAvatar
  },
  data() {
    return {
      haoe: '', // 用户ID
      loading: true,
      userInfo: {}, // 用户信息
      infoList: [] // 基本信息列表
    }
  },
  onLoad(options) {
    // 获取上游传递的参数
    this.haoe = options.haoe;
    if (this.haoe) {
      this.getUserInfo();
    } else {
      uni.showToast({
        title: '用户ID不能为空',
        icon: 'none'
      });
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    }
  },
  
  onShow() {
    // 当页面从编辑页返回时，重新获取用户信息
    if (this.haoe) {
      this.getUserInfo();
    }
  },
  methods: {
    // 获取用户信息
    async getUserInfo() {
      try {
        const res = await this.$request({
          url: this.$backUrlConfig.endpoints.getUserInfo + this.haoe,
          method: 'GET'
        });
        
        if (res) {
          this.userInfo = res;
          this.formatInfoList();
        } else {
          uni.showToast({
            title: '获取用户信息失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('获取用户信息异常:', error);
        uni.showToast({
          title: '网络请求异常',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    
    // 格式化基本信息列表
    formatInfoList() {
      this.infoList = [
        { label: '手机号', value: this.userInfo.phone },
        { label: '性别', value: this.userInfo.sexName }
      ];
    },
    
    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return '';
      return timeStr.replace('T', ' ').substring(0, 19);
    },
    
    // 获取通知时间
    // getNotifyTime(hour) {
    //   if (hour === null || hour === undefined) return '未设置';
    //   return `${hour}点`;
    // },
    
    onNotifyTipClick() {
      uni.showModal({
        title: '昨日收支日报',
        icon: 'none'
      });
    },
    
    // 获取通知状态样式
    getNotifyStatusClass(status) {
      return status === 1 ? 'status-enabled' : 'status-disabled';
    },
    
    // 返回上一页
    navigateBack() {
      uni.navigateBack();
    },
    
    // 跳转到编辑页面
    navigateToEdit() {
      uni.navigateTo({
        url: `/pages/user/editUser/editUser?haoe=${this.haoe}`
      });
    },

    onEditClick() {
      if (this.userInfo && this.userInfo.isGuest) {
        uni.showToast({
          title: '游客模式不允许编辑',
          icon: 'none'
        });
        return;
      }
      this.navigateToEdit();
    },
    onLogoutClick() {
      try {
        // 直接使用请求模块中的 logout 方法
        this.$request.logout({ redirect: true, tip: '已退出登录' });
      } catch (e) {
        console.error('退出登录失败', e);
        uni.showToast({ title: '操作失败', icon: 'none' });
      }
    },
    
    // 预览头像
    previewAvatar() {
      if (this.userInfo && this.userInfo.pictureAddress) {
        uni.previewImage({
          urls: [this.userInfo.pictureAddress],
          current: this.userInfo.pictureAddress,
          showmenu: true
        });
      }
    },
  }
}
</script>

<style lang="scss">

.user-detail-page {
  background-color: #f8f8f8;
  min-height: 100vh;
}

.loading-container {
  padding-top: 300rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.content-container {
  padding-bottom: 120rpx;
}

/* 用户头部信息 */
.user-header {
  background-color: #ffffff;
  padding: 60rpx 30rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #333333;
  position: relative;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.user-avatar {
  position: relative;
  transition: all 0.3s;
}

.avatar-badge {
  position: absolute;
  bottom: -10rpx;
  right: 37rpx;
  background-color: #e6f7ff;
  color: #1989fa;
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  font-weight: 500;
}

.user-basic-info {
  margin-top: 20rpx;
  text-align: center;
}

.user-nickname {
  font-size: 32rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 8rpx;
  color: #333333;
}

.user-id-text {
  font-size: 24rpx;
  color: #999999;
}

/* 卡片样式 */
.card {
  background-color: #ffffff;
  margin: 20rpx;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: all 0.3s;
}

.card-title {
  padding: 24rpx 30rpx;
  font-size: 28rpx;
  font-weight: 500;
  color: #333333;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
  transition: all 0.2s;
  
  &:active {
    background-color: #fafafa;
  }
  
  &:last-child {
    border-bottom: none;
  }
}

.item-label {
  font-size: 28rpx;
  color: #666666;
  display: flex;
  align-items: center;
}

.notify-tip-icon {
  margin-left: 8rpx;
}

.item-value {
  font-size: 28rpx;
  color: #333333;
  text-align: right;
}

/* 通知状态样式 */
.status-enabled {
  color: #52c41a !important;
}

.status-disabled {
  color: #999999 !important;
}

/* 积分卡片 */
.integral-card {
  padding: 40rpx 0;
  text-align: center;
  margin-top: 20rpx;
  
  .integral-value {
    .value-text {
      font-size: 60rpx;
      font-weight: bold;
      color: #ff7e00;
    }
    
    .value-desc {
      font-size: 26rpx;
      color: #666666;
      margin-top: 10rpx;
      display: block;
    }
  }
}

/* 底部编辑按钮 */
.edit-btn-container {
  padding: 20rpx;
  
  button {
    width: 100%;
    height: 90rpx;
    line-height: 90rpx;
    border-radius: 45rpx;
    font-size: 32rpx;
    font-weight: 500;
    background-color: #1989fa;
    color: #ffffff;
    border: none;
    transition: all 0.3s;
    
    &:active {
      background-color: #40a9ff;
    }
  }

  .logout-btn {
    margin-top: 16rpx;
    background-color: #e6e6e6 !important;
    color: #333333 !important;
  }
}

/* 适配不同尺寸的手机 */
@media screen and (max-width: 320px) {
  .user-avatar {
    width: 150rpx;
    height: 150rpx;
  }
  
  .integral-value .value-text {
    font-size: 50rpx !important;
  }
}

@media screen and (min-width: 375px) {
  .user-avatar {
    width: 180rpx;
    height: 180rpx;
  }
}
</style>
