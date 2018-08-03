package cn.shuzilm.bean.dmp;

import cn.shuzilm.bean.control.ICommand;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thunders on 2018/7/26.
 */
@Data
public class AudienceBean implements ICommand {

    //基本信息
    private String adUid; // 广告id
    private String adviserId;// 广告主ID
    private String name; //人群名称
    private String remark; //备注
    private String type; //人群类型 人群选择方式（location:地域/demographic:人群/company:公司）

    //地理位置
    private String citys; //地理位置 省份、地级、县级 选定列表
    private String geos; //地理位置 经纬度  对应：mysql 中的 location_map
    private String mobilityType; //流动性 不限制、居住地、工作地、活动地
    private String demographicTagId; //特定人群-标签选定项  例如： 大学生、家长、户外爱好者
    private String demographicCitys; //特定人群 - 城市范围选定列表 省份地级县级市


    //属性筛选
    private int incomeLevel; //收入水平  0 不限 1 超高 2 高 3 中  4 低
    private String appPreferenceIds;//兴趣

    private int platformId; //平台 安卓  或 IOS  0 不限 1 安卓 2 ios
    private String brandId; //品牌
    private int phonePriceLevel;//设备价格 分档  0 不限 1 1000 元内 2 1000-4000 3 4000- 10000  4 10000 以上
    private int networkId; //网络类型  不限 0 移动网络  1 WIFI 2
    private int carrierId; // 运营商 不限 0 移动 1 电信 2 联通 3

    //公司定向
    private String comApNames; //公司WIFI名称 ，多个用 ，号隔开
    private String comFullName;//公司全称
    private String comAddress;//公司地址

    public List<AreaBean> getCityList(){
        ArrayList<AreaBean> list = new ArrayList<>();
        //todo 解析 city json  为 list
//        JsonObject obj = JSONObject.parseObject(citys);
        return list;
    }

    /**
     * 经纬度、POI GPS 坐标点名称
     * @return
     */
    public ArrayList<GpsBean> getGeoList(){
        ArrayList<GpsBean> gpsList = new ArrayList<>();
        //todo 解析 geo json 为 List
//        JSONObject.parseObject(this.geos);
        return gpsList;
    }

}
