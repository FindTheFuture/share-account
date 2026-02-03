package cn.lizongyi.shareaccount.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Skin {
    private Long id;
    private String name;
    /** 0-免费，1-会员 */
    private Integer type;
    /** 用于预览的主色（例如 #2979ff） */
    private String previewColor;
    /** JSON 字符串，保存CSS变量或其他主题配置 */
    private String variablesJson;
    /** 0-发布，1-下线 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}