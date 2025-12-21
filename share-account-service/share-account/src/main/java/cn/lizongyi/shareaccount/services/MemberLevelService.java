package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.MemberLevelConfig;

import java.util.List;

public interface MemberLevelService {

    /**
     * 根据ID查询会员等级配置
     */
    MemberLevelConfig findById(Long id);

    /**
     * 根据等级数字查询会员等级配置
     */
    MemberLevelConfig findByLevel(Integer level);

    /**
     * 根据积分查询会员等级
     */
    MemberLevelConfig findLevelByPoints(Integer points);

    /**
     * 获取会员积分对应的最高等级
     */
    MemberLevelConfig findMaxLevelByPoints(Integer points);

    /**
     * 查询所有会员等级配置
     */
    List<MemberLevelConfig> findAllLevels();

    /**
     * 获取用户当前会员等级
     */
    MemberLevelConfig getUserCurrentLevel(Long userId);

    /**
     * 计算用户的会员等级
     */
    Integer calculateMemberLevel(Integer points);
}