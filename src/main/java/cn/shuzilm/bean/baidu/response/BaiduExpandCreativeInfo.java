package cn.shuzilm.bean.baidu.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 扩展创意信息
 * // 如果BidRequest的AdSlot中包含ExpandCreativeInfo信息，
 * // 则DSP可以在BidResponse中返回如下结构表示返回来扩展创意。
 * // 目前BES对扩展创意的支持仅限于动态创意，亦即填充了html_snippet。
 * @Author: houkp
 * @CreateDate: 2019/3/13 20:29
 * @UpdateUser: houkp
 * @UpdateDate: 2019/3/13 20:29
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class BaiduExpandCreativeInfo  implements Serializable{

    // 创意宽
    private Integer creative_width;// 1;
    // 创意高
    private Integer creative_height;// 2;
    // 着陆页
    private String creative_landing_page;// 3;
    // 物料
    private Integer creative_type;// 4;
}
