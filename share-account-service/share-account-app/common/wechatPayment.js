import request from './request.js';
import backUrl from './back_url.js';

/**
 * 微信支付服务类
 */
class WechatPaymentService {
  constructor() {
    this.baseUrl = backUrl.baseUrl;
  }
  
  /**
   * 验证服务器端支付状态（静态方法）
   * 使用延迟重试策略确保支付结果的一致性
   * @param {number} paymentId - 支付ID
   * @param {number} maxRetries - 最大重试次数
   * @param {number} retryDelay - 重试延迟（毫秒）
   * @returns {Promise<Object>} 服务端支付状态
   */
  static async verifyServerPaymentStatus(paymentId, maxRetries = 3, retryDelay = 1000) {
    const wechatPayment = new WechatPaymentService();
    let lastError = null;
    
    for (let attempt = 0; attempt < maxRetries; attempt++) {
      try {
        // 查询服务端支付状态
        const status = await wechatPayment.checkPaymentStatus(paymentId);
        
        // 如果状态明确为支付成功，直接返回
        if (status) {
          return { success: true, status: status };
        }
        
        // 如果状态明确为失败，直接返回
        if (!status) {
          return { success: false, status: status };
        }
        
        // 状态不确定（可能是处理中），记录错误并继续重试
        lastError = new Error('支付状态未确认，可能还在处理中');
        
        // 不是最后一次尝试时才延迟
        if (attempt < maxRetries - 1) {
          console.log(`第${attempt + 1}次查询支付状态未确认，${retryDelay}ms后重试`);
          await new Promise(resolve => setTimeout(resolve, retryDelay));
          // 指数退避策略
          retryDelay *= 1.5;
        }
      } catch (error) {
        console.error(`第${attempt + 1}次查询支付状态失败:`, error);
        lastError = error;
        
        // 不是最后一次尝试时才延迟
        if (attempt < maxRetries - 1) {
          await new Promise(resolve => setTimeout(resolve, retryDelay));
          retryDelay *= 1.5;
        }
      }
    }
    
    // 所有重试都失败，抛出最后一个错误
    throw lastError || new Error('验证支付状态失败');
  }

  /**
   * 创建支付记录
   * @param {Object} params - 支付参数
   * @param {number} params.packageId - 套餐ID
   * @returns {Promise<Object>} 支付记录信息
   */
  async createPayment(params) {
    try {
      const response = await request.post(`${this.baseUrl}${backUrl.endpoints.payment_createPayment}`, params);
      return response;
    } catch (error) {
      console.error('创建支付记录失败:', error);
      throw error;
    }
  }

  /**
   * 获取支付信息
   * @param {number} paymentId - 支付ID
   * @returns {Promise<Object>} 支付信息
   */
  async getPaymentInfo(paymentId) {
    try {
      const response = await request.get(`${this.baseUrl}${backUrl.endpoints.payment_getPayInfo}${paymentId}`);
      return response;
    } catch (error) {
      console.error('获取支付信息失败:', error);
      throw error;
    }
  }

  /**
   * 检查支付状态
   * @param {number} paymentId - 支付ID
   * @returns {Promise<Object>} 支付状态
   */
  async checkPaymentStatus(paymentId) {
    try {
      const response = await request.get(`${this.baseUrl}${backUrl.endpoints.payment_checkStatus}${paymentId}`);
      console.log('检查支付状态成功:', response);
      return response;
    } catch (error) {
      console.error('检查支付状态失败:', error);
      throw error;
    }
  }

  /**
   * 处理支付请求（供前端组件调用）
   * @param {Object} params - 支付参数
   * @param {number} params.packageId - 套餐ID
   * @param {number} params.amount - 金额
   * @param {string} params.payment_type - 支付类型
   * @param {string} params.goods_name - 商品名称
   * @param {string} params.goods_description - 商品描述
   * @returns {Promise<Object>} 支付结果
   */
  async processPayment(params) {
    try {
      // 直接将所有参数传递给requestPayment方法，确保支付记录保存到payment_record表
      return await this.requestPayment(params);
    } catch (error) {
      console.error('处理支付请求失败:', error);
      throw error;
    }
  }

  /**
   * 发起支付
   * @param {Object} params - 完整支付参数
   * @returns {Promise<Object>} 支付结果
   */
  async requestPayment(params) {
    try {
      // 步骤1: 创建支付记录，传递完整参数确保payment_record表数据完整
      const orderResponse = await this.createPayment(params);
      
      if (!orderResponse || !orderResponse.paymentId) {
        throw new Error('创建支付记录失败，未返回有效的支付ID');
      }
      
      const { paymentId } = orderResponse;
      
      // 步骤2: 获取支付信息
      const payInfoResponse = await this.getPaymentInfo(paymentId);
      
      if (!payInfoResponse) {
        throw new Error('获取支付信息失败');
      }
      
      const payInfo = payInfoResponse;
      
      // 步骤3: 使用uni-app的支付API调用微信支付
      return new Promise((resolve, reject) => {
        // 准备uni.requestPayment需要的参数
        const paymentParams = {
          timeStamp: payInfo.timeStamp,
          nonceStr: payInfo.nonceStr,
          package: payInfo.packageVal, // 后端返回的package字段可能是packageVal
          signType: payInfo.signType,
          paySign: payInfo.paySign,
          success: async function(res) {
            try {
              console.log('客户端支付成功回调，开始验证服务端支付状态');
              
              // 使用延迟重试策略验证支付状态
              const serverStatus = await WechatPaymentService.verifyServerPaymentStatus(paymentId);
              
              if (serverStatus && serverStatus.success) {
                console.log('服务端验证支付成功');
                resolve({ 
                  success: true, 
                  message: '支付成功', 
                  paymentId, 
                  serverVerified: true,
                  serverData: serverStatus
                });
              } else {
                console.warn('服务端验证支付状态失败，客户端显示支付成功但服务端未确认');
                resolve({ 
                  success: false, 
                  message: '请稍后查看支付结果', 
                  paymentId, 
                  serverVerified: false,
                  needCheckLater: true
                });
              }
            } catch (error) {
              console.error('验证服务端支付状态时出错:', error);
              // 验证出错时，告知用户需要稍后查看订单状态
              resolve({ 
                success: false, 
                message: '请稍后查看支付结果', 
                paymentId, 
                serverVerified: false,
                needCheckLater: true,
                error: error.message
              });
            }
          },
          fail: function(err) {
            // 支付失败或取消
            if (err.errMsg && err.errMsg.indexOf('cancel') > -1) {
              resolve({ success: false, message: '用户取消支付', canceled: true, paymentId });
            } else {
              resolve({ success: false, message: '支付失败', error: err.errMsg, paymentId });
            }
          }
        };
        
        // 调用uni-app支付API
        uni.requestPayment(paymentParams);
      });
    } catch (error) {
      console.error('支付失败:', error);
      throw error;
    }
  }


}

export default new WechatPaymentService();