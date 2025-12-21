<template>
  <view class="category-page">
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载分类信息..."></uni-load-more>
    </view>
    
    <!-- 内容区域 -->
    <view class="content-container" v-else>
      <!-- 分段器 - 展示顶层分类 -->
      <view class="segmented-container" v-if="topLevelCategories.length > 0">
        <uni-segmented-control 
          :values="topLevelCategories.map(cat => cat.name)" 
          :current="currentTopCategoryIndex" 
          @clickItem="onSegmentedClickItem" 
          activeColor="#007AFF"
        ></uni-segmented-control>
      </view>
      
      <!-- 手风琴加载状态 -->
      <view class="loading-container" v-if="subCategoriesLoading">
        <uni-load-more status="loading" contenttext="正在加载子分类信息..."></uni-load-more>
      </view>
      
      <!-- 手风琴 - 展示中层分类 -->
      <view class="accordion-container" v-else-if="currentTopCategory && currentCategoryTree.length > 0">
        <uni-collapse 
          accordion 
          v-model="expandedAccordionId"
          @change="onCollapseChange"
          :key="collapseKey"
        >
          <uni-collapse-item 
            v-for="(middleCategory, index) in currentCategoryTree" 
            :key="middleCategory.idStr"
            :name="middleCategory.idStr"
          >
            <template v-slot:title>
              <view class="collapse-title">
                <view class="collapse-title-icon">
                  <template v-if="hasFontIcon(middleCategory.icon)">
                    <custom-icon :type="normalizeIcon(middleCategory.icon)" :size="28" :color="classIconColor"></custom-icon>
                  </template>
                  <template v-else>
                    <text class="first-char-icon" :style="{ color: classIconColor }">{{ firstChar(middleCategory.name) }}</text>
                  </template>
                </view>
                <text class="collapse-title-text">{{ middleCategory.name }}</text>
              </view>
            </template>
            <!-- 方格 - 展示底层分类 -->
            <view class="grid-container">
              <uni-grid :column="4" :border="false">
                <!-- 底层分类 -->
                <view v-for="(bottomCategory, gIndex) in middleCategory.childClassList || []" :key="bottomCategory.idStr" class="grid-item">
                  <view class="category-card" @tap="handleCategoryClick(bottomCategory)">
                    <view class="card-content">
                      <view class="category-icon">
                        <template v-if="hasFontIcon(bottomCategory.icon)">
                          <custom-icon :type="normalizeIcon(bottomCategory.icon)" :size="36" :color="classIconColor"></custom-icon>
                        </template>
                        <template v-else>
                          <text class="first-char-icon" :style="{ color: classIconColor }">{{ firstChar(bottomCategory.name) }}</text>
                        </template>
                      </view>
                      <text class="category-name">{{ bottomCategory.name }}</text>
                    </view>
                  </view>
                </view>
                <!-- 添加底层分类按钮 -->
                <view class="grid-item" @tap="showAddCategoryModal(middleCategory.idStr)">
                  <view class="add-category-card">
                    <view class="card-content">
                      <text class="add-icon iconfont icon-plus-circle">+</text>
                      <text class="add-text">添加分类</text>
                    </view>
                  </view>
                </view>
              </uni-grid>
            </view>
          </uni-collapse-item>
          
          <!-- 添加中层分类按钮已移除，改为在下方单独显示 -->
        </uni-collapse>
      </view>
      
      <!-- 没有中层分类提示 -->
      <view class="no-data-container" v-else-if="currentTopCategory">
        <text class="no-data-text">暂无子分类</text>
        <view class="add-category-btn" @click="showAddCategoryModal(currentTopCategory.idStr)">
          <text class="iconfont icon-plus-circle">+</text>
          <text class="add-text">添加子分类</text>
        </view>
      </view>
      
      <!-- 没有顶层分类提示 -->
      <view class="no-data-container" v-else>
        <text class="no-data-text">暂无分类数据</text>
      </view>
      
      <!-- 添加中层分类按钮 -->
      <view class="view-disabled-btn-container">
        <text 
          class="view-disabled-btn" 
          @click="handleAddCategoryClick"
        >
          添加分类
        </text>
      </view>
      
      <!-- 查看已停用分类按钮 -->
      <view class="view-disabled-btn-container">
        <text 
          class="view-disabled-btn" 
          @click="toggleShowDisabled"
        >
          {{ showDisabled ? '收起已停用分类' : '查看已停用分类' }}
        </text>
      </view>
      
      <!-- 已停用分类 - 手风琴展示（核心修改部分） -->
      <view class="disabled-accordion-container" v-if="showDisabled">
        <!-- 已停用分类手风琴 -->
        <uni-collapse 
          v-if="disabledCategories && disabledCategories.length > 0"
          accordion
          v-model="expandedDisabledAccordionId"
          class="disabled-collapse"
        >
          <!-- 按顶层分类分组展示已停用分类 -->
          <uni-collapse-item 
            v-for="(group, groupIndex) in disabledCategoryGroups" 
            :key="groupIndex"
            :title="`${group.name} (${group.items.length})`"
            :name="groupIndex.toString()"
          >
            <view class="disabled-grid-container">
              <uni-grid :column="4" :border="false">
                <view v-for="(category, index) in group.items" :key="category.idStr" class="grid-item">
                  <view class="disabled-card" @tap="handleCategoryClick(category)">
                    <view class="card-content">
                      <view class="disabled-icon">
                        <template v-if="hasFontIcon(category.icon)">
                          <custom-icon :type="normalizeIcon(category.icon)" :size="32" :color="classIconColor"></custom-icon>
                        </template>
                        <template v-else>
                          <text class="first-char-icon" :style="{ color: classIconColor }">{{ firstChar(category.name) }}</text>
                        </template>
                      </view>
                      <text class="disabled-name">{{ category.name }}</text>
                      <text class="disabled-badge">已停用</text>
                    </view>
                  </view>
                </view>
              </uni-grid>
            </view>
          </uni-collapse-item>
        </uni-collapse>
        
        <!-- 没有已停用分类提示 -->
        <view class="no-data-container" v-else-if="!disabledCategories || disabledCategories.length === 0">
          <text class="no-data-text">暂无已停用分类</text>
        </view>
      </view>
    </view>
    
    <!-- 添加/修改分类弹窗 -->
    <uni-popup ref="categoryModal" type="center" @ready="onCategoryModalReady">
      <view class="modal-container">
        <view class="modal-title">{{ isEdit ? '修改分类' : '添加分类' }}</view>
        <view class="modal-content">
          <view class="modal-item">
            <text class="item-label">分类名称：</text>
            <input 
              type="text" 
              v-model="newCategoryName" 
              placeholder="请输入分类名称" 
              class="item-input"
              maxlength="50"
            />
          </view>
        </view>
        <view class="modal-buttons">
          <button class="cancel-btn" @click="closeCategoryModal">取消</button>
          <button class="confirm-btn" @click="saveCategory" :disabled="saveLoading">
            {{ saveLoading ? '保存中...' : '保存' }}
            <uni-load-more v-if="saveLoading" status="loading" size="mini"></uni-load-more>
          </button>
        </view>
      </view>
    </uni-popup>
    
    <!-- 分类操作弹窗 -->
    <uni-popup ref="actionModal" type="bottom" @ready="onActionModalReady">
      <view class="action-modal-container">
        <view class="action-item" @click="updateCategoryStatus">
          <text class="action-text">{{ currentCategory?.isDisabled ? '启用' : '停用' }}</text>
        </view>
        <view class="action-item" @click="showEditCategoryModal">
          <text class="action-text">修改</text>
        </view>
        <view class="action-item cancel" @click="closeActionModal">
          <text class="action-text">取消</text>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { mapGetters } from 'vuex';
import { themeIconColor } from '@/components/utils.js';

export default {
  data() {
    return {
      loading: true,
      categoryTree: [],
      topLevelCategories: [],
      currentTopCategoryIndex: 0,
      subCategoriesLoading: false,
      showDisabled: false,
      disabledCategories: [],
      disabledCategoryGroups: [], // 用于分组已停用分类
      expandedDisabledAccordionId: '', // 已停用分类手风琴的展开状态
      currentCategory: null,
      newCategoryName: '',
      parentId: '',
      isEdit: false,
      expandedAccordionId: '',
      shouldExpandAccordion: false,
      targetAccordionId: '',
      collapseKey: 0,
      categoryModal: null,
      actionModal: null,
      saveLoading: false // 保存按钮loading状态
    }
  },
  computed: {
    currentTopCategory() {
      return this.topLevelCategories[this.currentTopCategoryIndex] || null;
    },
    
    currentCategoryTree() {
      if (!this.currentTopCategory) return [];
      
      const categoryNode = this.findCategoryNode(this.categoryTree, this.currentTopCategory.idStr);
      return categoryNode?.childClassList || [];
    },
    
    // 图标颜色随主题变化，白色主题回退到默认色
    ...mapGetters(['themePrimaryColor']),
    classIconColor() {
      return themeIconColor(this.themePrimaryColor);
    }
  },
  methods: {
    // 初始化页面数据
    async initPage() {
      this.loading = true;
      try {
        const [activeRes, disabledRes] = await Promise.all([
          this.$request({
            url: this.$backUrlConfig.endpoints.class_getAll,
            method: 'GET'
          }),
          this.$request({
            url: this.$backUrlConfig.endpoints.class_getByStatus + 1,
            method: 'GET'
          })
        ]);
        
        if (Array.isArray(activeRes)) {
          this.categoryTree = this.convertIdsToString(activeRes);
          this.topLevelCategories = this.categoryTree.filter(cat => cat.parentId === null);
          
          if (this.topLevelCategories.length > 0 && this.currentTopCategoryIndex >= this.topLevelCategories.length) {
            this.currentTopCategoryIndex = 0;
          }
        } else {
          uni.showToast({ title: '获取分类失败', icon: 'none' });
        }
        
        if (Array.isArray(disabledRes)) {
          this.disabledCategories = this.convertIdsToString(disabledRes).map(cat => ({
            ...cat,
            isDisabled: true
          }));
          
          // 处理已停用分类的分组
          this.groupDisabledCategories();
        } else {
          this.disabledCategories = [];
          this.disabledCategoryGroups = [];
        }
      } catch (error) {
        console.error('初始化页面异常:', error);
        uni.showToast({ title: '网络请求异常', icon: 'none' });
      } finally {
        this.loading = false;
        
        if (this.shouldExpandAccordion && this.targetAccordionId) {
          this.$nextTick(() => {
            this.expandAccordionById(this.targetAccordionId);
            this.shouldExpandAccordion = false;
            this.targetAccordionId = '';
          });
        }
      }
    },
    
    // 对已停用分类进行分组（按顶层分类）
    groupDisabledCategories() {
      // 创建一个映射表，用于快速查找父分类
      const categoryMap = {};
      this.getAllCategories().forEach(cat => {
        categoryMap[cat.idStr] = cat;
      });
      
      // 按顶层分类分组
      const groups = {};
      
      this.disabledCategories.forEach(cat => {
        // 找到顶级父分类
        let topParent = cat;
        while (topParent.parentId && categoryMap[topParent.parentId]) {
          topParent = categoryMap[topParent.parentId];
        }
        
        const topParentId = topParent.idStr;
        
        // 如果该顶级分类不在分组中，则创建新分组
        if (!groups[topParentId]) {
          groups[topParentId] = {
            id: topParentId,
            name: topParent.name || '未分类',
            items: []
          };
        }
        
        // 将当前分类添加到对应的分组
        groups[topParentId].items.push(cat);
      });
      
      // 转换为数组
      this.disabledCategoryGroups = Object.values(groups);
    },
    
    // 获取所有分类（包括所有层级）
    getAllCategories() {
      const allCats = [];
      
      const traverse = (categories) => {
        if (!categories) return;
        categories.forEach(cat => {
          allCats.push(cat);
          traverse(cat.childClassList);
        });
      };
      
      traverse(this.categoryTree);
      return allCats;
    },
    
    // 转换所有ID为字符串
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
    
    // 查找分类节点
    findCategoryNode(categories, idStr) {
      if (!categories || categories.length === 0) return null;
      
      for (const category of categories) {
        if (category.idStr === idStr) {
          return category;
        }
        
        const found = this.findCategoryNode(category.childClassList, idStr);
        if (found) return found;
      }
      
      return null;
    },
    
    // 分段器点击事件
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
        this.subCategoriesLoading = true;
        this.collapseKey++;
        this.expandedAccordionId = '';
        this.subCategoriesLoading = false;
      }
    },
    
    // 手风琴状态变化事件
    onCollapseChange(value) {
      const stringValue = String(value);
      this.expandedAccordionId = stringValue;
    },
    
    // 显示添加分类弹窗
    showAddCategoryModal(parentIdStr) {
      if (!this.categoryModal) {
        this.categoryModal = this.$refs.categoryModal;
        if (!this.categoryModal) {
          uni.showToast({ title: '弹窗加载失败，请重试', icon: 'none' });
          return;
        }
      }
      
      this.isEdit = false;
      this.newCategoryName = '';
      this.parentId = parentIdStr;
      this.categoryModal.open();
    },
    
    // 显示编辑分类弹窗
    showEditCategoryModal() {
      if (!this.currentCategory) return;
      
      if (!this.categoryModal) {
        this.categoryModal = this.$refs.categoryModal;
        if (!this.categoryModal) {
          uni.showToast({ title: '弹窗加载失败，请重试', icon: 'none' });
          return;
        }
      }
      
      this.isEdit = true;
      this.newCategoryName = this.currentCategory.name;
      this.parentId = this.currentCategory.parentId ? String(this.currentCategory.parentId) : '';
      this.categoryModal.open();
      this.closeActionModal();
    },
    
    // 关闭分类弹窗
    closeCategoryModal() {
      if (this.categoryModal) {
        this.categoryModal.close();
      }
    },
    
    // 关闭操作弹窗
    closeActionModal() {
      if (this.actionModal) {
        this.actionModal.close();
      }
    },
    
    // 保存分类
    async saveCategory() {
      if (!this.newCategoryName.trim()) {
        uni.showToast({ title: '请输入分类名称', icon: 'none' });
        return;
      }
      
      // 设置loading状态
      this.saveLoading = true;
      uni.showLoading({ title: '保存中...' });
      
      try {
        const res = await this.$request({
          url: this.$backUrlConfig.endpoints.class_save,
          method: 'POST',
          data: {
            id: this.isEdit ? this.currentCategory.originalId : '',
            name: this.newCategoryName,
            parentId: this.parentId || ''
          }
        });
        
        if (res) {
          uni.showToast({ title: this.isEdit ? '修改成功' : '添加成功', icon: 'success' });
          this.closeCategoryModal();
          
          if (!this.isEdit && this.parentId) {
            this.shouldExpandAccordion = true;
            this.targetAccordionId = this.parentId;
          }
          
          await this.initPage();
        }
      } catch (error) {
        console.error('保存失败:', error);
        uni.showToast({ title: '保存失败，请重试', icon: 'none' });
      } finally {
        // 重置loading状态
        this.saveLoading = false;
        uni.hideLoading();
      }
    },
    
    // 展开指定ID的手风琴项
    expandAccordionById(id) {
      const targetId = String(id);
      
      const exists = this.currentCategoryTree.some(cat => cat.idStr === targetId);
      if (!exists) {
        console.warn('ID不存在:', targetId);
        return;
      }
      
      this.collapseKey++;
      
      this.$nextTick(() => {
        setTimeout(() => {
          this.$nextTick(() => {
            this.expandedAccordionId = targetId;
          });
        }, 50);
      });
    },
    
    // 切换显示/隐藏已停用分类
    toggleShowDisabled() {
      this.showDisabled = !this.showDisabled;
    },
    
    // 处理添加中层分类点击事件
    handleAddCategoryClick() {
      if (this.currentTopCategory && this.currentTopCategory.idStr) {
        this.showAddCategoryModal(this.currentTopCategory.idStr);
      } else {
        uni.showToast({ title: '无法获取当前分类信息', icon: 'none' });
      }
    },
    
    // 处理分类点击事件
    handleCategoryClick(category) {
      if (!category.userId) return;
      
      if (!this.actionModal) {
        this.actionModal = this.$refs.actionModal;
        if (!this.actionModal) {
          uni.showToast({ title: '弹窗加载失败，请重试', icon: 'none' });
          return;
        }
      }
      
      this.currentCategory = category;
      this.actionModal.open();
    },
    
    // 更新分类状态
    async updateCategoryStatus() {
      if (!this.currentCategory) return;
      
      try {
        const res = await this.$request({
          url: this.$backUrlConfig.endpoints.class_updateStatus + this.currentCategory.originalId,
          method: 'GET'
        });
        
        if (res) {
          uni.showToast({ title: `${this.currentCategory.isDisabled ? '启用' : '停用'}成功`, icon: 'success' });
          this.closeActionModal();
          await this.initPage();
        }
      } catch (error) {
        console.error('状态更新失败:', error);
      }
    },
    
    // 分类弹窗准备就绪
    onCategoryModalReady() {
      this.categoryModal = this.$refs.categoryModal;
    },
    
    // 操作弹窗准备就绪
    onActionModalReady() {
      this.actionModal = this.$refs.actionModal;
    },
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
    // 辅助：取分类名首字（空值回退为 '账'）
    firstChar(name) {
      if (!name || typeof name !== 'string') return '账';
      const s = name.trim();
      return s.length > 0 ? s[0] : '账';
    }
  },
  onLoad() {
    this.initPage();
  },
  onReady() {
    this.$nextTick(() => {
      this.categoryModal = this.$refs.categoryModal;
      this.actionModal = this.$refs.actionModal;
    });
  }
}
</script>

<style lang="scss">
/* 原有样式保持不变 */
.category-page {
  background-color: #f5f5f5;
  min-height: 100vh;
  padding-top: 20rpx;
}

.loading-container {
  padding-top: 300rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.content-container {
  padding-bottom: 120rpx;
}

.segmented-container {
  padding: 20rpx 30rpx;
  background-color: #fff;
  margin: 0 20rpx;
  border-radius: 12rpx;
}

.accordion-container {
  margin: 20rpx;
  background-color: #fff;
  border-radius: 12rpx;
  overflow: hidden;
}

.grid-container {
  padding: 20rpx;
}

.grid-item {
  padding: 10rpx;
}

.category-card {
  height: 160rpx;
  width: 110rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  position: relative;
}

.add-category-card {
  background-color: #f8f8f8;
  border: 2rpx dashed #ddd;
  border-radius: 16rpx;
  height: 160rpx;
  display: flex;
  align-items: center;
  justify-content: center;
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
  font-size: 48rpx;
  margin-bottom: 10rpx;
  color: #007AFF;
}

.category-name {
  font-size: 24rpx;
  color: #333;
  max-width: 120rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.no-data-container {
  padding: 60rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #fff;
  margin: 20rpx;
  border-radius: 12rpx;
}

.no-data-text {
  font-size: 28rpx;
  color: #86909c;
  margin-bottom: 20rpx;
}

.add-category-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background-color: #f0f7ff;
  border-radius: 8rpx;
  color: #007AFF;
  font-size: 26rpx;
}

.modal-container {
  width: 600rpx;
  background-color: #fff;
  border-radius: 20rpx;
  overflow: hidden;
}

.modal-title {
  padding: 30rpx 0;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
  border-bottom: 1rpx solid #eee;
}

.modal-content {
  padding: 30rpx;
}

.modal-item {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.item-label {
  width: 180rpx;
  font-size: 28rpx;
  color: #666;
}

.item-input {
  flex: 1;
  height: 60rpx;
  font-size: 28rpx;
  padding: 0 20rpx;
  border: 1rpx solid #eee;
  border-radius: 10rpx;
}

.modal-buttons {
  display: flex;
  border-top: 1rpx solid #eee;
}

.cancel-btn, .confirm-btn {
  flex: 1;
  height: 90rpx;
  line-height: 90rpx;
  text-align: center;
  font-size: 30rpx;
  background: none;
  border: none;
}

.cancel-btn {
  color: #666;
  border-right: 1rpx solid #eee;
}

.confirm-btn {
  color: #007AFF;
}

.action-modal-container {
  background-color: #fff;
  border-radius: 20rpx 20rpx 0 0;
  overflow: hidden;
}

.action-item {
  padding: 30rpx 0;
  text-align: center;
  font-size: 32rpx;
  border-bottom: 1rpx solid #eee;
}

.action-item.cancel {
  margin-top: 10rpx;
  background-color: #f8f8f8;
  border-bottom: none;
  color: #f56c6c;
}

/* 已停用分类部分 - 手风琴样式 */
.view-disabled-btn-container {
  padding: 15rpx 20rpx;
  margin: 0 20rpx;
}

.view-disabled-btn {
  color: #888; /* 灰色文字 */
  font-size: 26rpx;
  text-decoration: underline; /* 下划线 */
  padding: 5rpx 0;
  display: inline-block;
  transition: color 0.2s ease;
  
  &:active {
    color: #666;
  }
}

.disabled-accordion-container {
  margin: 0 20rpx 20rpx;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.disabled-collapse {
  background-color: #fff;
  border-radius: 12rpx;
  overflow: hidden;
}

.uni-collapse-item__content {
  padding: 0 !important;
}

.disabled-grid-container {
  padding: 15rpx;
}

.disabled-card {
  height: 160rpx;
  width: 110rpx;
  background-color: #f5f5f5;
  border: 1rpx solid #e0e0e0;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.2s ease;
  
  &:active {
    background-color: #eeeeee;
  }
}

.disabled-icon {
  font-size: 48rpx;
  color: #999;
  margin-bottom: 10rpx;
}

.disabled-name {
  font-size: 24rpx;
  color: #666;
  max-width: 120rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
}

.disabled-badge {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  background-color: #ccc;
  color: #fff;
  font-size: 20rpx;
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
}
.collapse-title {
  display: flex;
  align-items: center;
  gap: 40rpx;
  padding: 10px;
}
.collapse-title-icon { font-size: 28rpx; color: #007AFF; }
.collapse-title-text { font-size: 28rpx; color: #333; }
.first-char-icon {
  width: 25rpx;
  height: 25rpx;
  line-height: 25rpx;
  display: inline-block;
  text-align: center;
  font-weight: bold;
  color: #faad14;
}
</style>
