package cn.shuzilm.bean.baidu.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description:    位置信息
 * @Author:         houkp
 * @CreateDate:     2019/3/13 17:51
 * @UpdateUser:     houkp
 * @UpdateDate:     2019/3/13 17:51
 * @UpdateRemark:   修改内容
 * @Version:        1.0
 */
@Data
public class BaiduUserLocation   implements Serializable {

    // 省份
    private String province;// 1;
    // 城市
    private String city;// 2;
    // 区县
    private String district;// 3;
    // 街道
    private String street;// 4;
}
