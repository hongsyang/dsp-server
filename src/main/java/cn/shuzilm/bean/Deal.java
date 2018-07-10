package cn.shuzilm.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: Deal 传达特定的交易。
 * @Author: houkp
 * @CreateDate: 2018/7/10 16:35
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/10 16:35
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class Deal implements Serializable {
    private String id;//(T)直接交易的唯一标识。
    private Integer bidfloor;//(建议) CPM 底价，数值为 CPM 价格 *10000，如底价为 0.6 元，则该字段值为 6000
    private String bidfloorcur;//(F) 价格单位，取值: USD,RMB，缺省RMB
    private Integer at;//(T) 竞拍类型：0 – 最高价格成交 1 – 次高价格成交 2 - 约定价格成交（不参加竞价
    private String[] wseat;//(F) 允许投标交易白名单;省略意味着没有限制。
    private String[] wadomain;//(F)允许的广告商域名，省略意味着不限制广告商。   [“adview.com”]
    private  RequestExt requestExt;//(F) 扩展内容

}
