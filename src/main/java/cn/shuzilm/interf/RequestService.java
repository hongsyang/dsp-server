package cn.shuzilm.interf;

import cn.shuzilm.bean.BidRequestBean;

/**
* @Description:    Bid Request 解析
* @Author:         houkp
* @CreateDate:     2018/7/10 11:20
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/10 11:20
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public interface RequestService {

 //根据厂商名字区分

    /**
     *  解析post提交报文，转换为BidRequestBean
     * @param dataStr 报文入参
     * @return
     */
    public BidRequestBean parseRequest(String dataStr);
}
