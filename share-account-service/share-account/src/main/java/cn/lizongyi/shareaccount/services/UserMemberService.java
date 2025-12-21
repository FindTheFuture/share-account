package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.UserMember;
import com.github.pagehelper.PageInfo;
import cn.lizongyi.shareaccount.response.UserMemberResponse;

import java.util.List;

/**
 * 用户会员服务接口
 */
public interface UserMemberService {
    
    /**
     * 创建用户会员记录
     */
    Long createUserMember(UserMember userMember);
    
    /**
     * 更新用户会员状态
     */
    boolean updateUserMemberStatus(Long id, Integer status);
    
    /**
     * 根据支付记录ID更新状态
     */
    boolean updateStatusByPaymentRecordId(Long paymentRecordId, Integer status);
    
    /**
     * 增加AI使用次数
     */
    boolean incrementAiUsedCount(Long userId);
    
    /**
     * 增加PDF使用次数
     */
    boolean incrementPdfUsedCount(Long userId);
    
    /**
     * 查询用户会员记录（分页）
     */
    PageInfo<UserMember> findByUserIdWithPage(Long userId, Integer currentPage, Integer pageSize);
    
    /**
     * 查询用户正常状态且未过期的记录
     */
    List<UserMember> findNormalAndNotExpiredByUserId(Long userId);
    
    /**
     * 检查并更新过期会员
     */
    void checkAndUpdateExpiredMembers();
    
    /**
     * 检查并更新指定用户的过期会员记录
     * @param userId 用户ID
     */
    void checkAndUpdateUserMemberExpiry(Long userId);
    
    /**
     * 统计用户有效会员记录数量
     * @param userId 用户ID
     * @return 有效记录数量
     */
    int countValidMembersByUserId(Long userId);
    
    /**
     * 分页查询用户会员记录
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 用户会员记录列表
     */
    List<UserMember> findUserMembersByPage(Long userId, Integer currentPage, Integer pageSize);
    
    /**
     * 查询用户会员记录总数
     * @param userId 用户ID
     * @return 记录总数
     */
    Long countUserMembers(Long userId);
    
    /**
     * 获取用户AI识别剩余次数
     * @param userId 用户ID
     * @return 剩余AI识别次数
     */
    Integer getRemainingAiCount(Long userId);
    
    /**
     * 获取用户PDF导出剩余次数
     * @param userId 用户ID
     * @return 剩余PDF导出次数
     */
    Integer getRemainingPdfCount(Long userId);
    
    /**
     * 查询用户会员记录（分页）
     */
    PageInfo<UserMemberResponse> queryUserMembersWithPage(Long userId, Integer currentPage, Integer pageSize, Integer status);
    
    /**
     * 根据支付记录ID查询用户会员记录
     */
    UserMember findByPaymentRecordId(Long paymentRecordId);



    /**
     * 检查用户会员是否有效
     */
    boolean isUserMemberValid(Long userId);


    /**
     * 根据用户ID查询用户会员记录
     */
    List<UserMember> findUserMembersByUserId(Long userId);


    /**
     * 根据用户ID和状态查询用户会员记录
     */
    List<UserMember> findUserMembersByUserIdAndStatus(Long userId, Integer status);

    /**
     * 根据用户ID和套餐类型查询最新的有效会员记录
     */
    UserMember findLatestValidByUserIdAndType(Long userId, Integer packageType);
}