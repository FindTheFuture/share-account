<template>
  <view class="invitation-container">
    
    <view v-if="loading" class="loading-state">
      <uni-load-more :status="'loading'" />
      <text class="loading-text">正在处理邀请...</text>
    </view>
    
    <view v-else-if="member" class="invitation-content">
      <!-- 邀请人信息区域 -->
      <view class="inviter-section">
        <view class="avatar-container">
          <user-avatar
            :avatar-url="member.parentUserPictureAddress"
            :default-avatar="'https://shareaccount-1302778096.cos.ap-beijing.myqcloud.com/static/default_avatar.webp'"
            :clickable="false"
            :uploadable="false"
            :show-edit-mask="false"
          />
        </view>
        <text class="inviter-name">{{ member.parentUserName || '未知用户' }}</text>
      </view>
      
      <!-- 邀请内容区域 -->
      <view class="invitation-info-card">
        <!-- 账本邀请 -->
        <view v-if="!member.billId" class="invitation-info-section">
          <view class="invitation-title">邀请你加入账本</view>
          <view class="highlight-text ledger-name">{{ member.ledgerName || '未知账本' }}</view>
        </view>
        
        <!-- 账单邀请（过期时隐藏明细） -->
        <view v-else class="bill-invitation">
          <view class="invitation-title">邀请你查看账单</view>
          <view class="bill-detail-info" v-if="!expired">
            <view class="detail-row">
              <text class="detail-label">分类</text>
              <text class="detail-value">{{ member.className || '未分类' }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">金额</text>
              <text :class="['detail-value', member.price > 0 ? 'income' : 'expense']">
                {{ member.price > 0 ? '+' : '-' }}{{ (Math.abs(member.price) / 100).toFixed(2) }}
              </text>
            </view>
            <view class="detail-row">
            <text class="detail-label">预算状态</text>
            <text class="detail-value">{{ member.isBudgetName }}</text>
          </view>
            <view class="detail-row" v-if="member.ledgerName">
              <text class="detail-label">账本名称</text>
              <text class="detail-value">{{ member.ledgerName }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">账户类型</text>
              <text class="detail-value">{{ member.accountTypeName || '未选账户' }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">时间</text>
              <text class="detail-value">{{ formatBillTime(member.billTime) }}</text>
            </view>
            <view class="detail-row" v-if="member.memo">
              <text class="detail-label">备注</text>
              <text class="detail-value">{{ member.memo }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 操作按钮 -->
      <view class="action-buttons">
        <!-- 状态为0且未过期时显示接受邀请按钮 -->
        <button 
          v-if="member.status === 0 && !expired" 
          type="primary" 
          @click="acceptInvitation" 
          :disabled="processing"
          class="accept-btn"
        >接受邀请</button>
        <!-- 过期时显示提示并返回首页 -->
        <view v-else-if="expired" class="expired-tip" @click="backToHome">已过期，需要重新分享账单（点击返回首页）</view>
        <!-- 其他状态显示已加入 -->
        <view v-else class="joined-tag" @click="backToHome">已加入，返回首页</view>
      </view>
      
      <view class="invitation-tip" v-if="!member.billId" >
        <text class="tip-text">可以查看ta这个账本下的所有账单啦</text>
      </view>
      <view class="invitation-tip" v-if="member.billId && !expired" >
        <text class="tip-text">只能查看这个账单内容</text>
      </view>
    </view>
    
    <view v-else class="error-state">
      <text class="error-text">{{ errorMessage || '邀请信息获取失败' }}</text>
      <button @click="backToHome">返回首页</button>
    </view>
  </view>
</template>

<script>
import request from '../../common/request.js';
import backUrl from '../../common/back_url.js';
import uniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue';
import UserAvatar from '@/components/user-avatar.vue';

export default {
  components: {
    uniLoadMore,
    'user-avatar': UserAvatar
  },
  data() {
    return {
      loading: true,
      processing: false,
      member: null,
      errorMessage: '',
      id: ''
    };
  },
  computed: {
    expired() {
      return this.member && this.member.status === 2;
    }
  },
  onLoad(options) {
    // 从页面参数中获取成员id
    this.id = options.id || '';
    
    // 检查id是否存在
    if (!this.id) {
      this.loading = false;
      this.errorMessage = '邀请链接无效，请检查链接是否正确';
      return;
    }
    
    // 加载邀请详情
    this.loadInvitationDetail();
  },
  

  methods: {
    // 加载邀请详情
    async loadInvitationDetail() {
      this.loading = true;
      try {
        const res = await request.get(backUrl.endpoints.member_getById + this.id );
        if (!res) {
          // 记录不存在：仅显示过期提示并返回首页按钮
          this.member = null;
          this.errorMessage = '已过期，需要重新分享账单';
          return;
        }
        // 如果请求成功，说明用户已登录或接口允许未登录访问
        this.member = res;
        
        // 当billId有值且未过期时，查询账单详情
        if (this.member && this.member.billId && !this.expired) {
          try {
            const billDetail = await request.get(backUrl.endpoints.bill_getById + this.member.billId);
            // 将账单详情数据合并到member对象中，便于模板渲染
            this.member = {
              ...this.member,
              ...billDetail
            };
          } catch (billError) {
            console.error('查询账单详情失败:', billError);
            // 账单详情查询失败不影响整体页面显示
          }
        }
      } catch (error) {
        console.error('加载邀请详情失败:', error);
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        });
        this.errorMessage = '加载邀请详情失败，请稍后再试';
      } finally {
        this.loading = false;
      }
    },
    
    // 接受邀请
    async acceptInvitation() {
      if (this.processing || this.expired) return;
      
      this.processing = true;
      try {
        await request.post(`${backUrl.endpoints.member_acceptInvitation}?id=${this.id}`);
        uni.showToast({ title: '接受邀请成功', icon: 'success' });
        
        setTimeout(() => {
          uni.switchTab({ url: `/pages/firstpage/firstpage` });
        }, 1500);
      } catch (error) {
        console.error('接受邀请失败:', error);
        uni.showToast({ title: '接受邀请失败，请稍后再试', icon: 'none' });
        this.processing = false;
      }
    },
    
    // 格式化账单时间
    formatBillTime(time) {
      if (!time) return '';
      
      const date = new Date(time);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },
    
    // 返回首页
    backToHome() {
      uni.switchTab({ url: '/pages/firstpage/firstpage' });
    }
  }
};
</script>

<style scoped>
  .invitation-container {
    padding: 40rpx 30rpx;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: #f8f8f8;
  }
  
  .loading-state {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
  }
  
  .loading-text {
    margin-top: 20rpx;
    font-size: 28rpx;
    color: #666;
  }
  
  .invitation-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20rpx 0;
  }
  
  /* 邀请人信息区域 */
  .inviter-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 60rpx;
  }
  
  .avatar-container {
    width: 180rpx;
    height: 180rpx;
    border-radius: 50%;
    overflow: hidden;
    margin-bottom: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.15);
    background-color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .avatar {
    width: 100%;
    height: 100%;
  }
  
  .inviter-name {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }
  
  /* 邀请内容卡片 */
  .invitation-info-card {
    width: 85%;
    background-color: #fff;
    border-radius: 24rpx;
    padding: 48rpx;
    margin-bottom: 60rpx;
    box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.08);
    position: relative;
    overflow: hidden;
  }
  
  /* 添加卡片装饰元素 */
  .invitation-info-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 8rpx;
    height: 100%;
    background: linear-gradient(180deg, #1989fa 0%, #36cfc9 100%);
  }
  
  /* 邀请信息区域 */
  .invitation-info-section {
    padding-left: 16rpx;
  }
  
  /* 邀请标题 */
  .invitation-title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;
  }
  
  /* 高亮文本 */
  .highlight-text {
    font-size: 42rpx;
    font-weight: bold;
    margin-top: 10rpx;
    text-align: center;
    padding: 20rpx 0;
  }
  
  /* 账本名称 */
  .ledger-name {
    color: #1989fa;
  }
  
  /* 账单详情样式 */
  .bill-detail-info {
    margin-top: 20rpx;
  }
  
  .detail-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
  }
  
  .detail-row:last-child {
    border-bottom: none;
  }
  
  .detail-label {
    font-size: 28rpx;
    color: #666;
  }
  
  .detail-value {
    font-size: 28rpx;
    color: #333;
    text-align: right;
    flex: 1;
    margin-left: 30rpx;
  }
  
  .detail-value.income {
    color: #52c41a;
  }
  
  .detail-value.expense {
    color: #ff4d4f;
  }
  
  /* 操作按钮 */
  .action-buttons {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 20rpx;
    align-items: center;
  }
  
  /* 接受邀请按钮 */
  .accept-btn {
    width: 100%;
    font-size: 34rpx;
    height: 100rpx;
    border-radius: 50rpx;
    background: linear-gradient(135deg, #1989fa 0%, #1677ff 100%);
    color: #fff;
    border: none;
    box-shadow: 0 4rpx 20rpx rgba(25, 137, 250, 0.3);
    transition: all 0.3s ease;
  }
  
  .accept-btn:active {
    transform: scale(0.98);
    box-shadow: 0 2rpx 10rpx rgba(25, 137, 250, 0.2);
  }
  
  .accept-btn:disabled {
    background: linear-gradient(135deg, #a0cfff 0%, #80bdff 100%);
    box-shadow: none;
  }
  
  /* 已加入标签 */
  .joined-tag {
    width: 100%;
    height: 100rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 34rpx;
    color: #999;
    background-color: #f5f5f5;
    border-radius: 50rpx;
    border: 2rpx solid #e8e8e8;
    cursor: pointer;
    transition: all 0.3s ease;
  }
  
  .joined-tag:active {
    background-color: #e8e8e8;
    transform: scale(0.98);
  }
  
  /* 提示信息 */
  .invitation-tip {
    margin-top: 30rpx;
    text-align: center;
  }
  
  .tip-text {
    font-size: 26rpx;
    color: #999;
  }
  
  /* 错误状态 */
  .error-state {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    text-align: center;
  }
  
  .error-text {
    font-size: 28rpx;
    color: #999;
    margin-bottom: 40rpx;
    padding: 0 20rpx;
  }
  
  .expired-tip {
    width: 100%;
    text-align: center;
    color: #ff4d4f;
    font-size: 30rpx;
  }
</style>