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
package org.nuclos.common;

import java.util.prefs.Preferences;

import org.nuclos.common.collect.collectable.*;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * CollectableEntityFieldWithEntityForExternal supports including rows from a subform
 * in the base entity result list.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * <p>
 * TODO: Consider {@link org.nuclos.client.common.CollectableEntityFieldPreferencesUtil}
 * to write to {@link Preferences}.
 * </p>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class CollectableEntityFieldWithEntityForExternal extends CollectableEntityFieldWithEntity {

	private final boolean bFieldBelongsToSubEntity;
	private final boolean bFieldBelongsToMainEntity;

	public CollectableEntityFieldWithEntityForExternal(CollectableEntity clcte, String sFieldName, boolean bFieldBelongsToSubEntity, boolean bFieldBelongsToMainEntity) {
		super(clcte, sFieldName);
		this.bFieldBelongsToSubEntity = bFieldBelongsToSubEntity;
		this.bFieldBelongsToMainEntity = bFieldBelongsToMainEntity;
	}

	// As the values of subentity fields must be concatenatable,
	// we have to pretend they're plain String fields without maximum lengths.
	// -> No, we can a expect a list of values
	@Override
	public Class<?> getJavaClass() {
		return super.getJavaClass();
	}

	@Override
	public Integer getMaxLength() {
		return bFieldBelongsToSubEntity ? null : super.getMaxLength();
	}

	@Override
	public int getFieldType() {
		return bFieldBelongsToSubEntity ? CollectableEntityField.TYPE_VALUEFIELD : super.getFieldType();
	}

	@Override
	public CollectableField getDefault() {
		return bFieldBelongsToSubEntity ? CollectableValueField.NULL : super.getDefault();
	}

	@Override
	public int getDefaultCollectableComponentType() {
		return bFieldBelongsToSubEntity ? CollectableComponentTypes.TYPE_TEXTFIELD : super.getDefaultCollectableComponentType();
	}

	@Override
	public String getLabel() {
		
		return bFieldBelongsToMainEntity ?
				this.getField().getLabel() :
					getEntityLaybel() + "." + this.getField().getLabel();
	}
	
	private String getEntityLaybel() {
		String result = this.getCollectableEntityLabel();
		if (result == null) {
			if (this.getCollectableEntity() instanceof CollectableEOEntity) {
				CollectableEOEntity clcteo = (CollectableEOEntity) this.getCollectableEntity();
				result = CommonLocaleDelegate.getInstance().getLabelFromMetaDataVO(clcteo.getMeta());
			}
		}
		
		return result;
	}

	/**
	 * TODO: This is a HACK that takes debug representation as view representation.
	 */
	@Override
	public String toString() {
		/** @todo This formatting doesn't really belong here, but in the respective renderers */
		return bFieldBelongsToMainEntity ?
				this.getField().getLabel() :
				"<html>" +
						"<font color=\"blue\">" + getEntityLaybel() + "." + "</font>" +
						"<font color=\"black\">" + this.getField().getLabel() + "</font>" +
						"</html>";
	}

	public boolean fieldBelongsToSubEntity() {
		return bFieldBelongsToSubEntity;
	}

	public boolean fieldBelongsToMainEntity() {
		return bFieldBelongsToMainEntity;
	}
}
