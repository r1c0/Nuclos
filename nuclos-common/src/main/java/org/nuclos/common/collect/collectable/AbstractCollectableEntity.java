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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Abstract implementation of <code>CollectableEntity</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public abstract class AbstractCollectableEntity implements CollectableEntity {
	
	private final String sName;
	private final String sLabel;
	private final Map<String, CollectableEntityField> mpclctef = new HashMap<String, CollectableEntityField>();
	
	public AbstractCollectableEntity(String sName, String sLabel) {
		this.sName = sName;
		this.sLabel = sLabel;
	}
	
	protected SpringLocaleDelegate getSpringLocaleDelegate() {
		return SpringLocaleDelegate.getInstance();
	}

	protected void addCollectableEntityField(CollectableEntityField clctef) {
		this.mpclctef.put(clctef.getName(), clctef);
	}

	@Override
	public String getName() {
		return this.sName;
	}

	@Override
	public String getLabel() {
		return this.sLabel;
	}

	@Override
	public int getFieldCount() {
		return this.mpclctef.size();
	}

	@Override
	public Set<String> getFieldNames() {
		return this.mpclctef.keySet();
	}

	/**
	 * @return "name" (as default)
	 * @todo <code>null</code> would be a better default!
	 */
	@Override
	public String getIdentifierFieldName() {
		return "name";
	}

	@Override
	public CollectableEntityField getEntityField(String sFieldName) throws CommonFatalException {
		final CollectableEntityField result = this.mpclctef.get(sFieldName);
		if (result == null) {
			throw new CommonFatalException(StringUtils.getParameterizedExceptionMessage("field.not.available", sFieldName));//"Feld nicht vorhanden: " + sFieldName);
		}
		assert result != null;
		return result;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("name=").append(sName);
		result.append(",label=").append(sLabel);
		result.append(", fields=").append(mpclctef);
		result.append("]");
		return result.toString();
	}

}  // class AbstractCollectableEntity
