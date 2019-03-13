package cn.shuzilm.bean.baidu.response;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    信息流
* @Author:         houkp
* @CreateDate:     2019/3/13 20:34
* @UpdateUser:     houkp
* @UpdateDate:     2019/3/13 20:34
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class BaiduNativeAd implements Serializable{
    // 标题,编码UTF-8
    private String title ;// 1;
    // 描述,编码UTF-8
    private String desc ;// 2;
    // image,支持多图片
    private BadiduImage image ;// 3;
    // logo 或 icon
    private BadiduImage logo_icon ;// 4;
    // app 大小
    private Integer app_size ;// 5;
    // 品牌名称,编码UTF-8
    private String brand_name ;// 6;
}

