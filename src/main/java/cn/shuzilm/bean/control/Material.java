package cn.shuzilm.bean.control;

public class Material {
	
	private String uid;
	private String nid;
	
	private String creativeUid;//所属广告创意ID
	
	/**
     * 创意类型： banner interstitial 插屏<br> fullscreen 全屏广告<br> feed 信息流 <br> text 文字链
     */
	private String type;
	private String fileName;//物料文件名
	private String ext;//文件扩展名/文件类型
	private int size;//文件大小(字节)
	private int width;//素材宽度
	private int height;//素材高度
	private long createdAt;//创建时间
	private String createdBy;//创建者
	private long updatedAt;//更新时间
	private String updatedBy;//更新者
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getNid() {
		return nid;
	}
	public void setNid(String nid) {
		this.nid = nid;
	}
	public String getCreativeUid() {
		return creativeUid;
	}
	public void setCreativeUid(String creativeUid) {
		this.creativeUid = creativeUid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getExt() {
		return ext;
	}
	public void setExt(String ext) {
		this.ext = ext;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public long getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public long getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(long updatedAt) {
		this.updatedAt = updatedAt;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	
	
	
}
