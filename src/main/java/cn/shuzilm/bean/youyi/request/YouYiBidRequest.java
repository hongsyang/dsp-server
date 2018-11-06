package cn.shuzilm.bean.youyi.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class YouYiBidRequest implements Serializable {

    private String session_id;//是  竞价请求全局唯一 ID
    private String bucket_id;//;//否 string 流量 bucket
    private String host_nodes;//否 string 节点， 用|分割参数 必填 类型 描述
    private String detected_language;//否 string 语言
    private String anonymous_id;//否 string 匿名 id
    private String timezone_offset;//否 int32 时区偏移量
    private String keywords;//repeated string 用户搜索词
    private YouYiExchange Exchange;//否 object
    private YouYiUser User;//否 object 用户信息
    private YouYiAdzone Adzone;//否
    private YouYiSite Site;//否 object 媒体信息
    private YouYiMobile Mobile;//否 object app 信息
    private Boolean is_https;//否 bool
}
