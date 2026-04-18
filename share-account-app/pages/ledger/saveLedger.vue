<template>
  <view class="save-ledger-page">
    <!-- 加载中 -->
    <view v-if="isLoading" class="loading-container">
      <uni-load-more :status="'loading'" :content-text="{loading: '加载中'}"></uni-load-more>
    </view>

    <!-- 表单内容 -->
    <view class="form-container" v-else>
      <!-- 账本名称 -->
      <view class="form-item">
        <view class="form-label">账本名称<text class="required">*</text></view>
        <input class="form-input" v-model="formData.name" placeholder="给账本起个名字，比如“家庭账本”、“情侣账本”等" placeholder-style="color:#999999" />
      </view>

      <!-- 账本类型 -->
      <view class="form-item">
        <view class="form-label">账本类型<text class="required">*</text></view>
        <radio-group @change="handleTypeChange">
          <label class="radio-item">
            <radio value="0" :checked="formData.type === '0'" />共享账本
            <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click="showLedgerTip"/>
          </label>
          <label class="radio-item">
            <radio value="1" :checked="formData.type === '1'" />个人账本
            <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click="showPersonLedgerTip"/>
          </label>
        </radio-group>
      </view>

      <!-- 账本属性 -->
      <view class="form-item">
        <view class="form-label">账本属性<text class="required">*</text></view>
        <radio-group @change="handlePropertyChange">
          <label class="radio-item">
            <radio value="0" :checked="formData.property === '0'" />普通账本
            <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click="showNormalLedgerTip"/>
          </label>
          <label class="radio-item">
            <radio value="1" :checked="formData.property === '1'" />AA账本
            <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click="showAALedgerTip"/>
          </label>
        </radio-group>
      </view>
      <!-- 是否默认账本 -->
      <view class="form-item">
        <view class="form-label">默认账本<uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click="showDefaultLedgerTip"/></view>
        <switch :checked="formData.isDefault" @change="formData.isDefault = $event.detail.value" color="#1989fa" />
      </view>
      
      <!-- 备注 -->
        <view class="form-item">
          <view class="form-label">备注</view>
          <textarea class="form-textarea" v-model="formData.memo" placeholder="这个账本记录的是哪些事件/场景/人/事物等" placeholder-style="color:#999999"></textarea>
        </view>
        
        
      <!-- 提交按钮 -->
      <button class="submit-btn" @click="submitForm">{{ isEditMode ? '保存修改' : '创建账本' }}</button>
    </view>
  </view>
</template>

<script>
import request from '@/common/request.js';
import backUrl from '@/common/back_url.js';
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import UniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
import UniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue';
import messageService from '@/common/messageService.js';

export default {
  components: {
    UniIcons,
    UniLoadMore
  },
  data() {
    return {
      isEditMode: false,
      formData: {
        id: '',
        name: '', // 账本名称
        type: '0', // 默认共享账本
        property: '0', // 默认普通账本
        classId: -1, // 默认无分类
        memo: '',
        isDefault: 0 // 是否默认账本
      },
      isLoading: false,
      isSubmitting: false
    };
  },
  onLoad(options) {
    // 判断是否是编辑模式
    if (options && options.id) {
      this.isEditMode = true;
      this.formData.id = options.id;
      this.loadLedgerDetail();
    }
  },
  methods: {
      // 显示账本提示信息
      showLedgerTip() {
        uni.showModal({
          title: '1、情侣/家庭/班级/公司/婚礼/聚餐等各种需要记录收支的场景都可以建一个账本\n\n2、账本可以分享给其他人，ta就可以和你共同维护这个账本啦~',
          icon: 'none',
          duration: 3000
        });
      },

      showPersonLedgerTip() {
        uni.showModal({
          title: '可以建一个个人账本，记录自己的收支情况',
          icon: 'none',
          duration: 3000
        });
      },

      showNormalLedgerTip() {
        uni.showModal({
          title: '记录普通的收支情况',
          icon: 'none',
          duration: 3000
        });
      },
      
      showAALedgerTip() {
        uni.showModal({
          title: 'AA账本暂未开发，敬请期待',
          icon: 'none',
          duration: 3000
        });
      },
      
      showDefaultLedgerTip() {
        uni.showModal({
          title: '默认账本只能有一个，记账时会默认选择这个账本',
          icon: 'none',
          duration: 3000
        });
      },
      
      // 处理账本类型选择变化
      handleTypeChange(e) {
        this.formData.type = e.detail.value;
      },
      
      // 处理账本属性选择变化
      handlePropertyChange(e) {
        this.formData.property = e.detail.value;
      },
      
      // 加载账本详情（编辑模式下调用）
      loadLedgerDetail() {
      this.isLoading = true;
      
      request({
        url: backUrl.endpoints.ledger_getById + this.formData.id,
        method: 'GET'
      }).then(res => {
        this.isLoading = false;
        if (res && typeof res === 'object') {
          this.formData.name = res.name || '';
          this.formData.type = res.type.toString();
          this.formData.property = res.property.toString();
          this.formData.classId = res.classId;
          this.formData.memo = res.memo || '';
          this.formData.isDefault = res.isDefault || 0;
        } else {
          uni.showToast({
            title: '加载账本详情失败',
            icon: 'none'
          });
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        }
      }).catch(err => {
        this.isLoading = false;
        console.error('加载账本详情失败:', err);
        uni.showToast({
          title: '加载账本详情失败',
          icon: 'none'
        });
        setTimeout(() => {
          uni.navigateBack();
        }, 1500);
      });
    },
    
    // 提交表单（创建或更新）- 必须标记为async以支持await关键字
    async submitForm() {
      if (this.isSubmitting) {
        return;
      }
      
      // 验证必填字段
      if (!this.formData.name) {
        uni.showToast({
          title: '请输入账本名称',
          icon: 'none'
        });
        return;
      }
      
      if (!this.formData.type) {
        uni.showToast({
          title: '请选择账本类型',
          icon: 'none'
        });
        return;
      }
      
      if (!this.formData.property) {
        uni.showToast({
          title: '请选择账本属性',
          icon: 'none'
        });
        return;
      }

      // 直接请求消息授权（在用户点击的直接上下文中）
      try {
        await messageService.manualRequestAuthorization();
      } catch (error) {
        // 授权失败不处理
      }
      
      this.isSubmitting = true;
      
      // 创建一个新的提交数据对象，确保isDefault是数字类型
      const submitData = {
        ...this.formData,
        // 将布尔值转换为数字类型（1表示true，0表示false）
        isDefault: this.formData.isDefault ? 1 : 0
      };
      
      // 修改为使用async/await简化代码并确保在用户点击上下文中执行
      try {
        const response = await request({
          url: backUrl.endpoints.ledger_save,
          method: 'POST',
          data: submitData,
          customOptions: {
            returnFullResponse: true
          }
        });
        
        this.isSubmitting = false;
        
        if (response) {
          uni.showToast({
            title: this.isEditMode ? '保存成功' : '创建成功',
            icon: 'success'
          });
          
          // 延迟返回上一页，让用户看到成功提示
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } else {
          uni.showToast({
            title: (response && response.data && response.data.message) || (this.isEditMode ? '保存失败' : '创建失败'),
            icon: 'none'
          });
        }
      } catch (err) {
        this.isSubmitting = false;
        console.error(this.isEditMode ? '保存账本失败:' : '创建账本失败:', err);
        uni.showToast({
          title: this.isEditMode ? '保存失败，请重试' : '创建失败，请重试',
          icon: 'none'
        });
      }
    }
  }
};
</script>

<style scoped>
.save-ledger-page {
  padding-bottom: 40rpx;
  background-color: #f8f8f8;
  min-height: 100vh;
}

.page-header {
  height: 90rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  background-color: #ffffff;
  border-bottom: 1rpx solid #eeeeee;
}

.header-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
}

.header-right {
  width: 60rpx;
}

.loading-container {
  padding: 60rpx 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.form-container {
  padding: 30rpx;
}

.form-item {
  margin-bottom: 30rpx;
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 30rpx;
}

.form-label {
  font-size: 28rpx;
  color: #333333;
  margin-bottom: 20rpx;
}

.required {
  color: #ff4d4f;
  margin-left: 4rpx;
}

.radio-group {
  display: flex;
  gap: 40rpx;
}

.radio-item {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  color: #666666;
}

.radio-item radio {
  margin-right: 10rpx;
  transform: scale(0.8);
}

.info-icon {
  margin-left: 10rpx;
  font-size: 24rpx;
  color: #1989fa;
  border-radius: 50%;
  width: 40rpx;
  height: 40rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.form-input {
  width: 100%;
  height: 80rpx;
  border: 1rpx solid #eeeeee;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #333333;
  background-color: #ffffff;
  box-sizing: border-box;
}

.form-select {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 80rpx;
  border: 1rpx solid #eeeeee;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #666666;
}

.form-textarea {
  width: 100%;
  height: 200rpx;
  border: 1rpx solid #eeeeee;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 28rpx;
  color: #333333;
  background-color: #ffffff;
  box-sizing: border-box;
}

.submit-btn {
    width: 100%;
    height: 90rpx;
    line-height: 90rpx;
    font-size: 32rpx;
    background-color: #1989fa;
    color: #ffffff;
    border-radius: 45rpx;
    margin-top: 40rpx;
  }
</style>