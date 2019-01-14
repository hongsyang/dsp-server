package cn.shuzilm.interf.pixcel;

import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisQueueManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PixcelRedisTask implements Runnable{


    private static final String FILTER_CONFIG = "filter.properties";

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);

    //从redis取数据的线程池
    private static ExecutorService executor = Executors.newFixedThreadPool(configs.getInt("EXECUTOR_THREADS"));
    //启动取数据的线程
    public static void startPixcelPaeser() {
        PixcelRedisTask pixcelRedisTask =new PixcelRedisTask();
        Thread thread=new Thread(pixcelRedisTask);
        thread.start();

    }

    @Override
    public void run() {
        while (true) {
            executor.execute(new Runnable() {
                @Override
                public void run() {

                    Object adviewclick = JedisQueueManager.getElementFromQueue("adviewclick");
                    Object adviewexp = JedisQueueManager.getElementFromQueue("adviewexp");
                    Object adviewnurl = JedisQueueManager.getElementFromQueue("adviewnurl");
                    Object lingjiclick = JedisQueueManager.getElementFromQueue("lingjiclick");
                    Object lingjiexp = JedisQueueManager.getElementFromQueue("lingjiexp");
                    Object youyiclick = JedisQueueManager.getElementFromQueue("youyiclick");
                    Object youyiexp = JedisQueueManager.getElementFromQueue("youyiexp");
                    Object youyiimp = JedisQueueManager.getElementFromQueue("youyiimp");
                    if (adviewclick != null) {

                    }
                }
            });
        }
    }
}
