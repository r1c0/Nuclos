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

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;

public class LocaleUtils {
	public final static String FIELD_LABEL = "name";
	public final static String FIELD_MENUPATH = "menupath";
	public final static String FIELD_LABELPLURAL = "labelplural";
	public final static String FIELD_DESCRIPTION = "description";
	public final static String FIELD_TREEVIEW = "treeview";
	public final static String FIELD_TREEVIEWDESCRIPTION = "treeviewdescription";
	
	public final static String ENTITYFIELD_LABEL = "label";
	public final static String ENTITYFIELD_DESCRIPTION = FIELD_DESCRIPTION;

	private static String getColumnByFieldName(String sFieldName) {
		if (sFieldName.equals(FIELD_LABEL) || sFieldName.equals(ENTITYFIELD_LABEL))
			return "STR_LOCALERESOURCE_L";
		else if (sFieldName.equals(FIELD_LABELPLURAL))
			return "STR_LOCALERESOURCE_LP";
		else if (sFieldName.equals(FIELD_MENUPATH))
			return "STR_LOCALERESOURCE_M";
		else if (sFieldName.equals(FIELD_TREEVIEW))
			return "STR_LOCALERESOURCE_TW";
		else if (sFieldName.equals(FIELD_TREEVIEWDESCRIPTION))
			return "STR_LOCALERESOURCE_TT";
		else if (sFieldName.equals(FIELD_DESCRIPTION))
			return "STR_LOCALERESOURCE_D";
		else
			return null;
	}
	
	public static void setResourceIdForField(String sEntityTable, Integer iId, String sFieldName, String sResourceId) {
		String sColumn = getColumnByFieldName(sFieldName);
		if (sColumn != null) {
			DataBaseHelper.getInstance().execute(DbStatementUtils
				.updateValues(sEntityTable, sColumn, DbNull.escapeNull(sResourceId, String.class))
				.where("INTID", iId));
		}
	}
	
	public static String getResourceIdForField(String sEntityTable, Integer iId, String sFieldName) {
		String sColumn = getColumnByFieldName(sFieldName);
		DbQueryBuilder builder = DataBaseHelper.getInstance().getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		DbFrom t = query.from(sEntityTable).alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn(sColumn, String.class));
		query.where(builder.equal(t.baseColumn("INTID", Integer.class), iId));
		return CollectionUtils.getFirst(DataBaseHelper.getInstance().getDbAccess().executeQuery(query));
	}

}
