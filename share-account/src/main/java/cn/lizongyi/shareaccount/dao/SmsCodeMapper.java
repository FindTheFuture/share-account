package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.SmsCode;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SmsCodeMapper {

    @Insert("INSERT INTO sms_code (phone, code, type, status, expire_time, error_count, locked_until, create_time, ip_address) " +
            "VALUES (#{phone}, #{code}, #{type}, #{status}, #{expireTime}, #{errorCount}, #{lockedUntil}, #{createTime}, #{ipAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SmsCode smsCode);

    @Select("SELECT * FROM sms_code WHERE phone = #{phone} AND type = #{type} ORDER BY create_time DESC LIMIT 1")
    SmsCode findLatestByPhoneAndType(@Param("phone") String phone, @Param("type") String type);

    @Select("SELECT * FROM sms_code WHERE phone = #{phone} AND type = #{type} AND status = #{status} ORDER BY create_time DESC LIMIT 1")
    SmsCode findLatestByPhoneAndTypeAndStatus(@Param("phone") String phone, @Param("type") String type, @Param("status") Integer status);

    @Select("SELECT * FROM sms_code WHERE phone = #{phone} AND type = #{type} ORDER BY create_time DESC LIMIT #{limit}")
    List<SmsCode> findRecentByPhoneAndType(@Param("phone") String phone, @Param("type") String type, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM sms_code WHERE ip_address = #{ipAddress} AND create_time >= #{startTime}")
    int countByIpAddressSince(@Param("ipAddress") String ipAddress, @Param("startTime") LocalDateTime startTime);

    @Update("UPDATE sms_code SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE sms_code SET error_count = #{errorCount}, locked_until = #{lockedUntil} WHERE id = #{id}")
    int updateErrorCountAndLock(@Param("id") Long id, @Param("errorCount") Integer errorCount, @Param("lockedUntil") LocalDateTime lockedUntil);

    @Select("SELECT * FROM sms_code WHERE phone = #{phone} AND type = #{type} AND status = 0 AND expire_time > NOW() ORDER BY create_time DESC LIMIT 1")
    SmsCode findValidByPhoneAndType(@Param("phone") String phone, @Param("type") String type);

    @Update("UPDATE sms_code SET status = 2 WHERE status = 0 AND expire_time <= NOW()")
    int expireOldCodes();
}