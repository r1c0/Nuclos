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
package org.nuclos.common.statemodel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.UsageCriteria;
import org.nuclos.common.transport.GzipMap;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * Transferable container class for Statemodel-instances, which contains the
 * closure of all state models for a given module. Like the statemodels
 * themselves, an instance of this class is bound to the current user
 */
public class StatemodelClosure implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer                          moduleId;
	private Map<UsageCriteria, Statemodel>   statemodels;
	
	private Map<Integer, String>	         labelResourceSIDs;
	private Map<Integer, String>	         desriptionResourceSIDs;


	public StatemodelClosure(Integer moduleId) {
	    this.moduleId = moduleId;
	    statemodels = new GzipMap<UsageCriteria, Statemodel>();
    }

	public void addStatemodel(UsageCriteria uc, Statemodel statemodel) {
	    statemodels.put(uc, statemodel);
    }

	public Integer getModuleId() {
    	return moduleId;
    }

	public Map<UsageCriteria, Statemodel> getStatemodels() {
    	return statemodels;
    }
	
	public Statemodel getStatemodel(UsageCriteria crit) {
		Statemodel res = statemodels.get(crit);
		if(res == null)
			res = statemodels.get(new UsageCriteria(crit.getModuleId(), null));
		return res;
	}
	
	public Set<StateVO> getAllStates() {
		HashSet<StateVO> res = new HashSet<StateVO>();
		for(Statemodel s : statemodels.values())
			res.addAll(s.getAllStates());
		return res;
	}

	private void ensureResMaps() {
		if(labelResourceSIDs == null) {
			HashMap<Integer, String> t = new HashMap<Integer, String>();
			for(Statemodel s : statemodels.values())
				t.putAll(s.getLabelResourceSIDs());
			labelResourceSIDs = t;
		}
		if(desriptionResourceSIDs == null) {
			HashMap<Integer, String> t = new HashMap<Integer, String>();
			for(Statemodel s : statemodels.values())
				t.putAll(s.getDesriptionResourceSIDs());
			desriptionResourceSIDs = t;
		}
	}
	
	public String getResourceSIdForLabel(Integer stateId) {
		ensureResMaps();
	    return labelResourceSIDs.get(stateId);
    }

	public String getResourceSIdForDescription(Integer stateId) {
		ensureResMaps();
	    return desriptionResourceSIDs.get(stateId);
    }

}
