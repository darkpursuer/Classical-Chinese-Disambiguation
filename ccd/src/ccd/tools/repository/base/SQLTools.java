package ccd.tools.repository.base;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import ccd.tools.entity.EntityBase;

public class SQLTools implements IDataTools {

	private String tableName;

	private static BoneCP connPool = null;
	private static BoneCPConfig config = new BoneCPConfig();

	static {
		try {
			// class name for mysql driver
			Class.forName("com.mysql.jdbc.Driver");

			config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/yz3940_nlp?useUnicode=true&characterEncoding=UTF-8");
			config.setUsername("root");
			config.setPassword("");
//			config.setJdbcUrl("jdbc:mysql://warehouse.cims.nyu.edu:3306/yz3940_nlp?useUnicode=true&characterEncoding=UTF-8");
//			config.setUsername("yz3940");
//			config.setPassword("bru7gntn");
			config.setMinConnectionsPerPartition(10);
			config.setMaxConnectionsPerPartition(50);
			config.setPartitionCount(1);
			connPool = new BoneCP(config);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SQLTools(String tableName) {
		this.tableName = tableName;
	}

	private Connection setConnection() {
		try {
			return connPool.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void closeConnection(Connection c) {
		try {
			if (c != null)
				c.close();
			c = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public boolean insert(EntityBase entity) {
		Connection c = null;
		Statement stat = null;
		try {
			ArrayList<String> fields = new ArrayList<>();
			ArrayList<String> values = new ArrayList<>();

			for (Map.Entry<String, String> entry : entity.data.entrySet()) {
				fields.add("`" + entry.getKey() + "`");
				values.add("'" + entry.getValue() + "'");
			}

			String command = String.format("INSERT INTO `%s` (%s) VALUES (%s)", tableName, String.join(", ", fields),
					String.join(", ", values));

			c = setConnection();
			stat = c.createStatement();
			stat.execute(command);
			closeConnection(c);

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			closeConnection(c);
			try {
				stat.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean insert(List<EntityBase> entities) {
		Connection c = null;
		Statement stat = null;
		try {
			ArrayList<String> fields = new ArrayList<>();

			if (!entities.isEmpty()) {

				c = setConnection();

				EntityBase first = entities.get(0);
				for (Map.Entry<String, String> entry : first.data.entrySet()) {
					fields.add(entry.getKey());
				}

				String commandHeader = String.format("INSERT INTO `%s` (`%s`) VALUES ", tableName,
						String.join("`, `", fields));
				int index = 0;
				StringBuilder builder = new StringBuilder(commandHeader);

				for (; index < entities.size(); index++) {
					EntityBase entity = entities.get(index);

					ArrayList<String> values = new ArrayList<>();

					for (String field : fields) {
						values.add("'" + entity.get(field) + "'");
					}

					builder.append(String.format("(%s)", String.join(", ", values)));
					builder.append(", ");

					if (builder.length() > 120000) {
						builder.delete(builder.length() - 2, builder.length());

						stat = c.createStatement();
						stat.execute(builder.toString());

						builder = new StringBuilder(commandHeader);
					}
				}
				builder.delete(builder.length() - 2, builder.length());

				stat = c.createStatement();
				stat.execute(builder.toString());
				closeConnection(c);
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			closeConnection(c);
		}
	}

	@Override
	public List<EntityBase> get(String query) {
		List<EntityBase> result = new ArrayList<>();

		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			c = setConnection();
			ps = c.prepareStatement(query);
			rs = ps.executeQuery();
			closeConnection(c);

			String[] columns = null;

			while (rs.next()) {
				if (columns == null) {
					ResultSetMetaData metaData = rs.getMetaData();
					columns = new String[metaData.getColumnCount()];
					for (int i = 0; i < columns.length; i++) {
						columns[i] = metaData.getColumnName(i + 1);
					}
				}

				EntityBase entity = new EntityBase();

				for (int i = 0; i < columns.length; i++) {
					entity.put(columns[i], rs.getString(columns[i]));
				}

				result.add(entity);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection(c);
		}

		return result;
	}

	@Override
	public List<EntityBase> get(String query, String order) {
		return get(query);
	}

	@Override
	public boolean modify(String query, EntityBase entity) {
		Connection c = null;
		Statement stat = null;
		try {
			ArrayList<String> values = new ArrayList<>();

			for (Map.Entry<String, String> entry : entity.data.entrySet()) {
				values.add("`" + entry.getKey() + "` = '" + entry.getValue() + "'");
			}

			query = query.replace("#", String.join(", ", values));

			c = setConnection();
			stat = c.createStatement();
			stat.execute(query);
			closeConnection(c);

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			closeConnection(c);
			try {
				stat.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean delete(String query) {
		Connection c = null;
		Statement stat = null;
		try {
			c = setConnection();
			stat = c.createStatement();
			stat.execute(query);
			closeConnection(c);

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			closeConnection(c);
			try {
				stat.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void remove() {
		Connection c = null;
		Statement stat = null;
		try {
			c = setConnection();
			stat = c.createStatement();
			stat.execute("TRUNCATE `" + tableName + "`;");
			closeConnection(c);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection(c);
			try {
				stat.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
