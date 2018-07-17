package cn.shuzilm.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WriteDataToLog {

	//******** 设备1.0 *******
	static Logger logger_DevInfo = Logger.getLogger("DevInfo");
	static Logger logger_AppInfo = Logger.getLogger("AppInfo");
	static Logger logger_OpInfo = Logger.getLogger("OpInfo");
	static Logger logger_WebInfo = Logger.getLogger("WebInfo");
	static Logger logger_apps = Logger.getLogger("apps");

	//******** 应用1.0 *******
	static Logger logger_actionDataInfo = Logger.getLogger("actionDataInfo");
	static Logger logger_basalDataInfo = Logger.getLogger("basalDataInfo");


	//******* 设备2.0 *********
	static Logger logger_DevInfo2 = Logger.getLogger("DevInfo2");
	static Logger logger_NPLInfo = Logger.getLogger("NPLInfo");
	static Logger logger_IUUInfo = Logger.getLogger("IUUInfo");
	static Logger logger_OCInfo = Logger.getLogger("OCInfo");
	static Logger logger_WebInfo2 = Logger.getLogger("WebInfo2");
	static Logger logger_InstalledAppInfo = Logger.getLogger("InstalledAppInfo");


	//******* 应用2.0 *********
	static Logger logger_appDevInfo = Logger.getLogger("appDevInfo");
	static Logger logger_appNInfo = Logger.getLogger("appNInfo");
	static Logger logger_appAInfo = Logger.getLogger("appAInfo");
	static Logger logger_appPInfo = Logger.getLogger("appPInfo");
	static Logger logger_appEInfo = Logger.getLogger("appEInfo");

	/**
	 * 查看json中的key有没有
	 * @param strarry
	 * @param json
	 * @return
	 */
	private String checkJson(String[] strarry, JSONObject json){
		String str = "";
		if(json.containsKey("UserId")){
			for(int j = 0; j<strarry.length; j++){

				try {
					if(j == strarry.length-1){
						str = str+json.getString(strarry[j]);
					}else{
						str = str+json.getString(strarry[j])+"\t";
					}
				} catch (Exception e) {
					if(j == strarry.length-1){
						str = str+"null";
					}else{
						str = str+"null"+"\t";
					}
				}

			}
		}

//		System.out.println("str : "+str);
		return str;
	}

	private String checkJson_2_0_1(String[] strarry, JSONObject json){
		String str = "";
		for(int j = 0; j<strarry.length; j++){

			try {
				if(j == strarry.length-1){
					str = str+json.getString(strarry[j]);
				}else{
					str = str+json.getString(strarry[j])+"\t";
				}
			} catch (Exception e) {
				if(j == strarry.length-1){
					str = str+"null";
				}else{
					str = str+"null"+"\t";
				}
			}

		}

//		System.out.println("str : "+str);
		return str;
	}


	/**
	 * 设备1.0
	 * @param
	 */
	public void writeLogs(String[] ss){
//		String[] ss = jsonStrDouble.split("-----");
		String jsonStr = ss[0];
		String jsonUserIpStr = ss[1];
		System.out.println("-----------------------------------------------------");
		JSONObject jsonUserIp = JSONObject.fromObject(jsonUserIpStr);
		System.out.println("-----------------------------------------------------2");
//		JSONObject jsonUserIp = JSONObject.parseObject(jsonUserIpStr);
		JSONObject jsonObject = null;
		jsonObject = JSONObject.fromObject(jsonStr);
		JSONArray jsonArrayDevInfo = null;
		JSONArray jsonArrayAppInfo = null;
		JSONArray jsonArrayOpInfo = null;
		JSONArray jsonArrayWebInfo = null;
		JSONArray jsonArrayApps = null;
		if(jsonObject.containsKey("DevInfo")){
			jsonArrayDevInfo = jsonObject.getJSONArray("DevInfo");//checkdata表 --> checkdata
		}
		if(jsonObject.containsKey("AppInfo")){
			jsonArrayAppInfo = jsonObject.getJSONArray("AppInfo");//用户安装卸载数据 --> users_install_unload_data
		}
		if(jsonObject.containsKey("OpInfo")){
			jsonArrayOpInfo = jsonObject.getJSONArray("OpInfo");//temp_MB_Fact_Action_2 --> mb_fact_action
		}
		if(jsonObject.containsKey("WebInfo")){
			jsonArrayWebInfo = jsonObject.getJSONArray("WebInfo");//浏览器上网数据 --> browser_online_data
		}
		if(jsonObject.containsKey("apps")){
			jsonArrayApps = jsonObject.getJSONArray("apps");//APP_INFO --> app_info
		}


		if(jsonArrayDevInfo != null){
			//DevInfo  checkdata表 --> ks_or_user.checkdata
			for(int i = 0 ; i<jsonArrayDevInfo.size(); i++){

				JSONObject json = (JSONObject) jsonArrayDevInfo.get(i);
				json.put("userip", jsonUserIp.getString("userip"));


				String[] strarry = {"UserId","Device_Model","Network","System_Model","Location","Screen_Size",
						"AgentName","AgentID","AgentChannel","SDKVersion","userip","Number","LBS"};
				String logStr = checkJson(strarry, json);
				logger_DevInfo.info(logStr);


			}
		}


		//key_appinfo --stallpackageName
		if(jsonArrayAppInfo != null){
			//AppInfo 用户安装卸载数据 --> ks_or_user.users_install_unload_data  ----ok
			for(int i = 0 ; i<jsonArrayAppInfo.size(); i++){
				JSONObject json = (JSONObject) jsonArrayAppInfo.get(i);
//{"Add_App":"安装","App_Version":"1.0.12.0","Ins_App":"天天酷跑","Ins_Time":"2014-07-12 17:48:31","Ins_Type":"安装","Key_appinfo":"com.tencent.pao","UserId":"A000004391B90E"}
				String[] strarry = {"UserId","stallAppName","stallTime","appVersion","Ins_Type","stallpackageName"};
									//11111		"null"	  2015-04-13 16:54:49	2.11.4	安装	  com.achievo.vipshop
				String logStr = checkJson(strarry,json);
				logger_AppInfo.info(logStr);


			}
		}


		//r_app_url -- Key_opinfo
		if(jsonArrayOpInfo != null){
			//OpInfo temp_MB_Fact_Action_2 --> ks_or_user.mb_fact_action
			for(int i = 0 ; i<jsonArrayOpInfo.size(); i++){
				JSONObject json = (JSONObject) jsonArrayOpInfo.get(i);
				String[] strarry = {"UserId","Pro_App_Name","OpenTime","CloseTime_1","App_Version_1","Key_opinfo"};
				String logStr = checkJson(strarry, json);
				logger_OpInfo.info(logStr);


			}
		}


		if(jsonArrayWebInfo != null){
			//WebInfo 浏览器上网数据 --> ks_or_user.browser_online_data -----ok
			for(int i = 0 ; i<jsonArrayWebInfo.size(); i++){
				JSONObject json = (JSONObject) jsonArrayWebInfo.get(i);
				String[] strarry = {"UserId","Browser_Model","URL","OpenTime_1","CloseTime"};
				String logStr = checkJson(strarry, json);
				logger_WebInfo.info(logStr);

			}
		}


		if(jsonArrayApps != null){
			//apps APP_INFO --> app_info
			for(int i = 0 ; i<jsonArrayApps.size(); i++){

				String apps_userid = jsonObject.get("UserId").toString();//APP_INFO UserId
				JSONObject json = (JSONObject) jsonArrayApps.get(i);
				json.put("UserId", apps_userid);

				String[] strarry = {"UserId","app_name","package_name","version_name","version_code"};
				String logStr = checkJson(strarry, json);
				logger_apps.info(logStr);

			}
		}



	}



	public String nowDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(new Date());

		return date;
	}

}
