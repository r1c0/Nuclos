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
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.communication.MailCommunicator;
import org.nuclos.common2.communication.exception.CommonCommunicationException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.NuclosScheduler;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.impl.DataSourceExecutor;
import org.nuclos.server.dblayer.incubator.DbExecutor.ResultSetRunner;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.ejb3.GeneratorFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectRelationVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.job.ejb3.JobControlFacadeLocal;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.RelationDirection;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosFatalRuleException;
import org.nuclos.server.ruleengine.jobs.StateChangeJob;
import org.nuclos.server.ruleengine.jobs.TestJob;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.statemodel.NuclosNoAdequateStatemodelException;
import org.nuclos.server.statemodel.NuclosSubsequentStateNotLegalException;
import org.nuclos.server.statemodel.ejb3.StateFacadeLocal;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.transaction.annotation.Transactional;

/**
* Interface bean for rule developers.
* <br>
* <br>Created by Novabit Informationssysteme GmbH
* <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
@Stateless
@Local(RuleInterfaceFacadeLocal.class)
@Transactional
public class RuleInterfaceFacadeBean extends NuclosFacadeBean implements RuleInterfaceFacadeLocal {
// @todo add assertions!
	//private StateFacadeLocal stateFacade = ServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
	//private GenericObjectFacadeLocal goFacade = ServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);
	//private MasterDataFacadeLocal mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
	//private GeneratorFacadeLocal generatorFacade = ServiceLocator.getInstance().getFacade(GeneratorFacadeLocal.class);
	//private JobControlFacadeLocal jobFacade = ServiceLocator.getInstance().getFacade(JobControlFacadeLocal.class);

	@Override
	public Integer getModuleId(Integer iGenericObjectId) {
		try {
			return getGenericObjectFacade().getModuleContainingGenericObject(iGenericObjectId);
		}
		catch (CommonFinderException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	@Override
	public GenericObjectVO getGenericObject(Integer iGenericObjectId) throws CommonFinderException, CommonPermissionException {
		return getGenericObjectFacade().get(iGenericObjectId);
	}

	@Override
	public boolean isStateEqual(Integer iGenericObjectId, int iNumeral) throws CommonFinderException {
		final StateVO statevoCurrent = getFacade(StateFacadeLocal.class).getCurrentState(this.getModuleId(iGenericObjectId), iGenericObjectId);
		return new Integer(iNumeral).equals(statevoCurrent.getNumeral());
	}

	@Override
	public boolean isAttributeEqual(Integer iGenericObjectId, String sAttribute, Object oValue) {
		try {
//			GenericObjectAttribute attr = goFacade.findAttributeByGoAndAttributeId(iGenericObjectId,AttributeCache.getInstance().getAttribute(sAttribute).getId());
			Object sFieldValue = DalSupportForGO.getEntityObject(iGenericObjectId).getFields().get(sAttribute);

			return (oValue == null) ? (sFieldValue == null) : oValue.equals(sFieldValue);
		}
		catch (Exception ex) {
			throw new NuclosFatalException(ex);
		}
	}

	@Override
	public boolean isAttributeNull(Integer iGenericObjectId, String sAttribute) {
		try {
//			GenericObjectAttribute attr = goFacade.findAttributeByGoAndAttributeId(iGenericObjectId,AttributeCache.getInstance().getAttribute(sAttribute).getId());
			Object sFieldValue = DalSupportForGO.getEntityObject(iGenericObjectId).getFields().get(sAttribute);

			return (sFieldValue == null);
		}
		catch (Exception ex) {
			throw new NuclosFatalException(ex);
		}
	}

	@Override
	public void sendMessage(String[] asRecipients, String sSubject, String sMessage) throws NuclosBusinessRuleException {
		final MailCommunicator mailcommunicator = new MailCommunicator(ServerParameterProvider.getInstance().getValue("SMTP Server"), ServerParameterProvider.getInstance().getValue("SMTP Username"), ServerParameterProvider.getInstance().getValue("SMTP Password"));
		final String[] asRecipientAddresses = new String[asRecipients.length];
		for (int i = 0; i < asRecipients.length; i++) {
			final String sRecipient = asRecipients[i];
			if (mailcommunicator.isValid(sRecipient)) {
				asRecipientAddresses[i] = sRecipient;
			}
			else {
				DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
				DbQuery<String> query = builder.createQuery(String.class);
				DbFrom t = query.from("T_MD_USER").alias(ProcessorFactorySingleton.BASE_ALIAS);
				query.select(t.column("STREMAIL", String.class));
				query.where(builder.equal(builder.upper(t.column("STRUSER", String.class)), builder.upper(builder.literal(sRecipient))));

				// @todo P2 There will be a NPE in mailcommunicator.sendMessage(). Replace array with Collection.
				String sEmail = CollectionUtils.getFirst(DataBaseHelper.getDbAccess().executeQuery(query));
				asRecipientAddresses[i] = sEmail;
			}
		}
		try {
			mailcommunicator.sendMessage(ServerParameterProvider.getInstance().getValue("SMTP Authentication"), ServerParameterProvider.getInstance().getValue("SMTP Sender"), asRecipientAddresses, sSubject, sMessage);
		}
		catch (CommonCommunicationException ex) {
			throw new NuclosBusinessRuleException(StringUtils.getParameterizedExceptionMessage("task.facade.exception", ex.getMessage()), ex);
				//"Es ist ein Fehler beim Versenden einer Benachrichtigung per E-Mail aufgetreten./n/n" + ex.getMessage(), ex);
		}
	}

	@Override
	public Integer createObject(Integer iGenericObjectId, String sGenerator) throws NuclosBusinessRuleException {
		try {
			return getFacade(GeneratorFacadeLocal.class).generateGenericObject(iGenericObjectId, sGenerator);
		}
		catch (CommonBusinessException ex) {
			throw new NuclosFatalRuleException(ex);
		}
	}

	@Override
	public Integer createObject(String sEntityName, Integer iObjectId, String sGenerator) throws NuclosBusinessRuleException {
		try {
			return getFacade(GeneratorFacadeLocal.class).generateGenericObject(iObjectId, sGenerator);
		}
		catch (CommonBusinessException ex) {
			throw new NuclosFatalRuleException(ex);
		}
	}

	@Override
	public Integer createObject(RuleObjectContainerCVO loccvo, String sGenerator) throws NuclosBusinessRuleException {
		try {
			return getFacade(GeneratorFacadeLocal.class).generateGenericObject(loccvo, sGenerator);
		}
		catch (CommonBusinessException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
	}

	/**
	 * retrieves the attribute value with the given name in the leased object with the given id.
	 * @postcondition result != null
	 * @postcondition !result.isRemoved()
	 */
	@Override
	public DynamicAttributeVO getAttribute(Integer iGenericObjectId, String sAttribute) {
		DynamicAttributeVO result;

		Integer iAttributeId = null;
		try {
			iAttributeId = AttributeCache.getInstance().getAttribute(this.getModuleId(iGenericObjectId), sAttribute).getId();
			result = getGenericObjectFacade().findAttributeByGoAndAttributeId(iGenericObjectId, iAttributeId);
		}
		catch (CommonFinderException ex) {
			result = new DynamicAttributeVO(iAttributeId, null, null);
		}
		catch(NullPointerException ex){
			result = new DynamicAttributeVO(iAttributeId, null, null);
		}
		assert result != null;
		assert !result.isRemoved();
		return result;
	}

	/**
	 * sets the attribute with the given name in the leased object with the given id to the given value id and value.
	 * The leased object is read from the database and stored later (after the change).
	 * @precondition iGenericObjectId != null
	 */
	@Override
	public void setAttribute(RuleVO ruleVO, Integer iGenericObjectId, String sAttribute, Integer iValueId, Object oValue) throws NuclosBusinessRuleException {
		if (iGenericObjectId == null) {
			throw new NullArgumentException("iGenericObjectId");
		}
		//Object oldValue = null;
		try {
			/*oldValue =*/ getAttribute(iGenericObjectId, sAttribute).getValue();
		}
		catch (RuntimeException ex) {
			//to be sure that there's no interference in processing rules all exceptions caused by
			//logging will be dropped
		}
		try {
			getGenericObjectFacade().setAttribute(iGenericObjectId, sAttribute, iValueId, oValue);
		}
		catch (CommonBusinessException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
	}

	/**
	 * sets the attribute with the given name value in the given GenericObjectVO to the given value id and value.
	 * This method does not apply any changes to the database.
	 */
	@Override
	public void setAttribute(RuleVO ruleVO, GenericObjectVO govo, String sAttribute, Integer iValueId, Object oValue) {
		final Integer iAttributeId = AttributeCache.getInstance().getAttribute(govo.getModuleId(), sAttribute).getId();
		DynamicAttributeVO attrvo = govo.getAttribute(iAttributeId);
		//Object oldValue = null;

		if (attrvo == null) {
			attrvo = new DynamicAttributeVO(iAttributeId, iValueId, oValue);
		}
		else {
			/*oldValue =*/ attrvo.getValue();
			attrvo.setValueId(iValueId);
			attrvo.setValue(oValue);
			attrvo.unremove();
		}
		govo.setAttribute(attrvo);
	}

	/**
	 * sets the field with the given name value in the given MasterDataVO to the given value id and value.
	 * This method does not apply any changes to the database.
	 */
	@Override
	public void setMasterDataField(String sEntityName, MasterDataVO mdvo, String sFieldName, Integer iValueId, Object oValue) {
		if (MasterDataMetaCache.getInstance().getMetaData(sEntityName).getField(sFieldName).getForeignEntity() != null) {
			mdvo.setField(sFieldName + "Id", iValueId);
		}
		mdvo.setField(sFieldName, oValue);
	}

	/**
	 * sets the field with the given name in the masterdata object with the given id to the given value id and value.
	 * The masterdata object is read from the database and stored later (after the change).
	 * @precondition iId != null
	 */
	@Override
	public void setMasterDataField(String sEntityName, Integer iId, String sFieldName, Integer iValueId, Object oValue) {
		if (iId == null) {
			throw new NullArgumentException("iId");
		}
		MasterDataVO mdvo = this.getMasterData(sEntityName, iId);

		this.setMasterDataField(sEntityName, mdvo, sFieldName, iValueId, oValue);

		try {
			// NUCLOS-29, save without dependants because save with dependants would trigger a layoutml lookup
			// which leads to exception if there is not layout. In this case, dependants are empty anyway.
			getMasterDataFacade().modify(sEntityName, mdvo, null);
		}
		catch (Exception ex) {
			throw new NuclosFatalRuleException(ex);
		}
	}

	/**
	 * @precondition iGenericObjectId != null
	 */
	@Override
	public GenericObjectVO changeState(GenericObjectVO govoCurrent, Integer iGenericObjectId, int iNumeral) throws NuclosBusinessRuleException {
		try {
			final boolean bSyncNeeded = (govoCurrent != null) && iGenericObjectId.equals(govoCurrent.getId());

			GenericObjectVO goVO = null;
			Integer newId = null;

			if (bSyncNeeded) {
				/*goVO = MasterDataWrapper.getGenericObjectVO(mdFacade.get(NuclosEntity.GENERICOBJECT.getEntityName(), iGenericObjectId));
				goVO.setModuleId(govoCurrent.getModuleId());
				goVO.setParentId(govoCurrent.getParentId());
				goVO.setDeleted(govoCurrent.isDeleted());
				goVO.setAttributes(govoCurrent.getAttributes());
				goFacade.setValueObject(goVO);
				newId = (Integer)mdFacade.modify(NuclosEntity.GENERICOBJECT.getEntityName(), MasterDataWrapper.wrapGenericObjectVO(goVO), null);*/
				DalSupportForGO.getEntityObjectProcesserForGenericObject(iGenericObjectId).insertOrUpdate(DalSupportForGO.wrapGenericObjectVO(govoCurrent));
				newId = (govoCurrent.getId() != null) ? govoCurrent.getId() : iGenericObjectId;
			}

			this.changeState(iGenericObjectId, iNumeral);

			return bSyncNeeded ? getGenericObjectFacade().get(newId) : govoCurrent;
		}
		catch (CommonFinderException ex) {
			throw new NuclosFatalRuleException(ex);
		}
		catch (CommonPermissionException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
//		catch (CommonValidationException ex) {
//			throw new NuclosBusinessRuleException(ex);
//		}
		catch (NuclosBusinessException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
//		catch (CommonStaleVersionException ex) {
//			throw new NuclosBusinessRuleException(ex);
//		}
//		catch (CommonRemoveException ex) {
//			throw new NuclosBusinessRuleException(ex);
//		}
//		catch (CommonCreateException ex) {
//			throw new CommonFatalException(ex);
//		}
	}

	/**
	 * @precondition iGenericObjectId != null
	 */
	@Override
	public void changeState(Integer iGenericObjectId, int iNumeral) throws NuclosBusinessRuleException {
		// @todo defer sync to changeStateByRule
		try {
			getFacade(StateFacadeLocal.class).changeStateByRule(this.getModuleId(iGenericObjectId), iGenericObjectId, iNumeral);
		}
		catch (CommonCreateException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
		catch (CommonPermissionException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
		catch (NuclosNoAdequateStatemodelException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
		catch (NuclosSubsequentStateNotLegalException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
		catch (CommonFinderException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
		catch (NuclosBusinessException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
	}

	/**
	 * performs a state change for the leased object with the given id at the given point in time. If an old job exists already, it
	 * is always removed.
	 * @param iGenericObjectId
	 * @param iNewState the new state for the object.
	 * @param dateToSchedule the date for the state change to happen. If <code>null</code> only a possibly existing job is removed.
	 * If <code>dateToSchedule</code> is in the future, a new job is scheduled for the given date. If <code>dateToSchedule</code> is in the past,
	 * the state change is executed immediately (synchronously).
	 * @precondition iGenericObjectId != null
	 * @throws NuclosBusinessRuleException if the transition from the current state to the new state is not possible for the given object.
	 */
	@Override
	public GenericObjectVO scheduleStateChange(GenericObjectVO govoCurrent, Integer iGenericObjectId, int iNewState, Date dateToSchedule)
			throws NuclosBusinessRuleException, CommonFinderException {

		if (iGenericObjectId == null) {
			throw new NuclosFatalRuleException("scheduleStateChangeJob(Integer iGenericObjectId, int iNewState, Date dateToSchedule): iGenericObjectId darf nicht null sein.");
		}
		if (!this.isStateChangePossible(iGenericObjectId, iNewState)) {
			throw new NuclosBusinessRuleException(StringUtils.getParameterizedExceptionMessage("state.transition.error", iNewState));
				//"Der Status\u00fcbergang nach " + iNewState + " ist nicht m\u00f6glich.");
		}

		GenericObjectVO result = govoCurrent;
		try {
			final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();
			final String sJobName = "InnerStateChangeJob for GenericObjectId " + iGenericObjectId + " and target state " + iNewState;
			// Always delete a possibly existing old job:
			if (scheduler.deleteJob(sJobName, Scheduler.DEFAULT_GROUP)) {
				info("Removed " + sJobName + ".");
			}

			if (dateToSchedule != null) {
				if (dateToSchedule.before(new Date())) {
					// execute state change immediately:
					result = this.changeState(govoCurrent, iGenericObjectId, iNewState);
				}
				else {
					final JobDetail jobDetail = new JobDetail(sJobName, Scheduler.DEFAULT_GROUP, StateChangeJob.class);
					jobDetail.getJobDataMap().put(StateChangeJob.PARAM_LEASEDOBJECTID, iGenericObjectId.intValue());
					jobDetail.getJobDataMap().put(StateChangeJob.PARAM_NEWSTATE, iNewState);

					scheduler.scheduleJob(jobDetail, new SimpleTrigger(sJobName, Scheduler.DEFAULT_GROUP, dateToSchedule));
					info("Successfully scheduled " + sJobName + " at " + dateToSchedule.toString() + ".");
				}
			}
		}
		catch (SchedulerException ex) {
			throw new NuclosFatalException(ex);
		}
		return result;
	}

	/**
	 * schedules a test job once for ten seconds later.
	 */
	@Override
	public void scheduleTestJob() {
		final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();
		final String sDate = new Date().toString();

		final JobDetail jobDetail = new JobDetail("Test job (" + sDate + ")", Scheduler.DEFAULT_GROUP, TestJob.class);
		final SimpleTrigger jobTrigger = new SimpleTrigger("Test job (" + sDate + ")", Scheduler.DEFAULT_GROUP, new Date(System.currentTimeMillis() + 10000L));
		try {
			scheduler.scheduleJob(jobDetail, jobTrigger);
		}
		catch (SchedulerException ex) {
			throw new NuclosFatalException(ex);
		}
		info("Successfully scheduled TestJob.");
	}

	/**
	 * @param iModuleId
	 * @param cond
	 * @return Collection<Integer>
	 */
	@Override
	public Collection<Integer> getGenericObjectIds(Integer iModuleId, CollectableSearchCondition cond) {
		return getGenericObjectFacade().getGenericObjectIds(iModuleId, cond);
	}

	/**
	 * @param sEntityName
	 * @param cond
	 * @return Collection<Integer>
	 */
	@Override
	public Collection<Object> getMasterDataIds(String sEntityName, CollectableSearchExpression cond) {
		return getMasterDataFacade().getMasterDataIds(sEntityName, cond);
	}

	/**
	 * @param sEntityName
	 * @param cond
	 * @return Collection<Integer>
	 */
	@Override
	public Collection<Object> getMasterDataIds(String sEntityName) {
		return getMasterDataFacade().getMasterDataIds(sEntityName);
	}

	/**
	 * @param iModuleId
	 * @param iGenericObjectId
	 * @param direction
	 * @param relationType
	 * @return ids of the leased objects of the given module related to the given leased object in the specified way.
	 * @postcondition result != null
	 * @postcondition (iModuleId == null) --> result.isEmpty()
	 * @postcondition (iGenericObjectId == null) --> result.isEmpty()
	 */
	@Override
	public Collection<Integer> getRelatedGenericObjectIds(Integer iModuleId, Integer iGenericObjectId, RelationDirection direction, String relationType) {
		final Collection<Integer> result = getGenericObjectFacade().getRelatedGenericObjectIds(iModuleId, iGenericObjectId, direction, relationType);

		assert result != null;
		assert !(iModuleId == null) || result.isEmpty();
		assert !(iGenericObjectId == null) || result.isEmpty();
		return result;
	}

	@Override
	public void relate(Integer iGenericObjectIdSource, Integer iGenericObjectIdTarget, String relationType,
			Date dateValidFrom, Date dateValidUntil, String sDescription)
			throws CommonFinderException, CommonCreateException, CommonPermissionException, NuclosBusinessRuleException
	{
		final Integer iModuleIdTarget = getGenericObjectFacade().getModuleContainingGenericObject(iGenericObjectIdTarget);
		getGenericObjectFacade().relate(iModuleIdTarget, iGenericObjectIdTarget, iGenericObjectIdSource, relationType, dateValidFrom, dateValidUntil, sDescription);
	}

	@Override
	public void unrelate(Integer iGenericObjectIdSource, Integer iGenericObjectIdTarget, String relationType)
			throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonRemoveException {

		final Integer iModuleIdTarget = getGenericObjectFacade().getModuleContainingGenericObject(iGenericObjectIdTarget);
		getGenericObjectFacade().unrelate(iModuleIdTarget, iGenericObjectIdTarget, iGenericObjectIdSource, relationType);
	}

	/**
	 * invalidates the given relations by setting "validUntil" to the current date, if necessary
	 */
	@Override
	public void invalidateRelations(Integer iGenericObjectIdSource, Integer iGenericObjectIdTarget, String relationType)
			throws CommonFinderException, CommonBusinessException, NuclosBusinessRuleException {
		try {
			for (GenericObjectRelationVO vo : getGenericObjectFacade().findRelations(iGenericObjectIdSource, relationType, iGenericObjectIdTarget))
			{
				final Date dateOld = vo.getValidUntil();
				final Date dateNew = DateUtils.today();
				if (dateOld == null || dateNew.before(dateOld)) {
					vo.setValidUntil(dateNew);
					getMasterDataFacade().modify(NuclosEntity.GENERICOBJECTRELATION.getEntityName(), MasterDataWrapper.wrapGenericObjectRelationVO(vo), null);
				}
			}
		}
		catch (CommonPermissionException ex) {
			throw new CommonBusinessException(ex);
		}
	}

	@Override
	public boolean isStateChangePossible(Integer iGenericObjectId, int iNewState) throws CommonFinderException {
		final boolean result;
		try {
			final Collection<StateVO> collSubsequentStates = getFacade(StateFacadeLocal.class).getSubsequentStates(this.getModuleId(iGenericObjectId), iGenericObjectId, true);
			final Set<Integer> stNumerals = new HashSet<Integer>();
			for (StateVO statevo : collSubsequentStates) {
				stNumerals.add(statevo.getNumeral());
			}
			result = stNumerals.contains(iNewState);
		}
		catch (NuclosNoAdequateStatemodelException ex) {
			throw new NuclosFatalRuleException(ex);
		}
		return result;
	}

	/**
	 * call a database procedure
	 * @param sProcedureName the name of the procedure to call
	 * @param oParams the parameters (note that it is not possible to use null as a parameter use {@code DbNull} instead)
	 * @throws NuclosBusinessRuleException
	 */
	@Override
	public void callDbProcedure(String sProcedureName, Object... oParams) throws NuclosBusinessRuleException {
		try {
			DataBaseHelper.getDbAccess().executeProcedure(sProcedureName, oParams);
		} catch (DbException e) {
			throw new NuclosBusinessRuleException(e.getMessage(), e);
		}
	}

	/**
	 * call a database function
	 * @param sFunctionName the name of the function to call
	 * @param iResultType the type of the function result as defined in java.sql.Types
	 * @param oParams the parameters (note that it is not possible to use null as a parameter use {@code DbNull} instead)
	 * @return the result of the function the object is of the java type corresponding to iResultType
	 * @throws NuclosBusinessRuleException
	 */
	@Override
	public <T> T callDbFunction(String sFunctionName, Class<T> resultType, Object... oParams) throws NuclosBusinessRuleException {
		try {
			return DataBaseHelper.getDbAccess().executeFunction(sFunctionName, resultType, oParams);
		} catch (DbException e) {
			throw new NuclosBusinessRuleException(e.getMessage(), e);
		}
	}

	/**
	 *
	 * @param jndiName the JNDI Name set in the -ds.xml File
	 * @param selectStatement the select Statement to execute
	 * @return a {@link Collection} with {@link MasterDataVO} (easier to work with in a rule, columnames are the fields)
	 * @throws NuclosBusinessRuleException if the ds was not found
	 */
	@Override
	public Collection<MasterDataVO> executeSelectOnJCADatasource(String jndiName, String selectStatement) throws NuclosBusinessRuleException {
		DataSource db;

		try {
			db = (DataSource) SpringApplicationContextHolder.getBean(jndiName);
		} catch(Exception e) {
			throw new NuclosBusinessRuleException(e);
		}

		DataSourceExecutor executor = new DataSourceExecutor(db);
		try {
			return executor.executeQuery(selectStatement, new ResultSetRunner<Collection<MasterDataVO>>() {
				@Override
				public java.util.Collection<MasterDataVO> perform(ResultSet result) throws SQLException {
	            Collection<MasterDataVO> values = new ArrayList<MasterDataVO>();

	            MasterDataVO mdvo = null;
	            HashMap<String, Object> mpFields = null;
	            HashMap<Integer, String> columnames = new HashMap<Integer, String>();
	            ResultSetMetaData metadata = result.getMetaData();

	            for (int i = 1; i <= metadata.getColumnCount(); i++) {
	            	String columName = metadata.getColumnName(i);
	            	columnames.put(i, columName);
	            }

	             while (result.next()) {
	                mpFields = new HashMap<String, Object>();
	                for (Integer columnNumber : columnames.keySet()) {
	               	Object value = result.getObject(columnNumber);
	               	 mpFields.put(columnames.get(columnNumber), value);
	               }

	               mdvo = new MasterDataVO(null, null, null, null, null, null, mpFields);
	               values.add(mdvo);
	            }

	            return values;
				}
			});
		} catch (DbException e) {
			return null;
		} finally {
			executor.release();
		}
	}

	@Override
	public EntityObjectVO getEntityObject(String entity, Long id) {
		return NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getByPrimaryKey(id);
	}

	@Override
	public MasterDataVO getMasterData(String sEntityName, Integer iId) {
		try {
			return getMasterDataFacade().get(sEntityName, iId);
		}
		catch (Exception ex) {
			throw new NuclosFatalRuleException(ex);
		}
	}

	/**
	 * This Method sets the Connectionsettings defined in the Webservice Entity to the Stub.
	 * The Service is looked up by the serviceName.
	 *
	 * Everything set like Username, Encoding etc is set to the Stub.
	 * Somehow a Connection Initializer.
	 *
	 * @param Stub stub
	 * @param String servicename
	 */
	@Override
	public void setConnectionSettingsForWebservice(Object stub, String serviceName) throws NuclosBusinessRuleException {
		//public void setConnectionSettingsForWebservice(Stub stub, String serviceName) throws NuclosBusinessRuleException {

		MasterDataMetaVO webserviceMeta = getMasterDataFacade().getMetaData(NuclosEntity.WEBSERVICE.getEntityName());
		CollectableComparison condition = SearchConditionUtils.newMDComparison(webserviceMeta, "name", ComparisonOperator.EQUAL, serviceName);
		Collection<Object> foundService = getMasterDataIds(NuclosEntity.WEBSERVICE.getEntityName(), new CollectableSearchExpression(condition));

		if (foundService.size() == 0) {
			throw new NuclosBusinessRuleException("Kein Service gefunden");
		} else if (foundService.size() > 1) {
			throw new NuclosBusinessRuleException("Mehrere Services gefunden");
		}

		Object serviceId = foundService.iterator().next();
		MasterDataVO service = getMasterData(NuclosEntity.WEBSERVICE.getEntityName(), (Integer) serviceId);

		String username = (String)service.getField("user");
		String password = (String) service.getField("password");
		String url = (String) service.getField("host");

		try {
//    		stub._getServiceClient().getOptions().setTo(new EndpointReference(url));
        	Class<?> clzzEndpointReference = stub.getClass().getClassLoader().loadClass("org.apache.axis2.addressing.EndpointReference");
        	Object serviceClient = stub.getClass().getMethod("_getServiceClient").invoke(stub);
        	Object serviceClientOptions = serviceClient.getClass().getMethod("getOptions").invoke(serviceClient);
			serviceClientOptions.getClass().getMethod("setTo",
				clzzEndpointReference).invoke(serviceClientOptions, clzzEndpointReference.getConstructor(url.getClass()).newInstance(url));
			Method mthdSetProperty = serviceClientOptions.getClass().getMethod("setProperty", String.class, Object.class);

			Boolean useProxy = (Boolean) service.getField("useproxy");
			String authentificationType = (String) service.getField("authtype");
			String encoding = (String) service.getField("encoding");

			//PROXY
			if (useProxy != null) {
				if (useProxy) {
					String proxyName = (String) service.getField("proxyname");
					Integer proxyPort = (Integer) service.getField("proxyport");
					String proxyusername = (String) service.getField("proxyuser");
					String proxypasswort = (String) service.getField("proxypassword");
					String proxydomain = (String) service.getField("proxydomain");

					// PROXY Configuration
//					ProxyProperties proxyConfig = new ProxyProperties();
					Class<?> clzzProxyProperties = stub.getClass().getClassLoader().loadClass("org.apache.axis2.transport.http.HttpTransportProperties$ProxyProperties");
					Object proxyConfig = clzzProxyProperties.newInstance();

					if (!StringUtils.isNullOrEmpty(proxyName))
//						proxyConfig.setProxyName(proxyName);
						clzzProxyProperties.getMethod("setProxyName", String.class).invoke(proxyConfig, proxyName);
					if (proxyPort != null)
//						proxyConfig.setProxyPort(proxyPort);
						clzzProxyProperties.getMethod("setProxyPort", int.class).invoke(proxyConfig, proxyPort);
					if (!StringUtils.isNullOrEmpty(proxyusername))
//						proxyConfig.setUserName(proxyusername);
						clzzProxyProperties.getMethod("setUserName", String.class).invoke(proxyConfig, proxyusername);
					if (!StringUtils.isNullOrEmpty(proxypasswort))
//						proxyConfig.setPassWord(proxypasswort);
						clzzProxyProperties.getMethod("setPassWord", String.class).invoke(proxyConfig, proxypasswort);
					if (!StringUtils.isNullOrEmpty(proxydomain))
//						proxyConfig.setDomain(proxydomain);
						clzzProxyProperties.getMethod("setDomain", String.class).invoke(proxyConfig, proxydomain);


	//				stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.PROXY, proxyConfig);
					mthdSetProperty.invoke(serviceClientOptions, "PROXY", proxyConfig);
	//				stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_PROTOCOL_VERSION, org.apache.axis2.transport.http.HTTPConstants.HEADER_PROTOCOL_10);
					mthdSetProperty.invoke(serviceClientOptions, "__HTTP_PROTOCOL_VERSION__", "HTTP/1.0");
				}
			}

			// AUTHENTIFICATION
//			HttpTransportProperties.Authenticator auth = null;
			Class<?> clzzAuthenticator = stub.getClass().getClassLoader().loadClass("org.apache.axis2.transport.http.HttpTransportProperties$Authenticator");
			Object auth = null;
			if (!"NONE".equals(authentificationType)) {
				auth = clzzAuthenticator.newInstance();
//				auth.setPreemptiveAuthentication(true);
				clzzAuthenticator.getMethod("setPreemptiveAuthentication", boolean.class).invoke(auth, true);

				if (password != null) {
//					auth.setPassword(password);
					clzzAuthenticator.getMethod("setPassword", String.class).invoke(auth, password);
				}
				if (username != null) {
//					auth.setUsername(username);
					clzzAuthenticator.getMethod("setUsername", String.class).invoke(auth, username);
				}
	//			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
				mthdSetProperty.invoke(serviceClientOptions, "_NTLM_DIGEST_BASIC_AUTHENTICATION_", auth);
			}

			if ("BASIC".equals(authentificationType)) {
				ArrayList<String> authSchema = new ArrayList<String>();
//				authSchema.add(HttpTransportProperties.Authenticator.BASIC);
				authSchema.add("Basic");
//				auth.setAuthSchemes(authSchema);
				clzzAuthenticator.getMethod("setAuthSchemes", List.class).invoke(auth, authSchema);
			} else if ("NTLM".equals(authentificationType)) {
				ArrayList<String> authSchema = new ArrayList<String>();
//				authSchema.add(HttpTransportProperties.Authenticator.NTLM);
				authSchema.add("NTLM");
//				auth.setAuthSchemes(authSchema);
				clzzAuthenticator.getMethod("setAuthSchemes", List.class).invoke(auth, authSchema);
			} else if ("DIGEST".equals(authentificationType)) {
				ArrayList<String> authSchema = new ArrayList<String>();
//				authSchema.add(HttpTransportProperties.Authenticator.DIGEST);
				authSchema.add("Digest");
//				auth.setAuthSchemes(authSchema);
				clzzAuthenticator.getMethod("setAuthSchemes", List.class).invoke(auth, authSchema);
			}

			// ENCODING
			if ("UTF-8".equals(encoding))
	//			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-8");
				mthdSetProperty.invoke(serviceClientOptions, "CHARACTER_SET_ENCODING", "UTF-8");
			else if ("UTF-16".equals(encoding))
	//			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-16");
				mthdSetProperty.invoke(serviceClientOptions, "CHARACTER_SET_ENCODING", "UTF-16");
			else if ("ISO-8859".equals(encoding))
	//			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING, "ISO-8859");
				mthdSetProperty.invoke(serviceClientOptions, "CHARACTER_SET_ENCODING", "ISO-8859");
			else if ("ISO-8859-1".equals(encoding))
	//			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING, "ISO-8859-1");
				mthdSetProperty.invoke(serviceClientOptions, "CHARACTER_SET_ENCODING", "ISO-8859-1");

        }
        catch(Exception e) {
	        throw new NuclosFatalException(e);
        }
	}

	/**
	 * This Method is needed for creating a temp File for Sending over Webservice
	 */
	@Override
	public File createTempFile(String fileName, byte[] data) throws NuclosBusinessRuleException{
		String codegeneratorPath = NuclosSystemParameters.getString(NuclosSystemParameters.GENERATOR_OUTPUT_PATH);
		File tempFile = new File(codegeneratorPath  + "/temp");
		if (!tempFile.exists())
			tempFile.mkdir();
		tempFile = new File(tempFile.getAbsolutePath() + "/" + System.currentTimeMillis() + "_" +fileName);
		try {
			IOUtils.writeToBinaryFile(tempFile, data);
		} catch(IOException e) {
			throw new NuclosBusinessRuleException(e.getMessage());
		}

		return tempFile;
	}

	@Override
	public void logInfo(Integer iSessionId, String sMessage, String sRuleName) throws NuclosBusinessRuleException{
		this.writeToJobRunMessages(iSessionId, "INFO", sMessage,sRuleName);
	}

	@Override
	public void logWarning(Integer iSessionId, String sMessage, String sRuleName) throws NuclosBusinessRuleException{
		this.writeToJobRunMessages(iSessionId, "WARNING", sMessage, sRuleName);
	}

	@Override
	public void logError(Integer iSessionId, String sMessage, String sRuleName) throws NuclosBusinessRuleException{
		this.writeToJobRunMessages(iSessionId, "ERROR", sMessage, sRuleName);
	}

	private void writeToJobRunMessages(Integer iSessionId, String sLevel, String sMessage, String sRuleName) throws NuclosBusinessRuleException{
		try {
			getFacade(JobControlFacadeLocal.class).writeToJobRunMessages(iSessionId, sLevel, sMessage, sRuleName);
		}
		catch (Exception ex) {
			throw new NuclosBusinessRuleException(ex.getMessage(), ex);
		}
	}
}
