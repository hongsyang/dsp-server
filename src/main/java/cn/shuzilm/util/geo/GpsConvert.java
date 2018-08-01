package cn.shuzilm.util.geo;

import cn.shuzilm.bean.dmp.GpsBean;

/**
 * 各地图API坐标系统比较与转换;
 * WGS84坐标系：即地球坐标系，国际上通用的坐标系。设备一般包含GPS芯片或者北斗芯片获取的经纬度为WGS84地理坐标系,谷歌地图采用的是WGS84地理坐标系（中国范围除外）;
 * GCJ02坐标系：即火星坐标系，是由中国国家测绘局制订的地理信息系统的坐标系统。由WGS84坐标系经加密后的坐标系。
 * 谷歌中国地图和搜搜中国地图采用的是GCJ02地理坐标系;
 * BD09坐标系：即百度坐标系，GCJ02坐标系经加密后的坐标系;
 * 搜狗坐标系、图吧坐标系等，估计也是在GCJ02基础上加密而成的。
 */
public class GpsConvert {

    public static final String BAIDU_LBS_TYPE = "bd09ll";

    public static double pi = 3.1415926535897932384626;
    public static double a = 6378245.0;
    public static double ee = 0.00669342162296594323;

    /**
     * 84 to 火星坐标系 (GCJ-02) World Geodetic System ==> Mars Geodetic System
     *
     * @param lat
     * @param lon
     * @return
     */
    public static GpsBean gps84_To_Gcj02(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return null;
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new GpsBean(mgLat, mgLon);
    }

    /**
     * * 火星坐标系 (GCJ-02) to 84 * * @param lon * @param lat * @return
     * */
    public static GpsBean gcj_To_Gps84(double lat, double lon) {
        GpsBean gps = transform(lat, lon);
        double lontitude = lon * 2 - gps.getLng();
        double latitude = lat * 2 - gps.getLat();
        return new GpsBean(latitude, lontitude);
    }

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
     *
     * @param gg_lat
     * @param gg_lon
     */
    public static GpsBean gcj02_To_Bd09(double gg_lat, double gg_lon) {
        double x = gg_lon, y = gg_lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * pi);
        double bd_lon = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new GpsBean(bd_lat, bd_lon);
    }

    /**
     * * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 * * 将 BD-09 坐标转换成GCJ-02 坐标 * * @param
     * bd_lat * @param bd_lon * @return
     */
    public static GpsBean bd09_To_Gcj02(double bd_lat, double bd_lon) {
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * pi);
        double gg_lon = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new GpsBean(gg_lat, gg_lon);
    }

    /**
     * (BD-09)-->84
     * @param bd_lat
     * @param bd_lon
     * @return
     */
    public static GpsBean bd09_To_Gps84(double bd_lat, double bd_lon) {

        GpsBean gcj02 = GpsConvert.bd09_To_Gcj02(bd_lat, bd_lon);
        GpsBean map84 = GpsConvert.gcj_To_Gps84(gcj02.getLat(),
                gcj02.getLng());
        return map84;

    }

    /**
     * (BD-09)-->84
     * @return
     */
    public static GpsBean gps84_To_bd09(double lat, double lon) {
        GpsBean gcj02 = GpsConvert.gps84_To_Gcj02(lat,lon);
        GpsBean db09 = GpsConvert.gcj02_To_Bd09(gcj02.getLat(),gcj02.getLng());
        return db09;

    }


    public static boolean outOfChina(double lat, double lon) {
        return false;
//        if (lon < 72.004 || lon > 137.8347)
//            return true;
//        if (lat < 0.8293 || lat > 55.8271)
//            return true;
//        return false;
    }

    public static GpsBean transform(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new GpsBean(lat, lon);
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new GpsBean(mgLat, mgLon);
    }

    public static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    public static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
                * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
                * pi)) * 2.0 / 3.0;
        return ret;
    }

    public static void main(String[] args) {
//        Gps gcj = gps84_To_Gcj02(29.177128,103.820221);
//        System.out.println("new BMap.Point( " + gcj02_To_Bd09(gcj.getWgLon(),gcj.getWgLat()) + "),");
        String[] src = new String[]{
                "119.654666	34.656835				",
                "113.60975	22.220023       ",
                "117.51999	41.066947       ",
                "120.756887	28.318613       ",
                "108.720309	42.801897       ",
                "117.366654	25.522669       ",
                "112.067192	26.391437       ",
                "109.601305	29.226677       ",
                "117.906477	20.262815       ",
                "119.870703	42.626896       ",
                "116.655478	36.372358       ",
                "135.10281	53.570325       ",
                "116.141487	33.277027       ",
                "114.266566	30.132171       ",
                "128.02231	44.653929       ",
                "122.013288	35.128741       ",
                "118.492789	30.080458       ",
                "125.795742	43.498242       ",
                "125.342687	53.339857       ",
                "107.667164	39.394293       ",
                "103.078821	39.216911       ",
                "122.733348	38.664159       ",
                "114.567743	40.749611       ",
                "111.251173	39.592466       ",
                "122.029708	31.879719       ",
                "108.551899	34.318068       ",
                "122.110037	25.669047       ",
                "118.078628	40.257231       ",
                "99.117053	36.496533       ",
                "114.515156	22.568561       ",
                "96.391202	49.18903        ",
                "106.205371	29.253573       ",
                "122.999731	31.185007       ",
                "110.205735	32.207146       ",



        };
        // 将 百度 坐标转换为 WGS84
        for(String value :  src){
            if(value.trim().length() <= 5){
                System.out.println();
                continue;
            }
//            String[] array = value.split("\t");
////            System.out.println("new BMap.Point( " + array[0] + ","+ array[1] + "),");
//
//           GpsBean gps = new GpsBean(Double.parseDouble(array[1].trim()),Double.parseDouble(array[0].trim()));
//
//             GpsBean bd = bd09_To_Gps84(gps.getWgLat(),gps.getWgLon());
////            GpsBean bd = gps84_To_bd09(gps.getWgLat(),gps.getWgLon());
//
////            GpsBean bd = gcj_To_Gps84(gps.getWgLat(),gps.getWgLon());
//
////            System.out.println("new BMap.Point( " +bd.getWgLon() + "," + bd.getWgLat()+ "),");
//
//            System.out.println(bd.getWgLon() + "\t" + bd.getWgLat());
        }

    }
}
