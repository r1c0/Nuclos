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
package org.nuclos.server.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.dbcp.BasicDataSource;
import org.nuclos.common.NuclosFatalException;

public class NuclosBasicDatasource extends BasicDataSource { 

	@PostConstruct
	public void setInitSqlStatements() {
		try {
			Properties prop = new Properties();		
			prop.load(this.getClass().getClassLoader().getResourceAsStream("nuclos-server.properties"));
			Collection<String> colInitSqls = new ArrayList<String>();
			if("postgresql".equals(prop.get("database.adapter"))) {
				String sDBSchema = prop.getProperty("database.schema");
				colInitSqls.add("set search_path to "+ sDBSchema + ",public");
			}
			else if("oracle".equals(prop.get("database.adapter"))) {
				colInitSqls.add("alter session set nls_comp='BINARY' nls_sort='GERMAN'");
			}
			
			this.setConnectionInitSqls(colInitSqls);
		}
		catch(IOException e) {
			throw new NuclosFatalException(e);
		}
		
		
	}
	
}
