package cn.shuzilm.bean.lj.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: AppExt
 * @Author: houkp
 * @CreateDate: 2018/8/3 12:33
 * @UpdateUser: houkp
 * @UpdateDate: 2018/8/3 12:33
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class AppExt  implements Serializable{
    private String sdk;// 投放SDK的版本，例如“91_v1”
    private Integer market;// 应用商店，1—iOS Appstore，2—Google Play，3—91Market。
    private Integer deeplink;// 	是否支持应用直达 0-不支持(默认) 1-支持
}
