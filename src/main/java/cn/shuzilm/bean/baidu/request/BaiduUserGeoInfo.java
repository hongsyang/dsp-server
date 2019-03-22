package cn.shuzilm.bean.baidu.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

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
public class BaiduUserGeoInfo implements Serializable{

    //坐标信息
    private List<BaidUserCoordinate> user_coordinate;

    //城市信息
    private BaiduUserLocation user_location;// 2;
}
