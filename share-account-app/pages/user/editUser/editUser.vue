<template>
  <view class="user-edit-page">
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载用户信息..."></uni-load-more>
    </view>
    
    <!-- 内容区域 -->
    <view class="content-container" v-else>
      <!-- 顶部用户头像 -->
      <view class="avatar-section">
        <user-avatar
          ref="avatarComponent"
          :avatar-url="userInfo.pictureAddress"
          :user-id="haoe"
          :uploadable="true"
          :show-edit-mask="true"
          @click="onAvatarClick"
          @upload-success="onAvatarUploadSuccess"
          @upload-error="onAvatarUploadError"
          size="200rpx"
        ></user-avatar>
      </view>
      
      <!-- 表单区域 -->
      <view class="form-container">
        <!-- 用户ID -->
        <view class="form-item">
          <text class="item-label">用户ID</text>
          <text class="item-value">#{{ userInfo.haoe || '未知' }}</text>
        </view>
        
        <!-- 昵称 -->
        <view class="form-item">
          <text class="item-label">昵称</text>
          <uni-easyinput
            v-model="formData.nickName"
            placeholder="请输入昵称"
            maxlength="100"
            class="item-input"
          ></uni-easyinput>
        </view>
        
        <!-- 手机号 -->
        <view class="form-item">
          <text class="item-label">手机号</text>
          <uni-easyinput
            v-model="formData.phone"
            placeholder="请输入手机号"
            type="digit"
            maxlength="11"
            class="item-input"
            @blur="validatePhone"
          ></uni-easyinput>
        </view>
        
        
        <!-- 性别 -->
        <view class="form-item" @click="showSexPicker">
          <text class="item-label">性别</text>
          <view class="item-value">
            <text>{{ getSexName() || '请选择性别' }}</text>
            <text class="iconfont icon-arrow-right"></text>
          </view>
        </view>
        
        <!-- 消息通知 -->
        <view class="form-item">
          <text class="item-label">消息通知</text>
          <switch 
            :checked="formData.canSendMessage === 1" 
            @change="toggleMessageNotify"
            class="custom-switch"
            color="#1989fa"
          ></switch>
        </view>
        
        <!-- 记账提醒 -->
        <view class="form-item">
          <view class="item-label">
            <text>日报通知</text>
            <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click="onNotifyTipClick" />
          </view>
          <view class="item-value">
            <text>每日{{formData.notityBill}}点</text>
            <!--
            <uni-data-select
               class="notify-select"
               v-model="formData.notityBill"
               placeholder="请选择提醒时间(0-23点)"
               :localdata="notifyRangeOptions"
               :clear="false"
               :disabled="formData.canSendMessage !== 1"
             ></uni-data-select>
            -->
          </view>
          <!--
          <text class="disabled-tip" v-if="formData.canSendMessage !== 1">
            关闭消息通知后不可设置
          </text>
          -->
        </view>
      </view>
      
      <!-- 保存按钮 -->
      <view class="save-btn-container">
        <button @click="saveUserInfo" :loading="saving">
          保存
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import UserAvatar from '@/components/user-avatar.vue'

export default {
  components: {
    'user-avatar': UserAvatar
  },
  data() {
    return {
      haoe: '', // 用户ID
      loading: true,
      saving: false,
      userInfo: {}, // 用户信息
      formData: {}, // 表单数据
      enums: {}, // 枚举数据
      // notifyRange: Array.from({ length: 24 }, (_, i) => i),
      // notifyRangeOptions: Array.from({ length: 24 }, (_, i) => ({ text: String(i), value: i })),
    }
  },
  onLoad(options) {
    // 获取上游传递的参数
    this.haoe = options.haoe;
    if (this.haoe) {
      this.initPage();
    } else {
      uni.showToast({
        title: '用户ID不能为空',
        icon: 'none'
      });
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    }
  },
  methods: {
    // 返回上一页
    navigateBack() {
      uni.navigateBack();
    },
    
    // 初始化页面数据
    async initPage() {
      try {
        // 并行获取用户信息和枚举数据
        const [userRes, enumRes] = await Promise.all([
          this.$request({
            url: this.$backUrlConfig.endpoints.getUserInfo + this.haoe,
            method: 'GET'
          }),
          this.$request({
            url: this.$backUrlConfig.endpoints.getAllEnums,
            method: 'GET'
          })
        ]);
        
        if (userRes) {
          this.userInfo = userRes;
          // 初始化表单数据，排除不可编辑的字段
          this.formData = {
            nickName: userRes.nickName || '',
            phone: userRes.phone || '',
            sex: userRes.sex || null,
            role: userRes.role || null,
            canSendMessage: userRes.canSendMessage !== null ? userRes.canSendMessage : 0,
            notityBill: userRes.notityBill
          };
          // 头像URL由user-avatar组件内部处理
        } else {
          uni.showToast({
            title: '获取用户信息失败',
            icon: 'none'
          });
        }
        
        if (enumRes) {
          this.enums = enumRes;
        } else {
          uni.showToast({
            title: '获取枚举数据失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('初始化页面异常:', error);
        uni.showToast({
          title: '网络请求异常',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    
    // 头像点击事件
    async onAvatarClick() {
      // 显示上传中提示
      uni.showLoading({
        title: '请选择图片...',
        mask: true
      });
      
      try {
        // 获取user-avatar组件实例
        const avatarComponent = this.$refs.avatarComponent;
        if (avatarComponent) {
          // 使用组件的选择图片方法
          const tempFilePath = await avatarComponent.chooseImage();
          // 使用组件的上传方法
          await avatarComponent.uploadAvatar(tempFilePath);
        }
      } catch (error) {
        console.error('处理头像失败:', error);
      } finally {
        // 隐藏加载提示
        uni.hideLoading();
      }
    },
    
    // 头像上传成功事件
    onAvatarUploadSuccess(data) {
      // 使用this.$set确保Vue能检测到对象属性的变化
      this.$set(this.userInfo, 'pictureAddress', data.url);
      
      // 显示上传成功提示
      uni.showToast({
        title: '头像上传成功',
        icon: 'success'
      });
    },
    
    // 头像上传失败事件
    onAvatarUploadError(error) {
      console.error('头像上传失败:', error);
      // 可以在这里添加额外的错误处理逻辑
    },
    
    // 显示角色选择器
    showRolePicker() {
      // 已禁用角色选择，保留占位以防调用
      uni.showToast({
        title: '角色不可编辑',
        icon: 'none'
      });
    },
    
    // 显示性别选择器
    showSexPicker() {
      if (!this.enums?.sexEnum || this.enums.sexEnum.length === 0) {
        uni.showToast({
          title: '性别数据未加载完成',
          icon: 'none'
        });
        return;
      }
      
      const sexNames = this.enums.sexEnum.map(item => item.description);
      
      uni.showActionSheet({
        itemList: sexNames,
        success: (res) => {
          const selectedSex = this.enums.sexEnum[res.tapIndex];
          this.formData.sex = selectedSex.id;
        }
      });
    },
    
    // 获取角色名称
    getRoleName() {
      if (this.formData.role === null || this.formData.role === undefined || !this.enums?.roleEnum) return '';
      const role = this.enums.roleEnum.find(item => item.id === this.formData.role);
      return role?.description || '';
    },
    
    // 获取性别名称
    getSexName() {
      if (this.formData.sex === null || this.formData.sex === undefined || !this.enums?.sexEnum) return '';
      const sex = this.enums.sexEnum.find(item => item.id === this.formData.sex);
      return sex?.description || '';
    },
    
    // 切换消息通知状态
    toggleMessageNotify(e) {
      this.formData.canSendMessage = e.detail.value ? 1 : 0;
      if (!e.detail.value) {
        // this.formData.notityBill = ''; // 关闭通知时清空提醒时间
      }
    },
    
    // 验证手机号
    validatePhone() {
      const phone = this.formData.phone;
      if (phone && !/^1[3-9]\d{9}$/.test(phone)) {
        uni.showToast({
          title: '请输入正确的手机号',
          icon: 'none'
        });
      }
    },
    
    // 验证通知时间
    validateNotifyTime() {
      const time = this.formData.notityBill;
      if (time !== '' && (isNaN(time) || time < 0 || time > 23 || !Number.isInteger(Number(time)))) {
        uni.showToast({
          title: '请输入0-23之间的整数',
          icon: 'none'
        });
        this.formData.notityBill = '';
      }
    },
    
    // 保存用户信息
    async saveUserInfo() {
      // 表单验证
      if (!this.formData.nickName) {
        uni.showToast({
          title: '请输入昵称',
          icon: 'none'
        });
        return;
      }
      
      if (!this.formData.phone) {
        uni.showToast({
          title: '请输入手机号',
          icon: 'none'
        });
        return;
      }
      
      if (!/^1[3-9]\d{9}$/.test(this.formData.phone)) {
        uni.showToast({
          title: '请输入正确的手机号',
          icon: 'none'
        });
        return;
      }
      
      if (this.formData.sex === null || this.formData.sex === undefined) {
        uni.showToast({
          title: '请选择性别',
          icon: 'none'
        });
        return;
      }
      
      // 角色不再强制选择，按后端当前值只读保存
      // if (this.formData.role === null || this.formData.role === undefined) {
      //   uni.showToast({
      //     title: '请选择角色',
      //     icon: 'none'
      //   });
      //   return;
      // }
      
      // 记账提醒固定为10点，不再进行该项校验
      // if (this.formData.canSendMessage === 1 && 
      //     (this.formData.notityBill === '' || 
      //      isNaN(this.formData.notityBill) || 
      //      this.formData.notityBill < 0 || 
      //      this.formData.notityBill > 23 || 
      //      !Number.isInteger(Number(this.formData.notityBill)))) {
      //   uni.showToast({
      //     title: '请输入0-23之间的整数作为记账提醒时间',
      //     icon: 'none'
      //   });
      //   return;
      // }
      
      // 构建请求数据
      const requestData = {
        id: this.haoe,
        nickName: this.formData.nickName,
        phone: this.formData.phone,
        sex: this.formData.sex,
        role: this.formData.role,
        canSendMessage: this.formData.canSendMessage,
        notityBill: this.formData.notityBill || null
      };
      
      this.saving = true;
      
      try {
        const res = await this.$request({
          url: this.$backUrlConfig.endpoints.saveUser,
          method: 'POST',
          data: requestData
        });
        
        if (res) {
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          });
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } else {
          uni.showToast({
            title: '保存失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('保存用户信息异常:', error);
        uni.showToast({
          title: '网络请求异常',
          icon: 'none'
        });
      } finally {
        this.saving = false;
      }
    },
    // onNotifyChange(e) {
    //   const idx = e.detail.value;
    //   this.formData.notityBill = this.notifyRange[idx];
    // }
    onNotifyTipClick() {
      uni.showModal({
        title: '昨日收支日报',
        icon: 'none'
      });
    }
  }
}
</script>

<style lang="scss">
/* 样式部分 */
.user-edit-page {
  background-color: #f8f8f8;
  min-height: 100vh;
}

.loading-container {
  padding-top: 300rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.content-container {
  padding-bottom: 120rpx;
}

/* 头像区域 */
.avatar-section {
  background-color: #ffffff;
  padding: 40rpx 0;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

/* 表单区域 */
.form-container {
  background-color: #ffffff;
  border-radius: 12rpx;
  margin: 0 20rpx 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  overflow: visible;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 28rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  position: relative;
  transition: all 0.3s;
  
  &:active {
    background-color: #fafafa;
  }
  
  &:last-child {
    border-bottom: none;
  }
}

.item-label {
  width: 180rpx;
  font-size: 28rpx;
  color: #666666;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.notify-tip-icon {
  margin-left: 8rpx;
}

.item-value {
  flex: 1;
  font-size: 28rpx;
  color: #333333;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 10rpx;
}

.item-input {
  flex: 1;
  font-size: 28rpx;
  color: #333333;
  height: 72rpx;
  background-color: #f8f8f8;
  border-radius: 16rpx;
  border: none;
  padding: 0 20rpx;
  transition: all 0.3s;
  
  &:focus {
    background-color: #ffffff;
    border: 1rpx solid #1989fa;
    box-shadow: 0 0 0 2rpx rgba(25, 137, 250, 0.1);
  }
  
  &[disabled] {
    background-color: #f5f5f5;
    color: #999999;
  }
}

.notify-select {
  width: 100%;
}

/* 提升下拉面板层级，避免被底部按钮遮挡 */
.uni-select__selector {
  z-index: 9999;
}
.uni-select--mask {
  z-index: 9998;
}

.disabled-tip {
  position: absolute;
  right: 30rpx;
  bottom: 10rpx;
  font-size: 22rpx;
  color: #999999;
}

/* 开关样式 */
.custom-switch1 {
  transform: scale(1.2);
  
  & .wx-switch-input {
    border-radius: 20rpx !important;
  }
  
  & .wx-switch-input::before {
    border-radius: 20rpx !important;
  }
}

/* 保存按钮 */
.save-btn-container {
  padding: 40rpx 20rpx;
  
  button {
    width: 100%;
    height: 90rpx;
    line-height: 90rpx;
    border-radius: 45rpx;
    font-size: 32rpx;
    font-weight: 500;
    background-color: #1989fa;
    color: #ffffff;
    border: none;
    transition: all 0.3s;
    
    &:active {
      background-color: #40a9ff;
    }
    
    &[loading] {
      opacity: 0.8;
    }
  }
}

.avatar-section {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx 0;
  width: 100%;
  height: 280rpx;
  position: relative;
  background-color: #FFFFFF;
  margin-bottom: 20rpx;
}

/* 适配不同尺寸的手机 */
@media screen and (max-width: 320px) {
  .item-label {
    width: 150rpx;
    font-size: 26rpx;
  }
}
</style>
