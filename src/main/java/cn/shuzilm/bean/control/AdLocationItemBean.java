package cn.shuzilm.bean.control;

/**
 * Created by thunders on 2018/7/10.
 * 广告位详情
 */
public class AdLocationItemBean implements ICommand{
	
	private static final long serialVersionUID = 8010863497041613773L;

	/**
	 * 广告位ID
	 */
	private Integer placementId;
	
	/**
	 * 宽
	 */
	private Integer width;
	
	/**
	 * 高
	 */
	private Integer height; 
	
	public Integer getPlacementId() {
		return placementId;
	}

	public void setPlacementId(Integer placementId) {
		this.placementId = placementId;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}
	
}
