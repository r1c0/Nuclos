//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.

package org.nuclos.tools.dbsetup;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.nuclos.server.dblayer.DbAccess;


public class DbSetupTool {
	
	public static void main(String[] args) throws Exception {
		File configFile = (args.length > 0) ? new File(args[0]) : new File("nuclos.xml");
		
		if (configFile.isDirectory()) {
			final String[] files = configFile.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith("nuclos") && name.endsWith(".xml");
				}
			});
			String result = (String) JOptionPane.showInputDialog(null, 
				"Choose config file", "Choose config file",
				JOptionPane.QUESTION_MESSAGE,
				null,
				files, "nuclos.xml");
			
			if (result == null)
				System.exit(1);
			
			configFile = new File(configFile, result);
		}
		
		
		final DbAccess dbAccess = getDbAccess(configFile);
		
		final String title = "" + configFile;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame f = new StatementsFrame(title, dbAccess);
				f.pack();
				f.setVisible(true);
			}
		});
	}

	public static DbAccess getDbAccess(File configFile) throws Exception {
		/*Config config = Installer.loadConfig(new XMLHelper().readDocument(configFile));
		
		Map<String, String> dbConfig = new HashMap<String, String>();
		for (Map.Entry<String, String> e : config.getProperties().entrySet()) {
			if (e.getKey().startsWith("database.")) {
				dbConfig.put(e.getKey().substring(9), e.getValue());
			}
		}

		if (dbConfig.containsKey("driver")) {
			Class.forName(dbConfig.get("driver"));
		}
		
		DbType type = DbType.getFromName(dbConfig.get("adapter"));
		if (type == DbType.POSTGRESQL) {
			dbConfig.put("new.connection.sql", String.format("set search_path to %s,public", dbConfig.get("schema")));
		}
		dbConfig.put("user", dbConfig.get("username"));
		
		SimpleDataSource dataSource = new SimpleDataSource(dbConfig.get("connection.url"), dbConfig);
		DbAccess dbAccess = type.createDbAccess(dataSource, dbConfig);
		return dbAccess;*/
		return null;
	}
}
