package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.Picture;
import cn.lizongyi.shareaccount.response.BillResponse;

import java.io.IOException;

/**
 * AI服务接口
 * @author lizongyi
 */
public interface AiService {


    /**
     * 识别用户发送的聊天内容，提取消费信息
     * @param chat 聊天内容
     * @param userId 用户ID
     * @return 识别结果，封装为Bill对象
     * @throws IOException 网络或IO异常
     */
    BillResponse recognizeBillChat(String chat, Long userId) throws IOException;
    
    /**
     * 识别消费截图
     * @param imageUrl 图片URL
     * @param userId 用户ID
     * @return 识别结果，封装为Bill对象
     * @throws IOException 网络或IO异常
     */
    BillResponse recognizeBillScreenshot(Picture picture, Long userId) throws IOException;
    
}