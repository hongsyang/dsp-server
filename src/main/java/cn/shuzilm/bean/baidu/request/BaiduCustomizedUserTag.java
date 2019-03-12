package cn.shuzilm.bean.baidu.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BaiduCustomizedUserTag  implements Serializable {

    private  BaiduAmsTag  amsTag ;//
    private List<Integer> ams_tag_list ;//
}
