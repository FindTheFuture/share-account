<template>
  <view class="scheduled-bill-edit-page">
    <!-- 表单内容 -->
    <view class="form-container">
      <view class="form-title">设置定时记账配置，系统将按照设定的周期自动创建账单</view>
      
      <!-- 名称 -->
      <view class="form-item">
        <text class="form-label">名称 <text class="required">*</text></text>
        <view class="form-control">
          <input 
            type="text" 
            v-model="formData.name" 
            placeholder="请输入配置名称" 
            maxlength="20"
          />
          <view class="char-count" v-if="formData.name.length > 0">
            <text>{{ formData.name.length }}/20</text>
          </view>
        </view>
      </view>
      
      <!-- 开始时间和结束时间 -->
      <view class="form-item">
        <text class="form-label">时间范围 <text class="required">*</text></text>
        <view class="form-control">
          <view class="datetime-range-picker" @click="showDateTimeRangePicker">
            <text class="datetime-range-text">{{ formattedDateTimeRange || '请选择时间范围' }}</text>
            <uni-icons type="arrowright" size="24" color="#999"></uni-icons>
          </view>
        </view>
      </view>
      
      <!-- 是否提醒 -->
      <view class="form-item radio-group">
        <text class="form-label">是否提醒 <text class="required">*</text></text>
        <view class="form-control radio-options">
          <view 
            class="radio-option" 
            v-for="option in reminderOptions" 
            :key="option.value" 
            @click="formData.isRemind = option.value"
            :class="{ 'radio-selected': formData.isRemind === option.value }"
          >
            <view class="radio-circle">
              <view class="radio-dot" v-if="formData.isRemind === option.value"></view>
            </view>
            <text class="radio-text">{{ option.text }}</text>
          </view>
        </view>
      </view>
      
      <!-- 分类 -->
      <view class="form-item">
        <text class="form-label">分类 <text class="required">*</text></text>
        <view class="form-control">
          <view class="popup-select" @click="showCategoryPopup">
            <text class="select-text" :class="{ 'selected-text': selectedCategory }">{{ selectedCategory?.name || '请选择分类' }}</text>
            <uni-icons type="arrowright" size="24" color="#999"></uni-icons>
          </view>
        </view>
      </view>
      
      <!-- 账本 -->
      <view class="form-item">
        <text class="form-label">账本 <text class="required">*</text></text>
        <view class="form-control">
          <view class="popup-select" @click="showLedgerPopup">
            <text class="select-text" :class="{ 'selected-text': selectedLedger }">{{ selectedLedger?.name || '请选择账本' }}</text>
            <uni-icons type="arrowright" size="24" color="#999"></uni-icons>
          </view>
        </view>
      </view>
      
      <!-- 账户 -->
      <view class="form-item">
        <text class="form-label">账户 <text class="required">*</text></text>
        <view class="form-control">
          <view class="popup-select" @click="showAccountPopup">
            <text class="select-text" :class="{ 'selected-text': selectedAccount }">{{ selectedAccount?.name || '请选择账户' }}</text>
            <uni-icons type="arrowright" size="24" color="#999"></uni-icons>
          </view>
        </view>
      </view>
      
      <!-- 金额 -->
      <view class="form-item">
        <text class="form-label">金额<text class="unit">（元）</text> <text class="required">*</text></text>
        <view class="form-control input-with-prefix">
          <text class="currency-prefix">¥</text>
          <input 
            type="text" 
            v-model="formData.price" 
            placeholder="0.00" 
            @input="handleAmountInput"
            class="amount-input"
          />
        </view>
      </view>
      
      <!-- 周期 -->
      <view class="form-item">
        <text class="form-label">周期 <text class="required">*</text></text>
        <view class="form-control cycle-container">
          <input 
            type="text" 
            v-model="formData.cycleValue" 
            placeholder="请输入周期值" 
            @input="handleCycleValueInput"
            class="cycle-value-input"
          />
          <view class="cycle-type-select">
            <uni-data-select 
              v-model="formData.cycleType" 
              :localdata="cycleTypeOptions" 
              placeholder="选择周期类型" 
              @change="handleCycleTypeChange"
              class="custom-select"
            ></uni-data-select>
          </view>
          <view class="cycle-info-icon" @click="showCycleTypeInfo">
            <uni-icons type="info" size="24" color="#1890ff"></uni-icons>
          </view>
        </view>
      </view>
      
      <!-- 第一次执行时间 -->
      <view class="form-item">
        <text class="form-label">参考执行时间 <text class="required">*</text></text>
        <view class="form-control">
          <view class="time-selector" @click="showDateTimePicker">
            <text class="time-text">{{ formData.executeTime || '' }}</text>
            <uni-icons type="arrowright" size="24" color="#999"></uni-icons>
          </view>
        </view>
        <!-- 示例展示区域 -->
        <view class="example-container" v-if="nextExecuteTimeExample">
          <text class="example-label">示例下次执行时间：</text>
          <text class="example-value">{{ nextExecuteTimeExample }}</text>
        </view>
      </view>
      
      <!-- 描述 -->
      <view class="form-item">
        <text class="form-label">描述</text>
        <view class="form-control">
          <textarea 
            v-model="formData.description" 
            placeholder="请输入描述" 
            rows="3"
            maxlength="100"
            class="remark-textarea"
          ></textarea>
          <view class="char-count" v-if="formData.description.length > 0">
            <text>{{ formData.description.length }}/100</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部操作按钮 -->
    <view class="bottom-actions">
      <button class="btn cancel" @click="navigateBack">取消</button>
      <button class="btn confirm" @click="handleSubmit" :disabled="submitting">{{ submitting ? '提交中...' : '保存' }}</button>
    </view>
    
    <!-- 分类选择弹窗 -->
    <uni-popup ref="categoryPopup" type="bottom" :mask-click="!isCategoryChildOpen" @close="handleCategoryPopupClose">
      <view class="category-popup" style="background-color: #ffffff; border-radius: 20rpx; overflow: hidden;">
        <view class="popup-content">
          <category-select
            ref="categorySelect"
            :selectedCategory="selectedCategory"
            @select="handleCategorySelect"
            @child-popup-open="onCategoryChildOpen"
            @child-popup-close="onCategoryChildClose"
          />
        </view>
      </view>
    </uni-popup>
    
    <!-- 账本选择弹窗 -->
    <LedgerSelectPopup
      ref="ledgerPopup"
      :selectedLedger="selectedLedger"
      :autoSelectDefault="false"
      @select="handleLedgerSelect"
    />
    
    <!-- 账户选择弹窗 -->
    <AccountSelectPopup
      ref="accountPopup"
      :selectedAccount="selectedAccount"
      :autoSelectDefault="false"
      @select="handleAccountSelect"
    />
    
    <!-- 日期时间范围选择弹窗 -->
    <uni-popup ref="dateTimeRangePopup" type="bottom" :mask-click="false">
      <view class="time-popup-content">
        <view class="time-popup-header">
          <text class="time-popup-title">选择时间范围</text>
          <view class="time-popup-actions">
            <text class="time-popup-cancel" @click="closeDateTimeRangePopup">取消</text>
            <text class="time-popup-confirm" @click="confirmDateTimeRange">确定</text>
          </view>
        </view>
        <view class="time-picker-container">
          <uni-datetime-picker 
            v-model="tempDateTimeRange" 
            type="daterange" 
            rangeSeparator="至"
            start="2020-01-01"
            end="2030-12-31"
            class="time-picker"
          />
        </view>
      </view>
    </uni-popup>
    
    <!-- 第一次执行时间选择弹窗 -->
    <uni-popup ref="dateTimePopup" type="bottom" :mask-click="false">
      <view class="date-time-popup-content">
        <view class="date-time-popup-header">
          <text class="date-time-popup-title">参考执行时间</text>
          <view class="date-time-popup-actions">
            <text class="date-time-popup-cancel" @click="closeDateTimePicker">取消</text>
            <text class="date-time-popup-confirm" @click="confirmDateTimePicker">确定</text>
          </view>
        </view>
        <view class="date-time-picker-container">
          <uni-datetime-picker 
            v-model="tempExecuteTime" 
            type="datetime" 
            start="2020-01-01 00:00:00"
            end="2030-12-31 23:59:59"
            class="date-time-picker"
          />
        </view>
      </view>
    </uni-popup>
    
    <!-- 周期类型说明弹窗 -->
    <uni-popup ref="cycleTypeInfoPopup" type="center" :mask-click="true">
      <view class="cycle-type-info-popup">
        <view class="cycle-type-info-header">
          <text class="cycle-type-info-title">周期类型说明</text>
          <uni-icons type="close" size="24" color="#999" @click="closeCycleTypeInfo"></uni-icons>
        </view>
        <view class="cycle-type-info-content">
          <view class="cycle-type-info-item">
            <text class="cycle-type-info-item-label">1. 天：</text>
            <text class="cycle-type-info-item-desc">几天执行1次，填写数字</text>
          </view>
          <view class="cycle-type-info-item">
            <text class="cycle-type-info-item-label">2. 周：</text>
            <text class="cycle-type-info-item-desc">周几执行1次，填写1-7（1=周一，7=周日）</text>
          </view>
          <view class="cycle-type-info-item">
            <text class="cycle-type-info-item-label">3. 月：</text>
            <text class="cycle-type-info-item-desc">每月指定几号执行1次，填写1-31之间的数字</text>
          </view>
          <view class="cycle-type-info-item">
            <text class="cycle-type-info-item-label">4. 季度：</text>
            <text class="cycle-type-info-item-desc">每季度指定第几个月1号执行1次，填写1-3（1=第1个月，2=第2个月，3=第3个月）</text>
          </view>
          <view class="cycle-type-info-item">
            <text class="cycle-type-info-item-label">5. 年：</text>
            <text class="cycle-type-info-item-desc">每年指定几月份1号执行1次，填写1-12（1=1月，12=12月）</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import request from '@/common/request.js';
import backUrl from '@/common/back_url.js';
import uniDataSelect from '@/uni_modules/uni-data-select/components/uni-data-select/uni-data-select.vue';
import uniDateTimePicker from '@/uni_modules/uni-datetime-picker/components/uni-datetime-picker/uni-datetime-picker.vue';
import uniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import uniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
import CategorySelect from '@/components/category-select.vue';
import LedgerSelectPopup from '@/components/ledger-select-popup.vue';
import AccountSelectPopup from '@/components/account-select-popup.vue';
import TimePicker from '@/components/time-picker.vue';

export default {
  components: {
    uniDataSelect,
    uniDateTimePicker,
    uniIcons,
    uniPopup,
    CategorySelect,
    LedgerSelectPopup,
    AccountSelectPopup,
    TimePicker
  },
  data() {
    return {
      submitting: false,
      id: null,
      formData: {
        id: null,
        name: '',
        startDate: '',
        endDate: '',
        isRemind: 0,
        classId: null,
        ledgerId: null,
        accountId: null,
        price: '',
        cycleValue: '1',
        cycleType: 3,
        executeTime: '',
        description: ''
      },
      datetimeRange: [],
      tempDateTimeRange: [],
      tempExecuteTime: '',
      selectedCategory: null,
      selectedLedger: null,
      selectedAccount: null,
      reminderOptions: [
        { value: 0, text: '否' },
        { value: 1, text: '是' }
      ],
      cycleTypeOptions: [
        { value: 1, text: '天' },
        // { value: 2, text: '周' },
        { value: 3, text: '月' },
        // { value: 4, text: '季度' },
        { value: 5, text: '年' }
      ],
      isCategoryChildOpen: false
    };
  },
  
  computed: {
    /**
     * 格式化显示时间范围
     */
    formattedDateTimeRange() {
      if (!this.formData.startDate || !this.formData.endDate) {
        return '';     
      }
      return `${this.formData.startDate.replace('T', ' ')} 至 ${this.formData.endDate.replace('T', ' ')}`;
    },
    
    /**
     * 计算下一次执行时间示例
     */
    nextExecuteTimeExample() {
      // 确保所有必要的字段都已填写
      if (!this.formData.cycleType || !this.formData.cycleValue || !this.formData.executeTime) {
        return '';
      }
      
      try {
        const cycleType = Number(this.formData.cycleType);
        const cycleValue = Number(this.formData.cycleValue);
        const executeTime = this.formData.executeTime;
        
        // 解析第一次执行时间
        const [datePart, timePart] = executeTime.split(' ');
        const [year, month, day] = datePart.split('-').map(Number);
        const [hours, minutes, seconds] = timePart.split(':').map(Number);
        
        // 创建第一次执行时间的日期对象
        const firstExecuteDate = new Date(year, month - 1, day, hours, minutes, seconds);
        
        // 创建下一次执行时间的基础日期
        let nextExecuteDate = new Date(firstExecuteDate);
        
        // 根据周期类型计算下一次执行时间
        switch (cycleType) {
          case 1: // 天
            // 每隔cycleValue天执行一次，计算第一次执行时间 + 周期值
            const cycleDays = cycleValue || 1;
            nextExecuteDate.setDate(nextExecuteDate.getDate() + cycleDays);
            break;
            
          // case 2: // 周
          //   // 调整为0-6的格式（周日到周六），其中1=周一，7=周日
          //   const targetDayOfWeek = cycleValue === 7 ? 0 : cycleValue;
          //   
          //   // 计算第一次执行时间是周几
          //   const firstDayOfWeek = firstExecuteDate.getDay(); // 0-6, 0是周日
          //   
          //   // 计算从第一次执行时间到目标周几需要的天数
          //   let daysUntilTarget = targetDayOfWeek - firstDayOfWeek;
          //   if (daysUntilTarget < 0) {
          //     daysUntilTarget += 7;
          //   }
          //   
          //   // 设置第一次执行后的下一次执行时间
          //   nextExecuteDate.setDate(nextExecuteDate.getDate() + daysUntilTarget);
          //   break;
          //   
          case 3: // 月
            // 每隔cycleValue个月执行一次，计算第一次执行时间 + 周期值
            const cycleMonths = cycleValue;
            // 计算目标月份的最大天数
            const targetYear = nextExecuteDate.getFullYear();
            const targetMonth = nextExecuteDate.getMonth() + cycleMonths;
            const adjustedYear = targetYear + Math.floor(targetMonth / 12);
            const adjustedMonth = targetMonth % 12;
            const maxDaysInMonth = new Date(adjustedYear, adjustedMonth + 1, 0).getDate();
            const actualTargetDate = Math.min(day, maxDaysInMonth);
            
            // 设置日期
            nextExecuteDate.setFullYear(adjustedYear);
            nextExecuteDate.setMonth(adjustedMonth);
            nextExecuteDate.setDate(actualTargetDate);
            break;
            
          case 5: // 年
            // 每隔cycleValue年执行一次，计算第一次执行时间 + 周期值
            const cycleYears = cycleValue;
            // 计算目标年份
            const yearTarget = nextExecuteDate.getFullYear() + cycleYears;
            const monthTarget = nextExecuteDate.getMonth();
            
            // 计算目标月份的最大天数
            const maxDaysInYearMonth = new Date(yearTarget, monthTarget + 1, 0).getDate();
            const actualTargetDay = Math.min(day, maxDaysInYearMonth);
            
            // 设置日期
            nextExecuteDate.setFullYear(yearTarget);
            nextExecuteDate.setMonth(monthTarget);
            nextExecuteDate.setDate(actualTargetDay);
            break;
            
          default:
            return '';
        }
        
        // 格式化输出
        const yearStr = nextExecuteDate.getFullYear();
        const monthStr = String(nextExecuteDate.getMonth() + 1).padStart(2, '0');
        const dayStr = String(nextExecuteDate.getDate()).padStart(2, '0');
        
        return `${yearStr}-${monthStr}-${dayStr} ${timePart}`;
      } catch (error) {
        console.error('计算执行时间示例失败:', error);
        return '';
      }
    }
  },
  
  onLoad(options) {
    if (options && options.id) {
      this.isEdit = true;
      this.id = options.id;
      uni.setNavigationBarTitle({ title: '编辑定时记账配置' });
      this.loadScheduledBillDetail();
    } else {
      uni.setNavigationBarTitle({ title: '新增定时记账配置' });
      // 设置默认值
      this.formData.isReminder = 0;
      this.formData.cycleType = 3; // 默认值为 3，表示每月
      this.formData.cycleValue = '1';
      // 设置默认时间范围
      const now = new Date();
      // 开始时间：今天0点0分0秒
      const startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0);
      // 结束时间：一年后的今天0点0分0秒
      const endDate = new Date(now.getFullYear() + 1, now.getMonth(), now.getDate(), 0, 0, 0);
      // 执行时间：开始时间的1小时后
      const executeTime = new Date(startDate);
      executeTime.setHours(executeTime.getHours() + 1);
      
      // 格式化时间为本地时间
      function formatLocalDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
      }
      
      function formatLocalDateTime(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
      }
      
      const startDateStr = formatLocalDate(startDate);
      const endDateStr = formatLocalDate(endDate);
      const executeTimeStr = formatLocalDateTime(executeTime);
      
      // 设置表单数据
      this.formData.startDate = startDateStr;
      this.formData.endDate = endDateStr;
      this.formData.executeTime = executeTimeStr;
      this.tempDateTimeRange = [startDateStr, endDateStr];
      this.tempExecuteTime = executeTimeStr;
    }
  },
  
  methods: {
    /**
     * 加载定时记账配置详情
     */
    async loadScheduledBillDetail() {
      try {
        uni.showLoading({ title: '加载中...' });
        const response = await request({
          url: backUrl.endpoints.scheduledBill_getById + this.id,
          method: 'GET'
        });
        
        if (response) {
          const data = response;
          // 格式化时间为本地时间 YYYY-MM-DD 格式
          function formatLocalDate(date) {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
          }
          
          const startDate = data.startDate ? formatLocalDate(new Date(data.startDate)) : '';
          const endDate = data.endDate ? formatLocalDate(new Date(data.endDate)) : '';
          const executeTime = data.executeTime ? (data.executeTime.includes(':') ? data.executeTime : '08:00:00') : '08:00:00';
          
          this.formData = {
            id: data.id,
            name: data.name || '',
            startDate: startDate,
            endDate: endDate,
            isRemind: data.isRemind || 0,
            classId: data.classId,
            ledgerId: data.ledgerId,
            accountId: data.accountId,
            price: data.price || '0.00',
            cycleValue: data.cycleValue?.toString() || '1',
            cycleType: data.cycleType || 3, // 确保为数字类型，默认值为 3
            executeTime: executeTime,
            description: data.description || ''
          };
          
          // 设置时间范围
          this.tempDateTimeRange = [startDate, endDate];
          this.tempExecuteTime = executeTime;
          
          // 加载关联数据
          if (data.classId) {
            this.selectedCategory = {
              id: data.classId,
              name: data.className || ''
            };
          }
          if (data.ledgerId) {
            this.selectedLedger = {
              id: data.ledgerId,
              name: data.ledgerName || ''
            };
          }
          if (data.accountId) {
            this.selectedAccount = {
              id: data.accountId,
              name: data.accountName || ''
            };
          }
        } else {
          uni.showToast({
            title: '加载失败',
            icon: 'none'
          });
          this.navigateBack();
        }
      } catch (error) {
        console.error('加载定时记账配置详情失败:', error);
        uni.showToast({ title: '网络异常', icon: 'none' });
        this.navigateBack();
      } finally {
        uni.hideLoading();
      }
    },
    
    /**
     * 显示日期时间范围选择器
     */
    showDateTimeRangePicker() {
      if (this.$refs.dateTimeRangePopup) {
        // 初始化临时时间范围
        this.tempDateTimeRange = [this.formData.startDate, this.formData.endDate];
        this.$refs.dateTimeRangePopup.open();
      }
    },
    
    /**
     * 关闭日期时间范围选择器
     */
    closeDateTimeRangePopup() {
      if (this.$refs.dateTimeRangePopup) {
        this.$refs.dateTimeRangePopup.close();
      }
    },
    
    /**
     * 确认日期时间范围选择
     */
    confirmDateTimeRange() {
      if (this.tempDateTimeRange && this.tempDateTimeRange.length === 2) {
        // 确保时间格式为年月日
        const startDate = new Date(this.tempDateTimeRange[0]);
        const endDate = new Date(this.tempDateTimeRange[1]);
        
        // 格式化时间为本地时间
        function formatLocalDate(date) {
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, '0');
          const day = String(date.getDate()).padStart(2, '0');
          return `${year}-${month}-${day}`;
        }
        
        function formatLocalDateTime(date) {
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, '0');
          const day = String(date.getDate()).padStart(2, '0');
          const hours = String(date.getHours()).padStart(2, '0');
          const minutes = String(date.getMinutes()).padStart(2, '0');
          const seconds = String(date.getSeconds()).padStart(2, '0');
          return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
        }
        
        // 格式化时间
        const startDateStr = formatLocalDate(startDate);
        const endDateStr = formatLocalDate(endDate);
        
        // 设置表单数据
        this.formData.startDate = startDateStr;
        this.formData.endDate = endDateStr;
        
        // 执行时间：开始时间的1小时后
        const executeTime = new Date(startDate);
        executeTime.setHours(executeTime.getHours() + 1);
        const executeTimeStr = formatLocalDateTime(executeTime);
        this.formData.executeTime = executeTimeStr;
        this.tempExecuteTime = executeTimeStr;
      }
      this.closeDateTimeRangePopup();
    },
    
    /**
     * 处理金额输入
     */
    handleAmountInput(e) {
      const value = e.detail.value;
      // 确保只输入数字和小数点，且小数点只能有一个
      let filteredValue = value.replace(/[^\d.]/g, '').replace(/(\..*)\./g, '$1');
      // 确保小数点后最多两位
      filteredValue = filteredValue.replace(/\.(\d{2}).*/g, '.$1');
      this.formData.price = filteredValue;
    },
    
    /**
     * 处理周期值输入
     */
    handleCycleValueInput(e) {
      const value = e.detail.value;
      // 确保输入的是正整数，且不能以0开头
      let filteredValue = value.replace(/\D/g, '').replace(/^0+/, '');
      
      // 根据不同的周期类型限制输入范围
      if (filteredValue) {
        const cycleValue = parseInt(filteredValue);
        const cycleType = this.formData.cycleType;
        
        switch (cycleType) {
          case 1: // 天
            // 限制只能输入正整数
            if (cycleValue < 1) {
              filteredValue = '1';
            }
            break;
          case 3: // 月
            // 限制只能输入正整数
            if (cycleValue < 1) {
              filteredValue = '1';
            }
            break;
          case 5: // 年
            // 限制只能输入正整数
            if (cycleValue < 1) {
              filteredValue = '1';
            }
            break;
        }
      }
      
      this.formData.cycleValue = filteredValue;
    },
    
    /**
     * 处理周期类型变更
     */
    handleCycleTypeChange(e) {
      // 兼容不同的事件对象结构
      let value;
      if (typeof e === 'object') {
        if (e.detail && typeof e.detail === 'object') {
          value = e.detail.value;
        } else {
          value = e;
        }
      } else {
        value = e;
      }
      
      // 检查当前cycleValue是否符合新的cycleType的范围要求
      const cycleValue = Number(this.formData.cycleValue);
      let isValid = true;
      let message = '';
      
      switch (value) {
        case 1: // 天
          if (cycleValue < 1) {
            isValid = false;
            message = '天周期类型要求cycleValue为正整数';
          }
          break;
        case 3: // 月
          if (cycleValue < 1) {
            isValid = false;
            message = '月周期类型要求cycleValue为正整数';
          }
          break;
        case 5: // 年
          if (cycleValue < 1) {
            isValid = false;
            message = '年周期类型要求cycleValue为正整数';
          }
          break;
      }
      
      // 如果不符合要求，弹窗提示用户
      if (!isValid) {
        uni.showToast({
          title: message,
          icon: 'none',
          duration: 2000
        });
      }
      
      this.formData.cycleType = value;
    },
    
    /**
     * 显示第一次执行时间选择器
     */
    showDateTimePicker() {
      if (this.$refs.dateTimePopup) {
        // 初始化临时执行时间
        this.tempExecuteTime = this.formData.executeTime || new Date().toISOString().slice(0, 19).replace('T', ' ');
        this.$refs.dateTimePopup.open();
      }
    },
    
    /**
     * 关闭第一次执行时间选择器
     */
    closeDateTimePicker() {
      if (this.$refs.dateTimePopup) {
        this.$refs.dateTimePopup.close();
      }
    },
    
    /**
     * 确认第一次执行时间选择
     */
    confirmDateTimePicker() {
      this.formData.executeTime = this.tempExecuteTime;
      this.closeDateTimePicker();
    },
    
    /**
     * 打开分类选择弹窗
     */
    showCategoryPopup() {
      if (this.$refs.categoryPopup) {
        this.$refs.categoryPopup.open();
        // 弹窗打开后，通知子组件重新测量宫格
        this.$nextTick(() => {
          if (this.$refs.categorySelect && typeof this.$refs.categorySelect.onPopupOpened === 'function') {
            this.$refs.categorySelect.onPopupOpened();
          }
        });
      }
    },
    
    /**
     * 处理分类选择
     */
    handleCategorySelect(category) {
      this.selectedCategory = category;
      this.formData.classId = category?.id;
      this.$refs.categoryPopup.close();
    },
    
    /**
     * 分类子弹窗打开
     */
    onCategoryChildOpen() {
      this.isCategoryChildOpen = true;
    },
    
    /**
     * 分类子弹窗关闭
     */
    onCategoryChildClose() {
      this.isCategoryChildOpen = false;
    },
    
    /**
     * 处理分类弹窗关闭
     */
    handleCategoryPopupClose() {
      this.isCategoryChildOpen = false;
    },
    
    /**
     * 打开账本选择弹窗
     */
    showLedgerPopup() {
      if (this.$refs.ledgerPopup) {
        this.$refs.ledgerPopup.open();
      }
    },
    
    /**
     * 处理账本选择
     */
    handleLedgerSelect(ledger) {
      this.selectedLedger = ledger;
      this.formData.ledgerId = ledger?.id;
    },
    
    /**
     * 打开账户选择弹窗
     */
    showAccountPopup() {
      if (this.$refs.accountPopup) {
        this.$refs.accountPopup.open();
      }
    },
    
    /**
     * 处理账户选择
     */
    handleAccountSelect(account) {
      this.selectedAccount = account;
      this.formData.accountId = account?.id;
    },
    
    /**
     * 表单验证
     */
    validateForm() {
      if (!this.formData.name.trim()) {
        uni.showToast({ title: '请输入配置名称', icon: 'none' });
        return false;
      }
      
      if (!this.formData.startDate) {
        uni.showToast({ title: '请选择开始时间', icon: 'none' });
        return false;
      }
      
      if (!this.formData.endDate) {
        uni.showToast({ title: '请选择结束时间', icon: 'none' });
        return false;
      }
      
      if (!this.formData.classId) {
        uni.showToast({ title: '请选择分类', icon: 'none' });
        return false;
      }
      
      if (!this.formData.ledgerId) {
        uni.showToast({ title: '请选择账本', icon: 'none' });
        return false;
      }
      
      if (!this.formData.accountId) {
        uni.showToast({ title: '请选择账户', icon: 'none' });
        return false;
      }
      
      if (!this.formData.price || Number(this.formData.price) <= 0) {
        uni.showToast({ title: '请输入有效金额', icon: 'none' });
        return false;
      }
      
      if (!this.formData.cycleValue || Number(this.formData.cycleValue) <= 0) {
        uni.showToast({ title: '请输入有效周期值', icon: 'none' });
        return false;
      }
      
      if (!this.formData.cycleType) {
        uni.showToast({ title: '请选择周期类型', icon: 'none' });
        return false;
      }
      
      if (!this.formData.executeTime) {
        uni.showToast({ title: '请选择执行时间', icon: 'none' });
        return false;
      }
      
      // 验证开始时间是否早于结束时间
      const startDate = new Date(this.formData.startDate);
      const endDate = new Date(this.formData.endDate);
      if (startDate >= endDate) {
        uni.showToast({ title: '开始时间必须早于结束时间', icon: 'none' });
        return false;
      }
      
      // 验证执行时间是否在有效范围内
      const executeTime = new Date(this.formData.executeTime);
      const executeDate = new Date(executeTime.getFullYear(), executeTime.getMonth(), executeTime.getDate());
      const startDateTime = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate());
      const endDateTime = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate());
      
      if (executeDate < startDateTime) {
        uni.showToast({ title: '执行时间不能早于开始时间', icon: 'none' });
        return false;
      }
      
      if (executeDate > endDateTime) {
        uni.showToast({ title: '执行时间不能晚于结束时间', icon: 'none' });
        return false;
      }
      
      return true;
    },
    
    /**
     * 处理表单提交
     */
    async handleSubmit() {
      if (!this.validateForm()) {
        return;
      }
      
      this.submitting = true;
      
      try {
        // 构建请求数据，匹配后端参数格式
        const requestData = {
          id: this.formData.id,
          name: this.formData.name.trim(),
          startDate: this.formData.startDate,
          endDate: this.formData.endDate,
          isRemind: this.formData.isRemind,
          classId: this.formData.classId,
          ledgerId: this.formData.ledgerId,
          accountId: this.formData.accountId,
          price: Math.round(Number(this.formData.price) * 100), // 转换为分
          cycleType: Number(this.formData.cycleType),
          cycleValue: Number(this.formData.cycleValue),
          executeTime: this.formData.executeTime,
          description: this.formData.description.trim()
        };
        
        // 发送请求
        const response = await request({
          url: backUrl.endpoints.scheduledBill_save,
          method: 'POST',
          data: requestData
        });
        
        if (response !== undefined) {
          uni.showToast({
            title: this.id ? '更新成功' : '添加成功',
            icon: 'success'
          });
          
          // 延迟返回
          setTimeout(() => {
            uni.navigateBack();
          }, 1000);
        } else {
          uni.showToast({
            title: this.id ? '更新失败' : '添加失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('保存配置失败:', error);
        uni.showToast({
          title: '网络请求异常，请稍后重试',
          icon: 'none'
        });
      } finally {
        this.submitting = false;
      }
    },
    
    /**
     * 显示周期类型说明
     */
    showCycleTypeInfo() {
      if (this.$refs.cycleTypeInfoPopup) {
        this.$refs.cycleTypeInfoPopup.open();
      }
    },
    
    /**
     * 关闭周期类型说明
     */
    closeCycleTypeInfo() {
      if (this.$refs.cycleTypeInfoPopup) {
        this.$refs.cycleTypeInfoPopup.close();
      }
    },
    
    /**
     * 返回上一页
     */
    navigateBack() {
      uni.navigateBack({ delta: 1 });
    }
  }
};
</script>

<style scoped>
/* 页面整体样式 */
.scheduled-bill-edit-page {
  background-color: #f8f8f8;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 表单容器 */
.form-container {
  flex: 1;
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

/* 表单标题 */
.form-title {
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 30rpx;
  line-height: 1.5;
}

/* 表单项目 */
.form-item {
  margin-bottom: 40rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

/* 表单标签 */
.form-label {
  font-size: 28rpx;
  color: #333333;
  margin-bottom: 16rpx;
  display: block;
  font-weight: 500;
}

.required {
  color: #ff4d4f;
}

/* 表单控件容器 */
.form-control {
  position: relative;
  display: flex;
  align-items: center;
}

/* 带前缀的输入框 */
.input-with-prefix {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.currency-prefix {
  position: absolute;
  left: 24rpx;
  font-size: 28rpx;
  color: #666666;
  z-index: 1;
}

/* 输入框样式 */
input {
  flex: 1;
  height: 88rpx;
  border: 1rpx solid #d9d9d9;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #333333;
  transition: border-color 0.3s;
  background-color: #ffffff;
}

/* 金额输入框特殊样式 */
.amount-input {
  font-size: 36rpx !important;
  font-weight: 500;
  color: #ff7e00 !important;
  padding-left: 50rpx !important;
}

input:focus {
  border-color: #1890ff;
  outline: none;
  box-shadow: 0 0 0 4rpx rgba(24, 144, 255, 0.1);
}

/* 文本域样式 */
.remark-textarea {
  flex: 1;
  min-height: 160rpx;
  border: 1rpx solid #d9d9d9;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 28rpx;
  color: #333333;
  resize: none;
  transition: border-color 0.3s;
}

.remark-textarea:focus {
  border-color: #1890ff;
  outline: none;
  box-shadow: 0 0 0 4rpx rgba(24, 144, 255, 0.1);
}

/* 字数统计 */
.char-count {
  position: absolute;
  right: 24rpx;
  bottom: 16rpx;
  font-size: 24rpx;
  color: #999999;
}

/* 单位样式 */
.unit {
  margin-left: 16rpx;
  font-size: 28rpx;
  color: #666666;
}

/* 单选按钮组样式 */
.radio-group .form-control {
  flex-direction: row;
  gap: 60rpx;
}

.radio-options {
  display: flex;
  gap: 60rpx;
}

.radio-option {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  padding: 10rpx 0;
}

.radio-option.radio-selected .radio-text {
  color: #1890ff;
}

.radio-circle {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #d9d9d9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12rpx;
  transition: border-color 0.3s;
}

.radio-option.radio-selected .radio-circle {
  border-color: #1890ff;
}

.radio-dot {
  width: 24rpx;
  height: 24rpx;
  background-color: #1890ff;
  border-radius: 50%;
  transform: scale(0);
  transition: transform 0.3s;
}

.radio-option.radio-selected .radio-dot {
  transform: scale(1);
}

.radio-text {
  font-size: 28rpx;
  color: #333333;
  transition: color 0.3s;
}

/* 时间选择器样式 */
.datetime-picker {
  width: 100%;
  height: 88rpx;
  border: 1rpx solid #d9d9d9;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #333333;
  background-color: #ffffff;
}

/* 执行时间选择器样式 */
.time-picker {
  width: 100%;
  height: 88rpx;
  border: 1rpx solid #d9d9d9;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #333333;
  background-color: #ffffff;
}

/* 日期时间范围示例 body 样式 */
.example-body {
  margin-top: 16rpx;
}

/* 弹出选择样式 */
.popup-select {
  width: 100%;
  height: 88rpx;
  border: 1rpx solid #d9d9d9;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #333333;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
}

.select-text {
  flex: 1;
  text-align: left;
  color: #999999;
}

.select-text.selected-text {
  color: #1890ff;
}

/* 周期容器样式 */
.cycle-container {
  display: flex;
  gap: 20rpx;
  align-items: center;
}

.cycle-value-input {
  flex: 1;
  min-width: 0;
}

.cycle-type-select {
  flex: 1;
  min-width: 0;
}

.cycle-info-icon {
  margin-left: 10rpx;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 周期类型说明弹窗样式 */
.cycle-type-info-popup {
  width: 80%;
  max-width: 500rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.cycle-type-info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.cycle-type-info-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #333333;
}

.cycle-type-info-content {
  padding: 30rpx;
}

.cycle-type-info-item {
  margin-bottom: 24rpx;
  line-height: 1.5;
}

.cycle-type-info-item:last-child {
  margin-bottom: 0;
}

.cycle-type-info-item-label {
  font-size: 28rpx;
  font-weight: 500;
  color: #333333;
}

.cycle-type-info-item-desc {
  font-size: 26rpx;
  color: #666666;
  margin-top: 8rpx;
  display: block;
}

/* 自定义选择器样式 */
.custom-select {
  width: 100%;
  height: 100%;
}

:deep(.uni-data-select) .uni-select-input {
  height: 88rpx !important;
  font-size: 28rpx;
  color: #333333;
  padding: 0 24rpx;
  border: 1rpx solid #d9d9d9;
  border-radius: 16rpx;
  background-color: #ffffff;
  width: 100%;
}

:deep(.uni-data-select) .uni-select-placeholder {
  color: #999999;
  font-size: 28rpx;
}

/* 底部操作按钮 */
.bottom-actions {
  margin-top: 30rpx;
  padding: 0 30rpx 60rpx;
  display: flex;
  gap: 24rpx;
}

.btn {
  flex: 1;
  height: 96rpx;
  font-size: 32rpx;
  border-radius: 48rpx;
  line-height: 96rpx;
  transition: all 0.3s;
  border: none;
  outline: none;
  font-weight: 500;
}

.btn.cancel {
  background-color: #ffffff;
  color: #666666;
  border: 1rpx solid #d9d9d9;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.btn.cancel:active {
  background-color: #f5f5f5;
}

.btn.confirm {
  background-color: #1890ff;
  color: #ffffff;
  box-shadow: 0 4rpx 16rpx rgba(24, 144, 255, 0.3);
}

.btn.confirm:active {
  background-color: #40a9ff;
}

.btn:disabled {
  opacity: 0.6;
}

/* 分类弹窗样式 */
.category-popup {
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.15);
}

/* 美化选项样式 */
:deep(.uni-picker__content) .uni-picker__item {
  height: 80rpx !important;
  line-height: 80rpx !important;
  font-size: 30rpx !important;
}

:deep(.uni-picker__content) .uni-picker__item--selected {
  color: #1890ff !important;
  font-size: 32rpx !important;
  font-weight: 500;
}

/* 时间弹窗样式 */
.time-popup-content {
  background-color: #ffffff;
  border-radius: 20rpx 20rpx 0 0;
  padding-bottom: 30rpx;
}

.time-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.time-popup-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #333333;
}

.time-popup-actions {
  display: flex;
  gap: 30rpx;
}

.time-popup-cancel {
  font-size: 28rpx;
  color: #999999;
}

.time-popup-confirm {
  font-size: 28rpx;
  color: #1890ff;
  font-weight: 500;
}

.time-picker-container {
  padding: 30rpx;
}

/* 时间选择器样式 */
.time-selector {
  width: 100%;
  height: 88rpx;
  border: 1rpx solid #d9d9d9;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #333333;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  transition: border-color 0.3s;
}

.time-selector:active {
  border-color: #1890ff;
}

.time-text {
  font-size: 28rpx;
  color: #333333;
}

/* 示例展示区域样式 */
.example-container {
  margin-top: 16rpx;
  padding: 20rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.example-label {
  font-size: 24rpx;
  color: #666666;
  font-weight: 500;
}

.example-value {
  font-size: 24rpx;
  color: #1890ff;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 时间输入框样式 */
.time-input {
  flex: 1;
  height: 88rpx;
  border: 1rpx solid #d9d9d9;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #333333;
  transition: border-color 0.3s;
  background-color: #ffffff;
}

/* 日期时间范围选择器样式 */
.datetime-range-picker {
  width: 100%;
  height: 88rpx;
  border: 1rpx solid #d9d9d9;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #333333;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  transition: border-color 0.3s;
}

.datetime-range-picker:active {
  border-color: #1890ff;
}

.datetime-range-text {
  flex: 1;
  text-align: left;
  color: #333333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 第一次执行时间选择弹窗样式 */
.date-time-popup-content {
  background-color: #ffffff;
  border-radius: 20rpx 20rpx 0 0;
  padding-bottom: 30rpx;
}

.date-time-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.date-time-popup-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #333333;
}

.date-time-popup-actions {
  display: flex;
  gap: 30rpx;
}

.date-time-popup-cancel {
  font-size: 28rpx;
  color: #999999;
}

.date-time-popup-confirm {
  font-size: 28rpx;
  color: #1890ff;
  font-weight: 500;
}

.date-time-picker-container {
  padding: 30rpx;
}

.date-time-picker {
  width: 100%;
}
</style>