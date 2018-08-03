package cn.shuzilm.bean.dmp;

import cn.shuzilm.bean.control.ICommand;
import lombok.Data;

import static cn.shuzilm.util.geo.GpsConvert.bd09_To_Gps84;

/**
 * 经纬度坐标 bean
 * 默认是百度坐标，需要通过 转换成 GPS84 坐标
 * Created by thunders on 2018/7/27.
 */

public class GpsBean implements ICommand{


    private double lng;
    private double lat;
    private int radius;
    private String poiName;
    private String payload;// 附带的一些有价值的信息，比如 广告 UID 等

    public GpsBean(){}
    public GpsBean(double lat,double lng ){
        this.lng = lng;
        this.lat = lat;
//        double[] newValue = convertFromBd09(lng,lat);
//        lng = newValue[0];
//        lat = newValue[1];
    }

    private double[] convertFromBd09(double lng,double lat){
        double[] newValue = new double[2];
        GpsBean bean = bd09_To_Gps84(lat,lng);
        newValue[0] = bean.getLng();
        newValue[1] = bean.getLat();
        return newValue;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }
    public double getLat() {
        return lat;
    }

    public String getPoiName() {
        return poiName;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    public double[] getGpsLngLat(double lng, double lat){
        double[] des = convertFromBd09(lng,lat);
        return des;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
