package cn.shuzilm.bean.control;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
public class AdLogBean {
	
	/**
     * 广告单元ID
     */
    @Setter
    private String adUid;    
    /**
     * 广告名称
     */
    @Setter
    private String adName;
    /**
     * 广告主ID
     */
    @Setter
    private String advertiserUid;    
    /**
     * 广告主名称
     */
    @Setter
    private String advertiserName;
    /**
     * 创建时间
     */
    @Setter
    private Date createdAt;
    /**
     * 原因
     */
    @Setter
    private String reason;
    /**
     * 广告状态
     */
    @Setter
    private int status;
}
