package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.EnumResponse;
import cn.lizongyi.shareaccount.response.QueryFeatureListResponse;
import cn.lizongyi.shareaccount.services.ConfigService;
import cn.lizongyi.shareaccount.services.EnumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/base")
public class BaseController {

    @Autowired
    private EnumService enumService;

    @Autowired
    private ConfigService configService;

    @GetMapping("/getAllEnums")
    public ResponseEntity<ApiResponse<Map<String, List<EnumResponse>>>> getAllEnums() {

        try {
            Map<String, List<EnumResponse>> allEnums = enumService.getAllEnums();
            return ResponseEntity.ok(ApiResponse.success(allEnums));

        } catch (Exception e){
            return ResponseEntity.ok(ApiResponse.badRequest("查询配置失败"));
        }
    }



    /**
     * 根据类型 获取所有模板 - 订阅消息 列表
     */
    @GetMapping("/getMessagePermissions/{type}")
    public Map<String, List<EnumResponse>> getMessagePermissions(@PathVariable Integer type) {

        return enumService.getMessagePermissions(type);
    }


    @GetMapping("/getConfigByType/{type}")
    public ResponseEntity<ApiResponse<String>> getConfigByType(@PathVariable Integer type) {

        try {
            String value = configService.getConfigByType(type).getValue();
            return ResponseEntity.ok(ApiResponse.success(value));

        } catch (Exception e){
            return ResponseEntity.ok(ApiResponse.badRequest("查询配置失败"));
        }
    }



    @GetMapping("/getFeatureList")
    public ResponseEntity<ApiResponse<QueryFeatureListResponse>> getFeatureList() {

        try {
            QueryFeatureListResponse list = configService.getFeatureList();
            return ResponseEntity.ok(ApiResponse.success(list));

        } catch (Exception e){
            return ResponseEntity.ok(ApiResponse.badRequest("查询功能列表失败"));
        }
    }




}