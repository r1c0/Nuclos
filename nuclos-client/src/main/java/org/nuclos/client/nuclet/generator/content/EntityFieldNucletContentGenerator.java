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
package org.nuclos.client.nuclet.generator.content;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.nuclos.client.nuclet.generator.NucletGenerator;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.StringUtils;

public class EntityFieldNucletContentGenerator extends AbstractNucletContentGenerator {
	
	public static final String SHEET = "Attribute";
	
	public static final int COL_ENTITY_NAME = 0;			// STRING
	public static final int COL_NAME = 1;					// STRING
	public static final int COL_COLUMN = 2;					// STRING
	public static final int COL_LABEL = 3;					// STRING
	public static final int COL_ATTRBIUTE_GROUP = 4;		// STRING
	public static final int COL_DATATYPE = 5;				// STRING
	public static final int COL_DATASCALE = 6;				// INTEGER
	public static final int COL_DATAPRECISION = 7;			// INTEGER
	public static final int COL_READONLY = 8;				// BOOLEAN
	public static final int COL_NULLABLE = 9;				// BOOLEAN
	public static final int COL_UNIQUE = 10;				// BOOLEAN
	public static final int COL_LOGBOOK = 11;				// BOOLEAN
	public static final int COL_MODIFIABLE = 12;			// BOOLEAN
	public static final int COL_FOREIGN_ENTITY = 13;		// STRING
	public static final int COL_FOREIGN_ENTITY_FIELD = 14;	// STRING
	public static final int COL_SEARCHABLE = 15;			// BOOLEAN
	public static final int COL_DELETE_ON_CASCADE = 16;		// BOOLEAN
	public static final int COL_INDEXED = 17;				// BOOLEAN
	public static final int COL_LOOKUP_ENTITY = 18;			// STRING
	public static final int COL_LOOKUP_ENTITY_FIELD = 19;	// STRING
	public static final int COL_FORMAT_INPUT = 20;			// STRING
	public static final int COL_FORMAT_OUTPUT = 21;			// STRING
	public static final int COL_DEFAULT_VALUE = 22;			// STRING
	public static final int COL_CALC_FUNCTION = 23;			// STRING
	public static final int COLUMN_COUNT = 24;
	
	private final EntityNucletContentGenerator entityGenerator;
	private final EntityFieldGroupNucletContentGenerator entityFieldGroupGenerator;

	public EntityFieldNucletContentGenerator(
			NucletGenerator generator, 
			EntityNucletContentGenerator entityGenerator, 
			EntityFieldGroupNucletContentGenerator entityFieldGroupGenerator) {
		super(generator, NuclosEntity.ENTITYFIELD);
		this.entityGenerator = entityGenerator;
		this.entityFieldGroupGenerator = entityFieldGroupGenerator;
	}
	
	@Override
	public String getSheetName() {
		return SHEET;
	}

	@Override
	public void generateEntityObjects() {
		final XSSFSheet sheet = generator.getWorkbook().getSheet(SHEET);
		for (Row row : sheet) {
			if (row.getRowNum() <= 1)
				continue; // header row
			
			newEntityObject();
			storeField("order", row.getRowNum());
			
			boolean emptyRow = true;
			for (int i = 0; i < COLUMN_COUNT; i++) {
				final Cell cell = row.getCell(i); // could be null!
				try {
					switch (i) {
					case COL_ENTITY_NAME:
						storeField("entity", getStringValue(cell));
						storeFieldId("entity", entityGenerator.getIdByName(getStringValue(cell)));
						if (!StringUtils.looksEmpty(getStringValue(cell))) {
							emptyRow = false;
						}
						break;
					case COL_NAME:
						storeField("field", getStringValue(cell));
						break;
					case COL_COLUMN:
						storeField("dbfield", getStringValue(cell));
						break;
					case COL_LABEL:
						storeLocaleResource("localeresourcel", getStringValue(cell));
						storeLocaleResource("localeresourced", getStringValue(cell));
						break;
					case COL_ATTRBIUTE_GROUP:
						if (!StringUtils.looksEmpty(getStringValue(cell))) {
							storeField("entityfieldgroup", getStringValue(cell));
							storeFieldId("entityfieldgroup", entityFieldGroupGenerator.getIdByName(getStringValue(cell)));
						}
						break;
					case COL_DATATYPE:
						storeField("datatype", getStringValue(cell));
						break;
					case COL_DATASCALE:
						storeField("datascale", getIntegerValue(cell));
						break;
					case COL_DATAPRECISION:
						storeField("dataprecision", getIntegerValue(cell));
						break;
					case COL_READONLY:
						storeField("readonly", getBooleanValue(cell));
						break;
					case COL_NULLABLE:
						storeField("nullable", getBooleanValue(cell));
						break;
					case COL_UNIQUE:
						storeField("unique", getBooleanValue(cell));
						break;
					case COL_LOGBOOK:
						storeField("logbooktracking", getBooleanValue(cell));
						break;
					case COL_MODIFIABLE:
						storeField("modifiable", getBooleanValue(cell));
						break;
					case COL_FOREIGN_ENTITY:
						storeField("foreignentity", getStringValue(cell));
						break;
					case COL_FOREIGN_ENTITY_FIELD:
						storeField("foreignentityfield", getStringValue(cell));
						break;
					case COL_SEARCHABLE:
						storeField("searchable", getBooleanValue(cell));
						break;
					case COL_DELETE_ON_CASCADE:
						storeField("ondeletecascade", getBooleanValue(cell));
						break;
					case COL_INDEXED:
						storeField("indexed", getBooleanValue(cell));
						break;
					case COL_LOOKUP_ENTITY:
						storeField("lookupentity", getStringValue(cell));
						break;
					case COL_LOOKUP_ENTITY_FIELD:
						storeField("lookupentityfield", getStringValue(cell));
						break;
					case COL_FORMAT_INPUT:
						storeField("formatinput", getStringValue(cell));
						break;
					case COL_FORMAT_OUTPUT:
						storeField("formatoutput", getStringValue(cell));
						break;
					case COL_DEFAULT_VALUE:
						storeField("valuedefault", getStringValue(cell));
						break;
					case COL_CALC_FUNCTION:
						storeField("calcfunction", getStringValue(cell));
						break;
					}
				} catch (Exception ex) {
					error(cell, ex);
				}
			}
			
			if (!emptyRow)
				finishEntityObject();
		}
	}

	@Override
	protected boolean finishEntityObject() {
		boolean result = super.finishEntityObject();
		if (result) {
			// add static not nullable fields here
			fields.put("showmnemonic", false);
			fields.put("insertable", false);
		}
		return result;
	}
	
	

}
