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
import org.nuclos.server.statemodel.valueobject.SubformPermissionVO;

/**
 * User role for a state (for state dependent user subform rights).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */


public class CollectableStateRoleSubForm extends AbstractCollectable{

	public static final String FIELDNAME_ROLE = "role";
	public static final String FIELDNAME_SUBFORM = "entity";
	public static final String FIELDNAME_WRITEABLE = "writeable";

	public static class Entity extends AbstractCollectableEntity {
		public Entity() {
			super("stateroleentity", CommonLocaleDelegate.getMessage("CollectableStateRoleSubForm.9","Unterformulare f\u00fcr statusabh\u00e4ngige Rechte"));

			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_ROLE, String.class, CommonLocaleDelegate.getMessage("CollectableStateRoleSubForm.1","Benutzergruppe"),
				CommonLocaleDelegate.getMessage("CollectableStateRoleSubForm.6","\u00dcbergeordnete Benutzergruppe (Rolle)"), null, null, false, CollectableField.TYPE_VALUEIDFIELD, "staterole",
					CollectableValueIdField.NULL, null, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_SUBFORM,	String.class, CommonLocaleDelegate.getMessage("CollectableStateRoleSubForm.7","Unterformular"), 
				CommonLocaleDelegate.getMessage("CollectableStateRoleSubForm.8","Unterformular"), null, null, false, CollectableField.TYPE_VALUEFIELD, "entity",
					CollectableValueField.NULL, null, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_WRITEABLE,
					Boolean.class, CommonLocaleDelegate.getMessage("CollectableStateRoleSubForm.5","Schreibrecht?"), CommonLocaleDelegate.getMessage("CollectableStateRoleSubForm.4","Schreiben erlaubt?"), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null));
		}
	}

	public static final CollectableEntity clcte = new Entity();

	private CollectableField clctfRole;
	private final SubformPermissionVO sfpvo;

	public CollectableStateRoleSubForm(CollectableField clctfRole, SubformPermissionVO sfpvo) {
		this.clctfRole = clctfRole;
		this.sfpvo = sfpvo;
	}
	
	protected CollectableEntity getCollectableEntity() {
		return clcte;
	}

	public SubformPermissionVO getSubformPermissionVO() {
		return this.sfpvo;
	}

	@Override
	public Object getId() {
		return sfpvo.getId();
	}

	@Override
	public String getIdentifierLabel() {
		return LangUtils.toString(this.getValue(FIELDNAME_SUBFORM));
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
		else if (sFieldName.equals(FIELDNAME_SUBFORM)) {
			result = new CollectableValueField(this.sfpvo.getSubform());
		}
		else if (sFieldName.equals(FIELDNAME_WRITEABLE)) {
			result = new CollectableValueField(Boolean.valueOf(this.sfpvo.isWriteable()));
		}
		else {
			throw new IllegalArgumentException(CommonLocaleDelegate.getMessage("CollectableStateRoleSubForm.2","Feld nicht vorhanden: ") + sFieldName);
		}
		return result;
	}

	@Override
	public void setField(String sFieldName, CollectableField clctfValue) {
		if (sFieldName.equals(FIELDNAME_ROLE)) {
			this.clctfRole = clctfValue;
		}
		else if (sFieldName.equals(FIELDNAME_SUBFORM)) {
			this.sfpvo.setSubform((String) clctfValue.getValue());
		}
		else if (sFieldName.equals(FIELDNAME_WRITEABLE)) {
			this.sfpvo.setWriteable(((Boolean) clctfValue.getValue()).booleanValue());
		}
		else {
			throw new IllegalArgumentException(CommonLocaleDelegate.getMessage("CollectableStateRoleSubForm.3","Feld nicht vorhanden: ") + sFieldName);
		}

		assert this.getField(sFieldName).equals(clctfValue);
	}

	public static class MakeCollectable implements Transformer<SubformPermissionVO, CollectableStateRoleSubForm> {
		private final CollectableField clctfRole;

		public MakeCollectable(CollectableField clctfRole) {
			this.clctfRole = clctfRole;
		}

		@Override
		public CollectableStateRoleSubForm transform(SubformPermissionVO sfpvo) {
			return new CollectableStateRoleSubForm(clctfRole, sfpvo);
		}
	}	// inner class MakeCollectable
}
