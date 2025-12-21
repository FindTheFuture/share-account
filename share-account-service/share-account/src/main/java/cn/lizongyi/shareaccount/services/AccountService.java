package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.Account;
import cn.lizongyi.shareaccount.request.CreateAccountRequest;
import cn.lizongyi.shareaccount.response.AccountResponse;

import java.util.List;

public interface AccountService {
    /**
     * 查询所有账户信息
     * @return 账户实体列表
     */
    List<Account> findAll();

    /**
     * 根据 ID 查询账户信息
     * @param id 账户 ID
     * @return 账户实体
     */
    Account findById(Long id);

    /**
     * 根据 ID 查询账户响应信息
     * @param id 账户 ID
     * @return 账户响应对象
     */
    AccountResponse findResponseById(Long id);

    /**
     * 根据用户 ID 查询该用户的账户列表
     * @param userId 用户 ID
     * @return 账户响应对象列表
     */
    List<AccountResponse> getUserAccountList(Long userId);

    /**
     * 根据账户类型查询账户列表
     * @param type 账户类型
     * @return 账户实体列表
     */
    List<Account> findByType(Integer type);

    /**
     * 创建账户
     * @param request 创建账户请求对象
     * @return 创建成功返回 true，失败返回 false
     */
    Boolean save(CreateAccountRequest request);

    /**
     * 根据 ID 删除账户
     * @param id 账户 ID
     */
    void deleteById(Long id);

    /**
     * 更新账户余额
     * @param accountId 账户 ID
     * @param balance 新的余额
     * @return 更新成功返回 true，失败返回 false
     */
    Boolean updateBalance(Long accountId, Long balance);

    /**
     * 更新账户状态
     * @param accountId 账户 ID
     * @param status 新的账户状态
     * @return 更新成功返回 true，失败返回 false
     */
    Boolean updateStatus(Long accountId, Integer status);
}
