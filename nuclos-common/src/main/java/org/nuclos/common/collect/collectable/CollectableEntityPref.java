package org.nuclos.common.collect.collectable;

import java.util.prefs.Preferences;

/**
 * A representation of {@link CollectableEntity} that is more suited 
 * to be stored in {@link Preferences}.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.0-rc5
 */
public class CollectableEntityPref {

	private final String type;
	
	private final String entity;
	
	private final boolean belongsToSubEntity;
	
	private final boolean belongsToMainEntity;
	
	public CollectableEntityPref(String type, String entity, boolean belongsToSubEntity, boolean belongsToMainEntity) {
		this.type = type;
		this.entity = entity;
		this.belongsToSubEntity = belongsToSubEntity;
		this.belongsToMainEntity = belongsToMainEntity;
	}
	
	public String getType() {
		return type;
	}
	
	public String getEntity() {
		return entity;
	}
	
	public boolean getBelongsToSubEntity() {
		return belongsToSubEntity;
	}
	
	public boolean getBelongsToMainEntity() {
		return belongsToMainEntity;
	}
}
