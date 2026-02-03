package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.LedgerMapper;
import cn.lizongyi.shareaccount.entity.Ledger;
import cn.lizongyi.shareaccount.request.LedgerRequest;
import cn.lizongyi.shareaccount.response.LedgerResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.LedgerService;
import cn.lizongyi.shareaccount.dao.MemberMapper;
import cn.lizongyi.shareaccount.entity.Member;
import cn.lizongyi.shareaccount.response.SharedLedgerResponse;
import cn.lizongyi.shareaccount.services.UserService;
import cn.lizongyi.shareaccount.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账本服务实现类
 */
@Service
@Slf4j
public class LedgerServiceImpl implements LedgerService {

    @Autowired
    private LedgerMapper ledgerMapper;

    @Autowired
    private BaseHandler baseHandler;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private UserService userService;

    @Override
    public Ledger findById(Long id) {
        try {
            return ledgerMapper.findById(id);
        } catch (Exception e) {
            log.error("查询账本失败: id={}", id, e);
            return null;
        }
    }

    @Override
    public LedgerResponse findResponseById(Long id) {
        try {
            Ledger ledger = ledgerMapper.findById(id);
            if (ledger == null) {
                log.warn("未找到账本: id={}", id);
                return null;
            }
            
            // 验证用户访问权限
            if (!verifyUserAccess(ledger.getId(), baseHandler.getUserId())) {
                log.warn("无权限访问账本: ledgerId={}, userId={}", ledger.getId(), baseHandler.getUserId());
                return null;
            }
            
            return LedgerResponse.fromLedger(ledger);
        } catch (Exception e) {
            log.error("查询账本响应失败: id={}", id, e);
            return null;
        }
    }

    @Override
    public List<LedgerResponse> findUserLedgers() {
        try {
            Long userId = baseHandler.getUserId();
            List<Ledger> ledgers = ledgerMapper.findByUserId(userId);
            
            if (CollectionUtils.isEmpty(ledgers)) {
                return new ArrayList<>();
            }
            
            return ledgers.stream()
                    .map(LedgerResponse::fromLedger)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询用户账本列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<LedgerResponse> findByStatus(Integer status) {
        try {
            List<Ledger> ledgers = ledgerMapper.findByStatus(status);
            
            if (CollectionUtils.isEmpty(ledgers)) {
                return new ArrayList<>();
            }
            
            // 只返回用户有权限的账本
            Long userId = baseHandler.getUserId();
            return ledgers.stream()
                    .filter(ledger -> verifyUserAccess(ledger.getId(), userId))
                    .map(LedgerResponse::fromLedger)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据状态查询账本列表失败: status={}", status, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Boolean saveLedger(LedgerRequest request) {
        try {
            Long userId = baseHandler.getUserId();
            
            // 调试日志：打印接收到的请求数据
            log.info("接收到的LedgerRequest数据: id={}, name={}, type={}, property={}, classId={}, memo={}, isDefault={}", 
                     request.getId(), request.getName(), request.getType(), request.getProperty(), 
                     request.getClassId(), request.getMemo(), request.getIsDefault());
            
            // 如果请求中设置了isDefault为1，先重置该用户所有账本的默认状态
            if (request.getIsDefault() != null && request.getIsDefault() == 1) {
                ledgerMapper.resetDefaultLedger(userId);
            }
            
            if (request.getId() == null) {
                // 游客限制：仅允许创建一个账本
                if (baseHandler.isGuestUser(userId)) {
                    List<Ledger> existingLedgers = ledgerMapper.findByUserId(userId);
                    if (existingLedgers != null && !existingLedgers.isEmpty()) {
                        log.info("游客用户仅允许创建一个账本: userId={}", userId);
                        return false;
                    }
                }
                // 创建新账本
                Ledger ledger = new Ledger();
                BeanUtils.copyProperties(request, ledger);
                ledger.setUserId(userId);
                ledger.setStatus(0); // 默认状态为正常
                ledger.setCreateTime(LocalDateTime.now());
                
                // 调试日志：打印创建前的账本实体数据
                log.info("创建账本前的实体数据: name={}, memo={}, isDefault={}", ledger.getName(), ledger.getMemo(), ledger.getIsDefault());
                
                int rows = ledgerMapper.insert(ledger);
                
                if (rows > 0) {
                    log.info("创建账本成功: userId={}, name={}", userId, ledger.getName());
                    return true;
                } else {
                    log.error("创建账本失败: 数据库插入失败");
                    return false;
                }
            } else {
                // 更新账本
                // 验证账本是否存在
                Ledger existingLedger = ledgerMapper.findById(request.getId());
                if (existingLedger == null) {
                    log.warn("更新账本失败: 账本不存在 id={}", request.getId());
                    return false;
                }
                
                // 验证用户访问权限
                if (!verifyUserAccess(existingLedger.getId(), userId)) {
                    log.warn("无权限更新账本: ledgerId={}, userId={}", existingLedger.getId(), userId);
                    return false;
                }
                
                // 更新账本信息
                BeanUtils.copyProperties(request, existingLedger);
                
                // 调试日志：打印更新前的账本实体数据
                log.info("更新账本前的实体数据: name={}, memo={}, isDefault={}", existingLedger.getName(), existingLedger.getMemo(), existingLedger.getIsDefault());
                
                int rows = ledgerMapper.update(existingLedger);
                
                if (rows > 0) {
                    log.info("更新账本成功: id={}, name={}", request.getId(), existingLedger.getName());
                    return true;
                } else {
                    log.error("更新账本失败: 数据库更新失败");
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("保存账本失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Boolean updateLedgerStatus(Long id, Integer status) {
        try {
            // 验证账本是否存在
            Ledger existingLedger = ledgerMapper.findById(id);
            if (existingLedger == null) {
                log.warn("更新账本状态失败: 账本不存在 id={}", id);
                return false;
            }
            
            // 验证用户访问权限
            if (!verifyUserAccess(existingLedger.getId(), baseHandler.getUserId())) {
                log.warn("无权限更新账本状态: ledgerId={}, userId={}", existingLedger.getId(), baseHandler.getUserId());
                return false;
            }
            
            int rows = ledgerMapper.updateStatus(id, status);
            
            if (rows > 0) {
                log.info("更新账本状态成功: id={}, status={}", id, status);
                return true;
            } else {
                log.error("更新账本状态失败: 数据库更新失败");
                return false;
            }
        } catch (Exception e) {
            log.error("更新账本状态失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Boolean deleteLedger(Long id) {
        try {
            // 验证账本是否存在
            Ledger existingLedger = ledgerMapper.findById(id);
            if (existingLedger == null) {
                log.warn("删除账本失败: 账本不存在 id={}", id);
                return false;
            }
            
            // 验证用户访问权限
            if (!verifyUserAccess(existingLedger.getId(), baseHandler.getUserId())) {
                log.warn("无权限删除账本: ledgerId={}, userId={}", existingLedger.getId(), baseHandler.getUserId());
                return false;
            }
            
            int rows = ledgerMapper.deleteById(id);
            
            if (rows > 0) {
                log.info("删除账本成功: id={}", id);
                return true;
            } else {
                log.error("删除账本失败: 数据库删除失败");
                return false;
            }
        } catch (Exception e) {
            log.error("删除账本失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public java.util.List<SharedLedgerResponse> findSharedLedgersToMe() {
        try {
            Long currentUserId = baseHandler.getUserId();
            java.util.List<Member> members = memberMapper.findNormalByUserId(currentUserId, 1);
            if (members == null || members.isEmpty()) {
                return new java.util.ArrayList<>();
            }
            
            java.util.Set<String> seen = new java.util.LinkedHashSet<>();
            java.util.List<SharedLedgerResponse> results = new java.util.ArrayList<>();
            
            for (Member m : members) {
                // 仅保留符合条件的记录：账本级分享（bill_id为空）且 status=1
                if (!isLedgerLevelShareActive(m)) {
                    continue;
                }
                // 去重键：(ledger_id, parent_user_id, user_id)
                String key = composeMemberKey(m);
                if (!seen.add(key)) {
                    continue; // 已存在同样记录
                }
            
                Ledger ledger = ledgerMapper.findById(m.getLedgerId());
                if (ledger == null || ledger.getStatus() == null || ledger.getStatus() != 0) {
                    // 账本不存在或状态非正常，跳过
                    continue;
                }
            
                SharedLedgerResponse r = buildSharedResponse(m, ledger);
                if (r != null) {
                    results.add(r);
                }
            }
            return results;
        } catch (Exception e) {
            log.error("查询别人分享给我账本列表失败", e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 验证用户访问权限
     */
    private boolean verifyUserAccess(Long ledgerId, Long userId) {
        try {
            Ledger ledger = ledgerMapper.findById(ledgerId);
            if (ledger == null) {
                return false;
            }
            // 检查是否为创建者
            return ledger.getUserId().equals(userId);
        } catch (Exception e) {
            log.error("验证用户访问权限失败: ledgerId={}, userId={}", ledgerId, userId, e);
            return false;
        }
    }

private boolean isLedgerLevelShareActive(Member m) {
    return m != null && m.getBillId() == null && m.getStatus() != null && m.getStatus() == 1;
}

private String composeMemberKey(Member m) {
    Long lid = m.getLedgerId();
    Long pid = m.getParentUserId();
    Long uid = m.getUserId();
    return (lid == null ? "null" : lid.toString()) + "|" +
           (pid == null ? "null" : pid.toString()) + "|" +
           (uid == null ? "null" : uid.toString());
}

private SharedLedgerResponse buildSharedResponse(Member m, Ledger ledger) {
    SharedLedgerResponse r = new SharedLedgerResponse();
    r.setParentUserId(m.getParentUserId());
    // 父用户信息
    UserResponse parentUser = userService.findResponseById(m.getParentUserId());
    if (parentUser != null) {
        r.setParentUserName(parentUser.getNickName());
        r.setParentUserPictureAddress(parentUser.getPictureAddress());
    }
    r.setShareTime(m.getCreateTime() != null ? m.getCreateTime().toString() : "");
    // 账本信息
    r.setLedgerId(ledger.getId());
    r.setLedgerName(ledger.getName());
    r.setStatus(ledger.getStatus());
    r.setStatusName(ledger.getStatus() != null && ledger.getStatus() == 1 ? "已弃用" : "正常");
    r.setType(ledger.getType());
    r.setTypeName(ledger.getType() != null && ledger.getType() == 1 ? "个人账本" : "共享账本");
    r.setProperty(ledger.getProperty());
    r.setPropertyName(ledger.getProperty() != null && ledger.getProperty() == 1 ? "AA账本" : "普通账本");
    r.setMemo(ledger.getMemo());
    return r;
}
}