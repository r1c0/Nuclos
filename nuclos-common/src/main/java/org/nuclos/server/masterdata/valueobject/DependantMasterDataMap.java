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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.EntityAndFieldName;

/**
 * Interface to {@link org.nuclos.server.masterdata.valueobject.DependantMasterDataMapImpl}.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.8
 */
public interface DependantMasterDataMap {

	/**
	 * @param sDependantEntityName
	 * @return the dependants belonging to the given entity, if any.
	 * @postcondition result != null
	 */
	List<EntityObjectVO> getData(String sDependantEntityName);

	/**
	 * @param sDependantEntityName
	 * @return the dependants belonging to the given entity, if any.
	 * @postcondition result != null
	 * @deprecated Use {@link #getData(String sDependantEntityName)} instead
	 */
	@Deprecated
	Collection<MasterDataVO> getValues(String sDependantEntityName);

	/**
	 * @return all dependants for all entities.
	 * @todo consider eliminating this method
	 */
	Collection<EntityObjectVO> getAllData();

	/**
	 * @return all dependants for all entities.
	 * @todo consider eliminating this method
	 * @deprecated Use {@link #getAllData()} instead
	 */
	@Deprecated
	Collection<MasterDataVO> getAllValues();

	/**
	 * puts the given <code>MasterDataVO</code> into this map.
	 * @param sDependantEntityName
	 * @param mdvoDependant
	 * @deprecated Use {@link #addData(String,EntityObjectVO)} instead
	 */
	@Deprecated
	void addValue(String sDependantEntityName, MasterDataVO mdvoDependant);

	/**
	 * puts the given <code>MasterDataVO</code> into this map.
	 * @param sDependantEntityName
	 * @param mdvoDependant
	 */
	void addData(String sDependantEntityName, EntityObjectVO mdvoDependant);

	/**
	 * adds all elements of <code>collmdvoDependants</code> to this map.
	 * Note that if the given <code>collmdvoDependants</code> is empty, nothing will be added.
	 * @param sDependantEntityName
	 * @param collmdvoDependants
	 * @precondition collmdvoDependants != null
	 * @postcondition this.getValues(sDependantEntityName).containsAll(collvalue)
	 * @deprecated Use {@link #addAllData(String,Collection<EntityObjectVO>)} instead
	 */
	@Deprecated
	void addAllValues(String sDependantEntityName, Collection<MasterDataVO> collmdvoDependants);

	/**
	 * adds all elements of <code>collmdvoDependants</code> to this map.
	 * Note that if the given <code>collmdvoDependants</code> is empty, nothing will be added.
	 * @param sDependantEntityName
	 * @param collmdvoDependants
	 * @precondition collmdvoDependants != null
	 * @postcondition this.getValues(sDependantEntityName).containsAll(collvalue)
	 */
	void addAllData(String sDependantEntityName, Collection<EntityObjectVO> collmdvoDependants);

	/**
	 * @param sDependantEntityName
	 * @param collmdvoDependants
	 * @precondition collmdvoDependants != null
	 * @postcondition this.getValues(sDependantEntityName).size() == collmdvoDependants.size()
	 * @postcondition this.getValues(sDependantEntityName).containsAll(collmdvoDependants)
	 * * @deprecated Use {@link #setData(String sDependantEntityName, Collection<EntityObjectVO> collmdvoDependants)} instead
	 */
	@Deprecated
	void setValues(String sDependantEntityName, Collection<MasterDataVO> collmdvoDependants);

	/**
	 * @param sDependantEntityName
	 * @param collmdvoDependants
	 * @precondition collmdvoDependants != null
	 * @postcondition this.getValues(sDependantEntityName).size() == collmdvoDependants.size()
	 * @postcondition this.getValues(sDependantEntityName).containsAll(collmdvoDependants)
	 */
	void setData(String sDependantEntityName, Collection<EntityObjectVO> collmdvoDependants);

	@Deprecated
	void removeKey(String sDependantEntityName);

	boolean isEmpty();

	/**
	 * @return the names of entities that this map contains values for.
	 */
	Set<String> getEntityNames();

	/**
	 * @return Are all dependants new? That means: Do they all have <code>null</code> ids?
	 */
	boolean areAllDependantsNew();

	/**
	 * sets the parent id of masterdata records.
	 * @param iGenericObjectId
	 */
	void setParent(String moduleEntityName, Integer iGenericObjectId, Map<EntityAndFieldName, String> collSubEntities);

	boolean getPendingChanges();

}
