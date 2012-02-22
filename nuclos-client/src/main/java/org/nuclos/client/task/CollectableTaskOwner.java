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
package org.nuclos.client.task;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.AbstractCollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.DefaultCollectable;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;


public class CollectableTaskOwner extends DefaultCollectable {
	
	public static final String TASKOWNER_ENTITY = "TaskOwner";
	public static final String FIELDNAME_TASK = "task";
	public static final String FIELDNAME_USER = "user";
	
	public CollectableTaskOwner() {
		super(clcte);
	}
	
	public static class Entity extends AbstractCollectableEntity {
		private Entity() {
			super("taskowner", "Bearbeiter");
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_TASK, String.class, 
					getSpringLocaleDelegate().getMessage("CollectableTaskOwner.1","Aufgabe"),
					getSpringLocaleDelegate().getMessage("CollectableTaskOwner.2","Aufgabe"), null, null, false, CollectableField.TYPE_VALUEIDFIELD, "__task",
					CollectableValueIdField.NULL, null, null, TASKOWNER_ENTITY));

			this.addCollectableEntityField(new TaskOwnerCollectableEntityField(FIELDNAME_USER, String.class, 
					getSpringLocaleDelegate().getMessage("CollectableTaskOwner.4","Zust\u00e4ndig"), 
					getSpringLocaleDelegate().getMessage("CollectableTaskOwner.3","Bearbeiter der Aufgabe"), null, false, CollectableField.TYPE_VALUEIDFIELD, NuclosEntity.USER.getEntityName(),
					CollectableValueIdField.NULL, null, null, TASKOWNER_ENTITY));
		}
	}
	
	public static final CollectableEntity clcte = new Entity();

	private static class TaskOwnerCollectableEntityField extends DefaultCollectableEntityField {
		
		public TaskOwnerCollectableEntityField(String sName, Class<?> cls, String sLabel, String sDescription, Integer iMaxLength,
				boolean bNullable, int iFieldType, String sReferencedEntityName, CollectableField clctfDefault, String sFormatInput, 
				String sFormatOutput, String entityName) {
			super(sName, cls, sLabel, sDescription, iMaxLength, null, bNullable, iFieldType, sReferencedEntityName, clctfDefault, 
					sFormatInput, sFormatOutput, entityName);
		}
		
		@Override
		public String getReferencedEntityFieldName() {
			return "${lastname}, ${firstname}";
		}
	}

}
