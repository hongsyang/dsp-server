package cn.shuzilm.bean.tencent.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TencentUser  implements Serializable {

    private  String  id ;//否 Cookie Mapping ID（具体参见文档《腾讯广点通 CookieMapping 服务对接说明文档》）
    private List<Integer> audience_ids ;//否 用户 ID 命中的 DMP 人群包ID
}
