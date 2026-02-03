package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.CreateClassRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.ClassResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.ClassEntityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/class")
public class ClassController {

    @Resource
    private ClassEntityService classService;

    @Resource
    private BaseHandler baseHandler;

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<ClassResponse>> getById(@PathVariable Long id) {

        try {
            ClassResponse classResponse = classService.selectById(id);
            return ResponseEntity.ok(ApiResponse.success(classResponse));
        } catch (Exception e) {
            log.error("查询分类失败", e);
            return ResponseEntity.ok(ApiResponse.badRequest("查询分类失败"));
        }
    }


    @GetMapping("/getByParentId/{parentId}")
    public ResponseEntity<ApiResponse<List<ClassResponse>>> getByParentId(@PathVariable Long parentId) {

        try {
            List<ClassResponse> list = classService.selectByParentId(parentId);
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            log.error("查询分类失败", e);
            return ResponseEntity.ok(ApiResponse.badRequest("查询分类失败"));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<ClassResponse>>> getAll() {
        try {
            List<ClassResponse> list = classService.selectByUserId(baseHandler.getUserId());
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            log.error("查询分类失败", e);
            return ResponseEntity.ok(ApiResponse.badRequest("查询分类失败"));
        }
    }


    @GetMapping("/getByStatus/{status}")
    public ResponseEntity<ApiResponse<List<ClassResponse>>> getByStatus(@PathVariable Integer status) {
        try {
            List<ClassResponse> list = classService.selectByStatus(status);
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            log.error("查询分类失败", e);
            return ResponseEntity.ok(ApiResponse.badRequest("查询分类失败"));
        }
    }


    @GetMapping("/updateStatus/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateStatus(@PathVariable Long id) {
        try {
            Boolean success = classService.updateStatus(id);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            log.error("更新分类失败", e);
            return ResponseEntity.ok(ApiResponse.badRequest("更新分类失败"));
        }
    }



    /**
     * 保存分类
     * @param request 分类请求对象
     * @return 保存结果
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Boolean>> save(@RequestBody CreateClassRequest request) {

        try {
            Boolean success = classService.save(request);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            log.error("保存分类失败", e);
            return ResponseEntity.ok(ApiResponse.badRequest("保存分类失败"));
        }
    }





}
