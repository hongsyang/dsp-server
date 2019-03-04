package cn.shuzilm.bean.control;

import java.util.List;

/**
 * Created by thunders on 2018/7/10.
 * 广告位
 */
public class AdLocationBean implements ICommand{

	private static final long serialVersionUID = -1221535425557016424L;
	
	private Long id;
	
	/**
	 * ADXID
	 */
	private Long adxId;
	
	/**
	 * 媒体ID
	 */
	private Long mediaId;
	
	/**
	 * 广告位ID
	 */
	private String adLocationId;
	
	/**
	 * 广告位类型
	 */
	private String type;
	
	private String fields;
	
	/**
	 * 是否启用
	 */
	private boolean active;
	
	/**
	 * 是否删除
	 */
	private boolean deleted;
	
	private AdLocationItemBean adLocationItem;
	
	private MediaBean media;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAdxId() {
		return adxId;
	}

	public void setAdxId(Long adxId) {
		this.adxId = adxId;
	}

	public Long getMediaId() {
		return mediaId;
	}

	public void setMediaId(Long mediaId) {
		this.mediaId = mediaId;
	}

	public String getAdLocationId() {
		return adLocationId;
	}

	public void setAdLocationId(String adLocationId) {
		this.adLocationId = adLocationId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFields() {
		return fields;
	}

	public void setFields(String fields) {
		this.fields = fields;
	}

	public AdLocationItemBean getAdLocationItem() {
		return adLocationItem;
	}

	public void setAdLocationItem(AdLocationItemBean adLocationItem) {
		this.adLocationItem = adLocationItem;
	}

	public MediaBean getMedia() {
		return media;
	}

	public void setMedia(MediaBean media) {
		this.media = media;
	}
	
	
}
