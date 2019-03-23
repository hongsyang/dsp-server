package cn.shuzilm.bean.baidu.request;


import lombok.Data;

import java.io.Serializable;
import java.util.List;
/**
 * @Description:    BidRequest 请求体
 * @Author:         houkp
 * @CreateDate:     2019/3/13 17:47
 * @UpdateUser:     houkp
 * @UpdateDate:     2019/3/13 17:47
 * @UpdateRemark:   修改内容
 * @Version:        1.0
 */
@Data
public class BaiduBidRequest implements Serializable {

    // 请求ID，唯一标识本次请求，明文字符串
    private String id;// 1;

    // **** 用户信息 ****
    // 用户IP地址，点分十进制字符串
    private String ip;// 2;
    // User-Agent
    private String user_agent;// 3;

    // 用户分类
    private List<Integer> user_category;// 6;


    // 用户ID
    // 在2015年4月30日之前将会同时发送版本1和版本2的cookie
    // 从2015年5月1日起只发送版本2的cookie
    private List<BaiduIdList>  baidu_id_list;

    // **** 页面信息 ****
    // 当前页面URL
    private String url;// 11;
    // 请求的refer
    private String referer;// 12;
    // 网站分类
    private Integer site_category;// 13;
    // 网站质量类型
    private Integer site_quality;// 14;

    // 页面类型
    private Integer page_type;// 15;
    // 页面关键词
    private List<String> page_keyword;// 17;
    // 页面内容质量
    private Integer page_quality;// 18;
    // 页面分类
    private Integer page_vertical;// 21;




    // DSP 托管到 BES的 USER ID
    private String buyer_user_id;// 32;



    // 自定义的用户标签
    private BaiduCustomizedUserTag customized_user_tag;// 31;
    // 用户性别
    private String gender;// 7;
    // 页面语言
    private String detected_language;// 9;

    // 发布商不允许的广告行业
    private List<Integer> excluded_product_category;// 19 [packed;//true];

    // flash版本
    private String flash_version;// 10;
    // **** 系统使用 ****
    private Boolean is_test;// 26 [default;//false];
    private Boolean is_ping;// 27 [default;//false];



    // **** 广告位信息 ****
    // 默认每次请求一个广告位
    private List<BaiduAdSlot> adslot;// 20;

    // **** 位置信息 ****
    private BaiduUserGeoInfo user_geo_info;// 28;


    // **** 移动设备信息 ****
    private  BaiduMobile  mobile;

    // 视频流量的媒体信息
    private BaiduVideo video;// 30;




}
