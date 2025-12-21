<template>
  <view class="mycenter-page">
    <!-- 用户信息头部 -->
    <view class="user-info">
      <view class="user-info-content" @click="navigateToUserDetail">
        <view class="avatar-container">
          <user-avatar
            :avatar-url="userInfo.pictureAddress"
            :user-id="String(userInfo.haoe)"
            :uploadable="false"
            :show-edit-mask="false"
            :clickable="false"
          ></user-avatar>
        </view>
        <view class="user-details">
          <view class="nickname">{{ userInfo.nickName || '未设置昵称' }}</view>
          <view class="phone">{{ userInfo.phone || '未绑定手机号' }}</view>
          <view class="remaining-counts" @click.stop="navigateToVipMember">
            <text class="count-item">AI识别剩余: {{ remainingAiCount }}次</text>
            <text class="count-item">PDF导出剩余: {{ remainingPdfCount }}次</text>
          </view>
        </view>
      </view>
      <view class="member-level-icon" @click.stop="navigateToVipMember">
        {{ userInfo.memberLevelIcon || '☀️' }}
      </view>
    </view>

    <!-- 游客引导区 -->
    <view class="function-group" v-if="userInfo.isGuest">
      <view class="group-title">游客模式</view>
      <view class="strip-list">
        <view class="strip-item" @click="upgradeToNormal">
          <view class="strip-content">
            <view class="strip-icon">
              <custom-icon type="fufeihuiyuan" :size="24" color="#f56c6c"></custom-icon>
            </view>
            <view class="strip-text">
              <view class="strip-title important">升级为正式用户</view>
              <view class="strip-desc">解锁共享账本与完整功能</view>
            </view>
          </view>
          <custom-icon type="xiangyou1" :size="18" color="#999"></custom-icon>
        </view>
      </view>
    </view>

    <!-- 九宫格功能区 -->
    <view class="function-group" v-if="featureList.square && featureList.square.length > 0">
      <view class="square-grid">
        <view 
          class="square-item" 
          v-for="(item, index) in featureList.square" 
          :key="index"
          @click="navigateTo(item.url, item.isTabBar)">
          <view class="square-icon">
            <custom-icon :type="item.icon" :size="30" :color="item.color"></custom-icon>
          </view>
          <view class="square-title" :class="{ important: item.important }">{{ item.title }}</view>
          <view class="square-desc" v-if="item.desc">{{ item.desc }}</view>
        </view>
      </view>
    </view>

    <!-- 列表功能区 -->
    <view class="function-group" v-if="featureList.strip && featureList.strip.length > 0">
      <view class="group-title">更多功能</view>
      <view class="strip-list">
        <view 
          class="strip-item" 
          v-for="(item, index) in featureList.strip" 
          :key="index"
          @click="navigateTo(item.url, item.isTabBar)">
          <view class="strip-content">
            <view class="strip-icon">
              <custom-icon :type="item.icon" :size="22" :color="item.color"></custom-icon>
            </view>
            <view class="strip-text">
              <view class="strip-title" :class="{ important: item.important }">{{ item.title }}</view>
              <view class="strip-desc" v-if="item.desc">{{ item.desc }}</view>
            </view>
          </view>
          <custom-icon type="xiangyou1" :size="18" color="#999"></custom-icon>
        </view>
      </view>
    </view>
    
    <!-- 通知设置功能区 -->
    <view class="function-group">
      <view class="strip-list">
        <view class="strip-item" @click="handleNotificationClick">
          <view class="strip-content">
            <view class="strip-icon">
              <custom-icon type="xiaoxi" :size="22" color="#1989fa"></custom-icon>
            </view>
            <view class="strip-text">
              <view class="strip-title">接收账本通知</view>
              <view class="strip-desc">开启后，账本变更时会收到通知</view>
            </view>
          </view>
          <custom-icon type="xiangyou1" :size="18" color="#999"></custom-icon>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import UserAvatar from '@/components/user-avatar.vue'
import messageService from '../../common/messageService.js'
  
  export default {
    components: {
      UserAvatar
    },

    data() {
    return {
      userInfo: {},
      featureList: {},
      loadingUserInfo: false,
      loadingFeatureList: false,
      remainingAiCount: 0,
      remainingPdfCount: 0
    }
  },
  onLoad() {
  },
  onShow() {
    this.refreshData();
  },
  onPullDownRefresh() {  // 下拉刷新处理函数
	  this.refreshData();
  },
  methods: {
	  // 刷新数据方法
	  async refreshData() {
		try {
		  // 并行加载用户信息和功能列表
		  await Promise.all([
			  this.getUserInfo(),
			  this.getFeatureList()
		  ]);
		} catch (error) {
		  console.error('刷新失败:', error);
		  uni.showToast({
        title: '刷新失败',
        icon: 'none'
		  });
		} finally {
		  // 无论成功或失败，都要停止下拉刷新动画
		  uni.stopPullDownRefresh();
		}
	  },
	  
    // 获取用户信息
    async getUserInfo() {
      this.loadingUserInfo = true;
      try {
        const res = await this.$request({
          url: this.$backUrlConfig.endpoints.getUserInfo + 0,
          method: 'GET'
      });
        this.userInfo = res;
        // 获取会员剩余次数
        this.getRemainingCounts();
      } catch (error) {
        console.error('获取用户信息出错:', error);
        uni.showToast({
          title: '获取用户信息失败',
          icon: 'none'
        });
      } finally {
        this.loadingUserInfo = false;
      }
    },
    
    // 获取会员剩余次数
    async getRemainingCounts() {
      try {
        const res = await this.$request({
          url: this.$backUrlConfig.endpoints.userMember_queryUserMembers,
          method: 'GET',
          data: {}
        });
        if (res) {
          this.remainingAiCount = res.remainingAiCount || 0;
          this.remainingPdfCount = res.remainingPdfCount || 0;
        }
      } catch (error) {
        console.error('获取会员剩余次数失败:', error);
      }
    },
    
    // 跳转到会员页面
    navigateToVipMember() {
      uni.navigateTo({
        url: '/pages/vipmember/vipmember'
      });
    },
    
    // 获取功能列表
    async getFeatureList() {
      this.loadingFeatureList = true;
      try {
        const res = await this.$request({
          url: this.$backUrlConfig.endpoints.getFeatureList,
          method: 'GET'
        });
        this.featureList = res;
      } catch (error) {
        console.error('获取功能列表出错:', error);
        uni.showToast({
          title: '获取功能列表失败',
          icon: 'none'
        });
      } finally {
        this.loadingFeatureList = false;
      }
    },
    
    // 游客升级为正式用户
    async upgradeToNormal() {
      try {
        uni.showLoading({ title: '升级中...' });
        // 仅在小程序或App中可用
        await new Promise((resolve, reject) => {
          uni.login({
            provider: 'weixin',
            success: resolve,
            fail: reject
          });
        }).then(async (loginRes) => {
          const code = loginRes.code;
          if (!code) {
            uni.hideLoading();
            throw new Error('获取微信code失败');
          }
          const res = await this.$request({
            url: this.$backUrlConfig.endpoints.login_upgrade,
            method: 'POST',
            data: { code }
          });
          if (res && res.token) {
            // 持久化令牌与用户信息（与登录页/引导卡保持一致）
            const { token, refresh_token, expires_in, additionalId: newAdditionalId, thunder, canSendMessage } = res;
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
  
            // 刷新用户信息
            await this.getUserInfo();
            uni.hideLoading();
            uni.showToast({ title: '升级成功', icon: 'success' });
          } else {
            // 兼容request.js在HTTP 200但业务code!=200时直接resolve({code, message})的情况
            uni.hideLoading();
            const msg = (res && res.message) ? res.message : '升级失败';
            uni.showToast({ title: msg, icon: 'none' });
            if (msg.includes('已绑定其他账号') || msg.includes('请直接使用微信登录')) {
              setTimeout(() => {
                uni.navigateTo({ url: '/pages/login/login' });
              }, 500);
            }
            return;
          }
        });
      } catch (e) {
        console.error('升级失败', e);
        uni.hideLoading();
        const msg = e && e.message ? e.message : '升级失败，请稍后重试';
        uni.showToast({ title: msg, icon: 'none' });
        // 后端返回“该微信已绑定其他账号，请直接使用微信登录”时，分流到登录页
        if (msg.includes('已绑定其他账号') || msg.includes('请直接使用微信登录')) {
          setTimeout(() => {
            uni.navigateTo({ url: '/pages/login/login' });
          }, 500);
        }
      }
    },
    
    // 页面跳转
    navigateTo(url, isTabBar) {
      if (!url) return;
      
      if (isTabBar === 'true' || isTabBar === true) {
        uni.switchTab({ url });
      } else {
        uni.navigateTo({ url });
      }
    },
    
    // 处理通知授权
    async handleNotificationClick() {
      try {
        await messageService.manualRequestAuthorizationNoCheck();
      } catch (error) {
        console.error('通知授权失败:', error);
      }
    },
    
    // 跳转到用户详情页
    navigateToUserDetail() {
      if (this.userInfo && this.userInfo.haoe) {
        uni.navigateTo({
          url: `/pages/user/userDetail/userDetail?haoe=${this.userInfo.haoe}`
        });
      }
    }
  }
}
</script>

<style lang="scss">
.mycenter-page {
  background-color: #f8f8f8;
  min-height: 100vh;
  
  .user-info {
    background: #fff;
    padding: 30rpx 40rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #333;
    
    .user-info-content {
      display: flex;
      align-items: center;
      flex: 1;
    }
    
    .member-level-icon {
      font-size: 40rpx;
      cursor: pointer;
      padding: 10rpx;
      border-radius: 8rpx;
    }
    
    .avatar-container {
      width: 140rpx;
      height: 140rpx;
      border-radius: 50%;
      background-color: #fff;
      padding: 4rpx;
      margin-right: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      position: relative;
    }
    
    /* 头像容器样式 - 整合所有设置 */
    .avatar-container {
      border-radius: 50%;
      background-color: #fff;
      padding: 0; /* 移除内边距 */
      margin-right: 50rpx; /* 与右侧文字的间隔 */
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      position: relative;
      z-index: 10;
    }
    
    /* 确保头像组件在容器内正确显示 */
    .avatar-container > user-avatar {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%) scale(0.9); /* 定位并缩放头像 */
      z-index: 1;
      display: block;
    }
    
    .user-details {
      display: flex;
      flex-direction: column;
      justify-content: center;
      min-height: 140rpx;
      
      .nickname {
        font-size: 36rpx;
        font-weight: 600;
        color: #333;
        display: flex;
        align-items: center;
      }
      
      .phone {
        font-size: 28rpx;
        color: #666;
        opacity: 0.9;
      }
      
      .remaining-counts {
        display: flex;
        flex-direction: column;
        .count-item {
          color: #0284c7;
          display: inline-flex;
          align-items: center;
        }
      }
      
      .integral {
        font-size: 28rpx;
        color: #f97316;
        background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
        padding: 4rpx 16rpx;
        border-radius: 16rpx;
        align-self: flex-start;
        display: inline-flex;
        align-items: center;
        box-shadow: 0 2rpx 8rpx rgba(249, 115, 22, 0.1);
        
        .label {
          margin-right: 8rpx;
          font-weight: 500;
        }
        
        .value {
          color: #ea580c;
          font-weight: 600;
        }
      }
    }
  }
  
  .function-group {
    background-color: #fff;
    margin-top: 20rpx;
    
    .group-title {
      padding: 20rpx 30rpx;
      font-size: 28rpx;
      color: #999;
      border-bottom: 1rpx solid #eee;
    }
  }
  
  .square-grid {
    display: flex;
    flex-wrap: wrap;
    padding: 20rpx 0;
    
    .square-item {
      width: 25%;
      padding: 20rpx 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      
      .square-icon {
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 10rpx;
      }
      
      .square-title {
        font-size: 28rpx;
        margin-bottom: 6rpx;
        position: relative;
        
        &.important::after {
          content: '';
          position: absolute;
          bottom: -4rpx;
          left: 50%;
          transform: translateX(-50%);
          width: 20rpx;
          height: 4rpx;
          background-color: #f56c6c;
          border-radius: 2rpx;
        }
      }
      
      .square-desc {
        font-size: 22rpx;
        color: #999;
        text-align: center;
      }
    }
  }
  
  .strip-list {
    .strip-item {
      padding: 24rpx 30rpx;
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1rpx solid #eee;
      
      .strip-content {
        display: flex;
        align-items: center;
        
        .strip-icon {
          width: 50rpx;
          height: 50rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 20rpx;
        }
        
        .strip-text {
          .strip-title {
            font-size: 30rpx;
            margin-bottom: 4rpx;
            position: relative;
            
            &.important::after {
              content: '';
              position: absolute;
              bottom: -4rpx;
              left: 0;
              width: 20rpx;
              height: 4rpx;
              background-color: #f56c6c;
              border-radius: 2rpx;
            }
          }
          
          .strip-desc {
            font-size: 22rpx;
            color: #999;
          }
        }
      }
    }
  }
}
</style>