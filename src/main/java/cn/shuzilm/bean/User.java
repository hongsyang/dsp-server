package cn.shuzilm.bean;

import lombok.Data;

import java.io.Serializable;
/**
* @Description:    User 用户信息
* @Author:         houkp
* @CreateDate:     2018/7/10 17:06
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/10 17:06
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class User implements Serializable{

    private RequestExt requestExt;//(F) 扩展内容

}
