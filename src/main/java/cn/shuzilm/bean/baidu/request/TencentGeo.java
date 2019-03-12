package cn.shuzilm.bean.baidu.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TencentGeo implements Serializable {

    private Integer latitude;//否 纬度 * 1 000 000。 采用火星坐标系
    private Integer longitude;//否 经度 * 1 000 000。 采用火星坐标系
    private Double accuracy;//否 经纬度精度半径。 单位： 米
}
