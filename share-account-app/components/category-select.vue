<template>
  <!-- 分类选择区域 -->
  <view>
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载分类信息..."></uni-load-more>
    </view>

    <!-- 顶部分类分段器（保持原样式与结构） -->
    <view class="segmented-container" v-else-if="topLevelCategories.length > 0">
      <uni-segmented-control 
        :values="topLevelCategories.map(cat => cat.name)" 
        :current="currentTopCategoryIndex" 
        @clickItem="onSegmentedClickItem" 
        :inActiveColor="'#fff'"
        styleType="button"
        :style="{
          fontSize: '28rpx',
          height: '72rpx',
          borderRadius: '36rpx',
          border: 'none',
          width: 'auto',
          minWidth: '60%'
        }"
      ></uni-segmented-control>
      <!-- 左侧：不选分类；右侧：编辑按钮 -->
      <view class="settings-icon-container" style="display:flex; align-items:center; gap: 10rpx;">
        <text class="clear-category-text" @tap="clearCategorySelection" :style="{ color: categoryIconColor }">不选分类</text>
        <custom-icon 
          type="bianji" 
          :size="23"
          :color="categoryIconColor" 
          @click="navigateToClassSettings"
          style="padding: 10rpx;" 
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
              @tap="selectSubCategory(subCategory)"
            >
              <view class="card-content">
                <view class="category-icon">
                  <template v-if="hasFontIcon(subCategory.icon)">
                    <custom-icon :type="normalizeIcon(subCategory.icon)" :size="30" :color="categoryIconColor"></custom-icon>
                  </template>
                  <template v-else>
                    <text class="first-char-icon" :style="{ color: categoryIconColor }">{{ firstChar(subCategory.name) }}</text>
                  </template>
                </view>
                <text class="category-name">{{ subCategory.name }}</text>
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
                  v-for="subSubCategory in currentSubSubCategories" 
                  :key="subSubCategory.idStr"
                  class="sub-sub-category-item"
                  :class="{ 'selected': isCategorySelected(subSubCategory) }"
                  @tap="selectSubSubCategory(subSubCategory)"
                >
                <view class="sub-sub-category-icon">
                  <template v-if="hasFontIcon(subSubCategory.icon)">
                    <custom-icon :type="normalizeIcon(subSubCategory.icon)" :size="30" :color="categoryIconColor"></custom-icon>
                  </template>
                  <template v-else>
                    <text class="first-char-icon" :style="{ color: categoryIconColor }">{{ firstChar(subSubCategory.name) }}</text>
                  </template>
                </view>
                <text class="sub-sub-category-name">{{ subSubCategory.name }}</text>
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
      shouldRefresh: false
    };
  },
  watch: {
    // 监听选中分类变化，用于编辑模式回填
      selectedCategory: {
        handler(newVal) {
          if (newVal && newVal.originalId && this.topLevelCategories.length > 0) {
            // 直接调用，不再使用immediate选项，避免重复执行
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
            if (this.selectedCategory && this.selectedCategory.originalId) {
              // 立即执行，不再延迟，因为已经确保了数据加载完成
              this.$nextTick(() => {
                this.setSelectedByOriginalId(this.selectedCategory.originalId);
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
      if (!originalId || !this.topLevelCategories.length) return;
      
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
  margin-bottom: 20rpx;
}

/* 设置图标容器 + 清空文字 */
.settings-icon-container {
  padding: 10rpx;
}
.clear-category-text {
  font-size: 28rpx;
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
  padding-bottom: 20rpx;
  /* 确保内容少时容器能自动缩小 */
  min-height: 1px;
  margin-bottom: 100px;
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
}
.sub-category-grid-item { padding: 0; }

.category-item-container {
      position: relative;
      width: 100%;
      height: 160rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .category-card {
      height: 160rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      width: 100%;
      min-width: 0;
    }
.category-card:active {
  background-color: #f5f5f5;
  border-radius: 16rpx;
}
.category-card.selected {
  background-color: #f0e8fd;
  border-radius: 16rpx;
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 10rpx;
}
.category-icon { font-size: 48rpx; margin-bottom: 10rpx; }
.first-char-icon { width: 25px; height: 25px; line-height: 25px; text-align: center; display: inline-block; font-weight: bold; }
.category-name { font-size: 24rpx; color: #333; max-width: 120rpx; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    
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

.sub-sub-category-list { padding: 20rpx; margin-bottom: 100px;}
.sub-sub-category-item { display: flex; align-items: center; padding: 20rpx; border-radius: 8rpx; margin-bottom: 10rpx; }
.sub-sub-category-item:active { background-color: #f5f5f5; }
.sub-sub-category-item.selected { background-color: #f0e8fd; border-radius: 8rpx; }
.sub-sub-category-icon { font-size: 40rpx; margin-right: 20rpx; color: var(--theme-primary-color, #faad14); }
.sub-sub-category-name { font-size: 32rpx; color: #333; }
</style>