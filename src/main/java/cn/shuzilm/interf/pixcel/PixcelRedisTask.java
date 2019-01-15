package cn.shuzilm.interf.pixcel;

import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.interf.pixcel.parser.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PixcelRedisTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PixcelRedisTask.class);


    private static final String FILTER_CONFIG = "filter.properties";

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);

    //定时从redis取数据的线程池
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(configs.getInt("EXECUTOR_THREADS"));

    private static ExecutorService executor = Executors.newFixedThreadPool(configs.getInt("EXECUTOR_THREADS"));


    @Override
    public void run() {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                log.debug(Thread.currentThread().getName() + "---------从redis中取出数据");

                if (JedisQueueManager.getLength("adviewclick") > 1) {
                    log.debug("adviewclick=线程数--------------------" + Thread.currentThread().getName());
                    while (true) {
                        Object adviewclick = JedisQueueManager.getElementFromQueue("adviewclick");
                        if (adviewclick != null) {
                            AdViewClickParameterParserImpl.parseUrlStr(adviewclick.toString());
                        } else {
                            System.out.println("跳出循环");
                            break;
                        }
                    }
                }
                if (JedisQueueManager.getLength("adviewexp") > 1) {
                    log.debug("adviewexp=线程数--------------------" + Thread.currentThread().getName());
                    while (true) {
                        Object adviewexp = JedisQueueManager.getElementFromQueue("adviewexp");
                        if (adviewexp != null) {
                            AdViewExpParameterParserImpl.parseUrlStr(adviewexp.toString());
                        } else {
                            System.out.println("跳出循环");
                            break;
                        }
                    }
                }
                if (JedisQueueManager.getLength("adviewnurl") > 1) {
                    log.debug("adviewnurl=线程数--------------------" + Thread.currentThread().getName());
                    while (true) {
                        Object adviewnurl = JedisQueueManager.getElementFromQueue("adviewnurl");
                        if (adviewnurl != null) {
                            AdViewNurlParameterParserImpl.parseUrlStr(adviewnurl.toString());
                        } else {
                            System.out.println("跳出循环");
                            break;
                        }
                    }
                }
                if (JedisQueueManager.getLength("lingjiclick") > 1) {
                    log.debug("lingjiclick=线程数--------------------" + Thread.currentThread().getName());
                    while (true) {
                        Object lingjiclick = JedisQueueManager.getElementFromQueue("lingjiclick");
                        if (lingjiclick != null) {
                            LingJiClickParameterParserImpl.parseUrlStr(lingjiclick.toString());
                        } else {
                            System.out.println("跳出循环");
                            break;
                        }
                    }
                }
                if (JedisQueueManager.getLength("lingjiexp") > 1) {
                    log.debug("lingjiexp=线程数--------------------" + Thread.currentThread().getName());
                    while (true) {
                        Object lingjiexp = JedisQueueManager.getElementFromQueue("lingjiexp");
                        if (lingjiexp != null) {
                            LingJiExpParameterParserImpl.parseUrlStr(lingjiexp.toString());
                        } else {
                            System.out.println("跳出循环");
                            break;
                        }
                    }
                }
                if (JedisQueueManager.getLength("youyiclick") > 1) {
                    log.debug("youyiclick=线程数--------------------" + Thread.currentThread().getName());
                    while (true) {
                        Object youyiclick = JedisQueueManager.getElementFromQueue("youyiclick");
                        if (youyiclick != null) {
                            YouYiClickParameterParserImpl.parseUrlStr(youyiclick.toString());
                        } else {
                            System.out.println("跳出循环");
                            break;
                        }
                    }
                }
                if (JedisQueueManager.getLength("youyiexp") > 1) {
                    log.debug("youyiexp=线程数--------------------" + Thread.currentThread().getName());

                    while (true) {
                        Object youyiexp = JedisQueueManager.getElementFromQueue("youyiexp");
                        if (youyiexp != null) {
                            YouYiExpParameterParserImpl.parseUrlStr(youyiexp.toString());
                        } else {
                            System.out.println("跳出循环");
                            break;
                        }
                    }
                }
                if (JedisQueueManager.getLength("youyiimp") > 1) {
                    log.debug("youyiimp=线程数--------------------" + Thread.currentThread().getName());
                    while (true) {
                        Object youyiimp = JedisQueueManager.getElementFromQueue("youyiimp");
                        if (youyiimp != null) {
                            YouYiImpParameterParserImpl.parseUrlStr(youyiimp.toString());
                        } else {
                            System.out.println("跳出循环");
                            break;
                        }
                    }
                }

            }

        }, 1, 3, TimeUnit.SECONDS);

    }
}
