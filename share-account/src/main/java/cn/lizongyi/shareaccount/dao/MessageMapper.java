package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.Message;
import cn.lizongyi.shareaccount.response.MessageResponse;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageMapper {
    @Insert("INSERT INTO message (title, content, type, priority, status, created_at, updated_at) " +
            "VALUES (#{title}, #{content}, #{type}, #{priority}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);
    
    @Update("UPDATE message SET " +
            "title = #{title}, " +
            "content = #{content}, " +
            "type = #{type}, " +
            "priority = #{priority}, " +
            "status = #{status}, " +
            "updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int updateById(Message message);
    
    @Select("SELECT * FROM message WHERE id = #{id} AND status = 0")
    Message selectById(Long id);
    
    @Select("SELECT * FROM message WHERE type = #{type} AND status = 0 ORDER BY created_at DESC LIMIT #{limit}")
    List<Message> selectByType(@Param("type") Integer type, @Param("limit") Integer limit);
    
    @Update("UPDATE message SET status = 1 WHERE id = #{id}")
    int deleteById(Long id);
    
    @Select("SELECT m.id, m.title, m.content, m.type, m.priority, " +
            "um.is_read as isRead, m.created_at as createdAt, um.read_at as readAt " +
            "FROM message m " +
            "LEFT JOIN user_message um ON m.id = um.message_id AND um.user_id = #{userId} " +
            "WHERE um.user_id = #{userId} " +
            "AND um.is_read = 0 " +
            "ORDER BY m.created_at DESC LIMIT 1")
    MessageResponse selectLatestByUserId(Long userId);
}