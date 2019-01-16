package cn.shuzilm.bean.tencent.request;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    adx 信息
* @Author:         houkp
* @CreateDate:     2018/11/22 20:56
* @UpdateUser:     houkp
* @UpdateDate:     2018/11/22 20:56
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class YouYiExchange implements Serializable {

    private String version ;//  否 string adx proto version
    private String bid_id ;//  否 string bid id provided by exchange
    private Integer adx_id ;//  否 int32 adx id
}
