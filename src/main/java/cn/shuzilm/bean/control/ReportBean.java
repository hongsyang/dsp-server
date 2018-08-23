package cn.shuzilm.bean.control;

import java.math.BigDecimal;

/**
 * Created by thunders on 2018/7/18.
 */
public class ReportBean implements ICommand {
    private String adUid;
    private String adviserUid;
    private String agency_uid;
    /**
     * 曝光次数
     */
    private int impNums;
    /**
     * 独立曝光用户数
     */
    private int uImpNums;
    /**
     * 点击次数
     */
    private int clickNums;
    /**
     * 独立点击用户数
     */
    private int uClickNums;
    /**
     * 该广告下广告主的账户每日限额
     */
    private BigDecimal moneyQuota;

    /**
     * 该广告主账户下余额
     */
    private BigDecimal balance;
    /**
     * 消耗金额
     */
    private BigDecimal expense;
    /**
     * 成本
     */
    private BigDecimal cost;

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getMoneyQuota() {
        return moneyQuota;
    }

    public void setMoneyQuota(BigDecimal moneyQuota) {
        this.moneyQuota = moneyQuota;
    }

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
