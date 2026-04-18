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
        <custom-icon type="fufeihuiyuan" :size="32" color="#ffd700"></custom-icon>
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
  import CustomIcon from '@/components/custom-icon/custom-icon.vue'
  import messageService from '../../common/messageService.js'
  
  export default {
    components: {
      UserAvatar,
      CustomIcon
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
    upgradeToNormal() {
      try {
        // 直接跳转到登录页面
        uni.navigateTo({ url: '/pages/login/login' });
      } catch (e) {
        console.error('操作过程异常:', e);
        // 即使出错也要跳转到登录页面
        uni.navigateTo({ url: '/pages/login/login' });
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
    background: linear-gradient(135deg, #9333ea, #ec4899);
    padding: 30rpx 30rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #fff;
    border-radius: 30rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
    margin: 10rpx 20rpx;
  }  
    .user-info-content {
      display: flex;
      align-items: center;
      flex: 1;
    }
    
    .member-level-icon {
      width: 80rpx;
      height: 80rpx;
      cursor: pointer;
      padding: 12rpx;
      border-radius: 50%;
      background-color: rgba(255, 215, 0, 0.2);
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
      animation: breathe 2s infinite ease-in-out;
    }
    
    /* 呼吸动画效果 */
    @keyframes breathe {
      0% {
        transform: scale(1);
        box-shadow: 0 0 0 0 rgba(255, 215, 0, 0.4);
      }
      70% {
        transform: scale(1.1);
        box-shadow: 0 0 0 20rpx rgba(255, 215, 0, 0);
      }
      100% {
        transform: scale(1);
        box-shadow: 0 0 0 0 rgba(255, 215, 0, 0);
      }
    }
    
    /* 会员等级图标悬停效果 */
    .member-level-icon:hover {
      transform: scale(1.1) rotate(360deg);
      background-color: rgba(255, 215, 0, 0.3);
      animation: none;
    }
    
    .avatar-container {
      width: 190rpx;
      height: 190rpx;
      border-radius: 50%;
      background-color: #02ff39;
      padding: 4rpx;
      margin-right: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      position: relative;
      box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.2);
      transition: all 0.3s ease;
      border: 4rpx solid #4ade80;
    }
    
    /* 头像内部容器 */
    .avatar-container > user-avatar {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fff;
      border-radius: 50%;
      overflow: hidden;
    }
    
    /* 确保头像图片在组件内正确居中 */
    .avatar-container img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 50%;
    }
    
    /* 头像容器悬停效果 */
    .avatar-container:hover {
      transform: scale(1.05);
      box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.3);
    }
    
    .user-details {
      display: flex;
      flex-direction: column;
      justify-content: center;
      min-height: 140rpx;
      flex: 1;
      
      .nickname {
        font-size: 36rpx;
        font-weight: 600;
        color: #fff;
        display: flex;
        align-items: center;
        margin-bottom: 8rpx;
        transition: all 0.3s ease;
        
        /* 昵称悬停效果 */
        &:hover {
          transform: scale(1.05);
          color: #ffd700;
        }
      }
      
      .phone {
        font-size: 28rpx;
        color: rgba(255, 255, 255, 0.9);
        opacity: 0.9;
        margin-bottom: 12rpx;
        transition: all 0.3s ease;
        
        /* 手机号悬停效果 */
        &:hover {
          transform: scale(1.05);
          color: #ffd700;
        }
      }
      
      .remaining-counts {
        display: flex;
        flex-direction: column;
        
        .count-item {
          color: rgba(255, 255, 255, 0.8);
          display: inline-flex;
          align-items: center;
          font-size: 24rpx;
          margin-bottom: 4rpx;
          transition: all 0.3s ease;
          
          /* 剩余次数悬停效果 */
          &:hover {
            transform: scale(1.05);
            color: #ffd700;
          }
        }
      }
    }
  }
  
  .function-group {
    margin-top: 20rpx;
    padding: 0 20rpx;
    
    .group-title {
      padding: 0 0 16rpx 0;
      font-size: 28rpx;
      color: #666;
      font-weight: 500;
    }
  }
  
  .square-grid {
    display: flex;
    flex-wrap: wrap;
    padding: 20rpx;
    gap: 16rpx;
    
    .square-item {
      width: calc(28% - 12rpx);
      padding: 24rpx 16rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: flex-start;
      background-color: #f0f0f0;
      border-radius: 16rpx;
      transition: all 0.3s ease;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
      min-height: 140rpx;
      
      /* 不同功能项的背景色 */
      &:nth-child(1) {
        background-color: #dbeafe;
      }
      
      &:nth-child(2) {
        background-color: #e0e7ff;
      }
      
      &:nth-child(3) {
        background-color: #d1fae5;
      }
      
      &:nth-child(4) {
        background-color: #fef3c7;
      }
      
      &:nth-child(5) {
        background-color: #fce7f3;
      }
      
      &:nth-child(6) {
        background-color: #dbeafe;
      }
      
      .square-icon {
        width: 80rpx;
        height: 80rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12rpx;
        background-color: rgba(255, 255, 255, 0.9);
        border-radius: 50rpx;
        transition: all 0.3s ease;
        box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
      }
      
      .square-title {
        font-size: 24rpx;
        margin-bottom: 8rpx;
        position: relative;
        color: #333;
        font-weight: 500;
        text-align: center;
      }
      
      .square-desc {
        font-size: 18rpx;
        color: #666;
        text-align: center;
        line-height: 1.3;
      }
      
      /* 悬停效果 */
      &:hover {
        transform: scale(1.05) translateY(-2rpx);
        box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.15);
      }
      
      /* 卡片悬停时图标旋转效果 */
      &:hover .square-icon {
        transform: scale(1.1) rotate(360deg);
        box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.2);
      }
      
      /* 图标悬停效果 */
      .square-icon:hover {
        transform: scale(1.1) rotate(360deg);
        box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.2);
      }
    }
  }
  
  .strip-list {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
    padding: 0;
    
    .strip-item {
      padding: 24rpx 30rpx;
      display: flex;
      align-items: center;
      justify-content: space-between;
      border: none;
      transition: all 0.3s ease;
      border-radius: 16rpx;
      margin: 0;
      background-color: #fff;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
      
      /* 列表项悬停效果 */
      &:hover {
        transform: scale(1.02) translateY(-2rpx);
        background-color: #f8f9fa;
        box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
      }
      
      /* 卡片悬停时图标旋转效果 */
      &:hover .strip-icon {
        transform: scale(1.1) rotate(360deg);
        background-color: rgba(25, 137, 250, 0.2);
        box-shadow: 0 4rpx 12rpx rgba(25, 137, 250, 0.3);
      }
      
      .strip-content {
        display: flex;
        align-items: center;
        flex: 1;
        
        .strip-icon {
          width: 80rpx;
          height: 80rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 20rpx;
          transition: all 0.3s ease;
          background-color: rgba(25, 137, 250, 0.1);
          border-radius: 50rpx;
          
          /* 图标容器悬停效果 */
          &:hover {
            transform: scale(1.1) rotate(360deg);
            background-color: rgba(25, 137, 250, 0.2);
            box-shadow: 0 4rpx 12rpx rgba(25, 137, 250, 0.3);
          }
        }
        
        .strip-text {
          flex: 1;
          
          .strip-title {
            font-size: 28rpx;
            margin-bottom: 4rpx;
            position: relative;
            transition: all 0.3s ease;
            
            /* 标题悬停效果 */
            &:hover {
              color: #1989fa;
            }
          }
          
          .strip-desc {
            font-size: 22rpx;
            color: #999;
            transition: all 0.3s ease;
            
            /* 描述悬停效果 */
            &:hover {
              color: #666;
            }
          }
        }
      }
      
      /* 右侧箭头悬停效果 */
      :deep(.custom-icon) {
        transition: all 0.3s ease;
        
        &:hover {
          transform: scale(1.2);
          color: #1989fa;
        }
      }
    }
  }

</style>