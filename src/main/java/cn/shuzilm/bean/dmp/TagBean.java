package cn.shuzilm.bean.dmp;

import cn.shuzilm.util.JsonTools;
import lombok.Data;

/**
 * Created by thunders on 2018/7/25.
 */
@Data
public class TagBean {
    /**
     * 该 TAG 所在标签管理表中的
     */
    private int tagId;
    private float[] work;
    private float[] residence;
    private float[] activity;
    private int incomeId;
    private String brand;
    private int platformId;
    private int networkId;
    private int carrierId;
    private String appPreferenceId;


    public static void main(String[] args) {
        TagBean tag = new TagBean();

        String json = JsonTools.toJsonString(tag);
        System.out.println(json);
    }



}
