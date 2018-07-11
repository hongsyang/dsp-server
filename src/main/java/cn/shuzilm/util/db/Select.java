package cn.shuzilm.util.db;

import com.yao.util.db.DbUtil;
import com.yao.util.db.bean.ResultList;
import com.yao.util.db.bean.ResultMap;
import com.yao.util.pool.NewObjectException;

import java.sql.Connection;
import java.sql.SQLException;

public class Select {

	private static DbUtil dbUtil = new DbUtil();

//	static {
//        if(dbUtil == null)
//            dbUtil = new DbUtil();
//		dbUtil.setQueryTimeout(SystemConfig.C.getInteger("db.queryTimeout"));
//	}

	public ResultList select(String sql) throws SQLException {
		Connection con = null;
		try {
			con = DBPool.getInstance().getConnection();
			return dbUtil.select(con, sql);
		} catch (NewObjectException e) {
			throw new SQLException(e);
		} finally {
			DBPool.getInstance().releaseConnection(con);
		}
	}

	public ResultList select(String sql, Object[] params) throws SQLException {
		Connection con = null;
		try {
			con = DBPool.getInstance().getConnection();
			return dbUtil.select(con, sql, params);
		} catch (NewObjectException e) {
			throw new SQLException(e);
		} finally {
			DBPool.getInstance().releaseConnection(con);
		}
	}

	public ResultMap selectSingle(String sql) throws SQLException {
		Connection con = null;
		try {
			con = DBPool.getInstance().getConnection();
			return dbUtil.select(con, sql).peek();
		} catch (NewObjectException e) {
			throw new SQLException(e);
		} finally {
			DBPool.getInstance().releaseConnection(con);
		}
	}

	public ResultMap selectSingle(String sql, Object[] params) throws SQLException {
		Connection con = null;
		try {
			con = DBPool.getInstance().getConnection();
			return dbUtil.select(con, sql, params).peek();
		} catch (NewObjectException e) {
			throw new SQLException(e);
		} finally {
			DBPool.getInstance().releaseConnection(con);
		}
	}

}
