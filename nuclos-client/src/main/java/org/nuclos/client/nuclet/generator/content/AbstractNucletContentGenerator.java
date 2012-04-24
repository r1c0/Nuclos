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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.nuclos.client.nuclet.generator.NucletGenerator;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.nuclet.AbstractNucletContentConstants;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.StringUtils;

public abstract class AbstractNucletContentGenerator implements AbstractNucletContentConstants {

	protected final NucletGenerator generator;
	protected final NuclosEntity entity;
	
	private final List<EntityObjectVO> result;
	
	private boolean errorOccurred;
	
	protected EntityObjectVO eo;
	protected Map<String, Object> fields;
	protected Map<String, Long> fieldIds;
	protected Map<LocaleInfo, Map<String, String>> localeResources;

	public AbstractNucletContentGenerator(NucletGenerator generator, NuclosEntity entity) {
		super();
		this.generator = generator;
		this.entity = entity;
		this.result = new ArrayList<EntityObjectVO>();
	}
	
	protected void newEntityObject() {
		eo = new EntityObjectVO();
		eo.initFields(30, 5);
		
		fields = eo.getFields();
		fieldIds = eo.getFieldIds();
		
		localeResources = new HashMap<LocaleInfo, Map<String, String>>();
		fields.put(LOCALE_RESOURCE_MAPPING_FIELD_NAME, localeResources);
		
		errorOccurred = false;
	}
	
	protected boolean finishEntityObject() {
		if (errorOccurred) {
			return false;
		} else {
			fieldIds.put("nuclet", generator.getNucletId());
			
			eo.setEntity(entity.getEntityName());
			eo.setId(generator.getNextId());
			
			setMetaFields(eo);
			
			result.add(eo);
			return true;
		}
	}
	
	public NuclosEntity getEntity() {
		return entity;
	}
	
	public void setMetaFields(EntityObjectVO eo) {
		generator.setMetaFields(eo);
	}
	
	protected void storeField(String field, Object value) {
		fields.put(field, value);
	}
	
	protected void storeFieldId(String field, Long id) {
		fieldIds.put(field, id);
	}
	
	protected String storeLocaleResource(String resourceField, String text) {
		if (StringUtils.looksEmpty(text)) {
			return null;
		}
		
		String result = "RNucGen" + generator.getNextId();
		
		for (LocaleInfo locale : generator.getLocales()) {
			if (!localeResources.containsKey(locale)) {
				localeResources.put(locale, new HashMap<String, String>());
			}
			localeResources.get(locale).put(resourceField, text);
			generator.addLocaleResource(locale, result, text);
		}
		
		storeField(resourceField, result);
		
		return result;
	}
	
	protected void error(String error) {
		errorOccurred = true;
		generator.error(String.format("Error on sheet %s: %s", getSheetName(), error));
	}
	
	protected void error(Cell cell, Exception ex) {
		error(String.format("[Row %s, Column %s], %s", cell==null?"null":cell.getRowIndex()+1, cell==null?"null":cell.getColumnIndex()+1, ex.getMessage()));
	}
	
	protected void warning(String warning) {
		generator.warning(String.format("Warning on sheet %s: %s", getSheetName(), warning));
	}
	
	public List<EntityObjectVO> getResult() {
		return result;
	}
	
	public String getStringValue(Cell cell) {
		return cell==null ? null : cell.getStringCellValue();
	}
	
	public Integer getIntegerValue(Cell cell) {
		return cell==null ? null : new Double(cell.getNumericCellValue()).intValue();
	}
	
	public boolean getBooleanValue(Cell cell) {
		return StringUtils.equalsIgnoreCase(cell==null?"n":cell.getStringCellValue(), "y");
	}
	
	public abstract void generateEntityObjects();
	
	public abstract String getSheetName();
}
