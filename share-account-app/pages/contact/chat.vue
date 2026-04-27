<template>
    <view class="page-container">
        <!-- 顶部导航 -->
        <view class="header-section">
            <view class="back-btn" @click="goBack">
            </view>
            <text class="page-title">{{ nickName }}</text>
            <view class="placeholder"></view>
        </view>

        <!-- 消息列表 -->
        <scroll-view
            class="message-list"
            :scroll-y="true"
            :scroll-with-animation="false"
            :scroll-into-view="scrollIntoViewId"
            @scrolltoupper="loadMore"
        >
            <view v-if="hasMore" class="load-more" @click="loadMore">
                <text>加载更多</text>
            </view>
            <view v-else class="load-more-hint">
                <text>仅显示最近3个月的消息</text>
            </view>

            <view v-for="msg in messages" :key="msg.id" class="message-item">
                <!-- 我的消息 -->
                <view v-if="String(msg.fromUserId) === String(myUserId)" :id="'msg-' + msg.id" class="message-row right">
                    <view class="message-content">
                        <view v-if="msg.type === 0" class="bubble right" @longpress="showMessageActions(msg)">
                            <text class="bubble-text">{{ msg.content }}</text>
                        </view>
                        <view v-else-if="msg.type === 1" class="bubble image-bubble right" @longpress="showMessageActions(msg)">
                            <image class="message-image" :src="msg.imageUrl || msg.content" mode="aspectFill" @click="previewImage(msg.imageUrl || msg.content)" @error="onImageError($event, msg)"></image>
                        </view>
                        <view v-else-if="msg.type === 2" class="bubble card-bubble right" @click="handleCardClick(msg)">
                            <view class="card-row">
                                <custom-icon type="wodezhangben" :size="18" color="#34C759" />
                                <text class="card-title">{{ msg.content.title || '账本分享' }}</text>
                            </view>
                        </view>
                        <view v-else-if="msg.type === 3" class="bubble card-bubble right" @click="handleCardClick(msg)">
                            <view class="card-row">
                                <custom-icon type="menpiao" :size="18" color="#34C759" />
                                <text class="card-title">{{ msg.content.title || '账单分享' }}</text>
                            </view>
                        </view>
                        <text class="message-time">{{ msg.createTime }}</text>
                    </view>
                    <view class="avatar-column">
                        <view class="avatar-wrapper">
                            <user-avatar :avatar-url="myAvatar || ''" :uploadable="false" :clickable="false" size="88rpx"></user-avatar>
                        </view>
                        <view class="read-status">
                            <text v-if="msg.isRead === 1" class="read-tag read">已读</text>
                            <text v-else class="read-tag unread">未读</text>
                        </view>
                    </view>
                </view>

                <!-- 对方消息 -->
                <view v-else :id="'msg-' + msg.id" class="message-row left">
                    <view class="avatar-wrapper">
                        <user-avatar :avatar-url="msg.fromUserAvatar || ''" :uploadable="false" :clickable="false" size="88rpx"></user-avatar>
                    </view>
                    <view class="message-content">
                        <view v-if="msg.type === 0" class="bubble left">
                            <text class="bubble-text">{{ msg.content }}</text>
                        </view>
                        <view v-else-if="msg.type === 1" class="bubble image-bubble left">
                            <image class="message-image" :src="msg.imageUrl || msg.content" mode="aspectFill" @click="previewImage(msg.imageUrl || msg.content)" @error="onImageError($event, msg)"></image>
                        </view>
                        <view v-else-if="msg.type === 2" class="bubble card-bubble left" @click="handleCardClick(msg)">
                            <view class="card-row">
                                <custom-icon type="wodezhangben" :size="18" color="#34C759" />
                                <text class="card-title">{{ msg.content.title || '账本分享' }}</text>
                            </view>
                            <view class="card-status" v-if="msg.toUserId === myUserId">
                                <text v-if="!msg.content || typeof msg.content === 'string' || !msg.content.status || msg.content.status === 0" class="status-pending">待处理</text>
                                <text v-else-if="msg.content.status === 1" class="status-agreed">已同意</text>
                                <text v-else-if="msg.content.status === 2" class="status-rejected">已拒绝</text>
                            </view>
                            <view class="card-actions" v-if="msg.toUserId === myUserId && (!msg.content || typeof msg.content === 'string' || !msg.content.status || msg.content.status === 0)">
                                <button class="card-btn accept" @click.stop="handleCardAction(msg, 'agree')">同意</button>
                                <button class="card-btn cancel" @click.stop="handleCardAction(msg, 'cancel')">拒绝</button>
                            </view>
                        </view>
                        <view v-else-if="msg.type === 3" class="bubble card-bubble left" @click="handleCardClick(msg)">
                            <view class="card-row">
                                <custom-icon type="menpiao" :size="18" color="#34C759" />
                                <text class="card-title">{{ msg.content.title || '账单分享' }}</text>
                            </view>
                            <view class="card-status" v-if="msg.toUserId === myUserId">
                                <text v-if="!msg.content || typeof msg.content === 'string' || !msg.content.status || msg.content.status === 0" class="status-pending">待处理</text>
                                <text v-else-if="msg.content.status === 1" class="status-agreed">已同意</text>
                                <text v-else-if="msg.content.status === 2" class="status-rejected">已拒绝</text>
                            </view>
                            <view class="card-actions" v-if="msg.toUserId === myUserId && (!msg.content || typeof msg.content === 'string' || !msg.content.status || msg.content.status === 0)">
                                <button class="card-btn accept" @click.stop="handleCardAction(msg, 'agree')">同意</button>
                                <button class="card-btn cancel" @click.stop="handleCardAction(msg, 'cancel')">拒绝</button>
                            </view>
                        </view>
                        <text class="message-time">{{ msg.createTime }}</text>
                    </view>
                </view>
            </view>
            <view id="bottom-anchor" style="height: 1px;"></view>
        </scroll-view>

        <!-- 账本选择弹窗 -->
        <LedgerSelectPopup
            ref="ledgerPopup"
            :selectedLedger="selectedLedger"
            :autoSelectDefault="false"
            @select="onLedgerSelect"
        />

        <!-- 账本详情弹窗 -->
        <uni-popup ref="ledgerDetailPopup" type="center">
            <view class="detail-popup" v-if="currentLedgerDetail">
                <view class="popup-header">
                    <text class="popup-title">账本详情</text>
                    <custom-icon type="guanbi" :size="20" color="#999" @click="closeLedgerDetail"></custom-icon>
                </view>
                <view class="popup-body">
                    <view class="detail-row">
                        <text class="detail-label">账本名称</text>
                        <text class="detail-value">{{ currentLedgerDetail.name }}</text>
                    </view>
                    <view class="detail-row">
                        <text class="detail-label">账本类型</text>
                        <text class="detail-value">{{ currentLedgerDetail.typeName }}</text>
                    </view>
                    <view class="detail-row">
                        <text class="detail-label">账本属性</text>
                        <text class="detail-value">{{ currentLedgerDetail.propertyName }}</text>
                    </view>
                </view>
            </view>
        </uni-popup>

        <!-- 账单详情弹窗 -->
        <uni-popup ref="billDetailPopup" type="bottom" :custom="true">
            <view class="bill-popup" v-if="currentBillDetail">
                <view class="popup-header">
                    <text class="popup-title">账单详情</text>
                    <custom-icon type="guanbi" :size="21" color="#999" @click="closeBillDetail"></custom-icon>
                </view>
                <view class="popup-body">
                    <view class="detail-row" v-if="!isBillOwner">
                        <text class="detail-label">创建人</text>
                        <text class="detail-value">{{ currentBillDetail.createUserName || currentBillDetail.userName || '-' }}</text>
                    </view>
                    <view class="detail-row">
                        <text class="detail-label">分类</text>
                        <text class="detail-value">{{ currentBillDetail.className }}</text>
                    </view>
                    <view class="detail-row">
                        <text class="detail-label">金额</text>
                        <text :class="['detail-value', 'detail-amount', currentBillDetail.price > 0 ? 'income' : 'expense']">
                            {{ currentBillDetail.price > 0 ? '+' : '-' }}{{ (Math.abs(currentBillDetail.price) / 100).toFixed(2) }}
                        </text>
                    </view>
                    <view class="detail-row" v-if="currentBillDetail.ledgerName">
                        <text class="detail-label">账本名称</text>
                        <text class="detail-value">{{ currentBillDetail.ledgerName }}</text>
                    </view>
                    <view class="detail-row">
                        <text class="detail-label">账户名称</text>
                        <text class="detail-value">{{ currentBillDetail.accountName }}</text>
                    </view>
                    <view class="detail-row" v-if="currentBillDetail.isBudgetName">
                        <text class="detail-label">预算状态</text>
                        <text class="detail-value">{{ currentBillDetail.isBudgetName }}</text>
                    </view>
                    <view class="detail-row">
                        <text class="detail-label">时间</text>
                        <text class="detail-value">{{ formatBillTime(currentBillDetail.billTime, true) }}</text>
                    </view>
                    <view class="detail-row" v-if="currentBillDetail.memo">
                        <text class="detail-label">备注</text>
                        <text class="detail-value">{{ currentBillDetail.memo }}</text>
                    </view>
                </view>
                <!-- 评论列表 -->
                <view class="comments-section" v-if="billComments.length > 0">
                    <view class="comments-title">评论 ({{ billComments.length }})</view>
                    <view v-for="comment in billComments" :key="comment.id" class="comment-item">
                        <view class="comment-header">
                            <text class="comment-author">{{ comment.userName || '匿名' }}</text>
                            <text class="comment-time">{{ formatBillTime(comment.createTime, true) }}</text>
                        </view>
                        <text class="comment-content">{{ comment.content }}</text>
                    </view>
                </view>
            </view>
        </uni-popup>

        <!-- 消息操作弹窗 -->
        <uni-popup ref="messageActionPopup" type="center">
            <view class="action-popup">
                <view class="popup-title">消息操作</view>
                <view class="popup-actions">
                    <view class="action-item danger" @click="deleteMessage" v-if="selectedMessage && selectedMessage.fromUserId === myUserId">
                        <custom-icon type="shanchu" :size="22" color="#ef4444"></custom-icon>
                        <text class="action-text">删除</text>
                    </view>
                </view>
                <view class="popup-cancel" @click="closeMessageActions">
                    <text>取消</text>
                </view>
            </view>
        </uni-popup>

        <!-- 输入区域 -->
        <view class="input-section">
            <view class="input-wrapper">
                <input
                    class="text-input"
                    v-model="inputText"
                    type="text"
                    placeholder="输入消息..."
                    confirm-type="send"
                    @confirm="sendTextMessage"
                />
                <view class="send-btn" @click="sendTextMessage">
                    <text>发送</text>
                </view>
            </view>
            <view class="tool-bar">
                <view class="tool-btn" @click="chooseImage">
                    <custom-icon type="paizhao" :size="30" color="#34C759" />
                    <text class="tool-label">图片</text>
                </view>
                <view class="tool-btn" @click="showShareLedger">
                    <custom-icon type="wodezhangben" :size="30" color="#34C759" />
                    <text class="tool-label">账本</text>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
import UserAvatar from '@/components/user-avatar.vue'
import LedgerSelectPopup from '@/components/ledger-select-popup.vue'
import uniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue'
import CustomIcon from '@/components/custom-icon/custom-icon.vue'
import { uploadFile } from '@/common/upload.js'

export default {
    components: {
        UserAvatar,
        LedgerSelectPopup,
        uniPopup,
        CustomIcon
    },
    data() {
        return {
            userId: null,
            nickName: '',
            avatarUrl: '',
            myUserId: null,
            myAvatar: '',
            messages: [],
            inputText: '',
            scrollIntoViewId: '',
            hasMore: true,
            limit: 20,
            pollingTimer: null,
            shouldPoll: false,
            selectedLedger: null,
            selectedMessage: null,
            currentLedgerDetail: null,
            currentBillDetail: null,
            billComments: [],
            isBillOwner: false
        }
    },
    onLoad(options) {
        this.userId = parseInt(options.userId);
        this.nickName = decodeURIComponent(options.nickName || '联系人');
        this.avatarUrl = decodeURIComponent(options.avatarUrl || '');
        this.myUserId = uni.getStorageSync('additionalId');
        this.myAvatar = uni.getStorageSync('avatarUrl');
        this.loadHistory();
        setTimeout(() => {
            this.startPollingIfNeeded()
        }, 3000)
    },
    onUnload() {
        this.stopPolling()
        this.markAsRead()
    },
    methods: {
        async loadHistory() {
            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.chat_history,
                    method: 'POST',
                    data: { userId: this.userId, limit: this.limit },
                    timeout: 10000
                })

                if (res) {
                    const newMessages = (res || []).map(msg => {
                        if (msg.type === 2 || msg.type === 3) {
                            try {
                                msg.content = typeof msg.content === 'string' ? JSON.parse(msg.content) : msg.content
                            } catch (e) {}
                        }
                        return msg
                    }).reverse()

                    // 从消息中提取用户头像
                    const myUserIdStr = String(this.myUserId)
                    const userIdStr = String(this.userId)
                    for (const msg of newMessages) {
                        const msgFromUserIdStr = String(msg.fromUserId)
                        if (msgFromUserIdStr === myUserIdStr && !this.myAvatar && msg.fromUserAvatar) {
                            this.myAvatar = msg.fromUserAvatar
                            console.log('[chat] 设置 myAvatar:', this.myAvatar)
                        }
                        if (msgFromUserIdStr === userIdStr && !this.avatarUrl && msg.fromUserAvatar) {
                            this.avatarUrl = msg.fromUserAvatar
                            console.log('[chat] 设置 avatarUrl:', this.avatarUrl)
                        }
                        if (this.myAvatar && this.avatarUrl) break
                    }

                    const oldIds = this.messages.map(m => m.id)
                    const newIds = newMessages.map(m => m.id)
                    const hasNewMessages = newIds.some(id => !oldIds.includes(id))
                    const hasRemovedMessages = oldIds.some(id => !newIds.includes(id))

                    const myUnreadCount = newMessages.filter(m => 
                        String(m.toUserId) === String(this.myUserId) && m.isRead === 0
                    ).length

                    if (hasNewMessages || hasRemovedMessages) {
                        this.messages = newMessages
                        this.hasMore = newMessages.length >= this.limit
                        if (hasNewMessages) {
                            this.$nextTick(() => {
                                this.scrollToBottom()
                            })
                        }
                    } else {
                        this.messages = newMessages
                    }

                    this.updatePollingState(myUnreadCount > 0)
                }
            } catch (e) {
                console.error('加载聊天记录失败', e)
            }
        },
        async loadMore() {
            if (!this.hasMore) return
            this.limit += 20
            await this.loadHistory()
        },
        async sendTextMessage() {
            const text = this.inputText.trim()
            if (!text) return

            this.inputText = ''
            await this.sendMessage(0, text)
        },
        async chooseImage() {
            try {
                const chooseRes = await new Promise((resolve, reject) => {
                    uni.chooseImage({
                        count: 1,
                        sizeType: ['compressed'],
                        sourceType: ['album', 'camera'],
                        success: (res) => resolve(res),
                        fail: (err) => reject(err)
                    })
                })
                const filePath = chooseRes.tempFilePaths?.[0]
                if (!filePath) return

                uni.showLoading({ title: '上传中...' })
                const uploadRes = await uploadFile({
                    filePath,
                    fileType: 1,
                    pathType: 0,
                    path: `/chat/${this.userId}/`,
                    objectId: String(this.userId)
                })

                uni.hideLoading()
                if (uploadRes && uploadRes.id) {
                    await this.sendMessage(1, String(uploadRes.id))
                } else {
                    uni.showToast({ title: '上传失败', icon: 'none' })
                }
            } catch (e) {
                uni.hideLoading()
                console.error('上传图片失败', e)
                uni.showToast({ title: '上传失败', icon: 'none' })
            }
        },
        showShareLedger() {
            this.$refs.ledgerPopup.open()
        },
        onLedgerSelect(ledger) {
            this.selectedLedger = ledger
            if (ledger) {
                this.sendMessage(2, {
                    title: ledger.name || '账本分享',
                    ledgerId: ledger.id
                })
            }
        },
        async sendMessage(type, content) {
            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.chat_send,
                    method: 'POST',
                    data: {
                        toUserId: this.userId,
                        type: type,
                        content: type === 2 || type === 3 ? JSON.stringify(content) : content
                    }
                })

                if (res) {
                    await this.loadHistory()
                } else {
                    uni.showToast({ title: '发送失败', icon: 'none' })
                }
            } catch (e) {
                uni.showToast({ title: '发送失败', icon: 'none' })
            }
        },
        showMessageActions(msg) {
            this.selectedMessage = msg
            this.$refs.messageActionPopup.open()
        },
        closeMessageActions() {
            this.$refs.messageActionPopup.close()
            this.selectedMessage = null
        },
        async recallMessage() {
            if (!this.selectedMessage) return
            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.chat_recall + this.selectedMessage.id,
                    method: 'POST'
                })
                if (res) {
                    uni.showToast({ title: '已撤销', icon: 'success' })
                    this.closeMessageActions()
                    await this.loadHistory()
                } else {
                    uni.showToast({ title: '撤销失败', icon: 'none' })
                }
            } catch (e) {
                uni.showToast({ title: '撤销失败', icon: 'none' })
            }
        },
        async deleteMessage() {
            if (!this.selectedMessage) return
            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.chat_delete + this.selectedMessage.id,
                    method: 'POST'
                })
                if (res) {
                    uni.showToast({ title: '已删除', icon: 'success' })
                    this.closeMessageActions()
                    this.messages = this.messages.filter(m => m.id !== this.selectedMessage.id)
                } else {
                    uni.showToast({ title: '删除失败', icon: 'none' })
                }
            } catch (e) {
                uni.showToast({ title: '删除失败', icon: 'none' })
            }
        },
        async markAsRead() {
            try {
                await this.$request({
                    url: this.$backUrlConfig.endpoints.chat_markRead + this.userId,
                    method: 'POST'
                })
            } catch (e) {
                console.error('标记已读失败', e)
            }
        },
        handleCardClick(msg) {
            if (msg.type === 2) {
                this.showLedgerDetail(msg)
            } else if (msg.type === 3) {
                this.showBillDetail(msg)
            }
        },
        async showLedgerDetail(msg) {
            const ledgerId = msg.content.ledgerId
            if (!ledgerId) {
                uni.showToast({ title: '账本ID不存在', icon: 'none' })
                return
            }
            uni.showLoading({ title: '加载中...' })
            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.ledger_getById + ledgerId,
                    method: 'GET'
                })
                uni.hideLoading()
                if (res) {
                    this.currentLedgerDetail = res
                    this.$refs.ledgerDetailPopup.open()
                } else {
                    uni.showToast({ title: '账本不存在', icon: 'none' })
                }
            } catch (e) {
                uni.hideLoading()
                uni.showToast({ title: '加载失败', icon: 'none' })
            }
        },
        closeLedgerDetail() {
            this.$refs.ledgerDetailPopup.close()
        },
        async showBillDetail(msg) {
            const billId = msg.content.billId
            if (!billId) {
                uni.showToast({ title: '账单ID不存在', icon: 'none' })
                return
            }
            uni.showLoading({ title: '加载中...' })
            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.bill_getById + billId,
                    method: 'GET'
                })
                uni.hideLoading()
                if (res) {
                    this.currentBillDetail = res
                    this.isBillOwner = String(res.userId) === String(this.myUserId)
                    await this.loadBillComments(billId)
                    this.$refs.billDetailPopup.open()
                } else {
                    uni.showToast({ title: '账单不存在', icon: 'none' })
                }
            } catch (e) {
                uni.hideLoading()
                uni.showToast({ title: '加载失败', icon: 'none' })
            }
        },
        async loadBillComments(billId) {
            try {
                const res = await this.$request({
                    url: this.$backUrlConfig.endpoints.bill_commentsByBillId + billId,
                    method: 'GET'
                })
                if (res) {
                    this.billComments = Array.isArray(res) ? res : []
                } else {
                    this.billComments = []
                }
            } catch (e) {
                this.billComments = []
            }
        },
        async openBillComments() {
            if (!this.currentBillDetail) return
            await this.loadBillComments(this.currentBillDetail.id)
        },
        closeBillDetail() {
            this.$refs.billDetailPopup.close()
            this.billComments = []
        },
        formatBillTime(time, withTime = false) {
            if (!time) return '-'
            const date = new Date(time)
            const year = date.getFullYear()
            const month = String(date.getMonth() + 1).padStart(2, '0')
            const day = String(date.getDate()).padStart(2, '0')
            if (withTime) {
                const hours = String(date.getHours()).padStart(2, '0')
                const minutes = String(date.getMinutes()).padStart(2, '0')
                return `${year}-${month}-${day} ${hours}:${minutes}`
            }
            return `${year}-${month}-${day}`
        },
        handleCardAction(msg, action) {
            console.log('卡片操作', action, msg)
            const status = action === 'agree' ? 1 : 2
            uni.showLoading({ title: '处理中...' })
            this.$request({
                url: this.$backUrlConfig.endpoints.chat_updateInviteStatus + msg.id,
                method: 'POST',
                data: { status }
            }).then(res => {
                uni.hideLoading()
                if (res && (res.code === 200 || res.data === true || res === true)) {
                    uni.showToast({ title: action === 'agree' ? '已同意' : '已拒绝', icon: 'success' })
                    this.loadHistory().then(() => {
                        this.$forceUpdate()
                    })
                } else {
                    uni.showToast({ title: '处理失败', icon: 'none' })
                }
            }).catch(err => {
                uni.hideLoading()
                console.error('处理邀请失败:', err)
                uni.showToast({ title: '处理失败', icon: 'none' })
            })
        },
        previewImage(url) {
            if (!url || url === 'null' || url === 'undefined') {
                uni.showToast({ title: '图片加载失败', icon: 'none' })
                return
            }
            uni.previewImage({
                current: url,
                urls: [url]
            })
        },
        onImageError(e, msg) {
            console.error('图片加载失败', msg)
            uni.showToast({ title: '图片加载失败', icon: 'none' })
        },
        scrollToBottom() {
            this.$nextTick(() => {
                this.scrollIntoViewId = ''
                this.$nextTick(() => {
                    this.scrollIntoViewId = 'bottom-anchor'
                })
            })
        },
        goBack() {
            uni.navigateBack()
        },
        startPolling() {
            if (this.pollingTimer) return
            this.pollingTimer = setInterval(() => {
                this.loadHistory()
            }, 10000)
        },
        stopPolling() {
            if (this.pollingTimer) {
                clearInterval(this.pollingTimer)
                this.pollingTimer = null
            }
        },
        startPollingIfNeeded() {
            if (this.shouldPoll) {
                this.startPolling()
            }
        },
        updatePollingState(hasUnread) {
            this.shouldPoll = hasUnread
            if (hasUnread && !this.pollingTimer) {
                this.startPolling()
            } else if (!hasUnread && this.pollingTimer) {
                this.stopPolling()
            }
        }
    }
}
</script>

<style scoped>
.page-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
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
    font-size: 36rpx;
    font-weight: 700;
    background: linear-gradient(135deg, #db2777 0%, #9333ea 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.placeholder {
    width: 64rpx;
}

/* 消息列表 */
.message-list {
    flex: 1;
    overflow-y: auto;
    background: rgba(255, 255, 255, 0.5);
}

.load-more {
    text-align: center;
    padding: 24rpx;
    color: #a855f7;
    font-size: 26rpx;
}

.load-more-hint {
    text-align: center;
    padding: 16rpx;
    color: #999999;
    font-size: 24rpx;
}

.message-item {
    position: relative;
    margin-bottom: 36rpx;
}

.message-row {
    display: flex;
    align-items: flex-start;
}

.message-row.left {
    flex-direction: row;
}

.message-row.right {
    flex-direction: row;
    justify-content: flex-end;
}

.message-row.right .message-content {
    order: 1;
}

.message-row.right .avatar-wrapper {
    order: 2;
}

.avatar-wrapper {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, #fce7f3 0%, #c084fc 50%, #7dd3fc 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    box-shadow: 0 4rpx 16rpx rgba(168, 85, 247, 0.2);
    flex-shrink: 0;
}

.avatar-column {
    display: flex;
    flex-direction: column;
    align-items: center;
    order: 2;
}

.avatar-column .avatar-wrapper {
    order: 1;
}

.avatar-column .read-status {
    order: 2;
}

.message-content {
    max-width: 70%;
    margin: 0 20rpx;
}

.bubble {
    padding: 24rpx 28rpx;
    border-radius: 24rpx;
    background: rgba(255, 255, 255, 0.95);
    box-shadow: 0 4rpx 16rpx rgba(168, 85, 247, 0.1);
}

.bubble.left {
    border-bottom-left-radius: 8rpx;
}

.bubble.right {
    border-bottom-right-radius: 8rpx;
    box-shadow: 0 4rpx 16rpx rgba(7, 193, 96, 0.2);
}

.bubble-text {
    font-size: 32rpx;
    line-height: 1.5;
}


.image-bubble {
    padding: 8rpx;
    background: transparent !important;
    box-shadow: none;
}

.image-bubble.left {
    background: rgba(255, 255, 255, 0.95) !important;
    border-radius: 24rpx;
    border-bottom-left-radius: 8rpx;
}

.image-bubble.right {
    border-bottom-right-radius: 8rpx;
}

.message-image {
    width: 150rpx;
    height: 150rpx;
    border-radius: 16rpx;
    object-fit: cover;
}

.card-bubble {
    background: rgba(255, 255, 255, 0.95);
    padding: 24rpx;
    min-width: 300rpx;
    box-shadow: 0 4rpx 16rpx rgba(168, 85, 247, 0.1);
    border: 2rpx solid rgba(192, 132, 252, 0.2);
}

.card-row {
    display: flex;
    align-items: center;
    gap: 12rpx;
    flex-shrink: 0;
}

.card-row .icon-wrapper {
    flex-shrink: 0;
    display: flex;
    align-items: center;
}

.card-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #581c87;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 220rpx;
}

.card-status {
    display: flex;
    justify-content: flex-end;
    margin-top: 8rpx;
    font-size: 24rpx;
}

.status-pending {
    color: #999999;
}

.status-agreed {
    color: #07c160;
}

.status-rejected {
    color: #ff4d4f;
}

.card-actions {
    display: flex;
    gap: 20rpx;
    margin-top: 20rpx;
}

.card-btn {
    flex: 1;
    height: 68rpx;
    border-radius: 34rpx;
    font-size: 28rpx;
    font-weight: 500;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0;
}

.card-btn::after {
    border: none;
}

.card-btn.accept {
    background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
    color: #ffffff;
    box-shadow: 0 4rpx 12rpx rgba(7, 193, 96, 0.3);
}

.card-btn.cancel {
    background: rgba(255, 255, 255, 0.9);
    color: #a855f7;
    border: 2rpx solid rgba(192, 132, 252, 0.3);
}

.message-time {
    font-size: 22rpx;
    color: #a855f7;
    margin-top: 10rpx;
    display: block;
}

.message-row.right .message-time {
    text-align: right;
}

.read-status {
    text-align: center;
    margin-top: 4rpx;
    display: flex;
    justify-content: center;
}

.read-tag {
    font-size: 20rpx;
}

.read-tag.read {
    color: #999999;
}

.read-tag.unread {
    color: #ff3b30;
}

.read-icon {
    font-size: 22rpx;
    color: #10b981;
}

/* 详情弹窗 */
.detail-popup {
    background: #ffffff;
    border-radius: 24rpx;
    padding: 0;
    min-width: 500rpx;
    overflow: hidden;
}

.detail-popup .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32rpx;
    border-bottom: 1rpx solid #f0f0f0;
}

.detail-popup .popup-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333333;
}

.detail-popup .popup-body {
    padding: 32rpx;
}

.detail-popup .detail-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
}

.detail-popup .detail-row:last-child {
    border-bottom: none;
}

.detail-popup .detail-label {
    font-size: 28rpx;
    color: #666666;
}

.detail-popup .detail-value {
    font-size: 28rpx;
    color: #333333;
    text-align: right;
    max-width: 60%;
    word-break: break-all;
}

.detail-popup .detail-amount {
    font-size: 36rpx;
    font-weight: bold;
}

.detail-popup .detail-amount.income {
    color: #07c160;
}

.detail-popup .detail-amount.expense {
    color: #ff4d4f;
}

/* 账单详情弹窗 */
.bill-popup {
    background: #ffffff;
    border-radius: 24rpx 24rpx 0 0;
    max-height: 80vh;
    overflow-y: auto;
}

.bill-popup .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32rpx;
    border-bottom: 1rpx solid #f0f0f0;
    position: sticky;
    top: 0;
    background: #ffffff;
}

.bill-popup .popup-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333333;
}

.bill-popup .popup-body {
    padding: 32rpx;
}

.bill-popup .detail-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
}

.bill-popup .detail-row:last-child {
    border-bottom: none;
}

.bill-popup .detail-label {
    font-size: 28rpx;
    color: #666666;
}

.bill-popup .detail-value {
    font-size: 28rpx;
    color: #333333;
    text-align: right;
    max-width: 60%;
    word-break: break-all;
}

.bill-popup .detail-amount {
    font-size: 36rpx;
    font-weight: bold;
}

.bill-popup .detail-amount.income {
    color: #07c160;
}

.bill-popup .detail-amount.expense {
    color: #ff4d4f;
}

/* 评论样式 */
.popup-actions-header {
    display: flex;
    align-items: center;
    gap: 16rpx;
}

.bill-popup .action-text {
    font-size: 26rpx;
    color: #1989fa;
    background: none;
    border: none;
    padding: 0;
    line-height: 1;
}

.bill-popup .action-text::after {
    border: none;
}

.comments-section {
    padding: 24rpx 32rpx;
    border-top: 16rpx solid #f5f5f5;
}

.comments-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #333333;
    margin-bottom: 20rpx;
}

.comment-item {
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
}

.comment-item:last-child {
    border-bottom: none;
}

.comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8rpx;
}

.comment-author {
    font-size: 26rpx;
    color: #1989fa;
}

.comment-time {
    font-size: 22rpx;
    color: #999999;
}

.comment-content {
    font-size: 28rpx;
    color: #333333;
    line-height: 1.5;
}

/* 消息操作弹窗 */
.action-popup {
    background: rgba(255, 255, 255, 0.98);
    border-radius: 32rpx;
    padding: 40rpx;
    min-width: 500rpx;
}

.popup-title {
    font-size: 36rpx;
    font-weight: 700;
    color: #581c87;
    text-align: center;
    margin-bottom: 40rpx;
}

.popup-actions {
    display: flex;
    flex-direction: column;
    gap: 24rpx;
}

.action-item {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    padding: 28rpx;
    background: linear-gradient(135deg, #fce7f3 0%, #e0f2fe 100%);
    border-radius: 20rpx;
}

.action-item.danger {
    background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
}

.action-text {
    font-size: 32rpx;
    font-weight: 600;
    color: #581c87;
}

.action-item.danger .action-text {
    color: #ef4444;
}

.popup-cancel {
    margin-top: 32rpx;
    padding: 24rpx;
    text-align: center;
    background: rgba(168, 85, 247, 0.1);
    border-radius: 16rpx;
}

.popup-cancel text {
    font-size: 30rpx;
    color: #a855f7;
}

/* 输入区域 */
.input-section {
    background: rgba(255, 255, 255, 0.95);
    padding: 20rpx 24rpx;
    padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
    border-top: 2rpx solid rgba(192, 132, 252, 0.2);
}

.input-wrapper {
    display: flex;
    align-items: center;
    height: 88rpx;
    background: linear-gradient(135deg, #fce7f3 0%, #e0f2fe 100%);
    border-radius: 44rpx;
    padding: 0 12rpx 0 32rpx;
    border: 2rpx solid rgba(192, 132, 252, 0.2);
}

.text-input {
    flex: 1;
    font-size: 32rpx;
    color: #581c87;
    height: 100%;
}

.send-btn {
    width: 128rpx;
    height: 68rpx;
    background: linear-gradient(135deg, #ec4899 0%, #9333ea 100%);
    border-radius: 34rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 16rpx rgba(236, 72, 153, 0.3);
}

.send-btn text {
    font-size: 30rpx;
    color: #ffffff;
    font-weight: 500;
}

.tool-bar {
    display: flex;
    padding-top: 24rpx;
    gap: 64rpx;
}

.tool-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.tool-icon {
    font-size: 52rpx;
    margin-bottom: 6rpx;
}

.tool-label {
    font-size: 24rpx;
    color: #a855f7;
}
</style>