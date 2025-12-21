package cn.lizongyi.shareaccount.services;

import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface WeChatPayServiceV3 {
    PrepayWithRequestPaymentResponse createPrepayId(Long orderId) throws Exception;
    Boolean queryOrderStatus(Long orderId) throws Exception;
    Boolean refund(Long orderId) throws Exception;
    Boolean processNotify(HttpServletRequest request) throws Exception;
}