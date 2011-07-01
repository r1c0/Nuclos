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
package org.nuclos.server.job;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.Transformer;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.job.valueobject.JobVO;

/**
 * Quartz job to execute health checks.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */

public class HealthCheckJob extends SchedulableJob implements NuclosJob{
	private static Logger logger = Logger.getLogger(HealthCheckJob.class);

	@Override
	public String execute(JobVO jobVO, Integer iSessionId) {
		List<DataBaseObject> jobDBObjects = getJobDBObjects(jobVO.getId());
		String sExecutionResult = "";
		for (DataBaseObject dbObject : jobDBObjects) {
			if (dbObject.getType().equals("Funktion")) {
				String result = DataBaseHelper.getDbAccess().executeFunction(dbObject.getName(), String.class, iSessionId);
				sExecutionResult = sExecutionResult + dbObject.getName()+": "+ result + "\n";
			}
			else {
				DataBaseHelper.getDbAccess().executeProcedure(dbObject.getName(), iSessionId);
			}
		}
		return !sExecutionResult.isEmpty() ? sExecutionResult.substring(0, sExecutionResult.length() > 4000 ? 4001 : sExecutionResult.length()) : null;
	}

	/**
	 *
	 * @param oId
	 * @return
	 */
	private static List<DataBaseObject> getJobDBObjects(Object oId) {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_JOBDBOBJECT").alias("t");
		query.multiselect(t.column("STRNAME", String.class), t.column("STRTYPE", String.class));
		query.where(builder.equal(t.column("INTID_T_MD_JOBCONTROLLER", Integer.class), oId));
		query.orderBy(builder.asc(t.column("INTORDER", Integer.class)));

		try {
			return DataBaseHelper.getDbAccess().executeQuery(query, new Transformer<DbTuple, DataBaseObject>() {
				@Override
				public DataBaseObject transform(DbTuple t) {
					logger.info("Database object to execute: " + t.get(0, String.class));
					return new DataBaseObject(t.get(0, String.class), t.get(1, String.class));
				}
			});
		} catch (DbException ex) {
			logger.error(ex);
			return Collections.emptyList();
		}
	}

	private static class DataBaseObject {
		private String sName;
		private String sType;

		public DataBaseObject(String sName, String sType) {
			this.sName = sName;
			this.sType = sType;
		}

		public String getName() {
			return this.sName;
		}

		public String getType() {
			return this.sType;
		}
	}

}
