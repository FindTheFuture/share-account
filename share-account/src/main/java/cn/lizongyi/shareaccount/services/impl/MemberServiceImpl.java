package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.LedgerMapper;
import cn.lizongyi.shareaccount.dao.MemberMapper;
import cn.lizongyi.shareaccount.entity.Ledger;
import cn.lizongyi.shareaccount.entity.Member;
import cn.lizongyi.shareaccount.request.MemberRequest;
import cn.lizongyi.shareaccount.response.MemberResponse;
import cn.lizongyi.shareaccount.services.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.UserService;
import cn.lizongyi.shareaccount.response.UserResponse;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private LedgerMapper ledgerMapper;

    @Autowired
    private BaseHandler baseHandler;

    @Autowired
    private UserService userService;

    @Override
    public MemberResponse getMemberById(Long id) {
        log.info("根据ID获取成员信息: {}", id);
        try {
            Member member = memberMapper.findById(id);
            return convertToMemberResponse(member, true);
        } catch (Exception e) {
            log.error("获取成员信息失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<MemberResponse> getMembersByLedgerId(Long ledgerId) {
        log.info("根据账本ID获取成员列表: {}", ledgerId);
        try {
            List<Member> members = memberMapper.findByLedgerId(ledgerId);
            return convertToMemberResponseList(members);
        } catch (Exception e) {
            log.error("获取成员列表失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<MemberResponse> getMembersByUserId(Long userId) {
        log.info("根据用户ID获取成员列表: {}", userId);
        try {
            List<Member> members = memberMapper.findByUserId(userId);
            return convertToMemberResponseList(members);
        } catch (Exception e) {
            log.error("获取成员列表失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<MemberResponse> getMembersByParentUserId(Long parentUserId) {
        log.info("根据父用户ID获取成员列表: {}", parentUserId);
        try {
            List<Member> members = memberMapper.findByParentUserId(parentUserId);
            return convertToMemberResponseList(members);
        } catch (Exception e) {
            log.error("获取成员列表失败: {}", e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<MemberResponse> getNormalMembersByUserId() {
        log.info("获取当前用户的正常状态成员列表");
        try {
            Long userId = baseHandler.getUserId();
            List<Member> members = memberMapper.findNormalByUserId(userId, 1);
            return convertToMemberResponseList(members);
        } catch (Exception e) {
            log.error("获取正常状态成员列表失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<MemberResponse> getMembersByLedgerIdAndParentUserId(Long ledgerId, Long parentUserId) {
        log.info("根据账本ID和父用户ID获取成员列表: ledgerId={}, parentUserId={}", ledgerId, parentUserId);
        try {
            List<Member> members = memberMapper.findByLedgerIdAndParentUserId(ledgerId, parentUserId);
            return convertToMemberResponseList(members);
        } catch (Exception e) {
            log.error("获取成员列表失败: {}", e.getMessage());
            return null;
        }
    }

    // 将Member转换为MemberResponse，并设置账本名称
    private MemberResponse convertToMemberResponse(Member member, boolean showMoreInfo) {
        if (member == null) {
            return null;
        }
        
        MemberResponse response = MemberResponse.from(member);
        
        // 查询账本名称
        if (member.getLedgerId() != null) {
            try {
                Ledger ledger = ledgerMapper.findById(member.getLedgerId());
                if (ledger != null) {
                    response.setLedgerName(ledger.getName());
                }
            } catch (Exception e) {
                log.error("查询账本名称失败: {}", e.getMessage());
            }
        }
        
        // 查询用户名称
        if(showMoreInfo){
            if (member.getUserId() != null) {
                try {
                    UserResponse user = userService.findResponseById(member.getUserId());
                    if (user != null) {
                        response.setUserName(user.getNickName());
                        response.setUserPictureAddress(user.getPictureAddress());
                    }
                } catch (Exception e) {
                    log.error("查询用户名称失败: {}", e.getMessage());
                }
            }
            
            // 查询父用户名称
            if (member.getParentUserId() != null) {
                try {
                    UserResponse parentUser = userService.findResponseById(member.getParentUserId());
                    if (parentUser != null) {
                        response.setParentUserName(parentUser.getNickName());
                        response.setParentUserPictureAddress(parentUser.getPictureAddress());
                    }
                } catch (Exception e) {
                    log.error("查询父用户名称失败: {}", e.getMessage());
                }
            }
        }
        
        return response;
    }
    
    // 批量转换Member列表为MemberResponse列表
    private List<MemberResponse> convertToMemberResponseList(List<Member> members) {
        if (members == null || members.isEmpty()) {
            return null;
        }
        
        return members.stream()
                .map(member -> convertToMemberResponse(member, true))
                .filter(response -> response != null)
                .collect(java.util.stream.Collectors.toList());
    }
    

    @Override
    public boolean updateMemberPercentage(Long id, Integer percentage) {
        log.info("更新成员占比: id={}, percentage={}", id, percentage);
        try {
            // 检查参数
            if (percentage < 0 || percentage > 100) {
                log.warn("占比必须在0-100之间");
                return false;
            }

            int result = memberMapper.updatePercentage(id, percentage);
            return result > 0;
        } catch (Exception e) {
            log.error("更新成员占比失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteMember(Long id) {
        log.info("删除成员: {}", id);
        try {
            int result = memberMapper.deleteById(id);
            return result > 0;
        } catch (Exception e) {
            log.error("删除成员失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteMemberByLedgerIdAndUserId(Long ledgerId, Long userId) {
        log.info("根据账本ID和用户ID删除成员: ledgerId={}, userId={}", ledgerId, userId);
        try {
            int result = memberMapper.deleteByLedgerIdAndUserId(ledgerId, userId);
            return result > 0;
        } catch (Exception e) {
            log.error("删除成员失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Integer getTotalPercentageByLedgerId(Long ledgerId) {
        log.info("获取账本成员总占比: {}", ledgerId);
        try {
            Integer totalPercentage = memberMapper.getTotalPercentageByLedgerId(ledgerId);
            return totalPercentage != null ? totalPercentage : 0;
        } catch (Exception e) {
            log.error("获取账本成员总占比失败: {}", e.getMessage());
            return 0;
        }
    }
    
    @Override
    public boolean acceptInvitation(Long id, Long userId) {
        log.info("接受邀请: memberId={}, userId={}", id, userId);
        try {
            // 检查参数
            if (id == null || userId == null) {
                log.warn("接受邀请参数不正确");
                return false;
            }
            
            // 查询成员是否存在
            Member member = memberMapper.findById(id);
            if (member == null) {
                log.warn("成员不存在");
                return false;
            }
            
            // 检查成员是否为邀请状态（假设0为邀请中状态）
            if (member.getStatus() != 0) {
                log.warn("该成员不是邀请状态");
                return false;
            }
            
            // 游客限制：游客无法接受邀请，同时若邀请人是游客也不允许共享
            if (baseHandler.isGuestUser(userId)) {
                log.warn("游客账号不能接受邀请: userId={}", userId);
                return false;
            }
            if (member.getParentUserId() != null && baseHandler.isGuestUser(member.getParentUserId())) {
                log.warn("邀请人是游客，无法进行共享: parentUserId={}", member.getParentUserId());
                return false;
            }
            
            // 使用新方法只更新必要字段
            int result = memberMapper.updateForAcceptInvitation(id, userId, 1);
            return result > 0;
        } catch (Exception e) {
            log.error("接受邀请失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public Long saveMember(MemberRequest request) {
        log.info("新增或更新成员: {}", request);
        try {
            // 验证参数
            if (request == null || request.getLedgerId() == null) {
                log.error("参数不正确");
                return null;
            }

            // 转换为实体类
            Member member = new Member();
            member.setName(request.getName())
                  .setLedgerId(request.getLedgerId())
                  .setBillId(request.getBillId())
                  .setParentUserId(baseHandler.getUserId())
                  .setUserId(request.getUserId())
                  .setPercentage(request.getPercentage());
                  
            if (request.getId() == null) {
                // 唯一性与 parent_user_id 相关：
                // 若明确目标用户存在，则按 (ledger_id, bill_id, parent_user_id, user_id) 查重；
                // 否则复用同一邀请人、同一账单的待接受邀请（user_id 为空，status=0）。
                /** Member existingMember;
                if (member.getUserId() != null && member.getUserId() > 0) {
                    existingMember = memberMapper.findByFullKey(member.getLedgerId(), member.getBillId(), member.getParentUserId(), member.getUserId());
                } else {
                    existingMember = memberMapper.findPendingInvite(member.getLedgerId(), member.getBillId(), member.getParentUserId());
                }
                if (existingMember != null) {
                    log.warn("成员已存在或存在待接受邀请，直接返回");
                    return existingMember.getId();
                } **/

                // 新增成员（邀请中）
                member.setStatus(0);
                member.setCreateTime(LocalDateTime.now());
                int result = memberMapper.insert(member);
                return result > 0 ? member.getId() : null;
            } else {
                // 更新成员
                member.setId(request.getId());

                // 检查是否存在该成员
                Member existingMember = getMemberById(request.getId());
                if (existingMember == null) {
                    log.warn("未找到要更新的成员");
                    return null;
                }
                // 检查权限
                if (!existingMember.getParentUserId().equals(member.getParentUserId())) {
                    log.warn("无权限更新此成员");
                    return null;
                }
                // 调用update方法更新
                member.setStatus(existingMember.getStatus());
                int result = memberMapper.update(member);
                return result > 0 ? member.getId() : null;
            }
        } catch (Exception e) {
            log.error("新增或更新成员失败: {}", e.getMessage());
            return null;
        }
    }
    
}