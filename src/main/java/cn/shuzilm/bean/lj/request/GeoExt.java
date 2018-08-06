package cn.shuzilm.bean.lj.request;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    GeoExt
* @Author:         houkp
* @CreateDate:     2018/8/3 12:21
* @UpdateUser:     houkp
* @UpdateDate:     2018/8/3 12:21
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class GeoExt implements Serializable {
    private Integer accuracy;//GPS的精确度，单位为米。如：100表示精确度为100米。
}
