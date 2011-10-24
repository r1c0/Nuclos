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
