<template>
  <view class="comment-page">
    <view v-if="loading" class="loading">加载中...</view>
    <view v-else class="page-content">
      <!-- 顶部固定账单信息 -->
      <view v-if="billDetail" class="bill-header">
        <view class="bh-row">
          <text class="bh-category">{{ billDetail.className || '未分类' }}</text>
          <text :class="['bh-amount', billDetail.price > 0 ? 'income' : 'expense']">
            {{ billDetail.price > 0 ? '+' : '-' }}{{ formatAmount(Math.abs(billDetail.price) / 100) }}
          </text>
        </view>
        <view class="bh-row">
          <view class="bh-ledger-line">
            <text class="bh-ledger" v-if="billDetail.ledgerName">{{ billDetail.ledgerName }}</text>
            <text class="bh-ledger" v-if="billDetail.accountTypeName">{{ billDetail.accountTypeName }}</text>
            <text class="bh-ledger" v-if="billDetail.isBudgetName">{{ billDetail.isBudgetName }}</text>
          </view>
          <text class="bh-time">{{ formatTimeFriendly(billDetail.billTime, timeFormatStyle) }}</text>
        </view>
        <!-- 新增：备注行，仅当有值时显示 -->
        <view class="bh-row bh-memo-row">
          <text class="bh-memo" v-if="billDetail.memo">{{ billDetail.memo }}</text>
        </view>
      </view>

      <scroll-view ref="listScroll" class="list-scroll" scroll-y :scroll-with-animation="false" :scroll-into-view="scrollAnchorId || lastCommentAnchorId" @scroll="onListScroll">
      <view class="list-pagination-bar">
      <uni-pagination class="list-pagination" :current="uiCurrent" :pageSize="pageSize" :total="totalCount" @change="onPageChange" />
      </view>
      <!-- 评论列表（允许滚动） -->
        <view v-if="!comments.length" class="empty">暂无评论</view>
        <view v-else class="list">
          <view :id="'c-'+c.id" :class="['item', String(c.userId)===String(currentUserId) ? 'right' : 'left']" v-for="c in comments" :key="c.id" @longpress="onLongPress(c)" @contextmenu.prevent="onContextMenu(c)">
            <view :class="['bubble-row', String(c.userId)===String(currentUserId) ? 'right' : 'left']">
              <view class="avatar-tiny">
                <user-avatar :avatar-url="c.avatarUrl" :user-id="String(c.userId || '')" :uploadable="false" :clickable="false" :show-edit-mask="false" />
              </view>
              <view class="name-block">
                <text v-if="isCreator(c)" class="creator-tag">账单创建人</text>
                <text class="user">{{ c.userName || '匿名用户' }}</text>
                <text class="time">{{ formatTimeFriendly(c.createTime, timeFormatStyle) }}</text>
              </view>
              <view class="msg-block">
                <view v-if="c.type === 0 && c.content" class="content">{{ c.content }}</view>
                <image v-if="c.type === 1 && c.imageUrl" :src="c.imageUrl" mode="aspectFill" class="image-thumb" @click="previewImage(c.imageUrl)" @longpress="onLongPress(c)" @contextmenu.prevent="onContextMenu(c)" />
              </view>
            </view>
          </view>
        </view>
        <!-- 底部锚点：用于滚动定位到最新记录 -->
        <view id="latest-anchor" style="height:1px;"></view>
        <view v-if="false" class="load-more">
          <button :disabled="loadingMore" @click="loadMore">{{ loadingMore ? '加载中...' : '加载更多' }}</button>
        </view>
      </scroll-view>

      <!-- 发送图片标签（位于评论列表下面） -->
      <view class="editor-toolbar">
        <text class="image-tag" @click="addImageComment">发布图片</text>
      </view>

      
      <uni-popup ref="deletePopup" type="center">
        <view class="actions-popup">
          <view class="popup-title">操作</view>
          <view class="popup-subtitle">
            {{ deleteTarget && deleteTarget.type === 0 ? (deleteTarget.content || '') : (deleteTarget ? '图片评论' : '') }}
          </view>
          <view class="popup-actions">
            <view class="action-item danger" @click="confirmDelete">
              <custom-icon type="shanchu" :size="19" color="#ee0a24"></custom-icon>
              <text class="action-text">删除</text>
            </view>
            
          </view>
        </view>
      </uni-popup>

      <!-- 文本输入框（底部，发送按钮与文本框同高） -->
      <view class="editor">
        <view class="input-row">
          <textarea v-model="newText" class="input" placeholder="写下你的看法..." />
          <button class="send-btn" :disabled="submitting || !newText.trim()" @click="saveTextComment">发送</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import request from '@/common/request.js';
import backUrl from '@/common/back_url.js';
import { uploadFile } from '@/common/upload.js';
import { formatAmount, dateUtils } from '@/common/util.js';
import UserAvatar from '@/components/user-avatar.vue';
import uniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import UniPagination from '@/uni_modules/uni-pagination/components/uni-pagination/uni-pagination.vue';
export default {
  components: { UserAvatar, 'uni-popup': uniPopup, 'uni-icons': UniIcons, 'uni-pagination': UniPagination },
  data() {
    return {
      billId: null,
      billDetail: null,
      currentUserId: null,
      comments: [],
      loading: false,
      loadingMore: false,
      hasMore: true,
      pageNum: 1,
      pageSize: 20,
      timeFormatStyle: 'smart',
      lastCommentAnchorId: '',
      scrollAnchorId: '',
      isAtBottom: true,
      newText: '',
      submitting: false,
      deleteTarget: null,
      longPressTimer: null,
      longPressDelay: 3000,
      loadingPrev: false,
      totalCount: 0,
      backendTotalPages: 0
    };
  },
  onLoad(query) {
    this.billId = query?.billId || null;
    try {
      const additionalId = uni.getStorageSync('additionalId') || '';
      this.currentUserId = additionalId;
    } catch(e) {}
    if (!this.billId) {
      uni.showToast({ title: '缺少账单ID', icon: 'none' });
      return;
    }
    this.loadBillDetail();
    this.loadComments(true);
  },
  computed: {
    totalPages() {
      const backendPages = Number(this.backendTotalPages) || 0;
      if (backendPages > 0) return backendPages;
      const size = Number(this.pageSize) || 20;
      const total = Number(this.totalCount) || 0;
      return Math.max(1, Math.ceil(total / size));
    },
    uiCurrent() {
      const totalPages = this.totalPages;
      const real = (this.pageNum && this.pageNum > 0) ? this.pageNum : totalPages;
      return Math.min(totalPages, Math.max(1, totalPages - real + 1));
    }
  },
  methods: {
    formatAmount,
    async loadBillDetail() {
      try {
        const resp = await request.get(backUrl.endpoints.bill_getById + this.billId);
        if (resp && (resp.id || resp.billTime)) {
          this.billDetail = resp;
        }
      } catch (e) {
        console.error('获取账单详情失败', e);
      }
    },
    async loadComments(reset = false) {
      if (reset) {
        // 初始化为最新页：pageNum=1（后端按时间降序分页）
        this.pageNum = 1;
        this.hasMore = true;
        this.comments = [];
      }
      const isFirst = reset || (this.pageNum === 1);
      this[isFirst ? 'loading' : 'loadingMore'] = true;
      try {
        const resp = await request.get(backUrl.endpoints.bill_commentsByBillId + this.billId, { pageNum: this.pageNum, pageSize: this.pageSize });
        // 兼容新结构：BillCommentResponse
        const itemList = Array.isArray(resp?.itemList) ? resp.itemList : (Array.isArray(resp) ? resp : (resp?.list || []));
        const avatarMap = resp && resp.avatarUrlMap ? resp.avatarUrlMap : {};
        const total = (resp && typeof resp.total !== 'undefined') ? Number(resp.total) : null;
        if (typeof total === 'number' && !isNaN(total)) this.totalCount = total;
        const totalPagesFromBackend = (resp && typeof resp.totalPages !== 'undefined') ? Number(resp.totalPages) : null;
        if (typeof totalPagesFromBackend === 'number' && !isNaN(totalPagesFromBackend) && totalPagesFromBackend > 0) {
          this.backendTotalPages = totalPagesFromBackend;
        } else {
          this.backendTotalPages = 0;
        }
        const merged = (itemList || []).map(it => {
          const uidKey = (it && it.userId != null) ? String(it.userId) : '';
          const mappedAvatar = avatarMap[uidKey] || avatarMap[it?.userId] || null;
          return {
            ...it,
            avatarUrl: it?.avatarUrl || mappedAvatar || it?.avatarUrl
          };
        });
        const sorted = merged.slice().sort((a, b) => {
          const da = this.toDateSafe(a && (a.createTime || a.updateTime));
          const db = this.toDateSafe(b && (b.createTime || b.updateTime));
          return (da ? da.getTime() : 0) - (db ? db.getTime() : 0);
        });
        this.comments = sorted;
        const received = (itemList || []).length;
        this.hasMore = (typeof this.totalCount === 'number' && this.totalCount > 0)
          ? (this.comments.length < this.totalCount)
          : (received === this.pageSize);
        if (reset) {
          // 已由后端返回最新页数据
        }
        const last = this.comments[this.comments.length - 1];
        const autoscroll = reset ? true : !!this.isAtBottom;
        this.lastCommentAnchorId = autoscroll ? (last && last.id ? ('c-' + last.id) : 'latest-anchor') : '';
      } catch (e) {
        console.error(e);
        uni.showToast({ title: '加载失败', icon: 'none' });
      } finally {
        this.loading = false;
        this.loadingMore = false;
      }
    },
    loadMore() {
      this.loadComments(false);
    },
    onLongPress(c) {
      if (!c) return;
      if (String(c.userId) !== String(this.currentUserId)) return;
      this.deleteTarget = c;
      this.$refs?.deletePopup?.open?.('center');
    },
    onContextMenu(c) {
      if (!c) return;
      if (String(c.userId) !== String(this.currentUserId)) return;
      this.deleteTarget = c;
      this.$refs?.deletePopup?.open?.('center');
    },
    openActions(c) {
      if (!c) return;
      if (String(c.userId) !== String(this.currentUserId)) return;
      this.deleteTarget = c;
      this.$refs?.deletePopup?.open?.('center');
    },
    onListScroll(e) {
      try {
        const el = (e && e.target) || (this.$refs?.listScroll && ((this.$refs.listScroll.$el) || this.$refs.listScroll));
        const top = (e && e.detail && typeof e.detail.scrollTop === 'number') ? e.detail.scrollTop : (el ? el.scrollTop : 0);
        const h = el ? el.scrollHeight : 0;
        const ch = el ? (el.clientHeight || el.offsetHeight || 0) : 0;
        const gap = h && ch ? (h - top - ch) : 0;
        this.isAtBottom = gap < 24;
      } catch(_) {
        this.isAtBottom = true;
      }
    },
    onPageChange(e) {
      const newPageUi = (e?.current ?? e?.currentPage ?? e?.detail?.current ?? e);
      const ui = Number(newPageUi);
      if (!isNaN(ui) && ui > 0) {
        const total = this.totalPages;
        const targetPage = Math.min(total, Math.max(1, total - ui + 1));
        this.pageNum = targetPage;
        this.loadComments(false);
      }
    },
    previewImage(url) {
      if (!url) return;
      try {
        uni.previewImage({ current: url, urls: [url] });
      } catch (e) {
        console.error('预览图片失败', e);
      }
    },
    scrollToLatest() {
      this.$nextTick(() => {
        const ref = this.$refs && this.$refs.listScroll;
        const el = ref && (ref.$el || ref);
        if (el && typeof el.scrollTop !== 'undefined' && el.scrollHeight) {
          el.scrollTop = el.scrollHeight;
        } else {
          try { uni.pageScrollTo({ scrollTop: 999999, duration: 0 }); } catch (e) {}
        }
      });
    },
    async saveTextComment() {
      if (!this.billId || !this.newText.trim()) return;
      this.submitting = true;
      try {
        const payload = { billId: this.billId, type: 0, content: this.newText.trim() };
        const resp = await request.post(backUrl.endpoints.bill_comment_save, payload);
        if (resp === true || resp?.code === 200 || resp?.success) {
          this.newText = '';
          this.loadComments(false);
        } else {
          uni.showToast({ title: (resp?.msg || resp?.message || '发布失败'), icon: 'none' });
        }
      } catch (e) {
        console.error(e);
        uni.showToast({ title: '发布失败', icon: 'none' });
      } finally {
        this.submitting = false;
      }
    },
    async addImageComment() {
      if (!this.billId) return;
      try {
        const chooseRes = await new Promise((resolve, reject) => {
          uni.chooseImage({
            count: 1,
            sizeType: ['compressed'],
            sourceType: ['album', 'camera'],
            success: (res) => resolve(res),
            fail: (err) => reject(err)
          });
        });
        const filePath = chooseRes.tempFilePaths?.[0];
        if (!filePath) return;
        this.submitting = true;
        const uploadRes = await uploadFile({
          filePath,
          fileType: 201,
          pathType: 0,
          path: `/comment/${this.billId}/`,
          objectId: this.billId
        });
        if (!uploadRes || typeof uploadRes.id === 'undefined') {
          uni.showToast({ title: '上传失败', icon: 'none' });
          this.submitting = false;
          return;
        }
        const payload = { billId: this.billId, type: 1, content: String(uploadRes.id) };
        const resp = await request.post(backUrl.endpoints.bill_comment_save, payload);
        if (resp === true || resp?.code === 200 || resp?.success) {
          this.loadComments(false);
        } else {
          uni.showToast({ title: (resp?.msg || resp?.message || '发布失败'), icon: 'none' });
        }
      } catch (e) {
        console.error(e);
        uni.showToast({ title: '操作失败', icon: 'none' });
      } finally {
        this.submitting = false;
      }
    },
    async deleteComment(commentId) {
      if (!commentId) return;
      try {
        await request.post(backUrl.endpoints.bill_comment_deleteById + commentId, {});
        uni.showToast({ title: '已删除', icon: 'success' });
        this.$refs?.deletePopup?.close?.();
        this.deleteTarget = null;
        this.loadComments(false);
      } catch (e) {
        console.error(e);
        uni.showToast({ title: '删除失败', icon: 'none' });
      }
    },
    formatTimeFriendly(time, style = 'smart') {
      if (!time) return '';
      const d = this.toDateSafe(time);
      if (!d) return '';
      const now = new Date();
      const diff = now.getTime() - d.getTime();
      const pad = (n) => String(n).padStart(2, '0');
      const Y = d.getFullYear();
      const M = pad(d.getMonth() + 1);
      const D = pad(d.getDate());
      const h = pad(d.getHours());
      const m = pad(d.getMinutes());
      switch (style) {
        case 'relative': {
          if (diff < dateUtils.UNITS['天']) return dateUtils.humanize(diff);
          const days = Math.floor(diff / dateUtils.UNITS['天']);
          if (days === 1) return `昨天 ${h}:${m}`;
          if (days === 2) return `前天 ${h}:${m}`;
          if (days <= 7) return `${days}天前`;
          return `${Y}-${M}-${D} ${h}:${m}`;
        }
        case 'full': {
          return `${Y}-${M}-${D} ${h}:${m}`;
        }
        case 'weekday': {
          const names = ['周日','周一','周二','周三','周四','周五','周六'];
          const wk = names[d.getDay()];
          return `${Y}-${M}-${D} ${wk} ${h}:${m}`;
        }
        case 'compact': {
          if (d.toDateString() === now.toDateString()) return `${h}:${m}`;
          if (d.getFullYear() === now.getFullYear()) return `${M}-${D} ${h}:${m}`;
          return `${String(Y).slice(2)}-${M}-${D}`;
        }
        case 'smart':
        default: {
          if (diff < dateUtils.UNITS['天']) return dateUtils.humanize(diff);
          const isToday = d.toDateString() === now.toDateString();
          const isYesterday = new Date(now.getTime() - dateUtils.UNITS['天']).toDateString() === d.toDateString();
          if (isToday) return `${h}:${m}`;
          if (isYesterday) return `昨天 ${h}:${m}`;
          if (d.getFullYear() === now.getFullYear()) return `${M}-${D} ${h}:${m}`;
          return `${Y}-${M}-${D}`;
        }
      }
    },
    toDateSafe(input) {
      if (!input) return null;
      try {
        if (input instanceof Date) return isNaN(input.getTime()) ? null : input;
        const str = String(input).trim();
        if (/^\d+$/.test(str)) {
          const d = new Date(Number(str));
          return isNaN(d.getTime()) ? null : d;
        }
        let d = new Date(str.replace(/-/g, '/'));
        if (isNaN(d.getTime()) && dateUtils && typeof dateUtils.parse === 'function') {
          d = dateUtils.parse(str);
        }
        return isNaN(d.getTime()) ? null : d;
      } catch (e) {
        return null;
      }
    },
    onSelfCommentTap(c) {
      if (!c) return;
      if (String(c.userId) !== String(this.currentUserId)) return;
      this.deleteTarget = c;
      this.$refs?.deletePopup?.open?.('center');
    },
    confirmDelete() {
      const id = this.deleteTarget?.id;
      if (!id) {
        this.$refs?.deletePopup?.close?.();
        return;
      }
      uni.showModal({
        title: '确认删除',
        content: '确定删除这条评论？',
        success: (res) => {
          if (res && res.confirm) {
            this.deleteComment(id);
          }
        }
      });
    },
    isCreator(c) {
      const billCreatorId = this.billDetail && (this.billDetail.userId || this.billDetail.createUserId);
      if (billCreatorId) {
        return String(c.userId) === String(billCreatorId);
      }
      const billCreatorName = this.billDetail && this.billDetail.createUserName;
      return billCreatorName ? String(c.userName) === String(billCreatorName) : false;
    }
  },
  async loadPreviousPageAtTop() {
    if (this.loadingPrev) return;
    this.loadingPrev = true;
    try {
      const anchorId = await this.getTopVisibleAnchorId();
      const nextPage = this.pageNum + 1;
      const resp = await request.get(backUrl.endpoints.bill_commentsByBillId + this.billId, { pageNum: nextPage, pageSize: this.pageSize });
      const itemList = Array.isArray(resp?.itemList) ? resp.itemList : (Array.isArray(resp) ? resp : (resp?.list || []));
      const avatarMap = resp && resp.avatarUrlMap ? resp.avatarUrlMap : {};
      const total = (resp && typeof resp.total !== 'undefined') ? Number(resp.total) : null;
      if (typeof total === 'number' && !isNaN(total)) this.totalCount = total;
      const newItems = (itemList || []).map(it => {
        const uidKey = (it && it.userId != null) ? String(it.userId) : '';
        const mappedAvatar = avatarMap[uidKey] || avatarMap[it?.userId] || null;
        return { ...it, avatarUrl: it?.avatarUrl || mappedAvatar || it?.avatarUrl };
      });
      const existingIds = new Set((this.comments || []).map(c => c.id));
      const dedupedNew = newItems.filter(n => !existingIds.has(n.id));
      this.comments = [...dedupedNew, ...this.comments];
      this.pageNum = nextPage;
      this.hasMore = (typeof this.totalCount === 'number' && this.totalCount > 0)
        ? (this.comments.length < this.totalCount)
        : (newItems.length === this.pageSize);
      this.$nextTick(() => {
        if (anchorId) {
          this.scrollAnchorId = anchorId;
          setTimeout(() => { this.scrollAnchorId = ''; }, 300);
        }
      });
    } catch (e) {
      console.error(e);
      uni.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      this.loadingPrev = false;
    }
  },
  async getTopVisibleAnchorId() {
    return new Promise(resolve => {
      try {
        const q = uni.createSelectorQuery().in(this);
        q.select('.list-scroll').boundingClientRect();
        q.selectAll('.list .item').boundingClientRect();
        q.exec(res => {
          const containerRect = res && res[0];
          const itemsRect = res && res[1];
          if (!containerRect || !itemsRect || !itemsRect.length) return resolve('');
          const topY = containerRect.top;
          let index = 0;
          for (let i = 0; i < itemsRect.length; i++) {
            if (itemsRect[i] && typeof itemsRect[i].top === 'number' && itemsRect[i].top >= topY - 1) {
              index = i; break;
            }
          }
          const item = this.comments && this.comments[index];
          resolve(item && item.id ? ('c-' + item.id) : '');
        });
      } catch (_) {
        resolve('');
      }
    });
  },
  onPullDownRefresh() {
    uni.stopPullDownRefresh();
  },
  onReachBottom() {
    // 使用分页控件，禁用滚动触底加载
  },
  // onPageChange moved into methods
};
</script>

<style scoped>
.comment-page { height: 100vh; display: flex; flex-direction: column; overflow: hidden; }
.page-content { display: flex; flex-direction: column; flex: 1; overflow: hidden; }
.loading, .empty { color: #888; margin: 8px 0; }

/* 顶部固定账单信息（不随页面滚动） */
.bill-header { background: #f7f7f7; border-bottom: 1px solid #eee; padding: 12px; flex-shrink: 0; }
.bh-row { display: flex; justify-content: space-between; align-items: center; }
.bh-category { font-size: 14px; color: #333; font-weight: bold; }
.bh-amount { font-size: 16px; font-weight: 600; }
.bh-ledger-line { display: inline-flex; align-items: center; gap: 6px; flex-wrap: nowrap; }
.bh-ledger { font-size: 12px; color: #666; }
.bh-time { font-size: 12px; color: #999; }
/* 新增：备注样式 */
.bh-memo-row { justify-content: flex-start; }
.bh-memo { font-size: 12px; color: #666; margin-top: 2px; }

/* 评论列表容器（允许滚动） */
.list-scroll { flex: 1; min-height: 0; height: 100%; padding: 8px 20px 8px 12px; }
.list-pagination { display: flex; justify-content: center; align-items: center; padding: 8px 0 6px; }
.item { padding: 10px 0; border-bottom: 1px solid #eee; }
.item.left { text-align: left; }
.item.right { text-align: right; padding-right: 20px; }
.bubble-row { display: flex; align-items: flex-start; gap: 8px; width: 100%; }
.bubble-row.left { flex-direction: row; justify-content: flex-start; }
.bubble-row.right { flex-direction: row; justify-content: flex-end; padding-right: 20px; }
/* 调整右侧消息的排列顺序，使头像贴右侧 */
.bubble-row.right .msg-block { order: 1; }
.bubble-row.right .name-block { order: 2; }
.bubble-row.right .avatar-tiny { order: 3; }
.avatar-tiny { width: 96rpx; height: 96rpx; transform: none; transform-origin: center; overflow: hidden; display: flex; align-items: center; border-radius: 50%; background: linear-gradient(135deg, #ffffff 0%, #f7fbff 100%); box-shadow: 0 4px 12px rgba(22,119,255,0.08); border: 2px solid rgba(22,119,255,0.18); flex-shrink: 0; }
.name-block { display: flex; flex-direction: column; }
.item.right .name-block { align-items: flex-end; text-align: right; }
.creator-tag { font-size: 10px; color: #FF9500; border: 1px solid #FF9500; padding: 2px 6px; border-radius: 10px; margin-bottom: 4px; }
.user { font-size: 12px; color: #333; }
.time { font-size: 12px; color: #999; }
.msg-block { max-width: 70%; }
.content { margin-top: 6px; font-size: 14px; }
.image-thumb { width: 100px; height: 100px; object-fit: cover; margin-top: 6px; border-radius: 6px; }

/* 发送图片标签 */
.editor-toolbar { padding: 8px 12px; border-top: 1px solid #f0f0f0; flex-shrink: 0; }
.image-tag { display: inline-block; font-size: 12px; color: #1989fa; border: 1px solid #1989fa; padding: 4px 8px; border-radius: 12px; }

/* 底部文本输入与发送按钮 */
.editor { padding: 8px 12px; border-top: 1px solid #f0f0f0; background: #fff; flex-shrink: 0; }
.input-row { display: flex; align-items: center; gap: 8px; flex-wrap: nowrap; }
.input { width: 100%; height: 44px; min-height: 44px; border: 1px solid #ddd; border-radius: 10px; padding: 10px 14px; background: #f9f9f9; transition: all .2s ease; }
.input:focus { border-color: #1989fa; box-shadow: 0 0 0 2px rgba(25,137,250,0.12); background: #fff; }
.send-btn { height: 44px; line-height: 44px; padding: 0 18px; background: linear-gradient(90deg, #1989fa, #4aa3ff); color: #fff; border-radius: 10px; box-shadow: 0 4px 10px rgba(25,137,250,0.18); display: inline-flex; align-items: center; justify-content: center; white-space: nowrap; text-align: center; flex-shrink: 0; }

.load-more { display: flex; justify-content: center; padding: 10px 0; }
button { margin-top: 0; }
.income { color: #07c160; }
.expense { color: #ee0a24; }

.actions-popup { padding: 18px 16px; min-width: 280px; background: #fff; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.12); }
.popup-title { font-size: 16px; font-weight: 600; color: #333; }
.popup-subtitle { margin-top: 6px; font-size: 12px; color: #888; max-height: 36px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.popup-actions { margin-top: 12px; display: flex; flex-direction: column; gap: 8px; }
.action-item { display: flex; align-items: center; gap: 10px; padding: 10px 12px; border-radius: 10px; background: #f7f7f7; transition: background .2s ease; }
.action-item:hover { background: #f0f0f0; }
.action-item .action-text { font-size: 14px; color: #333; }
.action-item.danger { background: #fff5f5; }
.action-item.danger .action-text { color: #ee0a24; }
.action-item.primary { background: #f0f8ff; }
.more-btn { width: 20px; height: 20px; margin-top: 4px; margin-left: 6px; opacity: 0.7; }
</style>