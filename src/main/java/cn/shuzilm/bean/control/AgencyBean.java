package cn.shuzilm.bean.control;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by thunders on 2018/8/23.
 */
@Data
public class AgencyBean implements Serializable{
    private String uid;
    private String name;
    private String remark;
    private String company;
    private String abbr; //简称
    private double rebate;

}
