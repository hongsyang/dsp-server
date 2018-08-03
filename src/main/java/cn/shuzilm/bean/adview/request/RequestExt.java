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
    private Integer gdpr;//(F) Regs.ext  Whether or not the request issubject to GDPR.0:the request is not subject to GDPR1:the request is subject to GDPR  0

    private Integer media_source;// 流量来源，1-Xtrader 2-GroupM
    private Integer sourceid;//视频流量来源，3-搜狐 4-优酷 6-爱奇艺 16-芒果TV

}
