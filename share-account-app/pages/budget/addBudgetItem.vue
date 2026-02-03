<template>
  <view class="add-budget-item-page">
    <!-- 表单容器 -->
    <view class="form-container">
      <!-- 预算周期（可编辑） -->
      <view class="form-item">
        <text class="form-label">预算周期:</text>
        <picker mode="date" fields="month" :value="monthValue" @change="onMonthPickerChange">
          <view class="category-selector">
            <text class="category-name">{{ year }}-{{ monthStr }}</text>
            <custom-icon type="xiangyou1" :size="20" color="#999"></custom-icon>
          </view>
        </picker>
      </view>

      <!-- 选择账本 -->
      <view class="form-item">
        <text class="form-label">选择账本:</text>
        <view class="category-selector" @click="openLedgerPopup">
          <text class="category-name">{{ selectedLedger ? selectedLedger.name : '请选择账本' }}</text>
          <custom-icon type="xiangyou1" :size="20" color="#999"></custom-icon>
        </view>
      </view>
      <!-- 分类选择 -->
      <view class="form-item">
        <text class="form-label">选择分类:</text>
        <view
          class="category-selector"
          @click="showCategorySelector"
        >
          <text class="category-name">{{ selectedCategory ? selectedCategory.name : '请选择分类' }}</text>
          <custom-icon type="xiangyou1" :size="20" color="#999"></custom-icon>
        </view>
      </view>

      <!-- 预算金额 -->
      <view class="form-item">
        <text class="form-label">预算金额:</text>
        <view class="input-wrapper">
          <text class="currency-symbol">¥</text>
          <input
            type="digit"
            v-model="amount"
            placeholder="请输入预算金额"
            class="amount-input"
            @input="handleAmountInput"
          />
        </view>
      </view>

    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="save-btn" @click="saveBudgetItem">保存</button>
    </view>

    <!-- 分类选择弹窗 -->
    <uni-popup ref="categoryPopup" type="bottom" :custom="true" :mask-click="true">
      <view class="category-popup">
        <view class="popup-header">
          <text class="popup-title">选择分类</text>
          <custom-icon type="guanbi" :size="20" color="#999" @tap.stop="closeCategoryPopup"></custom-icon>
        </view>
        <view class="category-content">
          <view class="popup-content">
            <category-select
              ref="categorySelect"
              :selectedCategory="selectedCategory"
              @select="selectCategory"
            />
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 账本选择弹窗 -->
    <ledger-select-popup
        ref="ledgerSelectPopup"
        :selectedLedger="selectedLedger"
        :queryShared="false"
        @select="onLedgerSelected"
      />

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
import UniCollapse from '@/uni_modules/uni-collapse/components/uni-collapse/uni-collapse.vue';
import UniCollapseItem from '@/uni_modules/uni-collapse/components/uni-collapse-item/uni-collapse-item.vue';
import UniGrid from '@/uni_modules/uni-grid/components/uni-grid/uni-grid.vue';
import UniGridItem from '@/uni_modules/uni-grid/components/uni-grid-item/uni-grid-item.vue';
import UniSegmentedControl from '@/uni_modules/uni-segmented-control/components/uni-segmented-control/uni-segmented-control.vue';
import UniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue';
import CategorySelect from '@/components/category-select.vue';
import LedgerSelectPopup from '@/components/ledger-select-popup.vue';

export default {
  components: {
    UniIcons,
    UniCollapse,
    UniCollapseItem,
    UniGrid,
    UniGridItem,
    UniSegmentedControl,
    UniLoadMore,
    CategorySelect,
    LedgerSelectPopup
  },
  data() {
    return {
      id: '',
      budgetId: '',
      year: '',
      month: '',
      selectedCategory: null,
      selectedLedger: null,
      amount: '',
      memo: '',
      // 分类相关
      loadingCategories: false,
      topCategories: [], // 顶层分类
      middleCategories: [], // 中层分类
      leafCategories: [], // 底层分类
      currentTopCategoryIndex: 0,
      currentMiddleCategoryId: null,
      // 弹窗相关
      categoryPopup: null,
      confirmPopup: null,
      confirmTitle: '',
      confirmContent: '',
      confirmButtonText: '',
      // 手风琴组件绑定值，控制默认闭合状态
      accordionVal: ''
    };
  },
  computed: {
    monthStr() { return String(this.month).padStart(2, '0'); },
    monthValue() { return `${this.year}-${this.monthStr}`; }
  },
  onLoad(options) {
    // 获取页面参数
    this.id = options.id || '';
    this.budgetId = options.budgetId || '';
    this.year = options.year || this.year;
    this.month = options.month || this.month;
    // 默认本年本月（新增或编辑均适用，如果未初始化）
    const now = new Date();
    if (!this.year) this.year = now.getFullYear();
    if (!this.month) this.month = now.getMonth() + 1;
    // 若已有预算ID（新增场景从预算列表进入），同步预算表周期显示
    if (!this.id && this.budgetId) {
      this.fetchBudgetCycle();
    }

    // 初始化弹窗
    this.categoryPopup = this.$refs.categoryPopup;
    this.confirmPopup = this.$refs.confirmPopup;

    // 如果是修改预算明细，加载明细信息
    if (this.id) {
      this.loadBudgetItemInfo();
    } else if (!this.budgetId && this.year && this.month) {
      // 如果没有预算ID但有年份月份，检查是否已有预算
      this.checkBudgetExists();
    }
  },
  onReady() {
    if (!this.id && this.$refs.ledgerSelectPopup && typeof this.$refs.ledgerSelectPopup.initAutoSelect === 'function') {
      this.$refs.ledgerSelectPopup.initAutoSelect();
    }
  },
  onShow() {
    if (this.$refs.categorySelect && this.$refs.categorySelect.refreshIfNeeded) {
      this.$refs.categorySelect.refreshIfNeeded();
    }
    if (this.$refs.ledgerSelectPopup && typeof this.$refs.ledgerSelectPopup.refreshIfNeeded === 'function') {
      this.$refs.ledgerSelectPopup.refreshIfNeeded();
    }
  },
  methods: {
    // 预算周期月份选择回调
    onMonthPickerChange(e) {
      const val = e && e.detail ? e.detail.value : e;
      if (!val || typeof val !== 'string') return;
      const parts = val.split('-');
      if (parts.length >= 2) {
        const yy = parseInt(parts[0], 10);
        const mm = parseInt(parts[1], 10);
        if (!Number.isNaN(yy)) this.year = yy;
        if (!Number.isNaN(mm)) this.month = mm;
        // 同步预算ID到新周期：清空并重新检查/创建预算
        this.budgetId = '';
        this.checkBudgetExists();
      }
    },
    // 从预算表获取预算周期（年/月）
    fetchBudgetCycle() {
      if (!this.budgetId) return;
      const url = backUrl.baseUrl + backUrl.endpoints.budget_getById + this.budgetId;
      request({
        url,
        method: 'GET'
      }, { ignoreDataField: true })
        .then((res) => {
          let b = null;
          // 兼容多种响应结构
          if (res && res.data && res.data.data && typeof res.data.data === 'object') {
            b = res.data.data;
          } else if (res && res.data && typeof res.data === 'object') {
            b = res.data;
          } else if (res && typeof res === 'object') {
            b = res;
          }
          if (!b) return;
          const y = b.year !== undefined ? b.year : b.budgetYear;
          const m = b.month !== undefined ? b.month : b.budgetMonth;
          if (y !== undefined && y !== null) this.year = parseInt(String(y), 10);
          if (m !== undefined && m !== null) this.month = parseInt(String(m), 10);
        })
        .catch((error) => {
          console.error('加载预算周期失败:', error);
        });
    },
    // 检查预算是否存在
    checkBudgetExists() {
      request({
        url: backUrl.baseUrl + backUrl.endpoints.budget_list,
        method: 'GET',
        data: {
          year: this.year,
          month: this.month
        }
      })
        .then((res) => {
          // 安全处理响应数据，防止JSON解析错误
          if (!res || typeof res.data !== 'object') {
            console.warn('无效的预算数据响应:', res);
            return;
          }
          
          // 兼容不同的响应格式
          if (res.data) {
            // 格式1: 响应直接是预算对象
            if (res.data.id) {
              this.budgetId = res.data.id;
              return;
            }
            // 格式2: 响应有code和data字段
            else if (res.data.code === 0 && res.data.data && res.data.data.id) {
              this.budgetId = res.data.data.id;
              return;
            }
          }
          // 如果没有找到有效的预算，就创建一个
          this.createEmptyBudget();
        })
        .catch((error) => {
          console.error('检查预算是否存在失败:', error);
          // 详细记录错误信息，方便调试
          if (error && error.response && error.response.data) {
            console.error('服务器返回内容:', error.response.data);
          }
          // 出错时仍然尝试创建空预算，但记录错误
        });
    },

    // 创建空预算
    createEmptyBudget() {
      request({
        url: backUrl.baseUrl + backUrl.endpoints.budget_save,
        method: 'POST',
        data: {
          year: this.year,
          month: this.month,
          totalBalance: 0,
          memo: '自动创建的空预算'
        }
      })
        .then((res) => {
          // 安全处理响应数据
          if (res && res.data && res.data.id) {
            this.budgetId = res.data.id;
          } else if (res && res.data && res.data.code === 0 && res.data.data) {
            // 兼容另一种响应格式
            this.budgetId = res.data.data;
          }
        })
        .catch((error) => {
          console.error('创建空预算失败:', error);
          // 详细记录错误信息，方便调试
          if (error && error.response && error.response.data) {
            console.error('服务器返回内容:', error.response.data);
          }
        });
    },

    // 加载预算明细信息
    loadBudgetItemInfo() {
      const endpoint = backUrl.endpoints.budget_item_getById;
      const url = endpoint.endsWith('/') ? endpoint + this.id : endpoint + '/' + this.id;
      
      request({
        url: url,
        method: 'GET'
      }, { ignoreDataField: true })
        .then((res) => {
          let item = null;
          // 格式1: 直接返回的对象包含budgetId
          if (res && typeof res === 'object' && 'budgetId' in res) {
            item = res;
          }
          // 格式2: 标准格式，data.data中包含明细数据
          else if (res && res.data && res.data.data && typeof res.data.data === 'object') {
            item = res.data.data;
          }
          // 格式3: 只有一层data包装
          else if (res && res.data && typeof res.data === 'object' && 'budgetId' in res.data) {
            item = res.data;
          }
          // 格式4: 直接尝试提取data
          else if (res && res.data && typeof res.data === 'object') {
            item = res.data;
          }
          // 格式5: 如果都不匹配，尝试直接使用res
          else if (res && typeof res === 'object') {
            item = res;
          }
          
          if (item) {
            this.budgetId = item.budgetId || '';
            // 编辑场景：始终以预算表的年月为准显示
            if (this.budgetId) {
              this.fetchBudgetCycle();
            }
            
            // 强制设置分类信息，确保数据结构完整
            this.selectedCategory = {
              id: item.classId || item.categoryId || '',
              name: item.categoryName || item.name || item.className || '未命名分类',
              icon: item.icon || ''
            };
            
            // 强制设置金额，确保从分转换为元并转换为字符串
            this.amount = item.totalBalance !== undefined && item.totalBalance !== null 
              ? String(item.totalBalance / 100) 
              : item.amount ? String(item.amount / 100) : '';
            
            this.memo = item.memo || '';

            // 根据返回的ledgerId回填账本选择
            if (item.ledgerId) {
              this.setLedgerById(item.ledgerId);
            }
            
            // 强制刷新UI
            this.$nextTick(() => {
              console.log('UI已强制更新');
            });
          } else {
            console.error('未能从响应中提取有效数据');
            // 显示错误提示
            uni.showToast({
              title: '未能加载预算明细数据',
              icon: 'none'
            });
          }
        })
        .catch((error) => {
          uni.showToast({
            title: '加载预算明细信息失败: ' + (error.message || ''),
            icon: 'none'
          });
          console.error('加载预算明细信息失败:', error);
        });
    },

    // 加载分类信息
    loadCategories() {
      this.loadingCategories = true;

      request({
        url: backUrl.baseUrl + backUrl.endpoints.class_getAll,
        method: 'GET'
      })
        .then((res) => {
          this.loadingCategories = false;
          let categoryData = res;
          
          // 查找id=1的分类或名称为'支出'的分类
      if (categoryData.length > 0) {
        const categoryId1 = categoryData.find(cat => cat.id === 1);
        if (categoryId1) {
          this.topCategories = [categoryId1];
          this.updateCategoryTree();
        } else {
          console.warn('未找到目标分类');
          // 如果没找到目标分类，就使用第一个分类
          this.topCategories = [categoryData[0]];
          this.updateCategoryTree();
        }
      }
        })
        .catch((error) => {
          this.loadingCategories = false;
          console.error('加载分类信息失败:', error);
        });
    },

    // 获取子分类数据的方法 - 仅在必要时使用
    getSubCategories(category) {
      if (!category) {
        return [];
      }
      
      // 确保返回的始终是数组
      return category.childClassList && Array.isArray(category.childClassList) ? [...category.childClassList] : [];
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

    // 处理顶层分类点击
    onTopCategoryClick(e) {
      this.currentTopCategoryIndex = e.current;
      this.updateCategoryTree();
    },

    // 处理中层分类展开/收起
    onMiddleCategoryChange(categoryId) {
      this.currentMiddleCategoryId = this.currentMiddleCategoryId === categoryId ? null : categoryId;
    },

    // 显示分类选择器
    showCategorySelector() {
      this.categoryPopup.open();
      // 弹窗打开后通知子组件重新测量宫格，避免初次宽度为0
      this.$nextTick(() => {
        if (this.$refs.categorySelect && typeof this.$refs.categorySelect.onPopupOpened === 'function') {
          this.$refs.categorySelect.onPopupOpened();
        }
      });
    },

    // 关闭分类选择器
    closeCategoryPopup() {
      this.categoryPopup.close();
    },

    // 选择分类
    selectCategory(category) {
      this.selectedCategory = category;
      // 只有真正选择了叶子分类时才关闭弹窗；切换或清空不关闭
      if (category) {
        this.closeCategoryPopup();
      }
    },

    // 打开账本选择弹窗
    openLedgerPopup() {
      if (this.$refs.ledgerSelectPopup && typeof this.$refs.ledgerSelectPopup.open === 'function') {
        this.$refs.ledgerSelectPopup.open();
      }
    },

    // 账本选择回调
    onLedgerSelected(ledger) {
      this.selectedLedger = ledger;
    },

    // 通过ID设置账本（编辑场景回填）
    async setLedgerById(id) {
      if (this.$refs.ledgerSelectPopup && typeof this.$refs.ledgerSelectPopup.setSelectedById === 'function') {
        await this.$refs.ledgerSelectPopup.setSelectedById(id);
      }
    },

    // 处理金额输入
    handleAmountInput(e) {
      // 限制只能输入数字和小数点
      let value = e.detail.value;
      value = value.replace(/[^\d.]/g, ''); // 清除非数字和小数点
      value = value.replace(/\.(?!\d*$)/g, ''); // 只保留第一个小数点
      value = value.replace(/^\./, '0.'); // 确保小数点前有数字
      value = value.replace(/^0+(\d)/, '$1'); // 移除前导零

      this.amount = value;
    },



    // 保存预算明细
    async saveBudgetItem() {
      // 验证账本
      if (!this.selectedLedger || !this.selectedLedger.id) {
        uni.showToast({ title: '请选择账本', icon: 'none' });
        return;
      }
      // 验证输入
      if (!this.selectedCategory) {
        uni.showToast({
          title: '请选择分类',
          icon: 'none'
        });
        return;
      }

      if (!this.amount) {
        uni.showToast({
          title: '请输入预算金额',
          icon: 'none'
        });
        return;
      }

      // 将元转换为分，保留整数
      const totalBalance = Math.round(parseFloat(this.amount) * 100);
      if (isNaN(totalBalance) || totalBalance < 0) {
        uni.showToast({
          title: '请输入有效的预算金额',
          icon: 'none'
        });
        return;
      }

      // 直接提交预算明细，不再检查预算限额
      this.submitBudgetItem(totalBalance);
    },

    // 提交预算明细
    submitBudgetItem(amount) {
      // 准备请求数据
      const data = {
        budgetId: this.budgetId,
        ledgerId: this.selectedLedger ? this.selectedLedger.id : null,
        classId: this.selectedCategory.id,
        totalBalance: amount,
        memo: this.memo,
        // 添加年月信息，用于后端自动创建预算
        year: this.year,
        month: this.month
      };

      if (this.id) {
        data.id = this.id;
      }

      // 发送请求
      // 注意：后端没有独立的update接口，使用save接口同时处理创建和更新
      request({
        url: backUrl.endpoints.budget_item_save,
        method: 'POST',
        data: data
      })
        .then((res) => {
          uni.showToast({
            title: this.id ? '修改成功' : '添加成功',
            icon: 'success'
          });

          // 返回上一页并刷新预算列表
          setTimeout(() => {
            // 通过getCurrentPages()获取上一页实例，并调用其loadBudgetList方法刷新数据
            const pages = getCurrentPages();
            if (pages.length > 1) {
              const prevPage = pages[pages.length - 2];
              if (prevPage && prevPage.loadBudgetList) {
                prevPage.loadBudgetList();
              }
            }
            // 无论如何都执行返回操作
            uni.navigateBack();
          }, 1500);
        })
        .catch((error) => {
          uni.showToast({
            title: this.id ? '修改失败: ' : '添加失败: ' + (error.message || ''),
            icon: 'none'
          });
          console.error(this.id ? '修改预算明细失败:' : '添加预算明细失败:', error);
        });
    },

    // 返回上一页
    navigateBack() {
      uni.navigateBack();
    }
  }
};
</script>

<style scoped>
.add-budget-item-page {
  padding: 20rpx;
  box-sizing: border-box;
}

.page-header {
  text-align: center;
  padding: 20rpx 0;
  margin-bottom: 30rpx;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
}

.form-container {
  background-color: white;
  border-radius: 10rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.form-value {
  font-size: 30rpx;
  padding: 15rpx;
  background-color: #f5f5f5;
  border-radius: 5rpx;
}

.category-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15rpx;
  background-color: #f5f5f5;
  border-radius: 5rpx;
}

.category-name {
  font-size: 32rpx;
  color: #333;
  max-width: 140rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 10rpx;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: 5rpx;
  padding: 0 15rpx;
}

.currency-symbol {
  font-size: 30rpx;
  color: #666;
  margin-right: 10rpx;
}

.amount-input {
  flex: 1;
  height: 70rpx;
  font-size: 30rpx;
  background-color: transparent;
}

.memo-input {
  width: 100%;
  height: 150rpx;
  font-size: 30rpx;
  padding: 15rpx;
  background-color: #f5f5f5;
  border-radius: 5rpx;
  box-sizing: border-box;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
  padding: 0 20rpx;
}

.save-btn {
  flex: 1;
  background-color: #007aff;
  color: white;
  font-size: 30rpx;
  padding: 20rpx 0;
  border-radius: 24px;
}

.category-popup {
  width: 100%;
  height: 90vh;
  background-color: white;
  border-top-left-radius: 20rpx;
  border-top-right-radius: 20rpx;
  overflow: hidden;
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

.close-btn {
  font-size: 28rpx;
  color: #007aff;
}

.category-content {
  height: calc(100% - 70rpx);
  display: flex;
  flex-direction: column;
}

.loading-container {
  padding: 40rpx 0;
  text-align: center;
}

.category-segmented {
  padding: 10rpx;
  border-bottom: 1px solid #eee;
}

.category-scrollview {
  flex: 1;
  padding: 20rpx;
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120rpx;
  height: 120rpx;
  background-color: #f5f5f5;
  border-radius: 10rpx;
}

.form-value {
  background-color: transparent;
  padding: 15rpx 0;
  font-size: 30rpx;
}

/* 表单中分类选择器的分类名称样式 */
.category-selector .category-name {
  font-size: 30rpx;
  color: #333;
  flex: 1;
  text-align: left;
  max-width: 100%;
  white-space: normal;
  overflow: visible;
  text-overflow: clip;
  word-break: break-word;
}

.no-category {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 28rpx;
}

.confirm-modal {
  background-color: white;
  border-radius: 10rpx;
  padding: 30rpx;
  width: 80%;
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
}

.cancel-btn {
  background-color: #f8f8f8;
  color: #666;
}

.confirm-btn {
  background-color: #007aff;
  color: white;
}

/* 宫格容器样式 */
.grid-container {
  padding: 20rpx;
}

/* 网格容器样式 - 完全按照class.vue标准实现 */
.grid-container {
  padding: 20rpx;
}

/* 网格项样式 - 用于创建分类之间的间隔 */
.grid-item {
  padding: 10rpx;
}

/* 分类卡片样式 - 完全按照class.vue标准实现 */
.category-card {
  height: 160rpx;
  width: 110rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  background-color: #f5f5f5;
  border-radius: 8rpx;
  border: 1rpx solid #e0e0e0;
  position: relative;
}

.category-card:active {
  background-color: #e6e6e6;
}

/* 卡片内容样式 - 完全按照class.vue标准实现 */
.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  width: 100%;
  height: 100%;
}

/* 分类图标样式 - 完全按照class.vue标准实现 */
.category-icon {
  font-size: 48rpx;
  margin-bottom: 10rpx;
  color: #007AFF;
  display: block;
}

/* 分类名称样式 - 完全按照class.vue标准实现 */
.category-name {
  font-size: 24rpx;
  color: #333;
  max-width: 120rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
  line-height: 1.4;
}

/* 分类滚动区域样式 */
.category-scrollview {
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

/* 无子分类提示样式优化 */
.no-subcategory {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 28rpx;
  margin: 20rpx 0;
  grid-column: 1 / -1;
}
</style>
