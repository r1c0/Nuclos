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
package org.nuclos.server.updatejobs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.nuclos.common.UsageCriteria;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.UpdateJobs;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.expression.DbCurrentDateTime;
import org.nuclos.server.dblayer.expression.DbId;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.statemodel.ejb3.StateFacadeLocal;
import org.nuclos.server.statemodel.valueobject.StateModelUsagesCache;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * Assign rights to roles, that have attribute rights, for all subforms
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */

public class AssignSubformRightToRole implements UpdateJobs{

	public static final String sRelease = "Nucleus Release 2.1.1";
	private boolean isSuccessfulExecuted = false;

	@Override
	public boolean execute() {
		logger.debug("START executing AssignSubformRightToRole");

		try {
			StateFacadeLocal stateFacade = ServiceLocator.getInstance().getFacade(StateFacadeLocal.class);

			for (Iterator<StateModelVO> iter = stateFacade.getStateModels().iterator(); iter.hasNext();) {
				StateModelVO statemodelvo = iter.next();
				for (StateVO statevo : stateFacade.getStatesByModel(statemodelvo.getId())) {
					for (Integer iRoleId : getRoleIdsFrom(statevo)) {
						for (String sSubform : getSubformsFromUsageCriteriasOf(statemodelvo)) {
							final Integer iSubformId = MasterDataMetaCache.getInstance().getMetaData(sSubform).getId();

							DataBaseHelper.execute(DbStatementUtils.insertInto("T_MD_ROLE_SUBFORM",
								"INTID", new DbId(),
								"DATCREATED", DbCurrentDateTime.CURRENT_DATETIME,
								"STRCREATED", "INITIAL",
								"DATCHANGED", DbCurrentDateTime.CURRENT_DATETIME,
								"STRCHANGED", "INITIAL",
								"INTVERSION", 1,
								"INTID_T_AD_MASTERDATA", iSubformId,
								"BLNREADWRITE", true,
								"INTID_T_MD_STATE", statevo.getId(),
								"INTID_T_MD_ROLE", iRoleId));
						}
					}
				}
			}

			isSuccessfulExecuted = true;
		}
		catch(DbException ex) {
			logger.error(ex, ex.getCause());
		}
		catch(CommonFatalException ex) {
			logger.error(ex, ex.getCause());
		}

		return isSuccessfulExecuted;
	}

	private Collection<String> getSubformsFrom(UsageCriteria usagecriteria) {
		Collection<String> result = new HashSet<String>();
		try {
			result.addAll(GenericObjectMetaDataCache.getInstance().getMetaDataCVO().getBestMatchingLayoutSubformEntityNames(usagecriteria));
		}
		catch (CommonFinderException ex) {
			logger.error(ex, ex.getCause());
		}
		return result;
	}

	private Collection<String> getSubformsFromUsageCriteriasOf(StateModelVO statemodelvo) {
		Collection<String> result = new HashSet<String>();

		for (UsageCriteria uc : StateModelUsagesCache.getInstance().getStateUsages().getUsageCriteriaByStateModelId(statemodelvo.getId())) {
			for (String sSubformEntityName : getSubformsFrom(uc)) {
				if (!result.contains(sSubformEntityName)) {
					result.add(sSubformEntityName);
				}
			}
		}

		return result;
	}

	private Collection<Integer> getRoleIdsFrom(StateVO statevo) throws DbException, CommonFatalException {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_MD_ROLE_ATTRIBUTEGROUP").alias(ProcessorFactorySingleton.BASE_ALIAS);
		query.select(t.column("INTID_T_MD_ROLE", Integer.class));
		query.where(builder.equal(t.column("INTID_T_MD_STATE", Integer.class), statevo.getId()));
		return DataBaseHelper.getDbAccess().executeQuery(query);
	}
}
