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
package org.nuclos.server.common.valueobject;

import org.nuclos.common.HasId;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;
import java.io.Serializable;
import java.util.Date;

/**
 * Abstract Nucleus Value Object without a concrete id.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @param Id the type of this object's id.
 */
public abstract class AbstractNuclosValueObject<Id> implements Serializable, Cloneable, INuclosValueObject<Id> {

	private static final long serialVersionUID = 1012920874365879641L;
	private Date dateCreatedAt;
	private final String sCreatedBy;
	private Date dateChangedAt;
	private final String sChangedBy;
	private int iVersion;
	private boolean bRemoved;

	/**
	 * creates a value object representing a new object (that is to be inserted into the database)
	 */
	protected AbstractNuclosValueObject() {
		this(null, null, null, null, null);
	}

	/**
	 * copy ctor.
	 * @param that
	 * @postcondition this.getCreatedAt() == that.getCreatedAt()
	 * @postcondition this.getCreatedBy() == that.getCreatedBy()
	 * @postcondition this.getChangedAt() == that.getChangedAt()
	 * @postcondition this.getChangedBy() == that.getChangedBy()
	 * @postcondition this.getVersion() == that.getVersion()
	 */
	protected AbstractNuclosValueObject(AbstractNuclosValueObject<Id> that) {
		this(that.getCreatedAt(), that.getCreatedBy(), that.getChangedAt(), that.getChangedBy(), that.getVersion());
	}

	/**
	 * creates a value object representing an existing object (that was read from the database).
	 * @param dateCreatedAt creation date of underlying database record
	 * @param sCreatedBy creator of underlying database record
	 * @param dateChangedAt last changed date of underlying database record
	 * @param sChangedBy last changer of underlying database record
	 * @param iVersion the version of this record (see "Version Number" pattern)
	 */
	protected AbstractNuclosValueObject(Date dateCreatedAt, String sCreatedBy, Date dateChangedAt, String sChangedBy, Integer iVersion) {
		this.dateCreatedAt = dateCreatedAt;
		this.sCreatedBy = sCreatedBy;
		this.dateChangedAt = dateChangedAt;
		this.sChangedBy = sChangedBy;
		this.iVersion = LangUtils.defaultIfNull(iVersion, 0);
	}

	/**
	 * @return a clone of this object.
	 */
	@Override
	protected INuclosValueObject<Id> clone() {
		final AbstractNuclosValueObject<Id> result;
		try {
			result = (AbstractNuclosValueObject<Id>) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			throw new CommonFatalException(ex);
		}
		if (this.dateCreatedAt != null) {
			result.dateCreatedAt = (Date) this.dateCreatedAt.clone();
		}
		if (this.dateChangedAt != null) {
			result.dateChangedAt = (Date) this.dateChangedAt.clone();
		}
		return result;
	}

	/**
	 * @return this object's id (aka primary key).
	 */
	@Override
	public abstract Id getId();

	/**
	 * mark underlying database record as to be removed from database
	 */
	@Override
	public void remove() {
		this.bRemoved = true;
	}

	/**
	 * is underlying database record to be removed from database?
	 * @return boolean value
	 */
	@Override
	public boolean isRemoved() {
		return this.bRemoved;
	}

	/**
	 * get creation date (datcreated) of underlying database record
	 * @return created date of underlying database record
	 */
	@Override
	public Date getCreatedAt() {
		return this.dateCreatedAt;
	}

	/**
	 * get creator (strcreated) of underlying database record
	 * @return creator of underlying database record
	 */
	@Override
	public String getCreatedBy() {
		return this.sCreatedBy;
	}

	/**
	 * get last changed date (datchanged) of underlying database record
	 * @return last changed date of underlying database record
	 */
	@Override
	public Date getChangedAt() {
		return this.dateChangedAt;
	}

	/**
	 * get last changer (strchanged) of underlying database record
	 * @return last changer of underlying database record
	 */
	@Override
	public String getChangedBy() {
		return this.sChangedBy;
	}

	/**
	 * get version (intversion) of underlying database record
	 * @return version of underlying database record
	 */
	@Override
	public int getVersion() {
		return this.iVersion;
	}
	
	/**
	 * @since Nuclos 3.5
	 * @author Thomas Pasch
	 */
	@Override
	public void setVersion(int version) {
		this.iVersion = version;
	}

}	// class AbstractNuclosValueObject
