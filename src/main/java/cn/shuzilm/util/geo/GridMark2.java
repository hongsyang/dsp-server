package cn.shuzilm.util.geo;

import cn.shuzilm.bean.dmp.GpsBean;
import cn.shuzilm.bean.dmp.GpsGridBean;
import cn.shuzilm.util.InvokePython;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * 将前端页面提交的中心原点+半径形式 转换为 左下 + 右上 坐标的表示形式，因为 JAVA 没有没有找到可用的转换类，
 * 因此通过调用 PYTHON 实现。
 * 使用前说明：
 * 需要 安装PYTHON 2.7  或者 3.0 环境
 * 需要安装 pyproj 坐标转换工具集
 */
public class GridMark2 {
    /**
     * 将次改为对应的class目录
     */
    private static final String dir = "D:\\program\\mygit\\dsp-server\\bin\\src\\main\\";

    private static final String pythonEnvDir = "python.exe";

    /**
     * gps 坐标内部ID 与 广告UID 对应
     */
    HashMap<Integer,String> gpsMap = null;
    /**
     * lng ,<lat ,ad uid>
     */
    private TreeMap<Double,Integer> tmRightLng = null;

    private TreeMap<Double,Integer> tmLeftLng = null;

    private TreeMap<Double,Integer> tmUpLat = null;

    private TreeMap<Double,Integer> tmDownlat = null;



    /**
     * 将当前的中心坐标和半径转换为 该算法可以识别的矩形坐标
     * 通过 python 的 fwd 函数实现
     * 处理过程： 输入参数转换成 右上角坐标
     * 输出参数转换完成左下角坐标
     * @param coords
     */
    public ArrayList<GpsGridBean> reConvert(ArrayList<GpsBean> coords){
        ArrayList<GpsGridBean> destList = new ArrayList<>();
        int counter = 0;
//        String currWorkPath = this.getClass().getClassLoader().getResource(".").getPath();
//        try {
//            currWorkPath = URLDecoder.decode(currWorkPath,"utf-8");
//            currWorkPath = currWorkPath.substring(1,currWorkPath.length() - 1);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        String dir = currWorkPath + File.separator  + "geo_transfer.py";

        String currWorkPath = dir + "geo_transfer.py";
        for(GpsBean gps : coords){
            String[] args = new String[]{
                    pythonEnvDir,
                    currWorkPath,
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

            gridBean.setId(counter);
            gridBean.setPayload(gps.getPayload());
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
//            System.out.println("new BMap.Point(" + gridBean.getLngRight() + "," + gridBean.getLatUp() + ")");
            counter ++;
        }
        return destList;

    }

    public void init(ArrayList<GpsGridBean> coords){
        try {
            //使用 treemap 进行排序，先按照经度查找子树，然后再根据纬度进行查找，最终找到 第一个小于等于 当前经纬度的栅格,并将该栅格 ID 打上标记
            //grid_id,longileft,longiright,latibottom,latitop
            if(tmRightLng != null){
                gpsMap.clear();
                tmRightLng.clear();
                tmLeftLng.clear();
                tmUpLat.clear();
                tmDownlat.clear();
            }else{
                gpsMap = new HashMap<>();
                tmRightLng = new TreeMap();
                tmLeftLng = new TreeMap<>();
                tmUpLat = new TreeMap<>();
                tmDownlat = new TreeMap<>();
            }

            for(GpsGridBean gps : coords) {
                try{
                    gpsMap.put(gps.getId(),gps.getPayload());

                    if(!tmRightLng.containsKey(gps.getLngRight())){
                        tmRightLng.put(gps.getLngRight(),gps.getId());
                    }
                    if(!tmLeftLng.containsKey(gps.getLngLeft())){
                        tmLeftLng.put(gps.getLngLeft(),gps.getId());
                    }
                    if(!tmUpLat.containsKey(gps.getLatUp())){
                        tmUpLat.put(gps.getLatUp(),gps.getId());
                    }
                    if(!tmDownlat.containsKey(gps.getLatDown())){
                        tmDownlat.put(gps.getLatDown(),gps.getId());
                    }

                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }


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
            ArrayList<String> adUidList = new ArrayList<>();
            ArrayList<Integer> destList = new ArrayList<>();
            if(lng == null || lat == null)
                return adUidList;
            else{
                SortedMap smRight = tmRightLng.tailMap(lng);
                destList.addAll(smRight.values());

                ArrayList<Integer> leftList = new ArrayList<>();
                SortedMap smLeft = tmLeftLng.headMap(lng);
                leftList.addAll(smLeft.values());

                destList.retainAll(leftList);

                SortedMap smUp = tmUpLat.tailMap(lat);
                destList.retainAll(smUp.values());

                SortedMap smDown = tmDownlat.headMap(lat);
                destList.retainAll(smDown.values());
                for(Integer id : destList){
                    adUidList.add(gpsMap.get(id));
                }

                return adUidList;
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
        GridMark2 m = new GridMark2();
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
//        for(String gird : gridIdList){
//            System.out.println(gird);
//        }



    }
}