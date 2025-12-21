<template>
  <uni-popup ref="popup" type="center" :mask-click="true" @maskClick="close">
    <view class="popup-container">
      <view class="popup-header">
        <view class="title-with-icon">
          <text class="popup-title">选择账户</text>
          <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click.stop="onAccountTipClick" />
        </view>
        <view class="popup-actions">
          <custom-icon type="bianji" :size="20" color="#007AFF" @tap="navigateToAccountList" style="margin-right: 20rpx;"></custom-icon>
          <custom-icon type="guanbi" :size="20" color="#999" @tap="close"></custom-icon>
        </view>
      </view>
      <view class="popup-content">
        <div v-if="accountList.length > 0">
          <view class="account-list">
            <view
              v-for="account in accountList"
              :key="account.id"
              :class="['account-item', { 'selected': selectedAccount && selectedAccount.id === account.id }]"
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
            <view class="no-select-account" @tap="clearSelection">
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
/* 直接转移自 pages/bill/addBill.vue 的账户弹窗相关样式 */
.popup-container {
  width: 680rpx;
  background-color: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.15);
}
.popup-header {
  padding: 30rpx 40rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
  background-color: #FAFAFA;
}
.popup-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}
.title-with-icon {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.notify-tip-icon {
  margin-left: 8rpx;
}
.popup-content {
  max-height: 640rpx;
  overflow-y: auto;
  padding: 30rpx 40rpx;
}
.account-list {
  padding: 20rpx;
}
.account-item {
  padding: 25rpx;
  border-bottom: 1px solid #f0f0f0;
}
.account-item:active {
  background-color: #e0e0e0;
}
.account-item.selected {
  background-color: #e8fde8;
}
.account-info {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 15rpx;
}
.account-name-title {
  font-size: 32rpx;
  color: #1f2937; /* 深色标题 */
  font-weight: 600;
  max-width: 420rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.account-info-second-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 15rpx;
  margin-top: 10rpx;
  padding-left: 0;
}
.no-select-account {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 0;
  margin-top: 10rpx;
}
.no-select-account-text {
  color: #007AFF;
  text-decoration: underline;
  font-size: 28rpx;
}
.account-balance {
  font-size: 24rpx;
  color: #666;
  margin-left: auto;
}
.account-type-tag {
  font-size: 24rpx;
  color: #007AFF;
  padding: 5rpx 15rpx;
  border-radius: 15rpx;
}
.account-budget-tag {
  font-size: 24rpx;
  color: #FF9500;
  padding: 0 15rpx;
}
.account-total-tag {
  font-size: 24rpx;
  color: #34C759;
  padding: 0 15rpx;
}
.popup-actions {
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.default-badge {
  font-size: 24rpx;
  color: #007AFF;
  background: none;
  padding: 0;
}
</style>