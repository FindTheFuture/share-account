package cn.lizongyi.shareaccount.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class FreeColor {
    private Long id;
    private String name;
    /** 颜色十六进制值，例如 #FFFFFF */
    private String hex;
    /** 0-启用，1-禁用 */
    private Integer status;
    /** 排序字段，越小越靠前 */
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}