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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.EntityAndFieldName;

/**
 * A view to a wrapped {@link org.nuclos.server.masterdata.valueobject.DependantMasterDataMapImpl} 
 * that only 'shows' the non-deleted items.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.8
 */
public class DependantMasterDataMapNonDeletedView implements DependantMasterDataMap {
	
	private final DependantMasterDataMapImpl wrapped;
	
	public DependantMasterDataMapNonDeletedView(DependantMasterDataMapImpl wrapped) {
		this.wrapped = wrapped;
	}
	
	private static List<EntityObjectVO> filterEo(Collection<EntityObjectVO> in) {
		final List<EntityObjectVO> result = new ArrayList<EntityObjectVO>();
		for (EntityObjectVO eo: in) {
			if (!eo.isFlagRemoved()) {
				result.add(eo);
			}
		}
		return result;
	}

	private static List<MasterDataVO> filterMd(Collection<MasterDataVO> in) {
		final List<MasterDataVO> result = new ArrayList<MasterDataVO>();
		for (MasterDataVO md: in) {
			if (!md.isRemoved()) {
				result.add(md);
			}
		}
		return result;
	}

	@Override
	public List<EntityObjectVO> getData(String sDependantEntityName) {
		return filterEo(wrapped.getData(sDependantEntityName));
	}

	@Override
	@Deprecated
	public Collection<MasterDataVO> getValues(String sDependantEntityName) {
		return filterMd(wrapped.getValues(sDependantEntityName));
	}

	@Override
	public Collection<EntityObjectVO> getAllData() {
		return filterEo(wrapped.getAllData());
	}

	@Override
	@Deprecated
	public Collection<MasterDataVO> getAllValues() {
		return filterMd(wrapped.getAllValues());
	}

	@Override
	@Deprecated
	public void addValue(String sDependantEntityName, MasterDataVO mdvoDependant) {
		wrapped.addValue(sDependantEntityName, mdvoDependant);
	}

	@Override
	public void addData(String sDependantEntityName, EntityObjectVO mdvoDependant) {
		wrapped.addData(sDependantEntityName, mdvoDependant);
	}

	@Override
	@Deprecated
	public void addAllValues(String sDependantEntityName, Collection<MasterDataVO> collmdvoDependants) {
		wrapped.addAllValues(sDependantEntityName, collmdvoDependants);
	}

	@Override
	public void addAllData(String sDependantEntityName, Collection<EntityObjectVO> collmdvoDependants) {
		wrapped.addAllData(sDependantEntityName, collmdvoDependants);
	}

	@Override
	@Deprecated
	public void setValues(String sDependantEntityName, Collection<MasterDataVO> collmdvoDependants) {
		wrapped.setValues(sDependantEntityName, collmdvoDependants);
	}

	@Override
	public void setData(String sDependantEntityName, Collection<EntityObjectVO> collmdvoDependants) {
		wrapped.setData(sDependantEntityName, collmdvoDependants);
	}

	@Override
	@Deprecated
	public void removeKey(String sDependantEntityName) {
		wrapped.removeKey(sDependantEntityName);
	}

	@Override
	public boolean isEmpty() {
		return wrapped.isEmpty();
	}

	@Override
	public Set<String> getEntityNames() {
		return wrapped.getEntityNames();
	}

	@Override
	public boolean areAllDependantsNew() {
		return wrapped.areAllDependantsNew();
	}

	@Override
	public void setParent(String moduleEntityName, Integer iGenericObjectId,
			Map<EntityAndFieldName, String> collSubEntities) {
		wrapped.setParent(moduleEntityName, iGenericObjectId, collSubEntities);
	}

	@Override
	public boolean getPendingChanges() {
		return wrapped.getPendingChanges();
	}

}
