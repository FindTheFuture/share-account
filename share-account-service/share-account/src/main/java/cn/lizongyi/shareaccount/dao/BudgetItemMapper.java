package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.BudgetItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * BudgetItem 对应的 MyBatis Mapper 接口
 */
@Mapper
public interface BudgetItemMapper {

    /**
     * 查询所有预算明细信息
     * @return 预算明细信息列表
     */
    @Select("SELECT * FROM `budget_item`")
    List<BudgetItem> findAll();

    /**
     * 根据 ID 查询预算明细信息
     * @param id 预算明细 ID
     * @return 预算明细信息对象，如果未找到则返回 null
     */
    @Select("SELECT * FROM `budget_item` WHERE id = #{id}")
    BudgetItem findById(Long id);

    /**
     * 根据预算 ID 查询预算明细信息列表
     * @param budgetId 预算 ID
     * @return 预算明细信息列表
     */
    @Select("SELECT * FROM `budget_item` WHERE budget_id = #{budgetId}")
    List<BudgetItem> findByBudgetId(Long budgetId);

    /**
     * 查询指定预算ID下所有有效明细的总金额
     * @param budgetId 预算 ID
     * @param excludeId 排除的明细ID（用于更新操作）
     * @return 总金额
     */
    @Select("SELECT SUM(total_balance) FROM `budget_item` WHERE budget_id = #{budgetId} AND status = 0 AND (#{excludeId} IS NULL OR id != #{excludeId})")
    Double sumByBudgetId(@Param("budgetId") Long budgetId, @Param("excludeId") Long excludeId);

    /**
     * 根据用户 ID 查询预算明细信息列表
     * @param userId 用户 ID
     * @return 预算明细信息列表
     */
    @Select("SELECT * FROM `budget_item` WHERE user_id = #{userId}")
    List<BudgetItem> findByUserId(Long userId);

    /**
     * 插入预算明细信息
     * @param budgetItem 要插入的预算明细信息对象
     * @return 插入成功的记录数
     */
    @Insert("INSERT INTO `budget_item` (budget_id, user_id, ledger_id, class_id, total_balance, status, create_time) " +
            "VALUES (#{budgetId}, #{userId}, #{ledgerId}, #{classId}, #{totalBalance}, 0, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(BudgetItem budgetItem);

    /**
     * 根据 ID 更新预算明细信息
     * @param budgetItem 要更新的预算明细信息对象，需包含 ID
     * @return 更新成功的记录数
     */
    @Update("UPDATE `budget_item` SET budget_id = #{budgetId}, user_id = #{userId}, ledger_id = #{ledgerId}, class_id = #{classId}, total_balance = #{totalBalance}, status = #{status} WHERE id = #{id}")
    int update(BudgetItem budgetItem);

    /**
     * 根据 ID 删除预算明细信息（逻辑删除，更新状态为 1）
     * @param id 要删除的预算明细 ID
     * @return 删除成功的记录数
     */
    @Update("UPDATE `budget_item` SET status = 1 WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 根据预算 ID 删除预算明细信息（逻辑删除，更新状态为 1）
     * @param budgetId 预算 ID
     * @return 删除成功的记录数
     */
    @Update("UPDATE `budget_item` SET status = 1 WHERE budget_id = #{budgetId}")
    int deleteByBudgetId(Long budgetId);

    /**
     * 关联预算表按年月+账本ID查询明细列表（仅正常状态）
     * @param year 年份
     * @param month 月份
     * @param ledgerId 账本ID
     * @return 明细列表
     */
    @Select("SELECT bi.* FROM `budget_item` bi JOIN `budget` b ON bi.budget_id = b.id " +
            "WHERE b.year = #{year} AND b.month = #{month} AND bi.ledger_id = #{ledgerId} " +
            "AND b.status = 0 AND bi.status = 0")
    List<BudgetItem> findByYearMonthAndLedger(@Param("year") Integer year, @Param("month") Integer month, @Param("ledgerId") Long ledgerId);
}