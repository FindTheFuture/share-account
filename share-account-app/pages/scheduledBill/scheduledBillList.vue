<template>
  <view class="scheduled-bill-page">
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载定时记账配置..."></uni-load-more>
    </view>

    <!-- 错误提示 -->
    <view class="error-container" v-if="errorMsg">
      <text class="error-text">{{ errorMsg }}</text>
      <button class="retry-btn" @click="loadScheduledBillList">重试</button>
    </view>

    <!-- 定时记账配置列表 -->
    <view class="scheduled-bill-list" v-if="!loading && !errorMsg">
      <view v-if="scheduledBillList.length > 0">
        <view
          class="scheduled-bill-item"
          v-for="(item, index) in scheduledBillList"
          :key="item.id"
          :class="{ 'item-disabled': item.status !== 1 }"
        >
          <view class="item-info" @click="showScheduledBillDetail(item)">
            <view class="item-main-info">
              <text class="item-title">{{ item.name }}</text>
              <text class="item-type" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</text>
            </view>
            <view class="item-attributes">
              <text class="attr-item">频率: {{ getCycleTypeText(item.cycleType) }}</text>
              <text class="attr-item">金额: ¥{{ item.price }}</text>
            </view>
            <view class="item-details memo-row" v-if="item.description">
              <rich-text class="item-memo" :nodes="formatMemo(item.description)"></rich-text>
            </view>
          </view>
          <view class="item-actions">
            <view class="action-icon" @click.stop="navigateToEditScheduledBill(item)" title="编辑">
              <custom-icon type="bianji" size="24" color="#007aff"></custom-icon>
            </view>
            <view class="action-icon" @click.stop="handleStatusChange(item)"
              :title="item.status === 1 ? '暂停' : '启用'">
              <custom-icon :type="item.status === 1 ? 'guanbi' : 'qiyong'" size="24" :color="item.status === 1 ? '#ff4d4f' : '#52c41a'"/>
            </view>
            <view class="action-icon" @click.stop="handleDelete(item)" title="删除">
              <custom-icon type="shanchu" size="24" color="#ff4d4f"></custom-icon>
            </view>
            <view class="action-icon" @click.stop="navigateToLogList(item.id)" title="查看日志">
              <uni-icons type="list" size="24" color="#1890ff"></uni-icons>
            </view>
          </view>
        </view>
      </view>
      <view class="no-data" v-else>
        暂无定时记账配置，点击右下角按钮添加
      </view>
    </view>

    <!-- 分页组件 -->
    <view class="pagination-container" v-if="!loading && !errorMsg && total > 0">
      <uni-pagination 
        :current="currentPage" 
        :pageSize="pageSize" 
        :total="total" 
        :totalPages="totalPages"
        @change="handlePageChange"
        show-icon
      />
    </view>

    <!-- 悬浮新增按钮 -->
    <view class="floating-add-btn" @click="navigateToAddScheduledBill">
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
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import UniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
import UniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue';
import UniPagination from '@/uni_modules/uni-pagination/components/uni-pagination/uni-pagination.vue';

export default {
  components: {
    UniIcons,
    UniPopup,
    UniLoadMore,
    UniPagination
  },
  data() {
    return {
      loading: true,
      errorMsg: '',
      scheduledBillList: [],
      // 分页相关
      currentPage: 1,
      pageSize: 10,
      total: 0,
      totalPages: 0,
      // 确认对话框相关
      confirmTitle: '',
      confirmContent: '',
      confirmButtonText: '',
      currentScheduledBill: null,
      confirmActionType: '', // 'delete' 或 'status'
      isGuest: false // 游客模式标记
    }
  },
  onLoad() {
    // 设置页面标题
    uni.setNavigationBarTitle({
      title: '定时记账配置'
    });
    this.loadScheduledBillList();
  },
  onShow() {
    // 检查游客模式状态
    this.isGuest = !!uni.getStorageSync('isGuest');
    // 从添加/编辑页面返回时刷新列表
    if (!this.loading) {
      this.loadScheduledBillList();
    }
  },
  methods: {
    /**
     * 加载定时记账配置列表
     */
    async loadScheduledBillList(pageNum = this.currentPage) {
      this.loading = true;
      this.errorMsg = '';
      
      try {
        const response = await request({
          url: backUrl.endpoints.scheduledBill_list,
          method: 'POST',
          data: {
            pageNum: pageNum,
            pageSize: this.pageSize
          }
        });
        
        if (response) {
          this.scheduledBillList = response.itemList || [];
          this.total = response.total || 0;
          this.totalPages = response.totalPages || 0;
          this.currentPage = pageNum;
        } else {
          this.scheduledBillList = [];
          this.total = 0;
          this.totalPages = 0;
          this.currentPage = 1;
        }
      } catch (error) {
        console.error('加载定时记账配置列表失败:', error);
        this.errorMsg = '网络请求异常，请稍后重试';
      } finally {
        this.loading = false;
      }
    },
    

    
    /**
     * 根据状态获取样式类
     */
    getStatusClass(status) {
      return status === 1 ? 'status-enabled' : 'status-disabled';
    },
    
    /**
     * 根据类型获取样式类
     */
    getTypeClass(type) {
      return type === 0 ? 'type-income' : 'type-expense';
    },
    
    /**
     * 跳转到添加定时记账配置页面
     */
    navigateToAddScheduledBill() {
      // 检查是否为游客模式
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后添加定时记账配置',
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
        url: '/pages/scheduledBill/scheduledBillEdit'
      });
    },

    /**
     * 跳转到编辑定时记账配置页面
     */
    navigateToEditScheduledBill(item) {
      // 检查是否为游客模式
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后编辑定时记账配置',
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
        url: `/pages/scheduledBill/scheduledBillEdit?id=${item.id}`
      });
    },
    
    /**
     * 查看定时记账配置详情
     */
    showScheduledBillDetail(item) {
      // 检查是否为游客模式
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后查看定时记账配置详情',
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
        url: `/pages/scheduledBill/scheduledBillDetail?id=${item.id}`
      });
    },
    
    /**
     * 跳转到日志列表页面
     */
    navigateToLogList(scheduledBillId) {
      // 检查是否为游客模式
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后查看定时记账日志',
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
        url: `/pages/scheduledBill/scheduledBillLogList?scheduledBillId=${scheduledBillId}`
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
    
    /**
     * 根据状态值获取状态文本
     */
    getStatusText(status) {
      switch (status) {
        case 1:
          return '启用';
        case 2:
          return '暂停';
        case 3:
          return '删除';
        default:
          return '未知';
      }
    },
    
    /**
     * 根据周期类型值获取周期类型文本
     */
    getCycleTypeText(cycleType) {
      switch (cycleType) {
        case 1:
          return '每天';
        case 2:
          return '每周';
        case 3:
          return '每月';
        case 4:
          return '每季度';
        case 5:
          return '每年';
        default:
          return '未知';
      }
    },

    /**
     * 处理定时记账配置状态变更
     */
    handleStatusChange(item) {
      // 检查是否为游客模式
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后修改定时记账配置状态',
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
      this.currentScheduledBill = item;
      this.confirmActionType = 'status';
      this.confirmTitle = item.status === 1 ? '确认暂停配置' : '确认启用配置';
      this.confirmContent = item.status === 1 
        ? '暂停后该配置将不再执行' 
        : '';
      this.confirmButtonText = item.status === 1 ? '确认' : '确认';
      
      this.openConfirmPopup();
    },
    
    /**
     * 处理定时记账配置删除
     */
    handleDelete(item) {
      // 检查是否为游客模式
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后删除定时记账配置',
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
      this.currentScheduledBill = item;
      this.confirmActionType = 'delete';
      this.confirmTitle = '确认删除配置';
      this.confirmContent = '删除后将无法恢复，是否确认删除？';
      this.confirmButtonText = '确认删除';
      
      this.openConfirmPopup();
    },
    
    /**
     * 执行确认操作
     */
    async confirmAction() {
      if (this.confirmActionType === 'status' && this.currentScheduledBill) {
        await this.updateScheduledBillStatus(
          this.currentScheduledBill.id, 
          this.currentScheduledBill.status === 1 ? 2 : 1
        );
      } else if (this.confirmActionType === 'delete' && this.currentScheduledBill) {
        await this.deleteScheduledBill(this.currentScheduledBill.id);
      }
      
      this.closeConfirmPopup();
    },
    
    /**
     * 更新定时记账配置状态
     */
    async updateScheduledBillStatus(id, status) {
      try {
        // 状态值已与后端一致，无需转换
        const backendStatus = status;
        const response = await request({
          url: backUrl.endpoints.scheduledBill_updateStatus + id + '/status?status=' + backendStatus,
          method: 'PUT'
        });
        
        if (response !== undefined) {
          uni.showToast({
            title: status === 2 ? '配置已暂停' : '配置已启用',
            icon: 'success'
          });
          this.loadScheduledBillList();
        } else {
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('更新定时记账配置状态失败:', error);
        uni.showToast({
          title: '网络请求异常',
          icon: 'none'
        });
      }
    },
    
    /**
     * 删除定时记账配置
     */
    async deleteScheduledBill(id) {
      try {
        const response = await request({
          url: backUrl.endpoints.scheduledBill_delete + id + '/delete',
          method: 'PUT'
        });
        
        if (response !== undefined) {
          uni.showToast({
            title: '配置已删除',
            icon: 'success'
          });
          this.loadScheduledBillList();
        } else {
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('删除定时记账配置失败:', error);
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
      this.currentScheduledBill = null;
      this.confirmActionType = '';
    },
    
    /**
     * 处理分页变化
     */
    handlePageChange(event) {
      const pageNum = event.current;
      this.loadScheduledBillList(pageNum);
    }
  }
}
</script>

<style scoped>
.scheduled-bill-page {
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

.scheduled-bill-list {
  padding: 20rpx;
}

.scheduled-bill-item {
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
  max-width: calc(100% - 100rpx);
}

.item-main-info {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  gap: 16rpx;
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

/* 类型标签样式 */
.item-type {
  font-size: 26rpx;
  color: #666666;
  background-color: #e6f7ff;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
}

.status-enabled {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-disabled {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.type-income {
  background-color: #e6f7ff;
  color: #1890ff;
}

.type-expense {
  background-color: #fff7e6;
  color: #fa8c16;
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
  background-color: #f0f0f0;
}

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
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

/* 分页容器样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 40rpx 0;
  padding: 0 20rpx;
}
</style>
