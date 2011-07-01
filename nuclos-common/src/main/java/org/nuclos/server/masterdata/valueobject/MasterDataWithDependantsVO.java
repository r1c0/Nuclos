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
package org.nuclos.server.masterdata.valueobject;

import org.nuclos.common.collect.collectable.Collectable;

/**
 * A master data record including dependant records.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class MasterDataWithDependantsVO extends MasterDataVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * the dependants of this object.
	 */
	private DependantMasterDataMap mpDependants;

	/**
	 * @param mdvo
	 * @param mpDependants the dependants, if any, of this object.
	 * @postcondition this.isComplete() <--> (mpDependants != null)
	 */
	public MasterDataWithDependantsVO(MasterDataVO mdvo, DependantMasterDataMap mpDependants) {
		super(mdvo);
		this.mpDependants = mpDependants;

		assert this.isComplete() == (mpDependants != null);
	}

	/**
	 * @return Has this value object been loaded completely?
	 * @see Collectable#isComplete()
	 */
	public boolean isComplete() {
		return this.mpDependants != null;
	}

	/**
	 * @return the dependants of this object. If this object isn't complete, an empty DependantMasterDataMap will be returned.
	 * @postcondition result != null
	 */
	@Override
	public DependantMasterDataMap getDependants() {
		return this.isComplete() ? this.mpDependants : new DependantMasterDataMap();
	}

	@Override
	public void setDependants(DependantMasterDataMap mpDependants) {
		this.mpDependants = mpDependants;
	}
}	 // class MasterDataWithDependantsVO
