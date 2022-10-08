package lc.kristen.sqlite;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationSection {

	private volatile FileConfiguration root;

	private String cpath = "";

	public ConfigurationSection(FileConfiguration config) {
		this.root = config;
	}

	ConfigurationSection(FileConfiguration config, String path) {
		this.root = config;
		this.cpath = path;
	}

	/**
	 * Gets a set containing all keys in this section.
	 * <p>
	 * If deep is set to true, then this will contain all the keys within any
	 * child {@link ConfigurationSection}s (and their children, etc). These will
	 * be in a valid path notation for you to use.
	 * <p>
	 * If deep is set to false, then this will contain only the keys of any
	 * direct children, and not their own children.
	 *
	 * @param deep
	 *            Whether or not to get a deep list, as opposed to a shallow
	 *            list.
	 * @return Set of keys contained within this ConfigurationSection.
	 */
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

	/**
	 * Checks if this {@link ConfigurationSection} contains the given path.
	 * <p>
	 * If the value for the requested path does not exist but a default value
	 * has been specified, this will return true.
	 *
	 * @param path
	 *            Path to check for existence.
	 * @return True if this section contains the requested path, either via
	 *         default or being set.
	 * @throws IllegalArgumentException
	 *             Thrown when path is null.
	 */
	public boolean contains(String path) {
		if (cpath.length() > 0)
			path = cpath + "_" + path;
		try {
			return root.tableExists(path);
		} catch (SQLException e) {
			return false;
		}
	}

	/**
	 * Gets the path of this {@link ConfigurationSection} from its root
	 * {@link FileConfiguration}
	 * <p>
	 * For any {@link FileConfiguration} themselves, this will return an empty
	 * string.
	 * <p>
	 * If the section is no longer contained within its root for any reason,
	 * such as being replaced with a different value, this may return null.
	 * <p>
	 * To retrieve the single name of this section, that is, the final part of
	 * the path returned by this method, you may use {@link #getName()}.
	 *
	 * @return Path of this section relative to its root
	 */
	public String getCurrentPath() {
		return cpath;
	}

	/**
	 * Gets the name of this individual {@link ConfigurationSection}, in the
	 * path.
	 * <p>
	 * This will always be the final part of {@link #getCurrentPath()}, unless
	 * the section is orphaned.
	 *
	 * @return Name of this section
	 */
	public String getName() {
		if (cpath.length() > 0)
			return cpath.substring(cpath.lastIndexOf("_") + 1);
		return "";
	}

	/**
	 * Gets the root {@link FileConfiguration} that contains this
	 * {@link ConfigurationSection}
	 * <p>
	 * For any {@link FileConfiguration} themselves, this will return its own
	 * object.
	 * <p>
	 * If the section is no longer contained within its root for any reason,
	 * such as being replaced with a different value, this may return null.
	 *
	 * @return Root configuration containing this section.
	 */
	public FileConfiguration getRoot() {
		return root;
	}

	/**
	 * Gets the parent {@link ConfigurationSection} that directly contains this
	 * {@link ConfigurationSection}.
	 * <p>
	 * For any {@link FileConfiguration} themselves, this will return null.
	 * <p>
	 * If the section is no longer contained within its parent for any reason,
	 * such as being replaced with a different value, this may return null.
	 *
	 * @return Parent section containing this section.
	 */
	public ConfigurationSection getParent() {
		if (cpath.length() > 0) {
			int index;
			if ((index = cpath.lastIndexOf("_")) != -1) {
				return new ConfigurationSection(root, cpath.substring(0, index));
			}
		}
		return null;
	}

	/**
	 * Sets the specified path to the given value.
	 * <p>
	 * Some implementations may have limitations on what you may store. See
	 * their individual javadocs for details. No implementations should allow
	 * you to store {@link FileConfiguration}s or {@link ConfigurationSection}s
	 *
	 * @param path
	 *            Path of the object to set.
	 * @param value
	 *            New value to set the path to.
	 * @throws SQLException
	 */
	public void set(String path, Object value) throws SQLException {
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
			root.insert(path, "value", value);
		}
	}

	/**
	 * Sets the specified path to the given value.
	 * <p>
	 * Any existing entry will be replaced, regardless of what the new value is.
	 * <p>
	 * Some implementations may have limitations on what you may store. See
	 * their individual javadocs for details. No implementations should allow
	 * you to store {@link FileConfiguration}s or {@link ConfigurationSection}s
	 *
	 * @param path
	 *            Path of the object to set.
	 * @param value
	 *            New value to set the path to.
	 * @throws SQLException
	 */
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

	/**
	 * Executes the batch in queue simultaneously.
	 * <p>
	 * This method is reserved for advanced users only who care about
	 * performance and making use of the power of SQLite.
	 * <p>
	 * This is for use with performance-enhanced methods such as the likes of (@link
	 * {@link #set(String, Object, boolean)}).
	 * 
	 * @throws SQLException
	 */
	public void executeBatch() throws SQLException {
		root.stmt.executeBatch();
	}

	// Primitives
	/**
	 * Gets the requested String by path.
	 * <p>
	 * If the String does not exist but a default value has been specified, this
	 * will return the default value. If the String does not exist and no
	 * default value was specified, this will return null.
	 *
	 * @param path
	 *            Path of the String to get.
	 * @return Requested String.
	 * @throws SQLException
	 */
	public String getString(String path) throws SQLException {
		return getString(path, null);
	}

	/**
	 * Gets the requested String by path, returning a default value if not
	 * found.
	 * <p>
	 * If the String does not exist then the specified default value will
	 * returned regardless of if a default has been identified in the root
	 * {@link FileConfiguration}.
	 *
	 * @param path
	 *            Path of the String to get.
	 * @param def
	 *            The default value to return if the path is not found or is not
	 *            a String.
	 * @return Requested String.
	 * @throws SQLException
	 */
	public String getString(String path, String def) throws SQLException {
		if (cpath.length() > 0)
			path = cpath + "_" + path;
		return root.selectStringBy(path, "value", def);
	}

	/**
	 * Gets the requested int by path.
	 * <p>
	 * If the int does not exist but a default value has been specified, this
	 * will return the default value. If the int does not exist and no default
	 * value was specified, this will return 0.
	 *
	 * @param path
	 *            Path of the int to get.
	 * @return Requested int.
	 * @throws SQLException
	 */
	public int getInt(String path) throws SQLException {
		return getInt(path, 0);
	}

	/**
	 * Gets the requested int by path, returning a default value if not found.
	 * <p>
	 * If the int does not exist then the specified default value will returned
	 * regardless of if a default has been identified in the root
	 * {@link FileConfiguration}.
	 *
	 * @param path
	 *            Path of the int to get.
	 * @param def
	 *            The default value to return if the path is not found or is not
	 *            an int.
	 * @return Requested int.
	 * @throws SQLException
	 */
	public int getInt(String path, int def) throws SQLException {
		if (cpath.length() > 0)
			path = cpath + "_" + path;
		return root.selectIntBy(path, "value", def);
	}

	/**
	 * Gets the requested boolean by path.
	 * <p>
	 * If the boolean does not exist but a default value has been specified,
	 * this will return the default value. If the boolean does not exist and no
	 * default value was specified, this will return false.
	 *
	 * @param path
	 *            Path of the boolean to get.
	 * @return Requested boolean.
	 * @throws SQLException
	 */
	public boolean getBoolean(String path) throws SQLException {
		return getBoolean(path, false);
	}

	/**
	 * Gets the requested boolean by path, returning a default value if not
	 * found.
	 * <p>
	 * If the boolean does not exist then the specified default value will
	 * returned regardless of if a default has been identified in the root
	 * {@link FileConfiguration}.
	 *
	 * @param path
	 *            Path of the boolean to get.
	 * @param def
	 *            The default value to return if the path is not found or is not
	 *            a boolean.
	 * @return Requested boolean.
	 * @throws SQLException
	 */
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

	/**
	 * Gets the requested double by path.
	 * <p>
	 * If the double does not exist but a default value has been specified, this
	 * will return the default value. If the double does not exist and no
	 * default value was specified, this will return 0.
	 *
	 * @param path
	 *            Path of the double to get.
	 * @return Requested double.
	 * @throws SQLException
	 */
	public double getDouble(String path) throws SQLException {
		return getDouble(path, 0);
	}

	/**
	 * Gets the requested double by path, returning a default value if not
	 * found.
	 * <p>
	 * If the double does not exist then the specified default value will
	 * returned regardless of if a default has been identified in the root
	 * {@link FileConfiguration}.
	 *
	 * @param path
	 *            Path of the double to get.
	 * @param def
	 *            The default value to return if the path is not found or is not
	 *            a double.
	 * @return Requested double.
	 * @throws SQLException
	 */
	public double getDouble(String path, double def) throws SQLException {
		if (cpath.length() > 0)
			path = cpath + "_" + path;
		return root.selectDoubleBy(path, "value", def);
	}

	/**
	 * Gets the requested long by path.
	 * <p>
	 * If the long does not exist but a default value has been specified, this
	 * will return the default value. If the long does not exist and no default
	 * value was specified, this will return 0.
	 *
	 * @param path
	 *            Path of the long to get.
	 * @return Requested long.
	 * @throws SQLException
	 */
	public long getLong(String path) throws SQLException {
		return getLong(path, 0);
	}

	/**
	 * Gets the requested long by path, returning a default value if not found.
	 * <p>
	 * If the long does not exist then the specified default value will returned
	 * regardless of if a default has been identified in the root
	 * {@link FileConfiguration}.
	 *
	 * @param path
	 *            Path of the long to get.
	 * @param def
	 *            The default value to return if the path is not found or is not
	 *            a long.
	 * @return Requested long.
	 * @throws SQLException
	 */
	public long getLong(String path, long def) throws SQLException {
		if (cpath.length() > 0)
			path = cpath + "_" + path;
		return root.selectLongBy(path, "value", def);
	}

	/**
	 * Gets the requested ConfigurationSection by path.
	 * <p>
	 * If the ConfigurationSection does not exist but a default value has been
	 * specified, this will return the default value. If the
	 * ConfigurationSection does not exist and no default value was specified,
	 * this will return null.
	 *
	 * @param path
	 *            Path of the ConfigurationSection to get.
	 * @return Requested ConfigurationSection.
	 */
	public ConfigurationSection getConfigurationSection(String path) {
		path = path.replace(".", "_");
		if (cpath.length() > 0)
			path = cpath + "_" + path;
		return new ConfigurationSection(root, path);
	}
}
