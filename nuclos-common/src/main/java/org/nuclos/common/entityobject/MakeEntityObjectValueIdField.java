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
package org.nuclos.common.entityobject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.LangUtils;

public class MakeEntityObjectValueIdField implements Transformer<EntityObjectVO, CollectableField> {

	private final String sFieldNameForValue;

	public MakeEntityObjectValueIdField() {
		this("name");
	}

	public MakeEntityObjectValueIdField(String sFieldNameForValue) {
		this.sFieldNameForValue = sFieldNameForValue;
	}

	@Override
	public CollectableField transform(EntityObjectVO eo) {
		if (sFieldNameForValue.contains("${")){
			Pattern referencedEntityPattern = Pattern.compile ("[$][{][\\w]+[}]");
			Matcher referencedEntityMatcher = referencedEntityPattern.matcher (sFieldNameForValue);
			StringBuffer sb = new StringBuffer();

			while (referencedEntityMatcher.find()) {
				String fieldName = referencedEntityMatcher.group().substring(2,referencedEntityMatcher.group().length()-1);
				Object value = eo.getFields().get(fieldName);
				if (value != null) {
					referencedEntityMatcher.appendReplacement (sb, value.toString());
				}else{
					referencedEntityMatcher.appendReplacement (sb,"n/a");
				}
			}

			// complete the transfer to the StringBuffer
			referencedEntityMatcher.appendTail (sb);

			return new CollectableValueIdField(LangUtils.convertId(eo.getId()), sb.toString());
		}else{
			return new CollectableValueIdField(LangUtils.convertId(eo.getId()), eo.getFields().get(this.sFieldNameForValue));
		}
	}

}
