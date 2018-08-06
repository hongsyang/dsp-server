package cn.shuzilm.util.geo;

import cn.shuzilm.bean.dmp.GpsBean;
import cn.shuzilm.bean.dmp.GpsGridBean;
import cn.shuzilm.util.InvokePython;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class GridMark {

    private double LNG_DISTANCE = 0.0;
    /**
     * lng ,<lat ,ad uid>
     */
    private TreeMap<Double,TreeMap<Double,GpsGridBean>> tm = null;

    private static final String pythonFile = "E:\\工作源码\\数盟网络\\es-api\\util\\geo_transfer.py";
    private static final String dir  = "E:\\工作源码\\数盟网络\\es-api\\util\\";
    /**
     * 将当前的中心坐标和半径转换为 该算法可以识别的矩形坐标
     * 通过 python 的 fwd 函数实现
     * 处理过程： 输入参数转换成 右上角坐标
     * 输出参数转换完成左下角坐标
     * @param coords
     */
    public ArrayList<GpsGridBean> reConvert(ArrayList<GpsBean> coords){
        ArrayList<GpsGridBean> destList = new ArrayList<>();
        for(GpsBean gps : coords){
            String[] args = new String[]{
                    "E:\\python_2.7_64\\python",
                    pythonFile,
                    String.valueOf(gps.getLng()),
                    String.valueOf(gps.getLat()),
                    String.valueOf(gps.getRadius()),
            };
            // str(leftdown) + '\t' + str(rightup)
            String result = InvokePython.invoke(args,dir);
            String[] arr = result.split("\t");
            double lngLeft = Double.parseDouble(arr[1].split(",")[0]);
            double latDown = Double.parseDouble(arr[1].split(",")[1]);
            GpsGridBean gridBean = new GpsGridBean();
            //变成了 右上角坐标
            gridBean.setLngLeft(lngLeft);
            gridBean.setLatDown(latDown);
            //初始化 左下角坐标
            double lngRight = Double.parseDouble(arr[0].split(",")[0]);
            double latUp = Double.parseDouble(arr[0].split(",")[1]);

            gridBean.setLngRight(lngRight);
            gridBean.setLatUp(latUp);

            destList.add(gridBean);

//            System.out.println("new BMap.Point(" + gridBean.getLngLeft() + "," + gridBean.getLatDown() + ")");
            System.out.println("new BMap.Point(" + gridBean.getLngRight() + "," + gridBean.getLatUp() + ")");

        }
        return destList;

    }

    public void init(ArrayList<GpsGridBean> coords){
        try {
            //使用 treemap 进行排序，先按照经度查找子树，然后再根据纬度进行查找，最终找到 第一个小于等于 当前经纬度的栅格,并将该栅格 ID 打上标记
            //grid_id,longileft,longiright,latibottom,latitop
            if(tm != null)
                tm.clear();
                tm = null;

            tm = new TreeMap();
            for(GpsGridBean gps : coords) {
                try{
                    double lng = gps.getLngRight();//long right
                    double lat = gps.getLatUp();//lat up
                    //在广告业务场景中是适用于的 广告 UID
//                    String gridId = gps.getPayload();

                    if(!tm.containsKey(lng)){
                        TreeMap<Double,GpsGridBean> tmChild =  new TreeMap<Double, GpsGridBean>();
                        tmChild.put(lat,gps);
                        tm.put(lng,tmChild);
                    }else{
                        TreeMap<Double,GpsGridBean> tmChild = tm.get(lng);
                        if(!tmChild.containsKey(lat))
                            tmChild.put(lat,gps);
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }

            double firstKey = 0;
            double secondKey = 0;
            int i = 0;
            for(Double d :  tm.keySet()){
                if( i == 0)
                    firstKey = d;
                if( i == 1){
                    secondKey = d;
                    break;
                }
                i ++;
            }
            LNG_DISTANCE = secondKey - firstKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param lng
     * @param lat
     * @return
     */
    public ArrayList<String> findGrid(Double lng,Double lat){
        try {
            // pay load
            ArrayList<String> destList = new ArrayList<>();
            GpsGridBean gps = null;
            if(lng == null || lat == null)
                return destList;
            else{

                SortedMap sm = tm.tailMap(lng);

                if(sm != null && sm.size() > 0){
                    Double gridLng = 0.0;
                    TreeMap<Double,GpsGridBean> tmChild = null;
                    SortedMap gridLatMap = null;

//                    Double gridLng = (Double)sm.firstKey();
//                    TreeMap<Double,GpsGridBean> tmChild = tm.get(gridLng);
//                    SortedMap gridLatMap = tmChild.tailMap(lat);

                    //先提取出右上角的所有坐标，然后再对 左下角坐标进行一轮比对，筛选出合适的坐标

                    for(Object obj : sm.keySet()){
                       gridLng = (Double)obj;
                        tmChild = tm.get(gridLng);
                        gridLatMap = tmChild.tailMap(lat);

                        if (gridLatMap != null && gridLatMap.size() > 0) {
                            for(Object obj2 : gridLatMap.keySet()){
                                Double gridLat = (Double)obj2;
                                gps = tmChild.get(gridLat);
                                //在对右上角做完筛选过滤后，再对左下角做判断，如果框选的矩形区域包含该坐标点，则加入输出列表
                                if (gps.getLngLeft() <= lng && gps.getLatDown() <= lat)
                                    destList.add(gps.getPayload());
                                else{
                                    break;
                                }
                            }
                        }
                    }
                }
                return destList;
            }
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }

    }

    public static void main(String[] args) {

        double minLng = 114.428794 ;
        double minLat =	38.085325 ;

        String[] geoArray = new String[]{
            "115.324324,39.4324234",
                "114.428794	,38.085325		",
                "114.427794	,38.084875    ",
                "114.426794	,38.083425    ",
                "113.4324432,37.43242342",

        };
        GridMark m = new GridMark();
        try {
            ArrayList<GpsBean> list = new ArrayList<>();
            int counter = 0;
            for(String geo:geoArray){
                String[] arr = geo.split(",");
                GpsBean bean = new GpsBean(Double.parseDouble(arr[1]),Double.parseDouble(arr[0]));
                bean.setRadius(1000);
                bean.setPayload(String.valueOf(counter));
                list.add(bean);
                counter ++;
            }
            ArrayList<GpsGridBean> list2 = m.reConvert(list);
            m.init(list2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        double lng = 114.42674680997877;
        double lat = 38.085935245086155;

//        114.44290747073296,38.09787235002915 out

        ArrayList<String> gridIdList = m.findGrid(lng,lat);
        for(String gird : gridIdList){
            System.out.println(gird);
        }



    }
}
