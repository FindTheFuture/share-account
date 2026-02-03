import request from './request.js';
import backUrl from './back_url.js';

/**
 * 消息服务 - 封装模板消息授权管理功能
 */
const messageService = {
    // 模板ID常量（初始为空，将从后端加载）
    TEMPLATE_IDS: {},
    
    // 最后检查微信授权状态的时间戳，用于限制频繁检查
    lastWeChatAuthCheck: 0,
    
    // 微信授权状态缓存
    weChatAuthCache: null,
    
    // 检查间隔（5分钟）
    AUTH_CHECK_INTERVAL: 5 * 60 * 1000,
    
    // 本地存储键名
    STORAGE_KEYS: {
        LAST_AUTHORIZATION_DATE: 'share_account_last_authorization_date'
    },
    
    /**
     * 检查今天是否已经显示过授权面板
     */
    isTodayAlreadyShowedAuthorization() {
        try {
            const today = new Date();
            const todayStr = `${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`;
            
            const lastDate = wx.getStorageSync(this.STORAGE_KEYS.LAST_AUTHORIZATION_DATE);
            return lastDate === todayStr;
        } catch (error) {
            console.error('检查授权显示记录失败:', error);
            return false;
        }
    },
    
    /**
     * 记录今天已显示授权面板
     */
    setTodayShowedAuthorization() {
        try {
            const today = new Date();
            const todayStr = `${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`;
            
            wx.setStorageSync(this.STORAGE_KEYS.LAST_AUTHORIZATION_DATE, todayStr);
        } catch (error) {
            console.error('设置授权显示记录失败:', error);
        }
    },
    
    /**
     * 从后端加载模板ID配置
     */
    async loadTemplateConfigs() {
        try {
            const result = await request.get(backUrl.endpoints.templateMessage_configs, {}, { silent: true });
            if (result) {
                this.TEMPLATE_IDS = result;
                return true;
            }
            return false;
        } catch (error) {
            console.error('加载模板配置失败:', error);
            // 出错时不设置默认值
            return false;
        }
    },
    
    /**
     * 获取微信实际的订阅消息授权状态
     * 使用wx.getSetting withSubscriptions参数获取实际授权状态
     */
    async getWeChatSubscriptionStatus() {
        try {
            // 检查缓存是否有效
            const now = Date.now();
            if (this.weChatAuthCache && (now - this.lastWeChatAuthCheck) < this.AUTH_CHECK_INTERVAL) {
                return this.weChatAuthCache;
            }
            
            const { getSetting } = wx;
            const res = await new Promise((resolve, reject) => {
                getSetting({
                    withSubscriptions: true,
                    success: resolve,
                    fail: reject
                });
            });
            
            // 缓存结果并更新时间戳
            this.weChatAuthCache = res.subscriptionsSetting || { mainSwitch: false, itemSettings: {} };
            this.lastWeChatAuthCheck = now;
            
            return this.weChatAuthCache;
        } catch (error) {
            console.error('获取微信授权状态失败:', error);
            return { mainSwitch: false, itemSettings: {} };
        }
    },
    
    /**
     * 检查用户是否已授权模板
     * 支持单个或多个模板ID的授权状态检查
     * 结合后端状态和微信实际授权状态
     */
    async checkAuthorization(templateIds) {
        try {
            // 确保templateIds是数组
            const ids = Array.isArray(templateIds) ? templateIds : [templateIds];
            
            // 获取微信实际的授权状态
            const weChatStatus = await this.getWeChatSubscriptionStatus();
            // 确保itemSettings存在
            weChatStatus.itemSettings = weChatStatus.itemSettings || {};
            
            // 如果是单个模板ID，使用单个请求
            if (ids.length === 1) {
                const templateId = ids[0];
                const result = await request.get(backUrl.endpoints.templateMessage_check, {
                    templateId: templateId
                }, { silent: true });
                
                // 结合微信实际状态，如果微信显示拒绝，则覆盖后端状态
                // 安全地获取微信状态
                const weChatItemStatus = weChatStatus.itemSettings && weChatStatus.itemSettings[templateId];
                if (weChatItemStatus === 'reject') {
                    result.isAuthorized = false;
                    // 确保授权次数为0
                    result.authorizeCount = 0;
                }
                
                return result;
            }
            
            // 多个模板ID时，使用Promise.all并发请求所有模板的授权状态
            const promises = ids.map(templateId => 
                request.get(backUrl.endpoints.templateMessage_check, {
                    templateId: templateId
                }, { silent: true })
            );
            
            const results = await Promise.all(promises);
            
            // 将结果转换为Map形式，templateId作为key
            const formattedResults = {};
            ids.forEach((templateId, index) => {
                const result = results[index] || {
                    isAuthorized: false,
                    neverRemind: false,
                    authorizeCount: 0
                };
                
                // 结合微信实际状态，如果微信显示拒绝，则覆盖后端状态
                // 安全地获取微信状态
                const weChatItemStatus = weChatStatus.itemSettings && weChatStatus.itemSettings[templateId];
                if (weChatItemStatus === 'reject') {
                    result.isAuthorized = false;
                    result.authorizeCount = 0;
                }
                
                formattedResults[templateId] = result;
            });
            
            return formattedResults;
        } catch (error) {
            console.error('检查授权状态失败:', error);
            return {
                isAuthorized: false,
                neverRemind: false,
                authorizeCount: 0
            };
        }
    },
    
    /**
     * 请求模板消息授权
     * 支持单个或多个模板ID的授权请求，一次性请求所有需要授权的模板
     */
    async requestAuthorization(templateIds, existingAuthStatus = null) {
        try {
            // 确保templateIds是数组
            const ids = Array.isArray(templateIds) ? templateIds : [templateIds];
            const results = {};
            
            // 使用传入的授权状态或一次性检查所有模板的授权状态
            const allAuthStatus = existingAuthStatus || await this.checkAuthorization(ids);
            
            // 筛选出需要授权的模板ID（授权次数为0且未设置不再提醒）
            const templatesToAuthorize = [];
            for (const templateId of ids) {
                // 获取当前模板的授权状态
                const authStatus = ids.length === 1 ? allAuthStatus : allAuthStatus[templateId];
                
                // 如果用户设置了不再提醒
                if (authStatus.neverRemind) {
                    results[templateId] = { authorized: false, reason: 'user_never_remind' };
                    continue;
                }
                
                // 如果还有授权次数
                if (authStatus.authorizeCount > 0) {
                    results[templateId] = { authorized: true, authorizeCount: authStatus.authorizeCount };
                    continue;
                }
                
                // 需要请求授权的模板
                templatesToAuthorize.push(templateId);
            }
            
            // 如果没有需要授权的模板，直接返回结果
            if (templatesToAuthorize.length === 0) {
                return Array.isArray(templateIds) ? results : results[templateIds];
            }
            
            // 一次性请求所有需要授权的模板
            const { requestSubscribeMessage } = wx;
            const res = await new Promise((resolve, reject) => {
                requestSubscribeMessage({
                    tmplIds: templatesToAuthorize,
                    success: resolve,
                    fail: reject
                });
            });
            
            // 处理每个模板的授权结果
            await Promise.all(templatesToAuthorize.map(async (templateId) => {
                const status = res[templateId];
                const isAuthorized = status === 'accept';
                
                // 保存授权状态到后端
                if (isAuthorized) {
                    await this.saveAuthorizationStatus(templateId, true);
                }
                
                results[templateId] = { authorized: isAuthorized };
            }));
            
            return Array.isArray(templateIds) ? results : results[templateIds];
        } catch (error) {
            console.error('请求模板消息授权失败:', error);
            
            // 返回失败结果
            const ids = Array.isArray(templateIds) ? templateIds : [templateIds];
            const errorResults = {};
            for (const templateId of ids) {
                errorResults[templateId] = { authorized: false, reason: 'request_failed' };
            }
            
            return Array.isArray(templateIds) ? errorResults : errorResults[templateIds];
        }
    },
    
    /**
     * 保存授权状态到后端
     */
    async saveAuthorizationStatus(templateId, isAuthorized) {
        try {
            await request.post(backUrl.endpoints.templateMessage_updateAuthorization, {
                templateId: templateId,
                isAuthorized: isAuthorized
            }, { silent: true });
        } catch (error) {
            // 授权失败后不做错误处理，不记录日志
        }
    },
    
    /**
     * 设置不再提醒
     */
    async setNeverRemind(templateId, neverRemind) {
        try {
            await request.post(backUrl.endpoints.templateMessage_setNeverRemind, {
                templateId: templateId,
                neverRemind: neverRemind
            }, { silent: true });
        } catch (error) {
            console.error('设置不再提醒失败:', error);
        }
    },
    
    /**
     * 显示授权对话框，智能决定是否需要请求授权
     */
    async showAuthorizationDialog(templateId) {
        try {
            const authStatus = await this.checkAuthorization(templateId);
            
            // 用户设置了不再提醒，直接返回false
            if (authStatus.neverRemind) {
                return false;
            }
            
            // 如果授权次数充足，直接返回true
            if (authStatus.authorizeCount > 0) {
                return true;
            }
            
            // 提示用户需要授权
            const confirm = await new Promise((resolve) => {
                wx.showModal({
                    title: '账本通知',
                    content: '是否允许接收账本相关通知？',
                    success: (res) => resolve(res.confirm)
                });
            });
            
            if (!confirm) {
                return false;
            }
            
            // 请求微信授权
            const authResult = await this.manualRequestAuthorizationNoCheck();
            
            if (!authResult.authorized) {
                wx.showToast({
                    title: '授权失败，无法发送消息',
                    icon: 'none'
                });
            }
            
            return authResult.authorized;
        } catch (error) {
            console.error('显示授权对话框失败:', error);
            wx.showToast({
                title: '授权检查失败',
                icon: 'none'
            });
            return false;
        }
    },
    
    /**
     * 在指定页面初始化授权检查
     */
    async initPageAuthorization(pageName) {
        // 确保模板配置已加载
        const configsLoaded = await this.loadTemplateConfigs();
        
        // 如果模板配置未加载成功，不进行后续操作
        if (!configsLoaded || Object.keys(this.TEMPLATE_IDS).length === 0) {
            return {
                dailyStatus: { isAuthorized: false, neverRemind: false },
                hourlyStatus: { isAuthorized: false, neverRemind: false }
            };
        }
        
        // 获取所有模板ID
        const allTemplateIds = Object.values(this.TEMPLATE_IDS);
        
        // 一次性检查所有模板的授权状态
        const allAuthStatus = await this.checkAuthorization(allTemplateIds);
        
        // 首页首次打开时的特殊逻辑
        if (pageName === 'firstpage') {
            // 获取微信实际的授权状态
            const weChatStatus = await this.getWeChatSubscriptionStatus();
            
            // 检查消息订阅总开关是否关闭
            if (!weChatStatus.mainSwitch) {
                console.log('消息订阅总开关关闭，直接打开设置页面');
                // 总开关关闭时直接跳转设置页面
                wx.openSetting({
                    success: (res) => {
                        console.log('设置页面打开成功', res);
                    },
                    fail: (err) => {
                        console.error('设置页面打开失败', err);
                    }
                });
                return {
                    dailyStatus: { isAuthorized: false, neverRemind: false },
                    hourlyStatus: { isAuthorized: false, neverRemind: false }
                };
            }
            
            // 收集所有需要授权的模板ID
            const templatesToAuthorize = [];
            
            for (const templateId of allTemplateIds) {
                const authStatus = allAuthStatus[templateId];
                // 安全地获取微信状态
                const weChatItemStatus = weChatStatus.itemSettings && weChatStatus.itemSettings[templateId];
                
                // 如果用户设置了不再提醒，跳过
                if (authStatus.neverRemind) {
                    continue;
                }
                
                // 严格判断未授权的情况：
                // 1. 微信状态为reject（用户明确拒绝）
                // 2. 微信状态为ban（被微信禁止）
                // 3. 微信状态不存在（从未授权过或未勾选"总是保持以上选择"）且后端授权次数为0
                // 注意：根据微信文档，withSubscriptions只返回用户勾选过"总是保持以上选择，不再询问"的订阅消息
                // 所以weChatItemStatus不存在可能有两种情况：
                // - 用户从未授权过该模板
                // - 用户授权过但未勾选"总是保持以上选择"
                const isWeChatRejected = weChatItemStatus === 'reject' || weChatItemStatus === 'ban';
                const isNeedAuthFromBackend = authStatus.authorizeCount <= 0;
                
                if (isWeChatRejected || (!weChatItemStatus && isNeedAuthFromBackend)) {
                    templatesToAuthorize.push(templateId);
                }
            }
            
            console.log('检测到需要授权的模板数量:', templatesToAuthorize.length, '模板ID:', templatesToAuthorize);
            
            // 如果有需要授权的模板，只显示一次弹窗
            if (templatesToAuthorize.length > 0) {
                // 一次性请求所有需要授权的模板，传入已获取的授权状态避免重复查询
                await this.showBatchAuthorizationDialog(templatesToAuthorize, allAuthStatus);
            }
        }
        
        // 返回日报和小时报的状态（保持向后兼容）
        return {
            dailyStatus: allAuthStatus[this.TEMPLATE_IDS.dailyReport] || { isAuthorized: false, neverRemind: false },
            hourlyStatus: allAuthStatus[this.TEMPLATE_IDS.hourlyReport] || { isAuthorized: false, neverRemind: false }
        };
    },
    
    /**
     * 批量显示授权对话框
     */
    async showBatchAuthorizationDialog(templateIds, existingAuthStatus = null) {
        try {
            // 检查今天是否已经显示过授权面板
            if (this.isTodayAlreadyShowedAuthorization()) {
                console.log('今天已经显示过授权面板，不再重复显示');
                return false;
            }
            
            // 提示用户需要授权
            const confirm = await new Promise((resolve) => {
                wx.showModal({
                    title: '账本通知',
                    content: '是否允许接收账本相关通知？',
                    success: (res) => resolve(res.confirm)
                });
            });
            
            if (!confirm) {
                // 用户点击取消，清空缓存以便下次检查
                this.weChatAuthCache = null;
                // 即使用户取消，也要记录今天已显示过，避免重复打扰
                this.setTodayShowedAuthorization();
                return false;
            }
            
            // 记录今天已显示授权面板
            this.setTodayShowedAuthorization();
            
            // 一次性请求所有需要授权的模板，传入已获取的授权状态避免重复查询
            const results = await this.requestAuthorization(templateIds, existingAuthStatus);
            
            // 统计成功授权的数量
            let successCount = 0;
            for (const templateId of templateIds) {
                if (results[templateId]?.authorized) {
                    successCount++;
                }
            }
            
            // 授权后清空缓存，下次检查获取最新状态
            this.weChatAuthCache = null;
            
            return successCount > 0;
        } catch (error) {
            console.error('批量显示授权对话框失败:', error);
            return false;
        }
    },
    
    /**
     * 手动请求所有需要的消息授权 - 会判断是否需要授权
     * 合并多个模板ID的授权请求，一次性请求
     */
    async manualRequestAuthorization() {
        try {
            // 确保模板配置已加载
            const configsLoaded = await this.loadTemplateConfigs();
            
            // 如果模板配置未加载成功，不进行后续操作
            if (!configsLoaded || Object.keys(this.TEMPLATE_IDS).length === 0) {
                wx.showToast({
                    title: '模板配置加载失败',
                    icon: 'none'
                });
                return [];
            }
            
            // 获取所有需要授权的模板ID
            const allTemplateIds = Object.values(this.TEMPLATE_IDS);
            
            // 一次性检查所有模板的授权状态
            const allAuthStatus = await this.checkAuthorization(allTemplateIds);
            
            // 筛选需要授权的模板（授权次数为0且未设置不再提醒）
            const templatesToAuthorize = [];
            for (const templateId of allTemplateIds) {
                const authStatus = allAuthStatus[templateId];
                if (authStatus.authorizeCount <= 0 && !authStatus.neverRemind) {
                    templatesToAuthorize.push(templateId);
                }
            }
            
            // 如果没有需要授权的模板，直接返回
            if (templatesToAuthorize.length === 0) {
                wx.showToast({
                    title: '无需授权',
                    icon: 'none'
                });
                return [];
            }
            
            // 一次性请求所有需要授权的模板
            const results = await this.requestAuthorization(templatesToAuthorize);
            
            // 统计成功授权的数量
            let successCount = 0;
            const authorizedResults = [];
            
            for (const templateId of templatesToAuthorize) {
                const isAuthorized = results[templateId]?.authorized || false;
                if (isAuthorized) {
                    successCount++;
                }
                authorizedResults.push({ templateId, authorized: isAuthorized });
            }
            
            return authorizedResults;
        } catch (error) {
            console.error('手动请求授权失败:', error);
            wx.showToast({
                title: '请求授权失败',
                icon: 'none'
            });
            return [];
        }
    },



    /**
     * 手动请求所有需要的消息授权 - 不判断是否需要授权，直接发起授权
     * 合并多个模板ID的授权请求，一次性请求
     * 用于用户中心的通知设置卡片点击后调用
     */
    async manualRequestAuthorizationNoCheck() {
        try {
            // 确保模板配置已加载
            const configsLoaded = await this.loadTemplateConfigs();
            
            // 如果模板配置未加载成功，不进行后续操作
            if (!configsLoaded || Object.keys(this.TEMPLATE_IDS).length === 0) {
                wx.showToast({
                    title: '模板配置加载失败',
                    icon: 'none'
                });
                return [];
            }
            
            // 获取所有需要授权的模板ID
            const allTemplateIds = Object.values(this.TEMPLATE_IDS);
            
            // 清除缓存，确保获取最新的授权状态
            this.weChatAuthCache = null;
            
            // 获取微信实际的授权状态
            const weChatStatus = await this.getWeChatSubscriptionStatus();
            // 确保itemSettings存在
            weChatStatus.itemSettings = weChatStatus.itemSettings || {};
            
            // 检查是否有任何模板ID处于ban状态
            let hasBanStatus = false;
            console.log('微信授权状态:', weChatStatus);
            for (const templateId of allTemplateIds) {
                const weChatItemStatus = weChatStatus.itemSettings && weChatStatus.itemSettings[templateId];
                console.log('模板ID:', templateId, '状态:', weChatItemStatus);
                if (weChatItemStatus === 'ban' || weChatItemStatus === 'reject') {
                    hasBanStatus = true;
                    console.log('检测到ban状态，将打开设置页面');
                    break;
                }
            }
            
            // 如果有ban状态，直接打开设置页面
            if (hasBanStatus) {
                console.log('准备打开微信设置页面');
                // 为了确保兼容性，使用setTimeout延迟执行
                wx.openSetting({
                    success: (res) => {
                        console.log('设置页面打开成功', res);
                    },
                    fail: (err) => {
                        console.error('设置页面打开失败', err);
                        // 如果wx.openSetting失败，尝试使用更兼容的方式
                        if (wx.canIUse('openSetting')) {
                            wx.openSetting();
                        }
                    }
                });
                return [];
            }
            
            // 收集所有需要授权的模板
            const templatesToCheck = allTemplateIds;

            // 直接请求所有模板授权
            const { requestSubscribeMessage } = wx;
            const res = await new Promise((resolve, reject) => {
                requestSubscribeMessage({
                    tmplIds: templatesToCheck,
                    success: resolve,
                    fail: reject
                });
            });
            
            // 处理每个模板的授权结果
            const authorizedResults = [];
            await Promise.all(templatesToCheck.map(async (templateId) => {
                const status = res[templateId];
                const isAuthorized = status === 'accept';
                
                // 只有当状态为accept时才对接后台增加授权次数
                if (status === 'accept') {
                    await this.saveAuthorizationStatus(templateId, true);
                } else if (status === 'reject') {
                    // 如果用户拒绝了授权，确保neverRemind设置为false，以便用户可以再次尝试授权
                    await this.setNeverRemind(templateId, false);
                }
                
                authorizedResults.push({ templateId, authorized: isAuthorized });
            }));
            
            // 清空缓存，下次检查获取最新状态
            this.weChatAuthCache = null;
            
            return authorizedResults;
        } catch (error) {
            console.error('手动请求授权失败:', error);
            wx.showToast({
                title: '请求授权失败',
                icon: 'none'
            });
            
            // 出错时也清空缓存，避免缓存错误状态
            this.weChatAuthCache = null;
            
            return [];
        }
    }
};

// 导出服务
export default messageService;