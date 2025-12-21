package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.WxadInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WxadMapper {

    // 插入广告加载记录
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO wxad_info (user_id, lottery_id, source, type, status, code, message, ad_unit_id, load_time, is_shown, is_completed, retry_count, create_time) " +
            "VALUES (#{userId}, #{lotteryId}, #{source}, #{type}, #{status}, #{code}, #{message}, #{adUnitId}, #{loadTime}, #{isShown}, #{isCompleted}, #{retryCount}, NOW())")
    int insert(WxadInfo wxadInfo);

    // 根据ID查询广告加载记录
    @Select("SELECT * FROM wxad_info WHERE id = #{id}")
    WxadInfo selectById(Long id);

    // 查询所有广告加载记录
    @Select("SELECT * FROM wxad_info ORDER BY create_time DESC")
    List<WxadInfo> selectAll();
}