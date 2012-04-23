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

import org.apache.commons.collections.CollectionUtils;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dblayer.impl.util.PreparedStringBuilder;
import org.nuclos.server.dblayer.util.ForeignEntityFieldParser;

/**
 * Column mapping for referenced compound (one-)table expressions. This is used for nuclos 'stringified' 
 * references. The expression uses a CASE construct to return a NULL value if the foreign key is NULL.
 * 
 * @param <T> java type of the expression result, must be String for real compound 
 * 		expressions but maybe different for 'singleton' expressions.
 */
public class DbReferencedCompoundColumnExpression<T> extends DbExpression<T> {
	
	public DbReferencedCompoundColumnExpression(DbFrom from, EntityFieldMetaDataVO field, boolean setAlias) {
		super(from.getQuery().getBuilder(), (Class<T>) DalUtils.getDbType(field.getDataType()), 
				setAlias ? field.getDbColumn() : null, mkConcat(from, field));
		if (field.getForeignEntity() == null && field.getLookupEntity() == null) {
			throw new IllegalArgumentException();
		}
	}
	
	static final PreparedStringBuilder mkConcat(DbFrom from, EntityFieldMetaDataVO field) {
		final PreparedStringBuilder result = DbCompoundColumnExpression.mkConcat(from, field);
		final int size = CollectionUtils.size(new ForeignEntityFieldParser(field).iterator());
		if (size > 1) {
			final String foreignkeyColumn = field.getDbColumn().toUpperCase().replaceFirst("^(STRVALUE_|INTVALUE_|OBJVALUE_)", "INTID_");
			final String foreignKeyQualifiedName = DbColumnExpression.mkQualifiedColumnName(from.getAlias(), foreignkeyColumn, false).toString();
			return PreparedStringBuilder.concat("CASE WHEN (", foreignKeyQualifiedName, " IS NOT NULL) THEN ", result, " ELSE NULL END ");
		}
		else {
			return result;
		}
	}
}
