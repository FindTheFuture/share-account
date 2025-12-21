<template>
  <view class="member-page">
    
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载成员信息..."></uni-load-more>
    </view>

    <!-- 错误提示 -->
    <view class="error-container" v-if="errorMsg">
      <text class="error-text">{{ errorMsg }}</text>
      <button class="retry-btn" @click="loadMemberList">重试</button>
    </view>

    <!-- 成员列表 -->
    <view class="member-list" v-if="!loading && !errorMsg">
      <view v-if="memberList.length > 0">
        <view
          class="member-item"
          v-for="member in memberList"
          :key="member.id"
        >
          <view class="item-info">
            <view class="item-main-info">
              <text class="item-title">{{ member.name }}</text>
            </view>
            <view class="item-details">
              <text 
                class="item-type" 
                :class="{ 
                  'status-normal': member.status === 1, 
                  'status-pending': member.status === 0 
                }"
              >
                {{ member.status === 0 ? '添加中' : '正常' }}
              </text>
              <text v-if="member.ledgerName" class="item-property">
                {{ member.ledgerName }}
              </text>
              <text v-if="member.is_aa_bill" class="item-default">
                占比: {{ member.percentage }}%
              </text>
            </view>
            <view class="item-memo">
              <text>{{ formatDate(member.createTime) }}</text>
            </view>
          </view>
          <view class="item-actions">
            <view class="action-icon" @click.stop="editMember(member)" title="编辑">
              <custom-icon type="bianji" :size="23" color="#007aff"></custom-icon>
            </view>
            <view class="action-icon" @click.stop="deleteMember(member)" title="删除">
              <custom-icon type="shanchu" :size="23" color="#ff4d4f"></custom-icon>
            </view>
          </view>
        </view>
      </view>
      <view class="no-data" v-else>
        <view>你还没分享过账本给朋友们，快去分享吧~</view>
        <view>朋友就可以和你一起在这个账本下记账啦😄</view>
      </view>
    </view>
  </view>
</template>

<script>
import request from '../../common/request.js';
import backUrl from '../../common/back_url.js';
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import UniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue';

export default {
  components: {
    UniIcons,
    UniLoadMore
  },
  data() {
    return {
      memberList: [],
      loading: false,
      errorMsg: '',
      isGuest: false
    };
  },
  onLoad() {
  },
  onShow() {
    // 每次页面显示时重新加载成员列表，确保数据最新
    // 更新游客模式标记
    const guestFlag = uni.getStorageSync('isGuest');
    this.isGuest = guestFlag === true || guestFlag === 'true';
    this.loadMemberList();
  },
  methods: {
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },
    
    // 加载成员列表
    async loadMemberList() {
      this.loading = true;
      this.errorMsg = '';
      try {
        const res = await request.get(backUrl.endpoints.member_list);
        // 若无返回或返回非数组，直接清空并返回
        if (!res || !Array.isArray(res)) {
          this.memberList = [];
          return;
        }
        // 只展示共享账本成员（过滤掉 billId 有值的共享账单成员）
        this.memberList = res.filter(member => member && (member.billId === null || member.billId === ''));
      } catch (error) {
        console.error('加载成员列表失败:', error);
        this.errorMsg = '加载成员列表失败，请重试';
      } finally {
        this.loading = false;
      }
    },
    
    // 编辑成员
    editMember(member) {
      // 跳转到编辑页面
      uni.navigateTo({
        url: `/pages/member/addMember?id=${member.id}`
      });
    },
    
    // 删除成员
    deleteMember(member) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除成员"${member.name}"吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await request.delete('/member/delete/' + member.id);
              // 重新加载成员列表
              this.loadMemberList();
              uni.showToast({ title: '删除成功', icon: 'success' });
            } catch (error) {
              console.error('删除成员失败:', error);
              uni.showToast({ title: '删除成员失败', icon: 'none' });
            }
          }
        }
      });
    },
    
    // 跳转到新增/编辑成员页面
    gotoAddMember() {
      uni.navigateTo({
        url: '/pages/member/addMember'
      });
    },

    // 悬浮新增按钮点击（游客模式拦截）
    onFloatingAddClick() {
      if (this.isGuest) {
        uni.showToast({
          title: '游客模式不允许新增成员',
          icon: 'none'
        });
        return;
      }
      this.gotoAddMember();
    }
  }
};
</script>

<style scoped>
.member-page {
  padding-bottom: 80rpx;
  background-color: #f8f8f8;
  height: 100vh;
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

.member-list {
  padding: 20rpx;
}

.member-item {
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
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

.item-details {
  display: flex;
  gap: 20rpx;
  margin-bottom: 12rpx;
  flex-wrap: wrap;
}

.item-type {
  font-size: 26rpx;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
}

.status-normal {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-pending {
  background-color: #fff7e6;
  color: #fa8c16;
}

.item-property {
  font-size: 26rpx;
  color: #ffffff;
  background-color: #1989fa;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
  margin-left: 10rpx;
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
  color: #999999;
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

.floating-add-btn {
  position: fixed;
  right: 40rpx;
  bottom: 150rpx;
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
</style>
