package org.nuclos.server.database;

import java.util.Map;

import javax.sql.DataSource;

import org.nuclos.server.dblayer.DbAccess;

/**
 * @deprecated Use {@link SpringDataBaseHelper}.
 */
public class DataBaseHelper {
	
	private DataBaseHelper() {
		// Never invoked.
	}
	
	public static DbAccess getDbAccessFor(DataSource dataSource, Map<String, String> config) {
		return SpringDataBaseHelper.getInstance().getDbAccessFor(dataSource, config);
	}
	
	public static DbAccess getDbAccess() {
		return SpringDataBaseHelper.getInstance().getDbAccess();
	}

}
