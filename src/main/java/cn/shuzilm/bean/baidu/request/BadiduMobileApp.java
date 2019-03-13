package cn.shuzilm.bean.baidu.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description:   // 移动应用信息，当流量来自App时该字段非空
 * @Author: houkp
 * @CreateDate: 2019/3/13 17:51
 * @UpdateUser: houkp
 * @UpdateDate: 2019/3/13 17:51
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class BadiduMobileApp  implements Serializable  {


    // 百度移动联盟为该App分配的app id
    private String app_id;// 1;
    // 如果来自苹果商店，则直接是app-store id
    // 如果来自Android设备，则是package的全名
    private String app_bundle_id;// 2;
    // App应用分类
    private Integer app_category;// 3;
    // App开发者ID
    private Integer app_publisher_id;// 4;

    // App允许的交互类型   // 电话、下载、应用唤醒
    private List<String> app_interaction_type;// 5;
}
