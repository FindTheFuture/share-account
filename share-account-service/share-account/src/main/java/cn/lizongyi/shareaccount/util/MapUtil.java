package cn.lizongyi.shareaccount.util;


import cn.lizongyi.shareaccount.constants.NumConstant;

/**
 * 地图测算工具类
 * 
 * @author justin
 * @date 2021/4/9 18:50
 */
public class MapUtil {

    /**
     * 角度弧度计算公式 rad:(). <br/>
     * <p>
     * 360度=2π π=Math.PI
     * <p>
     * x度 = x*π/360 弧度
     *
     * @param degree 角度
     * @return 弧度
     */
    private static double getRadian(double degree) {
        return degree * Math.PI / NumConstant.INT_180;
    }

    /**
     * 依据经纬度计算两点之间的距离 GetDistance:(). <br/>
     *
     * @param lat1 1点的纬度
     * @param lng1 1点的经度
     * @param lat2 2点的纬度
     * @param lng2 2点的经度
     * @return 距离 单位 m
     */
    public static Double getDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return Double.MAX_VALUE;
        }
        double radLat1 = getRadian(lat1);
        double radLat2 = getRadian(lat2);
        double a = radLat1 - radLat2;
        double b = getRadian(lng1) - getRadian(lng2);
        double s = NumConstant.INT_2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / NumConstant.INT_2), NumConstant.INT_2)
            + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / NumConstant.INT_2), NumConstant.INT_2)));
        s = s * NumConstant.EARTH_RADIUS;
        return s;
    }
}