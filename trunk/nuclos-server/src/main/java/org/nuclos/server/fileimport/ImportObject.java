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
package org.nuclos.server.fileimport;

import java.util.Map;

import org.nuclos.common.dal.vo.EntityObjectVO;

/**
 * Value object for an imported object.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class ImportObject {

	private String entityname;
	private ImportObjectKey key;
	private Map<String, Object> attributes;
	private Map<String, ImportObject> references;
	private EntityObjectVO valueObject;

	private int lineNumber;

	public ImportObject(String entityname, ImportObjectKey key, Map<String, Object> attributes, int lineNumber) {
		this.entityname = entityname;
		this.key = key;
		this.attributes = attributes;
		this.lineNumber = lineNumber;
	}

	public ImportObject(String entityname, ImportObjectKey key, Map<String, Object> attributes, int lineNumber, Map<String, ImportObject> references) {
		this(entityname, key, attributes, lineNumber);
		this.references = references;
	}

	public String getEntityname() {
		return this.entityname;
	}

	public ImportObjectKey getKey() {
		return this.key;
	}

	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public Map<String, ImportObject> getReferences() {
		return this.references;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ImportObject) {
			return key.equals(((ImportObject)obj).key);
		}
		return false;
	}

	public void setValueObject(EntityObjectVO valueObject) {
		this.valueObject = valueObject;
	}

	public EntityObjectVO getValueObject() {
		return valueObject;
	}

	public int getLineNumber() {
		return this.lineNumber;
	}

	@Override
	public String toString() {
		return (key.toString()).replace("{", "").replace("}", "");
	}

	public boolean isEmpty() {
		for (Object o : attributes.values()) {
			if (o != null) {
				return false;
			}
		}
		if (references != null) {
			for (ImportObject o : references.values()) {
				if (o != null && !o.isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}
}
