<template>
  <uni-popup ref="popup" type="center" :mask-click="true" @maskClick="close">
    <view class="popup-container">
      <view class="popup-header">
        <view class="title-with-icon">
          <text class="popup-title">选择账户</text>
          <uni-icons type="info" size="18" color="#999" class="notify-tip-icon icon-hover" @click.stop="onAccountTipClick" />
        </view>
        <view class="popup-actions">
          <custom-icon type="bianji" :size="20" color="#007AFF" @tap="navigateToAccountList" class="icon-hover"></custom-icon>
          <custom-icon type="guanbi" :size="20" color="#999" @tap="close" class="icon-hover"></custom-icon>
        </view>
      </view>
      <view class="popup-content">
        <div v-if="accountList.length > 0">
          <view class="account-list">
            <view
              v-for="(account, index) in accountList"
              :key="account.id"
              :class="['account-item', 'account-card', { 'selected': selectedAccount && selectedAccount.id === account.id }]"
              :style="{ animationDelay: (index * 0.1) + 's' }"
              @tap="handleSelect(account)"
            >
              <view class="account-info">
                <text class="account-name-title">{{ (account.name || '未命名账户') }}</text>
                <text v-if="account.isDefault === 1" class="default-badge">默认</text>
                <text class="account-balance">¥{{ formatAmount(account.balance / 100) }}</text>
              </view>
              <view class="account-info-second-line">
                <text class="account-type-tag" :class="getAccountTypeClass(account.type)">{{ account.typeName }}</text>
                <text class="account-budget-tag">{{ account.isBudgetName }}</text>
                <text class="account-total-tag">{{ account.isTotalMoneyName }}</text>
              </view>
            </view>
            <!-- 不选账户选项 -->
            <view class="no-select-account btn-hover" @tap="clearSelection">
              <text class="no-select-account-text">不选账户</text>
            </view>
          </view>
        </div>
        <view v-else class="no-data-tip">
          暂无账户数据
        </view>
      </view>
    </view>
  </uni-popup>
</template>

<script>
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import request from '@/common/request.js';
import backUrl from '@/common/back_url.js';
import { formatAmount } from '@/common/util.js';

export default {
  name: 'AccountSelectPopup',
  components: { UniIcons },
  props: {
    selectedAccount: {
      type: Object,
      default: null
    },
    autoSelectDefault: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      accountList: [],
      loadingAccounts: false,
      loadedOnce: false,
      loadedInThisOpen: false,
      autoSelectedOnce: false
    };
  },
  watch: {
    accountList(newVal) {
      this.maybeAutoSelectDefault(newVal);
    }
  },
  mounted() {
    // 页面加载时即可查询一次账户并尝试自动选择默认账户
    this.initAutoSelect();
  },
  methods: {
    formatAmount,
    // 对外：初始化加载并尝试自动选择默认账户（不打开弹窗）
    async initAutoSelect() {
      if (this.loadedOnce) return;
      await this.loadAccounts();
      // 自动选择默认账户（父级尚未选择时）
      this.maybeAutoSelectDefault(this.accountList);
    },
    // 打开弹窗
    open() {
      if (this.$refs.popup && this.$refs.popup.open) {
        this.$refs.popup.open();
        // 弹窗打开时，如未加载过则查询一次
        if (!this.loadedInThisOpen) {
          this.loadAccounts().then(() => {
            this.maybeAutoSelectDefault(this.accountList);
          });
        } else {
          this.refreshIfNeeded();
        }
        this.loadedInThisOpen = true;
      }
    },
    // 关闭弹窗
    close() {
      if (this.$refs.popup && this.$refs.popup.close) {
        this.$refs.popup.close();
      }
    },
    // 选择账户并通知父级
    handleSelect(account) {
      this.$emit('select', account);
      this.close();
    },
    // 不选账户
    clearSelection() {
      this.$emit('select', null);
      this.close();
    },
    // 加载账户列表（组件内查询）
    async loadAccounts() {
      if (this.loadingAccounts) return;
      this.loadingAccounts = true;
      try {
        const res = await request({ url: backUrl.endpoints.account_getAll, method: 'GET' });
        this.accountList = Array.isArray(res) ? res.filter(acc => acc.status === 0) : [];
        this.loadedOnce = true;
      } catch (err) {
        console.error('加载账户失败:', err);
        uni.showToast({ title: '加载账户失败', icon: 'none' });
      } finally {
        this.loadingAccounts = false;
      }
    },
    // 返回后刷新（一次性标记）
    async refreshIfNeeded() {
      const shouldRefresh = !!uni.getStorageSync('refreshAccountsOnShow');
      if (shouldRefresh) {
        await this.loadAccounts();
        this.maybeAutoSelectDefault(this.accountList);
        try { uni.removeStorageSync('refreshAccountsOnShow'); } catch (e) {}
      }
    },
    // 跳转到账户列表页面
    navigateToAccountList() {
      try { uni.setStorageSync('refreshAccountsOnShow', true); } catch (e) {}
      uni.navigateTo({ url: '/pages/account/accountList' });
      // 关闭弹窗以便返回后用户重新打开查看最新数据
      this.close();
    },
    // 自动选择默认账户的判断逻辑
    maybeAutoSelectDefault(list) {
      if (!this.autoSelectDefault || this.autoSelectedOnce) return;
      const arr = Array.isArray(list) ? list : this.accountList;
      if (!this.selectedAccount && Array.isArray(arr)) {
        const defaultAccount = arr.find(acc => acc.isDefault === 1);
        if (defaultAccount) {
          this.$emit('select', defaultAccount);
          this.autoSelectedOnce = true;
        }
      }
    },
    // 类型名与样式
    getAccountTypeName(type) {
      switch(type) {
        case 0: return '储蓄账户';
        case 1: return '信贷账户';
        case 2: return '充值账户';
        case 3: return '投资理财';
        default: return '';
      }
    },
    getAccountTypeClass(type) {
      switch(type) {
        case 0: return 'type-savings';
        case 1: return 'type-credit';
        case 2: return 'type-recharge';
        case 3: return 'type-invest';
        default: return '';
      }
    },
    async setSelectedById(accountId) {
      if (!accountId) {
        this.$emit('select', null);
        return;
      }
      if (!this.loadedOnce) {
        await this.loadAccounts();
      }
      const target = this.accountList.find(a => a.id === accountId);
      if (target) {
        this.$emit('select', target);
      }
    },
    onAccountTipClick() {
      uni.showModal({
        title: '钱从哪个账户进出？例如各种银行卡、支付宝、微信、花呗、信用卡等账户',
        icon: 'none',
        duration: 3000
      });
    }
  }
};
</script>

<style scoped>
/* 弹窗整体样式 */
.popup-container {
  width: 720rpx;
  background-color: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.15);
}

/* 从左侧滑入动画 */
@keyframes slideInLeft {
  from {
    transform: translateX(-100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 账户卡片动画类 */
.account-card {
  animation: slideInLeft 0.5s ease-out forwards;
  opacity: 0;
}

/* 动态动画延迟通过模板计算设置 */

/* 弹窗头部样式 */
.popup-header {
  padding: 24rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
  border-bottom: 1rpx solid #f0f0f0;
}

.popup-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.title-with-icon {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.notify-tip-icon {
  margin-left: 0;
}

/* 图标悬浮动画效果 */
.icon-hover {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  padding: 12rpx;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.icon-hover:hover {
  transform: scale(1.2);
  background-color: #f5f5f5;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

/* 弹窗内容样式 */
.popup-content {
  max-height: 680rpx;
  overflow-y: auto;
  padding: 24rpx 32rpx;
}

/* 账户列表样式 */
.account-list {
  padding: 0;
}

/* 账户卡片样式 */
.account-card {
  background-color: #f8f0f8;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

/* 账户卡片悬浮动画效果 */
.account-card:hover {
  transform: scale(1.02);
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.1);
  border-color: #007AFF;
}

.account-card:active {
  background-color: #f0e6f0;
}

.account-card.selected {
  background-color: #e8f4fd;
  border: 2rpx solid #007AFF;
  box-shadow: 0 4rpx 16rpx rgba(0, 122, 255, 0.2);
}

/* 账户信息样式 */
.account-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.account-name-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
  flex: 1;
  margin-right: 16rpx;
}

.default-badge {
  font-size: 24rpx;
  color: #ffffff;
  background-color: #007AFF;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
  margin-right: 16rpx;
}

.account-balance {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
  flex-shrink: 0;
}

/* 账户信息第二行样式 */
.account-info-second-line {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-wrap: wrap;
}

.account-type-tag {
  font-size: 24rpx;
  color: #007AFF;
  padding: 0;
}

.account-budget-tag {
  font-size: 24rpx;
  color: #FF9500;
  padding: 0;
}

.account-total-tag {
  font-size: 24rpx;
  color: #34C759;
  padding: 0;
}

/* 弹窗操作按钮样式 */
.popup-actions {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

/* 不选账户按钮样式 */
.no-select-account {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
  margin-top: 20rpx;
  border: 1rpx dashed #e0e0e0;
  border-radius: 24rpx;
  background-color: #fafafa;
}

.no-select-account-text {
  color: #007AFF;
  font-size: 28rpx;
}

/* 按钮悬浮动画效果 */
.btn-hover {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.btn-hover:hover {
  transform: scale(1.02);
  background-color: #f0f0f0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

/* 无数据提示样式 */
.no-data-tip {
  padding: 48rpx 20rpx;
  text-align: center;
  font-size: 24rpx;
  color: #999999;
}
</style>