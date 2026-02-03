package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.Ledger;
import cn.lizongyi.shareaccount.request.LedgerRequest;
import cn.lizongyi.shareaccount.response.LedgerResponse;
import cn.lizongyi.shareaccount.response.SharedLedgerResponse;

import java.util.List;

public interface LedgerService {
    /**
     * 根据ID查询账本
     */
    Ledger findById(Long id);

    /**
     * 转换为响应对象
     */
    LedgerResponse findResponseById(Long id);

    /**
     * 查询当前用户的所有账本
     */
    List<LedgerResponse> findUserLedgers();

    /**
     * 根据状态查询账本
     */
    List<LedgerResponse> findByStatus(Integer status);

    /**
     * 创建或更新账本
     * 如果request.id为空，则创建账本；否则更新账本
     */
    Boolean saveLedger(LedgerRequest request);

    /**
     * 更新账本状态
     */
    Boolean updateLedgerStatus(Long id, Integer status);

    /**
     * 删除账本
     */
    Boolean deleteLedger(Long id);

    /**
     * 查询别人分享给当前用户的账本（仅账本级分享、且账本状态正常）
     */
    java.util.List<SharedLedgerResponse> findSharedLedgersToMe();
}