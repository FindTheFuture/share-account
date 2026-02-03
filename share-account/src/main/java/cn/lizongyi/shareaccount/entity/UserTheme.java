package cn.lizongyi.shareaccount.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class UserTheme {
    private Long id;
    private Long userId;
    /** 选中的皮肤ID（可为空，表示使用纯色） */
    private Long skinId;
    /** 选中的主色（例如 #2979ff） */
    private String primaryColor;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}