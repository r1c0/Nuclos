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
package org.nuclos.server.mbean;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.common.DatasourceCache;
import org.nuclos.server.report.SchemaCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @deprecated Is this still in use?
 */
@Component
public class NuclosCacheService /*extends ServiceMBeanSupport implements NuclosCacheServiceMBean */{

	protected static final Logger log = Logger.getLogger(NuclosCacheService.class);

	private String datasourceCacheJndiName;
	private DatasourceCache datasourceCache;
	private String schemaCacheJndiName;
	private SchemaCache schemaCache;
	
	public NuclosCacheService () {
		try {
			this.init();
		}
		catch(Exception e) {
			throw new NuclosFatalException(e);
		}
	}
	
	@Autowired
	void setDatasourceCache(DatasourceCache datasourceCache) {
		this.datasourceCache = datasourceCache;
	}

	
   private void init() throws Exception {
		log.info("init() in ...");

		// Das Login ist fuer den Zugriff auf die EJBs noetig. Wir leihen
		// uns dafuer den Quartz User (s. NucleusQuartJob).
		//
		// TODO: Eins systematischere Loesung finden (AOP?)
		// TODO spring security
//		Properties serverProperties = ServerProperties.loadProperties(ServerProperties.NUCLOS_SERVER_PROPERTIES);
//		String user = serverProperties.getProperty("QUARTZ_USERNAME");
//		String pwd = serverProperties.getProperty("QUARTZ_PASSWORD");
//
//		UsernamePasswordHandler handler = new UsernamePasswordHandler(user, pwd);
//      LoginContext lc = new LoginContext("client-login", handler);
		try {
//			lc.login();
			// this.datasourceCache = DatasourceCache.getInstance();
			this.schemaCache = SchemaCache.getInstance();
		} finally {
//			lc.logout();
		}

		log.info("init() out.");
	}


}
