package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.CreateWxadRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.WxadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/wxad")
public class WxadController {

    /*@Autowired
    private WxadService wxadService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Boolean>> save(@RequestBody CreateWxadRequest request) {
        log.info("保存广告加载记录");
        try {
            Boolean success = wxadService.save(request);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("保存广告加载记录失败: " + e.getMessage()));
        }
    }

    @GetMapping("/findById/{id}")
    public ResponseEntity<ApiResponse<WxadResponse>> findById(@PathVariable Long id) {
        log.info("根据ID查询广告加载记录");
        WxadResponse response = wxadService.findById(id);
        if (response == null) {
            return ResponseEntity.ok(ApiResponse.badRequest("未找到广告加载记录"));
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<WxadResponse>>> findAll() {
        log.info("查询所有广告加载记录");
        List<WxadResponse> responses = wxadService.findAll();
        if (responses == null || responses.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.badRequest("未找到广告加载记录"));
        }
        return ResponseEntity.ok(ApiResponse.success(responses));
    }*/
}