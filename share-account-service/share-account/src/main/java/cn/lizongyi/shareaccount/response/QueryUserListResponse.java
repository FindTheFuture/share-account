package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2025-03-03
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryUserListResponse {

    private List<UserResponse> userList;       // 用户列表
    private Long totalNum;          // 总数量
    private Integer pageTotalNum;        // 总页数
    private Integer currentPageNum;        // 当前页数
}
