package cn.shuzilm.bean.control;

import lombok.Getter;
import lombok.Setter;

@Getter
public class FlowControlBean {
	
	/**
	 * ADX或者APP ID
	 */
	@Setter
	private String aid;
	
	/**
	 * 名称
	 */
	@Setter
	private String name;
	
	/**
	 * 最低流量限制：(如果昨日同小时流量小于该值，则今日同小时不做限制/如果大于，才做限制)
	 */
	@Setter
	private int lowFlows;
	
	/**
	 * 流量控制百分比(百分制)
	 */
	@Setter
	private int flowControlRatio;
	
	/**
	 * 类型(1:adx / 2:app)
	 */
	@Setter
	private int type;
	
	/**
	 * 流量控制开关(1:开启/0:关闭)
	 */
	@Setter
	private int status;
	
	/**
	 * 广告投放开关(1:开启/0:关闭)
	 */
	@Setter
	private int putAdStatus;
}
