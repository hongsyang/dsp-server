package cn.shuzilm.util.db;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.yao.util.pool.NewObjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接池
 * 
 * @author yaoming
 */
public class DBPool {

	private static DBPool it;

    private BoneCP connectionPool;

    private final static Logger logger = LoggerFactory.getLogger(DBPool.class);

	private DBPool() throws NewObjectException {
        connectionPool = this.getConnectionPool();
	}

	/**
	 * 释放链接
	 * 
	 * @param con
	 */
	public void releaseConnection(Connection con) {
		if (null == con) return;
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	/**
	 * 得到数据库连接池对像
	 * 
	 * @return
	 */
	public static DBPool getInstance() {
		if (null == it) {
			synchronized (DBPool.class) {
				if (null == it) {
					try {
						it = new DBPool();
					} catch (NewObjectException e) {
                        logger.error("get dbPool object error.",e);
						throw new RuntimeException(e);
					}
				}
			}
		}
		return it;
	}


    public BoneCP getConnectionPool() {

        BoneCPConfig config;

        try{
            Class.forName("com.mysql.jdbc.Driver");
            config = new BoneCPConfig("baofeng");
        }catch(Exception e){
            e.printStackTrace();
            logger.warn("cannot init the bonecp-config.xml:"+e);
            config = new BoneCPConfig();
            config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/auto_rencoder?useUnicode=true&amp;characterEncoding=utf8");
            config.setUsername("root");
            config.setPassword("223238");
            config.setMaxConnectionsPerPartition(10);
            config.setMinConnectionsPerPartition(5);
            config.setPartitionCount(2);
            config.setAcquireIncrement(2);
            config.setReleaseHelperThreads(0);
            config.setConnectionTestStatement("/* ping */ SELECT 1");
            config.setIdleConnectionTestPeriod(240);
            config.setIdleMaxAge(60);
        }
        BoneCP boneCP = null;
        try {
            boneCP = new BoneCP(config);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.warn(boneCP.getConfig().toString());
        return boneCP;
    }

    public Connection getConnection() throws SQLException, NewObjectException {
        long step0 = System.currentTimeMillis();
        Connection connection = null;
        try{
            connection = connectionPool.getConnection();
        }catch (Exception e) {
            throw new NewObjectException(e);
        }
        long step1 = System.currentTimeMillis();
        if ((step1 - step0) > 500) {
            logger.warn("getConnection too slow:" + (step1 - step0) +",leased connections:"+connectionPool.getTotalLeased());
        }
        return connection;
    }
}
