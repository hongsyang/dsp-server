package cn.shuzilm.util.db;

import com.yao.util.db.DbUtil;
import com.yao.util.pool.NewObjectException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Update {

	private static final DbUtil dbUtil = new DbUtil();
//	static {
//		dbUtil.setQueryTimeout(SystemConfig.C.getInteger("db.queryTimeout"));
//	}

	public int doUpdate(String sql) throws SQLException {
		Connection con = null;
		try {
			con = DBPool.getInstance().getConnection();
			return dbUtil.update(con, sql);
		} catch (NewObjectException e) {
			throw new SQLException(e);
		} finally {
			DBPool.getInstance().releaseConnection(con);
		}
	}

	public int doUpdate(String sql, Object[] params) throws SQLException {
		Connection con = null;
		try {
			con = DBPool.getInstance().getConnection();
			return dbUtil.update(con, sql, params);
		} catch (NewObjectException e) {
			throw new SQLException(e);
		} finally {
			DBPool.getInstance().releaseConnection(con);
		}
	}

	/** 执行一批SQL语句 */
	public static int[] updateInsertBatch( String[] sqls) throws Exception {
		Connection con = DBPool.getInstance().getConnection();
		if (sqls == null) {
			return null;
		}
		Statement sm = null;
		try {
			sm = con.createStatement();
			for (int i = 0; i < sqls.length; i++) {
				sm.addBatch(sqls[i]);
				// 将所有的SQL语句添加到Statement中
			}
			// 一次执行多条SQL语句
			return sm.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			sm.close();
			DBPool.getInstance().releaseConnection(con);
		}
		return null;
	}

	
}
