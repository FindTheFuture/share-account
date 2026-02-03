package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user")
    List<User> findAll();

    @Select("SELECT * FROM user ORDER BY create_time DESC")
    List<User> findUserList();

    @Select("SELECT COUNT(1) FROM user")
    int findUserCount();

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Select("<script>" +
            "SELECT * FROM user WHERE id IN " +
            "<foreach item='userId' collection='userIds' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            "</script>")
    List<User> findByIds(@Param("userIds") List<Long> userIds);

    @Select("SELECT * FROM user WHERE openid = #{openid}")
    User findByOpenid(String openid);

    @Select("SELECT * FROM user WHERE phone = #{phone} ORDER BY create_time DESC")
    List<User> findByPhone(String phone);

    @Select("SELECT * FROM user WHERE phone LIKE CONCAT('%', #{phone}, '%') ORDER BY create_time DESC")
    List<User> findLikePhone(String phone);

    @Select("SELECT * FROM user WHERE family_id = #{familyId} ORDER BY create_time DESC")
    List<User> findByFamilyId(Long familyId);

    @Insert("INSERT INTO user (openid, nick_name, phone, sex, role, last_login_time, valid_integral, can_send_message, create_time, notity_bill, unionid) " +
            "VALUES (#{openid}, #{nickName}, #{phone}, #{sex}, #{role}, #{lastLoginTime}, #{validIntegral}, #{canSendMessage}, #{createTime}, #{notityBill}, #{unionid})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE user SET nick_name = #{nickName}, phone = #{phone}, sex = #{sex}, " +
            "role = #{role}, last_login_time = #{lastLoginTime}, valid_integral = #{validIntegral}, " +
            "can_send_message = #{canSendMessage}, notity_bill = #{notityBill} WHERE id = #{id}")
    int update(User user);

    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT u.valid_integral FROM user u WHERE u.id = #{userId}")
    Integer getUserValidIntegral(Long userId);

    @Update("UPDATE user SET valid_integral = #{validIntegral} WHERE id = #{userId}")
    Integer updateUserValidIntegral(@Param("userId") Long userId, @Param("validIntegral") Integer validIntegral);

    @Update("UPDATE user SET last_login_time = NOW() WHERE openid = #{openId}")
    void updateLastLoginTime(@Param("openId") String openId);

    @Update("UPDATE user SET can_send_message = #{canSendMessage} WHERE id = #{userId}")
    Integer canSendMessage(@Param("userId") Long userId, @Param("canSendMessage") Integer canSendMessage);

    @Update("UPDATE user SET phone = #{phone} WHERE id = #{userId}")
    int updatePhone(@Param("userId") Long userId, @Param("phone") String phone);

    @Update("UPDATE user SET phone = #{phone}, nick_name = #{nickName} WHERE id = #{userId}")
    int updateUserPhone(@Param("userId") Long userId, @Param("phone") String phone, @Param("nickName") String nickName);

    @Update("UPDATE user SET can_profit_sharing = #{canProfitSharing} WHERE openid = #{openid}")
    int updateCanProfitSharing(@Param("openid") String openid, @Param("canProfitSharing") Integer canProfitSharing);

    @Select("SELECT * FROM user WHERE can_profit_sharing = #{canProfitSharing}")
    List<User> findByCanProfitSharing(Integer canProfitSharing);

    @Update("UPDATE user SET parent_uid = #{parentUid} WHERE id = #{userId}")
    int updateParentUid(@Param("userId") Long userId, @Param("parentUid") Long parentUid);

    @Update("UPDATE user SET role = #{role} WHERE id = #{userId}")
    int updateRole(@Param("userId") Long userId, @Param("role") Integer role);

    @Select("SELECT * FROM user WHERE can_send_message = #{canSendMessage} AND notity_bill = #{notityBill}")
    List<User> findByCanSendMessageAndNotityBill(@Param("canSendMessage") Integer canSendMessage, @Param("notityBill") Integer notityBill);

    @Select("SELECT * FROM user WHERE can_send_message = #{canSendMessage}")
    List<User> findByCanSendMessage(@Param("canSendMessage") Integer canSendMessage);

    // 新增：升级游客为正常用户，更新openid并开启消息通知
    @Update("UPDATE user SET openid = #{openid}, can_send_message = #{canSendMessage}, last_login_time = NOW() WHERE id = #{userId}")
    int upgradeGuestOpenid(@Param("userId") Long userId, @Param("openid") String openid, @Param("canSendMessage") Integer canSendMessage);

    @Update("UPDATE user SET member_status = #{status} WHERE id = #{userId}")
    int updateMemberStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Select("SELECT * FROM user WHERE id = #{userId} FOR UPDATE")
    User findByIdForUpdate(@Param("userId") Long userId);
}