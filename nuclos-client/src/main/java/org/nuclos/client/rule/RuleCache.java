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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.nuclos.client.LocalUserCaches.AbstractLocalUserCache;
import org.nuclos.client.customcode.CodeDelegate;
import org.nuclos.client.jms.TopicNotificationReceiver;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.customcode.valueobject.CodeVO;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.ruleengine.valueobject.RuleEngineGenerationVO;
import org.nuclos.server.ruleengine.valueobject.RuleEngineTransitionVO;
import org.nuclos.server.ruleengine.valueobject.RuleEventUsageVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.springframework.beans.factory.InitializingBean;

/**
 * Caches whole contents from master data entities. It is not used for data dependant on a foreign key.
 * Retrieves notifications about changes from the server (singleton).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @todo the caller has to decide whether an entity is cacheable or not. This is bad.
 */
// @Component
public class RuleCache extends AbstractLocalUserCache implements InitializingBean {
	
	private static final Logger LOG = Logger.getLogger(RuleCache.class);

	private static RuleCache INSTANCE;

	private final Map<Integer, CodeVO> mpAllCodes = new ConcurrentHashMap<Integer, CodeVO>();

	private final Map<Integer, RuleVO> mpAllRules = new ConcurrentHashMap<Integer, RuleVO>();
	private final Map<String, RuleVO> mpAllRulesByName = new ConcurrentHashMap<String, RuleVO>();

	private final List<GeneratorActionVO> mpAllAdGenerations = new ArrayList<GeneratorActionVO>();
	private final Map<Integer, Collection<GeneratorActionVO>> mpAllAdGenerationsByRuleId = new ConcurrentHashMap<Integer, Collection<GeneratorActionVO>>();

	private final Map<Integer, RuleVO> mpAllTimelimitRules = new ConcurrentHashMap<Integer, RuleVO>();

	private final Map<Integer, Collection<RuleEngineGenerationVO>> mpAllRuleGenerationsForRuleId = new ConcurrentHashMap<Integer, Collection<RuleEngineGenerationVO>>();
	private final Map<Integer, Collection<RuleEngineGenerationVO>> mpAllRuleGenerationsForGenerationId = new ConcurrentHashMap<Integer, Collection<RuleEngineGenerationVO>>();
	private final Map<Integer, Collection<RuleEngineTransitionVO>> mpAllRuleTransitionsForRuleId = new ConcurrentHashMap<Integer, Collection<RuleEngineTransitionVO>>();
	private final Map<Integer, Collection<RuleEngineTransitionVO>> mpAllRuleTransitionsForTransitionId = new ConcurrentHashMap<Integer, Collection<RuleEngineTransitionVO>>();

	private final Map<Integer, Collection<RuleEventUsageVO>> mpAllRuleEventByRuleId = new ConcurrentHashMap<Integer, Collection<RuleEventUsageVO>>();

	private final Map<Integer, Collection<StateModelVO>> mpAllStateModelsForRuleId = new ConcurrentHashMap<Integer, Collection<StateModelVO>>();

		
	private transient TopicNotificationReceiver tnr;
	private transient MessageListener messageListener;
	
	private transient RuleDelegate ruleDelegate;
	
	private transient TimelimitRuleDelegate timelimitRuleDelegate;
	
	private transient CodeDelegate codeDelegate;


	public static RuleCache getInstance() {
		if (INSTANCE == null) {
			// lazy support
			INSTANCE = (RuleCache)SpringApplicationContextHolder.getBean("ruleCache");
		}
		return INSTANCE;
	}

	RuleCache() {
		INSTANCE = this;
	}
	
	// @PostConstruct
	public final void afterPropertiesSet() {
		if (!wasDeserialized() || !isValid()) {
			invalidate(null);
		}
	}
	
	public final void initMessageListener() {
		if (messageListener != null)
			return;

		messageListener = new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				String sEntity;
				if (msg instanceof TextMessage) {
					try {
						sEntity = ((TextMessage) msg).getText();
						LOG.info("onMessage: JMS message is of type TextMessage, text is: " + sEntity);
					}
					catch (JMSException ex) {
						LOG.warn("onMessage: Exception thrown in JMS message listener.", ex);
						sEntity = null;
					}
				}
				else {
					LOG.warn("onMessage: Message of type " + msg.getClass().getName() + " received, while a TextMessage was expected.");
					sEntity = null;
				}
				RuleCache.this.invalidate(sEntity);
			}
		};
		tnr.subscribe(getCachingTopic(), messageListener);
	}
	
	// @Autowired
	public final void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
	}
	
	// @Autowired
	public final void setRuleDelegate(RuleDelegate ruleDelegate) {
		this.ruleDelegate = ruleDelegate;
	}
	
	// @Autowired
	public final void setCodeDelegate(CodeDelegate codeDelegate) {
		this.codeDelegate = codeDelegate;
	}
	
	// @Autowired
	public final void setTimelimitRuleDelegate(TimelimitRuleDelegate timelimitRuleDelegate) {
		this.timelimitRuleDelegate = timelimitRuleDelegate;
	}
	
	@Override
	public String getCachingTopic() {
		return JMSConstants.TOPICNAME_MASTERDATACACHE;
	}

	/**
	 * @return all rules defined in this application.
	 * @throws NuclosFatalException
	 */
	public Collection<RuleVO> getAllRules() throws NuclosFatalException {
		return mpAllRules.values();
	}

	public Collection<CodeVO> getAllCodes() throws NuclosFatalException {
		return mpAllCodes.values();
	}
	
	/**
	 * Get all object generations that have a rule assigned.
	 */
	public Collection<GeneratorActionVO> getAllAdGenerationsWithRule() throws NuclosFatalException {
		return mpAllAdGenerations;
	}

	/**
	 * Get all object generations that have a certain rule assigned.
	 */
	public Collection<GeneratorActionVO> getAllAdGenerationsForRuleId(Integer aRuleId) {
		if (!mpAllAdGenerationsByRuleId.containsKey(aRuleId))
			mpAllAdGenerationsByRuleId.put(aRuleId, RuleDelegate.getInstance().getAllAdGenerationsForRuleId(aRuleId));
		return mpAllAdGenerationsByRuleId.get(aRuleId);
	}

	/**
	 * Get all RuleGeneration for the given rule.
	 */
	public Collection<RuleEngineGenerationVO> getAllRuleGenerationsForRuleId(Integer aRuleId)  {
		if (!mpAllRuleGenerationsForRuleId.containsKey(aRuleId))
			mpAllRuleGenerationsForRuleId.put(aRuleId, RuleDelegate.getInstance().getAllRuleGenerationsForRuleId(aRuleId));
		return mpAllRuleGenerationsForRuleId.get(aRuleId);
	}

	/**
	 * Get all Rule Transition that have the given rule assigned.
	 * @throws CommonPermissionException
	 */
	public Collection<RuleEngineTransitionVO> getAllRuleTransitionsForRuleId(Integer aRuleId)  {
		if (!mpAllRuleTransitionsForRuleId.containsKey(aRuleId))
			mpAllRuleTransitionsForRuleId.put(aRuleId, RuleDelegate.getInstance().getAllRuleTransitionsForRuleId(aRuleId));
		return mpAllRuleTransitionsForRuleId.get(aRuleId);
	}

	/**
	 * Get all Rule Transition that have the given transition assigned.
	 */
	public Collection<RuleEngineTransitionVO> getAllRuleTransitionsForTransitionId(Integer aTransitionId) {
		if (!mpAllRuleTransitionsForTransitionId.containsKey(aTransitionId))
			mpAllRuleTransitionsForTransitionId.put(aTransitionId, RuleDelegate.getInstance().getAllRuleTransitionsForTransitionId(aTransitionId));
		return mpAllRuleTransitionsForTransitionId.get(aTransitionId);
	}

	/**
	 * Get all RuleGeneration for the given generation.
	 */
	public Collection<RuleEngineGenerationVO> getAllRuleGenerationsForGenerationId(Integer aGenerationId)  {
		if (!mpAllRuleGenerationsForGenerationId.containsKey(aGenerationId))
			mpAllRuleGenerationsForGenerationId.put(aGenerationId, RuleDelegate.getInstance().getAllRuleGenerationsForGenerationId(aGenerationId));
		return mpAllRuleGenerationsForGenerationId.get(aGenerationId);
	}

	/**
	 * Get all state Models that have a given rule assigned.
	 */
	public Collection<StateModelVO> getAllStateModelsForRuleId(Integer aRuleId)  {
		if (!mpAllStateModelsForRuleId.containsKey(aRuleId))
			mpAllStateModelsForRuleId.put(aRuleId, RuleDelegate.getInstance().getAllStateModelsForRuleId(aRuleId));
		return mpAllStateModelsForRuleId.get(aRuleId);
	}
	

	public RuleVO get(Integer iRuleId) throws CommonFinderException {
		if (!mpAllRules.containsKey(iRuleId))
			mpAllRules.put(iRuleId, RuleDelegate.getInstance().get(iRuleId));
		return mpAllRules.get(iRuleId);
	}

	//NUCLOSINT-743 addititional method for resolving rulename to id
	public RuleVO get(String ruleName) throws CommonFinderException {
		if (!mpAllRulesByName.containsKey(ruleName))
			mpAllRulesByName.put(ruleName, RuleDelegate.getInstance().get(ruleName));
		return mpAllRulesByName.get(ruleName);
	}

	/**
	 * @return all timelimit rules defined in Nucleus.
	 */
	public Collection<RuleVO> getAllTimelimitRules() {
		try {
			return mpAllTimelimitRules.values();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}
	
	/**
	 * Get all rule usages of a rule for a certain event.
	 * @return Collection<RuleVO>
	 */
	public Collection<RuleEventUsageVO> getByEventAndRule(String sEventName, Integer iRuleId) {
		if (!mpAllRuleEventByRuleId.containsKey(iRuleId)) {
			Collection<RuleEventUsageVO> lstAllRuleEventByRuleId = mpAllRuleEventByRuleId.get(iRuleId);
			if (lstAllRuleEventByRuleId == null) {
				lstAllRuleEventByRuleId = new ArrayList<RuleEventUsageVO>();
			}
			lstAllRuleEventByRuleId.addAll(ruleDelegate.getByEventAndRule(sEventName, iRuleId));
			mpAllRuleEventByRuleId.put(iRuleId, lstAllRuleEventByRuleId);
		}

		Collection<RuleEventUsageVO> result = new ArrayList<RuleEventUsageVO>();
		// filter event.
		Collection<RuleEventUsageVO> lstAllRuleEventByRuleId = mpAllRuleEventByRuleId.get(iRuleId);
		for (RuleEventUsageVO reEventUsageVO : lstAllRuleEventByRuleId) {
			if (reEventUsageVO.getEvent().equals(sEventName))
				result.add(reEventUsageVO);
		}
		return result;
	}

	/**
	 * invalidates the cache
	 */
	public void invalidate(String sEntity) {
		try {
			if (sEntity == null || sEntity.equals(NuclosEntity.CODE.getEntityName())) {
				mpAllCodes.clear();
				for (CodeVO codeVO : codeDelegate.getAll()) {
					mpAllCodes.put(codeVO.getId(), codeVO);
				}
			}
	
			if (sEntity == null || sEntity.equals(NuclosEntity.RULE.getEntityName())) {
				mpAllRules.clear();
				mpAllRulesByName.clear();
				for (RuleVO ruleVO : ruleDelegate.getAllRules()) {
					mpAllRules.put(ruleVO.getId(), ruleVO);
					mpAllRulesByName.put(ruleVO.getRule(), ruleVO);
				}
				mpAllTimelimitRules.clear();
				for (RuleVO ruleVO : timelimitRuleDelegate.getAllTimelimitRules()) {
					mpAllTimelimitRules.put(ruleVO.getId(), ruleVO);
				}
			}
			
			if (sEntity == null || sEntity.equals(NuclosEntity.RULEUSAGE.getEntityName())) {
				mpAllRuleEventByRuleId.clear();
				for (RuleEventUsageVO ruleEventUsageVO : ruleDelegate.getAllRuleEventUsage()) {
					Collection<RuleEventUsageVO> lstAllRuleEventByRuleId = mpAllRuleEventByRuleId.get(ruleEventUsageVO.getRuleId());
					if (lstAllRuleEventByRuleId == null) {
						lstAllRuleEventByRuleId = new ArrayList<RuleEventUsageVO>();
					}
					lstAllRuleEventByRuleId.add(ruleEventUsageVO);
					mpAllRuleEventByRuleId.put(ruleEventUsageVO.getRuleId(), lstAllRuleEventByRuleId);
				}
			}
			
			if (sEntity == null || sEntity.equals(NuclosEntity.RULEGENERATION.getEntityName())) {
				mpAllAdGenerations.clear();
				for (GeneratorActionVO generatorActionVO : ruleDelegate.getAllAdGenerationsWithRule()) {
					mpAllAdGenerations.add(generatorActionVO);
				}
				
				mpAllAdGenerationsByRuleId.clear(); // we cannot recreate cache here. since we do not have an rule id in generatorActionVO
				/*for (Integer aRuleId : mpAllRules.keySet()) {
					getAllAdGenerationsForRuleId(aRuleId);
				}*/
				
				mpAllRuleGenerationsForRuleId.clear();
				mpAllRuleGenerationsForGenerationId.clear();
				for (RuleEngineGenerationVO ruleGenerationVO : ruleDelegate.getAllRuleGenerations()) {
					Collection<RuleEngineGenerationVO> lstAllRuleGenerationsForRuleId = mpAllRuleGenerationsForRuleId.get(ruleGenerationVO.getRuleId());
					if (lstAllRuleGenerationsForRuleId == null) {
						lstAllRuleGenerationsForRuleId = new ArrayList<RuleEngineGenerationVO>();
					}
					lstAllRuleGenerationsForRuleId.add(ruleGenerationVO);
					mpAllRuleGenerationsForRuleId.put(ruleGenerationVO.getRuleId(), lstAllRuleGenerationsForRuleId);
		
					Collection<RuleEngineGenerationVO> lstAllRuleGenerationsForGenerationId = mpAllRuleGenerationsForGenerationId.get(ruleGenerationVO.getGenerationId());
					if (lstAllRuleGenerationsForGenerationId == null) {
						lstAllRuleGenerationsForGenerationId = new ArrayList<RuleEngineGenerationVO>();
					}
					lstAllRuleGenerationsForGenerationId.add(ruleGenerationVO);
					mpAllRuleGenerationsForGenerationId.put(ruleGenerationVO.getGenerationId(), lstAllRuleGenerationsForGenerationId);
				}
			}
			
			if (sEntity == null || sEntity.equals(NuclosEntity.RULETRANSITION.getEntityName())) {
				mpAllStateModelsForRuleId.clear(); 
				// we cannot recreate cache here. since we do not have an rule id in generatorActionVO
				/*for (Integer aRuleId : mpAllRules.keySet()) {
					getAllStateModelsForRuleId(aRuleId);
				}*/
			}
		} 
		/*
		 * FIX for http://support.novabit.de/browse/ACC-379 and http://support.novabit.de/browse/ACC-369
		 * that were introduced by changeset r8766 from 09.08.2012.
		 * 
		 * The change r8766 now always initialize the RuleCache (that has not be the case before).
		 * 
		 * The change at this place is intended to avoid crashing the client after log-in only
		 * because the user hasn't the right to read rules.
		 */
		catch (Exception e) {
			LOG.info("error initializing rule cache: " + e);
		}
	}
}	// class RuleCache
