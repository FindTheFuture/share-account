package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.Member;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MemberMapper {

    @Select("SELECT * FROM member WHERE id = #{id}")
    Member findById(Long id);

    @Select("SELECT * FROM member WHERE ledger_id = #{ledgerId} ORDER BY create_time DESC")
    List<Member> findByLedgerId(Long ledgerId);

    @Select("SELECT * FROM member WHERE user_id = #{userId}")
    List<Member> findByUserId(Long userId);

    @Select("SELECT * FROM member WHERE parent_user_id = #{parentUserId} order by create_time desc")
    List<Member> findByParentUserId(Long parentUserId);
    
    /**
     * 查询正常状态的成员列表（status = 1）
     */
    @Select("SELECT * FROM member WHERE user_id = #{userId} AND status = #{status} order by parent_user_id,create_time desc")
    List<Member> findNormalByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    @Select("SELECT * FROM member WHERE ledger_id = #{ledgerId} AND bill_id = #{billId} AND user_id = #{userId}")
    Member findByLedgerIdAndUserId(@Param("ledgerId") Long ledgerId, @Param("billId") Long billId, @Param("userId") Long userId);

    @Select("SELECT * FROM member WHERE ledger_id = #{ledgerId} AND parent_user_id = #{parentUserId}")
    List<Member> findByLedgerIdAndParentUserId(@Param("ledgerId") Long ledgerId, @Param("parentUserId") Long parentUserId);

    @Insert("INSERT INTO member (name, ledger_id, bill_id, parent_user_id, user_id, percentage, status, create_time) " +
            "VALUES (#{name}, #{ledgerId}, #{billId}, #{parentUserId}, #{userId}, #{percentage}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Member member);
    
    @Update({
        "<script>",
        "UPDATE member",
        "<set>",
        "<if test='name != null'> name = #{name},</if>",
        "<if test='ledgerId != null'> ledger_id = #{ledgerId},</if>",
        "<if test='billId != null'> bill_id = #{billId},</if>",
        "<if test='parentUserId != null'> parent_user_id = #{parentUserId},</if>",
        "<if test='userId != null'> user_id = #{userId},</if>",
        "<if test='percentage != null'> percentage = #{percentage},</if>",
        "<if test='status != null'> status = #{status}</if>",
        "</set>",
        "WHERE id = #{id}",
        "</script>"
    })
    int update(Member member);
    
    /**
     * 接受邀请时更新成员信息（只更新userId和status）
     */
    @Update("UPDATE member SET user_id = #{userId}, status = #{status} WHERE id = #{id}")
    int updateForAcceptInvitation(@Param("id") Long id, @Param("userId") Long userId, @Param("status") Integer status);

    @Update("UPDATE member SET percentage = #{percentage} WHERE id = #{id}")
    int updatePercentage(@Param("id") Long id, @Param("percentage") Integer percentage);

    @Delete("DELETE FROM member WHERE id = #{id}")
    int deleteById(Long id);

    @Delete("DELETE FROM member WHERE ledger_id = #{ledgerId} AND user_id = #{userId}")
    int deleteByLedgerIdAndUserId(@Param("ledgerId") Long ledgerId, @Param("userId") Long userId);

    // 查找同一邀请人对同一账本的待接受邀请（ledger_id, parent_user_id, user_id, bill_id is null, status=0）
    @Select("SELECT * FROM member WHERE ledger_id = #{ledgerId} AND parent_user_id = #{parentUserId} AND user_id = #{userId} AND status = #{status} AND bill_id is null ORDER BY create_time DESC LIMIT 1")
    Member findPendingInvite(@Param("ledgerId") Long ledgerId, @Param("parentUserId") Long parentUserId, @Param("userId") Long userId, @Param("status") Integer status);

    // 查找账本成员（ledger_id, parent_user_id, user_id, bill_id is null）
    @Select("SELECT * FROM member WHERE ledger_id = #{ledgerId} AND parent_user_id = #{parentUserId} AND user_id = #{userId} AND bill_id is null ORDER BY create_time DESC LIMIT 1")
    Member findLedgerMember(@Param("ledgerId") Long ledgerId, @Param("parentUserId") Long parentUserId, @Param("userId") Long userId);

    // 查找完整唯一键（ledger_id, bill_id, parent_user_id, user_id）
    @Select("SELECT * FROM member WHERE ledger_id = #{ledgerId} AND bill_id = #{billId} AND parent_user_id = #{parentUserId} AND user_id = #{userId} AND status = #{status}")
    Member findByFullKey(@Param("ledgerId") Long ledgerId, @Param("billId") Long billId, @Param("parentUserId") Long parentUserId, @Param("userId") Long userId, @Param("status") Integer status);

    // 过期未接受的邀请标记为 status=2（2天前创建）
    @Update("UPDATE member SET status = 2 WHERE status = 0 AND create_time < #{cutoff}")
    int expirePendingInvites(@Param("cutoff") java.time.LocalDateTime cutoff);

    // 物理删除已过期的邀请（status=2 且 3天前创建）
    @Delete("DELETE FROM member WHERE status = 2 AND create_time < #{cutoff}")
    int deleteExpired(@Param("cutoff") java.time.LocalDateTime cutoff);

    @Select("SELECT SUM(percentage) FROM member WHERE ledger_id = #{ledgerId}")
    Integer getTotalPercentageByLedgerId(Long ledgerId);

    /**
     * 查询正常状态的成员列表（status = 1）
     */
    @Select("SELECT * FROM member WHERE ledger_id = #{ledgerId} AND status = #{status} ORDER BY create_time DESC")
    List<Member> findByLedgerIdAndStatus(@Param("ledgerId") Long ledgerId, @Param("status") Integer status);
}