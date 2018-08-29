//package cn.shuzilm.analytics.realtime;
//
//import org.apache.phoenix.jdbc.PhoenixResultSet;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.sql.DriverManager;
//
//
//import java.sql.*;
//import java.util.*;
//import java.util.concurrent.*;
//
///**
// * Created by Administrator on 2018/7/20.
// */
//public class PhoenixClient {
//
//    private final static Logger logger = LoggerFactory.getLogger(PhoenixClient.class);
//
//    private final static int CONN_TIMEOUT = 15;
//
//    private final static int BATCH_SIZE = 1000;
//    /**
//     * 获取 phoenix 连接
//     */
//    private static String phonenixUrl = "jdbc:phoenix:du-s2-idc,du_s3_idc,du_s4_idc:2181:/hbase-unsecure:zhangqian@SHUZILM.KDC:/home/zhangqian/zhangqian.keytab"; // 这里配置zookeeper server的地址,可单个也可多个
//
//    private static String phonenixDriver = "org.apache.phoenix.jdbc.PhoenixDriver";
//
//    // 利用静态块的方式初始化Driver，防止加载不到
//    static {
//        try {  Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
//        } catch (ClassNotFoundException e) {
//            logger.error("加载hbase的phoenix驱动失败", e);
//        }
//    }
//
////    /**
////     * 获取一个Hbase-Phoenix的连接
////     * @param host zookeeper的master-host
////     * @param port zookeeper的master-port
////     * @return
////     */
////    public static Connection getConnection(String phonenixUrl) {
////
////        String url = phonenixUrl;
////
////        return getConnection(url);
////    }
//
//    /**
//     * 获取一个Hbase-Phoenix的连接jdbc的url类似为
//     * jdbc:phoenix:192.168.1.101:2181
//     * jdbc:phoenix:41.test1.com,42.test2.com,43.test3.com:2181
//     * @return
//     */
//    public static Connection getConnection(final String phonenixUrl) {
//        Connection conn = null;
//
//        Properties properties = new Properties();
//        properties.setProperty("hbase.zookeeper.quorum", "du-s2-idc,du-s3-idc,du-s4-idc");
//        properties.setProperty("hbase.master.kerberos.principal", "hbase/_HOST@SHUZILM.KDC");
//        properties.setProperty("hbase.regionserver.kerberos.principal", "hbase/_HOST@SHUZILM.KDC");
//        properties.setProperty("hbase.security.authentication", "kerberos");
//        properties.setProperty("hadoop.security.authentication", "kerberos");
//        properties.setProperty("zookeeper.znode.parent", "/hbase-unsecure");
//
//        try {
//            // Phoenix不支持直接设置连接超时
//            // 所以这里使用线程池的方式来控制数据库连接超时
//            final ExecutorService exec = Executors.newFixedThreadPool(1);
//            Callable<Connection> call = new Callable<Connection>() {
//                public Connection call() throws Exception {
//                    return DriverManager.getConnection(phonenixUrl,properties);
//                }
//            };
//            Future<Connection> future = exec.submit(call);
//            // 如果在设定超时(以秒为单位)之内，还没得到 Connection 对象，则认为连接超时，不继续阻塞
//            conn = future.get(1000 * CONN_TIMEOUT, TimeUnit.MILLISECONDS);
////           if(null == conn){
//                exec.shutdownNow();
//
////           }
//        } catch (InterruptedException e) {
//            logger.error("获取连接线程中断！url=" + phonenixUrl, e);
//        } catch (ExecutionException e) {
//            logger.error("获取连接出错！url=" + phonenixUrl, e);
//        } catch (TimeoutException e) {
//            logger.error("获取连接超时！url=" + phonenixUrl, e);
//        }
//
//        return conn;
//    }
//
//
//    // 关闭一个Phoenix的连接
//    public static void closeConnection(Connection conn) {
//        if (null != conn) {
//            try {
//                conn.close();
//            } catch (SQLException e) {
//                logger.error("关闭连接失败！", e);
//            }
//        }
//    }
//
//    // 关闭一个Phoenix的结果集
//    public static void closeJdbcResultSet(ResultSet rs, Statement st) {
//        try {
//            if (rs != null) {
//                rs.close();
//            }
//            if (st != null) {
//                st.close();
//            }
//        } catch (SQLException e) {
//            logger.error("execute closeJdbcResultSet failed", e);
//        }
//    }
//
//    /**
//     * 检查数据表是否存在
//     * @param conn jdbc连接
//     * @param schema 命名空间
//     * @param table 表名
//     * @return
//     */
//    public static boolean checkTableExists(Connection conn, String schema, String table) {
//
//        boolean retFlag = false;
//
//        if (null != conn) {
//            try {
//                ResultSet rs = conn.getMetaData().getTables(null, schema, table, null);
//                if (rs.next()) {
//                    retFlag = true;
//                }
//            } catch (SQLException e) {
//                logger.error("检查表出错！sql=" + table, e);
//            }
//        }
//
//        return retFlag;
//    }
//
//    /**
//     * 根据phoenix支持的SQL格式，执行DDL操作
//     * create table test (mykey integer not null primary key, mycolumn varchar)
//     * CREATE TABLE user (id varchar PRIMARY KEY, INFO.account varchar ,INFO.passwd varchar)
//     * @param conn jdbc连接
//     * @param sql sql语句
//     * @return
//     */
//    public static boolean executeDDLSql(Connection conn, String sql) {
//
//        boolean retFlag = false;
//
//        int rs = 0;
//
//        if (null != conn) {
//            try {
//                // 耗时监控：记录一个开始时间
//                long startTime = System.currentTimeMillis();
//                Statement stmt = conn.createStatement();
//                retFlag = stmt.execute(sql);
//                // 耗时监控：记录一个结束时间
//                long time = System.currentTimeMillis() - startTime;
//                logger.info("执行时间：" + time + " sql=" + sql);
//            } catch (SQLException e) {
//                logger.error("DDL执行出错！sql=" + sql, e);
//            }
//        }
//
//        return retFlag;
//    }
//
//    /**
//     * 根据phoenix支持的SQL格式，执行增删改操作
//     * @param conn jdbc连接
//     * @param sql sql语句
//     * @return
//     */
//    public static boolean executeUpdateSql(Connection conn, String sql) {
//
//        boolean retFlag = false;
//        Statement stmt = null;
//        int rs = 0;
//
//        if (null != conn) {
//            try {
//                // 耗时监控：记录一个开始时间
//                long startTime = System.currentTimeMillis();
//                stmt = conn.createStatement();
//                rs = stmt.executeUpdate(sql);
//                // Phoenix进行数据更改时不会自动的commit,需要手动提交
//                conn.commit();
//                if (rs >= 1) {
//                    retFlag = true;
//                }
//                // 耗时监控：记录一个结束时间
//                long time = System.currentTimeMillis() - startTime;
//                logger.info("执行时间：" + time + " sql=" + sql);
//            } catch (SQLException e) {
//                logger.error("SQL执行出错！sql=" + sql, e);
//            } finally {
//                closeJdbcResultSet(null, stmt);
//            }
//        }
//
//        return retFlag;
//    }
//
//    /**
//     * 根据phoenix支持的SQL格式，批量执行新增修改删除操作
//     * @param conn jdbc连接
//     * @param sqlList sql语句
//     */
//    public static boolean executeUpdateSqlBatch(Connection conn, List<String> sqlList) {
//
//        boolean retFlag = false;
//        Statement stmt = null;
//        int rs = 0;
//
//        if (null != conn) {
//            try {
//                // 耗时监控：记录一个开始时间
//                long startTime = System.currentTimeMillis();
//                // 准备查询
//                stmt = conn.createStatement();
//                int size = sqlList.size();
//                for (int i = 0; i < size; i++) {
//                    rs = stmt.executeUpdate(sqlList.get(i));
//                    if (i % BATCH_SIZE == 0) {
//                        conn.commit();
//                    }
//                }
//                // Phoenix进行数据更改时不会自动的commit,需要手动提交
//                conn.commit();
//
//                retFlag = true;
//
//                // 耗时监控：记录一个结束时间
//                long time = System.currentTimeMillis() - startTime;
//            } catch (SQLException e) {
//                logger.error("批量SQL执行出错", e);
//            } finally {
//                closeJdbcResultSet(null, stmt);
//            }
//        }
//
//        return retFlag;
//    }
//
//    /**
//     * 根据phoenix支持的SQL格式，统计数据量 如果数据量很大，统计会很慢
//     * @param conn jdbc连接
//     * @param sql sql语句
//     * @return
//     */
//    public static String executeCountSql(Connection conn, String sql) {
//
//        Statement stmt = null;
//        ResultSet rsCount;
//        String count = "0";
//
//        if (null != conn) {
//            // 耗时监控：记录一个开始时间
//            long startTime = System.currentTimeMillis();
//
//            try {
//                // 准备查询
//                stmt = conn.createStatement();
//                rsCount = stmt.executeQuery(sql);
//                if (rsCount.next()) {
//                    count = rsCount.getString(1);
//                }
//
//                // 耗时监控：记录一个结束时间
//                long time = System.currentTimeMillis() - startTime;
//                logger.info("执行时间：" + time + " sql=" + sql);
//
//            } catch (SQLException e) {
//                logger.error("SQL执行出错！sql=" + sql, e);
//            } finally {
//                closeJdbcResultSet(null, stmt);
//            }
//        }
//
//        return count;
//    }
//
//    /**
//     * 根据phoenix支持的SQL格式，查询Hbase的数据，并返回Map格式的数据
//     * @param conn jdbc连接
//     * @param sql sql语句
//     * @return
//     */
//    public static List<Map<String, Object>> getMapBySqlQuery(Connection conn, String sql) {
//
//        List<Map<String, Object>> reslist = new ArrayList<Map<String, Object>>();
//
//        Statement stmt = null;
//        PhoenixResultSet set = null;
//
//        if (null != conn) {
//            // 耗时监控：记录一个开始时间
//            long startTime = System.currentTimeMillis();
//
//            try {
//                // 准备查询
//                stmt = conn.createStatement();
//                set = (PhoenixResultSet) stmt.executeQuery(sql);
//
//                // 查询出来的列是不固定的，所以这里通过遍历的方式获取列名
//                ResultSetMetaData meta = set.getMetaData();
//                ArrayList<String> cols = new ArrayList<String>();
//
//                while (set.next()) {
//                    if (cols.size() == 0) {
//                        for (int i = 1, count = meta.getColumnCount(); i <= count; i++) {
//                            cols.add(meta.getColumnName(i));
//                        }
//                    }
//
//                    Map<String, Object> perCol = new LinkedHashMap<String, Object>();
//                    for (int i = 0, len = cols.size(); i < len; i++) {
//                        perCol.put(cols.get(i), set.getObject(cols.get(i)));
//                    }
//                    reslist.add(perCol);
//                }
//                // 耗时监控：记录一个结束时间
//                long time = System.currentTimeMillis() - startTime;
//                logger.info("执行时间：" + time + " sql=" + sql);
//            } catch (SQLException e) {
//                logger.error("SQL执行出错！sql=" + sql, e);
//            } finally {
//                closeJdbcResultSet(set, stmt);
//            }
//        }
//
//        return reslist;
//    }
//
//    public static void main(String[] args) {
//        String ddlSql = "CREATE TABLE PHOENIXTEST (ID VARCHAR PRIMARY KEY,INFO.ACCOUNT VARCHAR ,INFO.PASSWD VARCHAR)";
//        String updateSql = "upsert into PHOENIXTEST (ID, INFO.ACCOUNT, INFO.PASSWD) values('001', 'admin', 'admin')";
//        String querySql = "select count(1) from PHOENIXTEST"; // 大小写敏感
//
//        // 获取一个Phoenix连接
//        Connection conn = PhoenixClient.getConnection(phonenixDriver);
//        if (conn == null) {
//            System.out.println("Phoenix DB连接超时！");
//        } else {
//            // 检查数据表是否存在
//            boolean tbExists = checkTableExists(conn, null, "PHOENIXTEST");
//            if (!tbExists) {
//                tbExists = executeDDLSql(conn, ddlSql);
//            }
//
//            if (tbExists && executeUpdateSql(conn, updateSql)) {
//                String result = executeCountSql(conn, querySql);
//                System.out.println("统计数量：" + result);
//            }
//        }
//
//        closeConnection(conn);
//
//    }
//
//}
