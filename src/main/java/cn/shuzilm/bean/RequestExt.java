package cn.shuzilm.bean;

import lombok.Data;

/**
 * @Description: RequestExt  扩展内容
 * @Author: houkp
 * @CreateDate: 2018/7/10 16:08
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/10 16:08
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class RequestExt {
    private String consent;//(F)   User.ext  Consent string according toIAB's consent string format1.1  BOMT6szOMT6szAAABAENAAAAAAAAoAAA
    private Integer gdpr;//(F) Regs.ext  Whether or not the request issubject to GDPR.0:the request is not subject to GDPR1:the request is subject to GDPR  0
}
