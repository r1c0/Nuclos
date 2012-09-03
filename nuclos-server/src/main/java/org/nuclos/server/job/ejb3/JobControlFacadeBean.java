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
package org.nuclos.server.job.ejb3;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.job.IntervalUnit;
import org.nuclos.common.job.JobUtils;
import org.nuclos.common.job.LogLevel;
import org.nuclos.common.mail.NuclosMail;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.ValueValidationHelper;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.mail.NuclosMailSender;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeBean;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.ejb3.SchedulerControlFacadeLocal;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.quartz.CronExpression;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for nucleus quartz job controller.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional(noRollbackFor= {Exception.class})
public class JobControlFacadeBean extends MasterDataFacadeBean implements JobControlFacadeRemote {

	private static final Logger LOG = Logger.getLogger(JobControlFacadeBean.class);

	private SchedulerControlFacadeLocal scheduler;

	private SpringLocaleDelegate localeDelegate;

	public JobControlFacadeBean() {
	}

	protected SpringLocaleDelegate getLocaleDelegate() {
		return localeDelegate;
	}

	@Autowired
	protected void setLocaleDelegate(SpringLocaleDelegate localeDelegate) {
		this.localeDelegate = localeDelegate;
	}

	private SchedulerControlFacadeLocal getScheduler() {
		if (scheduler == null) {
			scheduler = ServerServiceLocator.getInstance().getFacade(SchedulerControlFacadeLocal.class);
		}
		return scheduler;
	}

	@RolesAllowed("Login")
	public MasterDataVO create(JobVO job) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.JOBCONTROLLER);

		// try to generate cron expression and validate
		validate(job);

		MasterDataVO mdVO = job.toMasterDataVO();
		mdVO.setField("laststate", "Deaktiviert");
		mdVO.setField("running", false);
		mdVO.setField("lastfiretime", null);
		mdVO.setField("result", null);
		mdVO.setField("resultdetails", null);
		mdVO.setField("nextfiretime", null);

		MasterDataFacadeLocal mdFacade = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		MasterDataVO result = mdFacade.create(NuclosEntity.JOBCONTROLLER.getEntityName(), mdVO, job.getDependants(), null);
		getScheduler().addJob(new JobVO(result));
		return result;
	}

	@RolesAllowed("Login")
	public Object modify(JobVO job) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.JOBCONTROLLER);

		// try to generate cron expression and validate
		validate(job);

		MasterDataFacadeLocal mdFacade = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);

		boolean isScheduled;

		// check if name has changed. If it has changed, remove the quartz job with the old name.
		MasterDataVO jobFromDb = mdFacade.get(NuclosEntity.JOBCONTROLLER.getEntityName(), job.getId());
		isScheduled = getScheduler().isScheduled(jobFromDb.getField("name", String.class));
		if (!job.getName().equals(jobFromDb.getField("name"))) {
			getScheduler().deleteJob(jobFromDb.getField("name", String.class));
		}

		// update job
		Object result = mdFacade.modify(NuclosEntity.JOBCONTROLLER.getEntityName(), job.toMasterDataVO(), job.getDependants(), null);

		if (isScheduled) {
			Trigger trigger = getScheduler().scheduleJob(job);

			Date nextFireTime = trigger.getFireTimeAfter(Calendar.getInstance().getTime());
			if (nextFireTime != null) {
				MasterDataVO mdvo = get(NuclosEntity.JOBCONTROLLER.getEntityName(), job.getId());
				mdvo.setField("laststate", "Aktiviert");
				mdvo.setField("nextfiretime", DateUtils.getDateAndTime(nextFireTime));

				mdFacade.modify(NuclosEntity.JOBCONTROLLER.getEntityName(), mdvo, null, null);
			}
		}
		return result;
	}

	@RolesAllowed("Login")
    public void remove(JobVO job) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.JOBCONTROLLER);

		for (String name : getScheduler().getJobNames()) {
			if (name.equals(job.getName())) {
				// job is scheduled with quartz, try to delete
				getScheduler().deleteJob(name);
			}
		}

		MasterDataFacadeLocal mdFacade = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		mdFacade.remove(NuclosEntity.JOBCONTROLLER.getEntityName(), job.toMasterDataVO(), true, null);
    }

	/**
	 * Validates a <code>JobVO</code>.
	 * If the job is defined by interval, the corresponding cron expression is generated and validated.
	 *
	 * @param job The <code>JobVO</code> to validate.
	 * @throws CommonValidationException
	 */
	private void validate(JobVO job) throws CommonValidationException {
		if (job.getUseCronExpression() == null || !job.getUseCronExpression()) {
			Date startdate = job.getStartdate();
			String starttime = job.getStarttime();
			Integer interval = job.getInterval();
			String unit = job.getUnit();

			if (!ValueValidationHelper.validateInputFormat(starttime, MetaDataServerProvider.getInstance().getEntityField(NuclosEntity.JOBCONTROLLER.getEntityName(), "starttime").getFormatInput())) {
				throw new CommonValidationException("job.validation.starttime");
			}

			if (startdate == null) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("masterdata.error.validation.value",
					MetaDataServerProvider.getInstance().getEntity(NuclosEntity.JOBCONTROLLER.getEntityName()).getLocaleResourceIdForLabel(),
					MetaDataServerProvider.getInstance().getEntityField(NuclosEntity.JOBCONTROLLER.getEntityName(), "startdate").getLocaleResourceIdForLabel()));
			}

			if (interval == null) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("masterdata.error.validation.value",
					MetaDataServerProvider.getInstance().getEntity(NuclosEntity.JOBCONTROLLER.getEntityName()).getLocaleResourceIdForLabel(),
					MetaDataServerProvider.getInstance().getEntityField(NuclosEntity.JOBCONTROLLER.getEntityName(), "interval").getLocaleResourceIdForLabel()));
			}

			if (unit == null) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("masterdata.error.validation.value",
					MetaDataServerProvider.getInstance().getEntity(NuclosEntity.JOBCONTROLLER.getEntityName()).getLocaleResourceIdForLabel(),
					MetaDataServerProvider.getInstance().getEntityField(NuclosEntity.JOBCONTROLLER.getEntityName(), "unit").getLocaleResourceIdForLabel()));
			}

			IntervalUnit intervalUnit = org.nuclos.common2.KeyEnum.Utils.findEnum(IntervalUnit.class, unit);
			Calendar c = Calendar.getInstance(ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class).getUserLocale().toLocale());
			c.setTime(startdate);
			int iHour = Integer.parseInt(starttime.split(":")[0]);
			int iMinute = Integer.parseInt(starttime.split(":")[1]);
			c.set(Calendar.HOUR_OF_DAY, iHour);
			c.set(Calendar.MINUTE, iMinute);

			job.setCronExpression(JobUtils.getCronExpressionFromInterval(intervalUnit, interval, c));
		}
		if (!CronExpression.isValidExpression(job.getCronExpression())) {
			throw new CommonValidationException("scheduler.error.cronexpression");
		}
	}

	/**
	 * prepare job execution: clean protocol table, set 'running' true, create jobrun data record
	 *
	 * Note to transaction management:
	 * Execute this method in a separate transaction (TransactionAttributeType.REQUIRES_NEW)
	 * to avoid transaction isolation locks that last for the complete job execution.
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW, noRollbackFor= {Exception.class})
    public Pair<JobVO, MasterDataVO> prepare(Object oId) {
		try {
			//get the JobVO
			JobVO jobVO = makeJobVO(oId);

			//clean protocol table
			deleteOldLogInformation(jobVO);

			//job is running - set 'running' field true and modify mdvo
			jobVO.setRunning(true);
			makeMasterDataVO(jobVO);

			//create jobrun data record (starttime)
			MasterDataVO jobRun = createNewJobRun((Integer)oId);

			Pair<JobVO, MasterDataVO> pair = new Pair<JobVO, MasterDataVO>(jobVO, jobRun);

			return pair;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
    }

	/**
	 * delete Job entries older than delete value in days, 0 - delete all, null - delete nothing
	 * @param jobVO the job which log entries has to be deleted
	 * @throws CommonPermissionException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	private void deleteOldLogInformation(JobVO jobVO) throws CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonPermissionException, NuclosBusinessRuleException, CommonCreateException {
		if((jobVO.getDeleteInDays() != null)) {
			final long deleteDate = System.currentTimeMillis() - ((((((((long)jobVO.getDeleteInDays().intValue() * 24)) * 60)) * 60)) * 1000);

			for(MasterDataVO mdvo: getDependantMasterData(NuclosEntity.JOBRUN.getEntityName(), "parent", jobVO.getId())) {
				long createdAt = mdvo.getCreatedAt().getTime();
				if (createdAt < deleteDate) {
					removeJobRunMessages(mdvo);
					remove(NuclosEntity.JOBRUN.getEntityName(), mdvo, false, null);
				}
			}
		}
	}

	private void removeJobRunMessages(MasterDataVO mdvo) throws NuclosBusinessRuleException, CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonPermissionException {
		final CollectableComparison comp = SearchConditionUtils.newEOComparison(NuclosEntity.JOBRUNMESSAGES.getEntityName(), "parent", ComparisonOperator.EQUAL, mdvo.getIntId(), MetaDataServerProvider.getInstance());
		for(MasterDataVO vo : getMasterData(NuclosEntity.JOBRUNMESSAGES.getEntityName(), comp, true)) {
			remove(NuclosEntity.JOBRUNMESSAGES.getEntityName(), vo, false, null);
		}
	}

	/**
	 * @param iParentId
	 * @throws NuclosBusinessRuleException
	 */
	private MasterDataVO createNewJobRun(Integer iParentId) throws CommonCreateException, CommonPermissionException, NuclosBusinessRuleException{
		Map<String, Object> mpFields = new HashMap<String, Object>();

		mpFields.put("startdate", DateUtils.getActualDateAndTime());
		mpFields.put("parentId", iParentId);

		MasterDataVO mdvo = create(NuclosEntity.JOBRUN.getEntityName(), new MasterDataVO(null, new Date(), getCurrentUserName(), new Date(),
				getCurrentUserName(), 1, mpFields), null, null);

		return mdvo;
	}

	/**
	 * @param mdvo
	 * @throws CommonPermissionException
	 * @throws CommonValidationException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	private Object modifyJobRun(MasterDataVO mdvo) throws CommonCreateException, CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonValidationException, CommonPermissionException, NuclosBusinessRuleException {
		return modify(NuclosEntity.JOBRUN.getEntityName(), mdvo, null, null);
	}

	/**
	 * @param jobVO
	 * @throws CommonBusinessException
	 */
	@RolesAllowed("Login")
	public void scheduleJob(Object oId) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.JOBCONTROLLER);

		MasterDataVO mdVO = get(NuclosEntity.JOBCONTROLLER.getEntityName(), oId);

		final Trigger jobTrigger = getScheduler().scheduleJob(new JobVO(mdVO));
		Date nextFireTime = jobTrigger.getFireTimeAfter(Calendar.getInstance().getTime());
		if (nextFireTime != null) {
			LOG.info("Scheduled Job " + oId + " for " + nextFireTime);
			MasterDataVO mdvo = get(NuclosEntity.JOBCONTROLLER.getEntityName(), mdVO.getId());
			mdvo.setField("laststate", "Aktiviert");
			mdvo.setField("nextfiretime", DateUtils.getDateAndTime(nextFireTime));

			modify(NuclosEntity.JOBCONTROLLER.getEntityName(), mdvo, null, null);
		}
		else {
			LOG.error("Scheduling Job " + oId + " failed");
		}
	}


	/**
	 * @param jobVO
	 * @throws CommonBusinessException
	 */
	@RolesAllowed("Login")
	public void unscheduleJob(Object oId) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.JOBCONTROLLER);

		MasterDataVO mdVO = get(NuclosEntity.JOBCONTROLLER.getEntityName(), oId);

		getScheduler().unscheduleJob(new JobVO(mdVO));
		mdVO.setField("laststate", "Deaktiviert");
		mdVO.setField("running", false);
		mdVO.setField("nextfiretime", null);
		modify(NuclosEntity.JOBCONTROLLER.getEntityName(), mdVO, null, null);
	}

	/**
	 * @param oId - id of job to execute
	 */
	@RolesAllowed("Login")
	@Transactional(propagation=Propagation.NOT_SUPPORTED, noRollbackFor= {Exception.class})
	public void startJobImmediately(Object oId) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.JOBCONTROLLER);
		try {
			JobVO job = new JobVO(get(NuclosEntity.JOBCONTROLLER.getEntityName(), oId));
			getScheduler().triggerJob(job);
		} catch (CommonFinderException e) {
			throw new NuclosFatalException(e);
		}
	}

	/**
	 * @param sLevel
	 * @param sMessage
	 * @throws CommonPermissionException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW, noRollbackFor= {Exception.class})
	public void writeToJobRunMessages(Integer iSessionId, String sLevel, String sMessage, String sRuleName) {
		try {
			Map<String, Object> mpFields = new HashMap<String, Object>();

			mpFields.put("parentId", iSessionId);
			mpFields.put("level", sLevel);
			mpFields.put("message", sMessage);
			mpFields.put("rule", sRuleName);

			MasterDataVO mdvo = new MasterDataVO(null, new Date(), getCurrentUserName(), new Date(),
					getCurrentUserName(), 1, mpFields);
			create(NuclosEntity.JOBRUNMESSAGES.getEntityName(), mdvo, null, null);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void sendMessage(String sRecipient, String sSubject, String sMessage) {
		String sEmail = null;
		if (sRecipient != null) {
			DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<String> query = builder.createQuery(String.class);
			DbFrom t = query.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
			query.select(t.baseColumn("STREMAIL", String.class));
			query.where(builder.equal(builder.upper(t.baseColumn("STRUSER", String.class)), builder.upper(builder.literal(sRecipient))));

			sEmail = CollectionUtils.getFirst(dataBaseHelper.getDbAccess().executeQuery(query));
		}

		if (sRecipient != null && sEmail != null) {
			NuclosMail mail = new NuclosMail(sEmail, sSubject, sMessage);
			try {
				NuclosMailSender.sendMail(mail);
			}
			catch (CommonBusinessException ex) {
				throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("job.exception.mail", ex.getMessage()), ex);
			}
		}
		else {
			info("Es konnte keine E-Mail versendet werden, da entweder kein Benutzer oder keine E-Mail angegeben wurde.");
		}

	}

	/**
	 * transforms a JobVO to a MasterDataVO
	 * @param jobvo
	 * @return MasterDataVO
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 * @throws CommonValidationException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	private void makeMasterDataVO(JobVO jobvo) throws CommonFinderException, CommonPermissionException,
				CommonCreateException, CommonRemoveException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException {
		this.modify(NuclosEntity.JOBCONTROLLER.getEntityName(), jobvo.toMasterDataVO(), null, null);
	}

	/**
	 * @param jobVO
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 */
	private JobVO makeJobVO(Object oId) throws CommonFinderException, CommonPermissionException {
		return new JobVO(get(NuclosEntity.JOBCONTROLLER.getEntityName(), oId));
	}

	/**
	 * get job procedures/functions
	 * @param sType
	 * @return
	 */
	@RolesAllowed("Login")
	public Collection<String> getDBObjects()  throws CommonPermissionException {
		checkReadAllowed(NuclosEntity.JOBCONTROLLER);

		return CollectionUtils.applyFilter(dataBaseHelper.getDbAccess().getCallableNames(), new Predicate<String>() {
			@Override public boolean evaluate(String name) {
				return StringUtils.toUpperCase(name).startsWith("JOB_");
			}
		});
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, noRollbackFor= {Exception.class})
    public void setJobExecutionResult(Object oResult, Date dFireTime, Date dNextFireTime, JobVO jobVO, MasterDataVO jobRun) {
	    try {
	    	if (jobRun != null) {
	    		jobVO = makeJobVO(jobVO.getId());

				int iInfoCount = 0;
				int iWarningCount = 0;
				int iErrorCount = 0;
				for(MasterDataVO mdvo : getDependantMasterData(NuclosEntity.JOBRUNMESSAGES.getEntityName(), "parent", jobRun.getId())) {
					if (mdvo.getField("level").equals("INFO")) {
						iInfoCount++;
					}
					else if (mdvo.getField("level").equals("WARNING")) {
						iWarningCount++;
					}
					else if (mdvo.getField("level").equals("ERROR")) {
						iErrorCount++;
					}
				}
				String sState = "INFO: " + iInfoCount + " entries\nWARNING: "+ iWarningCount + " entries\nERROR: " + iErrorCount + " entries";
				jobRun.setField("enddate", DateUtils.getActualDateAndTime());
				jobRun.setField("state", oResult == null ? sState : (String)oResult);
				modifyJobRun(jobRun);


				jobVO.setLastFireTime(DateUtils.getDateAndTime(dFireTime));
				if (dNextFireTime != null) {
					jobVO.setNextFireTime(DateUtils.getDateAndTime(dNextFireTime));
				}

				jobVO.setRunning(false);
				jobVO.setResult(getResult(iWarningCount, iErrorCount));
				jobVO.setResultDetails(oResult == null ? sState : (String)oResult);
				makeMasterDataVO(jobVO);

				sendMessage(getResult(iWarningCount, iErrorCount), jobVO, sState);
			}
	    }
	    catch (Exception ex) {
	    	throw new RuntimeException(ex.getMessage(), ex);
	    }
    }

	@Transactional(propagation=Propagation.REQUIRES_NEW, noRollbackFor= {Exception.class})
    public void setJobExecutionResultError(Object oId, Date dFireTime, Date sNextFireTime, Integer iSessionId, Exception e) {
	    try {
	    	JobVO jobVO = makeJobVO(oId);
			jobVO.setRunning(false);
			jobVO.setResult("ERROR");
			jobVO.setLastFireTime(DateUtils.getDateAndTime(dFireTime));
			if (sNextFireTime != null) {
				jobVO.setNextFireTime(DateUtils.getDateAndTime(sNextFireTime));
			}
			StringBuilder sb = new StringBuilder(e.getClass().getName() + ": ");
			sb.append(getLocaleDelegate().getMessageFromResource(e.getMessage()));
			jobVO.setResultDetails(sb.toString());
			makeMasterDataVO(jobVO);

			if (iSessionId != null) {
				MasterDataVO jobRun = get(NuclosEntity.JOBRUN.getEntityName(), iSessionId);
				jobRun.setField("enddate", DateUtils.getActualDateAndTime());
				jobRun.setField("state", sb.toString());
				modifyJobRun(jobRun);
			}
			try {
				sendMessage("ERROR", jobVO, sb.toString());
			} catch (Exception ex) {
				error(ex);
				writeToJobRunMessages(iSessionId, "ERROR", "Error while trying to send email: " + ex.getMessage(), null);
			}
	    } catch (Exception ex) {
	    	throw new RuntimeException(ex.getMessage(), ex);
	    }
    }

	private void sendMessage(String sResult, JobVO jobVO, String sMessage) {
		if (jobVO.getLevel() != null) {
			LogLevel level = KeyEnum.Utils.findEnum(LogLevel.class, jobVO.getLevel());
			if (sResult.equals("INFO") && LogLevel.INFO.equals(level) ||
					sResult.equals("WARNING") && LogLevel.WARNING.equals(level) ||
						sResult.equals("ERROR") && LogLevel.ERROR.equals(level)) {
				sendMessage(jobVO.getUser(), "Results of job execution " + jobVO.getName(), sMessage);
			}
		}
	}

	private String getResult(Integer iWarning, Integer iError) {
		if (iError > 0) {
			return "ERROR";
		}
		else if (iWarning >0) {
			return "WARNING";
		}
		else {
			return "INFO";
		}
	}
}
