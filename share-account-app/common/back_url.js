// back_url.js
const config = {
    // 后端服务器的基础URL
    //baseUrl: 'http://localhost:8999/shareaccount',  // dev
    baseUrl: 'https://www.shangmao.site/shareaccount',  // prod
	
    // 定义各个API接口的相对路径
    endpoints: {
		getAllEnums: '/base/getAllEnums',
        login_login: '/login/login',
        login_guest: '/login/guest',
        // 新增：游客升级为正式用户
        login_upgrade: '/login/upgrade',
        getUserInfo: '/users/getById/',
		findUserList: '/users/findUserList',
		saveUser: '/users/save',
		setRole: '/users/setRole',
		getConfigByType: '/base/getConfigByType/',
		getFeatureList: '/base/getFeatureList',
		fileUpload: '/file/fileUpload',
		
		class_getById: '/class/getById/',
		class_getByParentId: '/class/getByParentId/',
		class_getAll: '/class/getAll',
		class_getByStatus: '/class/getByStatus/',
		class_save: '/class/save',
		class_updateStatus: '/class/updateStatus/',
		
		account_getById : '/account/getById/',
		account_getAll : '/account/getAll',
		account_save : '/account/save',
		account_delete : '/account/delete/',
		account_updateStatus : '/account/updateStatus/',

		// 账本相关接口
		ledger_getById: '/ledger/getById/',
		ledger_getByUser: '/ledger/getByUser',
		ledger_getByStatus: '/ledger/getByStatus/',
		ledger_save: '/ledger/save',
		ledger_updateStatus: '/ledger/updateStatus',
		ledger_delete: '/ledger/delete/',
		ledger_getSharedToMe: '/ledger/getSharedToMe',

		// 账单相关接口
		bill_getById: '/bill/getById/',
		bill_getByUserId: '/bill/getByUserId',
		bill_getByLedgerId: '/bill/getByLedgerId',
		bill_save: '/bill/save',
		bill_deleteById: '/bill/deleteById/',
		bill_getMonthlyStatisticsByLedgerId: '/bill/getMonthlyStatisticsByLedgerId',
		bill_getUserMonthlyTotalExpense: '/bill/getUserMonthlyTotalExpense',
		bill_listWithPagination: '/bill/listWithPagination', // 分页查询用户账单列表（支持多条件筛选）
		bill_updateStatus: '/bill/updateStatus', // 更新账单状态
        bill_export: '/bill/export', // 导出账单为PDF
        bill_ai_recognizeBillPic: '/api/ai/recognize/bill/pic', // 调用豆包API识别账单图片
        bill_ai_recognizeBillChat: '/api/ai/recognize/bill/chat', // 调用豆包API识别账单聊天

		// 账单评论接口
		bill_commentsByBillId: '/bill/comments/',
		bill_comment_save: '/bill/comment/save',
		bill_comment_deleteById: '/bill/comment/delete/',
        bill_comment_getById: '/bill/comment/',

        // 主题/皮肤相关接口
        theme_freeColors: '/theme/freeColors',
        theme_skins: '/theme/skins',
        theme_skins_vip: '/theme/skins/vip',
        theme_current: '/theme/current',
        theme_apply: '/theme/apply',

        // 预算相关接口
        budget_list: '/budget/getByYearAndMonth',
        budget_getByYearAndMonthAndLedger: '/budget/getByYearAndMonthAndLedger',
        budget_getById: '/budget/getById/',
        budget_update: '/budget/update',
        budget_save: '/budget/save',
        budget_create: '/budget/create',
        budget_check: '/budget/check',
        budget_item_getById: '/budgetItem/getById/',
        budget_item_getByBudgetId: '/budgetItem/getByBudgetId/',
        budget_item_checkLimit: '/budgetItem/checkLimit',
        budget_item_create: '/budgetItem/create',
        budget_item_update: '/budgetItem/update',
        budget_item_updateStatus: '/budgetItem/updateStatus',
        budget_item_save: '/budgetItem/save',
        budget_syncLastMonth: '/budget/syncLastMonth',
        
        // 统计相关接口
        statistics_getMultiThreadStatistics: '/statistics/getMultiThreadStatistics',
        
        // 成员管理相关接口
        member_getById: '/member/getById/',
        member_getByLedgerId: '/member/getByLedgerId/',
        member_save: '/member/save',
        member_updatePercentage: '/member/updatePercentage',
        member_delete: '/member/delete/',
        member_deleteByLedgerIdAndUserId: '/member/deleteByLedgerIdAndUserId',
        member_getTotalPercentageByLedgerId: '/member/getTotalPercentageByLedgerId/',
        member_list: '/member/list',
        member_acceptInvitation: '/member/acceptInvitation',
        member_getNormalByUser: '/member/getNormalByUser',
        
        // 消息相关接口
        message_latest: '/api/message/latest',
        message_list: '/api/message/list',
        message_advancedList: '/api/message/advancedList',
        message_read: '/api/message/{id}/read',
        message_readAll: '/api/message/all/read',
        message_unreadCount: '/api/message/unread/count',
        message_detail: '/api/message/{id}',
        message_save: '/api/message',
        message_updateStatus: '/api/message/updateStatus',
        
        // 模板消息相关接口
        templateMessage_configs: '/api/template-message/configs',
        templateMessage_check: '/api/template-message/check',
        templateMessage_updateAuthorization: '/api/template-message/update-authorization',
        templateMessage_setNeverRemind: '/api/template-message/set-never-remind',

        // 支付相关接口
        payment_createPayment: '/api/payment/v3/createPayment',
        payment_getPayInfo: '/api/payment/v3/payinfo/',
        payment_checkStatus: '/api/payment/v3/checkstatus/',
        
        // 会员套餐相关接口
        membership_packages: '/api/membership/packages',

        // 用户会员相关接口
        userMember_queryUserMembers: '/api/user/member/queryUserMembers',
        userMember_getRemainingAiCount: '/api/user/member/getRemainingAiCount',
        userMember_getRemainingPdfCount: '/api/user/member/getRemainingPdfCount',
        
        // 定时记账相关接口
        scheduledBill_list: '/api/scheduledBill/list',
        scheduledBill_getById: '/api/scheduledBill/',
        scheduledBill_save: '/api/scheduledBill/save',
        scheduledBill_updateStatus: '/api/scheduledBill/',
        scheduledBill_delete: '/api/scheduledBill/',
        scheduledBill_log_list: '/api/scheduledBill/',
        scheduledBill_log_getById: '/api/scheduledBill/log/'
    }
};

export default config;