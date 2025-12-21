package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.UserMemberMapper;
import cn.lizongyi.shareaccount.entity.UserMember;
import cn.lizongyi.shareaccount.services.UserMemberService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import cn.lizongyi.shareaccount.enums.UserMemberStatusEnum;
import cn.lizongyi.shareaccount.response.UserMemberResponse;

/**
 * 用户会员服务实现类
 */
@Service
@Slf4j
public class UserMemberServiceImpl implements UserMemberService {
    
    @Autowired
    private UserMemberMapper userMemberMapper;
    

    
    @Override
    @Transactional
    public Long createUserMember(UserMember userMember) {
        try {
            userMember.setCreateTime(LocalDateTime.now());
            if (userMember.getStartTime() == null) {
                userMember.setStartTime(LocalDateTime.now());
            }
            int result = userMemberMapper.insert(userMember);
            if (result > 0) {
                return userMember.getId();
            }
        } catch (Exception e) {
            log.error("创建用户会员记录失败 userId={}, packageId={}", userMember.getUserId(), userMember.getPackageId(), e);
            throw new RuntimeException("创建用户会员记录失败", e);
        }
        return null;
    }
    
    @Override
    public boolean updateUserMemberStatus(Long id, Integer status) {
        try {
            int result = userMemberMapper.updateStatusById(id, status);
            return result > 0;
        } catch (Exception e) {
            log.error("更新用户会员状态失败 id={}, status={}", id, status, e);
            return false;
        }
    }
    
    @Override
    public boolean updateStatusByPaymentRecordId(Long paymentRecordId, Integer status) {
        try {
            int result = userMemberMapper.updateStatusByPaymentRecordId(paymentRecordId, status);
            return result > 0;
        } catch (Exception e) {
            log.error("根据支付记录ID更新用户会员状态失败 paymentRecordId={}, status={}", paymentRecordId, status, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean incrementAiUsedCount(Long userId) {
        try {
            UserMember userMember = userMemberMapper.findLatestValidByUserId(userId);
            if (userMember == null) {
                log.error("用户会员记录不存在 userId={}", userId);
                return false;
            }
            
            int result = userMemberMapper.incrementAiUsedCount(userMember.getId());
            return result > 0;
        } catch (Exception e) {
            log.error("增加AI使用次数失败 userId={}", userId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean incrementPdfUsedCount(Long userId) {
        try {
            UserMember userMember = userMemberMapper.findLatestValidByUserId(userId);
            if (userMember == null) {
                log.error("用户会员记录不存在 userId={}", userId);
                return false;
            }
            
            int result = userMemberMapper.incrementPdfUsedCount(userMember.getId());
            return result > 0;
        } catch (Exception e) {
            log.error("增加PDF使用次数失败 userId={}", userId, e);
            return false;
        }
    }
    
    @Override
    public List<UserMember> findNormalAndNotExpiredByUserId(Long userId) {
        try {
            return userMemberMapper.findValidUserMembers(userId);
        } catch (Exception e) {
            log.error("查询用户正常状态会员记录失败 userId={}", userId, e);
            return null;
        }
    }

    @Override
    public void checkAndUpdateExpiredMembers() {
        List<UserMember> allMembers = userMemberMapper.findExpiredMembers();
        if (allMembers != null && !allMembers.isEmpty()) {
            for (UserMember member : allMembers) {
                try {
                    userMemberMapper.updateStatusById(member.getId(), UserMemberStatusEnum.EXPIRED.getCode()); // 1表示过期状态
                } catch (Exception e) {
                    log.error("更新会员状态 为 过期状态 失败 id={}", member.getId(), e);
                }
            }
        }
    }
    
    @Override
    public List<UserMember> findUserMembersByUserId(Long userId) {
        try {
            return userMemberMapper.findByUserId(userId);
        } catch (Exception e) {
            log.error("查询用户会员记录失败 userId={}", userId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<UserMember> findUserMembersByUserIdAndStatus(Long userId, Integer status) {
        try {
            return userMemberMapper.findByUserIdAndStatus(userId, status);
        } catch (Exception e) {
            log.error("根据用户ID和状态查询会员记录失败 userId={}, status={}", userId, status, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public UserMember findLatestValidByUserIdAndType(Long userId, Integer packageType) {
        try {
            return userMemberMapper.findLatestValidByUserIdAndType(userId, packageType);
        } catch (Exception e) {
            log.error("查询最新有效会员记录失败 userId={}, packageType={}", userId, packageType, e);
            return null;
        }
    }

    @Override
    public void checkAndUpdateUserMemberExpiry(Long userId) {
        // 查询指定用户的所有会员记录
        List<UserMember> members = userMemberMapper.findByUserIdAndStatus(userId, UserMemberStatusEnum.NORMAL.getCode());
        if (!CollectionUtils.isEmpty(members)) {
            for (UserMember member : members) {
                try {
                    // 判断是否过期
                    if (member.getEndTime() != null && member.getEndTime().isBefore(LocalDateTime.now())) {
                        // 更新为已过期状态
                        userMemberMapper.updateStatusById(member.getId(), UserMemberStatusEnum.EXPIRED.getCode());
                        log.info("用户 {} 的会员记录 {} 已过期，更新状态为已过期", userId, member.getId());
                    }
                } catch (Exception e) {
                    log.error("更新用户会员状态失败 id={}, status={}", member.getId(), UserMemberStatusEnum.EXPIRED.getCode(), e);
                }
            }
        }
    }
    
    @Override
    public boolean isUserMemberValid(Long userId) {
        try {
            int count = userMemberMapper.countValidMembersByUserId(userId);
            return count > 0;
        } catch (Exception e) {
            log.error("检查用户会员是否有效失败 userId={}", userId, e);
            return false;
        }
    }
    
    @Override
    public int countValidMembersByUserId(Long userId) {
        try {
            return userMemberMapper.countValidMembersByUserId(userId);
        } catch (Exception e) {
            log.error("统计用户 {} 有效会员记录失败", userId, e);
            return 0;
        }
    }
    
    @Override
    public List<UserMember> findUserMembersByPage(Long userId, Integer currentPage, Integer pageSize) {
        try {
            Integer offset = (currentPage - 1) * pageSize;
            return userMemberMapper.findUserMembersByPage(userId, offset, pageSize);
        } catch (Exception e) {
            log.error("分页查询用户 {} 会员记录失败", userId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Long countUserMembers(Long userId) {
        try {
            return userMemberMapper.countUserMembers(userId);
        } catch (Exception e) {
            log.error("统计用户 {} 会员记录总数失败", userId, e);
            return 0L;
        }
    }
    
    @Override
    public Integer getRemainingAiCount(Long userId) {
        try {
            List<UserMember> validMembers = userMemberMapper.findValidUserMembers(userId);
            if (CollectionUtils.isEmpty(validMembers)) {
                return 0;
            }

            int totalRemaining = 0;
            
            for (UserMember member : validMembers) {
                totalRemaining += (member.getAiCount() - member.getAiUsedCount());
            }
            
            return Math.max(0, totalRemaining);
        } catch (Exception e) {
            log.error("获取用户 {} AI识别剩余次数失败", userId, e);
            return 0;
        }
    }
    
    @Override
    public Integer getRemainingPdfCount(Long userId) {
        try {
            List<UserMember> validMembers = userMemberMapper.findValidUserMembers(userId);
            if (CollectionUtils.isEmpty(validMembers)) {
                return 0;
            }

            int totalRemaining = 0;
            
            for (UserMember member : validMembers) {
                totalRemaining += (member.getPdfCount() - member.getPdfUsedCount());
            }
            
            return Math.max(0, totalRemaining);
        } catch (Exception e) {
            log.error("获取用户 {} PDF导出剩余次数失败", userId, e);
            return 0;
        }
    }
    

    
    @Override
    public PageInfo<UserMemberResponse> queryUserMembersWithPage(Long userId, Integer currentPage, Integer pageSize, Integer status) {
        try {
            // 先使用pageSize=1000查询所有记录，确保获取总数正确
            PageHelper.startPage(currentPage, pageSize, true);
            List<UserMember> userMembers = userMemberMapper.findByUserIdCanStatus(userId, status);
            // 先创建原始数据的PageInfo
            PageInfo<UserMember> originalPageInfo = new PageInfo<>(userMembers);
            
            // 转换数据
            List<UserMemberResponse> responses = UserMemberResponse.fromUserMembers(userMembers);
            
            // 创建新的PageInfo并设置正确的总数
            PageInfo<UserMemberResponse> resultPageInfo = new PageInfo<>(responses);
            resultPageInfo.setTotal(originalPageInfo.getTotal());
            resultPageInfo.setPages(originalPageInfo.getPages());
            resultPageInfo.setPageNum(originalPageInfo.getPageNum());
            resultPageInfo.setPageSize(originalPageInfo.getPageSize());
            
            return resultPageInfo;
        } catch (Exception e) {
            log.error("分页查询用户会员记录失败 userId={}", userId, e);
            throw new RuntimeException("分页查询用户会员记录失败", e);
        }
    }
    
    @Override
    public PageInfo<UserMember> findByUserIdWithPage(Long userId, Integer currentPage, Integer pageSize) {
        try {
            PageHelper.startPage(currentPage, pageSize);
            List<UserMember> userMembers = userMemberMapper.findByUserId(userId);
            return new PageInfo<>(userMembers);
        } catch (Exception e) {
            log.error("分页查询用户会员记录失败 userId={}", userId, e);
            throw new RuntimeException("分页查询用户会员记录失败", e);
        }
    }
    

    
    @Override
    public UserMember findByPaymentRecordId(Long paymentRecordId) {
        try {
            return userMemberMapper.findByPaymentRecordId(paymentRecordId);
        } catch (Exception e) {
            log.error("根据支付记录ID查询用户会员记录失败 paymentRecordId={}", paymentRecordId, e);
            return null;
        }
    }
}