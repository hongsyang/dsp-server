package cn.shuzilm.bean.lj.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: LJNativeData 信息流：原始广告信息
 * @Author: houkp
 * @CreateDate: 2018/7/11 20:11
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/11 20:11
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class LJNativeData implements Serializable {
    private Integer type;//数据类型
    private Integer len;//最大允许的字符个数

    private String value;//格式化数据文本内容,可以包括格式化的值，比如"$10"
}
