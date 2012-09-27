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
package org.nuclos.server.ruleengine.valueobject;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMapImpl;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMapNonDeletedView;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Contains a rule object (generic object or master data) along with its dependants.
 * <p>
 * Since Nuclos 3.8 there is no more copying between DependantMasterDataMapForRule and
 * the 'normal' DependantMasterDataMap. Instead MasterDataVO is a thin wrapper on top
 * of EntityObjectVO. No more toggleing between the twos as there are now the same!
 * </p>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @author	<a href="mailto:Corina.Mandoki@novabit.de">Corina Mandoki</a>
 * @author Thomas Pasch
 */
public final class RuleObjectContainerCVOImpl implements Serializable, RuleObjectContainerCVO {

	private GenericObjectVO govo;
	private final MasterDataVO mdvo;
	
	private final DependantMasterDataMap realMap;

	public static enum Event {
		CREATE_BEFORE("create_before"),
		CREATE_AFTER("create_after"),
		MODIFY_BEFORE("modify_before"),
		MODIFY_AFTER("modify_after"),
		DELETE_BEFORE("delete_before"),
		DELETE_AFTER("delete_after"),
		CHANGE_STATE_BEFORE("change_state_before"),
		CHANGE_STATE_AFTER("change_state_after"),
		GENERATION_BEFORE("generation_before"),
		GENERATION_AFTER("generation_after"),
		USER("user"),
		INTERFACE("interface"),
		UNDEFINED("undefined");

		private String name;

		private Event(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		public boolean isFollowUp() {
			return name.endsWith("_after");
		}
	}

	private Integer iTargetStateId;
	private Integer iTargetStateNum;
	private String sTargetStateName;

	private Integer iSourceStateId;
	private Integer iSourceStateNum;
	private String sSourceStateName;

	private final Event event;

	/**
	 * @param govo
	 * @param mpDependants
	 * @param event
	 * @precondition govo != null
	 * @precondition mpDependants != null
	 * @precondition event != null
	 */
	public RuleObjectContainerCVOImpl(Event event, GenericObjectVO govo, DependantMasterDataMap mpDependants) {
		if (govo == null) {
			throw new NullArgumentException("govo");
		}
		if (mpDependants == null) {
			throw new NullArgumentException("mpDependants");
		}
		if (event == null) {
			throw new NullArgumentException("event");
		}
		this.govo = govo;
		this.mdvo = null;
		this.realMap = mpDependants;
		this.event = event;
	}

	/**
	 * @param mdvo
	 * @param mpDependants
	 * @param event
	 * @precondition mdvo != null
	 * @precondition mpDependants != null
	 * @precondition event != null
	 */
	public RuleObjectContainerCVOImpl(Event event, MasterDataVO mdvo, DependantMasterDataMap mpDependants) {
		if (mdvo == null) {
			throw new NullPointerException("mdvo");
		}
		if (mpDependants == null) {
			throw new NullArgumentException("mpDependants");
		}
		if (event == null) {
			throw new NullArgumentException("event");
		}
		this.govo = null;
		this.mdvo = mdvo;
		this.realMap = mpDependants;
		this.event = event;
	}

	/**
	 * @return the contained generic object
	 * @postcondition result != null
	 */
	@Override
	public GenericObjectVO getGenericObject() {
		return govo;
	}

	/**
	 * @return the contained master data
	 * @postcondition result != null
	 */
	@Override
	public MasterDataVO getMasterData() {
		return mdvo;
	}

	/** @todo try to avoid this method. */
	@Override
	public void setGenericObject(GenericObjectVO govo) {
		this.govo = govo;
	}

	/**
	 * @return the contained map of dependant masterdata records.
	 * @postcondition result != null
	 */
	@Override
	public DependantMasterDataMap getDependants() {
		/** @todo make unmodifiable */
		return getDependantsWithoutDeletedVOs();
	}

	@Override
	public DependantMasterDataMap getDependants(boolean withDeleted) {
		if(!withDeleted) {
			return getDependants();
		}
		return realMap;
	}

	@Override
	public void addDependant(String sDependantEntity, MasterDataVO entry) {
		realMap.addValue(sDependantEntity, entry);
	}

	/**
	 * @param sEntityName
	 * @return Collection<MasterDataVO> the leased object's dependants of the given entity. This Collection is not modifiable.
	 * @postcondition result != null
	 */
	@Override
	public Collection<MasterDataVO> getDependants(String sEntityName) {
		/*
		Collection<MasterDataVO> result;
		if (this.getGenericObject() != null) {
			result = this.getDependants(sEntityName, ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME);
		}
		else {
			result = CollectionUtils.transform(this.getMasterData().getDependants().getData(sEntityName),
					new EntityObjectToMasterDataTransformer());

		}
		return getDependantsWithoutDeletedVOs(result);
		 */
		final DependantMasterDataMap map = new DependantMasterDataMapNonDeletedView((DependantMasterDataMapImpl) realMap);
		return map.getValues(sEntityName);
	}

	/**
	 *
	 * @param sEntityName
	 * @return
	 */
	@Override
	public Collection<MasterDataVO> getDependantsWithDeleted(String sEntityName) {
		if (realMap != null) {
			return Collections.unmodifiableCollection(realMap.getValues(sEntityName));
		}
		else {
			return null;
		}
	}

	/**
	 * set the leased objects dependants of the given entity.
	 * @param sEntityName
	 * @param collmdvo
	 * @postcondition result != null
	 */
	@Override
	public void setDependants(String sEntityName, Collection<MasterDataVO> collmdvo) {
		realMap.setValues(sEntityName, collmdvo);
	}

	/**
	 * @param sEntityName
	 * @param sForeignKeyFieldName
	 * @return Collection<MasterDataVO> the leased object's dependants of the given entity, using the given foreign key field to this object.
	 * This Collection is not modifiable.
	 * @todo sForeignKeyFieldName must not be ignored!
	 * @postcondition result != null
	 */
	@Override
	public Collection<MasterDataVO> getDependants(String sEntityName, String sForeignKeyFieldName) {
		// return Collections.unmodifiableCollection(getDependantsWithoutDeletedVOs(realMap.getValues(sEntityName)));
		return Collections.unmodifiableCollection(
				getDependantsWithoutDeletedVOs().getValues(sEntityName));
	}

	private DependantMasterDataMap getDependantsWithoutDeletedVOs() {
		if (realMap instanceof DependantMasterDataMapImpl)
			return new DependantMasterDataMapNonDeletedView((DependantMasterDataMapImpl) realMap);
		else if (realMap instanceof DependantMasterDataMapNonDeletedView)
			return realMap;
		throw new IllegalStateException(realMap.getClass().getName());
	}

	/*
	private Collection<MasterDataVO> getDependantsWithoutDeletedVOs(Collection<MasterDataVO> colldependants) {
		Collection<MasterDataVO> result = new ArrayList<MasterDataVO>();

		for (MasterDataVO mdvo : colldependants) {
			if (!mdvo.isRemoved()) {
				result.add(mdvo);
			}
		}

		return result;
	}
	 */

	/**
	 * @return the iTargetStateId
	 */
	@Override
	public Integer getTargetStateId() {
		return iTargetStateId;
	}

	/**
	 * @return the iTargetStateNum
	 */
	@Override
	public Integer getTargetStateNum() {
		return iTargetStateNum;
	}

	/**
	 * @return the iTargetStateName
	 */
	@Override
	public String getTargetStateName() {
		return sTargetStateName;
	}

	/**
	 * @return the iSourceStateId
	 */
	@Override
	public Integer getSourceStateId() {
		return iSourceStateId;
	}

	/**
	 * @return the iSourceStateNum
	 */
	@Override
	public Integer getSourceStateNum() {
		return iSourceStateNum;
	}

	/**
	 * @return the iSourceStateName
	 */
	@Override
	public String getSourceStateName() {
		return sSourceStateName;
	}

	/**
	 * @param iTargetStateId the iTargetStateId to set
	 */
	@Override
	public void setTargetStateId(Integer iTargetStateId) {
		this.iTargetStateId = iTargetStateId;
	}

	/**
	 * @param iTargetStateNum the iTargetStateNum to set
	 */
	@Override
	public void setTargetStateNum(Integer iTargetStateNum) {
		this.iTargetStateNum = iTargetStateNum;
	}

	/**
	 * @param sTargetStateName the sTargetStateName to set
	 */
	@Override
	public void setTargetStateName(String sTargetStateName) {
		this.sTargetStateName = sTargetStateName;
	}

	/**
	 * @param iSourceStateId the iSourceStateId to set
	 */
	@Override
	public void setSourceStateId(Integer iSourceStateId) {
		this.iSourceStateId = iSourceStateId;
	}

	/**
	 * @param iSourceStateNum the iSourceStateNum to set
	 */
	@Override
	public void setSourceStateNum(Integer iSourceStateNum) {
		this.iSourceStateNum = iSourceStateNum;
	}

	/**
	 * @param sSourceStateName the sSourceStateName to set
	 */
	@Override
	public void setSourceStateName(String sSourceStateName) {
		this.sSourceStateName = sSourceStateName;
	}

	/**
	 * @return the event
	 */
	@Override
	public Event getEvent() {
		return event;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("RuleObjectContainerVO[");
		if (mdvo != null) {
			result.append("mdVO=").append(mdvo.toDescription());
		}
		if (govo != null) {
			result.append("goVO=").append(govo.toDescription());
		}
		if (event != null) {
			result.append(",event=").append(event);
		}
		if (iSourceStateId != null) {
			result.append(",srcSId=").append(iSourceStateId);
		}
		if (iSourceStateNum != null) {
			result.append(",srcSNum=").append(iSourceStateNum);
		}
		if (iTargetStateId != null) {
			result.append(",targetSId=").append(iTargetStateId);
		}
		if (iTargetStateNum != null) {
			result.append(",targetStateNum").append(iTargetStateNum);
		}
		result.append("]");
		return result.toString();
	}
	
}	// class RuleObjectContainerCVO
