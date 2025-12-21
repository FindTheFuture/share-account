<template>
  <!-- 页面根容器 -->
  <view class="add-bill-container">
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" contenttext="正在加载分类信息..."></uni-load-more>
    </view>
    
    <!-- 分类选择组件（替换原分段器+子分类列表） -->
    <category-select
      ref="categorySelect"
      :selectedCategory="selectedCategory"
      :isEditMode="!!billId"
      @select="onCategorySelected"
    />

    <!-- 输入区域整体容器 - 包含标签行、日期组件和计算器输入框 -->
    <view class="input-section-container">
      <!-- 备注输入框 -->
      <view class="memo-input-wrapper">
        <input 
          class="memo-input" 
          type="text" 
          v-model="billMemo" 
          placeholder="描述下账单"
          maxlength="100"
        />
        <text class="memo-input-count">{{ billMemo.length }}/100</text>
      </view>

      <!-- 可拖动标签区域 -->
      <view class="tags-container">
        <scroll-view scroll-x="true" class="tags-scroll-view" show-scrollbar="false">
          <view class="tags-wrapper">
            <!-- AI记账标签 -->
            <view class="tag-item ai-tag" @click="showAIChatPopup">
              <text class="tag-text">AI记账</text>
            </view>
            
            <!-- AI记账标签 -->
            <view class="tag-item ai-tag" @click="chooseImage">
              <text class="tag-text">AI识图</text>
            </view>
            
            <!-- 账本标签 -->
            <view class="tag-item ledger-tag" @click="showLedgerPopup">
              <text class="tag-text">{{ selectedLedger ? selectedLedger.name : '选择账本' }}</text>
            </view>
            
            <!-- 账户标签 -->
            <view class="tag-item account-tag" @click="showAccountPopup">
              <text class="tag-text">{{ selectedAccount ? selectedAccount.name : '可选账户' }}</text>
            </view>
            
            <!-- 计入预算标签 -->
            <view class="tag-item budget-tag" :class="{'budget-disabled': !isBudget}" @click="toggleBudget">
              <text class="tag-text">{{ isBudget ? '计入预算' : '不计入预算' }}</text>
            </view>
            
          </view>
        </scroll-view>
      </view>
      
      <!-- 日期选择器组件 - 紧贴标签行下方 -->
      <view class="date-picker-wrapper">
        <uni-datetime-picker
          ref="dateTimePicker"
          type="datetime"
          v-model="datePickerValue"
          :start="datePickerStart"
          :end="datePickerEnd"
          @change="onDateTimeChange"
        ></uni-datetime-picker>
      </view>
      
      <!-- 计算器输入框 -->
      <view class="calculator-container-wrapper">
        <view class="calculator-container">
          <input 
            class="calculator-input" 
            type="text" 
            v-model="calculatorExpression"
            @input="onCalculatorInput"
            @focus="onInputFocus"
            placeholder="金额/计算公式"
          />
          <view class="calculator-result">
            = {{ calculatorResult !== null ? calculatorResult : '' }}
          </view>
          <button class="save-button" @tap="submitBill">保存</button>
          <button v-if="!billId" class="continue-button" @tap="submitBillAndContinue">
            <text class="continue-line">再记</text>
            <text class="continue-line">一笔</text>
          </button>
        </view>
      </view>
    </view>

    <!-- 账本列表弹出层（组件） -->
    <ledger-select-popup 
      ref="ledgerPopup"
      :autoSelectDefault="true"
      :selectedLedger="selectedLedger"
      @select="onLedgerSelected"
    />
    
    <!-- 账户列表弹出层（组件） -->
    <account-select-popup 
      ref="accountPopup"
      :autoSelectDefault="true"
      :selectedAccount="selectedAccount"
      @select="onAccountSelected"
    />
    
    <!-- AI记账弹窗 -->
    <uni-popup ref="aiChatPopup" type="center" :mask-click="false" :mask="true" class="ai-chat-popup">
      <view class="ai-chat-popup-content">
        <view class="ai-chat-popup-header">
          <text class="ai-chat-popup-title">AI记账</text>
        </view>
        <view class="ai-chat-popup-body">
          <textarea 
            class="ai-chat-textarea" 
            v-model="aiChatContent" 
            placeholder="例如：火锅189、羊肉串100、送礼500"
            maxlength="200"
            :show-confirm-bar="false"
            auto-height
          ></textarea>
          <text class="ai-chat-textarea-count">{{ aiChatContent.length }}/200</text>
        </view>
        <view class="ai-chat-popup-footer">
          <button class="ai-chat-cancel-btn" @click="closeAIChatPopup">取消</button>
          <button class="ai-chat-confirm-btn" @click="confirmAIChat">确定</button>
        </view>
      </view>
    </uni-popup>
    
    <!-- AI识别图片缩略图悬浮窗 -->
    <view class="image-preview-container" v-if="imageUrl"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove"
      :style="{ left: imagePosition.x + 'px', top: imagePosition.y + 'px' }">
      <image :src="imageUrl" mode="aspectFit" class="image-preview" @click="previewImage"></image>
      <view class="close-preview" @click.stop="clearImage">✕</view>
      <view class="retry-button" v-if="aiRecognitionError" @click.stop="retryRecognition">
        重试
      </view>
    </view>
    
    <!-- 自定义可爱弹窗组件 -->
    <custom-cute-modal
      ref="cuteModal"
      :title="'提示'"
      :content="recognitionResult.otherMemo"
      :confirm-text="'好滴'"
      :icon-type="'kongxincai'"
      :icon-color="'#ff6b81'"
      @confirm="handleModalConfirm"
    />
  </view>
</template>

<script>
  import request from '@/common/request.js';
  import backUrl from '@/common/back_url.js';
  import { uploadFile } from '@/common/upload.js';
  import UniSegmentedControl from '@/uni_modules/uni-segmented-control/components/uni-segmented-control/uni-segmented-control.vue';
  import UniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue';
  import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
  import UniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
  import UniGrid from '@/uni_modules/uni-grid/components/uni-grid/uni-grid.vue';
  import UniGridItem from '@/uni_modules/uni-grid/components/uni-grid-item/uni-grid-item.vue';
  import UniDateTimePicker from '@/uni_modules/uni-datetime-picker/components/uni-datetime-picker/uni-datetime-picker.vue';
  import AccountSelectPopup from '@/components/account-select-popup.vue';
  import LedgerSelectPopup from '@/components/ledger-select-popup.vue';
  import CategorySelect from '@/components/category-select.vue';
  import CustomCuteModal from '@/components/custom-cute-modal.vue';
  
  export default {
    components: {
      UniSegmentedControl,
      UniLoadMore,
      UniIcons,
      UniPopup,
      UniGrid,
      UniGridItem,
      UniDateTimePicker,
      AccountSelectPopup,
      LedgerSelectPopup,
      CategorySelect,
      CustomCuteModal
    },
    data() {
      const now = new Date();
      const formattedDate = `${now.getMonth() + 1}月${now.getDate()}日`;
      const datePickerValue = this.formatDateForPicker(now);
      const datePickerStart = this.formatDateForPicker(new Date(now.getFullYear() - 10, now.getMonth(), now.getDate()));
      const datePickerEnd = this.formatDateForPicker(new Date(now.getFullYear() + 1, now.getMonth(), now.getDate()));
      
      return {
        selectedCategory: null,
        currentSubCategory: null,
        
        // 分类数据
        categoryTree: [],
        topLevelCategories: [],
        currentTopCategoryIndex: 0,
        currentSubCategories: [],
        currentSubSubCategories: [],
        loading: false,
        
        // 计算器相关
        calculatorExpression: '',
        calculatorResult: null,
        
        // 标签相关
        selectedDate: now,
        formattedDate: formattedDate,
        
        // 日期选择器相关
        datePickerValue: datePickerValue,
        datePickerStart: datePickerStart,
        datePickerEnd: datePickerEnd,
        
        // 账本相关
        selectedLedger: null,
        billId: null, // 当前编辑的账单ID
        
        // 预算状态，默认为计入预算
        isBudget: true,
        
        // 账户相关
        selectedAccount: null,
        
        // 备注相关
        billMemo: '',
        
        // AI记账相关
        recognitionResult: {
          merchant: '',
          amount: '',
          date: '',
          time: '',
          category: '',
          categoryId: '',
          otherMemo: '',
          showOtherMemo: false
        },
        imageUrl: '',
        uploadedPictureId: null, // 保存已上传的图片ID，用于重试识别
        uploading: false,
        recognizing: false,
        aiRecognitionError: '',
        
        // AI记账文本输入相关
        aiChatContent: '',
        aiChatPopupVisible: false,
        
        // 图片预览拖动相关
        imagePosition: {
          x: 0,  // 将在页面加载时动态设置
          y: 0   // 将在页面加载时动态设置
        },
        touchStartX: 0,
        touchStartY: 0
      }
    },
    computed: {
      currentTopCategory() {
        return this.topLevelCategories[this.currentTopCategoryIndex] || null;
      }
    },
    onLoad(options) {
      // 页面初次渲染后，组件内部进行默认账户自动选择（不打开弹窗）
      this.$nextTick(() => {
        if (this.$refs.ledgerPopup && this.$refs.ledgerPopup.initAutoSelect) {
          this.$refs.ledgerPopup.initAutoSelect();
        }
        if (this.$refs.accountPopup && this.$refs.accountPopup.initAutoSelect) {
          this.$refs.accountPopup.initAutoSelect();
        }
      });
      
      // 检查是否有billId参数（编辑模式）
      if (options && options.billId) {
        this.billId = options.billId;
        // 先初始化页面数据（加载分类），然后在分类数据加载完成后再加载账单详情
        this.initPage().then(() => {
          this.loadBillDetail();
        });
      } else {
        // 非编辑模式直接初始化页面
        this.initPage();
      }
    },
    
    onShow() {
      // 设置图片预览容器的默认位置为页面最右侧中部
      this.setDefaultImagePosition();
      
      // 组件内一次性刷新账本列表（返回自账本页面）
      if (this.$refs.ledgerPopup && this.$refs.ledgerPopup.refreshIfNeeded) {
        this.$refs.ledgerPopup.refreshIfNeeded();
      }
      // 组件内一次性刷新账户列表（返回自账户页面）
      if (this.$refs.accountPopup && this.$refs.accountPopup.refreshIfNeeded) {
        this.$refs.accountPopup.refreshIfNeeded();
      }
      // 分类组件返回后刷新（返回自分类页面）
      if (this.$refs.categorySelect && this.$refs.categorySelect.refreshIfNeeded) {
        this.$refs.categorySelect.refreshIfNeeded();
      }
    },
    
    onUnload() {
    },
    methods: {
      // 设置图片预览容器的默认位置为页面最右侧中部
      setDefaultImagePosition() {
        // 获取系统信息
        const systemInfo = uni.getSystemInfoSync();
        const windowWidth = systemInfo.windowWidth;
        const windowHeight = systemInfo.windowHeight;
        
        // 计算最右侧中部的位置：距离右侧10px，垂直居中
        this.imagePosition = {
          x: 0,
          y: (windowHeight - 20) / 2  // 假设容器高度也约为150px
        };
      },
      
      // AI记账相关方法
      async chooseImage() {
        try {
          // 先检查AI识别剩余次数
          uni.showLoading({ title: '加载中' });
          const remainingCountRes = await this.$request({
            url: backUrl.endpoints.userMember_getRemainingAiCount,
            method: 'GET',
          });
          uni.hideLoading();
          
          // 检查返回结果
          if (remainingCountRes.code && remainingCountRes.code != 200) {
            console.log('获取AI识别剩余次数失败:', remainingCountRes.message);
            uni.showToast({ 
              title: '获取 AI剩余次数失败', 
              icon: 'none' 
            });
            return;
          }
          
          const remainingAiCount = remainingCountRes || 0;
          
          // 如果剩余次数小于等于0，提示用户并跳转
          if (remainingAiCount <= 0) {
            uni.showModal({
              title: 'AI识别次数已用完',
              content: '您的AI识别次数已用完，请订阅会员获取更多次数',
              showCancel: true,
              success: (res) => {
                if (res.confirm) {
                  // 跳转到会员页面
                  uni.navigateTo({
                    url: '/pages/vipmember/vipmember'
                  });
                }
              }
            });
            return;
          }
          
          // 剩余次数足够，继续执行原逻辑
          const that = this;
          uni.chooseImage({
            count: 1,
            sizeType: ['compressed'],
            sourceType: ['album', 'camera'],
            success(res) {
              that.aiRecognitionError = '';
              const tempFilePath = res.tempFilePaths[0];
              
              // 显示图片缩略图
              that.imageUrl = tempFilePath;
              
              // 上传图片并识别
              that.uploadAndRecognize(tempFilePath);
            }
          });
        } catch (error) {
          uni.hideLoading();
          console.error('检查AI识别剩余次数失败:', error);
          uni.showToast({ 
            title: '检查失败，请重试', 
            icon: 'none' 
          });
        }
      },
      
      // 统一的AI识别接口调用方法
      async callAIRecognizeBill(pictureId) {
        // 使用uni.showLoading显示加载状态
        uni.showLoading({
          title: '识别中',
          mask: true
        });
        try {
          // 调用AI识别接口，只传递图片ID
          const recognizeRes = await this.$request({
            url: backUrl.endpoints.bill_ai_recognizeBillPic,
            method: 'POST',
            data: {
              pictureId: pictureId
            }
          });

          // 隐藏加载状态
          uni.hideLoading();

          // 适配新的响应格式
          if (recognizeRes && recognizeRes.bill) {
            this.parseRecognitionResult(recognizeRes.bill);
            this.aiRecognitionError = ''; // 清除错误信息，这样重试按钮就不会显示了
            return true;
          } else {this.aiRecognitionError = recognizeRes?.message || '识别失败，请重试';
            uni.showToast({ title: this.aiRecognitionError, icon: 'none' });
            return false;
          }
        } catch (error) {
          // 隐藏加载状态
          uni.hideLoading();
          
          console.error('AI识别错误:', error);
          this.aiRecognitionError = '识别过程中出错，请重试';
          uni.showToast({ title: this.aiRecognitionError, icon: 'none' });
          return false;
        }
      },
      
      // 文本形式AI记账识别方法
      async callAIRecognizeBillChat(chatContent) {
        // 使用uni.showLoading显示加载状态
        uni.showLoading({
          title: '识别中',
          mask: true
        });
        try {
          // 调用AI识别接口，传递文本内容
          const recognizeRes = await this.$request({
            url: backUrl.endpoints.bill_ai_recognizeBillChat,
            method: 'POST',
            data: {
              chat: chatContent
            }
          });

          // 隐藏加载状态
          uni.hideLoading();

          // 适配响应格式，与图片识别保持一致
          if (recognizeRes && recognizeRes.bill) {
            this.parseRecognitionResult(recognizeRes.bill);
            this.aiRecognitionError = '';
            return true;
          } else {
            const errorMsg = recognizeRes?.message || '识别失败，请重试';
            uni.showToast({ title: errorMsg, icon: 'none' });
            return false;
          }
        } catch (error) {
          // 隐藏加载状态
          uni.hideLoading();
          
          console.error('AI文本识别错误:', error);
          uni.showToast({ title: '识别过程中出错，请重试', icon: 'none' });
          return false;
        }
      },
      
      async uploadAndRecognize(filePath) {
        // 使用uni.showLoading显示加载状态
        uni.showLoading({
          title: '上传中',
          mask: true
        });
        
        try {
          // 使用upload.js的uploadFile函数上传图片
          const uploadRes = await uploadFile({
            filePath,
            fileType: 301, // 图片类型
            pathType: 0, // 存储路径类型
            path: '/bill/ai_recognition/',
            objectId: 0 // 临时对象ID
          });
          
          // 保存上传的图片ID，用于重试识别
          this.uploadedPictureId = uploadRes.id;
          
          // 使用统一的AI识别方法
          await this.callAIRecognizeBill(uploadRes.id);
        } catch (error) {
          // 隐藏加载状态
          uni.hideLoading();
          
          console.error('上传并识别错误:', error);
          this.aiRecognitionError = '识别过程中出错，请重试';
          uni.showToast({ title: this.aiRecognitionError, icon: 'none' });
        }
      },
      
      parseRecognitionResult(billData) {
        // 保存识别结果
        this.recognitionResult = {
          merchant: '',
          amount: billData.price ? (billData.price / 100).toString() : '',
          date: '',
          time: '',
          category: '',
          categoryId: billData.classId || '',
          memo: billData.memo || '',
          otherMemo: billData.otherMemo || '',
          showOtherMemo: billData.showOtherMemo || false
        };
        
        // 自动填充表单
        // 1. 填充金额（从price字段获取，需要除以100转换为元）
        if (billData.price) {
          const amount = billData.price / 100;
          this.calculatorExpression = amount.toString();
          this.calculateResult();
        }
        
        // 2. 设置分类（如果识别出分类ID）
        if (billData.classId) {
          this.setCategoryByClassId(billData.classId);
        }
        
        // 3. 设置日期（如果有billTime）
        if (billData.billTime) {
          try {
            const date = new Date(billData.billTime);
            if (!isNaN(date.getTime())) {
              this.selectedDate = date;
              this.datePickerValue = this.formatDateForPicker(date);
              const month = date.getMonth() + 1;
              const day = date.getDate();
              this.formattedDate = `${month}月${day}日`;
            }
          } catch (e) {
            console.error('日期格式错误:', e);
          }
        }
        
        // 4. 设置备注（直接使用后端返回的memo）
        if (billData.memo) {
          this.billMemo = billData.memo;
        }
        
        // 5. 如果showOtherMemo为true且otherMemo有内容，以弹窗方式显示以确保长文本完整显示
        if (billData.showOtherMemo && billData.otherMemo) {
          // 使用自定义可爱弹窗替代原生弹窗
          this.recognitionResult.otherMemo = billData.otherMemo;
          this.$refs.cuteModal.show();
        } else {
          uni.showToast({ title: '识别成功', icon: 'success' });
        }
      },
      
      // 处理图片拖动开始
      handleTouchStart(e) {
        const touch = e.touches[0];
        this.touchStartX = touch.clientX - this.imagePosition.x;
        this.touchStartY = touch.clientY - this.imagePosition.y;
      },
      
      // 处理自定义弹窗确认
      handleModalConfirm() {
        // 弹窗确认后的回调处理
      },
      
      // 处理图片拖动移动
      handleTouchMove(e) {
        const touch = e.touches[0];
        const newX = touch.clientX - this.touchStartX;
        const newY = touch.clientY - this.touchStartY;
        
        // 获取窗口尺寸，确保图片不会被拖出屏幕过多
        const windowWidth = uni.getSystemInfoSync().windowWidth;
        const windowHeight = uni.getSystemInfoSync().windowHeight;
        const imgWidth = 200 * 0.533; // 转换rpx为px，假设1rpx ≈ 0.533px
        const imgHeight = 200 * 0.533;
        
        // 限制拖动范围，确保图片始终在屏幕范围内可见
        const clampedX = Math.max(0, Math.min(windowWidth - imgWidth, newX));
        const clampedY = Math.max(0, Math.min(windowHeight - imgHeight, newY));
        
        this.imagePosition.x = clampedX;
        this.imagePosition.y = clampedY;
      },
      
      // AI记账弹窗相关方法
      showAIChatPopup() {
        // 关闭其他可能打开的弹窗，实现互斥逻辑
        // 关闭账本弹窗
        if (this.$refs.ledgerPopup) {
          this.$refs.ledgerPopup.close();
        }
        // 关闭账户弹窗
        if (this.$refs.accountPopup) {
          this.$refs.accountPopup.close();
        }
        // 关闭备注弹窗
        if (this.$refs.memoPopup) {
          this.$refs.memoPopup.close();
        }
        // 关闭子分类弹窗
        if (this.$refs.subCategoryPopup) {
          this.$refs.subCategoryPopup.close();
        }
        // 关闭分类选择组件的弹窗
        if (this.$refs.categorySelect && this.$refs.categorySelect.closePopup) {
          this.$refs.categorySelect.closePopup();
        }
        
        // 先清空之前的输入内容
        //this.aiChatContent = '';
        // 打开AI记账弹窗
        this.$refs.aiChatPopup.open();
      },
      
      closeAIChatPopup() {
        this.$refs.aiChatPopup.close();
      },
      
      async confirmAIChat() {
        // 验证输入内容
        if (!this.aiChatContent.trim()) {
          uni.showToast({ title: '请输入账单信息', icon: 'none' });
          return;
        }
        
        // 关闭弹窗
        this.closeAIChatPopup();
        
        // 调用AI识别方法
        await this.callAIRecognizeBillChat(this.aiChatContent);
      },
      
      // 预览图片
      previewImage() {
        uni.previewImage({
          urls: [this.imageUrl],
          current: this.imageUrl,
          showmenu: true
        });
      },
      
      clearImage() {
        this.imageUrl = '';
        this.uploadedPictureId = null; // 清除已上传的图片ID
        this.aiRecognitionError = '';
        this.recognitionResult = {
          merchant: '',
          amount: '',
          date: '',
          time: '',
          category: '',
          categoryId: ''
        };
      },
      
      async retryRecognition() {
        try {
          // 显示加载状态
          uni.showLoading({
            title: '识别中',
            mask: true
          });
          this.aiRecognitionError = '';
          
          // 直接使用已上传的图片ID重试识别，避免重复上传
          if (this.uploadedPictureId) {
            // 使用统一的AI识别方法
            await this.callAIRecognizeBill(this.uploadedPictureId);
          } else if (this.imageUrl) {
            // 回退方案：如果没有保存的图片ID，仍然使用原有的重试逻辑
              await this.uploadAndRecognize(this.imageUrl);
          }
        } catch (error) {
          console.error('重试识别错误:', error);
          this.aiRecognitionError = '重试过程中出错，请重试';
          uni.showToast({ title: this.aiRecognitionError, icon: 'none' });
        } finally {
          // 隐藏加载状态
          uni.hideLoading();
        }
      },
      
      // 公共：保存处理
      handleSave(stayOnPage) {
        // 验证是否选择了子分类
        if (!this.selectedCategory) {
          uni.showToast({ title: '请选择分类', icon: 'none' });
          return;
        }
        // 验证是否选择了账本
        if (!this.selectedLedger) {
          uni.showToast({ title: '请选择账本', icon: 'none' });
          return;
          }
          // 验证是否有金额结果
          if (this.calculatorResult === null || this.calculatorResult <= 0) {
          uni.showToast({ title: '请输入有效的金额', icon: 'none' });
          return;
        }
        const roundedAmount = parseFloat(Number(this.calculatorResult).toFixed(2));
        const billData = {
          ledgerId: this.selectedLedger?.id,
          classId: this.selectedCategory.originalId,
          price: Math.round(roundedAmount * 100),
          isBudget: this.isBudget ? 0 : 1,
          status: 0,
          memo: this.billMemo
        };
        if (this.selectedAccount) {
          billData.accountId = this.selectedAccount.id;
        }
        if (this.selectedDate) {
          billData.billTimestamp = this.selectedDate.getTime();
        } else {
          billData.billTimestamp = new Date().getTime();
        }
        if (this.billId) {
          billData.id = this.billId;
        }
        uni.showLoading({ title: this.billId ? '更新中...' : '保存中...' });
        this.$request({
          url: this.$backUrlConfig.endpoints.bill_save,
          method: 'POST',
          data: billData
        }).then(res => {
          uni.hideLoading();
          if (res) {
            uni.showToast({ title: this.billId ? '更新成功' : '保存成功', icon: 'success' });
            if (stayOnPage) {
              this.calculatorExpression = '';
              this.calculatorResult = null;
              this.billMemo = '';
            } else {
              setTimeout(() => { uni.navigateBack(); }, 1500);
            }
          } else {
            uni.showToast({ title: this.billId ? '更新失败，请重试' : '保存失败，请重试', icon: 'none' });
          }
        }).catch(error => {
          console.error(this.billId ? '更新账单失败:' : '保存账单失败:', error);
          uni.hideLoading();
          uni.showToast({ title: '网络异常，请重试', icon: 'none' });
        });
      },

      // 根据账户类型获取样式类
      getAccountTypeClass(type) {
        switch(type) {
          case 0: return 'type-savings'; // 储蓄账户
          case 1: return 'type-credit';  // 信贷账户
          case 2: return 'type-recharge';// 充值账户
          case 3: return 'type-invest';  // 投资理财
          default: return '';
        }
      },
      
      // 初始化页面数据
      async initPage() {
        this.loading = true;
        try {
          // 从后台查询所有分类
          const res = await this.$request({
            url: this.$backUrlConfig.endpoints.class_getAll,
            method: 'GET'
          });
          
          if (Array.isArray(res)) {
            // 处理分类数据
            this.categoryTree = this.convertIdsToString(res);
            // 正确过滤出真正的顶级分类（parentId为null的分类）
            this.topLevelCategories = this.categoryTree.filter(cat => cat.parentId === null);
            
            // 默认显示第一个分类的子分类
            if (this.topLevelCategories.length > 0) {
              this.loadCurrentSubCategories();
            }
          } else {
            uni.showToast({ title: '获取分类失败', icon: 'none' });
          }
        } catch (error) {
          console.error('初始化页面异常:', error);
          uni.showToast({ title: '网络请求异常', icon: 'none' });
        } finally {
          this.loading = false;
        }
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
      
      // 加载当前选中顶级分类的子分类
      loadCurrentSubCategories() {
        if (!this.currentTopCategory) return;
        
        // 查找当前顶级分类的所有子分类
        const categoryNode = this.findCategoryNode(this.categoryTree, this.currentTopCategory.idStr);
        this.currentSubCategories = categoryNode?.childClassList || [];
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
      
      // 格式化日期为picker组件需要的格式
      formatDateForPicker(date) {
        const year = date.getFullYear();
        const month = date.getMonth() + 1;
        const day = date.getDate();
        const hour = date.getHours();
        const minute = date.getMinutes();
        const second = date.getSeconds();
        
        return `${year}-${this.padZero(month)}-${this.padZero(day)} ${this.padZero(hour)}:${this.padZero(minute)}:${this.padZero(second)}`;
      },
      
      // 数字补零
      padZero(num) {
        return num < 10 ? `0${num}` : num;
      },
      
      // 显示日期选择器

      
      // 显示账本弹出层
      showLedgerPopup() {
        // 关闭所有其他弹窗
        if (this.$refs.accountPopup) {
          this.$refs.accountPopup.close();
        }
        if (this.$refs.memoPopup) {
          this.$refs.memoPopup.close();
        }
        // 关闭分类组件的弹窗（保留关闭弹窗功能）
        if (this.$refs.categorySelect && this.$refs.categorySelect.closePopup) {
          this.$refs.categorySelect.closePopup();
        }
        if (this.$refs.ledgerPopup && this.$refs.ledgerPopup.open) {
          this.$refs.ledgerPopup.open();
        }
      },
      
      // 关闭账本弹出层
      closeLedgerPopup() {
        this.$refs.ledgerPopup.close();
      },
      
      // 切换预算状态
      toggleBudget() {
        this.isBudget = !this.isBudget;
      },
      
      // 选择账本（来自组件事件）
      onLedgerSelected(ledger) {
        this.selectedLedger = ledger || null;
        this.closeLedgerPopup();
      },
      
      // 跳转到账本列表页面
      navigateToLedgerList() {
        this.closeLedgerPopup();
        // 设置标记，当从账本页面返回时需要刷新数据
        this.needRefreshLedgers = true;
        uni.navigateTo({
          url: '/pages/ledger/ledger' // 假设的页面路径，需要替换为实际路径
        });
      },
      
      // 显示账户弹出层
      showAccountPopup() {
        // 关闭所有其他弹窗
        if (this.$refs.ledgerPopup) {
          this.$refs.ledgerPopup.close();
        }
        if (this.$refs.memoPopup) {
          this.$refs.memoPopup.close();
        }
        // 关闭分类组件的弹窗（保留关闭弹窗功能）
        if (this.$refs.categorySelect && this.$refs.categorySelect.closePopup) {
          this.$refs.categorySelect.closePopup();
        }
        if (this.$refs.accountPopup && this.$refs.accountPopup.open) {
          this.$refs.accountPopup.open();
        }
      },
      
      // 关闭账户弹出层
      closeAccountPopup() {
        this.$refs.accountPopup.close();
      },
      
      // 组件选择事件回调
      onAccountSelected(account) {
        this.selectedAccount = account || null;
      },
      // 分类组件选择事件回调
      onCategorySelected(category) {
        this.selectedCategory = category || null;
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
          this.loadCurrentSubCategories();
          // 切换分类时清除已选的子分类
          this.selectedCategory = null;
        }
      },
      
      // 显示子分类弹出层
      showSubCategoryPopup(subCategory) {
        this.currentSubCategory = subCategory;
        this.currentSubSubCategories = subCategory.childClassList || [];
        // 关闭所有其他弹窗
        if (this.$refs.ledgerPopup) {
          this.$refs.ledgerPopup.close();
        }
        if (this.$refs.accountPopup) {
          this.$refs.accountPopup.close();
        }
        if (this.$refs.memoPopup) {
          this.$refs.memoPopup.close();
        }
        this.$refs.subCategoryPopup.open();
      },
      
      // 关闭子分类弹出层（迁移到分类组件）
      closeSubCategoryPopup() {
        if (this.$refs.categorySelect && this.$refs.categorySelect.closePopup) {
          this.$refs.categorySelect.closePopup();
        }
      },
      
      // 选择子分类
      selectSubSubCategory(subSubCategory) {
        this.selectedCategory = subSubCategory;
        this.closeSubCategoryPopup();
      },
      
      // 判断子分类是否被选中
      isSubCategorySelected(subCategory) {
        if (!this.selectedCategory) return false;
        
        // 检查当前子分类是否包含被选中的子子分类
        if (subCategory.childClassList) {
          return subCategory.childClassList.some(child => 
            child.idStr === this.selectedCategory.idStr
          );
        }
        
        return false;
      },
      
      // 判断子子分类是否被选中
      isSubSubCategorySelected(subSubCategory) {
        if (!this.selectedCategory) return false;
        
        return subSubCategory.idStr === this.selectedCategory.idStr;
      },
      
      // 跳转到分类设置页面
      navigateToClassSettings() {
        uni.navigateTo({
          url: '/pages/class/class'
        });
      },
      
      // 输入框获得焦点时的处理
      onInputFocus() {
        // 日期选择器始终显示，无需在输入法弹出时隐藏
      },
      
      // 计算器输入处理
      onCalculatorInput(e) {
        let value = e.target.value;
        
        // 只允许输入纯数字、小数点、加号、减号、乘号、除号、英文的左右括号
        // 修复正则表达式，确保特殊字符被正确转义
        const filteredValue = value.replace(/[^0-9.\+\-\*\/\(\)]/g, '');
        
        if (filteredValue !== this.calculatorExpression) {
          this.calculatorExpression = filteredValue;
        }
        
        // 尝试计算结果
        this.calculateResult();
      },
      

      
      showDatePicker() {
        // 先隐藏可能存在的其他弹出层
        if (this.$refs.categorySelect && this.$refs.categorySelect.closePopup) {
          this.$refs.categorySelect.closePopup();
        }
        this.closeLedgerPopup();
        this.closeAccountPopup();
        
        // 使用setTimeout确保组件已经渲染完成再调用show方法
        setTimeout(() => {
          this.$refs.dateTimePicker && this.$refs.dateTimePicker.show();
        }, 50);
      },
      
      onDateTimeChange(e) {
        try {
          // 添加适当的null/undefined检查
          let dateValue;
          if (e && e.detail && e.detail.value) {
            dateValue = e.detail.value;
          } else if (e && typeof e === 'string') {
            // 处理可能的直接传入日期字符串的情况
            dateValue = e;
          } else {
            // 默认使用当前时间
            dateValue = this.formatDateForPicker(new Date());
          }
          
          const selectedDate = new Date(dateValue);
          this.datePickerValue = dateValue;
          this.formattedDate = `${selectedDate.getMonth() + 1}月${selectedDate.getDate()}日`;
          
          // 格式化完整时间（年月日时分秒）
          const fullTimeFormat = `${selectedDate.getFullYear()}年${selectedDate.getMonth() + 1}月${selectedDate.getDate()}日 ${selectedDate.getHours()}:${selectedDate.getMinutes()}:${selectedDate.getSeconds()}`;
          
          // 更新组件的selectedDate属性，确保保存时使用用户选择的时间
          this.selectedDate = selectedDate;
        } catch (error) {
          console.error('日期处理错误:', error);
          // 使用当前时间作为备选
          const now = new Date();
          this.selectedDate = now;
          this.datePickerValue = this.formatDateForPicker(now);
          this.formattedDate = `${now.getMonth() + 1}月${now.getDate()}日`;
        }
      },
      
      // 计算表达式结果
      calculateResult() {
        // 即使表达式为空，也初始化结果
        this.calculatorResult = null;
        
        if (!this.calculatorExpression.trim()) {
          return;
        }
        
        try {
          // 1. 严格的格式验证
          // 确保表达式包含至少一个数字
          if (!/\d/.test(this.calculatorExpression)) {
            return;
          }
          
          // 2. 检查括号是否匹配
          const openBrackets = (this.calculatorExpression.match(/\(/g) || []).length;
          const closeBrackets = (this.calculatorExpression.match(/\)/g) || []).length;
          if (openBrackets !== closeBrackets) {
            return;
          }
          
          // 3. 安全过滤
          const safeExpression = this.calculatorExpression.replace(/[^0-9.\+\-\*\/\(\)]/g, '');
          if (!safeExpression || !/\d/.test(safeExpression)) {
            return;
          }
          
          // 4. 计算表达式结果
          let result;
          try {
            // 使用更强大的表达式计算器，支持复杂表达式计算
            result = this.evaluateExpression(safeExpression);
          } catch (e) {
            console.log('表达式计算失败:', e);
            return;
          }
          
          // 5. 检查结果是否为有效数字
          if (typeof result === 'number' && !isNaN(result) && isFinite(result)) {
            // 保留适当的小数位数，避免显示过长的小数
            // 移除末尾多余的0
            this.calculatorResult = parseFloat(result.toFixed(10));
          }
        } catch (error) {
          // 表达式无效时保持结果为null，但不阻止界面更新
          console.log('计算表达式时出错:', error);
        }
      },
      
      // 完整的表达式计算器，支持复杂表达式、括号和运算符优先级
      evaluateExpression(expression) {
        // 使用中缀表达式转后缀表达式（逆波兰表示法）的方法来计算
        const tokens = this.tokenize(expression);
        const postfix = this.infixToPostfix(tokens);
        return this.calculatePostfix(postfix);
      },
      
      // 将表达式拆分为标记（数字和运算符）
      tokenize(expression) {
        const tokens = [];
        let number = '';
        
        for (let i = 0; i < expression.length; i++) {
          const char = expression[i];
          
          // 如果是数字或小数点，累积数字
          if (/\d/.test(char) || char === '.') {
            number += char;
          } else {
            // 如果已经累积了数字，将其添加到标记列表
            if (number) {
              tokens.push(parseFloat(number));
              number = '';
            }
            
            // 添加运算符或括号
            tokens.push(char);
          }
        }
        
        // 添加最后一个数字（如果有）
        if (number) {
          tokens.push(parseFloat(number));
        }
        
        return tokens;
      },
      
      // 获取运算符优先级
      getPrecedence(operator) {
        if (operator === '+' || operator === '-') return 1;
        if (operator === '*' || operator === '/') return 2;
        return 0;
      },
      
      // 将中缀表达式转换为后缀表达式
      infixToPostfix(tokens) {
        const output = [];
        const operators = [];
        
        for (const token of tokens) {
          // 如果是数字，直接添加到输出
          if (typeof token === 'number') {
            output.push(token);
          } 
          // 如果是左括号，压入运算符栈
          else if (token === '(') {
            operators.push(token);
          } 
          // 如果是右括号，弹出运算符直到遇到左括号
          else if (token === ')') {
            while (operators.length > 0 && operators[operators.length - 1] !== '(') {
              output.push(operators.pop());
            }
            // 弹出左括号
            if (operators.length > 0) {
              operators.pop();
            }
          } 
          // 如果是运算符
          else {
            // 弹出优先级大于等于当前运算符的运算符
            while (operators.length > 0 && 
                  operators[operators.length - 1] !== '(' &&
                  this.getPrecedence(operators[operators.length - 1]) >= this.getPrecedence(token)) {
              output.push(operators.pop());
            }
            // 将当前运算符压入栈
            operators.push(token);
          }
        }
        
        // 将剩余的运算符弹出到输出
        while (operators.length > 0) {
          output.push(operators.pop());
        }
        
        return output;
      },
      
      // 计算后缀表达式
      calculatePostfix(tokens) {
        const stack = [];
        
        for (const token of tokens) {
          // 如果是数字，压入栈
          if (typeof token === 'number') {
            stack.push(token);
          } 
          // 如果是运算符，弹出两个操作数进行计算
          else {
            if (stack.length < 2) {
              throw new Error('表达式格式错误');
            }
            
            const b = stack.pop();
            const a = stack.pop();
            let result;
            
            switch (token) {
              case '+':
                result = a + b;
                break;
              case '-':
                result = a - b;
                break;
              case '*':
                result = a * b;
                break;
              case '/':
                if (b === 0) {
                  throw new Error('除数不能为零');
                }
                result = a / b;
                break;
              default:
                throw new Error('不支持的运算符');
            }
            
            stack.push(result);
          }
        }
        
        if (stack.length !== 1) {
          throw new Error('表达式格式错误');
        }
        
        return stack[0];
      },
      
      // 返回上一页
      navigateBack() {
        uni.navigateBack();
      },
      
      // 加载账单详情
      async loadBillDetail() {
        try {
          // 调用接口获取账单详情
          const res = await this.$request({
            url: backUrl.endpoints.bill_getById + this.billId,
            method: 'GET'
          });
          
          if (res) {
            // 回显账单数据
            // 1. 设置分类
            this.setCategoryByClassId(res.classId);
            
            // 2. 设置金额 - 统一显示正数金额
            this.calculatorExpression = Math.abs(res.price / 100).toString();
            this.calculateResult();
            
            // 3. 设置账本
            this.setSelectedLedger(res.ledgerId);
            
            // 4. 设置账户
            if (res.accountId) {
              this.setSelectedAccount(res.accountId);
            }
            
            // 5. 设置预算状态
            this.isBudget = res.isBudget === 0;
            
            // 6. 设置时间
            if (res.billTimestamp) {
              const billDate = new Date(res.billTimestamp);
              this.selectedDate = billDate;
              this.datePickerValue = this.formatDateForPicker(billDate);
              this.formattedDate = `${billDate.getMonth() + 1}月${billDate.getDate()}日`;
            } else if (res.billTime) {
              // 兼容旧数据格式，使用billTime字段
              const billDate = new Date(res.billTime);
              this.selectedDate = billDate;
              this.datePickerValue = this.formatDateForPicker(billDate);
              this.formattedDate = `${billDate.getMonth() + 1}月${billDate.getDate()}日`;
            }
            
            // 7. 设置备注
            this.billMemo = res.memo || '';
            
            // 强制刷新视图，确保分类选中状态正确显示
            this.$nextTick(() => {
              // 重新加载当前子分类，确保选中状态正确
              this.loadCurrentSubCategories();
              // 触发视图更新
              this.$forceUpdate();
            });
          }
        } catch (error) {
          console.error('加载账单详情失败:', error);
          uni.showToast({
            title: '加载账单详情失败',
            icon: 'none'
          });
        }
      },
      
      // 根据分类ID设置选中的分类（委托分类组件处理）
      setCategoryByClassId(classId) {
        if (this.$refs.categorySelect && this.$refs.categorySelect.setSelectedByOriginalId) {
          this.$refs.categorySelect.setSelectedByOriginalId(classId);
        } else {
          // 兜底：若组件尚未挂载，仅记录一个最小对象，待组件加载后由组件回填完整对象
          this.selectedCategory = this.selectedCategory && this.selectedCategory.originalId === classId ? this.selectedCategory : { originalId: classId };
        }
      },
      
      // 查找并设置顶级分类索引
      findTopLevelCategoryIndex(category) {
        // 查找该分类所在的顶级分类
        const findTopLevel = (categories, targetId) => {
          for (const category of categories) {
            if (category.originalId === targetId) {
              return category;
            }
            
            if (category.childClassList && category.childClassList.length > 0) {
              const found = findInChildren(category.childClassList, targetId);
              if (found) {
                return category;
              }
            }
          }
          return null;
        };
        
        const findInChildren = (categories, targetId) => {
          for (const category of categories) {
            if (category.originalId === targetId) {
              return true;
            }
            
            if (category.childClassList && category.childClassList.length > 0) {
              if (findInChildren(category.childClassList, targetId)) {
                return true;
              }
            }
          }
          return false;
        };
        
        const topLevelCategory = findTopLevel(this.categoryTree, category.originalId);
        if (topLevelCategory) {
          const index = this.topLevelCategories.findIndex(cat => cat.originalId === topLevelCategory.originalId);
          if (index !== -1) {
            this.currentTopCategoryIndex = index;
            this.loadCurrentSubCategories();
          }
        }
      },
      
      // 设置选中的账本
      setSelectedLedger(ledgerId) {
        if (this.$refs.ledgerPopup && this.$refs.ledgerPopup.setSelectedById) {
          this.$refs.ledgerPopup.setSelectedById(ledgerId);
        } else {
          // 兜底：若组件尚未挂载，先记录ID，待组件加载后由组件回填完整对象
          this.selectedLedger = this.selectedLedger && this.selectedLedger.id === ledgerId ? this.selectedLedger : { id: ledgerId };
        }
      },
      
      // 设置选中的账户（委托组件根据ID选择并回填）
      setSelectedAccount(accountId) {
        if (this.$refs.accountPopup && this.$refs.accountPopup.setSelectedById) {
          this.$refs.accountPopup.setSelectedById(accountId);
        } else {
          // 兜底：若组件尚未挂载，先记录ID，待组件加载后会回填完整对象
          this.selectedAccount = this.selectedAccount && this.selectedAccount.id === accountId ? this.selectedAccount : { id: accountId };
        }
      },
      
      // 提交账单
      submitBill() {
        return this.handleSave(false);
      },
      // 新增：保存并留在本页继续记账
      submitBillAndContinue() {
        return this.handleSave(true);
      }
    }
  }
</script>

<style scoped>
  .add-bill-container {
    padding: 0;
    height: 100vh;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    background-color: #fffdfd;
  }
  
  /* 下拉刷新样式 */
  .add-bill-container >>> .uni-refresher {
    height: 80rpx;
  }
  
  .add-bill-container >>> .uni-refresher .uni-refresher-icon {
    width: 40rpx;
    height: 40rpx;
    color: #007AFF;
  }
  
  .add-bill-container >>> .uni-refresher .uni-refresher-text {
    font-size: 24rpx;
    color: #888;
  }
  
  /* 自定义导航栏样式 */
  .custom-nav-bar {
    background-color: #fff;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
    height: 88rpx;
    padding: 0 20rpx;
    position: relative;
    z-index: 100;
  }
  
  /* 确保返回按钮图标大小和颜色 */
  .custom-nav-bar >>> .uni-nav-bar__left-icon {
    font-size: 32rpx !important;
    color: #333 !important;
    z-index: 101;
  }
  
  /* 增加返回按钮点击区域 */
  .custom-nav-bar >>> .uni-nav-bar__left {
    width: 80rpx;
    padding: 10rpx;
    display: flex;
    align-items: center;
    justify-content: flex-start;
  }
  
  .nav-center {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 20rpx;
  }
  
  /* 分段器样式优化 */
  .nav-center >>> .uni-segmented-control {
    height: 72rpx;
    background-color: #f5f5f5;
    border-radius: 36rpx;
    overflow: hidden;
    display: inline-flex;
    flex-wrap: nowrap;
  }
  
  .nav-center >>> .uni-segmented-control__item {
    line-height: 72rpx;
    background-color: transparent;
    color: #666;
    font-size: 28rpx;
    transition: all 0.3s ease;
    min-width: 80rpx;
  }
  
  .nav-center >>> .uni-segmented-control__item-active {
    color: #fff !important;
    background-color: #007AFF !important;
    border-radius: 36rpx;
    padding: 0 24rpx;
    margin: 0 8rpx;
    font-size: 28rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 122, 255, 0.3);
    transition: all 0.3s ease;
  }
  
  /* 加载状态 */
  .loading-container {
    padding-top: 300rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
  }
  
  /* 分段器容器样式 */
  .segmented-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 30rpx;
    background-color: #fff;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  }
  
  /* 设置图标容器 */
  .settings-icon-container {
    padding: 10rpx;
  }
  
  /* 表单样式 */
  .form-item {
    margin-bottom: 40rpx;
    padding: 0 30rpx;
  }
  
  .label {
    display: block;
    margin-bottom: 20rpx;
    font-size: 32rpx;
    color: #333;
  }
  
  /* 导航栏占位区域 */
  .nav-placeholder {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 10rpx 0;
  }
  
  /* 子分类宫格样式 - 响应式布局 */
  .sub-category-grid {
    border-radius: 12rpx;
    overflow: hidden;
    padding: 20rpx;
    width: 100%;
    box-sizing: border-box;
  }
  
  /* 移除固定宽度计算，让uni-grid组件根据column属性自动分配 */
  .sub-category-grid-item {
    padding: 10rpx;
  }
  
  .category-card {
    height: 160rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
    width: 100%;
    /* 确保卡片在任何宽度下都能正确显示 */
    min-width: 0;
  }
  
  .category-card:active {
    background-color: #f5f5f5;
    border-radius: 16rpx;
  }
  
  /* 选中状态的样式 */
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
  
  /* 确保uni-grid没有边框并能完全展开 */
  .sub-category-grid >>> .uni-grid {
    border: none;
  }
  
  .sub-category-grid >>> .uni-grid::before,
  .sub-category-grid >>> .uni-grid::after,
  .sub-category-grid >>> .uni-grid-item::before,
  .sub-category-grid >>> .uni-grid-item::after {
    border: none;
  }
  
  /* 无子分类提示 */
  .no-sub-category-tip {
    padding: 40rpx 20rpx;
    text-align: center;
    font-size: 24rpx;
    color: #999;
  }
  
  /* 底部输入框占位 */
  .bottom-input-placeholder {
    transition: height 0.3s ease;
  }
  
  /* 输入区域整体容器 */
  .input-section-container {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background-color: #fff;
    border-top: 1px solid #eee;
    z-index: 1000;
    transition: bottom 0.3s ease;
  }
  
  /* 标签容器样式 */
  .tags-container {
    padding: 10rpx 30rpx;
    background-color: rgba(255, 255, 255, 0.95);
    box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  }
  
  /* 日期选择器包装器样式 */
  .date-picker-wrapper {
    padding: 0 30rpx;
  }
  
  /* 确保日期选择器紧贴上方元素 */
  .date-picker-wrapper >>> .uni-datetime-picker {
    margin: 0;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
  }
  
  /* 计算器容器包装器 */
  .calculator-container-wrapper {
    padding: 20rpx 30rpx;
  }
  
  /* 计算器底部容器 */
  .calculator-bottom-container {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background-color: #fff;
    border-top: 1px solid #eee;
    padding: 20rpx 30rpx;
    transition: bottom 0.3s ease;
    z-index: 1000;
  }
  
  /* 计算器样式 */
  .calculator-container {
    display: flex;
    align-items: center;
    background-color: #fff;
    border: 2px solid #007AFF;
    border-radius: 12rpx;
    overflow: hidden;
    box-shadow: 0 2rpx 10rpx rgba(0, 122, 255, 0.1);
  }
  
  .calculator-input {
    flex: 1;
    height: 88rpx;
    padding: 0 30rpx;
    color: #333;
    min-width: 0;
  }
  
  .calculator-result {
    padding: 0 20rpx;
    color: #007AFF;
    font-weight: bold;
    /* 增加最小宽度，确保结果能完全显示 */
    min-width: 100rpx;
    text-align: right;
    white-space: nowrap;
    overflow: visible;
    border-right: 1px solid #eee;
  }
  
  .save-button {
    background-color: #007AFF;
    color: #fff;
    border-radius: 8rpx;
    height: 88rpx;
    line-height: 88rpx;
    font-size: 30rpx;
    padding: 0 36rpx;
    border: none;
    margin: 0 0 0 16rpx;
  }
  
  .save-button:active {
    opacity: 0.8;
  }
  
  .continue-button {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background-color: #FF4500;
    color: #fff;
    border: 1px solid #FF4500;
    border-radius: 8rpx;
    height: 88rpx;
    padding: 8rpx 36rpx;
    margin-left: 16rpx;
  }
  
  .continue-line {
    font-size: 22rpx;
    line-height: 28rpx;
  }
  
  /* 底部按钮样式 */
  .footer {
    padding: 30rpx;
    background-color: #fff;
    border-top: 1px solid #eee;
  }
  
  .submit-btn {
    background-color: #007AFF;
    color: #fff;
    font-size: 32rpx;
    line-height: 90rpx;
  }
  
  .submit-btn[disabled] {
    background-color: #ccc;
    color: #fff;
  }
  
  /* 弹出层样式 */
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
  
  .popup-title {
    font-size: 34rpx;
    font-weight: bold;
    color: #333;
  }
  
  .popup-content {
    max-height: 500rpx;
    overflow-y: auto;
    padding: 30rpx 40rpx;
  }
  
  /* 备注弹出层特定样式 */
  .memo-popup {
    width: 700rpx;
  }
  

  /* 备注输入框样式 */
  .memo-input-container {
    position: relative;
    width: 100%;
  }
  
  .memo-textarea {
    width: 100%;
    min-height: 300rpx;
    max-height: 400rpx;
    padding: 20rpx;
    box-sizing: border-box;
    border: 2rpx solid #E5E5EA;
    border-radius: 16rpx;
    font-size: 28rpx;
    color: #333;
    resize: none;
    background-color: #FAFAFA;
    transition: all 0.3s ease;
  }
  
  .memo-textarea:focus {
    border-color: #FF9500;
    background-color: #fff;
    box-shadow: 0 0 0 10rpx rgba(255, 149, 0, 0.05);
  }
  
  .memo-textarea::placeholder {
    color: #999;
    font-size: 26rpx;
  }
  
  .memo-count {
    position: absolute;
    right: 20rpx;
    bottom: 10rpx;
    font-size: 24rpx;
    color: #999;
    background-color: transparent;
    padding: 5rpx 10rpx;
  }
  
  /* 弹窗底部按钮样式 */
  .popup-footer {
    display: flex;
    flex-direction: row;
    border-top: 1px solid #f0f0f0;
  }
  
  .cancel-button {
    flex: 1;
    height: 96rpx;
    line-height: 96rpx;
    background-color: #f5f5f5;
    color: #666;
    font-size: 32rpx;
    border: none;
    border-radius: 0 0 0 24rpx;
    font-weight: 500;
  }
  
  .confirm-button {
    flex: 1;
    height: 96rpx;
    line-height: 96rpx;
    background-color: #2979FF;
    color: #fff;
    font-size: 32rpx;
    border: none;
    border-radius: 0 0 24rpx 0;
    font-weight: 500;
  }
  
  .cancel-button:active {
    background-color: #e5e5e5;
  }
  
  .confirm-button:active {
    background-color: #e68600;
  }
  
  .sub-sub-category-list {
    padding: 20rpx;
  }
  
  .sub-sub-category-item {
    display: flex;
    align-items: center;
    padding: 20rpx;
    border-radius: 8rpx;
    margin-bottom: 10rpx;
  }
  
  .sub-sub-category-item:active {
    background-color: #f5f5f5;
  }
  
  .sub-sub-category-item.selected {
    background-color: #f0e8fd;
    border-radius: 8rpx;
  }
  
  .sub-sub-category-icon {    
    font-size: 40rpx;    
    margin-right: 20rpx;    
    color: #007AFF;    
  }    
    
  .sub-sub-category-name {    
    font-size: 32rpx;    
    color: #333;    
  }
  
  /* 可拖动标签样式 */
  .draggable-tags-container {
    position: fixed;
    left: 0;
    right: 0;
    padding: 10rpx 30rpx;
    background-color: rgba(255, 255, 255, 0.95);
    z-index: 999;
    box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  }
  
  .tags-scroll-view {
    width: 100%;
  }
  
  .tags-wrapper {
    display: flex;
    gap: 20rpx;
    padding: 5rpx 0;
  }
  
  .tag-item {
    display: flex;
    align-items: center;
    padding: 0rpx 10rpx;
    background-color: #f0f0f0;
    border-radius: 36rpx;
    justify-content: center;
    transition: all 0.3s ease;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
    position: relative;
  }
  
  /* 预算标签样式 */
  .budget-tag {
    background-color: #E6F7EF;
    color: #07C160;
  }
  
  /* 不计入预算时的样式 */
  .budget-tag.budget-disabled {
    background-color: #f5f5f5;
    color: #999;
  }
  
  .tag-item:active {
    background-color: #e0e0e0;
  }
  
  .tag-text {
    font-size: 28rpx;
    margin-right: 10rpx;
    white-space: nowrap;
  }
  
  .tag-icon {
    font-weight: normal;
  }
  
  /* 标签颜色跟随父元素 */
  .date-tag .tag-text, .date-tag .tag-icon {
    color: #007AFF;
  }
  
  .ledger-tag .tag-text, .ledger-tag .tag-icon {
    color: #5856D6;
  }
  
  .account-tag .tag-text, .account-tag .tag-icon {
    color: #34C759;
  }
  
  .memo-tag .tag-text, .memo-tag .tag-icon {
    color: #FF9500;
  }
  
  .budget-tag .tag-text, .budget-tag .tag-icon {
    color: #07C160;
  }
  
  .budget-tag.budget-disabled .tag-text, .budget-tag.budget-disabled .tag-icon {
    color: #999;
  }
  
  /* 日期标签特定样式 */
  .date-tag {
    background-color: #e8f4fd;
  }
  
  .date-tag:active {
    background-color: #d0e7f9;
  }
  
  /* 账本标签特定样式 */
  .ledger-tag {
    background-color: #f0e8fd;
  }
  
  .ledger-tag:active {
    background-color: #e0d0f9;
  }
  
  /* 账户标签特定样式 */
  .account-tag {
    background-color: #e8fde8;
  }
  
  .account-tag:active {
    background-color: #d0f9d0;
  }
  
  /* 备注标签样式 */
  .memo-tag {
    background-color: #FFF7E6;
  }
  
  .memo-tag:active {
    background-color: #FFEDCC;
  }
  
  /* AI记账标签样式 */
  .ai-tag {
    background-color: #FF4500;
    font-weight: bold;
  }
  
  .ai-tag:active {
    background-color: #D0E7FF;
  }
  
  .ai-tag .tag-text {
    color: #ffffff;
  }
  
  /* 图片预览悬浮窗样式 */
  .image-preview-container {
    position: fixed;
    width: 200rpx;
    height: 200rpx;
    background-color: #fff;
    border-radius: 16rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.15);
    overflow: hidden;
    z-index: 9999;
    display: flex;
    flex-direction: column;
    touch-action: none; /* 防止拖动时页面滚动 */
  }
  
  .retry-button {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(255, 0, 0, 0.8);
    color: white;
    text-align: center;
    padding: 10rpx;
    font-size: 24rpx;
  }
  
  .image-preview {
    width: 100%;
    height: 100%;
  }
  
  .close-preview {
    position: absolute;
    top: 10rpx;
    right: 10rpx;
    width: 40rpx;
    height: 40rpx;
    background-color: rgba(0, 0, 0, 0.5);
    color: #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24rpx;
    z-index: 10000;
  }
  
  /* 备注已添加指示器 */
  .memo-indicator {
    width: 10rpx;
    height: 10rpx;
    background-color: #FF9500;
    border-radius: 50%;
    margin-left: 8rpx;
  }
  
  /* 账本列表样式 */
  .ledger-list {
    padding: 20rpx;
  }
  
  .ledger-item {
    padding: 25rpx;
    border-bottom: 1px solid #f0f0f0;
  }
  
  .ledger-item:active {
    background-color: #e0e0e0;
  }
  
  .ledger-item.selected {
    background-color: #e8f4fd;
  }
  
  .ledger-info-row {
    display: flex;
    align-items: center;
    gap: 15rpx;
    margin-bottom: 8rpx;
  }
  
  .ledger-info-row:first-child {
    justify-content: space-between;
  }
  
  .ledger-name {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
  }
  
  .ledger-type {
    font-size: 24rpx;
    color: #666;
    background-color: #f0f0f0;
    padding: 5rpx 15rpx;
    border-radius: 15rpx;
  }
  
  .ledger-attribute {
    font-size: 24rpx;
    color: #666;
    background-color: #f0f0f0;
    padding: 5rpx 15rpx;
    border-radius: 15rpx;
  }
  
  .default-badge {
    font-size: 24rpx;
    color: #fff;
    background-color: #007AFF;
    padding: 5rpx 15rpx;
    border-radius: 15rpx;
  }
  
  /* 账本类型和属性文本样式 */
  .ledger-type-text {
    font-size: 24rpx;
    color: #007AFF;
  }
  
  .ledger-property-text {
    font-size: 24rpx;
    color: #5AC8FA;
  }
  
  .budget-status-text {
    font-size: 24rpx;
    color: #888;
    cursor: pointer;
  }
  
  .budget-status-text.budget-enabled {
    color: #07C160;
  }
  
  .default-badge {
    font-size: 24rpx;
    color: #007AFF;
    background: none;
    padding: 0;
  }
  
  /* 账户列表样式 */
  .account-list {
    padding: 20rpx;
  }
  
  .account-item {
    padding: 25rpx;
    border-bottom: 1px solid #f0f0f0;
  }
  
  .account-item:active {
    background-color: #e0e0e0;
  }
  
  .account-item.selected {
    background-color: #e8fde8;
  }
  
  .account-info {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 15rpx;
  }
  
  /* 账户信息第二行样式 */
  .account-info-second-line {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 15rpx;
    margin-top: 10rpx;
    padding-left: 0;
  }
  
  /* 不选账户选项样式 */
  .no-select-account {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20rpx 0;
    margin-top: 10rpx;
  }
  
  .no-select-account-text {
    color: #007AFF;
    text-decoration: underline;
    font-size: 28rpx;
  }
  
  .account-name {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
  }
  
  .account-type {
    font-size: 24rpx;
    color: #666;
    background-color: #f0f0f0;
    padding: 5rpx 15rpx;
    border-radius: 15rpx;
  }
  
  .account-budget {
    font-size: 24rpx;
    color: #666;
    background-color: #f0f0f0;
    padding: 5rpx 15rpx;
    border-radius: 15rpx;
  }
  
  .account-balance {
    font-size: 24rpx;
    color: #666;
    margin-left: auto;
  }
  
  /* 账户标签样式 */
  .account-type-tag {
    font-size: 24rpx;
    color: #fff;
    padding: 5rpx 15rpx;
    border-radius: 15rpx;
    background-color: #007AFF;
  }
  
  /* 不同账户类型的样式 */
  .account-type-tag.type-savings {
    background-color: #007AFF;
  }
  
  .account-type-tag.type-credit {
    background-color: #FF2D55;
  }
  
  .account-type-tag.type-recharge {
    background-color: #5AC8FA;
  }
  
  .account-type-tag.type-invest {
    background-color: #34C759;
  }
  
  .account-budget-tag {
    font-size: 24rpx;
    color: #FF9500;
    padding: 0 15rpx;
  }
  
  .account-total-tag {
    font-size: 24rpx;
    color: #34C759;
    padding: 0 15rpx;
  }
  
  /* 弹出层操作按钮样式 */
  .popup-actions {
    display: flex;
    align-items: center;
    gap: 20rpx;
  }
  
  .setting-btn {
    background-color: transparent;
    color: #007AFF;
    font-size: 28rpx;
    padding: 0;
    line-height: normal;
    min-height: auto;
  }
  
  /* 无数据提示 */
  .no-data-tip {
    padding: 60rpx 20rpx;
    text-align: center;
    font-size: 28rpx;
    color: #999;
  }
  
  /* 备注输入框样式 */
  .memo-input-wrapper {
    padding: 0 30rpx;
    border-radius: 8rpx;
    display: flex;
    align-items: center;
  }
  
  .memo-input {
    flex: 1;
    height: 60rpx;
    font-size: 28rpx;
    color: #333;
    padding: 0 10rpx;
  }
  
  .memo-input-count {
    font-size: 24rpx;
    color: #999;
    margin-left: 10rpx;
  }
  /* AI记账弹窗样式 - 提高优先级 */
  .ai-chat-popup-content {
    width: 95% !important;
    max-width: 1400rpx !important;
    background-color: #fff;
    border-radius: 24rpx;
    overflow: hidden;
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
    min-width: 700rpx;
  }
  
  /* 确保弹窗容器不会限制宽度 */
  .ai-chat-popup {
    width: 100% !important;
    position: fixed;
    top: 0;
    left: 0;
    z-index: 9999;
  }
  
  /* 增强遮罩层样式 - 确保覆盖所有内容 */
  .uni-popup-mask {
    background-color: rgba(0, 0, 0, 0.5) !important;
    backdrop-filter: blur(4rpx);
    position: fixed !important;
    top: 0 !important;
    left: 0 !important;
    width: 100% !important;
    height: 100% !important;
    z-index: 9998 !important;
  }
  
  /* 确保uni-popup组件内部不会限制宽度 */
  .uni-popup {
    width: 100% !important;
    z-index: 9999;
  }
  
  .uni-popup--center {
    width: 100% !important;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  /* 恢复输入区域容器原有样式 */
  
  .ai-chat-popup-header {
    padding: 24rpx;
    text-align: center;
    border-bottom: 1px solid #f0f0f0;
    background-color: #fafafa;
  }
  
  .ai-chat-popup-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
  }
  
  .ai-chat-popup-body {
    padding: 24rpx;
  }
  
  .ai-chat-textarea {
    width: 100%;
    min-height: 160rpx;
    font-size: 28rpx;
    color: #333;
    border: 1px solid #e6e6e6;
    border-radius: 8rpx;
    padding: 16rpx;
    box-sizing: border-box;
    background-color: #fff;
  }
  
  .ai-chat-textarea-count {
    display: block;
    text-align: right;
    font-size: 22rpx;
    color: #999;
    margin-top: 10rpx;
  }
  
  .ai-chat-popup-footer {
    display: flex;
    border-top: 1px solid #f0f0f0;
  }
  
  .ai-chat-cancel-btn {
    flex: 1;
    border: none;
    border-right: 1px solid #f0f0f0;
    background-color: #fff;
    color: #666;
    font-size: 28rpx;
    padding: 24rpx 0;
  }
  
  .ai-chat-confirm-btn {
    flex: 1;
    border: none;
    background-color: #FF4500;
    color: #fff;
    font-size: 28rpx;
    padding: 24rpx 0;
  }
</style>