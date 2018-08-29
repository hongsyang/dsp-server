package cn.shuzilm.bean.lj.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: LJNativeImg 图片元素
 * @Author: houkp
 * @CreateDate: 2018/7/11 20:11
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/11 20:11
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class LJNativeImg  implements Serializable{
    private Integer type;//	图片类型,
    private Integer img_num;//	图片个数,默认值1
    private Integer w;//允许图片宽度
    private Integer h;//	允许图片高度
    private String[] mimes;//允许的图片类型，例：["png","jpg"]

    private List<String> urls;//图片素材地址,返回的图片个数与请求体中的img_num对应，图片尺寸要求相同
}
