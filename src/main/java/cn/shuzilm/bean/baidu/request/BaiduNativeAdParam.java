package cn.shuzilm.bean.baidu.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @Description:    原生广告
* @Author:         houkp
* @CreateDate:     2019/3/28 15:03
* @UpdateUser:     houkp
* @UpdateDate:     2019/3/28 15:03
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class BaiduNativeAdParam  implements Serializable{


        // 按照位图设置相应的位为1，不需要请求的位保持0
        // 示例：原生广告必须包含标题和图标，则required_fields = (0x1 | 0x8) = 9
        private List<Long>  required_fields ;
        // 标题最大长度
        private  Integer title_max_length ;
        // 描述最大长度
        private  Integer desc_max_length ;
        // 广告主logo或图标的宽高、形状要求
        private BaiduImageEle logo_icon ;
        // 主题图的宽高、形状要求
        private BaiduImageEle image;

        // 主题图数量
        private  Integer image_num ;

}
