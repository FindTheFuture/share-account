package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.ScheduledBill;
import cn.lizongyi.shareaccount.entity.ScheduledBillLog;
import cn.lizongyi.shareaccount.request.CreateScheduledBillRequest;
import cn.lizongyi.shareaccount.request.QueryScheduledBillListRequest;
import cn.lizongyi.shareaccount.response.ScheduledBillResponse;
import cn.lizongyi.shareaccount.response.ScheduledBillLogResponse;
import java.util.List;

/**
 * 定时记账配置服务接口
 */
public interface ScheduledBillService {

    /**
     * 保存定时记账配置（新增或更新）
     * @param request 定时记账配置请求参数
     * @return 保存是否成功
     */
    Boolean save(CreateScheduledBillRequest request);

    /**
     * 根据ID查询定时记账配置
     * @param id 定时记账配置ID
     * @return 定时记账配置响应项
     */
    ScheduledBillResponse.ScheduledBillResponseItem findById(Long id);

    /**
     * 分页查询定时记账配置列表
     * @param request 查询请求参数
     * @param userId 用户ID
     * @return 定时记账配置列表响应
     */
    ScheduledBillResponse findByUserId(QueryScheduledBillListRequest request);

    /**
     * 删除定时记账配置（逻辑删除）
     * @param id 定时记账配置ID
     * @return 删除成功的记录数
     */
    int delete(Long id);

    /**
     * 更新定时记账配置状态
     * @param id 定时记账配置ID
     * @param status 状态值
     * @return 更新成功的记录数
     */
    int updateStatus(Long id, Integer status);

    /**
     * 查询所有启用状态的定时记账配置
     * @return 定时记账配置列表
     */
    java.util.List<ScheduledBill> findAllEnabled();

    /**
     * 执行定时记账任务
     * @param scheduledBill 定时记账配置对象
     * @return 执行结果日志
     */
    ScheduledBillLog executeScheduledBill(ScheduledBill scheduledBill);

    /**
     * 根据ID查询定时记账执行日志
     * @param id 定时记账执行日志ID
     * @return 定时记账执行日志响应项
     */
    ScheduledBillLogResponse.ScheduledBillLogResponseItem findLogById(Long id);

    /**
     * 分页查询定时记账执行日志
     * @param scheduledId 定时记账配置ID
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @return 定时记账执行日志列表响应
     */
    ScheduledBillLogResponse findLogsResponseByScheduledId(Long scheduledId, Integer pageNum, Integer pageSize);

    /**
     * 分页查询定时记账执行日志
     * @param scheduledId 定时记账配置ID
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @return 定时记账执行日志列表响应
     */
    List<ScheduledBillLog> findLogsByScheduledId(Long scheduledId, Integer pageNum, Integer pageSize);
}

