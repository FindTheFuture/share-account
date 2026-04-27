package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.ChatMessage;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChatMessageMapper {

    @Select("SELECT * FROM chat_message WHERE ((from_user_id = #{userId1} AND to_user_id = #{userId2}) OR (from_user_id = #{userId2} AND to_user_id = #{userId1})) AND is_deleted = 0 AND create_time >= #{threeMonthsAgo} ORDER BY create_time DESC LIMIT #{limit}")
    List<ChatMessage> findChatHistory(@Param("userId1") Long userId1, @Param("userId2") Long userId2, @Param("limit") Integer limit, @Param("threeMonthsAgo") LocalDateTime threeMonthsAgo);

    @Select("SELECT * FROM chat_message WHERE to_user_id = #{toUserId} AND is_read = 0 AND is_deleted = 0 ORDER BY create_time DESC")
    List<ChatMessage> findUnreadByToUserId(@Param("toUserId") Long toUserId);

    @Select("SELECT COUNT(1) FROM chat_message WHERE to_user_id = #{userId} AND is_read = 0 AND is_deleted = 0")
    int countUnreadByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM chat_message WHERE from_user_id = #{fromUserId} AND to_user_id = #{toUserId} AND is_read = 0 AND is_deleted = 0")
    int countUnreadBetweenUsers(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    @Insert("INSERT INTO chat_message (from_user_id, to_user_id, type, content, is_read, is_deleted, create_time) VALUES (#{fromUserId}, #{toUserId}, #{type}, #{content}, 0, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatMessage message);

    @Update("UPDATE chat_message SET is_read = 1 WHERE to_user_id = #{userId} AND is_read = 0")
    int markAllReadByUserId(@Param("userId") Long userId);

    @Update("UPDATE chat_message SET is_read = 1 WHERE from_user_id = #{fromUserId} AND to_user_id = #{toUserId} AND is_read = 0")
    int markReadBetweenUsers(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    @Update("UPDATE chat_message SET is_deleted = 1 WHERE id = #{id} AND from_user_id = #{fromUserId}")
    int markDeleted(@Param("id") Long id, @Param("fromUserId") Long fromUserId);

    @Update("UPDATE chat_message SET is_recalled = 1 WHERE id = #{id} AND from_user_id = #{fromUserId}")
    int markRecalled(@Param("id") Long id, @Param("fromUserId") Long fromUserId);

    @Delete("DELETE FROM chat_message WHERE create_time < #{beforeTime}")
    int deleteOldMessages(@Param("beforeTime") LocalDateTime beforeTime);

    @Select("SELECT * FROM chat_message WHERE id = #{id}")
    ChatMessage findById(Long id);

    @Update("UPDATE chat_message SET content = #{content} WHERE id = #{id}")
    int updateContent(@Param("id") Long id, @Param("content") String content);
}
