package cn.lizongyi.shareaccount.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class SmsCode {
    private Long id;
    private String phone;
    private String code;
    private String type;
    private Integer status;
    private LocalDateTime expireTime;
    private Integer errorCount;
    private LocalDateTime lockedUntil;
    private LocalDateTime createTime;
    private String ipAddress;

    public enum Status {
        UNUSED(0),  // 未使用
        USED(1),    // 已使用
        EXPIRED(2);     // 已过期

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}