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
package org.nuclos.server.processmonitor.valueobject;

import org.nuclos.common.UsageCriteria;

/**
 * 
 *
 */
public class SubProcessUsageCriteriaVO extends UsageCriteria {

	public SubProcessUsageCriteriaVO(Integer moduleId, Integer processId) {
		super(moduleId, processId);
	}
	
	public SubProcessUsageCriteriaVO(UsageCriteria ucSuper) {
		this(ucSuper.getModuleId(), ucSuper.getProcessId());
	}

	/**
	 * 
	 */
	@Override
	public String toString(){
		String result = "";
		
		if (super.getModuleId() != null){
			/** @TODO Keine Client Classen hier! */
//			result = Modules.getInstance().getEntityLabelByModuleId(super.getModuleId());
//			try {
//				for (MasterDataVO mdVO : MasterDataCache.getInstance().get(NuclosEntity.PROCESS.getEntityName())){
//					if (mdVO.getId().equals(super.getProcessId())){
//						result = result + " / " + mdVO.getField("name");
//						break;
//					}
//				}
//			} catch (CommonFinderException e) {
//				throw new CommonFatalException(e); 
//			}
		} else {
			result = "No Module";
		}
		
		return result;
	}

	/**
	 * 
	 * @return
	 */
	public Integer getId() {
		/** @TODO Keine Client Classen hier! */
//		try {
//			for (MasterDataVO mdVO : MasterDataCache.getInstance().get(NuclosEntity.STATEMODELUSAGE.getEntityName())){
//				Integer moduleId = (Integer) mdVO.getField("moduleId");
//				Integer processId = (Integer) mdVO.getField("processId");
//				
//				if (moduleId.equals(super.getModuleId()) && 
//						( 
//								(processId == null && super.getProcessId() == null) ||
//								(processId != null && processId.equals(super.getProcessId()))
//								)){
//					return mdVO.getIntId();
//				}
//			}
//		} catch (CommonFinderException e) {
//			throw new CommonFatalException(e); 
//		}
		return null;
	}
	
}
