package cn.shuzilm.bean;

import lombok.Data;

/**
 * @Description: Regs请求中的对象，包含了相关法律、政府或行业法规。
 * @Author: houkp
 * @CreateDate: 2018/7/10 16:07
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/10 16:07
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class Regs {


    /*    适宜儿童内容标示，美国地区流量必须；
    coppa=0 标示你的应用内容根据
    COPPA 条例不可以被认为是面向儿童
            (child-direct) coppa=1 标示你的应
    用内容根据 COPPA 条例可以被认为是面
    向儿童(child-direct)*/
    private Integer coppa;
    private RequestExt ext;//(F) 扩展内容

}
