<template>
  <view class="scheduled-bill-log-list-page">
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载日志..."></uni-load-more>
    </view>

    <!-- 错误提示 -->
    <view class="error-container" v-if="errorMsg">
      <text class="error-text">{{ errorMsg }}</text>
      <button class="retry-btn" @click="loadScheduledBillLogs">重试</button>
    </view>

    <!-- 日志列表 -->
    <view class="log-list" v-if="!loading && !errorMsg">
      <view v-if="scheduledBillLogs.length > 0">
        <view
          class="log-item"
          v-for="(log, index) in scheduledBillLogs"
          :key="log.id"
          @click="showLogDetail(log)"
        >
          <view class="item-header">
            <text class="item-title">执行时间: {{ log.executeTime }}</text>
            <text class="item-status" :class="getStatusClass(log.status)">{{ getStatusText(log.status) }}</text>
          </view>
          <view class="item-details">
            <text class="detail-item">生成账单ID: {{ log.billId }}</text>
          </view>
          <view class="item-message">
            <text class="message-label">执行信息:</text>
            <text class="message-content">{{ log.errorMsg }}</text>
          </view>
          <view class="item-footer">
            <text class="footer-text">点击查看详情</text>
            <uni-icons type="arrowright" size="20" color="#999"></uni-icons>
          </view>
        </view>
      </view>
      <view class="no-data" v-else>
        暂无执行日志
      </view>
    </view>

    <!-- 分页组件 -->
    <view class="pagination-container" v-if="!loading && !errorMsg && total > 0">
      <uni-pagination 
        :current="currentPage" 
        :pageSize="pageSize" 
        :total="total" 
        :totalPages="totalPages"
        @change="handlePageChange"
        show-icon
      />
    </view>
  </view>
</template>

<script>
import request from '@/common/request.js';
import backUrl from '@/common/back_url.js';
import UniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue';
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import UniPagination from '@/uni_modules/uni-pagination/components/uni-pagination/uni-pagination.vue';

export default {
  components: {
    UniLoadMore,
    UniIcons,
    UniPagination
  },
  data() {
    return {
      loading: true,
      errorMsg: '',
      scheduledBillLogs: [],
      scheduledBillId: null,
      // 分页相关
      currentPage: 1,
      pageSize: 10,
      total: 0,
      totalPages: 0
    };
  },
  onLoad(options) {
    // 设置页面标题
    uni.setNavigationBarTitle({
      title: '执行日志列表'
    });
    
    if (options.scheduledBillId) {
      this.scheduledBillId = options.scheduledBillId;
      this.loadScheduledBillLogs();
    } else {
      this.errorMsg = '缺少配置ID';
      this.loading = false;
    }
  },
  methods: {
    /**
     * 加载定时记账执行日志
     */
    async loadScheduledBillLogs(pageNum = this.currentPage) {
      this.loading = true;
      this.errorMsg = '';
      
      try {
        const response = await request({
          url: backUrl.endpoints.scheduledBill_log_list + this.scheduledBillId + '/logs',
          method: 'GET',
          data: {
            pageNum: pageNum,
            pageSize: this.pageSize
          }
        });
        
        if (response) {
          this.scheduledBillLogs = response.itemList || [];
          this.total = response.total || 0;
          this.totalPages = response.totalPages || 0;
          this.currentPage = pageNum;
        } else {
          this.scheduledBillLogs = [];
          this.total = 0;
          this.totalPages = 0;
          this.currentPage = 1;
          this.errorMsg = '获取执行日志失败';
        }
      } catch (error) {
        console.error('加载执行日志失败:', error);
        this.errorMsg = '网络请求异常，请稍后重试';
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 格式化日期显示
     */
    formatDate(dateStr) {
      if (!dateStr) return '';
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
     * 查看日志详情
     */
    showLogDetail(log) {
      uni.navigateTo({
        url: `/pages/scheduledBill/scheduledBillLogDetail?id=${log.id}`
      });
    },
    
    /**
     * 处理分页变化
     */
    handlePageChange(event) {
      const pageNum = event.current;
      this.loadScheduledBillLogs(pageNum);
    }
  }
};
</script>

<style scoped>
.scheduled-bill-log-list-page {
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

.log-list {
  padding: 20rpx;
}

.form-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 20rpx;
}

.log-item {
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.item-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #0b0b0b;
  flex: 1;
}

.item-status {
  font-size: 26rpx;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
}

.status-success {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-failed {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.item-details {
  display: flex;
  gap: 40rpx;
  margin-bottom: 20rpx;
}

.detail-item {
  font-size: 26rpx;
  color: #666666;
}

.item-message {
  margin-bottom: 20rpx;
  padding: 20rpx;
  background-color: #f9f9f9;
  border-radius: 8rpx;
}

.message-label {
  font-size: 26rpx;
  color: #666666;
  font-weight: bold;
  display: block;
  margin-bottom: 8rpx;
}

.message-content {
  font-size: 26rpx;
  color: #333333;
  line-height: 1.5;
  white-space: pre-wrap;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.footer-text {
  font-size: 26rpx;
  color: #999999;
}

.no-data {
  text-align: center;
  padding: 80rpx 0;
  color: #999999;
  font-size: 28rpx;
}

/* 分页容器样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 40rpx 0;
  padding: 0 20rpx;
}
</style>
