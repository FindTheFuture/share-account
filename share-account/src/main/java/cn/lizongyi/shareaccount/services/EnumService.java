package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.enums.CanSendMessageEnum;
import cn.lizongyi.shareaccount.enums.RoleTypeEnum;
import cn.lizongyi.shareaccount.enums.SendMessageKeyEnum;
import cn.lizongyi.shareaccount.enums.SexEnum;
import cn.lizongyi.shareaccount.response.EnumResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnumService {

    public Map<String, List<EnumResponse>> getAllEnums() {
        Map<String, List<EnumResponse>> enumMap = new HashMap();

        // 性别
        List<EnumResponse> sexEnumList = new ArrayList<>();
        for (SexEnum item : SexEnum.values()) {
            EnumResponse response = new EnumResponse();
            response.setId(item.getId());
            response.setDescription(item.getName());
            sexEnumList.add(response);
        }
        enumMap.put("sexEnum", sexEnumList);

        // 角色
        List<EnumResponse> roleEnumList = new ArrayList<>();
        for (RoleTypeEnum item : RoleTypeEnum.values()) {
            EnumResponse response = new EnumResponse();
            response.setId(item.getId());
            response.setDescription(item.getName());
            roleEnumList.add(response);
        }
        enumMap.put("roleEnum", roleEnumList);

        // 角色
        List<EnumResponse> canSendMessageEnumList = new ArrayList<>();
        for (CanSendMessageEnum item : CanSendMessageEnum.values()) {
            EnumResponse response = new EnumResponse();
            response.setId(item.getId());
            response.setDescription(item.getName());
            canSendMessageEnumList.add(response);
        }
        enumMap.put("canSendMessageEnum", canSendMessageEnumList);

        return enumMap;
    }


    /**
     * 根据类型 获取所有模板 - 订阅消息 列表
     */
    public Map<String, List<EnumResponse>> getMessagePermissions(Integer type){
        Map<String, List<EnumResponse>> enumMap = new HashMap();

        List<EnumResponse> itemList = new ArrayList<>();
        for (SendMessageKeyEnum item : SendMessageKeyEnum.getListByType(type)) {
            EnumResponse response = new EnumResponse();
            response.setId(item.getId());
            response.setDescription(item.getTemplateId());
            itemList.add(response);
        }
        enumMap.put("template", itemList);

        return enumMap;
    }

}