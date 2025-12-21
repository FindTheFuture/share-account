package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.CreateAccountRequest;
import cn.lizongyi.shareaccount.response.AccountResponse;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.services.AccountService;
import cn.lizongyi.shareaccount.services.BaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
@Slf4j
public class AccountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private BaseHandler baseHandler;

    /**
     * 根据 ID 查询账户信息
     * @param accountId 账户 ID
     * @return 包含账户信息的响应实体
     */
    @GetMapping("/getById/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> getById(@PathVariable Long accountId) {
        if (accountId == null || accountId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        log.info("根据账户 id:{} 查询账户信息", accountId);

        AccountResponse account = accountService.findResponseById(accountId);
        if (account == null) {
            return ResponseEntity.ok(ApiResponse.badRequest("未找到账户信息"));
        }

        return ResponseEntity.ok(ApiResponse.success(account));
    }

    /**
     * 查询账户列表
     * @return 包含账户列表响应信息的响应实体
     */
    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountList() {

        try {
            List<AccountResponse> list = accountService.getUserAccountList(baseHandler.getUserId());
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("查询账户失败: " + e.getMessage()));
        }
    }

    /**
     * 创建账户
     * @param request 创建账户请求对象
     * @return 包含创建结果的响应实体
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Boolean>> save(@RequestBody CreateAccountRequest request) {
        log.info("创建账户信息");
        try {
            Boolean success = accountService.save(request);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("创建账户信息失败: " + e.getMessage()));
        }
    }

    /**
     * 根据 ID 删除账户
     * @param accountId 账户 ID
     * @return 包含删除结果的响应实体
     */
    @DeleteMapping("/delete/{accountId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteById(@PathVariable Long accountId) {
        if (accountId == null || accountId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        log.info("根据账户 id:{} 删除账户信息", accountId);
        try {
            accountService.deleteById(accountId);
            return ResponseEntity.ok(ApiResponse.success(true));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("删除账户信息失败: " + e.getMessage()));
        }
    }

    /**
     * 更新账户余额
     * @param accountId 账户 ID
     * @param balance 新的余额
     * @return 包含更新结果的响应实体
     */
    @PutMapping("/updateBalance/{accountId}/{balance}")
    public ResponseEntity<ApiResponse<Boolean>> updateBalance(@PathVariable Long accountId, @PathVariable Long balance) {
        if (accountId == null || accountId <= 0 || balance == null) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        log.info("根据账户 id:{} 更新账户余额为:{}", accountId, balance);
        try {
            Boolean success = accountService.updateBalance(accountId, balance);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("更新账户余额失败: " + e.getMessage()));
        }
    }

    /**
     * 更新账户状态
     * @param accountId 账户 ID
     * @param status 新的账户状态
     * @return 包含更新结果的响应实体
     */
    @PutMapping("/updateStatus/{accountId}/{status}")
    public ResponseEntity<ApiResponse<Boolean>> updateStatus(@PathVariable Long accountId, @PathVariable Integer status) {
        if (accountId == null || accountId <= 0 || status == null) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        log.info("根据账户 id:{} 更新账户状态为:{}", accountId, status);
        try {
            Boolean success = accountService.updateStatus(accountId, status);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("更新账户状态失败: " + e.getMessage()));
        }
    }
}
