package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.UserMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户会员表Mapper接口
 */
@Mapper
public interface UserMemberMapper {
    
    /**
     * 插入用户会员记录
     */
    @Insert("INSERT INTO user_member (user_id, package_id, package_name, price, package_type, payment_record_id, ai_count, ai_used_count, pdf_count, pdf_used_count, start_time, end_time, create_time, status) VALUES (#{userId}, #{packageId}, #{packageName}, #{price}, #{packageType}, #{paymentRecordId}, #{aiCount}, #{aiUsedCount}, #{pdfCount}, #{pdfUsedCount}, #{startTime}, #{endTime}, #{createTime}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserMember userMember);
    
    /**
     * 根据ID更新状态
     */
    @Update("UPDATE user_member SET status = #{status} WHERE id = #{id}")
    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 根据支付记录ID更新状态
     */
    @Update("UPDATE user_member SET status = #{status} WHERE payment_record_id = #{paymentRecordId}")
    int updateStatusByPaymentRecordId(@Param("paymentRecordId") Long paymentRecordId, @Param("status") Integer status);
    
    /**
     * 更新AI使用次数
     */
    @Update("UPDATE user_member SET ai_used_count = ai_used_count + 1 WHERE id = #{id} AND status = 0 AND end_time > NOW()")
    int incrementAiUsedCount(Long id);
    
    /**
     * 更新PDF使用次数
     */
    @Update("UPDATE user_member SET pdf_used_count = pdf_used_count + 1 WHERE id = #{id} AND status = 0 AND end_time > NOW()")
    int incrementPdfUsedCount(Long id);
    
    /**
     * 根据用户ID查询所有记录（分页）
     */
    @Select("SELECT * FROM user_member WHERE user_id = #{userId} ORDER BY end_time")
    List<UserMember> findByUserId(@Param("userId") Long userId);


    /**
     * 根据用户ID查询最新的有效记录（按结束时间升序）
     */
    @Select("SELECT * FROM user_member WHERE user_id = #{userId} AND status = 0 AND end_time > NOW() ORDER BY end_time LIMIT 1")
    UserMember findLatestValidByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和状态查询记录（支持状态筛选）
     */
    @Select("<script> SELECT * FROM user_member WHERE user_id = #{userId} " +
            "<if test=\"status != null\"> AND status = #{status} </if> " +
            "ORDER BY end_time </script>")
    List<UserMember> findByUserIdCanStatus(@Param("userId") Long userId, @Param("status") Integer status);
    
    /**
     * 根据用户ID和状态查询记录
     */
    @Select("SELECT * FROM user_member WHERE user_id = #{userId} AND status = #{status}")
    List<UserMember> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);
    
    /**
     * 查询所有过期但状态未更新的记录
     */
    @Select("SELECT * FROM user_member WHERE status = 0 AND end_time < NOW()")
    List<UserMember> findExpiredMembers();
    
    /**
     * 根据支付记录ID查询
     */
    @Select("SELECT * FROM user_member WHERE payment_record_id = #{paymentRecordId}")
    UserMember findByPaymentRecordId(Long paymentRecordId);
    
    /**
     * 根据用户ID、状态和套餐类型查询最新的有效记录
     */
    @Select("SELECT * FROM user_member WHERE user_id = #{userId} AND status = 0 AND package_type = #{packageType} AND end_time > NOW() ORDER BY end_time DESC LIMIT 1")
    UserMember findLatestValidByUserIdAndType(@Param("userId") Long userId, @Param("packageType") Integer packageType);
    
    /**
     * 统计用户有效会员记录数量
     */
    @Select("SELECT COUNT(*) FROM user_member WHERE user_id = #{userId} AND status = 0 AND end_time > NOW()")
    int countValidMembersByUserId(Long userId);
    
    /**
     * 分页查询用户会员记录
     */
    @Select("SELECT * FROM user_member WHERE user_id = #{userId} ORDER BY status ASC, end_time DESC LIMIT #{offset}, #{pageSize}")
    List<UserMember> findUserMembersByPage(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    
    /**
     * 统计用户购买记录数量（可按状态筛选）
     * @param userId 用户ID
     * @param statusList 状态列表，null表示不过滤状态
     * @return 符合条件的记录数量
     */
    @Select("<script> SELECT COUNT(*) FROM user_member WHERE user_id = #{userId} AND package_id > -1 " +
            "<if test=\"statusList != null and statusList.size() > 0\"> AND status IN " +
            "<foreach collection=\"statusList\" item=\"status\" open=\"(\" separator=\",\" close=\")\">" +
            "#{status}" +
            "</foreach>" +
            "</if>" +
            "</script>")
    int countUserPurchasesByStatus(@Param("userId") Long userId, @Param("statusList") List<Integer> statusList);
    
    /**
     * 统计用户会员记录总数
     */
    @Select("SELECT COUNT(*) FROM user_member WHERE user_id = #{userId}")
    Long countUserMembers(@Param("userId") Long userId);
    
    /**
     * 查询正常状态且未过期的会员记录
     */
    @Select("SELECT * FROM user_member WHERE user_id = #{userId} AND status = 0 AND end_time > NOW()")
    List<UserMember> findValidUserMembers(@Param("userId") Long userId);
    
}