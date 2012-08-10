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

	private static RuleDelegate INSTANCE;
	
	// Spring injection

	private RuleEngineFacadeRemote ruleEngineFacadeRemote;
	
	// end of Spring injection

	public static RuleDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}

	RuleDelegate() {
		INSTANCE = this;
	}
	
	public final void setRuleEngineFacadeRemote(RuleEngineFacadeRemote ruleEngineFacadeRemote) {
		this.ruleEngineFacadeRemote = ruleEngineFacadeRemote;
	}

	/**
	 * @return all rules defined in this application.
	 * @throws NuclosFatalException
	 */
	public Collection<RuleVO> getAllRules() throws NuclosFatalException {
		try {
			return ruleEngineFacadeRemote.getAllRules();
		}	catch (RuntimeException ex) {
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
			return ruleEngineFacadeRemote.getAllGenerationsWithRule();
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
			return ruleEngineFacadeRemote.getAllGenerationsForRuleId(aRuleId);
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
			return ruleEngineFacadeRemote.getAllRuleGenerationsForRuleId(aRuleId);
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
	public Collection<RuleEngineTransitionVO> getAllRuleTransitions()  {
		try {
			return ruleEngineFacadeRemote.getAllRuleTransitions();
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
			return ruleEngineFacadeRemote.getAllRuleTransitionsForRuleId(aRuleId);
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
			return ruleEngineFacadeRemote.getAllRuleTransitionsForTransitionId(aTransitionId);
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
	public Collection<RuleEngineGenerationVO> getAllRuleGenerations()  {
		try {
			return ruleEngineFacadeRemote.getAllRuleGenerations();
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
			return ruleEngineFacadeRemote.getAllRuleGenerationsForGenerationId(aRuleId);
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
			return ruleEngineFacadeRemote.getAllStateModelsForRuleId(aRuleId);
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
			return ruleEngineFacadeRemote.getByEventAndEntityOrdered(sEventName, sEntity);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * Get all rule usages
	 * @throws CommonPermissionException
	 */
	public Collection<RuleEventUsageVO> getAllRuleEventUsage()  {
		try {
			return ruleEngineFacadeRemote.getAllRuleEventUsage();
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
			return ruleEngineFacadeRemote.getByEventAndRule(sEventName, iRuleId);
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
			return ruleEngineFacadeRemote.findRulesByUsageAndEvent(sEventName, usagecriteria);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
   }

   /**
    *  Method returns all Rules of the given eventtype, eg. user, delete, insert
	 * @param sEventName
	 * @return
	 */
	public List<RuleVO> getByEventOrdered(String sEventName) throws CommonPermissionException{
	   List<RuleVO> retVal = null;
	   
	   retVal = ruleEngineFacadeRemote.getByEventOrdered(sEventName);
		
	   return retVal;
   }
   /**
    * Get all referenced entity names for a certain rule event.
    * @return collection of entity names
    * @throws CommonFatalException, CommonFatalException
    */
	public Collection<String> getRuleUsageEntityNamesByEvent(String sEventName) {
		try {
			return ruleEngineFacadeRemote.getRuleUsageEntityNamesByEvent(sEventName);
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
			rulevo = ruleEngineFacadeRemote.get(iRuleId);
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
			rulevo = ruleEngineFacadeRemote.get(ruleName);
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
			ruleEngineFacadeRemote.remove(rulevo);
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
			return ruleEngineFacadeRemote.modify(rulevo, mpmdvoDependants);
		}
		catch (RuntimeException ex) {
			throw new NuclosUpdateException(ex.getMessage(), ex);
		}
	}

	public RuleVO create(RuleVO rulevo, DependantMasterDataMap mpmdvoDependants) throws CommonBusinessException {
		try {
			return ruleEngineFacadeRemote.create(rulevo, mpmdvoDependants);
		}
		catch (RuntimeException ex) {
			throw new NuclosBusinessException(ex.getMessage(), ex);
		}
	}

	public void createRuleUsageForId(String eventName, String entity, Integer processId, Integer statusId, 
			Integer ruleToInsertId, Integer ruleBeforeId) throws CommonBusinessException {
		
		try {
			ruleEngineFacadeRemote.createRuleUsageInEntity(eventName, entity, processId, statusId, ruleToInsertId, ruleBeforeId);
		}
		catch (RuntimeException ex) {
			throw new CommonBusinessException(ex);
		}
	}

	public void moveRuleUsageForId(String eventName, String entity, Integer processId, Integer statusId, 
			Integer ruleToInsertId, Integer ruleBeforeId) throws CommonBusinessException {

		try {
			ruleEngineFacadeRemote.moveRuleUsageInEntity(eventName, entity, processId, statusId, ruleToInsertId, ruleBeforeId);
		}
		catch (RuntimeException ex) {
			throw new CommonBusinessException(ex);
		}
	}

	public void removeRuleUsage(String eventName, String entity, Integer processId, Integer statusId, 
			Integer ruleToRemove) throws CommonBusinessException {

		try {
			ruleEngineFacadeRemote.removeRuleUsage(eventName, entity, processId, statusId, ruleToRemove);
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
			ruleEngineFacadeRemote.importRules(collRuleWithUsages);
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
			ruleEngineFacadeRemote.check(ruleVO);
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
			return ruleEngineFacadeRemote.getClassTemplate();
		}
		catch (RuntimeException e) {
			throw new CommonBusinessException(e);
		}
	}

	public void invalidateCache() {
		try {
			ruleEngineFacadeRemote.invalidateCache();
		}
		catch (RuntimeException e) {
			throw new NuclosFatalException(e);
		}
	}
	
	public List<RuleVO> getByNucletEventsOrdered(String sEventName, Integer nucletId) {
		return ruleEngineFacadeRemote.getByNucletEventsOrdered(sEventName, nucletId);
	}
	

}	// class RuleDelegate
