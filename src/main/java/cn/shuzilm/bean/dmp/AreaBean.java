package cn.shuzilm.bean.dmp;

import cn.shuzilm.bean.control.ICommand;
import lombok.Data;

/**
 * 省份、地级市、县级市
 * Created by thunders on 2018/7/27.
 */
@Data
public class AreaBean implements ICommand {
    private int provinceId;
    private String provinceName;
    private int cityId;
    private String cityName;
    private int countyId;
    private String countyName;

}
