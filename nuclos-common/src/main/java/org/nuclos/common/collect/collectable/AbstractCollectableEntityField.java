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

import java.io.Serializable;
import java.util.Date;

import org.nuclos.common2.LangUtils;
import org.nuclos.common2.RelativeDate;

/**
 * Abstract implementation of a <code>CollectableEntityField</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public abstract class AbstractCollectableEntityField implements CollectableEntityField, Serializable {

	private static final long serialVersionUID = -6972777641358799942L;

	private transient CollectableEntity clcte;
	private transient CollectableEntityFieldSecurityAgent securityAgent = new CollectableEntityFieldSecurityAgent();

	@Override
    public boolean isIdField() {
		return (this.getFieldType() == TYPE_VALUEIDFIELD);
	}

	@Override
	public CollectableEntity getCollectableEntity() {
		return clcte;
	}

	@Override
	public void setCollectableEntity(CollectableEntity clent) {
		clcte = clent;
	}

	@Override
    public int getDefaultCollectableComponentType() {
		final int result;
		if (this.isIdField()) {
			// default is combobox. listofvalues must be specified as controltype explicitly.
			result = CollectableComponentTypes.TYPE_COMBOBOX;
		}
		else {
			result = CollectableUtils.getCollectableComponentTypeForClass(this.getJavaClass());
		}
		return result;
	}

	@Override
    public final CollectableField getNullField() {
		final CollectableField result = CollectableUtils.getNullField(this.getFieldType());

		assert result != null;
		assert result.isNull();
		assert result.getFieldType() == this.getFieldType();
		return result;
	}

	/**
	 * @return getNullField(). Successors may specify a non-null default value here.
	 */
	@Override
    public CollectableField getDefault() {
		final CollectableField result = this.getNullField();
		assert result != null;
		assert result.getFieldType() == this.getFieldType();
		if (!(this.getJavaClass() == Date.class && result.getValue() != null && result.getValue().toString().equalsIgnoreCase(RelativeDate.today().toString())))
			assert LangUtils.isInstanceOf(result.getValue(), this.getJavaClass());
		return result;
	}

	/**
	 * @precondition this.isIdField()
	 * @return <code>false</code>. This may be overridden by subclasses.
	 */
	@Override
    public boolean isRestrictedToValueList() {
		if (!this.isIdField()) {
			throw new IllegalStateException("isIdField");
		}
		return false;
	}

	/**
	 * @return "name" (as default)
	 */
	@Override
    public String getReferencedEntityFieldName() {
		return "name";
	}

	/**
	 * Two <code>CollectableEntityField</code> instances are equals iff their names
	 * (as defined by <code>getName()</code>) are equal.
	 */
	@Override
	public boolean equals(Object o) {
		return (this == o) || ((o instanceof CollectableEntityField) && this.getName().equals(
				((CollectableEntityField) o).getName()));
	}

	/**
	 * @return hash code based on getName().
	 */
	@Override
	public int hashCode() {
		return this.getName().hashCode();
	}

	/**
	 * @precondition isReferencing()
	 */
	@Override
    public boolean isReferencedEntityDisplayable() {
		if (!isReferencing()) {
			throw new IllegalStateException("referencing");
		}
		return true;
	}

	/**
	 * @return <code>this.getLabel()</code>
	 */
	@Override
	public String toString() {
		return this.getLabel() == null ? this.getName() : this.getLabel();
	}

	/**
	 * set security agent
	 */
	@Override
    public void setSecurityAgent(CollectableEntityFieldSecurityAgent sa) {
		this.securityAgent = sa;
	}

	/**
	 * get security agent
	 */
	@Override
    public CollectableEntityFieldSecurityAgent getSecurityAgent() {
		return this.securityAgent;
	}

	/**
	 * is this field readable
	 */
	@Override
    public boolean isReadable() {
		return getSecurityAgent().isReadable();
	}

	/**
	 * is this field writable
	 */
	@Override
    public boolean isWritable() {
		return getSecurityAgent().isWritable();
	}

	/**
	 * is this field removable
	 */
	@Override
    public boolean isRemovable() {
		return getSecurityAgent().isRemovable();
	}
}	// class AbstractCollectableEntityField
