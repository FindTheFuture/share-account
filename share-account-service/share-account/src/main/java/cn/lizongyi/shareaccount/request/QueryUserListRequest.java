package cn.lizongyi.shareaccount.request;

import lombok.Data;

@Data
public class QueryUserListRequest {
    private String phone;
    private Integer pageNum = 1;    // 从1开始
    private Integer pageSize = 20;   // 每页20个
}
