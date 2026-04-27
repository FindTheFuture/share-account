package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.Contact;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ContactMapper {

    @Select("SELECT * FROM contact WHERE id = #{id}")
    Contact findById(Long id);

    @Select("SELECT * FROM contact WHERE id = #{id} AND status = #{status}")
    Contact findByIdAndStatus(@Param("id") Long id, Integer status);

    @Select("SELECT * FROM contact WHERE user_id = #{userId} AND status = #{status} ORDER BY update_time DESC")
    List<Contact> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Select("SELECT * FROM contact WHERE user_id = #{userId} ORDER BY update_time DESC")
    List<Contact> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM contact WHERE user_id = #{userId} AND friend_id = #{friendId}")
    Contact findByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Select("SELECT * FROM contact WHERE (user_id = #{userId} AND friend_id = #{friendId}) OR (user_id = #{friendId} AND friend_id = #{userId})")
    List<Contact> findRelation(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Select("SELECT * FROM contact WHERE friend_id = #{friendId} AND status = 0 ORDER BY create_time DESC")
    List<Contact> findPendingRequests(@Param("friendId") Long friendId);

    @Select("SELECT COUNT(1) FROM contact WHERE friend_id = #{friendId} AND status = 0")
    int countPendingRequests(@Param("friendId") Long friendId);

    @Insert("INSERT INTO contact (user_id, friend_id, status, create_time, update_time) VALUES (#{userId}, #{friendId}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Contact contact);

    @Update("UPDATE contact SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM contact WHERE id = #{id}")
    int deleteById(Long id);

    @Delete("DELETE FROM contact WHERE (user_id = #{userId} AND friend_id = #{friendId}) OR (user_id = #{friendId} AND friend_id = #{userId})")
    int deleteRelation(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
