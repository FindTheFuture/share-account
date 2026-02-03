package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.AccountMapper;
import cn.lizongyi.shareaccount.entity.Account;
import cn.lizongyi.shareaccount.enums.AccountStatusEnum;
import cn.lizongyi.shareaccount.request.CreateAccountRequest;
import cn.lizongyi.shareaccount.response.AccountResponse;
import cn.lizongyi.shareaccount.services.AccountService;
import cn.lizongyi.shareaccount.services.BaseHandler;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private BaseHandler baseHandler;


    /**
     * 查询所有账户
     * @return
     */
    @Override
    public List<Account> findAll() {
        return accountMapper.findAll();
    }


    /**
     * 根据id查询账户信息
     * @param id 账户id
     * @return
     */
    @Override
    public Account findById(Long id) {
        return accountMapper.findById(id);
    }


    /**
     * 根据id查询账户信息
     * @param id 账户id
     * @return
     */
    @Override
    public AccountResponse findResponseById(Long id) {
        Account account = accountMapper.findById(id);
        return AccountResponse.fromAccount(account);
    }


    /**
     * 获取用户的账户列表
     * @param userId 用户id
     * @return
     */
    @Override
    public List<AccountResponse> getUserAccountList(Long userId) {
        List<Account> accounts = accountMapper.findByUserId(userId);
        return accounts.stream()
                .map(AccountResponse::fromAccount)
                .collect(Collectors.toList());
    }


    /**
     * 根据类型查询账户
     * @param type 类型
     * @return
     */
    @Override
    public List<Account> findByType(Integer type) {
        return accountMapper.findByType(type);
    }


    /**
     * 保存账户
     * @param request 账户信息
     * @return
     */
    @Transactional
    @Override
    public Boolean save(CreateAccountRequest request) {
        boolean isInsert = true;
        Long id = request.getId();
        Long targetUserId = null;
        if(id != null){
            Account exist = accountMapper.findById(id);
            isInsert = exist == null;
            if (exist != null) {
                targetUserId = exist.getUserId();
            }
        }

        Account account = new Account();
        account.setType(request.getType());
        account.setIsBudget(request.getIsBudget());
        account.setIsTotalMoney(request.getIsTotalMoney());
        account.setIsDefault(request.getIsDefault());
        account.setBalance(request.getBalance());
        account.setMemo(request.getMemo());
        account.setName(request.getName());

        if(isInsert){
            account.setUserId(baseHandler.getUserId());
            targetUserId = account.getUserId();

            // 游客限制：仅允许创建一个账户
            if (baseHandler.isGuestUser(targetUserId)) {
                List<Account> existingAccounts = accountMapper.findByUserId(targetUserId);
                if (existingAccounts != null && !existingAccounts.isEmpty()) {
                    // 已存在账户则拒绝新增
                    return false;
                }
            }
            // 若设置为默认，先清除该用户所有账户的默认标记
            if (request.getIsDefault() != null && request.getIsDefault() == 1) {
                accountMapper.clearDefaultForUser(targetUserId);
            }
            account.setStatus(0);
            account.setCreateTime(LocalDateTime.now());
            return accountMapper.insert(account) > 0;
        } else {
            account.setId(id);
            // 若设置为默认，先清除该用户所有账户的默认标记
            if (request.getIsDefault() != null && request.getIsDefault() == 1) {
                Long ownerUserId = targetUserId != null ? targetUserId : baseHandler.getUserId();
                accountMapper.clearDefaultForUser(ownerUserId);
            }
            return accountMapper.update(account) > 0;
        }
    }


    /**
     * 删除账户
     * @param id 账户id
     */
    @Override
    public void deleteById(Long id) {
        accountMapper.deleteById(id);
    }


    /**
     * 更新账户余额
     * @param accountId 账户id
     * @param balance 余额
     * @return
     */
    @Override
    public Boolean updateBalance(Long accountId, Long balance) {
        Account account = accountMapper.findById(accountId);
        if (account != null) {
            account.setBalance(balance);
            int rows = accountMapper.update(account);
            return rows > 0;
        }
        return false;
    }


    /**
     * 更新账户状态
     * @param accountId 账户id
     * @param status 状态
     * @return
     */
    @Override
    public Boolean updateStatus(Long accountId, Integer status) {
        Account account = accountMapper.findById(accountId);
        if (account == null) {
            return false;
        }
        int newStatus = account.getStatus() == AccountStatusEnum.NORMAL.getId() ? AccountStatusEnum.DISUSED.getId() : AccountStatusEnum.NORMAL.getId();
        return accountMapper.updateStatus(accountId, newStatus) > 0;
    }
}
