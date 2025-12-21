package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.entity.Picture;
import cn.lizongyi.shareaccount.request.CreatePictureRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.services.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pictures")
public class PictureController {
    @Autowired
    private PictureService pictureService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Picture>>> findAll() {
        List<Picture> pictures = pictureService.findAll();
        return ResponseEntity.ok(ApiResponse.success(pictures));
    }

    @GetMapping("/findById/{id}")
    public ResponseEntity<ApiResponse<Picture>> findById(@PathVariable Long id) {
        Picture picture = pictureService.findById(id);
        if (picture == null) {
            return ResponseEntity.ok(ApiResponse.badRequest("未找到图片"));
        }
        return ResponseEntity.ok(ApiResponse.success(picture));
    }


    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Boolean>> save(@RequestBody CreatePictureRequest request) {
        try {
            Picture picture = new Picture();
            picture.setId(request.getId());
            picture.setName(request.getName());
            picture.setPath(request.getPath());
            picture.setType(request.getType());
            picture.setStatus(0);
            picture.setAddress(request.getAddress());

            Boolean success = pictureService.save(picture);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("保存图片失败: " + e.getMessage()));
        }
    }


    @GetMapping("/updateStatus/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateStatus(@PathVariable Long id) {
        Boolean success = pictureService.updateStatus(id);
        return ResponseEntity.ok(ApiResponse.success(success));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        try {
            pictureService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("删除图片失败: " + e.getMessage()));
        }
    }
}