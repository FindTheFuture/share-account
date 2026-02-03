<template>
  <view class="add-budget-page">
    <!-- 表单容器 -->
    <view class="form-container">
      <!-- 年份月份信息 -->
      <view class="form-item">
        <text class="form-label">预算周期:</text>
        <view class="form-value">{{ year }}-{{ month || '00' }}</view>
      </view>

      <!-- 总预算金额 -->
      <view class="form-item">
        <text class="form-label">总预算金额:</text>
        <view class="input-wrapper">
          <text class="currency-symbol">¥</text>
          <input
            type="digit"
            v-model="totalBudget"
            placeholder="请输入总预算金额"
            class="amount-input"
            @input="handleAmountInput"
          />
        </view>
      </view>

    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="save-btn" @click="saveBudget">保存</button>
      <button class="cancel-btn" @click="navigateBack">取消</button>
    </view>
  </view>
</template>

<script>
import request from '@/common/request.js';
import { formatNumber } from '@/common/util.js';
import backUrl from '@/common/back_url.js';

export default {
  data() {
    return {
      budgetId: '',
      year: '',
      month: '',
      totalBudget: '',
      memo: ''
    };
  },
  onLoad(options) {
    // 获取页面参数
    this.budgetId = options.budgetId || '';
    this.year = options.year || '';
    this.month = options.month || '';

    // 如果是修改预算，加载预算信息
    if (this.budgetId) {
      this.loadBudgetInfo();
    }
  },
  methods: {
    // 加载预算信息
    loadBudgetInfo() {
      // 使用已有的budget_list接口来获取预算详情
      request({
        url: backUrl.baseUrl + backUrl.endpoints.budget_list,
        method: 'GET',
        data: {
          year: this.year,
          month: this.month
        }
      })
        .then((res) => {
          if (res && typeof res === 'object' && 'totalBalance' in res) {
            this.totalBudget = res.totalBalance.toString();
            this.memo = res.memo || '';
          }
        })
        .catch((error) => {
          uni.showToast({
            title: '加载预算信息失败: ' + (error.message || ''),
            icon: 'none'
          });
          console.error('加载预算信息失败:', error);
        });
    },

    // 处理金额输入
    handleAmountInput(e) {
      // 限制只能输入数字和小数点
      let value = e.detail.value;
      value = value.replace(/[^\d.]/g, ''); // 清除非数字和小数点
      value = value.replace(/\.(?!\d*$)/g, ''); // 只保留第一个小数点
      value = value.replace(/^\./, '0.'); // 确保小数点前有数字
      value = value.replace(/^0+(\d)/, '$1'); // 移除前导零

      this.totalBudget = value;
    },

    // 保存预算
    saveBudget() {
      // 验证输入
      if (!this.totalBudget) {
        uni.showToast({
          title: '请输入总预算金额',
          icon: 'none'
        });
        return;
      }

      const totalBalance = parseFloat(this.totalBudget);
      if (isNaN(totalBalance) || totalBalance < 0) {
        uni.showToast({
          title: '请输入有效的预算金额',
          icon: 'none'
        });
        return;
      }

      // 准备请求数据
      const data = {
        year: this.year,
        month: this.month || null,
        totalBalance: totalBalance,
        memo: this.memo
      };

      if (this.budgetId) {
        data.id = this.budgetId;
      }

      // 发送请求
      request({
        url: this.budgetId ? backUrl.endpoints.budget_update : backUrl.endpoints.budget_create,
        method: 'POST',
        data: data
      })
        .then((res) => {
          uni.showToast({
            title: this.budgetId ? '修改成功' : '添加成功',
            icon: 'success'
          });

          // 返回上一页
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        })
        .catch((error) => {
          uni.showToast({
            title: this.budgetId ? '修改失败: ' : '添加失败: ' + (error.message || ''),
            icon: 'none'
          });
          console.error(this.budgetId ? '修改预算失败:' : '添加预算失败:', error);
        });
    },

    // 返回上一页
    navigateBack() {
      uni.navigateBack();
    }
  }
};
</script>

<style scoped>
.add-budget-page {
  padding: 20rpx;
  box-sizing: border-box;
}

.page-header {
  text-align: center;
  padding: 20rpx 0;
  margin-bottom: 30rpx;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
}

.form-container {
  background-color: white;
  border-radius: 10rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.form-item {
  margin-bottom: 30rpx;
  display: flex;
  align-items: center;
}

.form-label {
  font-size: 28rpx;
  color: #666;
  margin-right: 15rpx;
  min-width: 120rpx;
}

.form-value {
  font-size: 30rpx;
  padding: 15rpx 0;
  border-radius: 5rpx;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: 5rpx;
  padding: 0 15rpx;
}

.currency-symbol {
  font-size: 30rpx;
  color: #666;
  margin-right: 10rpx;
}

.amount-input {
  flex: 1;
  height: 70rpx;
  font-size: 30rpx;
  background-color: transparent;
}

.memo-input {
  width: 100%;
  height: 150rpx;
  font-size: 30rpx;
  padding: 15rpx;
  background-color: #f5f5f5;
  border-radius: 5rpx;
  box-sizing: border-box;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
  padding: 0 20rpx;
}

.save-btn {
  flex: 1;
  background-color: #007aff;
  color: white;
  font-size: 30rpx;
  padding: 20rpx 0;
}

.cancel-btn {
  flex: 1;
  background-color: #f8f8f8;
  color: #666;
  font-size: 30rpx;
  padding: 20rpx 0;
}
</style>