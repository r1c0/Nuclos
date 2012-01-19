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

import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Default implementation for a <code>Collectable</code>.
 * This is suitable for <code>Collectable</code>s that stand on their own, not wrapping another object.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class DefaultCollectable extends AbstractCollectable {

	private final CollectableEntity clcte;

	protected final Map<String, CollectableField> mpFields;

	/**
	 * sets all fields to their default values.
	 */
	public DefaultCollectable(CollectableEntity clcte) {
		this.clcte = clcte;
		this.mpFields = new HashMap<String, CollectableField>(clcte.getFieldCount());

		for (String sFieldName : clcte.getFieldNames()) {
			this.mpFields.put(sFieldName, clcte.getEntityField(sFieldName).getDefault());
		}
	}

	protected CollectableEntity getCollectableEntity() {
		return this.clcte;
	}

	@Override
	public Object getId() {
		// We don't have an explicit id:
		return LangUtils.getJavaObjectId(this);
	}

	@Override
	public String getIdentifierLabel() {
		return LangUtils.toString(this.getId());
	}

	@Override
	public CollectableField getField(String sFieldName) throws CommonFatalException {
		this.checkContainedInEntity(sFieldName);
		final CollectableField result = this.mpFields.get(sFieldName);
		assert result != null;
		return result;
	}

	@Override
	public void setField(String sFieldName, CollectableField clctfValue) {
		this.checkContainedInEntity(sFieldName);
		this.mpFields.put(sFieldName, clctfValue);
	}

	/**
	 * This implementation doesn't care for concurrency. If concurrency is an issue for you, you must increase the
	 * version each time this Collectable is written to persistent store.
	 * @return 0
	 */
	@Override
	public int getVersion() {
		return 0;
	}

	private void checkContainedInEntity(String sFieldName) {
		this.getCollectableEntity().getEntityField(sFieldName);
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("entity=").append(getCollectableEntity());
		result.append(",id=").append(getId());
		result.append(",label=").append(getIdentifierLabel());
		result.append(",complete=").append(isComplete());
		result.append("]");
		return result.toString();
	}
	
}  // class DefaultCollectable
