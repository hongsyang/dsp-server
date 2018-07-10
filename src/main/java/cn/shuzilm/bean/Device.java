package cn.shuzilm.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: Device设备信息
 * @Author: houkp
 * @CreateDate: 2018/7/10 16:45
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/10 16:45
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class Device implements Serializable {
    private Integer dnt;//(建议) 是否不允许广告追踪：0- 允许追踪1- 不允许追踪
    private String ua;//(建议) 浏览器的 user agent 值
    private String ip;//(T) 设备的 IP 地址  117.136.54.129
    private Geo geo;//(建议) 浏览器的 user agent 值
    private String ifa;//(建议) 广告用户使用的 id，传原始值。ios 系统，ifa 指 IDFA值；android 系统，ifa 指Google play advertise id。
    private String didsha1;//(建议) IMEI 的 SHA1 值
    private String dpidsha1;//(建议) AndroidID 或 IDFA 的 SHA1 值
    private String macsha1;//(建议) MAC 地址的 SHA1 值
    private String didmd5;//(建议) IMEI 的 md5 值
    private String dpidmd5;//(建议) AndroidID 或 IDFA 的 MD5 值
    private String macmd5;//(建议) MAC 地址的 MD5 值
    private String carrier;//(F) 设备使用的运营商，MCC+MNC46000、46002、46007=>中国移动46001、46006=>中国联通 46003、46005=>中国电信  参考：http://en.wikipedia.org/wiki/Mobile_Network_Code
    private String language;//(F) 设备的语言设置,使用 alpha2/ISO639-1 "en"
    private String make;//(F) 设备制造商 “Apple
    private String model;//(F) 设备型号  “iPhone”
    private String os;//(F) 设备操作系统  “iOS”
    private String osv;//(F) 设备制造商 “7.0.6”
    private Integer js;//(F) 是否支持 Javascript 脚本1-支持  0，不支持
    private Integer battery;//(F) 设备电量百分比，取整数，数值区间 0~100。
    private Integer connectiontype;//(F) 设备联网类型，参考附录 7
    private Integer devicetype;//(F) 设备类型，参考附录 8
    private Float s_density;//(F) 设备屏幕像素密度    2.0
    private Integer sw;//(F) 设备屏幕分辨率宽度，单位为像素 1080
    private Integer sh;//(F)设备屏幕分辨率高度，单位为像素  1920
    private Integer orientation;//(F)设备屏幕方向:0- 竖向 1- 横向   0
    private RequestExt ext;//(F) 扩展内容
}
