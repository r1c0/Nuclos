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
}
