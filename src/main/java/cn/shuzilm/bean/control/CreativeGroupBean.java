package cn.shuzilm.bean.control;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import lombok.Setter;

/**
 * Created by thunders on 2018/7/10.
 * 关联创意
 */
public class CreativeGroupBean implements ICommand {

	private static final long serialVersionUID = 3685561606501388532L;
	
	/**
	 * 唯一标识
	 */
	private String uid;
	
	/**
	 * 名称
	 */
	private String name;
	
	/**
	 * 广告单元ID
	 */
	private String adUid;
	
	/**
     * 点击动作：0未确认/1打开网页/2下载应用/3播放视频/4打开应用/5打开Deeplink目标/6打开地图/7拨打电话/8发送短信/9其它
     */
    private int link_type;
	
    /**
     * 点击目标链接（含协议头）
     */
    private String link;
    
    /**
     * 曝光监测链接（null为无曝光监测）
     */
    private String tracking;
    
    /**
     * 创意所属的广告行业ID
     */
    private Integer tradeId;
    
    /**
     * 创意列表
     */
    private List<CreativeBean> creativeList;
    
    /**
     * 点击检测链接
     */
    private String clickTrackingUrl;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLink_type() {
		return link_type;
	}

	public void setLink_type(int link_type) {
		this.link_type = link_type;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTracking() {
		return tracking;
	}

	public void setTracking(String tracking) {
		this.tracking = tracking;
	}

	public String getClickTrackingUrl() {
		return clickTrackingUrl;
	}

	public void setClickTrackingUrl(String clickTrackingUrl) {
		this.clickTrackingUrl = clickTrackingUrl;
	}

	public List<CreativeBean> getCreativeList() {
		return creativeList;
	}

	public void setCreativeList(List<CreativeBean> creativeList) {
		this.creativeList = creativeList;
	}

	public String getAdUid() {
		return adUid;
	}

	public void setAdUid(String adUid) {
		this.adUid = adUid;
	}

	public Integer getTradeId() {
		return tradeId;
	}

	public void setTradeId(Integer tradeId) {
		this.tradeId = tradeId;
	}
   
}
