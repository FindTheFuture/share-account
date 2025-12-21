package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.Budget;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Budget 对应的 MyBatis Mapper 接口
 */
@Mapper
public interface BudgetMapper {

    /**
     * 查询所有预算信息
     * @return 预算信息列表
     */
    @Select("SELECT * FROM `budget` WHERE status = 0")
    List<Budget> findAll();

    /**
     * 根据 ID 查询预算信息
     * @param id 预算 ID
     * @return 预算信息对象，如果未找到则返回 null
     */
    @Select("SELECT * FROM `budget` WHERE id = #{id} AND status = 0")
    Budget findById(Long id);

    /**
     * 根据用户 ID 查询预算信息列表
     * @param userId 用户 ID
     * @return 预算信息列表
     */
    @Select("SELECT * FROM `budget` WHERE user_id = #{userId} AND status = 0")
    List<Budget> findByUserId(Long userId);

    /**
     * 根据用户 ID、年份和月份查询预算
     * @param userId 用户 ID
     * @param year 年份
     * @param month 月份
     * @return 预算信息对象，如果未找到则返回 null
     */
    @Select("SELECT * FROM `budget` WHERE user_id = #{userId} AND year = #{year} AND month = #{month} AND status = 0")
    Budget findByUserIdAndYearAndMonth(@Param("userId") Long userId, @Param("year") Integer year, @Param("month") Integer month);

    /**
     * 插入预算信息
     * @param budget 要插入的预算信息对象
     * @return 插入成功的记录数
     */
    @Insert("INSERT INTO `budget` (user_id, total_balance, year, month, status, create_time) " +
            "VALUES (#{userId}, #{totalBalance}, #{year}, #{month}, 0, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Budget budget);

    /**
     * 根据 ID 更新预算信息
     * @param budget 要更新的预算信息对象，需包含 ID
     * @return 更新成功的记录数
     */
    @Update("UPDATE `budget` SET total_balance = #{totalBalance}, year = #{year}, month = #{month} WHERE id = #{id} AND status = 0")
    int update(Budget budget);

    /**
     * 根据 ID 删除预算信息（逻辑删除，更新状态为 1）
     * @param id 要删除的预算 ID
     * @return 删除成功的记录数
     */
    @Update("UPDATE `budget` SET status = 1 WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT b.* FROM `budget` b JOIN `budget_item` bi ON b.id = bi.budget_id \n            WHERE b.year = #{year} AND b.month = #{month} \n              AND bi.ledger_id = #{ledgerId} \n              AND b.status = 0 AND bi.status = 0 \n            LIMIT 1")
    Budget findByYearMonthAndLedger(@Param("year") Integer year, @Param("month") Integer month, @Param("ledgerId") Long ledgerId);
}