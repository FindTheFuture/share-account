<template>
  <view class="firstpage">
    <!-- 账本选择栏 -->
    <view class="ledger-select-bar">
      <view class="ledger-select-content">
          <!-- 左侧：账本选择 -->
          <view class="ledger-select-left" @click="openDrawer">
            <view class="ledger-name-background">
              <text class="ledger-select-text">{{ selectedLedger ? selectedLedger.name : '选择账本' }}</text>
              <uni-icons type="info" size="18" color="#999" class="ledger-info-icon" @click.stop="onNotifyTipClick" />
            </view>
          </view>
          <!-- 年月选择 -->
          <view class="month-select-wrapper" @click="openMonthPicker">
            <view class="month-select-background">
              <text class="month-select-text">{{ displayMonthText }}</text>
              <uni-icons type="arrowdown" size="14" color="#666" class="month-select-icon" />
            </view>
          </view>
          <!-- 右侧图标区域 -->
          <view class="icon-container">
            <!-- 显示饼状图图标 -->
            <view class="icon-item" @click="gotoStatistics">
              <view class="icon-circle">
                <custom-icon type="bingzhuangtu" :size="22" color="#9254de" class="icon-with-animation"></custom-icon>
              </view>
            </view>
            <!-- 消息图标 -->
            <view class="message-icon-wrapper" @click.stop="gotoMessageList">
              <view class="icon-circle">
                <custom-icon type="lingdang" :size="24" color="#9254de" class="icon-with-animation"></custom-icon>
              </view>
              <!-- 未读消息红点/数字角标 -->
              <view v-if="unreadCount > 0" class="message-badge">
                <text class="badge-text">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
              </view>
            </view>
          </view>
        </view>
    </view>
    
    <!-- 消息通告栏 -->
    <view v-if="latestMessage" class="notice-bar-container animate-slide-in animate-slide-in-1">
      <uni-notice-bar 
        :text="latestMessage.title" 
        show-get-more 
        @click="handleNoticeClick" 
        @getmore="gotoMessageList"
      />
    </view>

    <!-- 引导卡片 -->
    <GuideCard v-if="showGuideCard" :card="guideCard" @close="markGuideCardRead" class="animate-slide-in animate-slide-in-2" />

    <!-- 手机号填写提示弹窗 -->
    <uni-popup ref="phonePromptPopup" type="center" :mask-click="false">
      <view class="phone-prompt-card">
        <view class="phone-prompt-header">
          <text class="phone-prompt-title">重要提醒</text>
        </view>
        <view class="phone-prompt-body">
          <text class="phone-prompt-text">微信登录即将关闭，请及时填写手机号，方便下次手机号登录</text>
        </view>
        <view class="phone-prompt-footer">
          <button class="phone-prompt-btn cancel" @click="closePhonePrompt">稍后再说</button>
          <button class="phone-prompt-btn confirm" @click="goToEditUser">去填写</button>
        </view>
      </view>
    </uni-popup>

    <!-- 主内容区域 -->
    <view class="content">
      <view class="main-content" v-if="selectedLedger">
        
        <!-- 预算统计区域 -->
        <view class="budget-card">

          <view class="budget-card-header">
            <view class="budget-card-title">
              <text>{{ budgetCardTitle }}</text>
              <uni-icons type="info" size="18" color="#fff" class="budget-card-info" @click.stop="onBudgetTipClick" />
            </view>
            <view class="budget-card-total" v-if="selectedLedger && !selectedLedger.isShared" @click.stop="gotoBudgetList">
              <view class="budget-total-background">
                <text>总 {{ formatAmount(budgetTotal) }}</text>
                <custom-icon type="bianji" :size="18" color="#fff" />
              </view>
            </view>
          </view>
          
          <view class="budget-card-content">
            <view class="budget-progress">
              <!-- 圆形进度条（使用qiun-data-charts组件） -->
              <view class="arcbar-box" @click="gotoStatistics">
                <view class="arcbar-container">
                  <qiun-data-charts 
                    type="arcbar"
                    :opts="arcOpts"
                    :chartData="arcChartData"
                    :canvas2d="true"
                  />
                </view>
              </view>
              
              <!-- 预算数据 -->
              <view class="budget-data">
                <view class="budget-data-item">
                  <text class="budget-data-label">已消费</text>
                  <text class="budget-data-value">{{ formatAmount(budgetSpent) }}</text>
                </view>
                <view class="budget-data-item budget-data-item-with-divider">
                  <div class="budget-data-label">剩余</div>
                  <div class="budget-data-value">{{ formatAmount(budgetRemaining) }}</div>
                </view>
              </view>
            </view>
            
            <!-- 预算统计 -->
            <view class="budget-stats">
              <view class="budget-stats-item">
                <text class="budget-stats-label">月日均消费</text>
                <text class="budget-stats-value">{{ formatAmount(dailyAverage) }}</text>
              </view>
              <view class="budget-stats-item">
                <text class="budget-stats-label">剩余每日可消费</text>
                <text class="budget-stats-value text-right">{{ formatAmount(remainingDaily) }}</text>
              </view>
            </view>
          </view>
          
          <view v-if="budgetTotal === '0.00'" class="no-budget-tip">
            <text>尚未设置预算，点击右上角设置预算</text>
          </view>
        </view>

        <!-- 收支统计区域 -->
        <view class="stats-section animate-bubble animate-bubble-2">
          <view class="stats-card income-card">
            <!-- 火箭动画 -->
            <view class="rocket-container">
              <view class="rocket">🚀</view>
            </view>
            <view class="stats-card-content">
              <view class="stats-card-label-container">
                <!-- 图标缩小到四分之一大小，去掉背景色 -->
                <custom-icon type="Gc_63_public-RiseOutlined" :size="18" color="#07c160" style="font-weight: bold;" />
                <text class="stats-card-label">月收入</text>
              </view>
              <text class="stats-card-value income">{{ formatAmount(monthlyIncome) }}</text>
            </view>
          </view>
          <view class="stats-card expense-card">
            <view class="stats-card-content">
              <view class="stats-card-label-container">
                <!-- 图标缩小到四分之一大小，去掉背景色 -->
                <custom-icon type="xiajiang" :size="22" color="#07c160" style="font-weight: bold;" />
                <text class="stats-card-label">月支出</text>
              </view>
              <text class="stats-card-value expense">{{ formatAmount(monthlyExpense) }}</text>
              <view class="stats-card-sub-items">
                <view class="stats-card-sub-item">
                  <text class="stats-card-sub-label">计入预算</text>
                  <text class="stats-card-sub-value">{{ formatAmount(budgetSpent) }}</text>
                </view>
                <view class="stats-card-sub-item">
                  <text class="stats-card-sub-label">不计入预算</text>
                  <text class="stats-card-sub-value">{{ formatAmount(budgetExcluded) }}</text>
                </view>
              </view>
            </view>
          </view>
          <view class="stats-card balance-card">
            <view class="balance-emoji-container">
              <view v-if="monthlyBalance > 0 && monthlyBalance > monthlyIncome * 0.3" class="balance-emoji happy">😎</view>
              <view v-else-if="monthlyBalance > 0 && monthlyBalance <= monthlyIncome * 0.3" class="balance-emoji thinking">🤔</view>
              <view v-else class="balance-emoji sad">😭</view>
            </view>
            <view class="stats-card-content">
              <view class="stats-card-label-container">
                <!-- 图标缩小到四分之一大小，去掉背景色 -->
                <uni-icons type="wallet-filled" size="20" color="#9254de" />
                <text class="stats-card-label">月结余</text>
                <uni-icons type="info" size="18" color="#9254de" class="stats-card-info" @click="onBalanceTipClick" />
              </view>
              <text class="stats-card-value balance">{{ formatAmount(monthlyBalance) }}</text>
            </view>
          </view>
        </view>

        <!-- 最近10条账单区域 -->
        <view class="recent-bills-section">
          <view class="recent-bills-header">
            <text class="recent-bills-title">{{ recentBillsTitle }}</text>
            <text class="recent-bills-view-all" @click="gotoBillList">全部账单 </text>
          </view>
          <view class="recent-bills-list">
            <!-- 账单列表项将通过组件渲染 - 使用无动画效果的版本 -->
            <bill-list-component 
              ref="billListComponent"
              :showLedgerTag="false"
              :request-params="recentBillsParams"
              :show-pagination="false"
              :show-statistics="false"
              :current-user-id="currentUserId"
              :accounts="accounts"
              :skip-time-validation="true"
              @refresh-list="onBillsChanged"
              @popup-visible="onBillPopupVisible"
            />
          </view>
        </view>
      </view>
      <view v-else class="no-ledger-tip animate-slide-in">
        <text>请选择一个账本</text>
      </view>
    </view>
    
    <!-- 账本选择弹窗改为复用组件 -->
    <LedgerSelectPopup
      ref="ledgerPopup"
      :selectedLedger="selectedLedger"
      :autoSelectDefault="true"
      @select="selectLedger"
    />
    
    <!-- 年月选择弹窗 -->
    <uni-popup ref="monthPickerPopup" type="center" :mask-click="false">
      <view class="month-picker-container">
        <view class="month-picker-header">
          <text class="month-picker-title">选择年月</text>
          <view class="month-picker-actions">
            <custom-icon type="guanbi" :size="20" color="#999" @tap="closeMonthPicker" class="icon-hover"></custom-icon>
          </view>
        </view>
        <view class="month-picker-divider"></view>
        <view class="month-picker-content">
          <!-- 年份选择 -->
          <view class="picker-section">
            <text class="picker-label">年份</text>
            <picker-view class="picker-view" :value="pickerYearValue" @change="onYearChange" indicator-style="height: 80rpx;">
              <picker-view-column>
                <view class="picker-item" v-for="(year, index) in yearList" :key="index">{{ year }}年</view>
              </picker-view-column>
            </picker-view>
          </view>
          <!-- 月份选择 -->
          <view class="picker-section">
            <text class="picker-label">月份</text>
            <picker-view class="picker-view" :value="pickerMonthValue" @change="onMonthChange" indicator-style="height: 80rpx;">
              <picker-view-column>
                <view class="picker-item" v-for="(month, index) in availableMonths" :key="index">{{ month.value }}月</view>
              </picker-view-column>
            </picker-view>
          </view>
        </view>
        <view class="month-picker-divider"></view>
        <view class="month-picker-footer">
          <view class="month-picker-btn cancel-btn" @click="closeMonthPicker">
            <text class="cancel-btn-text">取消</text>
          </view>
          <view class="month-picker-btn confirm-btn" @click="confirmMonthSelection">
            <text class="confirm-btn-text">确定</text>
          </view>
        </view>
      </view>
    </uni-popup>
    
    <!-- 悬浮添加按钮 -->
    <view v-if="!isBillPopupVisible" class="floating-add-button animate-slide-in animate-slide-in-5" @click="gotoAddBill">
      <view class="floating-button-circle">
        <text class="floating-button-plus">+</text>
      </view>
      <text class="floating-button-text">记一笔</text>
    </view>
  </view>
</template>

<script>
import request from '@/common/request.js';
import backUrl from '@/common/back_url.js';
import uniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import uniNoticeBar from '@/uni_modules/uni-notice-bar/components/uni-notice-bar/uni-notice-bar.vue';
import uniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
import billListComponent from '@/components/bill-list-component.vue';
import LedgerSelectPopup from '@/components/ledger-select-popup.vue';
import GuideCard from '@/components/guide-card.vue';
import { formatAmount } from '@/common/util.js';
import { isLoggedIn, clearLoginState } from '@/common/isLoggedIn.js';

export default {
  components: {
    uniIcons,
    uniNoticeBar,
    uniPopup,
    billListComponent,
    LedgerSelectPopup,
    GuideCard
  },
  data() {
      return {
        ledgerList: [], // 所有账本列表
        sharedLedgers: [], // 共享账本列表
        personalLedgers: [], // 个人账本列表
        defaultLedgerName: '', // 默认账本名称
        selectedLedger: null, // 当前选中的账本
        isLoading: false, // 加载状态
        isFromLedgerManage: false, // 是否从账本管理页面返回
        remainingAiCount: 0, // AI剩余次数

        
        // 统计数据
        monthlyExpense: '0.00', // 本月支出
        monthlyIncome: '0.00', // 本月收入
        monthlyBalance: '0.00', // 本月结余
        
        // 预算数据
        budgetTotal: '0.00', // 本月预算总额
        budgetSpent: '0.00', // 已消费金额（计入预算）
        budgetExcluded: '0.00', // 不计入预算已消费金额
        budgetRemaining: '0.00', // 剩余预算
        dailyAverage: '0.00', // 日均消费
        remainingDaily: '0.00', // 剩余每日可消费
        
        // 最近7天账单
        recentBills: [],
        // 当前用户ID
        currentUserId: '',
        // 账户列表
        accounts: [],
        // 最近10条账单参数
        recentBillsParams: {
          pageNum: 1,
          pageSize: 10,
          ledgerId: '',
          status: '0',
          startTime: '',
          endTime: ''
        },
        // 引导卡片
        guideCard: null,
        showGuideCard: false,
        isGuest: !!uni.getStorageSync('isGuest'),
        
        // 弹窗相关
        billPopupVisible: false,
        currentBill: null,
        // 账单弹窗可见性标记，用于控制悬浮添加按钮的显示/隐藏
        isBillPopupVisible: false,
        // 手机号填写提示弹窗
        showPhonePrompt: false,
        // 消息相关
        latestMessage: null,
        unreadCount: 0,
        
        // 年月选择相关
        selectedYear: new Date().getFullYear(), // 当前选中的年份
        selectedMonth: new Date().getMonth() + 1, // 当前选中的月份
        tempSelectedYear: new Date().getFullYear(), // 临时选中的年份（弹窗中）
        tempSelectedMonth: new Date().getMonth() + 1, // 临时选中的月份（弹窗中）
        yearList: [], // 年份列表
        monthList: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], // 月份列表
        pickerYearValue: [], // picker-view 年份选择器的值
        pickerMonthValue: [], // picker-view 月份选择器的值
        
        // Arcbar 圆形进度条数据与样式（整圆）
        arcChartData: {},
        arcOpts: {
          color: ['#FFFFFF'],
          padding: undefined,
          title: { name: '', fontSize: 0, color: '#FFFFFF' },
          subtitle: { name: '', fontSize: 0, color: '#FFFFFF' },
          extra: {
            arcbar: {
              type: 'circle',
              width: 10,
              backgroundColor: 'rgba(255, 255, 255, 0.3)',
              startAngle: 1.5,
              endAngle: 0.25,
              gap: 2
            }
          }
        },
        displayPercentText: '0%',
        isOverBudget: false,
        stageColor: '#2FC25B'
    };
  },
  computed: {
    messageUnreadCount() {
      return this.$store.state.message?.unreadCount || this.unreadCount;
    },
    messageLatest() {
      return this.$store.state.message?.latestMessage || this.latestMessage;
    },
    /**
     * 显示年月文本：当前年月显示"本月"，其他显示"年-月"格式
     */
    displayMonthText() {
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth() + 1;

      if (this.selectedYear === currentYear && this.selectedMonth === currentMonth) {
        return '本月';
      }
      return `${this.selectedYear}-${String(this.selectedMonth).padStart(2, '0')}`;
    },
    /**
     * 预算卡片标题：当前年月显示"本月预算"，其他显示"年-月预算"
     */
    budgetCardTitle() {
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth() + 1;

      if (this.selectedYear === currentYear && this.selectedMonth === currentMonth) {
        return '本月预算';
      }
      return `${this.selectedYear}-${String(this.selectedMonth).padStart(2, '0')}预算`;
    },
    /**
     * 最近账单标题：当前年月显示"本月最近10条账单"，其他显示"年-月最近10条账单"
     */
    recentBillsTitle() {
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth() + 1;

      if (this.selectedYear === currentYear && this.selectedMonth === currentMonth) {
        return '本月最近10条账单';
      }
      return `${this.selectedYear}-${String(this.selectedMonth).padStart(2, '0')}最近10条账单`;
    },
    /**
     * 可用的月份列表（根据选中的年份过滤掉未来月份）
     */
    availableMonths() {
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth() + 1;

      // 如果选中的年份大于当前年份，返回空数组
      if (this.tempSelectedYear > currentYear) {
        return [];
      }

      // 如果选中的年份等于当前年份，只显示到当前月份
      if (this.tempSelectedYear === currentYear) {
        return this.monthList
          .filter(month => month <= currentMonth)
          .map(month => ({ value: month }));
      }

      // 如果选中的年份小于当前年份，显示所有月份
      return this.monthList.map(month => ({ value: month }));
    }
  },
  onLoad() {
  },
  async onShow() {
    // 检查登录状态
    await this.checkLoginStatus();

    // 更新游客状态
    this.isGuest = !!uni.getStorageSync('isGuest');
    // 重置选中的账本，确保登录后能选择新的默认账本
    this.selectedLedger = null;
    // 初始化年月选择器数据
    this.initYearList();
    // 更新时间范围参数，设置为最近一年
    this.updateTimeRange();
    // 先确保账本已选中，再加载数据
    await this.waitForLedgerPopupReady();
    if (this.$refs.ledgerPopup && typeof this.$refs.ledgerPopup.initAutoSelect === 'function') {
      await this.$refs.ledgerPopup.initAutoSelect();
    }
    // 无论是否有选中的账本，都重新加载数据
    // 确保登录后能获取最新数据
    await this.loadAllData();
    this.tryShowGuideCard();
    this.checkPhonePrompt();
    // 显示分享菜单（支持多平台）
    try {
      // #ifdef MP-WEIXIN
      uni.showShareMenu({ withShareTicket: true, menus: ['shareAppMessage', 'shareTimeline'] });
      // #endif
      // #ifdef MP-TOUTIAO
      uni.showShareMenu({ withShareTicket: true, menus: ['share'] });
      // #endif
      // #ifndef MP-WEIXIN && !MP-TOUTIAO
      uni.showShareMenu({ withShareTicket: true });
      // #endif
    } catch (e) { /* ignore */ }
    
    // 获取AI剩余次数
    this.fetchRemainingAiCount();
  },
  
  onReady() {
  },
  // 下拉刷新方法
  onPullDownRefresh() {
    if (this.selectedLedger) {
      // 重新加载所有数据，并刷新最近10条账单组件
      try {
        this.loadAllData();
        
      } catch (error) {
        console.error('下拉刷新失败:', error);
      }
    }
    // 停止下拉刷新动画
    uni.stopPullDownRefresh();
  },

  watch: {
    // 监听预算数据变化，重新初始化进度条
    budgetSpent: {
      handler: function() {
        // 直接调用initProgressChart
        if (typeof this.initProgressChart === 'function') {
          this.initProgressChart();
        } else {
          console.error('initProgressChart is not a function');
        }
      },
      deep: true
    },
    budgetTotal: {
      handler: function() {
        // 直接调用initProgressChart
        if (typeof this.initProgressChart === 'function') {
          this.initProgressChart();
        } else {
          console.error('initProgressChart is not a function');
        }
      },
      deep: true
    }
  },
  methods: {
    /**
     * 显示账本统计提示
     */
    onNotifyTipClick() {
      uni.showToast({
        title: '按账本统计，包括预算、收入、支出、结余、账单列表',
        icon: 'none',
        duration: 2000
      });
    },
    
    /**
     * 显示预算提示
     */
    onBudgetTipClick() {
      uni.showToast({
        title: '当前账本，月所有预算的总和',
        icon: 'none',
        duration: 2000
      });
    },
    
    /**
     * 显示结余提示
     */
    onBalanceTipClick() {
      uni.showToast({
        title: '当前账本，月收入-月支出',
        icon: 'none',
        duration: 2000
      });
    },

    // 检查登录状态，如果未登录则自动获取游客账号
    async checkLoginStatus() {
      try {
        const result = await isLoggedIn();

        // 已登录：isGuest=false + token + additionalId + 用户存在
        if (result.loggedIn) {
          const additionalId = uni.getStorageSync('additionalId');
          this.currentUserId = additionalId;
          this.isGuest = false;
          return true;
        }

        // 未登录：清除登录态缓存
        clearLoginState();
        this.currentUserId = '';
        this.isGuest = false;

        // 调用游客登录接口
        uni.showLoading({ title: '加载中...' });

        const response = await uni.request({
          url: `${this.$backUrlConfig.baseUrl}${this.$backUrlConfig.endpoints.login_guest}`,
          method: 'POST'
        });

        const data = response.data;

        if (data.code == 200 && data.data && data.data.token) {
          const { token, expires_in, additionalId, thunder, canSendMessage, guideCard } = data.data;
          this.saveToken(token, expires_in, additionalId, thunder, canSendMessage);
          if (guideCard) {
            try { uni.setStorageSync('guideCard', JSON.stringify(guideCard)); } catch (e) { uni.setStorageSync('guideCard', guideCard); }
          }
          uni.setStorageSync('isGuest', true);
          this.isGuest = true;
          this.currentUserId = additionalId || '';

          uni.hideLoading();
          return true;
        } else {
          console.error('游客登录接口返回错误:', data);
          uni.hideLoading();
          return false;
        }
      } catch (error) {
        console.error('游客登录失败:', error);
        uni.hideLoading();
        return false;
      }
    },
    
    // 保存token信息
    saveToken(token, expires_in, additionalId, thunder, canSendMessage) {
      uni.setStorageSync('token', token);
      uni.setStorageSync('additionalId', additionalId);
      const expireAt = Date.now() + expires_in * 1000;
      uni.setStorageSync('expireAt', expireAt);

      uni.setStorageSync('thunder', thunder);
      uni.setStorageSync('canSendMessage', canSendMessage);

      const app = getApp();
      app.globalData.token = token;
      app.globalData.expireAt = expireAt;
      app.globalData.additionalId = additionalId;
      app.globalData.thunder = thunder;
      app.globalData.canSendMessage = canSendMessage;
    },

    /**
     * 获取AI剩余识别次数
     */
    async fetchRemainingAiCount() {
      try {
        const res = await this.$request({
          url: backUrl.endpoints.userMember_getRemainingAiCount,
          method: 'GET',
        });
        if (res && typeof res === 'number') {
          this.remainingAiCount = res;
        }
      } catch (error) {
        console.error('获取AI剩余次数失败:', error);
        // 出错时设置为0
        this.remainingAiCount = 0;
      }
    },

    /**
     * 等待 ledgerPopup 组件就绪
     * 在 onShow 时组件可能还未挂载，需要多次尝试
     */
    waitForLedgerPopupReady() {
      return new Promise((resolve) => {
        let retries = 0;
        const maxRetries = 10;
        const check = () => {
          if (this.$refs.ledgerPopup) {
            resolve();
          } else if (retries < maxRetries) {
            retries++;
            setTimeout(check, 100);
          } else {
            resolve();
          }
        };
        check();
      });
    },

    formatAmount,
    // 获取消息数据
    async fetchMessageData() {
      try {
        // 直接从API获取消息数据，避免调用不存在的Vuex actions
        // 获取最新消息
        const latestMsgRes = await request({
          url: backUrl.endpoints.message_latest,
          method: 'GET'
        });
        // 只有当返回数据包含有效消息时才设置latestMessage
        if (latestMsgRes && latestMsgRes.id && latestMsgRes.title) {
          this.latestMessage = latestMsgRes;
        } else {
          // 没有有效消息时，清空latestMessage以隐藏通告栏
          this.latestMessage = null;
        }
        
        // 获取未读消息数，使用正确的API路径
        const unreadCountRes = await request({
          url: backUrl.endpoints.message_unreadCount,
          method: 'GET'
        });
        // 修复：0 也应当覆盖本地未读数，不能被忽略
        const count = Number(unreadCountRes);
        if (Number.isFinite(count)) {
          this.unreadCount = count;
        } else {
          this.unreadCount = 0;
        }
      } catch (error) {
        console.error('获取消息数据失败:', error);
        // 发生错误时设置默认值，避免显示异常
        this.unreadCount = 0;
      }
    },
    
    // 跳转到消息列表页
    gotoMessageList() {
      uni.navigateTo({
        url: '/pages/message/messageList'
      });
    },
    
    // 处理通告栏点击
    handleNoticeClick() {
      if (this.latestMessage || this.$store.state.message?.latestMessage) {
        const msg = this.$store.state.message?.latestMessage || this.latestMessage;
        uni.navigateTo({
          url: `/pages/message/messageDetail?id=${msg.id}`
        });
      }
    },
    // 跳转到统计页面
    gotoStatistics() {
      uni.navigateTo({
        url: '/pages/statistics/statistics'
      });
    },

    // 账单列表刷新事件处理
    onBillsChanged(payload) {
      if (!this.selectedLedger) return;

      // 分页事件：仅更新页码
      if (payload && typeof payload === 'object' && ('pageNum' in payload)) {
        this.recentBillsParams = {
          ...this.recentBillsParams,
          pageNum: payload.pageNum
        };
        return;
      }

      // 其他事件：刷新所有数据
      this.loadAllData();
    },
    
    // 加载所有数据（账单和预算）
    async loadAllData() {
      // 同步更新账单列表查询参数
      this.initRecentBillsParams();
      // 更新时间范围参数，使用选中的年月
      this.updateTimeRange();

      // 构建要执行的Promise数组
      const promises = [
        this.fetchMessageData(),
        this.fetchRemainingAiCount()
      ];

      // 如果有选中的账本，添加账本相关的数据加载
      if (this.selectedLedger) {
        promises.push(
          this.loadMonthlyBills(),
          this.loadMonthlyBudget(),
          this.loadUserMonthlyTotalExpense()
        );

        // 重新赋值以触发子组件watch（deep+immediate），确保查询最新列表
        this.recentBillsParams = {
          ...this.recentBillsParams,
          pageNum: 1,
          ledgerId: this.selectedLedger.id,
          // 时间范围已经在 updateTimeRange 中更新
        };
      }

      return Promise.all(promises).then(() => {
        // 确保数据加载完成后重新初始化进度条
        this.$nextTick(() => {
          this.initProgressChart();
        });
      }).catch(error => {
        console.error('加载数据失败:', error);
        // 即使出错也尝试绘制进度条，避免显示空白
        this.$nextTick(() => {
          this.initProgressChart();
        });
        return Promise.reject(error);
      });
    },
    
    /**
     * 加载账单统计数据
     * 使用选中的年月作为请求参数
     */
    async loadMonthlyBills() {
      try {
        if (!this.selectedLedger) return;

        // 使用选中的年月作为请求参数
        const year = this.selectedYear;
        const month = this.selectedMonth;

        // 调用新的API接口获取本月收支统计
        const res = await request({
          url: `${backUrl.endpoints.bill_getMonthlyStatisticsByLedgerId}/${this.selectedLedger.id}`,
          method: 'GET',
          data: { year, month }
        });

        // 检查响应数据是否有效
        if (res) {
          // 注意：后端返回的金额以分为单位，需要转换为元
          const income = res.income || 0;
          const expense = res.expense || 0;
          const balance = res.balance || 0;

          this.monthlyExpense = (expense / 100).toFixed(2);
          this.monthlyIncome = (income / 100).toFixed(2);
          this.monthlyBalance = (balance / 100).toFixed(2);

          // 预算统计将在获取用户本月总支出后统一计算
          this.calculateBudgetStats();
        }
      } catch (error) {
        console.error('加载月账单统计失败:', error);
        // 清空数据
        this.monthlyExpense = '0.00';
        this.monthlyIncome = '0.00';
        this.monthlyBalance = '0.00';
      }
    },
    
    /**
     * 加载用户指定月份总支出（按当前选择账本筛选）
     * 使用选中的年月作为请求参数
     */
    async loadUserMonthlyTotalExpense() {
      try {
        // 使用选中的年月作为请求参数
        const year = this.selectedYear;
        const month = this.selectedMonth;

        // 调用API接口获取用户指定月份总支出（按账本筛选）
        const res = await request({
          url: backUrl.endpoints.bill_getUserMonthlyTotalExpense,
          method: 'GET',
          data: {
            year,
            month,
            ledgerId: this.selectedLedger ? this.selectedLedger.id : null
          }
        });

        // 检查响应数据是否有效
        if (res && res !== undefined) {
          // 注意：后端返回的金额以分为单位，需要转换为元
          const includedFen = (res && res.budgetIncluded !== undefined) ? Number(res.budgetIncluded) : 0;
          const excludedFen = (res && res.budgetExcluded !== undefined) ? Number(res.budgetExcluded) : 0;

          // 设置用户指定月份支出拆分（按当前账本）
          this.budgetSpent = (includedFen / 100).toFixed(2);
          this.budgetExcluded = (excludedFen / 100).toFixed(2);
          this.calculateBudgetStats();
        }
      } catch (error) {
        console.error('加载用户指定月份总支出失败:', error);
        // 清空数据 计入预算与不计入预算
        this.budgetSpent = '0.00';
        this.budgetExcluded = '0.00';
        this.calculateBudgetStats();
      }
    },
    
    /**
     * 加载预算数据
     * 使用选中的年月作为请求参数
     */
    async loadMonthlyBudget() {
      try {
        // 使用选中的年月作为请求参数
        const year = this.selectedYear;
        const month = this.selectedMonth;
        const ledgerId = this.selectedLedger ? this.selectedLedger.id : null;

        if (!ledgerId) {
          this.budgetTotal = '0.00';
          this.calculateBudgetStats();
          return;
        }

        // 新接口：按年月+账本聚合预算明细并返回总额（分）
        const summaryRes = await request({
          url: backUrl.endpoints.budget_getByYearAndMonthAndLedger,
          method: 'GET',
          data: { year, month, ledgerId }
        });

        const totalFen = summaryRes && summaryRes.totalBalance !== undefined && summaryRes.totalBalance !== null
          ? Number(summaryRes.totalBalance)
          : 0;

        // 分转元后展示到首页 budgetTotal
        this.budgetTotal = (Number.isFinite(totalFen) ? totalFen : 0) / 100;
        this.budgetTotal = Number(this.budgetTotal).toFixed(2);

        // 计算预算相关统计
        this.calculateBudgetStats();
      } catch (error) {
        console.error('加载月预算失败:', error);
        // 使用默认预算值
        this.budgetTotal = '0.00';
        this.calculateBudgetStats();
      }
    },
    
    // 计算预算统计数据
    calculateBudgetStats() {
      const budgetTotal = parseFloat(this.budgetTotal);
      const spent = parseFloat(this.budgetSpent);
      
      // 剩余预算，确保不小于0
      this.budgetRemaining = Math.max(budgetTotal - spent, 0).toFixed(2);
      
      // 获取本月天数
      const now = new Date();
      const daysInMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate();
      const today = now.getDate();
      const remainingDays = daysInMonth - today + 1;
      
      // 日均消费（已过去的天数），确保不小于0
      this.dailyAverage = today > 0 ? (spent / today).toFixed(2) : '0.00';
      
      // 剩余每日可消费，确保不小于0
      if (remainingDays > 0) {
        this.remainingDaily = Math.max((budgetTotal - spent) / remainingDays, 0).toFixed(2);
      } else {
        this.remainingDaily = '0.00';
      }
    },
    
    // 初始化账单列表参数
    initRecentBillsParams() {
      // 获取当前用户ID
      this.currentUserId = uni.getStorageSync('additionalId') || '';
    },
    
    
    // 跳转到账单列表页
    gotoBillList() {
      if (!this.selectedLedger) {
        uni.showToast({
          title: '请先选择账本',
          icon: 'none'
        });
        return;
      }
      uni.navigateTo({
        url: '/pages/bill/billList?ledgerId=' + this.selectedLedger.id
      });
    },
    
    // 跳转到预算列表页
    gotoBudgetList() {
      if (!this.selectedLedger) {
        uni.showToast({
          title: '请先选择账本',
          icon: 'none'
        });
        return;
      }
      uni.navigateTo({
        url: '/pages/budget/budgetList?ledgerId=' + this.selectedLedger.id
      });
    },
    
    // 计算预算进度百分比
    getProgressPercentage() {
      const total = parseFloat(this.budgetTotal);
      const spent = parseFloat(this.budgetSpent);
      if (!Number.isFinite(total) || !Number.isFinite(spent)) {
        return 0;
      }
      if (total <= 0) {
        // 预算为0且有支出：视为超额（显示∞%）
        if (spent > 0) return Infinity;
        return 0;
      }
      return Math.round((spent / total) * 100);
    },
    
    // 初始化圆形进度条（arcbar）
    initProgressChart() {
      this.$nextTick(() => {
        this.updateArcbarFromBudget();
      });
    },
    
    // 根据预算/支出更新 arcbar 数据与样式
    updateArcbarFromBudget() {
      const percent = this.getProgressPercentage();
      let overBudget = false;
      let displayText = '0%';
      let color = '#FFFFFF';
      let fraction = 0;
      
      if (percent === Infinity || percent > 100) {
        overBudget = true;
        fraction = 1; // 满圈
        displayText = percent === Infinity ? '∞%' : `${Math.round(percent)}%`;
      } else {
        const p = Math.max(0, Math.min(percent, 100));
        fraction = p / 100;
        displayText = `${p}%`;
      }
      
      this.stageColor = color;
      this.isOverBudget = overBudget;
      this.displayPercentText = displayText;
      
      // 更新图表配置与数据
      this.arcOpts = {
        ...this.arcOpts,
        color: [color],
        title: { name: this.displayPercentText, fontSize: 24, color },
        subtitle: { name: '已消费', fontSize: 14, color: 'rgba(255, 255, 255, 0.8)' }
      };
      this.arcChartData = {
        series: [
          {
            name: '已消费',
            color,
            data: fraction
          }
        ]
      };
    },
    
    
    // 简单的进度条绘制函数，适配小程序环境
    drawSimpleProgressBar(progress, color) {
      
      try {
        // 使用uni.createCanvasContext（旧版API，与canvas-id兼容）
        const ctx = uni.createCanvasContext('budgetProgressChart');
        if (!ctx) {
          console.error('无法创建canvas上下文');
          return;
        }
        
        // 清空画布
        ctx.setFillStyle('#FFFFFF');
        ctx.fillRect(0, 0, 120, 120);
        
        // 计算圆形的中心点和半径
        const centerX = 60;
        const centerY = 60;
        const radius = 35;
        const lineWidth = 6;
        
        // 绘制背景圆形
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, 0, 2 * Math.PI, false);
        ctx.setStrokeStyle('#e0e0e0');
        ctx.setLineWidth(lineWidth);
        ctx.stroke();
        
        // 绘制进度圆形（超过100%时仅绘制满圆，但文本显示真实值）
        const progressForArc = Math.max(0, Math.min(progress, 100));
        const progressAngle = -Math.PI/2 + (progressForArc / 100) * 2 * Math.PI;
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, -Math.PI/2, progressAngle, false);
        const validColor = color && color.trim() ? color : '#1989fa';
        ctx.setStrokeStyle(validColor);
        ctx.setLineWidth(lineWidth);
        ctx.stroke();
        
        // 绘制文字
        ctx.setFontSize(16);
        ctx.setFillStyle(validColor);
        ctx.setTextAlign('center');
        ctx.setTextBaseline('middle');
        ctx.fillText(progress + '%', centerX, centerY - 8);
        
        ctx.setFontSize(10);
        ctx.setFillStyle('#666666');
        ctx.fillText('已消费', centerX, centerY + 12);
        
        // 执行绘制
        ctx.draw();
      } catch (e) {
        console.error('绘制简单进度条失败:', e);
      }
    },

    // 打开账本选择弹窗
    openDrawer() {
      // 直接打开账本选择弹窗
      this.$refs.ledgerPopup.open();
    },

    // 关闭账本选择弹窗
    closeDrawer() {
      // 直接关闭账本选择弹窗
      this.$refs.ledgerPopup.close();
    },

    // 选择账本
    selectLedger(ledger) {
      this.selectedLedger = ledger;
      this.defaultLedgerName = ledger.name;
      this.closeDrawer();
      // 更新时间范围参数，设置为最近一年
      this.updateTimeRange();
      // 加载账单和预算数据
      this.loadAllData();
    },

    // 跳转到添加账单页面
    gotoAddBill() {
      if (!this.selectedLedger) {
        uni.showToast({
          title: '请先选择账本',
          icon: 'none'
        });
        return;
      }
      uni.navigateTo({
        url: '/pages/bill/addBill?ledgerId=' + this.selectedLedger.id
      });
    },
    
    // 处理账单弹窗显示/隐藏事件
    onBillPopupVisible(visible) {
      this.isBillPopupVisible = visible;
    },
    // 引导卡片：显示逻辑
    tryShowGuideCard() {
      try {
        const raw = uni.getStorageSync('guideCard');
        if (!raw) {
          this.showGuideCard = false;
          return;
        }
        const card = typeof raw === 'string' ? JSON.parse(raw) : raw;
        // 过期检查
        let expires = card.expiresAt;
        if (typeof expires === 'string') {
          const t = Date.parse(expires);
          expires = isNaN(t) ? null : t;
        }
        if (typeof expires === 'number' && Date.now() > expires) {
          this.showGuideCard = false;
          return;
        }
        const userId = this.currentUserId || 'guest';
        const readKey = `guideCardRead_${userId}`;
        const isRead = !!uni.getStorageSync(readKey);
        if (isRead) {
          this.showGuideCard = false;
          return;
        }
        this.guideCard = card;
        this.showGuideCard = !!this.isGuest;
      } catch (e) {
        this.showGuideCard = false;
      }
    },
    // 引导卡片：关闭并标记已读
    markGuideCardRead() {
      const userId = this.currentUserId || 'guest';
      const readKey = `guideCardRead_${userId}`;
      uni.setStorageSync(readKey, true);
      this.showGuideCard = false;
    },
    // 手机号填写提示弹窗：检查逻辑
    async checkPhonePrompt() {
      const userId = uni.getStorageSync('additionalId');
      const isGuest = !!uni.getStorageSync('isGuest');
      if (!userId || isGuest) {
        return;
      }
      try {
        const res = await this.$request({
          url: this.$backUrlConfig.endpoints.getUserInfo + userId,
          method: 'GET'
        });
        if (res && !res.phone) {
          this.$refs.phonePromptPopup.open();
        }
      } catch (e) {
        console.error('检查手机号失败:', e);
      }
    },
    // 关闭手机号提示弹窗
    closePhonePrompt() {
      this.$refs.phonePromptPopup.close();
    },
    // 跳转到编辑用户页面
    goToEditUser() {
      this.$refs.phonePromptPopup.close();
      const userId = uni.getStorageSync('additionalId');
      uni.navigateTo({
        url: `/pages/user/editUser/editUser?haoe=${userId}`
      });
    },
    
    /**
     * 更新时间范围参数
     * 使用选中的年月，开始时间为该月第一天，结束时间为该月最后一天
     */
    updateTimeRange() {
      // 使用选中的年月
      const year = this.selectedYear;
      const month = this.selectedMonth;

      // 计算选中月份的第一天
      const firstDayOfMonth = new Date(year, month - 1, 1);
      // 计算选中月份的最后一天
      const lastDayOfMonth = new Date(year, month, 0);

      // 格式化开始时间：YYYY-MM-DD HH:mm:ss
      const startTime = `${firstDayOfMonth.getFullYear()}-${String(firstDayOfMonth.getMonth() + 1).padStart(2, '0')}-${String(firstDayOfMonth.getDate()).padStart(2, '0')} 00:00:00`;
      // 格式化结束时间：YYYY-MM-DD HH:mm:ss
      const endTime = `${lastDayOfMonth.getFullYear()}-${String(lastDayOfMonth.getMonth() + 1).padStart(2, '0')}-${String(lastDayOfMonth.getDate()).padStart(2, '0')} 23:59:59`;

      // 更新时间范围参数
      this.recentBillsParams = {
        ...this.recentBillsParams,
        startTime,
        endTime
      };
    },

    /**
     * 初始化年份列表（从2020年到当前年份）
     */
    initYearList() {
      const now = new Date();
      const currentYear = now.getFullYear();
      const startYear = 2020;
      this.yearList = [];
      for (let year = startYear; year <= currentYear; year++) {
        this.yearList.push(year);
      }
    },

    /**
     * 打开年月选择弹窗
     */
    openMonthPicker() {
      // 重置临时选中的年月为当前选中的年月
      this.tempSelectedYear = this.selectedYear;
      this.tempSelectedMonth = this.selectedMonth;
      // 计算 picker-view 的值（索引）
      const yearIndex = this.yearList.findIndex(year => year === this.tempSelectedYear);
      const monthIndex = this.tempSelectedMonth - 1;
      // 设置 picker-view 的值
      this.pickerYearValue = [yearIndex >= 0 ? yearIndex : this.yearList.length - 1];
      this.pickerMonthValue = [monthIndex >= 0 ? monthIndex : 0];
      // 使用 nextTick 确保 picker-view 组件正确初始化滚动位置
      this.$nextTick(() => {
        this.$refs.monthPickerPopup.open();
      });
    },

    /**
     * 关闭年月选择弹窗
     */
    closeMonthPicker() {
      this.$refs.monthPickerPopup.close();
    },

    /**
     * 年份选择变化
     */
    onYearChange(e) {
      const index = e.detail.value[0];
      if (index >= 0 && index < this.yearList.length) {
        this.tempSelectedYear = this.yearList[index];
        // 更新 pickerYearValue
        this.pickerYearValue = [index];
        // 如果切换到当前年份，且临时选中的月份大于当前月份，则自动调整为当前月份
        const now = new Date();
        if (this.tempSelectedYear === now.getFullYear() && this.tempSelectedMonth > now.getMonth() + 1) {
          this.tempSelectedMonth = now.getMonth() + 1;
          // 同步更新月份选择器的值
          const monthIndex = this.availableMonths.findIndex(month => month.value === this.tempSelectedMonth);
          if (monthIndex >= 0) {
            this.pickerMonthValue = [monthIndex];
          }
        }
      }
    },

    /**
     * 月份选择变化
     */
    onMonthChange(e) {
      const index = e.detail.value[0];
      // 更新 pickerMonthValue
      this.pickerMonthValue = [index];
      const selectedMonthData = this.availableMonths[index];
      // 更新临时选中的月份
      if (selectedMonthData) {
        this.tempSelectedMonth = selectedMonthData.value;
      }
    },

    /**
     * 确认年月选择
     */
    confirmMonthSelection() {
      // 检查是否有可用的月份
      if (this.availableMonths.length === 0) {
        uni.showToast({
          title: '该年份没有可用的月份',
          icon: 'none'
        });
        return;
      }

      // 直接从 picker-view 的值获取最终选择的年月
      // 年份选择器的值
      const yearIndex = this.pickerYearValue[0];
      // 月份选择器的值
      const monthIndex = this.pickerMonthValue[0];

      // 计算选中的年份
      if (yearIndex >= 0 && yearIndex < this.yearList.length) {
        this.selectedYear = this.yearList[yearIndex];
      }

      // 计算选中的月份
      if (monthIndex >= 0 && monthIndex < this.availableMonths.length) {
        this.selectedMonth = this.availableMonths[monthIndex].value;
      }

      // 同时更新临时选中的值，确保下次打开弹窗时显示正确的默认值
      this.tempSelectedYear = this.selectedYear;
      this.tempSelectedMonth = this.selectedMonth;

      // 关闭弹窗
      this.closeMonthPicker();

      // 触发数据刷新（后续可以根据选中的年月重新加载数据）
      console.log('选中年月:', this.selectedYear, this.selectedMonth);

      // 可以在这里触发一个事件或调用方法来重新加载数据
      this.onMonthChanged();
    },

    /**
     * 年月变化后的处理
     */
    onMonthChanged() {
      // 根据选中的年月重新加载相关数据
      // 这里可以调用相关的方法来刷新账单列表、统计数据等
      if (this.selectedLedger) {
        this.loadAllData();
      }
    }

  }
}
</script>

<style scoped>
/* 引入iconfont.css */
@import url('@/static/iconfont.css');

/* 手机号填写提示弹窗 */
.phone-prompt-card {
  width: 560rpx;
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
}

.phone-prompt-header {
  padding: 40rpx 32rpx 20rpx;
  text-align: center;
}

.phone-prompt-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333333;
}

.phone-prompt-body {
  padding: 0 32rpx 32rpx;
}

.phone-prompt-text {
  font-size: 28rpx;
  color: #666666;
  line-height: 1.6;
  text-align: center;
}

.phone-prompt-footer {
  display: flex;
  gap: 24rpx;
  padding: 0 32rpx 32rpx;
}

.phone-prompt-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
  margin: 0;
}

.phone-prompt-btn.cancel {
  background: #f5f5f5;
  color: #666666;
}

.phone-prompt-btn.confirm {
  background: linear-gradient(135deg, #07c160 0%, #05a054 100%);
  color: #ffffff;
}

/* 页面加载动画 - 流畅冒泡效果 */
@keyframes bubbleIn {
  from {
    transform: translateY(15px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 为不同元素设置动画延迟 */
.animate-bubble {
  animation: bubbleIn 0.5s ease-out forwards;
  will-change: transform, opacity;
  backface-visibility: hidden;
  perspective: 1000px;
}

.animate-bubble-1 {
  animation-delay: 0.1s;
}

.animate-bubble-2 {
  animation-delay: 0.2s;
}

.animate-bubble-3 {
  animation-delay: 0.3s;
}

.animate-bubble-4 {
  animation-delay: 0.4s;
}

.animate-bubble-5 {
  animation-delay: 0.5s;
}

.animate-bubble-6 {
  animation-delay: 0.6s;
}

.firstpage {
  min-height: 100vh;
  background: #ffffff;
  position: relative;
}

/* 顶部选择栏 */
.ledger-select-bar {
  height: 80rpx;
  display: flex;
  background-color: #ffffff;
  z-index: 10;
  position: relative;
  border-bottom: 1rpx solid #f0f0f0;
}

.ledger-select-content {
  margin-left: 30rpx;
  margin-right: 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

/* 左侧账本选择 */
.ledger-select-left {
  display: flex;
  align-items: center;
  padding: 8rpx 0;
}

/* 账本名称背景 */
.ledger-name-background {
  background-color: #f5f3ff;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.ledger-info-icon {
  margin-left: 4rpx;
  cursor: pointer;
  transition: all 0.3s ease;
}

.ledger-info-icon:hover {
  color: #9254de;
  transform: scale(1.1);
}

.ledger-select-text {
  font-size: 36rpx;
  color: #333333;
  font-weight: 400;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  letter-spacing: 2rpx;
  text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.ledger-select-text:hover {
  color: #9254de;
  transform: translateY(-3rpx);
  text-shadow: 0 2rpx 6rpx rgba(146, 84, 222, 0.3);
}

.icon-container {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

/* 年月选择器样式 - 与账本选择保持一致 */
.month-select-wrapper {
  display: flex;
  align-items: center;
  padding: 8rpx 0;
  margin-left: 16rpx;
}

.month-select-background {
  background-color: #f5f3ff;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  transition: all 0.3s ease;
}

.month-select-background:hover {
  background-color: #e9d5ff;
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(146, 84, 222, 0.2);
}

.month-select-text {
  font-size: 36rpx;
  color: #666666;
  font-weight: 400;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  transition: all 0.3s ease;
}

.month-select-text:hover {
  color: #9254de;
}

.month-select-icon {
  transition: all 0.3s ease;
}

.month-select-wrapper:hover .month-select-icon {
  transform: rotate(180deg);
  color: #9254de;
}

.icon-item {
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-circle {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background-color: #f5f3ff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1rpx solid #e9d5ff;
  transition: all 0.3s ease;
}

.icon-circle:hover {
  background-color: #e9d5ff;
  transform: scale(1.1);
}

.message-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-badge {
  position: absolute;
  top: -4rpx;
  right: -4rpx;
  background-color: #ff4d4f;
  color: white;
  font-size: 18rpx;
  min-width: 28rpx;
  height: 28rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

.badge-text {
  color: white;
  font-size: 18rpx;
  font-weight: bold;
}

.notice-bar-container {
  padding: 0 30rpx;
  height: 60rpx;
  margin-top: 16rpx;
}

/* 图标动画 */
.icon-with-animation {
  transition: all 0.3s ease;
}

.icon-with-animation:active {
  transform: scale(1.1);
}

/* 全局悬浮放大动画效果 */
.ledger-select-left:hover,
.icon-item:hover,
.message-icon-wrapper:hover,
.floating-add-button:hover {
  transform: scale(1.08);
  transition: all 0.3s ease;
}

/* 预算卡片悬浮放大效果 */
.budget-card {
  cursor: pointer;
}

.stats-card {
  transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  cursor: pointer;
}

.stats-card:hover {
  transform: scale(1.1);
  box-shadow: 0 16rpx 32rpx rgba(0, 0, 0, 0.3);
  z-index: 10;
}



/* 主内容区域 */
.content {
  height: calc(100% - 80rpx);
  box-sizing: border-box;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 24rpx;
  position: relative;
  z-index: 1;
}

/* 滚动条样式 */
.content::-webkit-scrollbar {
  width: 6rpx;
}

.content::-webkit-scrollbar-track {
  background: rgba(255, 182, 193, 0.1);
  border-radius: 3rpx;
}

.content::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #ffb6c1, #e6e6fa);
  border-radius: 3rpx;
}

.main-content {
  width: 100%;
}

.no-ledger-tip {
  text-align: center;
  font-size: 32rpx;
  color: #999999;
  margin-top: 200rpx;
}

/* 收支统计区域 */
.stats-section {
  display: flex;
  justify-content: space-between;
  gap: 12rpx;
  margin-bottom: 32rpx;
}

/* 统计卡片 */
.stats-card {
  border-radius: 16rpx;
  padding: 24rpx 15rpx;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.15);
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

/* 收入卡片 - 30% 屏幕宽度 */
.income-card {
  width: 30%;
  background-color: #f0fff4;
  position: relative;
  overflow: hidden;
}

/* 支出卡片 - 40% 屏幕宽度 */
.expense-card {
  width: 40%;
  background-color: #fff5f5;
  overflow: hidden;
}

/* 结余卡片 - 30% 屏幕宽度 */
.balance-card {
  width: 30%;
  background-color: #f8f5ff;
  position: relative;
  overflow: hidden;
}

/* 火箭容器 - 作为背景动画 */
.rocket-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 1;
}

/* 火箭动画 - 从左下角飞入到右上角飞出 */
.rocket {
  position: absolute;
  font-size: 60rpx;
  left: -20%;
  top: 100%;
  transform: translate(0, 0);
  animation: rocketFly 3s ease-in-out infinite;
  opacity: 0.9;
}

@keyframes rocketFly {
  0% {
    left: -20%;
    top: 100%;
    transform: translate(0, 0);
    opacity: 0;
  }
  10% {
    opacity: 0.9;
  }
  90% {
    opacity: 0.9;
  }
  100% {
    left: 120%;
    top: -50%;
    transform: translate(0, 0);
    opacity: 0;
  }
}

/* 表情容器 - 作为背景动画 */
.balance-emoji-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 1;
}

/* 开心表情动画 - 整个卡片内漂浮 */
.balance-emoji.happy {
  position: absolute;
  font-size: 60rpx;
  top: 10%;
  left: 10%;
  animation: happyFloat 2s ease-in-out infinite;
  opacity: 0.8;
}

/* 思考表情动画 - 整个卡片内漂浮 */
.balance-emoji.thinking {
  position: absolute;
  font-size: 60rpx;
  top: 10%;
  left: 60%;
  animation: thinkingFloat 3s ease-in-out infinite;
  opacity: 0.8;
}

/* 伤心表情动画 - 左右大幅度摆动 */
.balance-emoji.sad {
  position: absolute;
  font-size: 60rpx;
  top: 50%;
  left: 35%;
  animation: sadSwing 0.8s ease-in-out infinite;
  opacity: 0.8;
}

@keyframes happyFloat {
  0% {
    top: 10%;
    left: 10%;
    transform: scale(1) rotate(0deg);
  }
  25% {
    top: 20%;
    left: 60%;
    transform: scale(1.1) rotate(10deg);
  }
  50% {
    top: 70%;
    left: 75%;
    transform: scale(1) rotate(0deg);
  }
  75% {
    top: 80%;
    left: 20%;
    transform: scale(0.9) rotate(-10deg);
  }
  100% {
    top: 10%;
    left: 10%;
    transform: scale(1) rotate(0deg);
  }
}

@keyframes thinkingFloat {
  0% {
    top: 10%;
    left: 60%;
    transform: rotate(-5deg);
  }
  50% {
    top: 70%;
    left: 30%;
    transform: rotate(5deg);
  }
  100% {
    top: 10%;
    left: 60%;
    transform: rotate(-5deg);
  }
}

@keyframes sadSwing {
  0%, 100% {
    transform: translateX(-30rpx) rotate(-20deg);
  }
  50% {
    transform: translateX(30rpx) rotate(20deg);
  }
}

/* 统计卡片内容容器 */
.stats-card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  width: 100%;
  position: relative;
  z-index: 2;
}

/* 统计卡片标签容器 */
.stats-card-label-container {
  display: flex;
  align-items: center;
  gap: 8rpx;
  justify-content: center;
}

/* 统计卡片标签 */
.stats-card-label {
  font-size: 24rpx;
  color: #666666;
  margin-bottom: 4rpx;
  font-weight: bold;
}

/* 统计卡片标签容器 */
.stats-card-label-container {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

/* 统计卡片info图标 */
.stats-card-info {
  margin-left: 4rpx;
}

/* 统计卡片数值 */
.stats-card-value {
  font-size: 36rpx;
  font-weight: bold;
}

.stats-card-value.expense {
  color: #ff4d4f;
}

.stats-card-value.income {
  color: #07c160;
}

.stats-card-value.balance {
  color: #9254de;
}

/* 统计卡片子项容器 */
.stats-card-sub-items {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  width: 100%;
  padding-top: 8rpx;
  border-top: 1rpx dashed #ffc0c0;
}

/* 统计卡片子项 */
.stats-card-sub-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

/* 统计卡片子项标签 */
.stats-card-sub-label {
  font-size: 20rpx;
  color: #999999;
}

/* 统计卡片子项数值 */
.stats-card-sub-value {
  font-size: 22rpx;
  color: #333333;
  font-weight: 500;
}

/* 预算卡片 */
.budget-card {
  background: linear-gradient(135deg, #ff6b8b 0%, #ff4757 100%);
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 20rpx rgba(255, 71, 87, 0.4);
  color: #ffffff;
  position: relative;
  overflow: hidden;
}

/* 预算卡片装饰元素 */
.budget-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(50%, 50%);
}

/* 预算卡片金元宝样式 */
.budget-card::after {
  content: '🔆';
  position: absolute;
  top: 50%;
  left: 50%;
  font-size: 120rpx;
  animation: float 10s ease-in-out infinite;
  transform: translate(-50%, -50%);
  opacity: 0.6;
}

/* 金元宝自由游动动画 */
@keyframes float {
  0% {
    transform: translate(-50%, -50%) scale(1) rotate(0deg);
    opacity: 0.6;
  }
  25% {
    transform: translate(calc(-50% + 150rpx), calc(-50% - 120rpx)) scale(1.1) rotate(15deg);
    opacity: 0.8;
  }
  50% {
    transform: translate(calc(-50% - 150rpx), calc(-50% + 100rpx)) scale(0.9) rotate(-15deg);
    opacity: 0.7;
  }
  75% {
    transform: translate(calc(-50% + 100rpx), calc(-50% + 150rpx)) scale(1.1) rotate(10deg);
    opacity: 0.8;
  }
  100% {
    transform: translate(-50%, -50%) scale(1) rotate(0deg);
    opacity: 0.6;
  }
}

/* 预算卡片头部 */
.budget-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.budget-card-title {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.budget-card-title text {
  font-size: 32rpx;
  font-weight: bold;
}

.budget-card-info {
  margin-left: 8rpx;
}

.budget-card-total {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 36rpx;
  font-weight: bold;
}

/* 预算总金额背景 */
.budget-total-background {
  background-color: rgba(255, 255, 255, 0.2);
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

/* 预算卡片内容 */
.budget-card-content {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

/* 预算进度 */
.budget-progress {
  display: flex;
  align-items: center;
  gap: 80rpx;
}

/* 进度圆环 */
.progress-circle {
  flex-shrink: 0;
}

.progress-circle svg {
  width: 200rpx;
  height: 200rpx;
}

.progress-circle-stroke {
  transition: stroke-dashoffset 1s ease-in-out;
  stroke-width: 12rpx;
}

.progress-circle-text {
  font-size: 20rpx;
  font-weight: bold;
  fill: #ffffff;
}

.progress-circle-subtext {
  font-size: 5rpx;
  fill: rgba(255, 255, 255, 0.9);
}

/* 进度条容器 */
.arcbar-box {
  flex-shrink: 0;
}

.arcbar-container {
  width: 162rpx;
  height: 162rpx;
}

/* 预算数据 */
.budget-data {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.budget-data-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4rpx 0;
}

.budget-data-label {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.budget-data-value {
  font-size: 36rpx;
  font-weight: 400;
  color: #ffffff;
  text-align: right;
  flex: 1;
}

/* 预算数据标签容器 */
.budget-data-label-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

/* 预算数据值容器 */
.budget-data-value-container {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex: 1;
}

/* 预算数据分割线 */
.budget-data-divider {
  width: 100%;
  height: 2rpx;
  background-color: rgba(255, 255, 255, 0.5);
  margin-bottom: 8rpx;
  border-radius: 1rpx;
}

/* 带顶部横线的预算数据项 */
.budget-data-item-with-divider {
  position: relative;
  padding-top: 12rpx;
}

.budget-data-item-with-divider::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2rpx;
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 1rpx;
}

.budget-data-item-with-divider .budget-data-label,
.budget-data-item-with-divider .budget-data-value {
  margin-top: 8rpx;
}

/* 预算统计 */
.budget-stats {
  display: flex;
  justify-content: space-between;
  padding-top: 16rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.3);
}

.budget-stats-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.budget-stats-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
}

.budget-stats-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #ffffff;
}

/* 文字靠右对齐 */
.text-right {
  text-align: right;
  flex: 1;
}

/* 无预算提示 */
.no-budget-tip {
  padding: 20rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 12rpx;
  text-align: center;
  margin-top: 16rpx;
}

.no-budget-tip text {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
}

/* 最近账单区域 */
.recent-bills-section {
  margin-bottom: 32rpx;
}

/* 最近账单头部 */
.recent-bills-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding: 0 8rpx;
}

.recent-bills-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.recent-bills-view-all {
  font-size: 24rpx;
  color: #9254de;
  font-weight: 500;
  transition: all 0.3s ease;
}

.recent-bills-view-all:hover {
  font-size: 28rpx;
  font-weight: 700;
  color: #6b46c1;
  transform: translateY(-2rpx);
  text-shadow: 0 2rpx 4rpx rgba(146, 84, 222, 0.3);
}

/* 最近账单列表 */
.recent-bills-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}



/* 账单列表项（通过组件样式控制） */

/* 颜色快捷类（通用） */
.income { color: #07c160 !important; }
.expense { color: #ee0a24 !important; }
.balance { color: #07c160; }



/* 悬浮添加按钮 */
.floating-add-button {
  position: fixed !important;
  right: 40rpx !important;
  bottom: 100rpx !important;
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  gap: 12rpx !important;
  z-index: 10 !important;
  pointer-events: auto !important;
}

/* 弹窗层级设置 */
uni-popup {
  z-index: 9999 !important;
}

.uni-popup {
  z-index: 9999 !important;
}

.uni-popup__content {
  z-index: 10000 !important;
}

.uni-popup__mask {
  z-index: 9998 !important;
}

/* 悬浮按钮圆形 */
.floating-button-circle {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #9254de 0%, #6b46c1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 28rpx rgba(146, 84, 222, 0.5);
  position: relative;
  z-index: 1;
  animation: breathe 2s infinite ease-in-out;
  transition: all 0.3s ease;
}

/* 呼吸动画效果 */
@keyframes breathe {
  0% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(146, 84, 222, 0.4);
  }
  70% {
    transform: scale(1.3);
    box-shadow: 0 0 0 20rpx rgba(146, 84, 222, 0);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(146, 84, 222, 0);
  }
}

/* 悬浮按钮悬停效果 */
.floating-button-circle:hover {
  transform: scale(1.3) rotate(360deg);
  box-shadow: 0 12rpx 32rpx rgba(146, 84, 222, 0.6);
  animation: none;
}

/* 加号 */
.floating-button-plus {
  font-size: 64rpx;
  font-weight: 300;
  color: #ffffff;
  line-height: 1;
  margin-top: -8rpx;
}

/* 按钮文字 */
.floating-button-text {
  font-size: 24rpx;
  color: #666666;
  font-weight: 500;
  background-color: #ffffff;
  padding: 10rpx 20rpx;
  border-radius: 18rpx;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
}

/* 优化uni-popup层级（用于弹窗覆盖canvas等场景） */
:deep(.uni-popup) {
  z-index: 999999 !important;
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  background-color: transparent;
  pointer-events: auto;
  will-change: transform;
}
:deep(.uni-popup__content) { z-index: 1000000 !important; position: relative !important; }
:deep(.uni-popup__mask) {
  z-index: 999998 !important;
  position: fixed !important;
  top: 0 !important; left: 0 !important; right: 0 !important; bottom: 0 !important;
  background-color: rgba(0, 0, 0, 0.4) !important;
  pointer-events: auto;
  -webkit-transform: none; transform: none; will-change: opacity; transform: translateZ(0);
}


/* 预算统计（小型）同一行显示 */
.budget-stats-horizontal { display: flex; align-items: center; justify-content: space-between; gap: 20rpx; padding: 10rpx 0; }
.budget-stat-item-horizontal { display: flex; align-items: center; gap: 10rpx; }

/* 年月选择弹窗样式 - 与账本选择弹窗保持一致 */
.month-picker-container {
  width: 600rpx;
  background-color: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.15);
  height: auto;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
}

.month-picker-header {
  padding: 24rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
}

.month-picker-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.month-picker-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.month-picker-divider {
  height: 1rpx;
  background-color: #f0f0f0;
  width: 100%;
}

.month-picker-content {
  padding: 32rpx;
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  gap: 40rpx;
  min-height: 400rpx;
}

.picker-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.picker-label {
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.picker-view {
  width: 100%;
  height: 400rpx;
  background-color: #f8f8f8;
  border-radius: 12rpx;
}

.picker-item {
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  font-size: 32rpx;
  color: #333333;
}

.month-picker-footer {
  padding: 24rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24rpx;
  background-color: #ffffff;
}

.month-picker-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.cancel-btn {
  background-color: #f5f5f5;
  border: 1rpx solid #e0e0e0;
}

.cancel-btn:hover {
  background-color: #e8e8e8;
}

.cancel-btn-text {
  font-size: 28rpx;
  color: #666666;
  font-weight: 500;
}

.confirm-btn {
  background: linear-gradient(135deg, #9254de 0%, #6b46c1 100%);
  box-shadow: 0 4rpx 16rpx rgba(146, 84, 222, 0.3);
}

.confirm-btn:hover {
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 20rpx rgba(146, 84, 222, 0.4);
}

.confirm-btn-text {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: 500;
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
  background-color: rgba(146, 84, 222, 0.1);
  transform: scale(1.1);
}

.icon-hover:active {
  transform: scale(0.95);
}

</style>

