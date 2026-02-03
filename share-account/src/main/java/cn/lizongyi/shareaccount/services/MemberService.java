package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.Member;
import cn.lizongyi.shareaccount.request.MemberRequest;
import cn.lizongyi.shareaccount.response.MemberResponse;
import java.util.List;

public interface MemberService {

    /**
     * 根据ID获取成员信息
     */
    MemberResponse getMemberById(Long id);

    /**
     * 根据账本ID获取成员列表
     */
    List<MemberResponse> getMembersByLedgerId(Long ledgerId);

    /**
     * 根据用户ID获取成员列表
     */
    List<MemberResponse> getMembersByUserId(Long userId);

    /**
     * 根据父用户ID获取成员列表
     */
    List<MemberResponse> getMembersByParentUserId(Long parentUserId);
    
    /**
     * 获取当前用户的正常状态成员列表（status = 1）
     */
    List<MemberResponse> getNormalMembersByUserId();

    /**
     * 根据账本ID和父用户ID获取成员列表
     */
    List<MemberResponse> getMembersByLedgerIdAndParentUserId(Long ledgerId, Long parentUserId);

    /**
     * 更新成员占比
     */
    boolean updateMemberPercentage(Long id, Integer percentage);

    /**
     * 新增或更新成员
     * @return 成员记录id
     */
    Long saveMember(MemberRequest request);
    
    /**
     * 删除成员
     */
    boolean deleteMember(Long id);

    /**
     * 根据账本ID和用户ID删除成员
     */
    boolean deleteMemberByLedgerIdAndUserId(Long ledgerId, Long userId);

    /**
     * 获取账本成员总占比
     */
    Integer getTotalPercentageByLedgerId(Long ledgerId);
    
    /**
     * 接受邀请
     * @param id 成员ID
     * @param userId 当前用户ID
     * @return 是否成功
     */
    boolean acceptInvitation(Long id, Long userId);
}