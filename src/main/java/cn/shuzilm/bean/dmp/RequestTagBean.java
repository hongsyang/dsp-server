package cn.shuzilm.bean.dmp;

import lombok.Data;

/**
 * Created by thunders on 2018/7/26.
 */
@Data
public class RequestTagBean {

    //基本信息
    private String adUid; // 广告id
    private String adviserId;// 广告主ID
    private String name; //人群名称
    private String remark; //备注
    private String type; //人群类型 人群选择方式（location:地域/demographic:人群/company:公司）

    //地理位置
    private String city; //地理位置 省份、地级、县级 选定列表
    private String geo; //地理位置 经纬度  对应：mysql 中的 location_map
    private String mobilityType; //流动性 不限制、居住地、工作地、活动地
    private String demographicTagId; //特定人群-标签选定项  例如： 大学生、家长、户外爱好者
    private String demographicCitys; //特定人群 - 城市范围选定列表 省份地级县级市


    //属性筛选
    private int incomeId; //收入水平
    private String appPreferenceIds;//兴趣

    private int platformId; //平台 安卓或 IOS
    private String brand; //品牌
    private int phonePrice;//设备价格 分档
    private int networkId; //网络类型 LTE 3G
    private int carrierId; // 运营商
    private String appPreferenceId;// 应用偏好

    //公司定向
    private String comApNames; //公司WIFI名称 ，多个用 ，号隔开
    private String comFullName;//公司全称
    private String comAddress;//公司地址


}
