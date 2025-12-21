<template>
  <view class="account-detail-page">
    <!-- 移除导航栏 -->
    
    <!-- 账户信息卡片 -->
    <view class="account-card">
      <view class="account-name">
        <text class="name-label">账户名称</text>
        <text class="name-value">{{ accountNameDisplay }}</text>
      </view>
      <view class="account-type">
        <text class="type-label">账户类型</text>
        <text class="type-value">{{ accountTypeName }}</text>
      </view>
      
      <view class="account-balance">
        <text class="balance-label">当前余额</text>
        <text class="balance-value">{{ formatBalance(account.balance) }}</text>
      </view>
      
      <view class="account-status" :class="{'status-active': account.status === 0, 'status-inactive': account.status === 1}">
        <text class="status-label">账户状态</text>
        <text class="status-value">{{ account.status === 0 ? '正常' : '停用' }}</text>
      </view>
    </view>
    
    <!-- 详细信息列表 - 移除最后更新时间 -->
    <view class="detail-list">
      <view class="detail-item">
        <text class="item-label">计入预算</text>
        <text class="item-value">{{ account.isBudget === 0 ? '是' : '否' }}</text>
      </view>
      
      <view class="detail-item">
        <text class="item-label">计入总资产</text>
        <text class="item-value">{{ account.isTotalMoney === 0 ? '是' : '否' }}</text>
      </view>
      
      <view class="detail-item">
        <text class="item-label">创建时间</text>
        <text class="item-value">{{ formatDate(account.createTime) }}</text>
      </view>
      
      <view class="detail-item memo-item">
        <text class="item-label">备注</text>
        <text class="item-value">{{ account.memo || '无备注信息' }}</text>
      </view>
    </view>
    
    <!-- 操作按钮 -->
    <view class="action-buttons" v-if="account.status === 0">
      <button class="btn deactivate" @click="changeAccountStatus(1)">停用账户</button>
    </view>
    <view class="action-buttons" v-else>
      <button class="btn activate" @click="changeAccountStatus(0)">启用账户</button>
    </view>
  </view>
</template>

<script>
import { formatAmount } from '@/common/util.js';
export default {
  name: 'AccountDetail',
  data() {
    return {
      accountId: null,
      account: {
        id: null,
        name: '',
        type: 0,
        balance: 0,
        isBudget: 0,
        isTotalMoney: 0,
        status: 0,
        memo: '',
        createTime: '',
        updateTime: ''
      },
      accountTypes: [
        { value: 0, text: '储蓄账户' },
        { value: 1, text: '信贷账户' },
        { value: 2, text: '充值账户' },
        { value: 3, text: '投资理财' }
      ]
    };
  },
  
  computed: {
    // 获取账户类型名称
    accountTypeName() {
      const type = this.accountTypes.find(item => item.value === this.account.type);
      return type ? type.text : '未知类型';
    },
    // 账户名称展示
    accountNameDisplay() {
      const name = (this.account.name || '').trim();
      return name || '未命名账户';
    }
  },
  
  onLoad(options) {
    if (options && options.id) {
      this.accountId = options.id;
      this.loadAccountDetail();
    } else {
      uni.showToast({ title: '参数错误', icon: 'none' });
      setTimeout(() => {
        this.navigateBack();
      }, 1500);
    }
  },
  
  methods: {
    // 加载账户详情
    async loadAccountDetail() {
      if (!this.accountId) return;
      
      try {
        uni.showLoading({ title: '加载中...' });
        const response = await this.$request({
          url: `${this.$backUrlConfig.endpoints.account_getById}${this.accountId}`,
          method: 'GET'
        });
        
        if (response) {
          this.account = {
            ...response,
            name: (response.name || '').trim(),
            // 确保数字类型正确
            type: Number(response.type),
            balance: Number(response.balance),
            isBudget: Number(response.isBudget),
            isTotalMoney: Number(response.isTotalMoney),
            status: Number(response.status)
          };
        } else {
          uni.showToast({ title: '加载失败', icon: 'none' });
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
    
    // 格式化余额（分→元，去单位）
    formatBalance(balance) {
      const n = Number(balance) || 0;
      return formatAmount(n / 100);
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '未知时间';
      
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
	  const seconds = date.getSeconds().toString().padStart(2, '0');
      
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
    
    // 更改账户状态
    async changeAccountStatus(status) {
      const confirmText = status === 0 ? '确定要启用此账户吗？' : '确定要停用此账户吗？';
      
      uni.showModal({
        title: '确认操作',
        content: confirmText,
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({ title: '处理中...' });
              const response = await this.$request({
                url: `${this.$backUrlConfig.endpoints.account_updateStatus}${this.accountId}/${status}`,
                method: 'PUT'
              });
              
              if (response) {
                uni.showToast({ title: status === 0 ? '启用成功' : '停用成功', icon: 'success' });
                this.account.status = status;
              } else {
                uni.showToast({ title: '操作失败', icon: 'none' });
              }
            } catch (error) {
              console.error('更改账户状态失败:', error);
              uni.showToast({ title: '网络异常', icon: 'none' });
            } finally {
              uni.hideLoading();
            }
          }
        }
      });
    },
    
    // 返回上一页（账户列表）
    navigateBack() {
      uni.navigateBack({ delta: 1 });
    },
    
    // 跳转到编辑页面
    navigateToEdit() {
      uni.navigateTo({
        url: `/pages/account/AccountEdit?id=${this.accountId}`
      });
    }
  }
};
</script>

<style lang="scss">
// 变量定义
$primary: #2563eb;
$text: #1e293b;
$text-light: #64748b;
$border: #e2e8f0;
$bg: #f8fafc;
$white: #ffffff;
$danger: #ef4444;
$success: #10b981;

.account-detail-page {
  background-color: $bg;
  min-height: 100vh;
  font-family: system-ui, -apple-system, sans-serif;
  padding-top: 16px; // 顶部增加内边距，替代原导航栏空间
}

// 移除导航栏样式

// 账户信息卡片
.account-card {
  background-color: $white;
  margin: 0 16px 16px; // 调整上边距
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  .account-type, .account-balance, .account-status, .account-name {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    
    &:not(:last-child) {
      border-bottom: 1px solid $border;
    }
  }
  
  .type-label, .balance-label, .status-label, .name-label {
    font-size: 14px;
    color: $text-light;
  }
  
  .type-value, .status-value, .name-value {
    font-size: 16px;
    color: $text;
    font-weight: 500;
  }
  
  .balance-value {
    font-size: 24px;
    color: $primary;
    font-weight: 600;
  }
  
  .status-active .status-value {
    color: $success;
  }
  
  .status-inactive .status-value {
    color: $danger;
  }
}

// 详细信息列表
.detail-list {
  background-color: $white;
  margin: 0 16px 16px;
  border-radius: 12px;
  overflow: hidden;
  
  .detail-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid $border;
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .memo-item {
    align-items: flex-start;
    
    .item-value {
      padding-top: 2px;
      line-height: 1.5;
    }
  }
  
  .item-label {
    font-size: 15px;
    color: $text;
  }
  
  .item-value {
    font-size: 15px;
    color: $text-light;
    text-align: right;
  }
}

// 操作按钮
.action-buttons {
  padding: 16px;
  
  .btn {
    width: 100%;
    height: 48px;
    line-height: 48px;
    font-size: 16px;
    font-weight: 500;
    border-radius: 8px;
    border: none;
    transition: all 0.2s;
  }
  
  .deactivate {
    background-color: $danger;
    color: $white;
    
    &:active {
      background-color: #dc2626;
    }
  }
  
  .activate {
    background-color: $success;
    color: $white;
    
    &:active {
      background-color: #059669;
    }
  }
}

// 动画效果
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.account-card, .detail-list {
  animation: fadeIn 0.3s ease forwards;
  opacity: 0;
}

.account-card {
  animation-delay: 0.1s;
}

.detail-list {
  animation-delay: 0.2s;
}
</style>
    