package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.adview.response.Bid;
import cn.shuzilm.bean.adview.response.BidResponseBean;
import cn.shuzilm.bean.adview.response.SeatBid;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.jedis.JedisManager;
import org.springframework.beans.BeanUtils;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.util.FilterRule;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: KuaiyouParser 快友post参数解析
 * @Author: houkp
 * @CreateDate: 2018/7/20 14:39
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/20 14:39
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class AdViewParser implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(AdViewParser.class);

    @Override
    public String parseRequest(String dataStr) {
        String response = "";
        log.debug(" BidRequest参数入参：{}", dataStr);
        //请求报文解析
        BidRequestBean bidRequestBean = JSON.parseObject(dataStr, BidRequestBean.class);
        //初步过滤规则
        if (FilterRule.filterRuleBidRequest(bidRequestBean)) {
            //创建返回结果
            DUFlowBean sourceDuFlowBean = new DUFlowBean();
            sourceDuFlowBean.setRequestId(bidRequestBean.getId());
            sourceDuFlowBean.setImpression(bidRequestBean.getImp());
            sourceDuFlowBean.setDeviceId(bidRequestBean.getDevice().getDidmd5());
            DUFlowBean targetDuFlowBean = new DUFlowBean();  //Todo 规则引擎 等待写入数据
            BeanUtils.copyProperties(sourceDuFlowBean,targetDuFlowBean);
//            BeanUtil.copyPropertyByNotNull(sourceDuFlowBean, targetDuFlowBean);
            log.debug("拷贝targetDuFlowBean:{}", targetDuFlowBean);
            BidResponseBean bidResponseBean = convertBidResponse(targetDuFlowBean);
            Jedis jedis = JedisManager.getInstance().getResource();
            if (jedis != null) {
                log.debug("jedis：{}", jedis);
                jedis.set(bidResponseBean.getId(), JSON.toJSONString(targetDuFlowBean));
                jedis.expire(bidResponseBean.getId(), 5 * 60);//设置超时时间为5分钟
            } else {
                log.debug("jedis为空：{}", jedis);
            }
            response = JSON.toJSONString(bidResponseBean);
            log.debug("bidResponseBean:{}", response);
        }else {
            response = "参数不合规";
        }
        return response;
    }


    private BidResponseBean convertBidResponse(DUFlowBean duFlowBean) {
        BidResponseBean bidResponseBean = new BidResponseBean();
        //请求报文BidResponse返回
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String format = LocalDateTime.now().format(formatter);//时间戳
        bidResponseBean.setId(duFlowBean.getRequestId());//从bidRequestBean里面取 bidRequest的id
        bidResponseBean.setBidid(duFlowBean.getBidid() + format);//BidResponse 的唯一标识,由 DSP生成
        List<SeatBid> seatBidList = new ArrayList<SeatBid>();//注意第一层数组  DSP出价 目前仅支持一个
        List<Bid> bidList = new ArrayList<Bid>();//注意第二层数组 针对单次曝光的出价
        SeatBid seatBid = new SeatBid();
        seatBid.setSeat(duFlowBean.getSeat());//SeatBid 的标识,由 DSP 生成
        Bid bid = new Bid();
        List<Impression> imp = duFlowBean.getImpression();//从bidRequestBean里面取
        Impression impression = imp.get(0);
        bid.setImpid(impression.getId());//从bidRequestBean里面取
        bid.setAdid(duFlowBean.getAdUid());//广告id，对应duFlowBean的AdUid；
        //曝光url
        String wurl = "http://localhost:8880/" + "adviewexp?" +
                "price=" + duFlowBean.getActualPrice() +
                "&act=" + format +
                "&adx=" + duFlowBean.getAdxId() +
                "&did=" + duFlowBean.getDid() +
                "&device=" + duFlowBean.getDeviceId() +
                "&app=" + duFlowBean.getAppId() +
                "&appn=" + duFlowBean.getAppPackageName() +
                "&appv=" + duFlowBean.getAppVersion() +
                "&req=" + duFlowBean.getRequestId() +
                "&imp=" + impression.getId() +
                "&pmp=" + duFlowBean.getDealid();
        bid.setWurl(wurl);//赢价通知，由 AdView 服务器 发出  编码格式的 CPM 价格*10000，如价格为 CPM 价格 0.6 元，则取值0.6*10000=6000。

//        List<String> urls = new ArrayList<>();
//        urls.add("http://dsp.example1.com");
//        urls.add("http://dsp.example2.com");
//        urls.add("http://dsp.example3.com");
//        urls.add("http://dsp.example4.com");
//        Map nurlMap = new HashMap();
//        nurlMap.put("0", urls);
//        bid.setNurl(nurlMap);//带延迟的展示汇报，由客户端发送//TODO 确认一下
        String curl = "http://dsp.example.com/" + "adviewclick?" +
                "price=" + duFlowBean.getActualPrice() +
                "&actualCreateTime=" + format +
                "&adxId=" + duFlowBean.getAdxId() +
                "&did=" + duFlowBean.getDid() +
                "&deviceId=" + duFlowBean.getDeviceId() +
                "&appId=" + duFlowBean.getAppId() +
                "&appPackageName=" + duFlowBean.getAppPackageName() +
                "&appVersion=" + duFlowBean.getAppVersion() +
                "&requestId=" + duFlowBean.getRequestId() +
                "&impId=" + impression.getId() +
                "&pmpId=" + duFlowBean.getDealid();
        List curls = new ArrayList();
        curls.add(curl);
        bid.setAdmt(duFlowBean.getAdmt());//广告类型
        bid.setPrice(duFlowBean.getPrice());//CPM 出价，数值为 CPM 实际价格*10000，如出价为 0.6 元，
        bid.setCurl(curls);//点击监控地址，客户端逐个发送通知 //TODO 等待确认
        bid.setCrid(duFlowBean.getCrid());//广告物料 ID
        bid.setAdm(duFlowBean.getAdm());// 广告物料数据
        bid.setAdh(duFlowBean.getAdw());//广告物料高度
        bid.setAdw(duFlowBean.getAdh());//广告物料宽度
        bid.setAdct(duFlowBean.getAdct());// 广告点击行为类型，参考附录 9
        bid.setCid(duFlowBean.getCreativeUid());//广告创意 ID，可用于去重
        //添加到list中
        bidList.add(bid);
        seatBid.setBid(bidList);
        seatBidList.add(seatBid);
        bidResponseBean.setSeatbid(seatBidList);
        return bidResponseBean;
    }

}
