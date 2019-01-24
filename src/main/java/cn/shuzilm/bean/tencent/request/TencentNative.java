package cn.shuzilm.bean.tencent.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TencentNative   implements Serializable {

    private Long required_fields;//否  原生广告位强制需要的组件， DSP 返回的广告至少需要满足此要求,所需要的组件根据Native::Fields 的定义取或后填充
    private List<String> type;//否 原生广告位要求的广告类型
}
