package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.BudgetItemMapper;
import cn.lizongyi.shareaccount.dao.BudgetMapper;
import cn.lizongyi.shareaccount.dao.ClassEntityMapper;
import cn.lizongyi.shareaccount.dao.LedgerMapper;
import cn.lizongyi.shareaccount.entity.Budget;
import cn.lizongyi.shareaccount.entity.BudgetItem;
import cn.lizongyi.shareaccount.request.CreateBudgetItemRequest;
import cn.lizongyi.shareaccount.response.BudgetItemResponse;
import cn.lizongyi.shareaccount.response.ClassResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.BudgetItemService;
import cn.lizongyi.shareaccount.services.BudgetService;
import cn.lizongyi.shareaccount.services.ClassEntityService;
import cn.lizongyi.shareaccount.services.LedgerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BudgetItemServiceImpl implements BudgetItemService {

    @Autowired
    private BudgetItemMapper budgetItemMapper;

    @Autowired
    private BudgetMapper budgetMapper;

    @Autowired
    private LedgerMapper ledgerMapper;

    @Autowired
    private ClassEntityMapper classEntityMapper;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private BaseHandler baseHandler;

    @Autowired
    private ClassEntityService classEntityService;

    @Autowired
    private LedgerService ledgerService;

    @Override
    public List<BudgetItem> findAll() {
        return budgetItemMapper.findAll();
    }

    @Override
    public BudgetItem findById(Long id) {
        return budgetItemMapper.findById(id);
    }

    @Override
    public List<BudgetItem> findByBudgetId(Long budgetId) {
        return budgetItemMapper.findByBudgetId(budgetId);
    }

    @Override
    public List<BudgetItem> findByUserId(Long userId) {
        return budgetItemMapper.findByUserId(userId);
    }

    @Override
    public Boolean save(CreateBudgetItemRequest request) {
        boolean isInsert = true;
        Long id = request.getId();
        if (id != null) {
            BudgetItem budgetItem = budgetItemMapper.findById(id);
            isInsert = budgetItem == null;
        }

        // 校验：账本ID必填且有效
        if (request.getLedgerId() == null || request.getLedgerId() <= 0) {
            throw new RuntimeException("账本id不能为空");
        }
        // 校验：账本必须存在且属于当前用户
        cn.lizongyi.shareaccount.entity.Ledger ledger = ledgerMapper.findById(request.getLedgerId());
        if (ledger == null) {
            throw new RuntimeException("账本不存在");
        }
        if (!ledger.getUserId().equals(baseHandler.getUserId())) {
            throw new RuntimeException("无权使用该账本");
        }

        // 优化：直接使用年月来判断预算是否存在
        Long budgetId = null;
        
        // 首先根据年份和月份查找预算
        if (request.getYear() != null && request.getMonth() != null) {
            Budget budget = budgetService.findByUserIdAndYearAndMonth(
                    baseHandler.getUserId(), request.getYear(), request.getMonth());
            
            if (budget != null) {
                // 找到对应年月的预算，使用其ID
                budgetId = budget.getId();
            } else {
                // 没有找到对应年月的预算，创建一个新的预算
                cn.lizongyi.shareaccount.request.CreateBudgetRequest budgetRequest = new cn.lizongyi.shareaccount.request.CreateBudgetRequest();
                budgetRequest.setYear(request.getYear());
                budgetRequest.setMonth(request.getMonth());
                budgetRequest.setTotalBalance(0L); // 设置总金额为0
                budgetService.save(budgetRequest);
                
                // 重新查询创建的预算ID
                budget = budgetService.findByUserIdAndYearAndMonth(
                        baseHandler.getUserId(), request.getYear(), request.getMonth());
                if (budget != null) {
                    budgetId = budget.getId();
                }
            }
        }
        
        // 如果通过年月没有找到预算，且请求中有budgetId，尝试使用传入的budgetId
        if (budgetId == null && request.getBudgetId() != null) {
            Budget budget = budgetService.findById(request.getBudgetId());
            if (budget != null) {
                budgetId = budget.getId();
            }
        }
        
        // 校验：确保budgetId不为空
        if (budgetId == null) {
            throw new RuntimeException("请先选择或创建预算");
        }

        BudgetItem budgetItem = new BudgetItem();
        budgetItem.setBudgetId(budgetId);
        budgetItem.setLedgerId(request.getLedgerId());
        budgetItem.setClassId(request.getClassId());
        budgetItem.setTotalBalance(request.getTotalBalance());
        budgetItem.setUserId(baseHandler.getUserId());

        boolean success = false;
        if (isInsert) {
            budgetItem.setStatus(0);
            budgetItem.setCreateTime(LocalDateTime.now());
            success = budgetItemMapper.insert(budgetItem) > 0;
        } else {
            budgetItem.setId(id);
            success = budgetItemMapper.update(budgetItem) > 0;
        }

        // 同步更新预算表总金额
        if (success) {
            budgetService.updateTotalBalance(budgetId);
        }

        return success;
    }

    @Override
    public BudgetItemResponse findResponseById(Long id) {
        BudgetItem budgetItem = budgetItemMapper.findById(id);
        return convertToResponse(budgetItem);
    }

    /**
     * 将BudgetItem转换为BudgetItemResponse，包含分类信息
     * @param budgetItem 预算明细实体
     * @return 预算明细响应对象
     */
    private BudgetItemResponse convertToResponse(BudgetItem budgetItem) {
        if (budgetItem == null) {
            return null;
        }

        BudgetItemResponse response = new BudgetItemResponse();
        response.setId(budgetItem.getId());
        response.setBudgetId(budgetItem.getBudgetId());
        response.setUserId(budgetItem.getUserId());
        response.setLedgerId(budgetItem.getLedgerId());
        response.setClassId(budgetItem.getClassId());
        response.setTotalBalance(budgetItem.getTotalBalance());
        response.setStatus(budgetItem.getStatus());
        response.setStatusName(cn.lizongyi.shareaccount.enums.BudgetStatusEnum.fromId(budgetItem.getStatus()).getName());
        response.setCreateTime(cn.lizongyi.shareaccount.util.DateUtil.localDateTimeToString(budgetItem.getCreateTime()));

        // 根据classId查询分类名称和图标
        if (budgetItem.getClassId() != null && classEntityService != null) {
            ClassResponse classResponse = classEntityService.selectById(budgetItem.getClassId());
            if (classResponse != null) {
                response.setClassName(classResponse.getName());
                response.setIcon(classResponse.getIcon());
            } else {
                response.setClassName("未知分类");
            }
        } else {
            response.setClassName("未分类");
        }

        // 查询账本名称
        if (budgetItem.getLedgerId() != null && ledgerService != null) {
            cn.lizongyi.shareaccount.response.LedgerResponse ledgerResponse = ledgerService.findResponseById(budgetItem.getLedgerId());
            if (ledgerResponse != null) {
                response.setLedgerName(ledgerResponse.getName());
            }
        }

        return response;
    }

    @Override
    public List<BudgetItemResponse> getBudgetItemListByBudgetId(Long budgetId) {
        List<BudgetItem> list = budgetItemMapper.findByBudgetId(budgetId);
        List<BudgetItemResponse> result = new ArrayList<>();
        for (BudgetItem item : list) {
            BudgetItemResponse response = convertToResponse(item);
            if (response != null) {
                result.add(response);
            }
        }
        return result;
    }

    @Override
    public List<BudgetItemResponse> getBudgetItemListByYearMonthAndLedger(Integer year, Integer month, Long ledgerId) {
        List<BudgetItem> list = budgetItemMapper.findByYearMonthAndLedger(year, month, ledgerId);
        List<BudgetItemResponse> result = new ArrayList<>();
        for (BudgetItem item : list) {
            BudgetItemResponse response = convertToResponse(item);
            if (response != null) {
                result.add(response);
            }
        }
        return result;
    }

    @Override
    public Double checkBudgetLimit(Long budgetId, Long classId, Double amount, Long id) {
        Budget budget = budgetService.findById(budgetId);
        if (budget == null) {
            return null;
        }
        Double sum = budgetItemMapper.sumByBudgetId(budgetId, id);
        sum = sum == null ? 0 : sum;
        Double total = sum + amount;
        if (budget.getTotalBalance() >= 0 && total > budget.getTotalBalance()) {
            return total - budget.getTotalBalance();
        }
        return null;
    }

    @Override
    public Boolean updateStatus(Long id, Integer status) {
        BudgetItem budgetItem = budgetItemMapper.findById(id);
        if (budgetItem == null) {
            return false;
        }
        Integer oldStatus = budgetItem.getStatus();
        budgetItem.setStatus(status);
        boolean success = budgetItemMapper.update(budgetItem) > 0;
        if (success && oldStatus != status) {
            budgetService.updateTotalBalance(budgetItem.getBudgetId());
        }
        return success;
    }

    @Override
    public void deleteById(Long id) {
        budgetItemMapper.deleteById(id);
    }

    @Override
    public void deleteByBudgetId(Long budgetId) {
        budgetItemMapper.deleteByBudgetId(budgetId);
    }
}