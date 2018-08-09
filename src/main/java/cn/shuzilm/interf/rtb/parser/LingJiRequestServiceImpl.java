package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.adview.response.BidResponseBean;
import cn.shuzilm.bean.adview.response.SeatBid;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.bean.lj.response.LJBid;
import cn.shuzilm.bean.lj.response.LJResponseExt;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.filter.FilterRule;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: LingjiParser 灵集post参数解析
 * @Author: houkp
 * @CreateDate: 2018/7/20 14:37
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/20 14:37
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class LingJiRequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(LingJiRequestServiceImpl.class);

    private AppConfigs configs = null;

    private static final String FILTER_CONFIG = "filter.properties";


    @Override
    public String parseRequest(String dataStr) {
        String response = "空请求";
        if (StringUtils.isNotBlank(dataStr)) {
            this.configs = AppConfigs.getInstance(FILTER_CONFIG);
            log.debug(" BidRequest参数入参：{}", dataStr);
            Map msg = new HashMap();//过滤规则的返回结果
            //请求报文解析
            BidRequestBean bidRequestBean = JSON.parseObject(dataStr, BidRequestBean.class);
            //创建返回结果  bidRequest请求参数保持不变
            DUFlowBean sourceDuFlowBean = new DUFlowBean();
            if (bidRequestBean != null) {
                sourceDuFlowBean.setRequestId(bidRequestBean.getId());
                sourceDuFlowBean.setImpression(bidRequestBean.getImp());
                sourceDuFlowBean.setDeviceId(bidRequestBean.getDevice().getDidmd5());
            }
            //初步过滤规则开关
            if (Boolean.valueOf(configs.getString("FILTER_SWITCH"))) {
                if (FilterRule.filterRuleBidRequest(bidRequestBean, true, msg, "lingji")) {
                    DUFlowBean targetDuFlowBean = new DUFlowBean();  //Todo 规则引擎 等待写入数据
                    BeanUtils.copyProperties(sourceDuFlowBean, targetDuFlowBean);
                    log.debug("拷贝过滤通过的targetDuFlowBean:{}", targetDuFlowBean);
                    BidResponseBean bidResponseBean = convertBidResponse(targetDuFlowBean);
                    pushRedis(targetDuFlowBean);//上传到redis服务器
                    log.debug("json计数");
                    response = JSON.toJSONString(bidResponseBean);
                    log.debug("过滤通过的bidResponseBean:{}", response);
                } else {
                    response = JSON.toJSONString(msg);//过滤规则结果输出
                }
            } else {
                DUFlowBean targetDuFlowBean = new DUFlowBean();  //Todo 规则引擎 等待写入数据
                BeanUtils.copyProperties(sourceDuFlowBean, targetDuFlowBean);
                log.debug("拷贝没有过滤的targetDuFlowBean:{}", targetDuFlowBean);
                BidResponseBean bidResponseBean = convertBidResponse(targetDuFlowBean);
                pushRedis(targetDuFlowBean);//上传到redis服务器
                response = JSON.toJSONString(bidResponseBean);
                log.debug("没有过滤的bidResponseBean:{}", response);
            }
            return response;
        } else {
            return response;
        }
    }

    /**
     * 内部流转DUFlowBean  转换为  BidResponseBean 输出给 ADX服务器
     *
     * @param duFlowBean
     * @return
     */
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
        bid.setId(format);////DSP对该次出价分配的ID
        bid.setImpid(impression.getId());//从bidRequestBean里面取
        bid.setAdm(configs.getString("ADM"));//duFlowBean.getAdm() 广告物料数据
        //等待结果
//        Double biddingPrice = duFlowBean.getBiddingPrice()*100;
//        Integer price = Integer.valueOf(String.valueOf(biddingPrice));
        bid.setPrice(6.0f);//price 测试值  //CPM 出价，数值为 CPM 实际价格*10000，如出价为 0.6 元，
        bid.setCrid(configs.getString("CRID"));//duFlowBean.getCrid() 测试值//广告物料 ID  ,投放动态创意(即c类型的物料),需添加该字段
        //曝光nurl
        String nurl = "http://101.200.56.200:8880/" + "lingjiexp?" +
                "id=" + "${AUCTION_ID}" +
                "&bidid=" + "${AUCTION_BID_ID}" +
                "&impid=" + "${AUCTION_IMP_ID}" +
                "&price=" + "${AUCTION_PRICE}" +
                "&act=" + format +
                "&adx=" + duFlowBean.getAdxId() +
                "&did=" + duFlowBean.getDid() +
                "&device=" + duFlowBean.getDeviceId() +
                "&app=" + duFlowBean.getAppId() +
                "&appn=" + duFlowBean.getAppPackageName() +
                "&appv=" + duFlowBean.getAppVersion() +
                "&pf=" + duFlowBean.getPremiumFactor() +
                "&pmp=" + duFlowBean.getDealid();

        bid.setNurl(nurl);

        String curl = "http://101.200.56.200:8880/" + "lingjiclick?" +
                "id=" + duFlowBean.getRequestId() +
                "&bidid=" + bidResponseBean.getBidid() +
                "&impid=" + impression.getId() +
                "&price=" + 6 +
                "&act=" + format +
                "&adx=" + duFlowBean.getAdxId() +
                "&did=" + duFlowBean.getDid() +
                "&device=" + duFlowBean.getDeviceId() +
                "&app=" + duFlowBean.getAppId() +
                "&appn=" + duFlowBean.getAppPackageName() +
                "&appv=" + duFlowBean.getAppVersion() +
                "&pmp=" + duFlowBean.getDealid();
        List curls = new ArrayList();
        curls.add(curl);
        LJResponseExt ljResponseExt = new LJResponseExt();
        ljResponseExt.setLdp("https://www.shuzilm.cn/");//点击目标URL。广告点击后会跳转到物料上绑定的landingpage，还是取实时返回的ldp，参见
        ljResponseExt.setCm(curls);// 曝光监测URL，监测数组支持的曝光条数和广告展现时是否会发物料上绑定的monitor地址，参见
        ljResponseExt.setPm(curls);// 点击监测URL，监测数组支持的点击监测条数，参见
        bid.setExt(ljResponseExt);

        //添加到list中
        bidList.add(bid);
        seatBid.setBid(bidList);
        seatBidList.add(seatBid);
        bidResponseBean.setSeatbid(seatBidList);
        return bidResponseBean;
    }

    /**
     * 把生成的内部流转DUFlowBean上传到redis服务器 设置5分钟失效
     *
     * @param targetDuFlowBean
     */
    private void pushRedis(DUFlowBean targetDuFlowBean) {
        log.debug("redis计数");
        Jedis jedis = JedisManager.getInstance().getResource();
        if (jedis != null) {
            log.debug("jedis：{}", jedis);
            String set = jedis.set(targetDuFlowBean.getRequestId(), JSON.toJSONString(targetDuFlowBean));
            Long expire = jedis.expire(targetDuFlowBean.getRequestId(), 5 * 60);//设置超时时间为5分钟
            log.debug("推送到redis服务器是否成功;{},设置超时时间是否成功(成功返回1)：{}", set, expire);
        } else {
            log.debug("jedis为空：{}", jedis);
        }
    }
}
