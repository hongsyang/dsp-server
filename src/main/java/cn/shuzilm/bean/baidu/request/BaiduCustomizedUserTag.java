package cn.shuzilm.bean.baidu.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
/**
* @Description:    java类作用描述
* @Author:         houkp
* @CreateDate:     2019/3/13 18:03
* @UpdateUser:     houkp
* @UpdateDate:     2019/3/13 18:03
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class BaiduCustomizedUserTag  implements Serializable {

    private  BaiduAmsTag  amsTag ;//
    private List<Integer> ams_tag_list ;//
}
