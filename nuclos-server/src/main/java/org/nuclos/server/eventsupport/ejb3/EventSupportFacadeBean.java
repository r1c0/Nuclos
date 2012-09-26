package org.nuclos.server.eventsupport.ejb3;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.api.EntityObject;
import org.nuclos.api.State;
import org.nuclos.api.annotation.NuclosEventType;
import org.nuclos.api.eventsupport.CustomSupport;
import org.nuclos.api.eventsupport.DeleteFinalSupport;
import org.nuclos.api.eventsupport.DeleteSupport;
import org.nuclos.api.eventsupport.GenerateEventObject;
import org.nuclos.api.eventsupport.GenerateSupport;
import org.nuclos.api.eventsupport.InsertFinalSupport;
import org.nuclos.api.eventsupport.InsertSupport;
import org.nuclos.api.eventsupport.JobEventObject;
import org.nuclos.api.eventsupport.JobSupport;
import org.nuclos.api.eventsupport.StateChangeEventObject;
import org.nuclos.api.eventsupport.StateChangeSupport;
import org.nuclos.api.eventsupport.UpdateFinalSupport;
import org.nuclos.api.eventsupport.UpdateSupport;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.PropertiesMap;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.api.EntityObjectImpl;
import org.nuclos.common.api.StateImpl;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.api.eventsupport.CustomEventObjectImpl;
import org.nuclos.server.api.eventsupport.DeleteEventObjectImpl;
import org.nuclos.server.api.eventsupport.GenerateEventObjectImpl;
import org.nuclos.server.api.eventsupport.InsertEventObjectImpl;
import org.nuclos.server.api.eventsupport.StateChangeEventObjectImpl;
import org.nuclos.server.api.eventsupport.TimelimitEventObjectImpl;
import org.nuclos.server.api.eventsupport.UpdateEventObjectImpl;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.StateCache;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.customcode.CustomCodeManager;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbJoin;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportGenerationVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportJobVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTypeVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.genericobject.ejb3.GeneratorFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVOImpl;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.statemodel.ejb3.StateFacadeLocal;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for event support management. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional(noRollbackFor = { Exception.class })
public class EventSupportFacadeBean extends NuclosFacadeBean implements
		EventSupportFacadeRemote {

	private static final Logger LOG = Logger
			.getLogger(EventSupportFacadeBean.class);

	private CustomCodeManager ccm;

	private MasterDataFacadeLocal masterDataFacade;

	public EventSupportFacadeBean() {
	}

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

	public RuleObjectContainerCVO fireStateTransitionEventSupport(
			Integer iSourceStateId, Integer iTargetStateId,
			RuleObjectContainerCVO loccvoBefore, boolean b)
			throws NuclosBusinessRuleException {
		RuleObjectContainerCVO retVal = loccvoBefore;

		StateFacadeLocal facade = ServerServiceLocator.getInstance().getFacade(
				StateFacadeLocal.class);
		Map<String, Object> cache = new HashMap<String, Object>();

		StateTransitionVO stVO = (iSourceStateId == null) ? facade
				.findStateTransitionByNullAndTargetState(iTargetStateId)
				: facade.findStateTransitionBySourceAndTargetState(
						iSourceStateId, iTargetStateId);
		StateVO sVOSource = iSourceStateId != null ? StateCache.getInstance()
				.getState(iSourceStateId) : null;
		StateVO sVOTarget = StateCache.getInstance().getState(iTargetStateId);
		try {
			Collection<EventSupportTransitionVO> eventSupportsByTransitionId = getEventSupportsByTransitionId(stVO
					.getId());
			String sEntityName = MetaDataServerProvider
					.getInstance()
					.getEntity(
							IdUtils.toLongId(loccvoBefore.getGenericObject()
									.getModuleId())).getEntity();
			EntityObjectVO eoVO = DalSupportForGO
					.wrapGenericObjectVO(loccvoBefore.getGenericObject());
			for (EventSupportTransitionVO estVO : eventSupportsByTransitionId) {

				StateImpl stateSource = null;

				if (iSourceStateId != null) {
					stateSource = new StateImpl();
					stateSource.setId(IdUtils.toLongId(sVOSource.getId()));
					stateSource.setName(sVOSource.getStatename());
					stateSource.setNumeral(sVOSource.getNumeral());
				}

				StateImpl stateTarget = new StateImpl();
				stateTarget.setId(IdUtils.toLongId(sVOTarget.getId()));
				stateTarget.setName(sVOTarget.getStatename());
				stateTarget.setNumeral(sVOTarget.getNumeral());
				EntityObject eo = new EntityObjectImpl(eoVO);

				StateChangeEventObject stObject = new StateChangeEventObjectImpl(
						cache, sEntityName, eo, (State) stateSource,
						(State) stateTarget);
				StateChangeSupport loadedStateChangeSupport = (StateChangeSupport) this.ccm
						.getClassLoader()
						.loadClass(estVO.getEventSupportClass()).newInstance();
				loadedStateChangeSupport.stateChange(stObject);

			}

		} catch (Exception e) {
			throw new NuclosBusinessRuleException();
		}

		return retVal;
	}

	public Collection<EventSupportTransitionVO> getEventSupportsByTransitionId(
			Integer transId) throws CommonFinderException,
			CommonPermissionException {

		final List<EventSupportTransitionVO> evtSupps = new ArrayList<EventSupportTransitionVO>();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_EVENTSUPPORT_TRANSITION").alias(
				SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("INTORDER", Integer.class),
				t.baseColumn("STREVENTSUPPORTCLASS", String.class),
				t.baseColumn("BLNRUNAFTERWARDS", Boolean.class),
				t.baseColumn("DATCREATED", java.sql.Date.class),
				t.baseColumn("STRCREATED", String.class),
				t.baseColumn("DATCHANGED", java.sql.Date.class),
				t.baseColumn("STRCHANGED", String.class),
				t.baseColumn("INTVERSION", Integer.class),
				t.baseColumn("INTID", Integer.class),
				t.baseColumn("STREVENTSUPPORTTYPE", String.class));
		query.where(builder.equal(
				t.baseColumn("INTID_T_MD_STATE_TRANSITION", Integer.class),
				transId));
		query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));

		for (DbTuple res : dataBaseHelper.getDbAccess().executeQuery(query)) {

			evtSupps.add(new EventSupportTransitionVO(new NuclosValueObject(res
					.get(8, Integer.class), new Date(res.get(3,
					java.sql.Date.class).getTime()), res.get(4, String.class),
					new Date(res.get(5, java.sql.Date.class).getTime()), res
							.get(6, String.class), res.get(7, Integer.class)),
					res.get(1, String.class), res.get(9, String.class),
					transId, res.get(0, Integer.class), res.get(2,
							Boolean.class)));
		}

		return evtSupps;
	}

	public Collection<EventSupportSourceVO> getAllEventSupports()
			throws CommonPermissionException {
		return this.ccm.getExecutableEventSupportFiles();
	}

	public List<EventSupportJobVO> getAllEventSupportsForJob(Integer jobId)
			throws CommonPermissionException {

		List<EventSupportJobVO> result = new ArrayList<EventSupportJobVO>();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("V_MD_EVENTSUPPORT_JOB").alias(
				SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("INTID", Integer.class),
				t.baseColumn("INTORDER", Integer.class),
				t.baseColumn("STREVENTSUPPORTCLASS", String.class),
				t.baseColumn("INTID_T_MD_JOBCONTROLLER", Integer.class),
				t.baseColumn("STRDESCRIPTION", String.class),
				t.baseColumn("DATCREATED", java.sql.Date.class),
				t.baseColumn("STRCREATED", String.class),
				t.baseColumn("DATCHANGED", java.sql.Date.class),
				t.baseColumn("STRCHANGED", String.class),
				t.baseColumn("INTVERSION", Integer.class),
				t.baseColumn("STREVENTSUPPORTTYPE", String.class));
		query.where(builder.equal(
				t.baseColumn("INTID_T_MD_JOBCONTROLLER", Integer.class), jobId));
		query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));

		for (DbTuple res : dataBaseHelper.getDbAccess().executeQuery(query)) {
			Integer id = res.get(0, Integer.class);
			Integer order = res.get(1, Integer.class);
			String esClass = res.get(2, String.class);
			Integer jobcontroller = res.get(3, Integer.class);
			String description = res.get(4, String.class);
			Date dCreated = new Date(res.get(5, java.sql.Date.class).getTime());
			String sCreated = res.get(6, String.class);
			Date dChanged = new Date(res.get(7, java.sql.Date.class).getTime());
			String sChanged = res.get(8, String.class);
			Integer version = res.get(9, Integer.class);
			String esType = res.get(10, String.class);
			NuclosValueObject nvo = new NuclosValueObject(id, dCreated,
					sCreated, dChanged, sChanged, version);
			result.add(new EventSupportJobVO(nvo, description, esClass, esType,
					order, jobcontroller));
		}
		return result;

	}

	private EventSupportSourceVO getEventSupportByClassname(String classname) {

		EventSupportSourceVO retVal = null;

		for (EventSupportSourceVO esVO : this.ccm
				.getExecutableEventSupportFiles()) {
			if (esVO.getClassname().equals(classname)) {
				retVal = esVO;
				break;
			}
		}

		return retVal;
	}

	public List<EventSupportEventVO> getEventSupportEntitiesByClassname(
			String classname) {
		List<EventSupportEventVO> retVal = new ArrayList<EventSupportEventVO>();

		List<EntityObjectVO> dependantMasterData = NucletDalProvider
				.getInstance()
				.getEntityObjectProcessor(NuclosEntity.EVENTSUPPORTENTITY)
				.getBySearchExpression(
						new CollectableSearchExpression(SearchConditionUtils
								.newEOComparison(
										NuclosEntity.EVENTSUPPORTENTITY
												.getEntityName(),
										"eventsupportclass",
										ComparisonOperator.EQUAL, classname,
										MetaDataServerProvider.getInstance())));

		for (EntityObjectVO mdVO : dependantMasterData) {
			retVal.add(MasterDataWrapper.getEventSupportEventVO(mdVO));
		}

		return retVal;
	}

	public List<JobVO> getJobsByClassname(String classname) {
		List<JobVO> retVal = new ArrayList<JobVO>();

		try {
			List<EntityObjectVO> transMDs = NucletDalProvider
					.getInstance()
					.getEntityObjectProcessor(NuclosEntity.EVENTSUPPORTJOB)
					.getBySearchExpression(
							new CollectableSearchExpression(
									SearchConditionUtils.newEOComparison(
											NuclosEntity.EVENTSUPPORTJOB
													.getEntityName(),
											"eventsupportclass",
											ComparisonOperator.EQUAL,
											classname, MetaDataServerProvider
													.getInstance())));

			for (EntityObjectVO eoVO : transMDs) {
				EventSupportJobVO eventSupportJobVO = MasterDataWrapper
						.getEventSupportJobVO(eoVO);
				MasterDataVO masterDataVO = masterDataFacade.get(
						NuclosEntity.JOBCONTROLLER.getEntityName(),
						eventSupportJobVO.getJobControllerId());
				retVal.add(new JobVO(masterDataVO));
			}

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return retVal;
	}

	public List<GeneratorActionVO> getGenerationsByClassname(String classname) {
		List<GeneratorActionVO> retVal = new ArrayList<GeneratorActionVO>();

		List<EntityObjectVO> lstESGenerations = NucletDalProvider
				.getInstance()
				.getEntityObjectProcessor(NuclosEntity.EVENTSUPPORTGENERATION)
				.getBySearchExpression(
						new CollectableSearchExpression(SearchConditionUtils
								.newEOComparison(
										NuclosEntity.EVENTSUPPORTGENERATION
												.getEntityName(),
										"eventsupportclass",
										ComparisonOperator.EQUAL, classname,
										MetaDataServerProvider.getInstance())));

		try {
			for (EntityObjectVO eoVO : lstESGenerations) {
				EventSupportGenerationVO esgVO = MasterDataWrapper
						.getEventSupportGenerationVO(eoVO);
				MasterDataVO allGenerations = masterDataFacade.get(
						NuclosEntity.GENERATION.getEntityName(),
						esgVO.getGeneration());
				GeneratorActionVO gaVO = MasterDataWrapper
						.getGeneratorActionVO(
								allGenerations,
								ServerServiceLocator
										.getInstance()
										.getFacade(GeneratorFacadeLocal.class)
										.getGeneratorUsages(
												esgVO.getGeneration()));
				retVal.add(gaVO);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}

	public List<StateModelVO> getStateModelsByEventSupportClassname(
			String classname) {
		List<StateModelVO> retVal = new ArrayList<StateModelVO>();

		try {
			List<EntityObjectVO> dependantMasterData = NucletDalProvider
					.getInstance()
					.getEntityObjectProcessor(
							NuclosEntity.EVENTSUPPORTTRANSITION)
					.getBySearchExpression(
							new CollectableSearchExpression(
									SearchConditionUtils.newEOComparison(
											NuclosEntity.EVENTSUPPORTTRANSITION
													.getEntityName(),
											"eventsupportclass",
											ComparisonOperator.EQUAL,
											classname, MetaDataServerProvider
													.getInstance())));

			for (EntityObjectVO mdVO : dependantMasterData) {
				EventSupportTransitionVO estVO = MasterDataWrapper
						.getEventSupportTransitionVO(mdVO);
				MasterDataVO masterDataVO = masterDataFacade.get(
						NuclosEntity.STATETRANSITION.getEntityName(),
						estVO.getTransitionId());
				StateTransitionVO stVO = MasterDataWrapper
						.getStateTransitionVOWithoutDependants(masterDataVO);
				Integer iTargetStateId = stVO.getStateTarget();
				MasterDataVO masterDataStateVO = masterDataFacade.get(
						NuclosEntity.STATE.getEntityName(), iTargetStateId);
				StateVO stateVO = MasterDataWrapper
						.getStateVO(masterDataStateVO);
				MasterDataVO masterDataModelVO = masterDataFacade.get(
						NuclosEntity.STATEMODEL.getEntityName(),
						stateVO.getModelId());

				retVal.add(MasterDataWrapper.getStateModelVO(masterDataModelVO));
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}

	public Collection<EventSupportSourceVO> getEventSupportsByClasstype(
			List<Class<?>> listOfinterfaces) throws CommonPermissionException {

		// List of valdid and available EventSupportClasses that have been found
		// in the classpath
		// and that implement the given interfaces
		return this.ccm
				.getExecutableEventSupportFilesByClassType(listOfinterfaces);
	}

	@Override
	public List<EventSupportTypeVO> getAllEventSupportTypes() throws CommonPermissionException {			

		List<EventSupportTypeVO> retVal = new ArrayList<EventSupportTypeVO>();
		List<Class> registeredSupportEventTypes = this.ccm
				.getRegisteredSupportEventTypes();
		
		for (Class<?> c : registeredSupportEventTypes) {
			List<String> lstOfImports = new ArrayList<String>();
			String sName = c.getSimpleName();
			String sBeschreibung = c.getSimpleName();
			lstOfImports.add(c.getName());
			
			Annotation[] annotations = c.getAnnotations();
			if (annotations.length > 0
					&& annotations[0].annotationType().equals(
							NuclosEventType.class)) {
				sBeschreibung = c.getAnnotation(NuclosEventType.class)
						.description();
				sName = c.getAnnotation(NuclosEventType.class).name();
			}
			List<String> lstMethods = new ArrayList<String>();
			for (Method m: c.getMethods()) {
				String sParameter = "(";
				if (m.getParameterTypes().length > 0) {
					for (int idx=0; idx < m.getParameterTypes().length; idx++) {
						Class<?> cls = m.getParameterTypes()[idx];
						lstOfImports.add(cls.getName());
						sParameter += cls.getSimpleName() + " arg" + idx;
						if (idx < m.getParameterTypes().length - 1)
							sParameter += ",";
					}
				}
				sParameter += ")";
				lstMethods.add("public " + m.getReturnType().getSimpleName() + " " + m.getName() + sParameter);
			}
			String pkgName = c.getPackage() != null ? c.getPackage().getName() : null;
			
			retVal.add(new EventSupportTypeVO(sName, sBeschreibung,
					c.getName(), lstMethods, lstOfImports, pkgName, null));
		}
		return retVal;
	}

	@Override
	public EventSupportEventVO createEventSupportEvent(
			EventSupportEventVO eseVOToInsert)
			throws CommonPermissionException, CommonValidationException,
			NuclosBusinessRuleException, CommonCreateException {

		this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTENTITY);

		eseVOToInsert.validate();
		MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.EVENTSUPPORTENTITY.getEntityName(), MasterDataWrapper.wrapEventSupportEventVO(eseVOToInsert), null, null);

		return MasterDataWrapper.getEventSupportEventVO(mdVO);
	}

	public EventSupportEventVO modifyEventSupportEvent(
			EventSupportEventVO eseVOToUpdate) {
		try {
			this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTENTITY);
			eseVOToUpdate.validate();
			getMasterDataFacade().modify(NuclosEntity.EVENTSUPPORTENTITY.getEntityName(), MasterDataWrapper.wrapEventSupportEventVO(eseVOToUpdate), null, null);
			eseVOToUpdate = MasterDataWrapper.getEventSupportEventVO(
					getMasterDataFacade().get(NuclosEntity.EVENTSUPPORTENTITY.getEntityName(), eseVOToUpdate.getId()));

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return eseVOToUpdate;
	}

	public void deleteEventSupportEvent(EventSupportEventVO eseVOToUpdate) {
		try {
			this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTENTITY);
			eseVOToUpdate.validate();			
			getMasterDataFacade().remove(NuclosEntity.EVENTSUPPORTENTITY.getEntityName(), MasterDataWrapper.wrapEventSupportEventVO(eseVOToUpdate), false, null);

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public List<ProcessVO> getProcessesByModuleId(Integer moduleId) {
		List<ProcessVO> retVal = new ArrayList<ProcessVO>();

		Collection<MasterDataVO> dependantMasterData = masterDataFacade
				.getDependantMasterData(NuclosEntity.PROCESS.getEntityName(),
						"module", moduleId);
		for (MasterDataVO mdVO : dependantMasterData) {
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
		MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.EVENTSUPPORTTRANSITION.getEntityName(), MasterDataWrapper.wrapEventSupportTransitionVO(eseVOToInsert), null, null);

		return MasterDataWrapper.getEventSupportTransitionVO(mdVO);
	}

	@Override
	public EventSupportTransitionVO modifyEventSupportTransition(
			EventSupportTransitionVO eseVOToUpdate) {
		try {
			this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTTRANSITION);
			eseVOToUpdate.validate();			
			eseVOToUpdate = (EventSupportTransitionVO) getMasterDataFacade().modify(NuclosEntity.EVENTSUPPORTTRANSITION.getEntityName(), MasterDataWrapper.wrapEventSupportTransitionVO(eseVOToUpdate), null, null);

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
			getMasterDataFacade().remove(NuclosEntity.EVENTSUPPORTTRANSITION.getEntityName(), MasterDataWrapper.wrapEventSupportTransitionVO(eseVOToUpdate), false, null);

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void deleteEventSupportEvents(Integer entityId,
			String eventSupportType) throws CommonFinderException,
			CommonPermissionException, NuclosBusinessRuleException,
			CommonRemoveException, CommonStaleVersionException {

		Collection<MasterDataVO> mdVOList = getMasterDataFacade()
				.getDependantMasterData(
						NuclosEntity.EVENTSUPPORTENTITY.getEntityName(),
						"entity", entityId);
		for (MasterDataVO mdvo : mdVOList) {
			if (eventSupportType.equals(mdvo.getField("eventsupporttype").toString()))
				getMasterDataFacade().remove(NuclosEntity.EVENTSUPPORTENTITY.getEntityName(), mdvo, false, null);
		}		

	}

	public List<EventSupportEventVO> getAllEventSupportsForEntity(
			Integer entityId) throws CommonPermissionException {

		List<EventSupportEventVO> result = new ArrayList<EventSupportEventVO>();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("V_MD_EVENTSUPPORT_ENTITY").alias(
				SystemFields.BASE_ALIAS);
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
		query.where(builder.equal(
				t.baseColumn("INTID_T_MD_ENTITY", Integer.class), entityId));
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
			String strState = res.get(9, String.class);
			Date dCreated = new Date(res.get(10, java.sql.Date.class).getTime());
			String sCreated = res.get(11, String.class);
			Date dChanged = new Date(res.get(12, java.sql.Date.class).getTime());
			String sChanged = res.get(13, String.class);
			Integer version = res.get(14, Integer.class);

			EventSupportEventVO retVal = new EventSupportEventVO(
					new NuclosValueObject(id, dCreated, sCreated, dChanged,
							sChanged, version), esClass, esType, entityId,
					process, state, order, strEntity, strState, strProcess);

			result.add(retVal);
		}
		return result;
	}


	public EventSupportJobVO createEventSupportJob(EventSupportJobVO esjVOToInsert)
			throws CommonPermissionException, CommonValidationException,
			NuclosBusinessRuleException, CommonCreateException {
		this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTJOB);
		
		esjVOToInsert.validate();
		
		MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.EVENTSUPPORTJOB.getEntityName(),
				MasterDataWrapper.wrapEventSupportJobVO(esjVOToInsert), null, null);
		
		return MasterDataWrapper.getEventSupportJobVO(mdVO);
	}

	@Override
	public EventSupportJobVO modifyEventSupportJob(
			EventSupportJobVO esjVOToUpdate) {
		try {
			this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTJOB);
			esjVOToUpdate.validate();
			esjVOToUpdate = (EventSupportJobVO) getMasterDataFacade().modify(
					NuclosEntity.EVENTSUPPORTJOB.getEntityName(),
					MasterDataWrapper.wrapEventSupportJobVO(esjVOToUpdate),
					null);
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
			getMasterDataFacade().remove(
					NuclosEntity.EVENTSUPPORTJOB.getEntityName(),
					MasterDataWrapper.wrapEventSupportJobVO(esjVOToUpdate),
					false);
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
		DbFrom t = query.from("V_MD_EVENTSUPPORT_GENERATION").alias(
				SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("INTID", Integer.class),
				t.baseColumn("BLNRUNAFTERWARDS", Boolean.class),
				t.baseColumn("INTORDER", Integer.class),
				t.baseColumn("STREVENTSUPPORTCLASS", String.class),
				t.baseColumn("INTID_T_MD_GENERATION", Integer.class),
				t.baseColumn("DATCREATED", java.sql.Date.class),
				t.baseColumn("STRCREATED", String.class),
				t.baseColumn("DATCHANGED", java.sql.Date.class),
				t.baseColumn("STRCHANGED", String.class),
				t.baseColumn("INTVERSION", Integer.class),
				t.baseColumn("STREVENTSUPPORTTYPE", String.class));
		query.where(builder.equal(
				t.baseColumn("INTID_T_MD_GENERATION", Integer.class), genId));
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
			String classtype = res.get(10, String.class);
			EventSupportGenerationVO retVal = new EventSupportGenerationVO(
					new NuclosValueObject(id, dCreated, sCreated, dChanged,
							sChanged, version), order, iGeneration, esClass,
					classtype, bRunAfterwards);

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
				MasterDataWrapper.wrapEventSupportGenerationVO(esgVOToInsert), null, null);
		
		return MasterDataWrapper.getEventSupportGenerationVO(mdVO);
	}

	@Override
	public EventSupportGenerationVO modifyEventSupportGeneration(
			EventSupportGenerationVO esgVOToUpdate) {
		try {
			this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTGENERATION);

			esgVOToUpdate.validate();			
			esgVOToUpdate = (EventSupportGenerationVO) getMasterDataFacade().modify(NuclosEntity.EVENTSUPPORTGENERATION.getEntityName(), MasterDataWrapper.wrapEventSupportGenerationVO(esgVOToUpdate), null, null);

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return esgVOToUpdate;
	}

	@Override
	public void deleteEventSupportGeneration(
			EventSupportGenerationVO esgVOToDelete) {
		try {
			this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTGENERATION);

			esgVOToDelete.validate();			
			getMasterDataFacade().remove(NuclosEntity.EVENTSUPPORTGENERATION.getEntityName(), MasterDataWrapper.wrapEventSupportGenerationVO(esgVOToDelete), false, null);

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

	public Collection<RuleVO> findEventSupportsByUsageAndEntity(
			String sEventclass, UsageCriteria usagecriteria) {

		List<RuleVO> ruleVOs = new ArrayList<RuleVO>();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		
		DbFrom t = query.from("V_MD_EVENTSUPPORT_ENTITY").alias(
				SystemFields.BASE_ALIAS);

		query.select(t.baseColumn("STREVENTSUPPORTCLASS", String.class));
		query.where(builder.and(builder.equal(
				t.baseColumn("INTID_T_MD_ENTITY", Integer.class),
				usagecriteria.getModuleId()), builder.equal(
				t.baseColumn("STREVENTSUPPORTTYPE", String.class), sEventclass)));

		DbColumnExpression<Integer> cp = t.baseColumn("INTID_T_MD_PROCESS",
				Integer.class);
		final Integer iProcessId = usagecriteria.getProcessId();
		if (iProcessId == null) {
			query.addToWhereAsAnd(cp.isNull());
		} else {
			query.addToWhereAsAnd(builder.or(cp.isNull(),
					builder.equal(cp, iProcessId)));
		}

		DbColumnExpression<Integer> cs = t.baseColumn("INTID_T_MD_STATE",
				Integer.class);
		final Integer iStatusId = usagecriteria.getStatusId();
		if (iStatusId == null) {
			query.addToWhereAsAnd(cs.isNull());
		} else {
			query.addToWhereAsAnd(builder.or(cs.isNull(),
					builder.equal(cs, iStatusId)));
		}

		query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));

		List<String> collUsableRuleIds = dataBaseHelper.getDbAccess()
				.executeQuery(query);

		for (String esClassName : collUsableRuleIds) {
			EventSupportSourceVO essVO = getEventSupportByClassname(esClassName);
			ruleVOs.add(new RuleVO(essVO.getName(), essVO.getDescription(),
					essVO.getClassname(), null, true));
		}

		return ruleVOs;
	}

	private void executeCustomSupportEvent(Map<String, Object> cache,
			String entity, String eventSupportClass, EntityObjectVO eoVO) {

		CustomEventObjectImpl ctx = new CustomEventObjectImpl(cache, entity,
				new EntityObjectImpl(eoVO));
		try {
			CustomSupport loadedCustomSupport = (CustomSupport) this.ccm
					.getClassLoader().loadClass(eventSupportClass)
					.newInstance();
			loadedCustomSupport.custom(ctx);

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private EntityObjectVO executeInsertSupportEvent(Map<String, Object> cache,
			String entity, String eventSupportClass, EntityObjectVO eoVO,
			boolean isFinal) {
		EntityObjectVO retVal = eoVO;

		EntityObjectImpl eo = new EntityObjectImpl(eoVO);
		InsertEventObjectImpl ctx = new InsertEventObjectImpl(cache, entity, eo);
		try {
			if (isFinal) {
				InsertFinalSupport loadedInsertFinalSupport = (InsertFinalSupport) this.ccm
						.getClassLoader().loadClass(eventSupportClass)
						.newInstance();
				loadedInsertFinalSupport.insertFinal(ctx);
			} else {
				InsertSupport loadedInsertSupport = (InsertSupport) this.ccm
						.getClassLoader().loadClass(eventSupportClass)
						.newInstance();
				loadedInsertSupport.insert(ctx);
			}
			retVal = eo.getEntityObjectVO();

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}

	private EntityObjectVO executeUpdateSupportEvent(Map<String, Object> cache,
			String entity, String eventSupportClass, EntityObjectVO eoVO,
			boolean isFinal) {
		EntityObjectVO retVal = null;
		EntityObjectImpl eo = new EntityObjectImpl(eoVO);
		UpdateEventObjectImpl ctx = new UpdateEventObjectImpl(cache, entity, eo);
		try {
			if (isFinal) {
				UpdateFinalSupport loadedUpdateFinalSupport = (UpdateFinalSupport) this.ccm
						.getClassLoader().loadClass(eventSupportClass)
						.newInstance();
				loadedUpdateFinalSupport.updateFinal(ctx);
			} else {
				UpdateSupport loadedUpdateSupport = (UpdateSupport) this.ccm
						.getClassLoader().loadClass(eventSupportClass)
						.newInstance();
				loadedUpdateSupport.update(ctx);
			}
			retVal = eo.getEntityObjectVO();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}

	private void executeDeleteSupportEvent(Map<String, Object> cache,
			String entity, String eventSupportClass, EntityObjectVO eoVO,
			boolean isFinal, boolean isLogical) {

		DeleteEventObjectImpl ctx = new DeleteEventObjectImpl(cache, entity,
				new EntityObjectImpl(eoVO), isLogical);
		try {
			if (isFinal) {
				DeleteFinalSupport loadedDeleteFinalSupport = (DeleteFinalSupport) this.ccm
						.getClassLoader().loadClass(eventSupportClass)
						.newInstance();
				loadedDeleteFinalSupport.deleteFinal(ctx);
			} else {
				DeleteSupport loadedDeleteSupport = (DeleteSupport) this.ccm
						.getClassLoader().loadClass(eventSupportClass)
						.newInstance();
				loadedDeleteSupport.delete(ctx);
			}

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public void fireGenerationEventSupport(Integer id,
			RuleObjectContainerCVO loccvoTargetBeforeRules,
			Collection<RuleObjectContainerCVO> loccvoSourceObjects,
			RuleObjectContainerCVO loccvoParameter, List<String> lstActions,
			PropertiesMap properties, Boolean after) {

		Map<String, Object> cache = new HashMap<String, Object>();
		try {
			List<EventSupportGenerationVO> eventSupportsByGenerationId = getEventSupportsByGenerationId(id);

			EntityObjectVO eoVOTarget = DalSupportForGO
					.wrapGenericObjectVO(loccvoTargetBeforeRules
							.getGenericObject());
			EntityObjectVO eoVOParameter = DalSupportForGO
					.wrapGenericObjectVO(loccvoParameter.getGenericObject());
		
			Collection<EntityObject> lstEoVOsSource = new HashSet<EntityObject>();
			for (RuleObjectContainerCVO roCVO : loccvoSourceObjects) {
				lstEoVOsSource.add(new EntityObjectImpl(DalSupportForGO
						.wrapGenericObjectVO(roCVO.getGenericObject())));
			}
			
			for (EventSupportGenerationVO esgVO : eventSupportsByGenerationId) {
				GenerateEventObject geObj = new GenerateEventObjectImpl(
						cache, null, lstEoVOsSource,
						new EntityObjectImpl(eoVOTarget),
						new EntityObjectImpl(eoVOParameter));
				GenerateSupport genSupport = (GenerateSupport) this.ccm
						.getClassLoader()
						.loadClass(esgVO.getEventSupportClass())
						.newInstance();
				genSupport.generate(geObj);
				
			}

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public RuleObjectContainerCVO fireSaveEventSupport(Integer iEntityId,
			String sEventSupportType, RuleObjectContainerCVO loccvoCurrent) {
		final RuleObjectContainerCVO retVal = loccvoCurrent;
		if (iEntityId == null) {
			throw new NullArgumentException("iEntityId");
		}
		try {

			UsageCriteria usagecriteria;
			if (loccvoCurrent.getMasterData() != null) {
				usagecriteria = new UsageCriteria(iEntityId, null, null, null);
			} else {
				Integer iProcessId = loccvoCurrent.getGenericObject()
						.getProcessId();
				Integer iStatusId = loccvoCurrent.getGenericObject()
						.getStatusId();
				if (iStatusId == null) {
					StateFacadeLocal facade = ServerServiceLocator
							.getInstance().getFacade(StateFacadeLocal.class);
					iStatusId = facade.getInitialState(
							new UsageCriteria(loccvoCurrent.getGenericObject()
									.getModuleId(), iProcessId,null, null)).getId();
				}
				usagecriteria = new UsageCriteria(loccvoCurrent
						.getGenericObject().getModuleId(), iProcessId,
						iStatusId, null);
			}

			List<EventSupportEventVO> allEventSupportsForEntity = getAllEventSupportsForEntity(iEntityId);
			Collection<RuleVO> lstRules = findEventSupportsByUsageAndEntity(
					sEventSupportType, usagecriteria);
			Map<String, Object> cache = new HashMap<String, Object>();

			String sEntityName = MetaDataServerProvider
					.getInstance()
					.getEntity(
							IdUtils.toLongId(loccvoCurrent.getGenericObject()
									.getModuleId())).getEntity();
			for (RuleVO rVO : lstRules) {
				if (rVO.getId() == null) {
					EventSupportSourceVO eseVO = getEventSupportByClassname(rVO
							.getSource());
					EntityObjectVO eoVO = DalSupportForGO
							.wrapGenericObjectVO(loccvoCurrent
									.getGenericObject());

					if (InsertSupport.name.equals(sEventSupportType)) {
						eoVO = executeInsertSupportEvent(cache, sEntityName,
								eseVO.getClassname(), eoVO, false);
					} else if (InsertFinalSupport.name
							.equals(sEventSupportType)) {
						eoVO = executeInsertSupportEvent(cache, sEntityName,
								eseVO.getClassname(), eoVO, true);
					} else if (UpdateSupport.name.equals(sEventSupportType)) {
						eoVO = executeUpdateSupportEvent(cache, sEntityName,
								eseVO.getClassname(), eoVO, false);
					} else if (UpdateFinalSupport.name
							.equals(sEventSupportType)) {
						eoVO = executeUpdateSupportEvent(cache, sEntityName,
								eseVO.getClassname(), eoVO, true);
					}
					// Dont set a new instance of generic object because of
					// 'loccvoCurrent' - given as parameter looses data with new reference
					mergeGenericObjects(
							DalSupportForGO.getGenericObjectVO(eoVO),
							loccvoCurrent.getGenericObject());
				}
			}

		} catch (CommonPermissionException e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}

	private void mergeGenericObjects(GenericObjectVO source,
			GenericObjectVO target) {
		// Attributes
		target.getAttributes().clear();
		for (DynamicAttributeVO dynAtt : source.getAttributes()) {
			target.addAndSetAttribute(dynAtt);
		}
	}

	public void fireDeleteEventSupport(Integer iEntityId,
			String sEventSupportType, UsageCriteria usagecriteria,
			RuleObjectContainerCVO loccvoCurrent, boolean isLogical) {
		if (iEntityId == null) {
			throw new NullArgumentException("iEntityId");
		}
		try {
			List<EventSupportEventVO> allEventSupportsForEntity = getAllEventSupportsForEntity(iEntityId);
			Collection<RuleVO> lstRules = findEventSupportsByUsageAndEntity(
					sEventSupportType, usagecriteria);
			Map<String, Object> cache = new HashMap<String, Object>();

			String sEntityName = MetaDataServerProvider
					.getInstance()
					.getEntity(
							IdUtils.toLongId(loccvoCurrent.getGenericObject()
									.getModuleId())).getEntity();
			for (RuleVO rVO : lstRules) {
				if (rVO.getId() == null) {
					EventSupportSourceVO eseVO = getEventSupportByClassname(rVO
							.getSource());
					EntityObjectVO eoVO = DalSupportForGO
							.wrapGenericObjectVO(loccvoCurrent
									.getGenericObject());

					if (DeleteSupport.name.equals(sEventSupportType)) {
						executeDeleteSupportEvent(cache, sEntityName,
								eseVO.getClassname(), eoVO, false, isLogical);
					} else if (DeleteFinalSupport.name
							.equals(sEventSupportType)) {
						executeDeleteSupportEvent(cache, sEntityName,
								eseVO.getClassname(), eoVO, true, isLogical);
					}
				}
			}

		} catch (CommonPermissionException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public void fireTimelimitEventSupport(Integer jobId) {
		try {
			List<EventSupportJobVO> allEventSupportsForJob = getAllEventSupportsForJob(jobId);
			for (EventSupportJobVO esjVO : allEventSupportsForJob) {
				JobEventObject tleObj = new TimelimitEventObjectImpl(null);
				JobSupport genSupport = (JobSupport) this.ccm.getClassLoader()
						.loadClass(esjVO.getEventSupportClass()).newInstance();
				genSupport.executeTimelimit(tleObj);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public RuleObjectContainerCVO fireCustomEventSupport(Integer iEntityId,
			List<RuleVO> lstRules, RuleObjectContainerCVO loccvoCurrent,
			boolean bIgnoreExceptions) {
		Map<String, Object> cache = new HashMap<String, Object>();
		String sEntityName = MetaDataServerProvider
				.getInstance()
				.getEntity(
						IdUtils.toLongId(loccvoCurrent.getGenericObject()
								.getModuleId())).getEntity();

		for (RuleVO rVO : lstRules) {
			if (rVO.getId() == null) {
				EventSupportSourceVO eseVO = getEventSupportByClassname(rVO
						.getSource());
				EntityObjectVO eoVO = DalSupportForGO
						.wrapGenericObjectVO(loccvoCurrent.getGenericObject());

				executeCustomSupportEvent(cache, sEntityName,
						CustomSupport.name, eoVO);
			}
		}
		return loccvoCurrent;
	}

	public Map<String, List<EventSupportGenerationVO>> getGenerationsByUsage() {
		
		Map<String, List<EventSupportGenerationVO>> retVal = new HashMap<String, List<EventSupportGenerationVO>>(); 
		
		try {
		
			DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<DbTuple> query = builder.createTupleQuery();
			
			DbFrom join = query.from("V_MD_EVENTSUPPORT_GENERATION").alias(SystemFields.BASE_ALIAS);
			DbJoin t = join.leftJoin("V_MD_GENERATION").alias("gg").on("INTID_T_MD_GENERATION","INTID", Long.class);
				query.multiselect(t.baseColumn("STRVALUE_T_MD_NUCLET", String.class), 
								  join.baseColumn("INTORDER", Integer.class),
								  join.baseColumn("INTID_T_MD_GENERATION", Integer.class),
								  join.baseColumn("STREVENTSUPPORTCLASS", String.class),
								  join.baseColumn("STREVENTSUPPORTTYPE", String.class),
								  join.baseColumn("BLNRUNAFTERWARDS", Boolean.class));
		
			for (DbTuple res : dataBaseHelper.getDbAccess().executeQuery(query)) {
				String  sNuclet = res.get(0, String.class) != null? res.get(0, String.class) : "<Default>";
				Integer iOrder =  res.get(1, Integer.class);
				Integer iGeneration =  res.get(2, Integer.class);
				String  sEventSupportClass = res.get(3, String.class);
				String  sEventSupportType = res.get(4, String.class);
				Boolean bRunAfterwards = res.get(5, Boolean.class);
				
				EventSupportGenerationVO esgVO = 
						new EventSupportGenerationVO(iOrder,iGeneration, sEventSupportClass, sEventSupportType,bRunAfterwards);
				
				if (retVal.containsKey(sNuclet)) {
					retVal.get(sNuclet).add(esgVO);
				}
				else {
					retVal.put(sNuclet, new ArrayList<EventSupportGenerationVO>());
					retVal.get(sNuclet).add(esgVO);
				}
					 
			}
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}
	
	public Map<String, List<EventSupportEventVO>> getEntitiesByUsage() {
		
		Map<String, List<EventSupportEventVO>> retVal = new HashMap<String, List<EventSupportEventVO>>(); 
		
		try {
		
			DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<DbTuple> query = builder.createTupleQuery();
			
			DbFrom join = query.from("V_MD_EVENTSUPPORT_ENTITY").alias(SystemFields.BASE_ALIAS);
			DbJoin t = join.leftJoin("V_MD_ENTITY").alias("gg").on("INTID_T_MD_ENTITY","INTID", Long.class);
				query.multiselect(t.baseColumn("STRVALUE_T_MD_NUCLET", String.class), 
						join.baseColumn("INTORDER", Integer.class),
						join.baseColumn("STREVENTSUPPORTTYPE", String.class),
						join.baseColumn("STREVENTSUPPORTCLASS", String.class),
						join.baseColumn("INTID_T_MD_ENTITY", Integer.class),
						join.baseColumn("INTID_T_MD_PROCESS", Integer.class),
						join.baseColumn("INTID_T_MD_STATE", Integer.class),
						join.baseColumn("STRVALUE_T_MD_ENTITY", String.class),
						join.baseColumn("STRVALUE_T_MD_PROCESS", String.class),
						join.baseColumn("STRVALUE_T_MD_STATE", String.class));
		
			for (DbTuple res : dataBaseHelper.getDbAccess().executeQuery(query)) {
				String  sNuclet = res.get(0, String.class) != null? res.get(0, String.class) : "<Default>";
				Integer iOrder =  res.get(1, Integer.class);
				String  sEventSupportClass = res.get(2, String.class);
				String  sEventSupportType = res.get(3, String.class);
				Integer iEntity =  res.get(4, Integer.class);
				Integer iProcess =  res.get(5, Integer.class);
				Integer iState =  res.get(6, Integer.class);
				String  sEntity =  res.get(7, String.class);
				String  sProcess =  res.get(8, String.class);
				String  sState =  res.get(9, String.class);
				
				EventSupportEventVO esgVO = 
						new EventSupportEventVO(sEventSupportClass, sEventSupportType, iEntity, iProcess, iState, iOrder, sEntity, sState, sProcess);
				
				if (retVal.containsKey(sNuclet)) {
					retVal.get(sNuclet).add(esgVO);
				}
				else {
					retVal.put(sNuclet, new ArrayList<EventSupportEventVO>());
					retVal.get(sNuclet).add(esgVO);
				}
					 
			}
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}

	public Map<String, List<EventSupportTransitionVO>> getTransitionsByUsage() {	
		
		Map<String, List<EventSupportTransitionVO>> retVal = new HashMap<String, List<EventSupportTransitionVO>>(); 
		
		try {
		
			DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<DbTuple> query = builder.createTupleQuery();
			
			DbFrom join = query.from("V_MD_EVENTSUPPORT_TRANSITION").alias(SystemFields.BASE_ALIAS);
			DbJoin t = join.leftJoin("V_MD_STATE_TRANSITION").alias("gg").on("INTID_T_MD_STATE_TRANSITION","INTID", Long.class);
			DbJoin s = t.leftJoin("V_MD_STATE").alias("hh").on("INTID_T_MD_STATE_2","INTID", Long.class);
			DbJoin u = s.leftJoin("V_MD_STATEMODEL").alias("ii").on("INTID_T_MD_STATEMODEL","INTID", Long.class);
				query.multiselect(u.baseColumn("STRVALUE_T_MD_NUCLET", String.class), 
								join.baseColumn("INTORDER", Integer.class),
								join.baseColumn("STREVENTSUPPORTTYPE", String.class),
								join.baseColumn("STREVENTSUPPORTCLASS", String.class),
								join.baseColumn("INTID_T_MD_STATE_TRANSITION", Integer.class),
								join.baseColumn("BLNRUNAFTERWARDS", Boolean.class));
		
			for (DbTuple res : dataBaseHelper.getDbAccess().executeQuery(query)) {
				String  sNuclet = res.get(0, String.class) != null? res.get(0, String.class) : "<Default>";
				Integer iOrder =  res.get(1, Integer.class);
				String  sEventSupportClass = res.get(2, String.class);
				String  sEventSupportType = res.get(3, String.class);
				Integer iStateTransition =  res.get(4, Integer.class);
				Boolean bRunAfterwars =  res.get(5, Boolean.class);
				
				EventSupportTransitionVO esgVO = 
						new EventSupportTransitionVO(sEventSupportClass, sEventSupportType,iStateTransition, iOrder, bRunAfterwars);
				
				if (retVal.containsKey(sNuclet)) {
					retVal.get(sNuclet).add(esgVO);
				}
				else {
					retVal.put(sNuclet, new ArrayList<EventSupportTransitionVO>());
					retVal.get(sNuclet).add(esgVO);
				}
					 
			}
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}

	public Map<String, List<EventSupportJobVO>> getJobsByUsage() {
		
		Map<String, List<EventSupportJobVO>> retVal = new HashMap<String, List<EventSupportJobVO>>(); 
		
		try {
		
			DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<DbTuple> query = builder.createTupleQuery();
			
			DbFrom join = query.from("V_MD_EVENTSUPPORT_JOB").alias(SystemFields.BASE_ALIAS);
			DbJoin t = join.leftJoin("V_MD_JOBCONTROLLER").alias("gg").on("INTID_T_MD_JOBCONTROLLER","INTID", Long.class);
				query.multiselect(t.baseColumn("STRVALUE_T_MD_NUCLET", String.class), 
								  join.baseColumn("INTORDER", Integer.class),
								  join.baseColumn("INTID_T_MD_JOBCONTROLLER", Integer.class),
								  join.baseColumn("STREVENTSUPPORTCLASS", String.class),
								  join.baseColumn("STREVENTSUPPORTTYPE", String.class),
								  t.baseColumn("STRDESCRIPTION", Boolean.class));
		
			for (DbTuple res : dataBaseHelper.getDbAccess().executeQuery(query)) {
				String  sNuclet = res.get(0, String.class) != null? res.get(0, String.class) : "<Default>";
				Integer iOrder =  res.get(1, Integer.class);
				Integer iJob =  res.get(2, Integer.class);
				String  sEventSupportClass = res.get(3, String.class);
				String  sEventSupportType = res.get(4, String.class);
				String  sDescription = res.get(5, String.class);
				
				EventSupportJobVO esgVO = 
						new EventSupportJobVO(sDescription, sEventSupportClass, sEventSupportType,iOrder,iJob);
				
				if (retVal.containsKey(sNuclet)) {
					retVal.get(sNuclet).add(esgVO);
				}
				else {
					retVal.put(sNuclet, new ArrayList<EventSupportJobVO>());
					retVal.get(sNuclet).add(esgVO);
				}
					 
			}
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}

}
