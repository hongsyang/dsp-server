package cn.shuzilm.bean.dmp;

import cn.shuzilm.bean.control.ICommand;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by thunders on 2018/7/26.
 */
@Getter
public class AudienceBean implements ICommand {

    //基本信息
    @Setter
    private String adUid; // 广告id
    @Setter
    private String adviserId;// 广告主ID
    @Setter
    private String name; //人群名称
    @Setter
    private String remark; //备注
    @Setter
    private String type; //人群类型 人群选择方式（location:地域/demographic:人群/company:公司）

    //地理位置
    private String citys; //地理位置 省份、地级、县级 选定列表
    private List<AreaBean> cityList;
    private String geos; //地理位置 经纬度  对应：mysql 中的 location_map
    private ArrayList<GpsBean> geoList;
    @Setter
    private int mobilityType; //地理位置-流动性 0 不限 1 居住地 2 工作地 3 活动地
    @Setter
    private String demographicTagId; //特定人群-标签选定项  例如： 大学生、家长、户外爱好者
    @Setter
    private String demographicCitys; //特定人群 - 城市范围选定列表 省份地级县级市

    private List<AreaBean> demographicCityList;//特定人群城市

    //属性筛选
    @Setter
    private int incomeLevel; //收入水平  0 不限 1 超高 2 高 3 中  4 低
    @Setter
    private String appPreferenceIds;//兴趣

    @Setter
    private int platformId; //平台 安卓  或 IOS  0 不限 1 安卓 2 ios
    @Setter
    private String brandIds; //品牌
    @Setter
    private int phonePriceLevel;//设备价格 分档  0 不限 1 1000 元内 2 1000-4000 3 4000- 10000  4 10000 以上
    @Setter
    private int networkId; //网络类型  不限 0 移动网络  1 WIFI 2
    @Setter
    private int carrierId; // 运营商 不限 0 移动 1 电信 2 联通 3

    //公司定向
    @Setter
    private String companyIds; //公司 ID 列表 ，多个用 ，号隔开
    @Setter
    private String companyNames;//公司全称 ，用","号隔开

    public void setDemographicCitys(String citys){
        demographicCitys = citys;
        String[] split = citys.split("],");
        List<String> list = new ArrayList();
        String re = "[";
        String ra = "]";
        for (String s : split) {
            String replace = s.replace(re, "").trim().replace(ra, "");
            list.add(replace);
        }
        this.demographicCityList = convertToAreaBeanList(list);
    }

    public String getCitys() {
        return citys;
    }

    public void setCitys(String citys) {
        this.citys = citys;
        String[] split = citys.split("],");
        List<String> list = new ArrayList();
        String re = "[";
        String ra = "]";
        for (String s : split) {
            String replace = s.replace(re, "").trim().replace(ra, "");
            list.add(replace);
        }
        this.cityList = convertToAreaBeanList(list);
    }

    /**
     * 转换省市区 编码
     *
     * @param list
     * @return
     */
    private ArrayList<AreaBean> convertToAreaBeanList(List<String> list) {
        ArrayList<AreaBean> cityList = new ArrayList<>();
        for (String city : list) {
            if(city == null || city.equals(""))
                continue;
            AreaBean areaBean = new AreaBean();
            String[] cityDetail = city.split(",");
            Integer provinceId = Integer.valueOf(cityDetail[0]);
            areaBean.setProvinceId(provinceId);
            Integer cityId = Integer.valueOf(cityDetail[1]);
            areaBean.setCityId(cityId);
            Integer countyId = Integer.valueOf(cityDetail[2]);
            areaBean.setCountyId(countyId);
            cityList.add(areaBean);
        }
        return cityList;
    }


    public String getGeos() {
        return geos;
    }

    public void setGeos(String geos) {
        if(geos == null || geos.trim().equals(""))
            return;
        this.geos = geos;
        JSONObject parse = JSONObject.parseObject(geos);
        Iterator<Map.Entry<String, Object>> iterator = parse.entrySet().iterator();
        List<Map.Entry> list = new ArrayList<Map.Entry>();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
//            System.out.println(entry.getKey() + ":" + entry.getValue());
            list.add(entry);
        }
        this.geoList = convertToGpsBeanList(list);
    }

    /**
     * 转换对应经纬度和位置描述
     *
     * @param list
     * @return
     */
    private ArrayList<GpsBean> convertToGpsBeanList(List<Map.Entry> list) {
        String re = "[";
        String ra = "]";
        ArrayList<GpsBean> geoList = new ArrayList<>();
        for (Map.Entry entry : list) {
            GpsBean gpsBean = new GpsBean();
            gpsBean.setPayload((String) entry.getKey());
            Object gpsValue = entry.getValue();
            String value = gpsValue.toString();
            String[] gpsDetail = value.replace(re, "").trim().replace(ra, "").split(",");
            Double provinceId = Double.valueOf(gpsDetail[0]);
            gpsBean.setLng(provinceId);
            Double cityId = Double.valueOf(gpsDetail[1]);
            gpsBean.setLat(cityId);
            Integer countyId = Integer.valueOf(gpsDetail[2]);
            gpsBean.setRadius(countyId);
//            System.out.println(value);
            geoList.add(gpsBean);
        }
        return geoList;
    }

}
