package cn.shuzilm.bean.baidu.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: // APP 唤醒信息
 * // 目前只有交互类型为应用唤醒时需要填写该字段
 * @Author: houkp
 * @CreateDate: 2019/3/13 20:32
 * @UpdateUser: houkp
 * @UpdateDate: 2019/3/13 20:32
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class BaiduDeeplinkInfo  implements Serializable{
    // 应用唤醒打开页面
    private String deeplink_url;// 1;
    // 应用唤醒版本
    private Integer app_version;// 2;
    // 应用唤醒退化链接
    private String fallback_url;// 3;
    // 应用唤醒退化链接类型: LANDING_PAGE ;// 1, DOWNLOAD ;// 2
    private Integer fallback_type;// 4;
}
