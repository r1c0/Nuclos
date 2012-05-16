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

import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Abstract (basic) implementation of a <code>Collectable</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
@Configurable
public abstract class AbstractCollectable implements Collectable {
	
	private SpringLocaleDelegate localeDelegate;
	
	protected AbstractCollectable() {
	}
	
	@Autowired
	void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	protected SpringLocaleDelegate getSpringLocaleDelegate() {
		return localeDelegate;
	}

	/**
	 * Subclasses may provide an implementation that is more efficient than <code>this.getField(sFieldName).getValue()</code>.
	 * @param sFieldName
	 * @return <code>this.getField(sFieldName).getValue()</code>
	 */
	@Override
	public Object getValue(String sFieldName) {
		return this.getField(sFieldName).getValue();
	}

	/**
	 * Subclasses may provide an implementation that is more efficient than <code>this.getField(sFieldName).getValueId()</code>.
	 * @param sFieldName
	 * @return <code>this.getField(sFieldName).getValueId()</code>
	 * @precondition this.getField(sFieldName).isIdField() -
	 * Note that this precondition may not be checked at runtime by successors because of possible side effects.
	 * @throws UnsupportedOperationException if the field with the given name doesn't have an id
	 */
	@Override
	public Object getValueId(String sFieldName) {
		return this.getField(sFieldName).getValueId();
	}

	/**
	 * @return <code>true</code>. By default, all <code>Collectable</code>s are loaded completely.
	 */
	@Override
	public boolean isComplete() {
		return true;
	}

	/**
	 * <code>Collectable</code>s are equal if they have the same id.
	 * If the id of both <code>Collectable</code>s is <code>null</code>, they are equal only if they are identical.
	 * @param o
	 * @return {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		final boolean result;
		if (this == o) {
			result = true;
		}
		else if (!(o instanceof Collectable)) {
			result = false;
		}
		else {
			final Collectable that = (Collectable) o;
			if (this.getId() == null && that.getId() == null) {
				assert this != that;
				// @todo P2 This implementation doesn't conform to the specification! Probably, the specification should be changed.
				result = false;
			}
			else {
				result = LangUtils.equals(this.getId(), that.getId());
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(this.getId());
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",label=").append(getIdentifierLabel());
		result.append(",complete=").append(isComplete());
		result.append("]");
		return result.toString();
	}

}	// class AbstractCollectable
