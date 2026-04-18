<template>
  <view class="account-page">
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载账户信息..."></uni-load-more>
    </view>

    <!-- 错误提示 -->
    <view class="error-container" v-if="errorMsg">
      <text class="error-text">{{ errorMsg }}</text>
      <button class="retry-btn" @click="loadAccountList">重试</button>
    </view>

    <!-- 账户列表 -->
    <view class="account-list" v-if="!loading && !errorMsg">
      <view class="form-title">钱从哪个账户进出？例如各种银行卡、支付宝、微信、花呗、信用卡等账户</view>
      <view v-if="accountList.length > 0">
        <view
          class="account-item"
          v-for="(account, index) in accountList"
          :key="account.id"
          :class="{ 'item-disabled': account.status === 1 }"
        >
          <view class="item-info" @click="showAccountDetail(account)">
            <view class="item-main-info">
              <text class="item-title">{{ getAccountTitle(account) }}</text>
            </view>
            <view class="item-details">
              <text class="item-type" :class="getTypeClass(account.type)">{{ account.typeName }}</text>
              <text v-if="account.isDefault === 1" class="item-default default-yes">默认</text>
            </view>
            <view class="item-attributes" v-if="hasAttributes(account)">
              <text class="attr-item budget-tag" v-if="account.isBudgetName">{{ account.isBudgetName }}</text>
              <text class="attr-item total-tag" v-if="account.isTotalMoneyName">{{ account.isTotalMoneyName }}</text>
            </view>
            <view class="item-details memo-row" v-if="hasMemo(account.memo)">
              <rich-text class="item-memo" :nodes="formatMemo(account.memo)"></rich-text>
            </view>
          </view>
          <view class="item-actions">
            <view class="action-icon" @click.stop="navigateToEditAccount(account)" title="编辑">
              <custom-icon type="bianji" :size="21" color="#007aff"></custom-icon>
            </view>
            <view class="action-icon" @click.stop="handleStatusChange(account)"
              :title="account.status === 0 ? '停用' : '启用'">
              <custom-icon :type="account.status === 0 ? 'guanbi' : 'qiyong'" :size="28" :color="account.status === 0 ? '#ff4d4f' : '#52c41a'"/>
            </view>
          </view>
        </view>
      </view>
      <view class="no-data" v-else>
        暂无账户，点击右下角按钮添加第一个账户
      </view>
    </view>

    <!-- 悬浮新增按钮 -->
    <view class="floating-add-btn" :class="{ 'disabled': isGuest }" @click="onFloatingAddClick">
      <text class="plus-icon">+</text>
    </view>

    <!-- 确认对话框 -->
    <uni-popup ref="confirmPopup" type="center">
      <view class="confirm-modal">
        <view class="modal-title">{{ confirmTitle }}</view>
        <view class="modal-content">{{ confirmContent }}</view>
        <view class="modal-buttons">
          <button class="modal-btn cancel-btn" @click="closeConfirmPopup">取消</button>
          <button class="modal-btn confirm-btn" @click="confirmAction">{{ confirmButtonText }}</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import request from '@/common/request.js';
import backUrl from '@/common/back_url.js';
import { formatAmount as fmtAmount } from '@/common/util.js';
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import UniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
import UniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue';

export default {
  components: {
    UniIcons,
    UniPopup,
    UniLoadMore
  },
  data() {
    return {
      loading: true,
      errorMsg: '',
      accountList: [],
      isGuest: false,
      // 确认对话框相关
      confirmTitle: '',
      confirmContent: '',
      confirmButtonText: '',
      currentAccount: null,
      confirmActionType: '' // 'delete' 或 'status'
    }
  },
  onLoad() {
    // 设置页面标题
    uni.setNavigationBarTitle({
      title: '账户列表'
    });
    this.loadAccountList();
  },
  onShow() {
    // 更新游客模式标记
    const guestFlag = uni.getStorageSync('isGuest');
    this.isGuest = guestFlag === true || guestFlag === 'true';
    // 从添加/编辑页面返回时刷新列表
    if (!this.loading) {
      this.loadAccountList();
    }
  },
  methods: {
    /**
     * 加载账户列表
     */
    async loadAccountList() {
      this.loading = true;
      this.errorMsg = '';
      
      try {
        const response = await request({
          url: backUrl.endpoints.account_getAll,
          method: 'GET'
        });
        
        if (response && Array.isArray(response)) {
          this.accountList = response;
        } else {
          this.accountList = [];
          this.errorMsg = '获取账户列表失败';
        }
      } catch (error) {
        console.error('加载账户列表失败:', error);
        this.errorMsg = '网络请求异常，请稍后重试';
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 获取账户标题
     */
    getAccountTitle(account) {
      let title = account.name + ' ¥' + this.formatBalance(account.balance);
      // 停用状态的账户在标题后显示(已停用)
      if (account.status === 1) {
        title += '（已停用）';
      }
      return title;
    },
    
    /**
     * 检查是否有属性
     */
    hasAttributes(account) {
      return account.isBudgetName || account.isTotalMoneyName;
    },
    
    /**
     * 格式化余额显示（分→元，去单位）
     */
    formatBalance(balance) {
      const n = Number(balance) || 0;
      return fmtAmount(n / 100);
    },
    
    /**
     * 根据账户类型获取样式类
     */
    getTypeClass(type) {
      switch(type) {
        case 0: return 'type-savings'; // 储蓄账户
        case 1: return 'type-credit';  // 信贷账户
        case 2: return 'type-recharge';// 充值账户
        case 3: return 'type-invest';  // 投资理财
        default: return '';
      }
    },
    
    /**
     * 跳转到添加账户页面
     */
    navigateToAddAccount() {
      uni.navigateTo({
        url: '/pages/account/editAccount'
      });
    },

    // 悬浮新增按钮点击（游客模式拦截）
    onFloatingAddClick() {
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后新增账户',
          showCancel: false,
          success: (res) => {
            if (res.confirm) {
              // 跳转到登录页面
              uni.navigateTo({
                url: '/pages/login/login'
              });
            }
          }
        });
        return;
      }
      this.navigateToAddAccount();
    },

    /**
     * 跳转到编辑账户页面
     */
    navigateToEditAccount(account) {
      // 检查是否为游客模式
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后编辑账户',
          showCancel: false,
          success: (res) => {
            if (res.confirm) {
              // 跳转到登录页面
              uni.navigateTo({
                url: '/pages/login/login'
              });
            }
          }
        });
        return;
      }
      uni.navigateTo({
        url: `/pages/account/editAccount?id=${account.id}`
      });
    },
    
    /**
     * 查看账户详情
     */
    showAccountDetail(account) {
      // 检查是否为游客模式
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后查看账户详情',
          showCancel: false,
          success: (res) => {
            if (res.confirm) {
              // 跳转到登录页面
              uni.navigateTo({
                url: '/pages/login/login'
              });
            }
          }
        });
        return;
      }
      uni.navigateTo({
        url: `/pages/account/accountDetail?id=${account.id}`
      });
    },
    
    /**
     * 备注渲染：支持换行、制表符和多空格
     */
    formatMemo(memo) {
      if (!memo || !String(memo).trim()) {
        return [];
      }
      // 统一字符串并替换制表符为4空格
      const safe = String(memo).replace(/\t/g, '    ');
      const lines = safe.split(/\r?\n/);
      const nodes = [];
      for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        nodes.push({
          name: 'span',
          attrs: { style: 'display:inline; white-space:pre-wrap; word-break:break-word;' },
          children: [{ type: 'text', text: line }]
        });
        if (i < lines.length - 1) {
          nodes.push({ name: 'br' });
        }
      }
      return nodes;
    },

    hasMemo(memo) {
      return !!(memo && String(memo).trim().length > 0);
    },
    
    /**
     * 处理账户状态变更
     */
    handleStatusChange(account) {
      // 检查是否为游客模式
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后修改账户状态',
          showCancel: false,
          success: (res) => {
            if (res.confirm) {
              // 跳转到登录页面
              uni.navigateTo({
                url: '/pages/login/login'
              });
            }
          }
        });
        return;
      }
      this.currentAccount = account;
      this.confirmActionType = 'status';
      this.confirmTitle = account.status === 0 ? '确认停用账户' : '确认启用账户';
      this.confirmContent = account.status === 0 
        ? '停用后该账户将不再参与日常统计' 
        : '';
      this.confirmButtonText = account.status === 0 ? '确认' : '确认';
      
      this.openConfirmPopup();
    },
    
    /**
     * 执行确认操作
     */
    async confirmAction() {
      if (this.confirmActionType === 'status' && this.currentAccount) {
        await this.updateAccountStatus(
          this.currentAccount.id, 
          this.currentAccount.status === 0 ? 1 : 0
        );
      }
      
      this.closeConfirmPopup();
    },
    
    /**
     * 更新账户状态
     */
    async updateAccountStatus(accountId, status) {
      try {
        const response = await request({
          url: backUrl.endpoints.account_updateStatus + accountId + '/' + status,
          method: 'PUT',
          customOptions: {
            returnFullResponse: true
          }
        });
        
        if (response) {
          uni.showToast({
            title: status === 1 ? '账户已停用' : '账户已启用',
            icon: 'success'
          });
          this.loadAccountList();
        } else {
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('更新账户状态失败:', error);
        uni.showToast({
          title: '网络请求异常',
          icon: 'none'
        });
      }
    },
    
    /**
     * 打开确认对话框
     */
    openConfirmPopup() {
      this.$refs.confirmPopup.open();
    },
    
    /**
     * 关闭确认对话框
     */
    closeConfirmPopup() {
      this.$refs.confirmPopup.close();
      this.currentAccount = null;
      this.confirmActionType = '';
    }
  }
}
</script>

<style scoped>
.account-page {
  padding-bottom: 80rpx;
  background-color: #f8f8f8;
  height: 100vh;
}

.page-header {
  height: 90rpx;
  line-height: 90rpx;
  text-align: center;
  background-color: #ffffff;
  border-bottom: 1rpx solid #eeeeee;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
}

.loading-container {
  padding: 40rpx 0;
  text-align: center;
}

.error-container {
  padding: 40rpx;
  text-align: center;
}

.error-text {
  display: block;
  color: #ff4d4f;
  margin-bottom: 20rpx;
}

.retry-btn {
  font-size: 28rpx;
  padding: 0 20rpx;
  background-color: #1989fa;
  color: #ffffff;
}

.account-list {
  padding: 20rpx;
}

.form-title {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  padding: 24rpx 30rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  line-height: 1.6;
  text-align: center;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
}

.account-item {
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.item-disabled {
  opacity: 0.6;
}

.item-info {
  flex: 1;
  max-width: calc(100% - 80rpx);
}

.item-main-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.item-title {
      font-size: 34rpx;
      font-weight: bold;
      color: #0b0b0b;
    }

.item-details {
  display: flex;
  gap: 20rpx;
  margin-bottom: 12rpx;
}

/* 备注行无边框、纯文本样式 */
.memo-row {
  align-items: flex-start;
}
.item-memo {
  font-size: 26rpx;
  color: #666666;
  line-height: 1.6;
  /* 允许文本换行和长词断行 */
  word-break: break-word;
}

/* 保留类型标签样式 */
.item-type {
  font-size: 26rpx;
  color: #666666;
  background-color: #e6f7ff;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
}

.item-default {
  font-size: 26rpx;
  color: #666666;
  background-color: #f0f0f0;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
}
.default-yes {
  background-color: #1989fa;
  color: #ffffff;
}


.item-balance {
      font-size: 26rpx;
      color: #666666;
      background-color: #fff7e6;
      padding: 4rpx 16rpx;
      border-radius: 16rpx;
    }

.type-savings {
  background-color: #4cd964; /* 储蓄账户 - 绿色 */
  color: #ffffff;
}

.type-credit {
  background-color: #ff3b30; /* 信贷账户 - 红色 */
  color: #ffffff;
}

.type-recharge {
  background-color: #ffcc00; /* 充值账户 - 黄色 */
  color: #333333;
}

.type-invest {
  background-color: #5856d6; /* 投资理财 - 紫色 */
  color: #ffffff;
}

.item-attributes {
  display: flex;
  gap: 15rpx;
  margin-top: 12rpx;
}

.attr-item {
      font-size: 24rpx;
      color: #666666;
      padding: 3rpx 12rpx;
      border-radius: 16rpx;
    }
    
    .budget-tag {
      background-color: #e6f7ff;
      color: #1890ff;
    }
    
    .total-tag {
      background-color: #fff7e6;
      color: #fa8c16;
    }

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-left: 20rpx;
}

.action-icon {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
  background-color: #f0f0f0;
}

.floating-add-btn {
  position: fixed;
  right: 40rpx;
  bottom: 40rpx;
  width: 100rpx;
  height: 100rpx;
  background-color: #1989fa;
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(25, 137, 250, 0.4);
}

.floating-add-btn.disabled {
  background-color: #cccccc;
  box-shadow: none;
}

.plus-icon {
  font-size: 60rpx;
  font-weight: bold;
  line-height: 1;
}

.no-data {
  text-align: center;
  padding: 80rpx 0;
  color: #999999;
  font-size: 28rpx;
}

/* 确认对话框样式 */
.confirm-modal {
  width: 600rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 40rpx;
}

.modal-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  text-align: center;
  margin-bottom: 20rpx;
}

.modal-content {
  font-size: 28rpx;
  color: #666666;
  text-align: center;
  margin-bottom: 40rpx;
}

.modal-buttons {
  display: flex;
  justify-content: space-between;
}

.modal-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
  border-radius: 40rpx;
  margin: 0 10rpx;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666666;
}

.confirm-btn {
  background-color: #1989fa;
  color: #ffffff;
}
</style>
    