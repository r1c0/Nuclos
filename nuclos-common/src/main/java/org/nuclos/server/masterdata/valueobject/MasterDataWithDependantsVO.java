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
 * 
 * @deprecated As MasterDataVO already contains its dependants, I see no reason
 * 		to stick to this class. (tp)
 */
public class MasterDataWithDependantsVO extends MasterDataVO implements IMasterDataWithDependantsVO {

	/**
	 * the dependants of this object.
	 */
	private DependantMasterDataMap mpDependants2;

	/**
	 * @param mdvo
	 * @param mpDependants the dependants, if any, of this object.
	 * @postcondition this.isComplete() <--> (mpDependants != null)
	 */
	public MasterDataWithDependantsVO(MasterDataVO mdvo, DependantMasterDataMap mpDependants) {
		super(mdvo.getEntityName(), mdvo);
		this.mpDependants2 = mpDependants;

		assert this.isComplete() == (mpDependants != null);
	}

	/**
	 * @return Has this value object been loaded completely?
	 * @see Collectable#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return this.mpDependants2 != null;
	}

	/**
	 * @return the dependants of this object. If this object isn't complete, an empty DependantMasterDataMap will be returned.
	 * @postcondition result != null
	 */
	@Override
	public DependantMasterDataMap getDependants() {
		return this.isComplete() ? this.mpDependants2 : new DependantMasterDataMapImpl();
	}

	@Override
	public void setDependants(DependantMasterDataMap mpDependants) {
		this.mpDependants2 = mpDependants;
	}
	
	@Override
	public String toDescription() {
		final StringBuilder result = new StringBuilder();
		result.append("MdwdVO[id=").append(getId());
		if (isChanged()) {
			result.append(",changed=").append(isChanged());
		}
		if (isSystemRecord()) {
			result.append(",sr=").append(isSystemRecord());
		}
		result.append(",fields=").append(getFields());
		final DependantMasterDataMap deps = getDependants();
		if (deps != null && !deps.isEmpty()) {
			result.append(",deps=").append(deps);
		}
		if (mpDependants2 != null && !mpDependants2.isEmpty()) {
			result.append(",deps2=").append(mpDependants2);
		}
		result.append("]");
		return result.toString();
	}

}	 // class MasterDataWithDependantsVO
