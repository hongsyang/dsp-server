package cn.shuzilm.bean.adview.request;

import lombok.Data;

import java.io.Serializable;

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
public class RequestExt implements Serializable {
    private String consent;//(F)   User.ext  Consent string according toIAB's consent string format1.1  BOMT6szOMT6szAAABAENAAAAAAAAoAAA
    private Integer gdpr;//(F) Regs.ext  Whether or not the request issubject to GDPR.0:the request is not subject to GDPR1:the request is subject to GDPR  0
    private String channel;//  灵集site.content.ext 	视频的频道ID，例如"1"。频道id需要与sourceid配合使用。视频流量的频道字典详见
    private String cs;//灵集site.content.ext  二级频道ID
    private String copyright;//灵集site.content.ext  版权信息 0---版权信息未知 1---有版权
    private String quality;//灵集site.content.ext   流量质量 1---流量质量保障 2---流量质量未知
    private String[] models;//灵集user
    private String showtype;//灵集imp
    private String required;//灵集imp
}
