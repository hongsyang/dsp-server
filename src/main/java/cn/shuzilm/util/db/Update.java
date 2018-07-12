package cn.shuzilm.util.db;

import com.yao.util.db.DbUtil;
import com.yao.util.pool.NewObjectException;

import java.sql.Connection;
import java.sql.SQLException;

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
	
}
