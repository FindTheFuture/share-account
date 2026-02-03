package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.Ledger;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LedgerMapper {

    @Select("SELECT * FROM ledger WHERE id = #{id}")
    Ledger findById(Long id);

    @Select("SELECT * FROM ledger WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Ledger> findByUserId(Long userId);

    @Select("SELECT * FROM ledger WHERE status = #{status} ORDER BY create_time DESC")
    List<Ledger> findByStatus(Integer status);

    @Insert("INSERT INTO ledger (user_id, name, type, property, class_id, status, memo, create_time, is_default) " +
            "VALUES (#{userId}, #{name}, #{type}, #{property}, #{classId}, #{status}, #{memo}, #{createTime}, #{isDefault})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Ledger ledger);

    @Update("UPDATE ledger SET name = #{name}, type = #{type}, property = #{property}, class_id = #{classId}, " +
            "status = #{status}, memo = #{memo}, is_default = #{isDefault} WHERE id = #{id}")
    int update(Ledger ledger);

    @Update("UPDATE ledger SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM ledger WHERE id = #{id}")
    int deleteById(Long id);
    
    @Update("UPDATE ledger SET is_default = 0 WHERE user_id = #{userId}")
    int resetDefaultLedger(Long userId);
}