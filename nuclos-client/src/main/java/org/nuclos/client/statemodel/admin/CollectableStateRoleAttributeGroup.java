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
package org.nuclos.client.statemodel.admin;

import org.nuclos.common.collect.collectable.AbstractCollectable;
import org.nuclos.common.collect.collectable.AbstractCollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.CommonLocaleDelegate;

import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.server.statemodel.valueobject.AttributegroupPermissionVO;

/**
 * User role for a state (for state dependent user rights).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class CollectableStateRoleAttributeGroup extends AbstractCollectable {
	public static final String FIELDNAME_ROLE = "role";
	public static final String FIELDNAME_ATTRIBUTEGROUP = "attributegroup";
	public static final String FIELDNAME_WRITEABLE = "writeable";

	public static class Entity extends AbstractCollectableEntity {
		public Entity() {
			super("stateroleattributegroup", CommonLocaleDelegate.getMessage("CollectableStateRoleAttributeGroup.3","Attributgruppe f\u00fcr statusabh\u00e4ngige Rechte"));

			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_ROLE, String.class, CommonLocaleDelegate.getMessage("CollectableStateRoleAttributeGroup.4","Benutzergruppe"),
				CommonLocaleDelegate.getMessage("CollectableStateRoleAttributeGroup.9","\u00dcbergeordnete Benutzergruppe (Rolle)"), null, null, false, CollectableField.TYPE_VALUEIDFIELD, "staterole",
					CollectableValueIdField.NULL, null, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_ATTRIBUTEGROUP,
					String.class, CommonLocaleDelegate.getMessage("CollectableStateRoleAttributeGroup.1","Attributgruppe"), CommonLocaleDelegate.getMessage("CollectableStateRoleAttributeGroup.2","Attributgruppe"), null, null, false, CollectableField.TYPE_VALUEIDFIELD, NuclosEntity.ENTITYFIELDGROUP.getEntityName(),
					CollectableValueIdField.NULL, null, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_WRITEABLE,
					Boolean.class, CommonLocaleDelegate.getMessage("CollectableStateRoleAttributeGroup.8","Schreibrecht?"), CommonLocaleDelegate.getMessage("CollectableStateRoleAttributeGroup.7","Schreiben erlaubt?"), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null));
		}
	}

	public static final CollectableEntity clcte = new Entity();

	private CollectableField clctfRole;
	private final AttributegroupPermissionVO agpvo;

	public CollectableStateRoleAttributeGroup(CollectableField clctfRole, AttributegroupPermissionVO agpvo) {
		this.clctfRole = clctfRole;
		this.agpvo = agpvo;
	}

	protected CollectableEntity getCollectableEntity() {
		return clcte;
	}

	public AttributegroupPermissionVO getAttributegroupPermissionVO() {
		return this.agpvo;
	}

	@Override
	public Object getId() {
		return agpvo.getId();
	}

	@Override
	public String getIdentifierLabel() {
		return LangUtils.toString(this.getValue(FIELDNAME_ATTRIBUTEGROUP));
	}

	@Override
	public int getVersion() {
		return 0;
	}

	@Override
	public CollectableField getField(String sFieldName) throws CommonFatalException {
		final CollectableField result;

		if (sFieldName.equals(FIELDNAME_ROLE)) {
			result = this.clctfRole;
		}
		else if (sFieldName.equals(FIELDNAME_ATTRIBUTEGROUP)) {
			result = new CollectableValueIdField(this.agpvo.getAttributegroupId(), this.agpvo.getAttributegroup());
		}
		else if (sFieldName.equals(FIELDNAME_WRITEABLE)) {
			result = new CollectableValueField(Boolean.valueOf(this.agpvo.isWritable()));
		}
		else {
			throw new IllegalArgumentException(CommonLocaleDelegate.getMessage("CollectableStateRoleAttributeGroup.5","Feld nicht vorhanden: ") + sFieldName);
		}
		return result;
	}

	@Override
	public void setField(String sFieldName, CollectableField clctfValue) {
		if (sFieldName.equals(FIELDNAME_ROLE)) {
			this.clctfRole = clctfValue;
		}
		else if (sFieldName.equals(FIELDNAME_ATTRIBUTEGROUP)) {
			this.agpvo.setAttributegroupId((Integer) clctfValue.getValueId());
			this.agpvo.setAttributegroup((String) clctfValue.getValue());
		}
		else if (sFieldName.equals(FIELDNAME_WRITEABLE)) {
			this.agpvo.setWritable(((Boolean) clctfValue.getValue()).booleanValue());
		}
		else {
			throw new IllegalArgumentException(CommonLocaleDelegate.getMessage("CollectableStateRoleAttributeGroup.6","Feld nicht vorhanden: ") + sFieldName);
		}

		assert this.getField(sFieldName).equals(clctfValue);
	}

	public static class MakeCollectable implements Transformer<AttributegroupPermissionVO, CollectableStateRoleAttributeGroup> {
		private final CollectableField clctfRole;

		public MakeCollectable(CollectableField clctfRole) {
			this.clctfRole = clctfRole;
		}

		@Override
		public CollectableStateRoleAttributeGroup transform(AttributegroupPermissionVO agpvo) {
			return new CollectableStateRoleAttributeGroup(clctfRole, agpvo);
		}
	}	// inner class MakeCollectable

}	// class CollectableStateRoleAttributeGroup