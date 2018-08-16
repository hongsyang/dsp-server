package cn.shuzilm.bean.control;

import java.util.List;
import java.util.Set;

import lombok.Setter;

/**
 * Created by thunders on 2018/7/10.
 * 关联创意
 */
public class CreativeBean implements ICommand {
    private String uid;
    private String name;
    private String title;
    private String titleShort;
    private String titleLong;
    private String desc;
    private String descShort;
    private String descLong;

    private String brand;
    private String domain;
    
    private List<Material> materialList;//物料集合
    
    /**
     * 点击动作：0未确认/1打开网页/2下载应用/3播放视频/4打开应用/5打开Deeplink目标/6打开地图/7拨打电话/8发送短信/9其它
     */
    private int link_type;
    
    /**
     * 点击目标链接（含协议头）
     */
    private String link;
    
    /**
     * 落地页链接（含协议头）
     */
    private String landing;
    
    /**
     * 曝光监测链接（null为无曝光监测）
     */
    private String tracking;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleShort() {
        return titleShort;
    }

    public void setTitleShort(String titleShort) {
        this.titleShort = titleShort;
    }

    public String getTitleLong() {
        return titleLong;
    }

    public void setTitleLong(String titleLong) {
        this.titleLong = titleLong;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDescShort() {
        return descShort;
    }

    public void setDescShort(String descShort) {
        this.descShort = descShort;
    }

    public String getDescLong() {
        return descLong;
    }

    public void setDescLong(String descLong) {
        this.descLong = descLong;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLanding() {
		return landing;
	}

	public void setLanding(String landing) {
		this.landing = landing;
	}

	public String getTracking() {
		return tracking;
	}

	public void setTracking(String tracking) {
		this.tracking = tracking;
	}

	public int getLink_type() {
		return link_type;
	}

	public void setLink_type(int link_type) {
		this.link_type = link_type;
	}

	public List<Material> getMaterialList() {
		return materialList;
	}

	public void setMaterialList(List<Material> materialList) {
		this.materialList = materialList;
	}
    
}
