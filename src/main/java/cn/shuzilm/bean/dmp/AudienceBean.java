package cn.shuzilm.bean.dmp;

import cn.shuzilm.bean.control.ICommand;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Created by thunders on 2018/7/26.
 */
@Getter
public class AudienceBean implements ICommand {
	private static final long serialVersionUID = -5959105878383063302l;
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
    
    private String demographicTagId; //特定人群-标签选定项  例如： 大学生、家长、户外爱好者
    
    private Set<String> demographicTagIdSet;
    
    
    private String demographicCitys; //特定人群 - 城市范围选定列表 省份地级县级市

    private Set<AreaBean> demographicCitySet;//特定人群城市

    //属性筛选
    @Setter
    private int incomeLevel; //收入水平  0 不限 1 超高 2 高 3 中  4 低
    
    private String appPreferenceIds;//兴趣
    
    private Set<String> appPreferenceIdSet;//兴趣列表

    @Setter
    private int platformId; //平台 安卓  或 IOS  0 不限 1 安卓 2 ios
    
    private String brandIds; //品牌
    
    private Set<String> brandIdSet;//品牌列表
    @Setter
    private int phonePriceLevel;//设备价格 分档  0 不限 1 1000 元内 2 1000-4000 3 4000- 10000  4 10000 以上
    @Setter
    private int networkId; //网络类型  不限 0 移动网络  1 WIFI 2
    @Setter
    private int carrierId; // 运营商 不限 0 移动 1 电信 2 联通 3

    //公司定向
    
    private String companyIds; //公司 ID 列表 ，多个用 ，号隔开
    
    private Set<String> companyIdSet;
    @Setter
    private String companyNames;//公司全称 ，用","号隔开

    public void setDemographicCitys(String citys){
        if(citys == null)
            return;
        demographicCitys = citys;
        String[] split = citys.split("],");
        Set<String> set = new HashSet();
        String re = "[";
        String ra = "]";
        for (String s : split) {
            String replace = s.replace(re, "").trim().replace(ra, "");
            set.add(replace);
        }
        this.demographicCitySet = convertToAreaBeanSet(set);
    }
    
    public void setCompanyIds(String companyIds) {
		this.companyIds = companyIds;
		String[] split = companyIds.split(",");
		Set<String> set = new HashSet();
		String re = "{";
		String ra = "}";
		for(String s : split){
			String replace = s.replace(re, "").trim().replace(ra, "");
			String[] nameAndId = replace.split(":");
			if(nameAndId.length >1){
				set.add(nameAndId[1]);
			}
		}
		
		this.companyIdSet = set;
	}

    public String getCitys() {
        return citys;
    }

    public void setCitys(String citys) {
        this.citys = citys;
        if (StringUtils.isNotBlank(citys)) {
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
    
    /**
     * 转换省市区 编码
     *
     * @param set
     * @return
     */
    private Set<AreaBean> convertToAreaBeanSet(Set<String> set) {
        HashSet<AreaBean> citySet = new HashSet<>();
        for (String city : set) {
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
            citySet.add(areaBean);
        }
        return citySet;
    }


    public String getGeos() {
        return geos;
    }

    public void setGeos(String geos) {
        if(geos == null || geos.trim().equals(""))
            return;
        this.geos = geos;
        //判断geos是否为空
        if (StringUtils.isNotBlank(geos)) {
        	JSONArray array = JSONArray.parseArray(geos);
        	if(array != null && array.size()>0){
        		List<Map.Entry> list = new ArrayList<Map.Entry>();
        		for(int i =0;i<array.size();i++){
        			JSONObject parse = array.getJSONObject(i);
        			Iterator<Map.Entry<String, Object>> iterator = parse.entrySet().iterator();        			
        			while (iterator.hasNext()) {
        				Map.Entry<String, Object> entry = iterator.next();
        				list.add(entry);
        			}
        		}
            this.geoList = convertToGpsBeanList(list);
        	}
        }
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
            String[] gpsDetail = value.replace(re, "").trim().replace(ra, "").replace("\"", "").split(",");
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
    
    public void setDemographicTagId(String demographicTagId) {
		this.demographicTagId = demographicTagId;
		if (StringUtils.isNotBlank(demographicTagId)) {
            String[] split = demographicTagId.split(",");
            Set<String> set = new HashSet<String>();
            String re = "[";
            String ra = "]";
            for (String s : split) {
                String replace = s.replace(re, "").trim().replace(ra, "");
                set.add(replace);
            }
            this.demographicTagIdSet = set;
        }
	}
    
    public void setAppPreferenceIds(String appPreferenceIds) {
    	this.appPreferenceIds = appPreferenceIds;
		if (StringUtils.isNotBlank(appPreferenceIds)) {
            String[] split = appPreferenceIds.split(",");
            Set<String> set = new HashSet<String>();
            String re = "[";
            String ra = "]";
            for (String s : split) {
                String replace = s.replace(re, "").trim().replace(ra, "");
                set.add(replace);
            }
            this.appPreferenceIdSet = set;
        }
	}

	public void setBrandIds(String brandIds) {
		this.brandIds = brandIds;
		if (StringUtils.isNotBlank(brandIds)) {
            String[] split = brandIds.split(",");
            Set<String> set = new HashSet<String>();
            String re = "[";
            String ra = "]";
            for (String s : split) {
                String replace = s.replace(re, "").trim().replace(ra, "");
                set.add(replace);
            }
            this.brandIdSet = set;
        }
	}
    
    
}
