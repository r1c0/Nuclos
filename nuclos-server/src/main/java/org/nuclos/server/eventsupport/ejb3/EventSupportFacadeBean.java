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
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.EventSupportCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.customcode.CustomCodeManager;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.statemodel.ejb3.StateFacadeLocal;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
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
			
			EventSupportVO eventSupport = 
					EventSupportCache.getInstance().getEventSupport(res.get(1, String.class));
	
			evtSupps.add(new EventSupportTransitionVO(
					new NuclosValueObject(
							res.get(8, Integer.class),
							new Date(res.get(3, java.sql.Date.class).getTime()),
							res.get(4, String.class),
							new Date(res.get(5, java.sql.Date.class).getTime()),
							res.get(6, String.class),
							res.get(7, Integer.class)),
					eventSupport.getName(),
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
		final List<EventSupportVO> evtSupps = new ArrayList<EventSupportVO>();

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
			List<EventSupportVO> evtSupps,
			RuleObjectContainerCVO ruleContainer, boolean b) 
	{
		RuleObjectContainerCVO retVal = ruleContainer;
		
		for (EventSupportVO esVO : evtSupps)
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

	public Collection<EventSupportVO> getAllEventSupports() throws CommonPermissionException {
		return this.ccm.getExecutableEventSupportFiles();
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
	    
	private EventSupportVO getEventSupportByClassname(String classname) {
		
		EventSupportVO retVal = null;
		
		for (EventSupportVO esVO : this.ccm.getExecutableEventSupportFiles()) {
			if (esVO.getClassname().equals(classname)) {
				retVal = esVO;
				break;
			}
		}
		
		return retVal;
	}
	
	public Collection<EventSupportVO> getEventSupportsByClasstype(List<Class<?>> listOfinterfaces)
			throws CommonPermissionException {
		
		// List of valdid and available EventSupportClasses that have been found in the classpath
		// and that implement the given interfaces
		return this.ccm.getExecutableEventSupportFilesByClassType(listOfinterfaces);
	}

	@Override
	public List<EventSupportVO> getAllEventSupportTypes()
			throws CommonPermissionException {
		
		List<EventSupportVO> retVal = new ArrayList<EventSupportVO>();
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
			retVal.add(new EventSupportVO(sName, sBeschreibung, c.getName(), c.getSimpleName(), pkgName, null));
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
			Object modify = getMasterDataFacade().modify(NuclosEntity.EVENTSUPPORTEVENT.getEntityName(), MasterDataWrapper.wrapEventSupportEventVO(eseVOToUpdate), null);
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
			getMasterDataFacade().modify(NuclosEntity.EVENTSUPPORTTRANSITION.getEntityName(), MasterDataWrapper.wrapEventSupportTransitionVO(eseVOToUpdate), null);
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
	
	
}
