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
package org.nuclos.client.genericobject.access;

import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.access.CefSecurityAgent;
import org.nuclos.common.security.Permission;

public class CgoWithDependantsSecurityAgentImpl implements CefSecurityAgent {

	private final CollectableGenericObjectWithDependants cgo;

	private final CollectableEntityField field;

	private final boolean fieldBelongsToSubEntity;

	public CgoWithDependantsSecurityAgentImpl(CollectableGenericObjectWithDependants cgo, CollectableEntityField field,
			boolean fieldBelongsToSubEntity) {
		this.cgo = cgo;
		this.field = field;
		this.fieldBelongsToSubEntity = fieldBelongsToSubEntity;
	}

	private CollectableGenericObjectWithDependants getCollectable() {
		return cgo;
	}

	@Override
	public boolean isReadable() {
		final Permission permission;
		final CollectableField clctfield = getCollectable().getField(NuclosEOField.STATE.getMetaData().getField());
		final Integer iStatusId = (clctfield != null) ? (Integer) clctfield.getValueId() : null;

		// check subform data
		if (fieldBelongsToSubEntity) {
			final String sEntityName = field.getEntityName();
			permission = SecurityCache.getInstance().getSubFormPermission(sEntityName, iStatusId);
		}
		// check attribute data
		else {
			permission = SecurityCache.getInstance().getAttributePermission(
					cgo.getCollectableEntity().getName(), field.getName(), iStatusId);
		}
		if (permission == null) {
			return false;
		}
		return permission.includesReading();
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public boolean isRemovable() {
		return false;
	}

}
