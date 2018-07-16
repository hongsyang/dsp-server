package cn.shuzilm.bean.control;

/**
 * 流量控制用的 BEAN
 * Created by thunders on 2018/7/11.
 */
public class AdFlowStatus {
    private String uid;
    private String name;
    private float money;
    private long amount;
    private long winNums;
    private long bidNums;

    public void reset(){
        money = 0;
        amount = 0;
        winNums = 0;
        bidNums = 0;
    }

    public long getWinNums() {
        return winNums;
    }

    public void setWinNums(long winNums) {
        this.winNums = winNums;
    }

    public long getBidNums() {
        return bidNums;
    }

    public void setBidNums(long bidNums) {
        this.bidNums = bidNums;
    }

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

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
