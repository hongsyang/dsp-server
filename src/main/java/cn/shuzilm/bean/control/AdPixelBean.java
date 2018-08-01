package cn.shuzilm.bean.control;

import lombok.Data;

/**
 * 从 PIXEL 服务器获得的 win notice 和 点击请求
 *
 * Created by thunders on 2018/7/11.
 */
@Data
public class AdPixelBean implements ICommand{
    private String host;//根据配置文件获取
    private String adUid;//广告id
      private String adName;//可以留空
    /**
     * 实际耗费的金额
     */
    private float money;
    /**
     * 实际耗费的量
     */
    private long winNoticeNums;

}
