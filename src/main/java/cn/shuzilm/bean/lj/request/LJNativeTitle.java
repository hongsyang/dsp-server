package cn.shuzilm.bean.lj.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: LJNativeTitle 标题
 * @Author: houkp
 * @CreateDate: 2018/7/11 20:11
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/11 20:11
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class LJNativeTitle implements Serializable{
    private Integer len;//最大允许的标题字符个数

    private String text;//标题文本内容
}
