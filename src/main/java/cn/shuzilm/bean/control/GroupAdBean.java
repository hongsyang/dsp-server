package cn.shuzilm.bean.control;

import java.math.BigDecimal;

/**
 * Created by thunders on 2018/7/18.
 */
public class GroupAdBean implements ICommand {
    private String groupId;
    private String adviserId;
    private String groupName;
    /**
     * 每日限额开关
     */
    private int quota; 
    /**
     * 每日限额
     */
    private BigDecimal quotaMoney;
    /**
     * 总限额开关
     */
    private int quota_total;
    /**
     * 总限额
     */
    private BigDecimal quotaTotalMoney;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAdviserId() {
        return adviserId;
    }

    public void setAdviserId(String adviserId) {
        this.adviserId = adviserId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public BigDecimal getQuotaMoney() {
        return quotaMoney;
    }

    public void setQuotaMoney(BigDecimal quotaMoney) {
        this.quotaMoney = quotaMoney;
    }

	public int getQuota() {
		return quota;
	}

	public void setQuota(int quota) {
		this.quota = quota;
	}

	public int getQuota_total() {
		return quota_total;
	}

	public void setQuota_total(int quota_total) {
		this.quota_total = quota_total;
	}

	public BigDecimal getQuotaTotalMoney() {
		return quotaTotalMoney;
	}

	public void setQuotaTotalMoney(BigDecimal quotaTotalMoney) {
		this.quotaTotalMoney = quotaTotalMoney;
	}
}
