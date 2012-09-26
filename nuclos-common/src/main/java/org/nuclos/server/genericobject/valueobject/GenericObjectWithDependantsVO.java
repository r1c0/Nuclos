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
package org.nuclos.server.genericobject.valueobject;

import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;

import java.util.Set;

/**
 * A leased object, along with its dependants.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class GenericObjectWithDependantsVO extends GenericObjectVO {

	/**
	 * the parent object, if any, for this object.
	 * 
	 * @deprecated Parent is no longer part of the entity model.
	 */
	private GenericObjectVO govoParent;

	/**
	 * the dependants of this object.
	 */
	private final DependantMasterDataMap mpDependants;

	/**
	 * @param nvo
	 * @param iModuleId
	 * @param iParentId
	 * @param stContainedAttributeIds
	 * @param bDeleted
	 * @param mpDependants
	 * @precondition permission != null
	 */
	public GenericObjectWithDependantsVO(NuclosValueObject nvo, Integer iModuleId, Integer iParentId, Integer iInstanceId, 
			Set<Integer> stContainedAttributeIds, boolean bDeleted, DependantMasterDataMap mpDependants) {

		super(nvo, iModuleId, iParentId, iInstanceId, stContainedAttributeIds, bDeleted);
		this.mpDependants = mpDependants;
	}

	/**
	 * @param govo the leased object itself (not the parent).
	 * @param mpDependants
	 */
	public GenericObjectWithDependantsVO(GenericObjectVO govo, DependantMasterDataMap mpDependants) {
		super(govo);

		this.mpDependants = mpDependants;
	}

	/**
	 * @param loccvo
	 * @deprecated For migration only.
	 */
	@Deprecated
	public GenericObjectWithDependantsVO(RuleObjectContainerCVO loccvo) {
		this(loccvo.getGenericObject(), loccvo.getDependants(true));
	}

	public DependantMasterDataMap getDependants() {
		return this.mpDependants;
	}

	/**
	 * @deprecated Parent is no longer part of the entity model.
	 */
	public GenericObjectVO getParent() {
		return this.govoParent;
	}

	/**
	 * @deprecated Parent is no longer part of the entity model.
	 */
	public void setParent(GenericObjectVO govoParent) {
		this.govoParent = govoParent;
	}

	public String toDescription() {
		final StringBuilder result = new StringBuilder();
		result.append("GowdVO[id=").append(getInstanceId());
		result.append(",moduleId=").append(getModuleId());
		if (isDeleted()) {
			result.append(",deleted=").append(isDeleted());
		}
		if (getParentId() != null) {
			result.append(",parentId=").append(getParentId());
		}
		if (getParent() != null) {
			result.append(",parent=").append(getParent());
		}
		if (mpDependants != null) {
			result.append(",deps=").append(mpDependants);
		}
		result.append(",fields=").append(getAttributes());
		result.append("]");
		return result.toString();
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("GenericObjectWithDependantsVO[");
		result.append("id=").append(getId());
		result.append(",moduleId=").append(getModuleId());
		result.append(",instanceId=").append(getInstanceId());
		result.append(",parent=").append(govoParent);
		result.append(",statusId=").append(getStatusId());
		result.append(",dependants=").append(mpDependants);
		result.append("]");
		return result.toString();
	}
	
}	// class GenericObjectWithDependantsVO
