package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.MemberLevelConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MemberLevelConfigMapper {

    @Select("SELECT * FROM member_level_config WHERE id = #{id}")
    MemberLevelConfig findById(Long id);

    @Select("SELECT * FROM member_level_config ORDER BY level ASC")
    List<MemberLevelConfig> findAllLevels();

    @Select("SELECT * FROM member_level_config WHERE level = #{level}")
    MemberLevelConfig findByLevel(Integer level);

    @Select("SELECT * FROM member_level_config WHERE #{points} >= min_points AND #{points} <= max_points")
    MemberLevelConfig findLevelByPoints(Integer points);

    @Select("SELECT * FROM member_level_config WHERE min_points <= #{points} ORDER BY level DESC LIMIT 1")
    MemberLevelConfig findMaxLevelByPoints(Integer points);
}