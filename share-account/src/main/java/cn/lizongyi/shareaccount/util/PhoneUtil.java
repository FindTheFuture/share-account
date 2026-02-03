package cn.lizongyi.shareaccount.util;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-12-13
 * @description
 */

public class PhoneUtil {

    // 正则表达式匹配中国大陆的手机号码
    private static final String PHONE_NUMBER_REGEX = "^1[3-9]\\d{9}$";

    /**
     * 将手机号的中间4位替换为星号。
     *
     * @param phone 完整的手机号码
     * @return 格式化后的手机号码，如果输入无效则返回原字符串
     */
    public static String maskPhoneNumber(String phone) {
        // 检查是否为空或长度不是11位
        if (phone == null || phone.length() != 11) {
            return phone; // 返回原始输入
        }

        // 检查是否全是数字
        if (!phone.matches("\\d{11}")) {
            return phone; // 返回原始输入
        }

        // 构造新的手机号，中间4位替换为星号
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }


    /**
     * 验证手机号码是否符合标准格式。
     *
     * @param phoneNumber 待验证的手机号码
     * @return 如果手机号码格式正确返回 true，否则返回 false
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        return phoneNumber.matches(PHONE_NUMBER_REGEX);
    }


}
