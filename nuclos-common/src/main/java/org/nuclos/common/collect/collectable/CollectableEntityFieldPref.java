package org.nuclos.common.collect.collectable;

import java.io.Serializable;
import java.util.prefs.Preferences;

import org.nuclos.common.dal.vo.PivotInfo;

/**
 * A representation of {@link CollectableEntityField} that is more suited 
 * to be stored in {@link Preferences}.
 * <p>
 * Attention: Consider using {@link org.nuclos.common.WorkspaceDescription}
 * when you want to store Workspace setting (in contrast to user preferences).
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.1.0-rc5
 */
public class CollectableEntityFieldPref implements Serializable {
	
	private final String type;
	
	private final String entity;

	private final String field;
	
	// optional 
	private final PivotInfo pivot;
	
	// optional
	private final CollectableEntityPref ce;
	
	// optional, only when ce is set
	private final boolean belongsToSubEntity;
	
	// optional, only when ce is set
	private final boolean belongsToMainEntity;
	
	public CollectableEntityFieldPref(String type, String entity, String field, PivotInfo pivot) {
		this.type = type;
		this.entity = entity;
		this.field = field;
		this.pivot = pivot;
		this.ce = null;
		this.belongsToSubEntity = false;
		this.belongsToMainEntity = true;
	}
	
	public CollectableEntityFieldPref(String type, CollectableEntityPref entity, String field, boolean belongsToSubEntity, boolean belongsToMainEntity) {
		this.type = type;
		this.entity = entity.getEntity();
		this.field = field;
		this.pivot = null;
		this.ce = entity;
		this.belongsToSubEntity = belongsToSubEntity;
		this.belongsToMainEntity = belongsToMainEntity;
	}
	
	public String getType() {
		return type;
	}
	
	public String getEntity() {
		return entity;
	}
	
	public String getField() {
		return field;
	}
	
	public PivotInfo getPivot() {
		return pivot;
	}
	
	public CollectableEntityPref getCollectableEntity() {
		return ce;
	}

	public boolean getBelongsToSubEntity() {
		return belongsToSubEntity;
	}
	
	public boolean getBelongsToMainEntity() {
		return belongsToMainEntity;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("CollectableEntityPref[").append(entity).append('.').append(field);
		result.append(", ").append(type);
		if (pivot != null) {
			result.append(", ").append(pivot);
		}
		if (ce != null) {
			result.append(", ").append(ce);
		}
		result.append(", main=").append(belongsToMainEntity);
		result.append(", sub=").append(belongsToSubEntity);
		result.append(']');
		return result.toString();
	}
	
}
