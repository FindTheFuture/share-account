<template>
  <view class="budget-list-page">

    <!-- 筛选条件 -->
    <view class="filter-container">
      <view class="filter-item">
        <view class="custom-date-picker" @click="openMonthPicker">
          <text class="date-text">{{ selectedMonth }}</text>
          <custom-icon type="rili" :size="24" color="#666"></custom-icon>
        </view>
        <!-- 自定义年月选择器弹窗 -->
        <uni-popup ref="monthPickerPopup" type="bottom" :mask-click="true">
          <view class="popup-content">
            <view class="popup-header">
              <text class="popup-title">选择年月</text>
              <text class="popup-close" @click="closeMonthPicker">关闭</text>
            </view>
            <view class="month-picker-container">
              <picker-view 
                :value="pickerValue" 
                @change="onPickerChange"
                :indicator-style="'height: 50px;'"
                style="width: 100%; height: 300rpx;">
                <picker-view-column>
                  <view v-for="year in years" :key="year" style="line-height: 50px;">{{ year }}年</view>
                </picker-view-column>
                <picker-view-column>
                  <view v-for="month in months" :key="month" style="line-height: 50px;">{{ month }}月</view>
                </picker-view-column>
              </picker-view>
              <view class="popup-footer">
                <button class="confirm-btn" @click="confirmMonthSelection">确定</button>
              </view>
            </view>
          </view>
        </uni-popup>
        
        <!-- 同步预算的年月选择器弹窗 -->
        <uni-popup ref="syncMonthPickerPopup" type="bottom" :mask-click="true">
          <view class="popup-content">
            <view class="popup-header">
              <text class="popup-title">选择要同步的年月</text>
              <text class="popup-close" @click="closeSyncMonthPicker">关闭</text>
            </view>
            <view class="month-picker-container">
              <picker-view 
                :value="syncPickerValue" 
                @change="onSyncPickerChange"
                :indicator-style="'height: 50px;'"
                style="width: 100%; height: 300rpx;">
                <picker-view-column>
                  <view v-for="year in years" :key="year" style="line-height: 50px;">{{ year }}年</view>
                </picker-view-column>
                <picker-view-column>
                  <view v-for="month in months" :key="month" style="line-height: 50px;">{{ month }}月</view>
                </picker-view-column>
              </picker-view>
              <view class="popup-footer">
                <button class="confirm-btn" @click="confirmSyncMonthSelection">确定</button>
              </view>
            </view>
          </view>
        </uni-popup>
      </view>
    </view>

    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载预算信息..."></uni-load-more>
    </view>

    <!-- 错误提示 -->
    <view class="error-container" v-if="errorMsg">
      <text class="error-text">{{ errorMsg }}</text>
      <button class="retry-btn" @click="loadBudgetList">重试</button>
    </view>

    <!-- 预算列表 -->
    <view class="budget-list" v-if="!loading && !errorMsg">
      <!-- 总预算信息 -->
      <view class="total-budget-card">
        <!-- <view class="total-budget-edit-icon" @click="navigateToAddBudget" :title="totalBudget > 0 ? '修改总预算' : '添加总预算'">
            <custom-icon type="bianji" size="20" color="#ffffff"></custom-icon>
          </view> -->
        <view class="budget-column">
          <view class="column-label">本月总预算</view>
          <view class="column-value">{{ formatAmount(totalBudget) }}</view>
        </view>

      </view>
      
      <!-- 预算数据提示 -->
      <view class="budget-tip" v-if="totalBudget === 0">
        <text class="tip-text">当前月份暂无预算数据，点击右下角按钮添加预算明细</text>
      </view>

      <!-- 预算明细列表 -->
      <view class="budget-items-container">
        <view class="section-title">预算明细</view>
        <view v-if="budgetItems.length > 0">
          <view
            class="budget-item"
            v-for="(item, index) in budgetItems"
            :key="item.id"
            :class="{ 'item-disabled': item.status === 1 }"
          >
            <view class="item-info">
              <view class="item-top">
                <text class="item-amount">{{ formatAmount(item.totalBalance) }}</text>
                <text v-if="item.status === 1" class="status-badge">已停用</text>
              </view>
              <view class="item-tags">
                <text class="tag tag-ledger">{{ item.ledgerName || '未知账本' }}</text>
                <text class="tag tag-category">{{ item.className || '未分类' }}</text>
              </view>
            </view>
            <view class="item-actions">
              <view class="action-icon" @click.stop="navigateToEditItem(item)" title="编辑">
                <custom-icon type="bianji" :size="23" color="#007aff"></custom-icon>
              </view>
              <view
                class="action-icon"
                @click.stop="handleStatusChange(item)"
                :title="item.status === 0 ? '停用' : '启用'"
              >
                <custom-icon :type="item.status === 0 ? 'guanbi' : 'qiyong'" :size="23" :color="item.status === 0 ? '#ff4d4f' : '#52c41a'"/>
              </view>
            </view>
          </view>
        </view>
        <view class="no-data" v-else>
          暂无预算明细
          <view class="sync-last-month" @click="openSyncMonthPicker">
          同步预算
        </view>
        </view>
      </view>
    </view>

    <!-- 悬浮新增按钮 -->
    <view class="floating-add-btn" @click="navigateToAddItem">
      <text class="plus-icon">+</text>
    </view>

    <!-- 确认对话框 -->
    <uni-popup ref="confirmPopup" type="center">
      <view class="confirm-modal">
        <view class="modal-title">{{ confirmTitle }}</view>
        <view class="modal-content">{{ confirmContent }}</view>
        <view class="modal-buttons">
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

export default {
  components: {
    UniIcons
  },
  data() {
    const currentYear = new Date().getFullYear();
    const currentMonth = new Date().getMonth() + 1;
    
    // 生成年份数组
    const years = [];
    for (let year = 2020; year <= currentYear + 1; year++) {
      years.push(year);
    }
    
    // 生成月份数组
    const months = [];
    for (let month = 1; month <= 12; month++) {
      months.push(month);
    }
    
    return {
      loading: true,
      errorMsg: '',
      selectedMonth: new Date().toISOString().slice(0, 7), // 格式: YYYY-MM
      minYear: '2020',
      maxYear: (currentYear + 1).toString(),
      totalBudget: 0,
      budgetItems: [],
      // 自定义年月选择器相关
      pickerValue: [currentYear - 2020, currentMonth - 1],
      years: years,
      months: months,
      // 同步预算的年月选择器相关
      syncPickerValue: [currentYear - 2020, currentMonth - 1 > 0 ? currentMonth - 2 : 11], // 默认选择上个月
      sourceYear: '',
      sourceMonth: '',
      // 确认对话框相关
      confirmPopup: null,
      confirmTitle: '',
      confirmContent: '',
      confirmButtonText: '',
      currentItem: null,
      confirmActionType: '' // 'status' 或 'delete'
    };
  },
  onLoad() {
    this.confirmPopup = this.$refs.confirmPopup;
  },
  
  onReady() {
    // 组件挂载后初始化引用
    this.syncMonthPickerPopup = this.$refs.syncMonthPickerPopup;
  },
  
  onShow() {
    // 当页面显示时（包括从其他页面返回时）刷新预算列表
    this.loadBudgetList();
  },
  computed: {
    // 格式化金额（分→元）使用公共函数，去单位
    formatAmount() {
      return (amount) => {
        return fmtAmount(amount / 100);
      };
    }
  },
  methods: {
    // 加载预算列表
    async loadBudgetList() {
      this.loading = true;
      this.errorMsg = '';

      // 从selectedMonth中提取年份和月份
      const [year, month] = this.selectedMonth.split('-');

      try {
        // 并行加载预算信息和用户本月总支出
        const budgetPromise = request({
          url: backUrl.baseUrl + backUrl.endpoints.budget_list,
          method: 'GET',
          data: {
            year,
            month
          }
        });

        const [budgetRes] = await Promise.all([budgetPromise]);

        // 处理预算数据
        if (budgetRes && typeof budgetRes === 'object' && 'totalBalance' in budgetRes) {
          // 直接使用返回的数据（分单位）
          this.totalBudget = budgetRes.totalBalance || 0;
          this.budgetItems = []; // 预算明细需要单独获取
          
          // 如果有预算ID，获取预算明细
          if (budgetRes.id) {
            this.loadBudgetItems(budgetRes.id);
          }
        } else {
          // 接口没有返回有效数据，重置为初始状态
          this.totalBudget = 0;
          this.budgetItems = [];
        }
        this.loading = false;
      }
      catch (error) {
        this.loading = false;
        this.errorMsg = error.message || '加载预算信息失败';
        console.error('加载预算信息失败:', error);
      }
    },

    // 已删除：加载用户指定月份总支出
    
    // 加载预算明细
    loadBudgetItems(budgetId) {
      request({
        url: backUrl.baseUrl + backUrl.endpoints.budget_item_getByBudgetId.replace('/:id', '') + budgetId,
        method: 'GET'
      })
        .then((res) => {
          // 判断res是否为数组，适配后端返回的直接数据格式
          let items = [];
          if (Array.isArray(res)) {
            // 直接使用返回的数组数据
            items = res || [];
          } else if (res && typeof res === 'object') {
            // 处理可能返回单个对象的情况
            items = [res];
          }
          
          // 排序预算明细：优先展示正常状态的明细，然后展示已停用的明细
          this.budgetItems = items.sort((a, b) => {
            // status === 0 表示正常状态，应该排在前面
            // status === 1 表示已停用，应该排在后面
            return a.status - b.status;
          });
          
        })
        .catch((error) => {
          console.error('加载预算明细失败:', error);
          // 发生错误时，确保budgetItems是空数组
          this.budgetItems = [];
        });
    },
    
    // 自定义年月选择器相关方法
    openMonthPicker() {
      // 初始化选择器值为当前选中的年月
      const [year, month] = this.selectedMonth.split('-');
      const yearIndex = this.years.indexOf(parseInt(year));
      const monthIndex = parseInt(month) - 1;
      this.pickerValue = [yearIndex, monthIndex];
      
      // 打开弹窗
      this.$refs.monthPickerPopup.open();
    },
    
    closeMonthPicker() {
      this.$refs.monthPickerPopup.close();
    },
    
    onPickerChange(e) {
      this.pickerValue = e.detail.value;
    },
    
    confirmMonthSelection() {
      const selectedYear = this.years[this.pickerValue[0]];
      const selectedMonth = String(this.months[this.pickerValue[1]]).padStart(2, '0');
      this.selectedMonth = `${selectedYear}-${selectedMonth}`;
      this.closeMonthPicker();
      this.loadBudgetList();
    },
    
    // 打开同步预算的年月选择器弹窗
    openSyncMonthPicker() {
      this.syncMonthPickerPopup = this.$refs.syncMonthPickerPopup;
      this.syncMonthPickerPopup.open();
    },
    
    // 关闭同步预算的年月选择器弹窗
    closeSyncMonthPicker() {
      this.syncMonthPickerPopup.close();
    },
    
    // 同步预算年月选择器值变化
    onSyncPickerChange(e) {
      this.syncPickerValue = e.detail.value;
    },
    
    // 确认同步年月选择
    confirmSyncMonthSelection() {
      this.sourceYear = this.years[this.syncPickerValue[0]];
      this.sourceMonth = this.months[this.syncPickerValue[1]];
      this.closeSyncMonthPicker();
      // 调用同步预算方法，传递当前年月和选择的源年月
      this.syncBudgetFromSource();
    },
    
    // 同步预算方法
    syncBudgetFromSource() {
      // 显示加载提示
      uni.showLoading({ title: '同步中...' });
      
      // 从selectedMonth中提取目标年份和月份
      const [targetYear, targetMonth] = this.selectedMonth.split('-');
      
      // 构造参数：包含目标年月和源年月
      const params = {
        year: parseInt(targetYear),
        month: parseInt(targetMonth),
        sourceYear: this.sourceYear,
        sourceMonth: this.sourceMonth
      };
      
      request({
          url: backUrl.baseUrl + backUrl.endpoints.budget_syncLastMonth,
          method: 'POST',
          data: params
        }).then(res => {
        uni.hideLoading();
        if (res) {
          uni.showToast({ title: '同步成功', icon: 'success' });
          // 刷新预算列表
          this.loadBudgetList();
        } else {
          uni.showToast({ title: res.data.msg || '同步失败', icon: 'none' });
        } 
      }).catch(err => {
        uni.hideLoading();
        uni.showToast({ title: '请求失败', icon: 'none' });
        console.error('同步预算失败:', err);
      });
    },


    // 导航到添加/修改总预算页面
    navigateToAddBudget() {
      // 从selectedMonth中提取年份和月份
      const [year, month] = this.selectedMonth.split('-');
      const budgetId = this.totalBudget > 0 ? (this.budgetItems[0] ? this.budgetItems[0].budgetId : null) : null;

      uni.navigateTo({
        url: `/pages/budget/addBudget?year=${year}&month=${month}&budgetId=${budgetId || ''}`
      });
    },

    // 导航到添加明细页面
    navigateToAddItem() {
      // 从selectedMonth中提取年份和月份
      const [year, month] = this.selectedMonth.split('-');
      let budgetId = null;

      // 如果已经有预算，使用现有预算ID
      if (this.budgetItems.length > 0) {
        budgetId = this.budgetItems[0].budgetId;
      }

      uni.navigateTo({
        url: `/pages/budget/addBudgetItem?year=${year}&month=${month}&budgetId=${budgetId || ''}`
      });
    },

    // 导航到编辑明细页面
    navigateToEditItem(item) {
      uni.navigateTo({
        url: `/pages/budget/addBudgetItem?id=${item.id}&budgetId=${item.budgetId}`
      });
    },

    // 处理明细状态变更
    handleStatusChange(item) {
      this.currentItem = item;
      this.confirmTitle = item.status === 0 ? '停用预算明细' : '启用预算明细';
      this.confirmContent = item.status === 0
        ? '确定要停用此预算明细吗？'
        : '';
      this.confirmButtonText = '确定';
      this.confirmActionType = 'status';
      this.confirmPopup.open();
    },

    // 打开确认对话框
    openConfirmPopup() {
      this.confirmPopup.open();
    },

    // 关闭确认对话框
    closeConfirmPopup() {
      this.confirmPopup.close();
    },

    // 确认操作
    confirmAction() {
      if (this.confirmActionType === 'status') {
        this.updateItemStatus();
      }
      this.closeConfirmPopup();
    },

    // 更新明细状态
    updateItemStatus() {
      if (!this.currentItem) return;

      const newStatus = this.currentItem.status === 0 ? 1 : 0;

      request({
        url: backUrl.baseUrl + backUrl.endpoints.budget_item_updateStatus + '?id=' + this.currentItem.id + '&status=' + newStatus,
        method: 'POST'
      })
        .then((res) => {
          uni.showToast({
            title: '操作成功',
            icon: 'success'
          });
          // 重新加载整个预算列表数据，确保数据完全同步
          this.loadBudgetList();
        })
        .catch((error) => {
          uni.showToast({
            title: '操作失败: ' + (error.message || ''),
            icon: 'none'
          });
          console.error('更新预算明细状态失败:', error);
        });
    },
    
    // 同步上个月预算（调用后端API）
    async syncLastMonthBudget() {
      // 显示加载中提示
      uni.showLoading({
        title: '正在同步...'
      });
      
      try {
        // 从selectedMonth中提取年份和月份
        const [currentYear, currentMonth] = this.selectedMonth.split('-');
        
        // 调用后端API进行同步
        const syncRes = await request({
          url: backUrl.baseUrl + backUrl.endpoints.budget_syncLastMonth,
          method: 'POST',
          data: {
            year: parseInt(currentYear),
            month: parseInt(currentMonth)
          }
        });
        
        uni.hideLoading();
        uni.showToast({
          title: '同步成功',
          icon: 'success'
        });
        
        // 重新加载预算列表
        this.loadBudgetList();
        
      } catch (error) {
        uni.hideLoading();
        uni.showToast({
          title: '同步失败: ' + (error.message || '未知错误'),
          icon: 'none'
        });
        console.error('同步上个月预算失败:', error);
      }
    }
  }
};
</script>

<style scoped>
.budget-list-page {
  padding: 20rpx;
  box-sizing: border-box;
}

.page-header {
  text-align: center;
  padding: 20rpx 0;
  margin-bottom: 20rpx;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
}

.filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.filter-item {
  flex: 1;
  min-width: 200rpx;
}

.filter-label {
  font-size: 28rpx;
  color: #666;
  margin-right: 10rpx;
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
  color: #e64340;
  margin-bottom: 20rpx;
}

.retry-btn {
  background-color: #007aff;
  color: white;
}

.budget-list {
  gap: 20rpx;
}

.total-budget-card {
  background: linear-gradient(135deg, #007aff, #5ac8fa);
  border-radius: 15rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.15);
  color: white;
}

.budget-column {
  flex: 1;
  text-align: center;
  min-width: 120rpx;
  margin-bottom: 15rpx;
}

.column-label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 8rpx;
}

.column-value {
  font-size: 36rpx;
  font-weight: bold;
  color: white;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}

.edit-btn {
  background-color: #007aff;
  color: white;
  font-size: 28rpx;
  padding: 10rpx 20rpx;
  border-radius: 5rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.edit-icon {
  display: inline-block;
}

.budget-items-container {
  background-color: white;
  border-radius: 10rpx;
  padding: 20rpx;
}

/* 预算数据提示样式 */
.budget-tip {
  text-align: center;
  margin: 20rpx 0;
}

.tip-text {
  font-size: 24rpx;
  color: #999999;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  padding-bottom: 10rpx;
  border-bottom: 1px solid #eee;
}

.budget-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1px solid #eee;
}

.item-disabled {
  opacity: 0.5;
}

.item-info {
  flex: 1;
}

.item-top {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.item-amount {
  font-size: 32rpx;
  color: #007aff;
}

.status-badge {
  display: inline-block;
  padding: 4rpx 12rpx;
  border-radius: 999px;
  font-size: 24rpx;
  line-height: 1.4;
  color: #fff;
  background-color: #ff4d4f;
}

.item-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 8rpx;
}

.tag {
  display: inline-block;
  padding: 6rpx 14rpx;
  border-radius: 999px;
  font-size: 24rpx;
  color: #666;
  background-color: #f5f5f5;
  border: 1px solid #e6e6e6;
}

.tag-ledger {
  background-color: #e6f4ff; /* 蓝色系浅底 */
  border-color: #91caff;
  color: #1677ff;
}

.tag-category {
  background-color: #f6ffed; /* 绿色系浅底 */
  border-color: #b7eb8f;
  color: #389e0d;
}

.item-meta {
  font-size: 26rpx;
  color: #666;
  margin-top: 8rpx;
}

.item-actions {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  font-size: 28rpx;
  padding: 5rpx 15rpx;
  border-radius: 5rpx;
  display: flex;
  align-items: center;
}

.action-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10rpx;
  border: none;
  background: none;
  cursor: pointer;
}

/* 确保所有操作图标在同一行居中显示 */
.item-actions {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

/* 确保操作图标居中显示 */
.action-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44rpx;
  height: 44rpx;
  padding: 10rpx;
  border: none;
  background: none;
  cursor: pointer;
}

.total-budget-edit-icon {
  position: absolute;
  top: 15rpx;
  right: 15rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10rpx;
  border: none;
  background: none;
  cursor: pointer;
}

.total-budget-card {
  position: relative;
}

/* 自定义日期选择器样式 */
.custom-date-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15rpx 20rpx;
  border: 1px solid #ddd;
  border-radius: 8rpx;
  background-color: white;
}

.date-text {
  font-size: 28rpx;
  color: #333;
}

.popup-content {
  background-color: white;
  border-radius: 10rpx 10rpx 0 0;
  padding-bottom: 20rpx;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  border-bottom: 1px solid #eee;
}

.popup-title {
  font-size: 32rpx;
  font-weight: bold;
}

.popup-close {
  font-size: 28rpx;
  color: #666;
}

.month-picker-container {
  padding: 0 20rpx;
}

.popup-footer {
  padding: 20rpx 0;
}

.confirm-btn {
  background-color: #007aff;
  color: white;
  border-radius: 8rpx;
}

.edit-icon {
  margin-right: 5rpx;
  display: inline-block; /* 确保图标正确显示 */
}

.edit-btn {
  background-color: #007aff;
  color: white;
}

.status-btn {
  background-color: #f8f8f8;
  color: #666;
}

.status-active {
  background-color: #e64340;
  color: white;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 40rpx 0;
}

.sync-last-month {
  margin-top: 20rpx;
  color: #007aff;
  font-size: 28rpx;
  text-decoration: underline;
  cursor: pointer;
}

.floating-add-btn {
  position: fixed;
  bottom: 40rpx;
  right: 40rpx;
  width: 100rpx;
  height: 100rpx;
  background-color: #007aff;
  color: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 60rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.2);
  z-index: 999;
}

.confirm-modal {
  background-color: white;
  border-radius: 10rpx;
  padding: 30rpx;
  max-width: 500rpx;
}

.modal-title {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  text-align: center;
}

.modal-content {
  font-size: 30rpx;
  color: #666;
  margin-bottom: 30rpx;
  text-align: center;
}

.modal-buttons {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.modal-btn {
  flex: 1;
  font-size: 30rpx;
  padding: 15rpx 0;
  border-radius: 24px;
}

.cancel-btn {
  background-color: #f8f8f8;
  color: #666;
}

.confirm-btn {
  background-color: #007aff;
  color: white;
}
</style>