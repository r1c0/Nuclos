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
	
	public CollectableEntityPref(String type, String entity) {
		this.type = type;
		this.entity = entity;
	}
	
	public String getType() {
		return type;
	}
	
	public String getEntity() {
		return entity;
	}
	
}
