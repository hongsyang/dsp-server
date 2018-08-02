package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.adview.response.BidResponseBean;
import cn.shuzilm.bean.adview.response.SeatBid;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.bean.lj.response.LJBid;
import cn.shuzilm.bean.lj.response.LJResponseExt;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.util.FilterRule;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: LingjiParser 灵集post参数解析
 * @Author: houkp
 * @CreateDate: 2018/7/20 14:37
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/20 14:37
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class LingJiParser implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(LingJiParser.class);

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
            BeanUtils.copyProperties(sourceDuFlowBean, targetDuFlowBean);
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
        } else {
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
        List<LJBid> bidList = new ArrayList<LJBid>();//注意第二层数组 针对单次曝光的出价
        SeatBid seatBid = new SeatBid();
        seatBid.setSeat(duFlowBean.getSeat());//SeatBid 的标识,由 DSP 生成
        LJBid bid = new LJBid();
        List<Impression> imp = duFlowBean.getImpression();//从bidRequestBean里面取
        Impression impression = imp.get(0);
        bid.setId(format);
        bid.setImpid(impression.getId());//从bidRequestBean里面取
        bid.setAdid(duFlowBean.getAdUid());//广告id，对应duFlowBean的AdUid；
        bid.setAdm(duFlowBean.getAdm());// 广告物料数据
        bid.setPrice(600);//duFlowBean.getPrice() 测试值  //CPM 出价，数值为 CPM 实际价格*10000，如出价为 0.6 元，
        bid.setCrid("1000014");//duFlowBean.getCrid() 测试值//广告物料 ID  ,投放动态创意(即c类型的物料),需添加该字段
        //曝光nurl
        String nurl = "http://101.200.56.200:8880/" + "lingjiexp?" +
                "id=" + duFlowBean.getRequestId() +
                "&bidid=" + bidResponseBean.getBidid() +
                "&impid=" + impression.getId() +
                "&price=" + duFlowBean.getActualPrice() +
                "&act=" + format +
                "&adx=" + duFlowBean.getAdxId() +
                "&did=" + duFlowBean.getDid() +
                "&device=" + duFlowBean.getDeviceId() +
                "&app=" + duFlowBean.getAppId() +
                "&appn=" + duFlowBean.getAppPackageName() +
                "&appv=" + duFlowBean.getAppVersion() +
                "&pmp=" + duFlowBean.getDealid();
        bid.setNurl(nurl);

        String curl = "http://101.200.56.200:8880/" + "lingjiclick?" +
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
        LJResponseExt ljResponseExt = new LJResponseExt();
        ljResponseExt.setLdp(curl);
        ljResponseExt.setCm(curls);
        ljResponseExt.setPm(curls);
        bid.setExt(ljResponseExt);

        //添加到list中
        bidList.add(bid);
        seatBid.setBid(bidList);
        seatBidList.add(seatBid);
        bidResponseBean.setSeatbid(seatBidList);
        return bidResponseBean;
    }
}
