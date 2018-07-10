package cn.shuzilm.bean;

import lombok.Data;

import java.io.Serializable;


/**
 * @Description: UserData 额外的用户数据。每个数据对象代表不同的数据源。
 * @Author: houkp
 * @CreateDate: 2018/7/10 17:56
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/10 17:56
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class UserData implements Serializable {
    private String id;//(建议) 数据源的唯一标识 ID，参考附录13
    private String name;//(建议) 数据源 名称  "momo"
    private UserSegment segment;//(建议) segment 数组，包含实际的数据值
    private RequestExt ext;//(F) 扩展内容
}
