package cn.shuzilm.bean.control;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thunders on 2018/6/4.
 */
public class TagBean {
    String tagName = null;
    String tagId = null;
    String tagValue = null;
    public TagBean(String tagName,String tagId,String tagValue){
        this.tagName = tagName;
        this.tagId = tagId;
        this.tagValue = tagValue;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getTagValue() {
        return tagValue;
    }

    public void setTagValue(String tagValue) {
        this.tagValue = tagValue;
    }

    public static void main(String[] args) {
        List<TagBean> l = new ArrayList<TagBean>() ;
//        l.add(new TagBean("tagname","tagid","tagvalue"));
//        l.add(new TagBean("tagname2","tagid2","tagvalue2"));
//        l.add(new TagBean("tagname3","tagid3","tagvalue3"));
//        List<String[]> l2 = new ArrayList<>();
//        String[] arr = new String[]{
//            "10000"
//        };
//        JSONObject json = JSONObject.fromObject(l);
//        JSONArray listArray = JSONArray.fromObject(l);
//        System.out.println(listArray.toString());
        JSONObject json = new JSONObject();
        json.put("code",0);
        json.put("msg","ok");

        JSONObject json2 = new JSONObject();
        json2.put("10000",0);
        json2.put("10001",1);
        json.put("result",json2);

        System.out.println(json);
    }
}
