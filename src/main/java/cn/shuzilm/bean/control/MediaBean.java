package cn.shuzilm.bean.control;

public class MediaBean {
	
	/**
	 * 媒体ID
	 */
	private Integer id;
	
	/**
	 * 包名
	 */
	private String packageName;
	
	/**
	 * 媒体名称
	 */
	private String appName;
	
	/**
	 * ADXID
	 */
	private Integer adxId;
	
	/**
	 * 媒体类型
	 */
	private Integer mediaType;
	
	/**
	 * 是否为其他
	 */
	private Integer setOther;
	
	/**
	 * 是否主媒体
	 */
	private Integer isMaster;
	
	/**
	 * 所属朱媒体ID
	 */
	private Integer masterMediaId;
	
	/**
	 * 启用状态
	 */
	private Integer opStatus;
	
	/**
	 * 媒体状态
	 */
	private Integer mediaStatus;
	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Integer getAdxId() {
		return adxId;
	}

	public void setAdxId(Integer adxId) {
		this.adxId = adxId;
	}

	public Integer getMediaType() {
		return mediaType;
	}

	public void setMediaType(Integer mediaType) {
		this.mediaType = mediaType;
	}

	public Integer getSetOther() {
		return setOther;
	}

	public void setSetOther(Integer setOther) {
		this.setOther = setOther;
	}

	public Integer getIsMaster() {
		return isMaster;
	}

	public void setIsMaster(Integer isMaster) {
		this.isMaster = isMaster;
	}

	public Integer getMasterMediaId() {
		return masterMediaId;
	}

	public void setMasterMediaId(Integer masterMediaId) {
		this.masterMediaId = masterMediaId;
	}

	public Integer getOpStatus() {
		return opStatus;
	}

	public void setOpStatus(Integer opStatus) {
		this.opStatus = opStatus;
	}

	public Integer getMediaStatus() {
		return mediaStatus;
	}

	public void setMediaStatus(Integer mediaStatus) {
		this.mediaStatus = mediaStatus;
	}
	
	
}
