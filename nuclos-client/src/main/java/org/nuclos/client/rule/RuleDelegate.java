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
package org.nuclos.client.rule;

import java.util.Collection;
import java.util.List;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.server.common.NuclosUpdateException;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeRemote;
import org.nuclos.server.ruleengine.valueobject.RuleEngineGenerationVO;
import org.nuclos.server.ruleengine.valueobject.RuleEngineTransitionVO;
import org.nuclos.server.ruleengine.valueobject.RuleEventUsageVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.ruleengine.valueobject.RuleWithUsagesVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;

/**
 * Business Delegate for <code>RuleEngineFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class RuleDelegate {

	private static RuleDelegate singleton;

	private RuleEngineFacadeRemote facade;

	public static synchronized RuleDelegate getInstance() {
		if (singleton == null) {
			singleton = new RuleDelegate();
		}
		return singleton;
	}

	private RuleDelegate() {
	}

	/**
	 * gets the facade once for this object and stores it in a member variable.
	 */
	private RuleEngineFacadeRemote getRuleEngineFacade() throws NuclosFatalException {
		if (this.facade == null)
			this.facade = ServiceLocator.getInstance().getFacade(RuleEngineFacadeRemote.class);
		return this.facade;
	}

	/**
	 * @return all rules defined in this application.
	 * @throws NuclosFatalException
	 */
	public Collection<RuleVO> getAllRules() throws NuclosFatalException {
		try {
			return this.getRuleEngineFacade().getAllRules();
		}	catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * Get all object generations.
	 * @throws NuclosFatalException
	 */
	public Collection<GeneratorActionVO> getAllAdGenerations() throws NuclosFatalException {
		try {
			return this.getRuleEngineFacade().getAllGenerations();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * Get all object generations that have a rule assigned.
	 */
	public Collection<GeneratorActionVO> getAllAdGenerationsWithRule() throws NuclosFatalException {
		try {
			return this.getRuleEngineFacade().getAllGenerationsWithRule();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * Get all object generations that have a certain rule assigned.
	 */
	public Collection<GeneratorActionVO> getAllAdGenerationsForRuleId(Integer aRuleId) {
		try {
			return this.getRuleEngineFacade().getAllGenerationsForRuleId(aRuleId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * Get all RuleGeneration for the given rule.
	 */
	public Collection<RuleEngineGenerationVO> getAllRuleGenerationsForRuleId(Integer aRuleId)  {
		try {
			return this.getRuleEngineFacade().getAllRuleGenerationsForRuleId(aRuleId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * Get all Rule Transition that have the given rule assigned.
	 * @throws CommonPermissionException
	 */
	public Collection<RuleEngineTransitionVO> getAllRuleTransitionsForRuleId(Integer aRuleId)  {
		try {
			return this.getRuleEngineFacade().getAllRuleTransitionsForRuleId(aRuleId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * Get all Rule Transition that have the given transition assigned.
	 */
	public Collection<RuleEngineTransitionVO> getAllRuleTransitionsForTransitionId(Integer aTransitionId) {
		try {
			return this.getRuleEngineFacade().getAllRuleTransitionsForTransitionId(aTransitionId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * Get all RuleGeneration for the given generation.
	 */
	public Collection<RuleEngineGenerationVO> getAllRuleGenerationsForGenerationId(Integer aRuleId)  {
		try {
			return this.getRuleEngineFacade().getAllRuleGenerationsForGenerationId(aRuleId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * Get all state Models that have a given rule assigned.
	 */
	public Collection<StateModelVO> getAllStateModelsForRuleId(Integer aRuleId)  {
		try {
			return this.getRuleEngineFacade().getAllStateModelsForRuleId(aRuleId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * Get a collection of rules by the eventname independent of the module.
	 * @return Collection<RuleVO>
	 */
	public List<RuleVO> getByEventOrdered(String sEventName) {
		try {
			return this.getRuleEngineFacade().getByEventOrdered(sEventName);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * Get a collection of rules by Eventname and entityId (ordered).
	 * @return Collection<RuleVO>
	 */
	public List<RuleVO> getByEventAndEntityOrdered(String sEventName, String sEntity) {
		try {
			return this.getRuleEngineFacade().getByEventAndEntityOrdered(sEventName, sEntity);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * Get all rule usages of a rule for a certain event.
	 * @return Collection<RuleVO>
	 */
	public Collection<RuleEventUsageVO> getByEventAndRule(String sEventName, Integer iRuleId) {
		try {
			return this.getRuleEngineFacade().getByEventAndRule(sEventName, iRuleId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}
	
   /**
    * finds rules by usage criteria
    * @param usagecriteria
    * @param sEventName
    * @return collection of rule
    */
   public Collection<RuleVO> findRulesByUsageAndEvent(String sEventName, UsageCriteria usagecriteria) {
	   try {
			return this.getRuleEngineFacade().findRulesByUsageAndEvent(sEventName, usagecriteria);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
   }

   /**
    * Get all referenced entity names for a certain rule event.
    * @return collection of entity names
    * @throws CommonFatalException, CommonFatalException
    */
	public Collection<String> getRuleUsageEntityNamesByEvent(String sEventName) {
		try {
			return this.getRuleEngineFacade().getRuleUsageEntityNamesByEvent(sEventName);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * Get all Rule engine Generations.
	 * @return Collection<RuleVO>
	 */
	public Collection<RuleEngineGenerationVO> getAllRuleEngineGenerations() {
		try {
			return this.getRuleEngineFacade().getAllRuleEngineGenerations();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	public RuleVO get(Integer iRuleId) throws CommonFinderException {
		RuleVO rulevo = null;
		try {
			rulevo = getRuleEngineFacade().get(iRuleId);
		}
		catch (RuntimeException e) {
			/** @todo If the server is down, we get a FinderException! */
			throw new CommonFinderException(e);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
		return rulevo;
	}

	//NUCLOSINT-743 addititional method for resolving rulename to id
	public RuleVO get(String ruleName) throws CommonFinderException {
		RuleVO rulevo = null;
		try {
			rulevo = getRuleEngineFacade().get(ruleName);
		}
		catch (RuntimeException e) {
			/** @todo If the server is down, we get a FinderException! */
			throw new CommonFinderException(e);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
		return rulevo;
	}

	public void remove(RuleVO rulevo) throws CommonStaleVersionException, CommonRemoveException, CommonFinderException, NuclosBusinessRuleException, NuclosCompileException {
		try {
			getRuleEngineFacade().remove(rulevo);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * @param rulevo
	 * @param mpmdvoDependants May be null.
	 * @return
	 * @throws CommonBusinessException
	 */
	public RuleVO update(RuleVO rulevo, DependantMasterDataMap mpmdvoDependants) throws CommonBusinessException {
		try {
			return getRuleEngineFacade().modify(rulevo, mpmdvoDependants);
		}
		catch (RuntimeException ex) {
			throw new NuclosUpdateException(ex.getMessage(), ex);
		}
	}

	public RuleVO create(RuleVO rulevo, DependantMasterDataMap mpmdvoDependants) throws CommonBusinessException {
		try {
			return getRuleEngineFacade().create(rulevo, mpmdvoDependants);
		}
		catch (RuntimeException ex) {
			throw new NuclosBusinessException(ex.getMessage(), ex);
		}
	}

	public void createRuleUsageForId(String eventName, String entity, Integer processId, Integer statusId, Integer ruleToInsertId, Integer ruleBeforeId) throws CommonBusinessException {

		try {
			getRuleEngineFacade().createRuleUsageInEntity(eventName, entity, processId, statusId, ruleToInsertId, ruleBeforeId);
		}
		catch (RuntimeException ex) {
			throw new CommonBusinessException(ex);
		}
	}

	public void moveRuleUsageForId(String eventName, String entity, Integer processId, Integer statusId, Integer ruleToInsertId, Integer ruleBeforeId) throws CommonBusinessException {

		try {
			getRuleEngineFacade().moveRuleUsageInEntity(eventName, entity, processId, statusId, ruleToInsertId, ruleBeforeId);
		}
		catch (RuntimeException ex) {
			throw new CommonBusinessException(ex);
		}
	}

	public void removeRuleUsage(String eventName, String entity, Integer processId, Integer statusId, Integer ruleToRemove) throws CommonBusinessException {

		try {
			getRuleEngineFacade().removeRuleUsage(eventName, entity, processId, statusId, ruleToRemove);
		}
		catch (RuntimeException ex) {
			throw new CommonBusinessException(ex);
		}
	}

	/**
	 * imports the given rules, adding new and overwriting existing rules. The other existing rules are untouched.
	 * Currently, only the rule code is imported, not the usages.
	 * @param collRuleWithUsages Collection<RuleWithUsagesVO>
	 */
	public void importRules(Collection<RuleWithUsagesVO> collRuleWithUsages) throws CommonBusinessException {
		try {
			getRuleEngineFacade().importRules(collRuleWithUsages);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * Compile the user defined code.
	 * @param ruleVO
	 * @throws NuclosCompileException
	 */
	public void compile(RuleVO ruleVO) throws NuclosCompileException {
		try {
			this.getRuleEngineFacade().check(ruleVO);
		}
		catch (RuntimeException e) {
			throw new NuclosFatalException(e);
		}
	}

	/**
	 * Returns a template for new rules to display in the rule editor.
	 */
	public String getClassTemplate() throws CommonBusinessException {
		try {
			return this.getRuleEngineFacade().getClassTemplate();
		}
		catch (RuntimeException e) {
			throw new CommonBusinessException(e);
		}
	}

	public void invalidateCache() {
		try {
			this.getRuleEngineFacade().invalidateCache();
		}
		catch (RuntimeException e) {
			throw new NuclosFatalException(e);
		}
	}

}	// class RuleDelegate
