package ccd.tools.repository.base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionPoolTest {
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		try {
			// 创建数据库连接库对象
			ConnectionPool connPool = new ConnectionPool(
					"com.mysql.jdbc.Driver",
					"jdbc:mysql://127.0.0.1:3306/wordset",
					"root", "zhangyi604");
			// 新建数据库连接库
			connPool.createPool();
			// SQL测试语句
			String sql = "select PrSk from wordsenses where word = '介' and sense = '助' ";
			// 设定程序运行起始时间
			long start = System.currentTimeMillis();
			// 循环测试100次数据库连接
			for (int i = 0; i < 100; i++) {
				Connection conn = connPool.getConnection(); // 从连接库中获取一个可用的连接
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while (rs.next()) {
					System.out.println(Double.parseDouble(rs.getString(1)));
				}
				rs.close();
				stmt.close();
				connPool.returnConnection(conn); // 连接使用完后释放连接到连接池
			}
			System.out.println("经过100次的循环调用，使用连接池花费的时间:"
					+ (System.currentTimeMillis() - start) + "ms");
			// connPool.refreshConnections();//刷新数据库连接池中所有连接，即不管连接是否正在运行，都把所有连接都释放并放回到连接池。注意：这个耗时比较大。
			connPool.closeConnectionPool();// 关闭数据库连接池。注意：这个耗时比较大。
			
			SQLTools tools = new SQLTools("");
			// 设定程序运行起始时间
			start = System.currentTimeMillis();
			for (int i = 0; i < 100; i++) {
				String command = "select PrSk from wordsenses where word = '介' and sense = '助'";
			}

			System.out.println("经过100次的循环调用，不使用连接池花费的时间:"
					+ (System.currentTimeMillis() - start) + "ms");
			/*
			// 导入驱动
			Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
			for (int i = 0; i < 100; i++) {
				// 创建连接
				Connection conn = DriverManager
						.getConnection(
								"jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=MyDataForTest",
								"sa", "yixinhuang");
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while (rs.next()) {
				}
				rs.close();
				stmt.close();
				conn.close();// 关闭连接
			}
			*/
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}