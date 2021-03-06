//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.dblayer.query;

import java.util.ArrayList;
import java.util.List;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dblayer.IFieldRef;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.jdbc.TableAliasSingleton;
import org.nuclos.server.dblayer.impl.standard.StandardSqlDBAccess;
import org.nuclos.server.dblayer.impl.util.PreparedStringBuilder;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType;
import org.nuclos.server.dblayer.util.ForeignEntityFieldParser;

/**
 * Column mapping for compound (one-)table expressions. This is used for nuclos 'stringified'
 * references.
 *
 * @author Thomas Pasch
 * @since Nuclos 3.2.01
 * @see org.nuclos.server.dal.processor.ColumnToRefFieldVOMapping#getDbColumn(DbFrom)
 *
 * @param <T> java type of the expression result, must be String for real compound
 * 		expressions but maybe different for 'singleton' expressions.
 */
public class DbCompoundColumnExpression<T> extends DbExpression<T> {

	public DbCompoundColumnExpression(DbFrom from, EntityFieldMetaDataVO field, boolean setAlias) {
		super(from.getQuery().getBuilder(), (Class<T>) DalUtils.getDbType(field.getDataType()),
				setAlias ? field.getDbColumn() : null, mkConcat(from, field));
		if (field.getForeignEntity() == null && field.getLookupEntity() == null) {
			throw new IllegalArgumentException();
		}
	}

	static final PreparedStringBuilder mkConcat(DbFrom from, EntityFieldMetaDataVO field) {
		if (field.getForeignEntity() == null && field.getLookupEntity() == null) {
			throw new IllegalArgumentException();
		}
		final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();
		final String tableAlias = TableAliasSingleton.getInstance().getAlias(field);
		final List<String> qualifiedNames = new ArrayList<String>();
		final List<String> toConcat = new ArrayList<String>();
		final StandardSqlDBAccess dbAccess = from.getQuery().getBuilder().getDBAccess();
		for (IFieldRef ref: new ForeignEntityFieldParser(field)) {
			if (ref.isConstant()) {
				toConcat.add("'" + ref.getContent() + "'");
			}
			else {
				final EntityFieldMetaDataVO mdField = mdProv.getEntityField(field.getForeignEntity() != null ? field.getForeignEntity() : field.getLookupEntity(), ref.getContent());
				final DbColumnType type = DalUtils.getDbColumnType(mdField).getGenericType() != DbGenericType.VARCHAR ? new DbColumnType(DbGenericType.VARCHAR, 255) : DalUtils.getDbColumnType(mdField);
				final String qualifiedName = DbColumnExpression.mkQualifiedColumnName(tableAlias, mdField.getDbColumn(), false).toString();
				qualifiedNames.add(qualifiedName);
				toConcat.add(dbAccess.getSqlForSubstituteNull(dbAccess.getSqlForCast(qualifiedName, type), "''"));
			}
		}
		if (toConcat.size() == 1) {
			return new PreparedStringBuilder(qualifiedNames.get(0));
		}
		else {
			return new PreparedStringBuilder(dbAccess.getSqlForConcat(toConcat));
		}
	}


}
