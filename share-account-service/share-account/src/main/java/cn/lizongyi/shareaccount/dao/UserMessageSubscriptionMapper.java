package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.UserMessageSubscription;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 用户消息订阅Mapper
 */
@Mapper
public interface UserMessageSubscriptionMapper {
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM user_message_subscription WHERE id = #{id}")
    Optional<UserMessageSubscription> selectById(Long id);
    
    /**
     * 根据用户ID和模板ID查询
     */
    @Select("SELECT * FROM user_message_subscription WHERE user_id = #{userId} AND template_id = #{templateId}")
    Optional<UserMessageSubscription> selectByUserIdAndTemplateId(@Param("userId") Long userId, @Param("templateId") String templateId);
    
    /**
     * 根据用户ID查询所有订阅
     */
    @Select("SELECT * FROM user_message_subscription WHERE user_id = #{userId}")
    List<UserMessageSubscription> selectByUserId(Long userId);
    
    /**
     * 查询授权次数大于0且未设置不再提醒的用户订阅
     */
    @Select("SELECT * FROM user_message_subscription WHERE template_id = #{templateId} AND authorize_count > 0 AND never_remind = false")
    List<UserMessageSubscription> selectAuthorizedSubscriptions(@Param("templateId") String templateId);
    
    /**
     * 插入订阅记录
     */
    @Insert("INSERT INTO user_message_subscription (user_id, template_id, template_key_id, authorize_count, never_remind, created_at, updated_at, authorized_at) " +
            "VALUES (#{userId}, #{templateId}, #{templateKeyId}, #{authorizeCount}, #{neverRemind}, #{createdAt}, #{updatedAt}, #{authorizedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UserMessageSubscription subscription);
    
    /**
     * 更新订阅记录
     */
    @Update("UPDATE user_message_subscription SET user_id = #{userId}, template_id = #{templateId}, template_key_id = #{templateKeyId}, " +
            "authorize_count = #{authorizeCount}, never_remind = #{neverRemind}, updated_at = #{updatedAt}, authorized_at = #{authorizedAt} WHERE id = #{id}")
    void updateById(UserMessageSubscription subscription);
    
    /**
     * 更新授权状态（增加授权次数）
     */
    @Update("UPDATE user_message_subscription SET authorize_count = authorize_count + 1, authorized_at = #{authorizedAt}, updated_at = NOW() " +
            "WHERE user_id = #{userId} AND template_id = #{templateId}")
    void updateAuthorizationStatus(@Param("userId") Long userId, @Param("templateId") String templateId, 
                                  @Param("authorizedAt") java.util.Date authorizedAt);
    
    /**
     * 减少授权次数（消息发送成功后）
     */
    @Update("UPDATE user_message_subscription SET authorize_count = GREATEST(0, authorize_count - 1), updated_at = NOW() " +
            "WHERE user_id = #{userId} AND template_id = #{templateId} AND authorize_count > 0")
    int decreaseAuthorizeCount(@Param("userId") Long userId, @Param("templateId") String templateId);
    
    /**
     * 更新不再提醒状态
     */
    @Update("UPDATE user_message_subscription SET never_remind = #{neverRemind}, updated_at = NOW() " +
            "WHERE user_id = #{userId} AND template_id = #{templateId}")
    void updateNeverRemind(@Param("userId") Long userId, @Param("templateId") String templateId, @Param("neverRemind") Boolean neverRemind);
}