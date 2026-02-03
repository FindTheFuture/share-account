package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.util.IconfontSyncTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 工具类控制器：提供后端运维/工具触发接口
 */
@Slf4j
@RestController
@RequestMapping("/tool")
public class ToolController {

    @Resource
    private IconfontSyncTool iconfontSyncTool;

    /**
     * 同步 iconfont.json 到数据库 class.icon 字段
     * 调用示例：
     * POST /tool/syncIconfont?path=/Users/lizongyi/IdeaProjects/share-account-service/share-account-app/static/fonts/iconfont.json
     *
     * @param path iconfont.json 的绝对路径（建议传绝对路径）
     */
    @GetMapping("/syncIconfont")
    public ResponseEntity<ApiResponse<Integer>> syncIconfont(@RequestParam String path) {
        try {
            int updated = iconfontSyncTool.syncIconToClassTable(path);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (Exception e) {
            log.error("同步 iconfont 失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.badRequest("同步失败: " + e.getMessage()));
        }
    }
}