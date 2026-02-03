package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.response.MemberPackageResponse;

import java.util.List;

public interface MemberPackageService {

    /**
     * 根据ID查询会员套餐
     */
    MemberPackageResponse findById(Long id);

    /**
     * 查询所有激活的会员套餐
     */
    List<MemberPackageResponse> findActivePackages();

    /**
     * 根据类型查询激活的会员套餐
     */
    List<MemberPackageResponse> findActivePackagesByType(Integer type);
    
    /**
     * 根据用户ID获取可用的会员套餐（考虑购买次数限制）
     * @param userId 用户ID
     * @return 可用的会员套餐列表
     */
    List<MemberPackageResponse> findAvailablePackagesByUserId(Long userId);
    
    /**
     * 根据用户ID和类型获取可用的会员套餐（考虑购买次数限制）
     * @param userId 用户ID
     * @param type 套餐类型
     * @return 可用的会员套餐列表
     */
    List<MemberPackageResponse> findAvailablePackagesByUserIdAndType(Long userId, Integer type);

    /**
     * 查询所有会员套餐
     */
    List<MemberPackageResponse> findAllPackages();

    /**
     * 统计指定类型的激活套餐数量
     */
    int countActivePackagesByType(Integer type);
}