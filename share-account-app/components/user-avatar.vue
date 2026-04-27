<template>
  <view id="user-avatar-root" class="user-avatar" :style="containerStyle" @click="handleClick">
    <image
      id="avatar-img"
      class="avatar-image"
      :src="processedAvatarUrl"
      mode="aspectFill"
      @error="onImageError"
      @load="onImageLoad"
    ></image>
    <view v-if="showEditMaskActual" class="edit-mask">
      <text class="edit-text">{{ editText }}</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'user-avatar',
  props: {
    avatarUrl: {
      type: String,
      default: ''
    },
    defaultAvatar: {
      type: String,
      default: '/static/default_avatar.webp'
    },
    clickable: {
      type: Boolean,
      default: true
    },
    uploadable: {
      type: Boolean,
      default: true
    },
    showEditMask: {
      type: Boolean,
      default: null
    },
    editText: {
      type: String,
      default: '修改头像'
    },
    userId: {
      type: String,
      default: ''
    },
    size: {
      type: String,
      default: '88rpx'
    }
  },
  data() {
    return {
      processedAvatarUrl: ''
    }
  },
  computed: {
    showEditMaskActual() {
      return this.showEditMask !== null ? this.showEditMask : this.uploadable;
    },
    containerStyle() {
      return {
        width: this.size,
        height: this.size
      };
    }
  },
  watch: {
    avatarUrl: {
      immediate: true,
      handler(newVal) {
        this.processAvatarUrl(newVal);
      }
    }
  },
  methods: {
    processAvatarUrl(url) {
      if (url) {
        let processedUrl = url.split('#')[0].trim();
        processedUrl = processedUrl.replace('http://', 'https://');
        const shouldBustCache = !!this.uploadable;
        if (shouldBustCache) {
          const timestamp = Date.now();
          if (processedUrl.includes('?')) {
            processedUrl = processedUrl.replace(/([?&])t=[^&]*(&|$)/, '$1');
            if (processedUrl.endsWith('?')) {
              processedUrl = processedUrl.slice(0, -1);
            }
          }
          processedUrl += (processedUrl.includes('?') ? '&' : '?') + `t=${timestamp}`;
        } else {
          if (processedUrl.includes('?')) {
            processedUrl = processedUrl.replace(/([?&])t=[^&]*(&|$)/, '$1');
            if (processedUrl.endsWith('?')) {
              processedUrl = processedUrl.slice(0, -1);
            }
          }
        }
        this.processedAvatarUrl = processedUrl;
      } else {
        this.processedAvatarUrl = this.defaultAvatar;
      }
    },
    onImageError(e) {
      console.error('[user-avatar] 图片加载失败:', this.processedAvatarUrl, e);
    },
    onImageLoad(e) {
      this.$nextTick(() => {
        const query = uni.createSelectorQuery().in(this);
        query.select('#user-avatar-root').boundingClientRect(rect => {
        }).exec();
        query.select('#avatar-img').boundingClientRect(rect => {
        }).exec();
      });
    },
    handleClick() {
      if (this.clickable) {
        this.$emit('click');
      }
    },
    chooseImage() {
      return new Promise((resolve, reject) => {
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
    async uploadAvatar(filePath) {
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

        this.processAvatarUrl(result.address);

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
.user-avatar {
  position: relative;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f8f8f8;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  margin: 0;
  box-sizing: border-box;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
  padding: 0;
  margin: 0;
  box-sizing: border-box;
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
