package cn.shuzilm.bean.baidu.request;

import cn.shuzilm.bean.youyi.request.YouYiMobile;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TencentDevice implements Serializable {
    private String id;//设备唯一标示。Android 用 IMEImd5sum， iOS 用IDFA md5sum。 微信流量不传递该字段。相关政策参见附录5。 加密规则： IDFA码（需转大写），进行 MD5SUM 以后得到的 32 位全小写MD5 表现字符串加密规则
    private String device_type;//否 设备类型
    private String os;//否 操作系统
    private String os_version;//否 操作系统版本
    private String user_agent;//否 user agent, 如"Mozilla/5.0"。
    private Integer screen_width;//否 屏幕宽度
    private Integer screen_height;//否 屏幕高度
    private Integer dpi;//否 屏幕每英寸像素
    private String carrier;//否 运营商
    private String connection_type;//否 设备联网方式。
    private String brand_and_model;//否 设备品牌型号
    private String connection;//否 运营商
    private String language;//否 设备语言
    private String idfa;//否 iOS 设备 IDFA 原文
    private String manufacturer;//否 设备制造商信息
    private String android_id;//否 Android 设备 Android ID 密文


}

