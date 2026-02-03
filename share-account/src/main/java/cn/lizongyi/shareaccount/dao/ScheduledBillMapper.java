package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.ScheduledBill;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时记账配置表对应的 MyBatis Mapper 接口
 */
@Mapper
public interface ScheduledBillMapper {

    /**
     * 查询所有定时记账配置
     * @return 定时记账配置列表
     */
    @Select("SELECT * FROM `scheduled_bill`")
    List<ScheduledBill> findAll();

    /**
     * 根据 ID 查询定时记账配置
     * @param id 定时记账配置 ID
     * @return 定时记账配置对象，如果未找到则返回 null
     */
    @Select("SELECT * FROM `scheduled_bill` WHERE id = #{id}")
    ScheduledBill findById(Long id);

    /**
     * 根据用户 ID 查询定时记账配置列表
     * @param userId 用户 ID
     * @return 定时记账配置列表
     */
    @Select("SELECT * FROM `scheduled_bill` WHERE user_id = #{userId} AND status != 3 ORDER BY created_at DESC")
    List<ScheduledBill> findByUserId(Long userId);

    /**
     * 根据用户 ID 和状态查询定时记账配置列表
     * @param userId 用户 ID
     * @param status 状态
     * @return 定时记账配置列表
     */
    @Select("SELECT * FROM `scheduled_bill` WHERE user_id = #{userId} AND status = #{status} ORDER BY created_at DESC")
    List<ScheduledBill> findByUserIdAndStatus(Long userId, Integer status);

    /**
     * 查询所有启用状态的定时记账配置
     * @return 定时记账配置列表
     */
    @Select("SELECT * FROM `scheduled_bill` WHERE status = 1")
    List<ScheduledBill> findAllEnabled();

    /**
     * 插入定时记账配置
     * @param scheduledBill 要插入的定时记账配置对象
     * @return 插入成功的记录数
     */
    @Insert("INSERT INTO `scheduled_bill` (user_id, ledger_id, name, price, class_id, account_id, cycle_type, cycle_value, execute_time, start_date, end_date, status, is_remind, description, created_at, updated_at) " +
            "VALUES (#{userId}, #{ledgerId}, #{name}, #{price}, #{classId}, #{accountId}, #{cycleType}, #{cycleValue}, #{executeTime}, #{startDate}, #{endDate}, #{status}, #{isRemind}, #{description}, #{createdAt}, #{updatedAt}) ")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(ScheduledBill scheduledBill);

    /**
     * 根据 ID 更新定时记账配置
     * @param scheduledBill 要更新的定时记账配置对象，需包含 ID
     * @return 更新成功的记录数
     */
    @Update("UPDATE `scheduled_bill` SET user_id = #{userId}, ledger_id = #{ledgerId}, name = #{name}, price = #{price}, " +
            "class_id = #{classId}, account_id = #{accountId}, cycle_type = #{cycleType}, cycle_value = #{cycleValue}, " +
            "execute_time = #{executeTime}, start_date = #{startDate}, end_date = #{endDate}, status = #{status}, " +
            "is_remind = #{isRemind}, description = #{description}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(ScheduledBill scheduledBill);

    /**
     * 根据 ID 更新定时记账配置状态
     * @param id 定时记账配置 ID
     * @param status 状态值
     * @return 更新成功的记录数
     */
    @Update("UPDATE `scheduled_bill` SET status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 根据 ID 删除定时记账配置（逻辑删除，设置状态为3）
     * @param id 定时记账配置 ID
     * @param updatedAt 更新时间
     * @return 删除成功的记录数
     */
    @Update("UPDATE `scheduled_bill` SET status = 3, updated_at = #{updatedAt} WHERE id = #{id}")
    int deleteById(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 彻底删除定时记账配置（物理删除）
     * @param id 定时记账配置 ID
     * @return 删除成功的记录数
     */
    @Delete("DELETE FROM `scheduled_bill` WHERE id = #{id}")
    int deleteCompletelyById(Long id);

    /**
     * 根据用户 ID 分页查询定时记账配置列表
     * @param userId 用户 ID
     * @param ledgerId 账本 ID（可选）
     * @param status 状态（可选）
     * @param cycleType 周期类型（可选）
     * @return 定时记账配置列表
     */
    @Select({"<script>",
            "SELECT * FROM `scheduled_bill` WHERE user_id = #{userId} AND status != 3",
            "<if test='ledgerId != null'>AND ledger_id = #{ledgerId}</if>",
            "<if test='status != null'>AND status = #{status}</if>",
            "<if test='cycleType != null'>AND cycle_type = #{cycleType}</if>",
            "ORDER BY created_at DESC",
            "</script>"})
    List<ScheduledBill> findByUserIdWithPagination(
            @Param("userId") Long userId,
            @Param("ledgerId") Long ledgerId,
            @Param("status") Integer status,
            @Param("cycleType") Integer cycleType);

    /**
     * 根据用户 ID 统计定时记账配置数量
     * @param userId 用户 ID
     * @param ledgerId 账本 ID（可选）
     * @param status 状态（可选）
     * @param cycleType 周期类型（可选）
     * @return 定时记账配置数量
     */
    @Select({"<script>",
            "SELECT COUNT(*) FROM `scheduled_bill` WHERE user_id = #{userId} AND status != 3",
            "<if test='ledgerId != null'>AND ledger_id = #{ledgerId}</if>",
            "<if test='status != null'>AND status = #{status}</if>",
            "<if test='cycleType != null'>AND cycle_type = #{cycleType}</if>",
            "</script>"})
    long countByUserId(
            @Param("userId") Long userId,
            @Param("ledgerId") Long ledgerId,
            @Param("status") Integer status,
            @Param("cycleType") Integer cycleType);
}
