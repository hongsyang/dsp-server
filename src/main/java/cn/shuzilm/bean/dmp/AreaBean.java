package cn.shuzilm.bean.dmp;

import cn.shuzilm.bean.control.ICommand;
import lombok.Data;

/**
 * 省份、地级市、县级市
 * Created by thunders on 2018/7/27.
 */
@Data
public class AreaBean implements ICommand {
    private int provinceId;//省编码
    private String provinceName;//省名称
    private int cityId;//市编码
    private String cityName;//市名称
    private int countyId;//县编码
    private String countyName;//县名称

}
