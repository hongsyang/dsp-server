package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.util.UrlParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

/**
* @Description:    ExposureParser  曝光量解析
* @Author:         houkp
* @CreateDate:     2018/7/19 15:57
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/19 15:57
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public class ExpParser  implements  ParameterParser{

    private static final Logger log = LoggerFactory.getLogger(ExpParser.class);


    @Override
    public String parseUrl(String url) {

        return "曝光量解析";
    }
}
