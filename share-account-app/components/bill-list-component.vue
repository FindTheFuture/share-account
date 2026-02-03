<template>
  <view class="bill-list-component">
    <!-- 账单列表 -->
    <scroll-view class="bills-list" scroll-y ref="billScrollView">
      <view v-if="bills.length === 0" class="empty-bills">
        <text>暂无账单记录</text>
      </view>
      <view v-for="bill in bills" :key="bill.id" :class="['bill-item', { 'bill-deleted': bill.status === 1 }]" @click="showBillDetail(bill)">
        <text class="bill-id">#{{ bill.id }}</text>
        <view class="bill-icon-no-bg">
          <view class="class-icon">
            <template v-if="hasFontIcon(bill.classIcon)">
              <custom-icon :type="normalizeIcon(bill.classIcon)" :size="30" :color="billIconColor"></custom-icon>
            </template>
            <template v-else>
              <text class="first-char-icon" :style="{ color: billIconColor }">{{ firstChar(bill.className) }}</text>
            </template>
          </view>
        </view>
        <view class="bill-info">
          <view class="bill-main-info">
            <text class="bill-category">{{ bill.className || '未分类' }}</text>
            <text :class="['bill-amount', bill.price > 0 ? 'income' : 'expense']">
              {{ bill.price > 0 ? '+' : '-' }}{{ formatAmount(Math.abs(bill.price) / 100) }}
            </text>
          </view>
          <view class="bill-sub-info" style="display:flex;align-items:center;flex-wrap:wrap;margin-top:6rpx;">
            <text v-if="showLedgerTag && bill.ledgerName" class="bill-tag tag-ledger">{{ bill.ledgerName }}</text>
            <text v-if="showAccountTag && resolveAccountName(bill)" class="bill-tag tag-account">{{ resolveAccountName(bill) }}</text>
            <text class="bill-tag" :class="budgetTagClass(bill)">{{ budgetTagText(bill) }}</text>
            <text class="bill-time">{{ formatBillTime(bill.billTime) }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 分页组件 -->
    <view v-if="showPagination && total > 0" style="padding: 20rpx;">
      <uni-pagination 
        :show-icon="true" 
        :total="total"
        :pageSize="requestParams.pageSize"
        :current="requestParams.pageNum"
        :disabled="loading"
        @change="onPageChange"
      />
    </view>
    
    <!-- 统计信息 -->
    <view v-if="showStatistics && total > 0" class="bill-statistics">
      <view class="statistics-item">
        <text class="statistics-label">总收入：</text>
        <text class="statistics-value income">+{{ formatAmount(totalIncome / 100) }}</text>
      </view>
      <view class="statistics-item">
        <text class="statistics-label">总支出：</text>
        <text class="statistics-value expense">-{{ formatAmount(totalExpense / 100) }}</text>
      </view>
    </view>
    
    <!-- 错误提示 -->
    <view class="error-container" v-if="errorMsg">
      <text class="error-text">{{ errorMsg }}</text>
    </view>
    
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载账单..."></uni-load-more>
    </view>

    <!-- 账单详情弹窗 -->
    <uni-popup ref="billPopup" type="bottom" :custom="true" :mask-click="true" @close="closeBillPopup">
      <view class="bill-popup">
        <view class="popup-header">
          <text class="popup-title">账单详情</text>
          <view class="popup-actions-header">
            <button open-type="share" class="action-text action-share" @touchstart="prepareShareMember" @tap="onShareClick">分享</button>
            <button class="action-text action-comment" @click="openComments">评论</button>
             <custom-icon v-if="isCurrentUserOwner && currentBill && currentBill.status === 0" type="bianji" :size="21" color="#1989fa" @click="editBill" class="action-icon"></custom-icon>
             <custom-icon v-if="isCurrentUserOwner && currentBill && currentBill.status === 0" type="shanchu" :size="21" color="#ff4d4f" @click="deleteBill" class="action-icon"></custom-icon>
             <custom-icon v-if="isCurrentUserOwner && currentBill && currentBill.status === 1" type="qiyong" :size="23" color="#07c160" @click="enableBill" class="action-icon"></custom-icon>
             <custom-icon v-if="isCurrentUserOwner && currentBill && currentBill.status === 1" type="shanchu" :size="23" color="#ff4d4f" @click="deleteCompletelyBill" class="action-icon"></custom-icon>
             <custom-icon type="guanbi" :size="21" color="#999" @click="closeBillPopup"></custom-icon>
          </view>
        </view>
        <view class="popup-body" v-if="currentBill">
          <view class="bill-detail-info">
            <view class="detail-row" v-if="!isCurrentUserOwner">
              <text class="detail-label">创建人</text>
              <text class="detail-value">{{ currentBill.createUserName || currentBill.userName || '-' }}</text>
            </view>
             <view class="detail-row">
               <text class="detail-label">分类</text>
               <view class="detail-value">
                  <text>{{ currentBill.className }}</text>
               </view>
            </view>
            <view class="detail-row">
              <text class="detail-label">金额</text>
              <text :class="['detail-value', currentBill.price > 0 ? 'income' : 'expense']">
                {{ currentBill.price > 0 ? '+' : '-' }}{{ formatAmount(Math.abs(currentBill.price) / 100) }}
              </text>
            </view>
            <view class="detail-row" v-if="currentBill.ledgerName">
              <text class="detail-label">账本名称</text>
              <text class="detail-value">{{ currentBill.ledgerName }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">账户名称</text>
              <text class="detail-value">{{ currentBill.accountName }}</text>
            </view>
            <view class="detail-row" v-if="currentBill.isBudgetName">
              <text class="detail-label">预算状态</text>
              <text class="detail-value">{{ currentBill.isBudgetName }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">时间</text>
              <text class="detail-value">{{ formatBillTime(currentBill.billTime, true) }}</text>
            </view>
            <view class="detail-row" v-if="currentBill.memo">
              <text class="detail-label">备注</text>
              <text class="detail-value">{{ currentBill.memo }}</text>
            </view>
          </view>


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
import { prepareBillMember, resolveShareInfo } from '@/common/shareStrategy.js';
import { formatAmount, normalizeBillQueryParams, validateBillQueryParams } from '@/common/util.js';
import { mapGetters } from 'vuex'
import { themeIconColor } from '@/components/utils.js'

export default {
  name: 'bill-list-component',
  components: {
    UniIcons,
    UniPopup,
    UniLoadMore,
    UniPagination
  },
  props: {
    // 请求参数，由父组件传入
    requestParams: {
      type: Object,
      default: () => ({
        pageNum: 1,
        pageSize: 10,
        userId: '',
        ledgerId: '',
        accountId: '',
        categoryId: '',
        isBudget: '',
        startTime: '',
        endTime: '',
        status: ''
      })
    },
    // 是否显示统计信息
    showStatistics: {
      type: Boolean,
      default: true
    },
    // 是否显示分页插件
    showPagination: {
      type: Boolean,
      default: true
    },
    // 分页变更后是否滚动到顶部
    scrollToTopOnPageChange: {
      type: Boolean,
      default: true
    },
    // 当前登录用户ID
    currentUserId: {
      type: [String, Number],
      required: true
    },
    // 账户列表，用于匹配账户类型名称
    accounts: {
      type: Array,
      default: () => []
    },
    // 是否显示账本名称标签（默认显示）
    showLedgerTag: {
      type: Boolean,
      default: true
    },
    // 是否显示账户名称标签（默认显示；首页可传 false）
    showAccountTag: {
      type: Boolean,
      default: true
    },
    // 首页最近账单：跳过时间范围与必填校验
    skipTimeValidation: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // 账单列表
      bills: [],
      loading: false,
      errorMsg: '',
      hasMore: true,
      total: 0,
      // 统计数据
      totalIncome: 0,
      totalExpense: 0,
      // 弹窗相关
      currentBill: null,
      billPopupVisible: false,
      // 分享相关
      shareMemberId: null,
      shareIntent: false,
      _sharePreparing: false,
      // 游客模式标记
      isGuest: false,
      // 评论相关
      comments: [],
      loadingComments: false,
      newCommentText: '',
      submittingComment: false
    };
  },
  mounted() {
    // 初始化游客模式标记
    const guestFlag = uni.getStorageSync('isGuest');
    this.isGuest = guestFlag === true || guestFlag === 'true';
  },
  computed: {
    ...mapGetters(['themePrimaryColor']),
    billIconColor() {
      return themeIconColor(this.themePrimaryColor);
    },
    // 判断当前用户是否是账单所有者
    isCurrentUserOwner() {
      return this.currentBill && String(this.currentBill.userId) === String(this.currentUserId);
    }
  },
  watch: {
    // 监听请求参数变化，立即按传入参数加载数据
    requestParams: {
      deep: true,
      immediate: true,
      handler() {
        this.loadBillList();
      }
    }
  },
  methods: {
    formatAmount,
    // 解析账户名称（优先bill.accountName；否则由accounts列表通过accountId匹配；再退回账户类型名或'-'）
    resolveAccountName(bill) {
      if (!bill) return '';
      if (bill.accountName) return bill.accountName;
      const id = bill.accountId;
      if (!id || !Array.isArray(this.accounts)) {
        return bill.accountTypeName || '';
      }
      const found = this.accounts.find(a => String(a.id) === String(id));
      return found ? (found.name || found.accountName || found.accountTypeName || '') : (bill.accountTypeName || '');
    },
    // 预算标签文案（优先isBudgetName；否则根据isBudget布尔/数值）
    budgetTagText(bill) {
      if (!bill) return '不计入预算';
      if (typeof bill.isBudgetName === 'string' && bill.isBudgetName) return bill.isBudgetName;
      const v = bill.isBudget;
      const isIn = v === 1 || v === true || v === '1' || v === 'true';
      return isIn ? '计入预算' : '不计入预算';
    },
    // 预算标签类名（绿色：计入预算；红色：不计入预算）
    budgetTagClass(bill) {
      const v = bill && bill.isBudget;
      const byName = bill && typeof bill.isBudgetName === 'string' && bill.isBudgetName.indexOf('计入') !== -1;
      const isIn = byName || v === 1 || v === true || v === '1' || v === 'true';
      return isIn ? 'budget-in' : 'budget-out';
    },
    openComments() {
      if (!this.currentBill || !this.currentBill.id) {
        uni.showToast({ title: '账单信息错误', icon: 'none' });
        return;
      }
      const ownerId = this.currentBill.userId || '';
      uni.navigateTo({ url: `/pages/comment/comment?billId=${this.currentBill.id}` });
    },
    // 加载账单列表
    loadBillList() {
      this.loading = true;
      this.errorMsg = '';
      
      // 构建请求参数
      let params = { ...this.requestParams };
      
      // 处理日期范围格式
      if (params.dateRange) {
        if (Array.isArray(params.dateRange) && params.dateRange.length === 2) {
          const [startDate, endDate] = params.dateRange;
          const formatDate = (date) => {
            if (!date) return '';
            const dateObj = date instanceof Date ? date : new Date(date);
            if (isNaN(dateObj.getTime())) return '';
            const year = dateObj.getFullYear();
            const month = String(dateObj.getMonth() + 1).padStart(2, '0');
            const day = String(dateObj.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
          };
          
          const formattedStartDate = formatDate(startDate);
          const formattedEndDate = formatDate(endDate);
          
          if (formattedStartDate && formattedEndDate) {
            params.startTime = `${formattedStartDate} 00:00:00`;
            params.endTime = `${formattedEndDate} 23:59:59`;
          }
          // 删除dateRange参数，避免传递给后端
          delete params.dateRange;
        } else if (typeof params.dateRange === 'string') {
          const dateRangeArray = params.dateRange.split('至');
          if (dateRangeArray.length === 2) {
            const startDate = dateRangeArray[0].trim();
            const endDate = dateRangeArray[1].trim();
            params.startTime = `${startDate} 00:00:00`;
            params.endTime = `${endDate} 23:59:59`;
          }
          // 删除dateRange参数，避免传递给后端
          delete params.dateRange;
        }
      }
      
      // 参数规范化与最小防御（避免重复提示）
      params = normalizeBillQueryParams(params);
      const v = validateBillQueryParams(params, { skipDateValidation: this.skipTimeValidation });
      if (!v.ok) {
        this.loading = false;
        this.errorMsg = v.errorMsg;
        // 页面级已提示，这里仅拦截不再重复toast
        return;
      }
      
      // 使用分页查询接口
      request({
        url: backUrl.endpoints.bill_listWithPagination,
        method: 'POST',
        data: params,
        loading: false
      }).then(res => {
        this.loading = false;
        if (res) {
          const result = res;
          const newBills = result.list || [];
          
          // 清空现有数据，确保无数据时显示空状态
          this.bills = [];
          
          // 添加新数据，并确保每个账单对象包含账户类型名称
          if (newBills && newBills.length > 0) {
            this.bills = [...newBills.map(bill => {
              // 查找对应的账户信息，获取账户类型名称
              return {
                ...bill
              };
            })];
          }
          // 保存总记录数
          this.total = result.total || 0;
          // 保存统计数据
          this.totalIncome = result.totalIncome || 0;
          this.totalExpense = result.totalExpense || 0;
          
          // 判断是否还有更多数据
          this.hasMore = this.bills.length < this.total;
          
          // 触发加载完成事件
          this.$emit('load-complete', {
            bills: this.bills,
            total: this.total,
            totalIncome: this.totalIncome,
            totalExpense: this.totalExpense
          });
        } else {
          this.errorMsg = res && res.data && res.data.message ? res.data.message : '加载账单失败';
          this.$emit('error', this.errorMsg);
        }
      }).catch(err => {
        this.loading = false;
        console.error('加载账单失败:', err);
        this.errorMsg = '加载账单失败';
        // 清空数据，确保错误情况下也能正确显示状态
        this.bills = [];
        this.total = 0;
        this.totalIncome = 0;
        this.totalExpense = 0;
        this.$emit('error', this.errorMsg);
      });
    },
    
    // 分页变更处理
    onPageChange: function(e) {
      // 创建新的参数对象，更新页码
      const newParams = {
        ...this.requestParams,
        pageNum: e.current
      };
      
      // 触发refresh-list事件，传递新的参数
      this.$emit('refresh-list', newParams);
      
      // 根据配置决定是否滚动到顶部
      if (this.scrollToTopOnPageChange) {
        const scrollView = this.$refs.billScrollView;
        if (scrollView) {
          scrollView.scrollTo(0, 0);
        }
      }
    },
    
    // 显示账单详情
    async showBillDetail(bill) {
      if (!bill || !bill.id) return;
      this.currentBill = bill;
      this.shareMemberId = null;
      this.billPopupVisible = true;
      this.$refs.billPopup && this.$refs.billPopup.open();
      try {
        this._sharePreparing = true;
        await prepareBillMember(bill);
      } finally {
        this._sharePreparing = false;
      }
      this.$emit('show-detail', bill);

    },
    
    // 关闭账单弹窗
    closeBillPopup() {
      if (this.$refs.billPopup) {
        this.$refs.billPopup.close();
      }
      this.billPopupVisible = false;
      this.currentBill = null;
      this.shareMemberId = null;
      this.shareIntent = false;
      // 触发关闭详情事件
      this.$emit('close-detail');
    },
    
    // 编辑账单
    editBill() {
      if (!this.currentBill) {
        uni.showToast({
          title: '账单信息错误',
          icon: 'none'
        });
        return;
      }
      
      // 先保存billId和ledgerId，再关闭弹窗
      const billId = this.currentBill.id || '';
      const ledgerId = this.currentBill.ledgerId || '';
      
      if (!ledgerId) {
        uni.showToast({
          title: '缺少账本信息',
          icon: 'none'
        });
        return;
      }
      
      // 先关闭弹窗，再跳转
      this.closeBillPopup();
      
      // 添加延迟确保弹窗完全关闭
      setTimeout(() => {
        // 标记返回时刷新账单列表
        try { uni.setStorageSync('refreshBillListOnShow', true); } catch (e) {}
        uni.navigateTo({
          url: `/pages/bill/addBill?billId=${billId}&ledgerId=${ledgerId}`,
          fail: (err) => {
            console.error('跳转失败:', err);
            uni.showToast({
              title: '跳转失败，请重试',
              icon: 'none'
            });
          }
        });
      }, 300);
    },
    
    // 打开账单
    enableBill() {
      if (!this.currentBill || !this.currentBill.id) {
        uni.showToast({
          title: '账单信息错误',
          icon: 'none'
        });
        return;
      }
      
      // 显示确认对话框
      uni.showModal({
        title: '确认打开',
        content: '确定要打开这条账单吗？',
        success: async (res) => {
          if (res.confirm) {
            // 显示加载状态
            uni.showLoading({
              title: '处理中...'
            });
            
            try {
              // 使用updateStatus接口
              const response = await request({
                url: backUrl.endpoints.bill_updateStatus,
                method: 'POST',
                data: {
                  billId: this.currentBill.id,
                  status: 0 // 0表示正常状态
                },
                loading: false
              });
              
              if (response) {
                uni.showToast({
                  title: '打开成功',
                  icon: 'success'
                });

                // 触发刷新事件，传递状态变更信息
                this.$emit('refresh-list', {
                  action: 'enable',
                  billId: this.currentBill.id
                });
                
                this.closeBillPopup();
                // 重置hasMore，确保能够重新加载数据
                this.hasMore = true;
                // 重新加载数据
                this.loadBillList();
              } else {
                throw new Error(response?.message || '打开失败');
              }
            } catch (error) {
              console.error('打开账单失败:', error);
              uni.showToast({
                title: error.message || '打开失败',
                icon: 'none'
              });
            } finally {
              // 无论成功失败都关闭加载
              uni.hideLoading();
            }
          }
        }
      });
    },
    
    // 删除账单
    deleteBill() {
      if (!this.currentBill || !this.currentBill.id) {
        uni.showToast({
          title: '账单信息错误',
          icon: 'none'
        });
        return;
      }
      
      // 显示确认对话框，增加风险提示
      uni.showModal({
        title: '确定冻结账单？',
        content: '账单冻结后可以重新打开，也可以彻底删除',
        success: async (res) => {
          if (res.confirm) {
            // 显示加载状态
            uni.showLoading({
              title: '处理中...'
            });
            
            try {
              // 构建URL，统一处理路径拼接
              let url = backUrl.endpoints.bill_deleteById;
              // 确保URL和ID正确拼接
              if (!url.endsWith('/')) {
                url += '/';
              }
              url += this.currentBill.id;
              
              const response = await request({
                url: url,
                method: 'POST',
                loading: false
              });
              
              if (response) {
                uni.showToast({
                  title: '冻结成功',
                  icon: 'success'
                });
                // 触发刷新事件，传递状态变更信息
                this.$emit('refresh-list', {
                  action: 'delete',
                  billId: this.currentBill.id
                });
                this.closeBillPopup();
                // 重置hasMore，确保能够重新加载数据
                this.hasMore = true;
                // 重新加载数据
                this.loadBillList();
                
              } else {
                throw new Error(response?.message || '删除失败');
              }
            } catch (error) {
              console.error('冻结账单失败:', error);
              uni.showToast({
                title: error.message || '冻结失败',
                icon: 'none'
              });
            } finally {
              // 无论成功失败都关闭加载
              uni.hideLoading();
            }
          }
        }
      });
    },

    // 彻底删除账单
    deleteCompletelyBill() {
      if (!this.currentBill || !this.currentBill.id) {
        uni.showToast({
          title: '账单信息错误',
          icon: 'none'
        });
        return;
      }
      
      // 显示确认对话框，增加风险提示
      uni.showModal({
        title: '确定彻底删除账单？',
        content: '彻底删除账单后将不可恢复，请再次确认！',
        success: async (res) => {
          if (res.confirm) {
            // 显示加载状态
            uni.showLoading({
              title: '处理中...'
            });
            
            try {
              // 构建URL，统一处理路径拼接
              let url = backUrl.endpoints.bill_deleteById;
              // 确保URL和ID正确拼接
              if (!url.endsWith('/')) {
                url += '/';
              }
              url += this.currentBill.id;
              
              const response = await request({
                url: url,
                method: 'POST',
                loading: false
              });
              
              if (response) {
                uni.showToast({
                  title: '彻底删除成功',
                  icon: 'success'
                });
                // 触发刷新事件，传递状态变更信息
                this.$emit('refresh-list', {
                  action: 'delete',
                  billId: this.currentBill.id
                });
                this.closeBillPopup();
                // 重置hasMore，确保能够重新加载数据
                this.hasMore = true;
                // 重新加载数据
                this.loadBillList();
                
              } else {
                throw new Error(response?.message || '删除失败');
              }
            } catch (error) {
              console.error('彻底删除账单失败:', error);
              uni.showToast({
                title: error.message || '彻底删除失败',
                icon: 'none'
              });
            } finally {
              // 无论成功失败都关闭加载
              uni.hideLoading();
            }
          }
        }
      });
    },
    
    
    // 格式化账单时间
    formatBillTime(time, showFull = false) {
      if (!time) return '';
      
      try {
        const date = new Date(time);
        if (isNaN(date.getTime())) return '';
        
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        
        if (showFull) {
          return `${year}-${month}-${day} ${hours}:${minutes}`;
        } else {
          const now = new Date();
          const isToday = date.toDateString() === now.toDateString();
          const isYesterday = new Date(now.getTime() - 86400000).toDateString() === date.toDateString();
          
          if (isToday) {
            return `${hours}:${minutes}`;
          } else if (isYesterday) {
            return `昨天 ${hours}:${minutes}`;
          } else if (date.getFullYear() === now.getFullYear()) {
            return `${month}-${day} ${hours}:${minutes}`;
          } else {
            return `${year}-${month}-${day}`;
          }
        }
      } catch (error) {
        console.error('格式化时间失败:', error);
        return time;
      }
    },
    
    // 刷新数据
    refresh() {
      this.loadBillList();
    },
    
    // 游客点击分享拦截 + 标记分享意图（保存前置由 touchstart 触发）
    async onShareClick() {
      console.log('onShareClick: shareIntent set to true');
      this.shareIntent = true;
    },

    // 触摸开始时预保存成员，尽量在分享回调前拿到ID
    async prepareShareMember() {
       if (this._sharePreparing) {
         return;
       }
       if (!this.currentBill) {
         return;
       }
       if (this.shareMemberId) {
         return;
       }
       this._sharePreparing = true;
       try {
         const id = await prepareBillMember(this.currentBill);
         if (id) {
           this.shareMemberId = id;
         }
       } catch (e) {
         console.warn('prepareShareMember failed', e);
       } finally {
         this._sharePreparing = false;
       }
     },

    // 保存账单信息到成员表（兜底使用）
    async saveBillToMember() {
      if (!this.currentBill) return null;
      try {
        const memberId = await prepareBillMember(this.currentBill);
        return memberId;
      } catch (error) {
        console.error('保存到成员表失败:', error);
        return null;
      }
    },
    
    // 组件内部分享方法，供父页面调用（同步返回）
    getShareInfo(force = false) {
      // 优先尝试统一解析
      const info = resolveShareInfo({
        currentBill: this.currentBill,
        billPopupVisible: this.billPopupVisible,
        shareIntent: force ? true : this.shareIntent,
        shareMemberId: this.shareMemberId
      });
      if (info && info.path && info.path.indexOf('/pages/member/acceptInvitation') === 0) {
        // 账单分享时追加回调与状态重置
        const withCallbacks = {
          ...info,
          success: () => {
            console.log('分享成功');
            uni.showToast({ title: '分享成功', icon: 'success' });
          },
          fail: (err) => {
            console.error('分享失败:', err);
          }
        };
        this.shareIntent = false;
        return withCallbacks;
      }
      return info;
    },

    // 辅助：是否有字体图标（后端给非空字符串）
    hasFontIcon(val) {
      return typeof val === 'string' && val.trim().length > 0;
    },
    // 辅助：统一去掉可能的前缀 icon-
    normalizeIcon(val) {
      if (!val || typeof val !== 'string') return '';
      const name = val.trim();
      return name.startsWith('icon-') ? name.slice(5) : name;
    },
    // 辅助：取分类名首字（为空给默认 '类'）
    firstChar(name) {
      if (!name || typeof name !== 'string') return '账';
      const s = name.trim();
      return s.length > 0 ? s[0] : '账';
    }
  }
};
</script>

<style scoped>
.bill-list-component {
  width: 100%;
  min-height: 300rpx;
  padding: 0 20rpx;
  box-sizing: border-box;
}

.bills-list {
  width: 100%;
  min-height: 300rpx;
  border-radius: 10rpx;
}

.bill-item {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  border-radius: 10rpx;
  box-sizing: border-box;
}

.bill-main-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
  width: 100%; /* 确保主信息区域占满宽度 */
}

.bill-amount {
  font-size: 32rpx;
  font-weight: bold;
  padding-left: 20rpx; /* 给金额字段左侧添加内边距，避免与其他内容太挤 */
  min-width: 120rpx; /* 设置最小宽度，确保金额完整显示 */
  text-align: right; /* 确保金额右对齐 */
}

.bill-deleted {
  opacity: 0.6;
}

/* 移除背景色的账单图标 */
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

.class-icon {
  margin-right: 10rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* 首字图标样式 */
.first-char-icon {
  font-size: 44rpx;
  line-height: 60rpx;
  text-align: center;
  display: inline-block;
  font-weight: bold;
  color: #faad14;
  width: 60rpx;
  height: 60rpx;
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
  font-size: 20rpx;
  color: #999999;
  margin-left: auto;
}

.empty-bills {
  text-align: center;
  padding: 60rpx 0;
  color: #999999;
  font-size: 28rpx;
}

.loading-container {
  padding: 40rpx 0;
  text-align: center;
}

.error-container {
  padding: 20rpx;
  text-align: center;
  color: #ff4d4f;
  font-size: 28rpx;
}

.error-text {
  color: #ff4d4f;
}

/* 统计信息样式 */
.bill-statistics {
  background-color: #ffffff;
  margin: 20rpx;
  padding: 20rpx;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-around;
}

.statistics-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.statistics-label {
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 8rpx;
}

.statistics-value {
  font-size: 32rpx;
  font-weight: bold;
}

.statistics-value.income {
  color: #07c160;
}

.statistics-value.expense {
  color: #ff4d4f;
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
  width: 100%;
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

.popup-actions {
  display: flex;
  align-items: center;
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

.action-text {
  margin-right: 20rpx;
  padding: 10rpx;
  font-size: 28rpx;
  color: #1989fa;
  line-height: 40rpx;
  white-space: nowrap;
  background-color: transparent;
  border: none;
}

/* 分享按钮美化：柔和蓝色底、圆角与细边框 */
.action-text.action-share {
  background: linear-gradient(90deg, #eaf6ff, #f5fbff);
  color: #1677ff;
  border: 1rpx solid #d0e7ff;
  border-radius: 24rpx;
}
.action-text.action-share:active { opacity: 0.92; }

/* 评论按钮美化：柔和暖色底、圆角与细边框 */
.action-text.action-comment {
  background: linear-gradient(90deg, #fff3e9, #fff8f2);
  color: #ff7a45;
  border: 1rpx solid #ffd8bf;
  border-radius: 24rpx;
}
.action-text.action-comment:active { opacity: 0.92; }

.popup-body {
  padding: 32rpx 24rpx;
}

.bill-detail-info {
  padding: 30rpx;
  margin-bottom: 30rpx;
  background-color: #fafafa;
  border-radius: 10rpx;
  display: flex;
  flex-direction: column;
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



/* 账单ID样式 */
.bill-id {
  position: absolute;
  top: 10rpx;
  left: 10rpx;
  font-size: 24rpx;
  color: #007AFF;
  font-weight: normal;
  z-index: 1;
 }
 .bill-tag {
   font-size: 20rpx;
   padding: 4rpx 10rpx;
   border-radius: 8rpx;
   margin-right: 10rpx;
   line-height: 1.2;
 }
 
 .tag-ledger {
   color: #1E80FF;
 }
 
 .tag-account {
   color: #7E3AF2;
 }
 
 .budget-in {
   color: #2E7D32;
 }
 
 .budget-out {
   color: #D4380D;
 }
 
 </style>