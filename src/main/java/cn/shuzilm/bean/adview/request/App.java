package cn.shuzilm.bean.adview.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: app 应用信息
 * @Author: houkp
 * @CreateDate: 2018/7/10 16:16
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/10 16:16
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class App implements Serializable {
    private String id;//(T) App 唯一标识，由 AdView 生成
    private String name;//(T) App 名称
    private String domain;//(F)  官网地址
    private String[] cat;//(F) App 类型，参考附录 6 [
    private String ver;//(F)  App 版本
    private String bundle;//(F) App 程序主包名
    private Integer paid;//(F) 是否为付费 App：0- 不是 1- 是付费 2- 应用内付费   0
    private String keywords;//(F) App 关键字，可以以逗号分隔多个
    private String storeurl;//(F) 市场下载地址
    private RequestExt ext;//(F) 扩展内容

}
