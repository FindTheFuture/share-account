<template>
  <view class="user-list-page">
    <!-- 搜索区域 -->
    <view class="search-container">
      <view class="search-group flex items-center">
        <uni-easyinput
          prefixIcon="search"
          v-model="searchPhone"
          placeholder="请输入手机号"
          type="digit"
          maxlength="11"
          :disabled="loading"
          @input="handleInput"
          class="flex-1 mr-2 search-input"
        ></uni-easyinput>
        <button
          :loading="loading"
          :disabled="loading"
          type="primary"
          @click="onSearch"
          class="search-button"
        >
          查询
        </button>
      </view>
    </view>
    
    <!-- 初始加载状态 -->
    <view class="loading-container" v-if="initialLoading">
      <uni-load-more status="loading" contenttext="正在加载用户数据..."></uni-load-more>
    </view>
    
    <!-- 列表区域 -->
    <view class="list-container" v-if="!initialLoading && userList.length > 0">
      <view class="user-item" v-for="(user, index) in userList" :key="index" @click="navigateToDetail(user.haoe)">
        <view class="user-info-top flex justify-between items-center mb-4">
          <view class="user-hao text-gray-500 text-sm">#{{ user.haoe }}</view>
          <view class="user-phone font-medium text-base">{{ user.phone }}</view>
          <view class="user-role-tag" :class="getRoleClass(user.roleName)">
            {{ user.roleName }}
          </view>
        </view>
        <view class="user-info-bottom flex justify-between items-center">
          <view class="user-integral text-danger text-base">积分: {{ user.validIntegral }}</view>
        </view>
        
        <!-- 操作按钮 -->
        <view class="user-actions flex justify-end mt-4">
          <button type="default" size="mini" class="mini-btn mini-btn-warning" @click.stop="showRolePicker(user)">
            设置角色
          </button>
          <button type="default" size="mini" class="mini-btn ml-2" @click.stop="navigateToEdit(user.haoe)">
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
export default {
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
    }
  }
}
</script>

<style lang="scss">
/* 使用uni-ui组件，减少自定义样式 */

.user-list-page {
  background-color: #f8f8f8;
  min-height: 100vh;
  padding-bottom: 30rpx;
}

.search-container {
  padding: 20rpx;
}

.search-group {
  display: flex;
  align-items: center;
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 10rpx 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  
  .search-input {
    flex: 1;
    margin-right: 10rpx;
    height: 68rpx;
    border: none;
    padding: 0 10rpx;
    font-size: 28rpx;
    color: #333333;
    
    &:focus {
      background-color: #ffffff;
    }
  }
  
  .search-button {
    min-width: 160rpx;
    height: 68rpx;
    border-radius: 12rpx;
    font-size: 28rpx;
    font-weight: 500;
    display: flex;
    align-items: center;
    justify-content: center;
    white-space: nowrap;
    background-color: #1989fa;
    color: #1989fa;
    border: none;
    transition: all 0.3s;
    
    &:active {
      background-color: #40a9ff;
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
  margin: 0 20rpx 20rpx;
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
  margin-bottom: 16rpx;
}

.user-hao {
  color: #999999;
  font-size: 24rpx;
}

.user-phone {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.user-role-tag {
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
  font-size: 24rpx;
  
  &.role-admin {
    background-color: #e6f7ff;
    color: #1989fa;
  }
  
  &.role-user {
    background-color: #f6ffed;
    color: #52c41a;
  }
  
  &.role-other {
    background-color: #fff7e6;
    color: #fa8c16;
  }
}

.user-info-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-integral {
  color: #ff7e00;
  font-size: 28rpx;
  font-weight: 500;
}

.user-actions {
  display: flex;
  justify-content: flex-end;
  width: 100%;
  margin-top: 16rpx;
  
  .mini-btn {
    min-width: 120rpx;
    padding: 0 16rpx;
    line-height: 56rpx;
    font-size: 26rpx;
    border-radius: 16rpx;
    transition: all 0.3s;
    border: 1rpx solid;
    
    &:active {
      transform: scale(0.95);
    }
  }
  
  .mini-btn-warning {
    color: #ffffff;
    background-color: #fa8c16;
    border-color: #fa8c16;
    
    &:active {
      background-color: #ffad33;
    }
  }
  
  .ml-2 {
    margin-left: 10rpx;
    color: #1989fa;
  }
}

.empty-container {
  padding-top: 100rpx;
  display: flex;
  justify-content: center;
}

.pagination-container {
  padding: 20rpx;
  
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
