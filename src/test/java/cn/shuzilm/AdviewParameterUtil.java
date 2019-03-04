package cn.shuzilm;

import cn.shuzilm.bean.adview.response.BidResponseBean;
import cn.shuzilm.bean.adview.response.SeatBid;
import com.alibaba.fastjson.JSON;

import java.util.List;

public class AdviewParameterUtil {

    public static void main(String[] args) {
        String json="2019-01-2414:11:13.049 bidResponseBean:{\"bidid\":\"6a9f19bfd10896fd0c0935a1d5bad7c0\",\"id\":\"20190124-141113_bidreq_165-1888-ZbGs-1633\",\"seatbid\":[{\"bid\":[{\"adct\":0,\"adh\":500,\"adi\":\"http://cdn.shuzijz.cn/material/549d1ddf-52b0-4dfb-8cef-4d5025ff6cf4.jpg\",\"adid\":\"549d1ddf-52b0-4dfb-8cef-4d5025ff6cf4\",\"admt\":1,\"adurl\":\"http://h55.shuzilm.cn?advertiserUid=89633fef-b7d0-4a36-802d-8960ffe5e851&adUid=e5554cdf-bc3e-4235-ae28-586fcd0a0d0a&creativeUid=dd4208a1-2dc0-43b5-81c2-0bb98671e94e&materialId=549d1ddf-52b0-4dfb-8cef-4d5025ff6cf4\",\"adw\":600,\"cid\":\"221415813602\",\"crid\":\"221415813602\",\"curl\":[\"http://pixel.shuzijz.cn/adviewclick?id=20190124-141113_bidreq_165-1888-ZbGs-1633&bidid=6a9f19bfd10896fd0c0935a1d5bad7c0&impid=20190124-141113_reqimp_165-1160-ftZl-1617&act=20190124141113&adx=2&did=45ef495717fe5a4763292674717fa0a8&device=45ef495717fe5a4763292674717fa0a8&app=%E5%86%9B%E4%BA%8B%E8%BF%B7-%E5%AE%89%E5%8D%93&appn=cc.jsm.news&appv=1.1.3&ddem=ebc5cb8a-997e-4713-87cb-591adbcd828e&dcuid=dd4208a1-2dc0-43b5-81c2-0bb98671e94e&dpro=12&dcit=122&dcou=1257&dade=89633fef-b7d0-4a36-802d-8960ffe5e851&dage=null&daduid=e5554cdf-bc3e-4235-ae28-586fcd0a0d0a&pmp=null&userip=183.156.103.8\",\"http://re.shuzilm.cn/1yuhfuv7?ip=${AUCTION_ID}&idfa=${AUCTION_BID_ID}&imei=${AUCTION_IMP_ID}&os=${AUCTION_PRICE}&mac=${MAC}&mac1=${mac1}&ua=%%WIN_PRICE%%\"],\"impid\":\"20190124-141113_reqimp_165-1160-ftZl-1617\",\"nurl\":{\"0\":[\"http://re.shuzilm.cn/1yuhfutz?ip=${AUCTION_ID}&idfa=${AUCTION_BID_ID}&imei=${AUCTION_IMP_ID}&os=${AUCTION_PRICE}&mac=${MAC}&mac1=${mac1}&ua=%%WIN_PRICE%%\",\"http://pixel.shuzijz.cn/adviewnurl?id=20190124-141113_bidreq_165-1888-ZbGs-1633&bidid=6a9f19bfd10896fd0c0935a1d5bad7c0&impid=20190124-141113_reqimp_165-1160-ftZl-1617&price=%%WIN_PRICE%%&act=20190124141113&adx=2&did=45ef495717fe5a4763292674717fa0a8&device=45ef495717fe5a4763292674717fa0a8&app=%E5%86%9B%E4%BA%8B%E8%BF%B7-%E5%AE%89%E5%8D%93&appn=cc.jsm.news&appv=1.1.3&pf=1.0&ddem=ebc5cb8a-997e-4713-87cb-591adbcd828e&dcuid=dd4208a1-2dc0-43b5-81c2-0bb98671e94e&dpro=12&dcit=122&dcou=1257&dade=89633fef-b7d0-4a36-802d-8960ffe5e851&dage=null&daduid=e5554cdf-bc3e-4235-ae28-586fcd0a0d0a&pmp=null&userip=183.156.103.8\"]},\"price\":600000,\"wurl\":\"http://pixel.shuzijz.cn/adviewexp?id=20190124-141113_bidreq_165-1888-ZbGs-1633&bidid=6a9f19bfd10896fd0c0935a1d5bad7c0&impid=20190124-141113_reqimp_165-1160-ftZl-1617&price=%%WIN_PRICE%%&act=20190124141113&adx=2&did=45ef495717fe5a4763292674717fa0a8&device=45ef495717fe5a4763292674717fa0a8&app=%E5%86%9B%E4%BA%8B%E8%BF%B7-%E5%AE%89%E5%8D%93&appn=cc.jsm.news&appv=1.1.3&pf=1.0&ddem=ebc5cb8a-997e-4713-87cb-591adbcd828e&dcuid=dd4208a1-2dc0-43b5-81c2-0bb98671e94e&dpro=12&dcit=122&dcou=1257&dade=89633fef-b7d0-4a36-802d-8960ffe5e851&dage=null&daduid=e5554cdf-bc3e-4235-ae28-586fcd0a0d0a&pmp=null&userip=183.156.103.8\"}]}]}";
        String[] split = json.split("bidResponseBean:");
        String createtime  = split[0];
        String newJson= split[1];
        BidResponseBean bidResponseBean = JSON.parseObject(newJson, BidResponseBean.class);
        System.out.println(bidResponseBean);
        List<SeatBid> seatbid = bidResponseBean.getSeatbid();
        seatbid.get(0);
        System.out.println("seatbid"+seatbid);
        System.out.println(bidResponseBean.getSeatbid());
        System.out.println(createtime);
        System.out.println(newJson);
        int i = newJson.indexOf("adviewclick?id=");

        System.out.println(i);
        String substring = newJson.substring(i);
        String s = substring.substring(0, substring.indexOf("\","));
        System.out.println(substring);
        System.out.println(s);
        TestAdViewClickParameterParserImpl.parseUrlStr(s);

    }
}
