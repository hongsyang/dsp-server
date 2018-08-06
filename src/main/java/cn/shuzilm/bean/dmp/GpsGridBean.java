package cn.shuzilm.bean.dmp;

import lombok.Data;

/**
 * Created by thunders on 2018/8/2.
 */
@Data
public class GpsGridBean {
    private int id;
    private double lngLeft;
    private double latDown;
    private double lngRight;
    private double latUp;
    private String payload;
}
