package org.nuclos.client.entityobject;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.nuclos.common.collect.collectable.AbstractCollectableField;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.exception.CollectableFieldValidationException;

public class CollectableEntityObjectField extends AbstractCollectableField {

	private String sFieldName;
	private CollectableEntityObject ceo;

	private Object oValue;
	private Object oValueId;

	private int fieldType;

	public CollectableEntityObjectField(String sFieldName, CollectableEntityObject ceo) {
		super();
		this.sFieldName = sFieldName;
		this.ceo = ceo;
		this.oValue = ceo.getValue(sFieldName);
		this.oValueId = ceo.getValueId(sFieldName);
		fieldType = ceo.getCollectableEntity().getEntityField(sFieldName).getFieldType();
	}

	@Override
	public int getFieldType() {
		return fieldType;
		//return (this.oValueId != null) ? CollectableField.TYPE_VALUEIDFIELD : CollectableField.TYPE_VALUEFIELD;
	}

	@Override
	public Object getValue() {
		return this.oValue;
	}

	@Override
	public Object getValueId() throws UnsupportedOperationException {
		return this.oValueId;
	}

	@Override
	public void validate(CollectableEntityField clctef)	throws CollectableFieldValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public String toDescription() {
		final ToStringBuilder b = new ToStringBuilder(this).append(fieldType).append(sFieldName).append(oValueId).append(oValue);
		return b.toString();
	}

}
