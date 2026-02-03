<template>
  <view class="scheduled-bill-detail-page">
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载配置详情..."></uni-load-more>
    </view>

    <!-- 错误提示 -->
    <view class="error-container" v-if="errorMsg">
      <text class="error-text">{{ errorMsg }}</text>
      <button class="retry-btn" @click="loadScheduledBillDetail">重试</button>
    </view>

    <!-- 配置详情 -->
    <view class="detail-container" v-if="!loading && !errorMsg && scheduledBill">
      <!-- 基本信息 -->
      <view class="detail-section">
        <view class="section-title">基本信息</view>
        <view class="detail-item">
          <text class="item-label">配置名称：</text>
          <text class="item-value">{{ scheduledBill.name }}</text>
        </view>
        <view class="detail-item">
          <text class="item-label">状态：</text>
          <text class="item-value" :class="getStatusClass(scheduledBill.status)">{{ getStatusText(scheduledBill.status) }}</text>
        </view>
        <view class="detail-item">
          <text class="item-label">开始日期：</text>
          <text class="item-value">{{ scheduledBill.startDate }}</text>
        </view>
        <view class="detail-item">
          <text class="item-label">结束日期：</text>
          <text class="item-value">{{ scheduledBill.endDate || '无' }}</text>
        </view>
        <view class="detail-item">
          <text class="item-label">周期类型：</text>
          <text class="item-value">{{ getCycleTypeText(scheduledBill.cycleType) }}</text>
        </view>
        <view class="detail-item">
          <text class="item-label">周期值：</text>
          <text class="item-value">{{ getCycleValueText(scheduledBill.cycleType, scheduledBill.cycleValue) }}</text>
        </view>
        <view class="detail-item">
          <text class="item-label">参考执行时间：</text>
          <text class="item-value">{{ scheduledBill.executeTime }}</text>
        </view>
        <view class="detail-item">
          <text class="item-label">金额：</text>
          <text class="item-value amount">{{ scheduledBill.price }}</text>
        </view>
        <view class="detail-item">
          <text class="item-label">是否提醒：</text>
          <text class="item-value">{{ scheduledBill.isRemind === 1 ? '是' : '否' }}</text>
        </view>
        <view class="detail-item">
          <text class="item-label">描述：</text>
          <text class="item-value description">{{ scheduledBill.description || '无' }}</text>
        </view>
      </view>

      <!-- 关联信息 -->
      <view class="detail-section">
        <view class="detail-item">
          <text class="item-label">分类：</text>
          <text class="item-value">{{ scheduledBill.className }}</text>
        </view>
        <view class="detail-item">
          <text class="item-label">账本：</text>
          <text class="item-value">{{ scheduledBill.ledgerName }}</text>
        </view>
        <view class="detail-item">
          <text class="item-label">账户：</text>
          <text class="item-value">{{ scheduledBill.accountName }}</text>
        </view>
      </view>

      <!-- 执行信息 -->
      <view class="detail-section">
        <view class="detail-item">
          <text class="item-label">创建时间：</text>
          <text class="item-value">{{ scheduledBill.createdAt }}</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-buttons">
        <button class="action-btn edit-btn" @click="navigateToEdit">编辑</button>
        <button class="action-btn log-btn" @click="navigateToLogList">执行记录</button>
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
      scheduledBill: null,
      id: null
    };
  },
  onLoad(options) {
    // 设置页面标题
    uni.setNavigationBarTitle({
      title: '定时记账配置详情'
    });
    
    if (options.id) {
      this.id = options.id;
      this.loadScheduledBillDetail();
    } else {
      this.errorMsg = '缺少配置ID';
      this.loading = false;
    }
  },
  methods: {
    /**
     * 加载定时记账配置详情
     */
    async loadScheduledBillDetail() {
      this.loading = true;
      this.errorMsg = '';
      
      try {
        const response = await request({
          url: backUrl.endpoints.scheduledBill_getById + this.id,
          method: 'GET'
        });
        
        if (response) {
          this.scheduledBill = response;
        } else {
          this.errorMsg = '获取配置详情失败';
        }
      } catch (error) {
        console.error('加载定时记账配置详情失败:', error);
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
      return '¥' + (n / 100).toFixed(2);
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
      return status === 1 ? 'status-enabled' : 'status-disabled';
    },
    
    /**
     * 获取状态文本
     */
    getStatusText(status) {
      switch (status) {
        case 1:
          return '启用';
        case 2:
          return '暂停';
        case 3:
          return '删除';
        default:
          return '未知';
      }
    },
    
    /**
     * 获取周期类型文本
     */
    getCycleTypeText(cycleType) {
      switch (cycleType) {
        case 1:
          return '每天';
        case 2:
          return '每周';
        case 3:
          return '每月';
        case 4:
          return '每季度';
        case 5:
          return '每年';
        default:
          return '未知';
      }
    },
    
    /**
     * 获取周期值文本
     */
    getCycleValueText(cycleType, cycleValue) {
      switch (cycleType) {
        case 1:
          return `每隔 ${cycleValue} 天`;
        case 2:
          return `每周 ${cycleValue}（${this.getWeekDayText(cycleValue)}）`;
        case 3:
          return `每隔 ${cycleValue} 个月`;
        case 4:
          return `每隔 ${cycleValue} 个季度`;
        case 5:
          return `每隔 ${cycleValue} 年`;
        default:
          return cycleValue;
      }
    },
    
    /**
     * 获取星期文本
     */
    getWeekDayText(weekDay) {
      const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
      return weekDays[weekDay - 1] || '';
    },
    
    /**
     * 返回上一页
     */
    goBack() {
      uni.navigateBack();
    },
    
    /**
     * 跳转到编辑页面
     */
    navigateToEdit() {
      uni.navigateTo({
        url: `/pages/scheduledBill/scheduledBillEdit?id=${this.id}`
      });
    },
    
    /**
     * 跳转到日志列表页面
     */
    navigateToLogList() {
      uni.navigateTo({
        url: `/pages/scheduledBill/scheduledBillLogList?scheduledBillId=${this.id}`
      });
    }
  }
};
</script>

<style scoped>
.scheduled-bill-detail-page {
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

.status-enabled {
  color: #52c41a;
}

.status-disabled {
  color: #ff4d4f;
}

.type-income {
  color: #1890ff;
}

.type-expense {
  color: #fa8c16;
}

.amount {
  font-size: 32rpx;
  font-weight: bold;
  color: #ff4d4f;
}

.description {
  line-height: 1.5;
  white-space: pre-wrap;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  margin-top: 20rpx;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
  border-radius: 40rpx;
}

.back-btn {
  background-color: #f5f5f5;
  color: #666666;
}

.edit-btn {
  background-color: #1989fa;
  color: #ffffff;
}

.log-btn {
  background-color: #52c41a;
  color: #ffffff;
}
</style>
