package cn.shuzilm.bean.baidu.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description:    设备id
 * @Author:         houkp
 * @CreateDate:     2019/3/13 18:03
 * @UpdateUser:     houkp
 * @UpdateDate:     2019/3/13 18:03
 * @UpdateRemark:   修改内容
 * @Version:        1.0
 */
@Data
public class BaiduID implements Serializable{


    // 序列号类型 imei, mac,或者未知
    private String type;// 1;


    private String id;// 2;   // 序列号
}
