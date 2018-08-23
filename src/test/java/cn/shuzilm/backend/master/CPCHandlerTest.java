package cn.shuzilm.backend.master;

import com.yao.util.db.bean.ResultList;

import java.sql.SQLException;

/**
 * Created by DENGJIAN on 2018/8/23.
 */
public class CPCHandlerTest {

    public static void main(String [] args) throws SQLException {

        // case 1 : 不初始化，直接获取广告是否可以投放
        AdFlowControl adFlowControl = AdFlowControl.getInstance();
        CPCHandler cpcHandler = new CPCHandler(adFlowControl);
        adFlowControl.loadAdInterval(true);
        //cpcHandler.checkAvailable("7635434a-7ff2-45f6-9806-09b6d4908e2d");

        // case 2 ：初始化阈值，不初始化monitor, 获取广告是否可以投放
        //加载广告信息
        TaskServicve taskService = new TaskServicve();
        ResultList adList = taskService.queryAdByUpTime(0);
        cpcHandler.updateIndicator();// 初始化阈值
        //cpcHandler.checkAvailable("7635434a-7ff2-45f6-9806-09b6d4908e2d");

        // case 3: 初始化monitor, 没有初始化阈值
        //cpcHandler.updatePixel("7635434a-7ff2-45f6-9806-09b6d4908e2d",2,5.0f,0,0);
        //cpcHandler.checkAvailable("7635434a-7ff2-45f6-9806-09b6d4908e2d");

        // case 4： 初始化阈值和monitor
        //cpcHandler.checkAvailable("7635434a-7ff2-45f6-9806-09b6d4908e2d");

        // (1) 没有点击， 未投放完，超出透支额度
        for(int i = 0; i < 20; i++ ){
            cpcHandler.updatePixel("f2cb2fda-caf3-4c24-8bdd-940ac2c74485",1,5.0f,0,0);
            cpcHandler.checkAvailable("f2cb2fda-caf3-4c24-8bdd-940ac2c74485");
        }
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++");
        // (2) 没有点击， 投放完毕
       for(int i = 0; i < 10; i++ ){
            cpcHandler.updatePixel("f5833355-3dfb-496c-9054-5c625f7396e2",500,5.0f,0,0);
            cpcHandler.checkAvailable("f5833355-3dfb-496c-9054-5c625f7396e2");
        }
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++");

       // (3) 出现点击
        for(int i = 0; i < 1; i++ ){
            cpcHandler.updatePixel("7635434a-7ff2-45f6-9806-09b6d4908e2d",1,5.0f,1,0);
            cpcHandler.checkAvailable("7635434a-7ff2-45f6-9806-09b6d4908e2d");
        }
        cpcHandler.updatePixel("7635434a-7ff2-45f6-9806-09b6d4908e2d",0,5.0f,2,1);
        for(int i = 0; i < 9; i++ ){
            cpcHandler.updatePixel("7635434a-7ff2-45f6-9806-09b6d4908e2d",1,5.0f,1,0);
            cpcHandler.checkAvailable("7635434a-7ff2-45f6-9806-09b6d4908e2d");
        }




    }
}
