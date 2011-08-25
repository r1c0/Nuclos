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
package org.nuclos.common.genericobject;

import org.nuclos.common.AttributeProvider;
import org.nuclos.common.ModuleProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.format.FormattingTransformer;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;

/**
 * Transformer that makes a value id field out of a <code>GenericObjectVO</code>,
 * with the govo's id as value id and with the govo's field with the given name
 * as value. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:martin.weber@novabit.de">martin.weber</a>
 * @version 01.00.00
 */
public class MakeGenericObjectValueIdField implements
		Transformer<GenericObjectVO, CollectableField> {

	private final AttributeProvider attributeProvider;
	private final String sAttributeNameForValue;

	public MakeGenericObjectValueIdField(AttributeProvider attributeProvider) {
		this(attributeProvider, "name");
	}

	public MakeGenericObjectValueIdField(AttributeProvider attributeProvider, String sFieldNameForValue) {
		this.attributeProvider = attributeProvider;
		this.sAttributeNameForValue = sFieldNameForValue;
	}

	@Override
	public CollectableField transform(final GenericObjectVO govo) {

		ModuleProvider modules = SpringApplicationContextHolder.getBean(ModuleProvider.class);
		final String entity = modules.getEntityNameByModuleId(govo.getModuleId());
		if (sAttributeNameForValue.contains("${")) {
			String value = StringUtils.replaceParameters(sAttributeNameForValue, new FormattingTransformer() {
				@Override
				protected Object getValue(String field) {
					DynamicAttributeVO att = govo.getAttribute(field, attributeProvider);
					if (att != null) {
						return att.getValue();
					}
					else {
						return null;
					}
				}

				@Override
				protected String getEntity() {
					return entity;
				}
			});
			return new CollectableValueIdField(govo.getId(), value);
		}
		else {
			DynamicAttributeVO att = govo.getAttribute(this.sAttributeNameForValue, attributeProvider);
			if (att != null) {
				return new CollectableValueIdField(govo.getId(), att.getValue());
			}
			else {
				return new CollectableValueIdField(govo.getId(), "Attribut not loaded!");
			}
		}
	}

} // class MakeValueIdField
