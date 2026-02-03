package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.Session;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface SessionMapper {

    @Select("SELECT * FROM sessions WHERE id = #{id}")
    Session findById(@Param("id") String id);

    @Select("SELECT * FROM sessions WHERE openid = #{openId}")
    List<Session> findByOpenId(@Param("openId") String openId);

    @Insert("INSERT INTO sessions (id, openid, session_key, create_time, expire_time, device_info) " +
            "VALUES (#{id}, #{openId}, #{sessionKey}, NOW(), #{expireTime}, #{deviceInfo})")
    void insert(Session session);

    @Update("UPDATE sessions SET expire_time=#{expireTime}, last_activity=NOW(), device_info=#{deviceInfo} WHERE id=#{id}")
    void update(Session session);

    @Delete("DELETE FROM sessions WHERE id = #{id}")
    void deleteById(@Param("id") String id);

    @Delete("DELETE FROM sessions WHERE openid = #{openId}")
    void deleteByOpenId(@Param("openId") String openId);

    @Delete("DELETE FROM sessions WHERE expire_time < #{currentTime}")
    void deleteExpiredSessions(@Param("currentTime") Date currentTime);
}