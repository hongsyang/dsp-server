package cn.shuzilm.bean.dmp;

import cn.shuzilm.util.JsonTools;
import lombok.Data;

/**
 * Created by thunders on 2018/7/25.
 */
@Data
public class TagBean {
    /**
     * 该 TAG 所在标签管理表中的 ID
     */
    private int tagId;//标签 id

    //地理位置
    private double[] work;//工作地
    private double[] residence;//居住地
    private double[] activity;//活动地
    private int provinceId;//省份ID
    private int cityId;//城市ID
    private int countyId;//县级ID

    //属性筛选
    private int incomeId; //收入水平
    private String appPreferenceIds;//兴趣

    private int platformId; //平台 安卓或 IOS
    private String brand; //品牌
    private int phonePrice;//设备价格 分档
    private int networkId; //网络类型 LTE 3G
    private int carrierId; // 运营商
    private String appPreferenceId;// 应用偏好
    /**
     * 人群标签ID 白领，政府机构人士 。。。
     */
    private String tagIdList;
    /**
     * 所属公司标签 公司 ID
     */
    private String companyIdList;
    
    private String ip;


    public static void main(String[] args) {
        TagBean tag = new TagBean();
//        tag.setActivity();
//        tag.setAppPreferenceId();
        String json = JsonTools.toJsonString(tag);
        System.out.println(json);
    }



}
