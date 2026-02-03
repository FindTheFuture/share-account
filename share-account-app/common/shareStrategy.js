import request from '@/common/request.js';
import backUrl from '@/common/back_url.js';

/**
 * 预保存账单对应的成员邀请记录，返回 memberId。
 * - 入参：当前账单对象，需包含 `id` 与 `ledgerId`。
 * - 返回：Promise<number|null>
 */
export async function prepareBillMember(currentBill) {
  if (!currentBill || !currentBill.id || !currentBill.ledgerId) {
    return null;
  }
  try {
    const payload = {
      name: '分享一个账单',
      billId: currentBill.id,
      ledgerId: currentBill.ledgerId
    };
    const res = await request.post(backUrl.endpoints.member_save, payload);
    return res || null;
  } catch (e) {
    console.error('prepareBillMember: save failed', e);
    return null;
  }
}

/**
 * 统一解析分享信息：有可用的账单且已准备 memberId 时走账单分享，否则账本分享。
 * - 入参：{ currentBill, billPopupVisible, shareIntent, shareMemberId }
 * - 返回：{ title, path, imageUrl }
 */
export function resolveShareInfo({ currentBill, billPopupVisible, shareIntent, shareMemberId }) {
  const titleBill = '分享一个账单';
  const titleLedger = '分享给你一个账本';
  const imageUrl = 'https://shareaccount-1302778096.cos.ap-beijing.myqcloud.com/title.png';

  const canShareBill = !!currentBill && (billPopupVisible || shareIntent) && !!shareMemberId;
  if (canShareBill) {
    return {
      title: titleBill,
      path: `/pages/member/acceptInvitation?id=${shareMemberId}`,
      imageUrl
    };
  }
  return {
    title: titleLedger,
    path: '/pages/firstpage/firstpage',
    imageUrl
  };
}