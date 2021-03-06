package cn.shuzilm.bean.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class Material implements ICommand {
	
	private static final long serialVersionUID = -6496094043819279538L;
	
	private String uid;
	private String nid;
	
	private String creativeUid;//所属广告创意ID
	
	/**
     * 创意类型： banner interstitial 插屏<br> fullscreen 全屏广告<br> feed 信息流 <br> text 文字链
     */
	//private String type;
	//private String fileName;//物料文件名
	//private String ext;//文件扩展名/文件类型
	//private int size;//文件大小(字节)
	//private int width;//素材宽度
	//private int height;//素材高度
	private long createdAt;//创建时间
	private String createdBy;//创建者
	private long updatedAt;//更新时间
	private String updatedBy;//更新者
	private int approved;//是否通过审批（0:待审批/1:通过/-1:拒绝）
	private String approved_adx;//适用平台
	
	private Set<String> approvedAdxSet;
	
	private int duration;//视频总时长
	
	private String auditId;//推审ID
	
	private Map<String,String> auditIdMap;
	
	private List<Image> imageList;

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
	public int getApproved() {
		return approved;
	}
	public void setApproved(int approved) {
		this.approved = approved;
	}
	public String getApproved_adx() {
		return approved_adx;
	}
	public void setApproved_adx(String approved_adx) {
		this.approved_adx = approved_adx;
		if (StringUtils.isNotBlank(approved_adx)) {
            String[] split = approved_adx.split(",");
            Set<String> set = new HashSet<String>();
            String re = "[";
            String ra = "]";
            for (String s : split) {
                String replace = s.replace(re, "").trim().replace(ra, "").replace("\"", "");
                set.add(replace);
            }
            this.approvedAdxSet = set;
        }
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public Set<String> getApprovedAdxSet() {
		return approvedAdxSet;
	}
	public String getAuditId() {
		return auditId;
	}
	public void setAuditId(String auditId) {
		this.auditId = auditId;
		if (StringUtils.isNotBlank(auditId)) {
			String[] split = auditId.split(",");
			Map<String,String> map = new HashMap<String,String>();
			String re = "{";
			String ra = "}";
			for(String s: split){
				String[] audSplit = s.replace(re, "").trim().replace(ra, "").replace("\"", "").split(":");
				map.put(audSplit[0], audSplit[1]);
			}
			this.auditIdMap = map;
		}
	}
	public Map<String, String> getAuditIdMap() {
		return auditIdMap;
	}
	
	public List<Image> getImageList() {
		return imageList;
	}
	public void setImageList(List<Image> imageList) {
		this.imageList = imageList;
	}
	public static void main(String[] args) {
		String s = "[1,\"3_111\"]";
		Material m = new Material();
		m.setApproved_adx(s);
		System.out.println(m.getApprovedAdxSet());
	}
	
}
