package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.UserMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMessageMapper {
    @Insert("INSERT INTO user_message (message_id, user_id, is_read, read_at, created_at) " +
            "VALUES (#{messageId}, #{userId}, #{isRead}, #{readAt}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserMessage userMessage);
    
    @Update("UPDATE user_message SET is_read = #{isRead}, read_at = #{readAt} WHERE id = #{id}")
    int updateById(UserMessage userMessage);
    
    @Update("UPDATE user_message SET is_read = 1, read_at = NOW() " +
            "WHERE user_id = #{userId} AND message_id = #{messageId} AND is_read = 0")
    int updateReadStatus(@Param("userId") Long userId, @Param("messageId") Long messageId);
    
    @Update("UPDATE user_message SET is_read = 1, read_at = NOW() " +
            "WHERE user_id = #{userId} AND is_read = 0")
    int updateAllReadByUserId(Long userId);
    
    @Update("UPDATE user_message SET is_read = 1, read_at = NOW() " +
            "WHERE user_id = #{userId} AND is_read = 0 " +
            "AND message_id IN (SELECT id FROM message WHERE type = #{type})")
    int updateAllReadByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);
    
    @Select({"<script>",
            "SELECT * FROM user_message ",
            "WHERE user_id = #{userId} ",
            "<if test=\"type != null\">",
            "AND message_id IN (SELECT id FROM message WHERE type = #{type}) ",
            "</if>",
            "ORDER BY created_at DESC ",
            "LIMIT #{page}, #{size}",
            "</script>"})
    @Lang(org.apache.ibatis.scripting.xmltags.XMLLanguageDriver.class)
    List<UserMessage> selectByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type, 
                                           @Param("page") Integer page, @Param("size") Integer size);
    
    // 新增：统计用户消息总数（可选按类型）
    @Select({"<script>",
            "SELECT COUNT(*) FROM user_message ",
            "WHERE user_id = #{userId} ",
            "<if test=\"type != null\">",
            "AND message_id IN (SELECT id FROM message WHERE type = #{type}) ",
            "</if>",
            "</script>"})
    @Lang(org.apache.ibatis.scripting.xmltags.XMLLanguageDriver.class)
    int countByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);
    
    @Select("SELECT COUNT(*) FROM user_message WHERE user_id = #{userId} AND is_read = 0")
    int countUnreadByUserId(Long userId);
    
    @Select("SELECT * FROM user_message WHERE user_id = #{userId} AND message_id = #{messageId}")
    UserMessage selectByUserIdAndMessageId(@Param("userId") Long userId, @Param("messageId") Long messageId);
    
    // 高级查询，按消息类型、优先级、创建时间排序，只查询系统消息（type = 1）
    @Select({"SELECT um.* FROM user_message um",
            "JOIN message m ON um.message_id = m.id",
            "WHERE um.user_id = #{userId} AND m.status = 0 AND m.type = 1",
            "ORDER BY m.type DESC, m.priority DESC, um.created_at DESC"})
    List<UserMessage> selectAdvancedByUserId(Long userId);
    
    // 统计高级查询的总数，只统计系统消息
    @Select("SELECT COUNT(*) FROM user_message um JOIN message m ON um.message_id = m.id WHERE um.user_id = #{userId} AND m.status = 0 AND m.type = 1")
    int countAdvancedByUserId(Long userId);
    
    @Delete("DELETE FROM user_message WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
}