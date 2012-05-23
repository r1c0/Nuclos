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
 *
 */
package org.nuclos.server.report;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRResultSetDataSource;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.report.api.JRNuclosDataSource;
import org.nuclos.server.report.ejb3.DatasourceFacadeLocal;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.security.NuclosLocalServerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author thomas.schiffmann
 *
 */
@Configurable
public class JRDefaultNuclosDataSource implements JRNuclosDataSource {

	private static final Logger LOG = Logger.getLogger(JRDefaultNuclosDataSource.class);

	//

	private DatasourceFacadeLocal datasourcefacade;

	private final String name;
	private final Map<String, Object> params;
	private final JRDefaultNuclosDataSource parent;
	private final Connection conn;

	private Map<String, Integer> fields = new HashMap<String, Integer>();

	private Statement statement;
	private ResultSet data;

	private JRResultSetDataSource datasource;

	private int size = 0;

	public JRDefaultNuclosDataSource(String name, Map<String, Object> params, Connection conn) {
		this(name, params, null, conn);
	}

	public JRDefaultNuclosDataSource(String name, Map<String, Object> params, JRDefaultNuclosDataSource parent, Connection conn) {
		this.name = name;
		this.params = params;
		this.parent = parent;
		this.conn = conn;
	}

	@Autowired
	final void setDatasourceFacade(DatasourceFacadeLocal datasourceFacade) {
		this.datasourcefacade = datasourceFacade;
	}

	private void getData() throws DbException {
		Map<String, Object> currentparams = new HashMap<String, Object>();
		currentparams.putAll(this.params);

		String query;
		try {
			boolean asSuperUser = false;
			if (SecurityContextHolder.getContext().getAuthentication() == null
				|| !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
				asSuperUser = true;
				NuclosLocalServerSession.getInstance().loginAsSuperUser();
			}
			DatasourceVO datasource = datasourcefacade.get(name);

			if (datasource == null) {
				throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("jrnuclosdatasource.exception.datasourcedoesnotexist", name));
			}

			if (parent != null && parent.next) {
				List<DatasourceParameterVO> parameters = datasourcefacade.getParameters(datasource.getId());

				for (DatasourceParameterVO parameter : parameters) {
					currentparams.put(parameter.getParameter(), parent.getFieldValue(parameter.getParameter(), parameter.getDatatype()));
				}
			}

			query = datasourcefacade.createSQLForReportExecution(name, currentparams);
			if (asSuperUser) {
				NuclosLocalServerSession.getInstance().logout();
			}
		} catch (Exception e1) {
			throw new NuclosFatalException(e1);
		}

		try {
			statement = conn.createStatement();

			LOG.debug("BEGIN executing SQL: " + query);
			data = statement.executeQuery(query);
			LOG.debug("END executing SQL");
			datasource = new JRResultSetDataSource(data);

			ResultSetMetaData metadata = data.getMetaData();

			for (int i = 1; i <= metadata.getColumnCount(); i++) {
				fields.put(metadata.getColumnName(i).toUpperCase(), i);
			}
		}
		catch (SQLException ex) {
			throw new DbException(null, "getData() fails", Collections.singletonList(query), ex);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (data != null) {
				data.close();
			}
		}
		catch (SQLException ex) {
			throw new NuclosFatalException(ex);
		}
		finally {
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (SQLException ex) {
				LOG.error(ex);
			}
		}
		super.finalize();
	}

	private boolean next = false;

	@Override
	public boolean next() throws JRException {
		if (data == null) {
			getData();
		}
		boolean result = false;
		try {
			result = data.next();
			next = result;
			if (!result) {
				finalize();
			}
			else {
				size++;
			}
		}
		catch (SQLException ex) {
			throw new NuclosFatalException(ex);
		}
		catch(Throwable e) {
			LOG.error("next: " + e);
			return false;
		}
		return result;
	}

	@Override
	public Object getFieldValue(JRField arg0) throws JRException {
		return datasource.getFieldValue(arg0);
	}

	public boolean hasField(String field) {
		return fields.containsKey(field.toUpperCase());
	}

	public Object getFieldValue(String fieldname, String datatype) {
		try {
			if (hasField(fieldname)) {
				int columnIndex = fields.get(fieldname.toUpperCase());

				if ("java.lang.Boolean".equals(datatype)) {
					return data.getBoolean(columnIndex) ? Boolean.TRUE : Boolean.FALSE;
				}
				else if ("java.lang.Integer".equals(datatype)) {
					return new Integer(data.getInt(columnIndex));
				}
				else if ("java.lang.Double".equals(datatype)) {
					return new Double(data.getDouble(columnIndex));
				}
				else if ("java.util.Date".equals(datatype)) {
					return data.getDate(columnIndex);
				}
				else if ("java.lang.String".equals(datatype)) {
					return data.getString(columnIndex);
				}
			}
			else if (params.containsKey(fieldname)) {
				return params.get(fieldname);
			}
			return null;
		}
		catch (SQLException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	@Override
	public JRNuclosDataSource getDataSource(String name) {
		return new JRDefaultNuclosDataSource(name, params, this, conn);
	}

	@Override
	public JRNuclosDataSource getDataSource(String name, Object[][] paramArray) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.putAll(this.params);
		for (Object[] param : paramArray) {
			if (param.length < 2) {
				throw new NuclosFatalException("jrnuclosdatasource.exception.parameterarraylength");
			}
			if (!(param[0] instanceof String)) {
				throw new NuclosFatalException("jrnuclosdatasource.exception.parameterkeytype");
			}

			params.put((String)param[0], param[1]);
		}

		return new JRDefaultNuclosDataSource(name, params, this, conn);
	}

	/**
	 * Get the total size of the main report set (after the datasource has been processed completely)
	 * @return Number of rows in result set.
	 */
	public int getSize() {
		return size;
	}
}
