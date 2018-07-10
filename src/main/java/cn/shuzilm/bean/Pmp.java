package cn.shuzilm.bean;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    Pmp 私有交易
* @Author:         houkp
* @CreateDate:     2018/7/10 16:01
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/10 16:01
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class Pmp implements Serializable {
    private Integer  private_auction;//(T)拍卖资格的指标，在直接交易对象命名，其中 0 =所有投标被接受，1 =投标被限制在规定的交易和条款。
    private Deal[]  deals;//(F)传达特定的交易。
    private RequestExt ext;//（F) 扩展内容

}
