package cn.shuzilm.bean.tencent.request;

import lombok.Data;

import java.io.Serializable;
@Data
public class TencentApp  implements Serializable {

    private Integer industry_id;//否 App 所属行业 id
    private String app_bundle_id;//否 App 的唯一标识。

}
