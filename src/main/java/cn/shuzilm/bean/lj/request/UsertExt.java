package cn.shuzilm.bean.lj.request;

import lombok.Data;
/**
* @Description:    UsertExt
* @Author:         houkp
* @CreateDate:     2018/8/3 12:24
* @UpdateUser:     houkp
* @UpdateDate:     2018/8/3 12:24
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class UsertExt {
    private String[] models;//	灵集的DMP标签, 形式是字符串标签组成的数组，例如["10000", "10008"]，具体的标签说明参见
    private String consent;//(F)   User.ext  Consent string according toIAB's consent string format1.1  BOMT6szOMT6szAAABAENAAAAAAAAoAAA

}
