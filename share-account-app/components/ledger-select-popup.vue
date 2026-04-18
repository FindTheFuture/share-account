<template>
  <uni-popup ref="ledgerPopup" type="center" :mask-click="true" @maskClick="close">
    <view class="popup-container">
      <view class="popup-header">
        <view class="title-with-icon">
          <text class="popup-title">选择账本</text>
          <uni-icons type="info" size="18" color="#999" class="notify-tip-icon icon-hover" @click.stop="onLedgerTipClick" />
        </view>
        <view class="popup-actions">
          <custom-icon type="bianji" :size="20" color="#007AFF" @tap="navigateToLedgerList" class="icon-hover"></custom-icon>
          <custom-icon type="guanbi" :size="20" color="#999" @tap="close" class="icon-hover"></custom-icon>
        </view>
      </view>
      <view class="divider"></view>
      <view class="popup-content">
        <div>
          <!-- 别人分享给我的账本：分组展示，置于顶部，头像与名称样式与ledger.vue一致 -->
          <view v-if="queryShared && sharedGroups.length > 0">
            <view v-for="group in sharedGroups" :key="group.parentUserId">
              <view class="section-title shared-title">
                <view class="avatar-small">
                  <user-avatar :avatarUrl="group.parentUserPictureAddress" :clickable="false" :uploadable="false" :showEditMask="false" />
                </view>
                <view class="shared-title-content">
                  <text class="shared-title-name">{{ group.parentUserName }}</text>
                  <text class="shared-title-label">分享的账本</text>
                  <uni-icons type="info" size="18" color="#999" class="notify-tip-icon icon-hover" @click.stop="onSharedLedgerTipClick" />
                </view>
              </view>
              <view class="ledger-list">
                <view 
                  v-for="(s, index) in group.ledgers" 
                  :key="s.id"
                  :class="['ledger-item', 'ledger-card', { 'selected': selectedLedger && selectedLedger.id === s.id }]"
                  :style="{ animationDelay: (index * 0.1) + 's' }"
                  @tap="selectLedger(s)"
                >
                  <view class="ledger-info-row ledger-name-row">
                    <text class="ledger-name">{{ s.name }}</text>
                  </view>
                  <view class="ledger-info-row ledger-tags">
                    <text class="ledger-type-text">{{ s.typeName }}</text>
                    <text class="ledger-property-text">{{ s.propertyName }}</text>
                  </view>
                  <button class="item-share-btn breath-animation" @tap.stop="navigateToAddMember(s)">分享</button>
                </view>
              </view>
            </view>
          </view>
          <view v-else-if="queryShared" class="no-data-tip">朋友还没有分享账本给你</view>

          <!-- 自己的账本标题 -->
          <view class="section-title">
            <text>自己的账本</text>
            <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click.stop="zijiLedgerTipClick"/>
          </view>
          <!-- 自己的账本列表：置于共享列表后面 -->
          <view class="ledger-list" v-if="ledgers.length > 0">
            <view 
              v-for="(ledger, index) in ledgers" 
              :key="ledger.id"
              :class="['ledger-item', 'ledger-card', { 'selected': selectedLedger && selectedLedger.id === ledger.id }]"
              :style="{ animationDelay: (index * 0.1) + 's' }"
              @tap="selectLedger(ledger)"
            >
              <view class="ledger-info-row ledger-name-row">
                <text class="ledger-name">{{ ledger.name }}</text>
              </view>
              <view class="ledger-info-row ledger-tags">
                <text class="ledger-type-text">{{ ledger.typeName }}</text>
                <text class="ledger-property-text">{{ ledger.propertyName }}</text>
                <text v-if="ledger.isDefault === 1" class="default-badge">默认</text>
              </view>
              <button class="item-share-btn breath-animation" @tap.stop="navigateToAddMember(ledger)">分享</button>
            </view>
            <!-- 底部操作：查看共享账单 + 不选账本 -->
            <view class="divider"></view>
            <view class="bottom-actions">
              <view v-if="showShareBillAction" class="share-all-bills btn-hover" @tap="onShareAllBillsTap">
                <text class="share-all-bills-text">查看所有共享账单</text>
              </view>
              <view class="no-select-account btn-hover" @tap="clearLedgerSelection">
                <text class="no-select-account-text">不选账本</text>
              </view>
            </view>
          </view>
          <view v-else class="no-data-tip">暂无账本数据</view>
        </div>
      </view>
    </view>
  </uni-popup>
</template>

<script>
import request from '@/common/request.js';
import backUrl from '@/common/back_url.js';
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import UniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
import UserAvatar from '@/components/user-avatar.vue';

export default {
  name: 'LedgerSelectPopup',
  components: {
    UniIcons,
    UniPopup,
    UserAvatar
  },
  props: {
    selectedLedger: {
      type: Object,
      default: null
    },
    autoSelectDefault: {
      type: Boolean,
      default: true
    },
    queryShared: {
      type: Boolean,
      default: true
    },
    // 是否显示“查看所有共享账单”入口（默认不显示）
    showShareBillAction: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      ledgers: [],
      needRefreshLedgers: false,
      sharedGroups: [],
      isGuest: false
    };
  },
  methods: {
    async loadLedgers() {
      try {
        const res = await request({ 
          url: backUrl.endpoints.ledger_getByUser, 
          method: 'GET' 
        });
        if (res && Array.isArray(res)) {
          const normalLedgers = res.filter(ledger => ledger.status === 0);
          // 规范化并补充归属标记：自己的账本 isShared=false
          this.ledgers = normalLedgers.map(l => ({
            ...l,
            id: l.id || l.ledgerId,
            name: l.name || l.ledgerName,
            isShared: false
          }));
          if (this.autoSelectDefault && (!this.selectedLedger || !this.selectedLedger.id)) {
            const defaultLedger = this.ledgers.find(ledger => ledger.isDefault === 1);
            if (defaultLedger) {
              this.$emit('select', defaultLedger);
            } else if (this.ledgers.length > 0) {
              this.$emit('select', this.ledgers[0]);
            }
          }
        } else {
          this.ledgers = [];
        }
      } catch (error) {
        console.error('加载账本数据异常:', error);
        this.ledgers = [];
      }
    },
    async loadSharedLedgers() {
      try {
        const res = await request({
          url: backUrl.endpoints.ledger_getSharedToMe,
          method: 'GET'
        });
        if (res && Array.isArray(res)) {
          const groups = this.groupSharedByParent(res);
          // 规范化字段，复用弹窗列表样式和选择逻辑
          this.sharedGroups = groups.map(g => ({
            ...g,
            ledgers: (g.ledgers || []).map(s => ({
              id: s.id || s.ledgerId,
              name: s.ledgerName || s.name,
              typeName: s.typeName,
              propertyName: s.propertyName,
              isDefault: 0, // 共享账本不标默认
              // 共享账本归属标记与分享者信息
              isShared: true,
              parentUserId: g.parentUserId,
              parentUserName: g.parentUserName,
              parentUserPictureAddress: g.parentUserPictureAddress
            }))
          }));
        } else {
          this.sharedGroups = [];
        }
      } catch (error) {
        console.error('加载共享账本数据异常:', error);
        this.sharedGroups = [];
      }
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
    open() {
      // 检查游客模式状态
      const guestFlag = uni.getStorageSync('isGuest');
      this.isGuest = guestFlag === true || guestFlag === 'true';
      
      if (this.ledgers.length === 0) {
        this.loadLedgers();
      }
      if (this.queryShared && this.sharedGroups.length === 0) {
        this.loadSharedLedgers();
      }
      this.$refs.ledgerPopup.open();
    },
    close() {
      this.$refs.ledgerPopup.close();
    },
    selectLedger(ledger) {
      this.$emit('select', ledger);
      this.close();
    },
    clearLedgerSelection() {
      this.$emit('select', null);
      this.close();
    },
    onShareAllBillsTap() {
      this.$emit('share-all-bills');
      this.close();
    },
    navigateToLedgerList() {
      this.close();
      this.needRefreshLedgers = true;
      uni.navigateTo({
        url: '/pages/ledger/ledger'
      });
    },
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
      if (!id) return;
      uni.navigateTo({
        url: `/pages/member/addMember?ledgerId=${id}`
      });
    },
    refreshIfNeeded() {
      if (this.needRefreshLedgers) {
        this.loadLedgers();
        if (this.queryShared) {
          this.loadSharedLedgers();
        }
        this.needRefreshLedgers = false;
      }
    },
    async setSelectedById(id) {
      if (!Array.isArray(this.ledgers) || this.ledgers.length === 0) {
        await this.loadLedgers();
      }
      if (this.queryShared && (!Array.isArray(this.sharedGroups) || this.sharedGroups.length === 0)) {
        await this.loadSharedLedgers();
      }
      const foundOwn = this.ledgers.find(l => String(l.id) === String(id));
      if (foundOwn) {
        this.$emit('select', foundOwn);
        return;
      }
      if (this.queryShared) {
        for (const g of this.sharedGroups) {
          const found = (g.ledgers || []).find(l => String(l.id) === String(id));
          if (found) {
            this.$emit('select', found);
            return;
          }
        }
      }
    },
    async initAutoSelect() {
      // 强制重新加载账本数据
      await this.loadLedgers();
      if (this.queryShared) {
        await this.loadSharedLedgers();
      }
      // 加载完成后，自动选择默认账本
      if (this.autoSelectDefault && (!this.selectedLedger || !this.selectedLedger.id)) {
        const defaultLedger = this.ledgers.find(ledger => ledger.isDefault === 1);
        if (defaultLedger) {
          this.$emit('select', defaultLedger);
        } else if (this.ledgers.length > 0) {
          this.$emit('select', this.ledgers[0]);
        }
      }
    },
    onLedgerTipClick() {
      uni.showModal({
        title: '个人/情侣/家庭/班级/公司/婚礼/聚餐等各种需要记录收支的场景都可以建一个账本',
        icon: 'none',
        duration: 3000
      });
    },

    zijiLedgerTipClick() {
      uni.showModal({
        title: '自己的账本是指你创建的账本',
        icon: 'none',
        duration: 3000
      });
    },

    onSharedLedgerTipClick() {
      uni.showModal({
        title: '1、共享账本是指朋友分享给你的账本\n      2、你能维护这个共享账本中的所有账单\n      3、你也可以把这个账本分享给其他朋友一起维护',
        icon: 'none',
        duration: 3000
      });
    },
  }
};
</script>

<style scoped>
/* 弹出层样式 */
.popup-container {
  width: 720rpx;
  background-color: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.15);
  /* 自适应内容高度，超过视口时限制最大高度 */
  height: auto;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

/* 分割线样式 */
.divider {
  height: 1rpx;
  background-color: #f0f0f0;
  width: 100%;
}

.popup-header {
  padding: 24rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
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

/* 弹出层操作按钮样式 */
.popup-actions {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.popup-content {
  /* 不强制撑满，按内容自适应；内容过长时内部滚动 */
  overflow-y: auto;
  padding: 24rpx 32rpx;
  /* 预留头部空间后最大高度，避免整体弹窗超高 */
  max-height: calc(80vh - 140rpx);
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

/* 呼吸动画效果 */
@keyframes breath {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.9;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.breath-animation {
  animation: breath 1.5s ease-in-out infinite;
}

/* 按钮悬浮动画效果 */
.btn-hover {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  padding: 12rpx 24rpx;
  border-radius: 12rpx;
  display: inline-block;
}

.btn-hover:hover {
  transform: scale(1.1);
  background-color: #f5f5f5;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

/* 账本卡片样式 */
.ledger-card {
  background-color: #f8f0f8;
  border: none;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  animation: slideInLeft 0.5s ease-out forwards;
  opacity: 0;
}

.ledger-card:hover {
  transform: scale(1.03);
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.1);
}

.ledger-card:active {
  background-color: #f0e6f0;
}

.ledger-card.selected {
  background-color: #e8f4fd;
  border: 2rpx solid #007AFF;
  box-shadow: 0 4rpx 16rpx rgba(0, 122, 255, 0.2);
}

.ledger-tags {
  gap: 8rpx;
}

/* 账本列表样式 */
.ledger-list {
  padding: 0;
  margin-bottom: 24rpx;
}

.ledger-item {
  padding: 24rpx;
  border-bottom: 1px solid #f0f0f0;
}

.ledger-item:active {
  background-color: #e0e0e0;
}

.ledger-item.selected {
  background-color: #e8f4fd;
}

.ledger-info-row {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 8rpx;
}

.ledger-info-row:first-child {
  justify-content: space-between;
}

.ledger-name-row {
  justify-content: flex-start;
}

.ledger-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  flex: 1;
}

.default-badge {
  font-size: 24rpx;
  color: #ffffff;
  background-color: #007AFF;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
  margin-right: 16rpx;
}

/* 账本类型和属性文本样式 */
.ledger-type-text {
  font-size: 24rpx;
  color: #007AFF;
}

.ledger-property-text {
  font-size: 24rpx;
  color: #5AC8FA;
}

/* 分享按钮（右上角） */
.item-share-btn {
  background-color: #ff7043;
  color: #ffffff;
  border: none;
  border-radius: 24rpx;
  height: 56rpx;
  line-height: 56rpx;
  font-size: 24rpx;
  padding: 0 24rpx;
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 112, 67, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.item-share-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 6rpx 16rpx rgba(255, 112, 67, 0.5);
}

/* 不选账本按钮 */
.no-select-account {
  padding: 16rpx;
}
.no-select-account-text {
  font-size: 24rpx;
  color: #007AFF;
  text-decoration: underline;
}

/* 底部操作行 */
.bottom-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40rpx;
  padding: 20rpx 0;
}
.share-all-bills-text {
  font-size: 24rpx;
  color: #007AFF;
  text-decoration: underline;
}

/* 弹出层操作按钮样式 */
.popup-actions {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

/* 无数据提示 */
.no-data-tip {
  padding: 48rpx 20rpx;
  text-align: center;
  font-size: 24rpx;
  color: #999999;
}

/* 分组标题样式，保持与ledger.vue一致 */
.section-title {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 16rpx;
  padding: 12rpx 0;
  font-size: 24rpx;
  font-weight: bold;
  color: #333333;
}

.shared-title {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 16rpx;
  padding: 12rpx 0;
}

.section-title.shared-title .avatar-small {
  width: 96rpx !important;
  height: 96rpx !important;
  overflow: hidden !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  margin-right: 16rpx !important;
  flex-shrink: 0 !important;
  position: relative !important;
  max-width: 96rpx !important;
  max-height: 96rpx !important;
  border-radius: 50% !important;
}

/* 移除朋友头像的背景色 */
.section-title.shared-title .avatar-small .user-avatar-container {
  width: 96rpx !important;
  height: 96rpx !important;
  max-width: 96rpx !important;
  max-height: 96rpx !important;
  overflow: hidden !important;
  border-radius: 50% !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.section-title.shared-title .avatar-small .avatar-wrapper {
  background-color: transparent !important;
  border: none !important;
  box-shadow: none !important;
  width: 96rpx !important;
  height: 96rpx !important;
  max-width: 96rpx !important;
  max-height: 96rpx !important;
  overflow: hidden !important;
  border-radius: 50% !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.section-title.shared-title .avatar-small .avatar-img {
  border-radius: 50% !important;
  width: 96rpx !important;
  height: 96rpx !important;
  max-width: 96rpx !important;
  max-height: 96rpx !important;
  overflow: hidden !important;
  object-fit: cover !important;
}

/* 调整共享标题布局，确保头像不遮挡文字 */
.shared-title {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  padding: 12rpx 0;
  flex-wrap: nowrap;
  width: 100%;
}

.shared-title-content {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
  min-width: 0;
}

.shared-title-name {
  font-size: 24rpx;
  font-weight: bold;
  color: #333333;
  flex-shrink: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shared-title-label {
  font-size: 24rpx;
  color: #999999;
  flex-shrink: 1;
  white-space: nowrap;
}

/* 调整info图标位置，使其靠近文字 */
.shared-title-content .notify-tip-icon {
  margin-left: 8rpx;
  margin-right: 0;
  flex-shrink: 0;
}

.shared-title-suffix {
  font-size: 20rpx;
  color: #999999;
  margin-left: 8rpx;
}

/* 兼容旧样式（可保留不影响） */
.shared-group-header {
  padding: 12rpx 20rpx;
}
.shared-group-name {
  font-size: 26rpx;
  color: #666;
}
</style>