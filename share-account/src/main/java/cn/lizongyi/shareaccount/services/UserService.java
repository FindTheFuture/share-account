package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.request.CreateUserRequest;
import cn.lizongyi.shareaccount.request.QueryUserListRequest;
import cn.lizongyi.shareaccount.response.QueryUserListResponse;
import cn.lizongyi.shareaccount.response.UserResponse;

import java.util.List;

public interface UserService {
    List<User> findAll();
    User findById(Long id);
    UserResponse findResponseById(Long id);
    User findByOpenid(String openid);
    UserResponse findResponseByOpenid(String openid, Integer queryFamily);
    List<UserResponse> findByPhone(String phone);
    List<UserResponse> findLikePhone(String phone);
    QueryUserListResponse findUserList(QueryUserListRequest request);
    Boolean save(CreateUserRequest request);
    Boolean setParentUid(Long userId, Long parentUid);
    Boolean setRole(Long userId, Integer role);
    boolean activateMember(Long userId);
}