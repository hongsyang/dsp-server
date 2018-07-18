package cn.shuzilm.bean.control;

import java.math.BigDecimal;

/**
 * Created by thunders on 2018/7/18.
 */
public class GroupAdBean implements ICommand {
    private String groupId;
    private String adviserId;
    private String groupName;
    private BigDecimal quotaMoney;

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
}
