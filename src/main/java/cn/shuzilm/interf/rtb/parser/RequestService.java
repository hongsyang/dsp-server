package cn.shuzilm.interf.rtb.parser;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

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

    /**
     *  解析post提交报文，转换为BidResponseBean
     * @param dataStr 报文入参
     * @return
     */
    public String parseRequest(String dataStr , ExecutorService executor,ConcurrentHashMap<String,Integer> countMap) throws Exception;


}
