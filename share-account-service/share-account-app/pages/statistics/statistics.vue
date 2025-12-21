<template>
  <view class="page">
    <!-- 维度 + 时间选择 + 筛选工具栏（同一容器显示） -->
    <view class="toolbar">
      <view class="toolbar-item" style="flex: 1 1 100%;">
        <uni-segmented-control
          :values="dimensionTabs"
          :current="dimensionIndex"
          @clickItem="onDimensionChange"
          :inActiveColor="'#fff'"
          styleType="button"
          :style="{
            fontSize: '28rpx',
            height: '72rpx',
            borderRadius: '36rpx',
            border: 'none',
            width: '100%'
          }"
        ></uni-segmented-control>
      </view>
      <view v-if="dimension === 'year'" class="toolbar-item">
        <picker mode="date" fields="year" :value="yearValue" @change="onYearChange">
          <view class="picker-view">{{ year }}</view>
        </picker>
      </view>
      <view v-if="dimension === 'month'" class="toolbar-item">
        <picker mode="date" fields="month" :value="monthValue" @change="onMonthChange">
          <view class="picker-view">{{ year }}-{{ monthStr }}</view>
        </picker>
      </view>
      <view v-if="dimension === 'custom'" class="toolbar-item">
        <view class="picker-view" @tap="openDateRangePopup">{{ customRangeStr }}</view>
      </view>
      <view :class="['toolbar-item', selectedLedger ? 'active' : '']" @tap="openLedgerPopup">
        <text>{{ selectedLedger ? selectedLedger.name : '账本' }}</text>
      </view>
      <view :class="['toolbar-item', selectedAccount ? 'active' : '']" @tap="openAccountPopup">
        <text>{{ selectedAccount ? selectedAccount.name : '账户' }}</text>
      </view>
      <view :class="['toolbar-item', selectedCategory ? 'active' : '']" @tap="openCategoryPopup">
        <text>{{ selectedCategory ? selectedCategory.name : '分类' }}</text>
      </view>
      <view class="toolbar-item" @tap="resetFilters"><text>重置</text></view>

    </view>

    <!-- 总览（总时）：汇总表 + 年度折线图（三线） + 年度列表 -->
    <view v-if="dimension === 'total'">
      <view class="section">
        <view class="summary-cards">
          <view class="card income">
            <text class="card-title">总收入</text>
            <text class="card-value">{{ formatAmount(summary.totalIncome / 100) }}</text>
          </view>
          <view class="card expense">
            <text class="card-title">总支出</text>
            <text class="card-value">{{ formatAmount(summary.totalExpense / 100) }}</text>
          </view>
          <view class="card count">
            <text class="card-title">总数量</text>
            <text class="card-value">{{ summaryCount }}</text>
          </view>
          <view class="card balance">
            <text class="card-title">结余</text>
            <text class="card-value">{{ formatAmount(summary.balance / 100) }}</text>
          </view>
        </view>
      </view>
      <view class="section">
        <view class="section-title">年度收支趋势（{{ rangeTitle }}）</view>
        <qiun-data-charts type="line" :ontouch="true" :opts="lineOpts" :chartData="yearlyLineChartData" :canvas2d="true" />
      </view>
      <view class="section">
        <view class="section-title">年度汇总列表（{{ rangeTitle }}）</view>
        <view class="table">
            <view class="table-header">
              <text class="cell cell-time">年份</text>
              <text class="cell">总收入</text>
              <text class="cell">总支出</text>
              <text class="cell">结余</text>
            </view>
          <view class="table-row" v-for="(row, idx) in yearRows" :key="idx">
            <text class="cell cell-time">{{ row.year }}</text>
            <text class="cell">{{ formatAmount(row.income) }}</text>
            <text class="cell">{{ formatAmount(row.expense) }}</text>
            <text class="cell">{{ formatAmount(row.balance) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 年/月/自定义：汇总表 + 分类折线图（双线） + 时间序列折线图（三线） + 列表 -->
    <view v-else-if="dimension === 'year' || dimension === 'month' || dimension === 'custom'">
      <view class="section">
        <view class="summary-cards">
          <view class="card income">
            <text class="card-title">总收入</text>
            <text class="card-value">{{ formatAmount(summary.totalIncome / 100) }}</text>
          </view>
          <view class="card expense">
            <text class="card-title">总支出</text>
            <text class="card-value">{{ formatAmount(summary.totalExpense / 100) }}</text>
          </view>
          <view class="card count">
            <text class="card-title">总数量</text>
            <text class="card-value">{{ summaryCount }}</text>
          </view>
          <view class="card balance">
            <text class="card-title">结余</text>
            <text class="card-value">{{ formatAmount(summary.balance / 100) }}</text>
          </view>
        </view>
      </view>
      <view class="section">
        <view class="section-title">
          <text>分类收入/支出（{{ rangeTitle }}）</text>
          <custom-icon type="bingzhuangtu" :size="30" color="#1989fa" @tap="openChartFullscreen('category')"></custom-icon>
        </view>
        <qiun-data-charts type="line" :ontouch="true" :opts="lineOpts" :chartData="categoryLineChartData" :canvas2d="true" />
      </view>
      <view class="section">
        <view class="section-title">
          <text>{{ dimension === 'year' ? '月度' : '每日' }}收支趋势（{{ rangeTitle }}）</text>
          <custom-icon type="bingzhuangtu" :size="30" color="#1989fa" @tap="openChartFullscreen('trend')"></custom-icon>
        </view>
        <qiun-data-charts
          type="line"
          :ontouch="true"
          :opts="lineOpts"
          :chartData="dimension === 'year' ? monthLineChartData : dayLineChartData"
          :canvas2d="true"
        />
      </view>
      <view class="section">
        <view class="section-title">{{ dimension === 'year' ? '月度' : '每日' }}汇总列表（{{ rangeTitle }}）</view>
        <view class="table">
            <view class="table-header">
              <text class="cell cell-time">{{ dimension === 'year' ? '月份' : '日期' }}</text>
              <text class="cell">总收入</text>
              <text class="cell">总支出</text>
              <text class="cell">结余</text>
            </view>
          <view class="table-row" v-for="(row, idx) in (dimension === 'year' ? monthRows : dayRows)" :key="idx">
            <text class="cell cell-time">{{ row.label }}</text>
            <text class="cell">{{ formatAmount(row.income) }}</text>
            <text class="cell">{{ formatAmount(row.expense) }}</text>
            <text class="cell">{{ formatAmount(row.balance) }}</text>
          </view>
        </view>
      </view>
    </view>


    <view v-if="fullscreenVisible" class="fullscreen-overlay">
      <view class="overlay-header">
        <text class="overlay-title">{{ fullscreenMode === 'category' ? '分类饼图' : (dimension === 'year' ? '月度' : '每日') + '收支饼图' }}</text>
        <text class="overlay-tip" v-if="!showAllPieAll">当前已隐藏 小于 5% 切片</text>
        <view class="overlay-actions-header">
          <view class="filter-tag" :class="showAllPieAll ? 'selected' : ''" @tap.stop="showAllData" @click.stop="showAllData">{{ showAllPieAll ? '恢复过滤' : '展示全部' }}</view>
          <custom-icon type="guanbi" :size="22" color="#999" @tap="closeFullscreen"></custom-icon>
        </view>
      </view>
      <view class="overlay-side">
        <view :class="['filter-tag', pieType === 'expense' ? 'selected' : '']" @tap="setPieType('expense')">支出</view>
        <view :class="['filter-tag', pieType === 'income' ? 'selected' : '']" @tap="setPieType('income')">收入</view>
      </view>
      <view class="overlay-content">
        <qiun-data-charts v-if="pieHasData" :key="pieRenderKey" type="pie" :ontap="true" :ontouch="true" :tapLegend="true" :inScrollView="true" :pageScrollTop="chartScrollTop" :opts="pieOpts" :chartData="pieChartData" />
        <view v-else class="empty-hint">{{ pieEmptyMessage }}</view>
      </view>

    </view>

    <!-- 弹窗：账本选择 -->
    <LedgerSelectPopup
      ref="ledgerPopup"
      :selectedLedger="selectedLedger"
      :autoSelectDefault="false"
      @select="onLedgerSelected"
    />

    <!-- 弹窗：账户选择 -->
    <AccountSelectPopup
      ref="accountPopup"
      :selectedAccount="selectedAccount"
      :autoSelectDefault="false"
      @select="onAccountSelected"
    />

    <!-- 弹窗：分类选择（包在弹窗中） -->
    <uni-popup ref="categoryPopup" type="center" :mask-click="true">
      <view class="category-popup">
        <view class="popup-header">
          <text class="popup-title">选择分类</text>
          <custom-icon type="guanbi" :size="20" color="#999" @tap="closeCategoryPopup"></custom-icon>
        </view>
        <view class="popup-content">
          <CategorySelect
            ref="categorySelect"
            :selectedCategory="selectedCategory"
            @select="onCategorySelected"
          />
        </view>
      </view>
    </uni-popup>

    <!-- 弹窗：日期范围选择 -->
    <uni-popup ref="dateRangePopup" type="center" :mask-click="true">
      <view class="date-popup-container">
        <view class="popup-header">
          <text class="popup-title">选择日期范围</text>
          <custom-icon type="guanbi" :size="20" color="#999" @tap="closeDateRangePopup"></custom-icon>
        </view>
        <view class="popup-content">
          <uni-datetime-picker
            type="daterange"
            v-model="dateRange"
            :start="minDateStr"
            :end="maxDateStr"
          />
        </view>
        <view class="popup-footer">
          <button class="confirm-btn" @tap="confirmDateRange">确定</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import request from '@/common/request.js';
import backUrl from '@/common/back_url.js';
import { formatAmount as fmtAmount } from '@/common/util.js';
import LedgerSelectPopup from '@/components/ledger-select-popup.vue';
import AccountSelectPopup from '@/components/account-select-popup.vue';
import CategorySelect from '@/components/category-select.vue';
import UniSegmentedControl from '@/uni_modules/uni-segmented-control/components/uni-segmented-control/uni-segmented-control.vue';
import UniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
import UniDateTimePicker from '@/uni_modules/uni-datetime-picker/components/uni-datetime-picker/uni-datetime-picker.vue';

export default {
  components: { LedgerSelectPopup, AccountSelectPopup, CategorySelect, UniSegmentedControl, UniPopup, UniDateTimePicker },
  data() {
    const now = new Date();
    const y = now.getFullYear();
    const m = now.getMonth() + 1;
    const d = now;
    return {
      // 维度切换
      dimensionTabs: ['总览', '年', '月', '自定义'],
      dimensionIndex: 2, // 默认：月
      // 时间参数
      year: y,
      month: m,
      day: d, // Date 对象
      dateRange: [new Date(y, m - 1, 1), new Date(y, m - 1, new Date(y, m, 0).getDate())],
      // 日期选择边界（最小/最大）
      minDateStr: '2000-01-01',
      maxDateStr: `${y}-${String(m).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`,
      // 筛选项
      selectedLedger: null,
      selectedAccount: null,
      selectedCategory: null,
      // 汇总
      summary: { totalIncome: 0, totalExpense: 0, balance: 0 },
      summaryCount: 0,
      // 统计视图数据
      yearlyLineChartData: { categories: [], series: [] },
      monthLineChartData: { categories: [], series: [] },
      dayLineChartData: { categories: [], series: [] },
      categoryLineChartData: { categories: [], series: [] },
      yearRows: [],
      monthRows: [],
      dayRows: [],
      // 通用时间线（保留以作兼容）
      timelineChartData: { categories: [], series: [] },
      // 折线图配置（开启拖拽滚动 + 旋转标签、缩小字号，无格式化/不换行）
      lineOpts: {
        legend: { show: true },
        enableScroll: true,
        xAxis: {
          disableGrid: false,
          type: 'grid',
          gridType: 'dash',
          dashLength: 4,
          fontSize: 10,
          itemCount: 7,
          scrollShow: true,
          rotateLabel: true,
          rotateAngle: 45,
          marginTop: 8
        },
        yAxis: { gridType: 'dash', dashLength: 4, splitNumber: 5, fontSize: 12 },
        extra: { line: { type: 'straight', width: 2, dot: true, activeType: 'hollow', animation: true } }
      },
      // 全屏遮罩与饼图
      fullscreenVisible: false,
      fullscreenMode: 'category', // 'category' | 'trend'
      pieType: 'expense', // 'expense' | 'income'
      orientationLandscape: false,
      pieChartData: { categories: [], series: [] },
      pieOpts: {
        legend: { show: true, position: 'bottom' },
        extra: { pie: { labelWidth: 10 } },
        dataLabel: true
      },
      pieRenderKey: 0,
      pieEmptyMessage: '',
      // 记录页面滚动偏移，供图例点击坐标换算
      chartScrollTop: 0
    }
  },
  computed: {
    dimension() {
      const map = ['total', 'year', 'month', 'custom'];
      return map[this.dimensionIndex] || 'month';
    },
    monthStr() { return String(this.month).padStart(2, '0'); },
    monthValue() { return `${this.year}-${this.monthStr}`; },
    yearValue() { return `${this.year}`; },
    dayStr() {
      const y = this.day.getFullYear();
      const m = String(this.day.getMonth() + 1).padStart(2, '0');
      const d = String(this.day.getDate()).padStart(2, '0');
      return `${y}-${m}-${d}`;
    },
    dayValue() { return this.dayStr; },
    customRangeStr() {
      const [s, e] = this.dateRange || [];
      if (!s || !e) return '请选择范围';
      const fmt = (dt) => {
        if (!dt) return '';
        let d = dt;
        const isDate = Object.prototype.toString.call(d) === '[object Date]';
        if (!isDate) {
          if (typeof d === 'string') {
            d = new Date(d.replace(/-/g, '/'));
          } else if (typeof d === 'number') {
            d = new Date(d);
          } else {
            return '';
          }
        }
        if (isNaN(d.getTime())) return '';
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        return `${y}-${m}-${dd}`;
      };
      const start = fmt(s);
      const end = fmt(e);
      return start && end ? `${start} ~ ${end}` : '请选择范围';
    },
    rangeTitle() {
      switch (this.dimension) {
        case 'total': return '总览';
        case 'year': return `${this.year}年`;
        case 'month': return `${this.year}-${this.monthStr}`;
        case 'day': return this.dayStr;
        case 'custom': return this.customRangeStr;
        default: return '';
      }
    },
    isAppPlus() { return typeof plus !== 'undefined'; },
    pieHasData() {
      const s = this.pieChartData && this.pieChartData.series;
      return Array.isArray(s) && s.length > 0;
    }
  },
  onShow() {
    // 刷新子组件数据（返回后自动更新）
    if (this.$refs.ledgerPopup && this.$refs.ledgerPopup.refreshIfNeeded) {
      this.$refs.ledgerPopup.refreshIfNeeded();
    }
    if (this.$refs.accountPopup && this.$refs.accountPopup.refreshIfNeeded) {
      this.$refs.accountPopup.refreshIfNeeded();
    }
    if (this.$refs.categorySelect && this.$refs.categorySelect.refreshIfNeeded) {
      this.$refs.categorySelect.refreshIfNeeded();
    }
    this.fetchStatistics();
  },
  onPullDownRefresh() {
    const done = () => {
      if (typeof uni !== 'undefined' && uni.stopPullDownRefresh) {
        uni.stopPullDownRefresh();
      }
    };
    const maybePromise = this.resetFilters();
    if (maybePromise && typeof maybePromise.finally === 'function') {
      maybePromise.finally(done);
    } else {
      done();
    }
  },
  onBackPress(e) {
    if (this.fullscreenVisible) {
      this.closeFullscreen();
      return true;
    }
    return false;
  },
  onPageScroll(e) {
    this.chartScrollTop = e && typeof e.scrollTop === 'number' ? e.scrollTop : 0;
  },
  methods: {
    formatAmount(val) {
      return fmtAmount(val);
    },
    // 维度切换
    onDimensionChange(e) {
      const idx = e && typeof e.currentIndex === 'number' ? e.currentIndex : 2;
      this.dimensionIndex = idx;
      if (this.fullscreenVisible) {
        this.closeFullscreen();
      }
      if (this.dimension === 'custom') {
        this.openDateRangePopup();
      }
      this.fetchStatistics();
    },
    // 年/月/日选择
    onYearChange(e) {
      const y = parseInt((e?.detail?.value || `${this.year}`), 10);
      if (!isNaN(y)) { this.year = y; this.fetchStatistics(); }
    },
    onMonthChange(e) {
      const val = e?.detail?.value || '';
      const [y, m] = val.split('-');
      const yy = parseInt(y, 10); const mm = parseInt(m, 10);
      if (!isNaN(yy) && !isNaN(mm)) { this.year = yy; this.month = mm; this.fetchStatistics(); }
    },
    onDayChange(e) {
      const val = e?.detail?.value || this.dayStr;
      const [y, m, d] = val.split('-');
      const yy = parseInt(y, 10), mm = parseInt(m, 10) - 1, dd = parseInt(d, 10);
      const next = new Date(yy, mm, dd);
      if (!isNaN(next.getTime())) { this.day = next; this.fetchStatistics(); }
    },

    // 弹窗控制
    openLedgerPopup() { if (this.$refs.ledgerPopup && this.$refs.ledgerPopup.open) this.$refs.ledgerPopup.open(); },
    openAccountPopup() { if (this.$refs.accountPopup && this.$refs.accountPopup.open) this.$refs.accountPopup.open(); },
    openCategoryPopup() {
      if (this.$refs.categoryPopup && this.$refs.categoryPopup.open) {
        this.$refs.categoryPopup.open();
        if (this.$refs.categorySelect && this.$refs.categorySelect.onPopupOpened) {
          this.$refs.categorySelect.onPopupOpened();
        }
      }
    },
    closeCategoryPopup() { if (this.$refs.categoryPopup && this.$refs.categoryPopup.close) this.$refs.categoryPopup.close(); },

    openDateRangePopup() { if (this.$refs.dateRangePopup && this.$refs.dateRangePopup.open) this.$refs.dateRangePopup.open(); },
    closeDateRangePopup() { if (this.$refs.dateRangePopup && this.$refs.dateRangePopup.close) this.$refs.dateRangePopup.close(); },
    confirmDateRange() {
      const [s, e] = this.dateRange || [];
      const toDate = (dt) => {
        if (!dt) return null;
        let d = dt;
        const isDate = Object.prototype.toString.call(d) === '[object Date]';
        if (!isDate) {
          if (typeof d === 'string') {
            d = new Date(d.replace(/-/g, '/'));
          } else if (typeof d === 'number') {
            d = new Date(d);
          } else {
            return null;
          }
        }
        return isNaN(d.getTime()) ? null : d;
      };
      const start = toDate(s);
      const end = toDate(e);
      if (!start || !end) {
        if (typeof uni !== 'undefined' && uni.showToast) {
          uni.showToast({ title: '请选择有效的日期范围', icon: 'none' });
        }
        return;
      }
      const diffMs = Math.abs(end.getTime() - start.getTime());
      const maxMs = 30 * 24 * 60 * 60 * 1000; // 30天
      if (diffMs > maxMs) {
        if (typeof uni !== 'undefined' && uni.showToast) {
          uni.showToast({ title: '自定义范围最多 30 天，请重新选择', icon: 'none' });
        }
        // 不关闭弹窗，便于用户重选
        return;
      }
      this.closeDateRangePopup();
      this.fetchStatistics();
    },

    // 选择事件
    onLedgerSelected(ledger) { this.selectedLedger = ledger; this.fetchStatistics(); },
    onAccountSelected(account) { this.selectedAccount = account; this.fetchStatistics(); },
    onCategorySelected(category) { this.selectedCategory = category; if (category) this.closeCategoryPopup(); this.fetchStatistics(); },

    // 重置筛选（分段器、时间组件、账本、账户、分类恢复初始化）
    resetFilters() {
      const now = new Date();
      const y = now.getFullYear();
      const m = now.getMonth() + 1;
      // 分段器：默认回到“月”
      this.dimensionIndex = 2;
      // 时间组件：恢复当前年/月/日与当月范围
      this.year = y;
      this.month = m;
      this.day = now;
      this.dateRange = [
        new Date(y, m - 1, 1),
        new Date(y, m - 1, new Date(y, m, 0).getDate())
      ];
      // 选择项：清空
      this.selectedLedger = null;
      this.selectedAccount = null;
      this.selectedCategory = null;
      // 如果弹窗打开，尝试关闭
      if (this.$refs.categoryPopup && this.$refs.categoryPopup.close) {
        this.$refs.categoryPopup.close();
      }
      if (this.$refs.dateRangePopup && this.$refs.dateRangePopup.close) {
        this.$refs.dateRangePopup.close();
      }
      // 返回 Promise，便于下拉刷新正确停止动画
      return this.fetchStatistics();
    },

    // 数据请求与图表构建
    async fetchStatistics() {
      try {
        const params = { dimension: this.dimension };
        // 时间参数
        if (this.dimension === 'year') {
          params.year = this.year;
        } else if (this.dimension === 'month') {
          params.year = this.year; params.month = this.month;
        } else if (this.dimension === 'custom') {
          const [s, e] = this.dateRange || [];
          const fmt = (dt) => {
            if (!dt) return '';
            let d = dt;
            const isDate = Object.prototype.toString.call(d) === '[object Date]';
            if (!isDate) {
              if (typeof d === 'string') {
                // 兼容 iOS/小程序，将 '-' 替换为 '/'
                d = new Date(d.replace(/-/g, '/'));
              } else if (typeof d === 'number') {
                d = new Date(d);
              } else {
                return '';
              }
            }
            if (isNaN(d.getTime())) return '';
            const y = d.getFullYear();
            const m = String(d.getMonth() + 1).padStart(2, '0');
            const dd = String(d.getDate()).padStart(2, '0');
            return `${y}-${m}-${dd}`;
          };
          if (s && e) {
            const start = fmt(s);
            const end = fmt(e);
            if (start && end) { params.startDate = start; params.endDate = end; }
          }
        }
        // 筛选参数
        if (this.selectedLedger && this.selectedLedger.id) params.ledgerId = this.selectedLedger.id;
        if (this.selectedAccount && this.selectedAccount.id) params.accountId = this.selectedAccount.id;
        if (this.selectedCategory && this.selectedCategory.originalId) params.categoryId = this.selectedCategory.originalId;
        const data = await request.get(backUrl.endpoints.statistics_getMultiThreadStatistics, params);
        // 汇总
        this.summary = {
          totalIncome: Number(data?.totalIncome || 0),
          totalExpense: Number(data?.totalExpense || 0),
          balance: Number(data?.balance || 0)
        };
        this.summaryCount = Number(data?.totalCount || data?.billCount || data?.count || data?.total || 0);
        // 图表
        this.buildTimelineChart(data);
         if (this.dimension !== 'total') {
           this.buildCategoryLineChart(data);
         }
      } catch (e) {
        console.error('统计数据获取失败', e);
        this.summary = { totalIncome: 0, totalExpense: 0, balance: 0 };
        this.summaryCount = 0;
        this.timelineChartData = { categories: [], series: [] };
        this.yearlyLineChartData = { categories: [], series: [] };
        this.monthLineChartData = { categories: [], series: [] };
        this.dayLineChartData = { categories: [], series: [] };
        this.categoryLineChartData = { categories: [], series: [] };
        this.yearRows = [];
        this.monthRows = [];
        this.dayRows = [];
      }
    },
    buildTimelineChart(data) {
      const items = Array.isArray(data?.timeline) ? data.timeline : [];
      const categories = items.map(t => t.label);
      const incomeArr = items.map(t => Number(t.income || 0) / 100);
      const expenseArr = items.map(t => Number(t.expense || 0) / 100);
      const balanceArr = incomeArr.map((v, i) => v - (expenseArr[i] || 0));

      // 通用双线（兼容旧逻辑）
      this.timelineChartData = { categories, series: [ { name: '收入', data: incomeArr }, { name: '支出', data: expenseArr } ] };

      if (this.dimension === 'total') {
        // 年度三线图 + 年度列表
        this.yearlyLineChartData = { categories, series: [
          { name: '收入', data: incomeArr },
          { name: '支出', data: expenseArr },
          { name: '结余', data: balanceArr }
        ] };
        this.yearRows = categories.map((label, idx) => ({
          year: label,
          income: incomeArr[idx] || 0,
          expense: expenseArr[idx] || 0,
          balance: balanceArr[idx] || 0
        }));
      } else if (this.dimension === 'year') {
        // 月度三线图 + 月度列表
        this.monthLineChartData = { categories, series: [
          { name: '收入', data: incomeArr },
          { name: '支出', data: expenseArr },
          { name: '结余', data: balanceArr }
        ] };
        this.monthRows = categories.map((label, idx) => ({
          label,
          income: incomeArr[idx] || 0,
          expense: expenseArr[idx] || 0,
          balance: balanceArr[idx] || 0
        }));
      } else if (this.dimension === 'month' || this.dimension === 'custom') {
        // 每日三线图 + 日列表
        this.dayLineChartData = { categories, series: [
          { name: '收入', data: incomeArr },
          { name: '支出', data: expenseArr },
          { name: '结余', data: balanceArr }
        ] };
        this.dayRows = categories.map((label, idx) => ({
          label,
          income: incomeArr[idx] || 0,
          expense: expenseArr[idx] || 0,
          balance: balanceArr[idx] || 0
        }));
      }
    },
    buildCategoryLineChart(data) {
      const items = Array.isArray(data?.categoryStats) ? data.categoryStats : (Array.isArray(data?.categories) ? data.categories : []);
      const byName = {};
      items.forEach(it => {
        const name = it.categoryName || it.name || '未分类';
        const income = Number(it.income || 0) / 100;
        const expense = Number(it.expense || 0) / 100;
        if (!byName[name]) byName[name] = { income: 0, expense: 0 };
        byName[name].income += income;
        byName[name].expense += expense;
      });
      const names = Object.keys(byName).sort((a, b) => byName[b].expense - byName[a].expense);
      const incomeData = names.map(n => byName[n].income);
      const expenseData = names.map(n => byName[n].expense);
      this.categoryLineChartData = {
        categories: names,
        series: [
          { name: '收入', data: incomeData },
          { name: '支出', data: expenseData }
        ]
      };
    },
    // 全屏遮罩：打开/关闭、方向切换、类型切换、饼图数据构建
    openChartFullscreen(mode) {
      this.fullscreenMode = mode === 'trend' ? 'trend' : 'category';
      this.pieType = 'expense';
      this.showAllPieAll = false;
      this.pieOpts = { ...this.pieOpts, legend: { ...(this.pieOpts.legend || {}), position: 'bottom' } };
      this.pieRenderKey++;
      this.fullscreenVisible = true;
      this.buildPieData();
    },
    closeFullscreen() {
      this.fullscreenVisible = false;
      this.orientationLandscape = false;
    },
    toggleOrientation() {
      this.orientationLandscape = !this.orientationLandscape;
      const nextPos = this.orientationLandscape ? 'top' : 'bottom';
      this.pieOpts = { ...this.pieOpts, legend: { ...(this.pieOpts.legend || {}), position: nextPos } };
      this.pieRenderKey++;
      this.buildPieData();
    },
    setPieType(type) {
      const next = type === 'income' ? 'income' : 'expense';
      if (this.pieType !== next) {
        this.pieType = next;
        this.buildPieData();
      }
    },
    // 展示全部（取消 5% 过滤）
    showAllData() {
      this.showAllPieAll = !this.showAllPieAll;
      this.buildPieData();
      this.pieRenderKey++;
    },
    buildPieData() {
      // 统一生成饼图数据：series 为 [{ name, data }] 列表，保持折线图原顺序
      let labels = [];
      let values = [];
      if (this.fullscreenMode === 'category') {
        labels = Array.isArray(this.categoryLineChartData?.categories) ? this.categoryLineChartData.categories : [];
        const seriesArr = Array.isArray(this.categoryLineChartData?.series) ? this.categoryLineChartData.series : [];
        const target = seriesArr.find(s => (this.pieType === 'income' ? /收入/.test(s.name) : /支出/.test(s.name)));
        const dataArr = Array.isArray(target?.data) ? target.data : [];
        values = dataArr;
      } else {
        const base = this.dimension === 'year' ? this.monthLineChartData : this.dayLineChartData;
        labels = Array.isArray(base?.categories) ? base.categories : [];
        const seriesArr = Array.isArray(base?.series) ? base.series : [];
        const target = seriesArr.find(s => (this.pieType === 'income' ? /收入/.test(s.name) : /支出/.test(s.name)));
        const dataArr = Array.isArray(target?.data) ? target.data : [];
        values = dataArr;
      }
      const series = labels.map((name, idx) => ({ name, data: Number(values[idx] || 0) }));
      const total = series.reduce((sum, it) => sum + (isNaN(it.data) ? 0 : it.data), 0);
      if (total <= 0) {
        this.pieEmptyMessage = `当前范围内暂无${this.pieType === 'income' ? '收入' : '支出'}数据`;
        this.pieChartData = { series: [] };
        return;
      }
      let filtered;
      if (this.showAllPieAll) {
        // 展示全部：显示全部非零切片
        filtered = series.filter(it => it.data > 0);
      } else {
        // 默认：严格隐藏占比 <5% 的切片
        filtered = series.filter(it => (it.data / total) >= 0.05);
      }
      if (!filtered.length) {
        this.pieEmptyMessage = '已隐藏 小于 5% 切片，点击“展示全部”查看';
        this.pieChartData = { series: [] };
      } else {
        this.pieEmptyMessage = '';
        this.pieChartData = { series: filtered };
      }
    }
  }
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 24rpx; padding: 24rpx; }

/* 汇总卡片 */
.summary-cards { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20rpx; }
.card {display: flex; flex-direction: row; align-items: baseline; gap: 12rpx; }
.card-title { color: #666; font-size: 26rpx; }
.card-value { color: #333; font-size: 36rpx; font-weight: 600; margin-top: 0; }
.card.income .card-value { color: #52c41a; }
.card.expense .card-value { color: #f5222d; }
.card.balance .card-value { color: #1890ff; }
.card.count .card-value { color: #f5222d; }

/* 列表美化 */
.table { display: block; }
.table-header { display: grid; grid-template-columns: 2fr 1fr 1fr 1fr; gap: 12rpx; background: #FAFAFA; border: 1px solid #F0F0F0; border-radius: 12rpx; padding: 16rpx; }
.table-row { display: grid; grid-template-columns: 2fr 1fr 1fr 1fr; gap: 12rpx; background: #fff; border-radius: 12rpx; padding: 16rpx; }
.table-row:nth-child(even) { background: #FCFEFF; }
.cell { color: #333; font-size: 28rpx; }
.cell-time { font-weight: 600; color: #555; }
.table .cell:not(.cell-time) { text-align: right; }

.segmented-container { display: flex; justify-content: center; align-items: center; padding: 10rpx 0; background-color: #fff; border-radius: 16rpx; }

.toolbar { display: flex; align-items: center; flex-wrap: wrap; gap: 12rpx; background: #fff; border-radius: 16rpx; padding: 24rpx; }
.toolbar-item { display: flex; align-items: center; gap: 16rpx; background: #f5f5f5; border-radius: 9999rpx; padding: 8rpx 16rpx; }
.toolbar-item.active { background: #e8f4fd; }
.toolbar-item.active text { color: #007AFF; }
.label { color: #666; }
.picker-view { color: #333; }
.refresh-btn { margin-left: 16rpx; }

.filters { display: flex; align-items: center; gap: 16rpx; background: #fff; border-radius: 16rpx; padding: 16rpx; }
.filter-tag { padding: 10rpx 20rpx; border-radius: 24rpx; background: #f5f5f5; color: #333; font-size: 26rpx; }
.filter-tag.selected { background: #e8f4fd; color: #007AFF; }
.filter-tag.reset { background: #fff; border: 1px solid #eee; }

.section { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 24rpx; }
.section-title { font-size: 30rpx; color: #333; margin-bottom: 16rpx; display: flex; align-items: center; justify-content: space-between; }
qiun-data-charts { width: 100%; height: 800rpx; display: block; }

/* 全屏遮罩：饼图展示 */
.fullscreen-overlay { position: fixed; left: 0; top: 0; right: 0; bottom: 0; background: #fff; z-index: 9999; display: flex; flex-direction: column; }
.overlay-header { display: flex; align-items: center; justify-content: space-between; padding: 24rpx; border-bottom: 1px solid #f0f0f0; z-index: 3; position: relative; }
.overlay-title { font-size: 32rpx; font-weight: 600; color: #333; }
.overlay-actions-header { display: flex; align-items: center; gap: 16rpx; position: relative; z-index: 4; }
.overlay-side { position: absolute; right: 24rpx; top: 90rpx; display: flex; flex-direction: column; gap: 12rpx; z-index: 3; }
.overlay-content { position: relative; flex: 1; padding: 24rpx; display: flex; align-items: center; justify-content: center; z-index: 1; }
.fullscreen-overlay qiun-data-charts { width: 100%; height: 100%; }
.orientation-hint { position: absolute; bottom: 24rpx; right: 24rpx; color: #999; font-size: 24rpx; z-index: 3; }
.overlay-tip { font-size: 24rpx; color: #999; margin-left: 16rpx; }
.empty-hint { color: #999; font-size: 28rpx; text-align: center; }

/* 分类弹窗样式 */
.category-popup { width: 680rpx; background-color: #fff; border-radius: 24rpx; overflow: hidden; box-shadow: 0 10rpx 40rpx rgba(0,0,0,0.15); }
.popup-header { padding: 30rpx 40rpx; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f0f0f0; background-color: #FAFAFA; }
.popup-title { font-size: 34rpx; font-weight: bold; color: #333; }
.popup-content { max-height: 600rpx; overflow-y: auto; padding: 30rpx 40rpx; }
.date-popup-container { width: 680rpx; background-color: #fff; border-radius: 24rpx; overflow: hidden; box-shadow: 0 10rpx 40rpx rgba(0,0,0,0.15); }
.popup-footer { padding: 20rpx 40rpx; display: flex; justify-content: flex-end; }
.confirm-btn { background-color: #007AFF; color: #fff; border: none; padding: 0 28rpx; border-radius: 24px; width: 80%;}
</style>