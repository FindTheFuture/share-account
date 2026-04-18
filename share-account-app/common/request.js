import $backUrlConfig from './back_url.js';

/**
 * 检查用户是否已登录
 * @returns {boolean} 是否已登录
 */
export function checkLoginStatusSync() {
    const token = uni.getStorageSync('token');
    return !!token;
}

/**
 * 退出登录（清理本地状态并可选跳转登录页）
 * @param {{ redirect?: boolean, tip?: string }} opts
 */
export function logout(opts = {}) {
    const { redirect = true, tip = '已退出登录' } = opts || {};
    try {
        // 清除鉴权相关本地存储
        uni.removeStorageSync('token');
        uni.removeStorageSync('refreshToken');
        uni.removeStorageSync('expireAt');
        uni.removeStorageSync('additionalId');
        uni.removeStorageSync('redirectAfterLogin');
        uni.removeStorageSync('isGuest');
        uni.removeStorageSync('thunder');
        uni.removeStorageSync('canSendMessage');
        uni.removeStorageSync('refreshBillListOnShow');
        uni.removeStorageSync('guideCard');
        
    } catch (e) {
        console.warn('清理登录态失败', e);
    }
    // 反馈提示
    try { uni.showToast({ title: tip, icon: 'none', duration: 1200 }); } catch (_) {}
    // 可选跳转登录页
    if (redirect) {
        setTimeout(() => {
            try { uni.navigateTo({ url: '/pages/login/login' }); } catch (_) {}
        }, 500);
    }
}

/**
 * 封装的请求函数
 * @param {Object} options - 请求参数
 * @param {Object} customOptions - 自定义选项
 * @returns {Promise}
 */
function _request(options, customOptions = {}) {
    // 获取本地存储中的 token 和 additionalId
    const token = uni.getStorageSync('token');
    const additionalId = uni.getStorageSync('additionalId');
    
    // 检查登录状态
    if (!customOptions.silent && !checkLoginStatusSync()) {
        // 显示"请先登录"提示，使用更长的显示时间确保用户能看到
        uni.showToast({ 
            title: '请先登录', 
            icon: 'none',
            duration: 2000,
            mask: true // 添加遮罩防止用户误操作
        });
        
        // 如果未登录且不是静默请求，跳转到登录页面
        const pages = getCurrentPages();
        if (pages && pages.length > 0) {
            const currentPage = pages[pages.length - 1];
            const currentPath = currentPage.route;
            const currentParams = currentPage.options;
            let redirectUrl = `/${currentPath}`;
            
            // 拼接参数
            const paramStr = Object.keys(currentParams)
                .map(key => `${key}=${encodeURIComponent(currentParams[key])}`)
                .join('&');
            
            if (paramStr) {
                redirectUrl += `?${paramStr}`;
            }
            
            // 保存重定向URL
            uni.setStorageSync('redirectAfterLogin', redirectUrl);
        }
        
        // 创建一个特殊的错误对象，标识这是一个未登录错误
        const notLoggedInError = new Error('');
        notLoggedInError.isNotLoggedIn = true;
        
        // 跳转到登录页面，延迟执行以便提示能显示
        setTimeout(() => {
            //uni.navigateTo({ url: '/pages/login/login' });
        }, 1000); // 增加延迟时间，确保提示完全显示
        return Promise.reject(notLoggedInError);
    }
    
    // 设置默认请求头
    const defaultHeader = {
        'content-type': 'application/json'
    };
    
    // 如果有token，添加到请求头
    if (token) {
        defaultHeader['Aboluo'] = `Aboluo ${token}`;
    }
    
    // 如果有additionalId，添加到请求头
    if (additionalId) {
        defaultHeader['Additional'] = `${additionalId}`;
    }
    
    const defaultOptions = {
        url: '',
        method: 'GET',
        data: {},
        header: defaultHeader
    };
    
    // 合并默认选项和传入选项，确保用户的header优先级更高
    const finalOptions = { ...defaultOptions, ...options };
    // 特殊处理header，确保用户自定义的header不会被覆盖
    finalOptions.header = { ...defaultHeader, ...(options.header || {}) };
    
    // 如果配置了基础URL且当前URL不是完整URL，则拼接
    if ($backUrlConfig.baseUrl && !finalOptions.url.startsWith('http://') && !finalOptions.url.startsWith('https://')) {
        finalOptions.url = $backUrlConfig.baseUrl + finalOptions.url;
    }
    
    return new Promise((resolve, reject) => {
        uni.request({
            ...finalOptions,
            success: (res) => {
                // 处理响应
                if (res.statusCode === 200) {
                    const data = res.data;
                    // 根据响应结构处理
                    if (!data || !data.hasOwnProperty('data')) {
                        resolve(data);
                    } else {
                        // 成功响应，返回嵌套的data字段
                        resolve(data.data);
                    }
                } else if (res.statusCode === 401) {
                    // 未授权，清除token并显示登录弹窗
                    uni.removeStorageSync('token');
                    uni.removeStorageSync('refreshToken');
                    uni.removeStorageSync('expireAt');
                    uni.removeStorageSync('additionalId');
                    
                    // 未授权处理，清除token并跳转到登录页面
                    if (!customOptions.silent) {
                        // 保存当前页面路径，以便登录成功后返回
                        const pages = getCurrentPages();
                        if (pages && pages.length > 0) {
                            const currentPage = pages[pages.length - 1];
                            const currentPath = currentPage.route;
                            const currentParams = currentPage.options;
                            let redirectUrl = `/${currentPath}`;
                            
                            // 拼接参数
                            const paramStr = Object.keys(currentParams)
                                .map(key => `${key}=${encodeURIComponent(currentParams[key])}`)
                                .join('&');
                            
                            if (paramStr) {
                                redirectUrl += `?${paramStr}`;
                            }
                            
                            // 保存重定向URL到全局或本地存储
                            uni.setStorageSync('redirectAfterLogin', redirectUrl);
                        }
                        
                        // 跳转到登录页面
                        //uni.navigateTo({ url: '/pages/login/login' });
                    }
                    
                    // 抛出未授权错误
                    reject(new Error('未授权'));
                } else {
                    // 其他错误状态码
                    if (!customOptions.silent) {
                        uni.showToast({ title: res.data?.message || '请求失败', icon: 'none' });
                    }
                    reject(new Error(res.data?.message || `请求失败，状态码: ${res.statusCode}`));
                }
            },
            fail: (error) => {
                console.error('请求失败:', error);
                // 仅当不是未登录错误且不是静默请求时才显示网络错误提示
                if (!customOptions.silent && !error.isNotLoggedIn) {
                    uni.showToast({ title: '网络连接失败', icon: 'none' });
                }
                reject(error);
            }
        });
    });
}

/**
 * 封装uni.request，提供常用HTTP方法的便捷访问
 */
// 创建一个可直接调用的request函数
function request(options, customOptions = {}) {
    return _request(options, customOptions);
}

// 为request函数添加各种HTTP方法
request.request = _request;
request.get = function(url, data, customOptions = {}) {
    return _request({
        url,
        method: 'GET',
        data
    }, customOptions);
};
request.post = function(url, data, customOptions = {}) {
    return _request({
        url,
        method: 'POST',
        data
    }, customOptions);
};
request.put = function(url, data, customOptions = {}) {
    return _request({
        url,
        method: 'PUT',
        data
    }, customOptions);
};
request.delete = function(url, data, customOptions = {}) {
    return _request({
        url,
        method: 'DELETE',
        data
    }, customOptions);
};

// 默认导出request函数
// 确保checkLoginStatusSync函数、logout在默认导出中也可用
request.checkLoginStatusSync = checkLoginStatusSync;
request.logout = logout;

export default request;