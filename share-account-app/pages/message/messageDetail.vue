<template>
  <view class="message-detail-container">
    
    <!-- 消息详情内容 -->
    <view v-if="message" class="message-content">
      <!-- 消息头部 -->
      <view class="message-header">
        <view class="message-type-badge" :class="message.priority === 1 ? 'important' : ''">
          {{ message.type === 1 ? '系统消息' : '业务消息' }}
        </view>
        <text class="message-time">{{ formatFullTime(message.createdAt) }}</text>
      </view>
      
      <!-- 消息标题 -->
      <view class="message-title">
        {{ message.title }}
      </view>
      
      <!-- 消息正文：当没有评论ID时展示原始内容 -->
      <view v-if="!commentDetail" class="message-body" v-html="message.content"></view>
      
      <!-- 评论内容展示（账单评论：有评论ID） -->
      <view v-else class="comment-section">
        <view class="comment-body" v-if="commentDetail.type === 0">
          <text>{{ commentDetail.content }}</text>
        </view>
        <view v-else>
          <image class="comment-image" :src="commentDetail.imageUrl" mode="widthFix" @click="previewCommentImage(commentDetail.imageUrl)" />
        </view>
      </view>
      
      <!-- 回复评论按钮（仅对账单评论消息显示） -->
      <view class="actions">
        <button v-if="commentPayload && commentPayload.bizType === 'bill_comment' && commentPayload.billId" class="reply-btn" @click="replyComment">回复评论</button>
      </view>
      
      <!-- 重要消息标记 -->
      <view v-if="message.priority === 1" class="important-mark">
        <text class="iconfont icon-important"></text>
        <text>重要消息</text>
      </view>
    </view>
    
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <uni-load-more status="loading" />  
    </view>
    
    <!-- 错误状态 -->
    <view v-if="error" class="error-container">
      <text class="error-text">{{ error }}</text>
      <button class="retry-btn" @click="loadMessageDetail">重试</button>
    </view>
  </view>
</template>

<script>
import request from '../../common/request.js'
import backUrlConfig from '../../common/back_url.js'

export default {
  data() {
    return {
      message: null,
      loading: true,
      error: '',
      commentDetail: null
    }
  },
  // 解析评论业务JSON（不影响原有逻辑）
  computed: {
    commentPayload() {
      if (!this.message || !this.message.content) return null
      try {
        const c = this.message.content
        if (typeof c === 'string') {
          const obj = JSON.parse(c)
          if (obj && obj.bizType === 'bill_comment') return obj
        } else if (typeof c === 'object' && c !== null) {
          if (c.bizType === 'bill_comment') return c
        }
      } catch (e) {
        // 非JSON或解析失败，返回null
      }
      return null
    }
  },
  onLoad(options) {
    if (options.id) {
      this.messageId = options.id
      this.loadMessageDetail()
    } else {
      this.error = '消息ID不存在'
      this.loading = false
    }
  },
  methods: {
    // 加载消息详情
    async loadMessageDetail() {
      if (!this.messageId) return
      
      try {
        this.loading = true
        this.error = ''
        
        // 加载消息详情
        const res = await request({
          url: `${backUrlConfig.baseUrl}${backUrlConfig.endpoints.message_detail.replace('{id}', this.messageId)}`,
          method: 'GET'
        })
        
        if (res) {
          this.message = res
          
          // 如果是评论消息并带有评论ID，加载评论详情
          if (this.commentPayload && this.commentPayload.commentId) {
            await this.loadCommentDetail(this.commentPayload.commentId)
          } else {
            this.commentDetail = null
          }
          
          // 标记为已读
          if (!this.message.isRead) {
            try {
              await request({
                url: `${backUrlConfig.baseUrl}${backUrlConfig.endpoints.message_read.replace('{id}', this.messageId)}`,
                method: 'POST'
              })
              // 更新本地状态
              this.message.isRead = true
            } catch (markError) {
              console.error('标记消息已读失败:', markError)
            }
          }
        } else {
          this.error = res || '加载消息失败'
        }
      } catch (error) {
        console.error('加载消息详情失败:', error)
        this.error = '网络错误，请稍后重试'
      } finally {
        this.loading = false
      }
    },

    // 拉取评论详情（带文本或图片）
    async loadCommentDetail(commentId) {
      try {
        const resp = await request({
          url: `${backUrlConfig.baseUrl}${backUrlConfig.endpoints.bill_comment_getById}${commentId}`,
          method: 'GET'
        })
        if (resp && resp.id) {
          this.commentDetail = resp
        } else {
          this.commentDetail = null
        }
      } catch (e) {
        console.error('获取评论详情失败:', e)
        this.commentDetail = null
      }
    },
    
    // 图片预览
    previewCommentImage(url) {
      if (!url) return
      uni.previewImage({
        urls: [url],
        current: url
      })
    },
    
    // 格式化完整时间
    formatFullTime(timeStr) {
      if (!timeStr) return ''
      let s = timeStr
      if (typeof s === 'string') {
        // 兼容 iOS：当为 "yyyy-MM-dd HH:mm:ss" 等非 ISO 格式时，改用斜杠
        if (s.indexOf('T') === -1) {
          s = s.replace(/-/g, '/')
        }
      }
      const date = new Date(s)
      if (isNaN(date.getTime())) {
        // 解析失败则直接返回原字符串，避免渲染错误
        return typeof timeStr === 'string' ? timeStr : ''
      }
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },
    
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },
    
    // 跳转到评论页面（仅当为账单评论消息）
    replyComment() {
      if (this.commentPayload && this.commentPayload.billId) {
        uni.navigateTo({
          url: `/pages/comment/comment?billId=${this.commentPayload.billId}`
        })
      }
    }
  }
}
</script>

<style scoped>
.message-detail-container {
  background-color: #ffffff;
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  background-color: #ffffff;
  border-bottom: 1rpx solid #eeeeee;
}

.left-btn,
.right-btn {
  width: 100rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
}

.message-content {
  padding: 30rpx;
}

.message-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.message-type-badge {
  padding: 5rpx 20rpx;
  background-color: #e6f7ff;
  color: #1890ff;
  font-size: 24rpx;
  border-radius: 15rpx;
}

.message-type-badge.important {
  background-color: #fff2e8;
  color: #fa8c16;
}

.message-time {
  font-size: 24rpx;
  color: #999999;
}

.message-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 30rpx;
  line-height: 50rpx;
}

.message-body {
  font-size: 30rpx;
  color: #666666;
  line-height: 50rpx;
  white-space: pre-wrap;
}

.message-body :deep(p) {
  margin-bottom: 20rpx;
}

.message-body :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 20rpx 0;
}

.important-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 40rpx;
  padding: 20rpx;
  background-color: #fff2e8;
  border-radius: 10rpx;
  color: #fa8c16;
  font-size: 28rpx;
}

.important-mark .iconfont {
  margin-right: 10rpx;
  font-size: 32rpx;
}

.loading-container {
  padding: 100rpx 0;
  text-align: center;
}

.error-container {
  padding: 100rpx 30rpx;
  text-align: center;
}

.error-text {
  font-size: 28rpx;
  color: #ff4d4f;
  margin-bottom: 30rpx;
  display: block;
}

.retry-btn {
  background-color: #1989fa;
  color: white;
  border: none;
  padding: 15rpx 60rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
}
.comment-section {
  margin-top: 20rpx;
  padding: 20rpx;
  border-radius: 10rpx;
}
.comment-title {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
}
.comment-body {
  font-size: 30rpx;
  color: #666;
  line-height: 48rpx;
}
.comment-image {
  width: 100px;
  border-radius: 8rpx;
}
.reply-btn {
  margin-top: 50rpx;
  background-color: #1989fa;
  color: #fff;
  border-radius: 24px;
}
</style>