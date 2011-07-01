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
/**
 * Provides access to the data sources used in a Nucleus application.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

package org.nuclos.server.common;

import javax.sql.DataSource;

import org.nuclos.common.SpringApplicationContextHolder;

public class NuclosDataSources {

	/**
	 * JNDI name for Nucleus' default DataSource
	 */
	public static final String JNDINAME_DEFAULT_DATASOURCE = "java:/ds/nuclos";

	/**
	 * @return the default <code>DataSource</code> for Nucleus based applications.
	 */
	public static DataSource getDefaultDS() {
		try {						
			return (DataSource) SpringApplicationContextHolder.getBean("nuclosDataSource");		
		}
		catch (Exception ex) {
			throw new org.nuclos.common2.exception.CommonFatalException("JNDI Datasource " + JNDINAME_DEFAULT_DATASOURCE + " could not be found.", ex);
		}
	}

	private NuclosDataSources() {
	}

}	// class NuclosDataSources
