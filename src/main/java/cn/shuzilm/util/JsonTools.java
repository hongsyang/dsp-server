package cn.shuzilm.util;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

public class JsonTools {

//	public static String toJsonString(JSONArray bean){
//		JSONSerializer js = new JSONSerializer() ;
//		JSON json = js.toJSON(bean);
//		return json.toString();
//	}
//
//	public static String toJsonString(JSONObject bean){
//		JSONSerializer js = new JSONSerializer() ;
//		JSON json = js.toJSON(bean);
//		return json.toString();
//	}

    public static String toJsonString(Object bean){
        JSONSerializer js = new JSONSerializer() ;
        JSON json = js.toJSON(bean);
        return json.toString();
    }


}
