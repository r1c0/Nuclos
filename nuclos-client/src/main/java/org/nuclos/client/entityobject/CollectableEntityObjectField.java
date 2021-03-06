package org.nuclos.client.entityobject;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.LocalizedCollectableValueField;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

public class CollectableEntityObjectField extends LocalizedCollectableValueField {

	private String sFieldName;

	private Object oValue;
	private Object oValueId;

	private int fieldType;

	private Class<?> javaType;

	public CollectableEntityObjectField(String sFieldName, CollectableEntityObject ceo) {
		super();

		this.sFieldName = sFieldName;
		this.oValue = ceo.getValue(sFieldName);
		this.oValueId = ceo.getValueId(sFieldName);
		this.fieldType = ceo.getCollectableEntity().getEntityField(sFieldName).getFieldType();
		this.javaType = ceo.getCollectableEntity().getEntityField(sFieldName).getJavaClass();

		if (this.oValue == null) {
			setLabel("");
		} else if (!MetaDataClientProvider.getInstance().isEntity(this.oValue.toString())) {
			setLabel(this.oValue.toString());
		} else {
			MasterDataMetaVO mdmVO = MasterDataDelegate.getInstance().getMetaData(this.oValue.toString());
			setLabel(SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(mdmVO));
		}
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
	public String toDescription() {
		final ToStringBuilder b = new ToStringBuilder(this).append(fieldType).append(sFieldName).append(oValueId).append(oValue);
		return b.toString();
	}

	@Override
	public boolean isNull() {
		if (getFieldType() == CollectableField.TYPE_VALUEFIELD) {
			return getValue() == null;
		}
		else if (getFieldType() == CollectableField.TYPE_VALUEIDFIELD) {
			return getValueId() == null;
		}
		throw new IllegalStateException();
	}

	@Override
	public int compareTo(CollectableField that) {
		if (String.class.isAssignableFrom(javaType)) {
			return super.compareTo(that);
		}
		else {
			return LangUtils.compare(this.getValue(), that.getValue());
		}
	}
}
