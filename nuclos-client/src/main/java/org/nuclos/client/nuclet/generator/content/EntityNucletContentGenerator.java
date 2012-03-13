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

import org.apache.commons.httpclient.util.LangUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.nuclos.client.nuclet.generator.NucletGenerator;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.StringUtils;

public class EntityNucletContentGenerator extends AbstractNucletContentGenerator {
	
	public static final String SHEET = "Entity";

	public static final int COL_NAME = 0;						// STRING
	public static final int COL_TABLE_NAME = 1;					// STRING
	public static final int COL_LABEL = 2;						// STRING
	public static final int COL_MENU = 3;						// STRING
	public static final int COL_MENUSHORTCUT = 4;				// STRING
	public static final int COL_ACCELERATOR = 5;				// STRING
	public static final int COL_ACCELERATOR_MODIFIER = 6;		// INTEGER
	public static final int COL_SEARCHABLE = 7;					// BOOLEAN
	public static final int COL_CACHEABLE = 8;					// BOOLEAN
	public static final int COL_LOGBOOK = 9;					// BOOLEAN
	public static final int COL_EDITABLE = 10;					// BOOLEAN
	public static final int COL_STATEMODEL = 11;				// BOOLEAN
	public static final int COL_SYSTEM_ID_PREFIX = 12;			// STRING
	public static final int COLUMN_COUNT = 13;
	
	public static final String FIELD_ENTITY = "entity";

	public EntityNucletContentGenerator(NucletGenerator generator) {
		super(generator, NuclosEntity.ENTITY);
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
			
			boolean emptyRow = true;
			for (int i = 0; i < COLUMN_COUNT; i++) {
				final Cell cell = row.getCell(i); // could be null!
				try {
					switch (i) {
					case COL_NAME:
						storeField(FIELD_ENTITY, getStringValue(cell));
						if (!StringUtils.looksEmpty(getStringValue(cell))) {
							emptyRow = false;
						}
						break;
					case COL_TABLE_NAME:
						storeField("dbentity", getStringValue(cell));
						break;
					case COL_LABEL:
						storeLocaleResource("localeresourcel", getStringValue(cell));
						storeLocaleResource("localeresourced", getStringValue(cell));
						break;
					case COL_MENU:
						storeLocaleResource("localeresourcem", getStringValue(cell));
						break;
					case COL_MENUSHORTCUT:
						storeField("menushortcut", getStringValue(cell));
						break;
					case COL_ACCELERATOR:
						storeField("accelerator", getStringValue(cell));
						break;
					case COL_ACCELERATOR_MODIFIER:
						storeField("acceleratormodifier", getIntegerValue(cell));
						break;
					case COL_SEARCHABLE:
						storeField("searchable", getBooleanValue(cell));
						break;
					case COL_CACHEABLE:
						storeField("cacheable", getBooleanValue(cell));
						break;
					case COL_LOGBOOK:
						storeField("logbooktracking", getBooleanValue(cell));
						break;
					case COL_EDITABLE:
						storeField("editable", getBooleanValue(cell));
						break;
					case COL_STATEMODEL:
						storeField("usessatemodel", getBooleanValue(cell));
						break;
					case COL_SYSTEM_ID_PREFIX:
						storeField("systemidprefix", getStringValue(cell));
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
			fields.put("fieldvalueentity", false);
			fields.put("treerelation", false);
			fields.put("treegroup", false);		
			fields.put("importexport", false);		
		}
		return result;
	}
	
	public Long getIdByName(String entity) throws NuclosBusinessException {
		
		for (EntityObjectVO eo : getResult()) {
			if (LangUtils.equals(eo.getField(FIELD_ENTITY), entity)) {
				return eo.getId();
			}
		}
		
		throw new NuclosBusinessException(String.format("Entity with name \"%s\" not found!", entity));
	}

}
