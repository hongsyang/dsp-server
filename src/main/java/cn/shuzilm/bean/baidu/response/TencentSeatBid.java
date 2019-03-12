package cn.shuzilm.bean.baidu.response;

import cn.shuzilm.bean.tencent.response.TencentBid;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TencentSeatBid implements Serializable {

    private String  impression_id ;//ADX 提供的 Bid Request唯一标识
    private List<TencentBid> bids ;//DSP 的 出 价 集 合 , 每
}
