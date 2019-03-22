package cn.shuzilm.bean.baidu.response;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    图片信息
* @Author:         houkp
* @CreateDate:     2019/3/13 20:37
* @UpdateUser:     houkp
* @UpdateDate:     2019/3/13 20:37
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class BadiduImage  implements Serializable{
    private String url ;// 1;
    private Integer width ;// 2;
    private Integer height ;// 3;

}
