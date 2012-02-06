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
package org.nuclos.server.report;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.database.query.definition.Column;
import org.nuclos.common.database.query.definition.Constraint;
import org.nuclos.common.database.query.definition.ConstraintEmumerationType;
import org.nuclos.common.database.query.definition.DataType;
import org.nuclos.common.database.query.definition.QueryTable;
import org.nuclos.common.database.query.definition.ReferentialContraint;
import org.nuclos.common.database.query.definition.Schema;
import org.nuclos.common.database.query.definition.Table;
import org.nuclos.server.common.DatasourceCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.structure.AbstractDbArtifactVisitor;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbPrimaryKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;
import org.nuclos.server.dblayer.structure.DbNullable;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbTableType;
import org.nuclos.server.mbean.MBeanAgent;
import org.nuclos.server.mbean.SchemaCacheMBean;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.DynamicEntityVO;
import org.nuclos.server.report.valueobject.RecordGrantVO;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Caches the database schema used for reports and forms.<br>
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @author	<a href="mailto:Rostislav.Maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version 02.00.00
 */
@Component
public class SchemaCache implements SchemaCacheMBean {
	
   private static final Logger LOG = Logger.getLogger(SchemaCache.class);

   private static SchemaCache INSTANCE;
   
   //

   private Schema currentSchema = null;
   
   private ServerParameterProvider serverParameterProvider;

   SchemaCache() {
	   INSTANCE = this;
   }
   
   @PostConstruct
   final void init() {
	   this.currentSchema = getSchemaFromDB();
   }
   
   @Autowired
   void setServerParameterProvider(ServerParameterProvider serverParameterProvider) {
	   this.serverParameterProvider = serverParameterProvider;
   }

   public static SchemaCache getInstance() {
      return INSTANCE;
   }

   public Schema getCurrentSchema() {
      return this.currentSchema;
   }

	private Schema getSchemaFromDB() {
   	// TODO: wie getTablesAndViewsFromDB ...
		//log.info("Initializing SchemaCache");

   	Predicate<String> predicate = null;
   	String filter = serverParameterProvider.getValue(ParameterProvider.KEY_DATASOURCE_TABLE_FILTER);
   	if (filter != null) {
   		predicate = PredicateUtils.wildcardFilterList(filter);
   	}

   	Schema result = getTablesAndViewsFromDB(predicate);

      // @TODO GOREF: "Dynamic tables": For legacy modules their v_ud_go_xxx view was added
      // and marked as "dynamic"
      // DataBaseHelper.getDynamicTables().addToSchema(result);

   	addToSchema(result);

      //log.info("Finished initializing SchemaCache.");
      return result;
   }

   public Schema getCompleteCurrentSchema() {
      return this.getTablesAndViewsFromDB(null);
   }

	private Schema getTablesAndViewsFromDB(Predicate<String> namePredicate) {
      Schema schema = new Schema();

      for (DbTableType tableType : DbTableType.TABLE_AND_VIEW) {
      	for (String tableName : DataBaseHelper.getDbAccess().getTableNames(tableType)) {
      		if (namePredicate != null && !namePredicate.evaluate(tableName))
      			continue;
	         Table table = new Table(schema, tableName);
	         table.setType(tableType.toString());
	         table.setEntityName(getEntityName(tableName));
	         schema.addTable(table);
      	}
      }

      return schema;
   }

	private String getEntityName(String tableName) {
		for (EntityMetaDataVO eMeta : MetaDataServerProvider.getInstance().getAllEntities()) {
			if(eMeta.getDbEntity().substring(1).equalsIgnoreCase(tableName.substring(1))) {
				// ignore T or V
				return eMeta.getEntity();
			}
		}
		return null;
	}

   @Override
public synchronized void invalidate() {
      LOG.debug("Invalidating SchemaCache");
      this.currentSchema = null;
      this.currentSchema = getSchemaFromDB();
   }

   // ***** static **********************************************************************

   public static synchronized void fillTableColumnsAndConstraints(final Table table){
      getColumns(table);
      getConstraints(table);

      //Table x = (Table)table.clone();
      //SchemaCache.getInstance().addTable((Table)table.clone());
   }

   public static synchronized void getColumns(final Table table) {
      if (table.isQuery()) {
         new QueryTable().setAllQueryColumns(table);
      }
      else {
         readColumnsIntoTable(table);
      }
   }

   private static synchronized void getConstraints(final Table table) {
   	DbTable tableMetaData = DataBaseHelper.getDbAccess().getTableMetaData(table.getName());
   	if (tableMetaData != null) {
   		DbArtifact.acceptAll(tableMetaData.getTableArtifacts(DbConstraint.class), new AbstractDbArtifactVisitor<Void>() {
   			@Override
   			public Void visitPrimaryKeyConstraint(DbPrimaryKeyConstraint constraint) {
   				registerConstraint(constraint, new Constraint(table, constraint.getConstraintName(), ConstraintEmumerationType.PRIMARY_KEY));
   				return null;
   			}
   			@Override
   			public Void visitForeignKeyConstraint(DbForeignKeyConstraint constraint) throws DbException {
               final String sReferentialOwner = null; // TODO_AUTOSYNC: add schema to db artifacts
               registerConstraint(constraint, new ReferentialContraint(table, constraint.getConstraintName(),
               	sReferentialOwner, constraint.getReferencedConstraintName(), ConstraintEmumerationType.FOREIGN_KEY));
   				return null;
   			}
   			@Override
   			public Void visitUniqueConstraint(DbUniqueConstraint constraint) {
   				registerConstraint(constraint, new Constraint(table, constraint.getConstraintName(), ConstraintEmumerationType.UNIQUE));
   				return null;
   			}
   			private void registerConstraint(DbConstraint dbConstraint, Constraint constraint) {
   				for (String columnName : dbConstraint.getColumnNames()) {
   					Column column = table.getColumn(columnName);
   					if (column == null) {
   						throw new NuclosFatalException("SchemaCache does not find column " + columnName + " for constraint " + constraint.getName());
   					}
   					constraint.addColumn(column);
   				}
   				table.addConstraint(constraint);
   			}
   		});
      }
   }

   private static void readColumnsIntoTable(final Table table) {
   	// TODO_AUTOSYNC: Other schema

   	DbTable dbTable = DataBaseHelper.getDbAccess().getTableMetaData(table.getName());
   	for (DbColumn dbColumn : dbTable.getTableArtifacts(DbColumn.class)) {
   		final String name = dbColumn.getColumnName();
   		final DbColumnType columnType = dbColumn.getColumnType();
   		DataType type = null;
   		if (columnType.getGenericType() != null) {
   			switch (columnType.getGenericType()) {
   			case NUMERIC:
   				type = DataType.NUMERIC;
   				if (columnType.getPrecision() != null) { // could be null (view)
	   				if (columnType.getPrecision() <= 9 && columnType.getScale() == 0)
	   					type = DataType.INTEGER;
   				}
   				break;
   			case DATE:
   				type = DataType.DATE;
   				break;
   			case DATETIME:
   				type = DataType.TIMESTAMP;
   				break;
   			}
   		}
         final Column column = new Column(table, name,
         	type != null ? type : DataType.VARCHAR,
         	columnType.getLength() != null ? columnType.getLength() : 0,
         	columnType.getPrecision() != null ? columnType.getPrecision() : -1,
         	columnType.getScale() != null ? columnType.getScale() : -1,
      		dbColumn.getNullable() == DbNullable.NULL);
         column.setComment(dbColumn.getComment());
         table.addColumn(column);
   	}
   }

   public static void addToSchema(Schema schema) {
		//DatasourceLocalHome datasourceHome = (DatasourceLocalHome) ServiceLocator.getInstance().getLocalHome(DatasourceLocalHome.JNDI_NAME);
		//try {
			//for (Iterator i = datasourceHome.findAll().iterator(); i.hasNext();) {
			//@todo is it the right way to ignore user permissions here?
			for (DatasourceVO  voDatasource : DatasourceCache.getInstance().getAllDatasources()) {
				//DatasourceLocal datasource = (DatasourceLocal) i.next();
				//DatasourceVO voDatasource = datasource.getValueObject();
//				if (voDatasource.getPermission() != DatasourceVO.PERMISSION_NONE) {
				String sName = voDatasource.getName();
				String sViewName = null;
				String sComment = voDatasource.getDescription();
				Table table = new Table(schema, sName, sViewName);
				table.setQuery(true);
				table.setType(QueryTable.QUERY_TYPE_REPORT);
				table.setDatasourceXML(voDatasource.getSource());
				table.setComment(sComment);
				schema.addTable(table);
//				}
			}
	  /*}
		catch (FinderException e) {
			throw new NuclosFatalException(e);
		}*/
			for (DynamicEntityVO dynamicEntityVO : DatasourceCache.getInstance().getAllDynamicEntities()) {
				String sName = dynamicEntityVO.getName();
				String sViewName = null;
				String sComment = dynamicEntityVO.getDescription();
				Table table = new Table(schema, sName, sViewName);
				table.setQuery(true);
				table.setType(QueryTable.QUERY_TYPE_DYNAMIC_ENTITY);
				table.setDatasourceXML(dynamicEntityVO.getSource());
				table.setComment(sComment);
				schema.addTable(table);
			}

			for (ValuelistProviderVO valuelistProviderVO : DatasourceCache.getInstance().getAllValuelistProvider()) {
				String sName = valuelistProviderVO.getName();
				String sViewName = null;
				String sComment = valuelistProviderVO.getDescription();
				Table table = new Table(schema, sName, sViewName);
				table.setQuery(true);
				table.setType(QueryTable.QUERY_TYPE_VALUELIST_PROVIDER);
				table.setDatasourceXML(valuelistProviderVO.getSource());
				table.setComment(sComment);
				schema.addTable(table);
			}

			for (RecordGrantVO recordGrantVO : DatasourceCache.getInstance().getAllRecordGrant()) {
				String sName = recordGrantVO.getName();
				String sViewName = null;
				String sComment = recordGrantVO.getDescription();
				Table table = new Table(schema, sName, sViewName);
				table.setQuery(true);
				table.setType(QueryTable.QUERY_TYPE_RECORDGRANT);
				table.setDatasourceXML(recordGrantVO.getSource());
				table.setComment(sComment);
				schema.addTable(table);
			}
	}
}	// class SchemaCache
