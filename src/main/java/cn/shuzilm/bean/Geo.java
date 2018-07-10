package cn.shuzilm.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: Geo 地理位置信息对象
 * @Author: houkp
 * @CreateDate: 2018/7/10 16:51
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/10 16:51
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class Geo implements Serializable {
    private Double lat;//(建议)  纬度
    private Double lon;//(建议) 经度
    private Integer timestamp;//(F) 获取经纬度数据时的时间戳
    private String country;//(F)  国家，使用 ISO-3166-1 Alpha-3
    private String region;//(F) 地区，使用 ISO 3166-2
    private Integer type;//(F) 数据来源1-GPS 2-IP 3-User provided
    private RequestExt ext;//(F) 扩展内容

}
