<template>
  <view class="ledger-page">

    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载账本信息..."></uni-load-more>
    </view>

    <!-- 错误提示 -->
    <view class="error-container" v-if="errorMsg">
      <text class="error-text">{{ errorMsg }}</text>
      <button class="retry-btn" @click="loadLedgerList">重试</button>
    </view>

    <!-- 账本列表 -->
    <view class="ledger-list" v-if="!loading && !errorMsg">
      <view class="form-title">个人/情侣/家庭/班级/公司/婚礼/聚餐等各种需要记录收支的场景都可以建一个账本</view>

      <!-- 自己的账本标题 -->
      <view class="section-title">
        <view class="title-with-icon">
          <text>自己的账本</text>
          <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click.stop="onLedgerTipClick" />
        </view>
      </view>
      <!-- 自己的账本 -->
      <view v-if="ledgers.length > 0">
        <view
          class="ledger-item"
          v-for="(item, index) in ledgers"
          :key="item.id"
          :class="{ 'item-disabled': item.status === 1 }"
        >
          <view class="item-info">
            <view class="item-main-info">
              <text class="item-title">{{ getLedgerTitle(item) }}</text>
            </view>
            <view class="item-details">
              <text class="item-type">{{ item.typeName }}</text>
              <text class="item-property">{{ item.propertyName }}</text>
              <text v-if="item.isDefault === 1" class="item-default">默认</text>
            </view>
            <view class="item-memo" v-if="item.memo">
              <text>{{ item.memo }}</text>
            </view>
          </view>
          <view class="item-actions">
            <button class="action-share-btn" @click.stop="navigateToAddMember(item)">分享</button>
            <view class="action-icon" @click.stop="navigateToEditLedger(item)" title="编辑">
              <custom-icon type="bianji" :size="23" color="#007aff"></custom-icon>
            </view>
            <view class="action-icon" @click.stop="handleStatusChange(item)" :title="item.status === 0 ? '停用' : '启用'">
              <custom-icon :type="item.status === 0 ? 'guanbi' : 'qiyong'" :size="23" :color="item.status === 0 ? '#ff4d4f' : '#52c41a'"></custom-icon>
            </view>
          </view>
        </view>
      </view>
      <view class="no-data" v-else>
        暂无账本，点击右下角按钮创建第一个账本
      </view>

       <!-- 他分享的账本：按分享人分组，标题在列表外，复用自己的账本样式 -->
      <view v-if="sharedGroups.length > 0">
        <view class="shared-group-block" v-for="group in sharedGroups" :key="group.parentUserId">
          <!-- 分组标题：使用 section-title 外观，左侧显示头像和名称 -->
          <view class="section-title shared-title">
            <view class="avatar-small">
              <user-avatar :avatarUrl="group.parentUserPictureAddress" :clickable="false" :uploadable="false" :showEditMask="false" />
            </view>
            <text class="shared-title-name">{{ group.parentUserName }}</text>
            <view class="shared-title-suffix">
              <text>分享的账本</text>
              <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click.stop="onSharedLedgerTipClick" />
            </view>
          </view>

          <!-- 共享列表：完全复用自己的账本样式结构 -->
          <view>
            <view class="ledger-item" v-for="s in group.ledgers" :key="s.id || s.ledgerId">
              <view class="item-info">
                <view class="item-main-info">
                  <text class="item-title">{{ s.ledgerName || s.name }}</text>
                </view>
                <view class="item-details">
                  <text class="item-type">{{ s.typeName }}</text>
                  <text class="item-property">{{ s.propertyName }}</text>
                </view>
                <view class="item-memo" v-if="s.memo">
                  <text>{{ s.memo }}</text>
                </view>
              </view>
              <view class="item-actions">
                <button class="action-share-btn" @click.stop="navigateToAddMember(s)">分享</button>
              </view>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="no-data-sub">朋友还没有分享账本给你，快去联系ta</view>
    </view>

    <!-- 悬浮新增按钮 -->
    <view class="floating-add-btn" :class="{ 'disabled': isGuest }" @click="onFloatingAddClick">
      <text class="add-btn-text">新增自己账本</text>
    </view>
    <!-- 新增：悬浮查看分享记录按钮 -->
    <view class="floating-share-record-btn" @click="navigateToMemberList">
      <text class="share-record-text">分享记录</text>
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
import { formatNumber } from '@/common/util.js';
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import UniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
import UniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue';
import UserAvatar from '@/components/user-avatar.vue';

export default {
  components: {
    UniIcons,
    UniPopup,
    UniLoadMore,
    UserAvatar
  },
  data() {
    return {
      loading: true,
      errorMsg: '',
      ledgers: [],
      sharedGroups: [],
      isGuest: false,
      // 确认对话框相关
      confirmTitle: '',
      confirmContent: '',
      confirmButtonText: '',
      currentLedger: null,
      confirmActionType: '' // 'status' 或 'delete'
    };
  },
  onLoad() {
    //this.loadLedgerList();
  },
  
  onShow() {
    // 当页面显示时（包括从其他页面返回时）刷新账本列表
    this.loadLedgerList();
    // 读取游客模式标记
    const guestFlag = uni.getStorageSync('isGuest');
    this.isGuest = guestFlag === true || guestFlag === 'true';
  },
  methods: {
    // 点击账本提示图标显示提示信息
    onLedgerTipClick() {
      uni.showModal({
        title: '1、个人/情侣/家庭/班级/公司/婚礼/聚餐等各种需要记录收支的场景都可以建一个账本\n      2、账本可以分享给其他人，ta就可以和你共同维护这个账本啦~',
        icon: 'none',
        duration: 3000
      });
    },

    // 点击共享账本提示图标显示提示信息
    onSharedLedgerTipClick() {
      uni.showModal({
        title: '1、共享账本是指朋友分享给你的账本\n      2、你能维护这个共享账本中的所有账单\n      3、你也可以把这个账本分享给其他朋友一起维护',
        icon: 'none',
        duration: 3000
      });
    },
    
    // 加载账本列表
    loadLedgerList() {
      this.loading = true;
      this.errorMsg = '';

      const p1 = request({
        url: backUrl.endpoints.ledger_getByUser,
        method: 'GET'
      }).then(res => {
        if (res && Array.isArray(res)) {
          this.ledgers = res;
        } else {
          this.ledgers = [];
        }
      });

      const p2 = request({
        url: backUrl.endpoints.ledger_getSharedToMe,
        method: 'GET'
      }).then(res => {
        if (res && Array.isArray(res)) {
          this.sharedGroups = this.groupSharedByParent(res);
        } else {
          this.sharedGroups = [];
        }
      });

      Promise.allSettled([p1, p2]).then(() => {
        this.loading = false;
      }).catch(err => {
        this.loading = false;
        this.errorMsg = '加载账本列表失败，请重试';
        console.error('加载账本列表失败:', err);
      });
    },

    groupSharedByParent(list) {
      const map = {};
      list.forEach(item => {
        const pid = item.parentUserId;
        if (!map[pid]) {
          map[pid] = {
            parentUserId: pid,
            parentUserName: item.parentUserName,
            parentUserPictureAddress: item.parentUserPictureAddress,
            ledgers: []
          };
        }
        map[pid].ledgers.push(item);
      });
      return Object.values(map);
    },

    formatShareDate(dateStr) {
      if (!dateStr) return '';
      // 简单从字符串截取日期部分
      // e.g. '2025-10-25T20:33:44' -> '2025-10-25'
      return dateStr.substring(0, 10);
    },
    
    // 获取账本标题
    getLedgerTitle(ledger) {
      let title = ledger.name;
      
      // 停用状态的账本在标题后显示(已停用)
      if (ledger.status === 1) {
        title += '（已停用）';
      }
      
      return title;
    },
    
    // 跳转到添加/编辑账本页面
    navigateToAddLedger() {
      uni.navigateTo({
        url: '/pages/ledger/saveLedger'
      });
    },

    // 悬浮新增按钮点击（游客模式拦截）
    onFloatingAddClick() {
      if (this.isGuest) {
        uni.showToast({
          title: '游客模式不允许新增账本',
          icon: 'none'
        });
        return;
      }
      this.navigateToAddLedger();
    },
    
    // 跳转到编辑账本页面
    navigateToEditLedger(ledger) {
      // 检查是否为游客模式
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后编辑账本',
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
        url: `/pages/ledger/saveLedger?id=${ledger.id}`
      });
    },
    // 新增：跳转到添加成员页面（带上ledgerId）
    navigateToAddMember(ledger) {
      // 检查是否为游客模式
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后分享账本',
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
      const id = ledger && (ledger.id || ledger.ledgerId);
      if (!id) {
        uni.showToast({ title: '无法识别账本', icon: 'none' });
        return;
      }
      uni.navigateTo({
        url: `/pages/member/addMember?ledgerId=${id}`
      });
    },
    // 新增：跳转到成员列表（查看分享记录）
    navigateToMemberList() {
      // 检查是否为游客模式
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后查看分享记录',
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
        url: '/pages/member/member'
      });
    },
    
    // 处理状态变更
    handleStatusChange(ledger) {
      // 检查是否为游客模式
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后修改账本状态',
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
      this.currentLedger = ledger;
      this.confirmTitle = ledger.status === 0 ? '停用账本' : '启用账本';
      this.confirmContent = ledger.status === 0 ? '确定要停用该账本吗？停用后账本将不可用。' : '确定要启用该账本吗？';
      this.confirmButtonText = ledger.status === 0 ? '停用' : '启用';
      this.confirmActionType = 'status';
      this.openConfirmPopup();
    },
    

    
    // 打开确认对话框
    openConfirmPopup() {
      this.$refs.confirmPopup.open();
    },
    
    // 关闭确认对话框
    closeConfirmPopup() {
      this.$refs.confirmPopup.close();
    },
    
    // 确认操作
    confirmAction() {
      if (this.confirmActionType === 'status') {
        this.updateLedgerStatus();
      } else if (this.confirmActionType === 'delete') {
        this.deleteLedger();
      }
      this.closeConfirmPopup();
    },
    
    // 更新账本状态
    updateLedgerStatus() {
      const newStatus = this.currentLedger.status === 0 ? 1 : 0;
      
      request({
        url: backUrl.endpoints.ledger_updateStatus + '?id=' + this.currentLedger.id + '&status=' + newStatus,
        method: 'POST',
        customOptions: {
          returnFullResponse: true
        }
      }).then(response => {
        if (response) {
          uni.showToast({
            title: newStatus === 1 ? '账本已停用' : '账本已启用',
            icon: 'success'
          });
          // 刷新列表
          this.loadLedgerList();
        } else {
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          });
        }
      }).catch(err => {
        console.error('更新账本状态失败:', err);
        uni.showToast({
          title: '操作失败',
          icon: 'none'
        });
      });
    },
    

  }
};
</script>

<style scoped>
.ledger-page {
  padding-bottom: 80rpx;
  background-color: #f8f8f8;
  min-height: 100vh;
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

.ledger-list {
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

.ledger-item {
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
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.item-status {
  font-size: 24rpx;
  color: #999999;
  background-color: #f0f0f0;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
}

.item-details {
  display: flex;
  gap: 20rpx;
  margin-bottom: 12rpx;
}

.item-type {
  font-size: 26rpx;
  color: #666666;
  background-color: #e6f7ff;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
}

.item-property {
  font-size: 26rpx;
  color: #666666;
  background-color: #f6ffed;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
  margin-left: 10rpx;
}

/* 共享账本样式 */
.shared-section {
  margin-top: 24rpx;
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 20rpx;
}

.shared-group {
  margin-bottom: 16rpx;
}
.shared-group-block {
  margin-top: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 20rpx;
}

.title-with-icon {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.notify-tip-icon {
  margin-left: 8rpx;
}

.title-with-icon {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.notify-tip-icon {
  margin-left: 8rpx;
}

.shared-title {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.shared-title-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.shared-title-suffix {
  font-size: 22rpx;
  color: #999999;
  margin-left: 8rpx;
}
.shared-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}
.shared-parent-name {
  font-size: 28rpx;
  color: #333333;
  display: flex;
  align-items: center;
}
.avatar-small {
  width: 64rpx;
  height: 64rpx;
  transform: scale(0.32);
  transform-origin: left center;
  overflow: visible;
  display: flex;
  align-items: center;
}

.no-data-sub {
  color: #999999;
  text-align: center;
  padding: 20rpx 0;
}

    .item-default {
      font-size: 26rpx;
      color: #ffffff;
      background-color: #1989fa;
      padding: 4rpx 16rpx;
      border-radius: 16rpx;
      margin-left: 10rpx;
    }

    .item-memo {
      font-size: 26rpx;
      color: #666666;
      padding: 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      display: block;
      max-width: 100%;
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

.action-share-btn {
  background-color: #ff4d4f;
  color: #ffffff;
  border: none;
  border-radius: 30rpx;
  padding: 0 24rpx;
  height: 60rpx;
  line-height: 60rpx;
  font-size: 26rpx;
}

.floating-add-btn {
  position: fixed;
  right: 40rpx;
  bottom: 250rpx; /* 上移，为分享记录按钮腾出空间 */
  min-width: 200rpx;
  height: 70rpx;
  padding: 0 24rpx;
  background-color: #1989fa;
  color: #ffffff;
  border-radius: 40rpx; /* 胶囊样式 */
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(25, 137, 250, 0.4);
}

.floating-add-btn.disabled {
  background-color: #cccccc;
  box-shadow: none;
}

.add-btn-text {
  font-size: 26rpx;
  font-weight: 600;
}

/* 新增：分享记录悬浮按钮样式 */
.floating-share-record-btn {
  position: fixed;
  right: 40rpx;
  bottom: 150rpx; /* 位于新增按钮下方 */
  height: 70rpx;
  padding: 0 24rpx;
  background-color: #52c41a;
  color: #ffffff;
  border-radius: 40rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(82, 196, 26, 0.35);
}

.share-record-text {
  font-size: 26rpx;
  font-weight: 600;
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
