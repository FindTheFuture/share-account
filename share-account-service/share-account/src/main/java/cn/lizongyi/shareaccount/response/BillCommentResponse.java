package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class BillCommentResponse {
    // 总条数（分页总数）
    private Long total = 0L;
    // 当前页码（当pageNum<=0时，后端会返回最后一页页码）
    private Integer pageNum = 1;
    // 每页大小
    private Integer pageSize = 20;
    // 总页数（由后端计算并返回）
    private Integer totalPages = 0;
    // 当type=1时有效，key为用户ID，value为用户头像URL
    private Map<Long, String> avatarUrlMap = new HashMap<>();

    // 评论列表
    private List<BillCommentResponseItem> itemList = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class BillCommentResponseItem {
        private Long id;
        private Long billId;
        private Long userId;
        private String userName;
        private Integer type;      // 0-文本，1-图片
        private String content;    // 当type=0时有效
        private String imageUrl;   // 当type=1时有效，图片URL（后端已生成临时访问地址）
        private String createTime; // yyyy-MM-dd HH:mm:ss
    }
}