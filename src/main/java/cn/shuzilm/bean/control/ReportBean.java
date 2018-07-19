package cn.shuzilm.bean.control;

import java.math.BigDecimal;

/**
 * Created by thunders on 2018/7/18.
 */
public class ReportBean implements ICommand {
    private String adUid;
    private String adviserUid;
    private String agency_uid;
    private int impNums;
    private int uImpNums;
    private int clickNums;
    private int uClickNums;
    /**
     * 消耗金额
     */
    private BigDecimal expense;
    /**
     * 成本
     */
    private BigDecimal cost;

    public String getAdUid() {
        return adUid;
    }

    public void setAdUid(String adUid) {
        this.adUid = adUid;
    }

    public String getAdviserUid() {
        return adviserUid;
    }

    public void setAdviserUid(String adviserUid) {
        this.adviserUid = adviserUid;
    }

    public String getAgency_uid() {
        return agency_uid;
    }

    public void setAgency_uid(String agency_uid) {
        this.agency_uid = agency_uid;
    }

    public int getImpNums() {
        return impNums;
    }

    public void setImpNums(int impNums) {
        this.impNums = impNums;
    }

    public int getuImpNums() {
        return uImpNums;
    }

    public void setuImpNums(int uImpNums) {
        this.uImpNums = uImpNums;
    }

    public int getClickNums() {
        return clickNums;
    }

    public void setClickNums(int clickNums) {
        this.clickNums = clickNums;
    }

    public int getuClickNums() {
        return uClickNums;
    }

    public void setuClickNums(int uClickNums) {
        this.uClickNums = uClickNums;
    }

    public BigDecimal getExpense() {
        return expense;
    }

    public void setExpense(BigDecimal expense) {
        this.expense = expense;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
}
