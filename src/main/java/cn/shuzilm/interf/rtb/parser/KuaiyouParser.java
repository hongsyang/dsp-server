package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.adview.response.Bid;
import cn.shuzilm.bean.adview.response.BidResponseBean;
import cn.shuzilm.bean.adview.response.SeatBid;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @Description:    KuaiyouParser 快友post参数解析
* @Author:         houkp
* @CreateDate:     2018/7/20 14:39
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/20 14:39
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public class KuaiyouParser implements RequestService{

    private static final Logger log = LoggerFactory.getLogger(RequestServiceImpl.class);
    @Override
    public String parseRequest(String dataStr) {
        String response=null;
        log.debug(" BidRequest参数入参：{}", dataStr);
        //请求报文解析
        BidRequestBean bidRequestBean = JSON.parseObject(dataStr, BidRequestBean.class);

        //创建返回结果
        BidResponseBean bidResponseBean =new BidResponseBean();
        //请求报文BidResponse返回
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String format = LocalDateTime.now().format(formatter);//时间戳
        bidResponseBean.setId(bidRequestBean.getId());
        bidResponseBean.setBidid("Bidid" + format);//BidResponse 的唯一标识,由 DSP生成
        List<SeatBid> seatBidList = new ArrayList<SeatBid>();//注意第一层数组  DSP出价
        List<Bid> bidList = new ArrayList<Bid>();//注意第二层数组 针对单次曝光的出价
        SeatBid seatBid = new SeatBid();
        seatBid.setSeat("seat" + format);//SeatBid 的标识,由 DSP 生成
        Bid bid = new Bid();
        bid.setAdid("adid" + format);//广告id，对应duFlowBean的AdUid；
        List<Impression> imp = bidRequestBean.getImp();
        Impression impression = imp.get(0);
        bid.setImpid(impression.getId());//从bidRequestBean里面取
        bid.setWurl("http://dsp.example.com/winnotice?price=" + "60000");//赢价通知，由 AdView 服务器 发出  编码格式的 CPM 价格*10000，如价格为 CPM 价格 0.6 元，则取值0.6*10000=6000。
        List<String> urls = new ArrayList<>();
        urls.add("http://dsp.example1.com");
        urls.add("http://dsp.example2.com");
        urls.add("http://dsp.example3.com");
        urls.add("http://dsp.example4.com");
        Map nurlMap = new HashMap();
        nurlMap.put(0, urls);
        bid.setNurl(nurlMap);//带延迟的展示汇报，由客户端发送
        bid.setAdmt(4);//广告类型
        bid.setPrice(6000);//CPM 出价，数值为 CPM 实际价格*10000，如出价为 0.6 元，
        bid.setCurl(urls);//点击监控地址，客户端逐个发送通知
        bid.setCrid("crid" + format);//广告物料 ID
        String adm = "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />";
        bid.setAdm(adm);// 广告物料数据
        bid.setAdh(50);//广告物料高度
        bid.setAdw(320);//广告物料宽度
        bid.setAdct(1);// 广告点击行为类型，参考附录 9
        bid.setCid("cid" + format);//广告创意 ID，可用于去重
        //添加到list中
        bidList.add(bid);
        seatBid.setBid(bidList);
        seatBidList.add(seatBid);
        bidResponseBean.setSeatBid(seatBidList);
        response= JSON.toJSONString(bidResponseBean);
        log.debug("bidResponseBean:{}",response);
        return response;
    }

}
