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

import java.util.Iterator;

import org.nuclos.common.collection.Transformer;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;


/**
 * Entity object vo
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik.Stueker</a>
 * @version 01.00.00
 */
public class EntityObjectVO extends AbstractDalVOWithFields<Object> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EntityObjectVO() {
		
	}

	private String entity;
	
	// map for dependant child subform data
	private DependantMasterDataMap mpDependants = new DependantMasterDataMap();
	
	public String getEntity() {
		return entity;
	}
	public void setEntity(String entity) {
		this.entity = entity;
	}
	
	public void setDependants(DependantMasterDataMap mpDependants) {
		this.mpDependants = mpDependants;
	}
	
	public DependantMasterDataMap getDependants() {
		return this.mpDependants;
	}
	
	public EntityObjectVO copy() {
		EntityObjectVO vo = new EntityObjectVO();
		vo.initFields(1, 1);
		vo.setDependants(this.getDependants());
		vo.getFields().putAll(this.getFields());
		vo.getFieldIds().putAll(this.getFieldIds());
		return vo;
	}
	
	public static class GetId implements Transformer<EntityObjectVO, Object> {
		@Override
        public Object transform(EntityObjectVO mdvo) {
			return mdvo.getId();
		}
	}
	
	/**
	 * Transformer: gets the field with the given name, casted to the given type.
	 */
	public static class GetTypedField<T> implements Transformer<EntityObjectVO, T> {
		private final String sFieldName;
		private final Class<T> cls;

		public GetTypedField(String sFieldName, Class<T> cls) {
			this.sFieldName = sFieldName;
			this.cls = cls;
		}

		/**
		 * @param mdvo
		 * @throws ClassCastException if the value of the field doesn't have the given type.
		 */
		@Override
        public T transform(EntityObjectVO mdvo) {
			return mdvo.getField(this.sFieldName, this.cls);
		}
	}
	
	public static EntityObjectVO newObject(String sEntity) {
		EntityObjectVO vo = new EntityObjectVO();
		vo.initFields(1, 1);
		vo.flagNew();
		vo.setEntity(sEntity);
		
		return vo;
	}
	
	public String getDebugInfo() {
		final StringBuffer sb = new StringBuffer();
		sb.append("EntityObjectVO {");
		sb.append("Id: " + this.getId() + " - ");
		sb.append("Fields: ");
		for (Iterator<String> iter = this.getFields().keySet().iterator(); iter.hasNext();) {
			final String sFieldName = iter.next();
			sb.append("{" + sFieldName + ": " + this.getFields().get(sFieldName) + "}");
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		if (this.isFlagRemoved()) {
			sb.append(" - REMOVED");
		}
		sb.append("}");
		return sb.toString();
	}
	
}
