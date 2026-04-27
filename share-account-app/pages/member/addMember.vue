<template>
  <view class="member-page">
    
    <view class="form-container">
      <!-- 成员名称 -->
      <view class="form-item">
        <text class="form-label">朋友名称</text>
        <input v-model="formData.name" @input="onNameInput" placeholder="朋友名字" />
      </view>
      
      

      <view class="button-container">
        <!-- 编辑模式下，如果状态不为0则显示保存按钮 -->
        <button v-if="isEditMode && status !== 0" @click="submitForm" class="share-wechat-btn" :class="{ 'btn-disabled': !isNameValid }">保存</button>
        <!-- 微信小程序专用分享按钮，带有open-type="share"属性 -->
        <!-- 这是微信小程序推荐的触发onShareAppMessage的标准方式 -->
        <button v-if="!isEditMode || status === 0" open-type="share" class="share-wechat-btn" :class="{ 'btn-disabled': !isNameValid }"> 快邀请你的朋友吧 </button>
      </view>
      <view class="tip-container">
        快与ta共享你的收入/支出吧😄
      </view>
      <view class="tip-container">
        ta就可以查看这个账本下的所有账单啦
      </view>
      
      
    </view>
    
  </view>
</template>

<script>
import request from '../../common/request.js';
import backUrl from '../../common/back_url.js';

export default {
  data() {
    return {
      isEditMode: false,
      memberId: null,
      formData: {
        name: '',
        ledgerId: ''
      },
      // 成员状态 0表示未激活/待分享，1表示已激活
      status: 0,
      // 控制分享按钮显示的标志位
      showShareButton: false,
      ledgers: [],
      selectedLedger: null,
      needRefreshLedgers: false,
      // 分享相关数据
      shareInfo: {
        title: '',
        path: '',
        imageUrl: 'https://shareaccount-1302778096.cos.ap-beijing.myqcloud.com/title.png'
      }
    };
  },
  computed: {
    // 检查名称是否有效
    isNameValid() {
      return this.formData.name && this.formData.name.trim().length > 0;
    }
  },
  onLoad(options) {
    // 检查是否为编辑模式
    if (options.id) {
      this.isEditMode = true;
      this.memberId = options.id;
    }
    // 直接接收上游传递的 ledgerId
    if (options && options.ledgerId) {
      this.formData.ledgerId = options.ledgerId;
    }

    // 编辑模式下加载成员数据（无需加载账本列表）
    if (this.isEditMode) {
      this.loadMemberData();
    }

    // #ifdef MP-WEIXIN
    uni.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage']
    });
    // #endif
    // #ifdef MP-TOUTIAO
    uni.showShareMenu({
      withShareTicket: true,
      menus: ['share']
    });
    // #endif
  },
  

  // 小程序分享生命周期函数 - 支持所有小程序平台
  // 注意：这是组件的顶层方法，不是methods的一部分
  // 这样微信小程序框架才能正确识别并调用它
  async onShareAppMessage() {
    // 验证名称是否为空
    if (!this.formData.name || this.formData.name.trim().length === 0) {
      uni.showToast({
        title: '请先输入朋友名称',
        icon: 'none'
      });
      return false; // 阻止分享
    }
    
    try {
      const memberId = await this.submitForm();
      console.log('获取到成员id:', memberId);

      // 使用prepareShareInfo方法获取分享信息
      const shareInfo = this.prepareShareInfo();
      // 添加更详细的日志以便调试
      console.log('onShareAppMessage被调用:', shareInfo);
      return shareInfo;
    } catch (error) {
      console.error('获取分享信息失败:', error);
      // 确保即使出现异常也能返回有效的分享信息
      return {
        title: `邀请你加入【${this.selectedLedger?.name || '共享账本'}】`,
        path: this.memberId ? `/pages/member/acceptInvitation?id=${this.memberId}` : '/pages/firstpage/firstpage',
        imageUrl: 'https://shareaccount-1302778096.cos.ap-beijing.myqcloud.com/title.png'
      };
    }
  },

  methods: {
    // 输入名称时的处理
    onNameInput() {
      // 触发 computed 属性重新计算
      this.$forceUpdate();
    },
    
    // 加载成员数据（编辑模式）
    async loadMemberData() {
      try {
        const res = await request.get(backUrl.endpoints.member_getById + this.memberId );
        this.formData.name = res.name;
        this.formData.percentage = res.percentage || 0;
        this.formData.ledgerId = res.ledgerId;
        // 加载成员状态
        this.status = res.status || 0;
        
        // 编辑模式下，如果状态为0，则显示分享按钮
        if (this.status === 0) {
          this.showShareButton = true;
          // 启用分享功能
          try {
            // #ifdef MP-WEIXIN
            await uni.showShareMenu({
              withShareTicket: true,
              menus: ['shareAppMessage']
            });
            // #endif
            // #ifdef MP-TOUTIAO
            await uni.showShareMenu({
              withShareTicket: true,
              menus: ['share']
            });
            // #endif
          } catch (err) {
            console.error('启用分享功能失败:', err);
          }
          // 设置分享信息
          this.shareInfo = {
            title: `邀请你加入【${this.selectedLedger?.name || '共享账本'}】`,
            path: `/pages/member/acceptInvitation?id=${this.memberId}`,
            imageUrl: 'https://shareaccount-1302778096.cos.ap-beijing.myqcloud.com/title.png'
          };
        }

      } catch (error) {
        console.error('加载成员数据失败:', error);
        uni.showToast({ title: '加载失败', icon: 'none' });
      }
    },
    
    // 加载账本列表
    async loadLedgers() {
      try {
        const res = await request.get(backUrl.endpoints.ledger_getByUser);
        this.ledgers = res || [];
        this.ledgers = this.ledgers.filter(ledger => ledger.type === 0);
      } catch (error) {
        console.error('加载账本列表失败:', error);
        uni.showToast({ title: '加载账本失败', icon: 'none' });
      }
    },
    
    

    
    // 提交表单
    async submitForm() {
      // 验证表单
      if (!this.formData.name.trim()) {
        uni.showToast({ title: '请输入成员名称', icon: 'none' });
        return;
      }
      
      if (!this.formData.ledgerId) {
        uni.showToast({ title: '请选择账本', icon: 'none' });
        return;
      }
      
      try {
        // 使用save接口保存成员信息
        const memberData = {
          name: this.formData.name,
          ledgerId: this.formData.ledgerId
        };
        
        // 编辑模式需要传入id和当前状态
        if (this.isEditMode) {
          memberData.id = this.memberId;
          memberData.status = this.status; // 保留当前状态
        }
        
        const memberRes = await request.post(backUrl.endpoints.member_save, memberData);
        
        if (!this.isEditMode) {
          // 新增模式下，成功保存后实现分享功能
          this.memberId = memberRes;
          return memberRes;
        } else {
          // 编辑模式下，显示保存成功提示并返回
          uni.showToast({ title: '保存成功', icon: 'success' });
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        }
      } catch (error) {
        console.error(this.isEditMode ? '修改成员失败' : '添加成员失败', error);
        uni.showToast({ title: this.isEditMode ? '修改失败' : '添加失败', icon: 'none' });
      }
    },
    
    // 分享到微信的辅助方法（被onShareAppMessage使用）
    prepareShareInfo() {
      // 设置分享信息
      return {
        title: `邀请你加入【${this.selectedLedger?.name || '共享账本'}】`,
        path: `/pages/member/acceptInvitation?id=${this.memberId}`,
        imageUrl: 'https://shareaccount-1302778096.cos.ap-beijing.myqcloud.com/title.png'
      };
    },
    
    // 复制分享链接的辅助方法
    copyShareLink() {
      try {
        // 构建完整的分享链接
        const shareInfo = this.prepareShareInfo();
        // 使用当前页面信息构建完整URL
        const fullShareUrl = `${location.origin}${location.pathname}#${shareInfo.path}`;
        
        uni.setClipboardData({
          data: fullShareUrl,
          success: () => {
            uni.showModal({
              title: '链接已复制',
              content: '请打开微信手动粘贴分享给好友',
              showCancel: false,
              success: () => {
                // 保持在当前页面，不要立即返回，让用户可以继续操作
                console.log('链接复制成功，保持在当前页面');
              }
            });
          },
          fail: (err) => {
            console.error('复制链接失败:', err);
            uni.showToast({ title: '复制失败，请手动保存链接', icon: 'none' });
          }
        });
      } catch (error) {
        console.error('生成分享链接失败:', error);
        uni.showToast({ title: '生成分享链接失败', icon: 'none' });
      }
    }
    
  }
};
</script>

<style scoped>
  .member-page {
    padding: 30rpx;
    background-color: #f8f9fa;
    min-height: 100vh;
  }
  
  .form-container {
    background-color: #fff;
    border-radius: 20rpx;
    padding: 40rpx;
    margin-bottom: 30rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  }
  
  .form-item {
    margin-bottom: 40rpx;
    position: relative;
  }
  
  .form-label {
    display: block;
    font-size: 30rpx;
    color: #333;
    margin-bottom: 15rpx;
    font-weight: 500;
  }
  
  .form-item input {
    width: 100%;
    height: 90rpx;
    border: 2rpx solid #e5e5ea;
    border-radius: 12rpx;
    padding: 0 30rpx;
    font-size: 30rpx;
    background-color: #fafafa;
    transition: all 0.3s ease;
    box-sizing: border-box;
  }
  
  .form-item input:focus {
    border-color: #007AFF;
    background-color: #fff;
    box-shadow: 0 0 0 15rpx rgba(0, 122, 255, 0.05);
  }
  
  .form-item input::placeholder {
    color: #999;
    font-size: 28rpx;
  }
  
  .button-container {
    padding: 30rpx;
    background-color: #fff;
    border-radius: 20rpx;
    display: flex;
    justify-content: center;
  }
  
  .share-wechat-btn {
    background-color: #1989fa;
    color: #fff;
    font-size: 32rpx;
    line-height: 90rpx;
    border-radius: 45rpx;
    border: none;
    font-weight: 500;
    box-shadow: 0 4rpx 15rpx rgba(7, 193, 96, 0.3);
    width: 100%;
    transition: all 0.3s ease;
  }
  
  .share-wechat-btn.btn-disabled {
    background-color: #cccccc;
    box-shadow: none;
    opacity: 0.7;
  }
  
  .share-wechat-btn.btn-disabled:active {
    transform: none;
  }
  
  .share-button {
    background-color: #07C160;
    color: white;
    font-size: 32rpx;
    font-weight: 500;
    margin-top: 40rpx;
    padding: 20rpx;
    border-radius: 10rpx;
    width: 100%;
  }
  
  .share-wechat-btn:active {
    opacity: 0.9;
    box-shadow: 0 2rpx 8rpx rgba(7, 193, 96, 0.2);
  }
  
  .tip-container {
    text-align: center;
    font-size: 28rpx;
    color: #999;
    padding: 30rpx 40rpx;
    line-height: 1.5;
  }
</style>