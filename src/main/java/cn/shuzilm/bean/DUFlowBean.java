package cn.shuzilm.bean;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Description: DUFlowBean 数盟流转Bean
 * @Author: houkp
 * @CreateDate: 2018/7/13 11:56
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/13 11:56
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class DUFlowBean implements Serializable {

    private String infoId;//上报信息的唯一ID
    private String requestId;//请求id
    private String deviceId;//   唯一识别用户
    private String createTime;// timestamp  该条信息的创建时间
    private Integer infoType;//tinyint  0 imp 1 click 2 play 3 close 4 land
    private String adUid;// varchar(36)  广告ID
    private String audienceuid;//  varchar(36)  人群ID
    private String advertiserUid;//  varchar(36)  广告主ID
    private String agencyUid;//varchar(36)  代理商ID
    private String creativeUid;//varchar(36)  创意ID
    private String province;//varchar(20)  省
    private String city;//varchar(20)  市

    public static void main(String[] args) {
        LocalDateTime ldt = LocalDateTime.now();
        ZonedDateTime zdt = ldt.atZone(ZoneOffset.UTC); //you might use a different zone
        String iso8601 = zdt.toString();
        System.out.println(iso8601);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String format = LocalDateTime.now().format(formatter);
        System.out.println(format);
    }
}
