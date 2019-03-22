package cn.shuzilm.bean.baidu.request;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    百度user id list
* @Author:         houkp
* @CreateDate:     2019/3/22 11:21
* @UpdateUser:     houkp
* @UpdateDate:     2019/3/22 11:21
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class BaiduIdList implements Serializable {

    // 用户ID，该字段将于2015年4月30日停止使用
    private String baidu_user_id;// 4;
    // 用户ID版本，该字段将于2015年4月30日停止使用
    private Integer baidu_user_id_version;// 5;
}
