<template>
  <!-- 分类选择区域 -->
  <view>
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载分类信息..."></uni-load-more>
    </view>

    <!-- 顶部分类分段器（保持原样式与结构） -->
    <view class="segmented-container" v-else-if="topLevelCategories.length > 0">
      <view class="segmented-control">
        <view 
          v-for="(category, index) in topLevelCategories" 
          :key="category.id"
          :class="['segmented-item', { 'active': currentTopCategoryIndex === index }]"
          @tap="onSegmentedClickItem({ currentIndex: index })"
        >
          {{ category.name }}
        </view>
      </view>
      <!-- 左侧：不选分类；右侧：编辑按钮 -->
      <view class="settings-icon-container" style="display:flex; align-items:center; gap: 10rpx;">
        <text class="clear-category-text" @tap="clearCategorySelection" :style="{ color: categoryIconColor }">不选分类</text>
        <custom-icon 
          type="bianji" 
          :size="23"
          color="#ff4d4f" 
          @click="navigateToClassSettings"
          style="padding: 10rpx; border-radius: 50%; background-color: #fff2f0;" 
        ></custom-icon>
      </view>
    </view>

    <!-- 子分类列表 - 使用uni-grid宫格展示（添加滚动功能） -->
    <view class="form-item">
      <view class="sub-category-scrollable-container">
        <uni-grid ref="grid" :column="gridColumns" :show-border="false" :class="['sub-category-grid', 'cols-' + gridColumns]">
          <uni-grid-item 
            v-for="subCategory in currentSubCategories" 
            :key="subCategory.idStr"
            class="sub-category-grid-item"
          >
          <view class="category-item-container">
            <view 
              :class="['category-card', { 'selected': isCategorySelected(subCategory) }]"
              :style="{ animationDelay: (index * 0.05) + 's' }"
              @tap="selectSubCategory(subCategory)"
            >
              <view class="card-content">
                <view class="category-icon" :style="{ backgroundColor: getRandomBgColor(subCategory.id) }">
                  <template v-if="hasFontIcon(subCategory.icon)">
                    <custom-icon :type="normalizeIcon(subCategory.icon)" :size="21" color="#ffffff"></custom-icon>
                  </template>
                  <template v-else>
                    <text class="first-char-icon" style="color: #ffffff">{{ firstChar(subCategory.name) }}</text>
                  </template>
                </view>
                <text class="category-name">{{ subCategory.name }}<text v-if="subCategory.childClassList && subCategory.childClassList.length > 0"></text></text>
              </view>
            </view>
            <!-- 三个点按钮，只有当有子分类时显示 -->
            <view 
              v-if="subCategory.childClassList && subCategory.childClassList.length > 0"
              class="more-options-btn"
              :style="{ backgroundColor: categoryIconColor}"
              @tap.stop="showSubCategoryPopup(subCategory)"
            >
              <text class="more-options-text">...</text>
            </view>
          </view>
          </uni-grid-item>
        </uni-grid>
      </view>
      <!-- 无子分类数据提醒 -->
      <view v-if="!loading && currentSubCategories.length === 0" class="no-sub-category-tip">
        暂无分类数据
      </view>
    </view>

    <!-- 子分类弹出层（保持原样式与结构） -->
    <uni-popup ref="subCategoryPopup" type="center" :mask-click="true" :z-index="10060" @maskClick="closeSubCategoryPopup">
      <view class="popup-container">
        <view class="popup-header">
          <text class="popup-title">{{ currentSubCategory?.name || '选择子分类' }}</text>
          <custom-icon type="guanbi" :size="20" color="#999" @tap="closeSubCategoryPopup"></custom-icon>
        </view>
        <view class="popup-content">
          <!-- 子分类列表，如果没有数据则显示提示 -->
          <div v-if="currentSubSubCategories && currentSubSubCategories.length > 0">
            <view class="sub-sub-category-list">
              <view 
                  v-for="(subSubCategory, index) in currentSubSubCategories" 
                  :key="subSubCategory.idStr"
                  class="sub-sub-category-item"
                  :class="{ 'selected': isCategorySelected(subSubCategory) }"
                  :style="{ animationDelay: (index * 0.05) + 's' }"
                  @tap="selectSubSubCategory(subSubCategory)"
                >
                <text class="sub-sub-category-tag">{{ subSubCategory.name }}</text>
              </view>
            </view>
          </div>
          <!-- 无子分类提示 -->
          <view v-else class="no-sub-category-tip">
            暂无子分类
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import UniSegmentedControl from '@/uni_modules/uni-segmented-control/components/uni-segmented-control/uni-segmented-control.vue';
import UniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue';
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import UniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
import UniGrid from '@/uni_modules/uni-grid/components/uni-grid/uni-grid.vue';
import UniGridItem from '@/uni_modules/uni-grid/components/uni-grid-item/uni-grid-item.vue';

import { mapGetters } from 'vuex';
import { themeIconColor } from '@/components/utils.js';

export default {
  name: 'CategorySelect',
  components: { UniSegmentedControl, UniLoadMore, UniIcons, UniPopup, UniGrid, UniGridItem },
  props: {
    selectedCategory: {
      type: Object,
      default: null
    },
    gridColumns: {
      type: Number,
      default: 5
    },
    // 是否处于编辑模式（用于控制分类回填时不显示弹窗）
    isEditMode: {
      type: Boolean,
      default: false
    }
  },
  emits: ['select', 'child-popup-open', 'child-popup-close'],
  // 新增：图标颜色随主题变化，白色主题回退到默认色
  computed: {
    ...mapGetters(['themePrimaryColor']),
    categoryIconColor() {
      return themeIconColor(this.themePrimaryColor);
    },
  },
  data() {
    return {
      // 分类数据
      categoryTree: [],
      topLevelCategories: [],
      currentTopCategoryIndex: 0,
      currentSubCategories: [],
      currentSubCategory: null,
      currentSubSubCategories: [],
      loading: false,
      // 返回后刷新标记
      shouldRefresh: false,
      // 待回填的分类ID（用于分类数据未加载完成时缓存）
      pendingCategoryId: null
    };
  },
  watch: {
    // 监听选中分类变化，用于编辑模式回填
      selectedCategory: {
        handler(newVal) {
          // 直接调用 setSelectedByOriginalId，它内部有保护逻辑
          // 不要在这里检查 topLevelCategories.length，因为可能在分类加载完成之前就被调用
          if (newVal && newVal.originalId) {
            this.setSelectedByOriginalId(newVal.originalId);
          }
        },
        deep: true
      }
  },
  mounted() {
    this.initLoad();
  },
  methods: {
    // 供父弹窗打开后调用，重新测量并初始化宫格布局
    onPopupOpened() {
      // 使用nextTick替代setTimeout，确保弹窗完成渲染后立即初始化
      this.$nextTick(() => {
        if (this.$refs.grid && typeof this.$refs.grid.init === 'function') {
          try {
            this.$refs.grid.init();
          } catch (e) {
            // 兜底：强制触发一次渲染
            this.$forceUpdate();
          }
        }
      });
    },
    async initLoad() {
      this.loading = true;
      try {
        const res = await this.$request({
          url: this.$backUrlConfig.endpoints.class_getAll,
          method: 'GET'
        });
        if (Array.isArray(res)) {
          this.categoryTree = this.convertIdsToString(res);
          this.topLevelCategories = this.categoryTree.filter(cat => cat.parentId === null);
          if (this.topLevelCategories.length > 0) {
            this.loadCurrentSubCategories();

            // 数据加载完成后，检查是否需要回填分类
            // 优先使用待回填的分类ID（来自父组件的编辑模式）
            const categoryIdToSelect = this.pendingCategoryId || (this.selectedCategory && this.selectedCategory.originalId);
            if (categoryIdToSelect) {
              this.$nextTick(() => {
                this.setSelectedByOriginalId(categoryIdToSelect);
                this.pendingCategoryId = null; // 清空待回填标记
              });
            }
          }
        } else {
          uni.showToast({ title: '获取分类失败', icon: 'none' });
        }
      } catch (e) {
        console.error('分类加载异常:', e);
        uni.showToast({ title: '网络请求异常', icon: 'none' });
      } finally {
        this.loading = false;
      }
    },
    convertIdsToString(categories) {
      return categories.map(category => {
        const converted = {
          ...category,
          idStr: String(category.id),
          originalId: category.id
        };
        if (Array.isArray(category.childClassList)) {
          converted.childClassList = this.convertIdsToString(category.childClassList);
        }
        return converted;
      });
    },
    loadCurrentSubCategories() {
      const currentTop = this.topLevelCategories[this.currentTopCategoryIndex];
      if (!currentTop) return;
      const node = this.findCategoryNode(this.categoryTree, String(currentTop.id));
      this.currentSubCategories = node?.childClassList || [];
    },
    findCategoryNode(categories, idStr) {
      if (!categories || categories.length === 0) return null;
      for (const category of categories) {
        if (String(category.idStr) === String(idStr)) {
          return category;
        }
        const found = this.findCategoryNode(category.childClassList, idStr);
        if (found) return found;
      }
      return null;
    },
    onSegmentedClickItem(e) {
      let index;
      if (e && typeof e.currentIndex === 'number') {
        index = e.currentIndex;
      } else {
        console.error('无法获取索引');
        return;
      }
      if (index < 0 || index >= this.topLevelCategories.length) return;
      if (this.currentTopCategoryIndex !== index) {
        this.currentTopCategoryIndex = index;
        this.loadCurrentSubCategories();
        // 切换分类时清除已选的子分类
        this.$emit('select', null);
      }
    },
    // 弹窗相关
    showSubCategoryPopup(subCategory) {
      this.currentSubCategory = subCategory;
      this.currentSubSubCategories = subCategory.childClassList || [];
      this.$refs.subCategoryPopup.open();
      this.$emit('child-popup-open');
    },
    closeSubCategoryPopup() {
      this.$emit('child-popup-close');
      this.$refs.subCategoryPopup.close();
    },
    // 直接选择子分类
    selectSubCategory(subCategory) {
      this.$emit('select', subCategory);
    },
    
    // 选择子分类（弹出层内的）
    selectSubSubCategory(subSubCategory) {
      this.$emit('select', subSubCategory);
      this.closeSubCategoryPopup();
    },
    
    // 统一的选中判断逻辑
    isCategorySelected(category) {
      const selected = this.selectedCategory;
      if (!selected) return false;
      // 直接匹配当前分类ID
      if (category.idStr === selected.idStr) {
        return true;
      }
      // 如果有子分类，检查子分类中是否有选中项
      if (category.childClassList) {
        return category.childClassList.some(child => child.idStr === selected.idStr);
      }
      return false;
    },
    
    isSubSubCategorySelected(subSubCategory) {
      const selected = this.selectedCategory;
      if (!selected) return false;
      return subSubCategory.idStr === selected.idStr;
    },
    // 编辑分类
    navigateToClassSettings() {
      this.shouldRefresh = true;
      uni.navigateTo({ url: '/pages/class/class' });
    },
    // 返回后刷新
    refreshIfNeeded() {
      if (this.shouldRefresh) {
        this.shouldRefresh = false;
        this.initLoad();
      }
    },
    // 提供给父页面的关闭方法（保留关闭弹窗能力）
    closePopup() {
      if (this.$refs.subCategoryPopup) {
        this.$emit('child-popup-close');
        this.$refs.subCategoryPopup.close();
      }
    },
    // 清空选中分类
    clearCategorySelection() {
      this.$emit('select', null);
    },
    // 此方法已被后面的实现替代，保留注释以避免重复定义问题
    // setSelectedByOriginalId方法移至下方

    // 辅助：是否有字体图标（非空字符串）
    hasFontIcon(val) {
      return typeof val === 'string' && val.trim().length > 0;
    },
    // 辅助：去掉可能的前缀 icon-
    normalizeIcon(val) {
      if (!val || typeof val !== 'string') return '';
      const name = val.trim();
      return name.startsWith('icon-') ? name.slice(5) : name;
    },
    // 辅助：取分类名首字（空值回退为 '类'）
    firstChar(name) {
      if (!name || typeof name !== 'string') return '账';
      const s = name.trim();
      return s.length > 0 ? s[0] : '账';
    },
    
    // 获取分类颜色，优先使用icon的颜色，如果没有则使用分类自身颜色或默认主题色
    getCategoryColor(category) {
      // 优先使用subCategory.icon的颜色（根据用户需求）
      if (category.icon && typeof category.icon === 'string' && category.icon.includes('#')) {
        // 假设icon字段可能包含颜色值
        const colorMatch = category.icon.match(/#[0-9a-fA-F]{6}|#[0-9a-fA-F]{3}/);
        if (colorMatch) {
          return colorMatch[0];
        }
      }
      // 其次使用分类自身的颜色
      if (category.color && category.color.trim()) {
        return category.color.trim();
      }
      // 如果没有颜色，回退到主题色
      return this.categoryIconColor;
    },
    
    // 根据原始ID设置选中的分类（用于编辑模式回填）
    setSelectedByOriginalId(originalId) {
      if (!originalId) return;

      // 如果分类数据还没加载完成，缓存待回填的分类ID
      if (!this.topLevelCategories || this.topLevelCategories.length === 0) {
        this.pendingCategoryId = originalId;
        return;
      }

      // 创建一个映射缓存，提高查找效率
      let categoryMap = this.createCategoryMap(this.categoryTree);
      let targetCategory = categoryMap[originalId] || categoryMap[String(originalId)];

      if (!targetCategory) {
        // 如果映射缓存没找到，使用递归查找作为兜底
        targetCategory = this.findCategoryById(originalId, this.categoryTree);
        if (!targetCategory) return;
      }

      // 快速定位到对应的顶级分类并切换
      this.switchToTopLevel(targetCategory);

      // 直接选中目标分类，避免多次查找
      this.$emit('select', targetCategory);
    },
    
    // 创建分类ID映射缓存
    createCategoryMap(categories, map = {}) {
      categories.forEach(category => {
        map[category.originalId] = category;
        map[category.idStr] = category;
        if (category.childClassList && category.childClassList.length > 0) {
          this.createCategoryMap(category.childClassList, map);
        }
      });
      return map;
    },
    
    // 递归查找分类
    findCategoryById(originalId, categories) {
      for (const category of categories) {
        if (category.originalId === originalId || category.idStr === originalId) {
          return category;
        }
        if (category.childClassList && category.childClassList.length > 0) {
          const found = this.findCategoryById(originalId, category.childClassList);
          if (found) return found;
        }
      }
      return null;
    },
    
    // 快速切换到对应的顶级分类
    switchToTopLevel(category) {
      // 查找顶级分类的索引
      let currentCategory = category;
      let parentCategory = this.findParentCategory(currentCategory);
      
      // 向上查找直到找到顶级分类
      while (parentCategory && parentCategory.parentId !== null) {
        currentCategory = parentCategory;
        parentCategory = this.findParentCategory(currentCategory);
      }
      
      // 如果已经是顶级分类或找到了顶级分类
      const topLevelCategory = parentCategory || currentCategory;
      const topLevelIndex = this.topLevelCategories.findIndex(cat => 
        cat.originalId === topLevelCategory.originalId || cat.idStr === topLevelCategory.idStr
      );
      
      // 如果索引有效且不是当前显示的分类，则切换
      if (topLevelIndex >= 0 && topLevelIndex !== this.currentTopCategoryIndex) {
        this.currentTopCategoryIndex = topLevelIndex;
        this.loadCurrentSubCategories();
      }
    },
    
    // 查找父分类
    findParentCategory(category) {
      if (!category || category.parentId === null) return null;
      return this.findCategoryById(category.parentId, this.categoryTree);
    },
    
    // 检查是否是顶级分类
    isTopLevelCategory(category) {
      return this.topLevelCategories.some(topCat => 
        topCat.originalId === category.originalId || topCat.idStr === category.idStr
      );
    },
    
    // 生成随机鲜艳背景色
    getRandomBgColor(id) {
      // 使用id作为种子，确保相同id的图标背景色一致
      const colors = [
        '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4',
        '#FFEAA7', '#DDA0DD', '#98D8C8', '#F7DC6F',
        '#BB8FCE', '#85C1E9', '#F8C471', '#82E0AA'
      ];
      const index = Math.abs(parseInt(id)) % colors.length;
      return colors[index];
    },
    
    // 找到并切换到包含指定分类的顶级分类
    findAndSwitchTopCategory(category) {
      console.log('开始查找并切换到顶级分类，目标分类:', category.name);
      
      // 检查分类是否在某个分类树中
      const isCategoryInTree = (parentCategory, targetCategory) => {
        // 检查是否是目标分类本身
        if (parentCategory.originalId === targetCategory.originalId || 
            parentCategory.idStr === targetCategory.idStr) {
          return true;
        }
        
        // 检查是否在子分类中
        if (parentCategory.childClassList) {
          for (const child of parentCategory.childClassList) {
            if (isCategoryInTree(child, targetCategory)) {
              return true;
            }
          }
        }
        return false;
      };
      
      // 查找该分类所在的顶级分类
      const findTopLevel = (categories, targetCategory) => {
        for (let i = 0; i < categories.length; i++) {
          const topCat = categories[i];
          console.log('检查顶级分类:', topCat.name);
          if (isCategoryInTree(topCat, targetCategory)) {
            console.log('找到包含目标分类的顶级分类:', topCat.name, '索引:', i);
            return i;
          }
        }
        console.log('未找到包含目标分类的顶级分类');
        return -1;
      };
      
      // 获取顶级分类索引并切换
      const index = findTopLevel(this.topLevelCategories, category);
      if (index !== -1) {
        console.log('准备切换到顶级分类索引:', index, '当前索引:', this.currentTopCategoryIndex);
        if (index !== this.currentTopCategoryIndex) {
          this.currentTopCategoryIndex = index;
          // 立即加载子分类
          this.loadCurrentSubCategories();
          console.log('已切换顶级分类并加载对应子分类');
        } else {
          console.log('已经在正确的顶级分类中');
        }
      }
    }
  }
};
</script>

<style scoped>
/* 分段器容器样式 */
.segmented-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0rpx;
  background-color: #fff;
}

/* 分段器样式 */
.segmented-control {
  display: flex;
  background-color: #f5f5f5;
  border-radius: 36rpx;
  padding: 4rpx;
  align-items: center;
}

/* 分段器项样式 */
.segmented-item {
  padding: 12rpx 32rpx;
  border-radius: 32rpx;
  font-size: 28rpx;
  color: #333;
  background-color: transparent;
  transition: all 0.3s ease;
  cursor: pointer;
  flex: 1;
  text-align: center;
}

/* 分段器项选中状态 */
.segmented-item.active {
  background-color: #d744e0;
  color: #ffffff;
  box-shadow: 0 4rpx 16rpx rgba(147, 51, 234, 0.3);
}

/* 分段器项悬浮效果 */
.segmented-item:hover {
  opacity: 0.8;
}

.segmented-item.active:hover {
  box-shadow: 0 6rpx 20rpx rgba(147, 51, 234, 0.4);
  opacity: 1;
}

/* 冒泡动画效果 */
@keyframes bubble {
  0% {
    transform: scale(0.9);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 图标旋转动画 */
@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.sub-sub-category-item {
  animation: bubble 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
  opacity: 0;
}
/* 设置图标容器 + 清空文字 */
.settings-icon-container {
  padding: 10rpx;
}
.clear-category-text {
  font-size: 28rpx;
  color: #666666;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  transition: all 0.3s ease;
}

.clear-category-text:hover {
  background-color: #f5f5f5;
  color: #333333;
}

/* 表单样式 */
.form-item {
  margin-bottom: 40rpx;
}

/* 子分类滚动容器 */
.sub-category-scrollable-container {
  max-height: 80vh;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  /* 确保内容少时容器能自动缩小 */
  min-height: 1px;
}

/* 隐藏滚动条但保留滚动功能 */
.sub-category-scrollable-container::-webkit-scrollbar {
  display: none;
}

/* 子分类宫格样式 - 响应式布局 */
.sub-category-grid {
  border-radius: 12rpx;
  overflow: hidden;
  padding: 20rpx;
  width: 100%;
  box-sizing: border-box;
  /* 确保grid内容完整显示 */
  min-height: 100%;
  gap: 24rpx;
}

/* 调整网格项的间距 */
.sub-category-grid >>> .uni-grid-item {
  margin-bottom: 24rpx;
}
.sub-category-grid-item { padding: 0; }

.category-item-container {
      position: relative;
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .category-card {
      height: 110rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s ease;
      width: 100%;
      min-width: 0;
      border-radius: 16rpx;
      padding: 16rpx;
      animation: bubble 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
      opacity: 0;
    }

.category-card:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.category-card:active {
  background-color: #f5f5f5;
  transform: translateY(0);
}

.category-card.selected {
  background-color: #f0e8fd;
  border: 1rpx solid #9333ea;
  box-shadow: 0 4rpx 16rpx rgba(147, 51, 234, 0.2);
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 10rpx;
}
.category-icon { 
  width: 60rpx; 
  height: 60rpx; 
  border-radius: 50%; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  margin-bottom: 10rpx; 
  border: 2rpx solid rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.category-icon:hover {
  transform: scale(1.1);
  border-color: rgba(0, 0, 0, 0.3);
}

.category-icon:hover custom-icon,
.category-icon:hover .first-char-icon {
  animation: rotate 1s ease-in-out forwards;
}

.first-char-icon { 
  width: 60rpx; 
  height: 60rpx; 
  line-height: 60rpx; 
  text-align: center; 
  display: inline-block; 
  font-weight: bold; 
  font-size: 36rpx;
}
.category-name { font-size: 22rpx; color: #333; max-width: 140rpx; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    
/* 更多选项按钮样式 */
.more-options-btn {
  position: absolute;
  bottom: 10rpx;
  right: 10rpx;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  /* 背景色通过动态样式绑定设置 */
}

.more-options-text {
  color: white;
  font-size: 24rpx;
  line-height: 1;
}

/* 取消 uni-grid 边框 */
.sub-category-grid >>> .uni-grid { border: none; }
.sub-category-grid >>> .uni-grid::before,
.sub-category-grid >>> .uni-grid::after,
.sub-category-grid >>> .uni-grid-item::before,
.sub-category-grid >>> .uni-grid-item::after { border: none; }
.sub-category-grid >>> .uni-grid-item { box-sizing: border-box; }
.sub-category-grid.cols-3 >>> .uni-grid-item { width: 33.3333% !important; }
.sub-category-grid.cols-4 >>> .uni-grid-item { width: 25% !important; }
.sub-category-grid.cols-5 >>> .uni-grid-item { width: 20% !important; }
.sub-category-grid.cols-6 >>> .uni-grid-item { width: 16.6667% !important; }
.sub-category-grid.cols-7 >>> .uni-grid-item { width: 14.2857% !important; }

/* 无数据提示 */
.no-sub-category-tip {
  padding: 40rpx 20rpx;
  text-align: center;
  font-size: 24rpx;
  color: #999;
}

/* 弹出层样式（保持与页面一致） */
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
.popup-title { font-size: 34rpx; font-weight: bold; color: #333; }
.popup-content { max-height: 500rpx; overflow-y: auto; padding: 30rpx 40rpx; }

.sub-sub-category-list { 
  padding: 20rpx; 
  margin-bottom: 100px; 
  display: flex; 
  flex-wrap: wrap; 
  gap: 24rpx;
}

.sub-sub-category-item { 
  padding: 0; 
  border-radius: 0; 
  margin-bottom: 16rpx;
  width: calc(25% - 12rpx);
  box-sizing: border-box;
}

/* 确保每行显示4个元素 */
.sub-sub-category-tag {
  padding: 16rpx 12rpx;
  border-radius: 32rpx;
  font-size: 28rpx;
  color: #333;
  background-color: #f5f5f5;
  transition: all 0.3s ease;
  border: 2rpx solid transparent;
  display: block;
  text-align: center;
  width: 100%;
  box-sizing: border-box;
}

.sub-sub-category-tag:hover {
  background-color: #e8e8e8;
  transform: translateY(-2rpx);
}

.sub-sub-category-item.selected .sub-sub-category-tag {
  background-color: #9333ea;
  color: #ffffff;
  border-color: #9333ea;
  box-shadow: 0 4rpx 16rpx rgba(147, 51, 234, 0.3);
}
</style>