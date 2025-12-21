// 工具函数集合

/**
 * 尝试切换到指定页面或重启应用
 * @param {String} url - 要跳转到的页面路径
 * @param {Object} options - 跳转选项
 */
export function tryToSwitchOrRelaunch(url, options = {}) {
  try {
    const { switchType = 'relaunch' } = options;
    
    if (switchType === 'navigateTo') {
      uni.navigateTo({ url });
    } else if (switchType === 'redirectTo') {
      uni.redirectTo({ url });
    } else if (switchType === 'switchTab') {
      uni.switchTab({ url });
    } else {
      // 默认使用relaunch
      uni.reLaunch({ url });
    }
  } catch (err) {
    console.error('页面跳转失败:', err);
    // 兜底方案 - 强制重启
    uni.reLaunch({
      url: '/pages/index/firstpage',
      success: () => {
        console.log('已重启应用到首页');
      },
      fail: (relaunchErr) => {
        console.error('应用重启失败:', relaunchErr);
      }
    });
  }
}

/**
 * 检查是否是微信环境
 * @returns {Boolean} 是否是微信环境
 */
export function isWechatEnv() {
  try {
    const platform = uni.getSystemInfoSync().platform;
    return platform === 'devtools' || platform === 'ios' || platform === 'android';
  } catch (e) {
    console.error('获取平台信息失败:', e);
    return false;
  }
}

/**
 * 格式化时间戳为日期字符串
 * @param {Number} timestamp - 时间戳
 * @param {String} format - 格式化模板
 * @returns {String} 格式化后的日期字符串
 */
export function formatDate(timestamp, format = 'YYYY-MM-DD HH:mm:ss') {
  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds);
}

/**
 * 显示加载提示
 * @param {String} title - 提示文字
 * @param {Boolean} mask - 是否显示透明蒙层
 * @returns {String} 加载提示的ID，可用于后续关闭
 */
export function showLoading(title = '加载中...', mask = true) {
  const loadingId = `loading_${Date.now()}`;
  uni.showLoading({
    title,
    mask,
    success: () => {
      console.log(`[${loadingId}] 加载提示已显示`);
    },
    fail: (err) => {
      console.error(`[${loadingId}] 显示加载提示失败:`, err);
    }
  });
  return loadingId;
}

/**
 * 隐藏加载提示
 * @param {String} loadingId - 加载提示的ID
 */
export function hideLoading(loadingId) {
  uni.hideLoading({
    success: () => {
      console.log(`[${loadingId}] 加载提示已隐藏`);
    },
    fail: (err) => {
      console.error(`[${loadingId}] 隐藏加载提示失败:`, err);
    }
  });
}

/**
 * 显示提示消息
 * @param {String} title - 提示内容
 * @param {String} icon - 图标类型，success、error、loading、none
 * @param {Number} duration - 提示的延迟时间
 */
export function showToast(title, icon = 'none', duration = 2000) {
  return new Promise((resolve) => {
    uni.showToast({
      title,
      icon,
      duration,
      success: () => {
        setTimeout(() => resolve(), duration);
      },
      fail: (err) => {
        console.error('显示提示消息失败:', err);
        setTimeout(() => resolve(), duration);
      }
    });
  });
}

// 颜色工具：判断是否为白色或近白色
export function isWhiteishColor(input) {
  try {
    if (!input) return false;
    const s = String(input).trim().toLowerCase();
    if (s === 'white' || s === '#fff' || s === '#ffffff' || s === 'rgb(255,255,255)' || s === 'rgba(255,255,255,1)' || s === '#fffff0' || s === '#f9f9f9') {
      return true;
    }
    // 支持 rgb/rgba 解析
    if (s.startsWith('rgb(') || s.startsWith('rgba(')) {
      const nums = s.replace(/rgba?\(|\)/g, '').split(',').map(x => parseInt(x.trim(), 10));
      const r = nums[0] || 0, g = nums[1] || 0, b = nums[2] || 0;
      const luminance = (0.299*r + 0.587*g + 0.114*b);
      return luminance > 245;
    }
    // 解析十六进制颜色 #rrggbb 或 #rgb
    const h = s.replace('#','');
    let r, g, b;
    if (h.length === 3) {
      r = parseInt(h[0] + h[0], 16);
      g = parseInt(h[1] + h[1], 16);
      b = parseInt(h[2] + h[2], 16);
    } else if (h.length >= 6) {
      r = parseInt(h.slice(0,2), 16);
      g = parseInt(h.slice(2,4), 16);
      b = parseInt(h.slice(4,6), 16);
    } else {
      return false;
    }
    const luminance = (0.299*r + 0.587*g + 0.114*b);
    return luminance > 245;
  } catch (e) {
    return false;
  }
}

// 根据主题色返回图标颜色：白/近白返回默认色#faad14，否则返回主题色
export function themeIconColor(themePrimaryColor) {
  const c = (themePrimaryColor || '').toString().trim().toLowerCase();
  if (!c) return '#faad14';
  return isWhiteishColor(c) ? '#faad14' : themePrimaryColor;
}