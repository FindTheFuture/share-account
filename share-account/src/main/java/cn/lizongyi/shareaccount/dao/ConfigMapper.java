package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.Config;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConfigMapper {

    @Select("SELECT * FROM config where user_id=#{userId} and `key` = #{key} order by create_time desc")
    List<Config> getConfigByUserIdAndKey(Long userId, String key);

    @Select("SELECT * FROM config where user_id=#{userId} order by create_time desc")
    List<Config> getConfigByUserId(Long userId);

    @Select("SELECT * FROM config where `key` = #{key} order by create_time desc")
    List<Config> getConfigByKey(String key);

    @Select("SELECT * FROM config where user_id=#{userId} and `type` = #{type} order by create_time desc")
    List<Config> getConfigByUserIdAndType(Long userId, Integer type);
}
