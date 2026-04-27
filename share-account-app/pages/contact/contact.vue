<template>
    <view class="page-container">
        <!-- 未登录状态 -->
        <view v-if="!isLoggedIn" class="login-state">
            <view class="login-icon-wrapper">
                <text class="login-icon">👤</text>
            </view>
            <text class="login-title">登录后查看联系人</text>
            <text class="login-desc">登录后将显示您的联系人列表</text>
            <button class="login-btn" @click="goToLogin">去登录</button>
        </view>

        <!-- 已登录状态 -->
        <template v-else>
            <!-- 顶部导航 -->
            <view class="header-section">
                <view class="search-container">
                    <view class="search-wrapper">
                        <text class="search-icon">🔍</text>
                        <input
                            class="search-input"
                            v-model="searchKey"
                            type="number"
                            maxlength="11"
                            placeholder="输入手机号搜索"
                            @input="onSearchInput"
                        />
                        <text class="clear-icon" v-if="searchKey" @click="clearSearch">✕</text>
                    </view>
                    <view class="action-btns">
                        <view class="request-btn" @click="goToFriendRequests">
                            <custom-icon type="xiaoxi" :size="18" color="#0bf527" style="font-weight: bold;" />
                            <view class="request-badge" v-if="requestCount > 0">
                                <text class="badge-text">{{ requestCount > 99 ? '99+' : requestCount }}</text>
                            </view>
                        </view>
                        <view class="add-btn" @click="showAddDialog">
                            <text class="add-icon">+</text>
                        </view>
                    </view>
                </view>
            </view>

            <!-- 联系人列表 -->
            <scroll-view class="contact-list" scroll-y>
                <view v-if="displayList.length === 0" class="empty-state">
                    <view class="empty-icon-wrapper">
                        <text class="empty-icon">{{ searchLoading ? '⏳' : (searchKey ? '🔍' : '👥') }}</text>
                    </view>
                    <text class="empty-title">{{ searchLoading ? '搜索中...' : (searchKey ? '未找到该用户' : '暂无联系人') }}</text>
                    <text class="empty-desc">{{ searchKey ? '换个手机号试试' : '点击右上角添加好友' }}</text>
                </view>

                <view v-else class="contact-groups">
                    <view
                        v-for="(group, letter) in groupedFilteredContacts"
                        :key="letter"
                        class="contact-group"
                    >
                        <view class="index-bar">{{ letter }}</view>
                        <view class="contact-items">
                            <view
                                v-for="contact in group"
                                :key="contact.userId"
                                class="contact-item"
                                @click="goToChat(contact)"
                            >
                                <view class="avatar-container">
                                    <user-avatar :avatar-url="contact.avatarUrl || ''" :uploadable="false" :clickable="false" size="104rpx"></user-avatar>
                                </view>
                                <view class="contact-info">
                                    <text class="contact-name">{{ contact.nickName || '未设置昵称' }}</text>
                                    <text class="contact-phone">{{ contact.phone }}</text>
                                </view>
                                <view class="unread-badge" v-if="contact.unreadCount > 0">
                                    <text class="badge-text">{{ contact.unreadCount > 99 ? '99+' : contact.unreadCount }}</text>
                                </view>
                            </view>
                        </view>
                    </view>
                </view>
            </scroll-view>
        </template>

        <!-- 添加联系人弹窗 -->
        <view class="dialog-mask" v-if="showDialog" @click="hideAddDialog">
            <view class="dialog-card" @click.stop>
                <view class="dialog-header">
                    <text class="dialog-title">添加联系人</text>
                    <text class="dialog-close" @click="hideAddDialog">✕</text>
                </view>
                <view class="dialog-body">
                    <view class="input-group">
                        <input
                            class="phone-input"
                            v-model="addPhone"
                            type="number"
                            maxlength="11"
                            placeholder="输入对方手机号"
                            @input="onAddPhoneInput"
                        />
                    </view>
                    <view v-if="searchUserResult" class="user-card">
                        <view class="user-avatar-wrapper">
                            <user-avatar :avatar-url="searchUserResult.pictureAddress" :uploadable="false" :clickable="false" size="112rpx"></user-avatar>
                        </view>
                        <view class="user-info">
                            <text class="user-name">{{ searchUserResult.nickName || '未设置昵称' }}</text>
                            <text class="user-phone">{{ searchUserResult.phone }}</text>
                        </view>
                    </view>
                    <view v-if="searchUserError" class="error-tip">
                        <text>{{ searchUserError }}</text>
                    </view>
                </view>
                <view class="dialog-footer">
                    <button class="btn-cancel" @click="hideAddDialog">取消</button>
                    <button class="btn-confirm" :disabled="!canAddContact" @click="confirmAddContact">
                        {{ addSuccess ? '已发送' : '添加' }}
                    </button>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
import UserAvatar from '@/components/user-avatar.vue'
import { isLoggedIn, clearLoginState } from '@/common/isLoggedIn.js'

export default {
    components: {
        UserAvatar
    },
    data() {
        return {
            contacts: [],
            requestCount: 0,
            pollingTimer: null,
            isLoggedIn: false,
            searchKey: '',
            showDialog: false,
            addPhone: '',
            searchUserResult: null,
            searchUserError: '',
            addSuccess: false,
            searchResult: null,
            searchLoading: false
        }
    },
    computed: {
        filteredContacts() {
            if (!this.searchKey) {
                return this.contacts
            }
            if (this.searchResult) {
                return []
            }
            return this.contacts.filter(contact => {
                const phone = contact.phone || ''
                const key = this.searchKey
                return phone.includes(key)
            })
        },
        displayList() {
            if (this.searchKey && this.searchResult) {
                return [this.searchResult]
            }
            return this.filteredContacts
        },
        groupedFilteredContacts() {
            const list = this.displayList
            if (this.searchKey && this.searchResult) {
                return { 'S': list }
            }
            const groups = {}
            list.forEach(contact => {
                const letter = (contact.nickName || '未').charAt(0).toUpperCase()
                if (!groups[letter]) {
                    groups[letter] = []
                }
                groups[letter].push(contact)
            })
            const sortedKeys = Object.keys(groups).sort()
            const sortedGroups = {}
            sortedKeys.forEach(key => {
                sortedGroups[key] = groups[key]
            })
            return sortedGroups
        },
        canAddContact() {
            return this.searchUserResult && !this.searchUserResult.isFriend && !this.addSuccess
        }
    },
    onLoad() {
        //this.checkLoginStatus()
    },
    onUnload() {
        this.stopPolling()
    },
    onShow() {
        this.checkLoginStatus()
    },
    methods: {
        async checkLoginStatus() {
            const result = await isLoggedIn();

            if (result.loggedIn) {
                this.isLoggedIn = true;
                this.stopPolling();
                this.loadContacts();
                this.loadRequestCount();
                this.startPolling();
            } else {
                this.isLoggedIn = false;
                this.stopPolling();
                this.contacts = [];
                this.requestCount = 0;
            }
        },
        goToLogin() {
            uni.navigateTo({
                url: '/pages/login/login'
            })
        },
        clearSearch() {
            this.searchKey = ''
            this.searchResult = null
        },
        async onSearchInput() {
            if (this.searchKey.length < 1) {
                this.searchResult = null
                return
            }

            if (!/^\d+$/.test(this.searchKey)) {
                this.searchResult = null
                return
            }

            this.searchLoading = true
            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.user_findLikePhone,
                    method: 'GET',
                    data: { phone: this.searchKey }
                })
                console.log('user_findLikePhone搜索用户结果:', res)
                if (res && res.length > 0) {
                    const user = res[0]
                    this.searchResult = {
                        userId: user.haoe,
                        nickName: user.nickName,
                        phone: user.phone,
                        pictureAddress: (user.pictureAddress || '').trim()
                    }
                } else {
                    this.searchResult = null
                }
            } catch (e) {
                console.error('搜索用户失败', e)
                this.searchResult = null
            } finally {
                this.searchLoading = false
            }
        },
        showAddDialog() {
            this.showDialog = true
            this.addPhone = ''
            this.searchUserResult = null
            this.searchUserError = ''
            this.addSuccess = false
        },
        hideAddDialog() {
            this.showDialog = false
            this.addPhone = ''
            this.searchUserResult = null
            this.searchUserError = ''
            this.addSuccess = false
        },
        onAddPhoneInput() {
            if (this.addPhone.length === 11) {
                this.searchUserByPhone()
            } else {
                this.searchUserResult = null
                this.searchUserError = ''
            }
        },
        async searchUserByPhone() {
            if (!/^1[3-9]\d{9}$/.test(this.addPhone)) {
                this.searchUserError = '请输入正确的手机号'
                this.searchUserResult = null
                return
            }

            this.searchUserError = ''
            this.searchUserResult = null

            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.user_findByPhone,
                    method: 'GET',
                    data: { phone: this.addPhone }
                })

                if (res && res.haoe) {
                    this.searchUserResult = {
                        ...res,
                        phone: this.addPhone,
                        isFriend: false,
                        pictureAddress: (res.pictureAddress || '').trim()
                    }
                } else {
                    this.searchUserError = '该用户不存在'
                }
            } catch (e) {
                this.searchUserError = '搜索失败，请稍后重试'
            }
        },
        async confirmAddContact() {
            if (!this.canAddContact) return

            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.contact_add,
                    method: 'POST',
                    data: { phone: this.addPhone }
                })
                console.log('contact_add添加联系人结果:', res)
                if (res === true) {
                    this.addSuccess = true
                    this.searchUserResult.isFriend = true
                    uni.showToast({
                        title: '发送请求成功',
                        icon: 'success'
                    })
                    setTimeout(() => {
                        this.hideAddDialog()
                    }, 1500)
                } else {
                    uni.showToast({
                        title: res.message || '添加失败',
                        icon: 'none'
                    })
                }
            } catch (e) {
                console.error('添加联系人失败', e)
                uni.showToast({
                    title: '添加失败',
                    icon: 'none'
                })
            }
        },
        async loadContacts() {
            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.contact_list,
                    method: 'GET'
                })
                this.contacts = res || []
                this.loadUnreadCounts()
            } catch (e) {
                console.error('加载联系人失败', e)
            }
        },
        async loadRequestCount() {
            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.contact_requestCount,
                    method: 'GET'
                })
                this.requestCount = res?.count || 0
            } catch (e) {
                console.error('加载请求数量失败', e)
            }
        },
        async loadUnreadCounts() {
            try {
                for (let contact of this.contacts) {
                    const res = await this.$request({
                        url: this.$backUrlConfig.endpoints.chat_unreadCount + contact.userId,
                        method: 'GET'
                    })
                    this.$set(contact, 'unreadCount', res?.count || 0)
                }
            } catch (e) {
                console.error('加载未读数失败', e)
            }
        },
        goToFriendRequests() {
            uni.navigateTo({
                url: '/pages/contact/friendRequest'
            })
        },
        goToChat(contact) {
            uni.navigateTo({
                url: '/pages/contact/chat?userId=' + contact.userId + '&nickName=' + encodeURIComponent(contact.nickName || '') + '&pictureAddress=' + encodeURIComponent(contact.pictureAddress || '')
            })
        },
        startPolling() {
            this.pollingTimer = setInterval(async () => {
                const result = await isLoggedIn();
                if (result.loggedIn) {
                    this.loadRequestCount();
                } else {
                    this.stopPolling();
                }
            }, 10000)
        },
        stopPolling() {
            if (this.pollingTimer) {
                clearInterval(this.pollingTimer)
                this.pollingTimer = null
            }
        }
    }
}
</script>

<style scoped>
.page-container {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background: linear-gradient(180deg, #fce7f3 0%, #f3e8ff 50%, #e0f2fe 100%);
    overflow-x: hidden;
}

/* 未登录状态 */
.login-state {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60rpx;
}

.login-icon-wrapper {
    width: 200rpx;
    height: 200rpx;
    background: linear-gradient(135deg, #f9a8d4 0%, #c084fc 50%, #7dd3fc 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 48rpx;
    box-shadow: 0 16rpx 48rpx rgba(192, 132, 252, 0.3);
}

.login-icon {
    font-size: 96rpx;
}

.login-title {
    font-size: 44rpx;
    font-weight: 700;
    background: linear-gradient(135deg, #db2777 0%, #9333ea 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 16rpx;
}

.login-desc {
    font-size: 28rpx;
    color: #7c3aed;
    margin-bottom: 64rpx;
    text-align: center;
}

.login-btn {
    width: 360rpx;
    height: 110rpx;
    background: linear-gradient(135deg, #ec4899 0%, #9333ea 100%);
    color: #ffffff;
    border-radius: 55rpx;
    font-size: 34rpx;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 12rpx 32rpx rgba(236, 72, 153, 0.4);
}

.login-btn::after {
    border: none;
}

/* 顶部搜索区域 */
.header-section {
    padding: 24rpx 32rpx;
    padding-top: calc(24rpx + env(safe-area-inset-top));
}

.search-container {
    display: flex;
    align-items: center;
    gap: 20rpx;
}

.search-wrapper {
    flex: 1;
    display: flex;
    align-items: center;
    height: 100rpx;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 50rpx;
    padding: 0 32rpx;
    box-shadow: 0 8rpx 24rpx rgba(168, 85, 247, 0.1);
}

.search-icon {
    font-size: 40rpx;
    margin-right: 16rpx;
}

.search-input {
    flex: 1;
    font-size: 32rpx;
    color: #581c87;
    height: 100%;
}

.clear-icon {
    font-size: 36rpx;
    color: #a855f7;
    padding: 8rpx;
}

.action-btns {
    display: flex;
    align-items: center;
    gap: 16rpx;
}

.request-btn {
    width: 100rpx;
    height: 100rpx;
    background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8rpx 24rpx rgba(6, 182, 212, 0.3);
    position: relative;
}

.request-icon {
    font-size: 44rpx;
}

.request-btn .request-badge {
    position: absolute;
    top: -8rpx;
    right: -8rpx;
    min-width: 40rpx;
    height: 40rpx;
    background: linear-gradient(135deg, #f43f5e 0%, #ef4444 100%);
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 10rpx;
    box-shadow: 0 4rpx 12rpx rgba(244, 63, 94, 0.3);
}

.add-btn {
    width: 100rpx;
    height: 100rpx;
    background: linear-gradient(135deg, #ec4899 0%, #a855f7 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8rpx 24rpx rgba(236, 72, 153, 0.3);
}

.add-icon {
    font-size: 48rpx;
    color: #ffffff;
    font-weight: 300;
}

.request-badge {
    min-width: 48rpx;
    height: 48rpx;
    background: linear-gradient(135deg, #f43f5e 0%, #ef4444 100%);
    border-radius: 24rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16rpx;
    box-shadow: 0 4rpx 12rpx rgba(244, 63, 94, 0.3);
}

.request-badge .badge-text {
    color: #ffffff;
    font-size: 26rpx;
    font-weight: 700;
}

/* 联系人列表 */
.contact-list {
    flex: 1;
    overflow-y: auto;
    padding: 0 24rpx;
    width: 100%;
    box-sizing: border-box;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding-top: 240rpx;
}

.empty-icon-wrapper {
    width: 200rpx;
    height: 200rpx;
    background: linear-gradient(135deg, #fce7f3 0%, #e0f2fe 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 48rpx;
    box-shadow: 0 12rpx 40rpx rgba(168, 85, 247, 0.15);
}

.empty-icon {
    font-size: 96rpx;
}

.empty-title {
    font-size: 40rpx;
    font-weight: 700;
    color: #7c3aed;
    margin-bottom: 16rpx;
}

.empty-desc {
    font-size: 28rpx;
    color: #a855f7;
}

.contact-groups {
    padding-bottom: 40rpx;
    max-width: 100%;
    box-sizing: border-box;
}

.contact-group {
    margin-top: 24rpx;
    max-width: 100%;
    box-sizing: border-box;
}

.index-bar {
    padding: 16rpx 32rpx;
    font-size: 28rpx;
    font-weight: 700;
    color: #ffffff;
    background: linear-gradient(90deg, #c084fc 0%, #7dd3fc 100%);
    border-radius: 16rpx 16rpx 0 0;
}

.contact-items {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 0 0 24rpx 24rpx;
    overflow: hidden;
    box-shadow: 0 8rpx 32rpx rgba(168, 85, 247, 0.1);
    overflow-x: hidden;
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
}

.contact-item {
    display: flex;
    align-items: center;
    padding: 32rpx 32rpx;
    background: rgba(255, 255, 255, 0.95);
    transition: all 0.2s;
    max-width: 100%;
    box-sizing: border-box;
}

.contact-item:active {
    background: linear-gradient(90deg, rgba(252, 231, 243, 0.5) 0%, rgba(224, 242, 254, 0.5) 100%);
}

.contact-item:not(:last-child) {
    border-bottom: 2rpx solid rgba(192, 132, 252, 0.1);
}

.avatar-container {
    width: 112rpx;
    height: 112rpx;
    margin-right: 28rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, #fce7f3 0%, #c084fc 50%, #7dd3fc 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    position: relative;
    box-shadow: 0 4rpx 16rpx rgba(168, 85, 247, 0.2);
    border: 4rpx solid rgba(255, 255, 255, 0.8);
}

.contact-info {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.contact-name {
    font-size: 34rpx;
    font-weight: 600;
    color: #581c87;
    margin-bottom: 8rpx;
}

.contact-phone {
    font-size: 26rpx;
    color: #a855f7;
}

.unread-badge {
    min-width: 48rpx;
    height: 48rpx;
    background: linear-gradient(135deg, #f43f5e 0%, #ef4444 100%);
    border-radius: 24rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16rpx;
    box-shadow: 0 4rpx 12rpx rgba(244, 63, 94, 0.3);
}

.unread-badge .badge-text {
    color: #ffffff;
    font-size: 26rpx;
    font-weight: 700;
}

/* 弹窗 */
.dialog-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(127, 29, 173, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
}

.dialog-card {
    width: 640rpx;
    background: linear-gradient(180deg, #ffffff 0%, #fdf2f8 100%);
    border-radius: 40rpx;
    overflow: hidden;
    box-shadow: 0 32rpx 80rpx rgba(168, 85, 247, 0.3);
}

.dialog-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 48rpx 40rpx 32rpx;
    background: linear-gradient(135deg, #fce7f3 0%, #e0f2fe 100%);
}

.dialog-title {
    font-size: 40rpx;
    font-weight: 700;
    background: linear-gradient(135deg, #db2777 0%, #9333ea 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.dialog-close {
    width: 72rpx;
    height: 72rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48rpx;
    color: #a855f7;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 50%;
}

.dialog-body {
    padding: 40rpx;
}

.input-group {
    margin-bottom: 24rpx;
}

.phone-input {
    width: 100%;
    height: 112rpx;
    background: rgba(255, 255, 255, 0.9);
    border: 4rpx solid rgba(192, 132, 252, 0.3);
    border-radius: 32rpx;
    padding: 0 36rpx;
    font-size: 36rpx;
    color: #581c87;
    box-sizing: border-box;
    transition: all 0.3s;
}

.phone-input:focus {
    border-color: #c084fc;
    box-shadow: 0 0 0 8rpx rgba(192, 132, 252, 0.2);
}

.user-card {
    display: flex;
    align-items: center;
    padding: 36rpx;
    background: linear-gradient(135deg, rgba(252, 231, 243, 0.5) 0%, rgba(224, 242, 254, 0.5) 100%);
    border-radius: 32rpx;
    margin-top: 32rpx;
    border: 2rpx solid rgba(192, 132, 252, 0.2);
}

.user-avatar-wrapper {
    width: 112rpx;
    height: 112rpx;
    margin-right: 28rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, #fce7f3 0%, #c084fc 50%, #7dd3fc 100%);
    padding: 4rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    position: relative;
    box-shadow: 0 4rpx 16rpx rgba(168, 85, 247, 0.2);
    border: 4rpx solid rgba(255, 255, 255, 0.8);
}

.user-info {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.user-name {
    font-size: 36rpx;
    font-weight: 600;
    color: #581c87;
    margin-bottom: 8rpx;
}

.user-phone {
    font-size: 28rpx;
    color: #a855f7;
}

.error-tip {
    background: linear-gradient(135deg, #fce7f3 0%, #fce7f3 100%);
    color: #db2777;
    font-size: 30rpx;
    text-align: center;
    padding: 32rpx;
    border-radius: 24rpx;
    margin-top: 24rpx;
}

.dialog-footer {
    display: flex;
    padding: 24rpx 40rpx 40rpx;
    gap: 32rpx;
}

.dialog-footer button {
    flex: 1;
    height: 112rpx;
    border-radius: 56rpx;
    font-size: 36rpx;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0;
}

.dialog-footer button::after {
    border: none;
}

.btn-cancel {
    background: rgba(255, 255, 255, 0.9);
    color: #a855f7;
    border: 4rpx solid rgba(192, 132, 252, 0.3);
}

.btn-confirm {
    background: linear-gradient(135deg, #ec4899 0%, #9333ea 100%);
    color: #ffffff;
    box-shadow: 0 8rpx 24rpx rgba(236, 72, 153, 0.3);
}

.btn-confirm[disabled] {
    background: linear-gradient(135deg, #d1d5db 0%, #9ca3af 100%);
    color: #ffffff;
    box-shadow: none;
}
</style>
