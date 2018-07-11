package cn.shuzilm.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MySqlHelper {

	public String selecetData(Connection conn, String sql){
		String resultStr = "";
		try {
			ResultSet rs = null;
			
			PreparedStatement preparedStatement = conn.prepareStatement(sql);// 准备查询语句
			rs = preparedStatement.executeQuery();// 执行查询语句
			while (rs.next()) {
//				System.out.println(rs.getString("id"));
				resultStr = rs.getString("appId");
			}
			rs.close();
			preparedStatement.close();
			return resultStr;
		} catch (Exception e) {
			return resultStr;
		}
		
	}
	
	/**
	 * mysql插入语句
	 * @param conn
	 * @param sql
	 */
	public void insertData(Connection conn, String sql){
		try {
			PreparedStatement preparedStatement = conn.prepareStatement(sql);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (Exception e) {
			
		}
		
	}
	
	
}
