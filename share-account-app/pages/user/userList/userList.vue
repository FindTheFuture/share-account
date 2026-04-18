<template>
  <view class="user-list-page">
    <!-- 搜索区域 -->
    <view class="search-container">
      <view class="search-group flex items-center">
        <!-- 自定义输入框 -->
        <view class="custom-input flex items-center" :class="{ 'input-focused': isInputFocused }">
          <view class="input-icon">
            <uni-icons type="search" size="32" color="#999999"></uni-icons>
          </view>
          <input
            v-model="searchPhone"
            placeholder="请输入手机号"
            type="number"
            maxlength="11"
            :disabled="loading"
            @input="handleInput"
            @focus="handleInputFocus"
            @blur="handleInputBlur"
            class="input-field"
          />
        </view>
        <!-- 自定义查询按钮 -->
        <view 
          :class="{ 'loading': loading }"
          :disabled="loading"
          @click="onSearch"
          class="custom-button"
        >
          <text class="button-text">查询</text>
        </view>
      </view>
    </view>
    
    <!-- 初始加载状态 -->
    <view class="loading-container" v-if="initialLoading">
      <uni-load-more status="loading" contenttext="正在加载用户数据..."></uni-load-more>
    </view>
    
    <!-- 列表区域 -->
    <view class="list-container" v-if="!initialLoading && userList.length > 0">
      <view class="user-item" v-for="(user, index) in userList" :key="index" @click="navigateToDetail(user.haoe)">
          <view class="user-info-top flex justify-between items-center">
            <view class="user-hao">#{{ user.haoe }}</view>
            <view class="user-role-tag" :class="getRoleClass(user.roleName)">
              {{ user.roleName }}
            </view>
          </view>
          <view class="user-info-middle">
            <view class="user-integral-row">
              <view class="user-integral">积分: {{ user.validIntegral }}</view>
              <view class="user-bill-count">账单数量: {{ user.billCount || 0 }}</view>
            </view>
          </view>
          <view class="user-info-details">
            <view class="info-row">
              <text class="info-label">昵称:</text>
              <text class="info-value">{{ user.nickName }}{{ user.isGuest ? ' (游客)' : '' }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">手机号:</text>
              <text class="info-value">{{ user.phone || '未设置' }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">上次登录:</text>
              <text class="info-value">{{ user.lastLoginTime || '未登录' }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">创建时间:</text>
              <text class="info-value">{{ user.createTime || '未知' }}</text>
            </view>
          </view>
        
        <!-- 操作按钮 -->
        <view class="user-actions flex justify-between">
          <button type="default" size="mini" class="mini-btn mini-btn-warning" @click.stop="showRolePicker(user)">
            设置角色
          </button>
          <button type="default" size="mini" class="mini-btn mini-btn-edit" @click.stop="navigateToEdit(user.haoe)">
            编辑
          </button>
        </view>
      </view>
    </view>
    
    <!-- 空状态 -->
    <view class="empty-container" v-else-if="!initialLoading && userList.length === 0">
      <ui-empty
        description="暂无用户数据"
        :show-icon="true"
        icon="empty"
      ></ui-empty>
    </view>
    
    <!-- 分页控件 -->
    <view class="pagination-container" v-if="!initialLoading && totalNum > 0">
      <uni-pagination
        :current="currentPageNum"
        :total="totalNum"
        :page-size="pageSize"
        show-icon
        @change="onPageChange"
      ></uni-pagination>
      <!-- 分页信息居中显示 -->
      <view class="btn-view flex justify-center mt-4">
        <text class="example-info">每页{{ pageSize }}条，总共{{ totalNum }}条</text>
      </view>
    </view>
  </view>
</template>

<script>
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';

export default {
  components: {
    UniIcons
  },
  data() {
    return {
      searchPhone: '',
      userList: [],
      totalNum: 0,
      pageTotalNum: 0,
      currentPageNum: 1,
      pageSize: 10,
      loading: false,
      initialLoading: true,
      isInputFocused: false,
      enumData: {} // 存储枚举值数据
    }
  },
  onLoad() {
    Promise.all([
      this.getEnums(),    // 获取枚举值
      this.searchUser()  // 获取用户列表
    ]).catch(err => {
      console.error('初始化失败:', err);
      uni.showToast({
        title: '初始化失败，请重试',
        icon: 'none'
      });
    });
  },
  computed: {
    loadMoreStatus() {
      if (this.currentPageNum >= this.pageTotalNum) {
        return 'noMore';
      }
      return this.loading ? 'loading' : 'more';
    }
  },
  methods: {
    // 获取所有枚举值
    async getEnums() {
      try {
        const response = await this.$request({
          url: this.$backUrlConfig.endpoints.getAllEnums,
          method: 'GET'
        });
        
        // 直接获取枚举数据（无code包装）
        this.enumData = response;
      } catch (error) {
        const errorMsg = error.message || '网络异常';
        console.error('获取枚举值异常:', errorMsg);
        uni.showToast({
          title: `网络错误: ${errorMsg}`,
          icon: 'none'
        });
      }
    },
    
    handleInput(e) {
      this.searchPhone = e.replace(/[^\d]/g, '');
    },
    
    validatePhone() {
      if (!this.searchPhone) return false;
      return /^\d{1,11}$/.test(this.searchPhone);
    },
    
    onSearch() {
      this.currentPageNum = 1;
      this.searchUser();
    },
    
    onLoadMore() {
      if (this.loading || this.currentPageNum >= this.pageTotalNum) {
        return;
      }
      
      this.currentPageNum++;
      this.searchUser(true);
    },
    
    onPageChange(e) {
      this.currentPageNum = e.current;
      this.searchUser();
    },
    
    async searchUser(isLoadMore = false) {
      this.loading = true;
      
      try {
        const res = await this.$request({
          url: this.$backUrlConfig.endpoints.findUserList,
          method: 'POST',
          data: {
            phone: this.searchPhone,
            pageNum: this.currentPageNum,
            pageSize: this.pageSize
          }
        });
        
        this.userList = isLoadMore ? [...this.userList, ...res.userList] : res.userList;
        this.totalNum = res.totalNum || 0;
        this.pageTotalNum = Math.ceil(this.totalNum / this.pageSize);
        this.currentPageNum = res.currentPageNum || this.currentPageNum;
        
        if (this.userList == null || this.userList.length === 0) {
          this.userList = [];
          uni.showToast({
            title: this.searchPhone ? '未找到匹配用户' : '暂无用户数据',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('查询用户列表失败:', error);
        uni.showToast({
          title: '查询失败，请重试',
          icon: 'none'
        });
      } finally {
        this.loading = false;
        this.initialLoading = false;
      }
    },
    
    navigateToDetail(haoe) {
      uni.navigateTo({
        url: `/pages/user/userDetail/userDetail?haoe=${haoe}`
      });
    },
    
    showRolePicker(user) {
      if (!this.enumData?.roleEnum || this.enumData.roleEnum.length === 0) {
        uni.showToast({
          title: '角色数据未加载完成',
          icon: 'none'
        });
        return;
      }
      
      const roleNames = this.enumData.roleEnum.map(item => item.description);
      
      uni.showActionSheet({
        itemList: roleNames,
        success: async (res) => {
          const selectedRole = this.enumData.roleEnum[res.tapIndex];
          await this.setUserRole(user.haoe, selectedRole.id);
        }
      });
    },
    
    async setUserRole(haoe, roleId) {
      try {
        const res = await this.$request({
          url: `${this.$backUrlConfig.endpoints.setRole}/${haoe}/${roleId}`,
          method: 'GET'
        });
        
        if (res) {
          uni.showToast({
            title: '角色设置成功',
            icon: 'success'
          });
          this.searchUser();
        } else {
          uni.showToast({
            title: res.message || '角色设置失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('设置角色异常:', error);
        uni.showToast({
          title: '网络请求异常',
          icon: 'none'
        });
      }
    },
    
    navigateToEdit(haoe) {
      uni.navigateTo({
        url: `/pages/user/editUser/editUser?haoe=${haoe}`
      });
    },
    
    // 根据角色ID获取角色名称
    getUserRoleName(roleId) {
      if (!this.enumData?.roleEnum) return '未知角色';
      const role = this.enumData.roleEnum.find(item => item.id === roleId);
      return role?.description || '未知角色';
    },
    
    // 根据角色名称获取角色样式类
    getRoleClass(roleName) {
      switch(roleName) {
        case '管理员':
          return 'role-admin';
        case '普通用户':
          return 'role-user';
        default:
          return 'role-other';
      }
    },
    
    // 处理输入框聚焦
    handleInputFocus() {
      this.isInputFocused = true;
    },
    
    // 处理输入框失焦
    handleInputBlur() {
      this.isInputFocused = false;
    }
  }
}
</script>

<style lang="scss">
/* 设计稿样式实现 */

.user-list-page {
  background-color: #f8f8f8;
  min-height: 100vh;
  padding: 20rpx;
}

.search-container {
  margin-bottom: 20rpx;
}

.search-group {
      display: flex;
      align-items: center;
      gap: 24rpx;
      width: 100%;
      
      .custom-input {
        flex: 1;
        height: 96rpx;
        padding: 0 36rpx;
        background: linear-gradient(145deg, #F5F0FF, #E8DFFF);
        border-radius: 24rpx;
        border: 2rpx solid #E0D6FF;
        box-shadow: 0 4rpx 12rpx rgba(146, 110, 255, 0.1);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        display: flex;
        align-items: center;
        flex-wrap: nowrap;
        min-width: 0;
        
        &:hover {
          border-color: #C9B8FF;
          box-shadow: 0 6rpx 16rpx rgba(146, 110, 255, 0.15);
        }
        
        &.input-focused {
          border-color: #926EFF;
          box-shadow: 0 0 0 12rpx rgba(146, 110, 255, 0.15), 0 6rpx 20rpx rgba(146, 110, 255, 0.2);
          transform: translateY(-2rpx);
        }
        
        .input-icon {
          margin-right: 24rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          flex-shrink: 0;
          width: 32rpx;
          height: 32rpx;
          
          uni-icons {
            color: #926EFF;
            transition: all 0.3s ease;
          }
        }
        
        .input-field {
          flex: 1;
          height: 100%;
          border: none;
          font-size: 34rpx;
          color: #333333;
          background-color: transparent;
          font-weight: 500;
          width: 100%;
          min-width: 0;
          
          &::placeholder {
            color: #A890FF;
            font-weight: 400;
          }
          
          &:focus {
            outline: none;
          }
        }
      }
  

  
  .custom-button {
    width: 180rpx;
    height: 96rpx;
    border-radius: 24rpx;
    font-size: 34rpx;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    white-space: nowrap;
    background: linear-gradient(145deg, #409EFF, #3385FF);
    color: #ffffff;
    border: none;
    box-shadow: 0 6rpx 16rpx rgba(64, 158, 255, 0.3);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    cursor: pointer;
    position: relative;
    overflow: hidden;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
      transition: left 0.6s ease;
    }
    
    &:hover {
      transform: translateY(-2rpx);
      box-shadow: 0 8rpx 20rpx rgba(64, 158, 255, 0.4);
      background: linear-gradient(145deg, #3385FF, #409EFF);
    }
    
    &:hover::before {
      left: 100%;
    }
    
    &.loading {
      opacity: 0.8;
      transform: scale(0.98);
    }
    
    &:active {
      transform: translateY(0);
      box-shadow: 0 4rpx 12rpx rgba(64, 158, 255, 0.3);
      background: linear-gradient(145deg, #3385FF, #2979FF);
    }
    
    .button-text {
      color: #ffffff;
      position: relative;
      z-index: 1;
    }
  }
}

.loading-container {
  padding-top: 200rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-item {
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
  
  &:active {
    transform: scale(0.98);
    box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.05);
  }
}

.user-info-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.user-hao {
  color: #666666;
  font-size: 28rpx;
  font-weight: 500;
}

.user-info-middle {
  margin-bottom: 20rpx;
}

.user-integral-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-integral {
  color: #FF7E00;
  font-size: 28rpx;
  font-weight: 500;
}

.user-bill-count {
  color: #409EFF;
  font-size: 28rpx;
  font-weight: 500;
}

.user-info-details {
  margin-bottom: 24rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
  font-size: 26rpx;
}

.info-label {
  width: 140rpx;
  color: #666666;
  font-weight: 500;
}

.info-value {
  flex: 1;
  color: #333333;
  word-break: break-word;
}

.user-role-tag {
    padding: 6rpx 20rpx;
    border-radius: 20rpx;
    font-size: 24rpx;
    font-weight: 500;
    
    &.role-admin {
      background-color: #E6F7EF;
      color: #409EFF;
    }
    
    &.role-user {
      background-color: #E6F7EF;
      color: #52C41A;
    }
    
    &.role-other {
      background-color: #E6F7EF;
      color: #52C41A;
    }
  }

.user-actions {
  display: flex;
  justify-content: space-between;
  
  .mini-btn {
    flex: 1;
    min-width: 180rpx;
    padding: 0 20rpx;
    line-height: 72rpx;
    font-size: 28rpx;
    font-weight: 500;
    border-radius: 28rpx;
    transition: all 0.3s;
    
    &:active {
      transform: scale(0.95);
    }
  }
  
  .mini-btn-warning {
    color: #ffffff;
    background-color: #FF7E00;
    border-color: #FF7E00;
    margin-right: 20rpx;
    
    &:active {
      background-color: #FF9E33;
    }
  }
  
  .mini-btn-edit {
    color: #409EFF;
    background-color: #ffffff;
    border: 5rpx solid #409EFF;
    
    &:active {
      background-color: #F0F8FF;
    }
  }
}

.empty-container {
  padding-top: 100rpx;
  display: flex;
  justify-content: center;
}

.pagination-container {
  margin-top: 20rpx;
  
  .btn-view {
    text-align: center;
    
    .example-info {
      display: inline-block;
      color: #666666;
      font-size: 24rpx;
    }
  }
}
</style>
