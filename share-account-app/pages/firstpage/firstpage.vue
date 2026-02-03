<template>
  <view class="firstpage">
    <!-- 账本选择栏 -->
    <view class="ledger-select-bar">
      <view class="ledger-select-content">
          <view class="current-ledger-name-wrapper"  >
            <text class="current-ledger-name" @click="openDrawer">{{ selectedLedger ? selectedLedger.name + ' >' : '选择账本 >' }}</text>
            <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click.stop="onNotifyTipClick" />
          </view>
          <text v-if="selectedLedger" class="ledger-ownership-suffix">{{ selectedLedger.isShared ? '朋友账本' : '自己账本' }}</text>
          <!-- 右侧图标区域 -->
          <view class="icon-container">
            <!-- 显示饼状图图标 -->
            <view class="icon-item" @click="gotoStatistics">
              <custom-icon type="bingzhuangtu" :size="30" color="#1989fa"></custom-icon>
            </view>
            <!-- 消息图标 -->
            <view class="message-icon-wrapper" @click.stop="gotoMessageList">
              <custom-icon type="xiaoxi" :size="30" color="#1989fa"></custom-icon>
              <!-- 未读消息红点/数字角标 -->
              <view v-if="unreadCount > 0" class="message-badge">
                <text class="badge-text">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
              </view>
            </view>
          </view>
        </view>
    </view>
    
    <!-- 消息通告栏 -->
    <view v-if="latestMessage" class="notice-bar-container">
      <uni-notice-bar 
        :text="latestMessage.title" 
        show-get-more 
        @click="handleNoticeClick" 
        @getmore="gotoMessageList"
      />
    </view>

    <!-- 引导卡片 -->
    <GuideCard v-if="showGuideCard" :card="guideCard" @close="markGuideCardRead" />

    <!-- 主内容区域 -->
    <view class="content">
      <view class="main-content" v-if="selectedLedger">
        
        <!-- 预算统计区域 -->
        <view class="budget-section">
          <view class="section-header">
            <view class="title-with-icon">
              <text class="section-title">本月预算</text>
              <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click.stop="onBudgetTipClick" />
            </view>
            <view class="budget-header-right">
              <text class="total-budget-amount">总 {{ formatAmount(budgetTotal) }}</text>
              <custom-icon v-if="selectedLedger && !selectedLedger.isShared" type="bianji" :size="20" color="#1989fa" @click.stop="gotoBudgetList" />
            </view>
          </view>
          
          <view class="budget-progress-wrapper">
            <view class="budget-progress-left">
              <!-- 进度条画布 -->
              <view class="arcbar-box" @click="gotoStatistics">
                <qiun-data-charts 
                  type="arcbar"
                  :opts="arcOpts"
                  :chartData="arcChartData"
                  :canvas2d="true"
                />
              </view>
            </view>
            <view class="budget-progress-right">
            <view class="budget-detail-item-horizontal">
              <text class="detail-value-mini expense">计入预算 {{ formatAmount(budgetSpent) }}</text>
            </view>
            <view class="budget-detail-item-horizontal">
              <text class="detail-value-mini expense">不计入预算 {{ formatAmount(budgetExcluded) }}</text>
            </view>
            <view class="budget-detail-item-horizontal">
              <text class="detail-value-mini balance">剩余 {{ formatAmount(budgetRemaining) }}</text>
            </view>
          </view>
          </view>
          
          <view class="budget-stats-horizontal">
            <view class="budget-stat-item-horizontal">
              <text class="budget-label-mini">本月日均消费</text>
              <text class="budget-value-mini expense">{{ formatAmount(dailyAverage) }}</text>
            </view>
            <view class="budget-stat-item-horizontal">
              <text class="budget-label-mini">剩余每日可消费</text>
              <text class="budget-value-mini balance">{{ formatAmount(remainingDaily) }}</text>
            </view>
          </view>
          
          <view v-if="budgetTotal === '0.00'" class="no-budget-tip">
            <text>尚未设置预算，点击右上角设置预算</text>
          </view>
        </view>

        <!-- 收支统计区域 -->
        <view class="statistics-section">
          <view class="stat-item">
            <text class="stat-label">本月收入</text>
            <text class="stat-value income">{{ formatAmount(monthlyIncome) }}</text>
          </view>
          <view class="stat-item">
            <text class="stat-label">本月支出</text>
            <text class="stat-value expense">{{ formatAmount(monthlyExpense) }}</text>
          </view>
          <view class="stat-item">
            <view class="title-with-icon">
              <text class="stat-label">本月结余</text>
              <uni-icons type="info" size="18" color="#999" class="notify-tip-icon" @click.stop="onBalanceTipClick" />
            </view>
            <text class="stat-value balance">{{ formatAmount(monthlyBalance) }}</text>
          </view>
        </view>

        <!-- 最近10条账单区域 -->
        <view class="recent-bills-section">
          <view class="section-header">
            <text class="section-title">最近10条账单</text>
            <text class="view-all" @click="gotoBillList">全部账单</text>
          </view>
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
          />
        </view>
      </view>
      <view v-else class="no-ledger-tip">
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
    
    <!-- 悬浮添加按钮 -->
    <view class="floating-button" @click="gotoAddBill">
      <view class="button-content">
        <text class="add-icon">记一笔</text>
        <text class="ai-count">AI剩{{remainingAiCount}}次</text>
      </view>
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
import { resolveShareInfo } from '@/common/shareStrategy.js';
import { formatAmount } from '@/common/util.js';
import messageService from '@/common/messageService.js';

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
          status: '0'
        },
        // 引导卡片
        guideCard: null,
        showGuideCard: false,
        isGuest: !!uni.getStorageSync('isGuest'),
        
        // 弹窗相关
        billPopupVisible: false,
        currentBill: null,
        // 消息相关
        latestMessage: null,
        unreadCount: 0,
        
        // Arcbar 圆形进度条数据与样式（整圆）
        arcChartData: {},
        arcOpts: {
          color: ['#2FC25B'],
          padding: undefined,
          title: { name: '', fontSize: 0, color: '#000000' },
          subtitle: { name: '', fontSize: 0, color: '#000000' },
          extra: {
            arcbar: {
              type: 'circle',
              width: 10,
              backgroundColor: '#E9E9E9',
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
    }
  },
  onLoad() {
  },
  onShow() {
    // 页面展示时，自动初始化账本组件并选中默认账本
    if (this.$refs.ledgerPopup && this.$refs.ledgerPopup.initAutoSelect) {
      this.$refs.ledgerPopup.initAutoSelect();
    }
    if (this.selectedLedger) {
      this.loadAllData();
    }
    this.tryShowGuideCard();
    // 检查消息授权状态（内部会自动初始化消息服务和加载模板配置）
    this.checkMessageAuthorization();
    // 显示分享菜单（包含朋友圈）
    try {
      uni.showShareMenu({ withShareTicket: true, menus: ['shareAppMessage', 'shareTimeline'] });
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
  
  // 分享生命周期函数
  async onShareAppMessage(res) {
    // 仅当来源为按钮（弹窗分享按钮）时，按账单分享（组件内已处理预保存与回调）
    if (res && res.from === 'button') {
      if (this.$refs.billListComponent && typeof this.$refs.billListComponent.getShareInfo === 'function') {
        try {
          return this.$refs.billListComponent.getShareInfo(true);
        } catch (error) {
          console.error('组件分享信息获取失败:', error);
        }
      }
    }

    // 非按钮触发：固定分享账本，不区分弹窗状态
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
        title: '按账本统计',
        icon: 'none',
        duration: 2000
      });
    },
    
    /**
     * 显示预算提示
     */
    onBudgetTipClick() {
      uni.showToast({
        title: '当前账本：本月所有预算的总和',
        icon: 'none',
        duration: 2000
      });
    },
    
    /**
     * 显示结余提示
     */
    onBalanceTipClick() {
      uni.showToast({
        title: '当前账本：本月收入-本月支出',
        icon: 'none',
        duration: 2000
      });
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
     * 检查消息授权状态
     */
    async checkMessageAuthorization() {
      try {
        // 使用wx.getSetting检查授权状态
        const setting = await new Promise((resolve) => {
          wx.getSetting({ 
            withSubscriptions: true,
            success: resolve 
          });
        });
        await messageService.initPageAuthorization('firstpage');
      } catch (error) {
        // 授权失败不处理，符合需求
      }
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
    loadAllData() {
      if (!this.selectedLedger) return Promise.resolve();
      
      return Promise.all([
        this.loadMonthlyBills(),
        this.loadMonthlyBudget(),
        this.loadUserMonthlyTotalExpense(),
        this.fetchMessageData(),

        // 同步更新账单列表查询参数
        this.initRecentBillsParams(),
        // 重新赋值以触发子组件watch（deep+immediate），确保查询最新列表
        this.recentBillsParams = {
          ...this.recentBillsParams,
          pageNum: 1,
          ledgerId: this.selectedLedger.id
        }
      ]).then(() => {
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
    
    // 加载本月账单数据（使用新的API获取统计数据）
    async loadMonthlyBills() {
      try {
        if (!this.selectedLedger) return;
        
        // 调用新的API接口获取本月收支统计
        const res = await request({
          url: `${backUrl.endpoints.bill_getMonthlyStatisticsByLedgerId}/${this.selectedLedger.id}`,
          method: 'GET'
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
        console.error('加载本月账单统计失败:', error);
        // 清空数据
        this.monthlyExpense = '0.00';
        this.monthlyIncome = '0.00';
        this.monthlyBalance = '0.00';
      }
    },
    
    // 加载用户指定月份总支出（按当前选择账本筛选）
    async loadUserMonthlyTotalExpense() {
      try {
        // 获取当前年份和月份
        const now = new Date();
        const year = now.getFullYear();
        const month = now.getMonth() + 1;
        
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
    
    // 加载本月预算数据
    async loadMonthlyBudget() {
      try {
        const now = new Date();
        const year = now.getFullYear();
        const month = now.getMonth() + 1;
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
        console.error('加载本月预算失败:', error);
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
      let color = '#2FC25B';
      let fraction = 0;
      
      if (percent === Infinity || percent > 100) {
        overBudget = true;
        color = '#FAC858';
        fraction = 1; // 满圈
        displayText = percent === Infinity ? '∞%' : `${Math.round(percent)}%`;
      } else {
        // 阈值颜色：≤60绿；>60≤80淡红；>80≤100深红
        const p = Math.max(0, Math.min(percent, 100));
        if (p <= 60) {
          color = '#2FC25B';
        } else if (p <= 80) {
          color = '#FF8A80';
        } else {
          color = '#EE6666';
        }
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
        subtitle: { name: '已消费', fontSize: 14, color: '#666666' }
      };
      this.arcChartData = {
        series: [
          { name: '已消费', color, data: fraction }
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
    }
    

  }
}
</script>

<style scoped>
/* 引入iconfont.css */
@import url('@/static/iconfont.css');

.firstpage {
  height: 100vh;
  background-color: #f8f8f8;
}

/* 顶部选择栏 */
.ledger-select-bar {
  height: 80rpx;
  background-color: #ffffff;
  display: flex;
  border-bottom: 1rpx solid #f0f0f0;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.ledger-select-content {
  margin-left: 20rpx;
  margin-right: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

/* 新增：将名称与归属后缀并排放置，靠近显示 */
.ledger-title {
  display: flex;
  align-items: baseline;
}

/* 调整后缀为更小字体与灰色，并靠近名称 */
.ledger-ownership-suffix {
  font-size: 22rpx;
  color: #999999;
  margin-left: 6rpx;
}
.icon-container {
  display: flex;
  align-items: center;
}

.icon-container > text {
  margin-left: 30rpx;
}

.message-icon-wrapper {
  position: relative;
  margin-left: 30rpx;
}

.message-badge {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  background-color: #ff4d4f;
  color: white;
  font-size: 20rpx;
  min-width: 30rpx;
  height: 30rpx;
  border-radius: 15rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 10rpx;
}

.badge-text {
  color: white;
  font-size: 20rpx;
}

.notice-bar-container {
  padding: 0 30rpx;
  height: 60rpx;
}

.current-ledger-name-wrapper {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.current-ledger-name {
  font-size: 32rpx;
  color: #333333;
}

.notify-tip-icon {
  margin-left: 6rpx;
}

/* 主内容区域  我手动去掉了 padding: 24rpx; */
.content {
  height: calc(100% - 80rpx);
  box-sizing: border-box;
  overflow-y: auto;
  overflow-x: hidden;
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
.statistics-section {
  display: flex;
  justify-content: space-around;
  padding: 32rpx 24rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.stat-item { text-align: center; }
.stat-label { font-size: 28rpx; color: #999999; display: block; margin-bottom: 8rpx; }
.stat-value { font-size: 36rpx; font-weight: bold; }
.stat-value.expense { color: #ff4d4f; }
.stat-value.income { color: #07c160; }
.stat-value.balance { color: #333333; }

/* 预算区域 */
.budget-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.section-header { display: flex; justify-content: space-between; align-items: center; }
.budget-header-right { display: flex; align-items: center; gap: 16rpx; }
.total-budget-amount { font-size: 28rpx; color: #1989fa; font-weight: bold; }
.section-title { font-size: 32rpx; font-weight: bold; color: #333333; }
.title-with-icon { display: flex; align-items: center; }
.view-all { font-size: 28rpx; color: #1989fa; }

/* 半圆形进度条容器 */
.budget-progress-wrapper { display: flex; align-items: center; justify-content: flex-start; gap: 20rpx; }
 .budget-progress-left { flex: 0 0 50%; max-width: 50%; position: relative; display: flex; justify-content: center; align-items: center; min-width: 0; }
 .progress-canvas { width: 100px; height: 100px; background-color: #fafafa; border-radius: 50%; display: block; box-shadow: inset 0 2rpx 6rpx rgba(0, 0, 0, 0.05); position: relative; transform: translateZ(0); }
 
 /* 新增：圆形进度条容器与中心文案 */
 .arcbar-box { width: 108px; height: 108px; position: relative; }
  .arcbar-center { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; }
  .center-title { font-size: 24px; line-height: 28px; font-weight: bold; }
  .center-subtitle { font-size: 14px; line-height: 18px; }
 @keyframes flash { 0% { opacity: 1; } 50% { opacity: 0.4; } 100% { opacity: 1; } }
 .flash { animation: flash 1s ease-in-out infinite; }
 .budget-progress-right { flex: 0 0 50%; max-width: 50%; display: flex; flex-direction: column; gap: 16rpx; justify-content: center; align-items: flex-start; box-sizing: border-box; min-width: 0; }
 .budget-detail-item-horizontal { padding: 8rpx 0; transition: all 0.2s ease; }
.budget-detail-item-horizontal:active { background-color: rgba(0, 0, 0, 0.03); border-radius: 6rpx; }

/* 无预算提示 */
.no-budget-tip { padding: 20rpx; background-color: #fff7e6; border-radius: 12rpx; text-align: center; }
.no-budget-tip text { font-size: 26rpx; color: #fa8c16; }

/* 缩小版预算标签与数值 */
.budget-label-mini { font-size: 24rpx; color: #666666; margin-right: 10rpx; }
.budget-value-mini { font-size: 26rpx; font-weight: bold; }

/* 最近账单区域 */
.recent-bills-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

/* 颜色快捷类（通用） */
.income { color: #07c160 !important; }
.expense { color: #ee0a24 !important; }
.balance { color: #07c160; }



/* 悬浮添加按钮 */
.floating-button {
  position: fixed;
  right: 40rpx;
  bottom: 40rpx;
  min-width: 160rpx;
  height: 120rpx;
  padding: 0 28rpx;
  background-color: #1989fa;
  border-radius: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.15);
  z-index: 997;
  color: #ffffff;
}

.button-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.add-icon { 
  font-size: 32rpx; 
  font-weight: 600; 
  line-height: 1;
  margin-bottom: 4rpx;
}

.ai-count {
  font-size: 22rpx;
  font-weight: bold;
  color: #fffacd;
  line-height: 1.2;
  text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.3);
}

/* 优化uni-popup层级（用于弹窗覆盖canvas等场景） */
:deep(.uni-popup) {
  z-index: 9999 !important;
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  background-color: transparent;
  pointer-events: auto;
  will-change: transform;
}
:deep(.uni-popup__content) { z-index: 10000 !important; position: relative !important; }
:deep(.uni-popup__mask) {
  z-index: 9998 !important;
  position: fixed !important;
  top: 0 !important; left: 0 !important; right: 0 !important; bottom: 0 !important;
  background-color: rgba(0, 0, 0, 0.4) !important;
  pointer-events: auto;
  -webkit-transform: none; transform: none; will-change: opacity; transform: translateZ(0);
}


/* 预算统计（小型）同一行显示 */
.budget-stats-horizontal { display: flex; align-items: center; justify-content: space-between; gap: 20rpx; padding: 10rpx 0; }
.budget-stat-item-horizontal { display: flex; align-items: center; gap: 10rpx; }

</style>

