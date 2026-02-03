<template>
  <view class="message-list-container">
    <!-- 下拉刷新组件 -->
    <view v-if="refreshing" class="refresh-indicator">
      <uni-load-more status="refreshing" contenttext="正在刷新..."></uni-load-more>
    </view>
    
    <!-- 消息类型切换 -->
    <view class="message-tabs">
      <view 
        v-for="tab in tabs" 
        :key="tab.value"
        class="tab-item"
        :class="{ active: activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </view>
    </view>
    
    <!-- 消息列表 -->
    <view class="message-list">
      <view 
        v-for="message in messages" 
        :key="message.id"
        class="message-item"
        :class="{ unread: !message.isRead }"
        @click="gotoDetail(message.id)"
      >
        <!-- 消息内容 -->
        <view class="message-content">
          <view class="message-header">
            <text class="message-title">{{ message.title }}</text>
            <text class="message-time">{{ formatTime(message.createdAt) }}</text>
          </view>
          <text class="message-brief" :class="{ 'detail-hint': isBillComment(message.content) }">
            {{ isBillComment(message.content) ? '查看详情' : getContentBrief(message.content) }}
          </text>
          </view>
        
        <!-- 未读标记 -->
        <view v-if="!message.isRead" class="unread-dot"></view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="messages.length === 0 && !loading" class="empty-state">
        <text class="empty-text">暂无消息</text>
      </view>
    </view>

    <!-- 分页控件：业务消息和系统消息共用，但依据activeTab显示不同总量与当前页 -->
    <view class="pagination-wrapper">
      <uni-pagination
        :current="pageByTab[activeTab]"
        :total="totalByTab[activeTab]"
        :pageSize="size"
        @change="onPageChange"
      />
    </view>
    
    <!-- 悬浮的全部已读按钮 -->
    <view class="float-read-all-btn" @click="markAllAsRead">
      <text class="read-all-text">全部已读</text>
    </view>
  </view>
</template>

<script>
import { mapActions, mapState } from 'vuex'
import request from '../../common/request.js'
import backUrlConfig from '../../common/back_url.js'

export default {
  data() {
    return {
      tabs: [
        { label: '业务消息', value: 2 },
        { label: '系统消息', value: 1 }
      ],
      activeTab: 2,
      messages: [],
      loading: false,
      refreshing: false,
      size: 10,
      // 每个tab独立维护分页状态与总量
      pageByTab: { 1: 1, 2: 1 },
      totalByTab: { 1: 0, 2: 0 },
      pagesByTab: { 1: 0, 2: 0 }
    }
  },
  computed: {
    ...mapState({
      messageList: state => state.message.messageList
    })
  },
  onShow() {
    // 初始化当前tab的分页数据
    this.loadMessages()
  },
  // 下拉刷新
  onPullDownRefresh() {
    // 保持当前页，重新拉取内容
    this.refreshing = true
    this.loadMessages()
  },
  methods: {
    // 将所有消息标记为已读
    async markAllAsRead() {
      try {
        await request({
          url: `${backUrlConfig.baseUrl}${backUrlConfig.endpoints.message_readAll}`,
          method: 'POST',
          data: {
            type: this.activeTab === '' ? null : this.activeTab
          }
        })
        // 标记成功后，重新加载当前页数据
        this.loadMessages()
        uni.showToast({ title: '全部已读', icon: 'success' })
      } catch (error) {
        console.error('标记全部已读失败:', error)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },
    
    // 切换消息类型标签（保留当前页数）
    switchTab(tabValue) {
      this.activeTab = tabValue
      // 切换后不重置页码，直接按当前页加载
      this.messages = []
      this.loadMessages()
    },
    
    // 加载消息列表（统一接收分页元数据）
    async loadMessages() {
      if (this.loading) return
      try {
        this.loading = true
        const currentPage = this.pageByTab[this.activeTab]
        const res = await request({
          url: `${backUrlConfig.baseUrl}${backUrlConfig.endpoints.message_list}`,
          method: 'GET',
          data: {
            type: this.activeTab === '' ? null : this.activeTab,
            page: currentPage,
            size: this.size
          }
        })
        if (res) {
          const list = Array.isArray(res.list) ? res.list : []
          const total = typeof res.total === 'number' ? res.total : 0
          const pages = typeof res.pages === 'number' ? res.pages : 0
          // 更新当前tab的分页元数据
          this.messages = list
          this.totalByTab[this.activeTab] = total
          this.pagesByTab[this.activeTab] = pages
        }
      } catch (error) {
        console.error('加载消息列表失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
        if (this.refreshing) {
          this.refreshing = false
          uni.stopPullDownRefresh()
        }
      }
    },
    
    // 分页切换
    onPageChange(e) {
      const nextPage = e?.current || 1
      this.pageByTab[this.activeTab] = nextPage
      this.loadMessages()
    },
    
    // 跳转到消息详情
    async gotoDetail(messageId) {
      // 标记为已读
      try {
        if (this.$store && this.$store.dispatch) {
          // store相关调用占位
        } else {
          await request({
            url: `${backUrlConfig.baseUrl}${backUrlConfig.endpoints.message_read.replace('{id}', messageId)}`,
            method: 'POST'
          })
        }
      } catch (error) {
        console.error('标记消息已读失败:', error)
      }
      // 跳转到详情页
      uni.navigateTo({ url: `/pages/message/messageDetail?id=${messageId}` })
    },
    
    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return ''
      let s = timeStr
      if (typeof s === 'string') {
        if (s.indexOf('T') === -1) s = s.replace(/-/g, '/')
      }
      const date = new Date(s)
      if (isNaN(date.getTime())) return typeof timeStr === 'string' ? timeStr : ''
      const now = new Date()
      const diff = now - date
      if (diff < 60 * 1000) return '刚刚'
      if (diff < 60 * 60 * 1000) return `${Math.floor(diff / (60 * 1000))}分钟前`
      if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
      if (diff < 7 * 24 * 60 * 60 * 1000) return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    },
    
    // 获取内容摘要
    getContentBrief(content) {
      if (!content) return ''
      const plainText = content.replace(/<[^>]+>/g, '')
      return plainText.length > 50 ? plainText.substring(0, 50) + '...' : plainText
    },
    // 判断是否为账单评论业务消息
    isBillComment(content) {
      if (!content) return false
      try {
        const c = typeof content === 'string' ? JSON.parse(content) : content
        return c && typeof c === 'object' && c.bizType === 'bill_comment'
      } catch (e) {
        return false
      }
    },
    
    // 返回上一页
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.message-list-container {
  background-color: #f5f5f5;
  min-height: 100vh;
  padding-top: 100rpx; /* 为固定顶部Tab留出空间 */
  padding-bottom: 140rpx; /* 为悬浮按钮及分页留出空间 */
}

.refresh-indicator {
  padding: 20rpx 0;
  text-align: center;
}

.message-tabs {
  display: flex;
  background-color: #ffffff;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 99;
  border-bottom: 1rpx solid #eee;
}

.tab-item {
  flex: 1;
  padding: 20rpx 0;
  text-align: center;
  font-size: 28rpx;
  color: #666666;
  border-bottom: 2rpx solid transparent;
}

.tab-item.active {
  color: #1989fa;
  border-bottom-color: #1989fa;
}

.message-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.message-item.unread {
  background-color: #f8f8f8;
}

.message-content { flex: 1; }
.message-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
.message-title { font-size: 30rpx; color: #333; }
.message-time { font-size: 24rpx; color: #999; }
.message-brief { font-size: 26rpx; color: #666; }
.detail-hint { color: #999; }

.unread-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background-color: #ff4d4f;
  margin-left: 12rpx;
  margin-top: 12rpx;
}

.empty-state { text-align: center; padding: 20rpx; color: #999; }

.pagination-wrapper {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 80rpx;
  background: #fff;
  padding: 20rpx 30rpx;
  box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.05);
}

.float-read-all-btn {
  position: fixed;
  right: 20rpx;
  bottom: 200rpx;
  background-color: #1989fa;
  color: #ffffff;
  padding: 16rpx 24rpx;
  border-radius: 30rpx;
}

.read-all-text { font-size: 28rpx; }
</style>