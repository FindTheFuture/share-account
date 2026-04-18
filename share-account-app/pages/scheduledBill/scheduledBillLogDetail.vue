<template>
  <view class="scheduled-bill-log-detail-page">
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载日志详情..."></uni-load-more>
    </view>

    <!-- 错误提示 -->
    <view class="error-container" v-if="errorMsg">
      <text class="error-text">{{ errorMsg }}</text>
      <button class="retry-btn" @click="loadScheduledBillLogDetail">重试</button>
    </view>

    <!-- 日志详情 -->
    <view class="detail-container" v-if="!loading && logDetail">
      <!-- 基本信息 -->
      <view class="detail-section">
        <view class="section-title">基本信息</view>
        <view class="detail-item">
          <text class="item-label">执行状态：</text>
          <text class="item-value" :class="getStatusClass(logDetail.status)">{{ getStatusText(logDetail.status) }}</text>
        </view>
        <view class="detail-item">
          <text class="item-label">执行时间：</text>
          <text class="item-value">{{ logDetail.executeTime }}</text>
        </view>
        <view class="detail-item" v-if="logDetail.errorMsg">
          <text class="item-label">执行信息：</text>
          <text class="item-value error-msg">{{ logDetail.errorMsg }}</text>
        </view>
      </view>

      <!-- 账单信息 -->
      <view class="detail-section" v-if="logDetail.bill">
        <view class="section-title">账单信息</view>
        <view class="bill-detail-info">
          <view class="detail-row" v-if="logDetail.bill.createUserName || logDetail.bill.userName">
            <text class="detail-label">创建人：</text>
            <text class="detail-value">{{ logDetail.bill.createUserName || logDetail.bill.userName || '-' }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">分类：</text>
            <view class="detail-value">
              <text>{{ logDetail.bill.className }}</text>
            </view>
          </view>
          <view class="detail-row">
            <text class="detail-label">金额：</text>
            <text :class="['detail-value', Number(logDetail.bill.price) > 0 ? 'income' : 'expense']">
              {{ formatAmount(logDetail.bill.price) }}
            </text>
          </view>
          <view class="detail-row" v-if="logDetail.bill.ledgerName">
            <text class="detail-label">账本名称：</text>
            <text class="detail-value">{{ logDetail.bill.ledgerName }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">账户名称：</text>
            <text class="detail-value">{{ logDetail.bill.accountName }}</text>
          </view>
          <view class="detail-row" v-if="logDetail.bill.isBudgetName">
            <text class="detail-label">预算状态：</text>
            <text class="detail-value">{{ logDetail.bill.isBudgetName }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">时间：</text>
            <text class="detail-value">{{ logDetail.bill.billTime }}</text>
          </view>
          <view class="detail-row" v-if="logDetail.bill.memo">
            <text class="detail-label">备注：</text>
            <text class="detail-value">{{ logDetail.bill.memo }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import request from '@/common/request.js';
import backUrl from '@/common/back_url.js';
import UniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue';

export default {
  components: {
    UniLoadMore
  },
  data() {
    return {
      loading: true,
      errorMsg: '',
      logDetail: null,
      id: null
    };
  },
  onLoad(options) {
    // 设置页面标题
    uni.setNavigationBarTitle({
      title: '执行日志详情'
    });
    
    if (options.id) {
      this.id = options.id;
      this.loadScheduledBillLogDetail();
    } else {
      this.errorMsg = '缺少日志ID';
      this.loading = false;
    }
  },
  methods: {
    /**
     * 加载定时记账执行日志详情
     */
    async loadScheduledBillLogDetail() {
      this.loading = true;
      this.errorMsg = '';
      
      try {
        const response = await request({
          url: backUrl.endpoints.scheduledBill_log_getById + this.id,
          method: 'GET'
        });
        
        if (response) {
          this.logDetail = response;
        } else {
          this.errorMsg = '获取日志详情失败';
        }
      } catch (error) {
        console.error('加载日志详情失败:', error);
        this.errorMsg = '网络请求异常，请稍后重试';
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 格式化金额显示
     */
    formatAmount(amount) {
      const n = Number(amount) || 0;
      // 后端返回的金额是分为单位，需要转换为元
      return (n / 100).toFixed(2);
    },
    
    /**
     * 格式化日期显示
     */
    formatDate(dateStr) {
      if (!dateStr) return '无';
      const date = new Date(dateStr);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },
    
    /**
     * 根据状态获取样式类
     */
    getStatusClass(status) {
      return status === 1 ? 'status-success' : 'status-failed';
    },

    /**
     * 获取状态文本
     */
    getStatusText(status) {
      return status === 1 ? '执行成功' : '执行失败';
    },
    
    /**
     * 返回上一页
     */
    goBack() {
      uni.navigateBack();
    }
  }
};
</script>

<style scoped>
.scheduled-bill-log-detail-page {
  padding-bottom: 40rpx;
  background-color: #f8f8f8;
  min-height: 100vh;
}

.loading-container {
  padding: 40rpx 0;
  text-align: center;
}

.error-container {
  padding: 40rpx;
  text-align: center;
}

.error-text {
  display: block;
  color: #ff4d4f;
  margin-bottom: 20rpx;
}

.retry-btn {
  font-size: 28rpx;
  padding: 0 20rpx;
  background-color: #1989fa;
  color: #ffffff;
}

.detail-container {
  padding: 20rpx;
}

.detail-section {
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 20rpx;
  padding-bottom: 10rpx;
  border-bottom: 1rpx solid #eeeeee;
}

.detail-item {
  display: flex;
  margin-bottom: 20rpx;
  align-items: flex-start;
}

.item-label {
  font-size: 28rpx;
  color: #666666;
  width: 140rpx;
  flex-shrink: 0;
}

.item-value {
  font-size: 28rpx;
  color: #333333;
  flex: 1;
}

.status-success {
  color: #52c41a;
}

.status-failed {
  color: #ff4d4f;
}

.error-msg {
  color: #ff4d4f;
  word-break: break-all;
}

.amount {
  font-size: 32rpx;
  font-weight: bold;
  color: #ff4d4f;
}

.message-content {
  font-size: 28rpx;
  color: #333333;
  line-height: 1.5;
  white-space: pre-wrap;
  padding: 20rpx;
  background-color: #f9f9f9;
  border-radius: 8rpx;
}

/* 账单详情样式 */
.bill-detail-info {
  padding: 10rpx 0;
}

.detail-row {
  display: flex;
  margin-bottom: 20rpx;
  align-items: flex-start;
}

.detail-label {
  font-size: 28rpx;
  color: #666666;
  width: 140rpx;
  flex-shrink: 0;
}

.detail-value {
  font-size: 28rpx;
  color: #333333;
  flex: 1;
}

.income {
  color: #52c41a;
  font-weight: bold;
}

.expense {
  color: #ff4d4f;
  font-weight: bold;
}

.action-buttons {
  padding: 20rpx;
  margin-top: 20rpx;
}

.action-btn {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
  border-radius: 40rpx;
}

.back-btn {
  background-color: #1989fa;
  color: #ffffff;
}
</style>
