package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.request.DouyinPayCreateOrderRequest;
import cn.lizongyi.shareaccount.response.DouyinPayCreateOrderResponse;
import cn.lizongyi.shareaccount.response.DouyinPayNotifyResponse;

public interface DouyinPayService {

    DouyinPayCreateOrderResponse createOrder(DouyinPayCreateOrderRequest request) throws Exception;

    Boolean queryOrderStatus(Long paymentId) throws Exception;

    Boolean refund(Long paymentId) throws Exception;

    DouyinPayNotifyResponse processNotify(String notifyData) throws Exception;
}
