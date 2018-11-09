package cn.shuzilm.bean.control;

import lombok.Getter;
import lombok.Setter;

/**
 * @author wanght
 *
 */
@Getter
public class AdNoticeDetailBean {
	
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
     * 曝光数
     */
    @Setter
    private long winNums;
    
    /**
     * 点击数
     */
    @Setter
    private long clickNums;
    
    /**
     * RTB请求数
     */
    @Setter
    private long bidNums;
    
    /**
     * 曝光率
     */
    @Setter
    private double winRatio;
    
    /**
     * 点击率
     */
    @Setter
    private float clickRatio;
}
