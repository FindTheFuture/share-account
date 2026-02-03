<template>
  <view class="account-edit-page">
    <!-- 表单内容 -->
    <view class="form-container">
      <view class="form-title">钱从哪个账户进出？例如各种银行卡、支付宝、微信、花呗、信用卡等账户</view>
      <!-- 账户名称 -->
      <view class="form-item">
        <text class="form-label">账户名称 <text class="required">*</text></text>
        <view class="form-control">
          <input 
            type="text" 
            v-model="formData.name" 
            placeholder="给账户起个名字吧" 
            maxlength="100"
          />
        </view>
      </view>
      
      <!-- 账户类型 -->
      <view class="form-item select-container">
        <text class="form-label">账户类型 <text class="required">*</text></text>
        <view class="form-control">
          <uni-data-select 
            v-model="formData.type" 
            :localdata="accountTypes" 
            placeholder="请选择" 
            @change="handleTypeChange" 
            class="custom-select"
            popup-class="custom-popup"
          ></uni-data-select>
        </view>
      </view>
      
      <!-- 初始余额 -->
      <view class="form-item">
        <text class="form-label">初始余额<text class="unit">（元）</text> <text class="required">*</text></text>
        <view class="form-control input-with-prefix">
          <text class="currency-prefix">¥</text>
          <input 
            type="number" 
            v-model="balance" 
            placeholder="0.00" 
            @input="handleBalanceInput" 
            step="0.01" 
            min="0"
            class="balance-input"
          />
          
        </view>
      </view>
      
      <!-- 是否计入预算 - 使用单选模式 -->
      <view class="form-item radio-group">
        <view class="form-label">
          <text>计入预算</text>
          <text class="required">*</text>
          <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click.stop="onBudgetTipClick" />
        </view>
        <view class="form-control radio-options">
          <view 
            class="radio-option" 
            v-for="option in budgetOptions" 
            :key="option.value" 
            @click="formData.isBudget = option.value"
            :class="{ 'radio-selected': formData.isBudget === option.value }"
          >
            <view class="radio-circle">
              <view class="radio-dot" v-if="formData.isBudget === option.value"></view>
            </view>
            <text class="radio-text">{{ option.text }}</text>
          </view>
        </view>
      </view>
      
      <!-- 是否计入总资产 - 使用单选模式 -->
      <view class="form-item radio-group">
        <view class="form-label">
          <text>计入总资产</text>
          <text class="required">*</text>
          <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click.stop="onTotalMoneyTipClick" />
        </view>
        <view class="form-control radio-options">
          <view 
            class="radio-option" 
            v-for="option in totalMoneyOptions" 
            :key="option.value" 
            @click="formData.isTotalMoney = option.value"
            :class="{ 'radio-selected': formData.isTotalMoney === option.value }"
          >
            <view class="radio-circle">
              <view class="radio-dot" v-if="formData.isTotalMoney === option.value"></view>
            </view>
            <text class="radio-text">{{ option.text }}</text>
          </view>
        </view>
      </view>

      <!-- 是否默认账户 - 使用单选模式 -->
      <view class="form-item radio-group">
        <view class="form-label">
          <text>默认账户</text>
          <text class="required">*</text>
          <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click.stop="onDefaultTipClick" />
        </view>
        <view class="form-control radio-options">
          <view 
            class="radio-option" 
            v-for="option in defaultOptions" 
            :key="option.value" 
            @click="formData.isDefault = option.value"
            :class="{ 'radio-selected': formData.isDefault === option.value }"
          >
            <view class="radio-circle">
              <view class="radio-dot" v-if="formData.isDefault === option.value"></view>
            </view>
            <text class="radio-text">{{ option.text }}</text>
          </view>
        </view>
      </view>
      
      <!-- 备注 -->
      <view class="form-item">
        <text class="form-label">备注</text>
        <view class="form-control">
          <textarea 
            v-model="formData.memo" 
            placeholder="描述下这个账户的作用"
            rows="3"
            class="memo-textarea"
          ></textarea>
          <view class="char-count">
            <text>{{ formData.memo.length }}/200</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部操作按钮 -->
    <view class="bottom-actions">
      <button class="btn cancel" @click="navigateBack">取消</button>
      <button class="btn confirm" @click="submitForm">保存</button>
    </view>
  </view>
</template>

<script>
export default {
  name: 'AccountEdit',
  components: {},
  data() {
    return {
      isEdit: false,
      accountId: null,
      
      formData: {
        name: '',
        type: null,
        isBudget: 0,  // 0:是, 1:否
        isTotalMoney: 0,  // 0:是, 1:否
        isDefault: 0, // 0:否, 1:是（默认：否）
        balance: 0,
        status: 0,  // 0:正常, 1:停用
        memo: ''
      },
      
      balance: '',
      
      accountTypes: [
        { value: 0, text: '储蓄账户' },
        { value: 1, text: '信贷账户' },
        { value: 2, text: '充值账户' },
        { value: 3, text: '投资理财' }
      ],
      
      budgetOptions: [
        { value: 0, text: '是' },
        { value: 1, text: '否' }
      ],
      
      totalMoneyOptions: [
        { value: 0, text: '是' },
        { value: 1, text: '否' }
      ],
      // 是否默认账户选项
      defaultOptions: [
        { value: 0, text: '否' },
        { value: 1, text: '是' }
      ],
      
      statusOptions: [
        { value: 0, text: '正常' },
        { value: 1, text: '停用' }
      ]
    };
  },
  
  onLoad(options) {
    if (options && options.id) {
      this.isEdit = true;
      this.accountId = options.id;
      uni.setNavigationBarTitle({ title: '编辑账户' });
      this.loadAccountDetail();
    } else {
      uni.setNavigationBarTitle({ title: '新增账户' });
    }
  },
  
  methods: {
    // 点击计入预算提示图标显示提示信息
    onBudgetTipClick() {
      uni.showModal({
        title: '1、计入预算后，该账户的收支会被计入总预算\n      2、不计入预算后，该账户的收支不会被计入总预算',
        icon: 'none',
        duration: 3000
      });
    },

    // 点击计入总资产提示图标显示提示信息
    onTotalMoneyTipClick() {
      uni.showModal({
        title: '1、计入总资产后，该账户的收支会被计入总资产\n      2、不计入总资产后，该账户的收支不会被计入总资产',
        icon: 'none',
        duration: 3000
      });
    },

    // 点击默认账户提示图标显示提示信息
    onDefaultTipClick() {
      uni.showModal({
        title: '默认账户只能有一个，记账时会默认选择这个账户',
        icon: 'none',
        duration: 3000
      });
    },

    async loadAccountDetail() {
      if (!this.accountId) return;
      
      try {
        uni.showLoading({ title: '加载中...' });
        const response = await this.$request({
          url: `${this.$backUrlConfig.endpoints.account_getById}${this.accountId}`,
          method: 'GET'
        });
        
        if (response) {
          const account = response;
          this.formData = {
            id: account.id,
            name: account.name || '',
            type: account.type,
            isBudget: Number(account.isBudget),
            isTotalMoney: Number(account.isTotalMoney),
            isDefault: Number(account.isDefault),
            balance: account.balance,
            status: Number(account.status),
            memo: account.memo || ''
          };
          this.balance = (account.balance / 100).toFixed(2);
        } else {
          uni.showToast({
            title: response?.message || '加载失败',
            icon: 'none'
          });
          this.navigateBack();
        }
      } catch (error) {
        console.error('加载账户详情失败:', error);
        uni.showToast({ title: '网络异常', icon: 'none' });
        this.navigateBack();
      } finally {
        uni.hideLoading();
      }
    },
    
    handleTypeChange(value) {
      this.formData.type = value;
    },
    
    handleBalanceInput(e) {
      const value = e.detail.value;
      this.balance = value;
      this.formData.balance = value ? Math.round(parseFloat(value) * 100) : 0;
    },
    
    async submitForm() {
      // 表单验证
      if (!this.formData.name || !this.formData.name.trim()) {
        return uni.showToast({ title: '请输入账户名称', icon: 'none' });
      }
      if (this.formData.name.trim().length > 100) {
        return uni.showToast({ title: '账户名称最多100字符', icon: 'none' });
      }

      if (this.formData.type === null || this.formData.type === undefined) {
        return uni.showToast({ title: '请选择账户类型', icon: 'none' });
      }
      
      if (!this.balance || isNaN(parseFloat(this.balance)) || parseFloat(this.balance) < 0) {
        return uni.showToast({ title: '请输入有效余额', icon: 'none' });
      }
      
      try {
        uni.showLoading({ title: '提交中...' });
        
        const response = await this.$request({
          url: this.$backUrlConfig.endpoints.account_save,
          method: 'POST',
          data: this.formData
        });
        
        if (response) {
          uni.showToast({ title: this.isEdit ? '更新成功' : '创建成功', icon: 'success' });
          this.navigateBack();
        } else {
          uni.showToast({ title: response.message || (this.isEdit ? '更新失败' : '创建失败'), icon: 'none' });
        }
      } catch (error) {
        console.error('提交表单失败:', error);
        uni.showToast({ title: '网络异常', icon: 'none' });
      } finally {
        uni.hideLoading();
      }
    },
    
    navigateBack() {
      uni.navigateBack({ delta: 1 });
    }
  }
};
</script>

<style scoped>
/* 页面整体样式 */
.account-edit-page {
  background-color: #f8f8f8;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 页面头部 */
.page-header {
  background-color: #ffffff;
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 20rpx;
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333333;
}

/* 表单容器 */
.form-container {
  flex: 1;
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

/* 表单项目 */
.form-item {
  margin-bottom: 40rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

/* 表单标签 */
.form-label {
  font-size: 28rpx;
  color: #333333;
  margin-bottom: 16rpx;
  display: block;
  font-weight: 500;
}

.required {
  color: #ff4d4f;
}

/* 表单控件容器 */
.form-control {
  display: flex;
  align-items: center;
}

/* 带前缀的输入框 */
.input-with-prefix {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.currency-prefix {
  position: absolute;
  left: 24rpx;
  font-size: 28rpx;
  color: #666666;
  z-index: 1;
}

.balance-input {
  padding-left: 50rpx !important;
}

/* 输入框样式 */
input {
  flex: 1;
  height: 88rpx;
  border: 1rpx solid #d9d9d9;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #333333;
  transition: border-color 0.3s;
  background-color: #ffffff;
}

/* 余额输入框特殊样式 */
.balance-input {
  font-size: 36rpx !important;
  font-weight: 500;
  color: #ff7e00 !important;
  padding-left: 50rpx !important;
}

input:focus {
  border-color: #1890ff;
  outline: none;
  box-shadow: 0 0 0 4rpx rgba(24, 144, 255, 0.1);
}

/* 文本域样式 */
.memo-textarea {
  flex: 1;
  min-height: 160rpx;
  border: 1rpx solid #d9d9d9;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 28rpx;
  color: #333333;
  resize: none;
  transition: border-color 0.3s;
}

.memo-textarea:focus {
  border-color: #1890ff;
  outline: none;
  box-shadow: 0 0 0 4rpx rgba(24, 144, 255, 0.1);
}

/* 字数统计 */
.char-count {
  position: absolute;
  right: 24rpx;
  bottom: 16rpx;
  font-size: 24rpx;
  color: #999999;
}

/* 单位样式 */
.unit {
  margin-left: 16rpx;
  font-size: 28rpx;
  color: #666666;
}

/* 单选按钮组样式 */
.radio-group .form-control {
  flex-direction: row;
  gap: 60rpx;
}

.radio-options {
  position: relative;
}

.radio-option {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  padding: 10rpx 0;
}

.radio-option.radio-selected .radio-text {
  color: #1890ff;
}

.radio-circle {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #d9d9d9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12rpx;
  transition: border-color 0.3s;
}

.radio-option.radio-selected .radio-circle {
  border-color: #1890ff;
}

.radio-dot {
  width: 24rpx;
  height: 24rpx;
  background-color: #1890ff;
  border-radius: 50%;
  transform: scale(0);
  transition: transform 0.3s;
}

.radio-option.radio-selected .radio-dot {
  transform: scale(1);
}

.radio-text {
  font-size: 28rpx;
  color: #333333;
  transition: color 0.3s;
}

/* 自定义选择器样式 */
.custom-select {
  width: 100%;
  height: 88rpx;
  border: none;  /* 移除外层边框 */
  background-color: transparent;
  display: flex;
  justify-content: center;
}

/* 美化下拉框内部样式 */
:deep(.uni-data-select) .uni-select {
  width: 100%;
  height: 100%;
  border-radius: 16rpx;
  display: flex;
  justify-content: center;
}

:deep(.uni-data-select) .uni-select-input {
  height: 88rpx !important;
  font-size: 28rpx;
  color: #333333;
  padding: 0 24rpx;
  border: 1rpx solid #d9d9d9;  /* 只保留文本输入区域的边框 */
  border-radius: 16rpx;
  background-color: #ffffff;
  text-align: center;
  width: 100%;
}

:deep(.uni-data-select) .uni-select-placeholder {
  color: #999999;
  font-size: 28rpx;
}

:deep(.uni-data-select) .uni-select-icon {
  right: 24rpx;
  z-index: 10;
}

:deep(.uni-data-select) .uni-select-icon-text {
  color: #999999;
  font-size: 24rpx;
}

.custom-popup {
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.15);
}

/* 美化选项样式 */
:deep(.uni-picker__content) .uni-picker__item {
  height: 80rpx !important;
  line-height: 80rpx !important;
  font-size: 30rpx !important;
}

:deep(.uni-picker__content) .uni-picker__item--selected {
  color: #1890ff !important;
  font-size: 32rpx !important;
  font-weight: 500;
}

/* 美化分隔线 */
:deep(.uni-picker__mask) {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0) 100%), linear-gradient(0deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0) 100%);
}

/* 底部按钮区域 */
.bottom-actions {
  margin-top: 30rpx;
  padding: 0 30rpx 60rpx;
  display: flex;
  gap: 24rpx;
}

.btn {
  flex: 1;
  height: 96rpx;
  font-size: 32rpx;
  border-radius: 48rpx;
  line-height: 96rpx;
  transition: all 0.3s;
  border: none;
  outline: none;
  font-weight: 500;
}

.btn.cancel {
  background-color: #ffffff;
  color: #666666;
  border: 1rpx solid #d9d9d9;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.btn.cancel:active {
  background-color: #f5f5f5;
}

.btn.confirm {
  background-color: #1890ff;
  color: #ffffff;
  box-shadow: 0 4rpx 16rpx rgba(24, 144, 255, 0.3);
}

.btn.confirm:active {
  background-color: #40a9ff;
}

/* 选择器容器 */
.select-container .form-control {
  position: relative;
  display: flex;
  justify-content: center;
}
</style>
    