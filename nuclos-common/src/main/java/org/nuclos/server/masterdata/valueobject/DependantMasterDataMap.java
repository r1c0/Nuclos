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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.MasterDataMetaProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.EntityObjectToMasterDataTransformer;
import org.nuclos.common.collection.MasterDataToEntityObjectTransformer;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.common.collection.multimap.MultiListHashMap;
import org.nuclos.common.collection.multimap.MultiListMap;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.IdUtils;
import org.nuclos.server.common.ModuleConstants;

/**
 * Map containing the dependent masterdata rows by entity.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public final class DependantMasterDataMap implements Serializable {

	private static final Logger log = Logger.getLogger(DependantMasterDataMap.class);

	protected final MultiListMap<String, EntityObjectVO> mmp = new MultiListHashMap<String, EntityObjectVO>();

	/**
	 * creates an empty map.
	 */
	public DependantMasterDataMap() {
	}
	
	/**
	 * @param sDependantEntityName
	 * @param collmdvoDependants Collection<MasterDataVO>
	 * @precondition collmdvoDependants != null
	 * @postcondition this.get(sDependantEntityName).equals(collmdvoDependants)
	 */
	@Deprecated
	public DependantMasterDataMap(String sDependantEntityName, Collection<MasterDataVO> collmdvoDependants) {
		this.addAllValues(sDependantEntityName, collmdvoDependants);
	}

	/**
	 * @param sDependantEntityName
	 * @param collmdvoDependants Collection<MasterDataVO>
	 * @precondition collmdvoDependants != null
	 * @postcondition this.get(sDependantEntityName).equals(collmdvoDependants)
	 */
	public DependantMasterDataMap(String sDependantEntityName, List<EntityObjectVO> collmdvoDependants) {
		this.addAllData(sDependantEntityName, collmdvoDependants);
	}

	/**
	 * @param sDependantEntityName
	 * @return the dependants belonging to the given entity, if any.
	 * @postcondition result != null
	 */
	public List<EntityObjectVO> getData(String sDependantEntityName) {
		return this.mmp.getValues(sDependantEntityName);
	}

	/**
	 * @param sDependantEntityName
	 * @return the dependants belonging to the given entity, if any.
	 * @postcondition result != null
	 * @deprecated Use {@link #getData(String sDependantEntityName)} instead
	 */
	@Deprecated
	public Collection<MasterDataVO> getValues(String sDependantEntityName) {
		Collection<MasterDataVO> out = CollectionUtils.transform(getData(sDependantEntityName),
			new EntityObjectToMasterDataTransformer());
		return out;
	}

	/**
	 * @return all dependants for all entities.
	 * @todo consider eliminating this method
	 */
	public Collection<EntityObjectVO> getAllData() {
		return CollectionUtils.concatAll(this.mmp.asMap().values());
	}

	/**
	 * @return all dependants for all entities.
	 * @todo consider eliminating this method
	 * @deprecated Use {@link #getAllData()} instead
	 */
	@Deprecated
	public Collection<MasterDataVO> getAllValues() {
		Collection<MasterDataVO> out = CollectionUtils.transform(getAllData(),
			new EntityObjectToMasterDataTransformer());
		return out;
	}

	/**
	 * puts the given <code>MasterDataVO</code> into this map.
	 * @param sDependantEntityName
	 * @param mdvoDependant
	 * @deprecated Use {@link #addData(String,EntityObjectVO)} instead
	 */
	@Deprecated
	public void addValue(String sDependantEntityName, MasterDataVO mdvoDependant) {
		addData(sDependantEntityName, DalSupportForMD.getEntityObjectVO(sDependantEntityName, mdvoDependant));
	}

	/**
	 * puts the given <code>MasterDataVO</code> into this map.
	 * @param sDependantEntityName
	 * @param mdvoDependant
	 */
	public void addData(String sDependantEntityName, EntityObjectVO mdvoDependant) {
		this.mmp.addValue(sDependantEntityName, mdvoDependant);
	}

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
	public void addAllValues(String sDependantEntityName, Collection<MasterDataVO> collmdvoDependants) {
		Collection<EntityObjectVO> colVo = CollectionUtils.transform(collmdvoDependants,
			new MasterDataToEntityObjectTransformer(sDependantEntityName));
		addAllData(sDependantEntityName, colVo);
	}

	/**
	 * adds all elements of <code>collmdvoDependants</code> to this map.
	 * Note that if the given <code>collmdvoDependants</code> is empty, nothing will be added.
	 * @param sDependantEntityName
	 * @param collmdvoDependants
	 * @precondition collmdvoDependants != null
	 * @postcondition this.getValues(sDependantEntityName).containsAll(collvalue)
	 */
	public void addAllData(String sDependantEntityName, Collection<EntityObjectVO> collmdvoDependants) {
		this.mmp.addAllValues(sDependantEntityName, collmdvoDependants);
	}

	/**
	 * @param sDependantEntityName
	 * @param collmdvoDependants
	 * @precondition collmdvoDependants != null
	 * @postcondition this.getValues(sDependantEntityName).size() == collmdvoDependants.size()
	 * @postcondition this.getValues(sDependantEntityName).containsAll(collmdvoDependants)
	 * * @deprecated Use {@link #setData(String sDependantEntityName, Collection<EntityObjectVO> collmdvoDependants)} instead
	 */
	@Deprecated
	public void setValues(String sDependantEntityName, Collection<MasterDataVO> collmdvoDependants) {
		Collection<EntityObjectVO> colVO = CollectionUtils.transform(collmdvoDependants,
			new MasterDataToEntityObjectTransformer(sDependantEntityName));
		setData(sDependantEntityName, colVO);
	}

	/**
	 * @param sDependantEntityName
	 * @param collmdvoDependants
	 * @precondition collmdvoDependants != null
	 * @postcondition this.getValues(sDependantEntityName).size() == collmdvoDependants.size()
	 * @postcondition this.getValues(sDependantEntityName).containsAll(collmdvoDependants)
	 */
	public void setData(String sDependantEntityName, Collection<EntityObjectVO> collmdvoDependants) {
		// @todo consider defining setValues() in MultiListMap already, because this seems awkward:
		this.mmp.removeKey(sDependantEntityName);
		this.mmp.addAllValues(sDependantEntityName, collmdvoDependants);

		assert this.getData(sDependantEntityName).size() == collmdvoDependants.size();
	}

	@Deprecated
	public void removeKey(String sDependantEntityName) {
		this.mmp.removeKey(sDependantEntityName);
	}

	public boolean isEmpty() {
		return mmp.isEmpty();
	}

	/**
	 * @return the names of entities that this map contains values for.
	 */
	public Set<String> getEntityNames() {
		return this.mmp.keySet();
	}

	/**
	 * @return Are all dependants new? That means: Do they all have <code>null</code> ids?
	 */
	public boolean areAllDependantsNew() {
		return CollectionUtils.forall(this.mmp.getAllValues(), PredicateUtils.transformedInputIsNull(new EntityObjectVO.GetId()));
	}

	/**
	 * sets the parent id of masterdata records.
	 * @param iGenericObjectId
	 */
	public void setParent(String moduleEntityName, Integer iGenericObjectId, Map<EntityAndFieldName, String> collSubEntities) {
		if (iGenericObjectId == null) {
			throw new NullArgumentException("iGenericObjectId");
		}

		Long longId = IdUtils.toLongId(iGenericObjectId);
		/** @todo eliminate this workaround: */
		final MasterDataMetaProvider cache = SpringApplicationContextHolder.getBean(MasterDataMetaProvider.class);
		for (String sEntityName : this.getEntityNames()) {
			if (cache != null) {
				final MasterDataMetaVO mdmetavo = cache.getMetaData(sEntityName);
				if (mdmetavo.isEditable()) {
					for (EntityObjectVO mdvo : this.getData(sEntityName)) {
						String foreignKeyIdField = null;
						for (EntityAndFieldName entityAndFieldName : collSubEntities.keySet()) {
							if (entityAndFieldName.getEntityName().equals(sEntityName)) {
								foreignKeyIdField = entityAndFieldName.getFieldName();
								break;
							}
						}
						if (foreignKeyIdField == null)
							foreignKeyIdField = getForeignKeyField(mdmetavo, moduleEntityName, false);
						final Long iOldGenericObjectId = mdvo.getFieldId(foreignKeyIdField);
						if (iOldGenericObjectId != null && !longId.equals(iOldGenericObjectId)) {
							log.warn("Bad parent id in dependant masterdata record; old id: " + iOldGenericObjectId + ", new id: " + longId + ".");
						}
						if (iOldGenericObjectId == null || (!longId.equals(iOldGenericObjectId) && mdvo.isFlagUpdated())) {
							mdvo.getFieldIds().put(foreignKeyIdField, longId);
						}
					}
				}
			}
		}
	}

	public static String getForeignKeyField(MasterDataMetaVO mdmetavo, String foreignEntityName) {
		return getForeignKeyField(mdmetavo, foreignEntityName, true);
	}

	public static String getForeignKeyField(MasterDataMetaVO mdmetavo, String foreignEntityName, boolean withId) {
		final String foreignKeyField;
		final Set<String> foreignKeyFields = new TreeSet<String>();
		for (MasterDataMetaFieldVO field : mdmetavo.getFields()) {
			if (foreignEntityName.equals(field.getForeignEntity())) {
				foreignKeyFields.add(field.getFieldName());
			}
		}
		switch (foreignKeyFields.size()) {
		case 0:
			foreignKeyField = ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME;
			break;
		case 1:
			foreignKeyField = foreignKeyFields.iterator().next();
			break;
		default:
			// more than one...
			if (foreignKeyFields.contains(ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME)) {
				foreignKeyField = ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME;
			} else {
				foreignKeyField = foreignKeyFields.iterator().next();
			}
		}
		if (withId) {
			return foreignKeyField + "Id";
		} else {
			return foreignKeyField;
		}
	}
	
	public boolean getPendingChanges() {
		for (EntityObjectVO eo : mmp.getAllValues()) {
			if (eo.isFlagNew() || eo.isFlagRemoved() || eo.isFlagUpdated())
				return true;
			if (eo.getDependants().getPendingChanges())
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("DependantMasterDataMap[");
		result.append(getEntityNames());
		result.append("]");
		return result.toString();
	}

}	// class DependantMasterDataMap
