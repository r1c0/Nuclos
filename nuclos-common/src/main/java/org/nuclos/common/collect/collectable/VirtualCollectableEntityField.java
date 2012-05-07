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
package org.nuclos.common.collect.collectable;

public class VirtualCollectableEntityField extends AbstractCollectableEntityField {
	
	private final String sEntityName;
	
	private final String sFieldName;
	
	private final String sReferencedEntityName;
	
	public VirtualCollectableEntityField(String entity, String field, String refEntity) {
		sEntityName = entity;
		sFieldName = field;
		sReferencedEntityName = refEntity;
	}

	@Override
	public String getDescription() {
		return "Verweis auf \u00fcbergeordneten Datensatz";
	}

	@Override
	public int getFieldType() {
		return CollectableEntityField.TYPE_VALUEIDFIELD;
	}

	@Override
	public Class<?> getJavaClass() {
		return String.class;
	}

	@Override
	public String getLabel() {
		return "Referenz auf Vaterobjekt";
	}

	@Override
	public Integer getMaxLength() {
		return null;
	}
	
	@Override
	public Integer getPrecision() {
		return null;
	}

	@Override
	public String getName() {
		return sFieldName;
	}
	
	@Override
	public String getFormatInput() {
		return null;
	}
	
	@Override
	public String getFormatOutput() {
		return null;
	}

	@Override
	public String getReferencedEntityName() {
		return sReferencedEntityName;
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public boolean isReferencing() {
		return true;
	}

	@Override
	public CollectableEntity getCollectableEntity() {
		return null;
	}

	@Override
	public void setCollectableEntity(CollectableEntity clent) {
	}

	@Override
	public String getEntityName() {
		return sEntityName;
	}

	@Override
	public String getDefaultComponentType() {
		return null;
	}
}
