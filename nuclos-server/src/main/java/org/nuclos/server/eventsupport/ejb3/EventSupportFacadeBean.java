package org.nuclos.server.eventsupport.ejb3;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.api.annotation.NuclosEventType;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.EventSupportCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.customcode.CustomCodeManager;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportGenerationVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportJobVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.genericobject.ejb3.GeneratorFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.statemodel.ejb3.StateFacadeLocal;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for event support management.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional(noRollbackFor= {Exception.class})
public class EventSupportFacadeBean extends NuclosFacadeBean implements EventSupportFacadeRemote {

	private static final Logger LOG = Logger.getLogger(EventSupportFacadeBean.class);
	
	private CustomCodeManager ccm;
	
	private MasterDataFacadeLocal masterDataFacade;
	
	public EventSupportFacadeBean() {}
	
	public void setCustomCodeManager(CustomCodeManager ccm) {
		this.ccm = ccm;
	}
	
	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}
	
	private final MasterDataFacadeLocal getMasterDataFacade() {
		return masterDataFacade;
	}
	
	public RuleObjectContainerCVO fireEventSupports(Integer iSourceStateId, Integer iTargetStateId,
			RuleObjectContainerCVO loccvoBefore, boolean b) throws NuclosBusinessRuleException 
	{
		StateFacadeLocal facade = ServerServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
		StateTransitionVO stVO = (iSourceStateId == null) ?
			facade.findStateTransitionByNullAndTargetState(iTargetStateId) :
				facade.findStateTransitionBySourceAndTargetState(iSourceStateId, iTargetStateId);

			return stVO != null ? fireEventSupports(stVO.getId(), loccvoBefore, b) : loccvoBefore;
	}
	
	public Collection<EventSupportTransitionVO> getEventSupportsByTransitionId(Integer transId) throws CommonFinderException, CommonPermissionException {
		
		final List<EventSupportTransitionVO> evtSupps = new ArrayList<EventSupportTransitionVO>();
		
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_EVENTSUPPORT_TRANSITION").alias(SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("INTORDER", Integer.class),
						  t.baseColumn("STREVENTSUPPORTCLASS", String.class),
					      t.baseColumn("BLNRUNAFTERWARDS", Boolean.class),
					      t.baseColumn("DATCREATED", java.sql.Date.class),
						  t.baseColumn("STRCREATED", String.class),
						  t.baseColumn("DATCHANGED", java.sql.Date.class),
						  t.baseColumn("STRCHANGED", String.class),
						  t.baseColumn("INTVERSION", Integer.class),
						  t.baseColumn("INTID", Integer.class));
		query.where(builder.equal(t.baseColumn("INTID_T_MD_STATE_TRANSITION", Integer.class), transId));
		query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));

		for (DbTuple res : dataBaseHelper.getDbAccess().executeQuery(query)) {
			
			evtSupps.add(new EventSupportTransitionVO(
					new NuclosValueObject(
							res.get(8, Integer.class),
							new Date(res.get(3, java.sql.Date.class).getTime()),
							res.get(4, String.class),
							new Date(res.get(5, java.sql.Date.class).getTime()),
							res.get(6, String.class),
							res.get(7, Integer.class)),
							res.get(1, String.class),
					transId, 
					res.get(0, Integer.class), 
					res.get(2, Boolean.class)));
		}
		
		return evtSupps;
	}
	
	/**
	 * fires a transition event by finding all eventsupports that correspond
	 * to the given transition id and by executing these rules.
	 * @param iTransitionId transition id to fire eveCustomCodeRules()nt for
	 * @param loccvoCurrent current leased object as parameter for rules
	 * @return the possibly change current object.
	 */
	private RuleObjectContainerCVO fireEventSupports(Integer transitionId, RuleObjectContainerCVO ruleContainer, Boolean after) throws NuclosBusinessRuleException {
		final List<EventSupportSourceVO> evtSupps = new ArrayList<EventSupportSourceVO>();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_EVENTSUPPORT_TRANSITION").alias(SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("STREVENTSUPPORTCLASS", String.class), t.baseColumn("BLNRUNAFTERWARDS", Boolean.class));
		query.where(builder.equal(t.baseColumn("INTID_T_MD_STATE_TRANSITION", Integer.class), transitionId));
		query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));

		for (DbTuple res : dataBaseHelper.getDbAccess().executeQuery(query)) {
			Boolean bRuleRunAfterwards = res.get(1, Boolean.class);
			if (bRuleRunAfterwards == null) bRuleRunAfterwards = Boolean.FALSE;
			if ((bRuleRunAfterwards && after) || (!bRuleRunAfterwards && !after)) {
				evtSupps.add(EventSupportCache.getInstance().getEventSupport(res.get(0, String.class)));
			}
		}

		// We can now execute the rules in their order:
		LOG.info("BEGIN    executing business rules for transition id " + transitionId + "...");
		final RuleObjectContainerCVO result = this.executeEventSupport(evtSupps, ruleContainer, false);
		LOG.info("FINISHED executing business rules for transition id " + transitionId + "...");
		return result;
	}

	private RuleObjectContainerCVO executeEventSupport(
			List<EventSupportSourceVO> evtSupps,
			RuleObjectContainerCVO ruleContainer, boolean b) 
	{
		RuleObjectContainerCVO retVal = ruleContainer;
		
		for (EventSupportSourceVO esVO : evtSupps)
		{
//			try {
////				StateChangeEvent cast = (StateChangeEvent) this.ccm.getInstance(esVO.getClassname());
////				Integer i = cast.changeState(Integer.valueOf(1));
////				i.intValue();
//			} catch (NuclosCompileException e) {
//				LOG.error(e.getMessage(), e);
//			}			
		}
		
		return retVal;
	}

	public Collection<EventSupportSourceVO> getAllEventSupports() throws CommonPermissionException {
		return this.ccm.getExecutableEventSupportFiles();
	}
	
	public List<EventSupportJobVO> getAllEventSupportsForJob(Integer jobId) throws CommonPermissionException {
		
		List<EventSupportJobVO> result = new ArrayList<EventSupportJobVO>();
		
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("V_MD_EVENTSUPPORT_JOB").alias(SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("INTID", Integer.class),
				t.baseColumn("INTORDER", Integer.class),
				t.baseColumn("STREVENTSUPPORTCLASS", String.class),
				t.baseColumn("INTID_T_MD_JOBCONTROLLER", Integer.class),
				t.baseColumn("STRDESCRIPTION", String.class),
				t.baseColumn("DATCREATED", java.sql.Date.class),
				t.baseColumn("STRCREATED", String.class),
				t.baseColumn("DATCHANGED", java.sql.Date.class),
				t.baseColumn("STRCHANGED", String.class),
				t.baseColumn("INTVERSION", Integer.class));
		query.where(builder.equal(t.baseColumn("INTID_T_MD_JOBCONTROLLER", Integer.class), jobId));
		query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));
		
		for (DbTuple res : dataBaseHelper.getDbAccess().executeQuery(query)) {
			Integer id = res.get(0, Integer.class);
			Integer order = res.get(1, Integer.class);
			String esClass = res.get(2, String.class);
			Integer jobcontroller = res.get(3, Integer.class);
			String description= res.get(4, String.class);
			Date dCreated = new Date(res.get(5, java.sql.Date.class).getTime());
			String sCreated = res.get(6, String.class);
			Date dChanged = new Date(res.get(7, java.sql.Date.class).getTime());
			String sChanged = res.get(8, String.class);
			Integer version = res.get(9, Integer.class);
			
			NuclosValueObject nvo = new NuclosValueObject(id, dCreated, sCreated, dChanged, sChanged, version);
			result.add(new EventSupportJobVO(nvo, description,esClass,order,jobcontroller));	
	    }
		return result;
	
	}
	    
	private EventSupportSourceVO getEventSupportByClassname(String classname) {
		
		EventSupportSourceVO retVal = null;
		
		for (EventSupportSourceVO esVO : this.ccm.getExecutableEventSupportFiles()) {
			if (esVO.getClassname().equals(classname)) {
				retVal = esVO;
				break;
			}
		}
		
		return retVal;
	}
	
	public List<EventSupportEventVO> getEventSupportEventsByClassname(String classname) {
		List<EventSupportEventVO> retVal = new ArrayList<EventSupportEventVO>();
		
		List<EntityObjectVO> dependantMasterData = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.EVENTSUPPORTEVENT).getBySearchExpression(
				new CollectableSearchExpression(SearchConditionUtils.newEOComparison(NuclosEntity.EVENTSUPPORTEVENT.getEntityName(), "eventsupportclass", ComparisonOperator.EQUAL, classname, MetaDataServerProvider.getInstance())));
	
		for (EntityObjectVO mdVO : dependantMasterData) {
			retVal.add(MasterDataWrapper.getEventSupportEventVO(mdVO));
		}
		
		return retVal;		
	}

	public List<JobVO> getJobsByClassname(String classname) {
		List<JobVO> retVal = new ArrayList<JobVO>();
		
		try {
			List<EntityObjectVO> transMDs = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.EVENTSUPPORTJOB).getBySearchExpression(
					new CollectableSearchExpression(SearchConditionUtils.newEOComparison(NuclosEntity.EVENTSUPPORTJOB.getEntityName(), "eventsupportclass", ComparisonOperator.EQUAL, classname, MetaDataServerProvider.getInstance())));
			
			for (EntityObjectVO eoVO : transMDs) {
				EventSupportJobVO eventSupportJobVO = MasterDataWrapper.getEventSupportJobVO(eoVO);
				MasterDataVO masterDataVO = masterDataFacade.get(NuclosEntity.JOBCONTROLLER.getEntityName(), eventSupportJobVO.getJobControllerId());
				retVal.add(new JobVO(masterDataVO));
			}
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
		return retVal;		
	}
	public List<GeneratorActionVO> getGenerationsByClassname(String classname) {
		List<GeneratorActionVO> retVal = new ArrayList<GeneratorActionVO>();
		
		List<EntityObjectVO> lstESGenerations = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.EVENTSUPPORTGENERATION).getBySearchExpression(
				new CollectableSearchExpression(SearchConditionUtils.newEOComparison(NuclosEntity.EVENTSUPPORTGENERATION.getEntityName(), "eventsupportclass", ComparisonOperator.EQUAL, classname, MetaDataServerProvider.getInstance())));

		try {
			for (EntityObjectVO eoVO : lstESGenerations) {
				EventSupportGenerationVO esgVO = MasterDataWrapper.getEventSupportGenerationVO(eoVO);
				MasterDataVO allGenerations = masterDataFacade.get(NuclosEntity.GENERATION.getEntityName(), esgVO.getGeneration());
				GeneratorActionVO gaVO = MasterDataWrapper.getGeneratorActionVO(allGenerations, 
						ServerServiceLocator.getInstance().getFacade(GeneratorFacadeLocal.class).getGeneratorUsages(esgVO.getGeneration()));
				retVal.add(gaVO);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}
	
	public List<StateModelVO> getStateModelsByEventSupportClassname(String classname) {
		List<StateModelVO> retVal = new ArrayList<StateModelVO>();
		
		try {
			List<EntityObjectVO> dependantMasterData = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.EVENTSUPPORTTRANSITION).getBySearchExpression(
					new CollectableSearchExpression(SearchConditionUtils.newEOComparison(NuclosEntity.EVENTSUPPORTTRANSITION.getEntityName(), "eventsupportclass", ComparisonOperator.EQUAL, classname, MetaDataServerProvider.getInstance())));
			
			for (EntityObjectVO mdVO : dependantMasterData) {
				EventSupportTransitionVO estVO = MasterDataWrapper.getEventSupportTransitionVO(mdVO);
				MasterDataVO masterDataVO = masterDataFacade.get(NuclosEntity.STATETRANSITION.getEntityName(), estVO.getTransitionId());
				StateTransitionVO stVO = MasterDataWrapper.getStateTransitionVOWithoutDependants(masterDataVO);
				Integer iTargetStateId = stVO.getStateTarget();
				MasterDataVO masterDataStateVO = masterDataFacade.get(NuclosEntity.STATE.getEntityName(), iTargetStateId);
				StateVO stateVO = MasterDataWrapper.getStateVO(masterDataStateVO);
				MasterDataVO masterDataModelVO = masterDataFacade.get(NuclosEntity.STATEMODEL.getEntityName(), stateVO.getModelId());
				
				retVal.add(MasterDataWrapper.getStateModelVO(masterDataModelVO));
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;		
	}
	
	public Collection<EventSupportSourceVO> getEventSupportsByClasstype(List<Class<?>> listOfinterfaces)
			throws CommonPermissionException {
		
		// List of valdid and available EventSupportClasses that have been found in the classpath
		// and that implement the given interfaces
		return this.ccm.getExecutableEventSupportFilesByClassType(listOfinterfaces);
	}

	@Override
	public List<EventSupportSourceVO> getAllEventSupportTypes()
			throws CommonPermissionException {
		
		List<EventSupportSourceVO> retVal = new ArrayList<EventSupportSourceVO>();
		List<Class> registeredSupportEventTypes = this.ccm.getRegisteredSupportEventTypes();
		
		for (Class<?> c : registeredSupportEventTypes)
		{
			String sName = c.getSimpleName();
			String sBeschreibung = c.getSimpleName();
			
			Annotation[] annotations = c.getAnnotations();
			if (annotations.length > 0 && annotations[0].annotationType().equals(NuclosEventType.class))
			{ 
				sBeschreibung = c.getAnnotation(NuclosEventType.class).description();
				sName = c.getAnnotation(NuclosEventType.class).name();
			}
			String pkgName = c.getPackage() != null ? c.getPackage().getName() : null;
			retVal.add(new EventSupportSourceVO(sName, sBeschreibung, c.getName(), c.getSimpleName(), pkgName, null));
		}
		return retVal;
	}

	@Override
	public EventSupportEventVO createEventSupportEvent(EventSupportEventVO eseVOToInsert) throws CommonPermissionException, CommonValidationException, NuclosBusinessRuleException, CommonCreateException {
	
		this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTEVENT);
		
		eseVOToInsert.validate();
		
		MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.EVENTSUPPORTEVENT.getEntityName(), MasterDataWrapper.wrapEventSupportEventVO(eseVOToInsert), null);
		
		return MasterDataWrapper.getEventSupportEventVO(mdVO);
	}
	
	public EventSupportEventVO modifyEventSupportEvent(EventSupportEventVO eseVOToUpdate) {
		try {
			this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTEVENT);
			eseVOToUpdate.validate();
			getMasterDataFacade().modify(NuclosEntity.EVENTSUPPORTEVENT.getEntityName(), MasterDataWrapper.wrapEventSupportEventVO(eseVOToUpdate), null);
			eseVOToUpdate = MasterDataWrapper.getEventSupportEventVO(
					getMasterDataFacade().get(NuclosEntity.EVENTSUPPORTEVENT.getEntityName(), eseVOToUpdate.getId()));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} 		
		
		return eseVOToUpdate;
	}
	
	public void deleteEventSupportEvent(EventSupportEventVO eseVOToUpdate) {
		try {
			this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTEVENT);
			eseVOToUpdate.validate();			
			getMasterDataFacade().remove(NuclosEntity.EVENTSUPPORTEVENT.getEntityName(), MasterDataWrapper.wrapEventSupportEventVO(eseVOToUpdate), false);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} 	
	}
	
	public List<ProcessVO> getProcessesByModuleId(Integer moduleId) {
		List<ProcessVO> retVal = new ArrayList<ProcessVO>();
		
		Collection<MasterDataVO> dependantMasterData = masterDataFacade.getDependantMasterData(NuclosEntity.PROCESS.getEntityName(), "module", moduleId);
		for (MasterDataVO mdVO :dependantMasterData) {
			retVal.add(MasterDataWrapper.getProcessVO(mdVO));
		}
		
		return retVal;
	}
	

	@Override
	public EventSupportTransitionVO createEventSupportTransition(
			EventSupportTransitionVO eseVOToInsert)
			throws CommonPermissionException, CommonValidationException,
			NuclosBusinessRuleException, CommonCreateException {
		this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTTRANSITION);
		
		eseVOToInsert.validate();
		
		MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.EVENTSUPPORTTRANSITION.getEntityName(), MasterDataWrapper.wrapEventSupportTransitionVO(eseVOToInsert), null);
		
		return MasterDataWrapper.getEventSupportTransitionVO(mdVO);
	}

	@Override
	public EventSupportTransitionVO modifyEventSupportTransition(
			EventSupportTransitionVO eseVOToUpdate) {
		try {
			this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTTRANSITION);
			eseVOToUpdate.validate();			
			eseVOToUpdate = (EventSupportTransitionVO) getMasterDataFacade().modify(NuclosEntity.EVENTSUPPORTTRANSITION.getEntityName(), MasterDataWrapper.wrapEventSupportTransitionVO(eseVOToUpdate), null);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} 	
		return eseVOToUpdate;
	}

	@Override
	public void deleteEventSupportTransition(
			EventSupportTransitionVO eseVOToUpdate) {
		try {
			this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTTRANSITION);
			eseVOToUpdate.validate();			
			getMasterDataFacade().remove(NuclosEntity.EVENTSUPPORTTRANSITION.getEntityName(), MasterDataWrapper.wrapEventSupportTransitionVO(eseVOToUpdate), false);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} 	
	}

	@Override
	public void deleteEventSupportEvents(Integer entityId,	String eventSupportType) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonRemoveException, CommonStaleVersionException {
	
		Collection<MasterDataVO> mdVOList = getMasterDataFacade().getDependantMasterData(NuclosEntity.EVENTSUPPORTEVENT.getEntityName(), "entity", entityId);
		for (MasterDataVO mdvo : mdVOList) {
			if (eventSupportType.equals(mdvo.getField("eventsupporttype").toString()))
				getMasterDataFacade().remove(NuclosEntity.EVENTSUPPORTEVENT.getEntityName(), mdvo, false);
		}		
	}

public List<EventSupportEventVO> getAllEventSupportsForEntity(Integer entityId) throws CommonPermissionException {
		
		List<EventSupportEventVO> result = new ArrayList<EventSupportEventVO>();
		
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("V_MD_EVENTSUPPORT_EVENT").alias(SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("INTID", Integer.class),
				t.baseColumn("INTORDER", Integer.class),
				t.baseColumn("STREVENTSUPPORTTYPE", String.class),
				t.baseColumn("STREVENTSUPPORTCLASS", String.class),
				t.baseColumn("INTID_T_MD_ENTITY", Integer.class),
				t.baseColumn("INTID_T_MD_PROCESS", Integer.class),
				t.baseColumn("INTID_T_MD_STATE", Integer.class),
				t.baseColumn("STRVALUE_T_MD_ENTITY", String.class),
				t.baseColumn("STRVALUE_T_MD_PROCESS", String.class),
				t.baseColumn("STRVALUE_T_MD_STATE", String.class),
				t.baseColumn("DATCREATED", java.sql.Date.class),
				t.baseColumn("STRCREATED", String.class),
				t.baseColumn("DATCHANGED", java.sql.Date.class),
				t.baseColumn("STRCHANGED", String.class),
				t.baseColumn("INTVERSION", Integer.class));
		query.where(builder.equal(t.baseColumn("INTID_T_MD_ENTITY", Integer.class), entityId));
		query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));
		
		for (DbTuple res : dataBaseHelper.getDbAccess().executeQuery(query)) {
			Integer id = res.get(0, Integer.class);
			Integer order = res.get(1, Integer.class);
			String esType = res.get(2, String.class);
			String esClass = res.get(3, String.class);
			Integer process = res.get(5, Integer.class);
			Integer state = res.get(6, Integer.class);
			String strEntity = res.get(7, String.class);
			String strProcess = res.get(8, String.class);
			String strState= res.get(9, String.class);
			Date dCreated = new Date(res.get(10, java.sql.Date.class).getTime());
			String sCreated = res.get(11, String.class);
			Date dChanged = new Date(res.get(12, java.sql.Date.class).getTime());
			String sChanged = res.get(13, String.class);
			Integer version = res.get(14, Integer.class);
			
			EventSupportEventVO retVal = new EventSupportEventVO(
					new NuclosValueObject(id, dCreated,sCreated,dChanged,sChanged,version), 
					esClass,esType, entityId ,process,state,order,strEntity, strState,strProcess);  
	     
			result.add(retVal);	
	    }
		return result;
	}

@Override
public EventSupportJobVO createEventSupportJob(EventSupportJobVO esjVOToInsert)
		throws CommonPermissionException, CommonValidationException,
		NuclosBusinessRuleException, CommonCreateException {
	this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTJOB);
	
	esjVOToInsert.validate();
	
	MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.EVENTSUPPORTJOB.getEntityName(),
			MasterDataWrapper.wrapEventSupportJobVO(esjVOToInsert), null);
	
	return MasterDataWrapper.getEventSupportJobVO(mdVO);
}

@Override
public EventSupportJobVO modifyEventSupportJob(EventSupportJobVO esjVOToUpdate) {
	try {
		this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTJOB);
		esjVOToUpdate.validate();			
		esjVOToUpdate = (EventSupportJobVO) getMasterDataFacade().modify(NuclosEntity.EVENTSUPPORTJOB.getEntityName(), MasterDataWrapper.wrapEventSupportJobVO(esjVOToUpdate), null);
	} catch (Exception e) {
		LOG.error(e.getMessage(), e);
	} 	
	return esjVOToUpdate;
}

@Override
public void deleteEventSupportJob(EventSupportJobVO esjVOToUpdate) {
	try {
		this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTJOB);
		esjVOToUpdate.validate();			
		getMasterDataFacade().remove(NuclosEntity.EVENTSUPPORTJOB.getEntityName(), MasterDataWrapper.wrapEventSupportJobVO(esjVOToUpdate), false);
	} catch (Exception e) {
		LOG.error(e.getMessage(), e);
	} 	
}

@Override
public List<EventSupportGenerationVO> getEventSupportsByGenerationId(
		Integer genId) throws CommonPermissionException {
	List<EventSupportGenerationVO> result = new ArrayList<EventSupportGenerationVO>();
	
	DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
	DbQuery<DbTuple> query = builder.createTupleQuery();
	DbFrom t = query.from("V_MD_EVENTSUPPORT_GENERATION").alias(SystemFields.BASE_ALIAS);
	query.multiselect(t.baseColumn("INTID", Integer.class),
			t.baseColumn("BLNRUNAFTERWARDS", Boolean.class),
			t.baseColumn("INTORDER", Integer.class),
			t.baseColumn("STREVENTSUPPORTCLASS", String.class),
			t.baseColumn("INTID_T_MD_GENERATION", Integer.class),
			t.baseColumn("DATCREATED", java.sql.Date.class),
			t.baseColumn("STRCREATED", String.class),
			t.baseColumn("DATCHANGED", java.sql.Date.class),
			t.baseColumn("STRCHANGED", String.class),
			t.baseColumn("INTVERSION", Integer.class));
	query.where(builder.equal(t.baseColumn("INTID_T_MD_GENERATION", Integer.class), genId));
	query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));
	
	for (DbTuple res : dataBaseHelper.getDbAccess().executeQuery(query)) {
		Integer id = res.get(0, Integer.class);
		Boolean bRunAfterwards = res.get(1, Boolean.class);
		Integer order = res.get(2, Integer.class);
		String esClass = res.get(3, String.class);
		Integer iGeneration = res.get(4, Integer.class);
		Date dCreated = new Date(res.get(5, java.sql.Date.class).getTime());
		String sCreated = res.get(6, String.class);
		Date dChanged = new Date(res.get(7, java.sql.Date.class).getTime());
		String sChanged = res.get(8, String.class);
		Integer version = res.get(9, Integer.class);
		
		EventSupportGenerationVO retVal = new EventSupportGenerationVO(
				new NuclosValueObject(id, dCreated,sCreated,dChanged,sChanged,version), 
				order,iGeneration, esClass, bRunAfterwards);  
     
		result.add(retVal);	
    }
	return result;
}
	
	@Override
	public EventSupportGenerationVO createEventSupportGeneration(
			EventSupportGenerationVO esgVOToInsert)
			throws CommonPermissionException, CommonValidationException,
			NuclosBusinessRuleException, CommonCreateException {
		this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTGENERATION);
		
		esgVOToInsert.validate();
		
		MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.EVENTSUPPORTGENERATION.getEntityName(),
				MasterDataWrapper.wrapEventSupportGenerationVO(esgVOToInsert), null);
		
		return MasterDataWrapper.getEventSupportGenerationVO(mdVO);
	}
	
	@Override
	public EventSupportGenerationVO modifyEventSupportGeneration(
			EventSupportGenerationVO esgVOToUpdate) {
		try {
			this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTGENERATION);
			esgVOToUpdate.validate();			
			esgVOToUpdate = (EventSupportGenerationVO) getMasterDataFacade().modify(NuclosEntity.EVENTSUPPORTGENERATION.getEntityName(), MasterDataWrapper.wrapEventSupportGenerationVO(esgVOToUpdate), null);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} 	
		return esgVOToUpdate;
	}
	
	@Override
	public void deleteEventSupportGeneration(EventSupportGenerationVO esgVOToDelete) {
		try {
			this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTGENERATION);
			esgVOToDelete.validate();			
			getMasterDataFacade().remove(NuclosEntity.EVENTSUPPORTGENERATION.getEntityName(), MasterDataWrapper.wrapEventSupportGenerationVO(esgVOToDelete), false);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} 	
	}

	public void forceEventSupportCompilation() {
		try {
			this.ccm.getNuclosJavaCompilerComponent().forceCompile();
			this.ccm.getClassLoader();
		} catch (NuclosCompileException e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
