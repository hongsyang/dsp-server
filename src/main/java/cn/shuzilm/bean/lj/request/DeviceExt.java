package cn.shuzilm.bean.lj.request;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    DeviceExt 扩展属性
* @Author:         houkp
* @CreateDate:     2018/8/3 12:16
* @UpdateUser:     houkp
* @UpdateDate:     2018/8/3 12:16
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class DeviceExt implements Serializable{
    private String idfa;//对应的移动端MMA字段：iOS的IDFA字段(iOS系统 osv>=6时会传该字段，传的是原始值未经过md5 sum)，如："1E2DFA89-496A-47FD-9941-DF1FC4E6484A"
    private String idfamd5;//对应的移动端MMA字段：iOS的IDFA字段取MD5值，如："40C7084B4845EEBCE9D07B8A18A055FC"
    private String mac;//	去除分隔符”:”(保持大写)的MAC地址取MD5摘要,eg:3D8A278F33E4F97181DF1EAEFE500D05
    private String macmd5;//	保留分隔符”:”(保持大写)的MAC地址取MD5摘要,eg:DC7D41E352D13D60765414D53F40BC25
    private String macsha1;//	MAC地址取sha1摘要
    private String ssid;//	WIFI的
    private Integer w;// 设备的屏幕宽度，以像素为单位
    private Integer h;//设备的屏幕高度，以像素为单位
    private Integer brk;// 设备是否越狱，1—已启用（默认），0—未启用。
    private Integer ts;// 发送请求时的本地UNIX时间戳（秒数，10进制）
    private Integer interstitial;// 	是否使用全屏/互动方式来展现广告。1—是，0—否（默认值）。
    private String realip;// 客户端ip地址
    private Boolean isipdx;// true:device中ip的值为ipdx ip；false:device中ip的值是客户端ip

}
