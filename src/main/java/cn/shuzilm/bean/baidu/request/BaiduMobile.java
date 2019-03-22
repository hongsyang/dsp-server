package cn.shuzilm.bean.baidu.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 设备信息
 * @Author: houkp
 * @CreateDate: 2019/3/13 17:51
 * @UpdateUser: houkp
 * @UpdateDate: 2019/3/13 17:51
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class BaiduMobile implements Serializable {

    // 百度唯一标示移动设备的编号
    private String DEPRECATED_device_id;// 1;

    // 序列号类型 imei, mac,或者未知
    private String type;// 1;


    private String id;// 2;   // 序列号

    // 设备类型
    private String device_type;// 2;


    // 移动平台名，例如android，iphone等等
    private String platform;// 3 [default ;// UNKNOWN_OS];
    // 移动操作系统版本号
    // 例如 Android 2.1, major, minor 分别是 2,1
// 例如 Iphone 4.2.1， major, minor, micro 分别是 4,2,
    private  BaiduOsVersion os_version;

    // 设备品牌
    private String brand;// 5;
    // 设备机型
    private String model;// 6;
    // 设备屏宽
    private Integer screen_width;// 7;
    // 设备屏高
    private Integer screen_height;// 8;
    // 设备屏幕像素密度
    private float screen_density;// 15;
    // 运营商编号（MCC+MNC编号）
    // 例如中国移动 46000
    // 前三位是Mobile Country Code
    // 后两位是Mobile Network Code
    private Integer carrier_id;// 9;
    // 无线网络类型
    private String wireless_network_type;// 10;

    // 移动设备上为广告控制提供的ID
    private String DEPRECATED_for_advertising_id;// 11;

    //移动应用信息
    private BadiduMobileApp mobile_app;// 12;

}
