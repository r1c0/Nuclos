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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.collect.collectable.searchcondition.RefJoinCondition;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.dal.processor.IColumnToVOMapping;
import org.nuclos.server.dal.processor.IColumnWithMdToVOMapping;

/**
 * A singleton for defining the table join aliases for 'stringified' references.
 * <p>
 * This is part of the effort to deprecate all views in Nuclos.
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.2.01
 */
public class TableAliasSingleton {
	
	private static final Logger LOG = Logger.getLogger(TableAliasSingleton.class);
	
	private static final TableAliasSingleton INSTANCE = new TableAliasSingleton();
	
	/**
	 * Special tablealiases:
	 * (String) DB column -> (String) tablealias
	 */
	private static final Map<String,String> SPECIAL_TABLE_ALIASES;
	
	static {
		Map<String,String> map = new HashMap<String, String>();
		// Attention: Use lower case here! (tp) 
		map.put("strvalue_t_md_module_target", "t_md_entitymod_target");
		map.put("strvalue_t_md_module_source", "t_md_entitymod_source");
		map.put("strvalue_entity_target", "t_md_entity_target");
		map.put("strvalue_entity_source", "t_md_entity_source");
		SPECIAL_TABLE_ALIASES = Collections.unmodifiableMap(map);
	}
	
	//
	
	private final MetaDataProvider mdProv;
	
	private final boolean debug;

	private TableAliasSingleton() {
		mdProv = MetaDataServerProvider.getInstance();
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
			throw new IllegalArgumentException();
		}
		return meta;
	}

	public String getAlias(EntityFieldMetaDataVO meta) {
		final String dbColumn = meta.getDbColumn();
		String alias = SPECIAL_TABLE_ALIASES.get(dbColumn.toLowerCase());
		if (alias == null) {
			// default value
			alias = meta.getForeignEntity();
		}
		if (alias == null) {
			throw new IllegalArgumentException("Field " + meta + " is not a reference to a foreign entity");
		}
		if (debug) {
			final EntityMetaDataVO entity = mdProv.getEntity(meta.getEntityId());
			LOG.debug("table alias for " + entity.getEntity() + "." + meta.getField() + " is " + alias);
		}
		return alias;
	}
	
	public RefJoinCondition getRefJoinCondition(IColumnToVOMapping<?> mapping) {
		final EntityFieldMetaDataVO meta = getMeta(mapping);
		final String tableAlias = getAlias(meta);
		return new RefJoinCondition(meta, tableAlias);
	}

}
