<template>
  <view class="message-send-container">
    
    <!-- 表单内容 -->
    <view class="form-container">
      <!-- 消息类型 -->
      <view class="form-item">
        <view class="form-label">消息类型</view>
        <radio-group class="radio-group" @change="handleTypeChange">
          <label class="radio-item">
            <radio value="1" :checked="formData.type === 1" />系统消息
          </label>
          <label class="radio-item">
            <radio value="2" :checked="formData.type === 2" />业务消息
          </label>
        </radio-group>
      </view>
      
      <!-- 重要性 -->
      <view class="form-item">
        <view class="form-label">重要性</view>
        <radio-group class="radio-group" @change="handlePriorityChange">
          <label class="radio-item">
            <radio value="0" :checked="formData.priority === 0" />普通
          </label>
          <label class="radio-item">
            <radio value="1" :checked="formData.priority === 1" />重要
          </label>
        </radio-group>
      </view>
      
      <!-- 消息标题 -->
      <view class="form-item">
        <view class="form-label">标题</view>
        <input 
          class="form-input" 
          v-model="formData.title"
          placeholder=""
          maxlength="100"
        />
      </view>
      
      <!-- 消息内容 -->
      <view class="form-item">
        <view class="form-label">内容</view>
        <textarea 
          class="content-textarea"
          v-model="formData.content"
          placeholder=""
          auto-height
          show-confirm-bar="true"
        />
      </view>
    </view>
    
    <!-- 底部发送按钮 -->
    <view class="bottom-btn-container">
      <button class="send-btn" @click="sendMessage">保存消息</button>
    </view>
  </view>
</template>

<script>
import request from '@/common/request.js';
import backUrlConfig from '@/common/back_url.js';

export default {
  data() {
    return {
      formData: {
        id: null,
        type: 1,  // 默认系统消息
        priority: 0,  // 默认普通消息
        title: '',
        content: ''
      },
      isEdit: false
    }
  },
  onLoad(options) {
    // 检查是否为编辑模式
    if (options.id) {
      this.isEdit = true
      this.formData.id = options.id
      // 加载消息详情数据
      this.loadMessageDetail(options.id)
    }
  },
  methods: {
    // 获取消息详情
    async getMessageDetailApi(id) {
      // 使用back_url.js中定义的接口路径，注意替换{id}占位符
      // 注意：根据后端代码，此接口应该使用GET方法
      const url = backUrlConfig.endpoints.message_detail.replace('{id}', id);
      return request.get(url);
    },

    // 保存消息（新增或更新）
    async saveMessageApi(messageData) {
      return request.post(backUrlConfig.endpoints.message_save, messageData);
    },
    // 处理消息类型变化
    handleTypeChange(e) {
      this.formData.type = parseInt(e.detail.value)
    },
    
    // 处理重要性变化
    handlePriorityChange(e) {
      this.formData.priority = parseInt(e.detail.value)
    },
    
    // 加载消息详情
    async loadMessageDetail(id) {
      try {
        uni.showLoading({
          title: '加载中...'
        })
        
        // 调用获取消息详情的API
        const res = await this.getMessageDetailApi(id)
        if (res) {
          this.formData = res
        } else {
          uni.showToast({
            title: res && res.msg || '加载失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载消息详情失败:', error)
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },
    
    // 验证表单
    validateForm() {
      if (!this.formData.title.trim()) {
        uni.showToast({
          title: '请输入消息标题',
          icon: 'none'
        })
        return false
      }
      
      if (!this.formData.content.trim()) {
        uni.showToast({
          title: '请输入消息内容',
          icon: 'none'
        })
        return false
      }
      
      return true
    },
    
    // 发送/更新消息
    async sendMessage() {
      if (!this.validateForm()) return
      
      try {
        uni.showLoading({
          title: this.isEdit ? '更新中...' : '发送中...'
        })
        
        // 准备发送数据（所有消息默认发送给所有用户）
        const messageData = {
          ...this.formData,
          target: 'all' // 设置为发送给所有用户
        }
        
        // 调用保存消息的API
        const res = await this.saveMessageApi(messageData)
        
        if (res) {
          uni.showToast({
            title: this.isEdit ? '更新成功' : '发送成功',
            icon: 'success'
          })
          
          // 返回上一页
          setTimeout(() => {
            this.goBack()
          }, 1500)
        } else {
          uni.showToast({
            title: this.isEdit ? '更新失败' : '发送失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error(this.isEdit ? '更新消息失败:' : '发送消息失败:', error)
        uni.showToast({
          title: this.isEdit ? '更新失败，请重试' : '发送失败，请重试',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },
    
    // 返回上一页
    goBack() {
      uni.navigateBack();
    }
  }
}
</script>

<style scoped>
.message-send-container {
  background-color: #f5f5f5;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
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

.form-container {
  padding: 30rpx;
  flex: 1;
}

.form-item {
  background-color: #ffffff;
  padding: 20rpx;
  border-radius: 10rpx;
  margin-bottom: 20rpx;
}

.form-label {
  font-size: 28rpx;
  color: #333333;
  margin-bottom: 20rpx;
}

.radio-group {
  display: flex;
  flex-wrap: wrap;
}

.radio-item {
  margin-right: 50rpx;
  margin-bottom: 10rpx;
  font-size: 28rpx;
  color: #666666;
}

.form-input {
  width: 100%;
  padding: 15rpx;
  border: 1rpx solid #e8e8e8;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333333;
}

.content-textarea {
  width: 100%;
  min-height: 300rpx;
  padding: 15rpx;
  border: 1rpx solid #e8e8e8;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333333;
  line-height: 1.5;
  box-sizing: border-box;
}

.bottom-btn-container {
  padding: 30rpx;
  background-color: #ffffff;
  border-top: 1rpx solid #eeeeee;
}

.send-btn {
  

  background-color: #1989fa;
  color: #ffffff;
  flex: 1;
  height: 100rpx;
  line-height: 100rpx;
  font-size: 28rpx;
  border-radius: 40rpx;
  margin: 0 10rpx;
}
</style>