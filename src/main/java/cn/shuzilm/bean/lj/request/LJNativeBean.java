package cn.shuzilm.bean.lj.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: LJNativeBean 信息流：原始广告信息
 * @Author: houkp
 * @CreateDate: 2018/7/11 20:11
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/11 20:11
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class LJNativeBean implements Serializable{
    private LJNativeExt ext;
    private List<LJAssets> assets;
}
