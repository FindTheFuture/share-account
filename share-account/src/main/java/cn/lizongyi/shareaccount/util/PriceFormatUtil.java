package cn.lizongyi.shareaccount.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-10-24
 * @description
 */

public class PriceFormatUtil {

    /**
     * 将分转成元
     * @author lizongyi@itbox.cn
     * @date 2024/10/24
     * @return
     */
    public static String formatPriceLong(Long price) {
        if(price == null || price == 0L){
            return "0";
        }
        return String.format("%.2f", price / 100.0);
    }

    public static String formatPriceInteger(Integer price) {
        if(price == null || price == 0L){
            return "0";
        }
        return String.format("%.2f", price / 100.0);
    }

    public static BigDecimal formatPriceToBigDecimal(Long price){
        return new BigDecimal(price).divide(new BigDecimal(100));
    }

    public static Long StringToLong(String amountStr) {
        try {
            // 将字符串转换为 BigDecimal
            BigDecimal amount = new BigDecimal(amountStr);

            // 保留两位小数，使用四舍五入
            BigDecimal roundedAmount = amount.setScale(2, RoundingMode.HALF_UP);

            // 将小数点向右移动四位
            BigDecimal scaledAmount = roundedAmount.movePointRight(2);

            // 转换为 Long 类型
            return scaledAmount.longValue();
        } catch (NumberFormatException e) {
            // 处理格式错误的情况
            System.err.println("Invalid amount format: " + amountStr);
            return null;
        }
    }

    public static String formatPriceDouble(Double price) {
        return String.format("%.2f", price);
    }


    /*public static String formatPriceString(String price)
    {
        return formatPrice(Long.parseLong(price));
    }


    public static String formatPriceInt(int price)
    {
        return formatPrice((long) price);
    }


    public static String formatPriceFloat(float price)
    {
        return formatPrice((double) price);
    }

    public static String formatPrice(double price)
    {
        return formatPrice(Double.toString(price));
    }*/

    public static String formatPrice(String price, String format)
    {
        return String.format(format, price);
    }


    public static String formatPrice(Long price, String format)
    {
        return formatPrice(price.toString(), format);
    }


    public static String formatPrice(Double price, String format)
    {
        return formatPrice(price.toString(), format);
    }




    public static String formatPrice(int price, String format)
    {
        return formatPrice(price + "", format);
    }

    public static String formatPrice(float price, String format)
    {
        return formatPrice(price + "", format);
    }


    public static String formatPrice(double price, String format)
    {
        return formatPrice(price + "", format);
    }


    public static String formatPrice(String price, int format)
    {
        return formatPrice(price, "%." + format + "f");
    }

    public static String formatPrice(Long price, int format)
    {
        return formatPrice(price.toString(), format);
    }

    public static String formatPrice(Double price, int format)
    {
        return formatPrice(price.toString(), format);
    }

    public static String formatPrice(int price, int format)
    {
        return formatPrice(price + "", format);
    }


    public static String formatPrice(float price, int format)
    {
        return formatPrice(price + "", format);
    }


    public static String formatPrice(double price, int format)
    {
        return formatPrice(price + "", format);
    }


    public static String formatPrice(String price, String format, int formatLength)
    {
        return formatPrice(price, format + "%0" + formatLength + "d");
    }

    public static String formatPrice(Long price, String format, int formatLength)
    {
        return formatPrice(price.toString(), format, formatLength);
    }


    public static String formatPrice(Double price, String format, int formatLength)
    {
        return formatPrice(price.toString(), format, formatLength);
    }


    public static String formatPrice(int price, String format, int formatLength)
    {
        return formatPrice(price + "", format, formatLength);
    }


    public static String formatPrice(float price, String format, int formatLength)
    {
        return formatPrice(price + "", format, formatLength);
    }


     public static String formatPrice(double price, String format, int formatLength)
     {
         return formatPrice(price + "", format, formatLength);
     }

     public static String formatPrice(String price, String format, String formatLength)
     {
         return formatPrice(price, format + "%0" + formatLength + "d");
     }


     public static String formatPrice(Long price, String format, String formatLength)
     {
         return formatPrice(price.toString(), format, formatLength);
     }


     public static String formatPrice(Double price, String format, String formatLength)
     {
         return formatPrice(price.toString(), format, formatLength);
     }


     public static String formatPrice(int price, String format, String formatLength)
     {
         return formatPrice(price + "", format, formatLength);
     }


     public static String formatPrice(float price, String format, String formatLength)
     {
         return formatPrice(price + "", format, formatLength);
     }




}
