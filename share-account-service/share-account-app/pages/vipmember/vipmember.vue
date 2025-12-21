<template>
  <view class="vipmember-page">
    <!-- 主内容区域 -->
    <view class="main-content">
      <!-- 用户信息头部 -->
      <view class="user-info">
      <view class="avatar-container">
        <user-avatar
          :avatar-url="userInfo.pictureAddress"
          :user-id="String(userInfo.haoe)"
          :uploadable="false"
          :show-edit-mask="false"
          :clickable="false"
        ></user-avatar>
      </view>
      <view class="user-details">
        <view class="nickname">{{ userInfo.nickName || '未设置昵称' }}</view>
        <view class="remaining-counts">
          <text class="count-item">AI识别剩余: {{ remainingAiCount }}次</text>
          <text class="count-item">PDF导出剩余: {{ remainingPdfCount }}次</text>
          <text class="count-item">会员等级: {{ userInfo.memberLevelIcon }} ({{ userInfo.validIntegral || 0 }}分)</text>
        </view>
      </view>
    </view>

    <!-- 会员信息区域 -->
    <view class="vip-section">
      <view class="section-title">
        我的会员
        <text class="view-all-btn" @click="showAllMembersPopup">全部</text>
      </view>
      
      <!-- 当前订阅的会员套餐列表 -->
        <view v-if="filteredMembers && filteredMembers.length > 0">
          <view class="member-card" v-for="member in filteredMembers" :key="member.id || index">
            <view class="card-header">
              <view class="package-name">{{ member.packageName || '套餐名称' }}</view>
            </view>
            <view class="card-body">
              <view class="count-item">
                <view class="count-label">AI识别</view>
                <view class="count-value">{{ member.aiCount - member.aiUsedCount > 0 ? (member.aiCount - member.aiUsedCount) : 0 }}/{{ member.aiCount || 0 }}</view>
                <progress :percent="(member.aiCount - member.aiUsedCount > 0 ? (member.aiCount - member.aiUsedCount) : 0) / (member.aiCount || 1) * 100" stroke-width="6" activeColor="#4cd964" backgroundColor="rgba(255, 255, 255, 0.3)" />
              </view>
              <view class="count-item">
                <view class="count-label">PDF导出</view>
                <view class="count-value">{{ member.pdfCount - member.pdfUsedCount > 0 ? (member.pdfCount - member.pdfUsedCount) : 0 }}/{{ member.pdfCount || 0 }}</view>
                <progress :percent="(member.pdfCount - member.pdfUsedCount > 0 ? (member.pdfCount - member.pdfUsedCount) : 0) / (member.pdfCount || 1) * 100" stroke-width="6" activeColor="#4cd964" backgroundColor="rgba(255, 255, 255, 0.3)" />
              </view>
            </view>
            <view class="card-footer">
              <view class="end-time">有效期至: {{ member.endTime || '--' }}</view>
              <view class="status" :class="member.status === 0 ? 'status-normal' : 'status-expired'">{{ member.statusName || '--' }}</view>
            </view>
          </view>
        
        <!-- 分页器 -->
        <uni-pagination
          v-if="total > 0"
          :current="currentPage"
          :pageSize="pageSize"
          :total="total"
          @change="changePage"
          show-icon
          prev-text="上一页"
          next-text="下一页"
        ></uni-pagination>
      </view>
      
      <view v-else class="empty-tip">
        无有效会员记录
      </view>
    </view>

    <!-- 所有会员套餐列表 -->
    <view class="packages-section">
      <view class="section-title">会员套餐</view>
      
      <!-- Tab切换结构 -->
      <view class="tab-container">
        <view class="tab-bar">
          <view 
            class="tab-item" 
            :class="{ active: activeTab === 0 }"
            @click="activeTab = 0"
          >
            周期套餐
          </view>
          <view 
            class="tab-item" 
            :class="{ active: activeTab === 1 }"
            @click="activeTab = 1"
          >
            应急包
          </view>
        </view>
        
        <!-- 周期套餐内容 -->
        <view class="tab-content" v-show="activeTab === 0">
          <view v-if="periodPackages && periodPackages.length > 0">
            <view class="package-card" v-for="(pkg, index) in periodPackages" :key="pkg.id || index" :data-recommend="pkg.isRecommend === 1">
              <view class="package-badge" v-if="pkg.isRecommend === 1">推荐</view>
              <view class="package-header">
                <view class="package-title">{{ pkg.name || '套餐名称' }}</view>
              </view>
              <view class="package-price">¥{{ pkg.price || 0 }}<text class="price-unit">/份</text></view>
              <view class="package-info">
                <view class="info-item">
                  <text class="info-icon">⏱</text>
                  <text class="info-text">{{ pkg.durationDays || 0 }}天有效期</text>
                </view>
                <view class="info-item">
                  <text class="info-icon">🤖</text>
                  <text class="info-text">AI识别 {{ pkg.aiCount || 0 }}次</text>
                </view>
                <view class="info-item">
                  <text class="info-icon">📄</text>
                  <text class="info-text">PDF导出 {{ pkg.pdfCount || 0 }}次</text>
                </view>
                <view class="info-item">
                  <text class="info-icon">🎁</text>
                  <text class="info-text">赠送 {{ pkg.points || 0 }}积分</text>
                </view>
              </view>
              <view class="package-description" v-if="pkg.description">{{ pkg.description }}</view>
              <button class="subscribe-btn" @click="handleSubscribe(pkg)">立即订阅</button>
            </view>
          </view>
          <view v-else class="empty-tip">
            暂无套餐
          </view>
        </view>
        
        <!-- 功能次数套餐内容 -->
        <view class="tab-content" v-show="activeTab === 1">
          <view v-if="functionalPackages && functionalPackages.length > 0">
            <view class="package-card" v-for="(pkg, index) in functionalPackages" :key="pkg.id || index" :data-recommend="pkg.isRecommend === 1">
              <view class="package-badge" v-if="pkg.isRecommend === 1">推荐</view>
              <view class="package-header">
                <view class="package-title">{{ pkg.name || '套餐名称' }}</view>
              </view>
              <view class="package-price">¥{{ pkg.price || 0 }}<text class="price-unit">/份</text></view>
              <view class="package-info">
                <view class="info-item">
                  <text class="info-icon">🤖</text>
                  <text class="info-text">AI识别 {{ pkg.aiCount || 0 }}次</text>
                </view>
                <view class="info-item">
                  <text class="info-icon">📄</text>
                  <text class="info-text">PDF导出 {{ pkg.pdfCount || 0 }}次</text>
                </view>
                <view class="info-item">
                  <text class="info-icon">🎁</text>
                  <text class="info-text">赠送 {{ pkg.points || 0 }}积分</text>
                </view>
              </view>
              <view class="package-description" v-if="pkg.description">{{ pkg.description }}</view>
              <button class="subscribe-btn" @click="handleSubscribe(pkg)">立即订阅</button>
            </view>
          </view>
          <view v-else class="empty-tip">
            暂无应急包
          </view>
        </view>
      </view>
    </view>
    </view>
    <!-- 会员列表弹窗 -->
    <view class="members-popup" v-if="showMembersPopup">
      <view class="popup-overlay" @click="closeMembersPopup"></view>
      <view class="popup-content">
        <view class="popup-header">
          <view class="popup-title">所有会员记录</view>
          <view class="popup-close" @click="closeMembersPopup">×</view>
        </view>
        <view class="popup-body">
          <view v-if="popupMembers && popupMembers.length > 0">
            <view class="popup-member-card" v-for="member in popupMembers" :key="member.id || index">
              <view class="card-header">
                <view class="package-name">{{ member.packageName || '套餐名称' }}</view>
                <view class="member-status" :class="member.status === 0 ? 'status-active' : 'status-inactive'">
                  {{ member.statusName || (member.status === 0 ? '正常' : '已过期') }}
                </view>
              </view>
              <view class="package-type" v-if="member.packageTypeName">
                {{ member.packageTypeName }}
              </view>
              <view class="card-body">
                <view class="count-item">
                  <view class="count-label">AI识别</view>
                  <view class="count-value">{{ member.aiCount - member.aiUsedCount > 0 ? (member.aiCount - member.aiUsedCount) : 0 }}/{{ member.aiCount || 0 }}</view>
                </view>
                <view class="count-item">
                  <view class="count-label">PDF导出</view>
                  <view class="count-value">{{ member.pdfCount - member.pdfUsedCount > 0 ? (member.pdfCount - member.pdfUsedCount) : 0 }}/{{ member.pdfCount || 0 }}</view>
                </view>
              </view>
              <view class="card-footer">
                <view class="time-info">
                  <view class="start-time">开始时间: {{ member.startTime || '--' }}</view>
                  <view class="end-time">结束时间: {{ member.endTime || '--' }}</view>
                </view>
              </view>
            </view>
            
            <!-- 分页器 -->
            <uni-pagination
              v-if="popupTotal > 0"
              :current="popupCurrentPage"
              :pageSize="popupPageSize"
              :total="popupTotal"
              @change="handlePopupPageChange"
              show-icon
              prev-text="上一页"
              next-text="下一页"
              class="popup-pagination"
            ></uni-pagination>
          </view>
          <view v-else class="empty-tip">
            <view class="empty-icon">🎫</view>
            <view class="empty-text">暂无会员记录</view>
            <view class="empty-subtext">快去订阅会员套餐享受更多权益吧</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import UserAvatar from '@/components/user-avatar.vue'
  import wechatPayment from '@/common/wechatPayment.js'
  export default {
    components: {
      UserAvatar,
      uniPagination: () => import('@/uni_modules/uni-pagination/components/uni-pagination/uni-pagination.vue')
    },

    data() {
    return {
      userInfo: {},
      // 所有会员剩余使用次数总计
      remainingAiCount: 0,
      remainingPdfCount: 0,
      currentMembers: [], // 所有会员数据
        filteredMembers: [], // 过滤后的会员数据(默认只显示status=0)
        popupMembers: [], // 弹窗中显示的会员数据
        allPackages: [],
        periodPackages: [], // 周期套餐 (type = 0)
        functionalPackages: [], // 功能次数套餐 (type = 1)
        activeTab: 0, // 默认选中周期套餐
        currentPage: 1,
        pageSize: 1,
        popupCurrentPage: 1,
        popupPageSize: 1,
        popupTotal: 0,
        total: 0,
        loading: false,
        showAllMembers: false, // 是否显示所有会员状态
        showMembersPopup: false, // 控制弹窗显示
      }
    },
    onLoad() {
        
    },
    onShow() {
      this.loadData()
    },
    methods: {
      async loadData() {
        try {
          this.loading = true
          
          // 并行加载用户信息和会员数据
          await Promise.all([
              this.getUserInfo(),
              this.loadCurrentMembers(),
              this.loadAllPackages()
          ])
        } catch (error) {
          console.error('加载数据失败:', error)
          uni.showToast({
            title: '加载失败',
            icon: 'none'
          })
          // 在真实环境加载失败时，使用模拟数据作为备份
          this.setMockData()
        } finally {
          this.loading = false
        }
      },
      
      // 获取用户信息
      async getUserInfo() {
        try {
          const res = await this.$request({
            url: this.$backUrlConfig.endpoints.getUserInfo + 0,
            method: 'GET'
          })
          this.userInfo = res
        } catch (error) {
          console.error('获取用户信息出错:', error)
        }
      },
      
      // 加载当前订阅的会员套餐
      async loadCurrentMembers() {
        try {
          const res = await this.$request({
            url: this.$backUrlConfig.endpoints.userMember_queryUserMembers,
            method: 'GET',
            data: {
              currentPage: this.currentPage,
              pageSize: this.pageSize,
              status: 0 // 主页查询时传递状态0
            }
          })
          // 确保数据格式正确
          if (res && res.pageInfo) {
            this.currentMembers = res.pageInfo.list || []
            this.total = res.pageInfo.total || 0
            // 保存所有会员剩余使用次数总计
            this.remainingAiCount = res.remainingAiCount || 0
            this.remainingPdfCount = res.remainingPdfCount || 0
            
            // 根据当前过滤状态处理数据
            if (this.showAllMembers) {
              // 显示所有状态的会员
              this.filteredMembers = this.currentMembers
            } else {
              // 只显示状态为0的会员
              this.filteredMembers = this.currentMembers.filter(member => member.status === 0)
              // 如果当前页过滤后没有数据且不是第一页，尝试加载上一页
              if (this.filteredMembers.length === 0 && this.currentPage > 1) {
                this.currentPage = 1
                return this.loadCurrentMembers() // 重新加载第一页
              }
            }
          } else {
            this.currentMembers = []
            this.filteredMembers = []
            this.total = 0
          }
        } catch (error) {
          console.error('加载会员套餐失败:', error)
          uni.showToast({
            title: '加载会员信息失败',
            icon: 'none'
          })
          this.currentMembers = []
          this.filteredMembers = []
          this.total = 0
        }
      },
      
      // 加载所有会员套餐
      async loadAllPackages() {
        try {
          const res = await this.$request({
            url: this.$backUrlConfig.endpoints.membership_packages,
            method: 'GET'
          })

          // 确保数据格式正确
          if (res) {
            this.allPackages = Array.isArray(res) ? res : []
            
            // 按type字段分组套餐数据
            this.periodPackages = this.allPackages.filter(pkg => pkg.type === 0)
            this.functionalPackages = this.allPackages.filter(pkg => pkg.type === 1)
          } else {
            this.allPackages = []
            this.periodPackages = []
            this.functionalPackages = []
          }
        } catch (error) {
          console.error('加载所有套餐失败:', error)
          uni.showToast({
            title: '加载套餐信息失败',
            icon: 'none'
          })
          this.allPackages = []
          this.periodPackages = []
          this.functionalPackages = []
        }
      },
      
      // 分页切换
      changePage(e) {
        this.currentPage = e.current
        // 重新加载对应页的数据
        this.loadCurrentMembers()
      },
      
      // 过滤会员数据
      filterMembers(showAll = false) {
        this.showAllMembers = showAll
        if (showAll) {
          this.filteredMembers = this.currentMembers
        } else {
          this.filteredMembers = this.currentMembers.filter(member => member.status === 0)
        }
      },
      
      // 显示所有会员弹窗
      showAllMembersPopup() {
        this.showMembersPopup = true
        this.popupCurrentPage = 1 // 重置为第一页
        this.loadPopupMembers() // 调用后端接口加载数据
      },
      
      // 关闭弹窗
      closeMembersPopup() {
        this.showMembersPopup = false
      },
      
      // 加载弹窗中的会员数据（调用后端接口）
      async loadPopupMembers() {
        try {
          const res = await this.$request({
            url: this.$backUrlConfig.endpoints.userMember_queryUserMembers,
            method: 'GET',
            data: {
              currentPage: this.popupCurrentPage,
              pageSize: this.popupPageSize
              // 弹窗查询时不传递status参数
            }
          })
          
          // 确保数据格式正确
          if (res && res.pageInfo) {
            this.popupMembers = res.pageInfo.list || []
            this.popupTotal = res.pageInfo.total || 0
          } else {
            this.popupMembers = []
            this.popupTotal = 0
          }
          
        } catch (error) {
          console.error('加载弹窗会员数据失败:', error)
          uni.showToast({
            title: '加载会员信息失败',
            icon: 'none'
          })
          this.popupMembers = []
          this.popupTotal = 0
        }
      },
      
      // 处理弹窗分页切换
      handlePopupPageChange(e) {
        this.popupCurrentPage = e.current
        // 调用后端接口加载对应页码的数据
        this.loadPopupMembers()
      },
      
      // 返回上一页
      goBack() {
        uni.navigateBack()
      },
      
      // 处理订阅点击
      async handleSubscribe(pkg) {
        if (!pkg || !pkg.id) {
          uni.showToast({
            title: '套餐信息有误',
            icon: 'none'
          })
          return
        }
        
        uni.showModal({
          title: '确认订阅',
          content: `确认订阅「${pkg.name || '会员套餐'}」吗？\n价格：¥${pkg.price || 0}`,
          success: async (res) => {
            if (res.confirm) {
              try {
                // 显示加载提示
                uni.showLoading({
                  title: '正在处理支付...'
                })
                
                // 调用微信支付服务
                const paymentResult = await wechatPayment.processPayment({
                  packageId: pkg.id,
                  amount: pkg.price || 0,
                  payment_type: 'WECHAT_PAY',
                  goods_name: pkg.name || '会员套餐',
                  goods_description: `${pkg.name || '会员套餐'}订阅服务`
                })
                
                // 隐藏加载提示
                uni.hideLoading()
                
                // 处理支付结果
                this.handlePaymentResult(paymentResult, pkg)
              } catch (error) {
                // 隐藏加载提示
                uni.hideLoading()
                
                // 显示错误提示
                uni.showToast({
                  title: error.message || '支付请求处理失败',
                  icon: 'none'
                })
                console.error('支付请求处理失败:', error)
              }
            }
          }
        })
      },
      
      // 处理支付结果
      handlePaymentResult(paymentResult, pkg) {
        console.log('支付结果:', paymentResult);
        
        if (paymentResult && paymentResult.success) {
          // 支付成功
          uni.showModal({
            title: '支付成功',
            content: `恭喜您，成功订阅「${pkg.name || '会员套餐'}」！\n您的会员权益已开通。`,
            showCancel: false,
            success: () => {
              // 延迟刷新数据，确保用户体验
              setTimeout(() => {
                // 重新加载会员数据，更新页面显示
                this.loadData();
              }, 1500);
            }
          })
        } else if (paymentResult && paymentResult.canceled) {
          // 用户取消支付
          uni.showToast({
            title: '您已取消支付',
            icon: 'none'
          })
        } else {
          // 支付失败
          uni.showModal({
            title: '支付失败',
            content: `支付未完成，${paymentResult?.message || '请稍后重试'}`,
            showCancel: false,
            success: () => {
              // 可以提供重试或联系客服的选项
              // 这里简单处理，返回当前页面
            }
          })
        }
      },
    }
  }
</script>

<style lang="scss">
.vipmember-page {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 30rpx;
  position: relative;
  
  // 渐变背景装饰
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 400rpx;
    background: linear-gradient(135deg, #f56c6c 0%, #ff9800 100%);
    z-index: -1;
  }
  
  /* 标题右侧"全部"按钮样式 */
  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .view-all-btn {
      font-size: 28rpx;
      color: #f56c6c;
      padding: 8rpx 20rpx;
      border-radius: 100rpx;
      background-color: #fff0f0;
      transition: all 0.3s;
      
      &:active {
        transform: scale(0.95);
        opacity: 0.8;
      }
    }
  }
  
  /* 会员列表弹窗样式 */
  .members-popup {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
    display: flex;
    align-items: center;
    justify-content: center;
    // 添加淡入动画
    animation: fadeIn 0.3s ease;
    
    @keyframes fadeIn {
      from {
        opacity: 0;
      }
      to {
        opacity: 1;
      }
    }
    
    // 添加弹窗滑入动画
    .popup-content {
      animation: slideIn 0.3s ease;
      
      @keyframes slideIn {
        from {
          transform: translateY(50rpx);
          opacity: 0;
        }
        to {
          transform: translateY(0);
          opacity: 1;
        }
      }
    }
    
    .popup-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(0, 0, 0, 0.5);
    }
    
    .popup-content {
      position: relative;
      width: 85%;
      max-width: 600rpx;
      max-height: 80vh;
      background-color: #fff;
      border-radius: 20rpx;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      
      .popup-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 30rpx;
        border-bottom: 2rpx solid #f0f0f0;
        
        .popup-title {
          font-size: 36rpx;
          font-weight: 600;
          color: #333;
        }
        
        .popup-close {
          font-size: 48rpx;
          color: #999;
          width: 60rpx;
          height: 60rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 50%;
          transition: all 0.3s;
          
          &:active {
            background-color: #f5f5f5;
          }
        }
      }
      
          .popup-body {
          flex: 1;
          padding: 30rpx;
          overflow-y: auto;
          
          // 美化滚动条
          &::-webkit-scrollbar {
            width: 6rpx;
          }
          
          &::-webkit-scrollbar-track {
            background: #f1f1f1;
            border-radius: 3rpx;
          }
          
          &::-webkit-scrollbar-thumb {
            background: #c0c4cc;
            border-radius: 3rpx;
          }
          
          &::-webkit-scrollbar-thumb:hover {
            background: #909399;
          }
          
          .popup-pagination {
            margin-top: 30rpx;
          }
        }
        
        .popup-member-card {
          border: 2rpx solid #e4e7ed;
          border-radius: 16rpx;
          padding: 24rpx;
          margin-bottom: 20rpx;
          background-color: #fafafa;
          
          .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20rpx;
            
            .package-name {
              font-size: 32rpx;
              font-weight: 600;
              color: #333;
            }
            
            .member-status {
                font-size: 24rpx;
                padding: 6rpx 20rpx;
                border-radius: 100rpx;
                font-weight: 500;
                
                &.status-active {
                  background-color: #e1f3d8;
                  color: #67c23a;
                }
                
                &.status-inactive {
                  background-color: #f5f7fa;
                  color: #909399;
                }
              }
              
              .package-type {
                font-size: 22rpx;
                color: #909399;
                margin-top: -10rpx;
                margin-bottom: 15rpx;
              }
          }
          
          .card-body {
            display: flex;
            justify-content: space-around;
            margin-bottom: 20rpx;
            padding-bottom: 20rpx;
            border-bottom: 1rpx solid #ebeef5;
            
            .count-item {
              text-align: center;
              
              .count-label {
                font-size: 24rpx;
                color: #606266;
                margin-bottom: 8rpx;
              }
              
              .count-value {
                font-size: 28rpx;
                font-weight: 500;
                color: #303133;
              }
            }
          }
          
          .card-footer {
            
            .time-info {
              font-size: 24rpx;
              color: #909399;
              
              .start-time,
              .end-time {
                margin-bottom: 8rpx;
              }
            }
          }
        }
      }
    }
  }
  
  /* 页面头部导航栏 */
  .header {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    height: 100rpx;
    background-color: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10rpx);
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
    z-index: 1000;
  }
  
  .header-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 100%;
    padding: 0 30rpx;
  }
  
  .header-left, .header-right {
    width: 80rpx;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .back-icon {
    font-size: 40rpx;
    color: #303133;
  }
  
  .help-icon {
    font-size: 36rpx;
    color: #303133;
    width: 48rpx;
    height: 48rpx;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background-color: #f5f7fa;
  }
  
  .header-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #303133;
  }
  
  /* 用户信息卡片 */
  .user-info {
    background: linear-gradient(135deg, #f56c6c 0%, #f59a6c 100%);
    margin: 30rpx;
    padding: 40rpx;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    color: #fff;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
    position: relative;
    overflow: hidden;
    transition: transform 0.3s ease;
    
    .remaining-counts {
      display: flex;
      flex-direction: column;
      
      .count-item {
        font-size: 26rpx;
        color: rgba(255, 255, 255, 0.9);
        margin-bottom: 5rpx;
      }
    }
    
    &::before {
      content: '';
      position: absolute;
      top: -100rpx;
      right: -100rpx;
      width: 400rpx;
      height: 400rpx;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
    }
    
    &::after {
      content: '';
      position: absolute;
      bottom: -80rpx;
      left: -80rpx;
      width: 300rpx;
      height: 300rpx;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 50%;
    }
    
    &:active {
      transform: scale(0.98);
    }
    
    .avatar-container {
      width: 140rpx;
      height: 140rpx;
      border-radius: 50%;
      background-color: #fff;
      padding: 4rpx;
      margin-right: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      position: relative;
    }
    
    /* 头像容器样式 - 整合所有设置 */
    .avatar-container {
      border-radius: 50%;
      background-color: #fff;
      padding: 4rpx;
      margin-right: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      position: relative;
      z-index: 10;
      box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
    }
    
    /* VIP徽章 */
    .vip-badge {
      position: absolute;
      bottom: 0;
      right: 0;
      background-color: #ff9800;
      color: #fff;
      font-size: 20rpx;
      padding: 4rpx 16rpx;
      border-radius: 20rpx 0 0 0;
      font-weight: 500;
      z-index: 20;
    }
    
    .user-details {
        position: relative;
        z-index: 1;
      .nickname {
        font-size: 36rpx;
        font-weight: 600;
        margin-bottom: 10rpx;
        position: relative;
        z-index: 1;
      }
      
      .phone {
        font-size: 28rpx;
        opacity: 0.9;
        position: relative;
        z-index: 1;
      }
      
      .integral {
        font-size: 28rpx;
        position: relative;
        z-index: 1;
        
        .label {
          margin-right: 10rpx;
          opacity: 0.8;
        }
        
        .value {
          color: #fff;
          font-weight: 500;
        }
      }
    }
  }
  
  .vip-section,
  .packages-section {
    background-color: #fff;
    margin: 0 30rpx 30rpx;
    border-radius: 20rpx;
    padding: 30rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
    
    .section-title {
      font-size: 36rpx;
      font-weight: 600;
      margin-bottom: 30rpx;
      color: #333;
      position: relative;
      padding-bottom: 20rpx;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        width: 80rpx;
        height: 4rpx;
        background: linear-gradient(90deg, #f56c6c 0%, #ff9800 100%);
        border-radius: 2rpx;
      }
    }
    
    .empty-tip {
        text-align: center;
        color: #999;
        padding: 80rpx 0;
        font-size: 30rpx;
        background-color: #fafafa;
        border-radius: 16rpx;
        margin-bottom: 20rpx;
        
        .empty-icon {
          font-size: 80rpx;
          margin-bottom: 20rpx;
        }
        
        .empty-text {
          font-size: 32rpx;
          font-weight: 500;
          color: #606266;
          margin-bottom: 10rpx;
        }
        
        .empty-subtext {
          font-size: 26rpx;
          color: #909399;
        }
      }
  }
  
  /* 会员卡片样式 */
    .member-card {
      background: linear-gradient(135deg, #f56c6c 0%, #f59a6c 100%);
      border-radius: 16rpx;
      padding: 30rpx;
      margin-bottom: 20rpx;
      color: #fff;
      box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.1);
      position: relative;
      overflow: hidden;
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        right: 0;
        width: 200rpx;
        height: 200rpx;
        background: rgba(255, 255, 255, 0.05);
        border-radius: 50%;
        transform: translate(50%, -50%);
      }
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20rpx;
        position: relative;
        z-index: 1;
        
        .package-name {
          font-size: 32rpx;
          font-weight: 600;
        }
        
        .package-type {
          font-size: 24rpx;
          background: rgba(255, 255, 255, 0.2);
          padding: 6rpx 20rpx;
          border-radius: 100rpx;
        }
      }
      
      .card-body {
        display: flex;
        justify-content: space-around;
        margin-bottom: 20rpx;
        position: relative;
        z-index: 1;
        
        .count-item {
          text-align: center;
          width: 45%;
          
          .count-label {
            font-size: 24rpx;
            opacity: 0.8;
            margin-bottom: 10rpx;
          }
          
          .count-value {
            font-size: 36rpx;
            font-weight: 600;
            margin-bottom: 10rpx;
          }
          
          .wx-progress {
            background-color: rgba(255, 255, 255, 0.3) !important;
          }
          
          .wx-progress-bar {
            background-color: #4cd964 !important;
          }
        }
      }
      
      .card-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 24rpx;
        opacity: 0.8;
        position: relative;
        z-index: 1;
        
        .status-normal {
          color: #00ff00;
          font-weight: 500;
        }
        
        .status-expired {
          color: #ffcc00;
          font-weight: 500;
        }
      }
    }
  
  /* Tab切换样式 */
    .tab-container {
      margin-bottom: 20rpx;
      
      .tab-bar {
        display: flex;
        background-color: #f5f7fa;
        border-radius: 100rpx;
        padding: 8rpx;
        margin-bottom: 30rpx;
        
        .tab-item {
          flex: 1;
          text-align: center;
          padding: 20rpx 0;
          border-radius: 92rpx;
          font-size: 28rpx;
          color: #606266;
          transition: all 0.3s;
          
          &:active {
            transform: scale(0.98);
          }
          
          &.active {
            background-color: #fff;
            color: #f56c6c;
            font-weight: 600;
            box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
          }
        }
      }
      
      .tab-content {
        padding-top: 10rpx;
      }
    }
    
  /* 套餐卡片样式 */
    .package-card {
        border: 2rpx solid #e4e7ed;
        border-radius: 16rpx;
        padding: 30rpx;
        margin-bottom: 30rpx;
        transition: all 0.3s;
        position: relative;
        overflow: hidden;
        background-color: #fff;
        
        /* 推荐套餐特殊样式 */
        &[data-recommend="true"] {
          border-color: #f56c6c;
          box-shadow: 0 0 20rpx rgba(245, 108, 108, 0.1);
        }
      
      &:hover {
        box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
        transform: translateY(-4rpx);
      }
      
      .package-badge {
        position: absolute;
        top: 20rpx;
        right: -60rpx;
        background-color: #f56c6c;
        color: #fff;
        font-size: 20rpx;
        padding: 4rpx 0;
        width: 240rpx;
        text-align: center;
        transform: rotate(45deg);
        z-index: 1;
      }
      
      .package-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20rpx;
        padding-bottom: 20rpx;
        border-bottom: 1rpx solid #f0f0f0;
        
        .package-title {
          font-size: 32rpx;
          font-weight: 600;
          color: #333;
        }
        
        .package-type-name {
          font-size: 24rpx;
          color: #606266;
          background-color: #f5f7fa;
          padding: 4rpx 20rpx;
          border-radius: 100rpx;
        }
      }
      
      .package-price {
        font-size: 48rpx;
        font-weight: 700;
        color: #f56c6c;
        margin-bottom: 25rpx;
        
        .price-unit {
          font-size: 24rpx;
          font-weight: normal;
          color: #909399;
          margin-left: 4rpx;
        }
      }
      
      .package-info {
        display: flex;
        flex-wrap: wrap;
        margin-bottom: 25rpx;
        
        .info-item {
          width: 100%;
          font-size: 26rpx;
          color: #606266;
          margin-bottom: 15rpx;
          display: flex;
          align-items: center;
          
          .info-icon {
            font-size: 32rpx;
            margin-right: 15rpx;
            width: 40rpx;
            text-align: center;
          }
          
          .info-text {
            color: #303133;
            line-height: 1.4;
          }
        }
      }
      
      .package-description {
        font-size: 26rpx;
        color: #606266;
        margin-bottom: 25rpx;
        line-height: 1.6;
        padding: 15rpx;
        background-color: #fafafa;
        border-radius: 8rpx;
      }
      
      .subscribe-btn {
        width: 100%;
        background-color: #f56c6c;
        color: #fff;
        border: none;
        font-size: 30rpx;
        padding: 25rpx 0;
        border-radius: 100rpx;
        font-weight: 500;
        transition: all 0.3s;
        
        &:hover {
          background-color: #e66262;
        }
        
        &:active {
          background-color: #d75858;
        }
      }
    }
  
  /* 分页器样式 */
  .uni-pagination {
    margin-top: 30rpx;
    padding-bottom: 20rpx;
  }
</style>