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
package org.nuclos.common.collect.collectable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.LangUtils;

/**
 * Transformer that makes a value id field out of a <code>EntityObjectVO</code>,
 * with the eo's id as value id and with the eo's field with the given name as value.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">maik.stueker</a>
 * @version 01.00.00
 */
public class MakeCollectableValueIdField implements Transformer<EntityObjectVO, CollectableField> {

	private final String sFieldNameForValue;

	public MakeCollectableValueIdField() {
		this("name");
	}

	public MakeCollectableValueIdField(String sFieldNameForValue) {
		this.sFieldNameForValue = sFieldNameForValue;
	}

	@Override
	public CollectableField transform(EntityObjectVO eovo) {
		/** @todo take care for "isShowMnemonic" */
		if (sFieldNameForValue.contains("${")){
			Pattern referencedEntityPattern = Pattern.compile ("[$][{][\\w]+[}]");
			Matcher referencedEntityMatcher = referencedEntityPattern.matcher (sFieldNameForValue);
			StringBuffer sb = new StringBuffer();

			while (referencedEntityMatcher.find()) {
				String fieldName = referencedEntityMatcher.group().substring(2,referencedEntityMatcher.group().length()-1);
				Object value = eovo.getFields().get(fieldName);
				if (value != null) {
					referencedEntityMatcher.appendReplacement (sb, value.toString());
				}else{
					referencedEntityMatcher.appendReplacement (sb,"n/a");
				}
			}

			// complete the transfer to the StringBuffer
			referencedEntityMatcher.appendTail (sb);

			return new CollectableValueIdField(LangUtils.convertId(eovo.getId()), sb.toString());
		}else{
			return new CollectableValueIdField(LangUtils.convertId(eovo.getId()), eovo.getFields().get(this.sFieldNameForValue));
		}		
	}
	
	/**
	 * 
	 * @return
	 */
	public Collection<String> getFields() {
		Collection<String> fields = new ArrayList<String>();

		if (sFieldNameForValue.contains("${")){
			Pattern referencedEntityPattern = Pattern.compile ("[$][{][\\w]+[}]");
			Matcher referencedEntityMatcher = referencedEntityPattern.matcher (sFieldNameForValue);

			while (referencedEntityMatcher.find()) {
				String fieldName = referencedEntityMatcher.group().substring(2,referencedEntityMatcher.group().length()-1);
				fields.add(fieldName);
			}
		} else {
			fields.add(sFieldNameForValue);
		}
		
		return fields;
	}

}	// class MakeValueIdField
