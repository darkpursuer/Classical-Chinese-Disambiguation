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
			// �������ݿ����ӿ����
			ConnectionPool connPool = new ConnectionPool(
					"com.mysql.jdbc.Driver",
					"jdbc:mysql://127.0.0.1:3306/wordset",
					"root", "zhangyi604");
			// �½����ݿ����ӿ�
			connPool.createPool();
			// SQL�������
			String sql = "select PrSk from wordsenses where word = '��' and sense = '��' ";
			// �趨����������ʼʱ��
			long start = System.currentTimeMillis();
			// ѭ������100�����ݿ�����
			for (int i = 0; i < 100; i++) {
				Connection conn = connPool.getConnection(); // �����ӿ��л�ȡһ�����õ�����
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while (rs.next()) {
					System.out.println(Double.parseDouble(rs.getString(1)));
				}
				rs.close();
				stmt.close();
				connPool.returnConnection(conn); // ����ʹ������ͷ����ӵ����ӳ�
			}
			System.out.println("����100�ε�ѭ�����ã�ʹ�����ӳػ��ѵ�ʱ��:"
					+ (System.currentTimeMillis() - start) + "ms");
			// connPool.refreshConnections();//ˢ�����ݿ����ӳ����������ӣ������������Ƿ��������У������������Ӷ��ͷŲ��Żص����ӳء�ע�⣺�����ʱ�Ƚϴ�
			connPool.closeConnectionPool();// �ر����ݿ����ӳء�ע�⣺�����ʱ�Ƚϴ�
			
			SQLTools tools = new SQLTools("");
			// �趨����������ʼʱ��
			start = System.currentTimeMillis();
			for (int i = 0; i < 100; i++) {
				String command = "select PrSk from wordsenses where word = '��' and sense = '��'";
			}

			System.out.println("����100�ε�ѭ�����ã���ʹ�����ӳػ��ѵ�ʱ��:"
					+ (System.currentTimeMillis() - start) + "ms");
			/*
			// ��������
			Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
			for (int i = 0; i < 100; i++) {
				// ��������
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
				conn.close();// �ر�����
			}
			*/
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}