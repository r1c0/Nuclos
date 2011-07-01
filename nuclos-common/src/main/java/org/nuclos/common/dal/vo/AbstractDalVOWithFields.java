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
package org.nuclos.common.dal.vo;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class AbstractDalVOWithFields extends AbstractDalVOWithVersion {

	private static final Logger LOG = Logger.getLogger(AbstractDalVOWithFields.class);

	private Map<String, Long> mapFieldId;
	
	private Map<String, Object> mapField;
	
	@Override
	public void initFields(int maxFieldCount, int maxFieldIdCount) {
		mapField = new HashMap<String, Object>(maxFieldCount);
		mapFieldId = new HashMap<String, Long>(maxFieldIdCount);
	}
	
	@Override
	public boolean hasFields() {
		return true;
	}

	@Override
	public Map<String, Long> getFieldIds() {
		if (this.mapFieldId == null) {
			throw new IllegalArgumentException("init fields first.");
		}
		return this.mapFieldId;
	}

	@Override
	public Map<String, Object> getFields() {
		if (this.mapField == null) {
			throw new IllegalArgumentException("init fields first.");
		}
		return this.mapField;
	}

	public Long getFieldId(String sFieldName) {
		return this.getFieldIds().get(sFieldName);
	}

	public <T> T getField(String sFieldName, Class<T> cls) {
		final Object value = getFields().get(sFieldName);
		try {
			return cls.cast(value);
		}
		catch (ClassCastException e) {
			LOG.error("On " + this + " field " + sFieldName + " value " + value + " expected type " + cls, e);
			throw e;
		}
	}
	
}
