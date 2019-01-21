package cn.shuzilm.bean.tencent.response;

import lombok.Data;

import java.io.Serializable;
@Data
public class TencentProductInfo  implements Serializable{
   private String  product_id  ;// DSP 的商品 ID
   private Integer  product_industry  ;// 商品一级行业 ID。取值见《广告主行业分类.xlsx》。
   private Integer  product_category  ;// 商品二级行业 ID。取值见《广告主行业分类.xlsx》。
   private String  product_name  ;// 商品名称
   private String  product_description  ;// 商品描述
   private String  product_multimedia_url;//商品图片地址
}
