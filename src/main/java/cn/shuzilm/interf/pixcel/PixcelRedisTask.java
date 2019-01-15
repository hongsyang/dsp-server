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

                if (JedisQueueManager.getLength("adviewclick") > 0) {
                    log.debug("adviewclick=线程数--------------------" + Thread.currentThread().getName());
                    while (true) {
                        long start = System.currentTimeMillis();
                        Object adviewclick = JedisQueueManager.getElementFromQueue("adviewclick");
                        if (adviewclick != null) {
                            AdViewClickParameterParserImpl.parseUrlStr(adviewclick.toString());
                            long end = System.currentTimeMillis();
                            log.debug("正常耗时：{}",end - start);
                        } else {
                            log.debug("跳出循环");
                            break;
                        }
                        long end1 = System.currentTimeMillis();
                        log.debug("取到耗时：{}",end1 -start );
                    }
                }
                if (JedisQueueManager.getLength("adviewexp") > 0) {
                    log.debug("adviewexp=线程数--------------------" + Thread.currentThread().getName());
                    while (true) {
                        long start = System.currentTimeMillis();
                        Object adviewexp = JedisQueueManager.getElementFromQueue("adviewexp");
                        long end1 = System.currentTimeMillis();
                        log.debug("redis取值耗时：{}，线程数：{}",end1 - start,Thread.currentThread().getName());
                        if (adviewexp != null) {
                            long end = System.currentTimeMillis();
                            AdViewExpParameterParserImpl.parseUrlStr(adviewexp.toString());
                            log.debug("正常耗时：{}",end - start);
                        } else {
                            log.debug("跳出循环");
                            break;
                        }
                        long end2 = System.currentTimeMillis();
                        log.debug("取到耗时：{}",end2 -start );
                    }
                }
                if (JedisQueueManager.getLength("adviewnurl") > 0) {
                    log.debug("adviewnurl=线程数--------------------" + Thread.currentThread().getName());
                    while (true) {
                        long start = System.currentTimeMillis();
                        Object adviewnurl = JedisQueueManager.getElementFromQueue("adviewnurl");

                        if (adviewnurl != null) {
                            AdViewNurlParameterParserImpl.parseUrlStr(adviewnurl.toString());
                        } else {
                            log.debug("跳出循环");
                            break;
                        }
                    }
                }
                if (JedisQueueManager.getLength("lingjiclick") > 0) {
                    log.debug("lingjiclick=线程数--------------------" + Thread.currentThread().getName());
                    while (true) {
                        Object lingjiclick = JedisQueueManager.getElementFromQueue("lingjiclick");
                        if (lingjiclick != null) {
                            LingJiClickParameterParserImpl.parseUrlStr(lingjiclick.toString());
                        } else {
                            log.debug("跳出循环");
                            break;
                        }
                    }
                }
                if (JedisQueueManager.getLength("lingjiexp") > 0) {
                    log.debug("lingjiexp=线程数--------------------" + Thread.currentThread().getName());
                    while (true) {
                        Object lingjiexp = JedisQueueManager.getElementFromQueue("lingjiexp");
                        if (lingjiexp != null) {
                            LingJiExpParameterParserImpl.parseUrlStr(lingjiexp.toString());
                        } else {
                            log.debug("跳出循环");
                            break;
                        }
                    }
                }
                if (JedisQueueManager.getLength("youyiclick") > 0) {
                    log.debug("youyiclick=线程数--------------------" + Thread.currentThread().getName());
                    while (true) {
                        Object youyiclick = JedisQueueManager.getElementFromQueue("youyiclick");
                        if (youyiclick != null) {
                            YouYiClickParameterParserImpl.parseUrlStr(youyiclick.toString());
                        } else {
                            log.debug("跳出循环");
                            break;
                        }
                    }
                }
                if (JedisQueueManager.getLength("youyiexp") > 0) {
                    log.debug("youyiexp=线程数--------------------" + Thread.currentThread().getName());

                    while (true) {
                        Object youyiexp = JedisQueueManager.getElementFromQueue("youyiexp");
                        if (youyiexp != null) {
                            YouYiExpParameterParserImpl.parseUrlStr(youyiexp.toString());
                        } else {
                            log.debug("跳出循环");
                            break;
                        }
                    }
                }
                if (JedisQueueManager.getLength("youyiimp") > 0) {
                    log.debug("youyiimp=线程数--------------------" + Thread.currentThread().getName());
                    while (true) {
                        Object youyiimp = JedisQueueManager.getElementFromQueue("youyiimp");
                        if (youyiimp != null) {
                            YouYiImpParameterParserImpl.parseUrlStr(youyiimp.toString());
                        } else {
                            log.debug("跳出循环");
                            break;
                        }
                    }
                }

            }

        }, 1, 1, TimeUnit.SECONDS);

    }
}
