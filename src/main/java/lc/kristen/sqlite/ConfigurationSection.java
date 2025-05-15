package lc.kristen.sqlite;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationSection {

	protected volatile FileConfiguration root;

	protected String cpath = "";

	public ConfigurationSection(FileConfiguration config) {
		this.root = config;
	}

	protected ConfigurationSection(FileConfiguration config, String path) {
		this.root = config;
		this.cpath = path;
	}

	public Set<String> getKeys(boolean deep) {
		Set<String> results = new HashSet<>();
		DatabaseMetaData md;
		try {
			if (root.safeMode == true || root.con == null) {
				root.openFile();
			}
			md = root.con.getMetaData();
			ResultSet rs = md.getTables(null, null, "%", null);
			while (rs.next()) {
				String tableName = rs.getString(3);
				if (tableName.startsWith(cpath)) {
					String sub = tableName.substring(cpath.length());
					if (sub.length() == 0)
						continue;
					if (sub.charAt(0) == '_') {
						sub = sub.substring(1);
						if (sub.contains("_") && !deep)
							sub = sub.substring(0, sub.indexOf("_"));
						if (!results.contains(sub))
							results.add(sub);
					}
				}
			}
			if (root.safeMode == true) {
				root.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return results;
	}

	public boolean contains(String path) {
		if (cpath.length() > 0)
			path = cpath + "_" + path;
		try {
			return root.tableExists(path);
		} catch (SQLException e) {
			return false;
		}
	}

	public String getCurrentPath() {
		return cpath;
	}

	public String getName() {
		if (cpath.length() > 0)
			return cpath.substring(cpath.lastIndexOf("_") + 1);
		return "";
	}

	public FileConfiguration getRoot() {
		return root;
	}

	public ConfigurationSection getParent() {
		if (cpath.length() > 0) {
			int index;
			if ((index = cpath.lastIndexOf("_")) != -1) {
				return new ConfigurationSection(root, cpath.substring(0, index));
			}
		}
		return null;
	}

	public void replace(String path, Object value) throws SQLException {
		if (cpath.length() > 0)
			path = cpath + "_" + path;
		if (value == null) {
			root.dropTable(path);
		} else {
			if (value instanceof Boolean) {
				if ((boolean) value == true) {
					value = 1;
				} else if ((boolean) value == false)
					value = 0;
			}
			root.replace(path, "value", value);
		}
	}

	public void executeBatch() throws SQLException {
		root.stmt.executeBatch();
	}

	// Primitives

	public String getString(String path) throws SQLException {
		return getString(path, null);
	}

	public String getString(String path, String def) throws SQLException {
		if (cpath.length() > 0)
			path = cpath + "_" + path;
		return root.selectStringBy(path, "value", def);
	}

	public int getInt(String path) throws SQLException {
		return getInt(path, 0);
	}

	public int getInt(String path, int def) throws SQLException {
		if (cpath.length() > 0)
			path = cpath + "_" + path;
		return root.selectIntBy(path, "value", def);
	}

	public boolean getBoolean(String path) throws SQLException {
		return getBoolean(path, false);
	}

	public boolean getBoolean(String path, boolean def) throws SQLException {
		if (cpath.length() > 0)
			path = cpath + "_" + path;
		int defInt = 1;
		if (def == false)
			defInt = 0;
		int value = root.selectIntBy(path, "value", defInt);
		if (value == 0) {
			return false;
		} else
			return true;
	}

	public double getDouble(String path) throws SQLException {
		return getDouble(path, 0);
	}

	public double getDouble(String path, double def) throws SQLException {
		if (cpath.length() > 0)
			path = cpath + "_" + path;
		return root.selectDoubleBy(path, "value", def);
	}

	public long getLong(String path) throws SQLException {
		return getLong(path, 0);
	}

	public long getLong(String path, long def) throws SQLException {
		if (cpath.length() > 0)
			path = cpath + "_" + path;
		return root.selectLongBy(path, "value", def);
	}

	public ConfigurationSection getConfigurationSection(String path) {
		path = path.replace(".", "_").replace("-", "");
		if (cpath.length() > 0)
			path = cpath + "_" + path;
		return new ConfigurationSection(root, path);
	}
}
