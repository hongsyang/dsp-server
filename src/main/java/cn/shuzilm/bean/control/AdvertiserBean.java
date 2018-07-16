package cn.shuzilm.bean.control;

/**
 * Created by thunders on 2018/7/10.
 * 广告主
 */
public class AdvertiserBean {
    private String uid;
    private String name;

    private String agencyUid;
    /**
     * 每天消耗金额总量
     */
    private float moneyDaily;
    /**
     * 每天消耗总量
     */
    private long amountDaily;
    /**
     * 每小时金额消耗
     */
    private float moneyHour;
    /**
     * 每小时流量消耗
     */
    private long amountHour;

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

    public String getAgencyUid() {
        return agencyUid;
    }

    public void setAgencyUid(String agencyUid) {
        this.agencyUid = agencyUid;
    }

    public float getMoneyDaily() {
        return moneyDaily;
    }

    public void setMoneyDaily(float moneyDaily) {
        this.moneyDaily = moneyDaily;
    }

    public long getAmountDaily() {
        return amountDaily;
    }

    public void setAmountDaily(long amountDaily) {
        this.amountDaily = amountDaily;
    }

    public float getMoneyHour() {
        return moneyHour;
    }

    public void setMoneyHour(float moneyHour) {
        this.moneyHour = moneyHour;
    }

    public long getAmountHour() {
        return amountHour;
    }

    public void setAmountHour(long amountHour) {
        this.amountHour = amountHour;
    }
}
