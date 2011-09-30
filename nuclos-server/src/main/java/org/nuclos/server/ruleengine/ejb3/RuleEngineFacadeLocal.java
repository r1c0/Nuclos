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
package org.nuclos.server.ruleengine.ejb3;

import java.util.Collection;
import java.util.List;

import javax.ejb.Local;

import org.nuclos.common.PropertiesMap;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.valueobject.RuleEngineTransitionVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

@Local
public interface RuleEngineFacadeLocal {

	/**
	 * fires an event by finding all rules that correspond
	 * to the given module id and event name and by executing these rules.
	 * @param iModuleId module id to fire event for
	 * @param sEventName event name to be fired
	 * @param loccvoCurrent current leased object as parameter for rules
	 * @return the possibly change current object.
	 * @precondition iModuleId != null
	 * @precondition Modules.getInstance().getUsesRuleEngine(iModuleId.intValue())
	 */
	public abstract RuleObjectContainerCVO fireRule(String sEntity,
		String sEventName, RuleObjectContainerCVO loccvoCurrent)
		throws NuclosBusinessRuleException;

	/**
	 * fires a transition event by finding all rules that correspond
	 * to the given source and target state ids and by executing these rules.
	 * @param iSourceStateId source state id to fire event for in combination with target state id
	 * @param iTargetStateId target state id to fire event for in combination with source state id
	 * @param loccvoCurrent current leased object as parameter for rules
	 * @return the possibly change current object.
	 */
	public abstract RuleObjectContainerCVO fireRule(Integer sourceStateId,
		Integer targetStateId, RuleObjectContainerCVO ruleContainer, Boolean after)
		throws NuclosBusinessRuleException;

	/**
	 * fires the rules for a specific object generation.
	 */
	public abstract RuleObjectContainerCVO fireGenerationRules(Integer iGenerationId, RuleObjectContainerCVO tgtRuleObject, Collection<RuleObjectContainerCVO> srcRuleObjects, RuleObjectContainerCVO parameterRuleObject, List<String> actions, PropertiesMap properties, Boolean after) throws NuclosBusinessRuleException;

	/**
	 * executes the given lstRules of business rules.
	 * @param lstRules List<RuleEngineRuleLocal>
	 * @param loccvoCurrent current leased object as parameter for rules
	 * @param bIgnoreExceptions
	 * @return the possibly change current object.
	 * @throws NuclosBusinessRuleException
	 */
	public abstract RuleObjectContainerCVO executeBusinessRules(
		List<RuleVO> lstRules, RuleObjectContainerCVO loccvoCurrent,
		boolean bIgnoreExceptions) throws NuclosBusinessRuleException;

	/**
	 * Get all Rule Transition that have the given transition assigned.
	 * @return Collection<RuleEngineTransitionVO>
	 * @throws CommonPermissionException
	 */
	public abstract Collection<RuleEngineTransitionVO> getAllRuleTransitionsForTransitionId(
		Integer transitionId) throws CommonPermissionException;

	/**
	 * gets a rule definition from the database by primary key.
	 * @param iId primary key of rule definition
	 * @return rule value object
	 * @throws CommonPermissionException
	 */
	public abstract RuleVO get(Integer iId) throws CommonFinderException,
		CommonPermissionException;

	/**
	 * gets a rule definition from the database by name.
     * NUCLOSINT-743
	 * @param ruleName Name of rule definition
	 * @return rule value object
	 * @throws CommonPermissionException
	 */
	public abstract RuleVO get(String ruleName) throws CommonFinderException,
		CommonPermissionException;

}
