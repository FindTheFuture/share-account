package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.MemberLevelConfigMapper;
import cn.lizongyi.shareaccount.dao.UserMapper;
import cn.lizongyi.shareaccount.entity.MemberLevelConfig;
import cn.lizongyi.shareaccount.services.MemberLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MemberLevelServiceImpl implements MemberLevelService {

    @Autowired
    private MemberLevelConfigMapper memberLevelConfigMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public MemberLevelConfig findById(Long id) {
        log.info("查询会员等级配置，id: {}", id);
        return memberLevelConfigMapper.findById(id);
    }

    @Override
    public MemberLevelConfig findByLevel(Integer level) {
        log.info("查询会员等级配置，等级: {}", level);
        return memberLevelConfigMapper.findByLevel(level);
    }

    @Override
    public MemberLevelConfig findLevelByPoints(Integer points) {
        log.info("根据积分查询会员等级，积分: {}", points);
        return memberLevelConfigMapper.findLevelByPoints(points);
    }

    @Override
    public MemberLevelConfig findMaxLevelByPoints(Integer points) {
        log.info("获取积分对应的最高等级，积分: {}", points);
        return memberLevelConfigMapper.findMaxLevelByPoints(points);
    }

    @Override
    public List<MemberLevelConfig> findAllLevels() {
        log.info("查询所有会员等级配置");
        return memberLevelConfigMapper.findAllLevels();
    }

    @Override
    public MemberLevelConfig getUserCurrentLevel(Long userId) {
        log.info("获取用户当前会员等级，userId: {}", userId);
        Integer points = userMapper.getUserValidIntegral(userId);
        if (points == null || points < 0) {
            points = 0;
        }
        return findMaxLevelByPoints(points);
    }

    @Override
    public Integer calculateMemberLevel(Integer points) {
        log.info("计算会员等级，积分: {}", points);
        MemberLevelConfig levelConfig = findMaxLevelByPoints(points);
        if (levelConfig != null) {
            return levelConfig.getLevel();
        }
        // 默认返回最低等级
        return 1;
    }
}