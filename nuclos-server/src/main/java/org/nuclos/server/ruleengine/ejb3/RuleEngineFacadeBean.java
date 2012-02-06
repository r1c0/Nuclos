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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Level;
import org.nuclos.common.Actions;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.PropertiesMap;
import org.nuclos.common.RuleNotification;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.collection.TransformerUtils;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoteException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.customcode.CustomCodeManager;
import org.nuclos.server.customcode.NuclosRule;
import org.nuclos.server.customcode.codegenerator.NuclosJavaCompiler;
import org.nuclos.server.customcode.codegenerator.RuleCodeGenerator;
import org.nuclos.server.customcode.codegenerator.RuleCodeGenerator.AbstractRuleTemplateType;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.ejb3.GeneratorFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.NuclosFatalRuleException;
import org.nuclos.server.ruleengine.RuleInterface;
import org.nuclos.server.ruleengine.valueobject.RuleEngineGenerationVO;
import org.nuclos.server.ruleengine.valueobject.RuleEngineTransitionVO;
import org.nuclos.server.ruleengine.valueobject.RuleEventUsageVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.ruleengine.valueobject.RuleWithUsagesVO;
import org.nuclos.server.statemodel.ejb3.StateFacadeLocal;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for rule engine management.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
// @Stateless
// @Local(RuleEngineFacadeLocal.class)
// @Remote(RuleEngineFacadeRemote.class)
@Transactional
public class RuleEngineFacadeBean extends NuclosFacadeBean implements RuleEngineFacadeLocal, RuleEngineFacadeRemote {
	
	private CustomCodeManager ccm;

	RuleEngineFacadeBean() {
	}

	public void setCustomCodeManager(CustomCodeManager ccm) {
		this.ccm = ccm;
	}

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
	@Override
	public RuleObjectContainerCVO fireRule(String sEntity, String sEventName, RuleObjectContainerCVO loccvoCurrent) throws NuclosBusinessRuleException {
		if (sEntity == null) {
			throw new NullArgumentException("sEntity");
		}
		//		if (!Modules.getInstance().getUsesRuleEngine(Modules.getInstance().getModuleIdByEntityName(metacache.getMetaDataById(iEntityId).getEntityName()))) {
		//			throw new IllegalArgumentException("iModuleId");
		//		}

		final List<RuleVO> lstRules = new ArrayList<RuleVO>(RuleCache.getInstance().getByEventAndEntityOrdered(sEventName, sEntity));

		// We can now execute the rules in their order
		info("BEGIN    executing business rules for event \"" + sEventName + "\" and entity " + sEntity + "..."); //Modules.getInstance().getEntityNameByModuleId(iModuleId)
		final RuleObjectContainerCVO result = this.executeBusinessRules(lstRules, loccvoCurrent, false);
		info("FINISHED executing business rules for event \"" + sEventName + "\" and entity " + sEntity + "..."); //Modules.getInstance().getEntityNameByModuleId(iModuleId)
		return result;
	}

	/**
	 * fires a transition event by finding all rules that correspond
	 * to the given source and target state ids and by executing these rules.
	 * @param iSourceStateId source state id to fire event for in combination with target state id
	 * @param iTargetStateId target state id to fire event for in combination with source state id
	 * @param loccvoCurrent current leased object as parameter for rules
	 * @return the possibly change current object.
	 */
	@Override
	public RuleObjectContainerCVO fireRule(Integer sourceStateId, Integer targetStateId, RuleObjectContainerCVO ruleContainer, Boolean after) throws NuclosBusinessRuleException {
		StateFacadeLocal facade = ServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
		StateTransitionVO stVO = (sourceStateId == null) ?
			facade.findStateTransitionByNullAndTargetState(targetStateId) :
				facade.findStateTransitionBySourceAndTargetState(sourceStateId, targetStateId);

			return stVO != null ? fireRule(stVO.getId(), ruleContainer, after) : ruleContainer;
	}

	/**
	 * fires a transition event by finding all rules that correspond
	 * to the given transition id and by executing these rules.
	 * @param iTransitionId transition id to fire event for
	 * @param loccvoCurrent current leased object as parameter for rules
	 * @return the possibly change current object.
	 */
	private RuleObjectContainerCVO fireRule(Integer transitionId, RuleObjectContainerCVO ruleContainer, Boolean after) throws NuclosBusinessRuleException {
		final List<RuleVO> rules = new ArrayList<RuleVO>();

		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_RULE_TRANSITION").alias(SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("INTID_T_MD_RULE", Integer.class), t.baseColumn("BLNRUNAFTERWARDS", Boolean.class));
		query.where(builder.equal(t.baseColumn("INTID_T_MD_STATE_TRANSITION", Integer.class), transitionId));
		query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));

		for (DbTuple res : DataBaseHelper.getDbAccess().executeQuery(query)) {
			Boolean bRuleRunAfterwards = res.get(1, Boolean.class);
			if (bRuleRunAfterwards == null) bRuleRunAfterwards = Boolean.FALSE;
			if ((bRuleRunAfterwards && after) || (!bRuleRunAfterwards && !after)) {
				rules.add(RuleCache.getInstance().getRule(res.get(0, Integer.class)));
			}
		}

		// We can now execute the rules in their order:
		info("BEGIN    executing business rules for transition id " + transitionId + "...");
		final RuleObjectContainerCVO result = this.executeBusinessRules(rules, ruleContainer, false);
		info("FINISHED executing business rules for transition id " + transitionId + "...");
		return result;
	}

	/**
	 * fires the rules for a specific object generation.
	 */
	@Override
	public RuleObjectContainerCVO fireGenerationRules(Integer iGenerationId, RuleObjectContainerCVO tgtRuleObject, Collection<RuleObjectContainerCVO> srcRuleObjects, RuleObjectContainerCVO parameterRuleObject, List<String> actions, PropertiesMap properties, Boolean after) throws NuclosBusinessRuleException {
		RuleObjectContainerCVO result = null;

		try {
			Collection<RuleVO> rules = findRulesByGeneration(iGenerationId, after);

			// We can now execute the rules in their order:
			info("BEGIN executing business rules for generator id " + iGenerationId + "...");
			result = this.executeBusinessRules(rules, tgtRuleObject, srcRuleObjects, parameterRuleObject, false, actions, properties);
			info("FINISHED executing business rules for generator id " + iGenerationId + "...");
		}
		catch (CommonPermissionException e) {
			throw new NuclosBusinessRuleException(e);
		}

		return result;
	}

	/**
	 * executes the given lstRules of business rules.
	 * @param lstRules List<RuleEngineRuleLocal>
	 * @param loccvoCurrent current leased object as parameter for rules
	 * @param bIgnoreExceptions
	 * @return the possibly change current object.
	 * @throws NuclosBusinessRuleException
	 */
	@Override
	public RuleObjectContainerCVO executeBusinessRules(List<RuleVO> lstRules, RuleObjectContainerCVO loccvoCurrent, boolean bIgnoreExceptions) throws NuclosBusinessRuleException {
		return this.executeBusinessRules(lstRules, loccvoCurrent, null, bIgnoreExceptions, null);
	}

	/**
	 * Convenience method for preserving calls without mpProperties
	 */
	private RuleObjectContainerCVO executeBusinessRules(List<RuleVO> lstRules, RuleObjectContainerCVO loccvoCurrent, Collection<RuleObjectContainerCVO> roccvoSourceObjects,
		boolean bIgnoreExceptions, List<String> lstActions) throws NuclosBusinessRuleException {
		return executeBusinessRules(lstRules, loccvoCurrent, roccvoSourceObjects, null, bIgnoreExceptions, lstActions, null);
	}

	/**
	 * executes active rules in the given lstRules of business rules.
	 * @param lstRules List<RuleEngineRuleLocal>
	 * @param loccvoSourceObject source leased object as parameter for generation rules
	 * @param loccvoTargetObject target leased object as parameter for generation rules
	 * @param bIgnoreExceptions
	 * @param lstActions
	 * @param mpProperties
	 * @return the possibly change current object.
	 * @throws NuclosBusinessRuleException
	 */
	private RuleObjectContainerCVO executeBusinessRules(Collection<RuleVO> lstRules, RuleObjectContainerCVO loccvoCurrent, Collection<RuleObjectContainerCVO> roccvoSourceObjects, RuleObjectContainerCVO loccvoParameterObject, boolean bIgnoreExceptions, List<String> lstActions, PropertiesMap mpProperties) throws NuclosBusinessRuleException {
		RuleObjectContainerCVO result = loccvoCurrent;
		if (!lstRules.isEmpty()) {
			final Iterator<RuleVO> iter = lstRules.iterator();
			final List<RuleNotification> lstRuleNotifications = new ArrayList<RuleNotification>();
			while (iter.hasNext()) {
				String sCurrentRule = null;
				int iHeaderLinesCount = 0;
				try {
					final RuleVO rulevo = iter.next();
					sCurrentRule = rulevo.getName();

					if (rulevo.isActive()) {
						info("Start executing rule \"" + rulevo.getName() + "\" (" + rulevo.getId() + ")");

						RuleCodeGenerator<NuclosRule> generator = getGenerator(rulevo);
						final NuclosRule ruleInstance = ccm.getInstance(generator);
						iHeaderLinesCount = generator.getHeaderLineCount();

						final RuleInterface ri = new RuleInterface(rulevo, loccvoCurrent, roccvoSourceObjects, loccvoParameterObject, lstActions);
						ri.setProperties(mpProperties);
						ruleInstance.rule(ri);
						result = ri.getRuleObjectContainerCVOIfAny();

						lstRuleNotifications.addAll(ri.getRuleNotification());

						info("Finished executing rule \"" + rulevo.getName() + "\"");
					}
					else {
						log(Level.INFO, "Skipped rule \"" + rulevo.getName() + "\" - it is not active.");
					}
				}
				catch (NuclosBusinessRuleException ex) {
					throw new NuclosBusinessRuleException(StringUtils.getParameterizedExceptionMessage("rule.execution.error",
						sCurrentRule, getErrorLineNumber(ex, iHeaderLinesCount), ex.getMessage()), ex.getMessage(), ex);
					//               	"Fehler bei der Ausf\u00fchrung der Gesch\u00e4ftsregel \"" + sCurrentRule + "\"" +
					//                     " (Zeile " + getErrorLineNumber(ex, iHeaderLinesCount) + ") aufgetreten:\n" + ex.getMessage(), ex);
				}
				catch (NuclosFatalRuleException ex) {
					log(Level.ERROR, ex.getMessage(), ex);
					if (!bIgnoreExceptions) {
						throw ex;
					}
				}
				catch (Exception ex) {
					log(Level.ERROR, ex.getMessage(), ex);
					if (!bIgnoreExceptions) {
						String sErrorMessage = StringUtils.getParameterizedExceptionMessage("rule.execution.error",
							sCurrentRule, getErrorLineNumber(ex, iHeaderLinesCount), null);
						sErrorMessage = StringUtils.getParameterizedExceptionMessage("rule.execution.error",
							sCurrentRule, getErrorLineNumber(ex, iHeaderLinesCount), ex.getMessage());
						throw new NuclosFatalRuleException(sErrorMessage, ex);
					}
				}
			}
			if (!bIgnoreExceptions) {
				sendMessagesByJMS(lstRuleNotifications);
			}
		}
		return result;
	}

	private int getErrorLineNumber(Exception ex, int iHeaderLinesCount) {
		int iErrorLineNumber = 0;
		for (StackTraceElement stackElement : ex.getStackTrace()) {
			if (stackElement.getClassName().startsWith("Rule_")) {
				iErrorLineNumber = stackElement.getLineNumber();
				break;
			}
		}
		return iErrorLineNumber > 0 ? (iErrorLineNumber - iHeaderLinesCount) : 0;
	}

	/**
	 * send the notification messages of all rules in this transaction.
	 */
	private void sendMessagesByJMS(List<RuleNotification> lstRuleNotifications) {
		if (lstRuleNotifications.size() > 0) {

			for (RuleNotification notification : lstRuleNotifications) {
				NuclosJMSUtils.sendObjectMessage(notification, JMSConstants.TOPICNAME_RULENOTIFICATION, this.getCurrentUserName());

			}
		}

//			final Context ctx = ServiceLocator.getInstance().getInitialContext();
//			Topic topic = null;
//			TopicConnection topicconn = null;
//			try {
//				// subscribe to topic "ruleNotification":
//				final TopicConnectionFactory topicconnfactory = (TopicConnectionFactory) ctx.lookup(CONNECTIONFACTORY_JNDINAME);
//				topicconn = topicconnfactory.createTopicConnection();
//				topic = (Topic) ctx.lookup(JMSConstants.TOPICNAME_RULENOTIFICATION);
//
//				//send the message
//				final TopicSession topicsession = topicconn.createTopicSession(true, 0);
//				final TopicPublisher topicpublisher = topicsession.createPublisher(topic);

//				for (RuleNotification notification : lstRuleNotifications) {
//					final Message msg = topicsession.createObjectMessage(notification);
//					msg.setJMSCorrelationID(this.getCurrentUserName());
//					topicpublisher.publish(msg);
//				}

//				topicsession.commit();
//				topicsession.close();
//			}
//			catch (NamingException ex) {
//				throw new NuclosFatalException("client.notifier.exception", ex);
//				//"Die Notifikation des Clients ist fehlgeschlagen.", ex);
//			}
//			catch (JMSException ex) {
//				throw new NuclosFatalException("client.notifier.exception", ex);
//				//"Die Notifikation des Clients ist fehlgeschlagen.", ex);
//			}
//			finally {
//				if (topicconn != null) {
//					try {
//						topicconn.close();
//						topicconn = null;
//						topic = null;
//					}
//					catch (JMSException ex) {
//						throw new NuclosFatalException(ex);
//					}
//				}
//			}
//		}
	}

	/**
	 * @return Collection<RuleVO> all rule definitions
	 * @throws CommonPermissionException
	 */
	@Override
	public Collection<RuleVO> getAllRules() throws CommonPermissionException {
		if(!this.isInRole("UseManagementConsole")) {
			this.checkReadAllowed(NuclosEntity.RULE, NuclosEntity.STATEMODEL);
		}
		return RuleCache.getInstance().getAllRules();
	}

	/**
	 * Get a collection of rules by the eventname independent of the module.
	 * @return Collection<RuleVO> all rule for a given event Name
	 * @throws CommonPermissionException
	 */
	@Override
	public List<RuleVO> getByEventOrdered(String sEventName) throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.RULE);
		return RuleCache.getInstance().getByEventOrdered(sEventName);
	}

	/**
	 * Get a collection of rules by Eventname and ModuleId (ordered).
	 * @return Collection<RuleVO> all rule for a given event Name
	 * @throws CommonPermissionException
	 */
	@Override
	public List<RuleVO> getByEventAndEntityOrdered(String sEventName, String sEntity) throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.RULE);
		return RuleCache.getInstance().getByEventAndEntityOrdered(sEventName, sEntity);
	}

	/**
	 * Create an ruleUsage for the given module and eventname.
	 * The oder of the new usage is dependent of the ruleBeforeId
	 *
	 * @param sEventname
	 * @param iModuleId
	 * @param ruleToInsertId
	 * @param ruleBeforeId - null the new usage is inserted at the end
	 * 					- not null the new usage is inserted after the usage with the ruleBeforeId
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 */
	@Override
	public void createRuleUsageInEntity(String sEventname, String sEntity, Integer ruleToInsertId, Integer ruleBeforeId) throws CommonCreateException, CommonPermissionException {
		// @todo the name is misspelled (Module).
		this.checkWriteAllowed(NuclosEntity.RULE);


		try {
			Collection<RuleEventUsageVO> reVOList = new ArrayList<RuleEventUsageVO>();
			Collection<MasterDataVO> mdVOList = getRuleUsageForEntity(sEntity);

			for (MasterDataVO vo : mdVOList) {
				if (vo.getField("event").equals(sEventname))
					reVOList.add(MasterDataWrapper.getREUsageVO(vo));
			}

			final List<RuleEventUsageVO> lstAllRulesSorted = CollectionUtils.sorted(reVOList, new UsageByOrderComparator());

			int iLastRuleOrder = 0;
			int iNewRuleOrder = 0;
			boolean bFoundRuleBefore = false;
			for (RuleEventUsageVO reUsageVO : lstAllRulesSorted) {
				if (!bFoundRuleBefore) {
					iLastRuleOrder = reUsageVO.getOrder();
					if (ruleBeforeId != null && reUsageVO.getRuleId().equals(ruleBeforeId)) {
						bFoundRuleBefore = true;
						iNewRuleOrder = iLastRuleOrder + 1;
						iLastRuleOrder = iNewRuleOrder;
					}
				}
				else {
					reUsageVO.setOrder(iLastRuleOrder + 1);
					iLastRuleOrder = reUsageVO.getOrder();
				}
			}

			if (!bFoundRuleBefore) {
				iNewRuleOrder = iLastRuleOrder + 1;
			}

			RuleEventUsageVO reUsageVO = new RuleEventUsageVO(sEventname, sEntity, ruleToInsertId, iNewRuleOrder);
			getMasterDataFacade().create(NuclosEntity.RULEUSAGE.getEntityName(), MasterDataWrapper.wrapREUsageVO(reUsageVO),null);

			RuleCache.getInstance().invalidate();
		}
		catch (CommonBusinessException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * remove an rule usage for the rule with the given id in the given module and the eventName
	 * @param eventName
	 * @param moduleId
	 * @param iRuleIdToRemove id of rule to remove
	 * @throws CommonPermissionException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws NuclosBusinessRuleException
	 */
	@Override
	public void removeRuleUsage(String eventName, String entity, Integer iRuleIdToRemove) throws CommonPermissionException, NuclosBusinessRuleException, CommonFinderException, CommonRemoveException, CommonStaleVersionException {
		this.checkWriteAllowed(NuclosEntity.RULE);

		Collection<RuleEventUsageVO> reUsageList = new ArrayList<RuleEventUsageVO>();
		Collection<MasterDataVO> mdVOList = getMasterDataFacade().getDependantMasterData(NuclosEntity.RULEUSAGE.getEntityName(), "rule", iRuleIdToRemove);

		for (MasterDataVO vo : mdVOList) {
			if (vo.getField("event").equals(eventName))
				reUsageList.add(MasterDataWrapper.getREUsageVO(vo));
		}

		MasterDataVO usageToRemove = null;

		// timelimits has no module, there should be only one rule exists
		if (reUsageList.size() == 1) {
			usageToRemove = MasterDataWrapper.wrapREUsageVO(reUsageList.iterator().next());
		}

		for (Iterator<RuleEventUsageVO> ruleIter = reUsageList.iterator(); usageToRemove == null && ruleIter.hasNext();) {
			final RuleEventUsageVO usage = ruleIter.next();
			if (usage.getEntity().equals(entity)) {
				usageToRemove = MasterDataWrapper.wrapREUsageVO(usage);
			}
		}

		if (usageToRemove != null) {
			//usageToRemove.remove();
			getMasterDataFacade().remove(NuclosEntity.RULEUSAGE.getEntityName(), usageToRemove, true);
			RuleCache.getInstance().invalidate();
		}
		else {
			throw new CommonRemoteException("rule.error.missing.usage");//"Keine Verwendung gefunden.");
		}
	}

	/**
	 *
	 * @param eventName
	 * @param moduleId
	 * @param ruleToMoveId
	 * @param ruleBeforeId
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 */
	@Override
	public void moveRuleUsageInEntity(String eventName, String entity, Integer ruleToMoveId, Integer ruleBeforeId) throws CommonCreateException, CommonPermissionException {
		this.checkWriteAllowed(NuclosEntity.RULE);

		Collection<RuleEventUsageVO> reVOList = new ArrayList<RuleEventUsageVO>();
		Collection<MasterDataVO> mdVOList = getRuleUsageForEntity(entity);

		for (MasterDataVO vo : mdVOList) {
			if (vo.getField("event").equals(eventName))
				reVOList.add(MasterDataWrapper.getREUsageVO(vo));
		}

		final List<RuleEventUsageVO> lstAllRulesSorted = CollectionUtils.sorted(reVOList, new UsageByOrderComparator());

		// find rule before:
		int orderRuleBefore = 0;
		if (ruleBeforeId != null) {
			for (RuleEventUsageVO ruleVO : lstAllRulesSorted) {
				if (ruleVO.getRuleId().equals(ruleBeforeId)) {
					orderRuleBefore = ruleVO.getOrder();
				}
			}
		}

		// move order:
		int lastOrder = 1;
		RuleEventUsageVO ruleToMove = null;
		Integer ruleToMoveOrder = null;
		for (RuleEventUsageVO ruleLocale : lstAllRulesSorted) {
			if (ruleLocale.getRuleId().equals(ruleToMoveId)) {
				ruleToMove = ruleLocale;
			}
			else {
				if (ruleLocale.getOrder() > orderRuleBefore) {
					if (ruleToMoveOrder == null) {
						ruleToMoveOrder = lastOrder++;
					}
					ruleLocale.setOrder(lastOrder++);
				}
				else {
					ruleLocale.setOrder(lastOrder++);
				}
			}
		}
		if (ruleToMoveOrder != null) {
			ruleToMove.setOrder(ruleToMoveOrder);
		}
		else {
			ruleToMove.setOrder(lastOrder);
		}

		RuleCache.getInstance().invalidate();
	}

	private Collection<MasterDataVO> getRuleUsageForEntity(String entity) {
		MasterDataMetaVO metaData = MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.RULEUSAGE);
		return getMasterDataFacade().getMasterData(NuclosEntity.RULEUSAGE.getEntityName(),
			SearchConditionUtils.newMDComparison(metaData, "entity", ComparisonOperator.EQUAL, entity), true);
	}

	/**
	 * Get all object generations.
	 * @return Collection<GeneratorActionVO>
	 * @throws CommonPermissionException
	 */
	@Override
	public Collection<GeneratorActionVO> getAllGenerations() throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.RULE);
		final Collection<GeneratorActionVO> result = new HashSet<GeneratorActionVO>();
		GeneratorFacadeLocal facade = ServiceLocator.getInstance().getFacade(GeneratorFacadeLocal.class);
		Collection<MasterDataVO> mdGenerationsVO = getMasterDataFacade().getMasterData(NuclosEntity.GENERATION.getEntityName(), null, true);

		for (MasterDataVO mdVO : mdGenerationsVO)
			result.add(MasterDataWrapper.getGeneratorActionVO(mdVO, facade.getGeneratorUsages(mdVO.getIntId())));

		return result;
	}

	/**
	 * Get all object generations that have a rule assigned.
	 * @return Collection<GeneratorActionVO>
	 * @throws CommonPermissionException
	 */
	@Override
	public Collection<GeneratorActionVO> getAllGenerationsWithRule() throws CommonPermissionException {
		return getAllGenerationsForRuleImpl(null);
	}

	/**
	 * Get all object generations that have a certain rule assigned.
	 * @return Collection<GeneratorActionVO>
	 * @throws CommonPermissionException
	 */
	@Override
	public Collection<GeneratorActionVO> getAllGenerationsForRuleId(Integer iRuleId) throws CommonPermissionException {
		if (iRuleId == null)
			return Collections.emptySet();
		return getAllGenerationsForRuleImpl(iRuleId);
	}

	private Collection<GeneratorActionVO> getAllGenerationsForRuleImpl(Integer iRuleId) throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.RULE);
		Collection<GeneratorActionVO> resultUnsorted = new HashSet<GeneratorActionVO>();

		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_MD_RULE_GENERATION").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID_T_MD_GENERATION", Integer.class));
		if (iRuleId != null) {
			query.where(builder.equal(t.baseColumn("INTID_T_MD_RULE", Integer.class), iRuleId));
		}

		List<Integer> generationIds = DataBaseHelper.getDbAccess().executeQuery(query.distinct(true));

		GeneratorFacadeLocal generatorFacade = ServiceLocator.getInstance().getFacade(GeneratorFacadeLocal.class);

		try {
			for (Integer id : generationIds) {
				MasterDataVO mdVO = getMasterDataFacade().get(NuclosEntity.GENERATION.getEntityName(), id);
				resultUnsorted.add(MasterDataWrapper.getGeneratorActionVO(mdVO, generatorFacade.getGeneratorUsages(mdVO.getIntId())));
			}
		}
		catch (CommonFinderException ex) {
			// Dateninkonsistenz?
			throw new CommonFatalException(ex);
		}

		return CollectionUtils.sorted(resultUnsorted, new GenerationOrderComparator());
	}



	/**
	 * Get all RuleGeneration for the given rule.
	 * @return Collection<RuleEngineGenerationVO>
	 * @throws CommonPermissionException
	 */
	@Override
	public Collection<RuleEngineGenerationVO> getAllRuleGenerationsForRuleId(Integer ruleId) throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.RULE);
		final Collection<RuleEngineGenerationVO> result = new HashSet<RuleEngineGenerationVO>();

		CollectableComparison cond = SearchConditionUtils.newMDComparison(
			MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.RULEGENERATION),"rule", ComparisonOperator.EQUAL, ruleId);
		Collection<MasterDataVO> mdVOList = getMasterDataFacade().getMasterData(NuclosEntity.RULEGENERATION.getEntityName(), cond, true);

		for (MasterDataVO mdVO : mdVOList)
			result.add(MasterDataWrapper.getRuleEngineGenerationVO(mdVO));

		return result;
	}

	/**
	 * Get all RuleGeneration for the given generation.
	 * @return Collection<RuleEngineGenerationVO>
	 * @throws CommonPermissionException
	 */
	@Override
	public Collection<RuleEngineGenerationVO> getAllRuleGenerationsForGenerationId(Integer generationId) throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.RULE);
		final Collection<RuleEngineGenerationVO> result = new HashSet<RuleEngineGenerationVO>();

		CollectableComparison cond = SearchConditionUtils.newMDComparison(
			MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.RULEGENERATION),"generation", ComparisonOperator.EQUAL, generationId);
		Collection<MasterDataVO> mdVOList = getMasterDataFacade().getMasterData(NuclosEntity.RULEGENERATION.getEntityName(), cond, true);

		for (MasterDataVO mdVO : mdVOList)
			result.add(MasterDataWrapper.getRuleEngineGenerationVO(mdVO));

		return CollectionUtils.sorted(result, new GeneratorByOrderComparator());
	}

	/**
	 * Get all RuleTransition that have the given rule assigned.
	 * @return Collection<RuleEngineTransitionVO>
	 * @throws CommonPermissionException
	 */
	@Override
	public Collection<RuleEngineTransitionVO> getAllRuleTransitionsForRuleId(Integer ruleId) throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.RULE);
		final Collection<RuleEngineTransitionVO> result = new HashSet<RuleEngineTransitionVO>();

		CollectableComparison cond = SearchConditionUtils.newMDComparison(
			MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.RULETRANSITION),"ruleId", ComparisonOperator.EQUAL, ruleId);
		Collection<MasterDataVO> mdVOList = getMasterDataFacade().getMasterData(NuclosEntity.RULETRANSITION.getEntityName(), cond, true);

		for (MasterDataVO mdVO : mdVOList)
			result.add(MasterDataWrapper.getRuleEngineTransitionVO(mdVO));

		return result;
	}

	/**
	 * Get all Rule Transition that have the given transition assigned.
	 * @return Collection<RuleEngineTransitionVO>
	 * @throws CommonPermissionException
	 */
	@Override
	public Collection<RuleEngineTransitionVO> getAllRuleTransitionsForTransitionId(Integer transitionId) throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.RULE);
		final Collection<RuleEngineTransitionVO> result = new HashSet<RuleEngineTransitionVO>();

		CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
			MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.RULETRANSITION),"transition", transitionId);
		Collection<MasterDataVO> mdVOList = getMasterDataFacade().getMasterData(NuclosEntity.RULETRANSITION.getEntityName(), cond, true);

		for (MasterDataVO mdVO : mdVOList)
			result.add(MasterDataWrapper.getRuleEngineTransitionVO(mdVO));

		return CollectionUtils.sorted(result, new TransitionByOrderComparator());
	}

	/**
	 * Get all state Models that have a given rule assigned.
	 * @return collection of generation actions
	 * @throws CommonPermissionException
	 */
	@Override
	public Collection<StateModelVO> getAllStateModelsForRuleId(Integer aRuleId) throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.RULE);
		final Collection<StateModelVO> result = new HashSet<StateModelVO>();
		final Collection<Integer> modelIdSet = new HashSet<Integer>();

		StateFacadeLocal stateFacade = ServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
		final Collection<StateModelVO> collstatemodel = stateFacade.findStateModelsByRuleId(aRuleId);
		for (StateModelVO statemodelvo : collstatemodel) {
			// avoid double transitions
			if (!modelIdSet.contains(statemodelvo.getId())) {
				result.add(statemodelvo);
				modelIdSet.add(statemodelvo.getId());
			}
		}
		return result;
	}

	/**
	 * Get all Rule engine Generations.
	 * @return Collection<RuleEngineGenerationVO>
	 * @throws CommonPermissionException
	 */
	@Override
	public Collection<RuleEngineGenerationVO> getAllRuleEngineGenerations() throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.RULE);
		final Collection<RuleEngineGenerationVO> result = new HashSet<RuleEngineGenerationVO>();

		Collection<MasterDataVO> mdVOList = getMasterDataFacade().getMasterData(NuclosEntity.RULEGENERATION.getEntityName(), null, true);

		for (MasterDataVO mdVO : mdVOList)
			result.add(MasterDataWrapper.getRuleEngineGenerationVO(mdVO));

		return result;
	}

	/**
	 * Get all rules contained in a specific object generation.
	 * @return Collection<RuleVO>
	 * @throws CommonPermissionException
	 */
	private Collection<RuleVO> findRulesByGeneration(Integer iGenerationId, Boolean after) throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.RULE);
		List<RuleVO> rules = new ArrayList<RuleVO>();

		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_RULE_GENERATION").alias(SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("INTID_T_MD_RULE", Integer.class), t.baseColumn("BLNRUNAFTERWARDS", Boolean.class));
		query.where(builder.equal(t.baseColumn("INTID_T_MD_GENERATION", Integer.class), iGenerationId));
		query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));

		for (DbTuple res : DataBaseHelper.getDbAccess().executeQuery(query)) {
			Boolean bRuleRunAfterwards = res.get(1, Boolean.class);
			if (bRuleRunAfterwards == null) bRuleRunAfterwards = Boolean.FALSE;
			if ((bRuleRunAfterwards && after) || (!bRuleRunAfterwards && !after)) {
				rules.add(RuleCache.getInstance().getRule(res.get(0, Integer.class)));
			}
		}

		return rules;
	}

	/**
	 * Get all rule usages of a rule for a certain event.
	 * @return collection of state model vo
	 * @throws CommonPermissionException
	 */
	@Override
	public Collection<RuleEventUsageVO> getByEventAndRule(String sEventName, Integer iRuleId) throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.RULE);
		final Collection<RuleEventUsageVO> result = new HashSet<RuleEventUsageVO>();

		Collection<MasterDataVO> mdVOList = getMasterDataFacade().getDependantMasterData(NuclosEntity.RULEUSAGE.getEntityName(), "rule", iRuleId);

		for (MasterDataVO vo : mdVOList)
			if (vo.getField("event").equals(sEventName))
				result.add(MasterDataWrapper.getREUsageVO(vo));

		return result;
	}

	/**
	 * Get all referenced entity names for a certain rule event.
	 * @return collection of entity names
	 * @throws CommonPermissionException
	 */
	@Override
	public Collection<String> getRuleUsageEntityNamesByEvent(String sEventName) throws CommonPermissionException {
		CollectableSearchCondition condEventEQUAL = SearchConditionUtils.newEOComparison(NuclosEntity.RULEUSAGE.getEntityName(), "event", ComparisonOperator.EQUAL, sEventName, MetaDataServerProvider.getInstance());

		List<EntityObjectVO> entities = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.RULEUSAGE.getEntityName()).getBySearchExpression(
			new CollectableSearchExpression(condEventEQUAL));
		final List<String> entityNames = CollectionUtils.transform(entities, new Transformer<EntityObjectVO, String>() {
			@Override
			public String transform(EntityObjectVO entityVO) {
				return entityVO.getField("entity", String.class);
			}
		});
		final List<String> distinctEntityNames = CollectionUtils.selectDistinct(entityNames, TransformerUtils.id());

		return distinctEntityNames;
	}

	/**
	 * gets a rule definition from the database by primary key.
	 * @param iId primary key of rule definition
	 * @return rule value object
	 * @throws CommonPermissionException
	 */
	@Override
	public RuleVO get(Integer iId) throws CommonFinderException, CommonPermissionException {
		this.checkRuleExecution();
		return RuleCache.getInstance().getRule(iId);
	}

	/**
	 * gets a rule definition from the database by name.
	 * NUCLOSINT-743
	 * @param ruleName Name of rule definition
	 * @return rule value object
	 * @throws CommonPermissionException
	 */
	@Override
	public RuleVO get(String ruleName) throws CommonFinderException, CommonPermissionException {
		this.checkRuleExecution();
		return RuleCache.getInstance().getRule(ruleName);
	}

	private void checkRuleExecution() throws CommonPermissionException {
		boolean blnExecuteRulesManually = false;
		if(SecurityCache.getInstance().getAllowedActions(this.getCurrentUserName()).contains(Actions.ACTION_EXECUTE_RULE_BY_USER)) {
			blnExecuteRulesManually = true;
		}
		if(!blnExecuteRulesManually)
			this.checkReadAllowed(NuclosEntity.RULE);
	}

	/**
	 * create a new rule definition in the database
	 * @param rulevo containing the rule
	 * @param mpDependants
	 * @return same layout as value object
	 * @throws CommonPermissionException
	 * @precondition (mpDependants != null) -> mpDependants.dependantsAreNew()
	 */
	@Override
	public RuleVO create(RuleVO rulevo, DependantMasterDataMap mpDependants) throws CommonCreateException, CommonFinderException, CommonRemoveException, CommonValidationException, CommonStaleVersionException, NuclosCompileException, CommonPermissionException {
		this.checkWriteAllowed(NuclosEntity.RULE);
		//check layout for validity
		rulevo.validate();	//throws CommonValidationException

		try {
			for (EntityObjectVO mdvo : mpDependants.getAllData()) {
				mdvo.getFields().put("rule", rulevo.getName());
				mdvo.getFields().put("ruleId", rulevo.getId());
				mdvo.getFieldIds().put("ruleId", IdUtils.toLongId(rulevo.getId()));
			}

			MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.RULE.getEntityName(), MasterDataWrapper.wrapRuleVO(rulevo), mpDependants);

			final RuleVO result = MasterDataWrapper.getRuleVO(mdVO);
			if (result.isActive()) {
				check(result);
			}
			RuleCache.getInstance().invalidate();
			return result;
		}
		catch (NuclosBusinessRuleException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * modify an existing rule definition in the database
	 * @param rulevo containing the rule
	 * @param mpDependants May be null.
	 * @return new rule as value object
	 * @throws NuclosCompileException
	 * @throws CommonPermissionException
	 */
	@Override
	public RuleVO modify(RuleVO rulevo, DependantMasterDataMap mpDependants) throws CommonCreateException, CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonValidationException, NuclosCompileException, CommonPermissionException {
		this.checkWriteAllowed(NuclosEntity.RULE);
		//check layout for validity
		rulevo.validate();	//throws CommonValidationException
		if (rulevo.isActive()) {
			check(rulevo);
		}
		try {
			validateUniqueConstraint(rulevo);

			Integer id = (Integer)getMasterDataFacade().modify(NuclosEntity.RULE.getEntityName(), MasterDataWrapper.wrapRuleVO(rulevo), mpDependants);
			MasterDataVO mdVO = getMasterDataFacade().get(NuclosEntity.RULE.getEntityName(), id);

			RuleCache.getInstance().invalidate();

			return MasterDataWrapper.getRuleVO(mdVO);
		}
		catch (NuclosBusinessRuleException ex) {
			throw new CommonFatalException(ex);
		}
	}

	private void validateUniqueConstraint(RuleVO rulevo) throws CommonValidationException {
		CollectableComparison cond = SearchConditionUtils.newMDComparison(
			MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.RULE),"rule", ComparisonOperator.EQUAL, rulevo.getName());
		Collection<MasterDataVO> mdVOList = getMasterDataFacade().getMasterData(NuclosEntity.RULE.getEntityName(), cond, true);

		if (mdVOList != null) {
			for (MasterDataVO mdVO : mdVOList) {
				if(rulevo.getId().intValue() != mdVO.getIntId().intValue()){
					throw new CommonValidationException(
						StringUtils.getParameterizedExceptionMessage("validation.unique.constraint", "Name", "Rule"));
				}
			}
		}
	}


	/**
	 * delete rule definition from database
	 * @param rulevo containing the rule
	 * @throws CommonPermissionException
	 * @throws NuclosBusinessRuleException
	 */
	@Override
	public void remove(RuleVO rulevo) throws CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonPermissionException, NuclosBusinessRuleException, NuclosCompileException {
		this.checkDeleteAllowed(NuclosEntity.RULE);

		if (rulevo.isActive()) {
			NuclosJavaCompiler.check(new RuleCodeGenerator<NuclosRule>(new RuleEngineFacadeBean.RuleTemplateType(), rulevo), true);
		}

		MasterDataVO mdVO = getMasterDataFacade().get(NuclosEntity.RULE.getEntityName(), rulevo.getId());

		// check for stale version:
		if (mdVO.getVersion() != rulevo.getVersion()) {
			throw new CommonStaleVersionException();
		}
		//mdVO.remove();

		getMasterDataFacade().remove(NuclosEntity.RULE.getEntityName(), mdVO, true);
		RuleCache.getInstance().invalidate();
	}

	/**
	 * imports the given rules, adding new and overwriting existing rules. The other existing rules are untouched.
	 * Currently, only the rule code is imported, not the usages. If one rule cannot be imported, the import will be aborted.
	 * @param collRuleWithUsages
	 */
	@Override
	@RolesAllowed("UseManagementConsole")
	public void importRules(Collection<RuleWithUsagesVO> collRuleWithUsages) throws CommonBusinessException {
		for (RuleWithUsagesVO ruleWithUsagesVO : collRuleWithUsages) {
			try {
				this.importRule(ruleWithUsagesVO);
			}
			catch (CommonBusinessException ex) {
				throw new CommonBusinessException(StringUtils.getParameterizedExceptionMessage("rule.import.error", ruleWithUsagesVO.getName()), ex);
				//"Fehler beim Importieren der Regel \"" + ruleWithUsagesVO.getName() + "\" aufgetreten.", ex);
			}
		}
	}

	/**
	 * imports the given rule. If a rule with this name exists already, it will be overwritten, otherwise
	 * the new rule will be added.
	 * Currently, only the rule code is imported, not the usages.
	 * @param ruleWithUsages
	 * @throws CommonCreateException
	 */
	private void importRule(RuleWithUsagesVO ruleWithUsages) throws CommonCreateException, CommonValidationException, CommonStaleVersionException, CommonFinderException, CommonRemoveException, CommonPermissionException, NuclosBusinessRuleException {
		final RuleVO rulevoNew = ruleWithUsages.getRuleVO();
		rulevoNew.validate();

		CollectableComparison cond = SearchConditionUtils.newMDComparison(
			MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.RULE),"rule", ComparisonOperator.EQUAL, ruleWithUsages.getName());
		Collection<MasterDataVO> mdVOList = getMasterDataFacade().getMasterData(NuclosEntity.RULE.getEntityName(), cond, true);

		assert mdVOList.size() <= 1;

		if (mdVOList != null && mdVOList.size() > 0) {
			final RuleVO rulevo = MasterDataWrapper.getRuleVO(mdVOList.iterator().next());
			rulevo.setRuleSource(rulevoNew.getRuleSource());
			getMasterDataFacade().modify(NuclosEntity.RULE.getEntityName(), MasterDataWrapper.wrapRuleVO(rulevo), null);
			RuleCache.getInstance().invalidate();
		}
		else {
			getMasterDataFacade().create(NuclosEntity.RULE.getEntityName(), MasterDataWrapper.wrapRuleVO(rulevoNew), null);
			RuleCache.getInstance().invalidate();
		}

		// import event usages:
		/** @todo	*/

		// import transition usages:
		/** @todo	*/
	}

	/**
	 * Compile the user defined code.
	 * @param ruleVO
	 * @throws NuclosCompileException
	 */
	@Override
	@RolesAllowed("Login")
	public void check(RuleVO ruleVO) throws NuclosCompileException {
		NuclosJavaCompiler.check(new RuleCodeGenerator<NuclosRule>(new RuleEngineFacadeBean.RuleTemplateType(), ruleVO), false);
	}

	/**
	 * Returns a template for new rules to display in the rule editor.
	 * @return String containing class template
	 */
	@Override
	public String getClassTemplate() {
		final StringBuffer sb = new StringBuffer();
		sb.append("/** @name        \n");
		sb.append("  * @description \n");
		sb.append("  * @usage       \n");
		sb.append("  * @change      \n");
		sb.append("*/\n\n");
		sb.append("public void rule(RuleInterface server) throws NuclosBusinessRuleException {\n\n}");
		return sb.toString();
	}

	private static class UsageByOrderComparator implements Comparator<RuleEventUsageVO> {
		@Override
		public int compare(RuleEventUsageVO u1, RuleEventUsageVO u2) {
			return u1.getOrder().compareTo(u2.getOrder());
		}
	}

	private static class GeneratorByOrderComparator implements Comparator<RuleEngineGenerationVO> {
		@Override
		public int compare(RuleEngineGenerationVO u1, RuleEngineGenerationVO u2) {
			return u1.getOrder().compareTo(u2.getOrder());
		}
	}

	private static class TransitionByOrderComparator implements Comparator<RuleEngineTransitionVO> {
		@Override
		public int compare(RuleEngineTransitionVO u1, RuleEngineTransitionVO u2) {
			return u1.getOrder().compareTo(u2.getOrder());
		}
	}

	private static class GenerationOrderComparator implements Comparator<GeneratorActionVO> {
		@Override
		public int compare(GeneratorActionVO g1, GeneratorActionVO g2) {
			return g1.getName().compareTo(g2.getName());
		}
	}

	/**
	 * Delete the Output Path Directory.
	 */
	@Override
	@RolesAllowed("UseManagementConsole")
	public void deleteDirectoryOutputPath() {
		final File fOutputPath = NuclosJavaCompiler.getOutputPath();
		final File[] find = fOutputPath.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File fDir, String sName) {
				return (sName.endsWith(".java") || sName.endsWith(".class"));
			}
		});

		if(find != null) {
			for (File fDelete : find) {
				fDelete.delete();
			}
		}
	}

	/**
	 * invalidates the rule cache
	 */
	@Override
	@RolesAllowed("Login")
	public void invalidateCache() {
		RuleCache.getInstance().invalidate();
	}

	private RuleCodeGenerator<NuclosRule> getGenerator(RuleVO ruleVO) {
		return new RuleCodeGenerator<NuclosRule>(new RuleTemplateType(), ruleVO);
	}

	static final String[] IMPORTS = new String[] {
		"org.nuclos.api.context.*",
		"org.nuclos.common.collect.collectable.CollectableEntity",
		"org.nuclos.common.collect.collectable.CollectableEntityField",
		"org.nuclos.common.collect.collectable.Collectable",
		"org.nuclos.common.collect.collectable.CollectableField",
		"org.nuclos.common.collect.collectable.CollectableValueField",
		"org.nuclos.common.collect.collectable.CollectableValueIdField",
		"org.nuclos.common.collect.collectable.searchcondition.*",
		"org.nuclos.common.*",
		"org.nuclos.common.mail.*",
		"org.nuclos.common.fileimport.*",
		"org.nuclos.server.common.*",
		"org.nuclos.server.common.calendar.CommonDate",
		"org.nuclos.server.customcode.NuclosRule",
		"org.nuclos.server.genericobject.valueobject.*",
		"org.nuclos.server.masterdata.valueobject.*",
		"org.nuclos.server.ruleengine.*",
		"org.nuclos.common.dal.vo.EntityObjectVO",
		"java.util.*"
	};

	@Configurable
	public static class RuleTemplateType extends AbstractRuleTemplateType<NuclosRule> {
		
		private ServerParameterProvider serverParameterProvider;

		public RuleTemplateType() {
			super("Rule", NuclosRule.class);
		}
		
		@Autowired
		void setServerParameterProvider(ServerParameterProvider serverParameterProvider) {
			this.serverParameterProvider = serverParameterProvider;
		}

		@Override
		protected List<String> getImports() {
			List<String> imports = new ArrayList<String>();
			CollectionUtils.addAll(imports, IMPORTS);
			final String additionalImports = serverParameterProvider.getValue(ParameterProvider.KEY_ADDITIONAL_IMPORTS_FOR_RULES);
			if (additionalImports != null) {
				CollectionUtils.addAll(imports, additionalImports.split(","));
			}
			imports.addAll(getWebserviceImports());
			return imports;
		}

		@Override
		protected String getHeaderImpl(RuleVO ruleVO) {
			final String ruleName = getClassName(ruleVO);
			final StringBuilder sb = new StringBuilder();
			sb.append("\npublic class ");
			sb.append(ruleName);
			sb.append(" implements NuclosRule {\n\t");
			sb.append("public ");
			sb.append(ruleName);
			sb.append("() {\n\t}\n\n");
			return sb.toString();
		}

		@Override
		public String getFooter() {
			return "\n}";
		}

		@Override
		public String getLabel() {
			return "Business rule \"{0}\"";
		}

		@Override
		public String getEntityname() {
			return NuclosEntity.RULE.getEntityName();
		}
	}
}
