package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.Bill;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Bill 对应的 MyBatis Mapper 接口
 */
@Mapper
public interface BillMapper {

    /**
     * 查询所有账单信息
     * @return 账单信息列表
     */
    @Select("SELECT * FROM `bill`")
    List<Bill> findAll();

    /**
     * 根据 ID 查询账单信息
     * @param id 账单 ID
     * @return 账单信息对象，如果未找到则返回 null
     */
    @Select("SELECT * FROM `bill` WHERE id = #{id}")
    Bill findById(Long id);

    /**
     * 根据用户 ID 查询账单信息列表
     * @param userId 用户 ID
     * @return 账单信息列表
     */
    @Select("SELECT * FROM `bill` WHERE user_id = #{userId}")
    List<Bill> findByUserId(Long userId);

    /**
     * 根据账本 ID 查询账单信息列表
     * @param ledgerId 账本 ID
     * @return 账单信息列表
     */
    @Select("SELECT * FROM `bill` WHERE ledger_id = #{ledgerId}")
    List<Bill> findByLedgerId(Long ledgerId);

    /**
     * 插入账单信息
     * @param bill 要插入的账单信息对象
     * @return 插入成功的记录数
     */
    @Insert("INSERT INTO `bill` (user_id, ledger_id, account_id, class_id, top_class_id, is_budget, bill_time, price, status, memo, create_time) " +
            "VALUES (#{userId}, #{ledgerId}, #{accountId}, #{classId}, #{topClassId}, #{isBudget}, #{billTime}, #{price}, #{status}, #{memo}, #{createTime}) ")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Bill bill);

    /**
     * 根据 ID 更新账单信息
     * @param bill 要更新的账单信息对象，需包含 ID
     * @return 更新成功的记录数
     */
    @Update("UPDATE `bill` SET user_id = #{userId}, ledger_id = #{ledgerId}, account_id = #{accountId}, " +
            "class_id = #{classId}, top_class_id = #{topClassId}, is_budget = #{isBudget}, bill_time = #{billTime}, price = #{price}, " +
            "status = #{status}, memo = #{memo} WHERE id = #{id}")
    int update(Bill bill);

    /**
     * 根据 ID 删除账单（逻辑删除）
     * @param id 账单 ID
     * @return 删除成功的记录数
     */
    @Update("UPDATE `bill` SET status = 1 WHERE id = #{id}")
    int deleteById(Long id);


    /**
     * 彻底删除账单（物理删除）
     * @param id 账单 ID
     * @return 删除成功的记录数
     */
    @Delete("DELETE FROM `bill` WHERE id = #{id}")
    int deleteCompletelyById(Long id);
    
    /**
     * 根据账本ID和时间范围查询账单信息列表
     * @param ledgerId 账本 ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 账单信息列表
     */
    @Select("SELECT * FROM `bill` WHERE ledger_id = #{ledgerId} AND bill_time BETWEEN #{startTime} AND #{endTime} ORDER BY bill_time DESC")
    List<Bill> findByLedgerIdAndTimeRange(@Param("ledgerId") Long ledgerId, 
                                         @Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据用户ID和时间范围查询账单信息列表
     * @param userId 用户 ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 账单信息列表
     */
    @Select("SELECT * FROM `bill` WHERE user_id = #{userId} AND bill_time BETWEEN #{startTime} AND #{endTime} ORDER BY bill_time DESC")
    List<Bill> findByUserIdAndTimeRange(@Param("userId") Long userId, 
                                       @Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据用户ID、时间范围、账单ID列表和其他条件查询账单信息列表
     * @param billIds 账单ID列表（可选）
     * @param userId 用户 ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ledgerId 账本ID（可选）
     * @param accountId 账户ID（可选）
     * @param childCategoryIds 分类ID（可选）
     * @param isBudget 是否计入预算（可选）
     * @param amount 金额（可选）
     * @param status 状态（可选）
     * @param sortBy 排序方式：1-时间由近到远（默认），2-时间由远到近，3-金额由大到小，4-金额由小到大
     * @return 账单信息列表
     */
    @Select("<script>" +
            "SELECT * FROM `bill` WHERE 1=1" +
            "<if test='billIds != null and billIds.size() > 0'> AND id IN <foreach collection='billIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>" +
            "<if test='userId != null'> AND user_id = #{userId}</if>" +
            "<if test='ledgerId != null and ledgerId > 0'> AND ledger_id = #{ledgerId}</if>" +
            "<if test='accountId != null and accountId > 0'> AND account_id = #{accountId}</if>" +
            "<if test='childCategoryIds != null and childCategoryIds.size() > 0'> AND class_id IN <foreach collection='childCategoryIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>" +
            "<if test='isBudget != null'> AND is_budget = #{isBudget}</if>" +
            "<if test='amount != null and amount > 0'> AND price = #{amount}</if>" +
            "<if test='status != null and status >= 0'> AND status = #{status}</if>" +
            " AND bill_time BETWEEN #{startTime} AND #{endTime} " + 
            " <choose>" +
            "   <when test='sortBy == 2'> ORDER BY bill_time ASC</when>" +
            "   <when test='sortBy == 3'> ORDER BY price DESC</when>" +
            "   <when test='sortBy == 4'> ORDER BY price ASC</when>" +
            "   <otherwise> ORDER BY bill_time DESC</otherwise>" +
            " </choose>" +
            "</script>")
    List<Bill> findByUserIdAndConditions(
            @Param("billIds") List<Long> billIds,
            @Param("userId") Long userId, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime,
            @Param("ledgerId") Long ledgerId,
            @Param("accountId") Long accountId,
            @Param("childCategoryIds") List<Long> childCategoryIds,
            @Param("isBudget") Integer isBudget,
            @Param("amount") Long amount,
            @Param("status") Integer status,
            @Param("sortBy") Integer sortBy);

    /**
     * 根据账本ID列表、时间范围及其他条件查询账单信息列表
     */
    @Select("<script>" +
            "SELECT * FROM `bill` WHERE 1=1" +
            "<if test='ledgerIds != null and ledgerIds.size() > 0'> AND ledger_id IN <foreach collection='ledgerIds' item='lid' open='(' separator=',' close=')'>#{lid}</foreach></if>" +
            "<if test='accountId != null and accountId > 0'> AND account_id = #{accountId}</if>" +
            "<if test='childCategoryIds != null and childCategoryIds.size() > 0'> AND class_id IN <foreach collection='childCategoryIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>" +
            "<if test='isBudget != null'> AND is_budget = #{isBudget}</if>" +
            "<if test='amount != null and amount > 0'> AND price = #{amount}</if>" +
            "<if test='status != null and status >= 0'> AND status = #{status}</if>" +
            " AND bill_time BETWEEN #{startTime} AND #{endTime} " +
            " <choose>" +
            "   <when test='sortBy == 2'> ORDER BY bill_time ASC</when>" +
            "   <when test='sortBy == 3'> ORDER BY price DESC</when>" +
            "   <when test='sortBy == 4'> ORDER BY price ASC</when>" +
            "   <otherwise> ORDER BY bill_time DESC</otherwise>" +
            " </choose>" +
            "</script>")
    List<Bill> findByLedgerIdsAndConditions(
            @Param("ledgerIds") List<Long> ledgerIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("accountId") Long accountId,
            @Param("childCategoryIds") List<Long> childCategoryIds,
            @Param("isBudget") Integer isBudget,
            @Param("amount") Long amount,
            @Param("status") Integer status,
            @Param("sortBy") Integer sortBy);
    
    /**
     * 更新账单状态
     * @param id 账单ID
     * @param status 状态值
     * @return 更新成功的记录数
     */
    @Update("UPDATE `bill` SET status = #{status} WHERE id = #{id}")
    int updateStatus(Long id, Integer status);
    
    /**
     * 根据账单ID列表和用户ID查询账单信息列表
     * @param billIds 账单ID列表
     * @param sortBy 排序方式：1-时间由近到远（默认），2-时间由远到近，3-金额由大到小，4-金额由小到大
     * @return 账单信息列表
     */
    @Select("<script>" +
            "SELECT * FROM `bill` WHERE " +
            "<if test='billIds != null and billIds.size() > 0'> id IN <foreach collection='billIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>" +
            " <choose>" +
            "   <when test='sortBy == 2'> ORDER BY bill_time ASC</when>" +
            "   <when test='sortBy == 3'> ORDER BY price DESC</when>" +
            "   <when test='sortBy == 4'> ORDER BY price ASC</when>" +
            "   <otherwise> ORDER BY bill_time DESC</otherwise>" +
            " </choose>" +
            "</script>")
    List<Bill> findByIds(@Param("billIds") List<Long> billIds,
                        @Param("sortBy") Integer sortBy);
}