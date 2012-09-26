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
package org.nuclos.server.ruleengine.valueobject;

import java.util.Collection;

import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVOImpl.Event;

/**
 * Interface to {@link org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVOImpl}.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.8
 */
public interface RuleObjectContainerCVO {

	/**
	 * @return the contained generic object
	 * @postcondition result != null
	 */
	GenericObjectVO getGenericObject();

	/**
	 * @return the contained master data
	 * @postcondition result != null
	 */
	MasterDataVO getMasterData();

	/** @todo try to avoid this method. */
	void setGenericObject(GenericObjectVO govo);

	/**
	 * @return the contained map of dependant masterdata records.
	 * @postcondition result != null
	 */
	DependantMasterDataMap getDependants();

	DependantMasterDataMap getDependants(boolean withDeleted);

	void addDependant(String sDependantEntity, MasterDataVO entry);

	/**
	 * @param sEntityName
	 * @return Collection<MasterDataVO> the leased object's dependants of the given entity. This Collection is not modifiable.
	 * @postcondition result != null
	 */
	Collection<MasterDataVO> getDependants(String sEntityName);

	/**
	 *
	 * @param sEntityName
	 * @return
	 */
	Collection<MasterDataVO> getDependantsWithDeleted(String sEntityName);

	/**
	 * set the leased objects dependants of the given entity.
	 * @param sEntityName
	 * @param collmdvo
	 * @postcondition result != null
	 */
	void setDependants(String sEntityName, Collection<MasterDataVO> collmdvo);

	/**
	 * @param sEntityName
	 * @param sForeignKeyFieldName
	 * @return Collection<MasterDataVO> the leased object's dependants of the given entity, using the given foreign key field to this object.
	 * This Collection is not modifiable.
	 * @todo sForeignKeyFieldName must not be ignored!
	 * @postcondition result != null
	 */
	Collection<MasterDataVO> getDependants(String sEntityName, String sForeignKeyFieldName);

	/**
	 * @return the iTargetStateId
	 */
	Integer getTargetStateId();

	/**
	 * @return the iTargetStateNum
	 */
	Integer getTargetStateNum();

	/**
	 * @return the iTargetStateName
	 */
	String getTargetStateName();

	/**
	 * @return the iSourceStateId
	 */
	Integer getSourceStateId();

	/**
	 * @return the iSourceStateNum
	 */
	Integer getSourceStateNum();

	/**
	 * @return the iSourceStateName
	 */
	String getSourceStateName();

	/**
	 * @param iTargetStateId the iTargetStateId to set
	 */
	void setTargetStateId(Integer iTargetStateId);

	/**
	 * @param iTargetStateNum the iTargetStateNum to set
	 */
	void setTargetStateNum(Integer iTargetStateNum);

	/**
	 * @param sTargetStateName the sTargetStateName to set
	 */
	void setTargetStateName(String sTargetStateName);

	/**
	 * @param iSourceStateId the iSourceStateId to set
	 */
	void setSourceStateId(Integer iSourceStateId);

	/**
	 * @param iSourceStateNum the iSourceStateNum to set
	 */
	void setSourceStateNum(Integer iSourceStateNum);

	/**
	 * @param sSourceStateName the sSourceStateName to set
	 */
	void setSourceStateName(String sSourceStateName);

	/**
	 * @return the event
	 */
	Event getEvent();

}
