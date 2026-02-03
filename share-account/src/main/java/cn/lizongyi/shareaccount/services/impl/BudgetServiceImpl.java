package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.BudgetItemMapper;
import cn.lizongyi.shareaccount.dao.BudgetMapper;
import cn.lizongyi.shareaccount.entity.Budget;
import cn.lizongyi.shareaccount.request.CreateBudgetRequest;
import cn.lizongyi.shareaccount.response.BudgetResponse;
import cn.lizongyi.shareaccount.services.BudgetService;
import cn.lizongyi.shareaccount.services.BaseHandler;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预算服务实现类
 */
@Service
public class BudgetServiceImpl implements BudgetService {

    @Resource
    private BudgetMapper budgetMapper;

    @Resource
    private BudgetItemMapper budgetItemMapper;

    @Resource
    private BaseHandler baseHandler;

    @Override
    public List<Budget> findAll() {
        return budgetMapper.findAll();
    }

    @Override
    public Budget findById(Long id) {
        return budgetMapper.findById(id);
    }

    @Override
    public List<Budget> findByUserId(Long userId) {
        return budgetMapper.findByUserId(userId);
    }

    @Override
    public Budget findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month) {
        return budgetMapper.findByUserIdAndYearAndMonth(userId, year, month);
    }

    @Override
    public Boolean save(CreateBudgetRequest request) {
        boolean isInsert = true;
        Long id = request.getId();
        if (id != null) {
            Budget budget = budgetMapper.findById(id);
            isInsert = budget == null;
        }

        Budget budget = new Budget();
        budget.setTotalBalance(request.getTotalBalance());
        budget.setYear(request.getYear());
        budget.setMonth(request.getMonth());

        if (isInsert) {
            budget.setUserId(baseHandler.getUserId());
            budget.setStatus(0);
            budget.setCreateTime(LocalDateTime.now());
            return budgetMapper.insert(budget) > 0;
        } else {
            budget.setId(id);
            return budgetMapper.update(budget) > 0;
        }
    }

    @Override
    public void deleteById(Long id) {
        budgetMapper.deleteById(id);
    }

    @Override
    public BudgetResponse findResponseById(Long id) {
        Budget budget = budgetMapper.findById(id);
        return BudgetResponse.fromBudget(budget);
    }

    @Override
    public List<BudgetResponse> getUserBudgetList(Long userId) {
        List<Budget> budgets = budgetMapper.findByUserId(userId);
        return budgets.stream()
                .map(BudgetResponse::fromBudget)
                .collect(Collectors.toList());
    }

    @Override
    public BudgetResponse findResponseByUserIdAndYearAndMonth(Long userId, Integer year, Integer month) {
        Budget budget = budgetMapper.findByUserIdAndYearAndMonth(userId, year, month);
        return BudgetResponse.fromBudget(budget);
    }
    
    @Override
    public void updateTotalBalance(Long budgetId) {
        if (budgetId == null) {
            return;
        }
        
        // 查询预算
        Budget budget = budgetMapper.findById(budgetId);
        if (budget == null) {
            return;
        }
        
        // 重新计算所有正常状态预算明细的总金额
        Double sum = budgetItemMapper.sumByBudgetId(budgetId, null);
        sum = sum == null ? 0 : sum;
        
        // 将Double转换为Long（假设金额单位是分）
        Long totalBalance = Math.round(sum);
        
        // 直接覆盖预算总金额
        budget.setTotalBalance(totalBalance);
        
        // 保存更新
        budgetMapper.update(budget);
    }

    @Override
    public Budget findByYearMonthAndLedger(Integer year, Integer month, Long ledgerId) {
        return budgetMapper.findByYearMonthAndLedger(year, month, ledgerId);
    }

    @Override
    public BudgetResponse findResponseByYearMonthAndLedger(Integer year, Integer month, Long ledgerId) {
        Budget budget = budgetMapper.findByYearMonthAndLedger(year, month, ledgerId);
        return BudgetResponse.fromBudget(budget);
    }
    
    @Override
    public Boolean syncLastMonthBudget(Long userId, Integer targetYear, Integer targetMonth, Integer sourceYear, Integer sourceMonth) {
        // 查询源年月的预算
        Budget sourceMonthBudget = budgetMapper.findByUserIdAndYearAndMonth(userId, sourceYear, sourceMonth);
        if (sourceMonthBudget == null) {
            throw new RuntimeException(sourceYear + "年" + sourceMonth + "月暂无预算数据");
        }
        
        // 查询源年月的预算明细
        List<cn.lizongyi.shareaccount.entity.BudgetItem> sourceMonthItems = budgetItemMapper.findByBudgetId(sourceMonthBudget.getId());
        if (sourceMonthItems == null || sourceMonthItems.isEmpty()) {
            throw new RuntimeException(sourceYear + "年" + sourceMonth + "月暂无预算明细");
        }
        
        // 过滤出正常状态的预算明细（status = 0）
        List<cn.lizongyi.shareaccount.entity.BudgetItem> activeItems = sourceMonthItems.stream()
                .filter(item -> item.getStatus() == 0)
                .collect(Collectors.toList());
        
        if (activeItems.isEmpty()) {
            throw new RuntimeException(sourceYear + "年" + sourceMonth + "月没有活跃的预算明细");
        }
        
        // 检查目标年月是否已有预算
        Budget targetMonthBudget = budgetMapper.findByUserIdAndYearAndMonth(userId, targetYear, targetMonth);
        Long targetMonthBudgetId;
        
        if (targetMonthBudget == null) {
            // 创建目标年月的预算记录
            targetMonthBudget = new Budget();
            targetMonthBudget.setUserId(userId);
            targetMonthBudget.setYear(targetYear);
            targetMonthBudget.setMonth(targetMonth);
            targetMonthBudget.setTotalBalance(sourceMonthBudget.getTotalBalance());
            targetMonthBudget.setStatus(0);
            targetMonthBudget.setCreateTime(LocalDateTime.now());
            
            if (budgetMapper.insert(targetMonthBudget) <= 0) {
                throw new RuntimeException("创建目标年月预算失败");
            }
            targetMonthBudgetId = targetMonthBudget.getId();
        } else {
            targetMonthBudgetId = targetMonthBudget.getId();
        }
        
        // 将上个月的预算明细复制到当前月
        for (cn.lizongyi.shareaccount.entity.BudgetItem item : activeItems) {
            // 创建新的预算明细记录
            cn.lizongyi.shareaccount.entity.BudgetItem newItem = new cn.lizongyi.shareaccount.entity.BudgetItem();
            newItem.setUserId(userId);
            newItem.setBudgetId(targetMonthBudgetId);
            newItem.setLedgerId(item.getLedgerId());
            newItem.setClassId(item.getClassId());
            newItem.setTotalBalance(item.getTotalBalance());
            newItem.setStatus(0); // 保持正常状态
            newItem.setCreateTime(LocalDateTime.now());
            
            budgetItemMapper.insert(newItem);
        }
        
        return true;
    }
}