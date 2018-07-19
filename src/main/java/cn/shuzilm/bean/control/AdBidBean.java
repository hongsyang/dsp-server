package cn.shuzilm.bean.control;

/**
 * Created by thunders on 2018/7/11.
 */
public class AdBidBean implements ICommand{
    private String uid;
    private String name;
    private long bidNums;
    private String host;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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

    public long getBidNums() {
        return bidNums;
    }

    public void setBidNums(long bidNums) {
        this.bidNums = bidNums;
    }
}
