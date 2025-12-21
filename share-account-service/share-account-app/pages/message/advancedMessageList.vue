<template>
  <view class="message-page">
    <!-- 下拉刷新组件 -->
    <uni-load-more v-if="refreshing" status="refreshing" contenttext="正在刷新..."></uni-load-more>
    
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载消息..."></uni-load-more>
    </view>

    <!-- 错误提示 -->
    <view class="error-container" v-if="errorMsg">
      <text class="error-text">{{ errorMsg }}</text>
      <button class="retry-btn" @click="loadMessageList">重试</button>
    </view>

    <!-- 消息列表 -->
    <view class="message-list" v-if="!loading && !errorMsg">
      <view v-if="messageList.length > 0">
        <view
          class="message-item"
          v-for="message in messageList"
          :key="message.id"
          :class="{ 'unread': !message.isRead }"
        >
          <view class="item-info" @click="gotoDetail(message.id)">
            <view class="item-main-info">
              <text class="item-title">{{ message.title }}</text>
            </view>
            <view class="item-tags">
              <text class="item-tag message-type">
                {{ getMessageTypeText(message.type) }}
              </text>
              <text class="item-tag priority" :class="'priority-' + message.priority">
                {{ getPriorityText(message.priority) }}
              </text>
              <text class="item-tag date">
                {{ formatDate(message.createdAt) }}
              </text>
            </view>
          </view>
          <view class="item-actions">
            <view class="action-icon" @click.stop="editMessage(message)" title="编辑">
              <custom-icon type="bianji" :size="23" color="#007aff"></custom-icon>
            </view>
            <view class="action-icon" @click.stop="deleteMessage(message)" title="删除">
              <custom-icon type="shanchu" :size="23" color="#ff4d4f"></custom-icon>
            </view>
          </view>
        </view>
      </view>
      <view class="no-data" v-else>
        🤔 暂无消息
      </view>
    </view>

    <!-- 悬浮新增按钮 -->
    <view class="floating-add-btn" @click="gotoAddMessage">
      <text class="plus-icon">+</text>
    </view>
  </view>
</template>

<script>
import request from '../../common/request.js';
import backUrlConfig from '../../common/back_url.js';
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import UniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue';

export default {
  components: {
    UniIcons,
    UniLoadMore
  },
  data() {
    return {
      messageList: [],
      loading: false,
      refreshing: false,
      errorMsg: '',
      pageNum: 1,
      pageSize: 10,
      hasMore: true
    };
  },
  onLoad() {
  },
  onShow() {
    // 重置分页参数，确保从编辑页面返回后能重新加载完整列表
    this.hasMore = true;
    this.loadMessageList();
  },
  // 下拉刷新
  onPullDownRefresh() {
    // 重置分页参数
    this.pageNum = 1;
    this.hasMore = true;
    this.refreshing = true;
    // 重新加载数据
    this.loadMessageList();
  },
  methods: {
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '';
      let s = dateString;
      if (typeof s === 'string') {
        // 统一斜杠格式，兼容 iOS 对 "yyyy/MM/dd HH:mm:ss" 的解析
        s = s.replace(/-/g, '/');
      }
      const date = new Date(s);
      if (isNaN(date.getTime())) return dateString;
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },
    
    // 获取消息类型文本
    getMessageTypeText(type) {
      const typeMap = {
        1: '系统消息',
        2: '业务消息',
        3: '通知消息'
      };
      return typeMap[type] || '系统消息';
    },
    
    // 获取优先级文本
    getPriorityText(priority) {
      const priorityMap = {
        0: '普通优先级',
        1: '重要优先级'
      };
      return priorityMap[priority];
    },
    
    // 加载消息列表（使用新接口）
    async loadMessageList() {
      if (this.loading || !this.hasMore) return;
      
      this.loading = true;
      this.errorMsg = '';
      try {
        // 调用新接口，按消息类型、优先级、创建时间排序
        const res = await request.get(backUrlConfig.baseUrl + backUrlConfig.endpoints.message_advancedList, {
          pageNum: this.pageNum,
          pageSize: this.pageSize
        });
        
        if (res) {
          const newMessages = res.list || [];
          
          if (this.pageNum === 1) {
            this.messageList = newMessages;
          } else {
            this.messageList = [...this.messageList, ...newMessages];
          }
          
          // 检查是否还有更多数据
          this.hasMore = newMessages.length === this.pageSize;
          
          // 如果还有更多数据，增加页码
          if (this.hasMore) {
            this.pageNum++;
          }
        } else {
          this.errorMsg = res.message || '加载消息列表失败';
        }
      } catch (error) {
        console.error('加载消息列表失败:', error);
        this.errorMsg = '加载消息列表失败，请重试';
      } finally {
        this.loading = false;
        // 如果正在刷新，停止刷新动画
        if (this.refreshing) {
          this.refreshing = false;
          uni.stopPullDownRefresh();
        }
      }
    },
    
    // 编辑消息
    editMessage(message) {
      // 跳转到编辑页面
      uni.navigateTo({
        url: `/pages/message/addMessage?id=${message.id}`
      });
    },
    
    // 删除消息
    deleteMessage(message) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除消息"${message.title}"吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await request.put(backUrlConfig.baseUrl + backUrlConfig.endpoints.message_updateStatus, {
                id: message.id,
                status: 1 // 1 表示已删除状态
              });
              // 重新加载消息列表
              this.hasMore = true;
              this.loadMessageList();
              uni.showToast({ title: '删除成功', icon: 'success' });
            } catch (error) {
              console.error('删除消息失败:', error);
              uni.showToast({ title: '删除消息失败', icon: 'none' });
            }
          }
        }
      });
    },
    
    // 跳转到新增/编辑消息页面
    gotoAddMessage() {
      uni.navigateTo({
        url: '/pages/message/addMessage'
      });
    },
    
    // 跳转到消息详情页
    async gotoDetail(messageId) {
      try {
        // 标记消息为已读
        await request.post(backUrlConfig.baseUrl + backUrlConfig.endpoints.message_read.replace('{id}', messageId));
        
        // 更新本地列表中该消息的已读状态
        const message = this.messageList.find(m => m.id === messageId);
        if (message) {
          message.isRead = true;
        }
      } catch (error) {
        console.error('标记消息已读失败:', error);
      }
      
      // 跳转到详情页
      uni.navigateTo({
        url: `/pages/message/messageDetail?id=${messageId}`
      });
    },
    
    // 上拉加载更多
    onReachBottom() {
      this.loadMessageList();
    }
  }
};
</script>

<style scoped>
.message-page {
  padding-bottom: 80rpx;
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

.message-list {
  padding: 20rpx;
}

.message-item {
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

/* 未读消息样式 */
.message-item.unread {
  background-color: #f8f8f8;
  box-shadow: 0 2rpx 8rpx rgba(0, 122, 255, 0.1);
}

.item-info {
  flex: 1;
  max-width: calc(100% - 80rpx);
}

.item-main-info {
  margin-bottom: 16rpx;
}

.item-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333333;
  display: block;
}

.item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.item-tag {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
  background-color: #f0f0f0;
  color: #666666;
}

.item-tag.message-type {
  background-color: #e6f7ff;
  color: #1890ff;
}

.item-tag.priority {
  background-color: #fff7e6;
  color: #fa8c16;
}

.item-tag.priority-3 {
  background-color: #fff1f0;
  color: #f5222d;
}

.item-tag.date {
  background-color: #f6ffed;
  color: #52c41a;
}

.item-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.action-icon {
  padding: 8rpx;
  border-radius: 8rpx;
}

.action-icon:active {
  background-color: #f0f0f0;
}

.no-data {
  text-align: center;
  padding: 80rpx 0;
  color: #999999;
  font-size: 28rpx;
}

.floating-add-btn {
  position: fixed;
  right: 30rpx;
  bottom: 80rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background-color: #1989fa;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 20rpx rgba(7, 193, 96, 0.3);
}

.plus-icon {
  font-size: 60rpx;
  line-height: 1;
}
</style>