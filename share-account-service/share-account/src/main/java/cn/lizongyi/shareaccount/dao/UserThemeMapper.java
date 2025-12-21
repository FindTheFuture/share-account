package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.UserTheme;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserThemeMapper {

    @Select("SELECT * FROM user_theme WHERE user_id = #{userId} ORDER BY id DESC LIMIT 1")
    UserTheme findLatestByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO user_theme(user_id, skin_id, primary_color, create_time, update_time) VALUES(#{userId}, #{skinId}, #{primaryColor}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserTheme userTheme);

    @Update("UPDATE user_theme SET skin_id = #{skinId}, primary_color = #{primaryColor}, update_time = NOW() WHERE id = #{id}")
    int update(UserTheme userTheme);

    @Select("SELECT * FROM user_theme WHERE user_id = #{userId}")
    List<UserTheme> findAllByUserId(@Param("userId") Long userId);
}