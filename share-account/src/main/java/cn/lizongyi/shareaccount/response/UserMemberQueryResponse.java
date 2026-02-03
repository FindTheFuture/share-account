package cn.lizongyi.shareaccount.response;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户会员查询响应数据
 */
@Data
@AllArgsConstructor
public class UserMemberQueryResponse {
    private PageInfo<UserMemberResponse> pageInfo;
    private Integer remainingAiCount;
    private Integer remainingPdfCount;
}