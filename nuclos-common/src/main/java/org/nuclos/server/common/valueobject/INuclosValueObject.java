//Copyright (C) 2012  Novabit Informationssysteme GmbH
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

import java.util.Date;

import org.nuclos.common.HasId;

/**
 * Interface to {@link org.nuclos.server.common.valueobject.AbstractNuclosValueObject<Id>}.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.8
 * @param <Id> primary key type
 */
public interface INuclosValueObject<Id> extends HasId<Id> {

	/**
	 * @return this object's id (aka primary key).
	 */
	Id getId();

	/**
	 * mark underlying database record as to be removed from database
	 */
	void remove();

	/**
	 * is underlying database record to be removed from database?
	 * @return boolean value
	 */
	boolean isRemoved();

	/**
	 * get creation date (datcreated) of underlying database record
	 * @return created date of underlying database record
	 */
	Date getCreatedAt();

	/**
	 * get creator (strcreated) of underlying database record
	 * @return creator of underlying database record
	 */
	String getCreatedBy();

	/**
	 * get last changed date (datchanged) of underlying database record
	 * @return last changed date of underlying database record
	 */
	Date getChangedAt();

	/**
	 * get last changer (strchanged) of underlying database record
	 * @return last changer of underlying database record
	 */
	String getChangedBy();

	/**
	 * get version (intversion) of underlying database record
	 * @return version of underlying database record
	 */
	int getVersion();

	/**
	 * @since Nuclos 3.5
	 * @author Thomas Pasch
	 */
	void setVersion(int version);

}
