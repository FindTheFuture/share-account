import backUrlConfig from './back_url.js';
import request from './request.js';

/**
 * 清除所有登录态相关的缓存数据
 */
export function clearLoginState() {
    uni.removeStorageSync('token');
    uni.removeStorageSync('additionalId');
    uni.removeStorageSync('expireAt');
    uni.removeStorageSync('thunder');
    uni.removeStorageSync('canSendMessage');
    uni.removeStorageSync('isGuest');
    uni.removeStorageSync('guideCard');
}

/**
 * 检查用户是否已登录
 * 已登录条件（必须同时满足）：
 *   1. isGuest = false（不是游客模式）
 *   2. 有 token
 *   3. 有 additionalId
 *   4. 用户存在（API验证通过）
 *
 * 未登录条件（满足任意一种）：
 *   1. isGuest = true（游客模式）
 *   2. 没有 token
 *   3. 没有 additionalId
 *   4. token 和 additionalId 都有但用户不存在
 *
 * @returns {Promise<{loggedIn: boolean, reason: string}>}
 */
export async function isLoggedIn() {
    try {
        const token = uni.getStorageSync('token');
        const additionalId = uni.getStorageSync('additionalId');
        const isGuest = uni.getStorageSync('isGuest');

        // 条件1: 游客模式 = true → 未登录
        if (isGuest) {
            return { loggedIn: false, reason: '游客模式' };
        }

        // 条件2: 没有 token → 未登录
        if (!token) {
            return { loggedIn: false, reason: '没有token' };
        }

        // 条件3: 没有 additionalId → 未登录
        if (!additionalId) {
            return { loggedIn: false, reason: '没有additionalId' };
        }

        // 条件4: 验证用户是否存在
        try {
            const exists = await request({
                url: `${backUrlConfig.baseUrl}${backUrlConfig.endpoints.userExists}${additionalId}`,
                method: 'GET'
            });
            if (exists) {
                return { loggedIn: true, reason: '用户存在', userId: additionalId };
            }

            // 用户不存在
            return { loggedIn: false, reason: '用户不存在' };
        } catch (e) {
            // API 调用失败，视为用户不存在
            console.warn('isLoggedIn: false - 验证用户存在性失败:', e);
            return { loggedIn: false, reason: 'API验证失败' };
        }
    } catch (error) {
        console.error('isLoggedIn 检查异常:', error);
        return { loggedIn: false, reason: '检查异常' };
    }
}

/**
 * 同步检查用户是否已登录（仅检查本地存储，无法验证用户是否真实存在）
 * 用途：用于页面初始化时快速判断，避免白屏
 *
 * @returns {boolean}
 */
export function isLoggedInSync() {
    const token = uni.getStorageSync('token');
    const additionalId = uni.getStorageSync('additionalId');
    const isGuest = uni.getStorageSync('isGuest');

    return !!(token && additionalId && !isGuest);
}

export default { isLoggedIn, isLoggedInSync, clearLoginState };
