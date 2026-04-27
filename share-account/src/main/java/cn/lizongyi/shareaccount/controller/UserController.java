package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.enums.RoleTypeEnum;
import cn.lizongyi.shareaccount.request.CreateUserRequest;
import cn.lizongyi.shareaccount.request.QueryUserListRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.QueryUserListResponse;
import cn.lizongyi.shareaccount.response.UserResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.UserService;
import cn.lizongyi.shareaccount.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private BaseHandler baseHandler;

    @GetMapping("/getById/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long userId) {

        if (userId == null || userId <= 0) {
            userId = baseHandler.getUserId();
        }
        log.info("根据用户id:{} 查询用户信息", userId);

        if(userId == null || userId == 0L){
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }

        UserResponse user = userService.findResponseById(userId);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.badRequest("未找到用户信息"));
        }

        return ResponseEntity.ok(ApiResponse.success(user));
    }


    /**
     * 查询用户列表
     * @param request
     * @return
     */
    @PostMapping("/findUserList")
    public ResponseEntity<ApiResponse<QueryUserListResponse>> getByPhone(@RequestBody QueryUserListRequest request) {
        log.info("查询用户列表，请求参数：{}", JacksonUtils.objToStr(request));

        try {

            QueryUserListResponse response = userService.findUserList(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("查询用户失败: " + e.getMessage()));
        }
    }



    /**
     * 保存用户信息
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Boolean>> save(@RequestBody CreateUserRequest request) {
        log.info("保存用户信息");
        try {

            Boolean success = userService.save(request);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("保存用户信息失败: " + e.getMessage()));
        }
    }




    @GetMapping("/setParentUid/{parentUid}")
    public ResponseEntity<ApiResponse<Boolean>> setParentUid(@PathVariable Long parentUid) {

        Long userId = baseHandler.getUserId();

        if(userId == null || userId == 0L){
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        if(parentUid == null || parentUid == 0L){
            parentUid = 1234567897L;            // 默认是我的 uid
        }

        Boolean success = userService.setParentUid(userId, parentUid);
        log.info("给用户id:{}  设置parentUid：{}   结果：{}", userId, parentUid, success);
        return ResponseEntity.ok(ApiResponse.success(success));
    }


    @GetMapping("/setRole/{userId}/{role}")
    public ResponseEntity<ApiResponse<Boolean>> setRole(@PathVariable Long userId, @PathVariable Integer role) {

        Long opUserId = baseHandler.getUserId();

        if(opUserId == null || opUserId == 0L || userId == null || userId == 0L){
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        Integer opUserRole = baseHandler.getUserRole(opUserId);
        if(opUserRole == null || opUserRole == RoleTypeEnum.USER.getId()){
            return ResponseEntity.ok(ApiResponse.error("只有管理员才能操作"));
        }

        Boolean success = userService.setRole(userId, role);
        log.info("给用户id:{}  设置角色：{}   结果：{}", userId, role, success);
        return ResponseEntity.ok(ApiResponse.success(success));
    }

    @GetMapping("/findByPhone")
    public ResponseEntity<ApiResponse<UserResponse>> findByPhone(@RequestParam String phone) {
        log.info("根据手机号:{} 查询用户信息", phone);

        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("手机号不能为空"));
        }

        try {
            List<UserResponse> users = userService.findByPhone(phone.trim());
            if (users == null || users.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(null));
            }
            UserResponse user = users.get(0);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            log.error("查询用户失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询用户失败"));
        }
    }

    @GetMapping("/exists/{userId}")
    public ResponseEntity<Boolean> exists(@PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            return ResponseEntity.ok(false);
        }
        UserResponse user = userService.findResponseById(userId);
        return ResponseEntity.ok(user != null);
    }

    @GetMapping("/findLikePhone")
    public ResponseEntity<ApiResponse<List<UserResponse>>> findLikePhone(@RequestParam String phone) {
        log.info("模糊搜索手机号:{} 查询用户信息", phone);

        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("手机号不能为空"));
        }

        try {
            List<UserResponse> users = userService.findLikePhone(phone.trim());
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            log.error("模糊搜索用户失败", e);
            return ResponseEntity.ok(ApiResponse.error("搜索用户失败"));
        }
    }



    /*@GetMapping("/checkNewUser")
    public ResponseEntity<ApiResponse<Boolean>> checkNewUser() {

        Boolean success = userService.checkNewUser();
        return ResponseEntity.ok(ApiResponse.success(success));
    }*/



}