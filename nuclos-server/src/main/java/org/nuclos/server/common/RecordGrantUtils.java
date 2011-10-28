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
package org.nuclos.server.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.visit.PutSearchConditionToPrefsVisitor;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.entityobject.CollectableEOEntityProvider;
import org.nuclos.common.preferences.ReadOnlyPreferences;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.entityobject.CollectableEOEntityServerProvider;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.report.valueobject.RecordGrantVO;
import org.nuclos.server.report.valueobject.ResultVO;

public class RecordGrantUtils {
	
	private static final Logger	LOG	= Logger.getLogger(RecordGrantUtils.class);

	public static void checkWriteInternal(String entity, Long id) throws CommonPermissionException {
		if (!getRecordGrantRightInternal(entity, id).canWrite()) {
			throw new CommonPermissionException("recordgrant.canwrite.not.allowed");
		}
	}

	public static void checkWrite(String entity, Long id) throws CommonPermissionException {
		if (!getRecordGrantRight(entity, id).canWrite()) {
			throw new CommonPermissionException("recordgrant.canwrite.not.allowed");
		}
	}

	public static void checkDeleteInternal(String entity, Long id) throws CommonPermissionException {
		if (!getRecordGrantRightInternal(entity, id).canDelete()) {
			throw new CommonPermissionException("recordgrant.candelete.not.allowed");
		}
	}

	public static void checkDelete(String entity, Long id) throws CommonPermissionException {
		if (!getRecordGrantRight(entity, id).canDelete()) {
			throw new CommonPermissionException("recordgrant.candelete.not.allowed");
		}
	}

	public static void checkInternal(String entity, Long id) throws CommonPermissionException {
		if (!isGrantedInternal(entity, id)) {
			throw new CommonPermissionException("recordgrant.read.not.allowed");
		}
	}

	public static void check(String entity, Long id) throws CommonPermissionException {
		if (!isGranted(entity, id)) {
			throw new CommonPermissionException("recordgrant.read.not.allowed");
		}
	}

	public static boolean isGrantedInternal(String entity, Long id) {
		if(!SessionUtils.isCalledRemotely())
			return true;

		return isGranted(entity, id);
	}

	public static boolean isGranted(String entity, Long id) {
		if (SecurityCache.getInstance().isSuperUser(SessionUtils.getCurrentUserName()))
			return true;

		final Set<RecordGrantVO> recordGrant = getByEntity(entity);
		if(recordGrant.isEmpty())
			return true;

		RecordGrantVO rgVO = recordGrant.iterator().next();

		try {
			if(rgVO.getValid()) {
				ResultVO queryResult = DataBaseHelper.getDbAccess().executePlainQueryAsResultVO(DatasourceServerUtils.createSQL(rgVO.getSource(), getParameter()), 1);

				if (queryResult.getRowCount() == 0)
					return false;
				else
					return true;
			} else {
				return true;
			}
		}
		catch(NuclosDatasourceException e) {
			throw new NuclosFatalException(
			    "datasource.error.recordgrant.invalid", e);
		}
	}

	public static RecordGrantRight getRecordGrantRightInternal(String entity, Long id) {
		if(!SessionUtils.isCalledRemotely())
			return RecordGrantRight.ALL_RIGHTS;

		return getRecordGrantRight(entity, id);
	}

	public static RecordGrantRight getRecordGrantRight(String entity, Long id) {
		if (SecurityCache.getInstance().isSuperUser(SessionUtils.getCurrentUserName()))
			return RecordGrantRight.ALL_RIGHTS;
		
		final Set<RecordGrantVO> recordGrant = getByEntity(entity);
		if(recordGrant.isEmpty())
			return RecordGrantRight.ALL_RIGHTS;

		RecordGrantVO rgVO = recordGrant.iterator().next();

		try {
			if(rgVO.getValid()) {
				ResultVO queryResult = DataBaseHelper.getDbAccess().executePlainQueryAsResultVO(DatasourceServerUtils.createSQL(rgVO.getSource(), getParameter()), 1);
				boolean canWrite = true;
				boolean canDelete = true;

				if (queryResult.getRowCount() == 0)
					return RecordGrantRight.NO_RIGHTS;

				for (int col = 0 ; col < queryResult.getColumns().size(); col++) {

					if (queryResult.getColumns().get(col).getColumnLabel().equalsIgnoreCase("CANWRITE"))
						canWrite = isTrue(queryResult.getRows().get(0)[col]);

					if (queryResult.getColumns().get(col).getColumnLabel().equalsIgnoreCase("CANDELETE"))
						canDelete = isTrue(queryResult.getRows().get(0)[col]);
				}

				return new RecordGrantRight(canWrite, canDelete);

			} else {
				return RecordGrantRight.DEFAULT;
			}
		}
		catch(NuclosDatasourceException e) {
			throw new NuclosFatalException(
			    "datasource.error.recordgrant.invalid", e);
		}
	}

	private static boolean isTrue(Object o) {
		if (o instanceof Boolean) {
			return (Boolean)o;
		} else if (o instanceof String) {
			if ("false".equalsIgnoreCase((String)o) || "0".equals(o)) {
				return false;
			}
		} else if (o instanceof Number){
			if (((Number)o).longValue() == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * append record grant(s) to cond for given entity.
	 *
	 * @param expr
	 * @param entity
	 * @return new AND condition if any record grant(s) found, otherwise cond
	 *         is returned.
	 */
	public static CollectableSearchCondition append(
		CollectableSearchCondition cond, String entity) {
		if(!SessionUtils.isCalledRemotely())
			return cond;
		
		if (SecurityCache.getInstance().isSuperUser(SessionUtils.getCurrentUserName()))
			return cond;

		// Look-up and add compulsory search filters
		CollectableSearchCondition compulsoryFilterCondition = getCompulsorySearchFilter(entity);
		if (compulsoryFilterCondition != null) {
			if (cond == null) {
				cond = compulsoryFilterCondition;
			} else {
				cond = new CompositeCollectableSearchCondition(LogicalOperator.AND,
					Arrays.asList(cond, compulsoryFilterCondition));
			}
		}
		
		final Set<RecordGrantVO> recordGrant = getByEntity(entity);
		if(recordGrant.isEmpty())
			return cond;

		RecordGrantVO rgVO = recordGrant.iterator().next();

		CompositeCollectableSearchCondition result = new CompositeCollectableSearchCondition(
		    LogicalOperator.AND);
		if(cond != null)
			result.addOperand(cond);

		try {
			if(rgVO.getValid())
				result.addOperand(DatasourceServerUtils.getConditionWithIdForInClause(rgVO.getSource(), getParameter()));
		}
		catch(NuclosDatasourceException e) {
			throw new NuclosFatalException(
			    "datasource.error.recordgrant.invalid", e);
		}

		return result;
	}

	/**
	 * append record grant(s) to expr for given entity.
	 *
	 * @param expr
	 * @param entity
	 * @return new AND 'condition' if any record grant(s) found, otherwise expr
	 *         is returned.
	 */
	public static CollectableSearchExpression append(
	    CollectableSearchExpression expr, String entity) {
		if(!SessionUtils.isCalledRemotely())
			return expr;


		CollectableSearchExpression result;

		if (expr instanceof CollectableGenericObjectSearchExpression) {
			CollectableGenericObjectSearchExpression goexpr = (CollectableGenericObjectSearchExpression)expr;
			result = new CollectableGenericObjectSearchExpression(append(expr.getSearchCondition(), entity), expr.getSortingOrder(), goexpr.getSearchDeleted());
		}
		else {
			result = new CollectableSearchExpression(append(expr.getSearchCondition(), entity), expr.getSortingOrder());
		}

		result.setIncludingSystemData(expr.isIncludingSystemData());
		result.setValueListProviderDatasource(expr.getValueListProviderDatasource());
		result.setValueListProviderDatasourceParameter(expr.getValueListProviderDatasourceParameter());

		return result;
	}

	private static Map<String, Object> getParameter() {
		Map<String, Object> mpParams = new HashMap<String, Object>();
		mpParams.put("username", SessionUtils.getCurrentUserName());
		return mpParams;
	}

	public static Set<RecordGrantVO> getByEntity(final String entity) {
		Set<RecordGrantVO> result = CollectionUtils.selectIntoSet(
		    DatasourceCache.getInstance().getAllRecordGrant(),
		    new Predicate<RecordGrantVO>() {

			    @Override
			    public boolean evaluate(RecordGrantVO t) {
				    return t.getEntity() != null
				        && t.getEntity().equals(entity);
			    }

		    });

		if (result.size() > 1) {
			throw new NuclosFatalException(
			    "datasource.error.recordgrant.invalid");
		}

		return result;
	}

	//
	// Compulsory search filters
	//
	
	private static CollectableSearchCondition getCompulsorySearchFilter(String entity) {
		Set<Integer> filterIds = SecurityCache.getInstance().getCompulsorySearchFilterIds(SessionUtils.getCurrentUserName(), entity);
		if (filterIds.isEmpty())
			return null;
		
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom table = query.from("T_UD_SEARCHFILTER").alias(SystemFields.BASE_ALIAS);
		DbColumnExpression<Integer> intId = table.baseColumn("INTID", Integer.class);
		DbColumnExpression<String> strName = table.baseColumn("STRNAME", String.class);
		DbColumnExpression<String> strEntity = table.baseColumn("STRENTITY", String.class);
		DbColumnExpression<String> xmlFilter = table.baseColumn("CLBSEARCHFILTER", String.class);
		query.multiselect(intId, strName, strEntity, xmlFilter);
		query.where(intId.in(filterIds));
		query.distinct(true);
		
		List<CollectableSearchCondition> cscs = new ArrayList<CollectableSearchCondition>();
		for (DbTuple t : DataBaseHelper.getDbAccess().executeQuery(query)) {
			String filterName = t.get(1, String.class);
			String entityName = t.get(2, String.class);
			String xml = t.get(3, String.class);
			try {
				CollectableSearchCondition csc = parseSearchFilter(filterName, entityName, xml);
				if (csc != null) {
					csc.setConditionName(filterName);
					cscs.add(csc);
				}
			} catch (Exception e) {
				LOG.info("getCompulsorySearchFilter failed: " + e, e);
				throw new NuclosFatalException("Invalid compulsory filter " + filterName, e);
			}
		}
		
		if (cscs.isEmpty())
			return null;
		CollectableSearchCondition result = new CompositeCollectableSearchCondition(LogicalOperator.AND, cscs);
		result.setConditionName("Compulsory Filters");
		return result;
	}
	
	private static CollectableSearchCondition parseSearchFilter(String filterName, String entityName, String xml) throws IOException, PreferencesException, BackingStoreException {
		Preferences topPrefs = new ReadOnlyPreferences(xml, "UTF-8");

		// See SearchFilterDelegate#makeSearchFilter for details about the path
		String prefsPath = "org/nuclos/client/searchFilters/" + filterName;
		if (!topPrefs.nodeExists(prefsPath))
			return null;

		Preferences prefs = topPrefs.node(prefsPath);
		CollectableEOEntityProvider provider = CollectableEOEntityServerProvider.getInstance();
		CollectableSearchCondition csc = PutSearchConditionToPrefsVisitor.getSearchCondition(prefs.node("searchCondition"), entityName, provider);
		return csc;
	}
}
