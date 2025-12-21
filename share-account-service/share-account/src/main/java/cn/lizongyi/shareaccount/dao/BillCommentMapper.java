package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.BillComment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BillCommentMapper {

    @Insert("INSERT INTO comment (bill_id, user_id, content, type, create_time) " +
            "VALUES (#{billId}, #{userId}, #{content}, #{type}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BillComment comment);

    @Select("SELECT * FROM comment WHERE id = #{id}")
    BillComment findById(Long id);

    @Select("SELECT * FROM comment WHERE bill_id = #{billId} ORDER BY create_time DESC")
    List<BillComment> findByBillId(Long billId);

    @Delete("DELETE FROM comment WHERE id = #{id}")
    int deleteById(Long id);
}