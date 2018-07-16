package cn.shuzilm.bean.adview.request;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    Banner  横幅或插屏广告信息描述
* @Author:         houkp
* @CreateDate:     2018/7/10 16:01
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/10 16:01
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class Banner implements Serializable{
    private  Integer w;//(T) 广告位的宽度，单位为像素
    private  Integer h;//(T)   广告位的高度，单位为像素
    private  Integer pos;//(F) 广告在屏幕上的位置，参考附录 2
    private  Integer[] btype;//(F) 拒绝的广告类型，参考附录 3
    private  Integer[] battr;//(F) 拒绝的广告创意属性，参考附录4
    private  Integer[] mimes;//(F) 广告物料形式支持，已支持部分不列入。参考附录 5
    private  Integer[] api;//(F) 列出支持的广告 API，参考附录10
    private RequestExt ext;//(F) 扩展内容
}
