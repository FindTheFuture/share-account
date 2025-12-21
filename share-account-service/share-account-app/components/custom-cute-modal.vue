<template>
  <uni-popup ref="popup" type="center" :mask-click="false" :mask="true" class="cute-modal-popup">
    <view class="cute-modal-container">
      <!-- 可爱图标区域 -->
      <view class="icon-container">
        <view class="icon-circle">
          <custom-icon type="baobao" :size="60" :color="iconColor" class="bounce-animation"></custom-icon>
        </view>
      </view>
      
      <!-- 标题区域 -->
      <view class="title-container">
        <text class="modal-title">{{ title }}</text>
      </view>
      
      <!-- 内容区域 -->
      <view class="content-container">
        <text class="modal-content">{{ content }}</text>
      </view>
      
      <!-- 按钮区域 -->
      <view class="button-container">
        <button class="confirm-button" @click="handleConfirm">{{ confirmText }}</button>
      </view>
    </view>
  </uni-popup>
</template>

<script>
import UniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
import CustomIcon from './custom-icon/custom-icon.vue';

export default {
  name: 'CustomCuteModal',
  components: {
    UniPopup,
    CustomIcon
  },
  props: {
    title: {
      type: String,
      default: '提示'
    },
    content: {
      type: String,
      required: true
    },
    confirmText: {
      type: String,
      default: '确定'
    },
    iconType: {
      type: String,
      default: 'kongxincai' // 爱心图标作为默认
    },
    iconColor: {
      type: String,
      default: '#ff6b81' // 粉红色作为默认
    }
  },
  methods: {
    // 显示弹窗
    show() {
      this.$refs.popup?.open();
    },
    
    // 隐藏弹窗
    hide() {
      this.$refs.popup?.close();
    },
    
    // 处理确定按钮点击
    handleConfirm() {
      this.hide();
      this.$emit('confirm');
    }
  }
};
</script>

<style scoped>
.cute-modal-popup {
  /* 确保在各种设备上居中显示 */
  display: flex;
  align-items: center;
  justify-content: center;
  /* 使用flex布局确保弹窗容器始终占据整个视口 */
  width: 100%;
  height: 100%;
}

.cute-modal-container {
  width: 70%;
  max-width: 500rpx;
  background: white;
  border-radius: 30rpx;
  padding: 40rpx;
  box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  /* 确保在真机上居中显示 */
  margin: 0 auto;
  /* 添加可爱的装饰效果 */
}

/* 可爱的顶部波浪装饰 */
.cute-modal-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 12rpx;
  background: linear-gradient(135deg, #ff6b81 25%, transparent 25%) -12rpx 0,
              linear-gradient(225deg, #ff6b81 25%, transparent 25%) -12rpx 0,
              linear-gradient(315deg, #ff6b81 25%, transparent 25%),
              linear-gradient(45deg, #ff6b81 25%, transparent 25%);
  background-size: 24rpx 24rpx;
}

.icon-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 30rpx;
}

.icon-circle {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #fff5f7, #fff0f3);
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 
    0 8rpx 16rpx rgba(255, 107, 129, 0.15),
    inset 0 2rpx 4rpx rgba(255, 255, 255, 0.5),
    inset 0 -2rpx 4rpx rgba(255, 107, 129, 0.1);
  position: relative;
  animation: float 3s ease-in-out infinite;
}

/* 图标周围的装饰点 */
.icon-circle::before,
.icon-circle::after {
  content: '';
  position: absolute;
  width: 16rpx;
  height: 16rpx;
  background: #ffd8dc;
  border-radius: 50%;
  animation: blink 2s ease-in-out infinite;
}

.icon-circle::before {
  top: -10rpx;
  right: 20rpx;
}

.icon-circle::after {
  bottom: -5rpx;
  left: 20rpx;
  animation-delay: 0.5s;
}

/* 图标弹跳动画 */
.bounce-animation {
  animation: bounce 1.5s ease-in-out infinite;
}

/* 浮动动画 */
@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10rpx);
  }
}

/* 弹跳动画 */
@keyframes bounce {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

/* 闪烁动画 */
@keyframes blink {
  0%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

.title-container {
  text-align: center;
  margin-bottom: 20rpx;
}

.modal-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.content-container {
  padding: 20rpx 0;
  margin-bottom: 30rpx;
}

.modal-content {
  font-size: 30rpx;
  color: #666;
  line-height: 1.6;
  text-align: center;
  word-break: break-word;
}

.button-container {
  display: flex;
  justify-content: center;
}

.confirm-button {
  background: linear-gradient(135deg, #ff6b81, #ff8fab);
  color: white;
  font-size: 32rpx;
  border-radius: 60rpx;
  padding: 20rpx 0;
  width: 100%;
  border: none;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 129, 0.3);
}

.confirm-button:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 6rpx rgba(255, 107, 129, 0.2);
}
</style>
