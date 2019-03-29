package cn.shuzilm.bean.baidu.request;

import lombok.Data;

/**
* @Description:    图片信息
* @Author:         houkp
* @CreateDate:     2019/3/28 15:15
* @UpdateUser:     houkp
* @UpdateDate:     2019/3/28 15:15
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class BaiduImageEle {

    // 宽
    private Integer width ;
    // 高
    private Integer height ;
    // 形状，
    // 0没有形状要求，
    // 1矩形，
    // 2圆形，
    // 3半圆形
    private Integer shape ;
}
