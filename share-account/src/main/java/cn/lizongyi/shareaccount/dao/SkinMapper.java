package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.Skin;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SkinMapper {

    @Select("SELECT * FROM skin WHERE status = 0 ORDER BY id DESC")
    List<Skin> findAllPublished();

    @Select("SELECT * FROM skin WHERE id = #{id}")
    Skin findById(@Param("id") Long id);

    @Insert("INSERT INTO skin(name, type, preview_color, variables_json, status, create_time, update_time) VALUES(#{name}, #{type}, #{previewColor}, #{variablesJson}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Skin skin);

    @Update("UPDATE skin SET name=#{name}, type=#{type}, preview_color=#{previewColor}, variables_json=#{variablesJson}, status=#{status}, update_time=NOW() WHERE id=#{id}")
    int update(Skin skin);

    // 新增：查询免费颜色（type=0，status=0）
    @Select("SELECT * FROM skin WHERE status = 0 AND type = 0 ORDER BY sort_order DESC")
    List<Skin> findAllFree();

    // 新增：查询会员皮肤（type=1，status=0）
    @Select("SELECT * FROM skin WHERE status = 0 AND type = 1 ORDER BY sort_order DESC")
    List<Skin> findAllVip();
}