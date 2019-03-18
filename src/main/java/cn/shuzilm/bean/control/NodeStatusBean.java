package cn.shuzilm.bean.control;


import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作机节点状态Bean
 * User: wanght
 * Date: 2018-07-11
 * Time: 上午10:39
 *
 */
public class NodeStatusBean implements ICommand{
	private static final long serialVersionUID = 3456333997029863081L;
    private ArrayList<AdPixelBean> pixelList;
    private ArrayList<AdBidBean> bidList;

    private int nodeId;
    private String nodeName;

    private ConcurrentHashMap<String,String> dynamicMap;
    /**
     * 总的竞价次数
     */
    private long totalBids;
    /**
     * 总运行时间 ，单位：小时
     */
    private long runTime;
    /**
     * 广告主个数
     */
    private int advertiserNums;
    /**
     * 广告个数
     */
    private int adNums;
    
    /**
     * 节点最后上报心跳时间
     */
    private long lastUpdateTime;

    public ArrayList<AdPixelBean> getPixelList() {
        return pixelList;
    }

    public void setPixelList(ArrayList<AdPixelBean> pixelList) {
        this.pixelList = pixelList;
    }

    public ArrayList<AdBidBean> getBidList() {
        return bidList;
    }

    public void setBidList(ArrayList<AdBidBean> bidList) {
        this.bidList = bidList;
    }

    public String getHost(){
        return nodeName;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public long getTotalBids() {
        return totalBids;
    }

    public void setTotalBids(long totalBids) {
        this.totalBids = totalBids;
    }

    public long getRunTime() {
        return runTime;
    }

    public void setRunTime(long runTime) {
        this.runTime = runTime;
    }

    public int getAdvertiserNums() {
        return advertiserNums;
    }

    public void setAdvertiserNums(int advertiserNums) {
        this.advertiserNums = advertiserNums;
    }

    public int getAdNums() {
        return adNums;
    }

    public void setAdNums(int adNums) {
        this.adNums = adNums;
    }

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

    public ConcurrentHashMap<String, String> getDynamicMap() {
        return dynamicMap;
    }

    public void setDynamicMap(ConcurrentHashMap<String, String> dynamicMap) {
        this.dynamicMap = dynamicMap;
    }
}
