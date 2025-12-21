package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.request.CreateBillCommentRequest;
import cn.lizongyi.shareaccount.response.BillCommentResponse;

public interface BillCommentService {
    BillCommentResponse.BillCommentResponseItem getById(Long commentId);
    BillCommentResponse listByBillId(Long billId, Integer pageNum, Integer pageSize);
    Boolean save(CreateBillCommentRequest request);
    Boolean deleteById(Long commentId);
}