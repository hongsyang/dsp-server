package cn.shuzilm.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: User 用户信息
 * @Author: houkp
 * @CreateDate: 2018/7/10 17:06
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/10 17:06
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class User implements Serializable {
    private String id;//(建议)  用户唯一标识；
    private Integer yob;//(F)出生年，4 位数字
    private Integer age_low;//(F)  年龄范围，最小年龄
    private Integer age_high;//(F) 年龄范围，最大年龄
    private String gender;//(F) 性别 “M”-Male “F”-Female “O”-Other“Null”-Unknown
    private String keywords;//(F)  用户兴趣或倾向，以逗号分隔；
    private UserData data;//(F)  额外的用户数据。每个数据对象代表不同的数据源。；
    private Geo geo;//(F)  用户家庭位置；
    private RequestExt ext;//(F) 扩展内容


}
