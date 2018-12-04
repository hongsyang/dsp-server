package cn.shuzilm.util;

/**
 * Created by DENGJIAN on 2018/9/19.
 */
public class GPSDistance {

    static double DEF_PI180= Math.PI / 180; // PI/180.0
    static double DEF_R =6370693.5; // radius of earth

    public static void main(String [] args) {
        double a1 = 116.3556137084961;
        double a2 = 39.98174285888672;
        double b1 = 116.370448;
        double b2 = 39.989634;
        long start = System.currentTimeMillis();
        boolean isInArea = isInArea(a1,a2,b1,b2,2000);
       System.out.println(isInArea);
    }


   public static boolean isInArea (double lon1, double lat1, double lon2, double lat2, double radius){
       double distance = getDistance(lon1,lat1,lon2,lat2);
       //System.out.println(distance);
       if(distance < radius)
           return true;
       else return false;
   }

    public static double getDistance(double lon1, double lat1, double lon2, double lat2) {
        double ew1, ns1, ew2, ns2;
        double distance;
        // 角度转换为弧度
        ew1 = lon1 * DEF_PI180;
        ns1 = lat1 * DEF_PI180;
        ew2 = lon2 * DEF_PI180;
        ns2 = lat2 * DEF_PI180;
        // 求大圆劣弧与球心所夹的角(弧度)
        distance = Math.sin(ns1) * Math.sin(ns2) + Math.cos(ns1) * Math.cos(ns2) * Math.cos(ew1 - ew2);
        // 调整到[-1..1]范围内，避免溢出
        if (distance > 1.0)
            distance = 1.0;
        else if (distance < -1.0)
            distance = -1.0;
        // 求大圆劣弧长度
        distance = DEF_R * Math.acos(distance);
        return distance;
    }
}
