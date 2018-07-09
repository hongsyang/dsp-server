package cn.shuzilm.util.dbutil;



import java.sql.Connection;
import java.sql.DriverManager;

public class MySqlConnection {
	public static String ip;
	public static String dbName;
	public static String username;
	public static String password;
	public static Connection conn;
	
	/**
	 * 设置连接数据库时的基本信息
	 * @param dbName	数据库名，localhost:3306，比如work_data
	 * @param username	用户名，比如root
	 * @param password	数据库密码，比如123456
	 */
	public MySqlConnection(String ip, String dbName, String username, String password){
		this.ip = ip;
		this.dbName = dbName;
		this.username = username;
		this.password = password;
	}
	
	/**
	 * 连接数据库
	 * @return
	 */
	public Connection getConn(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
//			conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/my_wgdata","root","123456");
			//防止中文入库乱码
			conn = DriverManager.getConnection("jdbc:mysql://"+ip+"/"+dbName+"?useUnicode=true&characterEncoding=utf8",username,password);
			System.out.println("数据库连接成功。。。");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	
//	public static void main(String[] args) {
//		MySqlConnection mySqlConnection = new MySqlConnection("192.168.1.47", "ark", "root", "root");
//		Connection conn = mySqlConnection.getConn();
//		
//		MySqlHelper mySqlHelper = new MySqlHelper();
//		try {
//			mySqlHelper.selecetData(conn, "select * from `ark`.`stand_app`  WHERE package_name ='com.qzone'");
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		
//	}
	
	
}
