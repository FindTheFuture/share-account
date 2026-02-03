package cn.lizongyi.shareaccount.util;

/**
 * 简单HTML转义工具，防止XSS，同时保留空格和换行符。
 * 仅转义特殊字符：& < > \" '
 */
public class HtmlSanitizerUtil {
    public static String escape(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}