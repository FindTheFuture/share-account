import wechatPayment from './wechatPayment.js';
import douyinPayment from './douyinPayment.js';

/**
 * 支付平台枚举
 */
const PaymentPlatform = {
  WECHAT: 'WECHAT',
  DOUYIN: 'DOUYIN',
  ALIPAY: 'ALIPAY',
  UNKNOWN: 'UNKNOWN'
};

/**
 * 支付抽象服务类
 * 根据当前运行环境自动选择对应的支付服务
 */
class PaymentService {
  constructor() {
    this.currentPlatform = this.detectPlatform();
    console.log('当前支付平台:', this.currentPlatform);
  }

  /**
   * 检测当前平台
   * @returns {string} 平台标识
   */
  detectPlatform() {
    // #ifdef MP-WEIXIN
    return PaymentPlatform.WECHAT;
    // #endif

    // #ifdef MP-TOUTIAO
    return PaymentPlatform.DOUYIN;
    // #endif

    // #ifdef MP-ALIPAY
    return PaymentPlatform.ALIPAY;
    // #endif

    // #ifdef H5
    // H5环境需要根据UA或其他方式判断
    const ua = navigator.userAgent.toLowerCase();
    if (ua.includes('micromessenger')) {
      return PaymentPlatform.WECHAT;
    } else if (ua.includes('toutiao') || ua.includes('lark')) {
      return PaymentPlatform.DOUYIN;
    } else if (ua.includes('alipay')) {
      return PaymentPlatform.ALIPAY;
    }
    return PaymentPlatform.UNKNOWN;
    // #endif

    return PaymentPlatform.UNKNOWN;
  }

  /**
   * 获取当前平台对应的支付服务
   * @returns {Object} 支付服务实例
   */
  getPaymentService() {
    switch (this.currentPlatform) {
      case PaymentPlatform.WECHAT:
        return wechatPayment;
      case PaymentPlatform.DOUYIN:
        return douyinPayment;
      case PaymentPlatform.ALIPAY:
        // 支付宝支付暂未实现，返回微信支付作为降级
        console.warn('支付宝支付暂未实现，降级使用微信支付');
        return wechatPayment;
      default:
        console.warn('未知支付平台，默认使用微信支付');
        return wechatPayment;
    }
  }

  /**
   * 判断是否为微信小程序
   * @returns {boolean}
   */
  isWeChat() {
    return this.currentPlatform === PaymentPlatform.WECHAT;
  }

  /**
   * 判断是否为抖音小程序
   * @returns {boolean}
   */
  isDouyin() {
    return this.currentPlatform === PaymentPlatform.DOUYIN;
  }

  /**
   * 判断是否为支付宝小程序
   * @returns {boolean}
   */
  isAlipay() {
    return this.currentPlatform === PaymentPlatform.ALIPAY;
  }

  /**
   * 创建支付记录
   * @param {Object} params - 支付参数
   * @returns {Promise<Object>} 支付记录信息
   */
  async createPayment(params) {
    const service = this.getPaymentService();
    return service.createPayment(params);
  }

  /**
   * 获取支付信息
   * @param {number} paymentId - 支付ID
   * @returns {Promise<Object>} 支付信息
   */
  async getPaymentInfo(paymentId) {
    const service = this.getPaymentService();
    return service.getPaymentInfo(paymentId);
  }

  /**
   * 检查支付状态
   * @param {number} paymentId - 支付ID
   * @returns {Promise<Object>} 支付状态
   */
  async checkPaymentStatus(paymentId) {
    const service = this.getPaymentService();
    return service.checkPaymentStatus(paymentId);
  }

  /**
   * 处理支付请求（统一入口）
   * @param {Object} params - 支付参数
   * @param {number} params.packageId - 套餐ID
   * @param {number} params.amount - 金额
   * @param {string} params.payment_type - 支付类型
   * @param {string} params.goods_name - 商品名称
   * @param {string} params.goods_description - 商品描述
   * @returns {Promise<Object>} 支付结果
   */
  async processPayment(params) {
    const service = this.getPaymentService();
    console.log(`使用 ${this.currentPlatform} 支付服务处理支付请求`);
    return service.processPayment(params);
  }

  /**
   * 验证服务端支付状态（静态方法）
   * @param {number} paymentId - 支付ID
   * @param {number} maxRetries - 最大重试次数
   * @param {number} retryDelay - 重试延迟（毫秒）
   * @returns {Promise<Object>} 服务端支付状态
   */
  static async verifyServerPaymentStatus(paymentId, maxRetries = 3, retryDelay = 1000) {
    const instance = new PaymentService();
    const service = instance.getPaymentService();

    if (typeof service.verifyServerPaymentStatus === 'function') {
      return service.verifyServerPaymentStatus(paymentId, maxRetries, retryDelay);
    }

    return instance.checkPaymentStatus(paymentId);
  }
}

// 导出支付平台枚举和支付服务实例
export { PaymentPlatform };
export default new PaymentService();
