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
package org.nuclos.server.migration.vm2m5;

import static org.nuclos.common2.StringUtils.looksEmpty;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.NuclosEOField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.layoutml.LayoutMLParser;
import org.nuclos.common2.layoutml.exception.LayoutMLException;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbInvalidResultSizeException;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.EntityObjectMetaDbHelper;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.impl.SchemaUtils;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbSelection;
import org.nuclos.server.dblayer.statements.DbDeleteStatement;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.statements.DbUpdateStatement;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.genericobject.valueobject.CanonicalAttributeFormat;
import org.nuclos.server.migration.AbstractMigration;
import org.xml.sax.InputSource;

public class MigrationVm2m5 extends AbstractMigration{

	private boolean armed = true;

	public void startMigration() {
		try {
			info("----------------------------------------");
			info("------- start migration v2.5.00 --------");
			info("----------------------------------------");
			migrateMasterdata();
			migrateAttributeGroups();
			migrateModules();
			migrateModuleSubnodes();
			MetaDataServerProvider.getInstance().revalidate();

			createEntityObjectTables();
			migrateModuleObjects();
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

	private final Class<?> DT_STRING = java.lang.String.class;
	private final Class<?> DT_INTEGER = java.lang.Integer.class;
	private final Class<?> DT_DOUBLE = java.lang.Double.class;
	private final Class<?> DT_DATE = java.util.Date.class;
	private final Class<?> DT_BOOLEAN = java.lang.Boolean.class;


	private void migrateMasterdata() {
		info("*** M I G R A T E   M A S T E R D A T A ***");

		DbQuery<DbTuple> query = DataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from("T_AD_MASTERDATA");
		from.alias("md");
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();
		columns.add(getColumn(from, "INTID", DT_INTEGER));
		columns.add(getColumn(from, "STRENTITY", DT_STRING));
		columns.add(getColumn(from, "STRDBENTITY", DT_STRING));
		columns.add(getColumn(from, "STRMENUPATH", DT_STRING));
		columns.add(getColumn(from, "STRLABEL", DT_STRING));
		columns.add(getColumn(from, "DATCREATED", DT_DATE));
		columns.add(getColumn(from, "STRCREATED", DT_STRING));
		columns.add(getColumn(from, "DATCHANGED", DT_DATE));
		columns.add(getColumn(from, "STRCHANGED", DT_STRING));
		columns.add(getColumn(from, "INTVERSION", DT_INTEGER));
		columns.add(getColumn(from, "BLNSEARCHABLE", DT_BOOLEAN));
		columns.add(getColumn(from, "BLNEDITABLE", DT_BOOLEAN));
		columns.add(getColumn(from, "STRLABELPLURAL", DT_STRING));
		columns.add(getColumn(from, "STRFIELDS_FOR_EQUALITY", DT_STRING));
		columns.add(getColumn(from, "BLNCACHEABLE", DT_BOOLEAN));
		columns.add(getColumn(from, "STRTREEVIEW", DT_STRING));
		columns.add(getColumn(from, "STRTREEVIEWDESCRIPTION", DT_STRING));
		columns.add(getColumn(from, "BLNSYSTEMENTITY", DT_BOOLEAN));
		columns.add(getColumn(from, "INTID_T_MD_RESOURCE", DT_INTEGER));
		columns.add(getColumn(from, "BLNIMPORTEXPORT", DT_BOOLEAN));
		columns.add(getColumn(from, "INTACCELERATORMODIFIER", DT_INTEGER));
		columns.add(getColumn(from, "STRACCELERATOR", DT_STRING));
		columns.add(getColumn(from, "STR_LOCALERESOURCE_L", DT_STRING));
		columns.add(getColumn(from, "STR_LOCALERESOURCE_M", DT_STRING));
		columns.add(getColumn(from, "STR_LOCALERESOURCE_LP", DT_STRING));
		columns.add(getColumn(from, "STR_LOCALERESOURCE_TW", DT_STRING));
		columns.add(getColumn(from, "STR_LOCALERESOURCE_TT", DT_STRING));
		query.multiselect(columns);

		List<DbTuple> result = DataBaseHelper.getDbAccess().executeQuery(query);


		for (DbTuple rs : result) {
			Integer id = rs.get("INTID", java.lang.Integer.class);
			Boolean systementity = rs.get("BLNSYSTEMENTITY", java.lang.Boolean.class);
			String sEntity = rs.get("STRENTITY", java.lang.String.class);
			String sDbEntity = rs.get("STRDBENTITY", java.lang.String.class);

			String sLabel = rs.get("STRLABEL", java.lang.String.class);
			String sMenu = rs.get("STRMENUPATH", java.lang.String.class);
			String sTree = rs.get("STRTREEVIEW", java.lang.String.class);
			String sTreeDesc = rs.get("STRTREEVIEWDESCRIPTION", java.lang.String.class);

			String sLabelRes = rs.get("STR_LOCALERESOURCE_L", java.lang.String.class);
			String sMenuRes = rs.get("STR_LOCALERESOURCE_M", java.lang.String.class);
			String sTreeRes = rs.get("STR_LOCALERESOURCE_TW", java.lang.String.class);
			String sTreeDescRes = rs.get("STR_LOCALERESOURCE_TT", java.lang.String.class);


			Integer newId = DataBaseHelper.getNextIdAsInteger(DataBaseHelper.DEFAULT_SEQUENCE);

			info("-----------------------------------");
			info("Processing masterdata entity " + sEntity);

			if (isTrue(systementity) || id.intValue() <= 10000) {
				info("Skipping... is system entity!");
				continue;
			}
			if ("T_UD_GENERICOBJECT".equals(sDbEntity.toUpperCase())) {
				info("Skipping... is genericobject entity!");
				continue;
			}

			Map<String, Object> values = new HashMap<String, Object>();
			values.put("INTID", newId);
			put(rs, "STRENTITY", values, "STRENTITY", DT_STRING);
			put(rs, "STRDBENTITY", values, "STRDBENTITY", DT_STRING);
			//put(rs, "", values, "STRSYSTEMIDPREFIX", DT_STRING);
			//put(rs, "", values, "STRMENUSHORTCUT", DT_STRING);
			put(rs, "BLNEDITABLE", values, "BLNEDITABLE", DT_BOOLEAN);
			values.put("BLNUSESSTATEMODEL", Boolean.FALSE);
			values.put("BLNLOGBOOKTRACKING", Boolean.FALSE);
			put(rs, "BLNCACHEABLE", values, "BLNCACHEABLE", DT_BOOLEAN);
			put(rs, "BLNSEARCHABLE", values, "BLNSEARCHABLE", DT_BOOLEAN);
			values.put("BLNTREERELATION", Boolean.FALSE);
			values.put("BLNTREEGROUP", Boolean.FALSE);
			put(rs, "BLNIMPORTEXPORT", values, "BLNIMPORTEXPORT", DT_BOOLEAN);
			values.put("BLNFIELDVALUEENTITY", Boolean.FALSE);
			put(rs, "STRACCELERATOR", values, "STRACCELERATOR", DT_STRING);
			put(rs, "INTACCELERATORMODIFIER", values, "INTACCELERATORMODIFIER", DT_INTEGER);
			put(rs, "STRFIELDS_FOR_EQUALITY", values, "STRFIELDS_FOR_EQUALITY", DT_STRING);
			put(rs, "INTID_T_MD_RESOURCE", values, "INTID_T_MD_RESOURCE", DT_INTEGER);
			put(looksEmpty(sLabelRes)? createResource(sLabel) : sLabelRes, values, "STR_LOCALERESOURCE_L");
			put(looksEmpty(sMenuRes)? createResource(sMenu) : sMenuRes, values, "STR_LOCALERESOURCE_M");
			//put(rs, "", values, "STR_LOCALERESOURCE_D", DT_STRING);
			put(looksEmpty(sTreeRes)? createResource(sTree) : sTreeRes, values, "STR_LOCALERESOURCE_TW");
			put(looksEmpty(sTreeDescRes)? createResource(sTreeDesc) : sTreeDescRes, values, "STR_LOCALERESOURCE_TT");
			put(rs, "DATCREATED", values, "DATCREATED", DT_DATE);
			put(rs, "STRCREATED", values, "STRCREATED", DT_STRING);
			put(rs, "DATCHANGED", values, "DATCHANGED", DT_DATE);
			put(rs, "STRCHANGED", values, "STRCHANGED", DT_STRING);
			put(rs, "INTVERSION", values, "INTVERSION", DT_INTEGER);

			/** update Logbook */
			Map<String, Object> columnValueMap = new HashMap<String, Object>();
			Map<String, Object> conditionMap = new HashMap<String, Object>();
			columnValueMap.put("INTID_T_AD_MASTERDATA", newId);
			conditionMap.put("INTID_T_AD_MASTERDATA", id);
			info("Update Logbook entry (\"INTID_T_AD_MASTERDATA " + id + " --> " + newId + "\")");
			if(armed) info(DataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_UD_LOGBOOK", columnValueMap, conditionMap)) + " records updated.");

			if(armed) DataBaseHelper.getDbAccess().execute(new DbInsertStatement("T_MD_ENTITY", values));

			/** update Generation */
			columnValueMap = new HashMap<String, Object>();
			conditionMap = new HashMap<String, Object>();
			columnValueMap.put("INTID_ENTITY_SOURCE", newId);
			conditionMap.put("INTID_ENTITY_SOURCE", id);
			info("Update Generation subentity entry (\"INTID_ENTITY_SOURCE " + id + " --> " + newId + "\")");
			if(armed) info(DataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_MD_GENERATION_SUBENTITY", columnValueMap, conditionMap)) + " records updated.");
			columnValueMap = new HashMap<String, Object>();
			conditionMap = new HashMap<String, Object>();
			columnValueMap.put("INTID_ENTITY_TARGET", newId);
			conditionMap.put("INTID_ENTITY_TARGET", id);
			info("Update Generation subentity entry (\"INTID_ENTITY_TARGET " + id + " --> " + newId + "\")");
			if(armed) info(DataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_MD_GENERATION_SUBENTITY", columnValueMap, conditionMap)) + " records updated.");

			/** update searchfilter */
			/*columnValueMap = new HashMap<String, Object>();
			conditionMap = new HashMap<String, Object>();
			columnValueMap.put("INTID_T_AD_MASTERDATA", newId);
			conditionMap.put("INTID_T_AD_MASTERDATA", id);
			info("Update Searchfilter entry (\"INTID_T_AD_MASTERDATA " + id + " --> " + newId + "\")");
			if(armed) info(DataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_UD_SEARCHFILTER", columnValueMap, conditionMap)) + " records updated.");*/

			info("Processing masterdata fields now...");
			migrateMasterdataFieldsForEntity(id, newId);

		}

		/** run after main migration of masterdata */
		result = DataBaseHelper.getDbAccess().executeQuery(query);
		for (DbTuple rs : result) {
			Integer id = rs.get("INTID", java.lang.Integer.class);
			String sEntity = rs.get("STRENTITY", java.lang.String.class);
			String sDbEntity = rs.get("STRDBENTITY", java.lang.String.class);

			if ("T_UD_GENERICOBJECT".equals(sDbEntity.toUpperCase())) {
				/** update searchfilters */
				DbQuery<Integer> queryModule = DataBaseHelper.getDbAccess().getQueryBuilder().createQuery(Integer.class);
				DbFrom module = queryModule.from("T_MD_MODULE").alias("module");
				queryModule.select(module.baseColumn("INTID", Integer.class));
				queryModule.where(
					DataBaseHelper.getDbAccess().getQueryBuilder().equal(module.baseColumn("STRENTITY", Integer.class), sEntity));
				Integer moduleid = DataBaseHelper.getDbAccess().executeQuerySingleResult(queryModule);
				/*Map<String, Object> columnValueMap = new HashMap<String, Object>();
				Map<String, Object> conditionMap = new HashMap<String, Object>();
				columnValueMap.put("INTID_T_AD_MASTERDATA", moduleid);
				conditionMap.put("INTID_T_AD_MASTERDATA", id);

				info("Update searchfilters with masterdata id " + id + " (\"INTID_T_AD_MASTERDATA " + id + " --> " + moduleid + "\")");
				if(armed) info(DataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_UD_SEARCHFILTER", columnValueMap, conditionMap)) + " records updated.");*/
			}
		}
	}

	private void migrateMasterdataFieldsForEntity(Integer iMasterdataId, Integer iNewMasterdataId) {
		DbQuery<DbTuple> query = DataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from("T_AD_MASTERDATA_FIELD");
		from.alias("mdf");
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();
		columns.add(getColumn(from, "INTID", DT_INTEGER));
		columns.add(getColumn(from, "INTID_T_AD_MASTERDATA", DT_INTEGER));
		columns.add(getColumn(from, "STRFIELD", DT_STRING));
		columns.add(getColumn(from, "STRDBFIELD", DT_STRING));
		columns.add(getColumn(from, "STRFIELDLABEL", DT_STRING));
		columns.add(getColumn(from, "STRDESCRIPTION", DT_STRING));
		columns.add(getColumn(from, "DATCREATED", DT_DATE));
		columns.add(getColumn(from, "STRCREATED", DT_STRING));
		columns.add(getColumn(from, "DATCHANGED", DT_DATE));
		columns.add(getColumn(from, "STRCHANGED", DT_STRING));
		columns.add(getColumn(from, "INTVERSION", DT_INTEGER));
		columns.add(getColumn(from, "STRFOREIGNENTITY", DT_STRING));
		columns.add(getColumn(from, "STRDATATYPE", DT_STRING));
		columns.add(getColumn(from, "INTDATASCALE", DT_INTEGER));
		columns.add(getColumn(from, "INTDATAPRECISION", DT_INTEGER));
		columns.add(getColumn(from, "STRFORMATINPUT", DT_STRING));
		columns.add(getColumn(from, "STRFORMATOUTPUT", DT_STRING));
		columns.add(getColumn(from, "BLNNULLABLE", DT_BOOLEAN));
		columns.add(getColumn(from, "BLNSEARCHABLE", DT_BOOLEAN));
		columns.add(getColumn(from, "BLNLOGBOOK", DT_BOOLEAN));
		columns.add(getColumn(from, "STRFOREIGNENTITYFIELD", DT_STRING));
		columns.add(getColumn(from, "BLNUNIQUE", DT_BOOLEAN));
		columns.add(getColumn(from, "STRDEFAULT", DT_STRING));
		columns.add(getColumn(from, "STR_LOCALERESOURCE_L", DT_STRING));
		columns.add(getColumn(from, "STR_LOCALERESOURCE_D", DT_STRING));
		//columns.add(getColumn(from, "BLNINVARIANT", DT_BOOLEAN));
		query.multiselect(columns);

		query.where(DataBaseHelper.getDbAccess().getQueryBuilder().equal(getColumn(from, "INTID_T_AD_MASTERDATA", DT_INTEGER),
			DataBaseHelper.getDbAccess().getQueryBuilder().literal(iMasterdataId)));

		List<DbTuple> result = DataBaseHelper.getDbAccess().executeQuery(query);


		for (DbTuple rs : result) {
			Integer id = rs.get("INTID", java.lang.Integer.class);
			String sField = rs.get("STRFIELD", java.lang.String.class);
			String sDbField = rs.get("STRDBFIELD", java.lang.String.class).toUpperCase();
			String sLabel = rs.get("STRFIELDLABEL", java.lang.String.class);
			String sLabelResource = rs.get("STR_LOCALERESOURCE_L", java.lang.String.class);
			String sDescription = rs.get("STRDESCRIPTION", java.lang.String.class);
			String sDescriptionResource = rs.get("STR_LOCALERESOURCE_D", java.lang.String.class);

			if ("DATCREATED".equals(sDbField) ||
				 "STRCREATED".equals(sDbField) ||
				 "DATCHANGED".equals(sDbField) ||
				 "STRCHANGED".equals(sDbField)) {
				continue;
			}

			info("Processing masterdata field " + sField);

			Integer iNewMDFieldId = DataBaseHelper.getNextIdAsInteger(DataBaseHelper.DEFAULT_SEQUENCE);

			Map<String, Object> values = new HashMap<String, Object>();
			values.put("INTID", iNewMDFieldId);
			values.put("INTID_T_MD_ENTITY", iNewMasterdataId);
			//put(rs, "", values, "INTID_T_MD_ENTITY_FIELD_GROUP", DT_INTEGER);
			put(rs, "STRFIELD", values, "STRFIELD", DT_STRING);
			values.put("STRDBFIELD", sDbField);
			put(replaceDeNovabitPackage(rs.get("STRDATATYPE", java.lang.String.class)), values, "STRDATATYPE");
			put(rs, "STRFOREIGNENTITY", values, "STRFOREIGNENTITY", DT_STRING);
			put(rs, "STRFOREIGNENTITYFIELD", values, "STRFOREIGNENTITYFIELD", DT_STRING);
			put(rs, "INTDATASCALE", values, "INTDATASCALE", DT_INTEGER);
			put(rs, "INTDATAPRECISION", values, "INTDATAPRECISION", DT_INTEGER);
			//put(rs, "", values, "INTID_FOREIGN_DEFAULT", DT_INTEGER);
			put(rs, "STRDEFAULT", values, "STRVALUE_DEFAULT", DT_STRING);
			put(rs, "STRFORMATINPUT", values, "STRFORMATINPUT", DT_STRING);
			put(rs, "STRFORMATOUTPUT", values, "STRFORMATOUTPUT", DT_STRING);
			put(rs, "BLNUNIQUE", values, "BLNUNIQUE", DT_BOOLEAN);
			put(rs, "BLNNULLABLE", values, "BLNNULLABLE", DT_BOOLEAN);
			put(rs, "BLNSEARCHABLE", values, "BLNSEARCHABLE", DT_BOOLEAN);
			values.put("BLNMODIFIABLE", Boolean.FALSE);
			values.put("BLNINSERTABLE", Boolean.FALSE);
			put(rs, "BLNLOGBOOK", values, "BLNLOGBOOKTRACKING", DT_BOOLEAN);
			values.put("BLNSHOWMNEMONIC", Boolean.FALSE);
			values.put("BLNREADONLY", Boolean.FALSE);
			//put(rs, "", values, "STRCALCFUNCTION", DT_STRING);
			//put(rs, "", values, "STRSORTATIONASC", DT_STRING);
			//put(rs, "", values, "STRSORTATIONDESC", DT_STRING);
			put(looksEmpty(sLabelResource)? createResource(sLabel) : sLabelResource, values, "STR_LOCALERESOURCE_L");
			put(looksEmpty(sDescriptionResource)? createResource(sDescription) : sDescriptionResource, values, "STR_LOCALERESOURCE_D");
			put(rs, "DATCREATED", values, "DATCREATED", DT_DATE);
			put(rs, "STRCREATED", values, "STRCREATED", DT_STRING);
			put(rs, "DATCHANGED", values, "DATCHANGED", DT_DATE);
			put(rs, "STRCHANGED", values, "STRCHANGED", DT_STRING);
			put(rs, "INTVERSION", values, "INTVERSION", DT_INTEGER);

			/** update Logbook */
			Map<String, Object> columnValueMap = new HashMap<String, Object>();
			Map<String, Object> conditionMap = new HashMap<String, Object>();
			columnValueMap.put("INTID_T_AD_MD_FIELD", iNewMDFieldId);
			conditionMap.put("INTID_T_AD_MD_FIELD", id);
			info("Update Logbook entry (\"INTID_T_AD_MD_FIELD " + id + " --> " + iNewMDFieldId + "\")");
			if(armed) info(DataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_UD_LOGBOOK", columnValueMap, conditionMap)) + " records updated.");

			if (armed) DataBaseHelper.getDbAccess().execute(new DbInsertStatement("T_MD_ENTITY_FIELD", values));
		}
	}

	private void migrateModules() throws CommonValidationException, ClassNotFoundException {
		info("*** M I G R A T E   M O D U L E S ***");

		DbQuery<DbTuple> query = DataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from("T_MD_MODULE");
		from.alias("mo");
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();
		columns.add(getColumn(from, "INTID", DT_INTEGER));
		columns.add(getColumn(from, "STRMODULE", DT_STRING));
		columns.add(getColumn(from, "STRDESCRIPTION", DT_STRING));
		columns.add(getColumn(from, "STRENTITY", DT_STRING));
		columns.add(getColumn(from, "DATCREATED", DT_DATE));
		columns.add(getColumn(from, "STRCREATED", DT_STRING));
		columns.add(getColumn(from, "DATCHANGED", DT_DATE));
		columns.add(getColumn(from, "STRCHANGED", DT_STRING));
		columns.add(getColumn(from, "INTVERSION", DT_INTEGER));
		columns.add(getColumn(from, "STRSYSTEMIDMNEMONIC", DT_STRING));
		columns.add(getColumn(from, "STRMENUMNEMONIC", DT_STRING));
		columns.add(getColumn(from, "BLNUSESSTATEMODEL", DT_BOOLEAN));
		columns.add(getColumn(from, "BLNLOGBOOKTRACKING", DT_BOOLEAN));
		columns.add(getColumn(from, "STRACCELERATOR", DT_STRING));
		columns.add(getColumn(from, "STRTREEVIEW", DT_STRING));
		columns.add(getColumn(from, "BLNTREERELATION", DT_BOOLEAN));
		columns.add(getColumn(from, "BLNTREEGROUP", DT_BOOLEAN));
		columns.add(getColumn(from, "STRTREEVIEWDESCRIPTION", DT_STRING));
		columns.add(getColumn(from, "STRMENUPATH", DT_STRING));
		columns.add(getColumn(from, "INTID_T_MD_RESOURCE", DT_INTEGER));
		columns.add(getColumn(from, "BLNIMPORTEXPORT", DT_BOOLEAN));
		columns.add(getColumn(from, "STR_LOCALERESOURCE_L", DT_STRING));
		columns.add(getColumn(from, "STR_LOCALERESOURCE_M", DT_STRING));
		columns.add(getColumn(from, "STR_LOCALERESOURCE_D", DT_STRING));
		columns.add(getColumn(from, "STR_LOCALERESOURCE_TW", DT_STRING));
		columns.add(getColumn(from, "STR_LOCALERESOURCE_TT", DT_STRING));
		query.multiselect(columns);

		List<DbTuple> result = DataBaseHelper.getDbAccess().executeQuery(query);


		for (DbTuple rs : result) {
			Integer id = rs.get("INTID", java.lang.Integer.class);
			String sEntity = rs.get("STRENTITY", java.lang.String.class);

			String sLabel = rs.get("STRMODULE", java.lang.String.class);
			String sMenu = rs.get("STRMENUPATH", java.lang.String.class);
			String sDesc = rs.get("STRDESCRIPTION", java.lang.String.class);
			String sTree = rs.get("STRTREEVIEW", java.lang.String.class);
			String sTreeDesc = rs.get("STRTREEVIEWDESCRIPTION", java.lang.String.class);

			String sLabelRes = rs.get("STR_LOCALERESOURCE_L", java.lang.String.class);
			String sMenuRes = rs.get("STR_LOCALERESOURCE_M", java.lang.String.class);
			String sDescRes = rs.get("STR_LOCALERESOURCE_D", java.lang.String.class);
			String sTreeRes = rs.get("STR_LOCALERESOURCE_TW", java.lang.String.class);
			String sTreeDescRes = rs.get("STR_LOCALERESOURCE_TT", java.lang.String.class);

			info("-----------------------------------");
			info("Processing module entity " + sEntity);

			Map<String, Object> values = new HashMap<String, Object>();
			put(rs, "INTID", values, "INTID", DT_INTEGER);
			put(rs, "STRENTITY", values, "STRENTITY", DT_STRING);
			values.put("STRDBENTITY", "V_EO_"+sEntity.toUpperCase());
			put(rs, "STRSYSTEMIDMNEMONIC", values, "STRSYSTEMIDPREFIX", DT_STRING);
			put(rs, "STRMENUMNEMONIC", values, "STRMENUSHORTCUT", DT_STRING);
			values.put("BLNEDITABLE", Boolean.TRUE);
			values.put("BLNUSESSTATEMODEL", Boolean.TRUE);
			put(rs, "BLNLOGBOOKTRACKING", values, "BLNLOGBOOKTRACKING", DT_BOOLEAN);
			values.put("BLNCACHEABLE", Boolean.FALSE);
			values.put("BLNSEARCHABLE", Boolean.TRUE);
			put(rs, "BLNTREERELATION", values, "BLNTREERELATION", DT_BOOLEAN);
			put(rs, "BLNTREEGROUP", values, "BLNTREEGROUP", DT_BOOLEAN);
			put(rs, "BLNIMPORTEXPORT", values, "BLNIMPORTEXPORT", DT_BOOLEAN);
			values.put("BLNFIELDVALUEENTITY", Boolean.FALSE);
			put(rs, "STRACCELERATOR", values, "STRACCELERATOR", DT_STRING);
			//put(rs, "", values, "INTACCELERATORMODIFIER", DT_INTEGER);
			//put(rs, "", values, "STRFIELDS_FOR_EQUALITY", DT_STRING);
			put(rs, "INTID_T_MD_RESOURCE", values, "INTID_T_MD_RESOURCE", DT_INTEGER);
			put(looksEmpty(sLabelRes)? createResource(sLabel) : sLabelRes, values, "STR_LOCALERESOURCE_L");
			put(looksEmpty(sMenuRes)? createResource(sMenu) : sMenuRes, values, "STR_LOCALERESOURCE_M");
			put(looksEmpty(sDescRes)? createResource(sDesc) : sDescRes, values, "STR_LOCALERESOURCE_D");
			put(looksEmpty(sTreeRes)? createResource(sTree) : sTreeRes, values, "STR_LOCALERESOURCE_TW");
			put(looksEmpty(sTreeDescRes)? createResource(sTreeDesc) : sTreeDescRes, values, "STR_LOCALERESOURCE_TT");
			put(rs, "DATCREATED", values, "DATCREATED", DT_DATE);
			put(rs, "STRCREATED", values, "STRCREATED", DT_STRING);
			put(rs, "DATCHANGED", values, "DATCHANGED", DT_DATE);
			put(rs, "STRCHANGED", values, "STRCHANGED", DT_STRING);
			put(rs, "INTVERSION", values, "INTVERSION", DT_INTEGER);

			if(armed) DataBaseHelper.getDbAccess().execute(new DbInsertStatement("T_MD_ENTITY", values));

			info("Processing attributes now...");

			migrateAttributesForModule(id, sEntity);
		}

		/** update system attributes in logbook */
		updateLogbook(100000, -10012, "[system_id]");
		updateLogbook(100001, -10010, "[status]");
		updateLogbook(100002, -10011, "[status_numeral]");
		updateLogbook(100003, -10013, "[prozess]");
		updateLogbook(100004, -10015, "[erstellt_von]");
		updateLogbook(100005, -10014, "[erstellt_am]");
		updateLogbook(100006, -10017, "[geaendert_von]");
		updateLogbook(100007, -10016, "[geaendert_am]");
		updateLogbook(100008, -10018, "[herkunft]");
	}

	private void updateLogbook(Integer oldAttrId, Integer newAttrId, String sAttributeName) {
		Map<String, Object> columnValueMap = new HashMap<String, Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();
		columnValueMap.put("INTID_T_MD_ATTRIBUTE", newAttrId);
		conditionMap.put("INTID_T_MD_ATTRIBUTE", oldAttrId);
		info("Update " + sAttributeName + " in Logbook...");
		if(armed) info(DataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_UD_LOGBOOK", columnValueMap, conditionMap)) + " records updated.");
	}

	private void migrateAttributesForModule(Integer iModuleId, String entity) throws CommonValidationException, ClassNotFoundException {
		int i = 0;
		for (String attribute : getAttributesFromLayouts(iModuleId, entity)) {
			DbQuery<DbTuple> query = DataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
			DbFrom from = query.from("T_MD_ATTRIBUTE");
			from.alias("attr");
			List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();
			columns.add(getColumn(from, "INTID", DT_INTEGER));
			columns.add(getColumn(from, "INTID_T_MD_ATTRIBUTEGROUP", DT_INTEGER));
			columns.add(getColumn(from, "STRATTRIBUTE", DT_STRING));
			columns.add(getColumn(from, "STRATTRIBUTELABEL", DT_STRING));
			columns.add(getColumn(from, "STRDESCRIPTION", DT_STRING));
			columns.add(getColumn(from, "STRDATATYPE", DT_STRING));
			columns.add(getColumn(from, "DATCREATED", DT_DATE));
			columns.add(getColumn(from, "STRCREATED", DT_STRING));
			columns.add(getColumn(from, "DATCHANGED", DT_DATE));
			columns.add(getColumn(from, "STRCHANGED", DT_STRING));
			columns.add(getColumn(from, "INTVERSION", DT_INTEGER));
			columns.add(getColumn(from, "INTDATASCALE", DT_INTEGER));
			columns.add(getColumn(from, "INTDATAPRECISION", DT_INTEGER));
			columns.add(getColumn(from, "STRFORMATINPUT", DT_STRING));
			columns.add(getColumn(from, "STRFORMATOUTPUT", DT_STRING));
			columns.add(getColumn(from, "BLNNULLABLE", DT_BOOLEAN));
			columns.add(getColumn(from, "BLNSEARCHABLE", DT_BOOLEAN));
			columns.add(getColumn(from, "BLNMODIFIABLE", DT_BOOLEAN));
			columns.add(getColumn(from, "BLNINSERTABLE", DT_BOOLEAN));
			columns.add(getColumn(from, "BLNLOGBOOKTRACKING", DT_BOOLEAN));
			columns.add(getColumn(from, "BLNSYSTEMATTRIBUTE", DT_BOOLEAN));
			columns.add(getColumn(from, "INTID_T_DP_VALUE_DEFAULT", DT_INTEGER));
			columns.add(getColumn(from, "INTID_EXTERNAL_DEFAULT", DT_INTEGER));
			columns.add(getColumn(from, "STRVALUE_DEFAULT", DT_STRING));
			columns.add(getColumn(from, "STREXTERNALENTITY", DT_STRING));
			columns.add(getColumn(from, "BLNSHOWMNEMONIC", DT_BOOLEAN));
			columns.add(getColumn(from, "STREXTERNALENTITYFIELD", DT_STRING));
			columns.add(getColumn(from, "STRCALCFUNCTION", DT_STRING));
			columns.add(getColumn(from, "STRSORTATIONASC", DT_STRING));
			columns.add(getColumn(from, "STRSORTATIONDESC", DT_STRING));
			columns.add(getColumn(from, "STR_LOCALERESOURCE_L", DT_STRING));
			columns.add(getColumn(from, "STR_LOCALERESOURCE_D", DT_STRING));
			query.multiselect(columns);
			query.where(DataBaseHelper.getDbAccess().getQueryBuilder().equal(
				from.baseColumn("STRATTRIBUTE", DT_STRING),
				DataBaseHelper.getDbAccess().getQueryBuilder().literal(attribute)));

			List<DbTuple> result = DataBaseHelper.getDbAccess().executeQuery(query);

			Set<String> systemArrtibutes = new HashSet<String>();
			systemArrtibutes.add("[erstellt_am]");
			systemArrtibutes.add("[erstellt_von]");
			systemArrtibutes.add("[geaendert_am]");
			systemArrtibutes.add("[geaendert_von]");
			systemArrtibutes.add("[herkunft]");
			systemArrtibutes.add("[plan_end]");
			systemArrtibutes.add("[plan_runtime]");
			systemArrtibutes.add("[plan_start]");
			systemArrtibutes.add("[prozess]");
			systemArrtibutes.add("[real_end]");
			systemArrtibutes.add("[real_runtime]");
			systemArrtibutes.add("[real_start]");
			systemArrtibutes.add("[status]");
			systemArrtibutes.add("[status_numeral]");
			systemArrtibutes.add("[system_id]");
			systemArrtibutes.add("processmodel");
			systemArrtibutes.add("instancename");

			for (DbTuple rs : result) {
				Integer iAttrId = rs.get("INTID", java.lang.Integer.class);
				String sField = rs.get("STRATTRIBUTE", java.lang.String.class);
				String sLabel = rs.get("STRATTRIBUTELABEL", java.lang.String.class);
				String sLabelResource = rs.get("STR_LOCALERESOURCE_L", java.lang.String.class);
				String sDescription = rs.get("STRDESCRIPTION", java.lang.String.class);
				String sDescriptionResource = rs.get("STR_LOCALERESOURCE_D", java.lang.String.class);
				String sForeignEntity = rs.get("STREXTERNALENTITY", java.lang.String.class);
				String sDataType = replaceDeNovabitPackage(rs.get("STRDATATYPE", java.lang.String.class));
				Integer iDataScale = rs.get("INTDATASCALE", java.lang.Integer.class);
				Integer iDataPrecision = rs.get("INTDATAPRECISION", java.lang.Integer.class);
				String sCalcFunction = rs.get("STRCALCFUNCTION", java.lang.String.class);

				Integer iNewAttributeId = DataBaseHelper.getNextIdAsInteger(DataBaseHelper.DEFAULT_SEQUENCE);

				if (systemArrtibutes.contains(sField)) {
					continue;
				}

				/** update logbook */
				DbQuery<Integer> queryLB = DataBaseHelper.getDbAccess().getQueryBuilder().createQuery(Integer.class);
				DbFrom lb = queryLB.from("T_UD_LOGBOOK").alias("lb");
				DbFrom go = lb.join("T_UD_GENERICOBJECT", JoinType.INNER).alias("go").on("INTID_T_UD_GENERICOBJECT", "INTID", Integer.class);
				queryLB.select(lb.baseColumn("INTID", Integer.class));
				queryLB.where(DataBaseHelper.getDbAccess().getQueryBuilder().and(
					DataBaseHelper.getDbAccess().getQueryBuilder().equal(go.baseColumn("INTID_T_MD_MODULE", Integer.class), iModuleId),
					DataBaseHelper.getDbAccess().getQueryBuilder().equal(lb.baseColumn("INTID_T_MD_ATTRIBUTE", Integer.class), iAttrId)));
				for (Integer logbookEntryid : DataBaseHelper.getDbAccess().executeQuery(queryLB.distinct(true))) {
					Map<String, Object> columnValueMap = new HashMap<String, Object>();
					Map<String, Object> conditionMap = new HashMap<String, Object>();
					columnValueMap.put("INTID_T_MD_ATTRIBUTE", iNewAttributeId);
					conditionMap.put("INTID", logbookEntryid);

					info("Update Logbook entry with id " + logbookEntryid + " (\"INTID_T_MD_ATTRIBUTE " + iAttrId + " --> " + iNewAttributeId + " WHERE INTID_T_MD_MODULE=" + iModuleId + "\")");
					if(armed) info(DataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_UD_LOGBOOK", columnValueMap, conditionMap)) + " records updated.");
				}

				info("Processing attribute " + sField);

				/**
				 * Look if attribute has simple values
				 */
				DbQuery<DbTuple> queryAttributeValues = DataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
				DbFrom fromAttributeValues = queryAttributeValues.from("T_MD_ATTRIBUTEVALUE");
				fromAttributeValues.alias("attrvalue");
				List<DbSelection<?>> columnsAttributeValues = new ArrayList<DbSelection<?>>();
				columnsAttributeValues.add(getColumn(fromAttributeValues, "INTID", DT_INTEGER));
				columnsAttributeValues.add(getColumn(fromAttributeValues, "INTID_T_MD_ATTRIBUTE", DT_INTEGER));
				columnsAttributeValues.add(getColumn(fromAttributeValues, "STRVALUE", DT_STRING));
				columnsAttributeValues.add(getColumn(fromAttributeValues, "STRMNEMONIC", DT_STRING));
				columnsAttributeValues.add(getColumn(fromAttributeValues, "STRDESCRIPTION", DT_STRING));
				columnsAttributeValues.add(getColumn(fromAttributeValues, "DATVALIDFROM", DT_DATE));
				columnsAttributeValues.add(getColumn(fromAttributeValues, "DATVALIDUNTIL", DT_DATE));
				columnsAttributeValues.add(getColumn(fromAttributeValues, "DATCREATED", DT_DATE));
				columnsAttributeValues.add(getColumn(fromAttributeValues, "STRCREATED", DT_STRING));
				columnsAttributeValues.add(getColumn(fromAttributeValues, "DATCHANGED", DT_DATE));
				columnsAttributeValues.add(getColumn(fromAttributeValues, "STRCHANGED", DT_STRING));
				columnsAttributeValues.add(getColumn(fromAttributeValues, "INTVERSION", DT_INTEGER));
				queryAttributeValues.multiselect(columnsAttributeValues);
				queryAttributeValues.where(DataBaseHelper.getDbAccess().getQueryBuilder().equal(
					fromAttributeValues.baseColumn("INTID_T_MD_ATTRIBUTE", DT_INTEGER),
					DataBaseHelper.getDbAccess().getQueryBuilder().literal(iAttrId)));

				String sFieldValueEntity = null;

				List<DbTuple> resultAttributeValues = DataBaseHelper.getDbAccess().executeQuery(queryAttributeValues);
				for (DbTuple rsAttributeValue : resultAttributeValues) {
					if (sFieldValueEntity == null) {
						sFieldValueEntity = createFieldValueEntity(sDataType, iDataScale, iDataPrecision);
					}
					String sValue = rsAttributeValue.get("STRVALUE", String.class);

					info("Processing attribute value " + sValue);
					{
						Class<?> valueClass = Class.forName(sDataType);
						Object oValue = CanonicalAttributeFormat.getInstance(valueClass).parse(sValue);
						Object oValueMnemonic = CanonicalAttributeFormat.getInstance(valueClass).parse(rsAttributeValue.get("STRMNEMONIC", String.class));

						Map<String, Object> values = new HashMap<String, Object>();
						put(rsAttributeValue, "INTID", values, "INTID", DT_INTEGER);
						put(oValue, values, "STRVALUE");
						put(oValueMnemonic, values, "STRMNEMONIC");
						put(rsAttributeValue, "STRDESCRIPTION", values, "STRDESCRIPTION", DT_STRING);
						put(rsAttributeValue, "DATVALIDFROM", values, "DATVALIDFROM", DT_DATE);
						put(rsAttributeValue, "DATVALIDUNTIL", values, "DATVALIDUNTIL", DT_DATE);
						put(rsAttributeValue, "DATCREATED", values, "DATCREATED", DT_DATE);
						put(rsAttributeValue, "STRCREATED", values, "STRCREATED", DT_STRING);
						put(rsAttributeValue, "DATCHANGED", values, "DATCHANGED", DT_DATE);
						put(rsAttributeValue, "STRCHANGED", values, "STRCHANGED", DT_STRING);
						put(rsAttributeValue, "INTVERSION", values, "INTVERSION", DT_INTEGER);

						/** update Logbook */
						{
							Map<String, Object> columnValueMap = new HashMap<String, Object>();
							Map<String, Object> conditionMap = new HashMap<String, Object>();
							columnValueMap.put("INTID_EXTERNAL_OLD", rsAttributeValue.get("INTID", DT_INTEGER));
							columnValueMap.put("INTID_T_DP_VALUE_OLD", DbNull.forType(Integer.class));
							conditionMap.put("INTID_T_DP_VALUE_OLD", rsAttributeValue.get("INTID", DT_INTEGER));
							info("Update Logbook entry (\"INTID_T_DP_VALUE_OLD --> INTID_EXTERNAL_OLD WHERE INTID_T_DP_VALUE_OLD=" + rsAttributeValue.get("INTID", DT_INTEGER) + "\")");
							if(armed) info(DataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_UD_LOGBOOK", columnValueMap, conditionMap)) + " records updated.");
						}{
							Map<String, Object> columnValueMap = new HashMap<String, Object>();
							Map<String, Object> conditionMap = new HashMap<String, Object>();
							columnValueMap.put("INTID_EXTERNAL_NEW", rsAttributeValue.get("INTID", DT_INTEGER));
							columnValueMap.put("INTID_T_DP_VALUE_NEW", DbNull.forType(Integer.class));
							conditionMap.put("INTID_T_DP_VALUE_NEW", rsAttributeValue.get("INTID", DT_INTEGER));
							info("Update Logbook entry (\"INTID_T_DP_VALUE_NEW --> INTID_EXTERNAL_NEW WHERE INTID_T_DP_VALUE_NEW=" + rsAttributeValue.get("INTID", DT_INTEGER) + "\")");
							if(armed) info(DataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_UD_LOGBOOK", columnValueMap, conditionMap)) + " records updated.");
						}


						if(armed) DataBaseHelper.getDbAccess().execute(new DbInsertStatement("T_EO_" + sFieldValueEntity.toUpperCase(), values));
					}
				}

				String sDbField = looksEmpty(sForeignEntity)&&sFieldValueEntity==null ? sField.toUpperCase() : "STRVALUE_"+sField.toUpperCase();
				if (sDbField.length() > 30) {
					i++;
					sDbField = sDbField.substring(0, 28) + (i<10?"0":"") + i;
				}

				Map<String, Object> values = new HashMap<String, Object>();
				values.put("INTID", iNewAttributeId);
				values.put("INTID_T_MD_ENTITY", iModuleId);
				put(rs, "INTID_T_MD_ATTRIBUTEGROUP", values, "INTID_T_MD_ENTITY_FIELD_GROUP", DT_INTEGER);
				put(rs, "STRATTRIBUTE", values, "STRFIELD", DT_STRING);
				put(sDbField, values, "STRDBFIELD");
				put(sDataType, values, "STRDATATYPE");
				if (sFieldValueEntity != null) {
					values.put("STRFOREIGNENTITY", sFieldValueEntity);
					values.put("STRFOREIGNENTITYFIELD", "value");
				} else {
					put(rs, "STREXTERNALENTITY", values, "STRFOREIGNENTITY", DT_STRING);
					put(rs, "STREXTERNALENTITYFIELD", values, "STRFOREIGNENTITYFIELD", DT_STRING);
				}
				put(rs, "INTDATASCALE", values, "INTDATASCALE", DT_INTEGER);
				put(rs, "INTDATAPRECISION", values, "INTDATAPRECISION", DT_INTEGER);
				put(rs, "INTID_EXTERNAL_DEFAULT", values, "INTID_FOREIGN_DEFAULT", DT_INTEGER);
				put(rs, "STRVALUE_DEFAULT", values, "STRVALUE_DEFAULT", DT_STRING);
				put(rs, "STRFORMATINPUT", values, "STRFORMATINPUT", DT_STRING);
				put(rs, "STRFORMATOUTPUT", values, "STRFORMATOUTPUT", DT_STRING);
				values.put("BLNUNIQUE", Boolean.FALSE);
				put(rs, "BLNNULLABLE", values, "BLNNULLABLE", DT_BOOLEAN);
				put(rs, "BLNSEARCHABLE", values, "BLNSEARCHABLE", DT_BOOLEAN);
				put(rs, "BLNMODIFIABLE", values, "BLNMODIFIABLE", DT_BOOLEAN);
				put(rs, "BLNINSERTABLE", values, "BLNINSERTABLE", DT_BOOLEAN);
				put(rs, "BLNLOGBOOKTRACKING", values, "BLNLOGBOOKTRACKING", DT_BOOLEAN);
				put(rs, "BLNSHOWMNEMONIC", values, "BLNSHOWMNEMONIC", DT_BOOLEAN);
				values.put("BLNREADONLY", Boolean.FALSE);
				put(sCalcFunction, values, "STRCALCFUNCTION");
				put(rs, "STRSORTATIONASC", values, "STRSORTATIONASC", DT_STRING);
				put(rs, "STRSORTATIONDESC", values, "STRSORTATIONDESC", DT_STRING);
				put(looksEmpty(sLabelResource)? createResource(sLabel) : sLabelResource, values, "STR_LOCALERESOURCE_L");
				put(looksEmpty(sDescriptionResource)? createResource(sDescription) : sDescriptionResource, values, "STR_LOCALERESOURCE_D");
				put(rs, "STR_LOCALERESOURCE_D", values, "STR_LOCALERESOURCE_D", DT_STRING);
				put(rs, "DATCREATED", values, "DATCREATED", DT_DATE);
				put(rs, "STRCREATED", values, "STRCREATED", DT_STRING);
				put(rs, "DATCHANGED", values, "DATCHANGED", DT_DATE);
				put(rs, "STRCHANGED", values, "STRCHANGED", DT_STRING);
				put(rs, "INTVERSION", values, "INTVERSION", DT_INTEGER);

				if (armed) DataBaseHelper.getDbAccess().execute(new DbInsertStatement("T_MD_ENTITY_FIELD", values));

				/** update Generation */
				{
					DbQuery<Integer> queryGENA = DataBaseHelper.getDbAccess().getQueryBuilder().createQuery(Integer.class);
					DbFrom gena = queryGENA.from("T_MD_GENERATION_ATTRIBUTE").alias("gena");
					DbFrom gen = gena.join("T_MD_GENERATION", JoinType.INNER).alias("gen").on("INTID_T_MD_GENERATION", "INTID", Integer.class);
					queryGENA.select(gena.baseColumn("INTID", Integer.class));
					queryGENA.where(DataBaseHelper.getDbAccess().getQueryBuilder().and(
						DataBaseHelper.getDbAccess().getQueryBuilder().equal(gen.baseColumn("INTID_T_MD_MODULE_SOURCE", Integer.class), iModuleId),
						DataBaseHelper.getDbAccess().getQueryBuilder().equal(gena.baseColumn("INTID_T_MD_ATTRIBUTE_SOURCE", Integer.class), iAttrId)));
					for (Integer genAttributeid : DataBaseHelper.getDbAccess().executeQuery(queryGENA.distinct(true))) {
						Map<String, Object> columnValueMap = new HashMap<String, Object>();
						Map<String, Object> conditionMap = new HashMap<String, Object>();
						columnValueMap.put("INTID_T_MD_ATTRIBUTE_SOURCE", iNewAttributeId);
						conditionMap.put("INTID", genAttributeid);

						info("Update Generation attribute entry with id " + genAttributeid + " (\"INTID_T_MD_ATTRIBUTE_SOURCE " + iAttrId + " --> " + iNewAttributeId + " WHERE INTID_T_MD_MODULE_SOURCE=" + iModuleId + "\")");
						if(armed) info(DataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_MD_GENERATION_ATTRIBUTE", columnValueMap, conditionMap)) + " records updated.");
					}
				}
				{
					DbQuery<Integer> queryGENA = DataBaseHelper.getDbAccess().getQueryBuilder().createQuery(Integer.class);
					DbFrom gena = queryGENA.from("T_MD_GENERATION_ATTRIBUTE").alias("gena");
					DbFrom gen = gena.join("T_MD_GENERATION", JoinType.INNER).alias("gen").on("INTID_T_MD_GENERATION", "INTID", Integer.class);
					queryGENA.select(gena.baseColumn("INTID", Integer.class));
					queryGENA.where(DataBaseHelper.getDbAccess().getQueryBuilder().and(
						DataBaseHelper.getDbAccess().getQueryBuilder().equal(gen.baseColumn("INTID_T_MD_MODULE_TARGET", Integer.class), iModuleId),
						DataBaseHelper.getDbAccess().getQueryBuilder().equal(gena.baseColumn("INTID_T_MD_ATTRIBUTE_TARGET", Integer.class), iAttrId)));
					for (Integer genAttributeid : DataBaseHelper.getDbAccess().executeQuery(queryGENA.distinct(true))) {
						Map<String, Object> columnValueMap = new HashMap<String, Object>();
						Map<String, Object> conditionMap = new HashMap<String, Object>();
						columnValueMap.put("INTID_T_MD_ATTRIBUTE_TARGET", iNewAttributeId);
						conditionMap.put("INTID", genAttributeid);

						info("Update Generation attribute entry with id " + genAttributeid + " (\"INTID_T_MD_ATTRIBUTE_TARGET " + iAttrId + " --> " + iNewAttributeId + " WHERE INTID_T_MD_MODULE_TARGET=" + iModuleId + "\")");
						if(armed) info(DataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_MD_GENERATION_ATTRIBUTE", columnValueMap, conditionMap)) + " records updated.");
					}
				}

				/** delete calcfunction go attributes */
				if (sCalcFunction != null) {

					Map<String, Object> conditionMap = new HashMap<String, Object>();
					conditionMap.put("INTID_T_MD_ATTRIBUTE", iAttrId);

					info("delete calcfunction go attributes with attribute id " + iAttrId);
					if(armed) info(DataBaseHelper.getDbAccess().execute(new DbDeleteStatement("t_ud_go_attribute", conditionMap)) + " records deleted.");

				}
			}
		}
	}

	private void migrateAttributeGroups() {
		info("*** M I G R A T E   A T T R I B U T E   G R O U P S ***");

		DbQuery<DbTuple> query = DataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from("T_MD_ATTRIBUTEGROUP");
		from.alias("attrgrp");
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();
		columns.add(getColumn(from, "INTID", DT_INTEGER));
		columns.add(getColumn(from, "STRGROUP", DT_STRING));
		columns.add(getColumn(from, "DATCREATED", DT_DATE));
		columns.add(getColumn(from, "STRCREATED", DT_STRING));
		columns.add(getColumn(from, "DATCHANGED", DT_DATE));
		columns.add(getColumn(from, "STRCHANGED", DT_STRING));
		columns.add(getColumn(from, "INTVERSION", DT_INTEGER));
		query.multiselect(columns);

		List<DbTuple> result = DataBaseHelper.getDbAccess().executeQuery(query);


		for (DbTuple rs : result) {
			String sGroup = rs.get("STRGROUP", java.lang.String.class);

			info("Processing attribute group " + sGroup);

			Map<String, Object> values = new HashMap<String, Object>();
			put(rs, "INTID", values, "INTID", DT_INTEGER);
			put(rs, "STRGROUP", values, "STRGROUP", DT_STRING);
			put(rs, "DATCREATED", values, "DATCREATED", DT_DATE);
			put(rs, "STRCREATED", values, "STRCREATED", DT_STRING);
			put(rs, "DATCHANGED", values, "DATCHANGED", DT_DATE);
			put(rs, "STRCHANGED", values, "STRCHANGED", DT_STRING);
			put(rs, "INTVERSION", values, "INTVERSION", DT_INTEGER);

			if(armed) DataBaseHelper.getDbAccess().execute(new DbInsertStatement("T_MD_ENTITY_FIELD_GROUP", values));
		}
	}

	private void migrateModuleSubnodes() {
		info("*** M I G R A T E   M O D U L E   S U B N O D E S ***");

		DbQuery<DbTuple> query = DataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from("T_MD_MODULE_SUBNODES");
		from.alias("modsubnode");
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();
		columns.add(getColumn(from, "INTID", DT_INTEGER));
		columns.add(getColumn(from, "INTID_T_MD_MODULE", DT_INTEGER));
		columns.add(getColumn(from, "STRENTITY", DT_STRING));
		columns.add(getColumn(from, "STRFIELD", DT_STRING));
		columns.add(getColumn(from, "STRFOLDERNAME", DT_STRING));
		columns.add(getColumn(from, "DATCREATED", DT_DATE));
		columns.add(getColumn(from, "STRCREATED", DT_STRING));
		columns.add(getColumn(from, "DATCHANGED", DT_DATE));
		columns.add(getColumn(from, "STRCHANGED", DT_STRING));
		columns.add(getColumn(from, "INTVERSION", DT_INTEGER));
		query.multiselect(columns);

		List<DbTuple> result = DataBaseHelper.getDbAccess().executeQuery(query);


		for (DbTuple rs : result) {
			Integer iModule = rs.get("INTID_T_MD_MODULE", java.lang.Integer.class);
			String sEntity = rs.get("STRENTITY", java.lang.String.class);
			String sField = rs.get("STRFIELD", java.lang.String.class);
			String sFolderName = rs.get("STRFOLDERNAME", java.lang.String.class);

			info("Processing module subnode (module=" + iModule + " entity=" + sEntity + " field=" + sField + ")");

			Map<String, Object> values = new HashMap<String, Object>();
			put(rs, "INTID", values, "INTID", DT_INTEGER);
			put(rs, "INTID_T_MD_MODULE", values, "INTID_T_MD_ENTITY", DT_INTEGER);
			put(rs, "STRENTITY", values, "STRENTITY", DT_STRING);
			put(rs, "STRFIELD", values, "STRFIELD", DT_STRING);
			put(createResource(sFolderName), values, "STR_LOCALERESOURCE_FN");
			put(rs, "DATCREATED", values, "DATCREATED", DT_DATE);
			put(rs, "STRCREATED", values, "STRCREATED", DT_STRING);
			put(rs, "DATCHANGED", values, "DATCHANGED", DT_DATE);
			put(rs, "STRCHANGED", values, "STRCHANGED", DT_STRING);
			put(rs, "INTVERSION", values, "INTVERSION", DT_INTEGER);

			if(armed) DataBaseHelper.getDbAccess().execute(new DbInsertStatement("T_MD_ENTITY_SUBNODES", values));
		}
	}

	int iCountFieldValueEntities = 0;
	String sFieldValueEntityNameBase = "fieldValueEntity";
	private String createFieldValueEntity(String sDataType, Integer iDataScape, Integer iDataPrecision) {
		iCountFieldValueEntities++;
		String sEntityName = sFieldValueEntityNameBase+iCountFieldValueEntities;
		Integer iEntityId = DataBaseHelper.getNextIdAsInteger(DataBaseHelper.DEFAULT_SEQUENCE);

		info("Create field value entity " + sEntityName);

		{
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("INTID", iEntityId);
			put(sEntityName, values, "STRENTITY");
			put("V_EO_"+sEntityName.toUpperCase(), values, "STRDBENTITY");
			values.put("BLNEDITABLE", Boolean.FALSE);
			values.put("BLNUSESSTATEMODEL", Boolean.FALSE);
			values.put("BLNLOGBOOKTRACKING", Boolean.FALSE);
			values.put("BLNCACHEABLE", Boolean.TRUE);
			values.put("BLNSEARCHABLE", Boolean.FALSE);
			values.put("BLNTREERELATION", Boolean.FALSE);
			values.put("BLNTREEGROUP", Boolean.FALSE);
			values.put("BLNIMPORTEXPORT", Boolean.FALSE);
			values.put("BLNFIELDVALUEENTITY", Boolean.TRUE);
			values.put("DATCREATED", new Date());
			values.put("STRCREATED", "Migration v2.5.00");
			values.put("DATCHANGED", new Date());
			values.put("STRCHANGED", "Migration v2.5.00");
			values.put("INTVERSION", new Integer(1));

			if(armed) DataBaseHelper.getDbAccess().execute(new DbInsertStatement("T_MD_ENTITY", values));
		}

		{
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("INTID", DataBaseHelper.getNextIdAsInteger(DataBaseHelper.DEFAULT_SEQUENCE));
			values.put("INTID_T_MD_ENTITY", iEntityId);
			values.put("STRFIELD", "value");
			values.put("STRDBFIELD", "STRVALUE");
			values.put("STRDATATYPE", sDataType);
			put(iDataScape, values, "INTDATASCALE");
			put(iDataPrecision, values, "INTDATAPRECISION");
			values.put("BLNUNIQUE", Boolean.TRUE);
			values.put("BLNNULLABLE", Boolean.FALSE);
			values.put("BLNSEARCHABLE", Boolean.FALSE);
			values.put("BLNMODIFIABLE", Boolean.FALSE);
			values.put("BLNINSERTABLE", Boolean.FALSE);
			values.put("BLNLOGBOOKTRACKING", Boolean.FALSE);
			values.put("BLNSHOWMNEMONIC", Boolean.FALSE);
			values.put("BLNREADONLY", Boolean.FALSE);
			values.put("DATCREATED", new Date());
			values.put("STRCREATED", "Migration v2.5.00");
			values.put("DATCHANGED", new Date());
			values.put("STRCHANGED", "Migration v2.5.00");
			values.put("INTVERSION", new Integer(1));

			if (armed) DataBaseHelper.getDbAccess().execute(new DbInsertStatement("T_MD_ENTITY_FIELD", values));
		}

		{
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("INTID", DataBaseHelper.getNextIdAsInteger(DataBaseHelper.DEFAULT_SEQUENCE));
			values.put("INTID_T_MD_ENTITY", iEntityId);
			values.put("STRFIELD", "mnemonic");
			values.put("STRDBFIELD", "STRMNEMONIC");
			values.put("STRDATATYPE", sDataType);
			put(iDataScape, values, "INTDATASCALE");
			put(iDataPrecision, values, "INTDATAPRECISION");
			values.put("BLNUNIQUE", Boolean.FALSE);
			values.put("BLNNULLABLE", Boolean.TRUE);
			values.put("BLNSEARCHABLE", Boolean.FALSE);
			values.put("BLNMODIFIABLE", Boolean.FALSE);
			values.put("BLNINSERTABLE", Boolean.FALSE);
			values.put("BLNLOGBOOKTRACKING", Boolean.FALSE);
			values.put("BLNSHOWMNEMONIC", Boolean.FALSE);
			values.put("BLNREADONLY", Boolean.FALSE);
			values.put("DATCREATED", new Date());
			values.put("STRCREATED", "Migration v2.5.00");
			values.put("DATCHANGED", new Date());
			values.put("STRCHANGED", "Migration v2.5.00");
			values.put("INTVERSION", new Integer(1));

			if (armed) DataBaseHelper.getDbAccess().execute(new DbInsertStatement("T_MD_ENTITY_FIELD", values));
		}

		{
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("INTID", DataBaseHelper.getNextIdAsInteger(DataBaseHelper.DEFAULT_SEQUENCE));
			values.put("INTID_T_MD_ENTITY", iEntityId);
			values.put("STRFIELD", "description");
			values.put("STRDBFIELD", "STRDESCRIPTION");
			put(String.class.getName(), values, "STRDATATYPE");
			values.put("INTDATASCALE", 4000);
			values.put("BLNUNIQUE", Boolean.FALSE);
			values.put("BLNNULLABLE", Boolean.TRUE);
			values.put("BLNSEARCHABLE", Boolean.FALSE);
			values.put("BLNMODIFIABLE", Boolean.FALSE);
			values.put("BLNINSERTABLE", Boolean.FALSE);
			values.put("BLNLOGBOOKTRACKING", Boolean.FALSE);
			values.put("BLNSHOWMNEMONIC", Boolean.FALSE);
			values.put("BLNREADONLY", Boolean.FALSE);
			values.put("DATCREATED", new Date());
			values.put("STRCREATED", "Migration v2.5.00");
			values.put("DATCHANGED", new Date());
			values.put("STRCHANGED", "Migration v2.5.00");
			values.put("INTVERSION", new Integer(1));

			if (armed) DataBaseHelper.getDbAccess().execute(new DbInsertStatement("T_MD_ENTITY_FIELD", values));
		}

		{
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("INTID", DataBaseHelper.getNextIdAsInteger(DataBaseHelper.DEFAULT_SEQUENCE));
			values.put("INTID_T_MD_ENTITY", iEntityId);
			values.put("STRFIELD", "validFrom");
			values.put("STRDBFIELD", "DATVALIDFROM");
			put(Date.class.getName(), values, "STRDATATYPE");
			values.put("BLNUNIQUE", Boolean.FALSE);
			values.put("BLNNULLABLE", Boolean.TRUE);
			values.put("BLNSEARCHABLE", Boolean.FALSE);
			values.put("BLNMODIFIABLE", Boolean.FALSE);
			values.put("BLNINSERTABLE", Boolean.FALSE);
			values.put("BLNLOGBOOKTRACKING", Boolean.FALSE);
			values.put("BLNSHOWMNEMONIC", Boolean.FALSE);
			values.put("BLNREADONLY", Boolean.FALSE);
			values.put("DATCREATED", new Date());
			values.put("STRCREATED", "Migration v2.5.00");
			values.put("DATCHANGED", new Date());
			values.put("STRCHANGED", "Migration v2.5.00");
			values.put("INTVERSION", new Integer(1));

			if (armed) DataBaseHelper.getDbAccess().execute(new DbInsertStatement("T_MD_ENTITY_FIELD", values));
		}

		{
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("INTID", DataBaseHelper.getNextIdAsInteger(DataBaseHelper.DEFAULT_SEQUENCE));
			values.put("INTID_T_MD_ENTITY", iEntityId);
			values.put("STRFIELD", "validUntil");
			values.put("STRDBFIELD", "DATVALIDUNTIL");
			put(Date.class.getName(), values, "STRDATATYPE");
			values.put("BLNUNIQUE", Boolean.FALSE);
			values.put("BLNNULLABLE", Boolean.TRUE);
			values.put("BLNSEARCHABLE", Boolean.FALSE);
			values.put("BLNMODIFIABLE", Boolean.FALSE);
			values.put("BLNINSERTABLE", Boolean.FALSE);
			values.put("BLNLOGBOOKTRACKING", Boolean.FALSE);
			values.put("BLNSHOWMNEMONIC", Boolean.FALSE);
			values.put("BLNREADONLY", Boolean.FALSE);
			values.put("DATCREATED", new Date());
			values.put("STRCREATED", "Migration v2.5.00");
			values.put("DATCHANGED", new Date());
			values.put("STRCHANGED", "Migration v2.5.00");
			values.put("INTVERSION", new Integer(1));

			if (armed) DataBaseHelper.getDbAccess().execute(new DbInsertStatement("T_MD_ENTITY_FIELD", values));
		}

		if (armed) {
			MetaDataServerProvider.getInstance().revalidate();
			EntityMetaDataVO eMeta = MetaDataServerProvider.getInstance().getEntity(sEntityName);
			EntityObjectMetaDbHelper dbHelper = new EntityObjectMetaDbHelper(DataBaseHelper.getDbAccess(), MetaDataServerProvider.getInstance());
			DataBaseHelper.getDbAccess().execute(SchemaUtils.create(dbHelper.getDbTable(eMeta)));
		}

		return sEntityName;
	}

	private List<DbStructureChange> teoForeignConstraints = new ArrayList<DbStructureChange>();
	private void createEntityObjectTables() {
		info("*** C R E A T E   E N T I T Y   O B J E C T   T A B L E S ***");

		EntityObjectMetaDbHelper dbHelper = new EntityObjectMetaDbHelper(DataBaseHelper.getDbAccess(), MetaDataServerProvider.getInstance());
		List<DbStructureChange> lstDropStructureChanges = SchemaUtils.drop(dbHelper.getSchema().values());
		List<DbStructureChange> lstCreateStructureChanges = SchemaUtils.create(dbHelper.getSchema().values());

		for (DbStructureChange sc : lstDropStructureChanges) {
			DbArtifact af = sc.getArtifact1();
			if (af instanceof DbSimpleView) {
				info("DROP " + ((DbSimpleView)af).getViewName());
				try {
					if (armed) DataBaseHelper.getDbAccess().execute(sc);
				} catch (Exception ex) {};
			}
		}

		String sFieldValueTableNameBase = "T_EO_"+sFieldValueEntityNameBase.toUpperCase();
		for (DbStructureChange sc : lstCreateStructureChanges) {
			DbArtifact af = sc.getArtifact2();
			if (af instanceof DbTable) {
				String sTableName = ((DbTable)af).getTableName();
				if (sTableName.startsWith("T_EO_") && !sTableName.startsWith(sFieldValueTableNameBase)) {

					info("CREATE " + sTableName);
					if (armed) DataBaseHelper.getDbAccess().execute(sc);
				}
			}/* else if (af instanceof DbConstraint.DbPrimaryKeyConstraint) {
				String sTableName = ((DbConstraint.DbPrimaryKeyConstraint)af).getTableName();
				if (sTableName.startsWith("T_EO_") && !sTableName.startsWith(sFieldValueTableNameBase)) {

					info("CREATE " + ((DbConstraint.DbPrimaryKeyConstraint)af).getConstraintName());
					if (armed) DataBaseHelper.getDbAccess().execute(sc);
				}
			} else if (af instanceof DbConstraint.DbForeignKeyConstraint) {
				String sTableName = ((DbConstraint.DbForeignKeyConstraint)af).getTableName();
				if (sTableName.startsWith("T_EO_") && !sTableName.startsWith(sFieldValueTableNameBase)) {
					teoForeignConstraints.add(sc);
				}
			} else if (af instanceof DbConstraint.DbUniqueConstraint) {
				String sTableName = ((DbConstraint.DbUniqueConstraint)af).getTableName();
				if (sTableName.startsWith("T_EO_") && !sTableName.startsWith(sFieldValueTableNameBase)) {

					info("CREATE " + ((DbConstraint.DbUniqueConstraint)af).getConstraintName());
					if (armed) DataBaseHelper.getDbAccess().execute(sc);
				}
			} else if (af instanceof DbSimpleView) {
				info("CREATE " + ((DbSimpleView)af).getViewName());
				if (armed) DataBaseHelper.getDbAccess().execute(sc);
			}*/
		}
	}

	private void migrateModuleObjects() throws DbException, ClassNotFoundException, CommonValidationException {
		info("*** M I G R A T E   M O D U L E   O B J E C T S ***");

		Map<Integer, String> mapOldAttributeIds = new HashMap<Integer, String>();
		{
			DbQuery<DbTuple> query = DataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
			DbFrom from = query.from("T_MD_ATTRIBUTE");
			from.alias("attr");
			List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();
			columns.add(getColumn(from, "INTID", DT_INTEGER));
			columns.add(getColumn(from, "STRATTRIBUTE", DT_STRING));
			query.multiselect(columns);
			List<DbTuple> result = DataBaseHelper.getDbAccess().executeQuery(query);
			for (DbTuple rs : result) {
				Integer id = rs.get("INTID", java.lang.Integer.class);
				String attribute = rs.get("STRATTRIBUTE", java.lang.String.class);
				mapOldAttributeIds.put(id, attribute);
			}
		}
		Map<Integer, Set<String>> mapAttributesInModule = new HashMap<Integer, Set<String>>();

		/**
		 * select old generic objects
		 */
		DbQuery<DbTuple> queryObjects = DataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom fromObjects = queryObjects.from("T_UD_GENERICOBJECT");
		fromObjects.alias("go");
		List<DbSelection<?>> columnsObjects = new ArrayList<DbSelection<?>>();
		columnsObjects.add(getColumn(fromObjects, "INTID", DT_INTEGER));
		columnsObjects.add(getColumn(fromObjects, "INTID_T_MD_MODULE", DT_INTEGER));
		columnsObjects.add(getColumn(fromObjects, "BLNDELETED", DT_BOOLEAN));
		columnsObjects.add(getColumn(fromObjects, "DATCREATED", DT_DATE));
		columnsObjects.add(getColumn(fromObjects, "STRCREATED", DT_STRING));
		columnsObjects.add(getColumn(fromObjects, "DATCHANGED", DT_DATE));
		columnsObjects.add(getColumn(fromObjects, "STRCHANGED", DT_STRING));
		columnsObjects.add(getColumn(fromObjects, "INTVERSION", DT_INTEGER));
		queryObjects.multiselect(columnsObjects);

		List<DbTuple> resultObjects = DataBaseHelper.getDbAccess().executeQuery(queryObjects);

		for (DbTuple rsObjects : resultObjects) {
			Integer iObjectId = rsObjects.get("INTID", java.lang.Integer.class);
			Integer iModuleId = rsObjects.get("INTID_T_MD_MODULE", java.lang.Integer.class);

			if (!mapAttributesInModule.containsKey(iModuleId)) {
				Set<String> attributes = getAttributesFromLayouts(iModuleId, MetaDataServerProvider.getInstance().getEntity(iModuleId.longValue()).getEntity());
				attributes.add("[status]");
				mapAttributesInModule.put(iModuleId, attributes);
			}

			EntityMetaDataVO eMeta = MetaDataServerProvider.getInstance().getEntity(iModuleId.longValue());

			/**
			 * Processing module object
			 */
			info("Processing module object " + iObjectId);

			Map<String, Object> values = new HashMap<String, Object>();
			put(rsObjects, "INTID", values, "INTID", DT_INTEGER);
			put(rsObjects, "BLNDELETED", values, "BLNNUCLOSDELETED", DT_BOOLEAN);
			put(rsObjects, "DATCREATED", values, "DATCREATED", DT_DATE);
			put(rsObjects, "STRCREATED", values, "STRCREATED", DT_STRING);
			put(rsObjects, "DATCHANGED", values, "DATCHANGED", DT_DATE);
			put(rsObjects, "STRCHANGED", values, "STRCHANGED", DT_STRING);
			put(rsObjects, "INTVERSION", values, "INTVERSION", DT_INTEGER);

			/**
			 * Select Attributes
			 */
			DbQuery<DbTuple> queryGOA = DataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
			DbFrom fromGOA = queryGOA.from("T_UD_GO_ATTRIBUTE");
			fromGOA.alias("goa");
			List<DbSelection<?>> columnsGOA = new ArrayList<DbSelection<?>>();
			columnsGOA.add(getColumn(fromGOA, "INTID", DT_INTEGER));
			columnsGOA.add(getColumn(fromGOA, "INTID_T_MD_ATTRIBUTE", DT_INTEGER));
			columnsGOA.add(getColumn(fromGOA, "INTID_T_DP_VALUE", DT_INTEGER));
			columnsGOA.add(getColumn(fromGOA, "INTID_EXTERNAL", DT_INTEGER));
			columnsGOA.add(getColumn(fromGOA, "STRVALUE", DT_STRING));
			queryGOA.multiselect(columnsGOA);
			queryGOA.where(DataBaseHelper.getDbAccess().getQueryBuilder().equal(
				fromGOA.baseColumn("INTID_T_UD_GENERICOBJECT", DT_INTEGER),
				DataBaseHelper.getDbAccess().getQueryBuilder().literal(iObjectId)));

			List<DbTuple> resultGOA = DataBaseHelper.getDbAccess().executeQuery(queryGOA);
			for (DbTuple rsGOA : resultGOA) {
				Integer iId = rsGOA.get("INTID", java.lang.Integer.class);
				Integer iAttrId = rsGOA.get("INTID_T_MD_ATTRIBUTE", java.lang.Integer.class);
				Integer iValueId = rsGOA.get("INTID_T_DP_VALUE", java.lang.Integer.class);
				Integer iExtId = rsGOA.get("INTID_EXTERNAL", java.lang.Integer.class);
				String sValue = rsGOA.get("STRVALUE", java.lang.String.class);

				EntityFieldMetaDataVO efMeta;
				try {
					final String sFieldName;
					if ("[status]".equals(mapOldAttributeIds.get(iAttrId))) {
						sFieldName = NuclosEOField.STATE.getMetaData().getField();
					} else if ("[system_id]".equals(mapOldAttributeIds.get(iAttrId))) {
						sFieldName = NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField();
					} else if ("[herkunft]".equals(mapOldAttributeIds.get(iAttrId))) {
						sFieldName = NuclosEOField.ORIGIN.getMetaData().getField();
					} else if ("[prozess]".equals(mapOldAttributeIds.get(iAttrId))) {
						sFieldName = NuclosEOField.PROCESS.getMetaData().getField();
					} else {
						sFieldName = mapOldAttributeIds.get(iAttrId);
					}
					efMeta = MetaDataServerProvider.getInstance().getEntityField(eMeta.getEntity(), sFieldName);
					if (efMeta.getCalcFunction() != null) {
						throw new Exception(efMeta.getField() + " is calcfunction");
					}
				} catch (Exception e) {
					info("Skipping attribute " + iId + " (" + e.getMessage() + ")");
					continue;
				}

				/**
				 * check if attribute is used
				 */
				if (mapAttributesInModule.get(iModuleId).contains(mapOldAttributeIds.get(iAttrId))) {
					if (iExtId != null) {
						put(iExtId, values, DalUtils.getDbIdFieldName(efMeta.getDbColumn()));
					} else if (iValueId != null) {
						put(iValueId, values, DalUtils.getDbIdFieldName(efMeta.getDbColumn()));
					} else if (sValue != null) {
						if (efMeta.getForeignEntity() != null) {
							/**
							 * Reference without id! Searching for id...
							 */
							warn("Entity field " + eMeta.getEntity() + "." + efMeta.getField() + " is foreign but has no external id! Value=\"" + sValue + "\"");

							EntityMetaDataVO eForeign = MetaDataServerProvider.getInstance().getEntity(efMeta.getForeignEntity());
							EntityFieldMetaDataVO efForeign = MetaDataServerProvider.getInstance().getEntityField(eForeign.getEntity(), efMeta.getForeignEntityField()==null?"name":efMeta.getForeignEntityField());

							warn("Try to search for \"" + sValue + "\" in " + eForeign.getDbEntity().replace("V_", "T_") + "." + efForeign.getDbColumn());

							DbQuery<DbTuple> queryForeign = DataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
							DbFrom fromForeign = queryForeign.from(eForeign.getDbEntity().replace("V_", "T_"));
							fromForeign.alias("f");
							queryForeign.select((DbSelection<DbTuple>) getColumn(fromForeign, "INTID", DT_INTEGER));
							queryForeign.where(DataBaseHelper.getDbAccess().getQueryBuilder().equal(
								fromForeign.baseColumn(efForeign.getDbColumn(), Class.forName(efForeign.getDataType())),
								DataBaseHelper.getDbAccess().getQueryBuilder().literal(sValue)));

							try {
								DbTuple rsForeign = DataBaseHelper.getDbAccess().executeQuerySingleResult(queryForeign);
								Integer iForeignId = rsForeign.get("INTID", java.lang.Integer.class);
								put(rsForeign, "INTID", values, DalUtils.getDbIdFieldName(efMeta.getDbColumn()), DT_INTEGER);
								warn("Record with id " + iForeignId + " found. Error fixed!");
							} catch (DbInvalidResultSizeException rsEx) {
								error("No or more than one record found! Skipping this generic object attribute!");
							}
						} else {
							Class<?> valueClass = Class.forName(efMeta.getDataType());
							Object oValue = CanonicalAttributeFormat.getInstance(valueClass).parse(sValue);
							put(oValue, values, efMeta.getDbColumn());
						}
					}
				}
			}

			/**
			 * Set not existing Boolean attributes to FALSE in T_EO-Table
			 */
			for (EntityFieldMetaDataVO efMeta : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(eMeta.getEntity()).values()) {
				if (Boolean.class.getName().equals(efMeta.getDataType()) && efMeta.getForeignEntity() == null && efMeta.getCalcFunction() == null) {
					if (!values.containsKey(efMeta.getDbColumn())) {
						put(Boolean.FALSE, values, efMeta.getDbColumn());
					}
				}
			}

			if(armed) DataBaseHelper.getDbAccess().execute(new DbInsertStatement(MetaDataServerProvider.getInstance().getEntity(iModuleId.longValue()).getDbEntity().replace("V_", "T_"), values));
		}


		/**
		 * install foreign constraints
		 */
		/*for (DbStructureChange sc : teoForeignConstraints) {
			DbArtifact af = sc.getArtifact2();
			info("CREATE " + ((DbConstraint.DbForeignKeyConstraint)af).getConstraintName());
			if (armed) DataBaseHelper.getDbAccess().execute(sc);
		}*/
	}

	private String replaceDeNovabitPackage(String sText) {
		String result = sText;
		boolean replace = false;
		if (result.contains("de.novabit")) {
			replace = true;
		}

		if (replace) {
			info("replacing package: \"de.novabit.nucleus\" --> \"org.nuclos\"");
			info("                   \"de.novabit.common\" --> \"org.nuclos.common2\"");
			info("                   \"de.novabit\" --> \"org.nuclos\"");
			info("               in: \"" + sText + "\"");

			result = result.replace("de.novabit.nucleus", "org.nuclos");
			result = result.replace("de.novabit.common", "org.nuclos.common2");
			result = result.replace("de.novabit", "org.nuclos");
		}

		return result;
	}

	private String createResource(String sText) {
		if (looksEmpty(sText)) {
			return sText;
		}
		info("create resource for \"" + sText + "\"");
		LocaleFacadeLocal localeFacade = ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
		return localeFacade.createResource(sText);
	}

	private void put(DbTuple rs, String columnSource, Map<String, Object> values, String columnTarget, Class<?> javaClass) {
		Object value = rs.get(columnSource, javaClass);
		put(value, values, columnTarget);
	}

	private void put(Object value, Map<String, Object> values, String columnTarget) {
		if (value != null) {
			values.put(columnTarget, value);
		}
	}

	private boolean isTrue(Boolean b) {
		return b != null && b.booleanValue();
	}

	private boolean isFalse(Boolean b) {
		return !isTrue(b);
	}

	private <T> DbColumnExpression<T> getColumn(DbFrom from, String columnName, Class<T> javaClass) {
		DbColumnExpression<T> result = from.baseColumn(columnName, javaClass);
		result.alias(columnName);

		return result;
	}

	private Set<String> getAttributesFromLayouts(Integer iModuleId, String entity) {
		Set<String> result = new HashSet<String>();

		DbQuery<DbTuple> subquery = DataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom subfrom = subquery.from("t_md_layoutusage");
		subfrom.alias("golayusage");
		List<DbSelection<?>> subcolumns = new ArrayList<DbSelection<?>>();
		subcolumns.add(getColumn(subfrom, "intid_t_md_layout", DT_INTEGER));
		subquery.distinct(true);
		subquery.multiselect(subcolumns);
		subquery.where(DataBaseHelper.getDbAccess().getQueryBuilder().equal(
			subfrom.baseColumn("strentity", DT_STRING),
			DataBaseHelper.getDbAccess().getQueryBuilder().literal(entity)));

		List<DbTuple> usages = DataBaseHelper.getDbAccess().executeQuery(subquery);

		for (DbTuple usage : usages) {
			Integer iLauyoutId = usage.get("intid_t_md_layout", java.lang.Integer.class);

			DbQuery<DbTuple> query = DataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
			DbFrom from = query.from("t_md_layout");
			from.alias("golayout");
			List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();
			columns.add(getColumn(from, "clblayoutml", DT_STRING));
			query.multiselect(columns);
			query.where(DataBaseHelper.getDbAccess().getQueryBuilder().equal(
				from.baseColumn("intid", DT_INTEGER),
				DataBaseHelper.getDbAccess().getQueryBuilder().literal(iLauyoutId)));

			List<DbTuple> layouts = DataBaseHelper.getDbAccess().executeQuery(query);

			for (DbTuple layout : layouts) {
				String sLayoutML = (String) layout.get("clblayoutml", DT_STRING);
				try {
					result.addAll(new LayoutMLParser().getCollectableFieldNames(new InputSource(new StringReader(sLayoutML))));
				}
				catch(LayoutMLException e) {
					throw new CommonFatalException(e);
				}
			}
		}
		return result;
	}
}
