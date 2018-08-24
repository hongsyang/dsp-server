package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.bean.adview.request.*;
import cn.shuzilm.bean.adview.response.*;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.bean.lj.request.*;
import cn.shuzilm.bean.lj.response.LJLink;
import cn.shuzilm.bean.lj.response.LJNativeResponse;
import cn.shuzilm.bean.lj.response.NativeAD;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import cn.shuzilm.filter.FilterRule;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: KuaiyouParser 快友post参数解析
 * @Author: houkp
 * @CreateDate: 2018/7/20 14:39
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/20 14:39
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class AdViewRequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(AdViewRequestServiceImpl.class);


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
            sourceDuFlowBean.setRequestId(bidRequestBean.getId());
            sourceDuFlowBean.setImpression(bidRequestBean.getImp());
            sourceDuFlowBean.setDeviceId(bidRequestBean.getDevice().getDidmd5());
            //初步过滤规则开关
            if (Boolean.valueOf(configs.getString("FILTER_SWITCH"))) {
                if (FilterRule.filterRuleBidRequest(bidRequestBean, true, msg, "adview")) {
                    DUFlowBean targetDuFlowBean = new DUFlowBean();  //Todo 规则引擎 等待写入数据
                    BeanUtils.copyProperties(sourceDuFlowBean, targetDuFlowBean);
                    log.debug("拷贝过滤通过的targetDuFlowBean:{}", targetDuFlowBean);
                    BidResponseBean bidResponseBean = convertBidResponse(targetDuFlowBean,bidRequestBean);
                    pushRedis(targetDuFlowBean);//上传到redis服务器
                    response = JSON.toJSONString(bidResponseBean);
                    log.debug("过滤通过的bidResponseBean:{}", response);
                } else {
                    response = JSON.toJSONString(msg);//过滤规则结果输出
                }
            } else {
                DUFlowBean targetDuFlowBean = new DUFlowBean();  //Todo 规则引擎 等待写入数据
                BeanUtils.copyProperties(sourceDuFlowBean, targetDuFlowBean);
                log.debug("拷贝没有过滤的targetDuFlowBean:{}", targetDuFlowBean);
                BidResponseBean bidResponseBean = convertBidResponse(targetDuFlowBean,bidRequestBean);
                pushRedis(targetDuFlowBean);//上传到redis服务器
                log.debug("json计数");
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
    private BidResponseBean convertBidResponse(DUFlowBean duFlowBean,BidRequestBean bidRequestBean) {
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
        bid.setAdid(configs.getString("ADID"));//duFlowBean.getAdUid()广告id，对应duFlowBean的AdUid；
        Integer instl = bidRequestBean.getImp().get(0).getInstl();
        String curl = "http://101.200.56.200:8880/" + "adviewclick?" +
                "id=" + duFlowBean.getRequestId() +
                "&bidid=" + bidResponseBean.getBidid() +
                "&impid=" + impression.getId() +
                "&price=" + 6.0 +
                "&act=" + format +
                "&adx=" + duFlowBean.getAdxId() +
                "&did=" + duFlowBean.getDid() +
                "&device=" + duFlowBean.getDeviceId() +
                "&app=" + duFlowBean.getAppName() +
                "&appn=" + duFlowBean.getAppPackageName() +
                "&appv=" + duFlowBean.getAppVersion() +
                "&pmp=" + duFlowBean.getDealid();
        if (instl==5){
            bid.setAdmt(6);//duFlowBean.getAdmt()广告类型
            ResponseVideo responseVideo =new ResponseVideo();
            responseVideo.setXmltype(2);
            responseVideo.setVideourl(configs.getString("VADURL"));
            responseVideo.setDuration(15);
            responseVideo.setWidth(Integer.valueOf(configs.getString("vw")));
            responseVideo.setHeight(Integer.valueOf(configs.getString("vh")));
            bid.setVideo(responseVideo);
//        bid.setAdm(configs.getString("ADM"));//duFlowBean.getAdm() 广告物料数据
//            bid.setAdw(1280);//duFlowBean.getAdh()广告物料宽度
//            bid.setAdh(720);//duFlowBean.getAdw()广告物料高度
        }else if (instl==6){
            bid.setAdmt(8);//duFlowBean.getAdmt()广告类型
            NativeResponseBean nativeResponseBean =new NativeResponseBean();
            nativeResponseBean.setVer("1");
            List<Assets> assets = bidRequestBean.getImp().get(0).getNative().getRequest().getAssets();
            List<Assets> assetsList =new ArrayList<>();
            Assets assetsTitle =new Assets();
            Assets assetsData =new Assets();
            Assets assetsImg =new Assets();
            Assets assetsVideo =new Assets();
            for (Assets asset : assets) {
                if (asset.getTitle()!=null){
                    assetsTitle.setId(asset.getId());
                }else if (asset.getData()!=null){
                    assetsData.setId(asset.getId());
                }else if (asset.getImg()!=null){
                    assetsImg.setId(asset.getId());
                }else if(asset.getVideo()!=null){
                    assetsVideo.setId(asset.getId());
                }
            }
            NativeRequestTitle title =new NativeRequestTitle();
            title.setText("数盟测试数据");
            assetsTitle.setTitle(title);
            assetsList.add(assetsTitle);

            NativeRequestData data =new NativeRequestData();
            data.setValue("数盟测试数据不知道对不对");
            assetsData.setData(data);
            assetsList.add(assetsData);
            for (Assets asset : assets) {
                if (asset.getImg()!=null){
                    NativeRequestImage image =new NativeRequestImage();
                    image.setW(Integer.valueOf(configs.getString("nw")));
                    image.setH(Integer.valueOf(configs.getString("nh")));
                    image.setUrl(configs.getString("NADM"));
                    assetsImg.setImg(image);
                    assetsList.add(assetsImg);
                }else if (asset.getVideo()!=null){
                    NativeRequestVideo video =new NativeRequestVideo();
                    video.setXmltype(2);
                    video.setVideourl(configs.getString("VADURL"));
                    video.setDuration(15);
                    video.setWidth(Integer.valueOf(configs.getString("vw")));
                    video.setHeight(Integer.valueOf(configs.getString("vh")));
                    assetsVideo.setVideo(video);
                    assetsList.add(assetsVideo);
                }
            }




            nativeResponseBean.setAssets(assetsList);
            ResponseLink link =new ResponseLink();
            List<String>  linkCurls =new ArrayList<>();
            linkCurls.add(curl);
            link.setUrl(configs.getString("ADURL"));
            link.setClicktrackers(linkCurls);
            nativeResponseBean.setLink(link);
            List<String>  curls =new ArrayList<>();
            curls.add(curl);
            nativeResponseBean.setImptrackers(curls);//点击检测
            log.debug("nativeResponseBean:{}",nativeResponseBean);
            bid.setNative(nativeResponseBean);
        }else if (instl==0){
            bid.setAdmt(2);//duFlowBean.getAdmt()广告类型
            bid.setCrid(configs.getString("CRID"));//duFlowBean.getCrid()广告物料 ID
//        bid.setAdm(configs.getString("ADM"));//duFlowBean.getAdm() 广告物料html数据
            bid.setAdi(configs.getString("ADM"));//图片路径
            bid.setAdh(50);//duFlowBean.getAdw()广告物料高度
            bid.setAdw(320);//duFlowBean.getAdh()广告物料宽度
        }else {
            log.debug("无此类型广告：{}",instl);
        }
        //曝光wurl
        String wurl = "http://101.200.56.200:8880/" + "adviewexp?" +
                "id=" + duFlowBean.getRequestId() +
                "&bidid=" + bidResponseBean.getBidid() +
                "&impid=" + impression.getId() +
                "&price=" + "%%WIN_PRICE%%" +
                "&act=" + format +
                "&adx=" + duFlowBean.getAdxId() +
                "&did=" + duFlowBean.getDid() +
                "&device=" + duFlowBean.getDeviceId() +
                "&app=" + duFlowBean.getAppName() +
                "&appn=" + duFlowBean.getAppPackageName() +
                "&appv=" + duFlowBean.getAppVersion() +
                "&pmp=" + duFlowBean.getDealid();
        bid.setWurl(wurl);//赢价通知，由 AdView 服务器 发出  编码格式的 CPM 价格*10000，如价格为 CPM 价格 0.6 元，则取值0.6*10000=6000。

        List<String> urls = new ArrayList<>();
        bid.setAdurl(configs.getString("ADURL"));//广告点击跳转落地页，可以支持重定向
        urls.add(curl);
        Map nurlMap = new HashMap();
        nurlMap.put("0", urls);
        bid.setNurl(nurlMap);//带延迟的展示汇报，由客户端发送
        List curls = new ArrayList();
        curls.add(curl);
        //等待写入
//        Double biddingPrice = duFlowBean.getBiddingPrice()*1000;
//        Integer price = Integer.valueOf(String.valueOf(biddingPrice));
        bid.setPrice(Integer.valueOf(configs.getString("PRICE")));//CPM 出价，数值为 CPM 实际价格*10000，如出价为 0.6 元，
        bid.setCurl(curls);//点击监控地址，客户端逐个发送通知

        bid.setAdct(0);//duFlowBean.getAdct() 广告点击行为类型，参考附录 9
        bid.setCid(configs.getString("CID"));//duFlowBean.getCreativeUid()广告创意 ID，可用于去重
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
        log.debug("redis连接时间计数");
        Jedis jedis = JedisManager.getInstance().getResource();
        if (jedis != null) {
            log.debug("AdView_jedis：{}", jedis);
            String set = jedis.set(targetDuFlowBean.getRequestId(), JSON.toJSONString(targetDuFlowBean));
            Long expire = jedis.expire(targetDuFlowBean.getRequestId(), 5 * 60);//设置超时时间为5分钟
            log.debug("推送到redis服务器是否成功;{},设置超时时间是否成功(成功返回1)：{}", set, expire);
        } else {
            log.debug("jedis为空：{}", jedis);
        }
    }
}
