package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.LedgerRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.LedgerResponse;
import cn.lizongyi.shareaccount.response.SharedLedgerResponse;
import cn.lizongyi.shareaccount.services.LedgerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ledger")
@Slf4j
public class LedgerController {

    @Autowired
    private LedgerService ledgerService;

    /**
     * 根据ID查询账本
     */
    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<LedgerResponse>> getById(@PathVariable Long id) {
        log.info("查询账本详情: {}", id);
        
        if (id == null || id <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }

        LedgerResponse ledger = ledgerService.findResponseById(id);
        if (ledger == null) {
            return ResponseEntity.ok(ApiResponse.badRequest("未找到账本信息"));
        }

        return ResponseEntity.ok(ApiResponse.success(ledger));
    }

    /**
     * 查询当前用户的所有账本
     */
    @GetMapping("/getByUser")
    public ResponseEntity<ApiResponse<List<LedgerResponse>>> getByUser() {
        log.info("查询当前用户的账本列表");
        
        List<LedgerResponse> ledgers = ledgerService.findUserLedgers();
        return ResponseEntity.ok(ApiResponse.success(ledgers));
    }

    /**
     * 根据状态查询账本
     */
    @GetMapping("/getByStatus/{status}")
    public ResponseEntity<ApiResponse<List<LedgerResponse>>> getByStatus(@PathVariable Integer status) {
        log.info("查询状态为{}的账本列表", status);
        
        List<LedgerResponse> ledgers = ledgerService.findByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(ledgers));
    }

    /**
     * 保存账本（创建或更新）
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Boolean>> save(@RequestBody LedgerRequest request) {
        log.info("保存账本: {}", request);
        
        Boolean result = ledgerService.saveLedger(request);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success(true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("保存账本失败"));
        }
    }

    /**
     * 更新账本状态
     */
    @PostMapping("/updateStatus")
    public ResponseEntity<ApiResponse<Boolean>> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        log.info("更新账本状态: id={}, status={}", id, status);
        
        Boolean result = ledgerService.updateLedgerStatus(id, status);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success(true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("更新账本状态失败"));
        }
    }

    /**
     * 删除账本
     */
    @PostMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        log.info("删除账本: {}", id);
        
        Boolean result = ledgerService.deleteLedger(id);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success(true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("删除账本失败"));
        }
    }

    @GetMapping("/getSharedToMe")
    public ResponseEntity<ApiResponse<List<SharedLedgerResponse>>> getSharedToMe() {
        try {
            List<SharedLedgerResponse> list = ledgerService.findSharedLedgersToMe();
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            log.error("查询别人分享给我账本失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询别人分享给我账本失败"));
        }
    }
}