package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.*;
import cn.lizongyi.shareaccount.entity.*;
import cn.lizongyi.shareaccount.request.CreateBillRequest;
import cn.lizongyi.shareaccount.request.CreateMessageRequest;
import cn.lizongyi.shareaccount.request.CreateScheduledBillRequest;
import cn.lizongyi.shareaccount.request.QueryScheduledBillListRequest;
import cn.lizongyi.shareaccount.response.ScheduledBillResponse;
import cn.lizongyi.shareaccount.response.ScheduledBillLogResponse;
import cn.lizongyi.shareaccount.response.BillResponse;
import cn.lizongyi.shareaccount.services.ScheduledBillService;
import cn.lizongyi.shareaccount.util.PriceFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.lizongyi.shareaccount.services.BillService;
import cn.lizongyi.shareaccount.services.MessageService;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.ClassEntityService;
import cn.lizongyi.shareaccount.services.LedgerService;
import cn.lizongyi.shareaccount.services.AccountService;
import cn.lizongyi.shareaccount.response.ClassResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 定时记账配置服务实现类
 */
@Slf4j
@Service
public class ScheduledBillServiceImpl implements ScheduledBillService {

    @Autowired
    private ScheduledBillMapper scheduledBillMapper;

    @Autowired
    private ScheduledBillLogMapper scheduledBillLogMapper;

    @Autowired
    private BillService billService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private BaseHandler baseHandler;

    @Autowired
    private ClassEntityService classEntityService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AccountService accountService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public Boolean save(CreateScheduledBillRequest request) {
        try {
            Boolean success = false;
            LocalDateTime now = LocalDateTime.now();
            ScheduledBill scheduledBill = new ScheduledBill();
            if (request.getId() != null && request.getId() > 0) {
                scheduledBill = scheduledBillMapper.findById(request.getId());
                if (scheduledBill == null) {
                    log.error("定时记账配置不存在，id: {}", request.getId());
                    return false;
                }
            }

            scheduledBill.setName(request.getName());
            scheduledBill.setPrice(request.getPrice());
            scheduledBill.setClassId(request.getClassId());
            scheduledBill.setLedgerId(request.getLedgerId());
            scheduledBill.setAccountId(request.getAccountId());
            scheduledBill.setCycleType(request.getCycleType());
            scheduledBill.setCycleValue(request.getCycleValue());
            // 解析执行时间
            LocalDateTime executeTime = LocalDateTime.parse(request.getExecuteTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            // 解析开始日期和结束日期（仅包含日期部分）
            LocalDate startDate = LocalDate.parse(request.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate endDate = LocalDate.parse(request.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            // 校验执行时间是否在有效范围内
            if (executeTime.toLocalDate().isBefore(startDate)) {
                // 如果执行时间早于开始日期，自动调整为开始日期的相同时间
                executeTime = LocalDateTime.of(startDate, executeTime.toLocalTime());
            }
            if (executeTime.toLocalDate().isAfter(endDate)) {
                // 如果执行时间晚于结束日期，返回错误
                log.error("执行时间晚于结束日期，执行时间: {}, 结束日期: {}", executeTime, endDate);
                return false;
            }
            
            scheduledBill.setExecuteTime(executeTime);
            scheduledBill.setStartDate(startDate);
            scheduledBill.setEndDate(endDate);
            scheduledBill.setIsRemind(request.getIsRemind());
            scheduledBill.setDescription(request.getDescription());
            
            if (request.getId() == null || request.getId() <= 0) {
                // 新增
                scheduledBill.setUserId(baseHandler.getUserId());
                scheduledBill.setStatus(1);
                scheduledBill.setCreatedAt(now);
                scheduledBill.setUpdatedAt(now);
                success = scheduledBillMapper.insert(scheduledBill) > 0;
            } else {
                // 更新
                scheduledBill.setStatus(request.getStatus());
                scheduledBill.setUpdatedAt(now);
                success = scheduledBillMapper.update(scheduledBill) > 0;
            }
            return success;
        } catch (Exception e) {
            log.error("保存定时记账配置失败", e);
            return false;
        }
    }

    @Override
    public ScheduledBillResponse.ScheduledBillResponseItem findById(Long id) {
        ScheduledBill scheduledBill = scheduledBillMapper.findById(id);
        if (scheduledBill == null) {
            return null;
        }
        ScheduledBillResponse.ScheduledBillResponseItem item = new ScheduledBillResponse.ScheduledBillResponseItem();
        mapToResponseItem(scheduledBill, item);
        return item;
    }

    @Override
    public ScheduledBillResponse findByUserId(QueryScheduledBillListRequest request) {
        // 获取当前用户ID
        Long userId = baseHandler.getUserId();
        
        // 使用 PageHelper 实现分页查询
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        List<ScheduledBill> scheduledBills = scheduledBillMapper.findByUserIdWithPagination(userId, request.getLedgerId(), request.getStatus(), request.getCycleType());
        PageInfo<ScheduledBill> pageInfo = new PageInfo<>(scheduledBills);
        
        ScheduledBillResponse response = new ScheduledBillResponse();
        response.setPageNum(pageInfo.getPageNum());
        response.setPageSize(pageInfo.getPageSize());
        response.setTotal(pageInfo.getTotal());
        response.setTotalPages(pageInfo.getPages());

        // 构建响应项
        for (ScheduledBill bill : scheduledBills) {
            ScheduledBillResponse.ScheduledBillResponseItem item = new ScheduledBillResponse.ScheduledBillResponseItem();
            mapToResponseItem(bill, item);
            response.getItemList().add(item);
        }
        return response;
    }

    @Override
    @Transactional
    public int delete(Long id) {
        return scheduledBillMapper.deleteById(id, LocalDateTime.now());
    }

    @Override
    @Transactional
    public int updateStatus(Long id, Integer status) {
        return scheduledBillMapper.updateStatus(id, status, LocalDateTime.now());
    }

    @Override
    public List<ScheduledBill> findAllEnabled() {
        return scheduledBillMapper.findAllEnabled();
    }

    @Override
    @Transactional
    public ScheduledBillLog executeScheduledBill(ScheduledBill scheduledBill) {
        LocalDateTime now = LocalDateTime.now();
        
        ScheduledBillLog billLog = new ScheduledBillLog();
        billLog.setScheduledId(scheduledBill.getId());
        billLog.setExecuteTime(now);
        billLog.setCreatedAt(now);

        try {
            // 生成账单
            Long billId = generateBill(scheduledBill, now);

            if (billId != null) {
                // 成功
                billLog.setStatus(1); // 成功
                billLog.setBillId(billId);

                String message = "定时记账执行成功：" + scheduledBill.getName();
                if(scheduledBill.getIsRemind() == null || scheduledBill.getIsRemind() == 0) {
                    message += "，未开启提醒功能，未发送通知, 建议打开消息提醒功能";
                }

                billLog.setErrorMsg(message);
                log.info("定时记账执行成功，配置ID: {}, 生成账单ID: {}", scheduledBill.getId(), billId);
            } else {
                throw new RuntimeException("定时记账生成账单失败，账单ID为空");
            }
        } catch (Exception e) {
            billLog.setStatus(2); // 失败
            billLog.setErrorMsg("定时记账执行失败:" + scheduledBill.getName());
            log.error("定时记账执行失败，配置ID: {}", scheduledBill.getId(), e);
        } finally {
            // 保存执行日志
            scheduledBillLogMapper.insert(billLog);
            // 处理提醒
            handleReminder(scheduledBill, billLog);
        }

        return billLog;
    }

    @Override
    public ScheduledBillLogResponse.ScheduledBillLogResponseItem findLogById(Long id) {
        ScheduledBillLog log = scheduledBillLogMapper.findById(id);
        if (log == null) {
            return null;
        }
        ScheduledBillLogResponse.ScheduledBillLogResponseItem item = new ScheduledBillLogResponse.ScheduledBillLogResponseItem();
        mapToLogResponseItem(log, item);
        return item;
    }

    @Override
    public ScheduledBillLogResponse findLogsResponseByScheduledId(Long scheduledId, Integer pageNum, Integer pageSize) {
        // 使用 PageHelper 实现分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<ScheduledBillLog> logs = scheduledBillLogMapper.findByScheduledId(scheduledId);
        PageInfo<ScheduledBillLog> pageInfo = new PageInfo<>(logs);
        
        ScheduledBillLogResponse response = new ScheduledBillLogResponse();
        response.setPageNum(pageInfo.getPageNum());
        response.setPageSize(pageInfo.getPageSize());
        response.setTotal(pageInfo.getTotal());
        response.setTotalPages(pageInfo.getPages());

        // 构建响应项
        for (ScheduledBillLog log : logs) {
            ScheduledBillLogResponse.ScheduledBillLogResponseItem item = new ScheduledBillLogResponse.ScheduledBillLogResponseItem();
            mapToLogResponseItem(log, item);
            response.getItemList().add(item);
        }
        return response;
    }


    @Override
    public List<ScheduledBillLog> findLogsByScheduledId(Long scheduledId, Integer pageNum, Integer pageSize) {
        // 使用 PageHelper 实现分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<ScheduledBillLog> logs = scheduledBillLogMapper.findByScheduledId(scheduledId);
        return logs;
    }

    /**
     * 生成账单
     * @param scheduledBill 定时记账配置
     * @param executeTime 执行时间
     * @return 生成的账单ID，保存失败时返回null
     */
    private Long generateBill(ScheduledBill scheduledBill, LocalDateTime executeTime) {

        CreateBillRequest request = new CreateBillRequest();
        request.setLedgerId(scheduledBill.getLedgerId());
        request.setAccountId(scheduledBill.getAccountId());
        request.setClassId(scheduledBill.getClassId());
        request.setIsBudget(0); // 默认计入预算
        request.setBillTime(executeTime);
        request.setPrice(scheduledBill.getPrice());
        request.setStatus(0); // 正常状态
        request.setMemo("定时记账自动生成: " + scheduledBill.getName());

        Boolean result = billService.save(request);
        return result ? request.getId() : null;
    }

    /**
     * 处理提醒
     * @param scheduledBill 定时记账配置
     * @param log 执行日志
     */
    private void handleReminder(ScheduledBill scheduledBill, ScheduledBillLog billLog) {
        // 检查是否开启提醒
        if (scheduledBill.getIsRemind() != null && scheduledBill.getIsRemind() == 1) {
            try {
                // 构建消息内容
                String status = billLog.getStatus() == 1 ? "成功" : "失败";
                String content = buildMessageContent(scheduledBill, billLog, status);

                CreateMessageRequest request = new CreateMessageRequest();
                request.setTitle("定时记账执行通知");
                request.setContent(content);
                request.setType(2); // 业务消息
                request.setPriority(1); // 重要
                request.setTarget("specific");
                request.setUserIds(java.util.Collections.singletonList(scheduledBill.getUserId()));

                messageService.saveMessage(request, scheduledBill.getUserId());

                log.info("定时记账提醒已发送，用户ID: {}, 配置ID: {}", scheduledBill.getUserId(), scheduledBill.getId());
            } catch (Exception e) {
                log.error("定时记账提醒发送失败，配置ID: {}", scheduledBill.getId(), e);
            }
        } else {
            log.info("定时记账提醒未开启,定时记账完成后未能发送通知，配置ID: {}", scheduledBill.getId());
        }
    }

    /**
     * 构建消息内容
     * @param scheduledBill 定时记账配置
     * @param log 执行日志
     * @param status 执行状态
     * @return 消息内容
     */
    private String buildMessageContent(ScheduledBill scheduledBill, ScheduledBillLog billLog, String status) {
        StringBuilder content = new StringBuilder();
        content.append(scheduledBill.getName() + " # 定时记账执行通知\n\n");
        content.append("## 执行结果：").append(status).append("\n");
        content.append("- 执行时间：").append(billLog.getExecuteTime()).append("\n");
        content.append("- 金额：").append(PriceFormatUtil.formatPriceLong(scheduledBill.getPrice())).append("元\n");
        content.append(billLog.getErrorMsg()).append("\n");
        return content.toString();
    }

    /**
     * 将ScheduledBill映射到响应项
     * @param bill 定时记账配置
     * @param item 响应项
     */
    private void mapToResponseItem(ScheduledBill bill, ScheduledBillResponse.ScheduledBillResponseItem item) {
        item.setId(bill.getId());
        item.setName(bill.getName());
        item.setPrice(PriceFormatUtil.formatPriceLong(bill.getPrice()));
        item.setClassId(bill.getClassId());
        item.setLedgerId(bill.getLedgerId());
        item.setAccountId(bill.getAccountId());
        item.setCycleType(bill.getCycleType());
        item.setCycleValue(bill.getCycleValue());
        item.setExecuteTime(bill.getExecuteTime() != null ? bill.getExecuteTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        item.setStartDate(bill.getStartDate() != null ? bill.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null);
        item.setEndDate(bill.getEndDate() != null ? bill.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null);
        item.setStatus(bill.getStatus());
        item.setIsRemind(bill.getIsRemind());
        item.setDescription(bill.getDescription());
        item.setCreatedAt(bill.getCreatedAt().format(DATE_TIME_FORMATTER));

        if(bill.getClassId() != null) {
            ClassResponse classResponse = classEntityService.selectById(bill.getClassId());
            if(classResponse != null) {
                item.setClassName(classResponse.getName());
            }
        }

        if(bill.getLedgerId() != null) {
            Ledger ledgerResponse = ledgerService.findById(bill.getLedgerId());
            if(ledgerResponse != null) {
                item.setLedgerName(ledgerResponse.getName());
            }
        }

        if(bill.getAccountId() != null) {
            Account accountResponse = accountService.findById(bill.getAccountId());
            if(accountResponse != null) {
                item.setAccountName(accountResponse.getName());
            }
        }
    }


    /**
     * 将ScheduledBillLog映射到响应项
     * @param billLog 定时记账执行日志
     * @param item 响应项
     */
    private void mapToLogResponseItem(ScheduledBillLog billLog, ScheduledBillLogResponse.ScheduledBillLogResponseItem item) {
        item.setId(billLog.getId());
        item.setScheduledId(billLog.getScheduledId());
        item.setExecuteTime(billLog.getExecuteTime().format(DATE_TIME_FORMATTER));
        item.setStatus(billLog.getStatus());
        item.setBillId(billLog.getBillId());
        item.setErrorMsg(billLog.getErrorMsg());
        item.setCreatedAt(billLog.getCreatedAt().format(DATE_TIME_FORMATTER));
        
        // 获取账单数据
        if (billLog.getBillId() != null) {
            try {
                BillResponse billResponse = billService.findBillResponseById(billLog.getBillId());
                item.setBill(billResponse);
            } catch (Exception e) {
                log.error("获取账单数据失败，账单ID: {}", billLog.getBillId(), e);
            }
        }
    }
}
