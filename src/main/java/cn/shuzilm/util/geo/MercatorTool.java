package cn.shuzilm.util.geo;

/**
 * Created by thunders on 2017/2/16.
 */
public class MercatorTool {

    public static final int MAX_DISTANCE = 1000;
    static double M_PI = Math.PI;

    /**
     *  投影技术贴：
     *  http://blog.csdn.net/angelazy/article/details/44085099
     *  http://www.cnblogs.com/suyanteng/p/4895132.html
     * 经纬度转墨卡托
     * 经度(lon)，纬度(lat)
     * @param lon
     * @param lat
     * @return
     */
    public static double[] lonLat2Mercator(double lon,double lat)
    {
        double[] xy = new double[2];
        double x = lon *20037508.342789/180;
        double y = Math.log(Math.tan((90+lat)*M_PI/360))/(M_PI/180);
        y = y *20037508.34789/180;
        xy[0] = x;
        xy[1] = y;
        return xy;
    }
    /**
     *  墨卡托转经纬度
     */
    public static double[] Mercator2lonLat(double mercatorX,double mercatorY)
    {
        double[] xy = new double[2];
        double x = mercatorX/20037508.34*180;
        double y = mercatorY/20037508.34*180;
        y= 180/M_PI*(2*Math.atan(Math.exp(y*M_PI/180))-M_PI/2);
        xy[0] = x;
        xy[1] = y;
        return xy;
    }


    public static void main(String[] args)
    {
        double lng = 116.770097 ;
        double lat = 39.987825;

        double lng2 =  116.794747;//1km
        double lat2 = 39.995481;
        double[] mercat = lonLat2Mercator(lng,lat);
        System.out.println(String.valueOf(mercat[0])  + "\t" + String.valueOf(mercat[1]));

        double[] mercat2 = lonLat2Mercator(lng2,lat2);
        System.out.println(String.valueOf(mercat2[0])  + "\t" + String.valueOf(mercat2[1]));

    }

}
