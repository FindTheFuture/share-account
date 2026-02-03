package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.MemberPackageMapper;
import cn.lizongyi.shareaccount.dao.UserMemberMapper;
import cn.lizongyi.shareaccount.entity.MemberPackage;
import cn.lizongyi.shareaccount.response.MemberPackageResponse;
import cn.lizongyi.shareaccount.services.MemberPackageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.lizongyi.shareaccount.enums.UserMemberStatusEnum;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MemberPackageServiceImpl implements MemberPackageService {

    @Autowired
    private MemberPackageMapper memberPackageMapper;
    
    @Autowired
    private UserMemberMapper userMemberMapper;

    @Override
    public MemberPackageResponse findById(Long id) {
        log.info("查询会员套餐，id: {}", id);
        return MemberPackageResponse.fromMemberPackage(memberPackageMapper.findById(id));
    }

    @Override
    public List<MemberPackageResponse> findActivePackages() {
        log.info("查询所有激活的会员套餐");
        List<MemberPackage> list = memberPackageMapper.findActivePackages();
        return list.stream().map(MemberPackageResponse::fromMemberPackage).toList();
    }
    
    /**
     * 根据用户ID获取可用的会员套餐（考虑购买次数限制）
     * @param userId 用户ID
     * @return 可用的会员套餐列表
     */
    public List<MemberPackageResponse> findAvailablePackagesByUserId(Long userId) {
        log.info("查询用户可用的会员套餐，用户ID: {}", userId);
        List<MemberPackage> allPackages = memberPackageMapper.findActivePackages();
        
        // 创建状态列表，包含正常(0)和过期(1)的状态
        List<Integer> validStatusList = Arrays.asList(UserMemberStatusEnum.NORMAL.getCode(), UserMemberStatusEnum.EXPIRED.getCode());
        // 统计用户购买该套餐的次数（状态为0或1），不包含已退款(2)的状态
        int purchaseCount = userMemberMapper.countUserPurchasesByStatus(userId, validStatusList);

        // 根据用户购买记录过滤套餐
        return allPackages.stream()
                .filter(pkg -> {
                    // 如果套餐没有设置最大购买次数限制，则始终可用
                    if (pkg.getMaxPurchaseCount() == null) {
                        return true;
                    }
                    
                    // 只有当购买次数小于限制次数时，才显示该套餐
                    // 比如新用户已经购买过了，那么就不能再显示新用户套餐了
                    return purchaseCount < pkg.getMaxPurchaseCount();
                })
                .map(MemberPackageResponse::fromMemberPackage)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据用户ID和类型获取可用的会员套餐（考虑购买次数限制）
     * @param userId 用户ID
     * @param type 套餐类型
     * @return 可用的会员套餐列表
     */
    public List<MemberPackageResponse> findAvailablePackagesByUserIdAndType(Long userId, Integer type) {
        log.info("查询用户可用的会员套餐，用户ID: {}，类型: {}", userId, type);
        List<MemberPackage> allPackages = memberPackageMapper.findActivePackagesByType(type);
        
        // 创建状态列表，包含正常(0)和过期(1)的状态
        List<Integer> validStatusList = Arrays.asList(UserMemberStatusEnum.NORMAL.getCode(), UserMemberStatusEnum.EXPIRED.getCode());
        // 统计用户购买该套餐的次数（状态为0或1）
        int purchaseCount = userMemberMapper.countUserPurchasesByStatus(userId, validStatusList);
        
        // 根据用户购买记录过滤套餐
        return allPackages.stream()
                .filter(pkg -> {
                    // 如果套餐没有设置最大购买次数限制，则始终可用
                    if (pkg.getMaxPurchaseCount() == null) {
                        return true;
                    }
                    
                    // 只有当购买次数小于限制次数时，才显示该套餐
                    // 比如新用户已经购买过了，那么就不能再显示新用户套餐了
                    return purchaseCount < pkg.getMaxPurchaseCount();
                })
                .map(MemberPackageResponse::fromMemberPackage)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberPackageResponse> findActivePackagesByType(Integer type) {
        log.info("查询激活的会员套餐，类型: {}", type);
        return memberPackageMapper.findActivePackagesByType(type).stream().map(MemberPackageResponse::fromMemberPackage).toList();
    }

    @Override
    public List<MemberPackageResponse> findAllPackages() {
        log.info("查询所有会员套餐");
        return memberPackageMapper.findAllPackages().stream().map(MemberPackageResponse::fromMemberPackage).toList();
    }

    @Override
    public int countActivePackagesByType(Integer type) {
        log.info("统计会员套餐数量，类型: {}", type);
        return memberPackageMapper.countActivePackagesByType(type);
    }
}