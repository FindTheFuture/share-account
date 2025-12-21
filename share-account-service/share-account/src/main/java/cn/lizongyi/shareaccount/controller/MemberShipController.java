package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.entity.MemberLevelConfig;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.MemberPackageResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.MemberPackageService;
import cn.lizongyi.shareaccount.services.MemberLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membership")
@Slf4j
public class MemberShipController {

    @Autowired
    private MemberPackageService memberPackageService;

    @Autowired
    private MemberLevelService memberLevelService;

    @Autowired
    private BaseHandler baseHandler;

    /**
     * 获取所有可用的会员套餐（考虑用户购买次数限制）
     */
    @GetMapping("/packages")
    public ResponseEntity<ApiResponse<List<MemberPackageResponse>>> getActivePackages() {
        log.info("获取活跃会员套餐请求");
        try {
            Long userId = baseHandler.getUserId(); // 获取当前登录用户ID
            if (userId == null || userId <= 0) {
                return ResponseEntity.ok(ApiResponse.error("请先登录"));
            }
            
            List<MemberPackageResponse> packages = memberPackageService.findAvailablePackagesByUserId(userId);
            
            return ResponseEntity.ok(ApiResponse.success(packages));
        } catch (Exception e) {
            log.error("获取会员套餐失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取会员套餐失败: " + e.getMessage()));
        }
    }

    /**
     * 根据类型获取会员套餐（考虑用户购买次数限制）
     */
    @GetMapping("/packages/type/{packageType}")
    public ResponseEntity<ApiResponse<List<MemberPackageResponse>>> getPackagesByType(@PathVariable Integer packageType) {
        log.info("根据类型获取会员套餐请求: type={}", packageType);
        try {
            if (packageType == null || packageType < 0) {
                return ResponseEntity.ok(ApiResponse.error("参数不正确"));
            }
            
            Long userId = baseHandler.getUserId();
            if (userId == null || userId <= 0) {
                return ResponseEntity.ok(ApiResponse.error("请先登录"));
            }

            List<MemberPackageResponse> packages = memberPackageService.findAvailablePackagesByUserIdAndType(userId, packageType);
            
            return ResponseEntity.ok(ApiResponse.success(packages));
        } catch (Exception e) {
            log.error("根据类型获取会员套餐失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取会员套餐失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有会员等级配置
     */
    @GetMapping("/levels")
    public ResponseEntity<ApiResponse<List<MemberLevelConfig>>> getAllLevels() {
        log.info("获取所有会员等级请求");
        try {
            Long userId = baseHandler.getUserId();
            if (userId == null || userId <= 0) {
                return ResponseEntity.ok(ApiResponse.error("请先登录"));
            }

            List<MemberLevelConfig> levels = memberLevelService.findAllLevels();
            return ResponseEntity.ok(ApiResponse.success(levels));
        } catch (Exception e) {
            log.error("获取会员等级失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取会员等级失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户当前会员等级信息
     */
    @GetMapping("/user/level")
    public ResponseEntity<ApiResponse<MemberLevelConfig>> getUserCurrentLevel() {
        log.info("获取用户当前会员等级请求");
        try {
            Long userId = baseHandler.getUserId();
            if (userId == null || userId <= 0) {
                return ResponseEntity.ok(ApiResponse.error("请先登录"));
            }
            MemberLevelConfig levelInfo = memberLevelService.getUserCurrentLevel(userId);
            return ResponseEntity.ok(ApiResponse.success(levelInfo));
        } catch (Exception e) {
            log.error("获取用户会员等级失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取用户会员等级失败: " + e.getMessage()));
        }
    }

}