package cn.shuzilm.bean.tencent.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 用户设备信息
 * @Author: houkp
 * @CreateDate: 2018/11/22 20:46
 * @UpdateUser: houkp
 * @UpdateDate: 2018/11/22 20:46
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class YouYiMobile implements Serializable {

    private Boolean is_app;//否 bool
    private String device_os;//否 string 操作系统(小写)  例如： android, ios
    private String device_os_version;//否 string  操作系统版本 例如： 7.0.2
    private String device_model;//否 string 设备型号(小写)  // 例如： n70, galaxy.
    private String device_brand;//否 string 设备品牌(小写)  // 例如： nokia, samsung.
    private String device_id;//否 string 设备 ID
    private Float longtitude;//否 float 经度
    private Float latitude;//否 float 维度
    private Integer network;//否 uint32  设备所处网络环境   // 0-未识别, 1-wifi, 2-2g, 3-3g, 4-4g
    private String device_type;//否 string 设备类型(phone pad tv mp3 outdoor_screen)
    private Integer operator;//否 int32  g 设备的网络运营商   // 0-未知, 1-移动, 2-联通, 3-电信
    private Boolean is_fullscreen;//否 bool
    private String app_id;//否 string ios :ituns id android : package name
    private String device_resolution;//否 string  设备的屏幕分辨率 // 例如： 1024x768
    private String imei;//否 string 下面的字段用于离线数据分析
    private String idfa;//否 string 原文大写
    private String mac;//否 string 原文去冒号大写
    private String android_id;//否 string 原文小写
    private String app_name;//否 string specific fields for mobile
    private String md5_imei;//否 string 原文 md5 小写
    private String md5_android_id;//否 string
    private String md5_mac;//否 string 原文去冒号大写后 md5 小写
    private String md5_duid;//否 string
    private String duid;//否 string
    private String phone_num;//否 string
    private String device_pixel_ratio;//否 string   new field add // 屏幕 PPI
    private String screen_orientation;//否 string  屏幕方向， 0， home 在下， 90,180,270，顺时针旋  转角度
    private Integer media_quality;//否 int32
    private String app_bundle;//否 string  包名


}
