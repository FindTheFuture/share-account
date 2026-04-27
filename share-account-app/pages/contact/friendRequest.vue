<template>
    <view class="page-container">

        <!-- 好友请求列表 -->
        <scroll-view class="request-list" scroll-y>
            <view v-if="requests.length === 0" class="empty-state">
                <view class="empty-icon-wrapper">
                    <text class="empty-icon">💌</text>
                </view>
                <text class="empty-title">暂无好友请求</text>
                <text class="empty-desc">快去添加好友吧~</text>
            </view>

            <view v-else class="request-items">
                <view
                    v-for="item in requests"
                    :key="item.contactId"
                    class="request-card"
                >
                    <view class="request-info">
                        <view class="avatar-wrapper">
                            <user-avatar :avatar-url="item.avatarUrl || ''" :uploadable="false" :clickable="false" size="104rpx"></user-avatar>
                        </view>
                        <view class="user-detail">
                            <text class="user-name">{{ item.nickName || '未设置昵称' }}</text>
                            <text class="user-phone">{{ item.phone }}</text>
                            <text class="request-time">{{ formatTime(item.createTime) }}</text>
                        </view>
                    </view>
                    <view class="action-btns">
                        <button class="btn-reject" @click="rejectRequest(item)">拒绝</button>
                        <button class="btn-accept" @click="acceptRequest(item)">同意</button>
                    </view>
                </view>
            </view>
        </scroll-view>
    </view>
</template>

<script>
import UserAvatar from '@/components/user-avatar.vue'

export default {
    components: {
        UserAvatar
    },
    data() {
        return {
            requests: []
        }
    },
    onLoad() {
    },
    onShow() {
        this.loadRequests()
    },
    methods: {
        goBack() {
            uni.navigateBack()
        },
        async loadRequests() {
            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.contact_requests,
                    method: 'GET'
                })
                this.requests = res || []
            } catch (e) {
                console.error('加载好友请求失败', e)
            }
        },
        async acceptRequest(item) {
            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.contact_accept + item.contactId,
                    method: 'POST'
                })

                if (res) {
                    uni.showToast({
                        title: '已同意',
                        icon: 'success'
                    })
                    this.requests = this.requests.filter(r => r.contactId !== item.contactId)
                }
            } catch (e) {
                uni.showToast({
                    title: '操作失败',
                    icon: 'none'
                })
            }
        },
        async rejectRequest(item) {
            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.contact_reject + item.contactId,
                    method: 'POST'
                })

                if (res) {
                    uni.showToast({
                        title: '已拒绝',
                        icon: 'success'
                    })
                    this.requests = this.requests.filter(r => r.contactId !== item.contactId)
                }
            } catch (e) {
                uni.showToast({
                    title: '操作失败',
                    icon: 'none'
                })
            }
        },
        formatTime(timeStr) {
            if (!timeStr) return ''
            const date = new Date(timeStr)
            const now = new Date()
            const diff = now - date

            if (diff < 60000) return '刚刚'
            if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
            if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
            if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
            return timeStr.substring(0, 10)
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
}

/* 顶部导航 */
.header-section {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 96rpx;
    padding: 0 24rpx;
    padding-top: calc(env(safe-area-inset-top));
    background: linear-gradient(135deg, #fce7f3 0%, #e0f2fe 100%);
}

.back-btn {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
}

.back-icon {
    font-size: 60rpx;
    color: #7c3aed;
    font-weight: 300;
}

.page-title {
    font-size: 38rpx;
    font-weight: 700;
    background: linear-gradient(135deg, #db2777 0%, #9333ea 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.placeholder {
    width: 64rpx;
}

/* 请求列表 */
.request-list {
    flex: 1;
    overflow-y: auto;
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

.request-items {
    padding: 32rpx;
}

.request-card {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 32rpx;
    padding: 36rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 8rpx 32rpx rgba(168, 85, 247, 0.1);
    border: 2rpx solid rgba(192, 132, 252, 0.2);
}

.request-info {
    display: flex;
    align-items: center;
    margin-bottom: 28rpx;
}

.avatar-wrapper {
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

.avatar-wrapper > user-avatar {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #fff;
    border-radius: 50%;
    overflow: hidden;
}

.user-detail {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.user-name {
    font-size: 34rpx;
    font-weight: 600;
    color: #581c87;
    margin-bottom: 8rpx;
}

.user-phone {
    font-size: 26rpx;
    color: #a855f7;
    margin-bottom: 6rpx;
}

.request-time {
    font-size: 24rpx;
    color: #a855f7;
}

.action-btns {
    display: flex;
    justify-content: flex-end;
    gap: 24rpx;
}

.action-btns button {
    width: 168rpx;
    height: 76rpx;
    border-radius: 38rpx;
    font-size: 30rpx;
    font-weight: 500;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0;
}

.action-btns button::after {
    border: none;
}

.btn-reject {
    background: rgba(255, 255, 255, 0.9);
    color: #a855f7;
    border: 4rpx solid rgba(192, 132, 252, 0.3);
}

.btn-accept {
    background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
    color: #ffffff;
    box-shadow: 0 8rpx 24rpx rgba(7, 193, 96, 0.3);
}
</style>