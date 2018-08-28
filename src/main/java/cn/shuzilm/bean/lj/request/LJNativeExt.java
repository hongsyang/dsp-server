package cn.shuzilm.bean.lj.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: LJNativeExt 扩展属性
 * @Author: houkp
 * @CreateDate: 2018/7/11 20:11
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/11 20:11
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class LJNativeExt implements Serializable {
    private Integer type;//0:常规信息流 1：普通博文 2：card样式博文 3：不区分类型
}
