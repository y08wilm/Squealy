package lc.kristen.sqlite;

import static lc.kristen.sqlite.runnable.HealthMonitor.lockedSqlDatabaseFiles;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FileConfiguration {

	public String fileName;
	public boolean debug = false;
	public boolean safeMode = false;

	Connection con;
	Statement stmt;

	public FileConfiguration(String fileName) throws ClassNotFoundException,
			SQLException {
		Class.forName("org.sqlite.JDBC");
		this.fileName = fileName;
		if (safeMode == false) {
			this.openFile();
		}
	}

	void openFile() throws SQLException {
		String fn = "jdbc:sqlite:" + fileName;
		if (lockedSqlDatabaseFiles.contains(fn) == true) {
			throw new SQLException(fn + " is locked!");
		}
		if (debug) {
			System.out.println(fn);
		}
		con = DriverManager.getConnection(fn);
		con.setAutoCommit(true);
		stmt = con.createStatement();
		if (lockedSqlDatabaseFiles.contains(fn) == false) {
			lockedSqlDatabaseFiles.add(fn);
		}
	}

	boolean tableExists(String tableName) throws SQLException {
		if (safeMode == true || con == null) {
			this.openFile();
		}
		try {
			tableName = tableName.replace(".", "_").replace("-", "");
			String arg0 = "SELECT VALUE " + "FROM '" + tableName + "';";
			if (debug) {
				System.out.println(arg0);
			}
			ResultSet rs = stmt.executeQuery(arg0);
			boolean exists = true;
			if (!rs.next()) {
				exists = false;
			}
			rs.close();
			if (safeMode == true) {
				this.close();
			}
			return exists;
		} catch (SQLException e) {
			if (safeMode == true) {
				this.close();
			}
			throw e;
		}
	}

	void dropTable(String tableName) throws SQLException {
		if (safeMode == true || con == null) {
			this.openFile();
		}
		try {
			tableName = tableName.replace(".", "_").replace("-", "");
			String arg0 = "DROP TABLE IF EXISTS " + tableName + ";";
			if (debug) {
				System.out.println(arg0);
			}
			stmt.executeUpdate(arg0);
			if (safeMode == true) {
				this.close();
			}
		} catch (SQLException e) {
			if (safeMode == true) {
				this.close();
			}
			throw e;
		}
	}

	void createTable(String tableName, Class<?> type) throws SQLException {
		if (safeMode == true || con == null) {
			this.openFile();
		}
		try {
			tableName = tableName.replace(".", "_").replace("-", "");
			if (type == int.class || type == Integer.class) {
				String arg0 = "CREATE TABLE IF NOT EXISTS " + tableName
						+ "(ID INT PRIMARY KEY, VALUE INT);";
				if (debug) {
					System.out.println(arg0);
				}
				stmt.executeUpdate(arg0);
			} else if (type == double.class || type == Double.class) {
				String arg0 = "CREATE TABLE IF NOT EXISTS " + tableName
						+ "(ID INT PRIMARY KEY, VALUE DECIMAL(10, 8));";
				if (debug) {
					System.out.println(arg0);
				}
				stmt.executeUpdate(arg0);
			} else if (type == long.class || type == Long.class) {
				String arg0 = "CREATE TABLE IF NOT EXISTS " + tableName
						+ "(ID INT PRIMARY KEY, VALUE SIGNED BIGINT);";
				if (debug) {
					System.out.println(arg0);
				}
				stmt.executeUpdate(arg0);
			} else if (type == String.class) {
				String arg0 = "CREATE TABLE IF NOT EXISTS " + tableName
						+ "(ID INT PRIMARY KEY, VALUE TEXT);";
				if (debug) {
					System.out.println(arg0);
				}
				stmt.executeUpdate(arg0);
			} else {
				if (safeMode == true) {
					this.close();
				}
				throw new IllegalArgumentException("Class type not supported!");
			}
			if (safeMode == true) {
				this.close();
			}
		} catch (SQLException e) {
			if (safeMode == true) {
				this.close();
			}
			throw e;
		}
	}

	void replace(String tableName, String key, Object value)
			throws SQLException {
		if (safeMode == true || con == null) {
			this.openFile();
		}
		try {
			tableName = tableName.replace(".", "_").replace("-", "");
			createTable(tableName, value.getClass());
			if (safeMode == true) {
				this.openFile();
			}
			if (value instanceof String) {
				String arg0 = "REPLACE INTO " + tableName + " (ID, " + key
						+ ") VALUES (0, '" + value + "');";
				if (debug) {
					System.out.println(arg0);
				}
				stmt.executeUpdate(arg0);
			} else {
				String arg0 = "REPLACE INTO " + tableName + " (ID, " + key
						+ ") VALUES (0, " + value + ");";
				if (debug) {
					System.out.println(arg0);
				}
				stmt.executeUpdate(arg0);
			}
			if (safeMode == true) {
				this.close();
			}
		} catch (SQLException e) {
			if (safeMode == true) {
				this.close();
			}
			throw e;
		}
	}

	void insert(String tableName, String key, Object value) throws SQLException {
		if (safeMode == true || con == null) {
			this.openFile();
		}
		try {
			tableName = tableName.replace(".", "_").replace("-", "");
			createTable(tableName, value.getClass());
			if (safeMode == true) {
				this.openFile();
			}
			if (value instanceof String) {
				String arg0 = "INSERT INTO " + tableName + " (ID, " + key
						+ ") VALUES (0, '" + value + "');";
				if (debug) {
					System.out.println(arg0);
				}
				stmt.executeUpdate(arg0);
			} else {
				String arg0 = "INSERT INTO " + tableName + " (ID, " + key
						+ ") VALUES (0, " + value + ");";
				if (debug) {
					System.out.println(arg0);
				}
				stmt.executeUpdate(arg0);
			}
			if (safeMode == true) {
				this.close();
			}
		} catch (SQLException e) {
			if (safeMode == true) {
				this.close();
			}
			throw e;
		}
	}

	int selectIntBy(String tableName, String key, int def) throws SQLException {
		if (safeMode == true || con == null) {
			this.openFile();
		}
		try {
			tableName = tableName.replace(".", "_").replace("-", "");
			String arg0 = "SELECT * FROM " + tableName + ";";
			if (debug) {
				System.out.println(arg0);
			}
			ResultSet rs = stmt.executeQuery(arg0);
			while (rs.next()) {
				int res = rs.getInt(key);
				if (safeMode == true) {
					this.close();
				}
				return res;
			}
			rs.close();
			if (safeMode == true) {
				this.close();
			}
			return def;
		} catch (SQLException e) {
			if (safeMode == true) {
				this.close();
			}
			return def;
		}
	}

	Double selectDoubleBy(String tableName, String key, double def)
			throws SQLException {
		if (safeMode == true || con == null) {
			this.openFile();
		}
		try {
			tableName = tableName.replace(".", "_").replace("-", "");
			String arg0 = "SELECT * FROM " + tableName + ";";
			if (debug) {
				System.out.println(arg0);
			}
			ResultSet rs = stmt.executeQuery(arg0);
			while (rs.next()) {
				double res = rs.getDouble(key);
				if (safeMode == true) {
					this.close();
				}
				return res;
			}
			rs.close();
			if (safeMode == true) {
				this.close();
			}
			return def;
		} catch (SQLException e) {
			if (safeMode == true) {
				this.close();
			}
			return def;
		}
	}

	Long selectLongBy(String tableName, String key, long def)
			throws SQLException {
		if (safeMode == true || con == null) {
			this.openFile();
		}
		try {
			tableName = tableName.replace(".", "_").replace("-", "");
			String arg0 = "SELECT * FROM " + tableName + ";";
			if (debug) {
				System.out.println(arg0);
			}
			ResultSet rs = stmt.executeQuery(arg0);
			while (rs.next()) {
				long res = rs.getLong(key);
				if (safeMode == true) {
					this.close();
				}
				return res;
			}
			rs.close();
			if (safeMode == true) {
				this.close();
			}
			return def;
		} catch (SQLException e) {
			if (safeMode == true) {
				this.close();
			}
			return def;
		}
	}

	String selectStringBy(String tableName, String key, String def)
			throws SQLException {
		if (safeMode == true || con == null) {
			this.openFile();
		}
		try {
			tableName = tableName.replace(".", "_").replace("-", "");
			String arg0 = "SELECT * FROM " + tableName + ";";
			if (debug) {
				System.out.println(arg0);
			}
			ResultSet rs = stmt.executeQuery(arg0);
			while (rs.next()) {
				String res = rs.getString(key);
				if (safeMode == true) {
					this.close();
				}
				return res;
			}
			rs.close();
			if (safeMode == true) {
				this.close();
			}
			return def;
		} catch (SQLException e) {
			if (safeMode == true) {
				this.close();
			}
			return def;
		}
	}

	public boolean delete() throws SQLException {
		if (safeMode == false) {
			this.close();
		}
		return new File(fileName).delete();
	}

	public ConfigurationSection getConfigurationSection() {
		return new ConfigurationSection(this);
	}

	public void close() throws SQLException {
		String fn = "jdbc:sqlite:" + fileName;
		if (stmt == null) {
		} else {
			stmt.close();
		}
		if (con == null) {
		} else {
			con.close();
		}
		stmt = null;
		con = null;
		if (lockedSqlDatabaseFiles.contains(fn) == true) {
			lockedSqlDatabaseFiles.remove(fn);
		}
	}
}
