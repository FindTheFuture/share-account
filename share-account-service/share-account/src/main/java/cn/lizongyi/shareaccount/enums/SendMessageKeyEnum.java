package cn.lizongyi.shareaccount.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-12-20
 * @description
 */

public enum SendMessageKeyEnum {
    // 1-99 任务提醒

    //100-199 活动提醒

    // 200-299 订单提醒


    // 300-399 统计提醒
    HOURLY_REPORT_REMINDER(301, "小时报提醒", "cBnRCyPpZbWDop4VdEMu3MQFDlat8_4jbxzciNdbUM8", true, 1),         // 小时报提醒，字段：记账金额{{amount3.DATA}}、记账数量{{number7.DATA}}、备注{{thing5.DATA}}
    DAILY_REPORT_REMINDER(302, "日报提醒", "XUUfM1XeirkivArOqaL_qn0ffXqPBaAHw72iywAwyvk", true, 1)   // 日报提醒，字段：昨日支出{{amount1.DATA}}、昨日收入{{amount2.DATA}}、当月支出{{amount3.DATA}}、当月收入{{amount4.DATA}}


    // 900-999 其他提醒
    ;
    private final int id;
    private final String name;
    private final String templateId;
    private final boolean sendMessage;      // 是否允许发送消息
    private final Integer pageType;    // 用户收到消息后，跳转到指定页面

    SendMessageKeyEnum(int id, String description, String templateId, boolean sendMessage, Integer pageType) {
        this.id = id;
        this.name = description;
        this.templateId = templateId;
        this.sendMessage = sendMessage;
        this.pageType = pageType;

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean getSendMessage() {
        return sendMessage;
    }

    public String getTemplateId() {
        return templateId;
    }

    public Integer getPageType() {
        return pageType;
    }

    public String getPage(){
        String homelistPage = "/pages/firstpage/firstpage";
        return homelistPage;
    }

    public static SendMessageKeyEnum fromId(int id) {
        for (SendMessageKeyEnum item : SendMessageKeyEnum.values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new IllegalArgumentException("SendMessageKeyEnum 根据id没找到发送消息的事件 id: " + id);
    }

    /**
     * 根据 类型 获取 订阅消息的模板列表 - 微信小程序要求每次最多返回3个订阅消息模板id
     */
    public static List<SendMessageKeyEnum> getListByType(int type){
        List<SendMessageKeyEnum> list = new ArrayList<>();

        // 1-99 任务提醒
        if(type == 1){  // 点击任务完成按钮触发
        }

        //100-199 活动提醒
        if(type == 100){

        }

        //101 抽奖提醒
        if(type == 101){
        }

        // 200-299 订单提醒
        if(type == 201){  // 点击去付款按钮触发
        }
        if(type == 202){  // 点击下单时候触发
        }
        if(type == 203){  // 点击退款时候触发
        }
        if(type == 204){  // 点击开始送水时候触发
        }
        if(type == 205){  // 点击取消送水时候触发
        }
        if(type == 207){  // 员工点击已送达保存按钮时候触发
        }
        if(type == 208){  // 员工点击未送达保存按钮时候触发
        }

        // 300-399 统计提醒
        if(type == 300){  // 统计提醒
            list.add(SendMessageKeyEnum.HOURLY_REPORT_REMINDER);
            list.add(SendMessageKeyEnum.DAILY_REPORT_REMINDER);
        }

        // 900-999 其他提醒
        if(type == 900){  // 其他提醒
        }

        // 只显示 允许显示的 模板id，并且去重了
        return list.stream().filter(s -> s.getSendMessage()).collect(Collectors.collectingAndThen(
                Collectors.toMap(
                        SendMessageKeyEnum::getTemplateId,
                        tm -> tm,
                        (existing, replacement) -> existing // 保留第一次出现的对象
                ),
                map -> new ArrayList<>(map.values())
        ));
    }

}
