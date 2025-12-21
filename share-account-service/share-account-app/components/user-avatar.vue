<template>
  <view class="user-avatar-container" @click="handleClick">
    <view class="avatar-wrapper">
      <!-- 使用image标签作为主要显示方式，确保兼容性 -->
      <image
        class="avatar-img"
        :src="processedAvatarUrl"
        mode="aspectFill"
      ></image>
      <view class="edit-mask" v-if="showEditMaskActual">
        <text class="edit-text">{{ editText }}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'user-avatar',
  props: {
    // 头像URL
    avatarUrl: {
      type: String,
      default: ''
    },
    // 默认头像URL
    defaultAvatar: {
      type: String,
      default: '/static/default_avatar.webp'
    },
    // 是否可点击
    clickable: {
      type: Boolean,
      default: true
    },
    // 是否可上传头像
    uploadable: {
      type: Boolean,
      default: true
    },
    // 是否显示编辑遮罩
    showEditMask: {
      type: Boolean,
      default: null
    },
    // 编辑文本
    editText: {
      type: String,
      default: '修改头像'
    },
    // 用户ID（用于上传路径）
    userId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      processedAvatarUrl: ''
    }
  },
  watch: {
    // 监听头像URL变化
    avatarUrl: {
      immediate: true,
      handler(newVal) {
        this.processAvatarUrl(newVal);
      }
    }
  },
  computed: {
    // 计算显示编辑遮罩的实际值
    showEditMaskActual() {
      return this.showEditMask !== null ? this.showEditMask : this.uploadable;
    }
  },
  methods: {
    // 处理头像URL，添加时间戳防缓存
    processAvatarUrl(url) {
      
      if (url) {
        // 清理URL
        let processedUrl = url.split('#')[0].trim();
        // 强制HTTPS
        processedUrl = processedUrl.replace('http://', 'https://');
        // 添加时间戳防止缓存（仅在允许上传时才启用，以避免列表场景重复请求）
        const shouldBustCache = !!this.uploadable;
        if (shouldBustCache) {
          const timestamp = Date.now();
          // 移除旧的时间戳参数
          if (processedUrl.includes('?')) {
            processedUrl = processedUrl.replace(/([?&])t=[^&]*(&|$)/, '$1');
            if (processedUrl.endsWith('?')) {
              processedUrl = processedUrl.slice(0, -1);
            }
          }
          // 添加新的时间戳
          processedUrl += (processedUrl.includes('?') ? '&' : '?') + `t=${timestamp}`;
        } else {
          // 不防缓存：移除历史 t 参数，保持稳定URL以命中浏览器/CDN缓存
          if (processedUrl.includes('?')) {
            processedUrl = processedUrl.replace(/([?&])t=[^&]*(&|$)/, '$1');
            if (processedUrl.endsWith('?')) {
              processedUrl = processedUrl.slice(0, -1);
            }
          }
        }
        this.processedAvatarUrl = processedUrl;
      } else {
        // 使用默认头像
        this.processedAvatarUrl = this.defaultAvatar;
      }
    },
    // 处理点击事件
    handleClick() {
      if (this.clickable && this.uploadable) {
        this.$emit('click');
      }
    },
    // 选择头像
    chooseImage() {
      return new Promise((resolve, reject) => {
        // 检查是否允许上传
        if (!this.uploadable) {
          const error = new Error('当前不允许上传头像');
          console.warn(error.message);
          reject(error);
          return;
        }
        
        uni.chooseImage({
          count: 1,
          sizeType: ['compressed'],
          sourceType: ['album', 'camera'],
          success: (res) => {
            const tempFilePath = res.tempFilePaths[0];
            resolve(tempFilePath);
          },
          fail: (err) => {
            console.error('选择图片失败:', err);
            reject(err);
          }
        });
      });
    },
    // 上传头像
    async uploadAvatar(filePath) {
      // 检查是否允许上传
      if (!this.uploadable) {
        const error = new Error('当前不允许上传头像');
        console.warn(error.message);
        this.$emit('upload-error', error);
        throw error;
      }
      
      if (!this.userId) {
        throw new Error('上传头像需要提供userId');
      }
      
      try {
        const result = await this.$uploadFile({
          filePath,
          fileType: 1,
          pathType: 0,
          path: `/user/${this.userId}/`,
          objectId: this.userId
        });
        
        // 处理上传成功后的URL
        this.processAvatarUrl(result.address);
        
        // 触发上传成功事件
        this.$emit('upload-success', {
          url: result.address,
          processedUrl: this.processedAvatarUrl
        });
        
        return {
          url: result.address,
          processedUrl: this.processedAvatarUrl
        };
      } catch (error) {
        console.error('上传头像失败:', error);
        this.$emit('upload-error', error);
        throw error;
      }
    }
  }
}
</script>

<style lang="scss">
.user-avatar-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 200rpx;
  height: 200rpx;
}

.avatar-wrapper {
  position: relative;
  width: 200rpx !important;
  height: 200rpx !important;
  border-radius: 50%;
  overflow: hidden;
  border: 4rpx solid #ffffff;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  background-color: #f8f8f8;
  display: block !important;
  
  &:active {
    transform: scale(0.95);
  }
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

.edit-mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 25%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  border-bottom-left-radius: 100rpx;
  border-bottom-right-radius: 100rpx;
  opacity: 1;
}

.edit-text {
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 500;
  text-align: center;
  padding: 10rpx;
}
</style>