package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.ScheduledBillLog;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时记账执行日志表对应的 MyBatis Mapper 接口
 */
@Mapper
public interface ScheduledBillLogMapper {

    /**
     * 查询所有定时记账执行日志
     * @return 定时记账执行日志列表
     */
    @Select("SELECT * FROM `scheduled_bill_log`")
    List<ScheduledBillLog> findAll();

    /**
     * 根据 ID 查询定时记账执行日志
     * @param id 定时记账执行日志 ID
     * @return 定时记账执行日志对象，如果未找到则返回 null
     */
    @Select("SELECT * FROM `scheduled_bill_log` WHERE id = #{id}")
    ScheduledBillLog findById(Long id);

    /**
     * 根据定时记账配置 ID 查询执行日志列表
     * @param scheduledId 定时记账配置 ID
     * @return 定时记账执行日志列表
     */
    @Select("SELECT * FROM `scheduled_bill_log` WHERE scheduled_id = #{scheduledId} ORDER BY execute_time DESC")
    List<ScheduledBillLog> findByScheduledId(Long scheduledId);

    /**
     * 插入定时记账执行日志
     * @param log 要插入的定时记账执行日志对象
     * @return 插入成功的记录数
     */
    @Insert("INSERT INTO `scheduled_bill_log` (scheduled_id, execute_time, status, bill_id, error_msg, created_at) " +
            "VALUES (#{scheduledId}, #{executeTime}, #{status}, #{billId}, #{errorMsg}, #{createdAt}) ")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(ScheduledBillLog log);

    /**
     * 根据定时记账配置 ID 和执行时间范围查询执行日志
     * @param scheduledId 定时记账配置 ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 定时记账执行日志列表
     */
    @Select("SELECT * FROM `scheduled_bill_log` WHERE scheduled_id = #{scheduledId} AND execute_time BETWEEN #{startTime} AND #{endTime} ORDER BY execute_time DESC")
    List<ScheduledBillLog> findByScheduledIdAndTimeRange(
            @Param("scheduledId") Long scheduledId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据定时记账配置 ID 分页查询执行日志
     * @param scheduledId 定时记账配置 ID
     * @return 定时记账执行日志列表
     */
    @Select("SELECT * FROM `scheduled_bill_log` WHERE scheduled_id = #{scheduledId} ORDER BY execute_time DESC")
    List<ScheduledBillLog> findByScheduledIdWithPagination(Long scheduledId);
}
