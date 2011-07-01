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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.EntityObjectToMasterDataTransformer;
import org.nuclos.common.collection.MasterDataToEntityObjectTransformer;
import org.nuclos.server.common.ModuleConstants;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMapForRule;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Contains a rule object (generic object or master data) along with its dependants.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @author	<a href="mailto:Corina.Mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */
public class RuleObjectContainerCVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GenericObjectVO govo;
	private MasterDataVO mdvo;

	private final DependantMasterDataMapForRule mpDependants;
	
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
	public RuleObjectContainerCVO(Event event, GenericObjectVO govo, DependantMasterDataMap mpDependants) {
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
		this.mpDependants = convert(mpDependants);
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
	public RuleObjectContainerCVO(Event event, MasterDataVO mdvo, DependantMasterDataMap mpDependants) {
		if (mdvo == null) {
			throw new NullPointerException("mdvo");
		}
		if (mpDependants == null) {
			throw new NullArgumentException("mpDependants");
		}
		if (event == null) {
			throw new NullArgumentException("event");
		}
		this.mdvo = mdvo;
		this.mpDependants = convert(mpDependants);
		this.event = event;
	}

	/**
	 * @return the contained generic object
	 * @postcondition result != null
	 */
	public GenericObjectVO getGenericObject() {
		return govo;
	}
	
	/**
	 * @return the contained master data
	 * @postcondition result != null
	 */
	public MasterDataVO getMasterData() {
		return mdvo;
	}

	/** @todo try to avoid this method. */
	public void setGenericObject(GenericObjectVO govo) {
		this.govo = govo;
	}

	/** @todo try to avoid this method. */
	public void setMasterData(MasterDataVO mdvo) {
		this.mdvo = mdvo;
	}
	
	/**
	 * @return the contained map of dependant masterdata records.
	 * @postcondition result != null
	 */
	public DependantMasterDataMap getDependants() {
		/** @todo make unmodifiable */		
		return convert(getDependantsWithoutDeletedVOs(this.mpDependants));
	}
	
	public DependantMasterDataMap getDependants(boolean withDeleted) {
		if(withDeleted)
			return getDependants();
		return convert(mpDependants);
	}

	public void addDependant(String sDependantEntity, MasterDataVO entry) {
		mpDependants.addValue(sDependantEntity, entry);
	}
	
	/**
	 * @param sEntityName
	 * @return Collection<MasterDataVO> the leased object's dependants of the given entity. This Collection is not modifiable.
	 * @postcondition result != null
	 */
	public Collection<MasterDataVO> getDependants(String sEntityName) {
		Collection<MasterDataVO> result;
		if (this.getGenericObject() != null) {
			result = this.getDependants(sEntityName, ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME);
		}
		else {
			result = CollectionUtils.transform(this.getMasterData().getDependants().getData(sEntityName), 
					new EntityObjectToMasterDataTransformer());
				
		}
		return getDependantsWithoutDeletedVOs(result);
	}
	
	/**
	 * 
	 * @param sEntityName
	 * @return
	 */
	public Collection<MasterDataVO> getDependantsWithDeleted(String sEntityName) {
		Collection<MasterDataVO> result;
		if (this.getGenericObject() != null) {
			Collection<MasterDataVO> col = CollectionUtils.transform(this.getDependants().getData(sEntityName), 
				new EntityObjectToMasterDataTransformer());
			result = Collections.unmodifiableCollection(col);		
		}
		else {
			Collection<MasterDataVO> col = CollectionUtils.transform(this.getMasterData().getDependants().getData(sEntityName), 
					new EntityObjectToMasterDataTransformer());
			result = col;
		}
		return result;
	}

	/**
	 * set the leased objects dependants of the given entity.
	 * @param sEntityName
	 * @param collmdvo
	 * @postcondition result != null
	 */
	public void setDependants(String sEntityName, Collection<MasterDataVO> collmdvo) {
				
		this.mpDependants.setValues(sEntityName, collmdvo);
	}

	/**
	 * @param sEntityName
	 * @param sForeignKeyFieldName
	 * @return Collection<MasterDataVO> the leased object's dependants of the given entity, using the given foreign key field to this object.
	 * This Collection is not modifiable.
	 * @todo sForeignKeyFieldName must not be ignored!
	 * @postcondition result != null
	 */
	public Collection<MasterDataVO> getDependants(String sEntityName, String sForeignKeyFieldName) {
		return Collections.unmodifiableCollection(getDependantsWithoutDeletedVOs(this.mpDependants.getValues(sEntityName)));
	}
	
	private DependantMasterDataMapForRule getDependantsWithoutDeletedVOs(DependantMasterDataMapForRule mpDependants) {
		DependantMasterDataMapForRule result = new DependantMasterDataMapForRule();
		
		for (String entity : mpDependants.getEntityNames()) {
			for (MasterDataVO mdvo : mpDependants.getValues(entity)) {
				if (!mdvo.isRemoved()) {
					result.addValue(entity, mdvo);
				}
			}
		}
		
		return result;
	}
	
	private Collection<MasterDataVO> getDependantsWithoutDeletedVOs(Collection<MasterDataVO> colldependants) {
		Collection<MasterDataVO> result = new ArrayList<MasterDataVO>();
		
		for (MasterDataVO mdvo : colldependants) {
			if (!mdvo.isRemoved()) {
				result.add(mdvo);
			}
		}
		
		return result;
	}

	/**
	 * @return the iTargetStateId
	 */
	public Integer getTargetStateId() {
		return iTargetStateId;
	}

	/**
	 * @return the iTargetStateNum
	 */
	public Integer getTargetStateNum() {
		return iTargetStateNum;
	}

	/**
	 * @return the iTargetStateName
	 */
	public String getTargetStateName() {
		return sTargetStateName;
	}

	/**
	 * @return the iSourceStateId
	 */
	public Integer getSourceStateId() {
		return iSourceStateId;
	}

	/**
	 * @return the iSourceStateNum
	 */
	public Integer getSourceStateNum() {
		return iSourceStateNum;
	}

	/**
	 * @return the iSourceStateName
	 */
	public String getSourceStateName() {
		return sSourceStateName;
	}

	/**
	 * @param iTargetStateId the iTargetStateId to set
	 */
	public void setTargetStateId(Integer iTargetStateId) {
		this.iTargetStateId = iTargetStateId;
	}

	/**
	 * @param iTargetStateNum the iTargetStateNum to set
	 */
	public void setTargetStateNum(Integer iTargetStateNum) {
		this.iTargetStateNum = iTargetStateNum;
	}

	/**
	 * @param sTargetStateName the sTargetStateName to set
	 */
	public void setTargetStateName(String sTargetStateName) {
		this.sTargetStateName = sTargetStateName;
	}

	/**
	 * @param iSourceStateId the iSourceStateId to set
	 */
	public void setSourceStateId(Integer iSourceStateId) {
		this.iSourceStateId = iSourceStateId;
	}

	/**
	 * @param iSourceStateNum the iSourceStateNum to set
	 */
	public void setSourceStateNum(Integer iSourceStateNum) {
		this.iSourceStateNum = iSourceStateNum;
	}

	/**
	 * @param sSourceStateName the sSourceStateName to set
	 */
	public void setSourceStateName(String sSourceStateName) {
		this.sSourceStateName = sSourceStateName;
	}

	/**
	 * @return the event
	 */
	public Event getEvent() {
		return event;
	}
	
	private static DependantMasterDataMap convert(DependantMasterDataMapForRule dep) {
		DependantMasterDataMap map = new DependantMasterDataMap();
		for(String key : dep.getEntityNames()) {
			map.setData(key, CollectionUtils.transform(dep.getValues(key), new MasterDataToEntityObjectTransformer()));			
		}
		
		return map;
	}
	
	private static DependantMasterDataMapForRule convert(DependantMasterDataMap dep) {
		DependantMasterDataMapForRule map = new DependantMasterDataMapForRule();
		for(String key : dep.getEntityNames()) {
			map.setValues(key, CollectionUtils.transform(dep.getData(key), new EntityObjectToMasterDataTransformer()));
		}
		return map;
	}
	

}	// class RuleObjectContainerCVO
