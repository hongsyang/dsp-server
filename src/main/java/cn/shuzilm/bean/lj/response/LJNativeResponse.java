package cn.shuzilm.bean.lj.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: LJNativeResponse 信息流返回结果
 * @Author: houkp
 * @CreateDate: 2018/7/11 20:11
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/11 20:11
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class LJNativeResponse  implements Serializable{
    private  NativeAD nativead;//NativeAd Object定义的最外层

}
