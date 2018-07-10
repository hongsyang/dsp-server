package cn.shuzilm.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: segment 数组，包含实际的数据值
 * @Author: houkp
 * @CreateDate: 2018/7/10 18:02
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/10 18:02
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class UserSegment implements Serializable {
    private String id;// (建议) 数据提供者特定的数据段编号 “0”
    private String name;// (建议)数据提供者特定的数据段名称 “interest”
    private String value;// (建议) 数据提供者特定的数据段的内容ID，参考附录 14
    private RequestExt ext;//(F) 扩展内容
}
