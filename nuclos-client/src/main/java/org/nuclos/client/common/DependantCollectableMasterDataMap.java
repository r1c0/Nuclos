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
package org.nuclos.client.common;

import java.util.Collection;
import java.util.Set;

import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.collection.multimap.MultiListHashMap;
import org.nuclos.common.collection.multimap.MultiListMap;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;

/**
 * <code>MultiMap<String sEntityName, Collectable></code>. A map containing the dependent <code>Collectable</code>s by entity.
 * @todo the key should be (sEntityName, sForeignKeyFieldToParent) - refactor, then consider moving this class to common.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class DependantCollectableMasterDataMap {

	private final MultiListMap<String, CollectableEntityObject> mmp = new MultiListHashMap<String, CollectableEntityObject>();

	/**
	 * creates an empty map.
	 */
	public DependantCollectableMasterDataMap() {
	}

	/**
	 * @param sDependantEntityName
	 * @param collclctDependants Collection<Collectable>
	 * @precondition collclctDependants != null
	 */
	public DependantCollectableMasterDataMap(String sDependantEntityName, Collection<? extends CollectableEntityObject> collclctDependants) {
		this();
		this.addValues(sDependantEntityName, collclctDependants);
	}

	/**
	 * @param mpDependants
	 */
	public DependantCollectableMasterDataMap(DependantMasterDataMap mpDependants) {
		this();
		for (String sEntityName : mpDependants.getEntityNames()) {
			final CollectableEOEntity clctmde = (CollectableEOEntity) DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sEntityName);
			this.addValues(sEntityName, CollectionUtils.transform(mpDependants.getData(sEntityName), new CollectableEntityObject.MakeCollectable(clctmde)));
		}
	}

	/**
	 * transforms a <code>DependantMasterDataMap</code> into a <code>DependantCollectableMap</code>.
	 * @param mpDependants
	 * @return DependantCollectableMap
	 */
	public static DependantCollectableMasterDataMap newDependantCollectableMap(DependantMasterDataMap mpDependants) {
		return new DependantCollectableMasterDataMap(mpDependants);
	}

	/**
	 * @param sDependantEntityName
	 * @return the dependants belonging to the given entity, if any.
	 * @postcondition result != null
	 */
	public Collection<CollectableEntityObject> getValues(String sDependantEntityName) {
		return this.mmp.getValues(sDependantEntityName);
	}

	/**
	 * puts the given <code>Collectable</code> into this map.
	 * @param sDependantEntityName
	 * @param clctDependant
	 */
	public void addValue(String sDependantEntityName, CollectableEntityObject clctDependant) {
		this.mmp.addValue(sDependantEntityName, clctDependant);
	}

	/**
	 * puts all elements of <code>collclctDependants</code> into this map.
	 * Note that if the given <code>collclctDependants</code> is empty, nothing will be added.
	 * @param sDependantEntityName
	 * @param collclctDependants
	 * @precondition collclctDependants != null
	 */
	public void addValues(String sDependantEntityName, Collection<? extends CollectableEntityObject> collclctDependants) {
		this.mmp.addAllValues(sDependantEntityName, collclctDependants);
	}

	/**
	 * @return the names of entities that this map contains values for.
	 */
	public Set<String> getEntityNames() {
		return this.mmp.keySet();
	}

	/**
	 * transforms <code>this</code> into a <code>DependantMasterDataMap</code>.
	 * @return DependantMasterDataMap
	 */
	public DependantMasterDataMap toDependantMasterDataMap() {
		final DependantMasterDataMap result = new DependantMasterDataMap();
		for (String sEntityName : this.getEntityNames()) {
			for (CollectableMasterData clctmd : this.getValues(sEntityName)) {
				clctmd.setDependantMasterDataMap(clctmd.getDependantCollectableMasterDataMap().toDependantMasterDataMap());
			}
			
			Collection<EntityObjectVO> collmdvo = CollectionUtils.transform(this.getValues(sEntityName), new ExtractEntityObjectVO());
			result.addAllData(sEntityName, collmdvo);
		}
		return result;
	}
	
	// clears dependant map
	public void clear() {
		this.mmp.clear();
	}
	
	public static class ExtractEntityObjectVO implements Transformer<CollectableEntityObject, EntityObjectVO> {
		@Override
		public EntityObjectVO transform(CollectableEntityObject clctmd) {
			DependantMasterDataMap depmdmp = clctmd.getDependantMasterDataMap();
			EntityObjectVO mdVO = clctmd.getEntityObjectVO();
			mdVO.setDependants(depmdmp);
			return mdVO;
		}
	}

}	// class DependantCollectableMasterDataMap
