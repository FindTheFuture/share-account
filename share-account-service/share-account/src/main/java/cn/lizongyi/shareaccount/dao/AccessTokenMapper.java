package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.AccessToken;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.Optional;

@Mapper
public interface AccessTokenMapper {

    @Select("SELECT * FROM wechat_access_token ORDER BY updated_at DESC LIMIT 1")
    Optional<AccessToken> findLatest();

    @Insert("INSERT INTO wechat_access_token (access_token, expires_at) VALUES (#{accessToken}, #{expiresAt}) ON DUPLICATE KEY UPDATE access_token=#{accessToken}, expires_at=#{expiresAt}")
    void saveOrUpdate(AccessToken accessToken);


    /**
     * 删除所有在给定日期之前过期的 AccessToken 记录。
     */
    @Delete("DELETE FROM wechat_access_token WHERE expires_at < #{date}")
    int deleteExpiredBefore(@Param("date") Date date);
}