package cn.shuzilm.bean.tencent.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TencentBidResponse implements Serializable {
     private String  request_id ;//ADX 提供的 Bid Request唯一标识
     private List<TencentSeatBid> seat_bids ;//DSP 的 出 价 集 合 , 每 个seat_bid 对 应 BidRequest 中 的 一 个impression
     private Integer  processing_time_ms ;//收到 Bid Request 至发送完 Bid Response 的用时。 单位： 毫秒
}
