package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.UserMapper;
import cn.lizongyi.shareaccount.entity.Picture;
import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.enums.PictureTypeEnum;
import cn.lizongyi.shareaccount.enums.RoleTypeEnum;
import cn.lizongyi.shareaccount.request.CreateUserRequest;
import cn.lizongyi.shareaccount.request.QueryUserListRequest;
import cn.lizongyi.shareaccount.response.QueryUserListResponse;
import cn.lizongyi.shareaccount.response.UserResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.MemberLevelService;
import cn.lizongyi.shareaccount.services.PictureService;
import cn.lizongyi.shareaccount.services.UserMemberService;
import cn.lizongyi.shareaccount.services.UserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import cn.lizongyi.shareaccount.entity.MemberLevelConfig;



@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PictureService pictureService;

    @Autowired
    private BaseHandler baseHandler;
    
    @Autowired
    private MemberLevelService memberLevelService;
    
    @Autowired
    private UserMemberService userMemberService;
    
    // 创建线程池用于异步操作
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    // 防抖时间（秒）
    private static final long DEBOUNCE_TIME = 5;
    
    // 用于存储用户最后检查时间的Map
    private final Map<Long, Long> lastCheckTimeMap = new HashMap<>();

    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    public User findById(Long id) {
        return userMapper.findById(id);
    }


    /**
     * 查询用户信息
     * @param id
     * @return
     */
    @Override
    public UserResponse findResponseById(Long id) {
        User user = findById(id);
        if (user == null) {
            log.info("用户null");
            return null;
        }

        // 异步检查用户会员记录是否过期（添加防抖机制）
        asyncCheckUserMemberExpiry(id);

        Integer role = baseHandler.getUserRole(baseHandler.getUserId());
        boolean isAdmin = role == RoleTypeEnum.ADMIN.getId() || baseHandler.getUserId() == user.getId();
        UserResponse userResponse = UserResponse.fromUser(user, isAdmin ? RoleTypeEnum.ADMIN.getId() : RoleTypeEnum.USER.getId());
        fillPicture(userResponse);
        fillUserMemberLevel(userResponse);

        return userResponse;
    }
    
    /**
     * 异步检查用户会员记录是否过期，并修改状态字段
     * @param userId 用户ID
     */
    private void asyncCheckUserMemberExpiry(Long userId) {
        long currentTime = System.currentTimeMillis() / 1000; // 当前时间（秒）
        
        // 检查防抖条件
        synchronized (lastCheckTimeMap) {
            Long lastCheckTime = lastCheckTimeMap.get(userId);
            if (lastCheckTime != null && (currentTime - lastCheckTime) < DEBOUNCE_TIME) {
                // 在防抖时间内，不执行检查
                return;
            }
            // 更新最后检查时间
            lastCheckTimeMap.put(userId, currentTime);
        }
        
        // 异步执行检查
        executorService.submit(() -> {
            try {
                // 检查并更新用户会员过期状态
                userMemberService.checkAndUpdateUserMemberExpiry(userId);
                
                // 检查用户是否还有有效会员记录
                int validCount = userMemberService.countValidMembersByUserId(userId);
                if (validCount == 0) {
                    userMapper.updateMemberStatus(userId, 0);
                    log.info("用户 {} 的所有会员记录均已过期，更新用户状态为非会员", userId);
                }
            } catch (Exception e) {
                log.error("异步检查用户会员过期状态失败，用户ID: {}", userId, e);
            }
        });
    }


    @Override
    public User findByOpenid(String openid) {
        return userMapper.findByOpenid(openid);
    }

    @Override
    public UserResponse findResponseByOpenid(String openid, Integer queryFamily) {
        User user = findByOpenid(openid);
        if (user == null) {
            log.info("用户null");
            return null;
        }
        return UserResponse.fromUser(user);
    }


    @Override
    public List<User> findByPhone(String phone){
        return userMapper.findByPhone(phone);
    }

    /**
     *  模糊查询
     */
    @Override
    public List<User> findLikePhone(String phone) {
        return userMapper.findLikePhone(phone);
    }


    /**
     * 查询用户列表
     * @param request
     * @return
     */
    @Override
    public QueryUserListResponse findUserList(QueryUserListRequest request) {

        Integer userRole = baseHandler.getUserRole(baseHandler.getUserId());
        if(userRole != RoleTypeEnum.ADMIN.getId()){
            log.info("非管理员不能查询用户列表");
            return null;
        }

        QueryUserListResponse response = new QueryUserListResponse();

        // 分页查询用户列表
        PageInfo<User> pageInfo = null;
        if(org.apache.commons.lang3.StringUtils.isBlank(request.getPhone())){
            pageInfo = PageHelper.startPage(request.getPageNum(), request.getPageSize()).doSelectPageInfo(() -> userMapper.findUserList());
        }else{
            pageInfo = PageHelper.startPage(request.getPageNum(), request.getPageSize()).doSelectPageInfo(() -> userMapper.findLikePhone(request.getPhone()));
        }
        List<User> userList = pageInfo.getList();
        if (!CollectionUtils.isEmpty(userList)) {
            response.setUserList(userList.stream().map(user -> UserResponse.fromUser(user, RoleTypeEnum.ADMIN.getId())).collect(Collectors.toList()));
        }
        response.setTotalNum(pageInfo.getTotal());
        response.setPageTotalNum(pageInfo.getPages());
        response.setCurrentPageNum(pageInfo.getPageNum());

        return response;
    }


    /**
     *  保存用户信息
     */
    @Override
    public Boolean save(CreateUserRequest request) {
        Long userId = request.getId();
        if(userId == null){
            return false;
        }

        User user = findById(userId);
        if(user == null){
            log.info("用户null");
            return false;
        }

        user.setNickName(request.getNickName() == null ? user.getNickName() : request.getNickName());
        user.setPhone(request.getPhone() == null ? user.getPhone() : request.getPhone());
        user.setPassword(request.getPassword() == null ? user.getPassword() : request.getPassword());
        user.setSex(request.getSex() == null ? user.getSex() : request.getSex());
        user.setRole(request.getRole() == null ? user.getRole() : request.getRole());
        user.setCanSendMessage(request.getCanSendMessage() == null ? user.getCanSendMessage() : request.getCanSendMessage());
        user.setNotityBill(request.getNotityBill() == null ? user.getNotityBill() : request.getNotityBill());
        boolean success = userMapper.update(user) > 0;
        log.info("更新用户信息结果:{}", success);
        return success;
    }


    @Override
    public Boolean setParentUid(Long userId, Long parentUid) {
        User user = userMapper.findById(userId);
        if(user == null || user.getId() == parentUid){
            return false;
        }
        // 游客限制：不可进行家庭关联等操作
        if (baseHandler.isGuestUser(userId)) {
            log.info("游客账号不可设置parentUid: userId={}", userId);
            return false;
        }

        boolean success = userMapper.updateParentUid(userId, parentUid) > 0;
        return success;
    }

    /**
     *  设置用户角色
     */
    @Override
    public Boolean setRole(Long userId, Integer role) {
        // 游客限制：游客账号不可修改角色
        if (baseHandler.isGuestUser(userId)) {
            log.info("游客账号不可修改角色: userId={}", userId);
            return false;
        }
        return userMapper.updateRole(userId, role) > 0;
    }
    
    @Override
    public boolean activateMember(Long userId) {
        try {
            User user = findById(userId);
            if (user == null) {
                return false;
            }
            return updateMemberStatus(userId, 1);
        } catch (Exception e) {
            log.error("激活会员失败: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }

    public boolean updateMemberStatus(Long userId, Integer status) {
        try {
            int result = userMapper.updateMemberStatus(userId, status);
            if (result > 0) {
                log.info("更新会员状态成功: userId={}, status={}", userId, status);
            }
            return result > 0;
        } catch (Exception e) {
            log.error("更新会员状态失败: userId={}, status={}, error={}", userId, status, e.getMessage());
            return false;
        }
    }


    /**
     *  填充用户头像
     */
    private void fillPicture(UserResponse user){
        if(user == null){
            return;
        }
        List<Picture> pictureList = pictureService.findByObjectId(user.getHaoe(), PictureTypeEnum.AVATAR_USER.getId());
        if(CollectionUtils.isEmpty(pictureList)){
            log.info("没有查询到图片");
            user.setPictureAddress(baseHandler.getDefaultUserAvatar(null));
            return;
        }
        pictureList = pictureList.stream().sorted(Comparator.comparing(Picture::getCreateTime).reversed()).collect(Collectors.toList());

        Picture picture = pictureList.get(0);
        if(picture.getStatus() == 1){
            log.info("图片状态不正确");
            return;
        }
        baseHandler.fillPicPresignUrl(picture);
        user.setPictureAddress(picture.getAddress());
    }


    private void fillUserMemberLevel(UserResponse user){
        if(user == null){
            return;
        }
        MemberLevelConfig levelConfig = memberLevelService.findMaxLevelByPoints(user.getValidIntegral());
        if(levelConfig == null){
            log.info("没有查询到MemberLevelConfig记录");
            return;
        }
        user.setMemberLevel(levelConfig.getLevel());
        user.setMemberLevelIcon(levelConfig.getIcon());
    }
}