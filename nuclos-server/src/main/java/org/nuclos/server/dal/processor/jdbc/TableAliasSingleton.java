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
package org.nuclos.server.dal.processor.jdbc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.searchcondition.RefJoinCondition;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.dal.processor.IColumnToVOMapping;
import org.nuclos.server.dal.processor.IColumnWithMdToVOMapping;
import org.springframework.stereotype.Component;

/**
 * A singleton for defining the table join aliases for 'stringified' references.
 * <p>
 * This is part of the effort to deprecate all views in Nuclos.
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.2.01
 */
@Component
public class TableAliasSingleton {
	
	private static final Logger LOG = Logger.getLogger(TableAliasSingleton.class);
	
	private static TableAliasSingleton INSTANCE;
	
	//
	
	private final AtomicInteger AI = new AtomicInteger(0);
	
	/**
	 * The table alias for each pivot (subform, key) pair must be unique (and repeatable).
	 * Hence we store this class-wise.
	 * <p>
	 * (Pair (subform, key) -> tableAlias) mapping.
	 * </p><p>
	 * TODO:
	 * Is PivotInfo the right place to store this information?
	 * </p>
	 */
	private final Map<Pair<String,String>,String> TABLE_ALIASES = new ConcurrentHashMap<Pair<String,String>, String>();
	
	private final boolean debug;

	private TableAliasSingleton() {
		INSTANCE = this;
		debug = LOG.isDebugEnabled();
	}
	
	public static TableAliasSingleton getInstance() {
		return INSTANCE;
	}
	
	/**
	 * @deprecated Try to avoid this and use {@link #getAlias(IColumnWithMdToVOMapping)} instead.
	 */
	public String getAlias(IColumnToVOMapping<?> mapping) {
		final EntityFieldMetaDataVO meta = getMeta(mapping);
		return getAlias(meta);
	}
	
	public String getAlias(IColumnWithMdToVOMapping<?> mapping) {
		final EntityFieldMetaDataVO meta = mapping.getMeta();
		return getAlias(meta);
	}
	
	private EntityFieldMetaDataVO getMeta(IColumnToVOMapping<?> mapping) {
		final EntityFieldMetaDataVO meta;
		if (mapping instanceof IColumnWithMdToVOMapping) {
			meta = ((IColumnWithMdToVOMapping<?>) mapping).getMeta();
		}
		else {
			throw new IllegalArgumentException("Unexpected column mapping: " + mapping 
					+ " of " + mapping.getClass().getName());
		}
		return meta;
	}

	public String getAlias(EntityFieldMetaDataVO meta) {
		final String fentity = meta.getForeignEntity();
		if (fentity == null) {
			throw new IllegalArgumentException("Field " + meta + " is not a reference to a foreign entity");
		}
		final Pair<String,String> pair = newPair(fentity, meta.getField());
		String result = TABLE_ALIASES.get(pair);
		if (result == null) {
			final String alias = meta.getForeignEntity();
			result = StringUtils.makeSQLIdentifierFrom("a_", alias, meta.getField(), Integer.toString(AI.incrementAndGet()));
			if (debug) {
				final MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
				try {
					final EntityMetaDataVO entity = mdProv.getEntity(meta.getEntityId());
					LOG.debug("table alias for " + entity.getEntity() + "." + meta.getField() + " is " + result);
				}
				catch (NullPointerException e) {
					// ignore
				}
			}
			TABLE_ALIASES.put(pair, result);
		}
		return result;
	}
	
	private Pair<String,String> newPair(String fentity, String field) {
		if (fentity.equals(NuclosEntity.STATE.getEntityName())) {
			if (field.equalsIgnoreCase("nuclosState") || field.equalsIgnoreCase("nuclosStateNumber")
					|| field.equalsIgnoreCase("nuclosStateIcon")) {
				field = "nuclosState";
			}
		}
		return new Pair<String,String>(fentity, field);
	}
	
	public RefJoinCondition getRefJoinCondition(IColumnToVOMapping<?> mapping) {
		final EntityFieldMetaDataVO meta = getMeta(mapping);
		return getRefJoinCondition(meta);
	}

	public RefJoinCondition getRefJoinCondition(EntityFieldMetaDataVO meta) {
		final String tableAlias = getAlias(meta);
		return new RefJoinCondition(meta, tableAlias);
	}

	/**
	 * <p>
	 * TODO:
	 * This seems not 100% thread-safe at present!
	 * </p>
	 */
	public String getPivotTableAlias(String subform, String keyValue) {
		if (keyValue == null) throw new IllegalArgumentException();
		final Pair<String,String> pair = new Pair<String,String>(subform, keyValue);
		String result = TABLE_ALIASES.get(pair);
		if (result == null) {
			// allow string with only numbers in, and trailing '_' in oracle db
			result = StringUtils.makeSQLIdentifierFrom("p_", subform, keyValue, Integer.toString(AI.incrementAndGet()));
			if (debug) {
				LOG.debug("pivot table alias for " + subform + "." + keyValue + " is " + result);
			}
			TABLE_ALIASES.put(pair, result);
		}
		return result;
	}
	
}
