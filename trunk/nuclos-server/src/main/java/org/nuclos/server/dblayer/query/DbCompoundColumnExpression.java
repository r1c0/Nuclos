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
import java.util.Iterator;
import java.util.List;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.jdbc.TableAliasSingleton;
import org.nuclos.server.dblayer.impl.standard.StandardSqlDBAccess;
import org.nuclos.server.dblayer.impl.util.PreparedStringBuilder;
import org.nuclos.server.dblayer.util.ForeignEntityFieldParser;
import org.nuclos.server.dblayer.util.IFieldRef;

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
		if (field.getForeignEntity() == null) {
			throw new IllegalArgumentException();
		}
	}
	
	static final PreparedStringBuilder mkConcat(DbFrom from, EntityFieldMetaDataVO field) {
		if (field.getForeignEntity() == null) {
			throw new IllegalArgumentException();
		}
		final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();
		final String tableAlias = TableAliasSingleton.getInstance().getAlias(field);
		final List<String> toConcat = new ArrayList<String>();
		String firstQualifiedName = null; 
		for (IFieldRef ref: new ForeignEntityFieldParser(field)) {
			if (ref.isConstant()) {
				toConcat.add("'" + ref.getContent() + "'");
			}
			else {
				final EntityFieldMetaDataVO mdField = mdProv.getEntityField(field.getForeignEntity(), ref.getContent());
				final String qualifiedName = DbColumnExpression.mkQualifiedColumnName(
						tableAlias, mdField.getDbColumn(), false).toString();
				toConcat.add(qualifiedName);
				if (firstQualifiedName == null) {
					firstQualifiedName = qualifiedName;
				}
			}
		}
		if (firstQualifiedName == null) {
			throw new IllegalArgumentException();
		}
		if (toConcat.size() == 1) {
			// No 'CASE WHEN' if there is only one ref (and not a compound ref).
			return new PreparedStringBuilder(toConcat.get(0));
		}
		
		final StandardSqlDBAccess dbAccess = from.getQuery().getBuilder().getDBAccess();
		final Iterator<String> it = toConcat.iterator();
		String result = it.next();
		while (it.hasNext()) {
			result = dbAccess.getSqlForConcat(result, it.next());
		}
		return PreparedStringBuilder.concat("CASE WHEN (", firstQualifiedName, " IS NOT NULL) THEN ", 
				result, " ELSE NULL END ");
	}

}
