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
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.MasterDataMetaProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.common.collection.multimap.MultiListHashMap;
import org.nuclos.common.collection.multimap.MultiListMap;
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
public class DependantMasterDataMapForRule implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(DependantMasterDataMapForRule.class);

	protected final MultiListMap<String, MasterDataVO> mmp = new MultiListHashMap<String, MasterDataVO>();

	/**
	 * creates an empty map.
	 */
	public DependantMasterDataMapForRule() {
	}

	/**
	 * @param sDependantEntityName
	 * @param collmdvoDependants Collection<MasterDataVO>
	 * @precondition collmdvoDependants != null
	 * @postcondition this.get(sDependantEntityName).equals(collmdvoDependants)
	 */
	@Deprecated
	public DependantMasterDataMapForRule(String sDependantEntityName, Collection<MasterDataVO> collmdvoDependants) {
		this.addAllValues(sDependantEntityName, collmdvoDependants);
	}

	/**
	 * @param sDependantEntityName
	 * @return the dependants belonging to the given entity, if any.
	 * @postcondition result != null
	 */
	public Collection<MasterDataVO> getValues(String sDependantEntityName) {
		return this.mmp.getValues(sDependantEntityName);
	}

	/**
	 * @return all dependants for all entities.
	 * @todo consider eliminating this method
	 */
	public Collection<MasterDataVO> getAllValues() {
		return CollectionUtils.concatAll(this.mmp.asMap().values());
	}

	/**
	 * puts the given <code>MasterDataVO</code> into this map.
	 * @param sDependantEntityName
	 * @param mdvoDependant
	 */
	public void addValue(String sDependantEntityName, MasterDataVO mdvoDependant) {
		this.mmp.addValue(sDependantEntityName, mdvoDependant);
	}

	/**
	 * adds all elements of <code>collmdvoDependants</code> to this map.
	 * Note that if the given <code>collmdvoDependants</code> is empty, nothing will be added.
	 * @param sDependantEntityName
	 * @param collmdvoDependants
	 * @precondition collmdvoDependants != null
	 * @postcondition this.getValues(sDependantEntityName).containsAll(collvalue)
	 */
	public void addAllValues(String sDependantEntityName, Collection<MasterDataVO> collmdvoDependants) {
		this.mmp.addAllValues(sDependantEntityName, collmdvoDependants);
	}


	/**
	 * @param sDependantEntityName
	 * @param collmdvoDependants
	 * @precondition collmdvoDependants != null
	 * @postcondition this.getValues(sDependantEntityName).size() == collmdvoDependants.size()
	 * @postcondition this.getValues(sDependantEntityName).containsAll(collmdvoDependants)
	 */
	public void setValues(String sDependantEntityName, Collection<MasterDataVO> collmdvoDependants) {
		this.mmp.removeKey(sDependantEntityName);
		this.mmp.addAllValues(sDependantEntityName, collmdvoDependants);
		assert this.getValues(sDependantEntityName).size() == collmdvoDependants.size();
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
		return CollectionUtils.forall(this.mmp.getAllValues(), PredicateUtils.transformedInputIsNull(new MasterDataVO.GetId()));
	}

	/**
	 * sets the parent id of masterdata records.
	 * @param iGenericObjectId
	 */
	public void setParent(String moduleEntityName, Integer iGenericObjectId) {
		if (iGenericObjectId == null) {
			throw new NullArgumentException("iGenericObjectId");
		}
		/** @todo eliminate this workaround: */
		for (String sEntityName : this.getEntityNames()) {
			MasterDataMetaProvider cache = SpringApplicationContextHolder.getBean(MasterDataMetaProvider.class);
			if (cache != null) {
				final MasterDataMetaVO mdmetavo = cache.getMetaData(sEntityName);
				if (mdmetavo.isEditable()) {
					for (MasterDataVO mdvo : this.getValues(sEntityName)) {
						String foreignKeyIdField = getForeignKeyField(mdmetavo, moduleEntityName);
						final Integer iOldGenericObjectId = mdvo.getField(foreignKeyIdField, Integer.class);
						if (iOldGenericObjectId != null && !iGenericObjectId.equals(iOldGenericObjectId)) {
							log.warn("Bad parent id in dependant masterdata record; old id: " + iOldGenericObjectId + ", new id: " + iGenericObjectId + ".");
						}
						if (iOldGenericObjectId == null || (!iGenericObjectId.equals(iOldGenericObjectId) && mdvo.isChanged())) {
							mdvo.getFields().put(foreignKeyIdField, iGenericObjectId);
						}
					}
				}
			}
		}
	}

	public static String getForeignKeyField(MasterDataMetaVO mdmetavo, String foreignEntityName) {
		String foreignKeyField = ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME;
		for (MasterDataMetaFieldVO field : mdmetavo.getFields()) {
			if (foreignEntityName.equals(field.getForeignEntity())) {
				foreignKeyField = field.getFieldName();
				break;
			}
		}
		return foreignKeyField + "Id";
	}



}	// class DependantMasterDataMap
