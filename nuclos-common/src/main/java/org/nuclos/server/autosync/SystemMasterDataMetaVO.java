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
package org.nuclos.server.autosync;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

import static org.nuclos.server.autosync.SystemMasterDataVO.CHANGED_DATE;
import static org.nuclos.server.autosync.SystemMasterDataVO.CHANGED_USER;
import static org.nuclos.server.autosync.SystemMasterDataVO.CREATED_DATE;
import static org.nuclos.server.autosync.SystemMasterDataVO.CREATED_USER;
import static org.nuclos.server.autosync.SystemMasterDataVO.VERSION;

@SuppressWarnings("deprecation")
public class SystemMasterDataMetaVO extends MasterDataMetaVO {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Collection<Set<String>> uniqueFieldCombinations;
	private final Collection<Set<String>> logicalUniqueFieldCombinations;
	
	SystemMasterDataMetaVO(
		Integer iId, String sEntityName,
		String sDBEntityName, String sMenuPath, boolean bSearchable,
		boolean bEditable, String sLabel,
		Collection<String> collFieldsForEquality, boolean bCacheable,

		Map<String, SystemMasterDataMetaFieldVO> mpFields, String sTreeView,
		String sDescription, boolean bSystemEntity, String sResourceName,
		String sNuclosResource,
		boolean bImportExport, String sLabelPlural, Integer iAccModifier,
		String accelerator, String sResourceIdForLabel,
		String sResourceIdForMenuPath, String sResourceIdForLabelPlural,
		String sResourceIdForTreeView, String sResourceIdForTreeViewDescription,
		List<Set<String>> uniqueFieldCombinations,
		List<Set<String>> logicalUniqueFieldCombinations)
	{
		super(iId, sEntityName, sDBEntityName, sMenuPath, bSearchable, bEditable,
			sLabel, collFieldsForEquality, bCacheable, CREATED_DATE, CREATED_USER,
			CHANGED_DATE, CHANGED_USER, VERSION, mpFields, sTreeView, sDescription,
			bSystemEntity, sResourceName, sNuclosResource, bImportExport, sLabelPlural, iAccModifier,
			accelerator, sResourceIdForLabel, sResourceIdForMenuPath,
			sResourceIdForLabelPlural, sResourceIdForTreeView,
			sResourceIdForTreeViewDescription);
		this.uniqueFieldCombinations = uniqueFieldCombinations;
		this.logicalUniqueFieldCombinations = logicalUniqueFieldCombinations;
	}
	
	public Collection<Set<String>> getUniqueFieldCombinations() {
		return uniqueFieldCombinations;
	}
	
	public Collection<Set<String>> getLogicalUniqueFieldCombinations() {
		return logicalUniqueFieldCombinations;
	}
	
	@Override
	public Set<String> getUniqueFieldNames() {
		// TODO: use super-set of all combinations
		return super.getUniqueFieldNames();
	}
}
