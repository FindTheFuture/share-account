package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.Account;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Account 对应的 MyBatis Mapper 接口
 */
@Mapper
public interface AccountMapper {

    /**
     * 查询所有账户信息
     * @return 账户信息列表
     */
    @Select("SELECT * FROM `account`")
    List<Account> findAll();

    /**
     * 根据 ID 查询账户信息
     * @param id 账户 ID
     * @return 账户信息对象，如果未找到则返回 null
     */
    @Select("SELECT * FROM `account` WHERE id = #{id}")
    Account findById(Long id);

    /**
     * 根据用户 ID 查询账户信息列表
     * @param userId 用户 ID
     * @return 账户信息列表
     */
    @Select("SELECT * FROM `account` WHERE user_id = #{userId}")
    List<Account> findByUserId(Long userId);

    /**
     * 插入账户信息
     * @param account 要插入的账户信息对象
     * @return 插入成功的记录数
     */
    @Insert("INSERT INTO `account` (user_id, type, is_budget, is_total_money, is_default, balance, name, status, memo, create_time) " +
            "VALUES (#{userId}, #{type}, #{isBudget}, #{isTotalMoney}, #{isDefault}, #{balance}, #{name}, #{status}, #{memo}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Account account);

    /**
     * 根据 ID 更新账户信息
     * @param account 要更新的账户信息对象，需包含 ID
     * @return 更新成功的记录数
     */
    @Update("UPDATE `account` SET type = #{type}, is_budget = #{isBudget}, is_total_money = #{isTotalMoney}, is_default = #{isDefault}, balance = #{balance}, name = #{name}, memo = #{memo} WHERE id = #{id}")
    int update(Account account);


    @Update("UPDATE `account` SET status = #{status} WHERE id = #{id}")
    int updateStatus(Long id, Integer status);



    /**
     * 根据 ID 删除账户信息（逻辑删除，更新状态为 1）
     * @param id 要删除的账户 ID
     * @return 删除成功的记录数
     */
    @Update("UPDATE `account` SET status = 1 WHERE id = #{id}")
    int deleteById(Long id);



    /**
     * 查询账户数量
     * @return 账户数量
     */
    @Select("SELECT COUNT(1) FROM `account`")
    int findAccountCount();

    /**
     * 根据类型查询账户信息列表
     * @param type 账户类型
     * @return 账户信息列表
     */
    @Select("SELECT * FROM `account` WHERE type = #{type}")
    List<Account> findByType(Integer type);

    /**
     * 清除该用户所有账户的默认标记（将 is_default 置为 0）
     */
    @Update("UPDATE `account` SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefaultForUser(@Param("userId") Long userId);
}