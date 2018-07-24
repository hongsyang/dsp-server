package cn.shuzilm.analytics.realtime;


import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.jedis.JedisQueueManager;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.*;

public class FetchDataFromRedis {

    private static String table = "<your table name>";
    private static int threadNum = 10;//默认为10

    private static String phonenixUrl = "jdbc:phoenix:du-s2-idc,du_s3_idc,du_s4_idc:2181:\\hbase-unsecure:yangqi@SHUZILM.KDC:D:\\yangqi.keytab"; // 这里配置zookeeper server的地址,可单个也可多个



    /**
     *
     * @param topic  名称
     * @param tableName odps 中的表名
     */
    public void getData(String topic,String tableName,int batchSize) {
        //要导出的表名
        table = tableName;
        // mongodb 连接
        try {
            ExecutorService pool = Executors.newFixedThreadPool(threadNum);
            ArrayList<DUFlowBean> duFlowBeanArrayList =null;

            while (open){
                Connection conn = PhoenixClient.getConnection(phonenixUrl);
                duFlowBeanArrayList=new ArrayList<>();
                long count = JedisQueueManager.getLength(topic);
                System.out.println("表的数据记录数: " + count+" 实际测试的记录数： 开始的时间："+new Date());
                if(count>=batchSize){
                for (int j = 0; j <batchSize ; j++) {
                   // DUFlowBean dfb  = (DUFlowBean) JedisQueueManager.getElementFromQueue(topic);
                    DUFlowBean dfb = new DUFlowBean();
                    dfb.setInfoId("1234567"+j);
                    dfb.setCreateTime(new Long("1514736000"));
                    duFlowBeanArrayList.add(dfb);
                }
                    Data2PhoenixRunnable data2Phoenix = new Data2PhoenixRunnable(conn,duFlowBeanArrayList,tableName);
                   pool.execute(data2Phoenix);
                }
                else {
                    Thread.sleep(5);
                }
            }

            //关闭资源
            pool.shutdown();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static  boolean open = true;
    public static void main(String[] args) {
        FetchDataFromRedis fetchDataFromODPS = new FetchDataFromRedis();

        if(args.length<3){
            System.err.println("输入参数个数不对。eg:参数 1：mongo 要导入数据集合名称，参数 2：odps 导出数据的表名，参数 3：每个节点的连接数，参数 4：线程数，参数 5：要导的记录数,参数 6：要跳过的记录数，参数 7：批处理数" );
        }

          fetchDataFromODPS.getData(args[0], args[1], Integer.parseInt(args[2]));



        //当有没成功入库的记录数，则会把该组的唯一 id 都打印出来
        //nohup java -jar shuzilmJob-1.0-SNAPSHOT-jar-with-dependencies.jar device_change1 id2_dev_change1 500 8 30000000 0 1000 >id2_dev_change1.log 2>&1 &

        //把上面打印出来的唯一 id 汇总起来，然后从总表把这些 id 的数据抽成一个表，利用下面的执行语句查错，可以把批处理数据调为 1
        //nohup java -jar shuzilmJob-1.0-SNAPSHOT-jar-with-dependencies.jar device_change1 id2_dev_change1 500 8 30000000 0 1 >id2_dev_change1.log 2>&1 &
    }

}
