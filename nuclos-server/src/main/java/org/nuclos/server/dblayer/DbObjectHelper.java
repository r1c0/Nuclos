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
package org.nuclos.server.dblayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.statements.DbPlainStatement;
import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.statements.DbStructureChange.Type;
import org.nuclos.server.dblayer.structure.DbCallable;
import org.nuclos.server.dblayer.structure.DbCallableType;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbSimpleView.DbSimpleViewColumn;

public class DbObjectHelper {

	public static class DbObject{
		private String name;
		private DbObjectType type;
		public DbObject(String name, DbObjectType type) {
	        super();
	        this.name = name;
	        this.type = type;
        }
		public String getName() {
        	return name;
        }
		public DbObjectType getType() {
        	return type;
        }
		@Override
		public String toString() {
			final StringBuilder result = new StringBuilder();
			result.append(getClass().getName()).append("[");
			result.append("name=").append(name);
			result.append(", type=").append(type);
			result.append("]");
			return result.toString();
		}
	}

	public static enum DbObjectType{
		VIEW("view"),
		FUNCTION("function"),
		PROCEDURE("procedure"),
		PACKAGE("package"),
		PACKAGE_BODY("package body");

		private String name;

		private DbObjectType(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		public boolean checkName(String name) {
			return this.name.equals(name);
		}

		public static DbObjectType getByName(String name) {
			for (DbObjectType type : DbObjectType.values()) {
				if (type.checkName(name))
					return type;
			}
			return null;
		}
	}

	private final DbAccess dbAccess;

	public DbObjectHelper(DbAccess dbAccess) {
		this.dbAccess = dbAccess;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("access=").append(dbAccess);
		result.append("]");
		return result.toString();
	}

	/**
	 *
	 * @param type (could be null)
	 * @return
	 */
	public Map<DbObject, Pair<DbStatement, DbStatement>> getAllDbObjects(DbObjectType type) {
		Map<DbObject, Pair<DbStatement, DbStatement>> result = new HashMap<DbObject, Pair<DbStatement, DbStatement>>();

		DbQuery<DbTuple> queryObject = dbAccess.getQueryBuilder().createTupleQuery();
		DbFrom fromObject = queryObject.from("T_MD_DBOBJECT").alias("dbo");
		queryObject.multiselect(
			fromObject.baseColumn("STRDBOBJECT", String.class).alias("STRDBOBJECT"),
			fromObject.baseColumn("STRDBOBJECTTYPE", String.class).alias("STRDBOBJECTTYPE"));

		for (DbTuple object : dbAccess.executeQuery(queryObject)) {
			String name = object.get("STRDBOBJECT", String.class);
			DbObjectType currentType = DbObjectType.getByName(object.get("STRDBOBJECTTYPE", String.class));
			Pair<DbStatement, DbStatement> statement = this.getDbObject(name, type);
			if (statement != null)
				result.put(new DbObject(name, currentType), statement);
		}
		return result;
	}

	/**
	 *
	 * @param eMeta
	 * @return
	 */
	public Pair<DbStatement, DbStatement> getUserdefinedEntityView(EntityMetaDataVO eMeta) {
		if (NuclosEntity.getByName(eMeta.getEntity()) != null){
			// is system entitiy
			return null;
		}
		DbQueryBuilder builder = dbAccess.getQueryBuilder();
		DbQuery<Long> queryObject = builder.createQuery(Long.class);
		DbFrom fromObject = queryObject.from("T_MD_DBOBJECT").alias("dbo");
		queryObject.select(fromObject.baseColumn("INTID", Long.class));
		queryObject.where(builder.and(
			builder.equal(fromObject.baseColumn("STRDBOBJECT", String.class), eMeta.getDbEntity()),
			builder.equal(fromObject.baseColumn("STRDBOBJECTTYPE", String.class), DbObjectType.VIEW.getName())));
		List<Long> dbObjectIds = dbAccess.executeQuery(queryObject);

		if (dbObjectIds.size() > 1)
			throw new NuclosFatalException("DbObject is not unique.");

		if (dbObjectIds.size() != 0)
			return this.getDbObject(eMeta.getDbEntity(), DbObjectType.VIEW);

		return null;
	}

	/**
	 *
	 * @param eMeta
	 * @return
	 */
	public boolean hasUserdefinedEntityView(EntityMetaDataVO eMeta) {
		return getUserdefinedEntityView(eMeta)!=null;
	}

	/**
	 *
	 * @param source
	 * @param type
	 * @return
	 */
	public static Pair<DbStatement, DbStatement> getStatements(EntityObjectVO source, String type) {
		return getStatements(source.getField("dbobject", String.class), type, source.getField("source", String.class), source.getField("dropstatement", String.class));
	}

	/**
	 *
	 * @param name
	 * @param type
	 * @param source
	 * @param drop
	 * @return
	 */
	public static Pair<DbStatement, DbStatement> getStatements(String name, String type, String source, String drop) {
		DbStatement dropStatement;
		if (StringUtils.looksEmpty(drop)) {
			switch (DbObjectType.getByName(type)) {
			case VIEW:
				dropStatement = new DbStructureChange(Type.DROP, new DbSimpleView(null, name, new ArrayList<DbSimpleViewColumn>()));
				break;
			case FUNCTION:
				dropStatement = new DbStructureChange(Type.DROP, new DbCallable(DbCallableType.FUNCTION, name, null));
				break;
			case PROCEDURE:
				dropStatement = new DbStructureChange(Type.DROP, new DbCallable(DbCallableType.PROCEDURE, name, null));
				break;
			default:
				throw new UnsupportedOperationException("Could not determine drop statement, please define it yourself.");
			}

		} else {
			dropStatement = new DbPlainStatement(drop);
		}
		return new Pair<DbStatement, DbStatement>(new DbPlainStatement(source), dropStatement);
	}


	/**
	 *
	 * @param name
	 * @param type
	 * @return
	 */
	public Pair<DbStatement, DbStatement> getDbObject(String name, DbObjectType type) {
		DbQueryBuilder builder = dbAccess.getQueryBuilder();
		DbQuery<DbTuple> querySource = builder.createTupleQuery();
		DbFrom fromSource = querySource.from("T_MD_DBSOURCE").alias("dbsource");
		DbFrom fromObject = fromSource.join("T_MD_DBOBJECT", JoinType.RIGHT).on("STRDBOBJECT", "STRDBOBJECT", String.class).alias("dbobject");
		querySource.multiselect(
			fromSource.baseColumn("CLBSOURCE", String.class).alias("CLBSOURCE"),
			fromSource.baseColumn("CLBDROPSTATEMENT", String.class).alias("CLBDROPSTATEMENT"),
			fromSource.baseColumn("BLNACTIVE", Boolean.class).alias("BLNACTIVE"),
			fromObject.baseColumn("STRDBOBJECTTYPE", String.class).alias("STRDBOBJECTTYPE"));
		querySource.where(builder.and(
			builder.equal(fromSource.baseColumn("STRDBTYPE", String.class), this.dbAccess.getDbType().toString()),
			builder.equal(fromSource.baseColumn("STRDBOBJECT", String.class), name)));
		List<DbTuple> dbSources = dbAccess.executeQuery(querySource);

		if (dbSources.size() > 1)
			throw new NuclosFatalException("DbSource is not unique.");

		if (dbSources.size() != 0) {
			DbTuple dbSource = dbSources.get(0);
			if (dbSource.get("BLNACTIVE", Boolean.class)) {
				String sSource = dbSource.get("CLBSOURCE", String.class);
				String sDrop = dbSource.get("CLBDROPSTATEMENT", String.class);
				String sType = dbSource.get("STRDBOBJECTTYPE", String.class);

				if (type != null && !DbObjectType.getByName(sType).equals(type)) {
					return null;
				}

				return getStatements(name, sType, sSource, sDrop);
			}
		}

		return null;
	}

	/**
	 *
	 * @param name
	 * @return
	 */
	public static boolean isUsedAsCalculatedAttribute(String name, MetaDataProvider metaProvider) {
		if (name.startsWith("CA"))
			for (EntityMetaDataVO eMeta : metaProvider.getAllEntities())
				for (EntityFieldMetaDataVO efMeta : metaProvider.getAllEntityFieldsByEntity(eMeta.getEntity()).values())
					if (name.equalsIgnoreCase(efMeta.getCalcFunction()))
						return true;
		return false;
	}

}
