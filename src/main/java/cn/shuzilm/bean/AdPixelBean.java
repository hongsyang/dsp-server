package cn.shuzilm.bean;

/**
 * 从 PIXEL 服务器获得的 win notice 和 点击请求
 *
 * Created by thunders on 2018/7/11.
 */
public class AdPixelBean {
    private String host;
    private String adUid;
    private String adName;
    /**
     * 实际耗费的金额
     */
    private float money;
    /**
     * 实际耗费的量
     */
    private long winNoticeNums;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAdUid() {
        return adUid;
    }

    public void setAdUid(String adUid) {
        this.adUid = adUid;
    }

    public String getAdName() {
        return adName;
    }

    public void setAdName(String adName) {
        this.adName = adName;
    }

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

    public long getWinNoticeNums() {
        return winNoticeNums;
    }

    public void setWinNoticeNums(long winNoticeNums) {
        this.winNoticeNums = winNoticeNums;
    }
}
