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
package org.nuclos.server.statemodel.valueobject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nuclos.common.collection.Pair;
import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Value object representing a state transition.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.00
 */
public class StateTransitionVO extends NuclosValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Integer iClientId;
	private Integer iStateSource;
	private Integer iStateTarget;
	private String sDescription;
	private boolean bAutomatic;
	private boolean bDefault;
	private List<Pair<Integer, Boolean>> lstRuleIdsWithRunAfterwards = new ArrayList<Pair<Integer, Boolean>>();
	private List<Integer> lstRoleIds = new ArrayList<Integer>();

	/**
	 * constructor to be called by server only
	 * @param iId primary key of underlying database record
	 * @param iStateSource id of state source for transition
	 * @param iStateTarget id of state target for transition
	 * @param sDescription description of transition
	 * @param bAutomatic automatic transition only?
	 * @param dCreated creation date of underlying database record
	 * @param sCreated creator of underlying database record
	 * @param dChanged last changed date of underlying database record
	 * @param sChanged last changer of underlying database record
	 */
	public StateTransitionVO(Integer iId, Integer iStateSource, Integer iStateTarget, String sDescription,
			boolean bAutomatic, boolean bDefault, Date dCreated, String sCreated, Date dChanged, String sChanged,
			Integer iVersion) {
		super(iId, dCreated, sCreated, dChanged, sChanged, iVersion);
		this.iClientId = iId;
		this.iStateSource = iStateSource;
		this.iStateTarget = iStateTarget;
		this.sDescription = sDescription;
		this.bAutomatic = bAutomatic;
		this.bDefault = bDefault;
	}

	/**
	 * constructor to be called by client only
	 * @param iStateSource id of state source for transition
	 * @param iStateTarget id of state target for transition
	 * @param bAutomatic automatic transition only?
	 */
	public StateTransitionVO(Integer iClientId, Integer iStateSource, Integer iStateTarget, String sDescription,
			boolean bAutomatic, boolean bDefault) {
		super();
		this.iClientId = iClientId;
		this.iStateSource = iStateSource;
		this.iStateTarget = iStateTarget;
		this.sDescription = sDescription;
		this.bAutomatic = bAutomatic;
		this.bDefault = bDefault;
	}

	/**
	 * get copy of primary key of underlying database record (or id of new transition inserted by client)
	 * @return primary key of underlying database record
	 */
	public Integer getClientId() {
		return iClientId;
	}

	/**
	 * get state source of underlying database record
	 * @return state source of underlying database record
	 */
	public Integer getStateSource() {
		return iStateSource;
	}

	/**
	 * set state source of underlying database record
	 * @param iStateSource state source of underlying database record
	 */
	public void setStateSource(Integer iStateSource) {
		this.iStateSource = iStateSource;
	}

	/**
	 * get state target of underlying database record
	 * @return state target of underlying database record
	 */
	public Integer getStateTarget() {
		return iStateTarget;
	}

	/**
	 * set state target of underlying database record
	 * @param iStateTarget state target of underlying database record
	 */
	public void setStateTarget(Integer iStateTarget) {
		this.iStateTarget = iStateTarget;
	}

	/**
	 * get automatic flag of underlying database record
	 * @return automatic flag of underlying database record
	 */
	public boolean isAutomatic() {
		return bAutomatic;
	}

	/**
	 * set automatic flag of underlying database record
	 * @param bAutomatic automatic flag of underlying database record
	 */
	public void setAutomatic(boolean bAutomatic) {
		this.bAutomatic = bAutomatic;
	}

	/**
	 * get default flag of underlying database record
	 * @return default flag of underlying database record
	 */
	public boolean isDefault() {
		return bDefault;
	}

	/**
	 * set default flag of underlying database record
	 * @param bDefault default flag of underlying database record
	 */
	public void setDefault(boolean bDefault) {
		this.bDefault = bDefault;
	}

	/**
	 * get description of underlying database record
	 * @return description of underlying database record
	 */
	public String getDescription() {
		return sDescription;
	}

	/**
	 * set description of underlying database record
	 * @param sDescription description of underlying database record
	 */
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}
	
	public void removeRule(Integer ruleId) {
		lstRuleIdsWithRunAfterwards.remove(new Pair<Integer, Boolean>(ruleId, Boolean.TRUE));
		lstRuleIdsWithRunAfterwards.remove(new Pair<Integer, Boolean>(ruleId, Boolean.FALSE));
		lstRuleIdsWithRunAfterwards.remove(new Pair<Integer, Boolean>(ruleId, null));
	}
	
	public List<Integer> getRuleIds() {
		List<Integer> result = new ArrayList<Integer>();
		for (Pair<Integer, Boolean> p : lstRuleIdsWithRunAfterwards) {
			result.add(p.x);
		}
		return result;
	}

	/**
	 * get list of rules attached to transition
	 * @return list of rules attached to transition
	 */
	public List<Pair<Integer, Boolean>> getRuleIdsWithRunAfterwards() {
		return lstRuleIdsWithRunAfterwards;
	}

	/**
	 * attach list of rules to transition
	 * @param lstRuleIdsWithRunAfterwards list of rules to attach to transition
	 */
	public void setRuleIdsWithRunAfterwards(List<Pair<Integer, Boolean>> lstRuleIdsWithRunAfterwards) {
		this.lstRuleIdsWithRunAfterwards = lstRuleIdsWithRunAfterwards;
	}

	/**
	 * get list of roles attached to transition
	 * @return list of roles attached to transition
	 */
	public List<Integer> getRoleIds() {
		return lstRoleIds;
	}

	/**
	 * attach list of roles to transition
	 * @param lstRoles list of roles to attach to transition
	 */
	public void setRoleIds(List<Integer> lstRoles) {
		this.lstRoleIds = lstRoles;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",stateSrc=").append(getStateSource());
		result.append(",stateTarget=").append(getStateTarget());
		result.append(",clientId=").append(getClientId());
		result.append("]");
		return result.toString();
	}

}	// class StateTransitionVO
