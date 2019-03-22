package cn.shuzilm.bean.baidu.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 用户坐标信息
 * @Author: houkp
 * @CreateDate: 2019/3/13 17:51
 * @UpdateUser: houkp
 * @UpdateDate: 2019/3/13 17:51
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class BaidUserCoordinate implements Serializable {


    // 地图坐标标准
    // 经纬度坐标标准
    // 百度地图的经纬度坐标标准
    //        BD_09;// 0;
    // 国测局制定的经纬度坐标标准
//        GCJ_02;// 1;
    // 国际经纬度坐标标准
//        WGS_84;// 2;
    // 百度地图的墨卡坐标标准,以米为单位
//        BD_09_LL;// 3;
    private String standard;

    // 维度
    private Float latitude;
    // 经度
    private Float longitude;
}