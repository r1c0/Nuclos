package org.nuclos.server.eventsupport.ejb3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.EventSupportCache;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.customcode.CustomCodeManager;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeRemote;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
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
	
	public EventSupportEventVO create(EventSupportEventVO esevo, DependantMasterDataMap mpDependants) 
			throws CommonCreateException, CommonFinderException, CommonRemoveException, CommonValidationException, 
			CommonStaleVersionException, NuclosCompileException, CommonPermissionException {
		
		EventSupportEventVO retVal = null;
		
		this.checkWriteAllowed(NuclosEntity.EVENTSUPPORTEVENT);
		//check layout for validity
		esevo.validate();	//throws CommonValidationException

		try {
			
			MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.EVENTSUPPORTEVENT.getEntityName(), MasterDataWrapper.wrapEventSupportEventVO(esevo), mpDependants);

			retVal = MasterDataWrapper.getEventSupportEventVO(mdVO);
		}
		catch (NuclosBusinessRuleException ex) {
			throw new CommonFatalException(ex);
		}
		
		return retVal;
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
			retVal.add(new EventSupportVO(c.getSimpleName(), c.getSimpleName(), c.getName(), c.getSimpleName()));
		}
		return retVal;
	}
	
}
