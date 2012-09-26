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
package org.nuclos.server.masterdata.valueobject;

import org.nuclos.common.collect.collectable.Collectable;

/**
 * Interface to {@link org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO}.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.8
 * @param <Id> primary key type
 */
public interface IMasterDataWithDependantsVO extends IMasterDataVO {

	/**
	 * @return Has this value object been loaded completely?
	 * @see Collectable#isComplete()
	 */
	boolean isComplete();

	/**
	 * @return the dependants of this object. If this object isn't complete, an empty DependantMasterDataMap will be returned.
	 * @postcondition result != null
	 */
	DependantMasterDataMap getDependants();

	void setDependants(DependantMasterDataMap mpDependants);

	String toDescription();

}
