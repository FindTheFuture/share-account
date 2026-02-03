<template>
  <view class="time-picker-container">
    <view class="time-picker-header">
      <text class="time-picker-title">{{ title || '选择时间' }}</text>
      <view class="time-picker-actions">
        <text class="time-picker-cancel" @click="handleCancel">取消</text>
        <text class="time-picker-confirm" @click="handleConfirm">确定</text>
      </view>
    </view>
    <view class="time-picker-content">
      <picker-view 
        indicator-style="height: 50px;" 
        style="width: 100%; height: 300px;" 
        :value="pickerValue" 
        @change="handleChange"
      >
        <picker-view-column>
          <view 
            v-for="hour in hours" 
            :key="hour" 
            style="line-height: 50px; text-align: center; font-size: 28rpx;"
          >
            {{ formatNumber(hour) }}
          </view>
        </picker-view-column>
        <picker-view-column>
          <view 
            v-for="minute in minutes" 
            :key="minute" 
            style="line-height: 50px; text-align: center; font-size: 28rpx;"
          >
            {{ formatNumber(minute) }}
          </view>
        </picker-view-column>
        <picker-view-column>
          <view 
            v-for="second in seconds" 
            :key="second" 
            style="line-height: 50px; text-align: center; font-size: 28rpx;"
          >
            {{ formatNumber(second) }}
          </view>
        </picker-view-column>
      </picker-view>
      <view class="time-picker-dividers">
        <text class="time-picker-divider">:</text>
        <text class="time-picker-divider">:</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'TimePicker',
  props: {
    title: {
      type: String,
      default: '选择时间'
    },
    value: {
      type: String,
      default: '08:00:00'
    }
  },
  data() {
    return {
      hours: Array.from({length: 24}, (_, i) => i),
      minutes: Array.from({length: 60}, (_, i) => i),
      seconds: Array.from({length: 60}, (_, i) => i),
      pickerValue: [8, 0, 0] // 默认值：08:00:00
    };
  },
  watch: {
    value: {
      handler(newValue) {
        this.setTimeValue(newValue);
      },
      immediate: true
    }
  },
  methods: {
    /**
     * 设置时间值
     */
    setTimeValue(timeString) {
      if (!timeString) return;
      
      const parts = timeString.split(':');
      if (parts.length === 3) {
        const hour = parseInt(parts[0]) || 0;
        const minute = parseInt(parts[1]) || 0;
        const second = parseInt(parts[2]) || 0;
        
        this.pickerValue = [hour, minute, second];
      }
    },
    
    /**
     * 处理选择变化
     */
    handleChange(e) {
      this.pickerValue = e.detail.value;
    },
    
    /**
     * 格式化数字为两位数
     */
    formatNumber(num) {
      return num.toString().padStart(2, '0');
    },
    
    /**
     * 处理取消
     */
    handleCancel() {
      this.$emit('cancel');
    },
    
    /**
     * 处理确认
     */
    handleConfirm() {
      const [hour, minute, second] = this.pickerValue;
      const timeString = `${this.formatNumber(hour)}:${this.formatNumber(minute)}:${this.formatNumber(second)}`;
      this.$emit('confirm', timeString);
    }
  }
};
</script>

<style scoped>
.time-picker-container {
  background-color: #ffffff;
  border-radius: 20rpx 20rpx 0 0;
  padding-bottom: 30rpx;
}

.time-picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.time-picker-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #333333;
}

.time-picker-actions {
  display: flex;
  gap: 30rpx;
}

.time-picker-cancel {
  font-size: 28rpx;
  color: #999999;
}

.time-picker-confirm {
  font-size: 28rpx;
  color: #1890ff;
  font-weight: 500;
}

.time-picker-content {
  position: relative;
  padding: 20rpx 0;
}

.time-picker-dividers {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 100rpx;
}

.time-picker-divider {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
}
</style>