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
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;

public class EntityFieldGroupNucletContentGenerator extends AbstractNucletContentGenerator {
	
	public static final String SHEET = "AttributeGroups";
	
	public static final int COL_NAME = 0;			// STRING
	public static final int COLUMN_COUNT = 1;
	
	public static final String FIELD_NAME = "name";

	public EntityFieldGroupNucletContentGenerator(NucletGenerator generator) {
		super(generator, NuclosEntity.ENTITYFIELDGROUP);
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
						storeField(FIELD_NAME, getStringValue(cell));
						if (!StringUtils.looksEmpty(getStringValue(cell))) {
							emptyRow = false;
						}
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

	public Long getIdByName(String name) throws NuclosBusinessException {
		
		for (EntityObjectVO eo : getResult()) {
			if (LangUtils.equals(eo.getField(FIELD_NAME), name)) {
				return eo.getId();
			}
		}
		
		throw new NuclosBusinessException(String.format("Attribute group with name \"%s\" not found!", name));
	}

}
