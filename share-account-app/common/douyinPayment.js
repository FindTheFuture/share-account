import request from './request.js';
import backUrl from './back_url.js';

/**
 * 抖音支付服务类
 */
class DouyinPaymentService {
  constructor() {
    this.baseUrl = backUrl.baseUrl;
  }

  /**
   * 验证服务器端支付状态（静态方法）
   * @param {number} paymentId - 支付ID
   * @param {number} maxRetries - 最大重试次数
   * @param {number} retryDelay - 重试延迟（毫秒）
   * @returns {Promise<Object>} 服务端支付状态
   */
  static async verifyServerPaymentStatus(paymentId, maxRetries = 3, retryDelay = 1000) {
    const douyinPayment = new DouyinPaymentService();
    let lastError = null;

    for (let attempt = 0; attempt < maxRetries; attempt++) {
      try {
        const status = await douyinPayment.checkPaymentStatus(paymentId);

        if (status) {
          return { success: true, status: status };
        }

        if (!status) {
          return { success: false, status: status };
        }

        lastError = new Error('支付状态未确认，可能还在处理中');

        if (attempt < maxRetries - 1) {
          console.log(`第${attempt + 1}次查询支付状态未确认，${retryDelay}ms后重试`);
          await new Promise(resolve => setTimeout(resolve, retryDelay));
          retryDelay *= 1.5;
        }
      } catch (error) {
        console.error(`第${attempt + 1}次查询支付状态失败:`, error);
        lastError = error;

        if (attempt < maxRetries - 1) {
          await new Promise(resolve => setTimeout(resolve, retryDelay));
          retryDelay *= 1.5;
        }
      }
    }

    throw lastError || new Error('验证支付状态失败');
  }

  /**
   * 创建支付记录
   * @param {Object} params - 支付参数
   * @returns {Promise<Object>} 支付记录信息
   */
  async createPayment(params) {
    try {
      const response = await request.post(`${this.baseUrl}${backUrl.endpoints.douyin_payment_createPayment}`, params);
      return response;
    } catch (error) {
      console.error('创建抖音支付记录失败:', error);
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
      const response = await request.get(`${this.baseUrl}${backUrl.endpoints.douyin_payment_getPayInfo}/${paymentId}`);
      return response;
    } catch (error) {
      console.error('获取抖音支付信息失败:', error);
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
      const response = await request.get(`${this.baseUrl}${backUrl.endpoints.douyin_payment_checkStatus}/${paymentId}`);
      console.log('检查抖音支付状态成功:', response);
      return response;
    } catch (error) {
      console.error('检查抖音支付状态失败:', error);
      throw error;
    }
  }

  /**
   * 处理支付请求
   * @param {Object} params - 支付参数
   * @returns {Promise<Object>} 支付结果
   */
  async processPayment(params) {
    try {
      return await this.requestPayment(params);
    } catch (error) {
      console.error('处理抖音支付请求失败:', error);
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
      const orderResponse = await this.createPayment(params);

      if (!orderResponse || !orderResponse.paymentId) {
        throw new Error('创建支付记录失败，未返回有效的支付ID');
      }

      const { paymentId } = orderResponse;

      const payInfoResponse = await this.getPaymentInfo(paymentId);

      if (!payInfoResponse) {
        throw new Error('获取支付信息失败');
      }

      const payInfo = payInfoResponse;

      return new Promise((resolve, reject) => {
        // 抖音小程序支付调用
        if (typeof tt !== 'undefined' && tt.requestPayment) {
          const paymentParams = {
            orderInfo: {
              order_id: payInfo.orderId,
              payment_url: payInfo.paymentUrl,
              sign: payInfo.sign,
              total_amount: payInfo.totalAmount,
              subject: payInfo.subject
            },
            success: async function(res) {
              try {
                console.log('抖音客户端支付成功回调，开始验证服务端支付状态');
                const serverStatus = await DouyinPaymentService.verifyServerPaymentStatus(paymentId);

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
              console.error('抖音支付失败:', err);
              if (err.errMsg && err.errMsg.indexOf('cancel') > -1) {
                resolve({ success: false, message: '用户取消支付', canceled: true, paymentId });
              } else {
                resolve({ success: false, message: '支付失败', error: err.errMsg, paymentId });
              }
            }
          };

          tt.requestPayment(paymentParams);
        } else {
          // H5或其他环境尝试唤起抖音支付
          if (payInfo.paymentUrl) {
            window.location.href = payInfo.paymentUrl;
            resolve({
              success: true,
              message: '正在跳转支付页面',
              paymentId,
              redirectToPayment: true
            });
          } else {
            reject(new Error('当前环境不支持抖音支付'));
          }
        }
      });
    } catch (error) {
      console.error('抖音支付失败:', error);
      throw error;
    }
  }
}

export default new DouyinPaymentService();
