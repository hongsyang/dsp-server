package cn.shuzilm.bean.youyi.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class YouYiBidResponse implements Serializable {
     private String  session_id ;//required string
     private List<YouYiAd> ads ;//repeated
}
