package cn.shuzilm.interf.pixcel.parser;
/**
* @Description:    ParameterParser  url请求报文解析
* @Author:         houkp
* @CreateDate:     2018/7/19 18:36
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/19 18:36
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public interface ParameterParser {
    /**
     * 解析ADX服务商的get请求
     * @param url
     * @return
     */
    public String parseUrl(String url);
}
