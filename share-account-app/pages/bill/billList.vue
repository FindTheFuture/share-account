<template>
  <view class="bill-list-page">
    <!-- 顶部功能栏 -->
    <view class="top-toolbar">
    
      <!-- 第一行：主要筛选标签 -->
      <view class="toolbar-row">
        <view class="toolbar-item ledger-tag" :class="{ selected: selectedLedger || (shareMemberBillIds && shareMemberBillIds.length > 0) }" @click.stop="showLedgerPopup">
          <text>{{ (shareMemberBillIds && shareMemberBillIds.length > 0) ? '共享账单' : (selectedLedger?.name || '账本') }}</text>
        </view>
        
        <view class="toolbar-item account-tag" :class="{ selected: selectedAccount }" @click.stop="showAccountPopup">
          <text>{{ selectedAccount?.name || '账户' }}</text>
        </view>
        
        <view class="toolbar-item category-tag" :class="{ selected: selectedCategory }" @click.stop="showCategoryPopup">
          <text>{{ selectedCategory?.name || '分类' }}</text>
        </view>
      </view>
    
      <!-- 第二行：次要筛选标签 -->
      <view class="toolbar-row">
        <view class="toolbar-item budget-tag" :class="{ selected: selectedBudgetIndex !== 0 }" @click.stop="showBudgetPopup">
          <text>{{ budgetOptions[selectedBudgetIndex].label }}</text>
        </view>
        
        <view class="toolbar-item date-tag" :class="{ selected: dateRange && dateRange.length > 0 }" @click.stop="showDateRangePicker">
          <text>{{ dateRangeText || '时间' }}</text>
        </view>
        
        <view class="toolbar-item status-tag" :class="{ selected: selectedStatusIndex > -1 }" @click.stop="showStatusPopup">
          <text>{{ statusOptions[selectedStatusIndex].label }}</text>
        </view>
        
        <!-- 排序标签 -->
        <view class="toolbar-item sort-tag" :class="{ selected: selectedSortOption > 0}" @click.stop="showSortPopup">
          <text>{{ currentSortLabel }}</text>
        </view>
      </view>
    
      <!-- 第三行：操作按钮 -->
      <view class="toolbar-row">
        <!-- 重置按钮 -->
        <view class="toolbar-item reset-button" @click.stop="resetAllFilters">
          <text>重置</text>
        </view>
        
        <!-- 导出按钮 -->
        <view class="toolbar-item export-button" @click.stop="exportBills">
          <text>导出</text>
        </view>
      </view>
    </view>

    <!-- 预算状态弹出层 -->
    <uni-popup ref="budgetPopup" type="center" :mask-click="true" @close="handlePopupClose('budgetPopup')">
      <view class="popup-container">
        <view class="popup-header">
          <text class="popup-title">预算状态</text>
          <view class="popup-actions">
            <custom-icon type="guanbi" :size="20" color="#999" @tap.stop="closeBudgetPopup"></custom-icon>
          </view>
        </view>
        <view class="popup-content">
          <view class="budget-options">
            <view 
              v-for="(option, index) in budgetOptions" 
              :key="index"
              class="budget-option-item"
              :class="{ 'selected': selectedBudgetIndex === index }"
              @tap.stop="selectBudget(index)"
            >
              <text class="budget-option-text">{{ option.label }}</text>
            </view>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 账本弹出层 - 替换为可复用组件 -->
    <LedgerSelectPopup
      ref="ledgerPopup"
      :selectedLedger="selectedLedger"
      :autoSelectDefault="false"
      :showShareBillAction="true"
      @select="selectLedger"
      @share-all-bills="selectShareBill"
    />

    <!-- 账户弹出层 -->
    <account-select-popup
      ref="accountPopup"
      :selectedAccount="selectedAccount"
      :autoSelectDefault="false"
      @select="selectAccount"
    />

    <!-- 分类选择弹窗 -->
    <uni-popup ref="categoryPopup" type="bottom" :custom="true" :mask-click="false" @maskClick="onCategoryMaskClick" @close="handlePopupClose('categoryPopup')">
      <view class="category-popup">
        
        <!-- 分类列表改为复用组件 -->
        <view class="popup-content">
          <category-select
            ref="categorySelect"
            :selectedCategory="selectedCategory"
            @select="selectCategory"
             @child-popup-open="onCategoryChildOpen"
             @child-popup-close="onCategoryChildClose"
          />
        </view>
        </view>
      </uni-popup>

    <!-- 日期范围选择器 -->
    <uni-popup ref="dateRangePopup" type="bottom" :mask-click="!isDatePickerOpen" @close="handlePopupClose('dateRangePopup')">
      <view class="date-popup-container">
        <view class="popup-content">
          <uni-datetime-picker 
            v-model="dateRange" 
            type="daterange" 
            @show="onDatePickerShow" 
            @maskClick="onDatePickerMaskClick" 
            @change="onDatePickerChange"
          />
          <view class="popup-footer">
            <button class="confirm-btn" @click="confirmDateRangeSelection">确定</button>
          </view>
        </view>
      </view>
    </uni-popup>


    <!-- 状态筛选弹出层 -->
    <uni-popup ref="statusPopup" type="center" :mask-click="true" @close="handlePopupClose('statusPopup')">
      <view class="popup-container">
        <view class="popup-header">
          <view class="title-with-icon">
            <text class="popup-title">选择状态</text>
            <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click.stop="onStatusTipClick" />
          </view>
          <view class="popup-actions">
            <custom-icon type="guanbi" :size="20" color="#999" @tap.stop="closeStatusPopup"></custom-icon>
          </view>
        </view>
        <view class="popup-content">
          <view class="budget-options">
            <view 
              v-for="(option, index) in statusOptions" 
              :key="index"
              class="budget-option-item"
              :class="{ 'selected': selectedStatusIndex === index }"
              @tap.stop="selectStatus(index)"
            >
              <text class="budget-option-text">{{ option.label }}</text>
            </view>
          </view>
        </view>
      </view>
    </uni-popup>
    
    <!-- 排序选择弹出层 -->
    <uni-popup ref="sortPopup" type="center" :mask-click="true" @close="handlePopupClose('sortPopup')">
      <view class="popup-container">
        <view class="popup-header">
          <text class="popup-title">选择排序方式</text>
          <view class="popup-actions">
            <custom-icon type="guanbi" :size="20" color="#999" @tap.stop="closeSortPopup"></custom-icon>
          </view>
        </view>
        <view class="popup-content">
          <view class="budget-options">
            <view 
              v-for="option in sortOptions" 
              :key="option.id"
              class="budget-option-item"
              :class="{ 'selected': selectedSortOption === option.id }"
              @tap.stop="selectSort(option.id)"
            >
              <text class="budget-option-text">{{ option.label }}</text>
            </view>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 账单列表内容区域 -->
    <view class="bill-list-content">
      <!-- 使用可复用的账单列表组件 -->
      <bill-list-component
        ref="billListComponent"
        v-show="hasInitialized"
        :request-params="billListParams"
        :show-statistics="true"
        :show-pagination="true"
        :current-user-id="currentUserId"
        :scroll-to-top-on-page-change="false"
        @refresh-list="handleRefreshList"
      ></bill-list-component>
      <!-- 错误提示 -->
      <view class="error-container" v-if="errorMsg">
        <text class="error-text">{{ errorMsg }}</text>
      </view>
    </view>

  </view>
</template>

<script>
import request from '@/common/request.js';
import backUrl from '@/common/back_url.js';
import { formatAmount as fmtAmount, normalizeBillQueryParams, validateBillQueryParams } from '@/common/util.js';
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import UniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
import UniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue';
import UniCollapse from '@/uni_modules/uni-collapse/components/uni-collapse/uni-collapse.vue';
import UniCollapseItem from '@/uni_modules/uni-collapse/components/uni-collapse-item/uni-collapse-item.vue';
import UniGrid from '@/uni_modules/uni-grid/components/uni-grid/uni-grid.vue';
import UniGridItem from '@/uni_modules/uni-grid/components/uni-grid-item/uni-grid-item.vue';
import UniDateTimePicker from '@/uni_modules/uni-datetime-picker/components/uni-datetime-picker/uni-datetime-picker.vue';
import UniSegmentedControl from '@/uni_modules/uni-segmented-control/components/uni-segmented-control/uni-segmented-control.vue';
import UniPagination from '@/uni_modules/uni-pagination/components/uni-pagination/uni-pagination.vue';
import BillListComponent from '@/components/bill-list-component.vue';
import AccountSelectPopup from '@/components/account-select-popup.vue';
import LedgerSelectPopup from '@/components/ledger-select-popup.vue';
import CategorySelect from '@/components/category-select.vue';

export default {
  components: {
    UniIcons,
    UniPopup,
    UniLoadMore,
    UniCollapse,
    UniSegmentedControl,
    UniCollapseItem,
    UniGrid,
    UniGridItem,
    UniDateTimePicker,
    UniPagination,
    BillListComponent,
    AccountSelectPopup,
    LedgerSelectPopup,
    CategorySelect
  },
  data() {
    const currentDate = new Date();
    const currentYear = currentDate.getFullYear();
    const currentMonth = currentDate.getMonth() + 1;
    const currentDay = currentDate.getDate();
    
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
    
    // 生成日期数组
    const days = [];
    for (let day = 1; day <= 31; day++) {
      days.push(day);
    }
    
    // 设置默认日期范围（当前时间之前的30天）
    const now = new Date();
    
    // 结束日期：今天
    const endDate = new Date(now);
    
    // 开始日期：30天前
    const startDate = new Date(now);
    startDate.setDate(startDate.getDate() - 30);
    
    // uni-datetime-picker的daterange类型需要数组格式
    const defaultDateRange = [startDate, endDate];

    return {
      // 弹窗管理相关 - 按照addBill.vue模式实现
      // 账本相关
      selectedLedger: null,

      
      // 账户相关
      selectedAccount: null,
      
      // 分类相关
      loadingCategories: false,
      topCategories: [], // 顶层分类
      middleCategories: [], // 中层分类
      leafCategories: [], // 底层分类
      currentTopCategoryIndex: 0,
      currentMiddleCategoryId: null,
      selectedCategory: null,
      // 弹窗相关
      categoryPopup: null,
      // 子级分类弹窗打开状态（用于控制遮罩点击逐级关闭）
      isCategoryChildOpen: false,
      // 手风琴组件绑定值，控制默认闭合状态
      accordionVal: '',
      
      // 预算状态选项
      budgetOptions: [
        { label: '未选预算', value: -1 },
        { label: '计入预算', value: 0 },
        { label: '不计入预算', value: 1 }
      ],
      selectedBudgetIndex: 0,
      
      // 日期范围选择
      dateRange: defaultDateRange,

      
      // 状态筛选
      statusOptions: [
        //{ label: '状态', value: -1 },
        { label: '正常', value: 0 },
        { label: '已冻结', value: 1 }
      ],
      selectedStatusIndex: 0,
      
      // 账单列表
      loading: false,
      errorMsg: '',
      
      // 账单列表组件参数
      billListParams: {
        pageNum: 1,
        pageSize: 10
      },
      currentUserId: '',
      // 分享相关
      shareInfo: null,
      // 分享成员ID
      shareMemberId: null,
      shareMemberBillIds: null,
      // 首屏 & 返回刷新控制
      hasInitialized: true,
      shouldRefreshOnShow: false,
      // 日期插件显隐状态，用于控制弹窗遮罩点击
      isDatePickerOpen: false,
      // 日期范围显示文本
      dateRangeText: '',
      // 排序相关
      sortOptions: [
        { id: 1, label: '时间:近->远' },
        { id: 2, label: '时间:远->近' },
        { id: 3, label: '金额:大->小' },
        { id: 4, label: '金额:小->大' }
      ],
      selectedSortOption: 1,
      sortPopupVisible: false,
      // 游客模式标记
      isGuest: false
    };
  },
  
  onLoad() {
    // 页面加载时初始化数据
    this.initData();
    this.currentUserId = uni.getStorageSync('additionalId') || '';
    // 初始化日期范围显示文本
    this.updateDateRangeText();
  },
  
  onShow() {
    // 从编辑/新增页面返回时按需刷新账单列表，首屏不重复加载
    this.loading = false;
    this.hasMore = true;
    // 检查游客模式状态
    const guestFlag = uni.getStorageSync('isGuest');
    this.isGuest = guestFlag === true || guestFlag === 'true';

    const shouldRefresh = !!uni.getStorageSync('refreshBillListOnShow');
    if (shouldRefresh || this.shouldRefreshOnShow) {
      this.loadBillList();
      this.shouldRefreshOnShow = false;
      try { uni.removeStorageSync('refreshBillListOnShow'); } catch (e) {}
    }
    // 组件内一次性刷新账本列表（返回自账本页面）
    if (this.$refs.ledgerPopup && this.$refs.ledgerPopup.refreshIfNeeded) {
      this.$refs.ledgerPopup.refreshIfNeeded();
    if (this.$refs.categorySelect && this.$refs.categorySelect.refreshIfNeeded) {
      this.$refs.categorySelect.refreshIfNeeded();
    }
    }
  },
  
  onReady() {
      // 无需初始化popups对象，直接使用$refs访问弹窗实例
    },
  
  beforeDestroy() {
  },

  // 分享生命周期函数
  onShareAppMessage(res) {
    // 仅当来源为按钮（弹窗分享按钮）时，按账单分享
    if (res && res.from === 'button') {
      if (this.$refs.billListComponent && typeof this.$refs.billListComponent.getShareInfo === 'function') {
        try {
          // 强制账单分享（组件内部已在按钮点击时保存shareMemberId）
          return this.$refs.billListComponent.getShareInfo(true);
        } catch (error) {
          console.error('组件分享信息获取失败:', error);
        }
      }
    }
    
    // 兜底的默认分享信息（右上角菜单、非按钮触发）
    return {
      title: '分享给你一个账本',
      path: '/pages/firstpage/firstpage',
      imageUrl: 'https://shareaccount-1302778096.cos.ap-beijing.myqcloud.com/title.png'
    };
  },
  // 朋友圈分享钩子（微信小程序）
  onShareTimeline() {
    const title = '分享给你一个账本';
    const imageUrl = 'https://shareaccount-1302778096.cos.ap-beijing.myqcloud.com/title.png';
    return {
      title,
      query: '',
      imageUrl
    };
  },
  
  computed: {
    // 金额文本格式（分->元，仅格式化文本，不带单位）
    formatAmount() {
      return (fen) => {
        return fmtAmount(fen / 100);
      };
    },
    // 当前选中的排序选项标签
    currentSortLabel() {
      const option = this.sortOptions.find(opt => opt.id === this.selectedSortOption);
      return option ? option.label : '排序';
    }
  },
  
  methods: {
    // 导出账单为PDF
    async exportBills() {
      // 游客模式验证
      if (this.isGuest) {
        uni.showModal({
          title: '提示',
          content: '请登录后导出账单',
          showCancel: false,
          success: (res) => {
            if (res.confirm) {
              uni.navigateTo({
                url: '/pages/login/login'
              });
            }
          }
        });
        return;
      }
      
      // 检查时间范围是否超过3个月
      const startDate = this.dateRange[0];
      const endDate = this.dateRange[1];
      const diffMonths = (endDate.getFullYear() - startDate.getFullYear()) * 12 + (endDate.getMonth() - startDate.getMonth());
      
      if (diffMonths > 3) {
        uni.showToast({
          title: '导出时间范围不能超过3个月',
          icon: 'none',
          duration: 2000
        });
        return;
      }

      // 显示加载提示
      uni.showLoading({
        title: '检查中',
        mask: true
      });
      
      // 检查PDF导出次数是否足够
      const remainingCountRes = await this.$request({
        url: backUrl.endpoints.userMember_getRemainingPdfCount,
        method: 'GET',
      });
      uni.hideLoading();
      
      // 检查返回结果
      if (remainingCountRes.code && remainingCountRes.code != 200) {
        console.log('获取PDF导出剩余次数失败:', remainingCountRes.message);
        uni.showToast({ 
          title: '获取 PDF导出剩余次数失败', 
          icon: 'none' 
        });
        return;
      }
      
      const remainingPdfCount = remainingCountRes || 0;
      
      // 如果剩余次数小于等于0，提示用户并跳转
      if (remainingPdfCount <= 0) {
        uni.showModal({
          title: 'PDF导出次数已用完',
          content: '您的PDF导出次数已用完，请订阅会员获取更多次数',
          showCancel: true,
          success: (res) => {
            if (res.confirm) {
              // 跳转到会员页面
              uni.navigateTo({
                url: '/pages/vipmember/vipmember'
              });
            }
          }
        });
        return;
      }
      
      // 准备导出参数
      const exportParams = {
        startTime: this.formatDate(startDate),
        endTime: this.formatDate(endDate)
      };
      
      // 添加账本筛选
      if (this.selectedLedger) {
        exportParams.ledgerId = this.selectedLedger.id;
      }
      
      // 添加账户筛选
      if (this.selectedAccount) {
        exportParams.accountId = this.selectedAccount.id;
      }
      
      // 添加分类筛选
      if (this.selectedCategory) {
        exportParams.categoryId = this.selectedCategory.id;
      }
      
      // 添加预算状态筛选
      if (this.selectedBudgetIndex !== 0) {
        exportParams.isBudget = this.budgetOptions[this.selectedBudgetIndex].value;
      }
      
      // 添加状态筛选
      if (this.selectedStatusIndex !== 0) {
        exportParams.status = this.statusOptions[this.selectedStatusIndex].value;
      }
      
      // 添加分享账单ID（如果是共享账单）
      if (this.shareMemberBillIds && this.shareMemberBillIds.length > 0) {
        exportParams.billIds = this.shareMemberBillIds;
      }
      
      // 显示加载中
      uni.showLoading({
        title: '导出中...',
        mask: true
      });
      
      // 调用导出API
      request.post(backUrl.endpoints.bill_export, exportParams).then(res => {
        uni.hideLoading();
        
        if (res && res.fileUrl) {
          // 导出成功，显示下载链接
          uni.showModal({
            title: '导出成功',
            content: '账单已成功导出为PDF文件，是否立即下载？',
            success: (modalRes) => {
              if (modalRes.confirm) {
                // 打开下载链接
                uni.downloadFile({
                  url: res.fileUrl,
                  success: (downloadRes) => {
                    if (downloadRes.statusCode === 200) {
                      // 打开文件
                      uni.openDocument({
                        filePath: downloadRes.tempFilePath,
                        showMenu: true,
                        success: () => {
                          console.log('打开PDF文件成功');
                        },
                        fail: (err) => {
                          uni.showToast({
                            title: '打开PDF失败',
                            icon: 'none'
                          });
                        }
                      });
                    }
                  },
                  fail: (err) => {
                    uni.showToast({
                      title: '下载失败',
                      icon: 'none'
                    });
                  }
                });
              }
            }
          });
        } else {
          uni.showToast({
            title: res.message || '导出失败',
            icon: 'none',
            duration: 2000
          });
        }
      }).catch(err => {
        uni.hideLoading();
        
        // 检查是否是频率限制错误
        if (err.message && err.message.includes('频率限制')) {
          uni.showToast({
            title: '导出太频繁，请30分钟后重试',
            icon: 'none',
            duration: 3000
          });
        } else {
          uni.showToast({
            title: '网络错误，导出失败',
            icon: 'none',
            duration: 2000
          });
        }
      });
    },
    
    // 日期格式化
    formatDate: function(date) {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    
    // 关闭所有其他弹窗
    closeAllOtherPopups: function(excludePopupId) {
      const popupIds = ['ledgerPopup', 'accountPopup', 'categoryPopup', 'dateRangePopup', 'statusPopup', 'budgetPopup', 'sortPopup'];
      
      popupIds.forEach(id => {
        if (id !== excludePopupId) {
          const popupInstance = this.$refs[id];
          if (popupInstance && typeof popupInstance.close === 'function') {
            try {
              popupInstance.close();
            } catch (e) {
              console.error(`关闭弹窗${id}时出错:`, e);
            }
          }
        }
      });
      
      // 额外：关闭分类选择组件内部的子弹窗（如有）
      if (excludePopupId !== 'categoryPopup' && this.$refs.categorySelect && typeof this.$refs.categorySelect.closePopup === 'function') {
        try {
          this.$refs.categorySelect.closePopup();
        } catch (e) {
          console.error('关闭分类选择组件子弹窗时出错:', e);
        }
      }
    },
    
    // 处理弹窗关闭事件（包括通过遮罩层点击关闭）
    handlePopupClose: function(popupId) {
    },
    
    // 处理子组件刷新事件（分页、删除、启用等）
    handleRefreshList: function(newParams) {
      if (newParams && typeof newParams === 'object' && 'pageNum' in newParams) {
        // 分页事件：使用子组件传回的新参数，更新页码并触发列表刷新
        this.billListParams = newParams;
      } else {
        // 其他刷新事件（如删除/启用）：保持当前筛选条件，触发子组件重新加载
        this.billListParams = { ...this.billListParams };
      }
    },
    
    // 格式化账单时间
    formatBillTime(timeString, showFull = true) {
      const date = new Date(timeString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
    },
    
    // 格式化时间
    formatTime(timeString) {
      if (!timeString) return '';
      const date = new Date(timeString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },
    

    

    
    // 选择共享账单（迁移后：直接从后端获取共享账单ID）
    selectShareBill() {
      this.loading = true;
      request({
        url: backUrl.endpoints.member_getNormalByUser,
        method: 'GET',
        data: {},
        loading: true
      }).then(res => {
        this.loading = false;
        const list = Array.isArray(res) ? res : [];
        const filtered = list.filter(item => item.billId && item.billId !== null && item.billId !== '');
        const ids = Array.from(new Set(filtered.map(i => i.billId)));
        this.shareMemberBillIds = ids;
        // 清除其他选项的状态
        this.selectedLedger = null;
        // 重新加载账单列表
        this.hasMore = true;
        this.loadBillList();
      }).catch(err => {
        this.loading = false;
        console.error('加载共享账单ID失败:', err);
        // 加载失败时仍刷新列表但不带共享账单过滤
        this.shareMemberBillIds = [];
        this.hasMore = true;
        this.loadBillList();
      });
    },
    
    // 初始化数据
    async initData() {
      try {

        
        // 默认查询这一个月内的账单，不区分账本，不区分账户，包含所有状态
        this.setDefaultDateRange();
        // 不默认选择任何账本，确保查询所有账本的账单
        this.selectedLedger = null;
        // 确保状态选择为全部（包括正常和已删除）
        this.selectedStatusIndex = 0; 
        
        // 加载账单列表
        this.loadBillList();
        // 标记初始化完成，避免 onShow 首屏重复刷新
        this.hasInitialized = true;
      } catch (error) {
        console.error('初始化数据失败:', error);
        this.errorMsg = '数据加载失败，请重试';
      }
    },
    
    // 设置默认日期范围（本月第一天到最后一天）
    setDefaultDateRange() {
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth();
      
      // 开始日期：本月第一天
      const startDate = new Date(currentYear, currentMonth, 1);
      
      // 结束日期：本月最后一天
      const endDate = new Date(currentYear, currentMonth + 1, 0);
      
      // 设置为数组格式，与uni-datetime-picker组件的daterange类型要求一致
      this.dateRange = [startDate, endDate];
      
      // 更新日期范围显示文本
      this.updateDateRangeText();
    },
    
    // 更新日期范围显示文本
    updateDateRangeText() {
      if (!this.dateRange || !Array.isArray(this.dateRange) || this.dateRange.length !== 2) {
        this.dateRangeText = '时间';
        return;
      }
      
      const startDate = new Date(this.dateRange[0]);
      const endDate = new Date(this.dateRange[1]);
      
      if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
        this.dateRangeText = '时间';
        return;
      }
      
      // 格式化日期为 yyyy-MM-dd
      const formatDate = (date) => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${month}/${day}`;
      };
      
      this.dateRangeText = `${formatDate(startDate)}-${formatDate(endDate)}`;
    },
    
    // 获取指定月份的天数
    getDaysInMonth(year, month) {
      return new Date(year, month, 0).getDate();
    },
    
    // 加载正常状态的分类
    loadCategories() {
      return new Promise((resolve, reject) => {
        this.loadingCategories = true;
        // 加载分类树结构，参考addBudgetItem.vue的实现
        request({
          url: backUrl.endpoints.class_getAll,
          method: 'GET',
          loading: true
        }).then(res => {
          this.loadingCategories = false;
          if (Array.isArray(res)) {
            let categoryData = res;
            
            // 过滤出真正的顶级分类（parentId为null的分类），参考addBill.vue的实现
            const topLevelCategories = categoryData.filter(cat => cat.parentId === null);
            
            if (topLevelCategories.length > 0) {
              this.topCategories = topLevelCategories;
              this.updateCategoryTree();
            } else {
              // 如果没有找到真正的顶级分类，就使用所有分类
              this.topCategories = categoryData;
              this.updateCategoryTree();
            }
            resolve(res);
          } else {
            uni.showToast({
              title: '加载分类失败',
              icon: 'none'
            });
            reject(new Error('加载分类失败'));
          }
        }).catch(err => {
          this.loadingCategories = false;
          console.error('加载分类失败:', err);
          uni.showToast({
            title: '加载分类失败',
            icon: 'none'
          });
          reject(err);
        });
      });
    },

    // 更新分类树，设置中层和底层分类
    updateCategoryTree() {
      if (this.topCategories.length > 0) {
        const topCategory = this.topCategories[this.currentTopCategoryIndex];
        
        // 获取中层分类数据并确保是数组
        let childCategories = [];
        if (topCategory.childClassList && Array.isArray(topCategory.childClassList)) {
          childCategories = topCategory.childClassList;
        } else if (topCategory.children && Array.isArray(topCategory.children)) {
          childCategories = topCategory.children;
        } else if (topCategory.subClasses && Array.isArray(topCategory.subClasses)) {
          childCategories = topCategory.subClasses;
        } else {
          childCategories = [];
        }
        
        // 设置中层分类并预处理子分类数据
        this.middleCategories = childCategories.map(middleCat => {
          const catCopy = {...middleCat};
          
          // 统一子分类数据结构，优先使用childClassList
          let subCategories = [];
          if (catCopy.childClassList && Array.isArray(catCopy.childClassList)) {
            subCategories = catCopy.childClassList;
          } else if (catCopy.children && Array.isArray(catCopy.children)) {
            subCategories = catCopy.children;
          } else if (catCopy.subClasses && Array.isArray(catCopy.subClasses)) {
            subCategories = catCopy.subClasses;
          }
          
          // 确保catCopy.childClassList始终存在且为数组
          catCopy.childClassList = [...subCategories];
          
          return catCopy;
        });
        
        // 清理leafCategories
        this.leafCategories = [];
        
        // 不设置默认选中的中层分类，保持手风琴默认闭合
        if (this.middleCategories.length > 0) {
          // 不自动选中第一个分类，保持手风琴默认闭合
          // this.currentMiddleCategoryId = this.middleCategories[0].id;
        } else {
          // 如果没有中层分类，检查是否有直接的底层分类
          this.currentMiddleCategoryId = null;
          this.leafCategories = [...childCategories];
        }
      } else {
        this.middleCategories = [];
        this.leafCategories = [];
        this.currentMiddleCategoryId = null;
      }
    },
    
    // 重置所有筛选条件
    // 清除分类选择
    clearCategorySelection: function() {
      this.selectedCategory = null;
      this.closeCategoryPopup();
      // 重新加载账单列表
      this.hasMore = true;
      this.loadBillList();
    },
    
    resetAllFilters: function() {
      // 重置账本选择
      this.selectedLedger = null;
      
      // 重置账户选择
      this.selectedAccount = null;
      
      // 重置分类选择
      this.selectedCategory = null;
      
      // 重置预算状态为全部
      this.selectedBudgetIndex = 0;
      
      // 重置日期范围为默认的30天
      this.setDefaultDateRange();
      // 更新日期范围显示文本
      this.updateDateRangeText();
      
      // 重置共享账单
      this.shareMemberBillIds = [];
      
      // 重置状态筛选为"全部"
      this.selectedStatusIndex = 0;
      
      // 重置排序选项为默认值
      this.selectedSortOption = 1;
      
      // 清空错误提示
      this.errorMsg = '';
      
      // 重新构建查询参数并刷新列表，确保带上默认的时间范围
      this.loadBillList();
    },
    

    
    // 加载账单列表 - 处理传入的参数或重新构建参数
    loadBillList: function(params) {
      // 如果传入了参数（通常来自分页变更），也做校验与规范化（统一调用公共方法）
      if (params && typeof params === 'object') {
        const normalized = normalizeBillQueryParams(params);
        const v = validateBillQueryParams(normalized);
        if (!v.ok) {
          this.errorMsg = v.errorMsg;
          uni.showToast({ title: this.errorMsg, icon: 'none' });
          return;
        }
        this.billListParams = normalized;
        return;
      }
      
      // 否则重新构建查询参数，包含账本、账户、成员和状态筛选
      const newParams = {
        pageNum: 1,
        pageSize: 10
      };

      // 确保个人账本、共享账本和共享账单参数互斥，只能赋值一个
      // 优先级顺序：共享账单 > 共享账本 > 个人账本
      if (this.shareMemberBillIds && this.shareMemberBillIds.length > 0) {
        // 优先使用共享账单参数，删除冲突的 ledgerId
        newParams.billIds = this.shareMemberBillIds;
        delete newParams.ledgerId;
      } else if (this.selectedLedger && this.selectedLedger.id) {
        // 使用个人账本参数，删除冲突的 billIds
        newParams.ledgerId = this.selectedLedger.id;
        delete newParams.billIds;
      } else {
        // 两者都未选择时，确保都不传
        delete newParams.billIds;
        delete newParams.ledgerId;
      }
      
      // 添加账户筛选条件（非必填）
      if (this.selectedAccount && this.selectedAccount.id) {
        newParams.accountId = this.selectedAccount.id;
      }
      
      // 添加状态筛选条件（非必填，当状态=全部时不加该参数）
      if (this.selectedStatusIndex !== undefined && this.statusOptions[this.selectedStatusIndex] && this.statusOptions[this.selectedStatusIndex].value !== -1) {
        newParams.status = this.statusOptions[this.selectedStatusIndex].value;
      }
      
      // 添加分类筛选条件（非必填）
      if (this.selectedCategory && this.selectedCategory.id) {
        newParams.categoryId = this.selectedCategory.id;
      }
      
      // 添加预算状态筛选条件（非必填，当选择全部时不传该参数）
      if (this.budgetOptions[this.selectedBudgetIndex] && this.budgetOptions[this.selectedBudgetIndex].value !== -1) {
        newParams.isBudget = this.budgetOptions[this.selectedBudgetIndex].value;
      }
      
      
      // 处理日期范围筛选（统一交给公共方法转换与校验）
      newParams.dateRange = this.dateRange;
      
      // 添加排序参数
      newParams.sortBy = this.selectedSortOption;
      
      const normalized = normalizeBillQueryParams(newParams);
      const v = validateBillQueryParams(normalized);
      if (!v.ok) {
        this.errorMsg = v.errorMsg;
        uni.showToast({ title: this.errorMsg, icon: 'none' });
        return;
      }
      
      // 更新组件参数，触发组件内部的数据加载逻辑
      this.billListParams = normalized;
    },
    
    // 账本相关方法
    showLedgerPopup: function() {
      this.closeAllOtherPopups('ledgerPopup');
      // 直接使用$refs访问弹窗实例并打开
      if (this.$refs.ledgerPopup) {
        this.$refs.ledgerPopup.open();
      }
    },
    
    closeLedgerPopup: function() {
      if (this.$refs.ledgerPopup) {
        this.$refs.ledgerPopup.close();
      }
    },
    
    selectLedger: function(ledger) {
      this.selectedLedger = ledger;
      this.closeLedgerPopup();
      // 清除其他选项的状态（互斥：选择账本时清空共享账单）
      this.shareMemberBillIds = [];
      // 更新组件参数并重置页码
      const newParams = {
        ...this.billListParams,
        pageNum: 1
      };
      if (ledger && ledger.id) {
        newParams.ledgerId = ledger.id;
      } else {
        delete newParams.ledgerId;
      }
      // 互斥保障：选择账本时删除 billIds
      delete newParams.billIds;
      this.billListParams = newParams;
    },
    
    navigateToLedgerPage: function() {
      this.closeLedgerPopup();
      uni.navigateTo({
        url: '/pages/ledger/ledger'
      });
    },
    
    // 账户相关方法
    showAccountPopup: function() {
      // 关闭所有其他弹窗
      this.closeAllOtherPopups('accountPopup');
      // 直接使用$refs访问弹窗实例并打开
      if (this.$refs.accountPopup) {
        this.$refs.accountPopup.open();
      }
    },
    
    closeAccountPopup: function() {
      if (this.$refs.accountPopup) {
        this.$refs.accountPopup.close();
      }
    },
    
    selectAccount: function(account) {
      this.selectedAccount = account;
      this.closeAccountPopup();
      // 更新组件参数并重置页码
      const newParams = {
        ...this.billListParams,
        pageNum: 1
      };
      if (account && account.id) {
        newParams.accountId = account.id;
      } else {
        delete newParams.accountId;
      }
      this.billListParams = newParams;
    },
    
    // 清除账户选择
    clearAccountSelection: function() {
      this.selectedAccount = null;
      this.closeAccountPopup();
      // 更新组件参数并重置页码
      const newParams = { ...this.billListParams, pageNum: 1 };
      delete newParams.accountId;
      this.billListParams = newParams;
    },
    
    // 清除账本选择
    clearLedgerSelection: function() {
      this.selectedLedger = null;
      // 同时清空“共享账单”选择，确保互斥
      this.shareMemberBillIds = [];
      this.closeLedgerPopup();
      // 更新组件参数并重置页码
      const newParams = { ...this.billListParams, pageNum: 1 };
      delete newParams.ledgerId;
      delete newParams.billIds;
      this.billListParams = newParams;
    },
    
    navigateToAccountPage: function() {
      this.closeAccountPopup();
      uni.navigateTo({
        url: '/pages/account/accountList'
      });
    },
    

    
    // 获取账户类型样式类
    getAccountTypeClass: function(type) {
      const classMap = {
        0: 'type-savings',
        1: 'type-credit',
        2: 'type-recharge',
        3: 'type-invest'
      };
      return classMap[type] || 'type-savings';
    },
    
    // 分类弹窗相关方法
    showCategoryPopup: function() {
      // 关闭所有其他弹窗
      this.closeAllOtherPopups('categoryPopup');
      // 直接使用$refs访问弹窗实例并打开
      if (this.$refs.categoryPopup) {
        this.$refs.categoryPopup.open();
        // 弹窗打开后，通知子组件重新测量宫格，避免宽度为0导致堆叠
        this.$nextTick(() => {
          if (this.$refs.categorySelect && typeof this.$refs.categorySelect.onPopupOpened === 'function') {
            this.$refs.categorySelect.onPopupOpened();
          }
        });
      }
    },
    
    // 分类弹窗遮罩点击：子弹窗打开时不做任何动作，未打开时关闭父弹窗
    onCategoryMaskClick: function() {
      if (this.isCategoryChildOpen) {
        // 子级弹窗打开时，忽略外层遮罩点击
        return;
      }
      // 仅在没有子弹窗时，允许外层遮罩关闭父弹窗
      this.closeCategoryPopup();
    },
    // 子级分类弹窗打开/关闭事件处理
    onCategoryChildOpen: function() {
      this.isCategoryChildOpen = true;
    },
    onCategoryChildClose: function() {
      this.isCategoryChildOpen = false;
    },
    
    // 分类顶部分段器点击事件
    onTopCategoryClick(e) {
      const index = e.currentIndex;
      this.currentTopCategoryIndex = index;
      
      // 调用updateCategoryTree方法更新子分类数据
      // 这个方法会处理不同格式的子分类数据并正确设置中层分类和底层分类
      this.updateCategoryTree();
    },
    
    closeCategoryPopup: function() {
      if (this.$refs.categoryPopup) {
        this.$refs.categoryPopup.close();
      }
    },
    
    // 中层分类切换
    onMiddleCategoryChange: function(categoryId) {
      // 查找对应中层分类
      const middleCategory = this.middleCategories.find(c => c.id === categoryId);
      if (middleCategory && middleCategory.childClassList) {
        this.leafCategories = middleCategory.childClassList;
      } else {
        this.leafCategories = [];
      }
    },
    
    // 选择分类
    selectCategory: function(category) {
      this.selectedCategory = category;
      // 只有真正选择了叶子分类时才关闭弹窗；顶部分段切换或清空选择不关闭
      if (category) {
        this.closeCategoryPopup();
      }
      // 更新组件参数并重置页码
      const newParams = {
        ...this.billListParams,
        pageNum: 1
      };
      if (category && category.id) {
        newParams.categoryId = category.id;
      } else {
        delete newParams.categoryId;
      }
      this.billListParams = newParams;
    },
    
    navigateToCategoryPage: function() {
      this.closeCategoryPopup();
      uni.navigateTo({
        url: '/pages/class/class'
      });
    },
    
    // 预算状态相关方法
    showBudgetPopup: function() {
      // 关闭所有其他弹窗
      this.closeAllOtherPopups('budgetPopup');
      // 直接使用$refs访问弹窗实例并打开
      if (this.$refs.budgetPopup) {
        this.$refs.budgetPopup.open();
      }
    },
    
    closeBudgetPopup: function() {
      if (this.$refs.budgetPopup) {
        this.$refs.budgetPopup.close();
      }
    },
    
    selectBudget: function(index) {
      this.selectedBudgetIndex = index;
      this.closeBudgetPopup();
      // 更新组件参数并重置页码
      const newParams = { ...this.billListParams, pageNum: 1 };
      // 当选择全部时不传递isBudget参数
      if (this.budgetOptions[index] && this.budgetOptions[index].value !== -1) {
        newParams.isBudget = this.budgetOptions[index].value;
      } else {
        delete newParams.isBudget;
      }
      this.billListParams = newParams;
    },
    
    // 日期范围选择器相关方法
    showDateRangePicker: function() {
      // 关闭所有其他弹窗
      this.closeAllOtherPopups('dateRangePopup');
      
      // 确保日期范围已设置为最近一个月
      if (!this.dateRange || !this.isValidDateRange(this.dateRange)) {
        this.setDefaultDateRange();
      }
      
      // 直接使用$refs访问弹窗实例并打开
      if (this.$refs.dateRangePopup) {
        this.$refs.dateRangePopup.open();
      }
    },
    
    // 验证日期范围是否有效
    isValidDateRange: function(dateRange) {
      if (!dateRange || !Array.isArray(dateRange) || dateRange.length !== 2) {
        return false;
      }
      
      const startDate = new Date(dateRange[0]);
      const endDate = new Date(dateRange[1]);
      
      // 检查日期是否有效，且开始日期不晚于结束日期
      return !isNaN(startDate.getTime()) && !isNaN(endDate.getTime()) && startDate <= endDate;
    },
    
    closeDateRangePicker: function() {
      if (this.$refs.dateRangePopup) {
        this.$refs.dateRangePopup.close();
      }
    },
    
    confirmDateRangeSelection: function() {
      // 校验：必须选择时间范围
      if (!this.dateRange || !Array.isArray(this.dateRange) || this.dateRange.length !== 2) {
        uni.showToast({ title: '请先选择时间范围（最多30天）', icon: 'none' });
        return;
      }
      const startDateObj = this.dateRange[0] instanceof Date ? this.dateRange[0] : new Date(this.dateRange[0]);
      const endDateObj = this.dateRange[1] instanceof Date ? this.dateRange[1] : new Date(this.dateRange[1]);
      if (isNaN(startDateObj.getTime()) || isNaN(endDateObj.getTime()) || endDateObj.getTime() < startDateObj.getTime()) {
        uni.showToast({ title: '时间范围格式错误', icon: 'none' });
        return;
      }
      // 校验：最多30天
      const daysBetween = Math.floor((endDateObj.getTime() - startDateObj.getTime()) / (1000 * 60 * 60 * 24));
      if (daysBetween > 30) {
        uni.showToast({ title: '时间范围最多允许30天', icon: 'none' });
        return;
      }
      // 更新日期范围显示文本
      this.updateDateRangeText();
      // 关闭日期弹窗
      this.closeDateRangePicker();
      // 更新组件参数并重置页码
      const newParams = { ...this.billListParams, pageNum: 1 };
      const formatDate = (date) => {
        const dateObj = date instanceof Date ? date : new Date(date);
        const year = dateObj.getFullYear();
        const month = String(dateObj.getMonth() + 1).padStart(2, '0');
        const day = String(dateObj.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
      };
      const formattedStartDate = formatDate(startDateObj);
      const formattedEndDate = formatDate(endDateObj);
      newParams.startTime = `${formattedStartDate} 00:00:00`;
      newParams.endTime = `${formattedEndDate} 23:59:59`;
      this.billListParams = newParams;
    },
    

    
    // 状态筛选相关方法
    showStatusPopup: function() {
      // 关闭所有其他弹窗
      this.closeAllOtherPopups('statusPopup');
      // 直接使用$refs访问弹窗实例并打开
      if (this.$refs.statusPopup) {
        this.$refs.statusPopup.open();
      }
    },
    
    closeStatusPopup: function() {
      if (this.$refs.statusPopup) {
        this.$refs.statusPopup.close();
      }
    },
    
    // 点击状态提示图标显示提示信息
    onStatusTipClick: function() {
      uni.showModal({
        title: '账单状态说明',
        content: '正常：表示该账单已正常记录；已冻结：表示该账单已被创建人冻结；创建人也可以彻底删除账单',
        icon: 'none',
        duration: 3000
      });
    },
    
    selectStatus: function(index) {
      this.selectedStatusIndex = index;
      this.closeStatusPopup();
      // 更新组件参数并重置页码
      const newParams = { ...this.billListParams, pageNum: 1 };
      // 当选择全部状态时不传递status参数
      if (this.statusOptions[index] && this.statusOptions[index].value !== -1) {
        newParams.status = this.statusOptions[index].value;
      } else {
        delete newParams.status;
      }
      this.billListParams = newParams;
    },
    
    // 排序相关方法
    showSortPopup: function() {
      // 关闭所有其他弹窗
      this.closeAllOtherPopups('sortPopup');
      
      // 直接使用$refs访问弹窗实例并打开
      if (this.$refs.sortPopup) {
        this.$refs.sortPopup.open();
      }
    },
    
    closeSortPopup: function() {
      if (this.$refs.sortPopup) {
        this.$refs.sortPopup.close();
      }
    },
    
    selectSort: function(sortId) {
      this.selectedSortOption = sortId;
      this.closeSortPopup();
      // 更新组件参数并重置页码
      const newParams = { ...this.billListParams, pageNum: 1, sortBy: sortId };
      this.billListParams = newParams;
    },
    
    // 格式化时间
    formatTime: function(time) {
      if (!time) return '';
      const date = new Date(time);
      const month = date.getMonth() + 1;
      const day = date.getDate();
      const hour = date.getHours();
      const minute = date.getMinutes();
      return `${month}月${day}日 ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
    },
    
    // 导航到添加账单页面
    navigateToAddBill: function() {
      // 标记返回时刷新列表
      this.shouldRefreshOnShow = true;
      try { uni.setStorageSync('refreshBillListOnShow', true); } catch (e) {}
      uni.navigateTo({
        url: '/pages/bill/addBill'
      });
    },

    // 日期插件事件：打开时禁用弹窗遮罩点击
    onDatePickerShow: function() {
      this.isDatePickerOpen = true;
    },
    // 点击插件遮罩时仅关闭插件，不关闭弹窗
    onDatePickerMaskClick: function() {
      this.isDatePickerOpen = false;
    },
    // 选择并确认后关闭插件，恢复弹窗遮罩关闭行为
    onDatePickerChange: function() {
      this.isDatePickerOpen = false;
    }
  }
};
</script>

<style scoped>
/* 页面容器 */
.bill-list-page {
  padding: 0;
  box-sizing: border-box;
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 顶部功能栏 */
.top-toolbar {
  display: flex;
  flex-direction: column;
  padding: 10rpx;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  border-bottom: 1rpx solid #e9ecef;
  max-height: 240rpx;
  overflow-y: auto;
}

/* 功能栏行 */
.toolbar-row {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  margin-bottom: 6rpx;
}

.toolbar-row:last-child {
  margin-bottom: 0;
}

.toolbar-item {
  flex: 1;
  min-width: 120rpx;
  padding: 8rpx 12rpx;
  margin: 6rpx;
  background: linear-gradient(145deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: #6c757d;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  border: 1rpx solid #dee2e6;
}

.toolbar-item:hover {
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.toolbar-item:active {
  transform: translateY(0);
  background: linear-gradient(145deg, #e9ecef 0%, #dee2e6 100%);
  box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}

/* 账本标签样式 */
.toolbar-item.ledger-tag {
  background: linear-gradient(145deg, #f8f9fa 0%, #e9ecef 100%);
  color: #6c757d;
}

/* 账户标签样式 */
.toolbar-item.account-tag {
  background: linear-gradient(145deg, #f8f9fa 0%, #e9ecef 100%);
  color: #6c757d;
}

/* 分类标签样式 */
.toolbar-item.category-tag {
  background: linear-gradient(145deg, #f8f9fa 0%, #e9ecef 100%);
  color: #6c757d;
}



/* 预算标签样式 */
.toolbar-item.budget-tag {
  background: linear-gradient(145deg, #f8f9fa 0%, #e9ecef 100%);
  color: #6c757d;
}

/* 日期标签样式 */
.toolbar-item.date-tag {
  background: linear-gradient(145deg, #f8f9fa 0%, #e9ecef 100%);
  color: #6c757d;
}

/* 状态标签样式 */
.toolbar-item.status-tag {
  background: linear-gradient(145deg, #f8f9fa 0%, #e9ecef 100%);
  color: #6c757d;
}

/* 排序标签样式 */
.toolbar-item.sort-tag {
  background: linear-gradient(145deg, #f8f9fa 0%, #e9ecef 100%);
  color: #6c757d;
}

/* 成员标签样式 */
.toolbar-item.member-tag {
  background: linear-gradient(145deg, #f8f9fa 0%, #e9ecef 100%);
  color: #6c757d;
}

/* 所有标签选中时统一颜色 */
.toolbar-item.selected,
.toolbar-item.status-tag.selected,
.toolbar-item.date-tag.selected,
.toolbar-item.budget-tag.selected,
.toolbar-item.category-tag.selected,
.toolbar-item.account-tag.selected,
.toolbar-item.ledger-tag.selected,
.toolbar-item.member-tag.selected,
.toolbar-item.sort-tag.selected {
  background: linear-gradient(145deg, #e3f2fd 0%, #bbdefb 100%);
  color: #1976d2;
  border-color: #90caf9;
  box-shadow: 0 4rpx 12rpx rgba(25, 118, 210, 0.2);
}

/* 重置按钮样式 */
.toolbar-item.reset-button {
  background: linear-gradient(145deg, #ff5252 0%, #ff1744 100%);
  color: #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(255, 82, 82, 0.4);
  border-color: #ff5252;
}

.toolbar-item.reset-button:hover {
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 16rpx rgba(255, 82, 82, 0.5);
}

.toolbar-item.reset-button:active {
  transform: translateY(0);
  background: linear-gradient(145deg, #ff1744 0%, #d32f2f 100%);
  box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
}

/* 导出按钮样式 */
.toolbar-item.export-button {
  background: linear-gradient(145deg, #4caf50 0%, #2e7d32 100%);
  color: #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.4);
  border-color: #4caf50;
}

.toolbar-item.export-button:hover {
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 16rpx rgba(76, 175, 80, 0.5);
}

.toolbar-item.export-button:active {
  transform: translateY(0);
  background: linear-gradient(145deg, #2e7d32 0%, #1b5e20 100%);
  box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
}

/* 成员列表样式 */
.member-list {
  padding: 20rpx;
}

.member-item {
  padding: 25rpx;
  border-bottom: 1px solid #f0f0f0;
}

.member-item:last-child {
  border-bottom: none;
}

.member-item:active {
  background-color: #e0e0e0;
}

.member-item.selected {
  background-color: #e8f4fd;
}

.member-info-row {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 8rpx;
}

.member-info-row:first-child {
  justify-content: space-between;
}

.member-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.member-create-time {
  font-size: 24rpx;
  color: #666;
}

.member-parent-tag {
  font-size: 24rpx;
  color: #007AFF;
  background-color: #E8F0FE;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
}

.no-select-member {
  padding: 30rpx 0;
  text-align: center;
  color: #007AFF;
  font-size: 30rpx;
}

.no-select-member-text {
  padding: 20rpx 40rpx;
  border-radius: 8rpx;
}

/* 状态标签样式 */
.toolbar-item.status-tag {
  background-color: #f0f0f0;
  color: #666;
}

.toolbar-item.status-tag:active {
  background-color: #e0e0e0;
}

.toolbar-item uni-icons {
  margin-left: 8rpx;
}

/* 预算选项样式 */
.budget-options {
  padding: 20rpx;
}

.budget-option-item {
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
  text-align: center;
}

.budget-option-item:last-child {
  border-bottom: none;
}

.budget-option-item.selected {
  background-color: #f0f7ff;
}

.budget-option-text {
  font-size: 32rpx;
  color: #333;
}

/* 不选分类样式 */
.no-select-text {
  color: #007AFF;
  font-size: 28rpx;
  margin-right: 20rpx;
}

/* 内容区域 */
.bill-list-content {
  margin-top: 220rpx;
  padding-bottom: 100rpx;
}

/* 账单列表 */
.bill-list {
  padding: 20rpx;
}

/* 修复弹窗被顶部标签挡住的问题 */
/* 全局弹窗样式覆盖 */
.uni-popup__mask {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  z-index: 9999 !important;
  background-color: rgba(0, 0, 0, 0.5) !important;
}

.uni-popup__content {
  z-index: 10000 !important;
}

/* 使用深度选择器确保样式应用到所有层级 */
::v-deep .uni-popup__mask {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  z-index: 9999 !important;
  background-color: rgba(0, 0, 0, 0.5) !important;
}

::v-deep .uni-popup__content {
  z-index: 10000 !important;
}

/* 确保所有弹窗组件都有足够高的层级 */
::v-deep .uni-popup {
  z-index: 9999 !important;
}

/* 确保自定义弹窗组件也能覆盖顶部工具栏 */
::v-deep .ledger-select-popup {
  z-index: 9999 !important;
}

::v-deep .account-select-popup {
  z-index: 9999 !important;
}

/* 确保分类弹窗也能覆盖顶部工具栏 */
::v-deep .category-popup {
  z-index: 9999 !important;
}

/* 确保日期弹窗也能覆盖顶部工具栏 */
  ::v-deep .date-popup-container {
  z-index: 9999 !important;
}

/* 确保所有弹窗容器都能覆盖顶部工具栏 */
.popup-container {
  z-index: 10000 !important;
}

.bill-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  position: relative;
}

.bill-item:last-child {
  border-bottom: none;
}

/* 删除状态的账单样式 */
.bill-item.bill-deleted {
  opacity: 0.6;
  background-color: #f9f9f9;
}

.bill-item.bill-deleted .bill-category,
.bill-item.bill-deleted .bill-amount,
.bill-item.bill-deleted .bill-time {
  color: #999 !important;
  text-decoration: line-through;
}

.bill-info {
  flex: 1;
}

.bill-id {
  position: absolute;
  top: 10rpx;
  left: 10rpx;
  font-size: 24rpx;
  color: #007AFF;
  font-weight: normal;
  z-index: 1;
}

.bill-category {
  font-size: 32rpx;
  color: #333333;
}

.bill-amount {
  font-size: 32rpx;
  font-weight: bold;
}

.bill-amount.expense {
  color: #FF4D4F;
}

.bill-amount.income {
  color: #52C41A;
}

.bill-detail {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.bill-account {
  font-size: 24rpx;
  color: #666;
}

.bill-time {
  font-size: 24rpx;
  color: #999;
}

.bill-memo {
  font-size: 24rpx;
  color: #999;
  display: block;
}

/* 悬浮新增按钮 */
.floating-add-btn {
  position: fixed;
  bottom: 30rpx;
  right: 30rpx;
  width: 96rpx;
  height: 96rpx;
  background-color: #007AFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 20rpx rgba(0, 122, 255, 0.3);
}

/* 加载状态 */
.loading-container {
  padding: 60rpx 0;
  text-align: center;
}

/* 错误状态 */
.error-container {
  padding: 60rpx 20rpx;
  text-align: center;
}

.error-text {
  display: block;
  color: #e64340;
  margin-bottom: 20rpx;
  font-size: 28rpx;
}

.retry-btn {
  background-color: #007aff;
  color: white;
}

/* 空状态 */
.empty-container {
  padding: 100rpx 0;
  text-align: center;
}

.empty-text {
  display: block;
  margin-top: 20rpx;
  color: #999;
  font-size: 28rpx;
}

/* 弹出层样式 */
.popup-container {
  background-color: #fff;
  border-radius: 12px 12px 0 0;
  padding-bottom: 20rpx;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #eee;
}

.popup-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.popup-close {
  color: #999;
  font-size: 28rpx;
}

.popup-actions {
  display: flex;
  align-items: center;
}

.popup-content {
  max-height: 60vh;
  overflow-y: auto;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 60rpx 0;
  font-size: 28rpx;
}

.close-btn {
  color: #999;
  font-size: 28rpx;
  padding: 10rpx;
}



.setting-btn {
  background-color: transparent;
  color: #007AFF;
  font-size: 28rpx;
  padding: 10rpx;
  line-height: normal;
  min-height: auto;
}

.header-actions, .popup-actions {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

/* 弹出层样式 */
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

/* 分类弹窗特定样式 */
.category-popup {
  width: 100%;
  background-color: #fff;
  border-radius: 12px 12px 0 0;
  height: 70vh;
}

.category-popup .popup-header {
  padding: 30rpx;
  background-color: #fff;
}

.category-popup .popup-actions {
  display: flex;
  align-items: center;
  gap: 20rpx;
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
  overflow-y: auto;
  padding: 30rpx 40rpx;
}

/* 账本列表样式 */
.ledger-list {
  padding: 20rpx;
}

.ledger-item {
  padding: 25rpx;
  border-bottom: 1px solid #f0f0f0;
}

.ledger-item:last-child {
  border-bottom: none;
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

.ledger-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.default-badge {
  background-color: #FF4D4F;
  color: white;
  font-size: 20rpx;
  padding: 4rpx 10rpx;
  border-radius: 4rpx;
  font-weight: normal;
}

.ledger-type-text {
  font-size: 24rpx;
  color: #007AFF;
}

.ledger-property-text {
  font-size: 24rpx;
  color: #5AC8FA;
}

.selected-icon {
  position: absolute;
  right: 30rpx;
  top: 50%;
  transform: translateY(-50%);
}

/* 账户列表样式 */
.account-list {
  padding: 20rpx;
}

.account-item {
  padding: 25rpx;
  border-bottom: 1px solid #f0f0f0;
}

.account-item:last-child {
  border-bottom: none;
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
  margin-bottom: 15rpx;
}

.account-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.account-type-tag {
  font-size: 24rpx;
  color: #fff;
  padding: 5rpx 15rpx;
  border-radius: 15rpx;
  background-color: #007AFF;
}

/* 不同账户类型的样式 */
.account-type-tag {
  margin-left: 8rpx;
  vertical-align: middle;
}

.account-type-tag.type-savings {
  background-color: #007AFF;
}

.account-type-tag.type-credit {
  background-color: #FF2D55;
}

.account-type-tag.type-recharge {
  background-color: #5AC8FA;
}

.account-type-tag.type-invest {
  background-color: #34C759;
}

.account-balance {
  font-size: 24rpx;
  color: #666;
  margin-left: auto;
}

.account-info-second-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 15rpx;
  margin-top: 10rpx;
  padding-left: 0;
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

.no-select-account {
  padding: 30rpx 0;
  text-align: center;
  color: #007AFF;
  font-size: 30rpx;
}

.no-select-account-text {
  padding: 20rpx 40rpx;
  border-radius: 8rpx;
}

/* 分类弹窗样式 - 调整大小以避免被标签行挡住 */
.category-popup {
  background-color: #fff;
  border-radius: 12px 12px 0 0;
  padding-bottom: 20rpx;
  transform: translateY(30rpx); /* 向上移动一点，避免被标签行挡住 */
}

.segmented-container {
  padding: 0 30rpx 20rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

.category-content {
  max-height: 60vh; /* 减小最大高度 */
  overflow-y: auto;
}

.loading-container {
  padding: 40rpx 0;
  text-align: center;
}

.category-scrollview {
  padding-bottom: 20rpx;
}

.grid-container {
  padding: 15rpx;
}

.category-card {
  padding: 15rpx 10rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100rpx;
}

.category-icon {
  font-size: 50rpx;
  margin-bottom: 8rpx;
}

.category-list {
  padding: 20rpx;
}

.no-subcategory {
  padding: 40rpx 0;
  text-align: center;
  color: #999;
  font-size: 28rpx;
}

.no-category {
  padding: 80rpx 0;
  text-align: center;
  color: #999;
  font-size: 28rpx;
}

/* 日期范围选择器样式 */
.date-popup-container {
  width: 100%;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 12px 12px 12px 12px;
}

.example-body {
  padding: 20rpx;
}

.popup-footer {
  padding: 30rpx;
  border-top: 1px solid #eee;
  margin-top: 20rpx;
}

.confirm-btn {
  background-color: #007AFF;
  color: #fff;
  font-size: 32rpx;
  border-radius: 20px;
}

/* 金额输入样式 */
.amount-input-content {
  padding: 30rpx;
}

.amount-input {
  width: 100%;
  height: 100rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 20px;
  padding: 0 30rpx;
  font-size: 36rpx;
  margin-bottom: 30rpx;
  box-sizing: border-box;
  background-color: #f9f9f9;
  transition: all 0.3s ease;
}

.amount-input:focus {
  border-color: #007aff;
  background-color: #fff;
  outline: none;
}

/* 金额弹窗样式 */
.amount-popup-container {
  width: 100%;
  max-width: 680rpx;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 12px 12px 0 0;
  padding-bottom: 20rpx;
}

/* 状态弹窗样式 */
.status-popup-container {
  width: 100%;
  max-width: 680rpx;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 12px 12px 0 0;
  padding-bottom: 20rpx;
}

/* 状态列表样式 */
.status-list {
  padding: 20rpx;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 20rpx;
  margin-bottom: 10rpx;
  background-color: #f8f8f8;
  border-radius: 12rpx;
  transition: all 0.3s ease;
}

.status-item:hover {
  background-color: #f0f0f0;
}

.status-item.selected {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
}

.status-name {
  font-size: 32rpx;
  color: #333;
}

/* 账单列表样式 */
.bills-list {
  background-color: #ffffff;
  margin: 0 auto;
  width: calc(100% - 100rpx);
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

/* 统计信息样式 */
.bill-statistics {
  background-color: #f8f8f8;
  margin: 20rpx 50rpx;
  padding: 30rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.statistics-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 0;
}

.statistics-label {
  font-size: 28rpx;
  color: #666;
}

.statistics-value {
  font-size: 32rpx;
  font-weight: bold;
}

.statistics-value.income {
  color: #07c160;
}

.statistics-value.expense {
  color: #ee0a24;
}

.bill-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.bill-item:last-child {
  border-bottom: none;
}

.bill-icon-no-bg {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 20rpx;
}

/* 文本图标的样式 */
.text-icon {
  font-size: 40rpx;
}

.bill-info {
  flex: 1;
}

.bill-main-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.bill-category {
  font-size: 32rpx;
  color: #333333;
}

.bill-amount {
  font-size: 32rpx;
  font-weight: bold;
}

.bill-amount.expense {
  color: #ff4d4f;
}

.bill-amount.income {
  color: #07c160;
}

.bill-time {
  font-size: 28rpx;
  color: #999999;
  text-align: left;
}

.empty-bills {
  text-align: center;
  padding: 60rpx 0;
  color: #999999;
  font-size: 28rpx;
}

/* 弹窗样式 */
.popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 998;
}

@keyframes popupSlideUp {
  to {
    transform: translateX(-50%) translateY(0);
  }
}

.bill-popup {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%) translateY(100%);
  width: 90%;
  max-width: 600rpx;
  background-color: #ffffff;
  border-radius: 24rpx 24rpx 0 0;
  z-index: 999;
  animation: popupSlideUp 0.3s forwards;
  max-height: 70vh;
  overflow-y: auto;
}

.popup-header {
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  background-color: #ffffff;
  z-index: 1002;
}

.popup-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.popup-close {
  font-size: 40rpx;
  color: #999999;
}

.popup-actions-header {
  display: flex;
  align-items: center;
}

.action-icon {
  margin-right: 20rpx;
  padding: 10rpx;
  font-size: 40rpx;
}

.popup-body {
  padding: 32rpx 24rpx;
}

.bill-detail-info {
  padding: 30rpx;
  margin-bottom: 30rpx;
  background-color: #fafafa;
  border-radius: 10rpx;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eeeeee;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: 28rpx;
  color: #666666;
}

.detail-value {
  font-size: 28rpx;
  color: #333333;
  text-align: right;
  display: flex;
  align-items: center;
}

.income {
  color: #07c160 !important;
}

.expense {
  color: #ee0a24 !important;
}

.category-icon {
  width: 40rpx;
  height: 40rpx;
  margin-right: 10rpx;
  vertical-align: middle;
}

.class-icon {
  display: inline-flex;
  align-items: center;
  margin-right: 10rpx;
}

.detail-value {
  display: inline-block;
  vertical-align: middle;
}

.action-text {
  margin-right: 20rpx;
  padding: 10rpx;
  font-size: 28rpx;
  color: #1989fa;
  line-height: 40rpx;
  white-space: nowrap;
  background: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 40rpx;
  min-height: auto;
}

.export-button {
  background-color: #007AFF;
  color: #ffffff;
}

/* 日期选择器层级与遮罩：保证先关闭插件再关闭弹窗 */
:deep(.uni-date-picker__container) { position: relative !important; z-index: 10011 !important; }
:deep(.uni-date-mask--pc) { z-index: 10010 !important; background-color: rgba(0,0,0,0.35) !important; }
:deep(.uni-date-mask) { z-index: 10010 !important; background-color: rgba(0,0,0,0.35) !important; }
:deep(.uni-date-range--x),
:deep(.uni-date-single--x) { z-index: 10011 !important; }
.date-popup-container { overflow: visible !important; }
.date-popup-container .popup-content { overflow: visible !important; }
</style>